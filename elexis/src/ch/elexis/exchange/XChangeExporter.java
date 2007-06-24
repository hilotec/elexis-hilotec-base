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
 *  $Id: XChangeExporter.java 2627 2007-06-24 14:23:27Z rgw_ch $
 *******************************************************************************/
package ch.elexis.exchange;

import java.util.List;

import ch.elexis.exchange.elements.AddressElement;
import ch.elexis.exchange.elements.ContactElement;
import ch.elexis.exchange.elements.MedicalElement;
import ch.rgw.tools.TimeTool;


/**
 * this is, at present, merely a stub for the export-personality of the Container
 * @author gerry
 *
 */
public abstract class XChangeExporter extends XChangeContainer implements IDataSender{
	List<ContactElement> contacts;
	
	public boolean canHandle(Class clazz) {
		return true;
	}

	public String toString(){
		StringBuilder sb=new StringBuilder();
		contacts=getContacts();
		for(ContactElement contact:contacts){
			MedicalElement me=contact.getMedical();
			if(me!=null){
				String patientData=writeContact(contact);
			}
		}
		
		return sb.toString();
	}
	
	private String writeContact(ContactElement c){
		StringBuilder sb=new StringBuilder();
		sb.append("Name\t\t").append(c.getAttr("lastname")).append("\n");
		sb.append("Vorname(n)\t\t").append(c.getAttr("firstname"));
		String middle=c.getAttr("middlename");
		if(middle.length()>0){
			sb.append(" ").append(middle);
		}
		sb.append("\nGeburtsdatum\t\t");
		TimeTool geb=new TimeTool(c.getAttr("birthdate"));
		sb.append(geb.toString(TimeTool.DATE_GER)).append("\n");
		sb.append("PID: ").append(c.getAttr("id")).append("\n\n");
		List<AddressElement> addresses=c.getAddresses();
		for(AddressElement adr:addresses){
			
		}
		return sb.toString();
	}
}
