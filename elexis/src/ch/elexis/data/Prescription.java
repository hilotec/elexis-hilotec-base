/*******************************************************************************
 * Copyright (c) 2005-20076, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: Prescription.java 2762 2007-07-08 20:35:24Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.util.Hashtable;
import java.util.SortedMap;
import java.util.TreeMap;

import ch.elexis.Hub;
import ch.elexis.admin.AccessControlDefaults;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Eine Verordnung. Also ein Artikel zusmamen mit einer Einnahmevorschrift,
 * verknüpft mit einem Patienten.
 */
public class Prescription extends PersistentObject {
	
	static{
		addMapping("PATIENT_ARTIKEL_JOINT","PatientID","ArtikelID","RezeptID","DatumVon=S:D:DateFrom",
				"DatumBis=S:D:DateUntil","Dosis","Bemerkung","Anzahl","ExtInfo");
	}
	
	public Prescription(Artikel a, Patient p, String d, String b){
		create(null);
		set(new String[]{"ArtikelID","PatientID","Dosis","Bemerkung"},a.getId(),p.getId(),d,b);
	}
	public Prescription(Prescription other){
		String[] fields=new String[]{"ArtikelID","PatientID","Dosis","Bemerkung"};
		String[] vals=new String[fields.length];
		if(other.get(fields, vals)){
			create(null);
			set(fields,vals);
		}
		addTerm(new TimeTool(), vals[2]);
	}
	public static Prescription load(String id){
		return new Prescription(id);
	}
	protected Prescription() {
	}

	protected Prescription(String id) {
		super(id);
	}

	/**
	 * Set the begin date of this prescription
	 * @param date may be null to set it as today
	 */
	public void setBeginDate(String date){
		set("DatumVon",date==null ? new TimeTool().toString(TimeTool.DATE_GER) : date);
	}
	
	public String getBeginDate(){
		return checkNull(get("DatumVon"));
	}
	
	public void setEndDate(String date){
		set("DatumBis",date==null ? new TimeTool().toString(TimeTool.DATE_GER) : date);
	}
	
	public String getEndDate(){
		return checkNull(get("DatumBis"));
	}
	
	@Override
	public String getLabel(){
		return getSimpleLabel()+" "+getDosis();
	}
	public String getSimpleLabel() {
		Artikel art=getArtikel();
		if(art!=null){
			return getArtikel().getLabel();
		}else{
			return "Fehler";
		}
	}
	
	public Artikel getArtikel(){
		return Artikel.load(get("ArtikelID"));	
	}
	public String getDosis(){
		return checkNull(get("Dosis"));
	}
	
	public void setDosis(String newDose){
		String oldDose=getDosis();
		if(!oldDose.equals(newDose)){
			addTerm(new TimeTool(),newDose);
		}
	}
	public String getBemerkung(){
		return checkNull(get("Bemerkung"));
	
	}
	
	/**
	 * Ein Medikament stoppen
	 */
	@Override
	public boolean delete() {
		if(Hub.acl.request(AccessControlDefaults.MEDICATION_MODIFY)){
			TimeTool today=new TimeTool();
			today.addHours(-24);
			addTerm(today,"0");
			return true;
		}
		return false;
	}
	
	/**
	 * Ein Medikament aus der Datenbank löschen
	 * @return
	 */
	public boolean remove(){
		if(Hub.acl.request(AccessControlDefaults.DELETE_MEDICATION)){
			return super.delete();
		}
		return false;
	}
	/**
	 * Insert a new dosage term, defined by a beginning date and a dose
	 * We store the old dose and its beginning date in the field "terms".
	 * @param dose a dosage definition of the form "1-0-0-0" or "0" to stop the article
	 */
	@SuppressWarnings("unchecked")
	public void addTerm(TimeTool begin, String dose){
		Hashtable<String, Object> extInfo=getHashtable("ExtInfo");
		String raw=(String)extInfo.get("terms");
		String lastBegin=get("DatumVon");
		String lastDose=get("Dosis");
		StringBuilder line=new StringBuilder();
		line.append(StringTool.flattenSeparator)
			.append(lastBegin)
			.append("::").append(lastDose);
		raw+=line.toString();
		extInfo.put("terms", raw);
		setHashtable("ExtInfo",extInfo);
		set("DatumVon",begin.toString(TimeTool.DATE_GER));
		set("Dosis",dose);
		if(dose.equals("0")){
			set("DatumBis",begin.toString(TimeTool.DATE_GER));
		}
	}
	
	/**
	 * A listing of all adinistration periods of this prescription. This is to retrieve later
	 * when and how the article was prescribed
	 * @return a Map of TimeTools and Doses (Sorted by date)
	 */
	public SortedMap<TimeTool, String> getTerms(){
		TreeMap<TimeTool, String> ret=new TreeMap<TimeTool,String>();
		Hashtable extInfo=getHashtable("ExtInfo");
		String raw=(String)extInfo.get("terms");
		if(raw!=null){
			String[] terms=raw.split(StringTool.flattenSeparator);
			for(String term:terms){
				String[] flds=term.split("::");
				TimeTool date=new TimeTool(flds[0]);
				String dose=flds[1];
				ret.put(date, dose);
			}
		}
		ret.put(new TimeTool(get("DatumVon")), get("Dosis"));
		return ret;
	}
	
	
	@Override
	protected String getTableName() {
		return "PATIENT_ARTIKEL_JOINT";
	}
	@Override
	public boolean isDragOK() {
		return true;
	}
	static class Term{
		String begin;
		String dose;
	}
	
}
