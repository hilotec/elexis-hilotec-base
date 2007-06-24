/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: AUF.java 766 2006-08-21 10:29:17Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import ch.rgw.tools.TimeTool;

public class AUF extends PersistentObject {

	static{
		addMapping("AUF","PatientID","FallID",
				"von=S:D:DatumVon",
				"bis=S:D:DatumBis",
				"Grund",
				"Prozent","Zusatz=AUFZusatz");
	}
	public AUF(Fall f,String von, String bis, String proz,String grund){
		if(f!=null){
			Patient p=f.getPatient();
			if(p!=null){
				create(null);
				set(new String[]{"PatientID","FallID","von","bis","Prozent","Grund"},
						p.getId(),f.getId(),von,bis,proz,grund);
			}
		}
		
		
	}
	@Override
	public String getLabel() {
		String[] f={"von","bis","Prozent","Grund"};
		String[] v=new String[f.length];
		get(f,v);
		StringBuilder sb=new StringBuilder();
		sb.append(v[0]).append("-").append(v[1]).append(": ")
			.append(v[2]).append("% (").append(v[3]).append(")");
		return sb.toString();
	}

	public Patient getPatient(){
		return getFall().getPatient();
	}
	public Fall getFall(){
		return Fall.load(get("FallID"));
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
		return checkNull(get("Grund"));
	}
	public String getZusatz(){
		return checkNull(get("Zusatz"));
	}
	public String getProzent(){
		return checkNull(get("Prozent"));
	}
	@Override
	protected String getTableName() {
		return "AUF";
	}
	public static AUF load(String id){
		return new AUF(id);
	}
	protected AUF(){}
	protected AUF(String id){
		super(id);
	}
}
