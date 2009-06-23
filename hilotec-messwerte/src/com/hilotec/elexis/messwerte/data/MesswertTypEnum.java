/*******************************************************************************
 * Copyright (c)2009, A. Kaufmann and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    A. Kaufmann - initial implementation 
 *    
 * $Id: MesswertTypEnum.java 5386 2009-06-23 11:34:17Z rgw_ch $
 *******************************************************************************/

package com.hilotec.elexis.messwerte.data;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;


/**
 * @author Antoine Kaufmann
 */
public class MesswertTypEnum extends MesswertBase implements IMesswertTyp {
	int defVal = 0;
	
	/**
	 * Bezeichnungen fuer die einzelnen Auswahlmoeglichkeiten
	 */
	ArrayList<String> choices = new ArrayList<String>();
	
	/**
	 * Werte fuer die Auswahlmoeglichkeiten. (notwendig, da die Combo nur
	 * fortlaufende Werte nimmt.
	 */
	ArrayList<Integer> values = new ArrayList<Integer>();
	
	public MesswertTypEnum(String n, String t, String u) {
		super(n, t, u);
	}
	
	public String erstelleDarstellungswert(Messwert messwert) {
		int wert = Integer.parseInt(messwert.getWert());
		for (int i = 0; i < values.size(); i++) {
			if (values.get(i) == wert) {
				return choices.get(i);
			}
		}
		return "";
	}

	public String getDefault() {
		return Double.toString(defVal);
	}

	public void setDefault(String str) {
		defVal = Integer.parseInt(str);
	}
	
	/**
	 * Neue Auswahlmoeglichkeit fuer dieses Enum-Feld anfuegen
	 * 
	 * @param c Beschriftung dieser Auswahlmoeglichkeit
	 * @param v Wert fuer diese Auswahlmoeglichkeit
	 */
	public void addChoice(String c, int v) {
		choices.add(c);
		values.add(v);
	}
	
	public Widget createWidget(Composite parent, Messwert messwert) {
		Combo combo = new Combo(parent, SWT.DROP_DOWN);
		for (int i = 0; i < choices.size(); i++) {
			combo.add(choices.get(i), i);
		}
		
		int wert = Integer.parseInt(messwert.getWert());
		for (int i = 0; i < values.size(); i++) {
			if (values.get(i).compareTo(wert) == 0) {
				combo.select(i);
				break;
			}
		}
		
		return combo;
	}
	
	public void saveInput(Widget widget, Messwert messwert) {
		Combo combo = (Combo) widget;
		messwert.setWert(Integer.toString(values.get(
			combo.getSelectionIndex())));
	}
}
