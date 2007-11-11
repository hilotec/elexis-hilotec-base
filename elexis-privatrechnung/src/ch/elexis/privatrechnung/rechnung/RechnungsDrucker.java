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
 * $Id: RechnungsDrucker.java 3333 2007-11-11 16:12:14Z rgw_ch $
 *******************************************************************************/

package ch.elexis.privatrechnung.rechnung;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.Desk;
import ch.elexis.Hub;
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
import ch.elexis.util.IRnOutputter;
import ch.elexis.util.Money;
import ch.elexis.util.Result;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

public class RechnungsDrucker implements IRnOutputter {
	//private static String pageESR="privatrechnung/vorlageESR";
	//private static String  pageBill="privatrechnung/vorlageRn";
	String templateESR,templateBill;
	TextContainer tc;
	
	/**
	 * We'll take all sorts of bills
	 */
	public boolean canBill(final Fall fall) {
		return true;
	}

	/**
	 * We never storno
	 */
	public boolean canStorno(final Rechnung rn) {
		return false;
	}

	/**
	 * Create the Control that will be presented to the user before
	 * selecting the bill output target.
	 * Here we simply chose a template to use for the bill. In fact we need two
	 * templates: a template for the page with summary and giro and a template for the other pages
	 */
	public Control createSettingsControl(final Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout());
		new Label(ret,SWT.NONE).setText("Formatvorlage für Rechnung (ESR-Seite)");
		final Text tVorlageESR=new Text(ret,SWT.BORDER);
		tVorlageESR.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tVorlageESR.setText(Hub.globalCfg.get(PreferenceConstants.cfgTemplateESR, ""));
		tVorlageESR.addFocusListener(new FocusAdapter(){
			@Override
			public void focusLost(final FocusEvent ev){
				templateESR=tVorlageESR.getText();
				Hub.globalCfg.set(PreferenceConstants.cfgTemplateESR, templateESR);
			}
		});
		new Label(ret,SWT.NONE).setText("Formatvorlage für Rechnung (Folgeseiten)");
		final Text tVorlageRn=new Text(ret,SWT.BORDER);
		tVorlageRn.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tVorlageRn.setText(Hub.globalCfg.get(PreferenceConstants.cfgTemplateBill, ""));
		tVorlageRn.addFocusListener(new FocusAdapter(){
			@Override
			public void focusLost(final FocusEvent ev){
				templateBill=tVorlageRn.getText();
				Hub.globalCfg.set(PreferenceConstants.cfgTemplateBill, templateBill);
			}
		});
		tVorlageESR.setText(Hub.globalCfg.get(PreferenceConstants.cfgTemplateESR, "privatrechnung_ESR"));
		tVorlageRn.setText(Hub.globalCfg.get(PreferenceConstants.cfgTemplateBill, "privatrechnung_S2"));
		return ret;
	}

	/**
	 * Print the bill(s)
	 */
	public Result<Rechnung> doOutput(final TYPE type, final Collection<Rechnung> rnn) {
		if(templateBill==null){
			templateBill=Hub.globalCfg.get(PreferenceConstants.cfgTemplateBill, "");
		}
		if(templateESR==null){
			templateESR=Hub.globalCfg.get(PreferenceConstants.cfgTemplateESR, "");
		}
		Result<Rechnung> ret=new Result<Rechnung>(); //=new Result<Rechnung>(Log.ERRORS,99,"Not yet implemented",null,true);
		Dialog dlg=new Dialog(Desk.getTopShell()){

			@Override
			protected Control createDialogArea(Composite parent) {
				tc=new TextContainer(parent.getShell());
				Control ret= tc.getPlugin().createContainer(parent, new ITextPlugin.ICallback(){

					public void save() {
						// we don't save
					}

					public boolean saveAs() {
						return false;	// nope
					}});

				return ret;

			}
			
		};
		dlg.setBlockOnOpen(false);
		dlg.open();
	
		for(Rechnung rn:rnn){
			ret.add(doPrint(rn));
		}
		dlg.close();
		if(!ret.isOK()){
			ret.display("Fehler beim Rechnungsdruck");
		}
		return ret;
	}

	public String getDescription() {
		return "Privatrechnung auf Drucker";
	}

	
	/**
	 * print a bill into a text container
	 */
	public Result<Rechnung> doPrint(final Rechnung rn){
		Result<Rechnung> ret=new Result<Rechnung>();
		Fall fall=rn.getFall();
		Kontakt adressat=fall.getRequiredContact("Rechnungsempfänger");
		tc.createFromTemplateName(null, templateBill, Brief.RECHNUNG, adressat,rn.getNr());
		
		List<Konsultation> kons=rn.getKonsultationen();
		Collections.sort(kons, new Comparator<Konsultation>(){
			TimeTool t0=new TimeTool();
			TimeTool t1=new TimeTool();
			public int compare(final Konsultation arg0, final Konsultation arg1) {
				t0.set(arg0.getDatum());
				t1.set(arg1.getDatum());
				return t0.compareTo(t1);
			}
			
		});
		Object pos=tc.getPlugin().insertText("[Rechnung]", "Leistungen\n", SWT.LEFT);
		Money sum=new Money();
		for(Konsultation k:kons){
			tc.getPlugin().setFont("Helvetica", SWT.BOLD, 12);
			tc.getPlugin().insertText(pos, new TimeTool(k.getDatum()).toString(TimeTool.DATE_GER)+"\n", SWT.LEFT);
			tc.getPlugin().setFont("Helvetica", SWT.NORMAL, 10);
			for(Verrechnet vv:k.getLeistungen()){
				Money preis=vv.getEffPreis();
				int zahl=vv.getZahl();
				Money subtotal=preis;
				subtotal.multiply(zahl);
				StringBuilder sb=new StringBuilder();
				sb.append(zahl).append("\t").append(vv.getText()).append("\t").append(preis.getAmountAsString())
					.append("\t").append(subtotal.getAmountAsString()).append("\n");
				tc.getPlugin().insertText(pos, sb.toString(), SWT.LEFT);
				sum.addMoney(subtotal);
			}
		}
		String toPrinter=Hub.localCfg.get("Drucker/A4/Name",null);
		tc.getPlugin().print(toPrinter, null, false);
		tc.createFromTemplateName(null, templateESR, Brief.RECHNUNG, adressat, rn.getNr());
		ESR esr=new ESR(PreferenceConstants.esrIdentity,PreferenceConstants.esrUser,rn.getRnId(),27);
		Kontakt bank=Kontakt.load(Hub.globalCfg.get(PreferenceConstants.cfgBank,""));
		if(!bank.isValid()){
			SWTHelper.showError("Keine Bank", "Bitte geben Sie eine Bank für die Zahlungen ein");
		}
		esr.printBESR(bank, adressat, rn.getMandant(), sum.getCentsAsString(), tc);
		tc.getPlugin().print(Hub.localCfg.get("Drucker/A4ESR/Name", null), null, false);
		return ret;
	}
}
