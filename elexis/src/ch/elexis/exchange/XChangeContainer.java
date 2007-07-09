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
 *  $Id: XChangeContainer.java 2765 2007-07-09 10:47:39Z rgw_ch $
 *******************************************************************************/
package ch.elexis.exchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.omg.CORBA.INVALID_ACTIVITY;

import ch.elexis.Hub;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Organisation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.exchange.elements.ContactElement;
import ch.elexis.exchange.elements.MedicalElement;
import ch.elexis.util.Extensions;
import ch.elexis.util.Result;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

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
@SuppressWarnings("unchecked")
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

	/**
	 * Create a new, empty container
	 *
	 */
	public XChangeContainer(){
		doc=new Document();
		eRoot=new Element("xChange",ns);
		eRoot.addNamespaceDeclaration(nsxsi);
		eRoot.addNamespaceDeclaration(nsschema);
		doc.setRootElement(eRoot);
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
	
	public boolean isValid(){
		return bValid;
	}
	public Document getDocument(){
		return doc;
	}
	public Element getRoot(){
		return doc.getRootElement();
	}
	
	@SuppressWarnings("unchecked")
	List<ContactElement> getContacts(){
		List<ContactElement> ret=new LinkedList<ContactElement>();
		List<Element> contacts=getRoot().getChildren("contact", ns);
		for(Element el:contacts){
			ret.add(new ContactElement(this,el));
		}
		return ret;
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
			if(ret.existence()>PersistentObject.INVALID_ID){
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
			contact.add(new MedicalElement(this,Patient.load(k.getId())));
		}
		return contact;
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
	
	Result<String> importDocuments(Patient p, Element ed){
		List<Element> eDocs=ed.getChildren("document", ns);
		Result<String> ret=new Result<String>("OK");
		if(eDocs!=null){
			
		}
		return ret;
	}
	
	public String mapExtToIntID(String idExt){
		String idInt=idMap.get(idExt);
		return idInt;
	}
	
	public void putExtToIntIDMapping(String idExt, String idInt){
		idMap.put(idExt,idInt);
	}
	
}
