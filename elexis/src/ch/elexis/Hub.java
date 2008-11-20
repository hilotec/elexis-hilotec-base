/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: Hub.java 4690 2008-11-20 12:40:51Z rgw_ch $
 *******************************************************************************/

package ch.elexis;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.Heartbeat;
import ch.elexis.actions.JobPool;
import ch.elexis.actions.Heartbeat.HeartListener;
import ch.elexis.admin.AccessControl;
import ch.elexis.data.Anwender;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.PersistentObjectFactory;
import ch.elexis.data.Query;
import ch.elexis.data.Reminder;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.preferences.PreferenceInitializer;
import ch.elexis.util.Log;
import ch.elexis.util.PlatformHelper;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.UtilFile;
import ch.rgw.io.FileTool;
import ch.rgw.io.LockFile;
import ch.rgw.io.Settings;
import ch.rgw.io.SqlSettings;
import ch.rgw.io.SysSettings;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionInfo;

/**
 * Diese Klasse ist der OSGi-Activator und steuert somit Start und Ende der Anwendung. Ganz früh
 * (vor dem Initialisieren der anwendung) und ganz spät (unmittelbar vor dem Entfernen der
 * Anwendung) notwendige Schritte müssen hier durchgeführt werden. Ausserdem werden hier globale
 * Variablen und Konstanten angelegt.
 */
public class Hub extends AbstractUIPlugin {
	// Globale Konstanten
	public static final boolean DEBUGMODE = false;
	public static final String PLUGIN_ID = "ch.elexis"; //$NON-NLS-1$
	public static final String COMMAND_PREFIX = PLUGIN_ID + ".commands."; //$NON-NLS-1$
	static final String neededJRE = "1.5.0"; //$NON-NLS-1$
	public static final String Version = "1.4.0"; //$NON-NLS-1$
	public static final String DBVersion = "1.7.2"; //$NON-NLS-1$
	static final String[] mine = {
		"ch.elexis", "ch.rgw"}; //$NON-NLS-1$ //$NON-NLS-2$
	private static List<ShutdownJob> shutdownJobs = new LinkedList<ShutdownJob>();
	
	// Globale Variable
	/** Das Singleton-Objekt dieser Klasse */
	public static Hub plugin;
	
	/** Lokale Einstellungen (Werden in der Registry bzw. ~/.java gespeichert) */
	public static Settings localCfg;
	
	/** Globale Einstellungen (Werden in der Datenbank gespeichert) */
	public static Settings globalCfg;
	
	/** Anwenderspezifische Einstellungen (Werden in der Datenbank gespeichert) */
	public static Settings userCfg;
	
	/** Mandantspezifische EInstellungen (Werden in der Datenbank gespeichert) */
	public static Settings mandantCfg;
	
	/** Zentrale Logdatei */
	public static Log log;
	
	/** Globale Aktionen */
	public static GlobalActions mainActions;
	
	/** Der aktuell angemeldete Anwender */
	public static Anwender actUser;
	
	/** Der Mandant, auf dessen namen die aktuellen Handlungen gehen */
	public static Mandant actMandant;
	
	/** Die zentrale Zugriffskontrolle */
	public static final AccessControl acl = new AccessControl();
	
	/** Der Initialisierer für die Voreinstellungen */
	public static final PreferenceInitializer pin = new PreferenceInitializer();;
	
	/** Hintergrundjobs zum Nachladen von Daten */
	public static final JobPool jobPool = JobPool.getJobPool();;
	
	/** Factory für interne PersistentObjects */
	public static final PersistentObjectFactory poFactory = new PersistentObjectFactory();
	
	/** Heartbeat */
	public static Heartbeat heart;
	
	private static File userDir;
	
	public Hub(){
		plugin = this;
		log = Log.get("Elexis startup"); //$NON-NLS-1$
		getWritableUserDir();
		localCfg = new SysSettings(SysSettings.USER_SETTINGS, Desk.class);
		setUserDir(userDir);
		String[] args = Platform.getApplicationArgs();
		String config = "default";
		for (String s : args) {
			if (s.startsWith("--use-config=")) { //$NON-NLS-1$
				String[] c = s.split("="); //$NON-NLS-1$
				config = c[1];
				localCfg = localCfg.getBranch(config, true);
				break;
			}
		}
		initializeLog(localCfg);
		log.log(Messages.Hub_12 + config, Log.INFOS);
		// Damit Anfragen auf userCfg und mandantCfg bei nicht eingeloggtem User
		// keine NPE werfen
		userCfg = localCfg;
		mandantCfg = localCfg;
		
		String basePath = UtilFile.getFilepath(PlatformHelper.getBasePath("ch.elexis"));
		localCfg.set("elexis-basepath", UtilFile.getFilepath(basePath));
		
		
		// Java Version prüfen
		VersionInfo vI = new VersionInfo(System.getProperty("java.version", "0.0.0")); //$NON-NLS-1$ //$NON-NLS-2$
		log.log("Elexis " + Version + ", build " + getRevision(true) + Messages.Hub_19 + //$NON-NLS-1$ //$NON-NLS-2$
			Messages.Hub_20 + vI.version(), Log.SYNCMARK);
		
		if (vI.isOlder(neededJRE)) {
			String msg = Messages.Hub_21 + neededJRE;
			getLog().log(new Status(Status.ERROR, "ch.elexis", //$NON-NLS-1$
				-1, msg, new Exception(msg)));
			SWTHelper.alert(Messages.Hub_23, msg);
			log.log(msg, Log.FATALS);
		}
		log.log(Messages.Hub_24 + getBasePath(), Log.INFOS);
		pin.initializeDefaultPreferences();
		
	}
	
	/*
	 * called by constructor
	 */
	private void initializeLog(final Settings cfg){
		String logfileName = cfg.get(PreferenceConstants.ABL_LOGFILE, null); //$NON-NLS-1$
		int maxLogfileSize = -1;
		File fLog;
		if (logfileName == null) {
			fLog = new File(userDir, "elexis.log");
		} else {
			fLog = new File(logfileName);
		}
		try {
			String defaultValue = new Integer(Log.DEFAULT_LOGFILE_MAX_SIZE).toString();
			String value = cfg.get(PreferenceConstants.ABL_LOGFILE_MAX_SIZE, defaultValue);
			maxLogfileSize = Integer.parseInt(value.trim());
		} catch (NumberFormatException ex) {
			// do nothing
		}
		Log.setLevel(cfg.get(PreferenceConstants.ABL_LOGLEVEL, Log.ERRORS));
		Log.setOutput(fLog.getAbsolutePath(), maxLogfileSize);
		Log.setAlertLevel(cfg.get(PreferenceConstants.ABL_LOGALERT, Log.ERRORS));
		// Exception handler initialiseren, Output wie log, auf eigene Klassen
		// begrenzen
		ExHandler.setOutput(fLog.getAbsolutePath()); //$NON-NLS-1$
		ExHandler.setClasses(mine);

	}
	
	private void initializeLock(){
		final int timeoutSeconds = 600;
		try {
			final LockFile lockfile = new LockFile(userDir, "elexislock", 4, timeoutSeconds);
			final int n = lockfile.lock();
			if (n == 0) {
				SWTHelper.alert("Zu viele Instanzen",
					"Es können keine weiteren Elexis-Instanzen gestartet werden");
				System.exit(2);
			} else {
				HeartListener lockListener = new HeartListener() {
					long timeSet;
					
					public void heartbeat(){
						long now = System.currentTimeMillis();
						if ((now - timeSet) > timeoutSeconds) {
							lockfile.updateLock(n);
							timeSet = now;
						}
					}
				};
				heart.addListener(lockListener, Heartbeat.FREQUENCY_LOW);
			}
		} catch (IOException ex) {
			log.log("Can not aquire lock file", Log.ERRORS);
		}
	}
	
	public static int getSystemLogLevel(){
		return localCfg.get(PreferenceConstants.ABL_LOGLEVEL, Log.ERRORS);
	}
	
	/**
	 * Hier stehen Aktionen, die ganz früh, noch vor dem Starten der Workbench, durchgeführt werden
	 * sollen.
	 */
	@Override
	public void start(final BundleContext context) throws Exception{
		// log.log("Basedir: "+getBasePath(),Log.DEBUGMSG);
		super.start(context);
		heart = Heartbeat.getInstance();
		initializeLock();
	}
	
	/**
	 * Programmende
	 */
	@Override
	public void stop(final BundleContext context) throws Exception{
		heart.stop();
		JobPool.getJobPool().dispose();
		if (Hub.actUser != null) {
			Anwender.logoff();
		}
		if (globalCfg != null) {
			// We should not flush acl's at this point, since this might
			// overwrite other client's
			// settings
			// acl.flush();
			globalCfg.flush();
		}
		PersistentObject.disconnect();
		globalCfg = null;
		super.stop(context);
		plugin = null;
		if ((shutdownJobs != null) && (shutdownJobs.size() > 0)) {
			Shell shell = new Shell(Display.getDefault());
			MessageDialog dlg =
				new MessageDialog(
					shell,
					"Elexis: Konfiguration",
					Dialog.getDefaultImage(),
					"Bitte schalten Sie den PC nicht aus und warten Sie mit Elexis-Neustart, bis diese Nachricht verschwindet",
					SWT.ICON_INFORMATION, new String[] {}, 0);
			dlg.setBlockOnOpen(false);
			dlg.open();
			for (ShutdownJob job : shutdownJobs) {
				job.doit();
			}
			dlg.close();
		}
	}
	
	public static void setMandant(final Mandant m){
		if (actMandant != null) {
			// Hub.mandantCfg.dump(null);
			mandantCfg.flush();
		}
		if (m == null) {
			if ((mainActions != null) && (mainActions.mainWindow != null)
				&& (mainActions.mainWindow.getShell() != null)) {
				mandantCfg = userCfg;
			}
		} else {
			mandantCfg =
				new SqlSettings(PersistentObject.getConnection(),
					"USERCONFIG", "Param", "Value", "UserID=" + m.getWrappedId()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		actMandant = m;
		setWindowText(null);
		GlobalEvents.getInstance().fireSelectionEvent(Hub.actMandant);
		GlobalEvents.getInstance().fireUpdateEvent(Mandant.class);
		GlobalEvents.getInstance().fireUserEvent();
	}
	
	public static void setWindowText(Patient pat){
		StringBuilder sb = new StringBuilder();
		sb.append("Elexis ").append(Version).append(" - ");
		if (Hub.actUser == null) {
			sb.append("Kein Anwender eingeloggt - ");
		} else {
			sb.append(" ").append(Hub.actUser.getLabel());
		}
		if (Hub.actMandant == null) {
			sb.append(" Kein Mandant ");
			
		} else {
			sb.append(" / ").append(Hub.actMandant.getLabel());
		}
		if (pat == null) {
			pat = GlobalEvents.getSelectedPatient();
		}
		if (pat == null) {
			sb.append("  -  Kein Patient ausgewählt");
		} else {
			String nr = pat.getPatCode();
			String alter = pat.getAlter();
			sb.append("  / ").append(pat.getLabel()).append("(").append(alter).append(") - ")
				.append("[").append(nr).append("]");
			
			if (Reminder.findForPatient(pat, Hub.actUser).size() != 0) {
				sb.append("    *** Reminders *** ");
			}
			String act = new TimeTool().toString(TimeTool.DATE_COMPACT);
			TimeTool ttPatg = new TimeTool();
			if (ttPatg.set(pat.getGeburtsdatum())) {
				String patg = ttPatg.toString(TimeTool.DATE_COMPACT);
				if (act.substring(4).equals(patg.substring(4))) {
					sb.append("   +++ Hat Geburtstag +++  ");
				}
			}
		}
		if (mainActions.mainWindow != null) {
			Shell shell = mainActions.mainWindow.getShell();
			if ((shell != null) && (!shell.isDisposed())) {
				mainActions.mainWindow.getShell().setText(sb.toString());
			}
		}
	}
	
	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(final String path){
		return AbstractUIPlugin.imageDescriptorFromPlugin("ch.elexis", path); //$NON-NLS-1$
	}
	
	public static String getId(){
		return "Elexis v." + Version + ", r." + getRevision(false) + " "
			+ System.getProperty("os.name") + "/" + System.getProperty("os.version");
	}
	
	/**
	 * Revisionsnummer und Erstellungsdatum dieser Instanz ermitteln. Dazu wird die beim letzten
	 * Commit von Subversion geänderte Variable LastChangedRevision untersucht, und fürs Datum das
	 * von ANT beim build eingetragene Datum gesucht. Wenn diese Instanz nicht von ANT erstellt
	 * wurde, handelt es sich um eine Entwicklerversion, welche unter Eclipse-Kontrolle abläuft.
	 */
	public static String getRevision(final boolean withdate){
		String SVNREV = "$LastChangedRevision: 4690 $"; //$NON-NLS-1$
		String res = SVNREV.replaceFirst("\\$LastChangedRevision:\\s*([0-9]+)\\s*\\$", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
		if (withdate == true) {
			File base = new File(getBasePath() + "/rsc/compiletime.txt");
			if (base.canRead()) {
				String dat = FileTool.readFile(base);
				if (dat.equals("@TODAY@")) {
					res += Messages.Hub_38;
				} else {
					res += ", " + new TimeTool(dat + "00").toString(TimeTool.FULL_GER);
				}
			} else {
				res += ",compiletime not known";
			}
		}
		return res;
	}
	
	public static String getBasePath(){
		return PlatformHelper.getBasePath(PLUGIN_ID);
	}
	
	public static List<Anwender> getUserList(){
		Query<Anwender> qbe = new Query<Anwender>(Anwender.class);
		return qbe.execute();
	}
	
	public static List<Mandant> getMandantenList(){
		Query<Mandant> qbe = new Query<Mandant>(Mandant.class);
		return qbe.execute();
	}
	
	public static Shell getActiveShell(){
		if (plugin != null) {
			IWorkbench wb = plugin.getWorkbench();
			if (wb != null) {
				IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
				if (win != null) {
					return win.getShell();
				}
			}
		}
		Display dis = Desk.getDisplay();
		if (dis == null) {
			dis = PlatformUI.createDisplay();
		}
		return new Shell(dis);
	}
	
	/**
	 * A job that executes during sstop() of the plugin (that means after the workbench is shut down
	 * 
	 * @author gerry
	 * 
	 */
	public interface ShutdownJob {
		/**
		 * do whatever you like
		 */
		public void doit() throws Exception;
	}
	
	public static void addShutdownJob(final ShutdownJob job){
		if (!shutdownJobs.contains(job)) {
			shutdownJobs.add(job);
		}
	}
	
	public void setUserDir(File dir){
		userDir = dir;
		localCfg.set("elexis-userDir", dir.getAbsolutePath());
	}
	
	/**
	 * return a directory suitable for plugin specific configuration data
	 * 
	 * @return a directory that exists always and is always writable and readable for plugins of the
	 *         currently running elexis instance
	 */
	public static File getWritableUserDir(){
		if (userDir == null) {
			String userhome = null;
			
			if (localCfg != null) {
				userhome = localCfg.get("elexis-userDir", null);
			}
			if (userhome == null) {
				userhome = System.getProperty("user.home");
			}
			if (StringTool.isNothing(userhome)) {
				userhome = System.getenv("java.io.tempdir");
			}
			userDir = new File(userhome, "elexis");
		}
		if (!userDir.exists()){
			if (!userDir.mkdirs()){
				System.err.print("fatal: could not create Userdir");
				SWTHelper.alert("Panic exit", "could not create userdir "
					+ userDir.getAbsolutePath());
				System.exit(-5);
			}
		}
		return userDir;
	}
	
	/**
	 * Return a directory suitable for temporary files. Most probably this will be a default tempdir
	 * provided by the os. If none such exists, it will be the user dir.
	 * 
	 * @return always a valid and writable directory.
	 */
	public static File getTempDir(){
		File ret = null;
		String temp = System.getProperty("java.io.tmpdir");
		if (!StringTool.isNothing(temp)) {
			ret = new File(temp);
			if (ret.exists() && ret.isDirectory()) {
				return ret;
			} else {
				if (ret.mkdirs()) {
					return ret;
				}
			}
		}
		return getWritableUserDir();
	}
	
}
