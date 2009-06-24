/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    A. Kaufmann - initial implementation 
 *    
 * $Id: MessungKonfiguration.java 5403 2009-06-24 12:07:31Z freakypenguin $
 *******************************************************************************/

package com.hilotec.elexis.messwerte.data;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import ch.elexis.Hub;
import ch.elexis.util.Log;

public class MessungKonfiguration {
	public static final String ATTR_TYPE = "type";
	public static final String NAME_DATAFIELD = "datafield";
	public static final String ATTR_SOURCE = "source";
	public static final String ELEMENT_VAR = "var";
	public static final String ATTR_INTERPRETER = "interpreter";
	public static final String ELEMENT_FORMULA = "formula";
	public static final String NAME_CALCFIELD = "calcfield";
	public static final String ATTR_VALUE = "value";
	public static final String NAME_ENUMFIELD = "enumfield";
	public static final String NAME_STRINGFIELD = "strfield";
	public static final String NAME_BOOLFIELD = "boolfield";
	public static final String NAME_NUMFIELD = "numfield";
	public static final String NAME_SCALEFIELD = "scalefield";
	public static final String ATTR_DEFAULT = "default";
	public static final String ATTR_UNIT = "unit";
	public static final String ATTR_TITLE = "title";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_MAX = "max";
	public static final String ATTR_MIN = "min";
	public static final String ELEMENT_DATATYPE = "datatype";
	public static final String CONFIG_FILENAME = "messwerte.xml";
	
	
	private static MessungKonfiguration the_one_and_only_instance = null;
	ArrayList<MessungTyp> types;
	private final Log log = Log.get("DataConfiguration"); 
	
	public static MessungKonfiguration getInstance() {
		if (the_one_and_only_instance == null) {
			the_one_and_only_instance = new MessungKonfiguration();
		}
		return the_one_and_only_instance;
	}
	
	private MessungKonfiguration() {
		types = new ArrayList<MessungTyp>();
		readFromXML(Hub.getWritableUserDir()+File.separator+CONFIG_FILENAME);
	}
	
	private void readFromXML(String path) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document doc;
		
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(new FileInputStream(path));
			
			Element rootel = doc.getDocumentElement();
			
			// datatype-Deklarationen durchgehen und einlesen
			NodeList nl = rootel.getElementsByTagName(ELEMENT_DATATYPE);
			for (int i = 0; i < nl.getLength(); i++) {
				Element edt = (Element) nl.item(i);
				String name = edt.getAttribute(ATTR_NAME);
				String title = edt.getAttribute(ATTR_TITLE);
				if (title.length() == 0) {
					title = name;
				}
				
				MessungTyp dt = new MessungTyp(name, title);
				// Einzlene Felddeklarationen durchgehen
				NodeList dtf = edt.getChildNodes();
				for (int j = 0; j < dtf.getLength(); j++) {
					Node ndtf = dtf.item(j);
					if (ndtf.getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}

					Element edtf = (Element) ndtf;
					String fn = edtf.getAttribute(ATTR_NAME);
					String ft = edtf.getAttribute(ATTR_TITLE);
					if (ft.equals("")) {
						ft = fn;
					}
					
					//OldMesswertTyp dft;
					IMesswertTyp typ;
					if (edtf.getNodeName().equals(NAME_NUMFIELD)) {
						typ = new MesswertTypNum(fn, ft, edtf.getAttribute(ATTR_UNIT));
						if (edtf.hasAttribute(ATTR_DEFAULT)) {
							typ.setDefault(edtf.getAttribute(ATTR_DEFAULT));
						}
					} else if (edtf.getNodeName().equals(NAME_BOOLFIELD)) {
						typ = new MesswertTypBool(fn, ft, edtf.getAttribute(ATTR_UNIT));
						if (edtf.hasAttribute(ATTR_DEFAULT)) {
							typ.setDefault(edtf.getAttribute(ATTR_DEFAULT));
						}
					} else if (edtf.getNodeName().equals(NAME_STRINGFIELD)) {
						typ = new MesswertTypStr(fn, ft, edtf.getAttribute(ATTR_UNIT));
						if (edtf.hasAttribute(ATTR_DEFAULT)) {
							typ.setDefault(edtf.getAttribute(ATTR_DEFAULT));
						}
					} else if (edtf.getNodeName().equals(NAME_ENUMFIELD)) {
						MesswertTypEnum en = new MesswertTypEnum(fn, ft, edtf.getAttribute(ATTR_UNIT));
						typ = en;
						
						if (edtf.hasAttribute(ATTR_DEFAULT)) {
							typ.setDefault(edtf.getAttribute(ATTR_DEFAULT));
						}
					
						NodeList children = edtf.getChildNodes();
						for (int k = 0; k < children.getLength(); k++) {
							if (children.item(k).getNodeType() != Node.ELEMENT_NODE) {
								continue;
							}
							
							Element choice = (Element) children.item(k);
							en.addChoice(choice.getAttribute(ATTR_TITLE),
								Integer.parseInt(choice.getAttribute(ATTR_VALUE)));
						}
						
						// Wenn kein vernuenftiger Standardwert angegeben wurde
						// nehmen wir die erste Auswahlmoeglichkeit
						if (typ.getDefault().equals("")) {
							for (int k = 0; k < children.getLength(); k++) {
								if (children.item(k).getNodeType() == Node.ELEMENT_NODE) {
									Element choice = (Element) children.item(k);
									typ.setDefault(choice.getAttribute(ATTR_VALUE));
									break;
								}
							}
						}
					} else if (edtf.getNodeName().equals(NAME_CALCFIELD)) {
						MesswertTypCalc calc = new MesswertTypCalc(fn, ft, edtf.getAttribute(ATTR_UNIT));
						typ = calc;
						
						Element formula = (Element) edtf.getElementsByTagName(ELEMENT_FORMULA).item(0);
						calc.setFormula(formula.getTextContent(), formula.getAttribute(ATTR_INTERPRETER));
						
						NodeList children = edtf.getElementsByTagName(ELEMENT_VAR);
						for (int k = 0; k < children.getLength(); k++) {
							Node n = children.item(k);
							if (n.getNodeType() != Node.ELEMENT_NODE) {
								continue;
							}
							Element var = (Element) n;
							calc.addVariable(var.getAttribute(ATTR_NAME), var.getAttribute(ATTR_SOURCE));
						}
					} else if (edtf.getNodeName().equals(NAME_DATAFIELD)) {
						MesswertTypData data = new MesswertTypData(fn, ft, edtf.getAttribute(ATTR_UNIT));
						typ = data;
						
						data.setRefType(edtf.getAttribute(ATTR_TYPE));
					} else if (edtf.getNodeName().equals(NAME_SCALEFIELD)) {
						MesswertTypScale scale = new MesswertTypScale(fn, ft,
							edtf.getAttribute(ATTR_UNIT));
						typ = scale;
						if (edtf.hasAttribute(ATTR_DEFAULT)) {
							scale.setDefault(edtf.getAttribute(ATTR_DEFAULT));
						}
						if (edtf.hasAttribute(ATTR_MIN)) {
							scale.setMin(Integer.parseInt(
								edtf.getAttribute(ATTR_MIN)));
						}
						if (edtf.hasAttribute(ATTR_MAX)) {
							scale.setMax(Integer.parseInt(
								edtf.getAttribute(ATTR_MAX)));
						}
					} else {
						log.log("Unbekannter Feldtyp: '" + edtf.getNodeName() + "'", Log.ERRORS);
						continue;
					}
					dt.addField(typ);
				}
				types.add(dt);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.log("Einlesen der XML-Datei felgeschlagen: " + e.getMessage(), Log.ERRORS);
		}
		
		
	}
	
	public ArrayList<MessungTyp> getTypes() {
		return types;
	}
	public MessungTyp getTypeByName(String name) {
		for (MessungTyp t: types) {
			if (t.getName().compareTo(name) == 0) {
				return t;
			}
		}
		return null;
	}
}
