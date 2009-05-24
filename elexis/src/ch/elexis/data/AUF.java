/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: AUF.java 5317 2009-05-24 15:00:37Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class AUF extends PersistentObject {

	public static final String PERCENT = "Prozent";
	public static final String REASON = "Grund";
	public static final String CASE_ID = "FallID";
	public static final String PATIENT_ID = "PatientID";
	public static final String TABLENAME = "AUF";
	public static final String ZUSATZ="Zusatz";
	public static final String DATE_FROM="von";
	public static final String DATE_UNTIL="bis";
	static{
		addMapping(TABLENAME,PATIENT_ID,CASE_ID,
				"von=S:D:DatumVon",
				"bis=S:D:DatumBis",
				REASON,
				PERCENT,"Zusatz=AUFZusatz","Erstellt=S:D:DatumAUZ");
	}
	public AUF(Fall f,String von, String bis, String proz,String grund){
		if(f!=null){
			Patient p=f.getPatient();
			if(p!=null){
				create(null);
				set(new String[]{PATIENT_ID,CASE_ID,"von","bis",PERCENT,REASON,"Erstellt"},
						p.getId(),f.getId(),von,bis,proz,grund,new TimeTool().toString(TimeTool.DATE_GER));
			}
		}
		
		
	}
	@Override
	public String getLabel() {
		String[] f={"von","bis",PERCENT,REASON,"Erstellt"};
		String[] v=new String[f.length];
		get(f,v);
		StringBuilder sb=new StringBuilder();
		if(!StringTool.isNothing(v[4])){
			sb.append("[").append(v[4]).append("]: ");
		}
		sb.append(v[0]).append("-").append(v[1]).append(": ")
			.append(v[2]).append("% (").append(v[3]).append(")");
		return sb.toString();
	}

	public Patient getPatient(){
		return getFall().getPatient();
	}
	public Fall getFall(){
		return Fall.load(get(CASE_ID));
	}
	public TimeTool getBeginn(){
		return new TimeTool(checkNull(get("von")));
	}
	public TimeTool getEnd(){
		return new TimeTool(checkNull(get("bis")));
	}
	public void setBeginn(String date){
		set("von",date);
	}
	public void setEnd(String date){
		set("bis",date);
	}
	public String getGrund(){
		return checkNull(get(REASON));
	}
	public String getZusatz(){
		return checkNull(get("Zusatz"));
	}
	public String getProzent(){
		return checkNull(get(PERCENT));
	}
	@Override
	protected String getTableName() {
		return TABLENAME;
	}
	public static AUF load(String id){
		return new AUF(id);
	}
	protected AUF(){}
	protected AUF(String id){
		super(id);
	}
}
