/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - Adaption (Import works; tested on RpInfo_M08_FED.xml)
 *    			   Imports Substances and Medikamente
 *    
 *  $Id: MedikamentImporterVidal.java 6333 2010-05-04 15:02:59Z marcode79 $
 *******************************************************************************/
package ch.elexis.artikel_at.data;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import ch.elexis.Hub;
import ch.elexis.data.Artikel;
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
	private static final String ENTRYTYPE_NAME = "Vidal2";
	
	public MedikamentImporterVidal(){
	}
	
	@Override
	public Composite createPage(Composite parent){
		Composite ret = new ImporterPage.FileBasedImporter(parent, this);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		return ret;
	}
	
	@Override
	public IStatus doImport(IProgressMonitor monitor) throws Exception{
		//TODO: Input File Versioning (What if same file is imported twice?)
		//TODO: Test cancellation check / what to do in case of cancellation?
		//TODO: Versioning of the Vidal files? Was tested on Full Set only!
		//TODO: Validate file before import?
		
		String importFilename = results[0];
		monitor.beginTask("Importiere Vidal", 4);
		SAXBuilder builder = new SAXBuilder();
		monitor.subTask("Lese Datei ein");
		Document doc = builder.build(new File(importFilename));
		monitor.worked(1);
		monitor.subTask("Analysiere Datei");
		Element eRoot = doc.getRootElement();
		monitor.worked(1);
		
		// Substanzen
		Element eSubstances = eRoot.getChild("RpSubstRefs");
		if (eSubstances != null) {
			monitor.subTask("Lese Substanzen ein");
			int noOfSubstances = eSubstances.getChildren("SubstRef").size();
			int counter = 1;
			for (Element eSubstance : (List<Element>) eSubstances.getChildren("SubstRef")) {
				String SubstID = eSubstance.getAttributeValue("SubstID");
				String SubstSalt = eSubstance.getAttributeValue("SubstSalt");
				String Name = eSubstance.getTextTrim();
				monitor.subTask("Lese Substanzen ein "+"["+counter+"/"+noOfSubstances+"]: "+Name);
				new Substance(SubstID, Name, SubstSalt);
				counter++;
			}
		}
		monitor.worked(1);

		// Medikamente
		Element eMedis = eRoot.getChild("RpData"); 
		if (eMedis != null) {
			monitor.subTask("Lese Medikamente ein");
			int noOfMedicaments = eMedis.getChildren("RpEntry").size();
			int counter = 1;
			NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);
			Money.setLocale(Locale.GERMAN);
			for(Element eMedi:(List<Element>) eMedis.getChildren("RpEntry")){
				Medikament medi = null;
				
				String actType=eMedi.getAttribute("EntryType").getValue(); // D|I|U
				String SName = eMedi.getChildText("SName");
				String PhZNr = eMedi.getChildText("PhZNr");
				log.log("Import of "+SName, Log.TRACE);

				Money AVP = new Money();
				Number AVPDouble = null;
					try {				
						AVPDouble = nf.parse(eMedi.getChildText("AVP").trim());
						double AVPout = AVPDouble.doubleValue()*100;
						int AVPint = (int) AVPout;
						AVP.addCent(AVPint);
					} catch (ParseException ex) {
						AVP.addCent(eMedi.getChildText("AVP").replaceFirst("[,\\.]", ""));
					}
				Money KVP = new Money();
				Number KVPDouble = null;
					try {
						KVPDouble = nf.parse(eMedi.getChildText("KVP").trim());
						double KVPout = KVPDouble.doubleValue()*100;
						int KVPint = (int) KVPout;
						KVP.addCent(KVPint);
					} catch (ParseException ex) {
						KVP.addCent(eMedi.getChildText("KVP").replaceFirst("[,\\.]", ""));
					}

				String codeClass = eMedi.getChild("SSigns").getAttribute("Box").getValue().trim();
				monitor.subTask("Lese Medikamente ein "+"["+counter+"/"+noOfMedicaments+"]: "+SName);
				
				//TODO: Sure this is <String, Object> and not <String, String> ?
				Hashtable<String, Object> act = new Hashtable<String, Object>();
				act.put("PhZNr", PhZNr);												// Pharmazentralnummer
				act.put("SName", SName);												// Kurzname
				act.put("OName", eMedi.getChildText("OName"));							// offizieller Produktname
				act.put("DoLC", eMedi.getChildText("DoLC"));							// Date of Last Change
				String Storage = eMedi.getChildText("Storage");	
					if(Storage != null) act.put("Storage", Storage);
				act.put("Quantity", eMedi.getChildText("Quantity"));
				act.put("SUnitDesc", eMedi.getChildText("Unit"));
				act.put("SUnit", eMedi.getChild("Unit").getAttributeValue("SUnit"));
				String EnhUnitDesc = eMedi.getChildText("EnhUnitDesc");
					if(EnhUnitDesc != null) act.put("EnhUnitDesc", EnhUnitDesc);
				act.put("ZInh", eMedi.getChildText("ZInh"));
				act.put("ZNr", eMedi.getChildText("ZNr"));
				act.put("KVP", KVP.getCentsAsString());
				act.put("AVP", AVP.getCentsAsString());
				
				act.put("ZNrNum", eMedi.getChild("ZNr").getAttributeValue("ZNrNum"));
				act.put("Remb", eMedi.getChild("SSigns").getAttributeValue("Remb"));
					Hashtable<String, Object> RSigns = new Hashtable<String, Object>();
						for (String val : Medikament.RSIGNS) {
							String cont = eMedi.getChild("RSigns").getAttributeValue(val);
							RSigns.put(val, cont == null ? "0" : cont);
						}
				act.put("RSigns", RSigns);
					Hashtable<String, Object> SSigns = new Hashtable<String, Object>();
						for (String val : Medikament.SSIGNS) {
							String cont = eMedi.getChild("SSigns").getAttributeValue(val);
							SSigns.put(val, cont == null ? "0" : cont);
						}
				act.put("SSigns", SSigns);
				String INDText = eMedi.getChild("SSigns").getChildText("INDText");
					if(INDText != null) act.put("INDText", INDText);
				String RuleText = eMedi.getChild("SSigns").getChildText("RuleText");
					if(RuleText != null) act.put("RuleText", RuleText);
				String RemarkText = eMedi.getChild("SSigns").getChildText("RemarkText");
					if(RemarkText != null) act.put("RemarkText", RemarkText);
				
				//Substances: n/SN/SN/SN/... where n = Anzahl elemente, SN = Substanz
				int noOfSubstances = eMedi.getChild("Substances").getChildren("Substance").size();
				StringBuilder eMediSubstances = new StringBuilder();	
				eMediSubstances.append(noOfSubstances);
				for (Element eSubstance : (List<Element>) eMedi.getChild("Substances").getChildren("Substance")) {
					eMediSubstances.append("/").append(eSubstance.getValue().trim());
				}
				act.put("Substances", eMediSubstances.toString());
				
				if (actType.equals(ENTRYTYPE_INSERT)) { // Datensatz ist zum anfuegen markiert
					medi = new Medikament(SName, ENTRYTYPE_NAME, PhZNr);
					
				} else {								// Datensatz ist zum loeschen/aktualisieren markiert
					Query<Medikament> qbe = new Query<Medikament>(Medikament.class);
					qbe.clear();
					qbe.add("Typ", "=", ENTRYTYPE_NAME);
					qbe.add("SubID", "=", PhZNr);
					List<Medikament> list = qbe.execute();			
					if (list.size() > 0) {
						//TODO: Check selection.. is this right?
						medi = list.get(0);
						if (actType.equals(ENTRYTYPE_DELETE)) {
							medi.delete();
							continue;
						}
					} else {
						medi = new Medikament(SName, ENTRYTYPE_NAME, PhZNr);
					}								
				}
				StringBuilder sb = new StringBuilder();
				sb.append(SName).append(" (").append(eMedi.getChildText("Quantity")).append(")").append("/").append(eMedi.getChild("SSigns").getAttributeValue("Remb"));
				
				medi.set(new String[] {Artikel.FLD_CODECLASS, Artikel.FLD_VK_PREIS, Artikel.FLD_EK_PREIS, Artikel.EIGENNAME}, 
						codeClass, AVP.getCentsAsString(), KVP.getCentsAsString(), sb.toString());
				
				Hashtable extInfo = medi.getHashtable("ExtInfo");
				extInfo.putAll(act);
				medi.setHashtable("ExtInfo", extInfo);
				
				
				if ((counter % 128) == 1) {
					if(monitor.isCanceled()) return Status.CANCEL_STATUS;
					System.gc();
					PersistentObject.clearCache();
				}
				counter++;
			}
		}
		
		monitor.done();
		return Status.OK_STATUS;
	}
	
	@Override
	public String getDescription(){
		return "Importiere Medikamente von vidal.at";
	}
	
	@Override
	public String getTitle(){
		return "Medikamente (vidal v2)";
	}
	
}
