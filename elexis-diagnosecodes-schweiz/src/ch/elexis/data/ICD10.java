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
 *    $Id: ICD10.java 1749 2007-02-06 21:04:45Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.io.ByteArrayInputStream;
import java.util.Hashtable;

import org.eclipse.jface.action.IAction;

import ch.rgw.tools.ExHandler;

public class ICD10 extends PersistentObject implements IDiagnose {
	static final String create="DROP INDEX icd1;" + //$NON-NLS-1$
			"DROP INDEX icd2;"+ //$NON-NLS-1$
			"DROP TABLE ICD10;"+ //$NON-NLS-1$
			"CREATE TABLE ICD10 ("+ //$NON-NLS-1$
					"ID       VARCHAR(25) primary key, "+ //$NON-NLS-1$
					"parent   VARCHAR(25),"+ //$NON-NLS-1$
					"ICDCode  VARCHAR(10),"+ //$NON-NLS-1$
					"encoded  TEXT,"+ //$NON-NLS-1$
					"ICDTxt   TEXT,"+ //$NON-NLS-1$
					"ExtInfo  BLOB);"+ //$NON-NLS-1$
					"CREATE INDEX icd1 ON ICD10 (parent);"+ //$NON-NLS-1$
					"CREATE INDEX icd2 ON ICD10 (ICDCode);"; //$NON-NLS-1$
	
	static{
		addMapping("ICD10","parent","Code=ICDCode","Text=ICDTxt","encoded","ExtInfo"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	}
	static final int LEVEL=0;
	static final int TERMINAL=1;
	static final int GENERATED=2;
	static final int KIND=3;
	static final int CHAPTER=4;
	static final int GROUP=5;
	static final int SUPERCODE=6;
	static final int CODE=7;
	static final int CODE_SHORT=8;
	static final int CODE_COMPACT=9;
	static final int TEXT=10;
	
	
	static public boolean createTable(){
		try{
			ByteArrayInputStream bais=new ByteArrayInputStream(create.getBytes("UTF-8")); //$NON-NLS-1$
			return j.execScript(bais,true, false);
		}catch(Exception ex){
			ExHandler.handle(ex);
			return false;
		}
			
	}
	public static ICD10 load(String id){
		return new ICD10(id);
	}
	
	public ICD10(String parent, String code,String shortCode){
		create(null);
		set("Code",code); //$NON-NLS-1$
		set("encoded",shortCode); //$NON-NLS-1$
		set("parent",parent); //$NON-NLS-1$
		set("Text",getField(TEXT)); //$NON-NLS-1$
	}
	/*
	public String createParentCode(){
		String code=getField(CODE);
		String ret="NIL";
		String chapter=getField(CHAPTER);
		String group=chapter+":"+getField(GROUP);
		String supercode=getField(SUPERCODE);
		if(code.equals(supercode)){
			if(code.equals(group)){
				if(code.equals(chapter)){
					ret="NIL";
				}else{
					ret=chapter;
				}
			}else{
				ret=group;
			}
		}else{
			ret=supercode;
		}
		return ret;
	}*/
	
	public String getEncoded(){
		return get("encoded"); //$NON-NLS-1$
	}
	public String getField(int f){
		return getEncoded().split(";")[f]; //$NON-NLS-1$
	}
	public ICD10() {}

	protected ICD10(String id) {
		super(id);
	}

	@Override
	public String getLabel() {
		StringBuilder b=new StringBuilder();
		b.append(getCode()).append(" ").append(getText()); //$NON-NLS-1$
		return b.toString();
	}

	@Override
	protected String getTableName() {
		return "ICD10"; //$NON-NLS-1$
	}

	public String getCode() {
		return get("Code"); //$NON-NLS-1$
	}

	public String getText() {
		return get("Text"); //$NON-NLS-1$
	}

	public String getCodeSystemName() {
			return "ICD-10"; //$NON-NLS-1$
	}
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public void setExt(String name,String value){
		Hashtable<String,String> ext=getExtInfo();
		ext.put(name,value);
		writeExtInfo(ext);
	}
	public String getExt(String name){
		Hashtable ext=getExtInfo();
		String ret=(String)ext.get(name);
		return checkNull(ret);
	}
	
	public Hashtable getExtInfo(){
		return getHashtable("ExtInfo"); //$NON-NLS-1$
	}
	public void writeExtInfo(Hashtable ext){
		setHashtable("ExtInfo",ext); //$NON-NLS-1$
	}
	@Override
	public boolean isDragOK() {
		if(getField(TERMINAL).equals("T")){ //$NON-NLS-1$
			return true;
		}
		return false;
	}
	public String getCodeSystemCode() {
		return "999"; //$NON-NLS-1$
	}
	public Iterable<IAction> getActions() {
		// TODO Auto-generated method stub
		return null;
	}

}
