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
 * $Id: KonsListe.java 4722 2008-12-04 10:11:09Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.KonsFilter;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.BackingStoreListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.dialogs.KonsFilterDialog;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;

public class KonsListe extends ViewPart implements ActivationListener, SelectionListener,
		BackingStoreListener, ISaveablePart2 {
	public static final String ID = "ch.elexis.HistoryView";
	HistoryDisplay liste;
	Patient actPatient;
	ViewMenus menus;
	private Action newKonsAction, filterAction;
	private KonsFilter filter;
	
	@Override
	public void createPartControl(final Composite parent){
		parent.setLayout(new GridLayout());
		liste = new HistoryDisplay(parent, getViewSite());
		liste.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		makeActions();
		menus = new ViewMenus(getViewSite());
		menus.createToolbar(filterAction, newKonsAction);
		// GlobalEvents.getInstance().addSelectionListener(this,
		// getViewSite().getWorkbenchWindow());
		GlobalEvents.getInstance().addActivationListener(this, this);
	}
	
	@Override
	public void dispose(){
		liste.stop();
		GlobalEvents.getInstance().removeActivationListener(this, this);
		GlobalEvents.getInstance().removeSelectionListener(this);
	}
	
	@Override
	public void setFocus(){
	// TODO Auto-generated method stub
	
	}
	
	/* SelectionListener */
	public void selectionEvent(final PersistentObject obj){
		if (obj instanceof Patient) {
			if ((actPatient == null) || (!actPatient.getId().equals(((Patient) obj).getId()))) {
				actPatient = (Patient) obj;
				restart();
			}
		} else if (obj instanceof Fall) {
			actPatient = ((Fall) obj).getPatient();
			restart();
		}
		
	}
	
	private void restart(){
		liste.stop();
		liste.load(actPatient);
		liste.start(filter);
	}
	
	public void clearEvent(final Class template){
		if (template.equals(Patient.class)) {
			liste.stop();
			liste.load(null, true);
			liste.start(filter);
		}
	}
	
	/* ActivationListener */
	public void activation(final boolean mode){ /* leer */}
	
	public void visible(final boolean mode){
		if (mode) {
			GlobalEvents.getInstance().addSelectionListener(this);
			GlobalEvents.getInstance().addBackingStoreListener(this);
			selectionEvent(GlobalEvents.getSelectedPatient());
		} else {
			GlobalEvents.getInstance().removeSelectionListener(this);
			GlobalEvents.getInstance().removeBackingStoreListener(this);
		}
		
	}
	
	private void makeActions(){
		newKonsAction = new Action("Neue Konsultation") {
			{
				setToolTipText("Neue Konsultation erstellen");
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_NEW));
			}
			
			@Override
			public void run(){
				if (actPatient == null) {
					return;
				}
				Fall fall = GlobalEvents.getSelectedFall();
				if (fall == null) {
					
					Konsultation k = actPatient.getLetzteKons(false);
					if (k != null) {
						fall = k.getFall();
					} else {
						if (SWTHelper.askYesNo("Kein Fall ausgewählt",
							"Soll ein neuern Fall für diese Konsultation erstellt werden?")) {
							fall = actPatient.neuerFall("Allgemein", "Krankheit", "KVG");
						} else {
							return;
						}
					}
				}
				Konsultation k = fall.neueKonsultation();
				k.setMandant(Hub.actMandant);
				restart();
				GlobalEvents.getInstance().fireSelectionEvent(k);
			}
		};
		filterAction = new Action("Liste Filtern", Action.AS_CHECK_BOX) {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_FILTER));
				setToolTipText("Liste der Konsultationen filtern");
			}
			
			@Override
			public void run(){
				if (!isChecked()) {
					filter = null;
				} else {
					KonsFilterDialog kfd = new KonsFilterDialog(actPatient, filter);
					if (kfd.open() == Dialog.OK) {
						filter = kfd.getResult();
					} else {
						kfd = null;
						setChecked(false);
					}
				}
				restart();
			}
		};
	}
	
	/*
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	public void doSave(final IProgressMonitor monitor){ /* leer */}
	
	public void doSaveAs(){ /* leer */}
	
	public boolean isDirty(){
		return true;
	}
	
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
	
	public void reloadContents(final Class clazz){
		if (clazz.equals(Konsultation.class)) {
			restart();
		}
		
	}
	
}
