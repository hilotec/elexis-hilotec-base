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
 * $Id: RechnungsDrucker.java 3287 2007-10-26 04:39:23Z rgw_ch $
 *******************************************************************************/

package ch.elexis.privatrechnung.rechnung;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jdom.Document;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.data.Brief;
import ch.elexis.data.Fall;
import ch.elexis.data.IVerrechenbar;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Rechnung;
import ch.elexis.data.Verrechnet;
import ch.elexis.text.ITextPlugin;
import ch.elexis.text.TextContainer;
import ch.elexis.util.IRnOutputter;
import ch.elexis.util.Log;
import ch.elexis.util.Money;
import ch.elexis.util.Result;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

public class RechnungsDrucker implements IRnOutputter {
	private static final String settings="privatrechnung/vorlage";
	String template;
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
	 * Here we simply chose a template to use for the bill
	 */
	public Control createSettingsControl(final Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout());
		new Label(ret,SWT.NONE).setText("Formatvorlage für Rechnung");
		final Text tVorlage=new Text(ret,SWT.BORDER);
		tVorlage.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tVorlage.setText(Hub.globalCfg.get(settings, ""));
		tVorlage.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				template=tVorlage.getText();
				
			}});
		return ret;

	}

	/**
	 * Print the bill(s)
	 */
	public Result<Rechnung> doOutput(final TYPE type, final Collection<Rechnung> rnn) {
		Hub.globalCfg.set(settings, template);
		Result<Rechnung> ret=new Result<Rechnung>(); //=new Result<Rechnung>(Log.ERRORS,99,"Not yet implemented",null,true);
		Dialog dlg=new Dialog(Desk.getTopShell()){

			@Override
			protected Control createDialogArea(Composite parent) {
				tc=new TextContainer(parent.getShell());
				Control ret= tc.getPlugin().createContainer(parent, new ITextPlugin.ICallback(){

					public void save() {
						// TODO Auto-generated method stub
						
					}

					public boolean saveAs() {
						// TODO Auto-generated method stub
						return false;
					}});

				return ret;

			}
			
		};
		dlg.setBlockOnOpen(false);
		dlg.open();
		String printer=Hub.localCfg.get("Drucker/A4ESR/Name",null);
		
		for(Rechnung rn:rnn){
			Fall fall=rn.getFall();
			Kontakt adressat=fall.getRequiredContact("Rechnungsempfänger");
			tc.createFromTemplateName(null, template, Brief.RECHNUNG, adressat,rn.getNr());
			ret.add(doPrint(rn,tc));
			tc.getPlugin().print(printer, null, true);
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

	public boolean printESR(){
		//ESR esr=new ESR();
		return false;
	}
	
	/**
	 * print a bill into a text container
	 * @param rn
	 * @param tc
	 * @return
	 */
	public Result<Rechnung> doPrint(Rechnung rn, TextContainer tc){
		Result<Rechnung> ret=new Result<Rechnung>();
		List<Konsultation> kons=rn.getKonsultationen();
		Collections.sort(kons, new Comparator<Konsultation>(){
			TimeTool t0=new TimeTool();
			TimeTool t1=new TimeTool();
			public int compare(Konsultation arg0, Konsultation arg1) {
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
				tc.getPlugin().insertText(pos, vv.getText()+"\n", SWT.LEFT);
				sum.addMoney(vv.getEffPreis());
			}
		}
		String toPrinter=Hub.localCfg.get("Drucker/A4ESR/Name",null);
		tc.getPlugin().print(toPrinter, null, false);
		return ret;
	}
}
