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
 *  $Id: XChangeExporter.java 2629 2007-06-24 16:31:32Z rgw_ch $
 *******************************************************************************/
package ch.elexis.exchange;

import java.util.List;

import ch.elexis.exchange.elements.ContactElement;
import ch.elexis.exchange.elements.MedicalElement;


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
				sb.append(contact.toString());
			}
		}
		return sb.toString();
	}
	
	
		
}
