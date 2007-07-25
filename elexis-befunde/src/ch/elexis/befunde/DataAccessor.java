/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id$
 *******************************************************************************/

package ch.elexis.befunde;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.IDataAccess;
import ch.elexis.util.Log;
import ch.elexis.util.Result;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Access data stored in Befunde
 * Access syntax is: Befunde:BD
 * @author gerry
 *
 */
public class DataAccessor implements IDataAccess {
	Hashtable<String, String> hash;
	Hashtable<String,String[]> columns;
	ArrayList<String> parameters;
	
	@SuppressWarnings("unchecked")
	public DataAccessor(){
		Messwert setup=Messwert.getSetup();
		columns=new Hashtable<String,String[]>();
		parameters=new ArrayList<String>();
		hash=setup.getHashtable("Befunde");
		String names=hash.get("names");
		if(!StringTool.isNothing(names)){
			for(String n:names.split(Messwert.SETUP_SEPARATOR)){
				String vals=hash.get(n+"_FIELDS");
				if(vals!=null){
					vals="Datum"+Messwert.SETUP_SEPARATOR+vals;
					String[] flds=vals.split(Messwert.SETUP_SEPARATOR);
					parameters.add(n);
					columns.put(n, flds);
				}
				
			}
		}
	}
	
	

	public List<Element> getList() {
		ArrayList<Element> ret=new ArrayList<Element>(parameters.size());
		for(String n:parameters){
			ret.add(new IDataAccess.Element(IDataAccess.TYPE.STRING,n,Patient.class,1));
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public Result<Object> getObject(final String name, final PersistentObject ref,final String... params) {
		Result<Object> ret=null;
		if(!(ref instanceof Patient)){
			ret=new Result<Object>(Log.ERRORS,IDataAccess.INVALID_PARAMETERS,"Ungültiger Parameter",ref,true);
		}else{
			if(params.length!=1){
				ret=new Result<Object>(Log.ERRORS,IDataAccess.INVALID_PARAMETERS,"Falsche Parameterzahl",params,true);
			}else{
				if(!(params[0] instanceof String)){
					ret=new Result<Object>(Log.ERRORS,IDataAccess.INVALID_PARAMETERS,"Datum,'all' oder 'last' erwartet",params,true);
					
				}else{
					Patient pat=(Patient)ref;
					Query<Messwert> qbe=new Query<Messwert>(Messwert.class);
					qbe.add("PatientID","=",pat.getId()); //$NON-NLS-1$ //$NON-NLS-2$
					qbe.add("Name","=",name); //$NON-NLS-1$ //$NON-NLS-2$
					List<Messwert> list=qbe.execute();
					String[][] values;
					String[] cols=columns.get(name);
					if(params[0].equals("all")){
						values=new String[list.size()+1][cols.length];
					}else{
						values=new String[2][cols.length];
					}
					for(int i=0;i<cols.length;i++){
						values[0][i]=cols[i].split(Messwert.SETUP_CHECKSEPARATOR)[0];
					}
					int i=1;
					if(params[0].equals("all")){
						for(Messwert m:list){
							String date=m.get("Datum");
							values[i][0]=new TimeTool(date).toString(TimeTool.DATE_GER);
							Hashtable befs=m.getHashtable("Befunde");
							for(int j=1;j<cols.length;j++){
								String vv=(String)befs.get(values[0][j]);
								values[i][j]=vv;
								if(values[i][j]==null){
									values[i][j]="";
								}
							}
							i++;
						}
						ret=new Result<Object>(values);
					}else if(params[0].equals("last")){
						TimeTool today=new TimeTool(TimeTool.BEGINNING_OF_UNIX_EPOCH);
						Messwert last=null;
						for(Messwert m:list){
							TimeTool vgl=new TimeTool(m.get("Datum"));
							if(vgl.isAfter(today)){
								today=vgl;
								last=m;
							}
						}
						if(last==null){
							ret=new Result<Object>(Log.ERRORS,IDataAccess.OBJECT_NOT_FOUND,"Nicht gefunden",params,true);
						}else{
							values[1][0]=today.toString(TimeTool.DATE_GER);
							Hashtable befs=last.getHashtable("Befunde");
							for(int j=0;j<parameters.size();j++){
								values[1][j+1]=(String)befs.get(parameters.get(j));
							}
							ret=new Result<Object>(values);
						}
					}else{
						TimeTool find=new TimeTool();
						if(find.set(params[0])==false){
							ret=new Result<Object>(Log.ERRORS,IDataAccess.INVALID_PARAMETERS,"Datum erwartet",params,true);
						}else{
							for(Messwert m:list){
								TimeTool vgl=new TimeTool(m.get("Datum"));
								if(vgl.isEqual(find)){
									values[1][0]=vgl.toString(TimeTool.DATE_GER);
									Hashtable befs=m.getHashtable("Befunde");
									for(int j=0;j<parameters.size();j++){
										values[1][j+1]=(String)befs.get(parameters.get(j));
									}
									ret=new Result<Object>(values);
								}
							}
						}
					}
				}
				
			}
			
		}
		return ret;
	}

}
