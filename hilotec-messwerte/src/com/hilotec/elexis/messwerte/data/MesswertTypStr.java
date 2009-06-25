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
 * $Id: MesswertTypStr.java 5405 2009-06-25 08:39:05Z freakypenguin $
 *******************************************************************************/

package com.hilotec.elexis.messwerte.data;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swt.widgets.Text;

import ch.elexis.util.SWTHelper;

/**
 * @author Antoine Kaufmann
 */
public class MesswertTypStr extends MesswertBase implements IMesswertTyp {
	String  defVal = "";
	
	/**
	 * Anzahl Zeilen, die das Textfeld haben soll
	 */
	int lines = 1;
	
	public MesswertTypStr(String n, String t, String u) {
		super(n, t, u);
	}
	
	public String erstelleDarstellungswert(Messwert messwert) {
		return messwert.getWert();
	}

	public String getDefault() {
		return defVal;
	}
	
	public void setDefault(String def) {
		defVal = def;
	}
	
	/**
	 * Anzahl der anzuzeigenden Zeilen setzen
	 */
	public void setLines(int l) {
		lines = l;
	}

	public Widget createWidget(Composite parent, Messwert messwert) {
		Text text = SWTHelper.createText(parent, lines, SWT.NONE);
		text.setText(messwert.getWert());
		return text;
	}
	
	public void saveInput(Widget widget, Messwert messwert) {
		Text text = (Text) widget;
		messwert.setWert(text.getText());
	}
}
