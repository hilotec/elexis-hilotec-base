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
 *  $Id: DayBar.java 5282 2009-05-09 14:55:35Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import ch.elexis.actions.Activator;
import ch.elexis.actions.AgendaActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.agenda.data.IPlannable;
import ch.elexis.agenda.data.Termin;
import ch.elexis.dialogs.TerminDialog;
import ch.elexis.util.Plannables;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

public class DayBar extends Composite {
	int height, width;
	double pixelPerDay;
	DayBar self=this;
	AgendaWeek container;
	private Termin actTermin;
	private TimeTool myDay;
	Activator agenda=Activator.getDefault();
	
	public DayBar(AgendaWeek parent){
		super(parent.cWeekDisplay,SWT.BORDER);
		container=parent;
		myDay=new TimeTool();
		this.addControlListener(new ControlListener(){

			public void controlMoved(ControlEvent e) {
				// don't mind
			}

			public void controlResized(ControlEvent e) {
				Rectangle rec=getBounds();
				height=rec.height;
				width=rec.width;
				recalc();
			}});
		setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		//setBackground(Desk.theColorRegistry.get(Desk.COL_GREEN));
		addMouseListener(new MouseAdapter(){
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				agenda.setActDate(myDay);
				new TerminDialog(null).open();
				recalc();
			}});
	}
	

	void recalc(){
		container.recalc();
		pixelPerDay=Math.max(width-10,5);
		for(Control ctrl:getChildren()){
			if(ctrl instanceof TerminFeld){
				((TerminFeld)ctrl).recalc();
			}
		}
		
	}
	public void set(TimeTool day, String bereich){
		List<IPlannable> dayList=Plannables.loadTermine(bereich, day);
		for(Control c:getChildren()){
			c.dispose();
		}
		for(IPlannable ip:dayList){
			/*TerminFeld f=*/ new TerminFeld(this,(Termin)ip);
		}
		myDay.set(day);
		recalc();
	}
	
	class TerminFeld extends Composite{
		Termin t;
		DayBar mine;
		Label myLabel;
		TerminFeld(final DayBar parent, final Termin termin){
			super(parent,SWT.BORDER);
			setLayout(new FillLayout());
			t=termin;
			myLabel=new Label(this,SWT.WRAP);
			myLabel.setBackground(Plannables.getTypColor(t));
			myLabel.setForeground(Plannables.getStatusColor(t));
			myLabel.setText(t.getTitle());
			StringBuilder sb=new StringBuilder();
			sb.append(termin.getLabel()).append("\n")
				.append(termin.getGrund());
			myLabel.addMouseListener(new MouseAdapter(){
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					agenda.setActDate(t.getDay());
					new TerminDialog(t).open();
					parent.recalc();
				}
			});
			myLabel.addMouseMoveListener(new MouseMoveListener(){
				public void mouseMove(MouseEvent e) {
					actTermin=t;
					GlobalEvents.getInstance().fireSelectionEvent(actTermin);
					System.out.println(actTermin.dump());	
				}
				
			});
			myLabel.setToolTipText(sb.toString());
			mine=parent;
			MenuManager manager=new MenuManager();
			manager.add(AgendaActions.terminStatusAction);
			manager.add(container.terminKuerzenAction);
			manager.add(container.terminVerlaengernAction);
			manager.add(container.terminAendernAction);
			manager.add(AgendaActions.delTerminAction);
			myLabel.setMenu(manager.createContextMenu(parent));
			
			//setBounds(0, 0, 20, 20);
		}

		public void recalc() {
			int anzeigeDauer=0;
			int terminStart=t.getStartMinute();
			int terminDauer=t.getDurationInMinutes();
			int terminEnde=terminStart+terminDauer;
			if(terminStart<container.tagStart){
				terminStart=container.tagStart;
			}
			if(terminEnde>container.tagEnde){
				terminEnde=container.tagEnde;
			}
			anzeigeDauer=Math.max(0,terminEnde-terminStart);
			int h=(int)Math.round(anzeigeDauer*container.pixelPerMinute);
			int beg=(int)Math.round((terminStart-container.tagStart)*container.pixelPerMinute);
			setBounds(0, beg, (int)Math.round(pixelPerDay), h);
			layout();
		}
		
		
	}
}
