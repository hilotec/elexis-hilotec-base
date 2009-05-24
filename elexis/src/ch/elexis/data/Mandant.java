/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: Mandant.java 5317 2009-05-24 15:00:37Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;


/**
 * Ein Mandant ist ein Anwender (und damit eine Person und damit ein Kontakt), der zusätzlich eigene
 * Abrechnungen führt.
 * 
 * @author gerry
 * 
 */
public class Mandant extends Anwender {
	
	public static final String BILLER = "Rechnungssteller";

	static {
		addMapping(Kontakt.TABLENAME, EXTINFO, IS_MANDATOR, "Label=Bezeichnung3");
	}
	
	public boolean isValid(){
		if (get(IS_MANDATOR).equals(StringTool.one)) {
			return super.isValid();
		}
		return false;
	}
	
	
	public Rechnungssteller getRechnungssteller(){
		Rechnungssteller ret = Rechnungssteller.load(getInfoString(BILLER));
		return ret.isValid() ? ret : Rechnungssteller.load(getId());
	}
	
	public void setRechnungssteller(Kontakt rs){
		setInfoElement(BILLER, rs.getId());
	}
	
	protected Mandant(String id){
		super(id);
	}
	
	public Mandant(final String Name, final String Vorname, final String Geburtsdatum,
		final String s){
		super(Name, Vorname, Geburtsdatum, s);
	}
	
	protected Mandant(){/* leer */}
	
	public static Mandant load(String id){
		Mandant ret = new Mandant(id);
		String ism = ret.get(IS_MANDATOR);
		if (ism != null && ism.equals(StringTool.one)) {
			return ret;
		}
		return null;
	}
	
	public Mandant(String name, String pwd){
		super(name, pwd);
	}
	
	protected String getConstraint(){
		return new StringBuilder(IS_MANDATOR)
		.append(Query.EQUALS)
		.append(JdbcLink.wrap(StringTool.one))
		.toString();
		
	}
	
	@Override
	protected void setConstraint(){
		set(new String[]{IS_MANDATOR,IS_USER},new String[]{StringTool.one,StringTool.one});
	}
	
	
}
