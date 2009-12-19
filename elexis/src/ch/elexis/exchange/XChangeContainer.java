/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich, SGAM.Informatics and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 * $Id: XChangeContainer.java 5879 2009-12-19 06:05:57Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;

import ch.elexis.data.PersistentObject;
import ch.elexis.exchange.elements.ContactElement;
import ch.elexis.exchange.elements.ContactsElement;
import ch.elexis.exchange.elements.DocumentElement;
import ch.elexis.exchange.elements.EpisodeElement;
import ch.elexis.exchange.elements.FindingElement;
import ch.elexis.exchange.elements.MedicationElement;
import ch.elexis.exchange.elements.RecordElement;
import ch.elexis.exchange.elements.RiskElement;
import ch.elexis.exchange.elements.XChangeElement;
import ch.elexis.util.Extensions;
import ch.elexis.util.Log;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

public class XChangeContainer {
	private static final String PLURAL = "s"; //$NON-NLS-1$
	public static final String Version = "2.0.0"; //$NON-NLS-1$
	public static final Namespace ns =
		Namespace.getNamespace("xChange", "http://informatics.sgam.ch/xChange"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final Namespace nsxsi =
		Namespace.getNamespace("xsi", "http://www.w3.org/2001/XML Schema-instance"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final Namespace nsschema =
		Namespace.getNamespace("schemaLocation", "http://informatics.sgam.ch/xChange xchange.xsd"); //$NON-NLS-1$ //$NON-NLS-2$
	
	public static final String ROOT_ELEMENT = "xChange"; //$NON-NLS-1$
	public static final String ROOTPATH = StringTool.slash + ROOT_ELEMENT + StringTool.slash;
	
	public static final String ENCLOSE_CONTACTS = ContactElement.XMLNAME + PLURAL;
	public static final String PATIENT_ELEMENT = "patient"; //$NON-NLS-1$
	public static final String ENCLOSE_DOCUMENTS = DocumentElement.XMLNAME + PLURAL;
	public static final String ENCLOSE_RECORDS = RecordElement.XMLNAME + PLURAL;
	public static final String ENCLOSE_FINDINGS = FindingElement.XMLNAME + PLURAL;
	public static final String ENCLOSE_MEDICATIONS = MedicationElement.XMLNAME + PLURAL;
	public static final String ENCLOSE_RISKS = RiskElement.XMLNAME + PLURAL;
	public static final String ENCLOSE_EPISODES = EpisodeElement.XMLNAME + PLURAL;
	
	protected Element eRoot;
	protected static Log log = Log.get("XChange"); //$NON-NLS-1$
	
	protected HashMap<String, byte[]> binFiles = new HashMap<String, byte[]>();
	/**
	 * Collection of all UserChoices to display to tzhe user for selection
	 */
	protected HashMap<Element, UserChoice> choices = new HashMap<Element, UserChoice>();
	
	/**
	 * Mapping between element in the xChange Container to the corresponding internal data object
	 */
	private final HashMap<XChangeElement, PersistentObject> mapElementToObject =
		new HashMap<XChangeElement, PersistentObject>();
	
	/**
	 * Mapping from an internal data object to an element in the xChange Container
	 */
	private final HashMap<PersistentObject, XChangeElement> mapObjectToElement =
		new HashMap<PersistentObject, XChangeElement>();
	
	private final List<IExchangeContributor> lex =
		Extensions.getClasses("ch.elexis.Transporter", "xChangeContribution"); //$NON-NLS-1$ //$NON-NLS-2$
	
	// public abstract Kontakt findContact(String id);
	
	public List<IExchangeContributor> getXChangeContributors(){
		return lex;
	}
	/**
	 * Map a database object to an xChange container element and vice versa
	 * 
	 * @param element
	 *            the Element
	 * @param obj
	 *            the Object
	 */
	public void addMapping(XChangeElement element, PersistentObject obj){
		mapElementToObject.put(element, obj);
		mapObjectToElement.put(obj, element);
	}
	
	/**
	 * Return the database Object that maps to a specified Element
	 * 
	 * @param element
	 *            the Element
	 * @return the object or null if no such mapping exists
	 */
	public PersistentObject getMapping(XChangeElement element){
		return mapElementToObject.get(element);
	}
	
	/**
	 * return the Container Element that is mapped to a specified database object
	 * 
	 * @param obj
	 *            the object
	 * @return the element or null if no such mapping exists
	 */
	public XChangeElement getMapping(PersistentObject obj){
		return mapObjectToElement.get(obj);
	}
	
	/**
	 * Retrieve the UserChoice attributed to a given Element
	 * 
	 * @param key
	 *            teh element
	 * @return the UserChoice or null if no such UserChoice exists
	 */
	public UserChoice getChoice(XChangeElement key){
		return choices.get(key.getElement());
	}
	
	public UserChoice getChoice(Element key){
		return choices.get(key);
	}
	
	public void addChoice(Element key, String name){
		choices.put(key, new UserChoice(true, name, key));
	}
	
	public void addChoice(XChangeElement key, String name){
		choices.put(key.getElement(), new UserChoice(true, name, key));
	}
	
	public ContactsElement getContactsElement(){
		Element ec = eRoot.getChild(ENCLOSE_CONTACTS, ns);
		ContactsElement eContacts = new ContactsElement();
		if (ec == null) {
			eRoot.addContent(eContacts.getElement());
			choices.put(eContacts.getElement(), new UserChoice(true,
				Messages.XChangeContainer_kontakte, eContacts));
		} else {
			eContacts.setElement(ec);
		}
		return eContacts;
	}
	
	public List<Element> getContactElements(){
		return getElements(ROOTPATH + ENCLOSE_CONTACTS + StringTool.slash + ContactElement.XMLNAME);
	}
	
	/**
	 * Find the registered Data handler that matches best the given element
	 * 
	 * @param el
	 *            Element o be imported
	 * @return the best matching handler or null if no handler exists at all for the given data type
	 */
	@SuppressWarnings("unchecked")
	public IExchangeDataHandler findImportHandler(XChangeElement el){
		IExchangeDataHandler ret = null;
		int matchedRestrictions = 0;
		for (IExchangeContributor iex : lex) {
			IExchangeDataHandler[] handlers = iex.getImportHandlers();
			for (IExchangeDataHandler cand : handlers) {
				if (cand.getDatatype().equalsIgnoreCase(el.getXMLName())) {
					if (ret == null) {
						ret = cand;
					}
					String[] restrictions = cand.getRestrictions();
					if (restrictions != null) {
						int matches = 0;
						for (String r : restrictions) {
							try {
								XPath xpath = XPath.newInstance(r);
								List<Object> nodes = xpath.selectNodes(el);
								if (nodes.size() > 0) {
									if (++matches > matchedRestrictions) {
										ret = cand;
										matchedRestrictions = matches;
									} else if (matches == matchedRestrictions) {
										if (ret.getValue() < cand.getValue()) {
											ret = cand;
										}
									}
								}
							} catch (JDOMException e) {
								ExHandler.handle(e);
								log.log("Parse error JDOM " + e.getMessage(), Log.WARNINGS); //$NON-NLS-1$
							}
						}
						
					} else {
						if (ret.getValue() < cand.getValue()) {
							ret = cand;
						}
					}
					
				}
			}
		}
		return ret;
	}
	
	
	/**
	 * get a binary content from the Container
	 * 
	 * @param id
	 *            id of the content
	 * @return the content or null if no such content exists
	 */
	public byte[] getBinary(String id){
		return binFiles.get(id);
	}
	
	public void addChoice(XChangeElement e, String name, Object o){
		choices.put(e.getElement(), new UserChoice(true, name, o));
	}
	
	public void addChoice(Element e, String name, Object o){
		choices.put(e, new UserChoice(true, name, o));
	}
	
	/**
	 * Get the root element.
	 * 
	 * @return the root element
	 */
	public Element getRoot(){
		return eRoot;
	}
	
	/**
	 * Retrieve a List of all Elements with a given Name at a given path
	 * 
	 * @param path
	 *            a string of the form /element1/element2/name will get all Elements with "name" in
	 *            the body of element2. If name is *, will retrieve all Children of element2. Path
	 *            must begin at root level.
	 * @return a possibly empty list af all matching elements at the given position
	 */
	@SuppressWarnings("unchecked")
	public List<Element> getElements(String path){
		LinkedList<Element> ret = new LinkedList<Element>();
		String[] trace = path.split(StringTool.slash);
		Element runner = eRoot;
		for (int i = 2; i < trace.length - 1; i++) {
			runner = runner.getChild(trace[i], ns);
			if (runner == null) {
				return ret;
			}
		}
		String name = trace[trace.length - 1];
		if (trace.equals("*")) { //$NON-NLS-1$
			return runner.getChildren();
		}
		return runner.getChildren(name, ns);
	}
	
	public Namespace getNamespace(){
		return ns;
	}
	
	/**
	 * get an Iterator over all binary contents of this Container
	 */
	public Iterator<Entry<String, byte[]>> getBinaries(){
		return binFiles.entrySet().iterator();
	}
	
	/*
	 * public List<Object> getSelectedChildren(Tree<UserChoice> tSelection){ List<Object> ret=new
	 * LinkedList<Object>(); for(Tree<UserChoice> runner:tSelection.getChildren()){ UserChoice
	 * choice=runner.contents; if(choice.isSelected()){ ret.add(choice.object); } } return ret; }
	 */
	
	/**
	 * Set any implementation-spezific configuration
	 * 
	 * @param props
	 */
	public void setConfiguration(Properties props){
		this.props = props;
	}
	
	/**
	 * Set a named property for this container
	 * 
	 * @param name
	 *            the name of the property
	 * @param value
	 *            the value for the property
	 */
	public void setProperty(String name, String value){
		if (props == null) {
			props = new Properties();
		}
		props.setProperty(name, value);
	}
	
	public String getProperty(String name){
		if (props == null) {
			props = new Properties();
		}
		return props.getProperty(name);
	}
	
	protected Properties getProperties(){
		return props;
	}
	
	protected Properties props;
	
	/**
	 * A UserChoice contains the information, whether the user selected the associated object for
	 * transfer
	 * 
	 * @author gerry
	 * 
	 */
	public static class UserChoice {
		boolean bSelected;
		String title;
		Object object;
		
		/**
		 * Include the object in the transfer
		 * 
		 * @param bSelection
		 */
		public void select(boolean bSelection){
			bSelected = bSelection;
		}
		
		/**
		 * tell wether the object is selected for transfer
		 * 
		 * @return
		 */
		public boolean isSelected(){
			return bSelected;
		}
		
		/**
		 * get the Title to display to the user when asked for selection
		 * 
		 * @return
		 */
		public String getTitle(){
			return title;
		}
		
		/**
		 * Get the associated object
		 * 
		 * @return
		 */
		public Object getObject(){
			return object;
		}
		
		/**
		 * Create a new UserChoice
		 * 
		 * @param bSelected
		 *            true if initially selected
		 * @param title
		 *            title to display to the user in selection form
		 * @param object
		 *            the object to select for transfer
		 */
		public UserChoice(boolean bSelected, String title, Object object){
			this.bSelected = bSelected;
			this.title = title;
			this.object = object;
		}
	}
	
}
