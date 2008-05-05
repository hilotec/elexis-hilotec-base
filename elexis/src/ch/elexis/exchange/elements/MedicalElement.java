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
 *  $Id: RecordElement.java 1279 2006-11-14 12:35:44Z rgw_ch $
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
import ch.elexis.exchange.XChangeImporter;

public class MedicalElement extends XChangeElement{
	private Element eRecords, eAnalyses, eDocuments, eAllergies, eMedications;
	private AnamnesisElement elAnamnesis; 
	
	public MedicalElement(XChangeContainer parent, Element el){
		super(parent,el);
	}
	
	public void add(AnamnesisElement ae){
		elAnamnesis=ae;
		super.add(ae);
	}

	public AnamnesisElement getAnamnesis(){
		if(elAnamnesis==null){
			Element eAnamnesis=e.getChild("anamnesis", XChangeContainer.ns);
			if(eAnamnesis==null){
				elAnamnesis=new AnamnesisElement(this);
			}else{
				elAnamnesis=new AnamnesisElement(this,eAnamnesis);
			}
		}
		return elAnamnesis;
	}
	public void addRecord(RecordElement rc){
		if(eRecords==null){
			eRecords=e.getChild("records", XChangeContainer.ns);
		}
		if(eRecords==null){
			eRecords=new Element("records",XChangeContainer.ns);
			e.addContent(eRecords);
		}
		eRecords.addContent(rc.getElement());
	}
	
	@SuppressWarnings("unchecked")
	public List<RecordElement> getRecords(){
		List<RecordElement> ret=new LinkedList<RecordElement>();
		if(eRecords==null){
			eRecords=e.getChild("records", XChangeContainer.ns);
		}
		if(eRecords!=null){
			List<Element> records=eRecords.getChildren("record", XChangeContainer.ns);
			for(Element el:records){
				ret.add(new RecordElement(parent,el));
			}
		}
		return ret;
	}
	
	public void addAnalyse(LabElement le){
		if(eAnalyses==null){
			eAnalyses=e.getChild("analyses", XChangeContainer.ns);
		}
		if(eAnalyses==null){
			eAnalyses=new Element("analyses",XChangeContainer.ns);
			e.addContent(eAnalyses);
		}
		eAnalyses.addContent(le.getElement());
	}
	
	@SuppressWarnings("unchecked")
	public List<LabElement> getAnalyses(){
		List<LabElement> ret=new LinkedList<LabElement>();
		if(eAnalyses==null){
			eAnalyses=e.getChild("analyses", XChangeContainer.ns);
		}
		if(eAnalyses!=null){
			List<Element> analyses=eAnalyses.getChildren("analyse", XChangeContainer.ns);
			for(Element el:analyses){
				ret.add(new LabElement(parent,el));
			}
		}
		return ret;
	}
	
	public void addDocument(DocumentElement de){
		if(eDocuments==null){
			eDocuments=e.getChild("documents", XChangeContainer.ns);
		}
		if(eDocuments==null){
			eDocuments=new Element("documents",XChangeContainer.ns);
			e.addContent(eDocuments);
		}
		eDocuments.addContent(de.getElement());
	
	}
	
	@SuppressWarnings("unchecked")
	public List<DocumentElement> getDocuments(){
		List<DocumentElement> ret=new LinkedList<DocumentElement>();
		if(eDocuments==null){
			eDocuments=e.getChild("documents", XChangeContainer.ns);
		}
		if(eDocuments!=null){
			List<Element> documents=eDocuments.getChildren("documents", XChangeContainer.ns);
			for(Element el:documents){
				ret.add(new DocumentElement(parent,el));
			}
		}
		return ret;
	}
	
	/**
	 * Create a new MedicalElement from the EMR of Patient p
	 */
	public MedicalElement(XChangeContainer parent, Patient p){
		super(parent);
		e=new Element("medical",XChangeContainer.ns);
		add(new AnamnesisElement(this));
		Fall[] faelle=p.getFaelle();
		for(Fall fall:faelle){
			Konsultation[] kons=fall.getBehandlungen(false);
			for(Konsultation k:kons){
				RecordElement record=new RecordElement(parent,k);
				getAnamnesis().link(k, record);
				addRecord(record);
			}
		}
		
		Query<LabResult> qbe=new Query<LabResult>(LabResult.class);
		qbe.add("PatientID", "=", p.getId());
		List<LabResult> labs=qbe.execute();
		if(labs!=null){
			for(LabResult lr:labs){
				addAnalyse(new LabElement(parent,lr));
			}
		}
		Query <Brief> qb=new Query<Brief>(Brief.class);
		qb.add("PatientID", "=", p.getId());
		List<Brief> lBriefe=qb.execute();
		if((lBriefe!=null) && (lBriefe.size())>0){
			for(Brief b:lBriefe){
				addDocument(new DocumentElement(parent,b));
			}

		}

	}

	/**
	 * Import the MedicalElement e from parent into Patient p
	 * @param parent the Importer
	 * @param e the actual element to import from
	 * @param p the patient to import to
	 */
	public MedicalElement(XChangeImporter parent, Element e, Patient p){
		super(parent,e);
		p.set("istPatient", "1");
		//Patient pat=Patient.load(p.getId());
				
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
