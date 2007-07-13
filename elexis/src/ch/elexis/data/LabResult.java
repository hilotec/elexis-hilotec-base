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
 *  $Id: LabResult.java 2793 2007-07-13 17:00:00Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.util.LinkedList;
import java.util.List;

import ch.rgw.tools.TimeTool;

public class LabResult extends PersistentObject {
	public static final int PATHOLOGIC = 1<<0; 
	public static final int OBSERVE = 1<<1;         // Anwender erklärt den Parameter für beobachtungswürdig
	public static final int NORMAL = 1<<2;         // Anwender erklärt den Wert explizit für normal (auch wenn er formal ausserhalb des Normbereichs ist) 
	@Override
	protected String getTableName() {
		return "LABORWERTE";
	}
	static{
		addMapping("LABORWERTE","PatientID","Datum=S:D:Datum",
				"ItemID","Resultat","Kommentar","Flags","Quelle=Origin");
		
	}
	public LabResult(final Patient p,final TimeTool date,final LabItem item,final String result,final String comment){
		create(null);
		String[] fields={"PatientID","Datum","ItemID","Resultat","Kommentar"};
		String[] vals=new String[]{
			p.getId(),
			date==null ? new TimeTool().toString(TimeTool.DATE_GER) : date.toString(TimeTool.DATE_GER),
			item.getId(),result,comment	};
		set(fields,vals);
		addToUnseen();
	}
	public static LabResult load(final String id){
		return new LabResult(id);
	}
	
	public Patient getPatient(){
		return Patient.load(get("PatientID"));
	}
	public String getDate(){
		return get("Datum");
	}
	public LabItem getItem(){
		return LabItem.load(get("ItemID"));
	}
	public String getResult(){
		return checkNull(get("Resultat"));
	}
	public void setResult(final String res){
		set("Resultat",checkNull(res));
	}
	public String getComment(){
		return checkNull(get("Kommentar"));
		
	}
	public boolean isFlag(final int flag){
		return (getFlags()&flag)!=0;
	}
	public void setFlag(final int flag, final boolean set){
		int flags=getFlags();
		if(set){
			flags|=flag;
		}else{
			 flags&=~(flag);
		}
		setInt("Flags",flags);
	}
	public int getFlags(){
		return checkZero(get("Flags"));
	}
	protected LabResult() {	}

	
	protected LabResult(final String id) {
		super(id);
	}

	@Override
	public String getLabel() {
		return getResult();
	}

	public void addToUnseen(){
		NamedBlob unseen=NamedBlob.load("Labresult:unseen");
		String results=unseen.getString();
		results+=","+getId();
		unseen.putString(results);
	}
	
	public void removeFromUnseen(){
		NamedBlob unseen=NamedBlob.load("Labresult:unseen");
		String results=unseen.getString();
		results=results.replaceAll(getId(), "");
		unseen.putString(results.replaceAll(",,", ","));
	}
	public static List<LabResult> getUnseen(){
		LinkedList<LabResult> ret=new LinkedList<LabResult>();
		NamedBlob unseen=NamedBlob.load("Labresult:unseen");
		String results=unseen.getString();
		if(results.length()>0){
			for(String id:results.split(",")){
				LabResult lr=load(id);
				if(lr.exists()){
					ret.add(lr);
				}
			}
		}
		return ret;
	}
	
}
