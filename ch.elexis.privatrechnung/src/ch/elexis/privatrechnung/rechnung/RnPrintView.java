/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 * $Id: RnPrintView.java 5973 2010-01-27 17:36:06Z rgw_ch $
 *******************************************************************************/
package ch.elexis.privatrechnung.rechnung;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Hub;
import ch.elexis.actions.ElexisEventDispatcher;
import ch.elexis.banking.ESR;
import ch.elexis.data.Brief;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Rechnung;
import ch.elexis.data.Verrechnet;
import ch.elexis.privatrechnung.data.PreferenceConstants;
import ch.elexis.text.ITextPlugin;
import ch.elexis.text.TextContainer;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

public class RnPrintView extends ViewPart {
	final static String ID = "ch.elexis.privatrechnung.view";
	String templateBill, templateESR;
	TextContainer tc;
	Fall fall;
	
	@Override
	public void createPartControl(final Composite parent){
		tc = new TextContainer(parent.getShell());
		tc.getPlugin().createContainer(parent, new ITextPlugin.ICallback() {
			
			public void save(){
			// we don't save
			}
			
			public boolean saveAs(){
				return false; // nope
			}
		});
		
	}
	
	@Override
	public void setFocus(){
	// TODO Auto-generated method stub
	
	}
	
	/**
	 * print a bill into a text container
	 */
	public Result<Rechnung> doPrint(final Rechnung rn){
		if (templateBill == null) {
			templateBill = Hub.globalCfg.get(PreferenceConstants.cfgTemplateBill, "");
		}
		if (templateESR == null) {
			templateESR = Hub.globalCfg.get(PreferenceConstants.cfgTemplateESR, "");
		}
		
		Result<Rechnung> ret = new Result<Rechnung>();
		fall = rn.getFall();
		ElexisEventDispatcher.fireSelectionEvent(fall);
		Kontakt adressat = fall.getGarant();// .getRequiredContact("Rechnungsempfänger");
		if (!adressat.isValid()) {
			adressat = fall.getPatient();
		}
		tc.createFromTemplateName(null, templateBill, Brief.RECHNUNG, adressat, rn.getNr());
		fillFields();
		List<Konsultation> kons = rn.getKonsultationen();
		Collections.sort(kons, new Comparator<Konsultation>() {
			TimeTool t0 = new TimeTool();
			TimeTool t1 = new TimeTool();
			
			public int compare(final Konsultation arg0, final Konsultation arg1){
				t0.set(arg0.getDatum());
				t1.set(arg1.getDatum());
				return t0.compareTo(t1);
			}
			
		});
		Object pos = null;
		// Das Wort Leistungen soll jeder selbst in die Vorlage nehmen:
		// pos = tc.getPlugin().insertText("[Leistungen]", "Leistungen\n\n", SWT.LEFT);
		pos = tc.getPlugin().insertText("[Leistungen]", "", SWT.LEFT);
		Money sum = new Money();
		for (Konsultation k : kons) {
			tc.getPlugin().setStyle(SWT.BOLD);
			pos =
				tc.getPlugin().insertText(pos,
					new TimeTool(k.getDatum()).toString(TimeTool.DATE_GER) + "\n", SWT.LEFT);
			tc.getPlugin().setStyle(SWT.NORMAL);
			for (Verrechnet vv : k.getLeistungen()) {
				Money preis = vv.getNettoPreis();
				int zahl = vv.getZahl();
				Money subtotal = new Money(preis);
				subtotal.multiply(zahl);
				StringBuilder sb = new StringBuilder();
				sb.append(zahl).append("\t").append(vv.getText()).append("\t").append(
					preis.getAmountAsString()).append("\t").append(subtotal.getAmountAsString())
					.append("\n");
				pos = tc.getPlugin().insertText(pos, sb.toString(), SWT.LEFT);
				sum.addMoney(subtotal);
			}
		}
		pos =
			tc.getPlugin().insertText(
				pos,
				"____________________________________________________________________\nTotal:\t\t"
					+ sum.getAmountAsString(), SWT.LEFT);
		String toPrinter = Hub.localCfg.get("Drucker/A4/Name", null);
		tc.getPlugin().print(toPrinter, null, false);
		tc.createFromTemplateName(null, templateESR, Brief.RECHNUNG, adressat, rn.getNr());
		fillFields();
		ESR esr =
			new ESR(Hub.globalCfg.get(PreferenceConstants.esrIdentity, ""), Hub.globalCfg.get(
				PreferenceConstants.esrUser, ""), rn.getRnId(), 27);
		Kontakt bank = Kontakt.load(Hub.globalCfg.get(PreferenceConstants.cfgBank, ""));
		if (!bank.isValid()) {
			SWTHelper.showError("Keine Bank", "Bitte geben Sie eine Bank für die Zahlungen ein");
		}
		esr.printBESR(bank, adressat, rn.getMandant(), sum.getCentsAsString(), tc);
		tc.replace("\\[Leistungen\\]", sum.getAmountAsString());
		tc.getPlugin().print(Hub.localCfg.get("Drucker/A4ESR/Name", null), null, false);
		return ret;
	}
	
	private void fillFields(){
		Kontakt versicherung = Kontakt.load(fall.getInfoString("Versicherung"));
		if (versicherung.isValid()) {
			tc.replace("\\?\\?Versicherung\\.Name\\?\\?]", versicherung.getLabel());
			tc.replace("\\?\\?Versicherung\\.Anschrift\\?\\?", versicherung.getPostAnschrift(true));
		}
		
	}
	
}
