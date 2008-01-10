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
 * $Id: Verrechnet.java 3512 2008-01-10 22:36:40Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.util.Hashtable;

import ch.elexis.Hub;
import ch.elexis.util.Log;
import ch.elexis.util.Money;
import ch.rgw.tools.TimeTool;

/**
 * Ein Verrechnet ist ein realisiertes Verrechenbar. Ein Verrechenbar wird durch die Zuordnung zu einer
 * Konsultation zu einem Verrechnet. Der Preis eines Verrechnet ist zunächst Taxpunkwert(TP) mal Scale (Immer in
 * der kleinsten Währungseinheit, also Rappen oder ggf. cent). Der effektive Preis kann aber geändert werden
 * (Rabatt etc.)   
 * @author gerry
 *
 */
public class Verrechnet extends PersistentObject {

	static{
		addMapping("LEISTUNGEN","Konsultation=Behandlung","Leistg_txt","Leistg_code",
				"Klasse","Zahl","EK_Kosten","VK_TP","VK_Scale","VK_Preis",
				"Scale","ExtInfo=Detail");
	}
	
	public Verrechnet(final IVerrechenbar iv, final Konsultation kons, final int zahl){
		create(null);
		TimeTool dat=new TimeTool(kons.getDatum());
		Fall fall=kons.getFall();
		int tp=iv.getTP(dat,fall);
		double factor=iv.getFactor(dat,fall);
		long preis=Math.round(tp*factor);
		set(new String[]{"Konsultation","Leistg_txt","Leistg_code","Klasse","Zahl","EK_Kosten",
				"VK_TP","VK_Scale","VK_Preis","Scale"},
				new String[]{kons.getId(),iv.getText(),iv.getId(),iv.getClass().getName(),Integer.toString(zahl),
				iv.getKosten(dat).getCentsAsString(),Integer.toString(tp),Double.toString(factor),
				Long.toString(preis),"100"});
		
	}
	public String getText(){
		return checkNull(get("Leistg_txt"));
	}
	
	public void setText(String text){
		set("Leistg_txt",text);
	}
	/** Den effektiven Preis setzen (braucht nicht TP*Scale zu sein */
	/*
	public void setPreisInRappen(int p){
		set("VK_Preis",Integer.toString(p));
	}
	*/
	public void setPreis(final Money m){
		set("VK_Preis",m.getCentsAsString());
	}
	/** Den effektiv verrechneten Preis holen (braucht nicht TP*Scale zu sein */
	/*
	public int getEffPreisInRappen(){
		return checkZero(get("VK_Preis"));
	}
	*/
	public Money getEffPreis(){
		return new Money(checkZero(get("VK_Preis")));
	}
	/** Den Standardpreis holen (Ist immer TP*Scale, auf ganze Rappen gerundet) */
	/*
	public int getStandardPreisInRappen(){
		double d=checkZeroDouble(get("VK_Scale"));
		int t=checkZero(get("VK_TP"));
		return (int)Math.round(d*t);
	}
	*/
	public Money getStandardPreis(){
		IVerrechenbar v=getVerrechenbar();
		Konsultation k=getKons();
		Fall fall=k.getFall();
		TimeTool date=new TimeTool(k.getDatum());
		double factor=v.getFactor(date, fall);
		int tp=v.getTP(date, fall);
		return new Money((int)Math.round(factor*tp));
	}
	/** Bequemlichkeits-Shortcut für Standardbetrag setzen */
	public void setStandardPreis(){
		IVerrechenbar v=getVerrechenbar();
		Konsultation k=getKons();
		Fall fall=k.getFall();
		TimeTool date=new TimeTool(k.getDatum());
		double factor=v.getFactor(date, fall);
		int tp=v.getTP(date, fall);
		long preis=Math.round(tp*factor);
		set(new String[]{"VK_Scale","VK_TP","VK_Preis"},
				Double.toString(factor),Integer.toString(tp),Long.toString(preis));
	}
	
	public Konsultation getKons(){
		return Konsultation.load(get("Konsultation"));
	}
	/** Wie oft wurde die Leistung bei derselben Kons. verrechnet? */
	public int getZahl(){
		return checkZero(get("Zahl"));
	}
	public void setZahl(final int z){
		set("Zahl",Integer.toString(z));
	}
	public String getCode(){
		IVerrechenbar verrechenbar=getVerrechenbar();
		if(verrechenbar==null){
			return "?";
		}else{
			return verrechenbar.getCode();
		}
	}
	
	public void setExtInfo(final String key, final String value){
		Hashtable ext=getHashtable("Detail");
		ext.put(key,value);
		setHashtable("Detail", ext);
		
	}
	
	public String getExtInfo(final String key){
		Hashtable ext=getHashtable("Detail");
		return (String)ext.get(key);
	}
	/** Frage, ob dieses Verrechnet aus dem IVerrechenbar tmpl entstanden ist */
	public boolean isInstance(final IVerrechenbar tmpl){
		String[] res=new String[2];
		get(new String[]{"Klasse","Leistg_code"},res);
		if(tmpl.getClass().getName().equals(res[0])){
			if(tmpl.getId().equals(res[1])){
				return true;
			}
		}
		return false;
	}
	public IVerrechenbar getVerrechenbar(){
		String[] res=new String[2];
		get(new String[]{"Klasse","Leistg_code"},res);
		 try{
             return(IVerrechenbar)Hub.poFactory.createFromString(res[0]+"::"+res[1]);
         }catch(Exception ex){
             log.log("Fehlerhafter Leistungscode "+getLabel(),Log.ERRORS);
         }
         return null;
	}
	@Override
	public String getLabel() {
		return checkNull(get("Leistg_txt"));
	}

	@Override
	protected String getTableName() {
		return "LEISTUNGEN";
	}

	public static Verrechnet load(final String id){
		return new Verrechnet(id);
	}
	protected Verrechnet(){}
	protected Verrechnet(final String id){
		super(id);
	}
}
