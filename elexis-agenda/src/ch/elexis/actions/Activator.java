/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation, adapted from JavaAgenda
 * 
 *  $Id: Activator.java 5745 2009-09-22 09:34:47Z rgw_ch $
 *******************************************************************************/
package ch.elexis.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.agenda.Messages;
import ch.elexis.agenda.data.Termin;
import ch.elexis.agenda.preferences.PreferenceConstants;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.util.Log;
import ch.rgw.tools.TimeTool;

/**
 * Einen Activator braucht man immer dann, wenn man irgendwelche Dinge sicher zu
 * Beginn der Plugin-Aktivierung ausgeführt haben will. Wir verwenden das hier,
 * um die AgendaActions zu initialisieren.
 */
public class Activator extends AbstractUIPlugin {
	
	// The plug-in ID
	public static final String PLUGIN_ID = "ch.elexis.agenda"; //$NON-NLS-1$
	
	// The shared instance
	private static Activator plugin;
	
	public static Log log = Log.get("Agenda"); //$NON-NLS-1$
	public static String IMG_HOME = "ch.elexis.agenda.home";
	private String actResource;
	private TimeTool actDate;
	
	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		AgendaActions.makeActions();
		//log.log("activated", Log.DEBUGMSG);
		Desk.getImageRegistry().put(IMG_HOME,
			getImageDescriptor("icons/calendar_view_day.png"));
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		// pinger.pause(true);
		plugin = null;
		super.stop(context);
	}
	
	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(
			"ch.elexis.agenda", path); //$NON-NLS-1$
	}
	
	public String[] getResources() {
		return Hub.globalCfg.get(PreferenceConstants.AG_BEREICHE,
			Messages.TagesView_14).split(",");
	}
	
	public String getActResource() {
		if (actResource == null) {
			actResource = Activator.getDefault().getResources()[0];
		}
		return actResource;
	}
	
	public void setActResource(String resname) {
		actResource = resname;
		Hub.userCfg.set(PreferenceConstants.AG_BEREICH, resname);
	}
	
	public TimeTool getActDate() {
		if (actDate == null) {
			actDate = new TimeTool();
		}
		return new TimeTool(actDate);
	}
	
	public void setActDate(String date) {
		if (actDate == null) {
			actDate = new TimeTool();
		}
		actDate.set(date);
	}
	
	public void setActDate(TimeTool date) {
		if (actDate == null) {
			actDate = new TimeTool();
		}
		actDate.set(date);
	}
	
	public TimeTool addDays(int day) {
		if(actDate==null){
			actDate=new TimeTool();
		}
		actDate.addDays(day);
		return new TimeTool(actDate);
	}
	
	/**
	 * propagate a termin selection through the system
	 * @param t
	 */
	public void dispatchTermin(Termin t) {
		GlobalEvents ev = GlobalEvents.getInstance();
		Patient pat = t.getPatient();
		ev.fireSelectionEvent(t);
		if (pat != null) {
			ev.fireSelectionEvent(pat);
			Konsultation kons = GlobalEvents.getSelectedKons();
			
			String sVgl = getActDate().toString(TimeTool.DATE_COMPACT);
			if ((kons == null)
					|| // Falls nicht die richtige Kons selektiert ist, passende
					// Kons für heute suchen
					!(kons.getFall().getPatient().getId().equals(pat.getId()))
					|| !(new TimeTool(kons.getDatum())
					.toString(TimeTool.DATE_COMPACT).equals(sVgl))) {
				Fall[] faelle = pat.getFaelle();
				TimeTool ttVgl = new TimeTool();
				for (Fall f : faelle) {
					Konsultation[] konsen = f.getBehandlungen(true);
					for (Konsultation k : konsen) {
						ttVgl.set(k.getDatum());
						if (ttVgl.toString(TimeTool.DATE_COMPACT).equals(sVgl)) {
							ev.fireSelectionEvent(k);
							return;
						}
					}
				}
				
			}
		}
	}
}
