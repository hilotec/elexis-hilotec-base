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
 *  $Id: LabItem.java 2807 2007-07-14 20:03:29Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import ch.rgw.tools.StringTool;

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
	@Override
	protected String getTableName() {
		return "LABORITEMS";
	}
	static{
		addMapping("LABORITEMS","kuerzel","titel","LaborID",
				"RefMann","RefFrauOrTx","Einheit","Typ","Gruppe","prio");
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
			String labid=qbe.findSingle("istLabor","=","1");
			if(labid==null){
				labor=new Labor("Eigen","Eigenlabor");
			}else{
				labor=Labor.load(labid);
			}
		}
		set(new String[]{"kuerzel","titel","LaborID","RefMann",
				"RefFrauOrTx","Einheit","Typ","Gruppe","prio"},
				k,t,labor.getId(),RefMann,RefFrau,Unit,tp,grp,seq);
	}

	public static LabItem load(String id){
		return new LabItem(id);
	}
	public String getEinheit(){
		return checkNull(get("Einheit"));
	}
	public String getGroup(){
		return checkNull(get("Gruppe"));
	}
	public String getPrio(){
		return checkNull(get("prio"));
	}
	public String getKuerzel(){
		return checkNull(get("kuerzel"));
	}
	public String getName(){
		return checkNull(get("titel"));
	}
	public Labor getLabor(){
		return Labor.load(get("LaborID"));
	}
	public typ getTyp(){
		String t=get("Typ");
		if(t.equals("0")){
			return typ.NUMERIC;
		}else if(t.equals("1")){
			return typ.TEXT;
		}
		return typ.ABSOLUTE;
		
	}
	
	public String getRefW(){
		return checkNull(get("RefFrauOrTx"));
	}
	public String getRefM(){
		return checkNull(get("RefMann"));
	}
	public void setRefW(String r){
		set("RefFrauOrTx",r);
	}
	public void setRefM(String r){
		set("RefMann",r);
	}
	protected LabItem() {/* leer */}
	

	protected LabItem(String id) {
		super(id);
	}
	

	@Override
	public String getLabel() {
		StringBuilder sb=new StringBuilder();
		String [] fields={"kuerzel","titel","RefMann","RefFrauOrTx","Einheit",
				        "Typ","Gruppe","prio"};
		String[] vals=new String[fields.length];
		get(fields,vals);
		sb.append(vals[0]).append(", ").append(vals[1]);
		if(vals[5].equals("0")){
			sb.append(" (").append(vals[2]).append("-").append(vals[3]).append(" ").append(vals[4]).append(")");
		}else{
			sb.append(" (").append(vals[3]).append(")");
		}
		sb.append("[").append(vals[6]).append(", ").append(vals[7]).append("]");
		return sb.toString();
		
	}

	public String getShortLabel(){
		StringBuilder sb=new StringBuilder();
		String[] fields={"titel","Einheit","LaborID"};
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
