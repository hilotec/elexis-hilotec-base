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
 *  $Id: AgendaWeek.java 3480 2007-12-25 10:28:13Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.data.Termin;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

public class AgendaWeek extends BaseAgendaView{
	DayBar[] days;
	Composite cLeft, cRight;
	Button bCal;
	ScrolledForm form;
	FormToolkit tk=Desk.theToolkit;
	TimePainter tp=new TimePainter();
	int ts,te, tagStart, tagEnde; 
	double pixelPerMinute;
	Composite cWeekDisplay;
	TimeTool actWeek=new TimeTool();
	
	@Override
	public void createPartControl(Composite parent) {
	
		days=new DayBar[TimeTool.Wochentage.length];
		form=tk.createScrolledForm(parent);
		Composite cBounding=form.getBody();
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		cBounding.setLayout(new GridLayout(3,false));
		cBounding.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Button bBack=tk.createButton(cBounding,"<",SWT.PUSH);
		bBack.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				actWeek.add(TimeTool.HOUR, -(7*24));
				setWeek(actWeek);
				cWeekDisplay.redraw();
			}
			
		});
		bCal=tk.createButton(cBounding,"",SWT.PUSH);
		Button bFwd=tk.createButton(cBounding,">",SWT.PUSH);
		bFwd.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				actWeek.add(TimeTool.HOUR, 7*24);
				setWeek(actWeek);
				cWeekDisplay.redraw();
				
			}
			
		});

		cLeft=tk.createComposite(cBounding);
		cLeft.setLayoutData(SWTHelper.getFillGridData(1, false, 1, true));
		cLeft.addPaintListener(tp);
		cWeekDisplay=new Composite(cBounding,SWT.BORDER);
		cRight=tk.createComposite(cBounding);
		cRight.setLayoutData(SWTHelper.getFillGridData(1, false, 1, true));
		cRight.addPaintListener(tp);
		cWeekDisplay.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		cWeekDisplay.setLayout(new GridLayout(days.length,true));
		TimeTool ttWeek=new TimeTool();
		ttWeek.set(TimeTool.DAY_OF_WEEK,TimeTool.MONDAY);
		bCal.setText(Integer.toString(ttWeek.get(TimeTool.WEEK_OF_YEAR))+". Woche");
		for(int d=0;d<days.length;d++){
			new Label(cWeekDisplay,SWT.NONE).setText(ttWeek.toString(TimeTool.WEEKDAY)+", "+ttWeek.toString(TimeTool.DATE_GER));
			ttWeek.addHours(24);
		}
		

		for(int d=0;d<days.length;d++){
			days[d]=new DayBar(this);
		}

		setWeek(actWeek);
	}

	public void setWeek(TimeTool ttContained){
		ttContained.set(TimeTool.DAY_OF_WEEK,TimeTool.MONDAY);
		actWeek.set(ttContained);
		bCal.setText(Integer.toString(ttContained.get(TimeTool.WEEK_OF_YEAR))+". Woche");
		for(int i=0;i<days.length;i++){
			days[i].set(ttContained, actBereich);
			ttContained.addHours(24);
		}
		
	}
	@Override
	public void create(Composite parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTermin(Termin t) {
		// TODO Auto-generated method stub

	}

	public void reloadContents(Class clazz) {
		

	}

	public void recalc(){
		ts=Hub.userCfg.get("agenda/dayView/Start",7); //$NON-NLS-1$
		te=Hub.userCfg.get("agenda/dayView/End",19); //$NON-NLS-1$
		tagStart=ts*60; // 7 Uhr
		tagEnde=te*60;
		int dayLen=tagEnde-tagStart;
		pixelPerMinute=(double)cWeekDisplay.getBounds().height/(double)dayLen;
	}
	
	class TimePainter implements PaintListener{

		public void paintControl(PaintEvent e) {
			GC gc=e.gc;
			Font font=gc.getFont();
			FontData fd=font.getFontData()[0];
			int h=fd.getHeight();
			int y=h+bCal.getBounds().height;
			TimeTool runner=new TimeTool();
			runner.set("07:00");
			TimeTool limit=new TimeTool(runner);
			limit.addHours(tagEnde-tagStart);
			while(runner.isBefore(limit)){
				gc.drawText(runner.toString(TimeTool.TIME_SMALL), 0, y);
				y+=Math.round(60.0*pixelPerMinute);
				runner.addHours(1);
			}
		}
		
	}
}
