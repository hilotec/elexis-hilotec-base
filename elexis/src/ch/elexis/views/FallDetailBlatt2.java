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
 *  $Id: FallDetailBlatt2.java 2865 2007-07-22 15:26:06Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.dialogs.KontaktSelektor;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

import com.tiff.common.ui.datepicker.DatePickerCombo;

/**
 * Display detail data of a Fall 
 */
public class FallDetailBlatt2 extends Composite {
	private final FormToolkit tk;
	private final ScrolledForm form;
	String[] Abrechnungstypen=Fall.getAbrechnungsSysteme();
	
	
    public static final String[] Reasons={Fall.TYPE_DISEASE,
    	Fall.TYPE_ACCIDENT,Fall.TYPE_MATERNITY,Fall.TYPE_PREVENTION,Fall.TYPE_BIRTHDEFECT,Fall.TYPE_OTHER};
    public static final String[] dgsys=null;
	Combo cAbrechnung,cReason;
	Text tBezeichnung;
    Button accept,reject;
    Hyperlink autoFill;
    List<Control> lReqs=new ArrayList<Control>();
	public FallDetailBlatt2(final Composite parent){
		super(parent,SWT.NONE);
		tk=Desk.theToolkit;
		form=tk.createScrolledForm(this);
		Composite top=form.getBody();
		setLayout(new FillLayout());
		top.setLayout(new GridLayout(2,false));
		tk.createLabel(top,"Abrechnungsmethode");
		Composite cpAbrechnung=new Composite(top,SWT.NONE);
		cpAbrechnung.setLayout(new GridLayout(2,false));
		cAbrechnung=new Combo(cpAbrechnung,SWT.READ_ONLY);
		autoFill=tk.createHyperlink(cpAbrechnung, "Daten übernehmen", SWT.NONE);
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
		cAbrechnung.setItems(Abrechnungstypen);

        cAbrechnung.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(final SelectionEvent e)
            {
                int i=cAbrechnung.getSelectionIndex();
                Fall fall=(Fall)GlobalEvents.getInstance().getSelectedObject(Fall.class);
                if(fall!=null){
                	if(fall.getBehandlungen(false).length>0){
                		SWTHelper.alert("Abrechnungssystem kann nicht geändert werden", "Bei einem Fall, zu dem schon Konsultationen existieren, kann das Gestz nicht geändert werden.");
                		String gesetz=fall.getAbrechnungsSystem();
                		if(ch.rgw.tools.StringTool.isNothing(gesetz)){
                			gesetz="frei";
                		}
                		cAbrechnung.select(cAbrechnung.indexOf(gesetz));
                		
                	}else{
                		fall.setAbrechnungsSystem(Abrechnungstypen[i]);
                		setFall(fall);
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

		        tk.paintBordersFor(top);
		setFall((Fall)GlobalEvents.getInstance().getSelectedObject(Fall.class));

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
            	fall.setInfoString(field,newval);
            	GlobalEvents.getInstance().fireSelectionEvent(fall.getPatient());
            }
        }
        
    }
	public void setFall(final Fall f){
		for(Control c:lReqs){
			c.dispose();
		}
		lReqs.clear();
		if(f==null){
			form.setText("Kein Fall ausgewählt");
			tBezeichnung.setText("");
			cReason.select(0);
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
		String abr=f.getAbrechnungsSystem();
		cAbrechnung.setText(abr);
		String reqs=f.getRequirements();
		if(reqs!=null){
			for(String req:reqs.split(";")){
				final String[] r=req.split(":");
				if(r.length<2){
					continue;
				}
				if(r[1].equals("T")){
					lReqs.add(tk.createLabel(form.getBody(), r[0]));
					String val=f.get(r[0]);
					if(val.startsWith("**ERROR")){
						val="";
					}
					Text tx=tk.createText(form.getBody(), val);
					tx.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
					tx.addFocusListener(new Focusreact(r[0]));
					lReqs.add(tx);
				}else if(r[1].equals("D")){
					lReqs.add(tk.createLabel(form.getBody(), r[0]));
					final DatePickerCombo dp=new DatePickerCombo(form.getBody(),SWT.NONE);
					String dat=f.get(r[0]);
					TimeTool tt=new TimeTool();
					if(tt.set(dat)){
						dp.setDate(tt.getTime());
					}
					dp.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
					dp.addSelectionListener(new SelectionAdapter(){
						@Override
						public void widgetSelected(SelectionEvent e) {
							TimeTool tt=new TimeTool(dp.getDate().getTime());
							f.setInfoString(r[0], tt.toString(TimeTool.DATE_GER));
						}});
					lReqs.add(dp);
				}else if(r[1].equals("K")){
					Hyperlink hl=tk.createHyperlink(form.getBody(), r[0], SWT.NONE);
					String val=f.get(r[0]);
					if(val.startsWith("**ERROR")){
						val="";
					}else{
						Kontakt k=Kontakt.load(val);
						val=k.getLabel();
					}
					Text tx=tk.createText(form.getBody(), val);
					hl.addHyperlinkListener(new HyperlinkAdapter(){
						@Override
						public void linkActivated(final HyperlinkEvent e) {
							KontaktSelektor ksl=new KontaktSelektor(getShell(),	Kontakt.class,"Kontakt auswählen",
									"Bitte wählen Sie den Kontakt für "+r[0]+" aus", true);
							if(ksl.open()==Dialog.OK){
								Kontakt sel=(Kontakt)ksl.getSelection();
						        Fall fall=(Fall)GlobalEvents.getInstance().getSelectedObject(Fall.class);
						        if(fall!=null){
						        	fall.setInfoString(r[0], sel.getId());
						        	setFall(fall);
				                	GlobalEvents.getInstance().fireSelectionEvent(fall.getPatient());
						        }
							}
						}
					});
					lReqs.add(hl);
					tx.setEditable(false);
					tx.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
					lReqs.add(tx);
				}
			}
			form.reflow(true);
			form.redraw();
		}
		
	}

}
