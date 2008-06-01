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
 * $Id: KontaktBlatt.java 3991 2008-06-01 13:32:22Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Organisation;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.data.Xid;
import ch.elexis.data.Xid.XIDDomain;
import ch.elexis.dialogs.AnschriftEingabeDialog;
import ch.elexis.dialogs.KontaktExtDialog;
import ch.elexis.util.LabeledInputField;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.LabeledInputField.AutoForm;
import ch.elexis.util.LabeledInputField.InputData;
import ch.elexis.util.LabeledInputField.InputData.Typ;

public class KontaktBlatt extends Composite implements SelectionListener, ActivationListener{
	static final String[] types={"istOrganisation","istLabor","istPerson","istPatient","istAnwender","istMandant"};
	static final String[] typLabels={"Organisation","Labor","Person","Patient","Anwender","Mandant"};
	private Button[] bTypes=new Button[types.length];
	private TypButtonAdapter tba=new TypButtonAdapter();
	private IViewSite site;
	private ScrolledForm form;
	private FormToolkit tk;
	AutoForm afDetails;
	/*
	static final String[] ExtFlds={"EAN="+Xid.DOMAIN_EAN,
		"rEAN="+.DOMAIN_RECIPIENT_EAN,
		"AHV="+Xid.DOMAIN_AHV,
		"OID="+Xid.DOMAIN_OID,
		"UUID="+Xid.DOMAIN_ELEXIS
		};
	*/
	static final InputData[] def=new InputData[]{
		new InputData("Bezeichnung1"),
		new InputData("Bezeichnung2"),
		new InputData("Bezeichnung3"),
		new InputData("Geschlecht"),
		new InputData("Strasse"),
		new InputData("Plz"),
		new InputData("Ort"),
		new InputData("Land"),
		new InputData("Telefon1"),
		new InputData("Telefon2"),
		new InputData("Mobil-Tel.","NatelNr",Typ.STRING,null),
		new InputData("Fax"),
		new InputData("E-Mail"),
		new InputData("Website"),
		new InputData("Kürzel/ID","Kuerzel",Typ.STRING,null),
		new InputData("Bemerkung"),
		//new InputData("EAN","ExtInfo",Typ.STRING,"EAN"),
		new InputData("Titel"),
		new InputData("Externe IDs","UUID",new LabeledInputField.IContentProvider(){

			public void displayContent(PersistentObject po, InputData ltf) {
				StringBuilder sb=new StringBuilder();
				Xid xid=po.getXid();
				String dom=Xid.getSimpleNameForXIDDomain(xid.getDomain());
				sb.append(dom).append(": ").append(xid.getDomainId());
				ltf.setText(sb.toString());
			}

			public void reloadContent(PersistentObject po, InputData ltf) {
				ArrayList<String> extFlds=new ArrayList<String>();
				Kontakt k=(Kontakt)po;
				for(String dom:Xid.getXIDDomains()){
					XIDDomain xd=Xid.getDomain(dom);
					if((k.istPerson() && xd.isDisplayedFor(Person.class)) || 
						(k.istOrganisation() && xd.isDisplayedFor(Organisation.class)))	{
						extFlds.add(Xid.getSimpleNameForXIDDomain(dom)+"="+dom);
					}
				}
				KontaktExtDialog dlg=new KontaktExtDialog(Desk.getTopShell(),(Kontakt) po,extFlds.toArray(new String[0]));
				dlg.create();
				dlg.setTitle("Identifikationselemente anzeigen/setzen"); //$NON-NLS-1$
				dlg.getShell().setText("Identifikation");
				dlg.open();
				
			}
			
		}),
	};
	private Kontakt actKontakt;
	private Label lbAnschrift;
	
	public KontaktBlatt(Composite parent, int style, IViewSite vs){
		super(parent,style);
		site=vs;
		tk=Desk.getToolkit();
		setLayout(new FillLayout());
		form=tk.createScrolledForm(this);
		Composite body=form.getBody();
		body.setLayout(new GridLayout());
		Composite cTypes=tk.createComposite(body,SWT.BORDER);
		for(int i=0;i<types.length;i++){
			bTypes[i]=tk.createButton(cTypes,typLabels[i],SWT.CHECK);
			bTypes[i].addSelectionListener(tba);
			bTypes[i].setData(types[i]);
		}
		cTypes.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		cTypes.setLayout(new FillLayout());
		
		Composite bottom=tk.createComposite(body);
		bottom.setLayout(new FillLayout());
		bottom.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		actKontakt=(Kontakt)GlobalEvents.getInstance().getSelectedObject(Kontakt.class);
		afDetails=new AutoForm(bottom,def);
		Composite cAnschrift=tk.createComposite(body);
		cAnschrift.setLayout(new GridLayout(2, false));
		cAnschrift.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		Hyperlink hAnschrift=tk.createHyperlink(cAnschrift,"Anschrift",SWT.NONE);
		hAnschrift.addHyperlinkListener(new HyperlinkAdapter(){

			@Override
			public void linkActivated(HyperlinkEvent e) {
				new AnschriftEingabeDialog(getShell(),actKontakt).open();
				GlobalEvents.getInstance().fireSelectionEvent(actKontakt);
			}
			
		});
		lbAnschrift=tk.createLabel(cAnschrift,"",SWT.WRAP);
		lbAnschrift.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		//GlobalEvents.getInstance().addSelectionListener(this);
		GlobalEvents.getInstance().addActivationListener(this, site.getPart());
	}
	
	public void selectionEvent(PersistentObject obj) {
		if(obj instanceof Kontakt){
			if(!isEnabled()){
				setEnabled(true);
			}
			actKontakt=(Kontakt)obj;
			afDetails.reload(actKontakt);
			String[] ret=new String[types.length];
			actKontakt.get(types,ret);
			for(int i=0;i<types.length;i++){
				bTypes[i].setSelection((ret[i]==null) ? false : "1".equals( ret[i] ));
				if(Hub.acl.request(AccessControlDefaults.KONTAKT_MODIFY)==false){
					bTypes[i].setEnabled(false);
				}
			}
			if(bTypes[0].getSelection()==true){
				def[0].setLabel("Bezeichnung");
				def[1].setLabel("Zusatz");
				def[2].setLabel("Ansprechperson");
				def[3].setEditable(false);
				def[3].setText("");
				def[10].setLabel("Tel. direkt");
			}else{
				def[0].setLabel("Name");
				def[1].setLabel("Vorname");
				def[2].setLabel("Zusatz");
				def[3].setEditable(true);
				def[10].setLabel("Mobil");
			}
			lbAnschrift.setText(actKontakt.getPostAnschrift(false));
			form.reflow(true);
		}
		
	}
	@Override
	public void dispose(){
		GlobalEvents.getInstance().removeActivationListener(this, site.getPart());
		super.dispose();
	}
	private final class TypButtonAdapter extends SelectionAdapter {
		ArrayList<String> alTypes=new ArrayList<String>();
		ArrayList<String> alValues=new ArrayList<String>();
		@Override
		public void widgetSelected(SelectionEvent e) {
			Button b=(Button)e.getSource();
			String type=(String)b.getData();
			
			if(b.getSelection()==true){
				if(type.equals("istOrganisation")){
					select("1","x","0","0","0","0");
					def[0].setLabel("Bezeichnung");
					def[1].setLabel("Zusatz");
					def[2].setLabel("Ansprechperson");
					def[3].setText("");
					def[10].setLabel("Tel. direkt");
				}else if(type.equals("istLabor")){
					select("1","1","0","0","0","0");
					def[0].setLabel("Bezeichnung");
					def[1].setLabel("Zusatz");
					def[2].setLabel("Laborleiter");
					def[10].setLabel("Tel. direkt");
				}else{
					def[0].setLabel("Name");
					def[1].setLabel("Vorname");
					def[2].setLabel("Zusatz");
					def[10].setLabel("Mobil");
					if("istPerson".equals( type )){
						select("0","0","1","x","x","x");
					}else if(type.equals("istPatient")){
						select("0","0","1","1","x","x");
					}else if(type.equals("istAnwender")){
						select("0","0","1","x","1","x");
					}else if(type.equals("istMandant")){
						select("0","0","1","x","1","1");
					}
				}
			}else{
				actKontakt.set(type,"0");
			}
		}
		void select(String... fields){
			alTypes.clear();
			alValues.clear();
			for(int i=0;i<fields.length;i++){
				if(fields[i].equals("x")){
					continue;
				}
				alTypes.add(types[i]);
				alValues.add(fields[i]);
				bTypes[i].setSelection(fields[i].equals("1"));
			}
			actKontakt.set(alTypes.toArray(new String[0]),alValues.toArray(new String[0]));
		}
	}
	public void activation(boolean mode) {
		if(GlobalEvents.getInstance().getSelectedObject(Kontakt.class)==null){
			setEnabled(false);
		}else{
			setEnabled(true);
		}
		
	}

	public void visible(boolean mode) {
		if(mode==true){
			selectionEvent(GlobalEvents.getInstance().getSelectedObject(Kontakt.class));
			GlobalEvents.getInstance().addSelectionListener(this);
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
		
	}

	public void clearEvent(Class template) {
		setEnabled(false);
		
	}

}
