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
 * $Id$
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
		readFromXML(Hub.getWritableUserDir()+File.separator+"messwerte.xml");
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
			NodeList nl = rootel.getElementsByTagName("datatype");
			for (int i = 0; i < nl.getLength(); i++) {
				Element edt = (Element) nl.item(i);
				String name = edt.getAttribute("name");
				String title = edt.getAttribute("title");
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
					String fn = edtf.getAttribute("name");
					String ft = edtf.getAttribute("title");
					if (ft.equals("")) {
						ft = fn;
					}
					
					//OldMesswertTyp dft;
					IMesswertTyp typ;
					if (edtf.getNodeName().equals("numfield")) {
						typ = new MesswertTypNum(fn, ft, edtf.getAttribute("unit"));
						if (edtf.hasAttribute("default")) {
							typ.setDefault(edtf.getAttribute("default"));
						}
					} else if (edtf.getNodeName().equals("boolfield")) {
						typ = new MesswertTypBool(fn, ft, edtf.getAttribute("unit"));
						if (edtf.hasAttribute("default")) {
							typ.setDefault(edtf.getAttribute("default"));
						}
					} else if (edtf.getNodeName().equals("strfield")) {
						typ = new MesswertTypStr(fn, ft, edtf.getAttribute("unit"));
						if (edtf.hasAttribute("default")) {
							typ.setDefault(edtf.getAttribute("default"));
						}
					} else if (edtf.getNodeName().equals("enumfield")) {
						MesswertTypEnum en = new MesswertTypEnum(fn, ft, edtf.getAttribute("unit"));
						typ = en;
						
						if (edtf.hasAttribute("default")) {
							typ.setDefault(edtf.getAttribute("default"));
						}
					
						NodeList children = edtf.getChildNodes();
						for (int k = 0; k < children.getLength(); k++) {
							if (children.item(k).getNodeType() != Node.ELEMENT_NODE) {
								continue;
							}
							
							Element choice = (Element) children.item(k);
							en.addChoice(choice.getAttribute("title"),
								Integer.parseInt(choice.getAttribute("value")));
						}
						
						// Wenn kein vernuenftiger Standardwert angegeben wurde
						// nehmen wir die erste Auswahlmoeglichkeit
						if (typ.getDefault().equals("")) {
							for (int k = 0; k < children.getLength(); k++) {
								if (children.item(k).getNodeType() == Node.ELEMENT_NODE) {
									Element choice = (Element) children.item(k);
									typ.setDefault(choice.getAttribute("value"));
									break;
								}
							}
						}
					} else if (edtf.getNodeName().equals("calcfield")) {
						MesswertTypCalc calc = new MesswertTypCalc(fn, ft, edtf.getAttribute("unit"));
						typ = calc;
						
						Element formula = (Element) edtf.getElementsByTagName("formula").item(0);
						calc.setFormula(formula.getTextContent(), formula.getAttribute("interpreter"));
						
						NodeList children = edtf.getElementsByTagName("var");
						for (int k = 0; k < children.getLength(); k++) {
							Node n = children.item(k);
							if (n.getNodeType() != Node.ELEMENT_NODE) {
								continue;
							}
							Element var = (Element) n;
							calc.addVariable(var.getAttribute("name"), var.getAttribute("source"));
						}
					} else if (edtf.getNodeName().equals("datafield")) {
						MesswertTypData data = new MesswertTypData(fn, ft, edtf.getAttribute("unit"));
						typ = data;
						
						data.setRefType(edtf.getAttribute("type"));
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
