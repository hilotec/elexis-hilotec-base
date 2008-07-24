/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: MedicalElement.java 4176 2008-07-24 19:50:11Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;

import ch.elexis.data.Brief;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.exchange.XChangeContainer;

/**
 * THis represents the medical History of a given patient
 * @author gerry
 *
 */
@SuppressWarnings("serial")
public class MedicalElement extends XChangeElement{
	public static final String XMLNAME="medical";
	private Element eRecords, eAnalyses, eDocuments, eAllergies, eMedications;
	private AnamnesisElement elAnamnesis;
	
	public String getXMLName(){
		return XMLNAME;
	}
	
	public MedicalElement(XChangeContainer parent){
		super(parent);
	}
	
	/**
	 * Create a new MedicalElement from the EMR of Patient p
	 */
	public MedicalElement(XChangeContainer parent, Patient p){
		super(parent);
		add(new AnamnesisElement(getContainer()));
		Fall[] faelle=p.getFaelle();
		for(Fall fall:faelle){
			Konsultation[] kons=fall.getBehandlungen(false);
			for(Konsultation k:kons){
				RecordElement record=new RecordElement(getContainer(),k);
				getAnamnesis().link(k, record);
				addRecord(record);
			}
		}
	
		Query<LabResult> qbe=new Query<LabResult>(LabResult.class);
		qbe.add("PatientID", "=", p.getId());
		List<LabResult> labs=qbe.execute();
		if(labs!=null){
			for(LabResult lr:labs){
				addAnalyse(new FindingElement(getContainer(),lr));
			}
		}
		
		Query <Brief> qb=new Query<Brief>(Brief.class);
		qb.add("PatientID", "=", p.getId());
		List<Brief> lBriefe=qb.execute();
		if((lBriefe!=null) && (lBriefe.size())>0){
			for(Brief b:lBriefe){
				addDocument(new DocumentElement(getContainer(),b));
			}

		}
	
	}


	public void add(AnamnesisElement ae){
		elAnamnesis=ae;
		super.add(ae);
	}

	
	/**
	 * Return or create the anamnesis-Element
	 * @return the newly created or existing anamnesis element
	 */
	public AnamnesisElement getAnamnesis(){
		if(elAnamnesis==null){
			elAnamnesis=(AnamnesisElement)getChild(AnamnesisElement.XMLNAME);
			if(elAnamnesis==null){
				elAnamnesis=new AnamnesisElement(getContainer());
			}
		}
		return elAnamnesis;
	}
	
	/**
	 * Add a medical record. Thios will create the records-parent element if neccessary
	 * @param rc the RecordElement to add
	 */
	public void addRecord(RecordElement rc){
		if(eRecords==null){
			eRecords=getChild("records");
		}
		if(eRecords==null){
			eRecords=new Element("records",getContainer().getNamespace());
			addContent(eRecords);
			getContainer().addChoice(eRecords, "KG-Einträge", eRecords);
		}
		eRecords.addContent(rc);
	}
	
	/**
	 * Add a finding 
	 * @param le
	 */
	public void addAnalyse(FindingElement le){
		if(eAnalyses==null){
			eAnalyses=getChild(FindingElement.ENCLOSING);
		}
		if(eAnalyses==null){
			eAnalyses=new Element(FindingElement.ENCLOSING,getContainer().getNamespace());
			addContent(eAnalyses);
			getContainer().addChoice(eAnalyses, "Befunde", eAnalyses);
		}
		eAnalyses.addContent(le);
	}
	
	public void addDocument(DocumentElement de){
		if(eDocuments==null){
			eDocuments=getChild("documents");
		}
		if(eDocuments==null){
			eDocuments=new Element("documents",getContainer().getNamespace());
			addContent(eDocuments);
			getContainer().addChoice(eDocuments, "Dokumente", eDocuments);
		}
		eDocuments.addContent(de);
	
	}
	
	/************************* Load methods *******************************************/
	@SuppressWarnings("unchecked")
	public List<RecordElement> getRecords(){
		List<RecordElement> ret=new LinkedList<RecordElement>();
		if(eRecords==null){
			eRecords=getChild("records");
		}
		if(eRecords!=null){
			List<Element> records=eRecords.getChildren("record", getContainer().getNamespace());
			for(Element el:records){
				ret.add(new RecordElement(getContainer()));
			}
		}
		return ret;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<FindingElement> getAnalyses(){
		List<FindingElement> ret=new LinkedList<FindingElement>();
		if(eAnalyses==null){
			eAnalyses=getChild("analyses");
		}
		if(eAnalyses!=null){
			List<Element> analyses=eAnalyses.getChildren("analyse",getContainer().getNamespace());
			for(Element el:analyses){
				ret.add(new FindingElement(getContainer()));
			}
		}
		return ret;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public List<DocumentElement> getDocuments(){
		List<DocumentElement> ret=new LinkedList<DocumentElement>();
		if(eDocuments==null){
			eDocuments=getChild("documents");
		}
		if(eDocuments!=null){
			List<Element> documents=eDocuments.getChildren("documents", getContainer().getNamespace());
			for(Element el:documents){
				ret.add(new DocumentElement(getContainer()));
			}
		}
		return ret;
	}
	
		
	public String toString(){
		StringBuilder ret=new StringBuilder();
		ret.append(getAnamnesis().toString());
		List<RecordElement> records=getRecords();
		for(RecordElement record:records){
			ret.append("\n......\n").append(record.toString());
		}
		
		return ret.toString();
	}
}
