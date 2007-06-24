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
 *  $Id: AccountTransaction.java 1733 2007-02-05 14:12:20Z danlutz $
 *******************************************************************************/
package ch.elexis.data;

import ch.elexis.util.Money;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class AccountTransaction extends PersistentObject {
	private static final String TABLENAME="KONTO";
	
	static{
		addMapping(TABLENAME,"PatientID","ZahlungsID","RechnungsID","Betrag",
				"Datum=S:D:Datum","Bemerkung");
	}
	
	public AccountTransaction(Patient pat, Rechnung r, Money betrag, String date,
			String bemerkung){
		create(null);
		if(date==null){
			date=new TimeTool().toString(TimeTool.DATE_GER);
		}
		set(new String[]{"PatientID","Betrag","Datum","Bemerkung"},
				pat.getId(),betrag.getCentsAsString(),date,bemerkung);
		if(r!=null){
			set("RechnungsID",r.getId());
		}
	}
	public AccountTransaction(Zahlung z){
		create(null);
		Rechnung r=z.getRechnung();
		Patient p=r.getFall().getPatient();
		set(new String[]{"PatientID","Betrag","Datum","Bemerkung","RechnungsID","ZahlungsID"},
				p.getId(),z.getBetrag().getCentsAsString(),z.getDatum(),z.getBemerkung(),r.getId(),z.getId());
	}
	
	public Money getAmount(){
		try{
			return new Money(checkZero(get("Betrag")));
		}catch(Exception ex){
			ExHandler.handle(ex);
			return new Money();
		}
	}
	public String getRemark(){
		return checkNull(get("Bemerkung"));
	}
	public Patient getPatient(){
		return Patient.load(get("PatientID"));
	}
	
	public Rechnung getRechnung() {
		return Rechnung.load(get("RechnungsID"));
	}
	
	public Zahlung getZahlung(){
		String zi=get("ZahlungsID");
		if(StringTool.isNothing(zi)){
			return null;
		}
		return Zahlung.load(zi);
	}
	
	@Override
	public boolean delete() {
		Zahlung z=getZahlung();
		if(z!=null){
			z.delete();
		}
		return super.delete();
	}
	@Override
	public String getLabel() {
		StringBuilder sb=new StringBuilder();
		sb.append(get("Datum")).append(" ").append(get("Betrag")).append(" ")
			.append(get("Bemerkung"));
		return sb.toString();
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}
	public static AccountTransaction load(String id){
		return new AccountTransaction(id);
	}
	protected AccountTransaction(String id){
		super(id);
	}
	protected AccountTransaction(){}
	

}
