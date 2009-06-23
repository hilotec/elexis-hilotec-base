/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    A. Kaufmann - initial implementation 
 *    
 * $Id$
 *******************************************************************************/

package com.hilotec.elexis.messwerte.data;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

import ch.elexis.data.Patient;

/**
 * @author Antoine Kaufmann
 */
public class MesswertTypData extends MesswertBase implements IMesswertTyp {
	/**
	 * Messungstyp der Messungen die ausgewaehlt werden koennen
	 */
	String refType;
	
	/**
	 * Liste mit den moeglichen Auswahlen fuer die Combo. Notwendig damit dem
	 * Index beim saveInput() auch wieder der passende Messung zugeordnet
	 * werden kann.
	 */
	List<Messung> refChoices;
	
	public MesswertTypData(String n, String t, String u) {
		super(n, t, u);
	}
	
	public String erstelleDarstellungswert(Messwert messwert) {
		if (messwert.getWert().equals("")) {
			return "";
		}
		Messung m = new Messung(messwert.getWert());
		return  m.getDatum();
	}

	public String getDefault() {
		return "";
	}

	public void setDefault(String str) {
	}
	
	/**
	 * Typ der auswaehlbaren Messungen setzen
	 * 
	 * @param t Typ
	 */
	public void setRefType(String t) {
		refType = t;
	}
	
	public Widget createWidget(Composite parent, Messwert messwert) {
		Patient patient = messwert.getMessung().getPatient();
		Combo combo = new Combo(parent, SWT.DROP_DOWN);
		
		refChoices = Messung.getPatientMessungen(patient,
			MessungKonfiguration.getInstance().getTypeByName(refType));
		for (int i = 0; i < refChoices.size(); i++) {
			Messung messung = refChoices.get(i);
			combo.add(messung.getDatum(), i);
		}
		
		if  (!messwert.getWert().equals("")) {
			for (int i = 0; i < refChoices.size(); i++) {
				if (refChoices.get(i).getId().equals(messwert.getWert())) {
					combo.select(i);
				}
			}
		} else if (refChoices.size() > 0) {
			combo.select(0);
		}
		
		return combo;
	}
	
	public void saveInput(Widget widget, Messwert messwert) {
        Combo combo = (Combo) widget;
        int selected = combo.getSelectionIndex();
        messwert.setWert(refChoices.get(selected).getId());
	}
	
	/**
	 * Messung zu einem Data-Messwert heraussuchen
	 * 
	 * @param messwert Messwert
	 * 
	 * @return Messwert oder null, wenn noch keine Messung zugewiesen ist
	 */
	public Messung getMessung(Messwert messwert) {
		if (messwert.getWert().equals("")) {
			return null;
		}
		
		return Messung.load(messwert.getWert());
	}
}
