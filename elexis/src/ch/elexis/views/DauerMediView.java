/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: DauerMediView.java 4376 2008-09-05 16:58:02Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
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
	private IAction toClipBoardAction;
	FixMediDisplay dmd;
	public DauerMediView() {
		
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		dmd=new FixMediDisplay(parent,getViewSite());
		dmd.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		makeActions();
		getViewSite().getActionBars().getToolBarManager().add(toClipBoardAction);
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
	private void makeActions(){
		toClipBoardAction=new Action("Kopieren"){
			{
				setToolTipText("In Zwischenablage kopieren");
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_CLIPBOARD));
			}

			@Override
			public void run(){
				dmd.toClipBoard(true);
			}
			
		};
		
	}
}
