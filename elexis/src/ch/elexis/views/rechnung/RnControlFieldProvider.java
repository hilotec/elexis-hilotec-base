/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: RnControlFieldProvider.java 3862 2008-05-05 16:14:14Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views.rechnung;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ch.elexis.Desk;
import ch.elexis.data.Fall;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.RnStatus;
import ch.elexis.dialogs.KontaktSelektor;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewerConfigurer;
import ch.elexis.util.ViewerConfigurer.ControlFieldListener;
import ch.rgw.tools.StringTool;

/**
 * Controlfieldprovider for RechnungsListeView. Creates a Composite that contains the controls to select
 * the criteria for the bills to be displayed
 * @author gerry
 *
 */
class RnControlFieldProvider implements ViewerConfigurer.ControlFieldProvider{
	//final String[] stats={"Alle","Bezahlt","Offen","Offen&Gedruckt","1. Mahnung","2. Mahnung","3. Mahnung","In Betreibung","Teilverlust","Totalverlust"};
	final static String[] stats={
		"Alle","Offen","Offen & gedruckt","Teilw. bezahlt","Bezahlt","Zuviel bezahlt",
		"Zahlungserinnerung","ZE gedruckt","2. Mahnung","2. M. gedruckt","3. Mahnung",
		"3. M. gedruckt","In Betreibung","Teilverlust","Totalverlust","Storniert","Fehlerhaft",
		"zu drucken","ausstehend"};
	
	
	final static int[] statInts={
		RnStatus.UNBEKANNT, RnStatus.OFFEN, RnStatus.OFFEN_UND_GEDRUCKT, RnStatus.TEILZAHLUNG, RnStatus.BEZAHLT,RnStatus.ZUVIEL_BEZAHLT,
		RnStatus.MAHNUNG_1, RnStatus.MAHNUNG_1_GEDRUCKT, RnStatus.MAHNUNG_2, RnStatus.MAHNUNG_2_GEDRUCKT, RnStatus.MAHNUNG_3,
		RnStatus.MAHNUNG_3_GEDRUCKT, RnStatus.IN_BETREIBUNG, RnStatus.TEILVERLUST, RnStatus.TOTALVERLUST,RnStatus.STORNIERT, 
		RnStatus.FEHLERHAFT, RnStatus.ZU_DRUCKEN,RnStatus.AUSSTEHEND};
	
	final static int STAT_DEFAULT_INDEX = 1;
	private final static String ALLE="[ --- Alle Patienten --- ]";
	private final static String ALL="Alle";
	
	Combo cbStat;
	Combo cbZType;
	/* DatePickerCombo dpVon, dpBis; */
	private List<ControlFieldListener> listeners;
	private final SelectionAdapter csel=new CtlSelectionListener();
	private boolean bDateAsStatus;
	private HyperlinkAdapter /*hlStatus,*/ hlPatient;
	private Label /*hDateFrom, hDateUntil,*/ lPatient;
	Text tNr, tBetrag;
	
	Patient actPatient;
	
	public Composite createControl(final Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		listeners=new ArrayList<ControlFieldListener>();
		ret.setLayout(new GridLayout(5,true));
		ret.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		hlPatient=new HyperlinkAdapter(){
			@Override
			public void linkActivated(final HyperlinkEvent e) {
				Patient oldPatient=actPatient;
				KontaktSelektor ksl=new KontaktSelektor(parent.getShell(),Patient.class,"Patient auswählen","Bitte wählen Sie einen Patienteneintrag oder Abbrechen für 'Alle'", true);
				if(ksl.open()==Dialog.OK){
					actPatient=(Patient)ksl.getSelection();
					if (actPatient != null) {
						lPatient.setText(actPatient.getLabel());
						cbStat.setText(stats[0]);
					} else {
						lPatient.setText(ALLE);
						cbStat.setText(stats[1]);
					}
				}else{
					actPatient=null;
					lPatient.setText(ALLE);
					cbStat.setText(stats[1]);
				}
				if(((actPatient==null) && (oldPatient!=null)) ||
				   (!actPatient.equals(oldPatient))){
						fireChangedEvent();
				}
			}
		};
		new Label(ret,SWT.NONE).setText(Messages.getString("RnControlFieldProvider.state")); //$NON-NLS-1$
		SWTHelper.createHyperlink(ret, "   Patient   ", hlPatient);
		new Label(ret,SWT.NONE).setText(Messages.getString("RnControlFieldProvider.PaymentSystem")); //$NON-NLS-1$
		new Label(ret,SWT.NONE).setText(Messages.getString("RnControlFieldProvider.invoideNr")); //$NON-NLS-1$
		new Label(ret,SWT.NONE).setText("Betrag");
		/// ^ labels / values
		cbStat=new Combo(ret,SWT.READ_ONLY);
		cbStat.setVisibleItemCount(stats.length);
		cbStat.setItems(stats);
		cbStat.addSelectionListener(csel);
		cbStat.select(STAT_DEFAULT_INDEX);
		lPatient=new Label(ret,SWT.NONE);
		lPatient.setText(ALLE);
		cbZType=new Combo(ret,SWT.SINGLE|SWT.READ_ONLY);
		cbZType.setItems(Fall.getAbrechnungsSysteme());
		cbZType.add(ALL);
		/*GridData gdlp=new GridData();
		gdlp.widthHint=150;
		gdlp.minimumWidth=150;*/
		tNr=new Text(ret,SWT.BORDER);
		tNr.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if(tNr.getText().length()==0){
					cbStat.select(STAT_DEFAULT_INDEX);
				}
				fireChangedEvent();
			}
			
		});
		tBetrag=new Text(ret,SWT.BORDER);
		tBetrag.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if(tNr.getText().length()==0){
					cbStat.select(STAT_DEFAULT_INDEX);
				}
				fireChangedEvent();
			}
			
		});
		GridData sgd=new GridData();
		sgd.minimumWidth=100;
		sgd.widthHint=100;
		return ret;
	}

	public void addChangeListener(final ControlFieldListener cl) {
		listeners.add(cl);
	}

	public void removeChangeListener(final ControlFieldListener cl) {
		listeners.remove(cl);
	}
	public boolean getDateModeIsStatus(){
		return bDateAsStatus;
	}
	public String[] getValues() {
		String[] ret=new String[5];
		int selIdx=cbStat.getSelectionIndex();
		if(selIdx!=-1){
			ret[0]=Integer.toString(statInts[selIdx]);
		}
		else{
			ret[0]="1"; //$NON-NLS-1$
		}
		if(actPatient!=null){
			ret[1]=actPatient.getId();
		}
		ret[2]=tNr.getText();
		ret[3]=tBetrag.getText().replaceAll("\\.", "");
		if(StringTool.isNothing(ret[2])){
			ret[2]=null;
		}else{	// Wenn RnNummer gegeben ist, alles andere auf Standard.
			clearValues();
			tNr.setText(ret[2]);
			ret[0]="0";
			ret[1]=null;
			ret[3]=null;
		}
		if(StringTool.isNothing(ret[3])){
			ret[3]=null;
		}else{
			clearValues();
			tBetrag.setText(ret[3]);
			ret[0]="0";
			ret[1]=null;
			ret[2]=null;
		}
		ret[4]=cbZType.getText();
		if(StringTool.isNothing(ret[4]) || ret[4].equals(ALL)){
			ret[4]=null;
		}
		return ret;
	}

	public void clearValues() {
		cbStat.select(0);
		//dpVon.setDate(new TimeTool("1.1.1900").getTime()); //$NON-NLS-1$
		//dpBis.setData(new TimeTool("31.12.2999").getTime()); //$NON-NLS-1$
		tNr.setText(""); //$NON-NLS-1$
		actPatient=null;
		lPatient.setText(ALLE);
	}

	public boolean isEmpty() {
		return false;
	}

	public void setQuery( final Query q){
		
	}
	/*
	public void setQuery(final Query q) {
		String[] val=getValues();
		q.add("RnStatus","=",val[0]); //$NON-NLS-1$ //$NON-NLS-2$
		if(val[1]!=null){
			q.add("RnDatum",">=",val[1]); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if(val[2]!=null){
			q.add("RnDatum","<=",val[2]); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if(val[3]!=null){
			q.add("RnNummer", "=", val[3]); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if(actPatient!=null){
			Fall[] faelle=actPatient.getFaelle();
			if((faelle!=null) && (faelle.length>0)){
				q.startGroup();
				q.insertFalse();
				q.or();
				for(Fall fall:faelle){
					if(fall.isOpen()){
						q.add("FallID", "=", fall.getId());
					}
				}
				q.endGroup();
			}
		}
	}
	*/

	public ViewerFilter createFilter() {
		return new ViewerFilter(){

			@Override
			public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
				return true;
			}};
	}

	public void fireChangedEvent() {
		Desk.theDisplay.syncExec(new Runnable(){
			public void run() {
				for(ControlFieldListener lis:listeners){
		    		lis.changed(new String[]{"Status"},new String[]{"0"}); //$NON-NLS-1$ //$NON-NLS-2$
		    		
		    	}					
			}
    	});			
	}

	public void fireSortEvent(final String text){
		for(ControlFieldListener lis:listeners){
    		lis.reorder(text);
    		
    	}	
	}
	public void setFocus() {
		
	}
	private static class CtlSelectionListener extends SelectionAdapter{
		@Override
		public void widgetSelected(final SelectionEvent e) {
			//fireChangedEvent(); do nothing. Only refresh by click on the refresh button 
		}
	}
}