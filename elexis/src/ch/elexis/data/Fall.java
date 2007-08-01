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
 *    $Id: Fall.java 2943 2007-08-01 07:46:16Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;


import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.MessageDialog;

import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.preferences.Leistungscodes;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.Extensions;
import ch.elexis.util.IRnOutputter;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
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
	
	/* das ist zu schweizlastig 
	public static final String LAW_DISEASE="KVG";
	public static final String LAW_ACCIDENT="UVG";
	public static final String LAW_INVALIDITY="IV";
	public static final String LAW_MILITARY="MV";
	public static final String LAW_INSURANCE="VVG";
	public static final String LAW_OTHER="privat";
	*/
	
	@Override
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
	
	@Override
	public boolean isValid(){
		
		if(!super.isValid()){
			return false;
		}
		Patient p=Patient.load(get("PatientID"));
		if((p==null) || (!p.isValid())){
			return false;
		}
		
		// Check whether all user-defined requirements for this billing system are met
		String reqs=getRequirements(getAbrechnungsSystem());
		if(reqs!=null){
			for(String req:reqs.split(";")){
				String[] r=req.split(":");
				String localReq=getInfoString(r[0]);
				if(StringTool.isNothing(localReq)){
					return false;
				}
				if(r[1].equals("K")){
					Kontakt k=Kontakt.load(localReq);
					if(!k.isValid()){
						return false;
					}
				}
			}
		}
		// check whether the outputter could output a bill
		IRnOutputter outputter=getOutputter();
		if(outputter!=null){
			if(!outputter.canBill(this)){
				return false;
			}
		}
		return true;
	}
	protected Fall(){/* leer */}
	protected Fall(final String id){
		super(id);
	}
	/**
	 * Einen neuen Fall zu einem Patienten mit einer Bezichnung erstellen
	 * (Garant muss später noch ergänzt werden; Datum wird von heute genommen
	 * @param PatientID
	 * @param Bezeichnung
	 */
	Fall(final String PatientID, final String Bezeichnung, final String Grund, String Abrechnungsmethode)
	{
		create(null);
		set(new String[]{"PatientID","Bezeichnung","Grund","DatumVon"},
				PatientID,Bezeichnung,Grund,new TimeTool().toString(TimeTool.DATE_GER));
		if(Abrechnungsmethode==null){
			String[] billings=getAbrechnungsSysteme();
			Abrechnungsmethode=billings[0];
		}
		setAbrechnungsSystem(Abrechnungsmethode);
		GlobalEvents.getInstance().fireObjectEvent(this, GlobalEvents.CHANGETYPE.create);
	}
	/** Einen Fall anhand der ID aus der Datenbank laden */
	public static Fall load(final String id){
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
     public void setBezeichnung(final String t){
         set("Bezeichnung",t);
     }
	/** 
	 * Anfangsdatum setzen 
	 * Zul�ssige Formate: dd.mm.yy, dd.mm.yyyy, yyyymmdd, yy-mm-dd
	 * */
	public void setBeginnDatum(final String dat){
		set("DatumVon",dat);
	}
	/** Enddatum lesen oder null: Fall noch nicht abgeschlossen */
	public String getEndDatum(){
		return checkNull(get("DatumBis"));
	}
	/** Enddatum setzen. Setzt zugleich den Fall auf abgeschlossen */
	public void setEndDatum(final String dat){
		set("DatumBis",dat);
	}
	
	/** Garant holen (existiert ev. nicht) */
	public Kontakt getGarant(){

		return Kontakt.load(getInfoString("Rechnungsempfänger"));
	}
	/**
	 * This is an update only for swiss installations that takes the old
	 * tarmed cases to the new system
	 */
	private static void update(){
		//String is=getInfoString("Kostenträger");
		Query<Fall> qbe=new Query<Fall>(Fall.class);
		for(Fall fall:qbe.execute()){
			if(fall.getInfoString("Kostenträger").equals("")){
				fall.setInfoString("Kostenträger",checkNull(fall.get("Kostentraeger")));
			}
			if(fall.getInfoString("Rechnungsempfänger").equals("")){
				fall.setInfoString("Rechnungsempfänger", checkNull(fall.get("GarantID")));
			}
			if(fall.getInfoString("Versicherungsnummer").equals("")){
				fall.setInfoString("Versicherungsnummer",checkNull(fall.get("VersNummer")));
			}
			if(fall.getInfoString("Fallnummer").equals("")){
				fall.setInfoString("Fallnummer",checkNull(fall.get("FallNummer")));
			}
			if(fall.getInfoString("Unfallnummer").equals("")){
				fall.setInfoString("Unfallnummer",checkNull(fall.get("FallNummer")));
			}
		}
	}
	/** Garant setzen 
	public void setGarant(final Kontakt garant){
		set("GarantID",garant==null ? "" : garant.getId());
	}
	public void setArbeitgeber(final Kontakt arbeitgeber){
		if(arbeitgeber==null){
			clearInfoString("Arbeitgeber");
		}else{
			setInfoString("Arbeitgeber",arbeitgeber.getId());
		}
	
	}

*/
	public Kontakt getArbeitgeber(){
		String id=getInfoString("Arbeitgeber");
		Kontakt ret=null;
		if(StringTool.isNothing(id) || ((ret=Kontakt.load(id)).exists()==false)){
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
		
		return Kontakt.load(getInfoString("Kostenträger"));
	}
	/** Kostenträger setzen 
	public void setKostentraeger(final Kontakt k){
		if(k!=null){
			set("Kostentraeger",k.getId());
		}
	}
	*/
	/** Versichertennummer holen */
	public String getVersNummer(){

		return checkNull(getInfoString("Versicherungsnummer"));
	}
	/** Versichertennummer setzen 
	public void setVersNummer(final String nr){
		set("VersNummer",nr);
	}
	*/
	/** Fallnummer lesen */
	public String getFallNummer(){
		return checkNull(get("FallNummer"));
	}
	/** Fallnummer setzen */
	public void setFallNummer(final String nr){
		set("FallNummer",nr);
	}
	/** Feststellen, ob der Fall noch offen ist */
	public boolean isOpen(){
		if(getEndDatum().equals("")){
			return true;
		}
		return false;
	}
	
	public void setAbrechnungsSystem(final String system){
		setInfoString("billing",system);
	}
	public String getAbrechnungsSystem(){
		String ret=getInfoString("billing");
		if(StringTool.isNothing(ret)){
			String[] systeme=getAbrechnungsSysteme();
			String altGesetz=get("Gesetz");
			int idx=StringTool.getIndex(systeme, altGesetz);
			if(idx==-1){
				ret=systeme[0];
			}else{
				ret=systeme[idx];
			}
			setAbrechnungsSystem(ret);
		}
		return ret;
	}
		
	public String getCodeSystemName(){
		return getCodeSystem(getAbrechnungsSystem());
	}
	
	public String getRequirements(){
		return getRequirements(getAbrechnungsSystem());
	}
	public String getOutputterName(){
		return getDefaultPrintSystem(getAbrechnungsSystem());
	}
	
	public IRnOutputter getOutputter(){
		String outputterName=getOutputterName();
		if(outputterName.length()>0){
			List<IConfigurationElement> list=Extensions.getExtensions("ch.elexis.RechnungsManager");
			for(IConfigurationElement ic:list){
				if(ic.getAttribute("name").equals(outputterName)){
					try {
						IRnOutputter ret=(IRnOutputter)ic.createExecutableExtension("outputter");
						return ret;
					} catch (CoreException e) {
						ExHandler.handle(e);
					}
				}
			}
		}
		return null;
	}
	
	/** Behandlungen zu diesem Fall holen */
	public Konsultation[] getBehandlungen(final boolean sortReverse)
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
    public void setGrund(final String g){
        set("Grund",g);
    }
    /*
	public String getGesetz(){
		return checkNull(get("Gesetz"));
	}
    public void setGesetz(final String g){
        set("Gesetz",g);
    }
    */
    @Override
	public String getLabel(){
    	String[] f=new String[]{"Grund","Bezeichnung","DatumVon","DatumBis"};
    	String[] v=new String[f.length];
    	get(f,v);
        StringBuilder ret=new StringBuilder();
        if(!isOpen()){
        	ret.append("-GESCHLOSSEN- ");
        }
        String ges=getAbrechnungsSystem();
        ret.append(ges).append(": ").append(v[0]).append(" - ");
        ret.append(v[1]).append("(");
        String ed=v[3];
        if((ed==null) || StringTool.isNothing(ed.trim())){
            ed=" offen ";
        }
        ret.append(v[2]).append("-").append(ed).append(")");    
        return ret.toString();
    }
    
    @Override
	public boolean delete() {
		return delete(false);
	}
    /**
     * Mark this Fall as deleted. This will fail if there exist Konsultationen fpr this Fall, unless
     * force is set 
     * @param force delete even if KOnsultationene xist (in that case, all Konsultationen will be deleted as well)
     * @return true if this Fall could be (and has been) deleted.
     */
	public boolean delete(final boolean force){
    	Konsultation[] bh=getBehandlungen(false);
    	if( (bh.length==0) || ((force==true) && (Hub.acl.request(AccessControlDefaults.DELETE_FORCED)==true))){
    		for(Konsultation b:bh){
    			b.delete(true);
    		}
    		delete_dependent();
    		return super.delete();
    	}
    	return false;
    }
	private boolean delete_dependent(){
		Query<AUF> qAUF=new Query<AUF>(AUF.class);
		qAUF.add("FallID", "=", getId());
		for(AUF auf:qAUF.execute()){
			auf.delete();
		}
		Query<Rechnung> qRn=new Query<Rechnung>(Rechnung.class);
		qRn.add("FallID", "=", getId());
		for(Rechnung rn:qRn.execute()){
			rn.delete();
		}
		return true;
	}
    @SuppressWarnings("unchecked")
	public String getInfoString(final String name){
    	Hashtable extinfo=getHashtable("ExtInfo");
    	return checkNull((String)extinfo.get(name));
    }
    @SuppressWarnings("unchecked")
	public void setInfoString(final String name, final String wert){
    	Hashtable<String, String> extinfo=getHashtable("ExtInfo");
    	extinfo.put(name,wert);
    	setHashtable("ExtInfo",extinfo);
    }
    @SuppressWarnings("unchecked")
    public void clearInfoString(final String string) {
    	Hashtable<String, String> extinfo=getHashtable("ExtInfo");
    	extinfo.remove(string);
    	setHashtable("ExtInfo",extinfo);
		
	}
	@Override
	public boolean isDragOK() {
		return true;
	}
	/*
	public void setPaymentMode(final String string) {
		setInfoString("payment",string);
		
	}
	*/
	/*
	public String getPaymentMode(){
		String tiers="TG";
		Kontakt garant=getGarant();
		if(!garant.isValid()){
			garant=getPatient();
		}
		Kontakt kk=getKostentraeger();
		if(!garant.isValid()){
			tiers="TP";
		}else{
			if(kk.isValid()){
				if(kk.equals(garant)){
					tiers="TP";
				}
			}
		}
		return tiers;
	}
	 */
	
	public static String getDefaultCaseLabel(){
		return Hub.userCfg.get(PreferenceConstants.USR_DEFCASELABEL, "Allgemein");
	}
	public static String getDefaultCaseReason(){
		return Hub.userCfg.get(PreferenceConstants.USR_DEFCASEREASON, "Krankheit");
	}
	public static String getDefaultCaseLaw(){
		return Hub.userCfg.get(PreferenceConstants.USR_DEFLAW, getAbrechnungsSysteme()[0]);
	}
	
	/**
	 * Find all installed bolling systems. If we do not find any, we assume that this is an old installation and
	 * try to update. If we find a tarmed-Plugin installed, we create default-tarmed billings.
	 * @return
	 */
	public static String[] getAbrechnungsSysteme(){
		String[] ret= Hub.globalCfg.nodes(Leistungscodes.CFG_KEY);
		if((ret==null) || (ret.length==0)){
			List<IConfigurationElement> list=Extensions.getExtensions("ch.elexis.RechnungsManager");
			for(IConfigurationElement ic:list){
				if(ic.getAttribute("name").startsWith("Tarmed")){
					Hub.globalCfg.set(Leistungscodes.CFG_KEY+"/KVG/name", "KVG");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY+"/KVG/leistungscodes", "TarmedLeistung");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY+"/KVG/standardausgabe", "Tarmed-Drucker");

					Hub.globalCfg.set(Leistungscodes.CFG_KEY+"/UVG/name", "UVG");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY+"/UVG/leistungscodes", "TarmedLeistung");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY+"/UVG/standardausgabe", "Tarmed-Drucker");
					
					Hub.globalCfg.set(Leistungscodes.CFG_KEY+"/IV/name", "IV");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY+"/IV/leistungscodes", "TarmedLeistung");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY+"/IV/standardausgabe", "Tarmed-Drucker");
					
					Hub.globalCfg.set(Leistungscodes.CFG_KEY+"/MV/name", "MV");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY+"/MV/leistungscodes", "TarmedLeistung");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY+"/MV/standardausgabe", "Tarmed-Drucker");

					Hub.globalCfg.set(Leistungscodes.CFG_KEY+"/privat/name", "privat");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY+"/privat/leistungscodes", "TarmedLeistung");
					Hub.globalCfg.set(Leistungscodes.CFG_KEY+"/privat/standardausgabe", "Tarmed-Drucker");
					
					PersistentObject.getConnection().exec("UPDATE VK_PREISE set typ='UVG' WHERE typ='ch.elexis.data.TarmedLeistungUVG'");
					PersistentObject.getConnection().exec("UPDATE VK_PREISE set typ='KVG' WHERE typ='ch.elexis.data.TarmedLeistungKVG'");
					PersistentObject.getConnection().exec("UPDATE VK_PREISE set typ='IV' WHERE typ='ch.elexis.data.TarmedLeistungIV'");
					PersistentObject.getConnection().exec("UPDATE VK_PREISE set typ='MV' WHERE typ='ch.elexis.data.TarmedLeistungMV'");
					update();
					break;
				}
			}
			ret= Hub.globalCfg.nodes(Leistungscodes.CFG_KEY);
			if(ret==null){
				return new String[]{"undefiniert"};
			}
		}
		return ret;
	}
	public static String getCodeSystem(final String billingSystem){
		String ret=Hub.globalCfg.get(Leistungscodes.CFG_KEY+"/"+billingSystem+"/leistungscodes", null);
		if(ret==null){		// compatibility
			getAbrechnungsSysteme();
			ret=Hub.globalCfg.get(Leistungscodes.CFG_KEY+"/"+billingSystem+"/leistungscodes", "?");
		}
		return ret;
	}
	
	public static String getDefaultPrintSystem(final String billingSystem){
		String ret=Hub.globalCfg.get(Leistungscodes.CFG_KEY+"/"+billingSystem+"/standardausgabe", null);
		if(ret==null){		// compatibility
			getAbrechnungsSysteme();
			ret=Hub.globalCfg.get(Leistungscodes.CFG_KEY+"/"+billingSystem+"/standardausgabe", "?");
		}
		return ret;
	}
	public static String getRequirements(final String billingSystem) {
		String ret=Hub.globalCfg.get(Leistungscodes.CFG_KEY+"/"+billingSystem+"/bedingungen", null);
		return ret;
	}
}
