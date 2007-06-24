/*******************************************************************************
 * Copyright (c) 2005-2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: Patientenblatt.java 1832 2007-02-18 09:12:31Z rgw_ch $
 *******************************************************************************/


package ch.elexis.views;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.data.*;
import ch.elexis.dialogs.*;
import ch.elexis.util.*;
import ch.elexis.util.DynamicListDisplay.DLDListener;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Detailansicht eines Patientrecords
 */
public class Patientenblatt extends Composite implements GlobalEvents.SelectionListener, ActivationListener{
	private FormToolkit tk;
	private final static String[] lbSimple={"Name","Vorname","Geburtsdatum","Geschlecht","Telefon 1",
			"Telefon 2","Mobil","Fax","E-Mail","Gruppe","Konto"};
	private final static String[] dfSimple={"Name","Vorname","Geburtsdatum","Geschlecht","Telefon1",
			"Telefon2","Natel","Fax","E-Mail","Gruppe","Konto"};
	private Text[] txSimple=new Text[lbSimple.length];
	private final static String[] lbExpandable={"Diagnosen","Persönliche Anamnese",/*"Familienanamnese",
			"Systemanamnese",*/"Allergien","Risiken","Bemerkungen"};
	private Text[] txExpandable=new Text[lbExpandable.length];
	private final static String[] dfExpandable={"Diagnosen","PersAnamnese",/*"FamilienAnamnese",
			"SystemAnamnese",*/"Allergien","Risiken","Bemerkung"};
	private ExpandableComposite[] ec=new ExpandableComposite[lbExpandable.length];
	private final static String[] lbLists={"Fixmedikation","Reminders"};
	private FormText inpAdresse;
	private DynamicListDisplay inpZusatzAdresse, dlReminder;
	private DauerMediDisplay dmd;
	Patient actPatient;
	IViewSite viewsite;
	private Hyperlinkreact hr=new Hyperlinkreact();
    private ScrolledForm form;
    private ViewMenus viewmenu;
    
	Patientenblatt(Composite parent, IViewSite site)
	{
		super(parent,SWT.NONE);
		viewsite=site;
		parent.setLayout(new FillLayout());
		setLayout(new FillLayout());
        tk=Desk.theToolkit;
        form=tk.createScrolledForm(this);
        TableWrapLayout twl=new TableWrapLayout();
		form.getBody().setLayout(twl);
       
        LabeledInputField.Tableau tblPersonalien=new LabeledInputField.Tableau(form.getBody());
        int tl=txSimple.length-1;
		for(int i=0;i<tl;i++){
		    txSimple[i]=(Text)tblPersonalien.addComponent(lbSimple[i]).getControl();
            txSimple[i].addFocusListener(new Focusreact(dfSimple[i]));
        }
		LabeledInputField li=tblPersonalien.addComponent(lbSimple[tl]);
		txSimple[tl]=(Text)li.getControl();
		txSimple[tl].setEditable(false);
		li.getLabelComponent().setForeground(Desk.theColorRegistry.get("blau"));
		li.getLabelComponent().addMouseListener(new MouseAdapter(){
			@Override
			public void mouseDown(MouseEvent e) {
				if(new AddBuchungDialog(getShell(),actPatient).open()==Dialog.OK){
					setPatient(actPatient);
				}
				
			}});
        TableWrapData twd=new TableWrapData(TableWrapData.FILL_GRAB);
        twd.grabHorizontal=true;
        tblPersonalien.setLayoutData(twd);
        
        Composite cPersonalien=tk.createComposite(form.getBody());
        cPersonalien.setLayout(new GridLayout(2,false));
        TableWrapData twd2=new TableWrapData(TableWrapData.FILL_GRAB);
        twd2.grabHorizontal=true;
        cPersonalien.setLayoutData(twd2);
        Hyperlink hHA=tk.createHyperlink(cPersonalien,"Anschrift",SWT.NONE);
		hHA.addHyperlinkListener(hr);
		hHA.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		inpAdresse=tk.createFormText(cPersonalien,false);
		inpAdresse.setText("---\n",false,false);
		inpAdresse.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		
        ExpandableComposite ecZA=WidgetFactory.createExpandableComposite(tk, form, "Zusatzadressen");
        
		inpZusatzAdresse=new DynamicListDisplay(ecZA,SWT.NONE,new DLDListener(){
			public boolean dropped(PersistentObject dropped) {
				return false;
			}

			public void hyperlinkActivated(String l) {
				KontaktSelektor ksl=new KontaktSelektor(getShell(),Kontakt.class,"Kontakt für Zusatzadresse","Bitte wählen Sie aus, wer als Zusatzadresse aufgenommen werden soll");
				if(ksl.open()==Dialog.OK){
					Kontakt k=(Kontakt) ksl.getSelection();
					InputDialog id=new InputDialog(getShell(),"Bezugstext für Adresse","Geben Sie bitte einen Text ein, der die Bedeutung dieser Adresse erklärt","",null);
					if(id.open()==Dialog.OK){
						String bezug=id.getValue();
						BezugsKontakt bk=actPatient.addBezugsKontakt(k, bezug);
						inpZusatzAdresse.add(bk);
						form.reflow(true);
					}
					
				}
			
			}});
		inpZusatzAdresse.addHyperlinks("Hinzu...");
		inpZusatzAdresse.setMenu(createZusatzAdressMenu());
        ecZA.setClient(inpZusatzAdresse);
		for(int i=0;i<lbExpandable.length;i++){
            ec[i]=WidgetFactory.createExpandableComposite(tk, form, lbExpandable[i]);
			txExpandable[i]=tk.createText(ec[i], "" , SWT.MULTI);
			txExpandable[i].addFocusListener(new Focusreact(dfExpandable[i]));
            ec[i].setData("dbfield",dfExpandable[i]);
            ec[i].addExpansionListener(new ExpansionAdapter(){
                @Override
                public void expansionStateChanging(ExpansionEvent e)
                {
                    if(e.getState()==true){
                        ExpandableComposite src=(ExpandableComposite)e.getSource();
                        Text tx=(Text)src.getClient();
                        tx.setText(StringTool.unNull(actPatient.get((String)src.getData("dbfield"))));
                        
                    }
                }
                
            });
            ec[i].setClient(txExpandable[i]);
		}
		ExpandableComposite ecdm=WidgetFactory.createExpandableComposite(tk, form, lbLists[0]);
		dmd=new DauerMediDisplay(ecdm,site);
		ecdm.setClient(dmd);
		ExpandableComposite ecrm=WidgetFactory.createExpandableComposite(tk, form, lbLists[1]);
		dlReminder=new DynamicListDisplay(ecrm,SWT.NONE,null);
		ecrm.setClient(dlReminder);
		makeActions();
		viewmenu=new ViewMenus(viewsite);
		viewmenu.createMenu(GlobalActions.printEtikette, GlobalActions.printAdresse,GlobalActions.printBlatt,GlobalActions.printRoeBlatt);
        GlobalEvents.getInstance().addActivationListener(this,site.getPart());
        tk.paintBordersFor(form.getBody());
	}
    
	public void dispose(){
		GlobalEvents.getInstance().removeSelectionListener(this);
		GlobalEvents.getInstance().removeActivationListener(this,viewsite.getPart());
		super.dispose();
	}
	
	private Menu createZusatzAdressMenu()
    {
            Menu ret=new Menu(inpZusatzAdresse);
            MenuItem delZA=new MenuItem(ret,SWT.NONE);
            delZA.setText("Adresse entfernen");
            delZA.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    BezugsKontakt a=(BezugsKontakt)inpZusatzAdresse.getSelection();
                    actPatient.removeBezugsKontakt(Kontakt.load(a.get("otherID")));
                    setPatient(actPatient);
                }
                
            });
            MenuItem showZA=new MenuItem(ret,SWT.NONE);
            showZA.setText("Adresse zeigen...");
            showZA.addSelectionListener(new SelectionAdapter(){
            	 @Override
                 public void widgetSelected(SelectionEvent e)
                 {
                     Kontakt a=Kontakt.load(((BezugsKontakt)inpZusatzAdresse.getSelection()).get("otherID"));
                     KontaktDetailDialog kdd=new KontaktDetailDialog(form.getShell(),a);
                     kdd.open();
                 }
            });
            return ret;
    }
    class Hyperlinkreact extends HyperlinkAdapter{
    	
		@SuppressWarnings("synthetic-access")
        public void linkActivated(HyperlinkEvent e)
        {  
			if(actPatient!=null){
				AnschriftEingabeDialog  aed=new AnschriftEingabeDialog(form.getShell(),actPatient);
				aed.open();
				/*
				Anschrift adr=actPatient.getAnschrift();
			    inpAdresse.setText(adr.getEtikette(true, false),false,false);
			    */
			    inpAdresse.setText(actPatient.getPostAnschrift(false),false,false);
			}
		}
	}
	class Focusreact extends FocusAdapter{
		private String field;
		Focusreact(String f){
			field=f;
		}
		@Override
		public void focusLost(FocusEvent e) {
			if(actPatient==null){
				return;
			}
			String oldvalue=actPatient.get(field);
			String newvalue=((Text)e.getSource()).getText();
			if(oldvalue!=null){
				if(oldvalue.equals(newvalue)){
					return;
				}
			}
			if(newvalue!=null){
				actPatient.set(field,newvalue);
			}
		}
	}
	void setPatient(Patient p)
	{
		actPatient=p;
	
		if(actPatient==null){
            form.setText("Kein Patient ausgewählt");
            for(int i=0;i<txSimple.length;i++){
                txSimple[i].setText("");
            }
            inpAdresse.setText("",false,false);
            inpZusatzAdresse.clear();
            return;
        }

		txSimple[0].setText(StringTool.unNull(p.get("Name")));
		txSimple[1].setText(StringTool.unNull(p.get("Vorname")));
        form.setText(StringTool.unNull(p.get("Name"))+" "+StringTool.unNull(p.get("Vorname"))+" ("+p.getPatCode()+")");
        txSimple[3].setText(StringTool.unNull(p.get("Geschlecht")));
		TimeTool gd=new TimeTool();
        if(gd.set(p.getGeburtsdatum())==false){
			txSimple[2].setText(" ");
		}else{
			txSimple[2].setText(gd.toString(TimeTool.DATE_GER));
		}
        for(int i=4;i<10;i++){
            txSimple[i].setText(PersistentObject.checkNull(p.get(dfSimple[i])));
        }
        txSimple[10].setText(p.getKontostand().getAmountAsString());
        inpAdresse.setText(p.getPostAnschrift(false),false,false);
        
		inpZusatzAdresse.clear();
		for(BezugsKontakt za : p.getBezugsKontakte()){
			inpZusatzAdresse.add(za);
		}
		
        
		for(int i=0;i<dfExpandable.length;i++){
            if(ec[i].isExpanded()==true){
                txExpandable[i].setText(p.get(dfExpandable[i]));
            }
		}
		dmd.reload();
		form.reflow(true);
	}
    public void refresh(){
    	form.reflow(true);
        //wf.getForm().redraw();
    }

    public void selectionEvent(PersistentObject obj)
    {
        if((obj instanceof Patient)){
            setPatient((Patient)obj);
        }
    }
	
	private void makeActions(){
	}

	public void activation(boolean mode) {
		// TODO Auto-generated method stub
		
	}

	public void visible(boolean mode) {
		if(mode==true){
			setPatient((Patient)GlobalEvents.getInstance().getSelectedObject(Patient.class));
			GlobalEvents.getInstance().addSelectionListener(this);
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
		
	}

	public void clearEvent(Class template) {
		// TODO Auto-generated method stub
		
	}
	
}
