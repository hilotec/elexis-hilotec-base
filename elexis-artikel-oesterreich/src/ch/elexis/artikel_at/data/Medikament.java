/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Medikament.java 4927 2009-01-10 21:26:30Z rgw_ch $
 *******************************************************************************/
package ch.elexis.artikel_at.data;

import java.util.Hashtable;

import ch.elexis.data.Artikel;
import ch.rgw.tools.StringTool;

public class Medikament extends Artikel {
	private static final String VERSION = "0.2.0";
	public static final String CODESYSTEMNAME = "Medikamente";
	public static final String[] RSIGNS =
		{
			"P1", "P5", "R1", "R2", "SG", "S1", "S5", "W1", "W2", "W6", "W7", "W8", "W9", "W10",
			"W11", "W12", "W13", "W14", "W15", "W16"
		};
	public static final String[] SSIGNS =
		{/* "Remb", */
			"Box", "AU", "B", "CH14", "D", "DS", "F", "GF", "F6J", "IND", "K", "KF14", "KF2", "L3",
			"L6", "L9", "L12", "NE", "PS", "R", "RE1", "RE2", "U"
		};
	
	static final String JOINTTABLE = "CH_ELEXIS_AUSTRIAMEDI_JOINT";
	
	static final String jointDB =
		"CREATE TABLE " + JOINTTABLE + "(" + "ID				VARCHAR(25) primary key,"
			+ "product			VARCHAR(25)," + "substance         VARCHAR(25)" + ");"
			+ "CREATE INDEX CHEMBJ1 ON " + JOINTTABLE + " (product);" + "CREATE INDEX CHEMBJ2 ON "
			+ JOINTTABLE + " (substance);" + "INSERT INTO " + JOINTTABLE
			+ " (ID,substance) VALUES('VERSION','" + VERSION + "');";
	
	public Medikament(String name, String typ, String subid){
		super(name, typ, subid);
		set("Klasse", getClass().getName());
	}
	
	@Override
	protected String getConstraint(){
		return "Typ='Vidal'";
	}
	
	protected void setConstraint(){
		set("Typ", "Vidal");
	}
	
	@Override
	public String getCodeSystemName(){
		return CODESYSTEMNAME;
	}
	
	@Override
	public String getText(){
		return getLabel();
	}
	
	@Override
	public String getLabel(){
		// String ret=getInternalName();
		// if(StringTool.isNothing(ret)){
		StringBuilder sb = new StringBuilder();
		sb.append(getInternalName()).append(" (").append(getExt("Quantity")).append(")");
		String ret = sb.toString();
		// }
		return ret;
	}
	
	public String getRemb(){
		String r = getExt("Remb");
		if (StringTool.isNothing(r)) {
			r = (String) ((Hashtable) getHashtable("ExtInfo").get("SSigns")).get("Remb");
			setExt("Remb", r);
		}
		return StringTool.isNothing(r) ? "1" : r;
	}
	
	@Override
	public String getCode(){
		return getPharmaCode();
	}
	
	public static Medikament load(String id){
		return new Medikament(id);
	}
	
	protected Medikament(){}
	
	protected Medikament(String id){
		super(id);
	}
	
	@Override
	public boolean isDragOK(){
		return true;
	}
	
}