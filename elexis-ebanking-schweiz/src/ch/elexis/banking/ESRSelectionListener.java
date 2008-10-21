/*******************************************************************************
 * Copyright (c) 2007-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ESRSelectionListener.java 4617 2008-10-21 11:49:55Z rgw_ch $
 *******************************************************************************/
package ch.elexis.banking;

import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;

import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.PersistentObject;

/**
 * Eigentlich nur zur Demonstration, dass ein Selectionlistener auch unabhängig von einer View
 * existieren kann
 * 
 * @author gerry
 * 
 */
public class ESRSelectionListener implements SelectionListener {
	private IWorkbenchWindow win;
	
	ESRSelectionListener(IViewSite site){
		win = site.getWorkbenchWindow();
	}
	
	void activate(boolean mode){
		if (mode) {
			GlobalEvents.getInstance().addSelectionListener(this);
		} else {
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
	}
	
	public void clearEvent(Class template){

	}
	
	public void selectionEvent(PersistentObject obj){
		if (obj instanceof ESRRecord) {
			ESRRecord esr = (ESRRecord) obj;
			GlobalEvents.getInstance().fireSelectionEvent(esr.getRechnung());
		}
		
	}
	
}
