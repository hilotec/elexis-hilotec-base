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
 *  $Id: XChangeContainer.java 2582 2007-06-23 21:11:15Z rgw_ch $
 *******************************************************************************/
package ch.elexis.exchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import ch.elexis.data.Kontakt;
import ch.elexis.data.Organisation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.exchange.elements.ContactElement;
import ch.elexis.exchange.elements.MedicalElement;
import ch.elexis.util.Extensions;

/**
 * The Container is a Java representation of an Sgam.xChange-archive. An xChange archive consists of an XML-document conforming 
 * to xChange.xsd, and an arbitrary number of accompanying binary files.
 * The Container class has methods for importing data into the elexis database, and for exporting data from the
 * elexis database into an xChange-document. It does not, however, make any assumptions on the kind or format of the resulting 
 * export of its content. Subclasses must implement doExport() to convert the container's content into the desired transport form and 
 * may extend doImport() to convert an external Form into the JDom-Document. However, we rather recommend using the generic 
 * importer mechanism (Extension Point ch.elexis.FremdDatenImport) and call Container#doImport from there.
 * @author gerry
 *
 */
public class XChangeContainer {
public static final String Version="0.2.0";
	
	public static final Namespace ns=Namespace.getNamespace("SgamXChange","http://informatics.sgam.ch/eXChange");
	public static final Namespace nsxsi=Namespace.getNamespace("xsi","http://www.w3.org/2001/XML Schema-instance");
	public static final Namespace nsschema=Namespace.getNamespace("schemaLocation","http://informatics.sgam.ch/eXChange SgamXChange.xsd");
	protected List<IExchangeContributor> lex=Extensions.getClasses("ch.elexis.ExchangeContribution", "class");;
	protected HashMap<String, String> idMap=new HashMap<String,String>();
	protected HashMap<String,byte[]> binFiles=new HashMap<String,byte[]>();
	protected Document doc;
	protected Element eRoot;
	protected boolean bValid;
	protected List<ContactElement> contacts=new ArrayList<ContactElement>();

	public XChangeContainer(){
		doc=new Document();
		eRoot=new Element("EMR",ns);
		eRoot.addNamespaceDeclaration(nsxsi);
		eRoot.addNamespaceDeclaration(nsschema);
		doc.setRootElement(eRoot);
	}
	
	public boolean isValid(){
		return bValid;
	}
	
	
	/**
	 * check whether a given &lt;contact&gt;-Element exists already as Kontakt. If not, create it
	 * @param iex an id of a &lt;contact&gt;-Element in an xChange file
	 */
	public Kontakt findContact(String iex){
		String id=idMap.get(iex);				// Mapping externe auf interne id
		Kontakt ret;
		if(id!=null){	
			ret=Kontakt.load(id);
			if(ret.exists()){
				return ret;						// Object existiert -> zurückgeben
			}
		}			// Sonst nachsehen, ob <contakt> mit dieser ID im File existiert
		List<Element> lContacts=eRoot.getChildren("contact",ns);
		for(Element e:lContacts){
			if(e.getAttributeValue("id").equals(iex)){	// Gefunden, Kontakt erstellen
				return createKontakt(e);
			}
		}
		return null;
	}
	
	private Kontakt createKontakt(Element e){
		Kontakt ret;
		if(e.getAttributeValue("type").equals("person")){
			String name=e.getAttributeValue("lastname");
			String vorname=e.getAttributeValue("firstname");
			String gebdat=e.getAttributeValue("birthdate");
			String geschlecht=e.getAttributeValue("sex").equals("male") ? "m" : "w";
			ret=new Person(name,vorname,gebdat,geschlecht);
		}else{
			String name=e.getAttributeValue("name");
			ret=new Organisation(name,"");
		}
		idMap.put(e.getAttributeValue("id"),ret.getId());		// Mapping eintragen
		return ret;
	}
	
	public boolean callExportHooks(Element e, PersistentObject o){
		for(IExchangeContributor iex:lex){
			iex.exportHook(this, e, o);
		}
		return true;
	}
	
	public boolean callImportHooks(Element e, PersistentObject o){
		for(IExchangeContributor iex:lex){
			iex.importHook(this, e, o);
		}
		return true;
	}
	/**
	 * get an Iterator over all binary contents of this Container
	 */
	public Iterator<Entry<String, byte[]>> getBinaries(){
		return binFiles.entrySet().iterator();
	}
	/**
	 * Add a binary content to the Container
	 * @param id a unique identifier for the content
	 * @param contents the content
	 */
	public void addBinary(String id, byte[] contents){
		binFiles.put(id,contents);
	}
	
	/**
	 * get a binary content from the Container
	 * @param id id of the content
	 * @return the content or null if no such content exists
	 */
	public byte[] getBinary(String id){
		return binFiles.get(id);
	}
	
	public String mapExtToIntID(String idExt){
		String idInt=idMap.get(idExt);
		return idInt;
	}
	
	public void putExtToIntIDMapping(String idExt, String idInt){
		idMap.put(idExt,idInt);
	}
}
