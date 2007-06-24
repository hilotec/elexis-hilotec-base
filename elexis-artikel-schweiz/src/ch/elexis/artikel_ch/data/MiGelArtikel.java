/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: MiGelArtikel.java 1742 2007-02-06 20:48:17Z rgw_ch $
 *******************************************************************************/
package ch.elexis.artikel_ch.data;

import ch.elexis.data.Artikel;
import ch.elexis.util.Money;
import ch.rgw.tools.StringTool;

public class MiGelArtikel extends Artikel{
	public MiGelArtikel(String code, String text, String unit, Money price){
		create("MiGeL"+code);
		String shortname=StringTool.getFirstLine(text,80);
		set(new String[]{"Name","Typ","SubID"},new String[]{shortname,"MiGeL",code});
		setExt("FullText",text);
		setExt("unit",unit==null? "-" : unit);
		set("VK_Preis",price.getCentsAsString());
	}
	@Override
	protected String getConstraint() {
		return "Typ='MiGeL'";
	}
	protected void setConstraint(){
	    set("Typ","MiGeL");
	}
	
	@Override
	public String getLabel() {
		return getCode()+" "+get("Name");
	}
	@Override
	public String getCode() {
		return get("SubID");
	}
	@Override
	public String getCodeSystemName() {
			return "MiGeL";
	}
	public static MiGelArtikel load(String id){
		return new MiGelArtikel(id);
	}
	protected MiGelArtikel(String id){
		super(id);
	}
	protected MiGelArtikel(){}
	
	@Override
	public boolean isDragOK() {
		return true;
	}
	
}