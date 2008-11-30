/*******************************************************************************
 * Copyright (c) 2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: MimeTool.java 4247 2008-08-08 14:40:22Z rgw_ch $
 *******************************************************************************/

package ch.rgw.tools;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * We try to make a simple converter frm and to SOAP without the huge overhead of Axis... This
 * implementation deals only with a limited number of variable types.
 * 
 * @author Gerry
 * 
 */
public class SoapConverter {
	public static final Namespace ns =
		Namespace.getNamespace("soap", "http://www.w3.org/2001/12/soap-envelope");
	public static final String TYPE_STRING = "string";
	public static final String TYPE_INTEGRAL = "integral";
	public static final String TYPE_FLOAT = "float";
	public static final String TYPE_ARRAY = "array";
	public static final String TYPE_HASH = "hash";
	public static final String TYPE_SIGNATURE = "signature";
	
	private Document doc;
	private Element eRoot;
	private Element eBody;
	private boolean bValid;
	
	public SoapConverter(){}
	
	public boolean load(byte[] input){
		SAXBuilder builder = new SAXBuilder();
		ByteArrayInputStream bais = new ByteArrayInputStream(input);
		try {
			doc = builder.build(bais);
			eRoot = doc.getRootElement();
			bValid = true;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			bValid = false;
		}
		return bValid;
	}
	
	public Document getXML(){
		return doc;
	}
	
	public boolean load(String input){
		SAXBuilder builder = new SAXBuilder();
		try {
			CharArrayReader car = new CharArrayReader(input.toCharArray());
			doc = builder.build(car);
			eRoot = doc.getRootElement();
			bValid = true;
		} catch (Exception e) {
			ExHandler.handle(e);
			bValid = false;
		}
		return bValid;
		
	}
	
	public Element getParameter(String name){
		if (bValid) {
			Element body = eRoot.getChild("Body", ns);
			List<Element> params = body.getChildren("parameter", ns);
			for (Element el : params) {
				if (el.getAttributeValue("name").equalsIgnoreCase(name)) {
					return el;
				}
			}
		}
		return null;
	}
	
	public HashMap<String, Object> loadHash(Element parm){
		HashMap<String, Object> ret = new HashMap<String, Object>();
		List<Element> params = parm.getChildren("parameter", ns);
		for (Element param : params) {
			String type = param.getAttributeValue("type");
			String s = param.getText();
			Object res = null;
			try {
				if (type.equals(TYPE_STRING)) {
					res = s;
				} else if (type.equals(TYPE_INTEGRAL)) {
					res = Long.parseLong(s);
				} else if (type.equals(TYPE_FLOAT)) {
					res = Double.parseDouble(s);
				} else if (type.equals(TYPE_ARRAY)) {
					res = new BASE64Decoder().decodeBuffer(s);
				} else if (type.equals(TYPE_HASH)) {
					res = loadHash(param);
				} else {
					res = "** unsupported type **";
				}
			} catch (Exception ex) {
				ExHandler.handle(ex);
				res = "** parse error **";
			}
			ret.put(param.getAttributeValue("name"), res);
		}
		return ret;
	}
	
	public HashMap<String, Object> getParameters(){
		if (bValid) {
			Element body = eRoot.getChild("Body", ns);
			return loadHash(body);
		}
		return null;
	}
	
	public void create(String creator, String version, String provider){
		eRoot = new Element("Envelope", ns);
		Element eHeader = new Element("Header", ns);
		Element eID = new Element("Creator", ns);
		eID.setAttribute("name", creator);
		eID.setAttribute("version", version);
		eID.setAttribute("provider", provider);
		eHeader.addContent(eID);
		eRoot.addContent(eHeader);
		eBody = new Element("Body", ns);
		eRoot.addContent(eBody);
		doc = new Document(eRoot);
		bValid = true;
	}
	
	public String toString(){
		if (doc != null && eRoot != null) {
			Format format = Format.getPrettyFormat();
			format.setEncoding("utf-8");
			XMLOutputter xmlo = new XMLOutputter(format);
			return xmlo.outputString(doc);
		}
		return null;
	}
	
	private Element createParameter(Element parent, String name, String type){
		if (parent == null) {
			parent = eBody;
		}
		Element ret = new Element("parameter", ns);
		ret.setAttribute("type", type);
		ret.setAttribute("name", name);
		parent.addContent(ret);
		return ret;
	}
	
	public void addString(String name, String s){
		createParameter(eBody, name, TYPE_STRING).setText(s);
	}
	
	public void addIntegral(String name, long x){
		createParameter(eBody, name, TYPE_INTEGRAL).setText(Long.toString(x));
	}
	
	public void addFloat(String name, double x){
		createParameter(eBody, name, TYPE_FLOAT).setText(Double.toString(x));
	}
	
	public void addArray(String name, byte[] arr){
		String res = new BASE64Encoder().encode(arr);
		createParameter(eBody, name, TYPE_ARRAY).setText(res);
	}
	
	public void addObject(Element parent, String name, Object obj) throws Exception{
		if (obj instanceof String) {
			createParameter(parent, name, TYPE_STRING).setText((String) obj);
		} else if ((obj instanceof Double) || (obj instanceof Float)) {
			createParameter(eBody, name, TYPE_FLOAT).setText(Double.toString((Double) obj));
		} else if ((obj instanceof Integer) || (obj instanceof Long) || (obj instanceof Byte)) {
			createParameter(eBody, name, TYPE_INTEGRAL).setText(Long.toString((Long) obj));
		} else if (obj instanceof byte[]) {
			String res = new BASE64Encoder().encode((byte[]) obj);
			createParameter(eBody, name, TYPE_ARRAY).setText(res);
		} else if (obj instanceof HashMap) {
			addHashMap(parent, name, (HashMap<String, Object>) obj);
		} else {
			throw new Exception("Invalid type for SoapConverter");
		}
	}
	
	public void addHashMap(Element parent, String name, HashMap<String, Object> hash)
		throws Exception{
		Element ret = createParameter(parent, name, TYPE_HASH);
		Set<Entry<String, Object>> entries = hash.entrySet();
		for (Entry<String, Object> entry : entries) {
			addObject(ret, entry.getKey(), entry.getValue());
		}
	}
	
}
