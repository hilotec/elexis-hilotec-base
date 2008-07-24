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
 *  $Id: LabResult.java 4176 2008-07-24 19:50:11Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.util.LinkedList;
import java.util.List;

import ch.elexis.Hub;
import ch.elexis.preferences.LabSettings;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.Log;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class LabResult extends PersistentObject {
	public static final int PATHOLOGIC = 1<<0; 
	public static final int OBSERVE = 1<<1;         // Anwender erklärt den Parameter für beobachtungswürdig
	public static final int NORMAL = 1<<2;         // Anwender erklärt den Wert explizit für normal (auch wenn er formal ausserhalb des Normbereichs ist)
	
	private static final String TABLENAME = "LABORWERTE";
	
	@Override
	protected String getTableName() {
		return TABLENAME;
	}
	static{
		addMapping("LABORWERTE","PatientID","Datum=S:D:Datum",
				"ItemID","Resultat","Kommentar","Flags","Quelle=Origin");
		
	}
	
	/**
	 * create a new LabResult. If the type is numeric, we'll check whether it's pathologic
	 */
	public LabResult(final Patient p,final TimeTool date,final LabItem item,final String result,final String comment){
		create(null);
		String[] fields={"PatientID","Datum","ItemID","Resultat","Kommentar","Flags"};
		int flags=isPathologic(p,item,result) ? PATHOLOGIC : 0;
		String[] vals=new String[]{
			p.getId(),
			date==null ? new TimeTool().toString(TimeTool.DATE_GER) : date.toString(TimeTool.DATE_GER),
			item.getId(),result,comment,Integer.toString(flags)};
		set(fields,vals);
		addToUnseen();
	}
	
	private boolean isPathologic(final Patient p, final LabItem item, final String result){
		if(item.getTyp().equals(LabItem.typ.ABSOLUTE)){
			if(result.toLowerCase().startsWith("pos")){
				return true;
			}
			if(result.trim().startsWith("+")){
				return true;
			}
		}else /*if(item.getTyp().equals(LabItem.typ.NUMERIC))*/{
			String nr;
			if(p.getGeschlecht().equalsIgnoreCase("m")){
				nr=item.getRefM();
			}else{
				nr=item.getRefW();
			}
			if(nr.trim().startsWith("<")){
				try{
					double ref=Double.parseDouble(nr.substring(1).trim());
					double val=Double.parseDouble(result);
					if(val>=ref){
						return true;
					}
				}catch(NumberFormatException nfe){
					// don't mind
				}
			}else if(nr.trim().startsWith(">")){
				try{
					double ref=Double.parseDouble(nr.substring(1).trim());
					double val=Double.parseDouble(result);
					if(val<=ref){
						return true;
					}
				}catch(NumberFormatException nfe){
					// again, don't mind
				}
			}else{
				String[] range=nr.split("\\s*-\\s*");
				if(range.length==2){
					try{
						double lower=Double.parseDouble(range[0]);
						double upper=Double.parseDouble(range[1]);
						double val=Double.parseDouble(result);
						if((val<lower) || (val>upper)){
							return true;
						}
					}catch(NumberFormatException nre){
						// still, we don't mind
					}
				}
			}
		}
		return false;
		
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
		int flags=isPathologic(getPatient(),getItem(),res) ? PATHOLOGIC : 0; 
		set(new String[]{"Resultat","Flags"},new String[]{checkNull(res),Integer.toString(flags)});
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
		StringBuilder sb=new StringBuilder();
		sb.append(getItem().getLabel())
			.append(", ").append(getDate())
			.append(": ").append(getResult());
		return sb.toString();
		//return getResult();
	}

	public static LabResult getForDate(final Patient pat,final TimeTool date,final LabItem item){
		Query<LabResult> qbe=new Query<LabResult>(LabResult.class);
		qbe.add("ItemID", "=", item.getId());
		qbe.add("PatientID", "=", pat.getId());
		qbe.add("Datum", "=", date.toString(TimeTool.DATE_COMPACT));
		List<LabResult> res=qbe.execute();
		if((res!=null) && (res.size()>0)){
			return res.get(0);
		}
		return null;
	}
	
	/**
	 * add a LabResult to the list of unseen LabResults. We do not keep
	 * LabResults older than KEEP_UNSEEN_LAB_RESULTS days in this list.
	 */
	public void addToUnseen(){
		List<LabResult> o=getUnseen();
		LinkedList<String> n=new LinkedList<String>();
		n.add(getId());
		TimeTool limit=new TimeTool();
		try{		// We need to catch wrong formatted numbers in KEEP_UNSEEN
			limit.addHours(-24*Integer.parseInt(Hub.globalCfg.get(LabSettings.KEEP_UNSEEN_LAB_RESULTS,PreferenceConstants.DAYS_TO_KEEP_UNSEEN_LAB_RESULTS)));
		}catch(NumberFormatException nex){
			ExHandler.handle(nex);
			limit.addHours(-24*7);
		}
		//log.log(limit.dump(),Log.INFOS);
		TimeTool tr=new TimeTool();
		for(LabResult lr:o){
			log.log(lr.getDate(),Log.INFOS);
			if(tr.set(lr.getDate())){
				if(tr.isAfter(limit)){
					n.add(lr.getId());
				}
			}
		}
		NamedBlob unseen=NamedBlob.load("Labresult:unseen");
		String results=StringTool.join(n, ",");
		unseen.putString(results);
		unseen.set("lastupdate", new TimeTool().toString(TimeTool.TIMESTAMP));
	}
	
	public void removeFromUnseen(){
		NamedBlob unseen=NamedBlob.load("Labresult:unseen");
		String results=unseen.getString();
		results=results.replaceAll(getId(), "");
		unseen.putString(results.replaceAll(",,", ","));
		unseen.set("lastupdate", new TimeTool().toString(TimeTool.TIMESTAMP));
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
	
	public static String getLastUpdateUnseen(){
		NamedBlob unseen=NamedBlob.load("Labresult:unseen");
		return unseen.get("lastupdate");
	}
}
