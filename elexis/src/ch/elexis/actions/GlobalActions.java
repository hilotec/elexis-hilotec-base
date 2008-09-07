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
 * $Id: GlobalActions.java 4386 2008-09-07 15:53:39Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

import static ch.elexis.admin.AccessControlDefaults.AC_ABOUT;
import static ch.elexis.admin.AccessControlDefaults.AC_CHANGEMANDANT;
import static ch.elexis.admin.AccessControlDefaults.AC_CONNECT;
import static ch.elexis.admin.AccessControlDefaults.AC_EXIT;
import static ch.elexis.admin.AccessControlDefaults.AC_HELP;
import static ch.elexis.admin.AccessControlDefaults.AC_IMORT;
import static ch.elexis.admin.AccessControlDefaults.AC_LOGIN;
import static ch.elexis.admin.AccessControlDefaults.AC_NEWWINDOW;
import static ch.elexis.admin.AccessControlDefaults.AC_PREFS;
import static ch.elexis.admin.AccessControlDefaults.AC_SHOWPERSPECTIVE;
import static ch.elexis.admin.AccessControlDefaults.AC_SHOWVIEW;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.LoginDialog;
import ch.elexis.PatientPerspektive;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.dialogs.DateSelectorDialog;
import ch.elexis.dialogs.NeuerFallDialog;
import ch.elexis.dialogs.SelectFallDialog;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.Importer;
import ch.elexis.util.ResultAdapter;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.TemplateDrucker;
import ch.elexis.views.FallDetailView;
import ch.elexis.wizards.DBConnectWizard;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Diese Klasse definiert alle statischen Actions, die global gelten sollen.
 */
public class GlobalActions {
	// globally used command ids (for key bindings / actions)
	public static final String RENAME_COMMAND = "org.eclipse.ui.edit.rename";
	public static final String DELETE_COMMAND = "org.eclipse.ui.edit.delete";
	public static final String PROPERTIES_COMMAND = "org.eclipse.ui.file.properties";
	public static final String DEFAULTPERSPECTIVECFG = "/default_perspective";
	
	public static IWorkbenchAction exitAction, newWindowAction, copyAction, cutAction, pasteAction;
	public static IAction loginAction, importAction, testAction, aboutAction, helpAction,
			prefsAction;
	public static IAction connectWizardAction, changeMandantAction, savePerspectiveAction,
			savePerspectiveAsAction;
	public static IAction savePerspectiveAsDefaultAction, resetPerspectiveAction, homeAction,
			fixLayoutAction;
	public static IAction printEtikette, printBlatt, printAdresse, printVersionedEtikette;
	public static IAction printRoeBlatt;
	public static IAction delFallAction, delKonsAction, openFallaction, filterAction,
			reopenFallAction, makeBillAction;
	public static IAction moveBehandlungAction, redateAction, neueKonsAction, neuerFallAction;
	
	public static MenuManager perspectiveMenu, viewMenu;
	public static IContributionItem perspectiveList, viewList;
	public IWorkbenchWindow mainWindow;
	public static Action printKontaktEtikette;
	private static IWorkbenchHelpSystem help;
	
	public GlobalActions(final IWorkbenchWindow window){
		if (Hub.mainActions != null) {
			return;
		}
		mainWindow = window;
		help = Hub.plugin.getWorkbench().getHelpSystem();
		exitAction = ActionFactory.QUIT.create(window);
		exitAction.setText(Messages.getString("GlobalActions.MenuExit")); //$NON-NLS-1$
		newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
		newWindowAction.setText(Messages.getString("GlobalActions.NewWindow")); //$NON-NLS-1$
		copyAction = ActionFactory.COPY.create(window);
		copyAction.setText(Messages.getString("GlobalActions.Copy")); //$NON-NLS-1$
		cutAction = ActionFactory.CUT.create(window);
		cutAction.setText(Messages.getString("GlobalActions.Cut")); //$NON-NLS-1$
		pasteAction = ActionFactory.PASTE.create(window);
		pasteAction.setText(Messages.getString("GlobalActions.Paste")); //$NON-NLS-1$
		aboutAction = ActionFactory.ABOUT.create(window);
		aboutAction.setText(Messages.getString("GlobalActions.MenuAbout")); //$NON-NLS-1$
		// helpAction=ActionFactory.HELP_CONTENTS.create(window);
		// helpAction.setText(Messages.getString("GlobalActions.HelpIndex")); //$NON-NLS-1$
		prefsAction = ActionFactory.PREFERENCES.create(window);
		prefsAction.setText(Messages.getString("GlobalActions.Preferences")); //$NON-NLS-1$
		savePerspectiveAction = new Action(Messages.getString("GlobalActions.SavePerspective")) { //$NON-NLS-1$
				{
					setId("savePerspektive"); //$NON-NLS-1$
					// setActionDefinitionId(Hub.COMMAND_PREFIX+"savePerspektive"); //$NON-NLS-1$
					setToolTipText(Messages.getString("GlobalActions.SavePerspectiveToolTip")); //$NON-NLS-1$
					setImageDescriptor(Hub.getImageDescriptor("rsc/save.gif")); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					mainWindow.getActivePage().savePerspective();
				}
			};
		
		helpAction = new Action("Handbuch") {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_BOOK));
				setToolTipText("Handbuch öffnen");
				
			}
			
			@Override
			public void run(){
				File base = new File(Hub.getBasePath()).getParentFile().getParentFile();
				String book = base.getAbsolutePath() + File.separator + "elexis.pdf";
				Program proggie = Program.findProgram(".pdf");
				if (proggie != null) {
					proggie.execute(book);
				} else {
					if (Program.launch(book) == false) {
						
						try {
							Runtime.getRuntime().exec(book);
						} catch (Exception e) {
							ExHandler.handle(e);
						}
					}
				}
			}
		};
		savePerspectiveAsAction = ActionFactory.SAVE_PERSPECTIVE.create(window);
		
		// ActionFactory.SAVE_PERSPECTIVE.create(window);
		resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE.create(window);
		resetPerspectiveAction.setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_REFRESH));
		
		homeAction = new Action(Messages.getString("GlobalActions.Home")) { //$NON-NLS-1$
				{
					setId("home"); //$NON-NLS-1$
					setActionDefinitionId(Hub.COMMAND_PREFIX + "home"); //$NON-NLS-1$
					setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_HOME));
					setToolTipText(Messages.getString("GlobalActions.HomeToolTip")); //$NON-NLS-1$
					help.setHelp(this, "ch.elexis.globalactions.homeAction"); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					// String perspektive=Hub.actUser.getInfoString("StartPerspektive");
					String perspektive =
						Hub.localCfg.get(Hub.actUser + DEFAULTPERSPECTIVECFG, null);
					if (StringTool.isNothing(perspektive)) {
						perspektive = PatientPerspektive.ID;
					}
					try {
						IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						PlatformUI.getWorkbench().showPerspective(perspektive, win);
						// Hub.heart.resume(true);
					} catch (Exception ex) {
						ExHandler.handle(ex);
					}
				}
			};
		savePerspectiveAsDefaultAction = new Action("als Startperspektive speichern") {
			{
				setId("start");
				// setActionDefinitionId(Hub.COMMAND_PREFIX+"startPerspective");
			}
			
			@Override
			public void run(){
				IPerspectiveDescriptor p = mainWindow.getActivePage().getPerspective();
				Hub.localCfg.set(Hub.actUser + DEFAULTPERSPECTIVECFG, p.getId());
				// Hub.actUser.setInfoElement("StartPerspektive",p.getId());
			}
			
		};
		loginAction = new Action(Messages.getString("GlobalActions.Login")) { //$NON-NLS-1$
				{
					setId("login"); //$NON-NLS-1$
					setActionDefinitionId(Hub.COMMAND_PREFIX + "login");} //$NON-NLS-1$
				
				@Override
				public void run(){
					try {
						IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						IWorkbenchWindow[] wins = PlatformUI.getWorkbench().getWorkbenchWindows();
						for (IWorkbenchWindow w : wins) {
							if (!w.equals(win)) {
								w.close();
							}
						}
						ch.elexis.data.Anwender.logoff();
						adaptForUser();
						LoginDialog dlg = new LoginDialog(win.getShell());
						dlg.create();
						dlg.setTitle(Messages.getString("GlobalActions.LoginDialogTitle")); //$NON-NLS-1$
						dlg.setMessage(Messages.getString("GlobalActions.LoginDialogMessage")); //$NON-NLS-1$
						dlg.getShell().setText(
							Messages.getString("GlobalActions.LoginDialogShelltext")); //$NON-NLS-1$
						dlg.open();
					} catch (Exception ex) {
						ExHandler.handle(ex);
					}
					System.out.println("login"); //$NON-NLS-1$
				}
			};
		importAction = new Action(Messages.getString("GlobalActions.Import")) { //$NON-NLS-1$
				{
					setId("import"); //$NON-NLS-1$
					setActionDefinitionId(Hub.COMMAND_PREFIX + "import");} //$NON-NLS-1$
				
				@Override
				public void run(){
					// cnv.open();
					Importer imp =
						new Importer(mainWindow.getShell(), "ch.elexis.FremdDatenImport"); //$NON-NLS-1$
					imp.create();
					imp.setMessage(Messages.getString("GlobalActions.ImportDlgMessage")); //$NON-NLS-1$
					imp.getShell().setText(Messages.getString("GlobalActions.ImportDlgShelltext")); //$NON-NLS-1$
					imp.setTitle(Messages.getString("GlobalActions.ImportDlgTitle")); //$NON-NLS-1$
					imp.open();
				}
			};
		
		connectWizardAction = new Action(Messages.getString("GlobalActions.Connection")) { //$NON-NLS-1$
				{
					setId("connectWizard"); //$NON-NLS-1$
					setActionDefinitionId(Hub.COMMAND_PREFIX + "connectWizard"); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					WizardDialog wd =
						new WizardDialog(mainWindow.getShell(), new DBConnectWizard());
					wd.open();
				}
				
			};
		
		changeMandantAction = new Action(Messages.getString("GlobalActions.Mandator")) { //$NON-NLS-1$
				{
					setId("changeMandant"); //$NON-NLS-1$
					// setActionDefinitionId(Hub.COMMAND_PREFIX+"changeMandant"); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					ChangeMandantDialog cmd = new ChangeMandantDialog();
					if (cmd.open() == org.eclipse.jface.dialogs.Dialog.OK) {
						Mandant n = cmd.result;
						if (n != null) {
							Hub.setMandant(n);
						}
					}
				}
			};
		printKontaktEtikette = new Action(Messages.getString("GlobalActions.PrintContactLabel")) { //$NON-NLS-1$
				{
					setToolTipText(Messages.getString("GlobalActions.PrintContactLabelToolTip")); //$NON-NLS-1$
					setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_ADRESSETIKETTE)); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					printAdr((Kontakt) GlobalEvents.getInstance().getSelectedObject(Kontakt.class));
				}
			};
		
		printAdresse = new Action(Messages.getString("GlobalActions.PrintAddressLabel")) { //$NON-NLS-1$
				{
					setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_ADRESSETIKETTE)); //$NON-NLS-1$
					setToolTipText(Messages.getString("GlobalActions.PrintAddressLabelToolTip")); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					printAdr((Patient) GlobalEvents.getInstance().getSelectedObject(Patient.class));
					System.out.println(Messages.getString("GlobalActions.45")); //$NON-NLS-1$
					
				}
				
			};
		
		printVersionedEtikette =
			new Action(Messages.getString("GlobalActions.PrintVersionedLabel")) {
				{
					setToolTipText(Messages.getString("GlobalActions.PrintVersionedLabelToolTip"));
					setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_VERSIONEDETIKETTE));
				}
				
				@Override
				public void run(){
					PrinterData pd = getPrinterData("Etiketten");
					if (pd != null) {
						Printer prn = new Printer(pd);
						if (prn.startJob(Messages.getString("GlobalActions.PrintLabelJobName")) == true) { //$NON-NLS-1$
							GC gc = new GC(prn);
							int y = 0;
							prn.startPage();
							
							Patient actPatient =
								(Patient) GlobalEvents.getInstance().getSelectedObject(
									Patient.class);
							String pid =
								StringTool.addModulo10(actPatient.getPatCode()) + "-"
									+ new TimeTool().toString(TimeTool.TIME_COMPACT);
							gc.drawString(
								Messages.getString("GlobalActions.OrderID") + ": " + pid, 0, 0); //$NON-NLS-1$
							FontMetrics fmt = gc.getFontMetrics();
							y += fmt.getHeight();
							String pers = actPatient.getPersonalia();
							gc.drawString(pers, 0, y);
							y += fmt.getHeight();
							gc
								.drawString(actPatient.getAnschrift().getEtikette(false, false), 0,
									y);
							y += fmt.getHeight();
							StringBuilder tel = new StringBuilder();
							tel
								.append(Messages.getString("GlobalActions.PhoneHomeLabelText")).append(actPatient.get("Telefon1")) //$NON-NLS-1$ //$NON-NLS-2$
								.append(Messages.getString("GlobalActions.PhoneWorkLabelText")).append(actPatient.get("Telefon2")) //$NON-NLS-1$ //$NON-NLS-2$
								.append(Messages.getString("GlobalActions.PhoneMobileLabelText")).append(actPatient.get("Natel")); //$NON-NLS-1$ //$NON-NLS-2$
							gc.drawString(tel.toString(), 0, y);
							prn.endPage();
							prn.endJob();
							prn.dispose();
						} else {
							MessageDialog
								.openError(
									mainWindow.getShell(),
									Messages.getString("GlobalActions.PrinterErrorTitle"), Messages.getString("GlobalActions.PrinterErrorMessage")); //$NON-NLS-1$ //$NON-NLS-2$
							
						}
					}
				}
			};
		
		printEtikette = new Action(Messages.getString("GlobalActions.PrintLabel")) { //$NON-NLS-1$
				{
					setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_PATIENTETIKETTE));
					setToolTipText(Messages.getString("GlobalActions.PrintLabelToolTip")); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					PrinterData pd = getPrinterData("Etiketten");
					if (pd != null) {
						Printer prn = new Printer(pd);
						if (prn.startJob(Messages.getString("GlobalActions.PrintLabelJobName")) == true) { //$NON-NLS-1$
							GC gc = new GC(prn);
							int y = 0;
							prn.startPage();
							Patient actPatient =
								(Patient) GlobalEvents.getInstance().getSelectedObject(
									Patient.class);
							gc
								.drawString(
									Messages.getString("GlobalActions.PatientIDLabelText") + actPatient.getPatCode(), 0, 0); //$NON-NLS-1$
							FontMetrics fmt = gc.getFontMetrics();
							y += fmt.getHeight();
							String pers = actPatient.getPersonalia();
							gc.drawString(pers, 0, y);
							y += fmt.getHeight();
							gc
								.drawString(actPatient.getAnschrift().getEtikette(false, false), 0,
									y);
							y += fmt.getHeight();
							StringBuilder tel = new StringBuilder();
							tel
								.append(Messages.getString("GlobalActions.PhoneHomeLabelText")).append(actPatient.get("Telefon1")) //$NON-NLS-1$ //$NON-NLS-2$
								.append(Messages.getString("GlobalActions.PhoneWorkLabelText")).append(actPatient.get("Telefon2")) //$NON-NLS-1$ //$NON-NLS-2$
								.append(Messages.getString("GlobalActions.PhoneMobileLabelText")).append(actPatient.get("Natel")); //$NON-NLS-1$ //$NON-NLS-2$
							gc.drawString(tel.toString(), 0, y);
							prn.endPage();
							prn.endJob();
							prn.dispose();
						} else {
							MessageDialog
								.openError(
									mainWindow.getShell(),
									Messages.getString("GlobalActions.PrinterErrorTitle"), Messages.getString("GlobalActions.PrinterErrorMessage")); //$NON-NLS-1$ //$NON-NLS-2$
							
						}
						
					}
				}
				
			};
		
		printBlatt = new Action(Messages.getString("GlobalActions.PrintEMR")) { //$NON-NLS-1$
				@Override
				public void run(){
					Patient actPatient =
						(Patient) GlobalEvents.getInstance().getSelectedObject(Patient.class);
					String printer = Hub.localCfg.get("Drucker/Einzelblatt/Name", null);
					String tray = Hub.localCfg.get("Drucker/Einzelblatt/Schacht", null);
					
					MessageBox mb =
						new MessageBox(Desk.getDisplay().getActiveShell(), SWT.ICON_INFORMATION
							| SWT.OK | SWT.CANCEL);
					mb.setText("Papier einlegen");
					mb.setMessage("Bitte legen Sie im Einzelblatteinzug Papier ein.");
					if (mb.open() == SWT.OK) {
						new TemplateDrucker("KG-Deckblatt", printer, tray).doPrint(actPatient); //$NON-NLS-1$
					}
				}
			};
		printRoeBlatt = new Action(Messages.getString("GlobalActions.PrintXRay")) { //$NON-NLS-1$
				@Override
				public void run(){
					Patient actPatient =
						(Patient) GlobalEvents.getInstance().getSelectedObject(Patient.class);
					String printer = Hub.localCfg.get("Drucker/A4/Name", null);
					String tray = Hub.localCfg.get("Drucker/A4/Schacht", null);
					
					new TemplateDrucker("Roentgen-Blatt", printer, tray).doPrint(actPatient); //$NON-NLS-1$
				}
			};
		
		fixLayoutAction =
			new Action(Messages.getString("GlobalActions.LockPerspectives"), Action.AS_CHECK_BOX) { //$NON-NLS-1$
				{
					setToolTipText(Messages.getString("GlobalActions.LockPerspectivesToolTip")); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					// store the current value in the user's configuration
					Hub.userCfg
						.set(PreferenceConstants.USR_FIX_LAYOUT, fixLayoutAction.isChecked());
				}
			};
		makeBillAction = new Action(Messages.getString("GlobalActions.MakeBill")) { //$NON-NLS-1$
				@Override
				public void run(){
					Fall actFall = GlobalEvents.getSelectedFall();
					Mandant mnd = Hub.actMandant;
					if (actFall != null && mnd != null) {
						String mndid = mnd.getId();
						Konsultation[] bhdl = actFall.getBehandlungen(false);
						ArrayList<Konsultation> lBehdl = new ArrayList<Konsultation>(bhdl.length);
						for (Konsultation b : bhdl) {
							Rechnung rn = b.getRechnung();
							if (rn == null){ // || rn.getStatus() == RnStatus.STORNIERT) {
								if (b.getMandant().getId().equals(mndid)) {
									lBehdl.add(b);
								}
							}
						}
						Result<Rechnung> res = Rechnung.build(lBehdl);
						if (!res.isOK()) {
							ErrorDialog.openError(mainWindow.getShell(), Messages
								.getString("GlobalActions.Error"), Messages
								.getString("GlobalActions.BillErrorMessage"), ResultAdapter
								.getResultAsStatus(res)); //$NON-NLS-1$ //$NON-NLS-2$
							// Rechnung rn=(Rechnung)res.get();
							// rn.storno(true);
							// rn.delete();
							
						}
					}
					// setFall(actFall,null);
				}
			};
		moveBehandlungAction = new Action(Messages.getString("GlobalActions.AssignCase")) { //$NON-NLS-1$
				@Override
				public void run(){
					// Object[] s=behandlViewer.getSelection();
					Konsultation k = GlobalEvents.getSelectedKons();
					if (k == null) {
						MessageDialog
							.openInformation(
								mainWindow.getShell(),
								Messages.getString("GlobalActions.NoKonsSelected"), Messages.getString("GlobalActions.NoKonsSelectedMessage")); //$NON-NLS-1$ //$NON-NLS-2$
						return;
					}
					
					SelectFallDialog dlg = new SelectFallDialog(mainWindow.getShell());
					if (dlg.open() == Dialog.OK) {
						Fall f = dlg.result;
						if (f != null) {
							k.setFall(f);
							GlobalEvents.getInstance().fireSelectionEvent(f);
						}
					}
				}
			};
		redateAction = new Action(Messages.getString("GlobalActions.Redate")) { //$NON-NLS-1$
				@Override
				public void run(){
					Konsultation k = GlobalEvents.getSelectedKons();
					if (k == null) {
						MessageDialog
							.openInformation(
								mainWindow.getShell(),
								Messages.getString("GlobalActions.NoKonsSelected"), Messages.getString("GlobalActions.NoKonsSelectedMessage")); //$NON-NLS-1$ //$NON-NLS-2$
						return;
					}
					
					DateSelectorDialog dlg = new DateSelectorDialog(mainWindow.getShell());
					if (dlg.open() == Dialog.OK) {
						TimeTool date = dlg.getSelectedDate();
						k.setDatum(date.toString(TimeTool.DATE_GER), false);
						GlobalEvents.getInstance().fireSelectionEvent(k);
					}
				}
			};
		delFallAction = new Action(Messages.getString("GlobalActions.DeleteCase")) { //$NON-NLS-1$
				@Override
				public void run(){
					Fall actFall = GlobalEvents.getSelectedFall();
					if ((actFall != null) && (actFall.delete(false) == false)) {
						SWTHelper.alert(Messages
							.getString("GlobalActions.CouldntDeleteCaseMessage"), //$NON-NLS-1$
							Messages.getString("GlobalActions.CouldntDeleteCaseExplanation") + //$NON-NLS-1$
								Messages.getString("GlobalActions.93")); //$NON-NLS-1$
					}
					GlobalEvents.getInstance().fireUpdateEvent(Fall.class);
				}
			};
		delKonsAction = new Action(Messages.getString("GlobalActions.DeleteKons")) { //$NON-NLS-1$
				@Override
				public void run(){
					Konsultation k = GlobalEvents.getSelectedKons();
					if ((k != null) && (k.delete(false) == false)) {
						SWTHelper.alert(Messages.getString("GlobalActions.CouldntDeleteKons"), //$NON-NLS-1$
							Messages.getString("GlobalActions.CouldntDeleteKonsExplanation") + //$NON-NLS-1$
								Messages.getString("GlobalActions.97")); //$NON-NLS-1$
					}
					GlobalEvents.getInstance().clearSelection(Konsultation.class);
					GlobalEvents.getInstance().fireSelectionEvent(k.getFall());
				}
			};
		openFallaction = new Action(Messages.getString("GlobalActions.EditCase")) { //$NON-NLS-1$
			
				@Override
				public void run(){
					try {
						Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(FallDetailView.ID);
						// getViewSite().getPage().showView(FallDetailView.ID);
					} catch (Exception ex) {
						ExHandler.handle(ex);
					}
				}
				
			};
		reopenFallAction = new Action(Messages.getString("GlobalActions.ReopenCase")) { //$NON-NLS-1$
				@Override
				public void run(){
					Fall actFall = GlobalEvents.getSelectedFall();
					if (actFall != null) {
						actFall.setEndDatum(""); //$NON-NLS-1$
					}
				}
			};
		neueKonsAction = new Action(Messages.getString("GlobalActions.NewKons")) { //$NON-NLS-1$
				{
					setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_NEW));
					setToolTipText(Messages.getString("GlobalActions.NewKonsToolTip")); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					neueKons(null);
				}
			};
		neuerFallAction = new Action(Messages.getString("GlobalActions.NewCase")) { //$NON-NLS-1$
				{
					setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_NEW)); //$NON-NLS-1$
					setToolTipText(Messages.getString("GlobalActions.NewCaseToolTip")); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					Patient pat = GlobalEvents.getSelectedPatient();
					if (pat != null) {
						NeuerFallDialog nfd = new NeuerFallDialog(mainWindow.getShell(), null);
						if (nfd.open() == Dialog.OK) {

						}
					}
				}
			};
	}
	
	/**
	 * Creates a new Konsultation object, with an optional initial text.
	 * 
	 * @param initialText
	 *            the initial text to be set, or null if no initial text should be set.
	 */
	public static void neueKons(final String initialText){
		Fall actFall = GlobalEvents.getSelectedFall();
		if (actFall == null) {
			Patient actPatient = GlobalEvents.getSelectedPatient();
			if (actPatient == null) {
				SWTHelper
					.showError(
						Messages.getString("GlobalActions.CantCreateKons"), Messages.getString("GlobalActions.DoSelectPatient")); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
			if (actFall == null) {
				Konsultation k = actPatient.getLetzteKons(false);
				if (k != null) {
					actFall = k.getFall();
					if (actFall == null) {
						SWTHelper
							.showError(
								Messages.getString("GlobalActions.CantCreateKons"), Messages.getString("GlobalActions.DoSelectCase")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} else {
					Fall[] faelle = actPatient.getFaelle();
					if ((faelle == null) || (faelle.length == 0)) {
						actFall =
							actPatient.neuerFall(Fall.getDefaultCaseLabel(), Fall
								.getDefaultCaseReason(), Fall.getDefaultCaseLaw());
					} else {
						actFall = faelle[0];
					}
				}
			}
		}
		if (!actFall.isOpen()) {
			SWTHelper.showError("Fall geschlossen",
				"Zu einem geschlossenen Fall können keine neuen Konsultationen erstellt werden");
			return;
		}
		Konsultation actLetzte = actFall.getLetzteBehandlung();
		if ((actLetzte != null)
			&& actLetzte.getDatum().equals(new TimeTool().toString(TimeTool.DATE_GER))) {
			if (MessageDialog.openQuestion(Desk.getTopShell(), Messages
				.getString("GlobalActions.SecondForToday"), //$NON-NLS-1$
				Messages.getString("GlobalActions.SecondForTodayQuestion")) == false) { //$NON-NLS-1$
				return;
			}
		}
		Konsultation n = actFall.neueKonsultation();
		n.setMandant(Hub.actMandant);
		if (initialText != null) {
			n.updateEintrag(initialText, false);
		}
		GlobalEvents.getInstance().fireSelectionEvent(actFall);
		GlobalEvents.getInstance().fireSelectionEvent(n);
	}
	
	protected void printAdr(final Kontakt k){
		PrinterData pd = getPrinterData("Etiketten");
		if (pd != null) {
			Printer prn = new Printer(pd);
			if (prn.startJob("Etikette drucken") == true) { //$NON-NLS-1$
				GC gc = new GC(prn);
				int y = 0;
				prn.startPage();
				FontMetrics fmt = gc.getFontMetrics();
				String pers = k.getPostAnschrift(true);
				String[] lines = pers.split("\n"); //$NON-NLS-1$
				for (String line : lines) {
					gc.drawString(line, 0, y);
					y += fmt.getHeight();
				}
				prn.endPage();
				prn.endJob();
				prn.dispose();
			} else {
				MessageDialog
					.openError(
						mainWindow.getShell(),
						Messages.getString("GlobalActions.PrinterErrorTitle"), Messages.getString("GlobalActions.PrinterErrorMessage")); //$NON-NLS-1$ //$NON-NLS-2$
				
			}
			
		}
	}
	
	/**
	 * Return a PrinterData object according to the given type (e. g. "Etiketten") and the user
	 * settings. Shows a printer selection dialog if required.
	 * 
	 * @param type
	 *            the printer type according to the printer settings
	 * @return a PrinterData object describing the selected printer
	 */
	private PrinterData getPrinterData(final String type){
		String cfgPrefix = "Drucker/" + type + "/"; //$NON-NLS-1$ $NON-NLS-2$
		
		PrinterData pd = null;
		String printer = Hub.localCfg.get(cfgPrefix + "Name", null); //$NON-NLS-1$
		String driver = Hub.localCfg.get(cfgPrefix + "Driver", null); //$NON-NLS-1$
		boolean choose = Hub.localCfg.get(cfgPrefix + "Choose", false); //$NON-NLS-1$
		if (choose || StringTool.isNothing(printer) || StringTool.isNothing(driver)) {
			Shell shell = Desk.getTopShell();
			PrintDialog pdlg = new PrintDialog(shell);
			pd = pdlg.open();
		} else {
			pd = new PrinterData(driver, printer);
		}
		
		return pd;
	}
	
	/**
	 * Verfügbarkeit der einzelnen Menuepunkte an den angemeldeten Anwender anpassen
	 * Menueeinstellungen wiederherstellen
	 */
	public void adaptForUser(){
		setMenuForUser(AC_EXIT, exitAction);
		// setMenuForUser(AC_UPDATE,updateAction); //$NON-NLS-1$
		setMenuForUser(AC_NEWWINDOW, newWindowAction);
		setMenuForUser(AC_LOGIN, loginAction);
		setMenuForUser(AC_IMORT, importAction);
		setMenuForUser(AC_ABOUT, aboutAction);
		setMenuForUser(AC_HELP, helpAction);
		setMenuForUser(AC_PREFS, prefsAction);
		setMenuForUser(AC_CHANGEMANDANT, changeMandantAction);
		// setMenuForUser("importTarmedAction",importTarmedAction);
		setMenuForUser(AC_CONNECT, connectWizardAction);
		if (Hub.acl.request(AC_SHOWPERSPECTIVE) == true) {
			perspectiveList.setVisible(true);
		} else {
			perspectiveList.setVisible(false);
		}
		if (Hub.acl.request(AC_SHOWVIEW) == true) {
			viewList.setVisible(true);
		} else {
			viewList.setVisible(false);
		}
		
		// restore menue settings
		if (Hub.actUser != null) {
			boolean fixLayoutChecked =
				Hub.userCfg.get(PreferenceConstants.USR_FIX_LAYOUT,
					PreferenceConstants.USR_FIX_LAYOUT_DEFAULT);
			fixLayoutAction.setChecked(fixLayoutChecked);
			// System.err.println("fixLayoutAction: set to " + fixLayoutChecked);
		} else {
			fixLayoutAction.setChecked(PreferenceConstants.USR_FIX_LAYOUT_DEFAULT);
			// System.err.println("fixLayoutAction: reset to false");
		}
	}
	
	private void setMenuForUser(final String name, final IAction action){
		if (Hub.acl.request(name) == true) {
			action.setEnabled(true);
		} else {
			action.setEnabled(false);
		}
		
	}
	
	/**
	 * Creates an ActionHandler for the given IAction and registers it to the Site's HandlerService,
	 * i. e. binds the action to the command so that key bindings get activated. You need to set the
	 * action's actionDefinitionId to the command id.
	 * 
	 * @param action
	 *            the action to activate. The action's actionDefinitionId must have been set to the
	 *            command's id (using <code>setActionDefinitionId()</code>)
	 * @param part
	 *            the view this action should be registered for
	 */
	public static void registerActionHandler(final ViewPart part, final IAction action){
		String commandId = action.getActionDefinitionId();
		if (!StringTool.isNothing(commandId)) {
			IHandlerService handlerService =
				(IHandlerService) part.getSite().getService(IHandlerService.class);
			IHandler handler = new ActionHandler(action);
			handlerService.activateHandler(commandId, handler);
		}
	}
	
	class ChangeMandantDialog extends TitleAreaDialog {
		List<Mandant> lMandant;
		org.eclipse.swt.widgets.List lbMandant;
		Mandant result;
		
		ChangeMandantDialog(){
			super(mainWindow.getShell());
		}
		
		@Override
		public Control createDialogArea(final Composite parent){
			lbMandant = new org.eclipse.swt.widgets.List(parent, SWT.BORDER | SWT.SINGLE);
			lbMandant.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			Query<Mandant> qbe = new Query<Mandant>(Mandant.class);
			lMandant = qbe.execute();
			for (PersistentObject m : lMandant) {
				lbMandant.add(m.getLabel());
			}
			return lbMandant;
		}
		
		@Override
		protected void okPressed(){
			int idx = lbMandant.getSelectionIndex();
			if (idx > -1) {
				result = lMandant.get(idx);
			}
			super.okPressed();
		}
		
		@Override
		public void create(){
			super.create();
			setTitle(Messages.getString("GlobalActions.ChangeMandator")); //$NON-NLS-1$
			setMessage(Messages.getString("GlobalActions.ChangeMandatorMessage")); //$NON-NLS-1$
		}
		
	};
}
