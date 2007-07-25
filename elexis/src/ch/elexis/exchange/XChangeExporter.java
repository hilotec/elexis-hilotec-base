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
 *  $Id: XChangeExporter.java 2912 2007-07-25 12:27:46Z rgw_ch $
 *******************************************************************************/
package ch.elexis.exchange;

import java.util.List;

import ch.elexis.data.PersistentObject;
import ch.elexis.exchange.elements.ContactElement;
import ch.elexis.exchange.elements.MedicalElement;


/**
 * this is, at present, merely a stub for the export-personality of the Container
 * @author gerry
 *
 */
public abstract class XChangeExporter extends XChangeContainer implements IDataSender{
	List<ContactElement> contacts;
	
	public boolean canHandle(final Class<? extends PersistentObject> clazz) {
		return true;
	}

	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder();
		contacts=getContacts();
		ContactElement first=null;
		for(ContactElement contact:contacts){
			MedicalElement me=contact.getMedical();
			if(me!=null){
				first=contact;
				sb.append(contact.toString());
			}
		}
		sb.append("\nBezugskontakte: \n");
		for(ContactElement contact:contacts){
			if(contact.equals(first)){
				continue;
			}
			sb.append(contact.toString());
		}
		return sb.toString();
	}
	
	
		
}
