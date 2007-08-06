/*******************************************************************************
 * Copyright (c) 2007, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    
 *    $Id$
 *******************************************************************************/

package org.iatrix;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


public class IatrixActivator extends AbstractUIPlugin {
	
	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "org.iatrix";

	/**
	 * The shared instance
	 */
	private static IatrixActivator instance;

	 //  hide constructor
	public IatrixActivator() {
		instance = this;
	}

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
		instance = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static IatrixActivator getInstance() {
		return instance;
	}
	
}
