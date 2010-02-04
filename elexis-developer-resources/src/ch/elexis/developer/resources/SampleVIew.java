/*******************************************************************************
 * Copyright (c) 2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *    $Id: DBImage.java 6058 2010-02-03 15:02:13Z rgw_ch $
 *******************************************************************************/

package ch.elexis.developer.resources;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.actions.GlobalEventDispatcher.IActivationListener;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.ViewerConfigurer;

/**
 * This is a sample view to explain how to connect to elexis's event scheduler and how to
 * display elexis's data types.
 * We implement IActivationListener to be informed, wenn the user can see our View. All UI funktions 
 * must only be active in that case.
 * @author gerry
 *
 */
public class SampleVIew extends ViewPart implements IActivationListener{

	/**
	 * CommonViewer is a "golden hammer". Use it for fast prototyping. In many cases
	 * you should use mor spezialiced viewers.
	 */
	CommonViewer cv;
	
	/**
	 * A Common Viewer is always controlled by a ViewerConfigurer. So you'll never have only
	 * one of them
	 */
	ViewerConfigurer vc;
	
	public SampleVIew() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * This method is called immediately before the view is created. This is the right place to
	 * create all UI elements. The parent composite already has a GridLayout.
	 */
	@Override
	public void createPartControl(Composite parent) {
		
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	/**
	 * From IActivationListener: the view was activated or inactivated
	 * @param mode
	 */
	public void activation(boolean mode) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * From IActivationListener: The View changes visibility
	 * @param mode true: the view becomes visible. false: the view becomes invisible
	 */
	public void visible(boolean mode) {
		if(mode){
			
		}else{
			
		}
		
	}

}
