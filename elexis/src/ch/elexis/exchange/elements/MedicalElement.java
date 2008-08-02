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
 *  $Id: MedicalElement.java 4224 2008-08-02 19:12:53Z rgw_ch $
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
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Query;
import ch.elexis.exchange.XChangeContainer;
import ch.rgw.tools.StringTool;

/**
 * THis represents the medical History of a given patient
 * @author gerry
 *
 */
@SuppressWarnings("serial")
public class MedicalElement extends XChangeElement{
	public static final String XMLNAME="medical";
	private Element eRecords, eAnalyses, eDocuments, eRisks, eMedications;
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
				ResultElement.addResult(this, lr);
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
		Prescription[] medis=p.getFixmedikation();
		for(Prescription medi:medis){
			add(new MedicationElement(getContainer(),medi));
		}
		String risks=p.get("Risiken");
		if(!StringTool.isNothing(risks)){
			for(String r:risks.split("[\\n\\r]+")){
				add(new RiskElement(getContainer(),r));
			}
		}
		risks=p.get("Allergien");
		if(!StringTool.isNothing(risks)){
			for(String r:risks.split("[\\n\\r]+")){
				add(new RiskElement(getContainer(),r));
			}
		}
	}


	public void add(AnamnesisElement ae){
		elAnamnesis=ae;
		super.add(ae);
	}

	public void add(RiskElement re){
		if(eRisks==null){
			eRisks=new Element(XChangeContainer.ENCLOSE_RISKS,getContainer().getNamespace());
			addContent(eRisks);
			getContainer().addChoice(eRisks, "Risiken");
		}
		eRisks.addContent(re);
	}
	
	public void add(MedicationElement med){
		if(eMedications==null){
			eMedications=new Element(XChangeContainer.ENCLOSE_MEDICATIONS,getContainer().getNamespace());
			getContainer().addChoice(eMedications, "Medikamente");
			addContent(eMedications);
		}
		eMedications.addContent(med);
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
	 * Add a medical record. This will create the records-parent element if neccessary
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
	 * Add a result 
	 * @param le
	 */
	public void addAnalyse(ResultElement le){
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
	
	public void addFindingItem(FindingElement fe){
		if(eAnalyses==null){
			eAnalyses=getChild(FindingElement.ENCLOSING);
		}
		if(eAnalyses==null){
			eAnalyses=new Element(FindingElement.ENCLOSING,getContainer().getNamespace());
			addContent(eAnalyses);
			getContainer().addChoice(eAnalyses, "Befunde", eAnalyses);
		}
		eAnalyses.addContent(fe);
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
			eRecords=getChild(XChangeContainer.ENCLOSE_RECORDS);
		}
		if(eRecords!=null){
			List<RecordElement> records=eRecords.getChildren(RecordElement.XMLNAME, getContainer().getNamespace());
			for(RecordElement el:records){
				el.setContainer(getContainer());
				ret.add(el);
			}
		}
		return ret;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<FindingElement> getAnalyses(){
		List<FindingElement> ret=new LinkedList<FindingElement>();
		if(eAnalyses==null){
			eAnalyses=getChild(XChangeContainer.ENCLOSE_FINDINGS);
		}
		if(eAnalyses!=null){
			List<FindingElement> analyses=eAnalyses.getChildren(FindingElement.XMLNAME,getContainer().getNamespace());
			for(FindingElement el:analyses){
				el.setContainer(getContainer());
				ret.add(el);
			}
		}
		return ret;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public List<DocumentElement> getDocuments(){
		List<DocumentElement> ret=new LinkedList<DocumentElement>();
		if(eDocuments==null){
			eDocuments=getChild(XChangeContainer.ENCLOSE_DOCUMENTS);
		}
		if(eDocuments!=null){
			List<DocumentElement> documents=eDocuments.getChildren(DocumentElement.XMLNAME, getContainer().getNamespace());
			for(DocumentElement el:documents){
				el.setContainer(getContainer());
				ret.add(el);
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
	
	/**
	 * Load medical data from xchange-file into patient
	 * @param context the Patient 
	 * @return the patient
	 */
	public PersistentObject doImport(PersistentObject context){
		Patient pat=Patient.load(context.getId());
		AnamnesisElement elAnamnesis=getAnamnesis();
		List<RecordElement> records=getRecords();
		
		return pat;
	}
}
