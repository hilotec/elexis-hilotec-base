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
 *    $Id: PrefsPage.java 4134 2008-07-13 19:13:37Z rgw_ch $
 *******************************************************************************/
package ch.elexis.befunde;

import java.util.Hashtable;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.data.Query;
import ch.elexis.scripting.ScriptEditor;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;

public class PrefsPage extends Composite {
	
	
	String[] mNames;
	Text[] texts;
	Button[] checkboxes;
	Label[] labels;
	Hashtable<String,String> hash;
	String name;
	
	PrefsPage(final Composite parent, final Hashtable<String,String> hash, final String name){
		super(parent,SWT.NONE);
		setLayout(new GridLayout(3,false));
		this.hash=hash;
		this.name=name;

		load();
	}
	
	void load(){
		if(texts!=null){
			for(int i=0;i<texts.length;i++){
				texts[i].dispose();
				checkboxes[i].dispose();
				labels[i].dispose();
			}
			mNames=null;
			checkboxes=null;
			labels=null;
			texts=null;
		}
		String fields=hash.get(name+"_FIELDS");
		if(StringTool.isNothing(fields)){
			texts=new Text[1];
			checkboxes=new Button[1];
			labels=new Label[1];
			labels[0]=new Label(this,SWT.NONE);
			labels[0].setText("F1");
			texts[0]=SWTHelper.createText(this, 1, SWT.NONE);
			checkboxes[0]=new Button(this,SWT.CHECK);
			checkboxes[0].setText("mehrzeilig");
			
		}else{
			mNames=fields.split(Messwert.SETUP_SEPARATOR);
			texts=new Text[mNames.length+1];
			checkboxes=new Button[texts.length];
			labels=new Label[texts.length];
			for(int i=0;i<texts.length;i++){
				labels[i]=new Label(this,SWT.NONE);
				labels[i].setText("F"+Integer.toString(i+1));
				labels[i].setForeground(Desk.getColor(Desk.COL_BLUE));
				labels[i].addMouseListener(new ScriptListener(i));
				texts[i]=SWTHelper.createText(this, 1, SWT.NONE);
				checkboxes[i]=new Button(this,SWT.CHECK);
				checkboxes[i].setText("mehrzeilig");
				if(i<mNames.length){
					String[] line=mNames[i].split(Messwert.SETUP_CHECKSEPARATOR);
					texts[i].setText(line[0]);
					if(line.length>1){
						checkboxes[i].setSelection(line[1].equals("m"));
					}
				}
			}
		}
		layout();
		//Button bAddLine=new Button(this,SWT.PUSH);
		//bAddLine.setText("neue Zeile");
	}
	
	void flush(){
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<texts.length;i++){
			String n=texts[i].getText();
			if(StringTool.isNothing(n)){
				continue;
			}
			String m="m";
			if(checkboxes[i].getSelection()==false){
				m="s";
			}
			sb.append(n).append(Messwert.SETUP_CHECKSEPARATOR).append(m).append(Messwert.SETUP_SEPARATOR);
		}
		if(sb.length()>Messwert.SETUP_SEPARATOR.length()){
			sb.setLength(sb.length()-Messwert.SETUP_SEPARATOR.length());
			hash.put(name+"_FIELDS", sb.toString());
		}
	}
	
	boolean remove(){
		if(Hub.acl.request(ACLContributor.DELETE_PARAM) && SWTHelper.askYesNo("Warnung: Unwiederufbare Aktion",
				"Wenn Sie diesen Parameter löschen, werden auch sämtliche dazu eingegebenen Daten gelöscht. Wirklich löschen?")){
			Query<Messwert> qbe=new Query<Messwert>(Messwert.class);
			qbe.add("Name", "=", name);
			for(Messwert m:qbe.execute()){
				m.delete();
			}
			hash.remove(name+"_FIELDS");
			return true;
		}
		return false;
	}
	private  class ScriptListener extends MouseAdapter{
		private int i;
		ScriptListener(int x){
			i=x;
		}
		@Override
		public void mouseUp(MouseEvent e) {
			ScriptEditor se=new ScriptEditor(getShell(),texts[i].getText(),"Geben Sie bitte an, wie dieser Parameter errechnet werden soll");
			if(se.open()==Dialog.OK){
				texts[i].setText(se.getScript());
			}

			super.mouseUp(e);
		}
		
	}
}
