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
 * $Id: RechnungsDrucker.java 2862 2007-07-21 19:32:41Z rgw_ch $
 *******************************************************************************/

package ch.elexis.privatrechnung.rechnung;

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.Hub;
import ch.elexis.data.Fall;
import ch.elexis.data.Rechnung;
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
	public boolean canBill(Fall fall) {
		return true;
	}

	/**
	 * We never storno
	 */
	public boolean canStorno(Rechnung rn) {
		return false;
	}

	/**
	 * Create the Control that will be presented to the user before
	 * selecting the bill output target.
	 * Here we simply chose a template to use for the bill
	 */
	public Control createSettingsControl(Composite parent) {
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
	public Result<Rechnung> doOutput(TYPE type, Collection<Rechnung> rnn) {
		String template=tVorlage.getText();
		Hub.globalCfg.set(settings, template);
		Result<Rechnung> ret=new Result<Rechnung>(Log.ERRORS,99,"Not yet implemented",null,true);
		for(Rechnung rn:rnn){
			// TODO print
		}
		if(!ret.isOK()){
			ret.display("Fehler beim Rechnungsdruck");
		}
		return ret;
	}

	public String getDescription() {
		return "Privatrechnung auf Drucker";
	}

}
