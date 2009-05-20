/*******************************************************************************
 * Copyright (c) 2008-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: MahnlaufCommand.java 5316 2009-05-20 11:34:51Z rgw_ch $
 *******************************************************************************/
package ch.elexis.commands;

import java.text.ParseException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import ch.elexis.Hub;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.preferences.PreferenceConstants;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class MahnlaufCommand extends AbstractHandler {
	public final static String ID = "bill.reminder";
	
	public Object execute(ExecutionEvent arg0) throws ExecutionException{
		Query<Rechnung> qbe = new Query<Rechnung>(Rechnung.class);
		qbe.add("RnStatus", "=", Integer.toString(RnStatus.OFFEN_UND_GEDRUCKT));
		qbe.add("MandantID", "=", Hub.actMandant.getId());
		TimeTool tt = new TimeTool();
		// Rechnung zu 1. Mahnung
		int days = Hub.mandantCfg.get(PreferenceConstants.RNN_DAYSUNTIL1ST, 30);
		Money betrag = new Money();
		try {
			betrag = new Money(Hub.mandantCfg.get(PreferenceConstants.RNN_AMOUNT1ST, "0.00"));
		} catch (ParseException ex) {
			ExHandler.handle(ex);
			
		}
		tt.addHours(days * 24 * -1);
		qbe.add("StatusDatum", "<", tt.toString(TimeTool.DATE_COMPACT));
		List<Rechnung> list = qbe.execute();
		for (Rechnung rn : list) {
			rn.setStatus(RnStatus.MAHNUNG_1);
			if (!betrag.isZero()) {
				rn.addZahlung(new Money(betrag).multiply(-1.0), "Mahngebühr 1. Mahnung",null);
			}
		}
		// 1. Mahnung zu 2. Mahnung
		qbe.clear();
		qbe.add("RnStatus", "=", Integer.toString(RnStatus.MAHNUNG_1_GEDRUCKT));
		qbe.add("MandantID", "=", Hub.actMandant.getId());
		tt = new TimeTool();
		days = Hub.mandantCfg.get(PreferenceConstants.RNN_DAYSUNTIL2ND, 10);
		try {
			betrag = new Money(Hub.mandantCfg.get(PreferenceConstants.RNN_AMOUNT2ND, "0.00"));
		} catch (ParseException ex) {
			ExHandler.handle(ex);
			betrag = new Money();
		}
		tt.addHours(days * 24 * -1);
		qbe.add("StatusDatum", "<", tt.toString(TimeTool.DATE_COMPACT));
		list = qbe.execute();
		for (Rechnung rn : list) {
			rn.setStatus(RnStatus.MAHNUNG_2);
			if (!betrag.isZero()) {
				rn.addZahlung(new Money(betrag).multiply(-1.0), "Mahngebühr 2. Mahnung",null);
			}
		}
		// 2. Mahnung zu 3. Mahnung
		qbe.clear();
		qbe.add("RnStatus", "=", Integer.toString(RnStatus.MAHNUNG_2_GEDRUCKT));
		qbe.add("MandantID", "=", Hub.actMandant.getId());
		tt = new TimeTool();
		days = Hub.mandantCfg.get(PreferenceConstants.RNN_DAYSUNTIL3RD, 10);
		try {
			betrag = new Money(Hub.mandantCfg.get(PreferenceConstants.RNN_AMOUNT3RD, "0.00"));
		} catch (ParseException ex) {
			ExHandler.handle(ex);
			betrag = new Money();
		}
		tt.addHours(days * 24 * -1);
		qbe.add("StatusDatum", "<", tt.toString(TimeTool.DATE_COMPACT));
		list = qbe.execute();
		for (Rechnung rn : list) {
			rn.setStatus(RnStatus.MAHNUNG_3);
			if (!betrag.isZero()) {
				rn.addZahlung(new Money(betrag).multiply(-1.0), "Mahngebühr 3. Mahnung",null);
			}
		}
		
		return null;
	}
	
}
