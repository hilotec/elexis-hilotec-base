package com.hilotec.elexis.messwerte.data;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;


public class Panel {
	private String type;
	private Panel[] panels;
	private String[] fields;
	private String[] attributes;
	
	public Panel[] getPanels() {
		return panels;
	}
	public void setPanels(Panel[] panels) {
		this.panels = panels;
	}
	public String[] getFields() {
		return fields;
	}
	public void setFields(String[] fields) {
		this.fields = fields;
	}
	public String[] getAttributes() {
		return attributes;
	}
	public void setAttributes(String[] attributes) {
		this.attributes = attributes;
	}

	public Panel(String type){
		this.type=type;
	}
	public String getAttribute(String name){
		for(String a:attributes){
			if(a.startsWith(name+"=")){
				return a.substring(name.length()+1);
			}
		}
		return null;
	}
	public Composite createComposite(Composite parent){
		Composite ret=new Composite(parent,SWT.NONE);
		if(type.equals("plain")){
			ret.setLayout(new FillLayout());
		}else if(type.equals("grid")){
			String cols=getAttribute("columns");
			if(cols==null){
				ret.setLayout(new GridLayout());
			}else{
				ret.setLayout(new GridLayout(Integer.parseInt(cols),false));
			}
		}
		for(Panel p:panels){
			p.createComposite(ret);
		}
		return ret;
	}
}
