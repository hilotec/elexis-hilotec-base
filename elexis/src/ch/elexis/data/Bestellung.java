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
 *    $Id: Bestellung.java 284 2006-05-08 05:58:10Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.util.ArrayList;
import java.util.List;
import ch.rgw.tools.TimeTool;

public class Bestellung extends PersistentObject {
	private List<Item> alItems;
	public enum ListenTyp{PHARMACODE,NAME,VOLL};
	
	static{
		addMapping("HEAP2","Liste=S:C:Contents");
	}
	public Bestellung(String name, Anwender an){
		TimeTool t=new TimeTool();
		create(name+":"+t.toString(TimeTool.TIMESTAMP)+":"+an.getId());
		alItems=new ArrayList<Item>();
	}
	@Override
	public String getLabel() {
		String[] i=getId().split(":");
		TimeTool t=new TimeTool(i[1]);
		return i[0]+": "+t.toString(TimeTool.FULL_GER);
	}

	public String asString(ListenTyp type){
		StringBuilder ret=new StringBuilder();
		for(Item i:alItems){
			switch (type) {
			case PHARMACODE:
				ret.append(i.art.getPharmaCode());
				break;
			case NAME:
				ret.append(i.art.getLabel());
				break;
			case VOLL:
				ret.append(i.art.getPharmaCode()).append(" ")
					.append(i.art.getName());
				break;
			default:
				break;
			}
			ret.append(",").append(i.num).append("\n");
		}
		return ret.toString();
	}
	
	public List<Item> asList(){
		return alItems;
	}
	public void addItem(Artikel art, int num){
		Item i=findItem(art);
		if(i!=null){
			i.num+=num;
		}else{
			alItems.add(new Item(art,num));
		}
	}
	
	public Item findItem(Artikel art){
		for(Item i:alItems){
			if(i.art.getId().equals(art.getId())){
				return i;
			}
		}
		return null;
	}
	public void removeItem(Item art){
			alItems.remove(art);
		
	}
	
	public void save(){
		StringBuilder sb=new StringBuilder();
		for(Item i:alItems){
			sb.append(i.art.getId()).append(",").append(i.num).append(";");
		}
		set("Liste",sb.toString());
	}
	
	public void load(){
		String[] it=checkNull(get("Liste")).split(";");
		if(alItems==null){
			alItems=new ArrayList<Item>();
		}else{
			alItems.clear();
		}
		for(String i:it){
			String[] fld=i.split(",");
			if(fld.length==2){
				Artikel art=Artikel.load(fld[0]);
				alItems.add(new Item(art,Integer.parseInt(fld[1])));
			}
		}
	}
	@Override
	protected String getTableName() {
		return "HEAP2";
	}
	
	public static Bestellung load(String id){
		Bestellung ret=new Bestellung(id);
		if(ret!=null){
			ret.load();
		}
		return ret;
	}
	protected Bestellung(){
	}
	protected Bestellung(String id){
		super(id);
	}
	public static class Item{
		public Item(Artikel a, int n){
			art=a;
			num=n;
		}
		public Artikel art;
		public int num;
	}
}
