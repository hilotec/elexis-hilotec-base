/*******************************************************************************
 * Copyright (c) 2009, A. Kaufmann and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    A. Kaufmann - initial implementation 
 *    
 * $Id: MesswertTypBool.java 5386 2009-06-23 11:34:17Z rgw_ch $
 *******************************************************************************/

package com.hilotec.elexis.messwerte.data;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;

/**
 * @author Antoine Kaufmann
 */
public class MesswertTypBool extends MesswertBase implements IMesswertTyp {
	boolean defVal;
	
	MesswertTypBool(String n, String t, String u) {
		super(n, t, u);
		defVal = false;
	}
	
	public String erstelleDarstellungswert(Messwert messwert) {
		return (Boolean.parseBoolean(messwert.getWert()) ? "Ja" : "Nein");
	}

	public String getDefault() {
		return Boolean.toString(defVal);
	}
	
	public void setDefault(String def) {
		defVal = Boolean.parseBoolean(def);
	}
	
	public Widget createWidget(Composite parent, Messwert messwert) {
		Button button = new Button(parent, SWT.CHECK);
		button.setSelection(Boolean.parseBoolean(messwert.getWert()));
		return button;
	}

	public void saveInput(Widget widget, Messwert messwert) {
		Button button = (Button) widget;
		messwert.setWert(Boolean.toString(button.getSelection()));
	}
}
