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
 *    $Id: Fall.java 2373 2007-05-15 11:28:40Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;


import java.util.*;

import org.eclipse.jface.dialogs.MessageDialog;

import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Ein Fall ist eine Serie von zusammengehörigen Behandlungen.
 * Ein Fall hat einen Garanten, ein Anfangsdatum, eine Diagnoseliste,
 * ein Enddatum, eine Bezeichnung und allenfalls ein Enddatum 
 * @author Gerry
 *
 */
public class Fall extends PersistentObject{
	
	public static final String TYPE_DISEASE="Krankheit";
	public static final String TYPE_ACCIDENT="Unfall";
	public static final String TYPE_MATERNITY="Mutterschaft";
	public static final String TYPE_PREVENTION="Prävention";
	public static final String TYPE_BIRTHDEFECT="Geburtsgebrechen";
	public static final String TYPE_OTHER="Anderes";
	
	public static final String LAW_DISEASE="KVG";
	public static final String LAW_ACCIDENT="UVG";
	public static final String LAW_INVALIDITY="IV";
	public static final String LAW_MILITARY="MV";
	public static final String LAW_INSURANCE="VVG";
	public static final String LAW_OTHER="privat";
	
	
	protected String getTableName() {
		return "FAELLE";
	}
	static{
		addMapping("FAELLE",
				"PatientID","res=Diagnosen","DatumVon=S:D:DatumVon","DatumBis=S:D:DatumBis",
                "GarantID","Behandlungen=LIST:FallID:BEHANDLUNGEN:Datum",
				"Bezeichnung","Grund","Gesetz","Kostentraeger=KostentrID",
				"VersNummer","FallNummer","BetriebsNummer","ExtInfo");
	}
	
	public boolean isValid(){
		Patient p=Patient.load(get("PatientID"));
		if((p==null) || (!p.isValid())){
			return false;
		}
		if(getPaymentMode().equalsIgnoreCase("TG")){
			if(!getKostentraeger().exists()){
				return false;
			}
		}
		if(!getGarant().exists()){
			return false;
		}
		if(getGesetz().equalsIgnoreCase(LAW_ACCIDENT)){
			if(StringTool.isNothing(getFallNummer())){
				return false;
			}
		}else if(getGesetz().equalsIgnoreCase(LAW_DISEASE)){
			if(StringTool.isNothing(getVersNummer())){
				return false;
			}
		}
		return super.isValid();
	}
	protected Fall(){/* leer */}
	protected Fall(String id){
		super(id);
	}
	/**
	 * Einen neuen Fall zu einem Patienten mit einer Bezichnung erstellen
	 * (Garant muss später noch ergänzt werden; Datum wird von heute genommen
	 * @param PatientID
	 * @param Bezeichnung
	 */
	Fall(String PatientID, String Bezeichnung, String Grund, String Gesetz)
	{
		create(null);
		set(new String[]{"PatientID","Bezeichnung","Grund","Gesetz","DatumVon"},
				PatientID,Bezeichnung,Grund,Gesetz,new TimeTool().toString(TimeTool.DATE_GER));
		GlobalEvents.getInstance().fireObjectEvent(this, GlobalEvents.CHANGETYPE.create);
	}
	/** Einen Fall anhand der ID aus der Datenbank laden */
	public static Fall load(String id){
		Fall ret= new Fall(id);
		if(ret.exists()){
			return ret;
		}
		return null;
	}
	
	/** Anfangsdatum lesen (in der Form dd.mm.yy) */
	public String getBeginnDatum(){
		return checkNull(get("DatumVon"));
	}
	 public String getBezeichnung(){
		 return checkNull(get("Bezeichnung"));
	 }
     public void setBezeichnung(String t){
         set("Bezeichnung",t);
     }
	/** 
	 * Anfangsdatum setzen 
	 * Zul�ssige Formate: dd.mm.yy, dd.mm.yyyy, yyyymmdd, yy-mm-dd
	 * */
	public void setBeginnDatum(String dat){
		set("DatumVon",dat);
	}
	/** Enddatum lesen oder null: Fall noch nicht abgeschlossen */
	public String getEndDatum(){
		return checkNull(get("DatumBis"));
	}
	/** Enddatum setzen. Setzt zugleich den Fall auf abgeschlossen */
	public void setEndDatum(String dat){
		set("DatumBis",dat);
	}
	
	/** Garant holen (existiert ev. nicht) */
	public Kontakt getGarant(){
		return Kontakt.load(get("GarantID"));
	}
	/** Garant setzen */
	public void setGarant(Kontakt garant){
		set("GarantID",garant==null ? "" : garant.getId());
	}
	public void setArbeitgeber(Kontakt arbeitgeber){
		if(arbeitgeber==null){
			clearInfoString("Arbeitgeber");
		}else{
			setInfoString("Arbeitgeber",arbeitgeber.getId());
		}
	
	}

	public Kontakt getArbeitgeber(){
		String id=getInfoString("Arbeitgeber");
		Kontakt ret=null;
		if(StringTool.isNothing(id) || (ret=Kontakt.load(id)).exists()==false){
			return null;
		}
		return ret;
	}
	
	public String getArbeitgeberName(){
		return getArbeitgeber().getLabel();
	}
	
	public String getKostentraegerKuerzel(){
		return getKostentraeger().getKuerzel();
	}
	/** Kostenträger laden */
	public Kontakt getKostentraeger(){
		return Kontakt.load(get("Kostentraeger"));
	}
	/** Kostenträger setzen */
	public void setKostentraeger(Kontakt k){
		if(k!=null){
			set("Kostentraeger",k.getId());
		}
	}
	/** Versichertennummer holen */
	public String getVersNummer(){
		return checkNull(get("VersNummer"));
	}
	/** Versichertennummer setzen */
	public void setVersNummer(String nr){
		set("VersNummer",nr);
	}
	/** Fallnummer lesen */
	public String getFallNummer(){
		return checkNull(get("FallNummer"));
	}
	/** Fallnummer setzen */
	public void setFallNummer(String nr){
		set("FallNummer",nr);
	}
	/** Feststellen, ob der Fall noch offen ist */
	public boolean isOpen(){
		if(getEndDatum().equals("")){
			return true;
		}
		return false;
	}
	/** Behandlungen zu diesem Fall holen */
	public Konsultation[] getBehandlungen(boolean sortReverse)
	{
		List<String> list=getList("Behandlungen",sortReverse);
		int i=0;
		Konsultation[] ret=new Konsultation[list.size()];
		for(String id:list){
			ret[i++]=Konsultation.load(id);
		}
		//Arrays.sort(ret,new Konsultation.BehandlungsComparator(sortReverse));
		return ret;
	}
	public Konsultation getLetzteBehandlung(){
		List<String> list=getList("Behandlungen",true);
		if(list.size()>0){
			return Konsultation.load(list.get(0));
		}
		return null;
	}
	/** Neue Konsultation zu diesem Fall anlegen */
	public Konsultation neueKonsultation()
	{
		if(isOpen()==false){
			MessageDialog.openError(null,"Fall geschlossen","Zu einem abgeschlossenen Fall kann keine neue Konsultation erstellt werden");
			return null;
		}
		if((Hub.actMandant==null) || (!Hub.actMandant.exists())){
			SWTHelper.showError("Kein Mandant ausgewält", "Sie müssen erst einen Mandanten erstellen und auswählen, bevor Sie eine Konsultation erstellen können");
			return null;
		}
		return new Konsultation(this);
	}
	public Patient getPatient(){
		return Patient.load(get("PatientID"));
	}
	public String getGrund(){
		return checkNull(get("Grund"));
	}
    public void setGrund(String g){
        set("Grund",g);
    }
	public String getGesetz(){
		return checkNull(get("Gesetz"));
	}
    public void setGesetz(String g){
        set("Gesetz",g);
    }
    public String getLabel(){
    	String[] f=new String[]{"Gesetz","Grund","Bezeichnung","DatumVon","DatumBis"};
    	String[] v=new String[f.length];
    	get(f,v);
        StringBuilder ret=new StringBuilder();
        if(!isOpen()){
        	ret.append("-GESCHLOSSEN- ");
        }
        ret.append(v[0]).append(": ").append(v[1]).append(" - ");
        ret.append(v[2]).append("(");
        String ed=v[4];
        if((ed==null) || StringTool.isNothing(ed.trim())){
            ed=" offen ";
        }
        ret.append(v[3]).append("-").append(ed).append(")");    
        return ret.toString();
    }
    public boolean remove(boolean force){
    	Konsultation[] bh=getBehandlungen(false);
    	if(bh.length==0){
    		return super.delete();
    	}
    	if((force==true) && (Hub.acl.request(AccessControlDefaults.DELETE_FORCED)==true)){
    		for(Konsultation b:bh){
    			b.remove(true);
    		}
    		j.exec("DELETE FROM AUF WHERE FALLID="+getWrappedId());
    		j.exec("DELETE FROM RECHNUNGEN WHERE FALLID="+getWrappedId());
    		GlobalEvents.getInstance().clearSelection(getClass());
    		return true;
    	}
    	return false;
    	/*
    	String id=j.queryString("SELECT ID FROM BEHANDLUNGEN WHERE FALLID="+getWrappedId());
    	if(StringTool.isNothing(id)){
    		j.exec("DELETE FROM FAELLE WHERE ID="+getWrappedId());
    		return true;
    	}
    	return false;
    	*/
    }
    public String getInfoString(String name){
    	Hashtable extinfo=getHashtable("ExtInfo");
    	return checkNull((String)extinfo.get(name));
    }
    @SuppressWarnings("unchecked")
	public void setInfoString(String name, String wert){
    	Hashtable<String, String> extinfo=getHashtable("ExtInfo");
    	extinfo.put(name,wert);
    	setHashtable("ExtInfo",extinfo);
    }
    @SuppressWarnings("unchecked")
    public void clearInfoString(String string) {
    	Hashtable<String, String> extinfo=getHashtable("ExtInfo");
    	extinfo.remove(string);
    	setHashtable("ExtInfo",extinfo);
		
	}
	@Override
	public boolean isDragOK() {
		return true;
	}
	public void setPaymentMode(String string) {
		setInfoString("payment",string);
		
	}
	public String getPaymentMode(){
		return getInfoString("payment");
	}

	public static String getDefaultCaseLabel(){
		return Hub.userCfg.get(PreferenceConstants.USR_DEFCASELABEL, "Allgemein");
	}
	public static String getDefaultCaseReason(){
		return Hub.userCfg.get(PreferenceConstants.USR_DEFCASEREASON, "Krankheit");
	}
	public static String getDefaultCaseLaw(){
		return Hub.userCfg.get(PreferenceConstants.USR_DEFLAW, LAW_DISEASE);
	}
}
