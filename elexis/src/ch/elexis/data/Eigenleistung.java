/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: Eigenleistung.java 4739 2008-12-04 21:01:33Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class Eigenleistung extends VerrechenbarAdapter {
	
	static {
		addMapping("EIGENLEISTUNGEN", "Code", "Bezeichnung", "EK_Preis", "VK_Preis", "Zeit");
	}
	
	@Override
	protected String getTableName(){
		return "EIGENLEISTUNGEN";
	}
	
	@Override
	public String getCode(){
		return get("Code");
	}
	
	@Override
	public String getText(){
		return get("Bezeichnung");
	}
	
	public String[] getDisplayedFields(){
		return new String[] {
			"Code", "Bezeichnung"
		};
	}
	
	@Override
	public String getCodeSystemName(){
		return "Eigenleistung";
	}
	
	@Override
	public Money getKosten(final TimeTool dat){
		return new Money(checkZero(get("EK_Preis")));
	}
	
	public Money getPreis(final TimeTool dat, final Fall fall){
		return new Money(checkZero(get("VK_Preis")));
	}
	
	public Eigenleistung(final String code, final String name, final String ek, final String vk){
		create(null);
		set(new String[] {
			"Code", "Bezeichnung", "EK_Preis", "VK_Preis"
		}, code, name, ek, vk);
	}
	
	protected Eigenleistung(){}
	
	protected Eigenleistung(final String id){
		super(id);
	}
	
	public static Eigenleistung load(final String id){
		return new Eigenleistung(id);
	}
	
	@Override
	public boolean isDragOK(){
		return true;
	}
	
	@Override
	public String getCodeSystemCode(){
		return "999";
	}
	
	public int getTP(final TimeTool date, final Fall fall){
		return getPreis(date, fall).getCents();
	}
	
	public double getFactor(final TimeTool date, final Fall fall){
		return 1.0;
	}
	
}
