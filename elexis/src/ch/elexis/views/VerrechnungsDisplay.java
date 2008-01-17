/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: VerrechnungsDisplay.java 3550 2008-01-17 12:13:19Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.text.ParseException;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Artikel;
import ch.elexis.data.IVerrechenbar;
import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Verrechnet;
import ch.elexis.util.Log;
import ch.elexis.util.Money;
import ch.elexis.util.PersistentObjectDropTarget;
import ch.elexis.util.Result;
import ch.elexis.util.SWTHelper;
import ch.elexis.views.codesystems.LeistungenView;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

public class VerrechnungsDisplay extends Composite {
	Table tVerr;
	private Hyperlink hVer;
	private PersistentObjectDropTarget dropTarget;
	private Log log=Log.get("VerrechnungsDisplay");
	
	VerrechnungsDisplay(final IWorkbenchPage page, Composite parent, int style){
		super(parent,style);
		setLayout(new GridLayout());
		hVer=Desk.theToolkit.createHyperlink(this,"Verrechnung",SWT.NONE);
        hVer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));
        hVer.addHyperlinkListener(new HyperlinkAdapter(){
			@Override
			public void linkActivated(HyperlinkEvent e) {
				try{
					// TODO
					if(StringTool.isNothing(LeistungenView.ID)){
						SWTHelper.alert("Fehler", "LeistungenView.ID");
					}
					page.showView(LeistungenView.ID);
					GlobalEvents.getInstance().setCodeSelectorTarget(dropTarget);
				}catch(Exception ex){
					ExHandler.handle(ex);
					log.log("Fehler beim Starten des Leistungscodes "+ex.getMessage(),Log.ERRORS );
				}
			}
        });

		tVerr=Desk.theToolkit.createTable(this,SWT.SINGLE);
        tVerr.setLayoutData(new GridData(GridData.FILL_BOTH));
        tVerr.setMenu(createVerrMenu());
        dropTarget=new PersistentObjectDropTarget("Verrechnen",tVerr,new DropReceiver());
	}
	
	public void clear(){
		tVerr.removeAll();
	}
	
	public void addPersistentObject(PersistentObject o) {
		Konsultation actKons = GlobalEvents.getSelectedKons();
		if (actKons != null) {
			if (o instanceof IVerrechenbar) {
				if (Hub.acl.request(AccessControlDefaults.LSTG_VERRECHNEN) == false) {
					SWTHelper
							.alert("Fehlende Rechte",
									"Sie haben nicht die Berechtigung, Leistungen zu verrechnen");
				} else {
					boolean exists = false;
					// TODO Immi
					Verrechnet foundVerrechnet = null;
					for (Verrechnet verrechnet: actKons.getLeistungen()) {
						if (verrechnet.getVerrechenbar().getId().equals(o.getId())) {
							foundVerrechnet = verrechnet;
						}
					}
					
					if (foundVerrechnet != null) {
						changeAnzahl(foundVerrechnet, foundVerrechnet.getZahl() + 1);
					} else {
						Result<IVerrechenbar> result = actKons
						.addLeistung((IVerrechenbar) o);

						if (!result.isOK()) {
							SWTHelper.alert("Diese Verrechnung ist ungültig",
									result.toString());
						}
						setLeistungen(actKons);
					}
				}
			}
		}
	}
	
	private final class DropReceiver implements PersistentObjectDropTarget.Receiver {
		public void dropped(PersistentObject o, DropTargetEvent ev) {
			addPersistentObject(o);
		}

		public boolean accept(PersistentObject o) {
			if (GlobalEvents.getSelectedPatient() != null) {
				if(o instanceof IVerrechenbar){
					return true;
				}
			}
			return false;
		}
	}
	
	void setLeistungen(Konsultation b){
        List<Verrechnet> lgl=b.getLeistungen();
        //DecimalFormat df=new DecimalFormat("0.00");
        //Collections.sort(lgl,TarmedLeistung.tarmedComparator);
        tVerr.setRedraw(false);
        tVerr.removeAll();
        StringBuilder sdg=new StringBuilder();
        Money sum=new Money(0);
        //double sum=0;
        for(Verrechnet lst:lgl){
            sdg.setLength(0);
            int z=lst.getZahl();
            //double preis=(z*lst.getEffPreisInRappen())/100.0;
            Money preis=lst.getEffPreis().multiply(z);
            sum.addMoney(preis);
            sdg.append(z).append(" ").append(lst.getCode())
             .append(" ").append(lst.getText())
             .append(" (").append(preis.getAmountAsString()).append(")");
            TableItem ti=new TableItem(tVerr,SWT.WRAP);
            ti.setText(sdg.toString());
            ti.setData(lst);
        }
        tVerr.setRedraw(true);
        sdg.setLength(0);
        sdg.append("Verrechnung (")
         .append(sum.getAmountAsString()).append(")");
        hVer.setText(sdg.toString());
    }
	
	class delVerrListener extends SelectionAdapter{
        public void widgetSelected(SelectionEvent e){
        	int sel=tVerr.getSelectionIndex();
        	TableItem ti=tVerr.getItem(sel);
        	Result result=GlobalEvents.getSelectedKons().removeLeistung((Verrechnet)ti.getData());
            if(!result.isOK()){
                SWTHelper.alert("Leistungsposition kann nicht entfernt werden",result.toString());
            }
            setLeistungen(GlobalEvents.getSelectedKons());
        }
    }
	
	private void changeAnzahl(Verrechnet v, int neuAnzahl) {
		int vorher=v.getZahl();
		v.setZahl(neuAnzahl);
		IVerrechenbar vv=v.getVerrechenbar();
		if(vv instanceof Artikel){
			Artikel art=(Artikel)vv;
			art.einzelRuecknahme(vorher);
			art.einzelAbgabe(neuAnzahl);
		}
		setLeistungen(GlobalEvents.getSelectedKons());
	}
	
    private Menu createVerrMenu(){
        Menu ret=new Menu(tVerr);
        MenuItem delVerr=new MenuItem(ret,SWT.NONE);
        delVerr.setText("Leistungsposition entfernen");
        delVerr.addSelectionListener(new delVerrListener());
        MenuItem chgPrice=new MenuItem(ret,SWT.NONE);
        chgPrice.setText("Preis ändern");
        chgPrice.addSelectionListener(new SelectionAdapter(){
        	@Override
        	public void widgetSelected(SelectionEvent e){
        		int sel=tVerr.getSelectionIndex();
        		//String ext=actBehandlung.getFall().getGesetz();
        		TableItem ti=tVerr.getItem(sel);
        		Verrechnet v=(Verrechnet)ti.getData();
        		//String p=Rechnung.geldFormat.format(v.getEffPreisInRappen()/100.0);
        		String p=v.getEffPreis().getAmountAsString();
        		InputDialog dlg=new InputDialog(Desk.theDisplay.getActiveShell(),"Preis für Leistung ändern","Geben Sie bitte den neuen Preis für die Leistung ein (x.xx)",p,null);
        		if(dlg.open()==Dialog.OK){
        			//v.setPreisInRappen(Integer.parseInt(dlg.getValue().replaceAll("\\.","")));
        			try{
        				Money newPrice=new Money(dlg.getValue());
        				v.setPreis(newPrice);
        				setLeistungen(GlobalEvents.getSelectedKons());
        			}catch(ParseException ex){
        				SWTHelper.showError("Falsche Betragseingabe", "Der eingegebene Betrag konnte nicht interpretiert werden");
        			}
        		}
        	}
        });
        MenuItem chgNumber=new MenuItem(ret,SWT.NONE);
        chgNumber.setText("Zahl ändern");
        chgNumber.addSelectionListener(new SelectionAdapter(){
        	@Override
        	public void widgetSelected(SelectionEvent e){
        		int sel=tVerr.getSelectionIndex();
        		TableItem ti=tVerr.getItem(sel);
        		Verrechnet v=(Verrechnet)ti.getData();
        		String p=Integer.toString(v.getZahl());
        		InputDialog dlg=new InputDialog(Desk.theDisplay.getActiveShell(),"Zahl der Leistung ändern","Geben Sie bitte die neue Anwendungszahl für die Leistung bzw. den Artikel ein",p,null);
        		if(dlg.open()==Dialog.OK){
        			try{
        				int neu=Integer.parseInt(dlg.getValue());
        				changeAnzahl(v, neu);
        				//int vorher=v.getZahl();
        			}catch(NumberFormatException ne){
        				SWTHelper.showError("Ungültige Eingabe", "Bitte geben Sie eine ganze Zahl ein");
        			}
        		}
        	}
        });
        MenuItem chgText=new MenuItem(ret,SWT.NONE);
        chgText.setText("Text ändern");
        chgText.addSelectionListener(new SelectionAdapter(){
        	@Override
        	public void widgetSelected(SelectionEvent e){
        		int sel=tVerr.getSelectionIndex();
        		TableItem ti=tVerr.getItem(sel);
        		Verrechnet v=(Verrechnet)ti.getData();
        		String oldText=v.getText();
        		InputDialog dlg=new InputDialog(Desk.theDisplay.getActiveShell(),"Text der Leistung ändern","Geben Sie bitte die neue Beschreibung für die Leistung bzw. den Artikel ein",oldText,null);
        		if(dlg.open()==Dialog.OK){
        			v.setText(dlg.getValue());
        			setLeistungen(GlobalEvents.getSelectedKons());
        		}
        	}
        });
        return ret;
    }

}
