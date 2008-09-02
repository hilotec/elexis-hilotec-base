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
 *    $Id: EncounterView.java 4356 2008-09-02 16:20:10Z rgw_ch $
 *******************************************************************************/

package ch.elexis.icpc.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.icpc.CodeSelectorFactory;
import ch.elexis.icpc.Encounter;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewerConfigurer;

public class EncounterView extends ViewPart implements ActivationListener,
		SelectionListener {
	public static final String ID="ch.elexis.icpc.encounterView";
	private EncounterDisplay display;
	
	public EncounterView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		display=new EncounterDisplay(parent);
		display.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		GlobalEvents.getInstance().addActivationListener(this, getViewSite().getPart());
		
	}

	@Override
	public void dispose(){
		GlobalEvents.getInstance().removeActivationListener(this, getViewSite().getPart());
	}
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void activation(boolean mode) {
		// TODO Auto-generated method stub

	}

	public void visible(boolean mode) {
		if(mode){
			GlobalEvents.getInstance().addSelectionListener(this);
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}

	}

	public void clearEvent(Class template) {
		// TODO Auto-generated method stub

	}

	public void selectionEvent(PersistentObject obj) {
		if(obj instanceof Encounter){
			display.setEncounter((Encounter)obj);
		}else if(obj instanceof Patient){
			display.setEncounter(null);
		}

	}

}
