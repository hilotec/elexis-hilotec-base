/*******************************************************************************
 * Copyright (c) 2008, G. Weirich
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: ZahlungsJournal.java 1051 2008-12-21 16:10:18Z  $
 *******************************************************************************/
package ch.elexis.buchhaltung.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.data.AccountTransaction;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;
import ch.unibe.iam.scg.archie.model.AbstractTimeSeries;

public class ZahlungsJournal extends AbstractTimeSeries {
	private static final String NAME = "Zahlungsjournal";
	
	public ZahlungsJournal(){
		super(NAME);
	}
	
	@Override
	protected IStatus createContent(IProgressMonitor monitor){
		int total = 10000000;
		Query<AccountTransaction> qbe = new Query<AccountTransaction>(AccountTransaction.class);
		TimeTool ttStart = new TimeTool(this.getStartDate().getTimeInMillis());
		TimeTool ttEnd = new TimeTool(this.getEndDate().getTimeInMillis());
		qbe.add("Datum", ">=", ttStart.toString(TimeTool.DATE_COMPACT));
		qbe.add("Datum", "<=", ttEnd.toString(TimeTool.DATE_COMPACT));
		monitor.beginTask(NAME, total);
		monitor.subTask("Datenbankabfrage");
		List<AccountTransaction> transactions = qbe.execute();
		int sum = transactions.size();
		int step = total / sum;
		monitor.worked(20 * step);
		final ArrayList<Comparable<?>[]> result = new ArrayList<Comparable<?>[]>();
		for (AccountTransaction at : transactions) {
			Patient pat = at.getPatient();
			Money amount = at.getAmount();
			if ((amount == null) || (amount.isNegative())) {
				continue;
			}
			String remark = at.getRemark();
			if (remark.toLowerCase().contains("storno")) {
				continue;
			}
			if (pat != null) {
				Comparable<?>[] row = new Comparable<?>[this.dataSet.getHeadings().size()];
				row[0] = Integer.parseInt(pat.get("PatientNr"));
				row[1] = at.getDate();
				row[2] = at.getAmount();
				row[4] = at.getRemark();
				Rechnung rn = at.getRechnung();
				if (rn != null) {
					Money rnAmount = rn.getBetrag();
					if (rnAmount.isMoreThan(amount)) {
						row[3] = "TZ";
					} else {
						row[3] = "ZA";
					}
					
				} else {
					row[3] = "AD";
				}
				
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				result.add(row);
			}
			monitor.worked(step);
		}
		
		// Set content.
		this.dataSet.setContent(result);
		
		// Job finished successfully
		monitor.done();
		
		return Status.OK_STATUS;
	}
	
	@Override
	protected List<String> createHeadings(){
		ArrayList<String> ret = new ArrayList<String>();
		ret.add("Patient-Nr");
		ret.add("Datum");
		ret.add("Betrag");
		ret.add("Typ");
		ret.add("Text");
		return ret;
	}
	
	@Override
	public String getDescription(){
		return NAME;
	}
	
}
