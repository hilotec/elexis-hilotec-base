/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: FallDetailView.java 4424 2008-09-21 13:56:56Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.util.SWTHelper;

public class FallDetailView extends ViewPart implements SelectionListener, ISaveablePart2 {
	public static final String ID = "ch.elexis.FallDetailView";
	FallDetailBlatt2 fdb;
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new GridLayout());
		fdb = new FallDetailBlatt2(parent);
		fdb.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		GlobalEvents.getInstance().addSelectionListener(this);
	}
	
	@Override
	public void setFocus(){}
	
	@Override
	public void dispose(){
		GlobalEvents.getInstance().removeSelectionListener(this);
		super.dispose();
	}
	
	/* 2 Methoden des Selection listeners */
	public void selectionEvent(PersistentObject obj){
		if (obj instanceof Fall) {
			fdb.setFall((Fall) obj);
		} else if (obj instanceof Patient) {
			// Fall der letzten Konsultation waehlen, falls aktueller Fall nicht zum Patienten
			// gehoert
			// (siehe KonsDetailView.selectionEvent())
			Patient patient = (Patient) obj;
			Fall selectedFall = GlobalEvents.getSelectedFall();
			
			if (selectedFall == null || !selectedFall.getPatient().equals(patient)) {
				
				Konsultation letzteKons = patient.getLetzteKons(false);
				if (letzteKons != null) {
					fdb.setFall(letzteKons.getFall());
				} else {
					fdb.setFall(null);
				}
			}
		}
	}
	
	public void clearEvent(Class template){
		if (template.equals(Patient.class) || template.equals(Fall.class)) {
			fdb.setFall(null);
		}
	}
	
	/***********************************************************************************************
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	public void doSave(IProgressMonitor monitor){ /* leer */}
	
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
	
}
