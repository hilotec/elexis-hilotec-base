/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    A. Kaufmann - copied from befunde-Plugin and adapted to new data structure 
 *    
 * $Id$
 *******************************************************************************/

package com.hilotec.elexis.messwerte.views;

import java.util.HashMap;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import com.hilotec.elexis.messwerte.data.IMesswertTyp;
import com.hilotec.elexis.messwerte.data.Messung;
import com.hilotec.elexis.messwerte.data.Messwert;
import com.tiff.common.ui.datepicker.DatePickerCombo;

import ch.elexis.util.SWTHelper;

import ch.rgw.tools.TimeTool;

/**
 * Dialog um eine Messung zu bearbeiten oder neu zu erstellen
 * 
 * @author Antoine Kaufmann
 */
public class MessungBearbeiten extends TitleAreaDialog {
	private Messung messung;
	private HashMap<Messwert, Widget> widgetMap;
	private DatePickerCombo dateWidget;
	
	public MessungBearbeiten(final Shell parent, Messung m) {
		super(parent);
		messung = m;
		widgetMap = new HashMap<Messwert, Widget>();
	}
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite comp = new Composite(parent,SWT.NONE);

		comp.setLayout(new GridLayout());
		comp.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));

		dateWidget = new DatePickerCombo(comp, SWT.NONE);
		dateWidget.setDate(new TimeTool(messung.getDatum()).getTime());
		
		for (Messwert messwert: messung.getMesswerte()) {
			Label l = new Label(comp, SWT.NONE);
			IMesswertTyp dft = messwert.getTyp();
			String labelText = dft.getTitle();
			if (!dft.getUnit().equals("")) {
				labelText += " [" + dft.getUnit() + "]";
			}
			l.setText(labelText);
			
			widgetMap.put(messwert, dft.createWidget(comp, messwert));
		}
		
		return comp;
	}
	
	@Override
	public void create() {
		super.create();
		getShell().setText("Messung bearbeiten");
	}
	
	@Override
	public void okPressed() {
		TimeTool tt = new TimeTool(dateWidget.getDate().getTime());
		messung.setDatum(tt.toString(TimeTool.DATE_GER));
		for (Messwert mwrt: messung.getMesswerte()) {
			mwrt.getTyp().saveInput(widgetMap.get(mwrt), mwrt);
		}
		close();
	}
}
