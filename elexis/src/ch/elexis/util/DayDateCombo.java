/*******************************************************************************
 * Copyright (c) 2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: DayDateCombo.java 4424 2008-09-21 13:56:56Z rgw_ch $
 *******************************************************************************/
package ch.elexis.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Spinner;

import ch.elexis.Desk;
import ch.rgw.tools.TimeTool;

import com.tiff.common.ui.datepicker.DatePickerCombo;

/**
 * A Composite with a spinner indicating a number of days and a DatePicker indicating the resulting
 * date from a base date and the spinner setting. Manipulating the spinner will modify the
 * DatePicker and vice versa. SelectionListeners will be informed on each change.
 * 
 * @author Gerry
 * 
 */
public class DayDateCombo extends Composite {
	private Spinner spinner;
	private DatePickerCombo dp;
	private SpinnerListener spl;
	private DateListener dl;
	private TimeTool baseDate;
	
	/**
	 * Create the Composite
	 * @param parent parent composite
	 * @param text1 the text to display in front of the spinner
	 * @param text2 the text to display between spinner and DatePicker
	 */
	public DayDateCombo(Composite parent, String text1, String text2){
		super(parent, SWT.NONE);
		
		setLayout(new RowLayout(SWT.HORIZONTAL));
		Desk.getToolkit().createLabel(this, text1);
		spl = new SpinnerListener();
		dl = new DateListener();
		spinner = new Spinner(this, SWT.NONE);
		Desk.getToolkit().createLabel(this, text2);
		dp = new DatePickerCombo(this, SWT.NONE);
		baseDate = new TimeTool();
		setListeners();
	}
	
	/**
	 * Set the dates of the composite
	 * @param baseDate the date from which to calculate
	 * @param endDate the initial setting of the DatePicker
	 */
	public void setDates(TimeTool baseDate, TimeTool endDate){
		this.baseDate = new TimeTool(baseDate);
		removeListeners();
		dp.setDate(endDate.getTime());
		int days = baseDate.secondsTo(endDate) / 86400;
		spinner.setValues(days, 0, 999, 0, 1, 10);
		setListeners();
	}
	
	/**
	 * Get the aktual setting of the DatePicker
	 * @return a TimeTool with the DatePicker's date
	 */
	public TimeTool getDate(){
		return new TimeTool(dp.getDate().getTime());
	}
	
	private void setListeners(){
		spinner.addModifyListener(spl);
		dp.addSelectionListener(dl);
	}
	
	private void removeListeners(){
		spinner.removeModifyListener(spl);
		dp.removeSelectionListener(dl);
	}
	
	class SpinnerListener implements ModifyListener {
		
		public void modifyText(ModifyEvent me){
			removeListeners();
			int d = spinner.getSelection();
			TimeTool nt = new TimeTool(baseDate);
			nt.addHours(d * 24);
			dp.setDate(nt.getTime());
			Event e = new Event();
			e.time = me.time;
			notifyListeners(SWT.DefaultSelection, e);
			setListeners();
		}
		
	}
	
	class DateListener extends SelectionAdapter {
		
		@Override
		public void widgetSelected(SelectionEvent se){
			removeListeners();
			TimeTool nt = new TimeTool(dp.getDate().getTime());
			int days = baseDate.secondsTo(nt) / 86400;
			spinner.setValues(days, 0, 999, 0, 1, 10);
			Event e = new Event();
			e.time = se.time;
			notifyListeners(SWT.DefaultSelection, e);
			setListeners();
		}
		
	}
	
}
