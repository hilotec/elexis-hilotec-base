/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: EncounterDisplay.java 1775 2007-02-09 21:30:59Z rgw_ch $
 *******************************************************************************/

package ch.elexis.icpc.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.Form;

import ch.elexis.Hub;
import ch.elexis.data.PersistentObject;
import ch.elexis.icpc.Activator;
import ch.elexis.icpc.Encounter;
import ch.elexis.icpc.IcpcCode;
import ch.elexis.util.PersistentObjectDropTarget;
import ch.elexis.util.SWTHelper;
import ch.elexis.views.codesystems.DiagnosenView;
import ch.rgw.tools.ExHandler;

/**
 * An ICPC-Encounter. Every encounter belongs to exactly one Episode, but an Episode can
 * (and will usually) contain several Encounters.
 * An Encounter has an RFE (Reason for encounter, Problem), a diagnosis and a plan.
 * This display will allow the user to attach those Elements by drag&drop
 * @author Gerry
 *
 */
public class EncounterDisplay extends Composite {
	Form form;
	Group gRfe,gDiag,gProc;
	Label lRfe,lDiag,lProc;
	Encounter actEncounter;
	ClickReact clicker=new ClickReact();
	public EncounterDisplay(Composite parent){
		super(parent,SWT.NONE);
		form=Activator.getToolkit().createForm(this);
		setLayout(new GridLayout());
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Composite body=form.getBody();
		body.setLayout(new GridLayout());
		gRfe=new Group(body,SWT.NONE);
		gRfe.setText("RFE / Problem");
		gRfe.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new PersistentObjectDropTarget(gRfe,new PersistentObjectDropTarget.Receiver(){

			public boolean accept(PersistentObject o) {
				if(o instanceof IcpcCode){
					return true;
				}
				return false;
			}

			public void dropped(PersistentObject o, DropTargetEvent ev) {
				if((actEncounter)!=null && (o instanceof IcpcCode)){
					actEncounter.setRFE((IcpcCode)o);
					setEncounter(actEncounter);
				}
			}
			
		});
		gRfe.setLayout(new FillLayout());
		gRfe.addMouseListener(clicker);
		lRfe=new Label(gRfe,SWT.WRAP);
		
		gDiag=new Group(body,SWT.NONE);
		gDiag.setText("Diagnose");
		gDiag.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new PersistentObjectDropTarget(gDiag,new PersistentObjectDropTarget.Receiver(){

			public boolean accept(PersistentObject o) {
				if(o instanceof IcpcCode){
					return true;
				}
				return false;
			}

			public void dropped(PersistentObject o, DropTargetEvent ev) {
				if((actEncounter)!=null && (o instanceof IcpcCode)){
					actEncounter.setDiag((IcpcCode)o);
					setEncounter(actEncounter);
				}
			}
			
		});
		gDiag.setLayout(new FillLayout());
		gDiag.addMouseListener(clicker);
		lDiag=new Label(gDiag,SWT.WRAP);
		gProc=new Group(body,SWT.NONE);
		gProc.setText("Procedere");
		new PersistentObjectDropTarget(gProc,new PersistentObjectDropTarget.Receiver(){

			public boolean accept(PersistentObject o) {
				if(o instanceof IcpcCode){
					return true;
				}
				return false;
			}

			public void dropped(PersistentObject o, DropTargetEvent ev) {
				if((actEncounter)!=null && (o instanceof IcpcCode)){
					actEncounter.setProc((IcpcCode)o);
					setEncounter(actEncounter);
				}
			}
			
		});
		gProc.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		gProc.setLayout(new FillLayout());
		gProc.addMouseListener(clicker);
		lProc=new Label(gProc,SWT.WRAP);
	}
	
	public void setEncounter(Encounter e){
		actEncounter=e;
		if(e==null){
			form.setText("Keine Episode gewählt");
			lRfe.setText("");
			lDiag.setText("");
			lProc.setText("");
		}else{
			form.setText(e.getEpisode().getLabel());
			IcpcCode rfe=e.getRFE();
			lRfe.setText(rfe==null ? "" : rfe.getLabel());
			IcpcCode diag=e.getDiag();
			lDiag.setText(diag==null ? "" : diag.getLabel());
			IcpcCode proc=e.getProc();
			lProc.setText(proc==null ? "" : proc.getLabel());
		}
	}
	class ClickReact extends MouseAdapter{

		@Override
		public void mouseUp(MouseEvent arg0) {
			try{
				Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(DiagnosenView.ID);
			}catch(Exception ex){
				ExHandler.handle(ex);
				
			}
			super.mouseUp(arg0);
		}
		
	}
}
