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
 * $Id: Verrechnet.java 4708 2008-12-02 16:44:44Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.util.Hashtable;

import ch.elexis.Hub;
import ch.elexis.util.Log;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

/**
 * Ein Verrechnet ist ein realisiertes Verrechenbar. Ein Verrechenbar wird durch
 * die Zuordnung zu einer Konsultation zu einem Verrechnet. Der Preis eines
 * Verrechnet ist zunächst Taxpunkwert(TP) mal Scale (Immer in der kleinsten
 * Währungseinheit, also Rappen oder ggf. cent). Der effektive Preis kann aber
 * geändert werden (Rabatt etc.)
 * Nebst VK_Scale, welche in der Schweiz dem taxpunktwert entspricht, können noch externe
 * und interne zusätzlich Skalierungen angewendet werden. PrimaryScalefactor wird beispielsweise für
 * %-Reduktionen oder Zusschläge gemäss Tarmed verwendet, SecondaryScalefactor kann ein Rabatt oder ein 
 * Privatzuschschlag sein.
 * 
 * @author gerry
 * 
 */
public class Verrechnet extends PersistentObject {
	
	static {
		addMapping("LEISTUNGEN", "Konsultation=Behandlung", "Leistg_txt",
			"Leistg_code", "Klasse", "Zahl", "EK_Kosten", "VK_TP", "VK_Scale",
			"VK_Preis", "Scale", "Scale2", "ExtInfo=Detail");
	}
	
	public Verrechnet(final IVerrechenbar iv, final Konsultation kons,
		final int zahl){
		create(null);
		TimeTool dat = new TimeTool(kons.getDatum());
		Fall fall = kons.getFall();
		int tp = iv.getTP(dat, fall);
		double factor = iv.getFactor(dat, fall);
		long preis = Math.round(tp * factor);
		set(new String[] {
			"Konsultation", "Leistg_txt", "Leistg_code", "Klasse", "Zahl",
			"EK_Kosten", "VK_TP", "VK_Scale", "VK_Preis", "Scale", "Scale2"
		}, new String[] {
			kons.getId(), iv.getText(), iv.getId(), iv.getClass().getName(),
			Integer.toString(zahl), iv.getKosten(dat).getCentsAsString(),
			Integer.toString(tp), Double.toString(factor),
			Long.toString(preis), "100","100"
		});
		if (iv instanceof Artikel) {
			((Artikel) iv).einzelAbgabe(1);
		}
	}
	
	public String getText(){
		return checkNull(get("Leistg_txt"));
	}
	
	public void setText(String text){
		set("Leistg_txt", text);
	}
	
	/**
	 * Taxpunktwert auslesen
	 * @return
	 */
	public double getTPW(){
		return checkZeroDouble(get("VK_Scale"));
	}
	
	/**
	 * set the primary scale factor (usually system specific or "internal" to
	 * the code system NOTE: This ist NOT identical to the multiplier or
	 * "Taxpunkt". The final price will be calculated as VK_PREIS * VK_SCALE *
	 * primaryScale * secondaryScale
	 * 
	 * @param scale
	 *            the new scale value as x.x
	 */
	public void setPrimaryScaleFactor(double scale){
		int sca = (int) Math.round(scale * 100);
		setInt("Scale", sca);
	}
	
	/**
	 * get the prinary scale factor
	 * 
	 * @see setPrimaryScaleFactor
	 * @return the primary svcale factor as double
	 */
	public double getPrimaryScaleFactor(){
		int sca = checkZero(get("Scale"));
		if(sca==0){
			return 1.0;
		}
		return ((double) sca) / 100.0;
	}
	
	/**
	 * Set the secondary scale factor
	 * 
	 * @see setPromaryScaleFactor
	 * @param scale
	 *            the factor
	 */
	public void setSecondaryScaleFactor(double scale){
		int sca = (int) Math.round(scale * 100);
		setInt("Scale2", sca);
	}
	
	/**
	 * Get the secondary scale factor
	 * 
	 * @see setPrimaryScaleFactor
	 * @return the factor
	 */
	public double getSecondaryScaleFactor(){
		int sca = checkZero(get("Scale2"));
		if(sca==0){
			return 1.0;
		}
		return ((double) sca) / 100.0;
	}
	
	
	/**
	 * Taxpunktpreis setzen
	 * @param tp
	 */
	public void setTP(double tp){
		set("VK_TP",Long.toString(Math.round(tp)));
	}
	/**
	 * Den effektiven Preis setzen (braucht nicht TP*Scale zu sein
	 * 
	 * @deprecated use setTP and setFactor
	 */
	@Deprecated
	public void setPreis(final Money m){
		set("VK_Preis", m.getCentsAsString());
	}
	
	/**
	 * Einkaufskosten
	 */
	public Money getKosten(){
		System.out.println(getText());
		return new Money(checkZero(get("EK_Kosten")));
	}
	
	/**
	 * Den effektiv verrechneten Preis holen (braucht nicht TP*Scale zu sein
	 * 
	 * @deprecated
	 */
	@Deprecated
	public Money getEffPreis(){
		return new Money(checkZero(get("VK_Preis")));
		/*
		 * double
		 * amount=checkZero(get("VK_Preis"))*checkZero(get("Scale"))/100.0;
		 * return new Money((int)Math.round(amount));
		 */
	}
	
	/**
	 * Den Preis nach Anwendung sämtlicher SKalierungsfaktoren zurückgeben
	 * @return
	 */
	public Money getNettoPreis(){

		Money brutto=getBruttoPreis();
		brutto.multiply(getPrimaryScaleFactor());
		brutto.multiply(getSecondaryScaleFactor());
		return brutto;
	}
	
	/**
	 * Den Preis nach Anwendung des Taxpunktwerts (aber ohne sonstige Skalierungen) holen
	 */
	public Money getBruttoPreis(){
		int tp=checkZero(get("VK_TP"));
		Konsultation k = getKons();
		Fall fall = k.getFall();
		TimeTool date = new TimeTool(k.getDatum());
		IVerrechenbar v=getVerrechenbar();
		double tpw=1.0;
		if(v!=null){						// Unknown tax system
			tpw=v.getFactor(date, fall); 
		}
		return new Money((int) Math.round(tpw * tp));
	}
	/** Den Standardpreis holen (Ist immer TP*Scale, auf ganze Rappen gerundet) */
	
	public Money getStandardPreis(){
		IVerrechenbar v = getVerrechenbar();
		Konsultation k = getKons();
		Fall fall = k.getFall();
		TimeTool date = new TimeTool(k.getDatum());
		double factor=1.0;
		int tp=0;
		if(v!=null){
			factor = v.getFactor(date, fall);
			tp = v.getTP(date, fall);
		}else{
			tp=checkZero(get("VK_TP"));
		}
		return new Money((int) Math.round(factor * tp));
	}
	
	/** Bequemlichkeits-Shortcut für Standardbetrag setzen */
	public void setStandardPreis(){
		IVerrechenbar v = getVerrechenbar();
		Konsultation k = getKons();
		Fall fall = k.getFall();
		TimeTool date = new TimeTool(k.getDatum());
		double factor = v.getFactor(date, fall);
		int tp = v.getTP(date, fall);
		long preis = Math.round(tp * factor);
		set(new String[] {
			"VK_Scale", "VK_TP", "VK_Preis"
		}, Double.toString(factor), Integer.toString(tp), Long.toString(preis));
	}
	
	public Konsultation getKons(){
		return Konsultation.load(get("Konsultation"));
	}
	
	/** Wie oft wurde die Leistung bei derselben Kons. verrechnet? */
	public int getZahl(){
		return checkZero(get("Zahl"));
	}
	
	public void setZahl(final int z){
		set("Zahl", Integer.toString(z));
	}
	
	public String getCode(){
		IVerrechenbar verrechenbar = getVerrechenbar();
		if (verrechenbar == null) {
			return "?";
		} else {
			return verrechenbar.getCode();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void setDetail(final String key, final String value){
		Hashtable ext = getHashtable("Detail");
		if(value==null){
			ext.remove(key);
		}else{
			ext.put(key, value);
		}
		setHashtable("Detail", ext);
		
	}
	
	@SuppressWarnings("unchecked")
	public String getDetail(final String key){
		Hashtable ext = getHashtable("Detail");
		return (String) ext.get(key);
	}
	
	public void changeAnzahl(int neuAnzahl){
		int vorher = getZahl();
		setZahl(neuAnzahl);
		IVerrechenbar vv = getVerrechenbar();
		if (vv instanceof Artikel) {
			Artikel art = (Artikel) vv;
			art.einzelRuecknahme(vorher);
			art.einzelAbgabe(neuAnzahl);
		}
	}
	
	/** Frage, ob dieses Verrechnet aus dem IVerrechenbar tmpl entstanden ist */
	public boolean isInstance(final IVerrechenbar tmpl){
		String[] res = new String[2];
		get(new String[] {
			"Klasse", "Leistg_code"
		}, res);
		if (tmpl.getClass().getName().equals(res[0])) {
			if (tmpl.getId().equals(res[1])) {
				return true;
			}
		}
		return false;
	}
	
	public IVerrechenbar getVerrechenbar(){
		String[] res = new String[2];
		get(new String[] {
			"Klasse", "Leistg_code"
		}, res);
		try {
			return (IVerrechenbar) Hub.poFactory.createFromString(res[0] + "::"
				+ res[1]);
		} catch (Exception ex) {
			log.log("Fehlerhafter Leistungscode " + getLabel(), Log.ERRORS);
		}
		return null;
	}
	
	@Override
	public String getLabel(){
		return checkNull(get("Leistg_txt"));
	}
	
	@Override
	protected String getTableName(){
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
