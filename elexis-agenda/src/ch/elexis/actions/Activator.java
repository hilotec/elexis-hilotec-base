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
 *  $Id: Activator.java 5280 2009-05-09 10:46:12Z rgw_ch $
 *******************************************************************************/
package ch.elexis.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ch.elexis.Desk;
import ch.elexis.util.Log;

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
	// public TimeTool theDay;
	// public Synchronizer pinger;
	public static String IMG_HOME = "ch.elexis.agenda.home";

	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
		AgendaActions.makeActions();
		// theDay=new TimeTool();
		Desk.getImageRegistry().put(IMG_HOME,
				getImageDescriptor("icons/calendar_view_day.png"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		// pinger=new Synchronizer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
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

	
}
