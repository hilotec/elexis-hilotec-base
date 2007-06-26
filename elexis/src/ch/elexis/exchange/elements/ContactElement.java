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
 *  $Id: ContactElement.java 2636 2007-06-26 18:19:20Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;

import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.elexis.exchange.XChangeContainer;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * A Contact can contain elements of the types address, 
 * connection, and medical
 * @author Gerry
 *
 */
public class ContactElement extends XChangeElement{

	
	public ContactElement(XChangeContainer parent, Element el){
		super(parent, el);
	}

	public void add(AddressElement ae){
		super.add(ae);
	}
	public void add(ConnectionElement ce){
		super.add(ce);
	}
	public void add(ContactrefElement cre){
		super.add(cre);
	}
	public void add(MedicalElement me){
		super.add(me);
	}
	
	public List<AddressElement> getAddresses(){
		List<AddressElement> ret=new LinkedList<AddressElement>();
		for(Element el:getElements("address")){
			ret.add(new AddressElement(parent,el));
		}
		return ret;
	}
	public List<ConnectionElement> getConnections(){
		List<ConnectionElement> ret=new LinkedList<ConnectionElement>();
		for(Element el:getElements("connection")){
			ret.add(new ConnectionElement(parent,el));
		}
		return ret;
	}
	public List<ContactrefElement> getContactrefs(){
		List<ContactrefElement> ret=new LinkedList<ContactrefElement>();
		for(Element el:getElements("contactref")){
			ret.add(new ContactrefElement(parent,el));
		}
		return ret;
	}
	
	public MedicalElement getMedical(){
		Element medical=e.getChild("medical", XChangeContainer.ns);
		if(medical!=null){
			return new MedicalElement(parent,medical);	
		}
		return null;
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
		add(new AddressElement(parent, k.getAnschrift(),"default"));
		parent.callExportHooks(e, k);
		
	}

	public ContactElement(XChangeContainer parent, Patient p){
		this(parent,(Kontakt)p);
		add(new MedicalElement(parent,p));
	}
	
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append("Name\t\t\t\t").append(getAttr("lastname")).append("\n");
		sb.append("Vorname(n)\t\t\t").append(getAttr("firstname"));
		String middle=getAttr("middlename");
		if(middle.length()>0){
			sb.append(" ").append(middle);
		}
		sb.append("\nGeburtsdatum\t\t");
		TimeTool geb=new TimeTool(getAttr("birthdate"));
		sb.append(geb.toString(TimeTool.DATE_GER)).append("\n");
		sb.append("PID: ").append(getAttr("id")).append("\n\n");
		List<AddressElement> addresses=getAddresses();
		for(AddressElement adr:addresses){
			sb.append(adr.toString()).append("\n");
		}
		MedicalElement me=getMedical();
		if(me!=null){
			sb.append(me.toString());
		}
		return sb.toString();
	
	}
}
