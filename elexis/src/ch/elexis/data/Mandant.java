/*******************************************************************************
 * Copyright (c) 2005-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: Mandant.java 2762 2007-07-08 20:35:24Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import ch.elexis.Hub;


/**
 * Ein Mandant ist ein Anwender (und damit eine Person und damit ein Kontakt),
 * der zusätzlich eigene Abrechnungen führt.  
 * @author gerry
 *
 */
public class Mandant extends Anwender {
	
	static{
		addMapping("KONTAKT","ExtInfo","istMandant","Label=Bezeichnung3");
	}
		
  
	public boolean isValid(){
		if(get("istMandant").equals("1")){
			return super.isValid();
		}
		return false;
	}
	
    /** EAN lesen 
     * @Deprecated belongs to Arzttarife-Schweiz
     * */
	/*
	@Deprecated
    public String getEan() {
         return checkNull((String)getInfoElement("EAN"));
    }
    */
    /** EAN setzen. Es erfolgt keine Prüfung auf Plausibilit�t 
     * @Deprecated belongs to Arzttarife-Schweiz
     * */
	/*
    @Deprecated
    public void setEan(String ean) {
        setInfoElement("EAN",ean);
    }
    */
    /** KSK-Nr lesen 
     * @Deprecated belongs to Arzttarife-Schweiz
     * */
	
    @Deprecated
    public String getKsk() {
        return checkNull((String)getInfoElement("KSK"));
    }
    
    /** KSK-Nr. setzen. Es erfolgt keine Prüfung auf Plausibilit�t 
     * @Deprecated belongs to Arzttarife-Schweiz
     * */
	/*
    @Deprecated
    public void setKsk(String ksk) {
        setInfoElement("KSK",ksk);
    }
    */
    /** NIF lesen 
     * @Deprecated belongs to Arzttarife-Schweiz
     * */
    @Deprecated
    public String getNif() {
        return checkNull((String)getInfoElement("NIF"));
    }
    /** NIF setzen. Es erfolgt keine Prüfung auf Plausibilit�t 
     * @Deprecated belongs to Arzttarife-Schweiz
     * */
    @Deprecated
    public void setNif(String nif) {
        setInfoElement("NIF",nif);
    }
    /**
     * @Deprecated belongs to Arzttarife-Schweiz
     */
    @Deprecated
    public String getQuantDignitaet(){
    	return checkNull((String)getInfoElement("QuantDignität"));
    }
    /** @Deprecated belongs to Arzttarife-Schweiz */
    @Deprecated
    public String getQualiDignitaet(){
    	return checkNull((String)getInfoElement("QualiDignität"));
    }
    /** @Deprecated belongs to Arzttarife-Schweiz */
    @Deprecated
    public String getSparte(){
    	return checkNull((String)getInfoElement("Sparte"));
    }

	protected Mandant(String id){
		super(id);
	}
    protected Mandant(){/* leer */}
    public static Mandant load(String id){
    	Mandant ret=new Mandant(id);
    	String ism=ret.get("istMandant");
    	if(ism!=null && ism.equals("1")){
    		return ret;
    	}
        return null;
    }
    
    public Mandant(String name, String pwd){
    	super(name,pwd);
    }

    protected String getConstraint(){
		return "istMandant='1'";
	}
	@Override
	protected void setConstraint(){
		set("istMandant","1");
		set("istAnwender","1");
	}
	    /**
     * Initiale Rechte setzen.
     * initializes only deprecated fields. Will probalbly soon be removed. 
     */
    protected static void init(){
        Hub.acl.grant("Alle","ReadEAN","ReadKSK","ReadNIF","ReadAgendaLabel");
        Hub.acl.grant("Admin","WriteEAN","WriteKSK","WriteNIF","WriteAgendaLabel");
    }
    
    
}
