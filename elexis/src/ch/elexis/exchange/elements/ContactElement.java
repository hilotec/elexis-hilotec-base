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
 *  $Id: ContactElement.java 4179 2008-07-25 11:01:27Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.elements;

import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;

import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.exchange.XChangeContainer;
import ch.elexis.exchange.XIDHandler;
import ch.elexis.util.Result;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * A Contact can contain elements of the types address, 
 * connection, and medical
 * @author Gerry
 *
 */
@SuppressWarnings("serial")
public class ContactElement extends XChangeElement{
	public static final String XMLNAME="contact";
	public static final String ATTR_BIRTHDATE="birthdate";
	public static final String ATTR_FIRSTNAME="firstname";
	public static final String ATTR_MIDDLENAME="middlename";
	public static final String ATTR_LASTNAME="lastname";
	public static final String ATTR_SEX="sex";
	public static final String ATTR_SALUTATION="salutation";
	public static final String ATTR_TITLE="title";
	public static final String ATTR_TYPE="type";
	public static final String ATTR_SHORTNAME="shortname";
	public static final String ELEM_XID="xid";
	public static final String ELEM_ADDRESS="address";
	public static final String VALUE_PERSON="person";
	public static final String VALUE_ORGANIZATION="organization";
	public static final String VALUE_MALE="male";
	public static final String VALUE_FEMALE="female";
	
	
	public ContactElement(XChangeContainer home){
		super(home);
	}

	public void add(AddressElement ae){
		super.add(ae);
	}
	public void add(ContactRefElement ce){
		super.add(ce);
	}
	
	public void add(MedicalElement me){
		super.add(me);
	}
	
	public List<AddressElement> getAddresses(){
		List<AddressElement> ret=new LinkedList<AddressElement>();
		for(Element el:getElements(ELEM_ADDRESS)){
			ret.add(new AddressElement(getContainer()));
		}
		return ret;
	}
	
	/**
	 * Create a ContactElement from a Kontakt
	 * @param parent
	 * @param k
	 */
	public ContactElement(XChangeContainer parent, Kontakt k){
		super(parent);
		Element eXid=parent.xidHandler.createXidElement(k, getNamespace());
		setID(eXid.getAttributeValue(XIDHandler.XID_UUID));
		addContent(eXid);
		if(k.istPerson()){
			Person p=Person.load(k.getId());
			setAttribute(ATTR_TYPE,VALUE_PERSON);
			setAttribute(ATTR_LASTNAME,p.getName());
			setAttribute(ATTR_FIRSTNAME,p.getVorname());
			if(p.getGeschlecht().startsWith("m")){
				setAttribute(ATTR_SEX,VALUE_MALE);
			}else{
				setAttribute(ATTR_SEX,VALUE_FEMALE);
			}
			String gebdat=p.getGeburtsdatum();
			if(!StringTool.isNothing(gebdat)){
				setAttribute(ATTR_BIRTHDATE,new TimeTool(gebdat).toString(TimeTool.DATE_ISO));
			}
		
		}else{
			setAttribute(ATTR_TYPE,VALUE_ORGANIZATION);
			setAttribute(ATTR_LASTNAME, k.getLabel());
		}
		add(new AddressElement(parent, k.getAnschrift(),"default"));
		
	}
	
	public List<ContactRefElement> getAssociations(){
		List<ContactRefElement> ret=new LinkedList<ContactRefElement>();
		for(Element el:getElements("connection")){
			ret.add(new ContactRefElement(getContainer()));
		}
		return ret;
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
		return sb.toString();
	}
	
	@Override
	public String getXMLName() {
		return XMLNAME;
	}

	public PersistentObject doImport(){
		Element xid=getChild(ELEM_XID);
		if(xid!=null){
			
		}
		return null;
	}
	
	
}
