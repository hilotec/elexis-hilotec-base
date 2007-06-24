/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: XChangeExporter.java 2582 2007-06-23 21:11:15Z rgw_ch $
 *******************************************************************************/
package ch.elexis.exchange;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import ch.elexis.Hub;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.exchange.elements.ContactElement;
import ch.elexis.exchange.elements.MedicalElement;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * this is, at present, merely a stub for the export-personality of the Container
 * @author gerry
 *
 */
public class XChangeExporter extends XChangeContainer{
	public Document getDocument(){
		return doc;
	}
	public Element getRoot(){
		return doc.getRootElement();
	}
	
	/**
	 * Create a default xChange Document with the actually logged-in mandator as responsible. 
	 *
	 */
	@SuppressWarnings("unchecked")
	public XChangeExporter(){
		
		Element eDocument=new Element("document",ns);
		eDocument.setAttribute("version", Version);
		eDocument.setAttribute("creatorName","Elexis");
		eDocument.setAttribute("creatorID","ch.elexis");
		eDocument.setAttribute("creatorVersion",Hub.Version);
		eDocument.setAttribute("date",new TimeTool().toString(TimeTool.DATE_ISO));
		eDocument.setAttribute("id",StringTool.unique("Exchange"));
		eRoot.addContent(eDocument);
		if(Hub.actMandant==null){
			SWTHelper.showError("Kein Mandant angemeldet", "Bitte melden Sie zuerst einen Mandanten an");
			bValid=false;
		}else{
			ContactElement eResponsible=addContact(Hub.actMandant, false);
			String id=eResponsible.getElement().getAttributeValue("id");
			eDocument.setAttribute("responsible",id);
			bValid=true;
		}
	}
	
	/**
	 * Add a new Contact to the file. It will only be added, if it does not yet exist
	 * Rule for the created ID: If a Contact has a really unique ID (EAN, Unique Patient Identifier)
	 * then this shold be used. Otherwise a unique id should be generated (here we take the existing
	 * id from Elexis which is by definition already a UUID)
	 * @param k the contact to insert
	 * @param withMedical append a medical record if the contact happens to be (also) a patient.
	 * @return the Element node of the newly inserted (or earlier inserted) contact
	 */
	public ContactElement addContact(Kontakt k, boolean withMedical){
		List<Element> lContacts=eRoot.getChildren("contact", ns);
		String id=k.getId();
		for(Element e:lContacts){
			if(e.getAttributeValue("id").equals(id)){
				return new ContactElement(this,e);
			}
		}
		ContactElement contact=new ContactElement(this,k);
		eRoot.addContent(contact.getElement());
		if(withMedical && k.istPatient()){
			contact.addMedical(new MedicalElement(this,Patient.load(k.getId())));
		}
		return contact;
	}


}
