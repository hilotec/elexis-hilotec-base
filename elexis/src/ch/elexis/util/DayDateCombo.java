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
 *  $Id: DayDateCombo.java 4425 2008-09-21 15:50:10Z rgw_ch $
 *******************************************************************************/
package ch.elexis.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TypedListener;

import ch.elexis.Desk;
import ch.rgw.tools.StringTool;
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
		if(endDate==null){
			dp.setDate(null);
			spinner.setValues(0, 0, 999, 0, 1, 10);
		}else{
			dp.setDate(endDate.getTime());
			int days = baseDate.secondsTo(endDate) / 86400;
			spinner.setValues(days+1, 0, 999, 0, 1, 10);
		}
		setListeners();
	}
	
	/**
	 * Get the aktual setting of the DatePicker
	 * @return a TimeTool with the DatePicker's date
	 */
	public TimeTool getDate(){
		if(StringTool.isNothing(dp.getText())){
			return null;
		}
		return new TimeTool(dp.getDate().getTime());
	}
	
	public void addSelectionListener(SelectionListener listener){
		checkWidget();

		if (listener == null)
		{
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Selection, typedListener);
		addListener(SWT.DefaultSelection, typedListener);

	}
	
	public void removeSelectionListener(SelectionListener listener)
	{
		checkWidget();

		if (listener == null)
		{
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		removeListener(SWT.Selection, listener);
		removeListener(SWT.DefaultSelection, listener);
	}

	private void setListeners(){
		spinner.addModifyListener(spl);
		dp.addSelectionListener(dl);
		dp.addModifyListener(dl);
	}
	
	private void removeListeners(){
		spinner.removeModifyListener(spl);
		dp.removeSelectionListener(dl);
		dp.removeModifyListener(dl);
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
			notifyListeners(SWT.Selection, e);
			setListeners();
		}
		
	}
	
	class DateListener extends SelectionAdapter implements ModifyListener {
		
		@Override
		public void widgetSelected(SelectionEvent se){
			removeListeners();
			TimeTool nt = new TimeTool(dp.getDate().getTime());
			int days = baseDate.secondsTo(nt) / 86400;
			spinner.setValues(days, 0, 999, 0, 1, 10);
			Event e = new Event();
			e.time = se.time;
			notifyListeners(SWT.Selection, e);
			setListeners();
		}

		public void modifyText(ModifyEvent me){
			String t=dp.getText();
			Event e = new Event();
			e.time = me.time;
			notifyListeners(SWT.Selection, e);
		}
		
	}
	
}
