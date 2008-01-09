/*******************************************************************************
 * Copyright (c) 2005-2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: Kontakt.java 3509 2008-01-09 15:00:37Z rgw_ch $
 *******************************************************************************/


package ch.elexis.data;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import ch.elexis.Hub;
import ch.elexis.util.Log;
import ch.elexis.util.MFUList;
import ch.rgw.tools.StringTool;


/**
 * Ein Kontakt ist der kleinste gemeinsame Nenner anller Arten von Menschen
 * und Institutionen und somit die Basisklasse für alle Kontakte.
 * Ein  Kontakt hat eine  Anschrift und beliebig viele zusätzliche Bezugsadressen, 
 * sowie Telefon, E-Mail und Website. Zu einem Kontakt können ausserdem Reminders erstellt
 * werden.
 * Schliesslich hat jeder Kontakt noch einen "Infostore", einen im Prinzip unbegrenzt grossen
 * Speicher für beliebig viele parameter=wert - Paare, wo Informationen aller Art abgelegt werden
 * können. Jedem Element des Infostores können Zugriffsrechte zugeteilt werden, die definieren, wer
 * dieses Element lesen und Schreiben darf.
 * 
 * @author gerry
 *
 */
public class Kontakt extends PersistentObject{
	volatile String Bezug;
	protected String getTableName() {
		return "KONTAKT";
	}
	static{
		addMapping("KONTAKT",
		"BezugsKontakte = JOINT:myID:otherID:KONTAKT_ADRESS_JOINT",
		"MyReminders		= LIST:IdentID:REMINDERS",	
		"Bezeichnung1",
		"Bezeichnung2",
		"Bezeichnung3",
		"Kuerzel		= PatientNr",	
		"Bemerkung","Telefon1","Telefon2","E-Mail=EMail","Website","ExtInfo",
		"istOrganisation","istPerson","istPatient","istAnwender","istMandant",
		"istLabor","Strasse","Plz","Ort","Land","Fax","Anschrift","NatelNr"
		);
	}
	
	/**
	 * Returns a label describing this Kontakt.
	 * 
	 * The default implementation returns the short label, i. e. label(false)
	 * Sublcasses should overwrite getLabel(boolean short) for defining their
	 * own labels.
	 * @return a string describing this Kontakt.
	 */
	public String getLabel() {
		// return the long label
		return getLabel(false);
	}
	
	/**
	 * Returns a label describing this Kontakt.
	 * 
	 * The default implementation returns "Bezeichnung1" for the short label,
	 * and "Bezeichnung1", "Bezeichnung2", "Strasse", "Plz" and "Ort",
	 * separated with a comma, for the long label. 
	 * 
	 * Subclasses can overwrite this method and define their own label(s).
	 * If short is true, they should return a short label suitable for addresses.
	 * If short is false, they should return a long label describing all important
	 * properties of this Kontakt for unique identification by the user.
	 * 
	 * @param shortLabel return a short label for true, and a long label otherwise
	 * @return a string describing this Kontakt.
	 */
	public String getLabel(boolean shortLabel) {
		StringBuilder bld=new StringBuilder();
		
		if (shortLabel) {
			bld.append(get("Bezeichnung1"));
		} else {
			String[] ret=new String[5];
			get(new String[]{"Bezeichnung1","Bezeichnung2","Strasse","Plz","Ort"},ret);
			bld.append(ret[0]).append(" ").append(checkNull(ret[1]))
				.append(", ").append(checkNull(ret[2])).append(", ")
				.append(checkNull(ret[3]))
				.append(" ").append(checkNull(ret[4]));
		}

		return bld.toString();
	}
	
	public boolean isValid(){
		if(!super.isValid()){
			return false;
		}
		return true;
	}
	
	/**
	 * Ein Array mit allen zu diesem Kontakt definierten Bezugskontakten holen
	 * @return Ein Adress-Array, das auch die Länge null haben kann
	 */
	public List<BezugsKontakt> getBezugsKontakte(){
		Query<BezugsKontakt> qbe=new Query<BezugsKontakt>(BezugsKontakt.class);
		qbe.add("myID", "=", getId());
		return qbe.execute();
	}
	
	/** Die Anschrift dieses Kontakts holen */
	public Anschrift getAnschrift(){
		return new Anschrift(this); 
	}
	/** Die Anschrift dieses Kontakts setzen */
	public void setAnschrift(Anschrift adr){
        if(adr!=null){
            set(new String[]{"Strasse","Plz","Ort","Land"},
            		adr.getStrasse(),adr.getPlz(),adr.getOrt(),
            		adr.getLand());
        }
	}
	
	public String getPostAnschrift(boolean multiline){
		String an=get("Anschrift");
		if(StringTool.isNothing(an)){
			an=createStdAnschrift();
		}
		an=an.replaceAll("[\\r\\n]\\n","\n");
		return multiline==true ? an : an.replaceAll("\\n"," ");
	}
	
	public String createStdAnschrift(){
		Anschrift an=getAnschrift();
		String ret="";
		StringBuilder sb=new StringBuilder();
		if(istPerson()==true){
			Person p=Person.load(getId());

			// TODO default salutation should be configurable
			String salutation;
			if(p.getGeschlecht().equals("m")){
				salutation = Messages.getString("Kontakt.SalutationM"); //$NON-NLS-1$
			}else{
				salutation = Messages.getString("Kontakt.SalutationF"); //$NON-NLS-1$
			}
			sb.append(salutation);
			sb.append("\n");

			String titel=p.get("Titel");
			if(!StringTool.isNothing(titel)){
				sb.append(titel).append(" ");
			}
			sb.append(p.getVorname()).append(" ")
				.append(p.getName()).append("\n");
			sb.append(an.getEtikette(false,true));
			ret=sb.toString();
		} else if (this instanceof Organisation) {
			Organisation o = Organisation.load(getId());
			String[] rx=new String[2];
			o.get(new String[]{"Bezeichnung1","Bezeichnung2"},rx);
			sb.append(rx[0]).append(" ").append(checkNull(rx[1])).append("\n");
			sb.append(an.getEtikette(false, true));
			ret = sb.toString();
		}else{
			ret= an.getEtikette(true, true);
		}
		// create the postal if it does not exist yet
		String old=get("Anschrift");
		if(StringTool.isNothing(old)){
			set("Anschrift",ret);
		}
		return ret;
	}
	/** 
	 * Eine neue Zusatzadresse zu diesem Kontakt zufügen
	 * @param adr die Adresse
	 * @param sBezug ein Text, der die Beziehung dieser Adresse
	 * zum Kontakt definiert (z.B. "Geschäftlich" oder "Orthopäde" oder so) 
	 */
	public BezugsKontakt addBezugsKontakt(Kontakt adr,String sBezug){
        if((adr!=null) && (sBezug!=null)){
        	return new BezugsKontakt(this,adr,sBezug);
        }
        return null;
	}
	
	/**
	 * Zusatzadresse aus der Liste entfernen
	 * @param adr die Adresse
	 */
	public void removeBezugsKontakt(Kontakt adr){
        if(adr!=null){
            j.exec("DELETE FROM KONTAKT_ADRESS_JOINT WHERE otherID="+adr.getWrappedId());
        }
	}
	
	protected Kontakt(String id){
		super(id);
	}
   
	/** Kontakt mit gegebener Id aus der Datanbank einlesen */
	public static Kontakt load(String id){
		return new Kontakt(id);
	}
	protected Kontakt(){
		// System.out.println("Kontakt");
	}

	/** Die Reminders zu diesem Kontakt holen */
	public Reminder[] getRelatedReminders(){
		List<String> l=getList("MyReminders",false);
		Reminder[] ret=new Reminder[l.size()];
		int i=0;
		for(String id:l){
			ret[i++]=Reminder.load(id);
		}
		return ret;
	}
   
	@Override
	public boolean delete() {
		for(Reminder r:getRelatedReminders()){
			r.delete();
		}
		for(BezugsKontakt bk:getBezugsKontakte()){
			bk.delete();
		}
		return super.delete();
	}

	/** Ein Element aus dem Infostore auslesen 
	 *	Der Rückgabewert ist ein Object oder Null. 
	 *  Wenn die Rechte des aktuellen Anwenders zum Lesen
	 *  dieses Elements nicht ausreichen, wird ebenfalls 
	 *  Null zurückgeliefert.
	 *  2.9.2007 We remove the checks. they are useless at this moment
	 *  better check permissions on inout fields. gw
	 */
    public Object getInfoElement(String elem){
    	
    	//if(Hub.acl.request("Read"+elem)==true){
    		return getInfoStore().get(elem);
    	//}else{
    	//	log.log("Unzureichende Rechte zum Lesen von "+elem,Log.WARNINGS);
        //	return null;
    	//}
    }
    
    /**
     * Convenience-Methode und einen String aus dem Infostore auszulesen.
     * @param elem Name des Elements
     * @return Wert oder "" wenn das Element nicht vorhanden ist oder die Rechte
     * nicht zum Lesen ausreichen
     */
    public String getInfoString(String elem){
    	return checkNull((String)getInfoElement(elem));
    }
    
    /**
     * Ein Element in den Infostore schreiben. Wenn ein Element mit demselben
     * Namen schon existiert, wird es überschrieben. 
     * Wenn die Rechte des angemeldeten Anwenders nicht für das Schreiben dieses
     * Elements ausreichen, wird die Funktion still ignoriert.
     * @param elem Name des Elements
     * @param val Inhalt des Elements
     * 2.9.2007 emoved the checks g. weirich
     */
    @SuppressWarnings("unchecked")
	public void setInfoElement(String elem, Object val){
    	//if(Hub.acl.request("Write"+elem)==true){
	        Hashtable extinfos=getHashtable("ExtInfo");
	        if(extinfos!=null){
	            extinfos.put(elem,val);
	            setHashtable("ExtInfo",extinfos);
	        }
    	/*}else{
    		log.log("Unzureichende Rechte zum Schreiben von "+elem,Log.WARNINGS);
    	}*/
    }
    /**
     * Den gesamten Infostore holen. Dies ist sinnvoll, wenn kurz nacheinander
     * mehrere Werte gelesen oder geschrieben werden sollen, da damit das wiederholte
     * entpacken/packen gespart wird. Nach Änderungen muss der Store mit flushInfoStore()
     * explizit gesichert werden.
     * ACHTUNG: Nicht Thread-Safe. Konkurriende Schreiboperationen, während ein Thread den 
     * store hält, werden verlorengehen.
     * @return eine Hashtable, die die parameter-wert-paare enthält.
     */
    public Hashtable getInfoStore(){
    	return getHashtable("ExtInfo");
    	/*
    	if(Hub.acl.request("LoadInfoStore")==true){
    		return getHashtable("ExtInfo");
    	}
    	else{
    		log.log("Unzureichende Rechte zum lesen des Infostore",Log.WARNINGS);
    		return new Hashtable();
    	}
    	*/
    }
    /**
     * Den mit getInfoStore geholten Infostore wieder zurückschreiben. Dies muss immer dann
     * geschehen, wenn nach getInfoStore() schreiboperationen durchgeführt wurden.
     * @param store die zuvor mit getInfoStore() erhaltene Hashtable.
     */
    public void flushInfoStore(Hashtable store){
    	setHashtable("ExtInfo",store);
    	/*
    	if(Hub.acl.request("WriteInfoStore")==true){
    		setHashtable("ExtInfo",store);
    	}else{
    		log.log("Unzureichende Rechte zum Schreiben des Infostore",Log.WARNINGS);
    	}
    	*/
    }
    
    
    /** 
     * Einen Kontakt finden, der einen bestimmten Eintrag im Infostore enthält.
     * Falls mehrere passende Kontakte vorhanden sind, wird nur der erste 
     * zurückgeliefert.
     * @param clazz Unterklasse von Kontakt, nach der gesucht werden soll
     * @param field Name des gesuchten Infostore-Eintrags
     * @param value gesuchter Wert dieses Eintrags
     * @return Ein Objekt der Klasse clazz, welches einen Infostore-Eintrag field
     * mit dem Inhalt value enthält, oder null wenn kein solches Objekt existiert.
     */
    @SuppressWarnings("unchecked")
	public static Kontakt findKontaktfromInfoStore(Class clazz, String field,String value){
    	Query qbe=new Query(clazz);
    	List list=qbe.execute();
    	for(Kontakt k:(List<Kontakt>)list){
    		String i=(String)k.getInfoElement(field);
    		if(i!=null && i.equals(value)){
    			return k;
    		}
    	}
    	return null;
    }
    
    /**
     * Statistik für einen bestimmten Objekttyp holen
     * @param typ Der Typ (getClass().getName()) des Objekts. 
     * @return eine Liste mit Objektbezeichnern, die zwischen 0 und
     * 30 nach Häufigkeit sortierte Elemente enthält. 
     */
    @SuppressWarnings("unchecked")
	public List<String> getStatForItem(String typ){
    	Hashtable exi=getHashtable("ExtInfo");
    	ArrayList<statL> al=(ArrayList<statL>)exi.get(typ);
    	ArrayList<String> ret=new ArrayList<String>(al==null ? 1 : al.size());
    	if(al!=null){
	    	for(statL sl:al){
	    		ret.add(sl.v);
	    	}
    	}
    	return ret;
    }
    /**
     * Eine Statistik für ein bestimmtes Objekt anlegen. Es wird gezählt, wie oft
     * diese Funktion für dieses Objekt schon aufgerufen wurde, und Objekte desselben
     * Typs aber unterschiedlicher Identität werden in einer Rangliste aufgelistet.
     * Diese Rangliste kann mit getStatForItem() angerufen werden. Die Rangliste enthält
     * maximal 40 Einträge.
     * @param lst Das Objekt, das gezählt werden soll.
     */
    @SuppressWarnings("unchecked")
	public void statForItem(PersistentObject lst) {
    	Hashtable exi=getHashtable("ExtInfo");
    	String typ=lst.getClass().getName();
    	String ident=lst.storeToString();
    	// Die Rangliste für diesen Objekttyp auslesen bzw. neu anlegen.
		ArrayList<statL> l=(ArrayList<statL>)exi.get(typ);
		if(l==null){
			l=new ArrayList<statL>();
		}
		// Grösse der Rangliste limitieren. ggf. least frequently used entfernen
		while(l.size()>40){
			l.remove(l.size()-1);
		}
		// Sehen, ob das übergebene Objekt schon in der Liste enthalten ist
		boolean found=false;
		for(statL c:l){
			if(c.v.equals(ident)){
				c.c++;				// Gefunden, dann Zähler erhöhen
				found=true;
				break;
			}
		}
		if(found==false){
			l.add(new statL(ident)); // Nicht gefunden, dann neu eintragen
		}
		Collections.sort(l);		// Liste sortieren
		exi.put(typ,l);
		setHashtable("ExtInfo",exi);
	}
	public static class statL implements Comparable, Serializable{
		private static final long serialVersionUID = 10455663346456L;
		String v;
		int c;
		public statL(){}
		statL(String vv){
			v=vv;
			c=1;
		}
		public int compareTo(Object o) {
			if(o instanceof statL){
				statL ot=(statL)o;
				return ot.c-c;
			}
			return -1;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void statForString(String typ,String toStat){
		Hashtable exi=getHashtable("ExtInfo");
		MFUList<String> l=(MFUList<String>)exi.get(typ);
		if(l==null){
			l=new MFUList<String>(5,15);
		}
		l.count(toStat);
		exi.put(typ, l);
		setHashtable("ExtInfo", exi);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getStatForString(String typ){
    	Hashtable exi=getHashtable("ExtInfo");
    	MFUList<String> al=(MFUList<String>)exi.get(typ);
    	if(al==null){
    		al=new MFUList<String>(5,15);
    	}
    	return al.getAll();
    }
	
	@SuppressWarnings("unchecked")
	public MFUList<String> getMFU(String typ){
		Hashtable exi=getHashtable("ExtInfo");
		MFUList<String> l=(MFUList<String>)exi.get(typ);
		if(l==null){
			l=new MFUList<String>(5,15);
		}
		return l;
	}
	@SuppressWarnings("unchecked")
	public void setMFU(String typ, MFUList<String> mfu){
		Hashtable exi=getHashtable("ExtInfo");
		exi.put(typ, mfu);
		setHashtable("ExtInfo", exi);
	}
	public String getKuerzel(){
		return get("Kuerzel");
	}
	public String getBemerkung(){
		return get("Bemerkung");
	}
	public void setBemerkung(String b){
		set("Bemerkung",b);
	}
	public boolean istPerson(){
		return checkNull(get("istPerson")).equals("1");
	}
	public boolean istPatient(){
		return checkNull(get("istPatient")).equals("1");
	}
	public boolean istOrganisation() {
		return checkNull(get("istOrganisation")).equals("1");
	}
}
