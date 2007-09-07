/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: BAGMediDetailBlatt.java 3109 2007-09-07 16:22:03Z rgw_ch $
 *******************************************************************************/

package ch.elexis.medikamente.bag.views;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Artikel;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.dialogs.KontaktSelektor;
import ch.elexis.medikamente.bag.data.BAGMedi;
import ch.elexis.medikamente.bag.data.Substance;
import ch.elexis.util.LabeledInputField;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.LabeledInputField.InputData;
import ch.elexis.util.LabeledInputField.InputData.Typ;

public class BAGMediDetailBlatt extends Composite {
	private final LabeledInputField.AutoForm fld;
	private final Text tSubstances;
	private final Composite parent;
	private final ScrolledForm form;
	
	InputData[] fields=new InputData[]{
			new InputData("Hersteller","ExtInfo",InputData.Typ.STRING,"HerstellerID"),
			new InputData("Generika","ExtInfo",InputData.Typ.STRING,"Generika"),
			new InputData("Pharmacode","ExtInfo",InputData.Typ.STRING,"Pharmacode"),
			new InputData("BAG-Dossier","ExtInfo",InputData.Typ.STRING,"BAG-Dossier"),
			new InputData("Swissmedic-Nr","ExtInfo",InputData.Typ.STRING,"Swissmedic-Nr"),
			new InputData("Swissmedic-Liste","ExtInfo",InputData.Typ.STRING,"Swissmedic-Liste"),
			new InputData("ExFactory","EK_Preis",InputData.Typ.CURRENCY,null),
			new InputData("Verkauf","VK_Preis",InputData.Typ.CURRENCY,null),
			new InputData("Limitatio","ExtInfo",InputData.Typ.STRING,"Limitatio"),
			new InputData("LimitatioPts","ExtInfo",InputData.Typ.STRING,"LimitatioPts"),
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
					KontaktSelektor ksl=new KontaktSelektor(Hub.getActiveShell(),Kontakt.class,"Lieferant","Bitte wählen Sie, wer diesen Artikel liefert");
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

	
	public BAGMediDetailBlatt(final Composite pr){
		super(pr,SWT.NONE);
		parent=pr;
		setLayout(new GridLayout());
		setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		form=Desk.theToolkit.createScrolledForm(this);
		Composite ret=form.getBody();
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout());
		fld=new LabeledInputField.AutoForm(ret,fields);
		fld.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		//fld.setEnabled(false);
		Desk.theToolkit.adapt(fld);
		tSubstances=SWTHelper.createText(Desk.theToolkit, ret, 3, SWT.BORDER|SWT.READ_ONLY|SWT.WRAP|SWT.V_SCROLL);
		tSubstances.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
	}
	public void display(final BAGMedi m){
		form.setText(m.getLabel());
		fld.reload(m);
		List<Substance> list=m.getSubstances();
		StringBuilder sb=new StringBuilder();
		for(Substance s:list){
			sb.append(s.getLabel()).append("\n");
		}
		tSubstances.setText(sb.toString());
	}
}
