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
 * $Id: RechnungsDrucker.java 2861 2007-07-21 18:39:33Z rgw_ch $
 *******************************************************************************/

package ch.elexis.privatrechnung.rechnung;

import java.util.Collection;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ch.elexis.data.Fall;
import ch.elexis.data.Rechnung;
import ch.elexis.util.IRnOutputter;
import ch.elexis.util.Result;

public class RechnungsDrucker implements IRnOutputter {

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

	public Control createSettingsControl(Composite parent) {
		// TODO Auto-generated method stub
		return null;
	}

	public Result<Rechnung> doOutput(TYPE type, Collection<Rechnung> rnn) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDescription() {
		return "Privatrechnung auf Drucker";
	}

}
