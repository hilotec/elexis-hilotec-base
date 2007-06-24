/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: Eigenleistung.java 2219 2007-04-15 16:05:13Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import ch.elexis.util.Money;
import ch.rgw.tools.TimeTool;

public class Eigenleistung extends VerrechenbarAdapter {

	static{
		addMapping("EIGENLEISTUNGEN","Code","Bezeichnung","EK_Preis","VK_Preis","Zeit");
	}
	@Override
	protected String getTableName() {
		return "EIGENLEISTUNGEN";
	}

	public String getCode() {
		return get("Code");
	}

	public String getText() {
		return get("Bezeichnung");
	}
	public String[] getDisplayedFields(){
		return new String[]{"Code","Bezeichnung"};
	}
	public String getCodeSystemName() {
		return "Eigenleistung";
	}

	
	@Override
	public Money getKosten(TimeTool dat) {
		return new Money(checkZero(get("EK_Preis")));
	}

	public Money getPreis(TimeTool dat, String subgroup) {
		return new Money(checkZero(get("VK_Preis")));
	}


	public Eigenleistung(String code, String name, String ek, String vk){
		create(null);
		set(new String[]{"Code","Bezeichnung","EK_Preis","VK_Preis"},
				code,name,ek,vk);
	}
	protected Eigenleistung(){
	}
	protected Eigenleistung(String id){
		super(id);
	}
	public static Eigenleistung load(String id){
		return new Eigenleistung(id);
	}

	@Override
	public boolean isDragOK() {
		return true;
	}

	public String getCodeSystemCode() {
		return "999";
	}

	public int getTP(TimeTool date, String subgroup) {
		return getPreis(date,subgroup).getCents();
	}

	public double getFactor(TimeTool date, String subgroup) {
		return 1.0;
	}
	
}
