/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: AccountTransaction.java 5317 2009-05-24 15:00:37Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class AccountTransaction extends PersistentObject {
	public static final String DATE = "Datum"; //$NON-NLS-1$
	public static final String REMARK = "Bemerkung"; //$NON-NLS-1$
	private static final String DATE_FIELD = "Datum=S:D:Datum"; //$NON-NLS-1$
	public static final String AMOUNT = "Betrag"; //$NON-NLS-1$
	public static final String BILL_ID = "RechnungsID"; //$NON-NLS-1$
	public static final String PAYMENT_ID = "ZahlungsID"; //$NON-NLS-1$
	public static final String PATIENT_ID = "PatientID"; //$NON-NLS-1$
	private static final String TABLENAME = "KONTO"; //$NON-NLS-1$

	static {
		addMapping(TABLENAME, PATIENT_ID, PAYMENT_ID, BILL_ID, AMOUNT,
				DATE_FIELD, REMARK);
	}

	public AccountTransaction(Patient pat, Rechnung r, Money betrag,
			String date, String bemerkung) {
		create(null);
		if (date == null) {
			date = new TimeTool().toString(TimeTool.DATE_GER);
		}
		set(new String[] { PATIENT_ID, AMOUNT, DATE, REMARK }, pat.getId(),
				betrag.getCentsAsString(), date, bemerkung);
		if (r != null) {
			set(BILL_ID, r.getId());
		}
	}

	public AccountTransaction(Zahlung z) {
		create(null);
		Rechnung r = z.getRechnung();
		Patient p = r.getFall().getPatient();
		set(new String[] { PATIENT_ID, AMOUNT, DATE, REMARK, BILL_ID,
				PAYMENT_ID }, p.getId(), z.getBetrag().getCentsAsString(), z
				.getDatum(), z.getBemerkung(), r.getId(), z.getId());
	}

	public String getDate() {
		return get(DATE);
	}

	public Money getAmount() {
		try {
			return new Money(checkZero(get(AMOUNT)));
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return new Money();
		}
	}

	public String getRemark() {
		return checkNull(get(REMARK));
	}

	public Patient getPatient() {
		return Patient.load(get(PATIENT_ID));
	}

	public Rechnung getRechnung() {
		return Rechnung.load(get(BILL_ID));
	}

	public Zahlung getZahlung() {
		String zi = get(PAYMENT_ID);
		if (StringTool.isNothing(zi)) {
			return null;
		}
		return Zahlung.load(zi);
	}

	@Override
	public boolean delete() {
		Zahlung z = getZahlung();
		if (z != null) {
			z.delete();
		}
		return super.delete();
	}

	@Override
	public String getLabel() {
		StringBuilder sb = new StringBuilder();
		sb.append(get(DATE)).append(StringTool.space).append(get(AMOUNT))
				.append(StringTool.space).append(get(REMARK));
		return sb.toString();
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}

	public static AccountTransaction load(String id) {
		return new AccountTransaction(id);
	}

	protected AccountTransaction(String id) {
		super(id);
	}

	protected AccountTransaction() {
	}

}
