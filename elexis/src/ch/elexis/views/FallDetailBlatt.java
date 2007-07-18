/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: FallDetailBlatt.java 2839 2007-07-18 17:44:17Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.dialogs.KontaktSelektor;
import ch.elexis.preferences.Leistungscodes;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

import com.tiff.common.ui.datepicker.DatePickerCombo;

public class FallDetailBlatt extends Composite {
	private final FormToolkit tk;
	private final ScrolledForm form;
	String[] Abrechnungstypen=Hub.globalCfg.keys(Leistungscodes.CFG_KEY);
	/*
    public static final String[] Gesetze={Fall.LAW_DISEASE,Fall.LAW_ACCIDENT,Fall.LAW_INSURANCE,
    	Fall.LAW_INVALIDITY,Fall.LAW_MILITARY,Fall.LAW_OTHER};
    */
    public static final String[] Reasons={Fall.TYPE_DISEASE,
    	Fall.TYPE_ACCIDENT,Fall.TYPE_MATERNITY,Fall.TYPE_PREVENTION,Fall.TYPE_BIRTHDEFECT,Fall.TYPE_OTHER};
    public static final String[] dgsys=null;
	Combo cGesetz,cReason, cPaymentMode;
	Text tBezeichnung,tGarant, tKostentraeger, tVersNummer, tFallNummer, tBetriebsNummer, tArbeitgeber;
	DatePickerCombo dpBeginn, dpEnd;
    Button accept,reject;
    Hyperlink garant, kostentraeger, arbeitgeber,autoFill;
	public FallDetailBlatt(final Composite parent){
		super(parent,SWT.NONE);
		tk=Desk.theToolkit;
		form=tk.createScrolledForm(this);
		Composite top=form.getBody();
		setLayout(new FillLayout());
		top.setLayout(new GridLayout(2,false));
		tk.createLabel(top,"Abrechnungsmethode");
		Composite cpGesetz=new Composite(top,SWT.NONE);
		cpGesetz.setLayout(new GridLayout(2,false));
		cGesetz=new Combo(cpGesetz,SWT.READ_ONLY);
		autoFill=tk.createHyperlink(cpGesetz, "Daten übernehmen", SWT.NONE);
		autoFill.addHyperlinkListener(new HyperlinkAdapter(){

			@Override
			public void linkActivated(final HyperlinkEvent e) {
				// copy data from previous Fall of the same Gesetz
				
				/* fix this later
				Fall f=GlobalEvents.getSelectedFall();
				// don't do anything if no Fall is selected
				if (f == null) {
					return;
				}
				
				Fall[] faelle = f.getPatient().getFaelle();
				String g=f.getGesetz();
				for(Fall f0:faelle){
					if (f0.getId().equals(f.getId())) {
						// ignore current Fall
						continue;
					}
					
					if(f0.getGesetz().equals(g)){
						f.setGarant(f0.getGarant());
						String pm=f0.getPaymentMode();
						f.setPaymentMode(pm);
						Kontakt k=f0.getKostentraeger();
						f.setKostentraeger(k);
						// TODO break? or looking for the most current Fall?
					}
				}
		    	setFall(f);
		    	*/
			}
		});
		for(String s:Abrechnungstypen){
			cGesetz.add(s.split(";")[0]);
		}

        cGesetz.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(final SelectionEvent e)
            {
                int i=cGesetz.getSelectionIndex();
                Fall fall=(Fall)GlobalEvents.getInstance().getSelectedObject(Fall.class);
                if(fall!=null){
                	if(fall.getBehandlungen(false).length>0){
                		SWTHelper.alert("Abrechnungssystem kann nicht geändert werden", "Bei einem Fall, zu dem schon Konsultationen existieren, kann das Gestz nicht geändert werden.");
                		String gesetz=fall.getAbrechnungsSystemName();
                		if(ch.rgw.tools.StringTool.isNothing(gesetz)){
                			gesetz="frei";
                		}
                		cGesetz.select(cGesetz.indexOf(gesetz));
                		
                	}else{
                		fall.setAbrechnungsSystem(Abrechnungstypen[i]);
                		GlobalEvents.getInstance().fireSelectionEvent(fall.getPatient());
                	// Falls noch kein Garant gesetzt ist: Garanten des letzten Falles zum selben Gesetz nehmen
                	}
                }
            }
            
        });
		tk.createLabel(top,"Bezeichnung");
		tBezeichnung=tk.createText(top,"");
        tBezeichnung.addFocusListener(new Focusreact("Bezeichnung"));
        tBezeichnung.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		tk.createLabel(top,"Grund für Versicherung");
		cReason=new Combo(top,SWT.READ_ONLY);
		cReason.setItems(Reasons);
		cReason.addSelectionListener(new SelectionAdapter(){
			@Override
            public void widgetSelected(final SelectionEvent e)
            {
                int i=cReason.getSelectionIndex();
                Fall fall=(Fall)GlobalEvents.getInstance().getSelectedObject(Fall.class);
                if(fall!=null){
                	fall.setGrund(Reasons[i]);
                	GlobalEvents.getInstance().fireSelectionEvent(fall.getPatient());
                }
            }
		});
        cReason.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
        /*
        tk.createLabel(top,"Zahlungsmethode");
       
        cPaymentMode=new Combo(top,SWT.READ_ONLY);
        cPaymentMode.setItems(new String[]{"Tiers Garant","Tiers Payant"});
        cPaymentMode.addSelectionListener(new SelectionAdapter(){
        	@Override
            public void widgetSelected(final SelectionEvent e) {
        		int i=cPaymentMode.getSelectionIndex();
        		Fall fall=(Fall)GlobalEvents.getInstance().getSelectedObject(Fall.class);
                if(fall!=null){
                	if(i==0){
                		tGarant.setEnabled(true);
                		fall.setPaymentMode("TG");
                	}else if(i==1){
                		tGarant.setEnabled(false);
                		fall.setPaymentMode("TP");
                	}
	               	GlobalEvents.getInstance().fireSelectionEvent(fall.getPatient());
                }
            
            }
        });
        */
		tk.createLabel(top,"Beginndatum/Unfalldatum");
		dpBeginn=new DatePickerCombo(top,SWT.NONE);
        dpBeginn.addSelectionListener(new SelectionAdapter(){

            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(final SelectionEvent e)
            {
                TimeTool beg=new TimeTool(dpBeginn.getDate().getTime());
                Fall fall=(Fall)GlobalEvents.getInstance().getSelectedObject(Fall.class);
                fall.setBeginnDatum(beg.toString(TimeTool.DATE_COMPACT));
            	GlobalEvents.getInstance().fireSelectionEvent(fall.getPatient());
            }
            
        });
        dpBeginn.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		tk.createLabel(top,"EndDatum");
		dpEnd=new DatePickerCombo(top,SWT.NONE);
        dpEnd.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(final SelectionEvent e)
            {
                TimeTool end=new TimeTool(dpEnd.getDate().getTime());
                Fall fall=(Fall)GlobalEvents.getInstance().getSelectedObject(Fall.class);
                fall.setEndDatum(end.toString(TimeTool.DATE_COMPACT));
            	GlobalEvents.getInstance().fireSelectionEvent(fall.getPatient());
            }
            
        });
        dpEnd.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
        garant=tk.createHyperlink(top,"Rechnungsempfänger",SWT.NONE);
        garant.addHyperlinkListener(new KontaktAdapter("Debitor auswählen","Bitte wählen Sie den Debitor für diesen Fall aus"));
        tGarant=tk.createText(top,"");
        tGarant.setEditable(false);
        tGarant.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
        kostentraeger=tk.createHyperlink(top,"Kostenträger",SWT.NONE);
        kostentraeger.addHyperlinkListener(new KontaktAdapter("Kostenträger auswählen","Bitte den Kostenträger für diesen Fall aus der Liste auswählen"));
        tKostentraeger=tk.createText(top,"");
        tKostentraeger.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
        arbeitgeber=tk.createHyperlink(top,"Arbeitgeber",SWT.NONE);
        arbeitgeber.addHyperlinkListener(new KontaktAdapter("Arbeitgeber auswählen","Bitte den Arbeitgeber für diesen Fall asuwählen"));
        tArbeitgeber=tk.createText(top,"");
        tArbeitgeber.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
        tArbeitgeber.setEditable(false);
        tk.createLabel(top,"Versicherungsnummer");
        tVersNummer=tk.createText(top,"");
        tVersNummer.addFocusListener(new Focusreact("VersNummer"));
        tVersNummer.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
        tk.createLabel(top,"Fallnummer");
        tFallNummer=tk.createText(top,"");
        tFallNummer.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
        tFallNummer.addFocusListener(new Focusreact("FallNummer"));
        tk.paintBordersFor(top);
		setFall((Fall)GlobalEvents.getInstance().getSelectedObject(Fall.class));

	}
	
	private final class KontaktAdapter extends HyperlinkAdapter {
    	String t,m;
    	KontaktAdapter(final String titel, final String msg){
    		t=titel;
    		m=msg;
    	}
		@Override
		public void linkActivated(final HyperlinkEvent e) {
			Hyperlink source=(Hyperlink)e.getSource();
			KontaktSelektor ksl=new KontaktSelektor(getShell(),	Kontakt.class,t,m, true);
			if(ksl.open()==Dialog.OK){
				Kontakt sel=(Kontakt)ksl.getSelection();
		        Fall fall=(Fall)GlobalEvents.getInstance().getSelectedObject(Fall.class);
		        if(fall!=null){
		        	if(source.equals(garant)){
		        		fall.setGarant(sel);
		        	}else if(source.equals(arbeitgeber)){
		        		fall.setArbeitgeber(sel);
		        	}else{
		        		fall.setKostentraeger(sel);
		        	}
		        	setFall(fall);
                	GlobalEvents.getInstance().fireSelectionEvent(fall.getPatient());
		        }
			}
		}
	}
	class Focusreact implements FocusListener{
        private final String field;
        Focusreact(final String dbField){
            field=dbField;
        }
        public void focusGained(final FocusEvent e)
        { /* nichts */}

        public void focusLost(final FocusEvent e)
        {
            String newval=((Text)e.getSource()).getText();
            Fall fall=(Fall)GlobalEvents.getInstance().getSelectedObject(Fall.class);
            if(fall!=null){
            	fall.set(field,newval);
            	GlobalEvents.getInstance().fireSelectionEvent(fall.getPatient());
            }
        }
        
    }
	public void setFall(final Fall f){
		if(f==null){
			form.setText("Kein Fall ausgewählt");
			tBezeichnung.setText("");
			cReason.select(0);
			cGesetz.select(0);
			tGarant.setText("");
			tKostentraeger.setText("");
			tFallNummer.setText("");
			tArbeitgeber.setText("");
			dpBeginn.setDate(null);
			dpEnd.setDate(null);
			//cPaymentMode.select(0);
			return;
		}
    	

		form.setText(f.getLabel());
		tBezeichnung.setText(f.getBezeichnung());
		String grund=f.getGrund();
		int ix=cReason.indexOf(grund);
		if(ix==-1){
			ix=0;
		}
		cReason.select(ix);
		String gesetz=f.getAbrechnungsSystemName();
		if(ch.rgw.tools.StringTool.isNothing(gesetz)){
			cGesetz.select(0);
		}else{
			cGesetz.select(cGesetz.indexOf(gesetz));
		}
		TimeTool tt=new TimeTool();
		if(tt.set(f.getBeginnDatum())==true){
			dpBeginn.setDate(tt.getTime());
		}else{
			dpBeginn.setDate(null);
		}
		if(tt.set(f.getEndDatum())==true){
			dpEnd.setDate(tt.getTime());
		}else{
			dpEnd.setDate(null);
		}
        Kontakt garant1=f.getGarant();
        if((garant1==null) || (!garant1.exists())){
            tGarant.setText("");
        }else{
            tGarant.setText(garant1.getLabel());
        }/*
        if(f.getPaymentMode().equals("TG")){
        	cPaymentMode.select(0);
        }else if(f.getPaymentMode().equals("TP")){
        	cPaymentMode.select(1);
        }else{
        	f.setPaymentMode("TG");
        	cPaymentMode.select(0);
        }
        */
        Kontakt ktr=f.getKostentraeger();
        if((ktr==null) || (!ktr.exists())){
        	tKostentraeger.setText("");
        }else{
        	tKostentraeger.setText(ktr.getLabel());
        }
        Kontakt arb=f.getArbeitgeber();
        if((arb==null) || (!arb.exists())){
        	tArbeitgeber.setText("");
        }else{
        	tArbeitgeber.setText(arb.getLabel());
        }
		tVersNummer.setText(f.getVersNummer());
		tFallNummer.setText(f.getFallNummer());
	}

}
