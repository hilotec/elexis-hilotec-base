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
 *  $Id: LaborblattView.java 1728 2007-02-03 10:08:17Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.jdom.Document;
import org.jdom.Element;

import ch.elexis.data.Brief;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.text.ITextPlugin;
import ch.elexis.text.TextContainer;
import ch.elexis.text.ITextPlugin.ICallback;

public class LaborblattView extends ViewPart implements ICallback{
	public static final String ID="ch.elexis.Laborblatt";
	TextContainer text;
	
	public LaborblattView() {
	}

	@Override
	public void dispose(){
		if(text!=null){
			text.dispose();
		}
		super.dispose();
	}
	@Override
	public void createPartControl(Composite parent) {
		text=new TextContainer(getViewSite());
		text.getPlugin().createContainer(parent,this);

	}

	@Override
	public void setFocus() {
		// TODO Automatisch erstellter Methoden-Stub

	}
	public boolean createLaborblatt(final Patient pat,final String[] header, final TableItem[] rows){
		Brief br=text.createFromTemplateName(Konsultation.getAktuelleKons(),"Laborblatt",Brief.LABOR,pat, null);
		if(br==null){
			return false;
		}
		Table table=rows[0].getParent();
		int cols=table.getColumnCount();
		int[] colsizes=new int[cols];
		float first=25;
		float second=10;
		if(cols>2){
			int rest=Math.round((100f-first-second)/(cols-2f));
			for(int i=2;i<cols;i++){
				colsizes[i]=rest;
			}
		}
		colsizes[0]=Math.round(first);
		colsizes[1]=Math.round(second);

		LinkedList<String[]> usedRows=new LinkedList<String[]>();
		usedRows.add(header);
		for(int i=0;i<rows.length;i++){
			boolean used=false;
			String[] row=new String[cols];
			for(int j=0;j<cols;j++){
				row[j]=rows[i].getText(j);
				if((j>1) && (row[j].length()>0)){
					used=true;
					//break;
				}
			}
			if(used==true){
				usedRows.add(row);
			}
		}	
		String[][] fld=usedRows.toArray(new String[0][]);
		return text.getPlugin().insertTable("[Laborwerte]",ITextPlugin.FIRST_ROW_IS_HEADER,fld, colsizes);
	}

	@SuppressWarnings("unchecked")
	public boolean createLaborblatt(Patient pat, Document doc){
		/*Brief br=*/text.createFromTemplateName(Konsultation.getAktuelleKons(),"Laborblatt",Brief.LABOR,pat, null);
		
	    ArrayList<String[]> rows=new ArrayList<String[]>();
	    Element root=doc.getRootElement();
	    String druckdat=root.getAttributeValue("Erstellt");
	    Element daten=root.getChild("Daten");
	    List datlist=daten.getChildren();
	    int cols=datlist.size()+1;
	    String[] firstline=new String[cols];
	    firstline[0]=druckdat;
	    for(int i=1;i<cols;i++){
	    	Element dat=(Element)datlist.get(i-1);
	    	firstline[i]=dat.getAttributeValue("Tag");
	    }
	    rows.add(firstline);
	    List groups=root.getChildren("Gruppe");
	    for(Element el:(List<Element>)groups){
	    	rows.add(new String[]{el.getAttribute("Name").getValue()});
	    	List<Element> params=el.getChildren("Parameter");
	    	for(Element param:params){
	    		Element ref=param.getChild("Referenz");
	    		String[] row=new String[cols];
	    		StringBuilder sb=new StringBuilder();
	    		sb.append(param.getAttributeValue("Name"))
	    			.append(" (").append(ref.getAttributeValue("min"))
	    			.append("-").append(ref.getAttributeValue("max")).append(") ")
	    			.append(param.getAttributeValue("Einheit"));
	    		row[0]=sb.toString();
	    		List<Element> results=param.getChildren("Resultat");
	    		int i=1;
	    		for(Element result:results){
	    			row[i++]=result.getValue();
	    		}
	    		rows.add(row);
	    	}
	    }
	    return text.getPlugin().insertTable("[Laborwerte]",ITextPlugin.FIRST_ROW_IS_HEADER,rows.toArray(new String[0][]), null);

	}
	public void save() {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	public boolean saveAs() {
		// TODO Automatisch erstellter Methoden-Stub
		return false;
	}
}
