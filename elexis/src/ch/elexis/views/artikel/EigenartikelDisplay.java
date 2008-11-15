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
 *  $Id: EigenartikelDisplay.java 4683 2008-11-15 20:39:23Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views.artikel;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.widgets.*;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Artikel;
import ch.elexis.data.Eigenartikel;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.dialogs.KontaktSelektor;
import ch.elexis.util.LabeledInputField;
import ch.elexis.util.LabeledInputField.InputData;
import ch.elexis.util.LabeledInputField.InputData.Typ;
import ch.elexis.views.IDetailDisplay;

public class EigenartikelDisplay implements IDetailDisplay {
	
	static final public InputData[] getFieldDefs(final Shell shell){
		InputData[] ret =
			new InputData[] {
				new InputData("Typ", "Typ", Typ.STRING, null),
				//new InputData("EANCode", "ExtInfo", Typ.STRING, "EAN"),
				//new InputData("Pharmacode", "ExtInfo", Typ.STRING, "Pharmacode"),
				new InputData("Gruppe","Codeclass",Typ.STRING,null),
				new InputData("Einkaufspreis", "EK_Preis", Typ.CURRENCY, null),
				new InputData("Verkaufspreis", "VK_Preis", Typ.CURRENCY, null),
				new InputData("Max. Pckg. an Lager", "Maxbestand", Typ.STRING, null),
				new InputData("Min. Pckg. an Lager", "Minbestand", Typ.STRING, null),
				new InputData("Aktuell Pckg. an Lager", "Istbestand", Typ.STRING, null),
				new InputData("Aktuell an Lager", "ExtInfo", Typ.INT, "Anbruch"),
				new InputData("Stück pro Packung", "ExtInfo", Typ.INT, "Verpackungseinheit"),
				new InputData("Stück pro Abgabe", "ExtInfo", Typ.INT, "Verkaufseinheit"),
				new InputData("Lieferant", "Lieferant", new LabeledInputField.IContentProvider() {
					public void displayContent(PersistentObject po, InputData ltf){
						String lbl = ((Artikel) po).getLieferant().getLabel();
						if (lbl.length() > 15) {
							lbl = lbl.substring(0, 12) + "...";
						}
						ltf.setText(lbl);
					}
					
					public void reloadContent(PersistentObject po, InputData ltf){
						KontaktSelektor ksl =
							new KontaktSelektor(shell, Kontakt.class, "Lieferant",
								"Bitte wählen Sie, wer diesen Artikel liefert");
						if (ksl.open() == Dialog.OK) {
							Kontakt k = (Kontakt) ksl.getSelection();
							((Artikel) po).setLieferant(k);
							String lbl = ((Artikel) po).getLieferant().getLabel();
							if (lbl.length() > 15) {
								lbl = lbl.substring(0, 12) + "...";
							}
							ltf.setText(lbl);
							GlobalEvents.getInstance().fireUpdateEvent(Artikel.class);
						}
					}
					
				})
			};
		return ret;
	}
	
	FormToolkit tk = Desk.getToolkit();
	ScrolledForm form;
	LabeledInputField.AutoForm tblArtikel;
	
	public Composite createDisplay(Composite parent, IViewSite site){
		parent.setLayout(new FillLayout());
		form = tk.createScrolledForm(parent);
		Composite ret = form.getBody();
		TableWrapLayout twl = new TableWrapLayout();
		ret.setLayout(twl);
		tblArtikel =
			new LabeledInputField.AutoForm(ret, getFieldDefs(parent.getShell()));
		
		TableWrapData twd = new TableWrapData(TableWrapData.FILL_GRAB);
		twd.grabHorizontal = true;
		tblArtikel.setLayoutData(twd);
		return ret;
		
	}
	
	public Class<? extends PersistentObject> getElementClass(){
		return Eigenartikel.class;
	}
	
	public void display(Object obj){
		if (obj instanceof Eigenartikel) {
			Eigenartikel m = (Eigenartikel) obj;
			form.setText(m.getLabel());
			tblArtikel.reload(m);
		}
		
	}
	
	public String getTitle(){
		return "Eigenartikel";
	}
	
}
