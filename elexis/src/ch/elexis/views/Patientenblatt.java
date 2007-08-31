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
 * $Id: Patientenblatt.java 3047 2007-08-31 09:30:34Z danlutz $
 *******************************************************************************/


package ch.elexis.views;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.data.Anwender;
import ch.elexis.data.BezugsKontakt;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.dialogs.AddBuchungDialog;
import ch.elexis.dialogs.AnschriftEingabeDialog;
import ch.elexis.dialogs.KontaktDetailDialog;
import ch.elexis.dialogs.KontaktSelektor;
import ch.elexis.preferences.UserSettings2;
import ch.elexis.util.DynamicListDisplay;
import ch.elexis.util.LabeledInputField;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;
import ch.elexis.util.WidgetFactory;
import ch.elexis.util.DynamicListDisplay.DLDListener;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Detailansicht eines Patientrecords
 */
public class Patientenblatt extends Composite implements GlobalEvents.SelectionListener, ActivationListener{
	private final FormToolkit tk;
	private final static String[] lbSimple={"Name","Vorname","Geburtsdatum","Geschlecht","Telefon 1",
			"Telefon 2","Mobil","Fax","E-Mail","Gruppe","Konto"};
	private final static String[] dfSimple={"Name","Vorname","Geburtsdatum","Geschlecht","Telefon1",
			"Telefon2","Natel","Fax","E-Mail","Gruppe","Konto"};
	private final Text[] txSimple=new Text[lbSimple.length];
	private final static String[] lbExpandable={"Diagnosen","Persönliche Anamnese",/*"Familienanamnese",
			"Systemanamnese",*/"Allergien","Risiken","Bemerkungen"};
	private final Text[] txExpandable=new Text[lbExpandable.length];
	private final static String[] dfExpandable={"Diagnosen","PersAnamnese",/*"FamilienAnamnese",
			"SystemAnamnese",*/"Allergien","Risiken","Bemerkung"};
	private final ExpandableComposite[] ec=new ExpandableComposite[lbExpandable.length];
	private final static String[] lbLists={"Fixmedikation","Reminders"};
	private final FormText inpAdresse;
	private final DynamicListDisplay inpZusatzAdresse, dlReminder;
	private final DauerMediDisplay dmd;
	Patient actPatient;
	IViewSite viewsite;
	private final Hyperlinkreact hr=new Hyperlinkreact();
    private final ScrolledForm form;
    private final ViewMenus viewmenu;
    private ExpandableComposite ecdm,ecZA;
    
	Patientenblatt(final Composite parent, final IViewSite site)
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
			public void mouseDown(final MouseEvent e) {
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
		
		IExpansionListener ecExpansionListener =  new ExpansionAdapter(){
            @Override
            public void expansionStateChanging(final ExpansionEvent e)
            {
            	ExpandableComposite src=(ExpandableComposite)e.getSource();
                saveExpandedState("Patientenblatt/"+src.getText(), e.getState());
            }
            
        };
		
        ecZA=WidgetFactory.createExpandableComposite(tk, form, "Zusatzadressen");
        setExpandedState(ecZA,"Patientenblatt/Zusatzadressen");
        //ecZA.setExpanded(true);
        ecZA.addExpansionListener(ecExpansionListener);
        
		inpZusatzAdresse=new DynamicListDisplay(ecZA,SWT.NONE,new DLDListener(){
			public boolean dropped(final PersistentObject dropped) {
				return false;
			}

			public void hyperlinkActivated(final String l) {
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
            setExpandedState(ec[i], "Patientenblatt/"+lbExpandable[i]);
            //ec[i].setExpanded(true);
			txExpandable[i]=tk.createText(ec[i], "" , SWT.MULTI);
			txExpandable[i].addFocusListener(new Focusreact(dfExpandable[i]));
            ec[i].setData("dbfield",dfExpandable[i]);
            ec[i].addExpansionListener(new ExpansionAdapter(){
                @Override
                public void expansionStateChanging(final ExpansionEvent e)
                {
                	ExpandableComposite src=(ExpandableComposite)e.getSource();
                    if(e.getState()==true){
                        Text tx=(Text)src.getClient();
                        if (actPatient != null) {
                        	tx.setText(StringTool.unNull(actPatient.get((String)src.getData("dbfield"))));
                        } else {
                        	tx.setText("");
                        }
                    }
                    saveExpandedState("Patientenblatt/"+src.getText(), e.getState());
                }
                
            });
            ec[i].setClient(txExpandable[i]);
		}
		ecdm=WidgetFactory.createExpandableComposite(tk, form, lbLists[0]);
		setExpandedState(ecdm, "Patientenblatt/"+lbLists[0]);
		//ecdm.setExpanded(true);
		ecdm.addExpansionListener(ecExpansionListener);
		dmd=new DauerMediDisplay(ecdm,site);
		ecdm.setClient(dmd);
		ExpandableComposite ecrm=WidgetFactory.createExpandableComposite(tk, form, lbLists[1]);
		setExpandedState(ecrm, "Patientenblatt/"+lbLists[1]);
		//ecrm.setExpanded(true);
		ecrm.addExpansionListener(ecExpansionListener);
		dlReminder=new DynamicListDisplay(ecrm,SWT.NONE,null);
		ecrm.setClient(dlReminder);
		makeActions();
		viewmenu=new ViewMenus(viewsite);
		viewmenu.createMenu(GlobalActions.printEtikette, GlobalActions.printAdresse,GlobalActions.printBlatt,GlobalActions.printRoeBlatt);
        GlobalEvents.getInstance().addActivationListener(this,site.getPart());
        tk.paintBordersFor(form.getBody());
	}
    
	private void saveExpandedState(String field, boolean state){
		if(state){
			Hub.userCfg.set(UserSettings2.STATES+field, UserSettings2.OPEN);
		}else{
			Hub.userCfg.set(UserSettings2.STATES+field, UserSettings2.CLOSED);
		}
	}
	private void setExpandedState(ExpandableComposite ec,String field){
		String mode=Hub.userCfg.get(UserSettings2.EXPANDABLE_COMPOSITES,UserSettings2.REMEMBER_STATE);
		if(mode.equals(UserSettings2.OPEN)){
			ec.setExpanded(true);
		}else if(mode.equals(UserSettings2.CLOSED)){
			ec.setExpanded(false);
		}else{
			String state=Hub.userCfg.get(UserSettings2.STATES+field,UserSettings2.CLOSED);
			if(state.equals(UserSettings2.CLOSED)){
				ec.setExpanded(false);
			}else{
				ec.setExpanded(true);
			}
		}
	}
	@Override
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
                public void widgetSelected(final SelectionEvent e)
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
                 public void widgetSelected(final SelectionEvent e)
                 {
                     Kontakt a=Kontakt.load(((BezugsKontakt)inpZusatzAdresse.getSelection()).get("otherID"));
                     KontaktDetailDialog kdd=new KontaktDetailDialog(form.getShell(),a);
                     kdd.open();
                 }
            });
            return ret;
    }
    class Hyperlinkreact extends HyperlinkAdapter{
    	
		@Override
		@SuppressWarnings("synthetic-access")
        public void linkActivated(final HyperlinkEvent e)
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
		private final String field;
		Focusreact(final String f){
			field=f;
		}
		@Override
		public void focusLost(final FocusEvent e) {
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
	void setPatient(final Patient p) {
		/*
		 * work-around:
		 * The ExpandableComposites for Zusatzadressen and DauerMedi are
		 * expanded correctly only if the method setPatientInternal is called
		 * twice. Just calling form.reflow() doesn't help, not even calling it
		 * twice. Maybe the problem is that some ExpandableComposites contain
		 * controls with a layout not implementing ILayoutExtension.  
		 */
		setPatientInternal(p);
		setPatientInternal(p);
	}
	private void setPatientInternal(final Patient p)
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
        setExpandedState(ecZA, "Patientenblatt/Zusatzadressen");
		inpZusatzAdresse.clear();
		for(BezugsKontakt za : p.getBezugsKontakte()){
			inpZusatzAdresse.add(za);
		}
		
        
		for(int i=0;i<dfExpandable.length;i++){
			setExpandedState(ec[i], "Patientenblatt/"+ec[i].getText());
            if(ec[i].isExpanded()==true){
                txExpandable[i].setText(p.get(dfExpandable[i]));
            }
		}
		dmd.reload();
		setExpandedState(ecdm, "Patientenblatt/"+lbLists[0]);
		form.reflow(true);
	}
    public void refresh(){
    	form.reflow(true);
        //wf.getForm().redraw();
    }

    public void selectionEvent(final PersistentObject obj)
    {
        if(obj instanceof Patient){
            setPatient((Patient)obj);
        }else if(obj instanceof Anwender){
        	setPatient(GlobalEvents.getSelectedPatient());
        }
    }
	
	private void makeActions(){
	}

	public void activation(final boolean mode) {
		// TODO Auto-generated method stub
		
	}

	public void visible(final boolean mode) {
		if(mode==true){
			setPatient((Patient)GlobalEvents.getInstance().getSelectedObject(Patient.class));
			GlobalEvents.getInstance().addSelectionListener(this);
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
		
	}

	public void clearEvent(final Class<? extends PersistentObject> template) {
		// TODO Auto-generated method stub
		
	}
	
}
