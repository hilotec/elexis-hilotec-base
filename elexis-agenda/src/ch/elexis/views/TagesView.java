/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: TagesView.java 1247 2006-11-07 20:08:28Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import ch.elexis.Desk;
import ch.elexis.actions.Activator;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.agenda.Messages;
import ch.elexis.data.*;
import ch.elexis.dialogs.DateSelectorDialog;
import ch.elexis.util.Plannables;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.*;

public class TagesView extends BaseAgendaView{
	public static final String ID="ch.elexis.agenda.tagesview"; //$NON-NLS-1$
	Button  bDay, bToday, bPrint;
	Text tDetail;

	Label lCreator;
	
	public TagesView(){
		self=this;
	}
	@Override
	public void create(Composite parent) {
		parent.setLayout(new GridLayout());
		Composite top=new Composite(parent,SWT.NONE);
		top.setLayout(new GridLayout(5,false));
		top.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		actDate=new TimeTool();
		bToday=new Button(top,SWT.CENTER|SWT.PUSH|SWT.FLAT);
		bToday.setImage(Desk.theImageRegistry.get(Activator.IMG_HOME));
		bToday.setToolTipText(Messages.TagesView_showToday); 
		bToday.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				TimeTool dat=new TimeTool();
				actDate.set(dat);
				updateDate();
			}
			
		});

		Button bMinus=new Button(top,SWT.PUSH);
		bMinus.setToolTipText(Messages.TagesView_previousDay); 
		bMinus.setText("<"); //$NON-NLS-1$
		bMinus.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				//TimeTool dat=Activator.getDefault().theDay;
				actDate.addHours(-24);
				updateDate();
			}
		});

		bDay=new Button(top,SWT.CENTER|SWT.PUSH|SWT.FLAT);
		bDay.setToolTipText(Messages.TagesView_selectDay); 
		bDay.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bDay.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				DateSelectorDialog dsl=new DateSelectorDialog(bDay.getShell(),actDate);
				//Point pt=bDay.getLocation();
				//dsl.getShell().setLocation(pt.x, pt.y);
				dsl.create();
				Point m=Desk.theDisplay.getCursorLocation();
				dsl.getShell().setLocation(m.x,m.y);
				if(dsl.open()==Dialog.OK){
					TimeTool dat=dsl.getSelectedDate();
					actDate.set(dat);
					updateDate();
				}
			}
			
		});
		bDay.setText(actDate.toString(TimeTool.DATE_GER));
		Button bPlus=new Button(top,SWT.PUSH);
		bPlus.setToolTipText(Messages.TagesView_nextDay); 
		
		bPlus.setText(">"); //$NON-NLS-1$
		bPlus.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				actDate.addHours(24);
				updateDate();
			}
		});
		
		Button bPrint=new Button(top,SWT.CENTER|SWT.PUSH|SWT.FLAT);
		bPrint.setImage(Desk.theImageRegistry.get(Desk.IMG_PRINT));
		bPrint.setToolTipText(Messages.TagesView_printDay); 
		bPrint.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				printAction.run();
			}
		});
		
		SashForm sash=new SashForm(parent,SWT.VERTICAL);
		sash.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tv=new TableViewer(sash,SWT.NONE);
		tv.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tv.setLabelProvider(new AgendaLabelProvider());
		
		tDetail=new Text(sash,SWT.MULTI|SWT.BORDER|SWT.WRAP);
		lCreator=new Label(parent,SWT.NONE);
		lCreator.setFont(Desk.theFontRegistry.get(Desk.FONT_SMALL));
		lCreator.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		lCreator.setText(" - ");
		
		sash.setWeights(new int[]{80,20});
		makeActions();		
	}

	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
	public void dispose(){
		GlobalEvents.getInstance().removeBackingStoreListener(this);
		GlobalEvents.getInstance().removeActivationListener(this, getViewSite().getPart());
	}
	
	
	
	class AgendaLabelProvider extends LabelProvider implements ITableColorProvider, ITableLabelProvider{

		public Color getBackground(Object element, int columnIndex) {
			if(element instanceof IPlannable){
				IPlannable p=(IPlannable)element;
				return Plannables.getStatusColor(p);
			}
			return null;
		}

		public Color getForeground(Object element, int columnIndex) {
			if(element instanceof IPlannable){
				IPlannable p=(IPlannable) element;
				return SWTHelper.getContrast(Plannables.getTypColor(p));
			}
			return null;
		}

		public Image getColumnImage(Object element, int columnIndex) {
			if(element instanceof IPlannable){
				IPlannable p=(IPlannable)element;
				return Plannables.getTypImage(p);
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if(element instanceof IPlannable){
				IPlannable p=(IPlannable)element;
				StringBuilder sb=new StringBuilder();
				sb.append(Plannables.getStartTimeAsString(p)).append("-") //$NON-NLS-1$
					.append(Plannables.getEndTimeAsString(p)).append(" ").append(p.getTitle()); //$NON-NLS-1$
				return sb.toString();
			}
			return "?"; //$NON-NLS-1$
		}
		
	}


		
	public void updateDate(){
		pinger.doSync();
		bDay.setText(actDate.toString(TimeTool.WEEKDAY)+", "+actDate.toString(TimeTool.DATE_GER)); //$NON-NLS-1$
		tv.refresh();
	}
	
	@Override
	public void setTermin(Termin t) {
		Patient pat=t.getPatient();
		StringBuilder sb=new StringBuilder(200);
		TimeSpan ts=t.getTimeSpan();
		sb.append(ts.from.toString(TimeTool.TIME_SMALL)).append("-").append(ts.until.toString(TimeTool.TIME_SMALL)) //$NON-NLS-1$
		.append(" ").append(t.getPersonalia()).append("\n(") //$NON-NLS-1$ //$NON-NLS-2$
		.append(t.getType()).append(",").append(t.getStatus()).append(")\n--------\n").append(t.getGrund()); //$NON-NLS-1$ //$NON-NLS-2$
		tDetail.setText(sb.toString());
		GlobalEvents ev=GlobalEvents.getInstance();
		sb.setLength(0);
		sb.append(StringTool.unNull(t.get("ErstelltVon"))).append("/")
		.append(t.getCreateTime().toString(TimeTool.FULL_GER));
		lCreator.setText(sb.toString());
		ev.fireSelectionEvent(t);
		if(pat!=null){
			ev.fireSelectionEvent(pat);
			Konsultation kons=GlobalEvents.getSelectedKons();

			String sVgl=actDate.toString(TimeTool.DATE_COMPACT);
			if((kons==null) || 	// Falls nicht die richtige Kons selektiert ist, passende Kons für heute suchen
					!(kons.getFall().getPatient().getId().equals(pat.getId())) || 
					!(new TimeTool(kons.getDatum()).toString(TimeTool.DATE_COMPACT).equals(sVgl))){
				Fall[] faelle=pat.getFaelle();
				TimeTool ttVgl=new TimeTool();							
				for(Fall f:faelle){
					Konsultation[] konsen=f.getBehandlungen(true);
					for(Konsultation k:konsen){
						ttVgl.set(k.getDatum());
						if(ttVgl.toString(TimeTool.DATE_COMPACT).equals(sVgl)){
							ev.fireSelectionEvent(k);
							return;
						}
					}
				}
				
			}
			
		}		

		
	}
	private void makeActions(){
		newViewAction=new Action("Neues Fenster"){
			@Override
			public void run(){
				try {
					getViewSite().getPage().showView(ID, StringTool.unique("Agenda"), IWorkbenchPage.VIEW_VISIBLE);
				} catch (PartInitException e) {
					ExHandler.handle(e);
				}
			}
		};
	}
}
