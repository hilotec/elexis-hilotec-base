/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: Artikel.java 3851 2008-04-30 13:40:34Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import ch.elexis.Hub;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.Log;
import ch.elexis.util.Money;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Ein Artikel ist ein Objekt, das im Lager vorhanden ist oder sein sollte oder einem
 * Patienten verordnet werden kann
 */
public class Artikel extends VerrechenbarAdapter{
	public static final String SUB_ID = "SubID";
	public static final String ARTIKEL = "Artikel";
	private static final String LIEFERANT_ID = "LieferantID";
	public static final String PHARMACODE = "Pharmacode";
	public static final String EXT_INFO = "ExtInfo";
	private static final String ANBRUCH = "Anbruch";
	private static final String MINBESTAND = "Minbestand";
	private static final String MAXBESTAND = "Maxbestand";
	private static final String VERKAUFSEINHEIT = "Verkaufseinheit";
	private static final String VERPACKUNGSEINHEIT = "Verpackungseinheit";
	private static final String ISTBESTAND = "Istbestand";
	private static final String VK_PREIS = "VK_Preis";
	private static final String EK_PREIS = "EK_Preis";
	private static final String EIGENNAME = "Eigenname";
	private static final String TYP = "Typ";
	private static final String NAME = "Name";
	public static final String TABLENAME="ARTIKEL";
	
	@Override
	protected String getTableName() {
		return TABLENAME;
	}
	static{
		addMapping(TABLENAME,LIEFERANT_ID,NAME,MAXBESTAND,MINBESTAND,
				ISTBESTAND,EK_PREIS,VK_PREIS,TYP,EXT_INFO,"EAN",
				SUB_ID,"Eigenname=Name_intern","Codeclass","Klasse");
	}
    /**
     * This implementation of PersistentObject#load is special in that it tries to load
     * the actual appropriate subclass
     */
	public static Artikel load(final String id){
		if(id==null){
			return null;
		}
	   	Artikel ret=new Artikel(id);
	   	if(!ret.exists()){
	   		return ret;
	   	}
	   	String clazz=ret.get("Klasse");
	   	if(!StringTool.isNothing(clazz)){
		   	 try{
	             ret= (Artikel)Hub.poFactory.createFromString(clazz+"::"+id);
		   	 }catch(Exception ex){
		            log.log("Fehlerhafter Leistungscode "+clazz+"::"+id,Log.ERRORS);
		     }
	   	}
        return ret;
	}
    /**
     * Einen neuen Artikel mit vorgegebenen Parametern erstellen
     * @param Name
     * @param Typ
     */
	public Artikel(final String Name,final String Typ){
		create(null);
		set(new String[]{NAME,TYP},new String[]{Name,Typ});
	}
	public Artikel(final String Name, final String Typ, final String subid){
		create(null);
		set(new String[]{NAME,TYP,SUB_ID},Name,Typ,subid);
	}
	@Override
	public String getLabel(){
		if(!exists()){
			return "("+getName()+")";
		}
		return getInternalName();
	}
	public String[] getDisplayedFields(){
		return new String[]{TYP,NAME};
	}
	/**
	 * Den internen Namen setzen. Dieser ist vom Anwender frei wählbar und erscheint
	 * in der Artikelauswahl und auf der Rechnung.
	 * @param nick Der "Spitzname"
	 */
	public void setInternalName(final String nick){
		set(EIGENNAME,nick);
	}
	/**
	 * Den internen Namen holen
	 * @return
	 */
	public String getInternalName(){
		String ret=get(EIGENNAME);
		if(StringTool.isNothing(ret)){
			ret=getName();
		}
		return ret;
	}
	/**
	 * Den offiziellen namen holen
	 * @return
	 */
	public String getName(){
		return checkNull(get(NAME));
	}
	/**
	 * Den "echten" Namen setzen. Dies ist der offizielle Name des Artikels, wie
	 * er beispielsweise in Katalogen aufgeführt ist. Dieser sollte normalerweise 
	 * nicht geändert werden.
	 * @param name der neue "echte" Name
	 */
	public void setName(final String name){
		set(NAME,name);
	}
	
	/** 
	 * Basis-Einkaufspreis in Rappen pro Einheit
	 * @return
	 */
	public Money getEKPreis(){
		try{
			return new Money(checkZero(get(EK_PREIS)));
		}catch(Throwable ex){
			Hub.log.log("Fehler beim Einlesen von EK für "+getLabel(),Log.ERRORS);
		}
		return new Money();

	}
	/**
	 * Basis-Verkaufspreis in Rappen pro Einheit
	 * @return
	 */
	public Money getVKPreis(){
		try{
			return new Money(checkZero(get(VK_PREIS)));
		}catch(Throwable ex){
			Hub.log.log("Fehler beim Einlesen von VK für "+getLabel(),Log.ERRORS);
		}
		return new Money();

	}
	
	public void setEKPreis(final Money preis){
		set(EK_PREIS,preis.getCentsAsString());
	}
	
	public void setVKPreis(final Money preis){
		set(VK_PREIS,preis.getCentsAsString());
	}
	
	public int getIstbestand(){
		try{
			return checkZero(get(ISTBESTAND));
		}catch(Throwable ex){
			Hub.log.log("Fehler beim Einlesen von istbestand für "+getLabel(),Log.ERRORS);
		}
		return 0;
	}
	
	public int getTotalCount(){
		int pack=getIstbestand();
		int VE=getPackungsGroesse();
		if(VE==0){
			return pack;
		}
		int AE=getAbgabeEinheit();
		if(AE<VE){
			return (pack*VE)+(getBruchteile()*AE);
		}
		return pack;
	}
	public int getPackungsGroesse(){
		return checkZero(getExt(VERPACKUNGSEINHEIT));
	}
	public int getAbgabeEinheit(){
		return checkZero(getExt(VERKAUFSEINHEIT));
	}
	public int getMaxbestand(){
		try{
			return checkZero(get(MAXBESTAND));
		}catch(Throwable ex){
			Hub.log.log("Fehler beim Einlesen von Maxbestand für "+getLabel(),Log.ERRORS);
		}
		return 0;
	}
	public int getMinbestand(){
		try{
			return checkZero(get(MINBESTAND));
		}catch(Throwable ex){
			Hub.log.log("Fehler beim Einlesen von Minbestand für "+getLabel(),Log.ERRORS);
		}
		return 0;
	}
	public void setMaxbestand(final int s){
		String sl=checkLimit(s);
		if(sl!=null){
			set(MAXBESTAND,sl);
		}
	}
	public void setMinbestand(final int s){
		String sl=checkLimit(s);
		if(sl!=null){
			set(MINBESTAND,sl);
		}
	}
	public void setIstbestand(final int s){
		String sl=null;
		if(Hub.globalCfg.get(PreferenceConstants.INVENTORY_CHECK_ILLEGAL_VALUES, true)){
			sl=checkLimit(s);
		}else{
			sl=Integer.toString(s);
		}
		if( sl!=null){
			set(ISTBESTAND,sl);
		}
	}
	public int getBruchteile(){
		return checkZero(getExt(ANBRUCH));
	}
	
	private String checkLimit(final int s){
		String str=Integer.toString(s);
		if(s>-1 && s<1001){
			return str;
		}
		if(isLagerartikel()){
			SWTHelper.showError("Ungültiger Lagerbestand", "Der Lagerbestand ist auf "+str+". Bitte einen Wert zwischen 0 und 1000 eingeben.");
		}
		return null;
	}
	public boolean isLagerartikel() {
		if ((getMinbestand() > 0) || (getMaxbestand() > 0)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static List<Artikel> getLagerartikel() {
		Query<Artikel> qbe=new Query<Artikel>(Artikel.class);
		qbe.add(MINBESTAND,">","0");
		qbe.or();
		qbe.add(MAXBESTAND,">","0");
		qbe.orderBy(false, new String[] {NAME});
		List<Artikel> l=qbe.execute();
		return l==null ? new ArrayList<Artikel>(0) : l;
	}
	
	@SuppressWarnings("unchecked")
	public void einzelAbgabe(final int n){
		Hashtable<String,String> ext=getHashtable(EXT_INFO);
		int anbruch=checkZero(ext.get(ANBRUCH));
		int ve=checkZero(ext.get(VERKAUFSEINHEIT));
		int vk=checkZero(ext.get(VERPACKUNGSEINHEIT));
		if(vk==0){
			if(ve!=0){
				vk=ve;
				ext.put(VERKAUFSEINHEIT,Integer.toString(vk));
				setHashtable(EXT_INFO,ext);
			}
		}
		if(ve==0){
			if(vk!=0){
				ve=vk;
				ext.put(VERPACKUNGSEINHEIT,Integer.toString(ve));
				setHashtable(EXT_INFO,ext);
			}
		}
		int num=n*ve;
		if(vk==ve){
			setIstbestand(getIstbestand()-n);
		}else{
			int rest=anbruch-num;
			while(rest<0){
				rest=rest+vk;
				setIstbestand(getIstbestand()-1);
			}
			ext.put(ANBRUCH,Integer.toString(rest));
			setHashtable(EXT_INFO,ext);
		}
	}
	@SuppressWarnings("unchecked")
	public void einzelRuecknahme(final int n){
		Hashtable<String,String> ext=getHashtable(EXT_INFO);
		int anbruch=checkZero(ext.get(ANBRUCH));
		int ve=checkZero(ext.get(VERKAUFSEINHEIT));
		int vk=checkZero(ext.get(VERPACKUNGSEINHEIT));
		int num=n*ve;
		if(vk==ve){
			setIstbestand(getIstbestand()+n);
		}else{
			int rest=anbruch+num;
			while(rest>vk){
				rest=rest-vk;
				setIstbestand(getIstbestand()+1);
			}
			ext.put(ANBRUCH,Integer.toString(rest));
			setHashtable(EXT_INFO,ext);
		}
	}
	public String getEAN(){
		String ean=get("EAN");
		return ean;
	}
	public void setEAN(String ean){
		set("EAN",ean);
	}
	
	@SuppressWarnings("unchecked")
	public String getPharmaCode(){
		Hashtable ext=getHashtable(EXT_INFO);
		return checkNull((String)ext.get(PHARMACODE));
	}
	public Kontakt getLieferant(){
		return Kontakt.load(get(LIEFERANT_ID));
	}
	public void setLieferant(final Kontakt l){
		set(LIEFERANT_ID,l.getId());
	}
	@SuppressWarnings("unchecked")
	public int getVerpackungsEinheit(){
		Hashtable ext=getHashtable(EXT_INFO);
		return checkZero((String)ext.get(VERPACKUNGSEINHEIT));
	}
	@SuppressWarnings("unchecked")
	public int getVerkaufseinheit(){
		Hashtable ext=getHashtable(EXT_INFO);
		return checkZero((String)ext.get(VERKAUFSEINHEIT));
	}
	@SuppressWarnings("unchecked")
	public void setExt(final String name, final String value){
		Hashtable h=getHashtable(EXT_INFO);
		if(value==null){
			h.remove(name);
		}else{
			h.put(name,value);
		}
		setHashtable(EXT_INFO,h);
	}
	@SuppressWarnings("unchecked")
	public String getExt(final String name){
		Hashtable h=getHashtable(EXT_INFO);
		return checkNull((String)h.get(name));
	}
	protected Artikel(final String id){
		super(id);
	}
	protected 
	Artikel(){
	}
	
	
	/************************ Verrechenbar ************************/
	@Override
	public String getCode() { return getId();}
	@Override
	public String getText() { return getInternalName();}
	@Override
	public String getCodeSystemName() { return ARTIKEL;}
	
	@SuppressWarnings("unchecked")
	public int getPreis(final TimeTool dat, final Fall fall) {
		double vkt= checkZeroDouble(get(VK_PREIS));
		Hashtable ext=getHashtable(EXT_INFO);
		double vpe= checkZeroDouble((String)ext.get(VERPACKUNGSEINHEIT));
		double vke= checkZeroDouble((String)ext.get(VERKAUFSEINHEIT));
		if(vpe!=vke){
			return (int)Math.round(vke*(vkt/vpe));
		}else{
			return (int)Math.round(vkt);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public Money getKosten(final TimeTool dat){
		double vkt= checkZeroDouble(get(EK_PREIS));
		Hashtable ext=getHashtable(EXT_INFO);
		double vpe= checkZeroDouble((String)ext.get(VERPACKUNGSEINHEIT));
		double vke= checkZeroDouble((String)ext.get(VERKAUFSEINHEIT));
		if(vpe!=vke){
			return new Money((int)Math.round(vke*(vkt/vpe)));
		}else{
			return new Money((int)Math.round(vkt));
		}
	}
	public int getTP(final TimeTool date, final Fall fall) {
		return getPreis(date,fall);
	}
	public double getFactor(final TimeTool date, final Fall fall) {
		return 1.0;
	}
}
