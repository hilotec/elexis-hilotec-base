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
 *  $Id: Samdas.java 2897 2007-07-24 20:12:10Z rgw_ch $
 *******************************************************************************/

package ch.elexis.text;

import java.io.CharArrayReader;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import ch.rgw.tools.TimeTool;
/**
 * Ganz bescheiden: (S)tandard für den (A)ustausch (m)edizinischer (Da)ten in der (Schweiz) -> SAmDaS.
 * In Ermangelung eines besseren Standards sei dieser Name gestattet ;-)
 * Samdas ist ein XML-Schema, das die Übertragung medizinischer Krankengeschichten (Elecronic medical record, EMR)
 * zwischen verschiedenen Endanwendungen ermöglicht.
 * Diese Klasse ist ein API dafür
 * @author Gerry
 *
 */
public class Samdas {
	public static Namespace ns=Namespace.getNamespace("samdas","http://www.elexis.ch/XSD");
	public static Namespace nsxsi=Namespace.getNamespace("xsi","http://www.w3.org/2001/XML Schema-instance");
	public static Namespace nsschema=Namespace.getNamespace("schemaLocation","http://www.elexis.ch/XSD EMR.xsd");
	
	private Document doc;
	private Element eRoot;
	
	/**
	 * Der String-Konstruktor erstellt ein Samdas aus der XML-Repräsentation. Wenn diese ungültig ist, wird
	 * ein Default-Dokument erstellt. 
	 * @param input eine XML-Datei (oder irgendein Text, der dann komplett ins Text-Element des Standard-Dokuments
	 * eingebunden wird)
	 */
	public Samdas(String input){
		SAXBuilder builder = new SAXBuilder();
		try{
			CharArrayReader car=new CharArrayReader(input.toCharArray());
			doc = builder.build(car);
			eRoot=doc.getRootElement();
	
		} catch (Exception e) {
			//SWTHelper.alert("Fehler beim Datenimport","Der XML-String enthält formale Fehler oder kann nicht gelesen werden");
			//ExHandler.handle(e);
			doc=new Document();
			eRoot=new Element("EMR",ns);
			doc.setRootElement(eRoot);
			Element record=new Element("record",ns);
			Element text=new Element("text",ns);
			doc.getRootElement().addContent(record);
			record.addContent(text);
			text.setText(input);
		} 
	}
	
	/**
	 * Der Default-Konstruktor erstellt ein leeres Standard-Dokument
	 *
	 */
	public Samdas(){
		doc=new Document();
		eRoot=new Element("EMR",ns);
		//eRoot.addNamespaceDeclaration(nsxsi);
		//eRoot.addNamespaceDeclaration(nsschema);
		doc.setRootElement(eRoot);
	}
	
	
	public Document getDocument(){
		return doc;
	}
	/**
	 * Get the contents of this Samdas in form of an XML-String
	 */
	@Override
	public String toString(){
		XMLOutputter xo=new XMLOutputter(Format.getRawFormat());
		return xo.outputString(doc);
	}
	/** Shortcut für Dokumente, die sowieso nur einen Record haben */
	public String getRecordText(){
		Element rec=getRecordElement();
		String ret=rec.getChildText("text",ns);
		return ret==null ? "" : ret;
	}
	
	public Element getRecordElement(){
		Element ret=eRoot.getChild("record",ns);
		if(ret==null){
			ret=new Element("record",ns);
			eRoot.addContent(ret);
		}
		return ret;
	}
	public Record getRecord(){
		return new Record(getRecordElement());
	}
	
	public void add(Record r){
		eRoot.addContent(r.eRecord);
	}
	
	
	/**
	 * A record is an EMR entry
	 * @author Gerry
	 *
	 */
	public static class Record{
		private Element eRecord;
		public Record(Element e){
			eRecord=e;
		}
		public String getAuthor(){
			return eRecord.getAttributeValue("author");
		}
		public String getResponsibleEAN(){
			return eRecord.getAttributeValue("responsibleEAN");
		}
		public TimeTool getDate(){
			return new TimeTool(eRecord.getAttributeValue("date"));
		}
		public Element getTextElement(){
			Element ret=eRecord.getChild("text",ns);
			if(ret==null){
				ret=new Element("text",ns);
				eRecord.addContent(ret);
			}
			return ret;
		}
		public void setText(String t){
			Element eText=getTextElement();
			eText.setText(t);
		}
		public String getText(){
			Element eText=getTextElement();
			return  eText.getText();
		}
		@SuppressWarnings("unchecked")
		public List<XRef> getXrefs(){
			List<Element> lElm=eRecord.getChildren("xref", ns);
			List<XRef> ret=new ArrayList<XRef>(lElm.size());
			for(Element el:lElm){
				ret.add(new XRef(el));
			}
			return ret;
		}
		@SuppressWarnings("unchecked")
		public List<Markup> getMarkups(){
			List<Element> lElm=eRecord.getChildren("markup", ns);
			List<Markup> ret=new ArrayList<Markup>(lElm.size());
			for(Element el:lElm){
				ret.add(new Markup(el));
			}
			return ret;
		}
		
		@SuppressWarnings("unchecked")
		public List<Section> getSections(){
			List<Element> lElm=eRecord.getChildren("section", ns);
			List<Section> ret=new ArrayList<Section>(lElm.size());
			for(Element el:lElm){
				ret.add(new Section(el));
			}
			return ret;
		}
		
		public void add(Range x){
			eRecord.addContent(x.el);
		}
		public void remove(Range x){
			eRecord.removeContent(x.el);
		}
	}
	
	/**
	 * A Range is a part of the text. It is defined by a position, a length and a type.
	 * 
	 */
	static class Range{
		protected Element el;
		Range(Element e){
			el=e;
		}
		Range(String typ, int pos, int length){
			el=new Element(typ,ns);
			el.setAttribute("from",Integer.toString(pos));
			el.setAttribute("length",Integer.toString(length));
		}
		public int getPos(){
			return Integer.parseInt(el.getAttributeValue("from"));
		}
		public void setPos(int p){
			el.setAttribute("from", Integer.toString(p));
		}
		public int getLength(){
			return Integer.parseInt(el.getAttributeValue("length"));
		}
	}
	/**
	 * An XRef is a range that defines a crossreference to some other piece of information
	 * it can define a class that can handle its contents
	 * @author Gerry
	 *
	 */
	public static class XRef extends Range{
		
		XRef(Element e){
			super(e);
		}
		public XRef(String provider, String id, int pos, int length){
			super("xref",pos,length);
			el.setAttribute("provider", provider);
			el.setAttribute("id", id);
		}
		

		public String getProvider(){
			return el.getAttributeValue("provider");
		}
		public String getID(){
			return el.getAttributeValue("id");
		}
	}
	/**
	 * A Markup is a Range that defines some text attributes
	 * @author Gerry
	 *
	 */
	public static class Markup extends Range{
		Markup(Element e){
			super(e);
		}
		public Markup(int pos, int length, String typ){
			super("markup",pos,length);
			el.setAttribute("type",typ);
		}
		public String getType(){
			return el.getAttributeValue("type");
		}
	}
	
	/**
	 * A Section is a Markup that summarizes a piece of text unter a section header.
	 * @author Gerry
	 *
	 */
	public static class Section extends Range{
		Section(Element e){
			super(e);
		}
		public Section(int pos, int length, String name){
			super("section",pos,length);
			el.setAttribute("name",name);
		}
	}
	
	public static class Finding{
		protected Element el;
		
		Finding(Element e){
			el=e;
		}
		Finding(String typ, String date, String labEAN, boolean abnormal){
			el=new Element(typ);
			el.setAttribute("date", date);
			el.setAttribute("labEAN",labEAN);
			el.setAttribute("abnormal", Boolean.toString(abnormal).toLowerCase());
		}
		public TimeTool getDate(){
			return new TimeTool(el.getAttributeValue("date"));
		}
		public boolean isAbnormal(){
			return(el.getAttributeValue("abormal").equals("true"));
		}
	}
	public static class Analyse extends Finding{
		public Analyse(){
			super(new Element("analysis"));
		}
	}
	public static class Image extends Finding{
		public Image(){
			super(new Element("image"));
		}
	}
	
	public static class ECG extends Finding{
		public ECG(){
			super(new Element("ecg"));
		}
	}
	
}
