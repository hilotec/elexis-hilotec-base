/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: MiGelArtikel.java 5932 2010-01-14 22:30:04Z rgw_ch $
 *******************************************************************************/
package ch.elexis.artikel_ch.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.elexis.data.Artikel;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;

public class MiGelArtikel extends Artikel {
	static Pattern pattern = Pattern.compile("([a-z0-9A-Z])([A-Z][a-z])");
	
	public MiGelArtikel(String code, String text, String unit, Money price){
		create("MiGeL" + code); //$NON-NLS-1$
		String shortname = StringTool.getFirstLine(text, 120);
		Matcher matcher = pattern.matcher(shortname);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, matcher.group(1) + " " + matcher.group(2));
		}
		matcher.appendTail(sb);
		shortname = sb.toString();
		set(new String[] {
			"Name", "Typ", "SubID"}, new String[] { shortname, "MiGeL", code}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		setExt("FullText", text); //$NON-NLS-1$
		setExt("unit", unit == null ? "-" : unit); //$NON-NLS-1$ //$NON-NLS-2$
		set("VK_Preis", price.getCentsAsString()); //$NON-NLS-1$
	}
	
	@Override
	protected String getConstraint(){
		return "Typ='MiGeL'"; //$NON-NLS-1$
	}
	
	protected void setConstraint(){
		set("Typ", "MiGeL"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public String getLabel(){
		return getCode() + " " + get("Name"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public String getCode(){
		return checkNull(get("SubID")); //$NON-NLS-1$
	}
	
	@Override
	public String getCodeSystemName(){
		return "MiGeL"; //$NON-NLS-1$
	}
	
	public static MiGelArtikel load(String id){
		return new MiGelArtikel(id);
	}
	
	protected MiGelArtikel(String id){
		super(id);
	}
	
	protected MiGelArtikel(){}
	
	@Override
	public boolean isDragOK(){
		return true;
	}
	
}