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
 * $Id: DauerMediDisplay.java 4722 2008-12-04 10:11:09Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.RestrictedAction;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Artikel;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Rezept;
import ch.elexis.dialogs.MediDetailDialog;
import ch.elexis.util.DynamicListDisplay;
import ch.elexis.util.ViewMenus;
import ch.elexis.views.codesystems.LeistungenView;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;

/**
 * Display and let the user modify the medication of the currently selected patient
 * @author gerry
 *
 *@deprecated use FixMediDisplay
 */
@Deprecated
public class DauerMediDisplay extends DynamicListDisplay {
	private DLDListener dlisten;
	private IAction stopMedicationAction,changeMedicationAction,removeMedicationAction;
	DauerMediDisplay self;
	
	public DauerMediDisplay(Composite parent, IViewSite s){
		super(parent,SWT.NONE,null);
		dlisten=new DauerMediListener(s);
		self=this;
		addHyperlinks("Hinzu... ","Liste... ","Rezept... ");
		makeActions();
		ViewMenus menu=new ViewMenus(s);
		menu.createControlContextMenu(list,stopMedicationAction,changeMedicationAction,null,removeMedicationAction);
		setDLDListener(dlisten);
	}
	
	
	public void reload(){
		clear();
		Patient act=GlobalEvents.getSelectedPatient();
		if(act!=null){
			Prescription[] pre=act.getFixmedikation();
			for(Prescription pr:pre){
				add(pr);
			}
		}
	}
	
	
	class DauerMediListener implements DLDListener {
		IViewSite site;

		DauerMediListener(IViewSite s){
			site=s;
		}
	
		public boolean dropped(PersistentObject dropped) {
			if(dropped instanceof Artikel){
				Prescription pre=new Prescription((Artikel)dropped,GlobalEvents.getSelectedPatient(),"","");
				pre.set("DatumVon", new TimeTool().toString(TimeTool.DATE_GER));
				MediDetailDialog dlg=new MediDetailDialog(getShell(),pre);
				if(dlg.open()==Window.OK){
					self.add(pre);
				}
				return true;
			}else if(dropped instanceof Prescription){
				Prescription pre=(Prescription)dropped;
				Prescription now=new Prescription(pre.getArtikel(),GlobalEvents.getSelectedPatient(),pre.getDosis(),pre.getBemerkung());
				now.set("DatumVon", new TimeTool().toString(TimeTool.DATE_GER));
				self.add(now);
				return true;
			}else{
				return false;
			}
		}

		public void hyperlinkActivated(String l) {
			try{
				if(l.equals("Hinzu... ")){
					site.getPage().showView(LeistungenView.ID);
				}else if(l.equals("Liste... ")){
					
					RezeptBlatt rpb=(RezeptBlatt)site.getPage().showView(RezeptBlatt.ID);
					rpb.createEinnahmeliste(GlobalEvents.getSelectedPatient(),getAll().toArray(new Prescription[0]));
				}else if(l.equals("Rezept... ")){
					Rezept rp=new Rezept(GlobalEvents.getSelectedPatient());
					for(Prescription p:getAll().toArray(new Prescription[0])){
						/*
						rp.addLine(new RpZeile("1",p.getArtikel().getLabel(),"",
								p.getDosis(),p.getBemerkung()));
								*/
						rp.addPrescription(new Prescription(p));
					}
					RezeptBlatt rpb=(RezeptBlatt)site.getPage().showView(RezeptBlatt.ID);
					rpb.createRezept(rp);
				}
			}catch(Exception ex){
				ExHandler.handle(ex);
			}
			
		}
	}
	private void makeActions(){
		
		changeMedicationAction=new RestrictedAction(AccessControlDefaults.MEDICATION_MODIFY,"Ändern..."){
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_EDIT));
				setToolTipText("Dauermedikation modifizieren");
			}
			public void doRun(){
				Prescription pr=(Prescription)getSelection();
				if(pr!=null){
					new MediDetailDialog(getShell(),pr).open();
					redraw();
				}
			}
		};
		
		stopMedicationAction=new RestrictedAction(AccessControlDefaults.MEDICATION_MODIFY,"Stoppen"){
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_REMOVEITEM));
				setToolTipText("Diese Medikation stoppen");
			}
			public void doRun(){
				Prescription pr=(Prescription) getSelection();
				if(pr!=null){
					remove(pr);
					pr.delete();	// this does not delete but stop the Medication. Sorry for that
				}
			}
		};
		
		removeMedicationAction=new RestrictedAction(AccessControlDefaults.DELETE_MEDICATION,"Löschen"){
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_DELETE));
				setToolTipText("Medikation unwiederruflich löschen");
			}
			public void doRun(){
				Prescription pr=(Prescription) getSelection();
				if(pr!=null){
					remove(pr);
					pr.remove();	// this does, in fact, remove the medication from the database
				}
			}
		};
		
	}

}
