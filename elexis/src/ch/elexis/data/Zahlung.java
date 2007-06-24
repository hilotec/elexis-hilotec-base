/*******************************************************************************
 * Copyright (c) 2005-2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: Zahlung.java 1672 2007-01-26 21:19:37Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import ch.elexis.util.Money;
import ch.rgw.tools.TimeTool;

public class Zahlung extends PersistentObject {
	static{
		addMapping("ZAHLUNGEN","RechnungsID","Betragx100=Betrag","Datum=S:D:Datum","Bemerkung");
	}
	
	public Zahlung(Rechnung rn,Money Betrag, String text){
		create(null);
		String Datum=new TimeTool().toString(TimeTool.DATE_GER);
		set(new String[]{"RechnungsID","Betragx100","Datum","Bemerkung"},
				rn.getId(),Betrag.getCentsAsString(),Datum,text);
		new AccountTransaction(this);
		rn.addTrace(Rechnung.PAYMENT, Betrag.getAmountAsString());
	}
	
	
	@Override
	public boolean delete() {
		StringBuilder sb=new StringBuilder();
		sb.append("DELETE FROM KONTO WHERE ID=")
			.append(getWrappedId());
		j.exec(sb.toString());
		Rechnung rn=getRechnung();
		if(rn!=null){
			rn.addTrace(Rechnung.CORRECTION, "Zahlung gelöscht");
		}
		return super.delete();
	}

	public String getBemerkung(){
		return get("Bemerkung");
	}

	public Rechnung getRechnung(){
		return Rechnung.load(get("RechnungsID"));
	}
	public Money getBetrag(){
		return new Money(checkZero(get("Betragx100")));
	}
	public String getDatum(){
		return get("Datum");
	}
	public static Zahlung load(String id){
		return new Zahlung(id);
	}
	protected Zahlung() {/* leer */	}

	protected Zahlung(String id) {
		super(id);
	}

	@Override
	public String getLabel() {
		StringBuilder sb=new StringBuilder();
		sb.append(getDatum()).append(": ").append(getBetrag().getAmountAsString());
		return sb.toString();
	}

	@Override
	protected String getTableName() {
		return "ZAHLUNGEN";
	}

}
