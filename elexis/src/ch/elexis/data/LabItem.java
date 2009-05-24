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
 *  $Id: LabItem.java 5317 2009-05-24 15:00:37Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bsh.EvalError;
import bsh.Interpreter;

import ch.elexis.text.TextContainer;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Ein Laboritem, also ein anzeigbarer Laborwert. Jedes Laboritem hat einen
 * Titel, ein Kürzel, ein Labor, aus dem es stammt, einen Normbereich.
 * Ausserdem gehört jedes Laboritem zu einer Itemgruppe (Beispielsweise
 * Hämatologie oder Vitamine) und hat eine Priorität innerhalb dieser Gruppe.
 * Gruppe und Priorität beeinflussen die Darstellungsreihenfolge und Gruppierung
 * auf dem Laborblatt.
 * @author Gerry
 *
 */
public class LabItem extends PersistentObject implements Comparable<LabItem>{
	public static final String REF_MALE = "RefMann";
	public static final String REF_FEMALE_OR_TEXT = "RefFrauOrTx";
	public static final String PRIO = "prio";
	public static final String GROUP = "Gruppe";
	public static final String TYPE = "Typ";
	public static final String UNIT = "Einheit";
	public static final String LAB_ID = "LaborID";
	public static final String TITLE = "titel";
	public static final String SHORTNAME = "kuerzel";
	private static final String LABITEMS = "LABORITEMS";
	private static final Pattern varPattern=Pattern.compile(TextContainer.TEMPLATE_REGEXP);
	@Override
	protected String getTableName() {
		return LABITEMS;
	}
	static{
		addMapping(LABITEMS,SHORTNAME,TITLE,LAB_ID,
				REF_MALE,REF_FEMALE_OR_TEXT,UNIT,TYPE,GROUP,PRIO);
	}
	public enum typ{NUMERIC,TEXT,ABSOLUTE,FORMULA};
	
	public LabItem(String k,String t, Kontakt labor, String RefMann, String RefFrau, 
			String Unit, typ type,String grp,String seq){
		String tp="1";
		if(type==typ.NUMERIC){
			tp="0";
		}else if(type==typ.ABSOLUTE){
			tp="2";
		}
		create(null);
		if(StringTool.isNothing(seq)){
			seq=t.substring(0,1);
		}
		if(StringTool.isNothing(grp)){
			grp="z Verschiedenes";
		}
		if(labor==null){
			Query<Kontakt> qbe=new Query<Kontakt>(Kontakt.class);
			String labid=qbe.findSingle(Kontakt.IS_LAB,Query.EQUALS,StringTool.one);
			if(labid==null){
				labor=new Labor("Eigen","Eigenlabor");
			}else{
				labor=Labor.load(labid);
			}
		}
		set(new String[]{SHORTNAME,TITLE,LAB_ID,REF_MALE,
				REF_FEMALE_OR_TEXT,UNIT,TYPE,GROUP,PRIO},
				k,t,labor.getId(),RefMann,RefFrau,Unit,tp,grp,seq);
	}

	public static LabItem load(String id){
		return new LabItem(id);
	}
	public String getEinheit(){
		return checkNull(get(UNIT));
	}
	public String getGroup(){
		return checkNull(get(GROUP));
	}
	public String getPrio(){
		return checkNull(get(PRIO));
	}
	public String getKuerzel(){
		return checkNull(get(SHORTNAME));
	}
	public String getName(){
		return checkNull(get(TITLE));
	}
	public Labor getLabor(){
		return Labor.load(get(LAB_ID));
	}
	public typ getTyp(){
		String t=get(TYPE);
		if(t.equals(StringTool.zero)){
			return typ.NUMERIC;
		}else if(t.equals(StringTool.one)){
			return typ.TEXT;
		}else if(t.equals("2")){
			return typ.ABSOLUTE;
		}else{
			return typ.FORMULA;
		}
		
	}
	/**
	 * Evaluate a formula-based LabItem for a given Patient at a given date.
	 * It will try to retrieve all LabValues it depends on of that Patient and date 
	 * and then calculate the result. If there are not all necessare values given,
	 * it will return "?formula?". 
	 * @param date The date to consider for calculating
	 * @return the result or "?formel?" if no result could be calculated.
	 */
	public String evaluate(Patient pat,TimeTool date){
		if(!getTyp().equals(typ.FORMULA)){
			return  null;
		}
		Query<LabResult> qbe=new Query<LabResult>(LabResult.class);
		qbe.add(LabResult.PATIENT_ID, Query.EQUALS, pat.getId());
		qbe.add(LabResult.DATE, Query.EQUALS, date.toString(TimeTool.DATE_COMPACT));
		List<LabResult> results=qbe.execute();
		String formel=getFormula();
		boolean bMatched=false;
		for(LabResult result:results){
			String var=result.getItem().makeVarName();
			if(formel.indexOf(var)!=-1){
				formel=formel.replaceAll(var, result.getResult());
				bMatched=true;
			}
		}
		Matcher matcher=varPattern.matcher(formel);
		// Suche Variablen der Form [Patient.Alter]
		StringBuffer sb = new StringBuffer();

		while(matcher.find()){
			String var=matcher.group();
			String[] fields=var.split("\\.");
			if(fields.length>1){
				String repl="\""+pat.get(fields[1].replaceFirst("\\]", StringTool.leer))+"\"";
				//formel=matcher.replaceFirst(repl);
				matcher.appendReplacement(sb, repl);
				bMatched=true;
			}
		}
		matcher.appendTail(sb);
		if(!bMatched){
			return null;
		}
		Interpreter scripter=new Interpreter();
		
		try {
			return scripter.eval(sb.toString()).toString();
		} catch (EvalError e) {
			return "?formel?";
		}

	}
	/**
	 * Return the variable Name that identifies this item (in a script)
	 * @return a name that is made of the group and the priority values.
	 */
	public String makeVarName(){
		String[] group=getGroup().split(StringTool.space,2);
		String num=getPrio().trim();
		return group[0]+"_"+num;
	}
	public String getRefW(){
		String ret= checkNull(get(REF_FEMALE_OR_TEXT)).split("##")[0];
		return ret;
	}
	public String getRefM(){
		return checkNull(get(REF_MALE));
	}
	public void setRefW(String r){
		set(REF_FEMALE_OR_TEXT,r);
	}
	public void setRefM(String r){
		set(REF_MALE,r);
	}
	
	public void setFormula(String f){
		String val=getRefW();
		if(!StringTool.isNothing(f)){
			val+="##"+f;
		}
		set(REF_FEMALE_OR_TEXT,val);
	}
	public String getFormula(){
		String[] all=get(REF_FEMALE_OR_TEXT).split("##");
		return all.length>1 ? all[1] : "";
	}
	protected LabItem() {/* leer */}
	

	protected LabItem(String id) {
		super(id);
	}
	

	@Override
	public String getLabel() {
		StringBuilder sb=new StringBuilder();
		String [] fields={SHORTNAME,TITLE,REF_MALE,REF_FEMALE_OR_TEXT,UNIT,
				        TYPE,GROUP,PRIO};
		String[] vals=new String[fields.length];
		get(fields,vals);
		sb.append(vals[0]).append(", ").append(vals[1]);
		if(vals[5].equals(StringTool.zero)){
			sb.append(" (").append(vals[2]).append("-").append(vals[3]).append(StringTool.space).append(vals[4]).append(")");
		}else{
			sb.append(" (").append(vals[3]).append(")");
		}
		sb.append("[").append(vals[6]).append(", ").append(vals[7]).append("]");
		return sb.toString();
		
	}

	public String getShortLabel(){
		StringBuilder sb=new StringBuilder();
		String[] fields={TITLE,UNIT,LAB_ID};
		String[] vals=new String[fields.length];
		get(fields,vals);
		Labor lab=Labor.load(vals[2]);
		String labName="Labor?";
		if(lab!=null){
			labName=lab.get("Bezeichnung1");
		}
		sb.append(vals[0]).append(" (").append(vals[1]).append("; ")
			.append(labName).append(")");
		return sb.toString();
	}
	
	public int compareTo(LabItem other) {
		// check for null; put null values at the end
		if (other == null) {
			return -1;
		}
		
		// first, compare the groups
		String mineGroup = getGroup();
		String otherGroup = other.getGroup();
		if (!mineGroup.equals(otherGroup)) {
			// groups differ, just compare groups
			return mineGroup.compareTo(otherGroup);
		}
		
		// compare item priorities
		String mine=getPrio();
		String others=other.getPrio();
		if((mine.matches("[0-9]+")) && (others.matches("[0-9]+"))){
			Integer iMine=Integer.parseInt(mine);
			Integer iOthers=Integer.parseInt(others);
			return iMine.compareTo(iOthers);
		}
		return mine.compareTo(others);
	}
}
