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
 *  $Id: Artikeldetail.java 1832 2007-02-18 09:12:31Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views.artikel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.data.Artikel;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.dialogs.KontaktSelektor;
import ch.elexis.util.LabeledInputField;
import ch.elexis.util.LabeledInputField.InputData;
import ch.elexis.util.LabeledInputField.InputData.Typ;

public class Artikeldetail extends ViewPart implements GlobalEvents.SelectionListener, ActivationListener, ISaveablePart2{
	public static final String ID="ch.elexis.ArtikelDetail";

	static final public InputData[] getFieldDefs(final Shell shell){ 
	    InputData[] ret=new InputData[]{
			new InputData("Typ","Typ",Typ.STRING,null),
			new InputData("EANCode","ExtInfo",Typ.STRING,"EAN"),
			new InputData("Pharmacode","ExtInfo",Typ.STRING,"Pharmacode"),
			new InputData("Einkaufspreis","EK_Preis",Typ.CURRENCY,null),
			new InputData("Verkaufspreis","VK_Preis",Typ.CURRENCY,null),
			new InputData("Max. Pckg. an Lager","Maxbestand",Typ.STRING,null),
			new InputData("Min. Pckg. an Lager","Minbestand",Typ.STRING,null),
			new InputData("Aktuell Pckg. an Lager","Istbestand",Typ.STRING,null),
			new InputData("Aktuell an Lager","ExtInfo",Typ.INT,"Anbruch"),
			new InputData("Stück pro Packung","ExtInfo",Typ.INT,"Verpackungseinheit"),
			new InputData("Stück pro Abgabe","ExtInfo",Typ.INT,"Verkaufseinheit"),
			new InputData("Lieferant","Lieferant",new LabeledInputField.IContentProvider(){
				public void displayContent(PersistentObject po,InputData ltf) {
					String lbl=((Artikel)po).getLieferant().getLabel();
					if(lbl.length()>15){
						lbl=lbl.substring(0,12)+"...";
					}
					ltf.setText(lbl);
				}
				public void reloadContent(PersistentObject po, InputData ltf) {
					KontaktSelektor ksl=new KontaktSelektor(shell,Kontakt.class,"Lieferant","Bitte wählen Sie, wer diesen Artikel liefert");
					if(ksl.open()==Dialog.OK){
						Kontakt k=(Kontakt)ksl.getSelection();
						((Artikel)po).setLieferant(k);
						String lbl=((Artikel)po).getLieferant().getLabel();
						if(lbl.length()>15){
							lbl=lbl.substring(0,12)+"...";
						}
						ltf.setText(lbl);
						GlobalEvents.getInstance().fireUpdateEvent(Artikel.class);
					}
				}
				
			})
	    };
	    return ret;
	}
	   
	FormToolkit tk=Desk.theToolkit;
	ScrolledForm form;
	LabeledInputField.AutoForm tblArtikel;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		form=tk.createScrolledForm(parent);
        TableWrapLayout twl=new TableWrapLayout();
		form.getBody().setLayout(twl);
		
		tblArtikel=new LabeledInputField.AutoForm(form.getBody(),getFieldDefs(parent.getShell()));
        
        TableWrapData twd=new TableWrapData(TableWrapData.FILL_GRAB);
        twd.grabHorizontal=true;
        tblArtikel.setLayoutData(twd);
        GlobalEvents.getInstance().addActivationListener(this,this);

	}

	@Override
	public void setFocus() {
		
	}

	@Override
	public void dispose() {
		GlobalEvents.getInstance().removeSelectionListener(this);
		GlobalEvents.getInstance().removeActivationListener(this,this);
		super.dispose();
	}

	public void selectionEvent(PersistentObject obj) {
		if(obj instanceof Artikel){
			form.setText(obj.getLabel());
			tblArtikel.reload(obj);

		}
	}

	public void activation(boolean mode) {
		// TODO Auto-generated method stub
		
	}

	public void visible(boolean mode) {
		if(mode==true){
			selectionEvent(GlobalEvents.getInstance().getSelectedObject(Artikel.class));
			GlobalEvents.getInstance().addSelectionListener(this);
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
	}

	public void clearEvent(Class template) {
		// TODO Auto-generated method stub
		
	}
	
	
	/* ******
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2
	 * Wir benötigen das Interface nur, um das Schliessen einer View zu verhindern,
	 * wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */ 
	public int promptToSaveOnClose() {
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL : ISaveablePart2.NO;
	}
	public void doSave(IProgressMonitor monitor) { /* leer */ }
	public void doSaveAs() { /* leer */}
	public boolean isDirty() {
		return true;
	}
	public boolean isSaveAsAllowed() {
		return false;
	}
	public boolean isSaveOnCloseNeeded() {
		return true;
	}	

}
