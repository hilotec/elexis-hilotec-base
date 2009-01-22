/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: MedikamentImporterVidal.java 4999 2009-01-22 14:25:53Z rgw_ch $
 *******************************************************************************/
package ch.elexis.artikel_at.data;

import java.text.ParseException;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.ImporterPage;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.Money;

public class MedikamentImporterVidal extends ImporterPage {
	
	private static Log log = Log.get("VidalImport");
	private static final String ENTRYTYPE_DELETE = "D";
	private static final String ENTRYTYPE_INSERT = "I";
	private static final String ENTRYTYPE_UPDATE = "U";
	
	private Button bClear;
	private boolean bDoClear;
	
	public MedikamentImporterVidal(){
	// TODO Auto-generated constructor stub
	}
	
	@Override
	public Composite createPage(Composite parent){
		Composite ret = new ImporterPage.FileBasedImporter(parent, this);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		bClear = new Button(parent, SWT.CHECK | SWT.WRAP);
		bClear.setText("Alle Daten vorher löschen (VORSICHT! Bitte Anleitung beachten)");
		bClear.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		return ret;
	}
	
	@Override
	public void collect(){
		bDoClear = bClear.getSelection();
	}
	
	
	@Override
	public IStatus doImport(IProgressMonitor monitor) throws Exception{
		// System.out.println(importFile);
		monitor.beginTask("Importiere Vidal", -1);
		if (bDoClear) {
			monitor.subTask("Lösche alte Daten");
			PersistentObject.getConnection().exec("DELETE FROM ARTIKEL WHERE TYP='Vidal'");
		}
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		SAXParser parser = saxFactory.newSAXParser();
		parser.parse(results[0], new Handler(monitor));
		monitor.done();
		return Status.OK_STATUS;
	}
	
	@Override
	public String getDescription(){
		return "Importiere Medikamente von vidal.at";
	}
	
	@Override
	public String getTitle(){
		return "Medikamente (vidal)";
	}
	
	class Handler extends org.xml.sax.helpers.DefaultHandler {
		IProgressMonitor monitor;
		int counter = 0;
		// RpEntry act;
		Hashtable<String, Object> act;
		String actType = null;
		StringBuilder chars;
		Query<Medikament> qbe = new Query<Medikament>(Medikament.class);
		
		Handler(IProgressMonitor m){
			monitor = m;
		}
		
		@Override
		public void endDocument() throws SAXException{
			// TODO Auto-generated method stub
			super.endDocument();
		}
		
		@Override
		public void startDocument() throws SAXException{
			monitor.subTask("Starte einlesen");
			monitor.worked(1);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attr)
			throws SAXException{
			if (monitor.isCanceled()) {
				throw new SAXException("Thread cancelled");
			}
			if (qName.equals("RpEntry")) {
				// act=new RpEntry();
				act = new Hashtable();
				actType = attr.getValue("EntryType");
				
			} else if (qName.equals("PhZNr")
				|| (qName.endsWith("Name"))
				|| (qName.equals("DoLC"))
				|| qName.equals("Storage")
				|| (qName.equals("Quantity"))
				|| (qName.equals("EnhUnitDesc"))
				|| (qName.equals("KVP"))
				|| (qName.equals("AVP"))
				|| (qName.equals("zInh"))
				|| (qName.equals("INDText") || (qName.equals("RuleText") || (qName
					.equals("RemarkText"))))) {
				chars = new StringBuilder();
			} else if (qName.equals("RSigns")) {
				Hashtable RSigns = new Hashtable();
				for (String val : Medikament.RSIGNS) {
					String cont = attr.getValue(val);
					RSigns.put(val, cont == null ? "0" : cont);
				}
				act.put("RSigns", RSigns);
				
			} else if (qName.equals("SSigns")) {
				Hashtable SSigns = new Hashtable();
				String remb = attr.getValue("Remb");
				act.put("Remb", remb);
				for (String val : Medikament.SSIGNS) {
					String cont = attr.getValue(val);
					SSigns.put(val, cont == null ? "0" : cont);
				}
				act.put("SSigns", SSigns);
			} else if (qName.equals("Unit")) {
				act.put("SUnit", attr.getValue("SUnit"));
				chars = new StringBuilder();
			} else if (qName.equals("ZNr")) {
				act.put("ZnrNum", attr.getValue("ZNrNum"));
				chars = new StringBuilder();
			
			}else if(qName.equals("SubstRef")){
				act=new Hashtable();
				act.put("SubstID", attr.getValue("SubstID"));
				act.put("SubstSalt", attr.getValue("SubstSalt"));
				chars = new StringBuilder();
			}
			
			
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException{
			if (qName.equals("RpEntry")) {
				Medikament art = null;
				if (actType.equals(ENTRYTYPE_INSERT)) {
					art =
						new Medikament((String) act.get("SName"), "Vidal", (String) act
							.get("PhZNr"));
				} else {
					qbe.clear();
					qbe.add("Typ", "=", "Vidal");
					qbe.add("SubID", "=", (String) act.get("PhZNr")); // (String)act.get("ZnrNum"));
					List<Medikament> list = qbe.execute();
					if (list.size() > 0) {
						art = list.get(0);
						if (actType.equals(ENTRYTYPE_DELETE)) {
							art.delete();
							return;
						}
					} else {
						art =
							new Medikament((String) act.get("SName"), "Vidal", (String) act
								.get("PhZNr"));
					}
				}
				Hashtable extInfo = art.getHashtable("ExtInfo");
				extInfo.put("Pharmacode", act.get("PhZNr"));
				String box = (String) ((Hashtable) act.get("SSigns")).get("Box");
				if (box != null) {
					box = box.trim();
				}
				art.set("Codeclass", box);
				art.set("VK_Preis", (String) act.get("KVP")); // or AVP?
				extInfo.putAll(act);
				art.setHashtable("ExtInfo", extInfo);
				monitor.worked(1);
				act = null;
				art = null;
				if ((counter & 64) == counter) {
					System.gc();
					PersistentObject.clearCache();
				}
				counter++;
			} else if (qName.equals("PhZNr")) {
				act.put("PhZNr", chars.toString());
				chars = null;
			} else if (qName.equals("SName")) {
				act.put("SName", chars.toString());
				monitor.subTask("Lese " + act.get("SName"));
				chars = null;
			} else if (qName.equals("OName")) {
				act.put("OName", chars.toString());
				chars = null;
			} else if (qName.equals("DoLC")) {
				act.put("DoLC", chars.toString());
				chars = null;
			} else if (qName.equals("Storage")) {
				act.put("Storage", chars.toString());
				chars = null;
			} else if (qName.equals("Quantity")) {
				act.put("Quantity", chars.toString());
				chars = null;
			} else if (qName.equals("Unit")) {
				act.put("SUnitDesc", chars.toString());
				chars = null;
			} else if (qName.equals("EnhUnitDesc")) {
				act.put("EnhUnitDesc", chars.toString());
				chars = null;
			} else if (qName.equals("KVP")) {
				Money money = new Money();
				try {
					// Money.setLocale(new Locale("de","AT"));
					money.addAmount(chars.toString());
				} catch (ParseException ex) {
					money.addCent(chars.toString().replaceFirst("[,\\.]", ""));
				}
				act.put("KVP", money.getCentsAsString());
				chars = null;
			} else if (qName.equals("AVP")) {
				Money money = new Money();
				try {
					// Money.setLocale(new Locale("de","AT"));
					money.addAmount(chars.toString());
				} catch (ParseException ex) {
					money.addCent(chars.toString().replaceFirst("[,\\.]", ""));
				}
				act.put("AVP", money.getCentsAsString());
				chars = null;
			} else if (qName.equals("zInh")) {
				act.put("ZInh", chars.toString());
				chars = null;
			} else if (qName.equals("ZNr")) {
				act.put("ZNr", chars.toString());
				chars = null;
			} else if (qName.equals("INDText")) {
				act.put("INDText", chars.toString());
				chars = null;
			} else if (qName.equals("RuleText")) {
				act.put("RuleText", chars.toString());
				chars = null;
			} else if (qName.equals("RemarkText")) {
				act.put("RemarkText", chars.toString());
				chars = null;
			}else if(qName.equals("ATCCode")){
				
			}else if(qName.equals("SubstRef")){
				Substance subst=new Substance((String)act.get("SubstID"),chars.toString(),(String)act.get("SubstSalt"));
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException{
			if (chars != null) {
				chars.append(ch, start, length);
			}
		}
		
	}
	
}
