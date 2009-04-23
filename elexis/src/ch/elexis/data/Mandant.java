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
 *    $Id: Mandant.java 5266 2009-04-23 13:41:31Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;


/**
 * Ein Mandant ist ein Anwender (und damit eine Person und damit ein Kontakt), der zusätzlich eigene
 * Abrechnungen führt.
 * 
 * @author gerry
 * 
 */
public class Mandant extends Anwender {
	
	static {
		addMapping("KONTAKT", "ExtInfo", "istMandant", "Label=Bezeichnung3");
	}
	
	public boolean isValid(){
		if (get("istMandant").equals("1")) {
			return super.isValid();
		}
		return false;
	}
	
	
	public Rechnungssteller getRechnungssteller(){
		Rechnungssteller ret = Rechnungssteller.load(getInfoString("Rechnungssteller"));
		return ret.isValid() ? ret : Rechnungssteller.load(getId());
	}
	
	public void setRechnungssteller(Kontakt rs){
		setInfoElement("Rechnungssteller", rs.getId());
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
		String ism = ret.get("istMandant");
		if (ism != null && ism.equals("1")) {
			return ret;
		}
		return null;
	}
	
	public Mandant(String name, String pwd){
		super(name, pwd);
	}
	
	protected String getConstraint(){
		return "istMandant='1'";
	}
	
	@Override
	protected void setConstraint(){
		set("istMandant", "1");
		set("istAnwender", "1");
	}
	/**
	 * Initiale Rechte setzen. initializes only deprecated fields. Will probalbly soon be removed.
	 * 
	 * protected static void init(){
	 * Hub.acl.grant("Alle","ReadEAN","ReadKSK","ReadNIF","ReadAgendaLabel");
	 * Hub.acl.grant("Admin","WriteEAN","WriteKSK","WriteNIF","WriteAgendaLabel"); }
	 */
	
}
