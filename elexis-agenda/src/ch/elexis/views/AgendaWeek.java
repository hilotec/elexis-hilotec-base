/*******************************************************************************
 * Copyright (c) 2007-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: AgendaWeek.java 3731 2008-03-19 21:41:32Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.AgendaActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.agenda.data.Termin;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class AgendaWeek extends BaseAgendaView{
	public static final String ID="ch.elexis.agenda.week";
	DayBar[] days;
	Composite cLeft, cRight, cBounding;
	Button bCal;
	ScrolledForm form;
	FormToolkit tk=Desk.theToolkit;
	TimePainter tp=new TimePainter();
	int ts,te, tagStart, tagEnde; 
	double pixelPerMinute;
	Composite cWeekDisplay;
	Label[] dayLabels=new Label[7];
	TimeTool actWeek=new TimeTool();
	AgendaWeekListener awl;
	
	@Override
	public void createPartControl(Composite parent) {
		
		days=new DayBar[TimeTool.Wochentage.length];
		form=tk.createScrolledForm(parent);
		cBounding=form.getBody();
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		cBounding.setLayout(new GridLayout(3,false));
		cBounding.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Button bBack=tk.createButton(cBounding,"<",SWT.PUSH);
		bBack.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				TimeTool lastWeek=new TimeTool(actWeek);
				System.out.println(actWeek.get(TimeTool.WEEK_OF_YEAR)+","+actWeek.get(TimeTool.DAY_OF_WEEK));
				lastWeek.add(TimeTool.WEEK_OF_YEAR, -1);
				System.out.println(actWeek.get(TimeTool.WEEK_OF_YEAR)+","+actWeek.get(TimeTool.DAY_OF_WEEK));
				setWeek(lastWeek);
				//cWeekDisplay.redraw();
				System.out.println(actWeek.get(TimeTool.WEEK_OF_YEAR)+","+actWeek.get(TimeTool.DAY_OF_WEEK));
			}
			
		});
		bCal=tk.createButton(cBounding,"",SWT.PUSH|SWT.CENTER);
		bCal.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Button bFwd=tk.createButton(cBounding,">",SWT.PUSH);
		bFwd.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				TimeTool nextWeek=new TimeTool(actWeek);
				nextWeek.add(TimeTool.WEEK_OF_YEAR,1);
				System.out.println(actWeek.get(TimeTool.WEEK_OF_YEAR)+","+actWeek.get(TimeTool.DAY_OF_WEEK));
				setWeek(nextWeek);
				//cWeekDisplay.redraw();
				System.out.println(actWeek.get(TimeTool.WEEK_OF_YEAR)+","+actWeek.get(TimeTool.DAY_OF_WEEK));				
			}
			
		});

		cLeft=tk.createComposite(cBounding);
		//cLeft.setLayoutData(SWTHelper.getFillGridData(1, false, 1, true));
		Point size=SWTHelper.getStringBounds(cLeft, "00:00");
		GridData gd=new GridData(GridData.FILL_VERTICAL);
		gd.widthHint=size.x+2;
		cLeft.setLayoutData(gd);
		cLeft.addPaintListener(tp);
		cWeekDisplay=new Composite(cBounding,SWT.BORDER);
		cRight=tk.createComposite(cBounding);
		//cRight.setLayoutData(SWTHelper.getFillGridData(1, false, 1, true));
		cRight.addPaintListener(tp);
		size=SWTHelper.getStringBounds(cRight, "00:00");
		gd=new GridData(GridData.FILL_VERTICAL);
		gd.widthHint=size.x+2;
		cRight.setLayoutData(gd);
		cWeekDisplay.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		cWeekDisplay.setLayout(new GridLayout(days.length,true));
		TimeTool ttWeek=new TimeTool();
		ttWeek.set(TimeTool.DAY_OF_WEEK,TimeTool.MONDAY);
		//bCal.setText(Integer.toString(ttWeek.get(TimeTool.WEEK_OF_YEAR))+". Woche");
		for(int d=0;d<days.length;d++){
			dayLabels[d]=new Label(cWeekDisplay,SWT.NONE);
			ttWeek.addHours(24);
		}
		

		for(int d=0;d<days.length;d++){
			days[d]=new DayBar(this);
		}
		makePrivateActions();
		actDate=new TimeTool(actWeek);
		setWeek(actWeek);
		awl=new AgendaWeekListener(this);
	}

	public void setWeek(TimeTool ttContained){
		ttContained.set(TimeTool.DAY_OF_WEEK,TimeTool.MONDAY);
		actWeek.set(ttContained);
		System.out.println("setWeek: "+actWeek.get(TimeTool.WEEK_OF_YEAR)+","+actWeek.get(TimeTool.DAY_OF_WEEK));
		bCal.setText(Integer.toString(actWeek.get(TimeTool.WEEK_OF_YEAR))+". Woche");
		for(int i=0;i<days.length;i++){
			days[i].set(ttContained, actBereich);
			dayLabels[i].setText(ttContained.toString(TimeTool.WEEKDAY)+", "+ttContained.toString(TimeTool.DATE_GER));
			ttContained.addHours(24);
		}
		actDate.set(actWeek);
	}
	@Override
	public void create(Composite parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTermin(Termin t) {
		// TODO Auto-generated method stub

	}

	public void reload() {
		setWeek(new TimeTool(actWeek));
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
		
			Point abs=cWeekDisplay.toDisplay(days[0].getLocation());
			Point rel=cLeft.toControl(abs);
			int y=rel.y;
			TimeTool runner=new TimeTool();
			runner.set("07:00");
			TimeTool limit=new TimeTool(runner);
			limit.addHours(tagEnde-tagStart);
			int quarter=(int)Math.round(15.0*pixelPerMinute);
			while(runner.isBefore(limit)){
				gc.drawLine(0, y, e.width, y);
				gc.drawText(runner.toString(TimeTool.TIME_SMALL), 0, y+1);
				y+=quarter;
				gc.drawLine(e.width-3, y, e.width, y);
				y+=quarter;
				gc.drawLine(e.width-6, y, e.width, y);
				y+=quarter;
				gc.drawLine(e.width-3, y, e.width, y);
				y+=quarter;
				runner.addHours(1);
			}
		}
		
	}
	private void makePrivateActions(){
		newViewAction=new Action("Neues Fenster"){
			@Override
			public void run(){
				try {
					getViewSite().getPage().showView(ID, StringTool.unique("AgendaWeek"), IWorkbenchPage.VIEW_VISIBLE);
				} catch (PartInitException e) {
					ExHandler.handle(e);
				}
			}
		};
		makeActions();
	}
	 
}
