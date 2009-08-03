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

package com.hilotec.elexis.pluginstatistiken;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import com.hilotec.elexis.pluginstatistiken.config.KonfigurationQuery;

import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Verrechnet;
import ch.elexis.util.Extensions;
import ch.elexis.util.IDataAccess;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;


/**
 * Einzeiner Datensatz einer Abfrage (ist jeweils mit einem bestimmten Patient
 * verknuepft.
 *
 * @author Antoine Kaufmann
 */
public class Datensatz {
	private Patient patient;
	private boolean valid = true;
	HashMap<String,String> fieldMap;
	
	/**
	 * Hilfsfunktion die alle Vorkommen eines bestimmten Strings in einem
	 * Stringbuffer ersetzt.
	 * 
	 * @param sb   Stringbuffer
	 * @param find Zu suchenden Text
	 * @param repl Text durch den ersetzt werden soll
	 */
	private void replaceInSB(StringBuffer sb, String find, String repl) {
		int pos;
		int findll = find.length();
		
		while (true) {
			pos = sb.indexOf(find);
			if (pos < 0) {
				return;
			}
			
			sb.replace(pos, pos + findll, repl);
		}
	}
	
	/**
	 * Berechnet die Kosten aller Konsultationen des Patienten während der
	 * angegebenen Zeitspanne.
	 * 
	 * @return Kosten als String
	 */
	private String getKostenVonBis(String startDatum, String endDatum) {
		double wert = 0.0;
		TimeTool start = new TimeTool(startDatum);
		TimeTool end = new TimeTool(endDatum);
		
		Fall[] faelle = patient.getFaelle();
		for (Fall f: faelle) {
			TimeTool beginn = new TimeTool(f.getBeginnDatum());
			TimeTool ende = new TimeTool(f.getEndDatum());
			
			if (beginn.isAfter(end) || ende.isBefore(beginn)) {
				continue;
			}
			
			Konsultation[] konsultationen = f.getBehandlungen(false);
			for (Konsultation k: konsultationen) {
				TimeTool datum = new TimeTool(k.getDatum());
				if (datum.isBefore(start) || datum.isAfter(end)) {
					continue;
				}
				
				List<Verrechnet> leistungen = k.getLeistungen();
				for (Verrechnet l: leistungen) {
					wert += l.getNettoPreis().multiply(l.getZahl()).
						getAmount();
				}
			}
		}
		
		return Double.toString(wert);
	}
	
	
	/**
	 * Hilfsfunktion, die Daten von einem Plugin ueber die IDataAccess-
	 * Schnittstelle holt.
	 * 
	 * @param source Zugriffsstring (durch : getrennt)
	 * 
	 * @return String vom Plugin
	 */
	private String getPluginData(String source) {
		String[] adr = source.split(":");
		if (adr.length < 4) {
			SWTHelper.showError("Datenzugriff-Fehler", "Das Datenfeld " +
				source + " wird falsch angesprochen");
			return null;
		}
		String plugin = adr[0];
		String dependendObject = adr[1];
		String dates = adr[2];
		String desc = adr[3];
		String[] params = null;
		if (adr.length == 5) {
			params = adr[4].split("\\.");
		}
		
		PersistentObject ref = null;
		if (dependendObject.equals("Patient")) {
			ref = patient;
		}
		for (IConfigurationElement ic : Extensions.getExtensions("ch.elexis.DataAccess")) {
			String icName = ic.getAttribute("name");
			if (icName.equals(plugin)) {
				IDataAccess ida;
				try {
					ida = (IDataAccess) ic.createExecutableExtension("class");
					Result<Object> ret = ida.getObject(desc, ref, dates, params);
					if (ret.isOK()) {
						return (String) ret.get();
					} else {
						return null;
					}
				} catch (CoreException e) {
					ExHandler.handle(e);
				}
				
			}
		}
		return null;
	}
	
	/**
	 * Konstruktor fuer Datensatz
	 * 
	 * @param q          Abfrage zu der dieser Datensatz gehoeren soll
	 * @param p          Patient
	 * @param startDatum Startdatum des Bereichs der als Parameter angegeben
	 *                   wurde.
	 * @param endDatum   Enddatum des Bereiches
	 */
	public Datensatz(KonfigurationQuery q, Patient p, String startDatum,
		String endDatum)
	{
		patient = p;
		
		List<String> names = q.getColNames();
		List<String> sources = q.getColSources();
		fieldMap = new HashMap<String,String>();
		for (int i = 0; i < names.size(); i++) {
			StringBuffer sb = new StringBuffer(sources.get(i));
			String data;

			replaceInSB(sb, "[startdatum]", startDatum);
			replaceInSB(sb, "[enddatum]", endDatum);
			if (sb.indexOf(":") >= 0) {
				data = getPluginData(sb.toString());
			} else if (sb.toString().equals("kostenBereich")) {
				data = getKostenVonBis(startDatum, endDatum);
			} else {
				data = patient.get(sb.substring(sb.indexOf(".") + 1));
			}
			
			if (data == null) {
				valid = false;
				break;
			} else {
				fieldMap.put(names.get(i), data);
			}
		}
	
	}
	
	/**
	 * Bestimmtes Feld des Datensatzes auslesen anhand des Namens
	 * 
	 * @param name Name des Felds
	 * 
	 * @return Wert des Feldes
	 */
	public String getFeld(String name) {
		if (!valid) {
			return null;
		}
		return fieldMap.get(name);
	}

	/**
	 * Prueft ob der Datensatz gueltig ist.
	 */
	public boolean isValid() {
		return valid;
	}
}
