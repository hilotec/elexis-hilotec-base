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
 * $Id: Prescription.java 5789 2009-10-30 13:39:20Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.util.Hashtable;
import java.util.SortedMap;
import java.util.TreeMap;

import ch.elexis.Hub;
import ch.elexis.StringConstants;
import ch.elexis.admin.AccessControlDefaults;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Eine Verordnung. Also ein Artikel zusmamen mit einer Einnahmevorschrift,
 * verknüpft mit einem Patienten.
 */
public class Prescription extends PersistentObject {
	
	public static final String TERMS = "terms";
	public static final String DATE_UNTIL = "DatumBis";
	public static final String DATE_FROM = "DatumVon";
	public static final String COUNT = "Anzahl";
	public static final String REMARK = "Bemerkung";
	public static final String DOSAGE = "Dosis";
	public static final String REZEPT_ID = "RezeptID";
	private static final String ARTICLE_ID = "ArtikelID";
	public static final String ARTICLE="Artikel";
	public static final String PATIENT_ID = "PatientID";
	private static final String TABLENAME = "PATIENT_ARTIKEL_JOINT";
	static{
		addMapping(TABLENAME,PATIENT_ID,ARTICLE,ARTICLE_ID,REZEPT_ID,"DatumVon=S:D:DateFrom",
			"DatumBis=S:D:DateUntil",DOSAGE,REMARK,COUNT,EXTINFO);
	}
	
	public Prescription(Artikel a, Patient p, String d, String b){
		create(null);
		String article=a.storeToString();
		set(new String[]{ARTICLE,PATIENT_ID,DOSAGE,REMARK,DATE_FROM},article,p.getId(),d,b,new TimeTool().toString(TimeTool.DATE_GER));
	}
	public Prescription(Prescription other){
		String[] fields=new String[]{ARTICLE,PATIENT_ID,DOSAGE,REMARK};
		String[] vals=new String[fields.length];
		if(other.get(fields, vals)){
			create(null);
			set(fields,vals);
			addTerm(new TimeTool(), vals[2]);
		}
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
		set(DATE_FROM,date==null ? new TimeTool().toString(TimeTool.DATE_GER) : date);
	}
	
	public String getBeginDate(){
		return checkNull(get(DATE_FROM));
	}
	
	public void setEndDate(String date){
		set(DATE_UNTIL,date==null ? new TimeTool().toString(TimeTool.DATE_GER) : date);
	}
	
	public String getEndDate(){
		return checkNull(get(DATE_UNTIL));
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
	
	/**
	 * return the article contained in this prescription. In earlier versions of elexis, this was the Article ID, now it is
	 * a String representation of the Article itself (which allows for reconstruction of the subclass used). For compatibility reasons
	 * we use the old technique for old prescriptions.
	 * @return
	 */
	public Artikel getArtikel(){
		// compatibility layer
		String art=get(ARTICLE);
		if(StringTool.isNothing(art)){
			return Artikel.load(get(ARTICLE_ID));
		}
		return (Artikel)Hub.poFactory.createFromString(art);
		
	}
	public String getDosis(){
		return checkNull(get(DOSAGE));
	}
	
	public void setDosis(String newDose){
		String oldDose=getDosis();
		if(!oldDose.equals(newDose)){
			addTerm(new TimeTool(),newDose);
		}
	}
	public String getBemerkung(){
		return checkNull(get(REMARK));
		
	}
	
	/**
	 * Ein Medikament stoppen
	 */
	@Override
	public boolean delete() {
		if(Hub.acl.request(AccessControlDefaults.MEDICATION_MODIFY)){
			TimeTool today=new TimeTool();
			today.addHours(-24);
			addTerm(today,StringConstants.ZERO);
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
		Hashtable<String, Object> extInfo=getHashtable(EXTINFO);
		String raw=(String)extInfo.get(TERMS);
		if(raw==null){
			raw="";
		}
		String lastBegin=get(DATE_FROM);
		String lastDose=get(DOSAGE);
		StringBuilder line=new StringBuilder();
		line.append(StringTool.flattenSeparator)
		.append(lastBegin)
		.append("::").append(lastDose);
		raw+=line.toString();
		extInfo.put(TERMS, raw);
		setHashtable(EXTINFO,extInfo);
		set(DATE_FROM,begin.toString(TimeTool.DATE_GER));
		set(DOSAGE,dose);
		if(dose.equals("0")){
			set(DATE_UNTIL,begin.toString(TimeTool.DATE_GER));
		}
	}
	
	/**
	 * A listing of all adinistration periods of this prescription. This is to retrieve later
	 * when and how the article was prescribed
	 * @return a Map of TimeTools and Doses (Sorted by date)
	 */
	public SortedMap<TimeTool, String> getTerms(){
		TreeMap<TimeTool, String> ret=new TreeMap<TimeTool,String>();
		Hashtable extInfo=getHashtable(EXTINFO);
		String raw=(String)extInfo.get(TERMS);
		if(raw!=null){
			String[] terms=raw.split(StringTool.flattenSeparator);
			for(String term:terms){
				String[] flds=term.split("::");
				TimeTool date=new TimeTool(flds[0]);
				String dose="n/a";
				if(flds.length>1){
					dose=flds[1];
				}
				ret.put(date, dose);
			}
		}
		ret.put(new TimeTool(get(DATE_FROM)), get(DOSAGE));
		return ret;
	}
	
	
	@Override
	protected String getTableName() {
		return TABLENAME;
	}
	@Override
	public boolean isDragOK() {
		return true;
	}
}
