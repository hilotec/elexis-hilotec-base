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
 *  $Id: ContactElement.java 2583 2007-06-23 21:14:16Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import org.jdom.Element;

import ch.elexis.data.Anschrift;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Person;
import ch.elexis.exchange.IExchangeContributor;
import ch.elexis.exchange.XChangeContainer;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class ContactElement extends XChangeElement{

	public ContactElement(XChangeContainer parent, Element el){
		super(parent, el);
	}
	public ContactElement(XChangeContainer parent){
		super(parent);
		e=new Element("contact",XChangeContainer.ns);
		
	}
	public ContactElement(XChangeContainer parent, Kontakt k){
		super(parent);
		e=new Element("contact",XChangeContainer.ns);
		e.setAttribute("id", k.getId());
		
		if(k.istPerson()){
			Person p=Person.load(k.getId());
			e.setAttribute("type","person");
			e.setAttribute("lastname",p.getName());
			e.setAttribute("firstname",p.getVorname());
			if(p.getGeschlecht().startsWith("m")){
				e.setAttribute("sex","male");
			}else{
				e.setAttribute("sex","female");
			}
			String gebdat=p.getGeburtsdatum();
			if(!StringTool.isNothing(gebdat)){
				e.setAttribute("birthdate",new TimeTool(gebdat).toString(TimeTool.DATE_ISO));
			}
		
		}else{
			e.setAttribute("type","organization");
			e.setAttribute("name", k.getLabel());
		}
		e.addContent(createAddress(k.getAnschrift(),"default"));
		parent.callExportHooks(e, k);
		
	}
	public void addMedical(MedicalElement element) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * Create an address element
	 */
	public Element createAddress(Anschrift an, String bezug){
		Element eAd=new Element("address",XChangeContainer.ns);
		eAd.setAttribute("type", bezug);
		Element eStreet=new Element("street",XChangeContainer.ns);
		eStreet.addContent(an.getStrasse());
		eAd.addContent(eStreet);
		Element eZip=new Element("zip",XChangeContainer.ns);
		eZip.addContent(an.getPlz());
		eAd.addContent(eZip);
		Element eCity=new Element("city",XChangeContainer.ns);
		eCity.addContent(an.getOrt());
		eAd.addContent(eCity);
		Element eCountry=new Element("country",XChangeContainer.ns);
		eCountry.addContent(an.getLand());
		return eAd;
	}
}
