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
 *  $Id: AgendaGross.java 3947 2008-05-22 18:33:28Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.agenda.Messages;
import ch.elexis.agenda.data.IPlannable;
import ch.elexis.agenda.data.TagesNachricht;
import ch.elexis.agenda.data.Termin;
import ch.elexis.agenda.preferences.PreferenceConstants;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.util.Plannables;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeSpan;
import ch.rgw.tools.TimeTool;

import com.tiff.common.ui.datepicker.DatePicker;

/**
 * A larger view for the agenda with more features than the compact "TagesView"
 * @author gerry
 *
 */
public class AgendaGross extends BaseAgendaView {
	public static final String ID= "ch.elexis.agenda.largeview";
	DatePicker cal;
	Composite cButtons;
	Composite right;
	Text dayMessage;
	Text terminDetail;
	Label lbDetails;
	Label lbDayString;
	private static final String[] columnTitles={"von","bis","Typ","Status","Personalien", "Grund"};
	private static final int[] columnWidths={30,30,50,70,300, 200};

	@Override
	public void create(Composite parent) {
		parent.setLayout(new FillLayout());
		actDate=new TimeTool();
		
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new FormLayout());
		//Button tv=new Button(ret,SWT.PUSH);
		cButtons=new Composite(ret,SWT.BORDER);
		cButtons.setLayout(new RowLayout());
		FormData fdTop=new FormData();
		fdTop.top=new FormAttachment(0,3);
		fdTop.left=new FormAttachment(0,3);
		fdTop.right=new FormAttachment(100,-3);
		cButtons.setLayoutData(fdTop);
		right=new Composite(ret,SWT.BORDER);
		FormData fdRight=new FormData();
		fdRight.right=new FormAttachment(100,-5);
		fdRight.top=new FormAttachment(cButtons,0);
		fdRight.bottom=new FormAttachment(100,-5);
		right.setLayoutData(fdRight);
		String[] bereiche=Hub.globalCfg.get(PreferenceConstants.AG_BEREICHE, Messages.TagesView_14).split(",");
		ChangeBereichAdapter chb=new ChangeBereichAdapter();
		for(String bereich:bereiche){
			Button bChange=new Button(cButtons,SWT.RADIO);
			bChange.setText(bereich);
			bChange.addSelectionListener(chb);
			if(bereich.equals(actBereich)){
				bChange.setSelection(true);
			}
		}
		tv=new TableViewer(ret,SWT.FULL_SELECTION|SWT.SINGLE);
		FormData fdTV=new FormData();
		fdTV.left=new FormAttachment(0,0);
		fdTV.top=new FormAttachment(cButtons,0);
		fdTV.right=new FormAttachment(right,4);
		fdTV.bottom=new FormAttachment(100,-4);
		//fdTV.bottom=new FormAttachment(0,0);
		tv.getControl().setLayoutData(fdTV);
		
		
		//fdRight.left=new FormAttachment(tv,5);
		//fdRight.bottom=new FormAttachment(0,0);
		
		right.setLayout(new GridLayout());
		cal=new DatePicker(right,SWT.NONE);
		cal.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cal.setDate(actDate.getTime());
		Button bToday=new Button(right,SWT.PUSH);
		bToday.setText("Heute");
		bToday.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bToday.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				actDate=new TimeTool();
				cal.setDate(actDate.getTime());
				updateDate();
			}
			
		});
		dayMessage=SWTHelper.createText(right, 4, SWT.NONE);
		dayMessage.addFocusListener(new FocusAdapter(){

			@Override
			public void focusLost(FocusEvent arg0) {
				String tx=dayMessage.getText();
				TagesNachricht tn=TagesNachricht.load(actDate);
				if(tn.exists()){
					tn.setLangtext(tx);
				}else{
					tn=new TagesNachricht(actDate," - ",tx);
				}
			}
			
		});
		terminDetail=SWTHelper.createText(right, 5, SWT.NONE);
		terminDetail.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		lbDetails=new Label(right,SWT.WRAP);
		lbDetails.setText("-");
		lbDetails.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		lbDayString=new Label(ret,SWT.NONE);
		FormData fdBottom=new FormData();
		fdBottom.left=new FormAttachment(0,0);
		fdBottom.bottom=new FormAttachment(100,0);
		fdBottom.right=new FormAttachment(100,0);
		
		tv.setLabelProvider(new AgendaLabelProvider());
		Table table=tv.getTable();
	
		GC gc=new GC(table);
		FontMetrics fm=gc.getFontMetrics();
		int average=fm.getAverageCharWidth();
		int nw=gc.getCharWidth('0');
		gc.dispose();
		columnWidths[0]=8*nw;
		columnWidths[1]=columnWidths[0];
		int w=10;
		for(int i=0;i<Termin.TerminTypes.length;i++){
			if(Termin.TerminTypes[i].length()>w){
				w=Termin.TerminTypes[i].length();
			}
		}
		columnWidths[2]=(w+1)*average;
		w=10;
		for(int i=0;i<Termin.TerminStatus.length;i++){
			if(Termin.TerminStatus[i].length()>w){
				w=Termin.TerminStatus[i].length();
			}
		}
		columnWidths[3]=(w+1)*average;
		for(int i=0;i<columnTitles.length;i++){
			TableColumn tc=new TableColumn(table,SWT.NONE);
			tc.setText(columnTitles[i]);
			tc.setWidth(columnWidths[i]);
		}
		table.setHeaderVisible(true);
		makePrivateActions();
		cal.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				actDate.setTime(cal.getDate());
				updateDate();
			}

		});
	}

	protected void updateDate() {
		TagesNachricht tn=TagesNachricht.load(actDate);
		lbDayString.setText("");
		dayMessage.setText("");
		if(tn.exists()){
			lbDayString.setText(tn.getZeile());
			dayMessage.setText(tn.getLangtext());
		}
		pinger.doSync();
		tv.refresh();
	}

	@Override
	public void setFocus() {
		tv.getControl().setFocus();
	}

	
	private class AgendaLabelProvider extends LabelProvider implements ITableColorProvider, ITableLabelProvider{

		public Color getBackground(Object element, int columnIndex) {
			if(element instanceof IPlannable){
				IPlannable p=(IPlannable)element;
				if(columnIndex==3){
					return Plannables.getStatusColor(p);
				}else{
					return Plannables.getTypColor(p);
				}
			}
			return null;
		}

		public Color getForeground(Object element, int columnIndex) {
			if(element instanceof IPlannable){
				IPlannable p=(IPlannable) element;
				if(columnIndex==3){
					return SWTHelper.getContrast(Plannables.getStatusColor(p));
				}else{
					return SWTHelper.getContrast(Plannables.getTypColor(p));
				}
			}
			return null;
		}

		public Image getColumnImage(Object element, int columnIndex) {
			/*
			if(element instanceof IPlannable){
				IPlannable p=(IPlannable)element;
				return Plannables.getTypImage(p);
			}*/
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if(element instanceof IPlannable){
				IPlannable ip=(IPlannable)element;
				switch(columnIndex){
				case 0:
					return Plannables.getStartTimeAsString(ip);
				case 1:
					return Plannables.getEndTimeAsString(ip);
				case 2:
					return ip.getType();
				case 3:
					return ip.getStatus();
				case 4:
					return ip.getTitle();
				case 5:
					if (ip instanceof Termin) {
						Termin termin = (Termin) ip;
						String grund = termin.getGrund();
						if (grund != null) {
							String[] tokens = grund.split("[\r\n]+");
							if(tokens.length>0){
								grund = tokens[0];
							}
						}
						return grund==null ? "" : grund;
					} else {
						return "";
					}
				}
			}
			return "?"; //$NON-NLS-1$
		}
		
	}
	private class ChangeBereichAdapter extends SelectionAdapter{

		@Override
		public void widgetSelected(SelectionEvent ev) {
			Button source=(Button)ev.getSource();
			String bereich=source.getText();
			setBereich(bereich);
			pinger.doSync();
			tv.refresh();
		}
		
	}
	@Override
	public void setTermin(Termin t) {
		Patient pat=t.getPatient();
		StringBuilder sb=new StringBuilder(200);
		TimeSpan ts=t.getTimeSpan();
		sb.append(ts.from.toString(TimeTool.TIME_SMALL)).append("-").append(ts.until.toString(TimeTool.TIME_SMALL)) //$NON-NLS-1$
		.append(" ").append(t.getPersonalia()).append("\n(") //$NON-NLS-1$ //$NON-NLS-2$
		.append(t.getType()).append(",").append(t.getStatus()).append(")\n--------\n").append(t.getGrund()); //$NON-NLS-1$ //$NON-NLS-2$
		terminDetail.setText(sb.toString());
		GlobalEvents ev=GlobalEvents.getInstance();
		sb.setLength(0);
		sb.append(StringTool.unNull(t.get("ErstelltVon"))).append("/")
		.append(t.getCreateTime().toString(TimeTool.FULL_GER));
		lbDetails.setText(sb.toString());
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
	
	private void makePrivateActions(){
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
