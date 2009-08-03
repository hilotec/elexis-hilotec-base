/*******************************************************************************
 * Copyright (c) 2009, A. Kaufmann and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    A. Kaufmann - initial implementation 
 *    
 * $Id: MesswertBase.java 5386 2009-06-23 11:34:17Z rgw_ch $
 *******************************************************************************/

package com.hilotec.elexis.pluginstatistiken.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import ch.elexis.Hub;
import ch.elexis.util.Log;

/**
 * Parser fuer die Konfiguration der Abfragen
 * 
 * @author Antoine Kaufmann
 */
public class Konfiguration {
	public static final String STATISTIKEN_FILENAME = "statistiken.xml";
	public static final String ELEM_QUERY = "query";
	public static final String ELEM_COLS = "cols";
	public static final String ELEM_WHERE = "where";
	public static final String ATTR_TITLE = "title";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_SOURCE = "source";
	
	Log log = Log.get("Messwertstatistiken");
	ArrayList<KonfigurationQuery> queries;
	
	private static Konfiguration the_one_and_only_instance = null; 
	public static Konfiguration getInstance() {
		if (the_one_and_only_instance == null) {
			the_one_and_only_instance = new Konfiguration();
		}
		return the_one_and_only_instance;
	}
	
	/**
	 * Das ist ein Singleton, also muss der Konstruktor privat sein
	 */
	private Konfiguration() {
		queries = new ArrayList<KonfigurationQuery>();
		readFromXML(Hub.getWritableUserDir()+File.separator+STATISTIKEN_FILENAME);
	}
	
	/**
	 * XML-Datei mit den Definitionen der Abfragen einlesen und parsen
	 * 
	 * @param path Pfad zur Datei
	 */
	private void readFromXML(String path) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document doc;
		
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(new FileInputStream(path));
			
			Element rootel = doc.getDocumentElement();
			NodeList ql = rootel.getElementsByTagName(ELEM_QUERY);
			for (int i = 0; i < ql.getLength(); i++) {
				Node qn = ql.item(i);
				if (qn.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				
				Element qe = (Element) qn;
				KonfigurationQuery kq = new KonfigurationQuery(qe.getAttribute(ATTR_TITLE));
				
				Element colse = (Element) qe.getElementsByTagName(ELEM_COLS).item(0);
				Element wheree = (Element) qe.getElementsByTagName(ELEM_WHERE).item(0);
				
				NodeList colsList = colse.getChildNodes();
				for (int j = 0; j < colsList.getLength(); j++) {
					Node cn = colsList.item(j);
					if (cn.getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}
					Element ce = (Element) cn;
					
					kq.addCol(ce.getAttribute(ATTR_NAME), ce.getAttribute(ATTR_SOURCE));
				}
				
				
				Element whereOp = null;
				NodeList wl = wheree.getChildNodes();
				for (int j = 0; j < wl.getLength(); j++) {
					if (wl.item(j).getNodeType() == Node.ELEMENT_NODE) {
						whereOp = (Element) wl.item(j);
						break;
					}
				}
				KonfigurationWhere where = new KonfigurationWhere(whereOp);
				kq.setWhere(where);
				
				queries.add(kq);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.log("Einlesen der XML-Datei felgeschlagen: " + e.getMessage(), Log.ERRORS);
		}
	}
	
	/**
	 * Alle Abfragen in dieser Konfiguration zurzueckgeben
	 */
	public List<KonfigurationQuery> getQueries() {
		return queries;
	}
}
