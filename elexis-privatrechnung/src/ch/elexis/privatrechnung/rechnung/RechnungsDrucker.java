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
 * $Id: RechnungsDrucker.java 2972 2007-08-08 15:17:09Z rgw_ch $
 *******************************************************************************/

package ch.elexis.privatrechnung.rechnung;

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.data.Brief;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Rechnung;
import ch.elexis.text.TextContainer;
import ch.elexis.util.IRnOutputter;
import ch.elexis.util.Log;
import ch.elexis.util.Result;
import ch.elexis.util.SWTHelper;

public class RechnungsDrucker implements IRnOutputter {
	private static final String settings="privatrechnung/vorlage";
	Text tVorlage;
	
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
		tVorlage=new Text(ret,SWT.BORDER);
		tVorlage.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tVorlage.setText(Hub.globalCfg.get(settings, ""));
		return ret;

	}

	/**
	 * Print the bill(s)
	 */
	public Result<Rechnung> doOutput(final TYPE type, final Collection<Rechnung> rnn) {
		String template=tVorlage.getText();
		Hub.globalCfg.set(settings, template);
		Result<Rechnung> ret=new Result<Rechnung>(Log.ERRORS,99,"Not yet implemented",null,true);
		String printer=Hub.localCfg.get("Drucker/A4ESR/Name",null);
		for(Rechnung rn:rnn){
			Fall fall=rn.getFall();
			Kontakt adressat=fall.getRequiredContact("Rechnungsempfänger");
			TextContainer tc=new TextContainer(Desk.getTopShell());
			tc.createFromTemplateName(null, tVorlage.getText(), Brief.RECHNUNG, adressat,rn.getNr());
			tc.getPlugin().print(printer, null, true);
		}
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
}
