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
 *  $Id: DatumEingabeDialog.java 2348 2007-05-07 14:57:47Z rgw_ch $
 *******************************************************************************/
package ch.elexis.buchhaltung.kassenbuch;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

import com.tiff.common.ui.datepicker.DatePicker;

public class DatumEingabeDialog extends TitleAreaDialog {
	DatePicker dpVon, dpBis;
	TimeTool ttVon,ttBis;
	
	public DatumEingabeDialog(Shell parentShell, TimeTool von, TimeTool bis) {
		super(parentShell);
		ttVon=von==null ? null : new TimeTool(von);
		ttBis=bis==null ? null : new TimeTool(bis);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(2,true));
		new Label(ret,SWT.NONE).setText("Von:");
		new Label(ret,SWT.NONE).setText("Bis:");
		dpVon=new DatePicker(ret,SWT.NONE);
		dpBis=new DatePicker(ret,SWT.NONE);
		if(ttVon!=null){
			dpVon.setDate(ttVon.getTime());
		}
		if(ttBis!=null){
			dpBis.setDate(ttBis.getTime());
		}
		return ret;
	}

	@Override
	public void create() {
		super.create();
		setMessage("Bitte geben Sie den gewünschten Zeitraum ein oder drücken Sie 'Abbrechen'.");
		setTitle("Anzeigezeitraum für Kassenbuch");
		getShell().setText("Elexis Kassenbuch");
	}

	@Override
	protected void okPressed() {
		ttVon=new TimeTool(dpVon.getDate().getTime());
		ttBis=new TimeTool(dpBis.getDate().getTime());
		super.okPressed();
	}

	
}
