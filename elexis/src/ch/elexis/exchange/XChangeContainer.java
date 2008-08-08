package ch.elexis.exchange;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import org.jdom.Element;
import org.jdom.Namespace;

import ch.elexis.data.BezugsKontakt;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.exchange.elements.ContactElement;
import ch.elexis.exchange.elements.ContactRefElement;
import ch.elexis.exchange.elements.DocumentElement;
import ch.elexis.exchange.elements.FindingElement;
import ch.elexis.exchange.elements.MedicalElement;
import ch.elexis.exchange.elements.MedicationElement;
import ch.elexis.exchange.elements.RecordElement;
import ch.elexis.exchange.elements.RiskElement;
import ch.elexis.exchange.elements.XChangeElement;
import ch.elexis.exchange.elements.XidElement;
import ch.elexis.util.Extensions;


public abstract class XChangeContainer implements IDataSender, IDataReceiver{
	public static final String Version="1.0.2";
	public static final Namespace ns=Namespace.getNamespace("xChange","http://informatics.sgam.ch/xChange");
	public static final Namespace nsxsi=Namespace.getNamespace("xsi","http://www.w3.org/2001/XML Schema-instance");
	public static final Namespace nsschema=Namespace.getNamespace("schemaLocation","http://informatics.sgam.ch/xChange xchange.xsd");

	public static final String ROOT_ELEMENT="xChange";
	public static final String ROOTPATH="/"+ROOT_ELEMENT+"/";
	
	public static final String ENCLOSE_CONTACTS=ContactElement.XMLNAME+"s";
	public static final String PATIENT_ELEMENT="patient";
	public static final String ENCLOSE_DOCUMENTS=DocumentElement.XMLNAME+"s";
	public static final String ENCLOSE_RECORDS=RecordElement.XMLNAME+"s";
	public static final String ENCLOSE_FINDINGS=FindingElement.XMLNAME+"s";
	public static final String ENCLOSE_MEDICATIONS=MedicationElement.XMLNAME+"s";
	public static final String ENCLOSE_RISKS=RiskElement.XMLNAME+"s";
	public static final String ENCLOSE_EPISODES="anamnesis";
	
	
	protected Element eRoot;
	protected HashMap<String,byte[]> binFiles=new HashMap<String,byte[]>();
	protected HashMap<Element,UserChoice> choices=new HashMap<Element,UserChoice>();
	
	private HashMap<XChangeElement,PersistentObject> mapElementToObject=new HashMap<XChangeElement, PersistentObject>();
	private HashMap<PersistentObject,XChangeElement> mapObjectToElement=new HashMap<PersistentObject, XChangeElement>();
	
	protected List<IExchangeContributor> lex=Extensions.getClasses("ch.elexis.Transporter", "xChangeContribution");

	public abstract Kontakt findContact(String id);
	
	
	/**
	 * Add a new Contact to the file. It will only be added, if it does not yet exist
	 * Rule for the created ID: If a Contact has a really unique ID (EAN, Unique Patient Identifier)
	 * then this shold be used. Otherwise a unique id should be generated (here we take the existing
	 * id from Elexis which is by definition already a UUID)
	 * @param k the contact to insert
	 * @return the Element node of the newly inserted (or earlier inserted) contact
	 */
	@SuppressWarnings("unchecked")
	public ContactElement addContact(Kontakt k){
		Element eContacts=eRoot.getChild(ENCLOSE_CONTACTS, ns);
		if(eContacts==null){
			eContacts=new Element(ENCLOSE_CONTACTS,ns);
			eRoot.addContent(eContacts);
			choices.put(eContacts, new UserChoice(true,"Kontakte",eContacts));
		}else{
			List<ContactElement> lContacts=eContacts.getChildren(ContactElement.XMLNAME, ns);
			for(ContactElement e:lContacts){
				XidElement xid=e.getXid();
				if( (xid!=null) && (xid.match(k)==XidElement.XIDMATCH.SURE)){
					e.setContainer(this);
					return e;	
				}
			}
		}
		ContactElement contact=new ContactElement(this,k);
		eContacts.addContent(contact);
		choices.put(contact, new UserChoice(true,k.getLabel(),k));
		return contact;
	}
	
	public void addMapping(XChangeElement element, PersistentObject obj){
		mapElementToObject.put(element, obj);
		mapObjectToElement.put(obj,element);
	}
	
	
	public PersistentObject getMapping(XChangeElement element){
		return mapElementToObject.get(element);
	}
	
	public XChangeElement getMapping(PersistentObject obj){
		return mapObjectToElement.get(obj);
	}
	
	public UserChoice getChoice(Element key){
		return choices.get(key);
	}
	public void addChoice(Element key, String name){
		choices.put(key, new UserChoice(true,name,key));
	}

	public ContactElement addPatient(Patient pat){
		ContactElement ret=addContact(pat);
		List<BezugsKontakt> bzl=pat.getBezugsKontakte();
		for(BezugsKontakt bz:bzl){
			ret.add(new ContactRefElement(this,bz));
		}
		MedicalElement eMedical=new MedicalElement(this,pat);
		ret.add(eMedical);
		choices.put(eMedical, new UserChoice(true,"Krankengeschichte",eMedical));
		for(IExchangeContributor iex:lex){
			iex.exportHook(eMedical);
		}
		return ret;
	}

	public List<ContactElement> getContactElements(){
		return (List<ContactElement>)getElements(ROOTPATH+ENCLOSE_CONTACTS+"/"+ContactElement.XMLNAME);
	}
	
	
	/**
	 * Add a binary content to the Container
	 * @param id a unique identifier for the content
	 * @param contents the content
	 */
	public void addBinary(String id, byte[] contents){
		binFiles.put(id, contents);
	}
	
	/**
	 * get a binary content from the Container
	 * @param id id of the content
	 * @return the content or null if no such content exists
	 */
	public byte[] getBinary(String id){
		return binFiles.get(id);
	}

	public void addChoice(Element e,String name, Object o){
		choices.put(e, new UserChoice(true,name,o));
	}
	
	/**
	 * Get the root element.
	 * @return the root element
	 */
	public Element getRoot(){
		return eRoot;
	}
	
	/**
	 * Retrieve a List of all Elements with a given Name at a given path
	 * @param path a string of the form /element1/element2/name will get all Elements with "name" in the body of
	 * element2. If name is *, will retrieve all Children of element2. Path must begin at root level.
	 * @return a possibly empty list af all matching elements at the given position
	 */
	@SuppressWarnings("unchecked")
	public List<? extends XChangeElement> getElements(String path){
		LinkedList<XChangeElement> ret=new LinkedList<XChangeElement>();
		String[] trace=path.split("/");
		Element runner=eRoot;
		for(int i=2;i<trace.length-1;i++){
			runner=runner.getChild(trace[i], ns);
			if(runner==null){
				return ret;
			}
		}
		String name=trace[trace.length-1];
		if(trace.equals("*")){
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
	public List<Object> getSelectedChildren(Tree<UserChoice> tSelection){
		List<Object> ret=new LinkedList<Object>();
		for(Tree<UserChoice> runner:tSelection.getChildren()){
			UserChoice choice=runner.contents;
			if(choice.isSelected()){
				ret.add(choice.object);
			}
		}
		return ret;
	}
	*/
	
	/**
	 * Set any implementation-spezific configuration
	 * @param props
	 */
	public void setConfiguration(Properties props){
		this.props=props;
	}
	
	/**
	 * Set a named property for this container
	 * @param name the name of the property
	 * @param value the value for the property
	 */
	public void setProperty(String name, String value){
		if(props==null){
			props=new Properties();
		}
		props.setProperty(name, value);
	}
	
	public String getProperty(String name){
		if(props==null){
			props=new Properties();
		}
		return props.getProperty(name);
	}
	protected Properties getProperties(){
		return props;
	}
	protected Properties props;
	public static class UserChoice{
		boolean bSelected;
		String title;
		Object object;
		
		public void select(boolean bSelection){
			bSelected=bSelection;
		}
		public boolean isSelected(){
			return bSelected;
		}
		public String getTitle(){
			return title;
		}
		public Object getObject(){
			return object;
		}
		public UserChoice(boolean bSelected, String title, Object object){
			this.bSelected=bSelected;
			this.title=title;
			this.object=object;
		}
	}
}
