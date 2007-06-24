/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: Activator.java 1709 2007-02-01 14:34:08Z rgw_ch $
 *******************************************************************************/

package ch.elexis.icpc;

import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ch.elexis.Desk;
import ch.elexis.util.Log;


public class Activator extends AbstractUIPlugin {
	
//	 The plug-in ID
	public static final String PLUGIN_ID = "ch.elexis.icpc";

	// The shared instance
	private static Activator plugin;

	private static FormToolkit tk;
	public static FormToolkit getToolkit(){
		if(tk==null){
			tk=new FormToolkit(Desk.theDisplay);
		}
		return tk;
	}
	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
		log=Log.get("ICPC");
	}

	static Log log;
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
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
	
}
