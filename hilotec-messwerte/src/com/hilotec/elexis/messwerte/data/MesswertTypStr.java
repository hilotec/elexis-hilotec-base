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

	public Widget createWidget(Composite parent, Messwert messwert) {
		Text text = SWTHelper.createText(parent, 1, SWT.NONE);
		text.setText(messwert.getWert());
		return text;
	}
	
	public void saveInput(Widget widget, Messwert messwert) {
		Text text = (Text) widget;
		messwert.setWert(text.getText());
	}
}
