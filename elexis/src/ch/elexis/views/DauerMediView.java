/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: DauerMediView.java 1832 2007-02-18 09:12:31Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.util.SWTHelper;

/**
 * Eine platzsparende View zur Anzeige der Dauermedikation
 * @author gerry
 *
 */
public class DauerMediView extends ViewPart implements ActivationListener, SelectionListener{
	public final static String ID="ch.elexis.dauermedikationview";
	DauerMediDisplay dmd;
	public DauerMediView() {
		
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		dmd=new DauerMediDisplay(parent,getViewSite());
		dmd.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		GlobalEvents.getInstance().addActivationListener(this, this);
	}

	public void dispose(){
		GlobalEvents.getInstance().removeActivationListener(this, this);
	}
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void activation(boolean mode) { /* leer */}

	public void visible(boolean mode) {
		if(mode){
			GlobalEvents.getInstance().addSelectionListener(this);
			selectionEvent(null);
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
		
	}

	public void clearEvent(Class template) {
		// TODO Auto-generated method stub
		
	}

	public void selectionEvent(PersistentObject obj) {
		if((obj==null) || (obj instanceof Patient)){
			dmd.reload();
		}
	}
}
