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
 * $Id: Leistungsblock.java 4395 2008-09-08 17:21:12Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;

import ch.elexis.Hub;
import ch.rgw.Compress.CompEx;
import ch.rgw.tools.ExHandler;

public class Leistungsblock extends PersistentObject implements ICodeElement{
	static {
		addMapping("LEISTUNGSBLOCK","Name","MandantID","Leistungen");
	}
	
	public Leistungsblock(String Name, Mandant m){
		create(null);
		String[] f=new String[]{"Name","MandantID"};
		set(f,Name,m.getId());
	}
	
	/** 
	 * return a List of elements contained in this block
	 * will never return null, but the list might be empty
	 * @return a possibly empty list of ICodeElements
	 */
	public List<ICodeElement> getElements(){
		return load();
	}
	
	/**
	 * Add an ICodeElement to this block
	 * @param v an Element
	 */
	public void addElement(ICodeElement v){
		if(v!=null){
			List<ICodeElement> lst=load();
			int i=0;
			for(ICodeElement ice:lst){
				if(ice.getCode().compareTo(v.getCode())>0){
					break;
				}
				i++;
			}
			lst.add(i,v);
			flush(lst);
		}
	}
	
	public void removeElement(ICodeElement v){
		if(v!=null){
			List<ICodeElement> lst=load();
			lst.remove(v);
			flush(lst);
		}
	}
	
	/**
	 * Move a CodeElement inside the block
	 * @param v the element to move
	 * @param offset offset to move. negative values move up, positive down
	 */
	public void moveElement(ICodeElement v, int offset){
		if(v!=null){
			List<ICodeElement> lst=load();
			int idx=lst.indexOf(v);
			if(idx!=-1){
				int npos=idx+offset;
				if(npos<0){
					npos=0;
				}else if(npos>=lst.size()){
					npos=lst.size()-1;
				}
				ICodeElement el=lst.remove(idx);
				lst.add(npos, el);
				flush(lst);
			}
		}
	}
	@Override
	public String storeToString(){
		return toString(load());
	}
	public String toString(List<ICodeElement> lst){
		StringBuilder st=new StringBuilder();
		for(ICodeElement v:lst){
			st.append(((PersistentObject)v).storeToString()).append(",");
		}
		return st.toString().replaceFirst(",$","");
		
	}
	@Override
	public String getLabel() {
		return get("Name");
	}
	public String getText(){
		return get("Name");
	}
	public String getCode(){
		return get("Name");
	}
	@Override
	protected String getTableName(){
			return "LEISTUNGSBLOCK";
	}

	public static Leistungsblock load(String id){
		return new Leistungsblock(id);
	}
	protected Leistungsblock(String id){
		super(id);
	}
	protected Leistungsblock(){}
	
	private boolean flush(List<ICodeElement> lst){
		try{
			if(lst==null){
				lst=new ArrayList<ICodeElement>();
			}
			String storable=toString(lst);
			setBinary("Leistungen",CompEx.Compress(storable,CompEx.ZIP));
			return true;
		}catch(Exception ex){
			ExHandler.handle(ex);
		}
		return false;
	}
	private List<ICodeElement> load(){
		ArrayList<ICodeElement> lst=new ArrayList<ICodeElement>();
		try{
			lst=new ArrayList<ICodeElement>();
			byte[] compressed=getBinary("Leistungen");
			if(compressed!=null){
				String storable=new String(CompEx.expand(compressed),"UTF-8");
				for(String p:storable.split(",")){
					lst.add((ICodeElement)Hub.poFactory.createFromString(p));
				}
			}
		}catch(Exception ex){
			ExHandler.handle(ex);
		}
		return lst;
	}
	@Deprecated
	public boolean isEmpty(){
		byte[] comp=getBinary("Leistungen");
		return (comp==null);
	}
	public String getCodeSystemName() {
		return "Block";
	}
	public String getCodeSystemCode() {
		return "999";
	}
	@Override
	public boolean isDragOK() {
		return true;
	}
	public List<IAction> getActions(Verrechnet kontext) {
		
		return null;
	}
}
