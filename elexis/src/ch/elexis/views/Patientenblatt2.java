/*******************************************************************************
 * Copyright (c) 2008-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: Patientenblatt2.java 5584 2009-07-29 06:44:53Z freakypenguin $
 *******************************************************************************/


package ch.elexis.views;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
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

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.StringConstants;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.RestrictedAction;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Anwender;
import ch.elexis.data.BezugsKontakt;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.dialogs.AddBuchungDialog;
import ch.elexis.dialogs.AnschriftEingabeDialog;
import ch.elexis.dialogs.KontaktDetailDialog;
import ch.elexis.dialogs.KontaktSelektor;
import ch.elexis.preferences.UserSettings2;
import ch.elexis.util.InputPanel;
import ch.elexis.util.LabeledInputField;
import ch.elexis.util.ListDisplay;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;
import ch.elexis.util.WidgetFactory;
import ch.elexis.util.LabeledInputField.InputData;
import ch.rgw.tools.StringTool;

/**
 * Detailansicht eines Patientrecords
 * Ersatz für Patientenblatt mit erweiterter Funktionalität (Lock, Nutzung von InputPanel)
 */
public class Patientenblatt2 extends Composite implements GlobalEvents.SelectionListener, ActivationListener{
	private static final String KEY_DBFIELD = "dbfield"; //$NON-NLS-1$
	private static final String KEY_PATIENTENBLATT = "Patientenblatt/"; //$NON-NLS-1$
	private final FormToolkit tk;
	private InputPanel ipp;
	private IAction lockAction;
	MenuItem delZA;
	private final static String CFG_BEZUGSKONTAKTTYPEN="views/patientenblatt/Bezugskontakttypen"; //$NON-NLS-1$
	public final static String CFG_EXTRAFIELDS="views/patientenblatt/extrafelder"; //$NON-NLS-1$
	private final static String SPLITTER="#!>"; //$NON-NLS-1$

	private final static String[] lbExpandable={Messages.getString("Patientenblatt2.diagnosesLbl"),Messages.getString("Patientenblatt2.persAnamnesisLbl"),/*"Familienanamnese", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-2$
			"Systemanamnese",*/Messages.getString("Patientenblatt2.allergiesLbl"),Messages.getString("Patientenblatt2.risksLbl"),Messages.getString("Patientenblatt2.remarksLbk")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private final Text[] txExpandable=new Text[lbExpandable.length];
	private final static String[] dfExpandable={"Diagnosen","PersAnamnese",/*"FamilienAnamnese", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-2$
			"SystemAnamnese",*/"Allergien","Risiken","Bemerkung"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private final ExpandableComposite[] ec=new ExpandableComposite[lbExpandable.length];
	private final static String FIXMEDIKATION=Messages.getString("Patientenblatt2.fixmedication");  //$NON-NLS-1$
	//private final static String[] lbLists={"Fixmedikation"/*,"Reminders" */};
	private final FormText inpAdresse;
	private final ListDisplay<BezugsKontakt> inpZusatzAdresse /*, dlReminder */;
	private final FixMediDisplay dmd;
	Patient actPatient;
	IViewSite viewsite;
	private final Hyperlinkreact hr=new Hyperlinkreact();
    private final ScrolledForm form;
    private final ViewMenus viewmenu;
    private final ExpandableComposite ecdm,ecZA;
    private boolean bLocked=true;
    private Composite cUserfields;
    Hyperlink hHA;
    
    void recreateUserpanel(){
	    	//cUserfields.setRedraw(false);
	    	if(ipp!=null){
	    		ipp.dispose();
	    		ipp=null;
	    	}
	    	
	    	ArrayList<InputData> fields=new ArrayList<InputData>(20);
	    	fields.add(new InputData(Messages.getString("Patientenblatt2.name"),Patient.NAME,InputData.Typ.STRING,null)); //$NON-NLS-1$
			fields.add(new InputData(Messages.getString("Patientenblatt2.firstname"),Patient.FIRSTNAME,InputData.Typ.STRING,null)); //$NON-NLS-1$
			fields.add(new InputData(Messages.getString("Patientenblatt2.birthdate"),Patient.BIRTHDATE,InputData.Typ.DATE,null)); //$NON-NLS-1$
			fields.add(new InputData(Messages.getString("Patientenblatt2.sex"),Patient.SEX,null,new String[]{Person.FEMALE,Person.MALE},false)); //$NON-NLS-1$
			fields.add(new InputData(Messages.getString("Patientenblatt2.phone1"),Patient.PHONE1,InputData.Typ.STRING,null)); //$NON-NLS-1$
			fields.add(new InputData(Messages.getString("Patientenblatt2.phone2"),Patient.PHONE2,InputData.Typ.STRING,null)); //$NON-NLS-1$
			fields.add(new InputData(Messages.getString("Patientenblatt2.mobile"),Patient.MOBILE,InputData.Typ.STRING,null)); //$NON-NLS-1$
			fields.add(new InputData(Messages.getString("Patientenblatt2.fax"),Patient.FAX,InputData.Typ.STRING,null)); //$NON-NLS-1$
			fields.add(new InputData(Messages.getString("Patientenblatt2.email"),Patient.E_MAIL,InputData.Typ.STRING,null)); //$NON-NLS-1$
			fields.add(new InputData(Messages.getString("Patientenblatt2.group"),Patient.GROUP,InputData.Typ.STRING,null)); //$NON-NLS-1$
			fields.add(new InputData(Messages.getString("Patientenblatt2.balance"),Patient.BALANCE,new LabeledInputField.IContentProvider(){ //$NON-NLS-1$

				public void displayContent(PersistentObject po, InputData ltf) {
					ltf.setText(actPatient.getKontostand().getAmountAsString());
					
				}

				public void reloadContent(PersistentObject po, InputData ltf) {
					if(new AddBuchungDialog(getShell(),actPatient).open()==Dialog.OK){
						ltf.setText(actPatient.getKontostand().getAmountAsString());
					}
				}
				
			}));

	    	String[] userfields=Hub.userCfg.get(CFG_EXTRAFIELDS,StringConstants.EMPTY).split(StringConstants.COMMA);
	    	for(String extfield:userfields){
	    		if(!StringTool.isNothing(extfield)){
	    			fields.add(new InputData(extfield,Patient.EXTINFO,InputData.Typ.STRING,extfield));
	    		}
	    	}
			ipp=new InputPanel(cUserfields,2,6,fields.toArray(new InputData[0]));
	    	ipp.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
	    	ipp.changed(ipp.getChildren());
		    	// cUserfields.setRedraw(true);
	    	cUserfields.setBounds(ipp.getBounds());
    }
	Patientenblatt2(final Composite parent, final IViewSite site)
	{
		super(parent,SWT.NONE);
		viewsite=site;
		parent.setLayout(new FillLayout());
		setLayout(new FillLayout());
        tk=Desk.getToolkit();
    	form=tk.createScrolledForm(this);
        form.getBody().setLayout(new GridLayout());
        cUserfields=new Composite(form.getBody(),SWT.NONE);
        cUserfields.setLayout(new GridLayout());
        cUserfields.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
        recreateUserpanel();
        
        Composite cPersonalien=tk.createComposite(form.getBody());
        cPersonalien.setLayout(new GridLayout(2,false));
        cPersonalien.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
        hHA=tk.createHyperlink(cPersonalien,Messages.getString("Patientenblatt2.postal"),SWT.NONE); //$NON-NLS-1$
		hHA.addHyperlinkListener(hr);
		hHA.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		inpAdresse=tk.createFormText(cPersonalien,false);
		inpAdresse.setText("---\n",false,false); //$NON-NLS-1$
		inpAdresse.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		
		IExpansionListener ecExpansionListener =  new ExpansionAdapter(){
            @Override
            public void expansionStateChanging(final ExpansionEvent e)
            {
            	ExpandableComposite src=(ExpandableComposite)e.getSource();
                UserSettings2.saveExpandedState(KEY_PATIENTENBLATT+src.getText(), e.getState());
            }
            
        };
		
        ecZA=WidgetFactory.createExpandableComposite(tk, form, Messages.getString("Patientenblatt2.additionalAdresses")); //$NON-NLS-1$
        UserSettings2.setExpandedState(ecZA,"Patientenblatt/Zusatzadressen"); //$NON-NLS-1$

        ecZA.addExpansionListener(ecExpansionListener);
        
		inpZusatzAdresse=new ListDisplay<BezugsKontakt>(ecZA,SWT.NONE,new ListDisplay.LDListener(){
			/*
			public boolean dropped(final PersistentObject dropped) {
				return false;
			}
			 */
			
			public void hyperlinkActivated(final String l) {
				KontaktSelektor ksl=new KontaktSelektor(getShell(),Kontakt.class,Messages.getString("Patientenblatt2.contactForAdditionalAddress"),Messages.getString("Patientenblatt2.pleaseSelectardress")); //$NON-NLS-1$ //$NON-NLS-2$
				if(ksl.open()==Dialog.OK){
					Kontakt k=(Kontakt) ksl.getSelection();
					BezugsKontaktAuswahl bza=new BezugsKontaktAuswahl();
					//InputDialog id=new InputDialog(getShell(),"Bezugstext für Adresse","Geben Sie bitte einen Text ein, der die Bedeutung dieser Adresse erklärt","",null);
					if(bza.open()==Dialog.OK){
						String bezug=bza.getResult();
						BezugsKontakt bk=actPatient.addBezugsKontakt(k, bezug);
						inpZusatzAdresse.add(bk);
						form.reflow(true);
					}
					
				}
			
			}

			public String getLabel(Object o) {
				BezugsKontakt bezugsKontakt = (BezugsKontakt) o;

				StringBuffer sb = new StringBuffer();
				sb.append(bezugsKontakt.getLabel());
				
				Kontakt other = Kontakt.load(bezugsKontakt.get(BezugsKontakt.OTHER_ID));
				if (other.exists()) {
					List<String> tokens = new ArrayList<String>();
					
					String telefon1 = other.get(Kontakt.PHONE1);
					String telefon2 = other.get(Kontakt.PHONE2);
					String mobile = other.get(Kontakt.MOBILEPHONE);
					String eMail = other.get(Kontakt.E_MAIL);
					String fax = other.get(Kontakt.FAX);
					
					if (!StringTool.isNothing(telefon1)) {
						tokens.add("T1: " + telefon1); //$NON-NLS-1$
					}
					if (!StringTool.isNothing(telefon2)) {
						tokens.add("T2: " + telefon2); //$NON-NLS-1$
					}
					if (!StringTool.isNothing(mobile)) {
						tokens.add("M: " + mobile); //$NON-NLS-1$
					}
					if (!StringTool.isNothing(fax)) {
						tokens.add("F: " + fax); //$NON-NLS-1$
					}
					if (!StringTool.isNothing(eMail)) {
						tokens.add(eMail);
					}
					for (String token : tokens) {
						sb.append(", "); //$NON-NLS-1$
						sb.append(token);
					}
					return sb.toString();
				}
				return "?"; //$NON-NLS-1$
			}});
		inpZusatzAdresse.addHyperlinks(Messages.getString("Patientenblatt2.add")); //$NON-NLS-1$
		inpZusatzAdresse.setMenu(createZusatzAdressMenu());
		
        ecZA.setClient(inpZusatzAdresse);
		for(int i=0;i<lbExpandable.length;i++){
            ec[i]=WidgetFactory.createExpandableComposite(tk, form, lbExpandable[i]);
            UserSettings2.setExpandedState(ec[i], KEY_PATIENTENBLATT+lbExpandable[i]);
			txExpandable[i]=tk.createText(ec[i], "" , SWT.MULTI); //$NON-NLS-1$
			txExpandable[i].addFocusListener(new Focusreact(dfExpandable[i]));
            ec[i].setData(KEY_DBFIELD,dfExpandable[i]);
            ec[i].addExpansionListener(new ExpansionAdapter(){
                @Override
                public void expansionStateChanging(final ExpansionEvent e)
                {
                	ExpandableComposite src=(ExpandableComposite)e.getSource();
                    if(e.getState()==true){
                        Text tx=(Text)src.getClient();
                        if (actPatient != null) {
                        	tx.setText(StringTool.unNull(actPatient.get((String)src.getData(KEY_DBFIELD))));
                        } else {
                        	tx.setText(""); //$NON-NLS-1$
                        }
                    }
                    UserSettings2.saveExpandedState(KEY_PATIENTENBLATT+src.getText(), e.getState());
                }
                
            });
            txExpandable[i].addKeyListener(new KeyListener() {
                @Override
                public void keyReleased(KeyEvent e) {
                        Text tx = (Text) e.getSource();
                        tx.redraw();
                        form.getBody().layout(true);
                }  
                @Override
                public void keyPressed(KeyEvent e) {} 
});
            
            ec[i].setClient(txExpandable[i]);
		}
		ecdm=WidgetFactory.createExpandableComposite(tk, form, FIXMEDIKATION);
		UserSettings2.setExpandedState(ecdm, KEY_PATIENTENBLATT+FIXMEDIKATION);
		ecdm.addExpansionListener(ecExpansionListener);
		dmd=new FixMediDisplay(ecdm,site);
		ecdm.setClient(dmd);
		makeActions();
		viewmenu=new ViewMenus(viewsite);
		viewmenu.createMenu(GlobalActions.printEtikette, GlobalActions.printAdresse,GlobalActions.printBlatt,GlobalActions.printRoeBlatt);
        viewmenu.createToolbar(lockAction);
		GlobalEvents.getInstance().addActivationListener(this,site.getPart());
        tk.paintBordersFor(form.getBody());
	}
    
	
	
	@Override
	public void dispose(){
		GlobalEvents.getInstance().removeSelectionListener(this);
		GlobalEvents.getInstance().removeActivationListener(this,viewsite.getPart());
		super.dispose();
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
				if(bLocked){
					((Text)e.getSource()).setText(oldvalue);
				}else{
					actPatient.set(field,newvalue);
				}
			}
		}
	}
	private Menu createZusatzAdressMenu()
    {
            Menu ret=new Menu(inpZusatzAdresse);
            delZA=new MenuItem(ret,SWT.NONE);
            delZA.setText(Messages.getString("Patientenblatt2.removeAddress")); //$NON-NLS-1$
            delZA.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected(final SelectionEvent e)
                {
                	if(!bLocked){
                		BezugsKontakt a=(BezugsKontakt)inpZusatzAdresse.getSelection();
                		actPatient.removeBezugsKontakt(Kontakt.load(a.get(BezugsKontakt.OTHER_ID)));
                		setPatient(actPatient);
                	}
                }
                
            });
            MenuItem showZA=new MenuItem(ret,SWT.NONE);
            showZA.setText(Messages.getString("Patientenblatt2.showAddress")); //$NON-NLS-1$
            showZA.addSelectionListener(new SelectionAdapter(){
            	 @Override
                 public void widgetSelected(final SelectionEvent e)
                 {
                     Kontakt a=Kontakt.load(((BezugsKontakt)inpZusatzAdresse.getSelection()).get(BezugsKontakt.OTHER_ID));
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
			    inpAdresse.setText(actPatient.getPostAnschrift(false),false,false);
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
		ipp.getAutoForm().reload(actPatient);
		
		if(actPatient==null){
            form.setText(Messages.getString("Patientenblatt2.noPatientSelected")); //$NON-NLS-1$
            inpAdresse.setText(StringConstants.EMPTY,false,false);
            inpZusatzAdresse.clear();
            return;
        }

		form.setText(StringTool.unNull(p.getName())+StringConstants.SPACE+StringTool.unNull(p.getVorname())+" ("+p.getPatCode()+")");  //$NON-NLS-1$ //$NON-NLS-2$
        inpAdresse.setText(p.getPostAnschrift(false),false,false);
        UserSettings2.setExpandedState(ecZA, "Patientenblatt/Zusatzadressen"); //$NON-NLS-1$
		inpZusatzAdresse.clear();
		for(BezugsKontakt za : p.getBezugsKontakte()){
			inpZusatzAdresse.add(za);
		}
		
        
		for(int i=0;i<dfExpandable.length;i++){
			UserSettings2.setExpandedState(ec[i], KEY_PATIENTENBLATT+ec[i].getText());
            if(ec[i].isExpanded()==true){
                txExpandable[i].setText(p.get(dfExpandable[i]));
            }
		}
		dmd.reload();
		form.reflow(true);
		setLocked(true);
	}
    public void refresh(){
    	form.reflow(true);
    }

    public void selectionEvent(final PersistentObject obj)
    {
        if(obj instanceof Patient){
            setPatient((Patient)obj);
        }else if(obj instanceof Anwender){
        	setPatient(GlobalEvents.getSelectedPatient());
        	recreateUserpanel();
        }
    }
	
	private void makeActions(){
		lockAction=new RestrictedAction(AccessControlDefaults.PATIENT_MODIFY,Messages.getString("Patientenblatt2.saved"),Action.AS_CHECK_BOX){ //$NON-NLS-1$
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_LOCK_CLOSED));
				setToolTipText(Messages.getString("Patientenblatt2.savedToolTip")); //$NON-NLS-1$
				setChecked(true);
			}
			@Override
			public void doRun() {
				setLocked(isChecked());
			}
			
		};
	}

	public void setLocked(boolean bLock){
		bLocked=bLock;
		ipp.setLocked(bLock);
		inpZusatzAdresse.enableHyperlinks(!bLock);
		hHA.setEnabled(!bLock);
		delZA.setEnabled(!bLock);
		if(bLock){
			hHA.setForeground(Desk.getColor(Desk.COL_GREY));
			lockAction.setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_LOCK_CLOSED));
		}else{
			hHA.setForeground(Desk.getColor(Desk.COL_BLUE));
			lockAction.setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_LOCK_OPEN));
		}
		lockAction.setChecked(bLock);
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
	
	class BezugsKontaktAuswahl extends Dialog{
		Combo cbType;
		String result=""; //$NON-NLS-1$
		
		public BezugsKontaktAuswahl() {
			super(Patientenblatt2.this.getShell());
		}

		@Override
		public void create() {
			super.create();
			getShell().setText(Messages.getString("Patientenblatt2.kindOfRelation")); //$NON-NLS-1$
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite ret=(Composite)super.createDialogArea(parent);
			new Label(ret,SWT.NONE).setText(Messages.getString("Patientenblatt2.pleaseEnterKindOfRelationship")); //$NON-NLS-1$
			cbType=new Combo(ret,SWT.NONE);
			cbType.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			String bez=Hub.globalCfg.get(CFG_BEZUGSKONTAKTTYPEN, ""); //$NON-NLS-1$
			cbType.setItems(bez.split(SPLITTER));
			return ret;
		}

		@Override
		protected void okPressed() {
			result=cbType.getText();
			String[] items=cbType.getItems();
			String nitem=cbType.getText();
			String res=""; //$NON-NLS-1$
			if(StringTool.getIndex(items, nitem)==-1){
				res=nitem+SPLITTER;
			}
			Hub.globalCfg.set(CFG_BEZUGSKONTAKTTYPEN, res+StringTool.join(items, SPLITTER));
			super.okPressed();
		}
		public String getResult(){
			return result;
		}
		
	}
	
}
