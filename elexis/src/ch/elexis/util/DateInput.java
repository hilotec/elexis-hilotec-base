/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: DateInput.java 5321 2009-05-28 12:06:28Z rgw_ch $
 *******************************************************************************/

package ch.elexis.util;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.rgw.tools.TimeTool;

import com.tiff.common.ui.datepicker.DatePickerCombo;

public class DateInput extends Composite {
	DatePickerCombo dpc;
	
	public DateInput(final Composite parent){
		super(parent,SWT.NONE);
		setLayout(new FillLayout());
		dpc=new DatePickerCombo(this, SWT.BORDER);
	}
	public DateInput(final Composite parent, final String label){
		super(parent,SWT.NONE);
		setLayout(new GridLayout());
		new Label(this,SWT.NONE).setText(label);
		dpc=new DatePickerCombo(this, SWT.BORDER);
		dpc.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
	}
	
	public DateInput(final Composite parent, final String label, final String date){
		this(parent,label);
		TimeTool tt=new TimeTool(date);
		dpc.setDate(tt.getTime());
	}
	
	public TimeTool getDate(){
		Date d=dpc.getDate();
		return d==null ? null : new TimeTool(d.getTime());
	}
}
