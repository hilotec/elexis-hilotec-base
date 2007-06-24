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
import ch.elexis.exchange.XChangeExporter;
import ch.elexis.exchange.XChangeImporter;
import ch.elexis.util.Result;

public class MedicalElement extends XChangeElement{
	AnamnesisElement anamnesis;
	List<RecordElement> records;
	List<DocumentElement> documents;
	List<LabElement> analyses;
	
	public MedicalElement(XChangeExporter parent, Patient p){
		super(parent);
		e=new Element("medical",XChangeContainer.ns);
		anamnesis=new AnamnesisElement(parent,this,p);
		e.addContent(anamnesis.e);
		Element eRecords=new Element("records",XChangeContainer.ns);
		e.addContent(eRecords);
		Fall[] faelle=p.getFaelle();
		for(Fall fall:faelle){
			Konsultation[] kons=fall.getBehandlungen(false);
			for(Konsultation k:kons){
				RecordElement record=new RecordElement(parent,e,k);
				anamnesis.add(k, record);
				eRecords.addContent(record.e);
			}
		}
		
		Query<LabResult> qbe=new Query<LabResult>(LabResult.class);
		qbe.add("PatientID", "=", p.getId());
		List<LabResult> labs=qbe.execute();
		if(labs!=null){
			Element eAnalyses=new Element("analyses",XChangeContainer.ns);
			e.addContent(eAnalyses);
			for(LabResult lr:labs){
				LabElement eLab=new LabElement(parent,this,lr);
				eAnalyses.addContent(eLab.e);
			}
		}
		Query <Brief> qb=new Query<Brief>(Brief.class);
		qb.add("PatientID", "=", p.getId());
		List<Brief> lBriefe=qb.execute();
		if((lBriefe!=null) && (lBriefe.size())>0){
			Element eDocuments=new Element("documents",XChangeContainer.ns);
			e.addContent(eDocuments);
			for(Brief b:lBriefe){
				DocumentElement eDoc=new DocumentElement(parent,this,b);
				eDocuments.addContent(eDoc.e);
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
		Patient pat=Patient.load(p.getId());
		Element eAnamnesis=e.getChild("anamnesis",XChangeContainer.ns);
		if(eAnamnesis!=null){
			anamnesis=new AnamnesisElement(parent,eAnamnesis);
		}
		
		Element eRecords=e.getChild("records",XChangeContainer.ns);
		if(eRecords!=null){
			List<Element> lRecords=eRecords.getChildren("record", XChangeContainer.ns);
			records=new LinkedList<RecordElement>();
			if(lRecords!=null){
				for(Element er:lRecords){
					records.add(new RecordElement(parent,er));
				}
			}	
		}
		
		Element eDocuments=e.getChild("documents",XChangeContainer.ns);
		if(eDocuments!=null){
			List<Element> lDocuments=eDocuments.getChildren("document",XChangeContainer.ns);
			if(lDocuments!=null){
				documents=new LinkedList<DocumentElement>();
				for(Element eDoc:lDocuments){
					documents.add(new DocumentElement(parent,e));		
				}
			}
			
		}
		Element eAnalyses=e.getChild("analyses",XChangeContainer.ns);
		if(eAnalyses!=null){
			List<Element> lAnalyses=eAnalyses.getChildren("analyse", XChangeContainer.ns);
			if(lAnalyses!=null){
				analyses=new LinkedList<LabElement>();
				for(Element ea:lAnalyses){
					analyses.add(new LabElement(parent,ea,p));
				}
			}
		}
	}

}
