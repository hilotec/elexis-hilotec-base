/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    A. Kaufmann - Allow extraction of single fields and of first occurance
 *    A. Kaufmann - copied from befunde-Plugin and adapted to new data structure 
 *    
 * $Id$
 *******************************************************************************/

package com.hilotec.elexis.messwerte.data;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.util.IDataAccess;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

public class DataAccessor implements IDataAccess {
	MessungKonfiguration config;
	
	public DataAccessor() {
		config = MessungKonfiguration.getInstance();
	}
	
	/**
	 * Liste mit den verfuegbaren Messungstypen holen
	 */
	public List<Element> getList(){
		ArrayList<Element> ret = new ArrayList<Element>();
		for (MessungTyp typ : config.getTypes()) {
			ret.add(new IDataAccess.Element(IDataAccess.TYPE.STRING,
				typ.getName(), Patient.class, 1));
		}
		return ret;
	}
	
	/**
	 * Spaltentitel fuer Tabelle mit bestimmten Messtyp in Array eintragen
	 */
	private void spaltentitelEintragen(MessungTyp typ, String[] ziel) {
		List<IMesswertTyp> feldtypen = typ.getMesswertTypen();
		int i = 0;
		for (IMesswertTyp dft: feldtypen) {
			ziel[i++] = dft.getTitle();
		}
	}
	
	/**
	 * Aus Liste von Messungen Result-Objekt(Tabelle) erstellen
	 */
	private Result<Object> erstelleResultTabelle(MessungTyp typ, List<Messung> messungen) {
		String values[][] =
			new String[messungen.size() + 1][typ.getMesswertTypen().size() + 1];
		
		// Spaltenueberschriften in erste Zeile eintragen
		spaltentitelEintragen(typ, values[0]);
		
		// Messwerte in Array einfuellen
		int i = 1;
		for (Messung messung: messungen) {
			int j = 0;
			values[i][j++] = messung.getDatum();
			for (Messwert wert: messung.getMesswerte()) {
				values[i][j++] = wert.getDarstellungswert();
			}
			i++;
		}
		
		return new Result<Object>(values);
	}
	
	/**
	 * Bestimmte Messung in Liste finden
	 * 
	 * @param suchbegriff Entweder ein Datum, "first" fuer die aelteste Messung
	 *                    oder "last" fuer die Neuste.
	 */
	private Messung sucheMessung(List<Messung> messungen, String suchbegriff) {
		Messung messung = null;
		
		if (suchbegriff.equals("last")) {
			// Neuste Messung suchen
			TimeTool today = new TimeTool(TimeTool.BEGINNING_OF_UNIX_EPOCH);
			for (Messung m: messungen) {
				TimeTool vgl = new TimeTool(m.getDatum());
				if (vgl.isAfter(today)) {
					today = vgl;
					messung = m;
				}
			}
		} else if (suchbegriff.equals("first")) {
			// Aelteste Messung suchen
			messung = messungen.get(0);
			TimeTool firstdate = new TimeTool(messung.getDatum());
			for (Messung m: messungen) {
				TimeTool vgl = new TimeTool(m.getDatum());
				if (vgl.isBefore(firstdate)) {
					messung = m;
					firstdate = vgl;
				}
			}
		} else {
			// Bestimmte Messung suchen
			TimeTool find = new TimeTool();
			if (find.set(suchbegriff) == false) {
				return null;
			}
			
			for (Messung m: messungen) {
				TimeTool vgl = new TimeTool(m.getDatum());
				if (vgl.isEqual(find)) {
					messung = m;
					break;
				}
			}
		}
		
		return messung;
	}
	
	/**
	 * Bestimmten Messwert, oder eine ganze Messung holen
	 */
	public Result<Object> getObject(final String descriptor,
		final PersistentObject dependentObject, final String dates, final String[] params)
	{
		if (!(dependentObject instanceof Patient)) {
			return new Result<Object>(Result.SEVERITY.ERROR,
				IDataAccess.INVALID_PARAMETERS, "Ungültiger Parameter",
				dependentObject, true);
			
		}
		
		Patient patient = (Patient) dependentObject;
		
		String[] parts = descriptor.split("\\.");
		String messungsname = parts[0];
		String[] feldnamen = null;
		if (parts.length > 1) {
			feldnamen = new String[parts.length - 1];
			for (int i = 0; i < feldnamen.length; i++) {
				feldnamen[i] = parts[i + 1];
			}
		}
		
		MessungTyp typ = config.getTypeByName(messungsname);
		if (typ == null) {
			return new Result<Object>(Result.SEVERITY.ERROR,
				IDataAccess.OBJECT_NOT_FOUND, "Ungültiger Messungstyp" + messungsname,
				messungsname, true);
				
		}
	
		// Spaltenueberschriften fuer Tabelle holen

		
		// Alle Messungen dieses Patienten und Typs holen
		List<Messung> messungen = Messung.getPatientMessungen(patient, typ);
	
		Messung messung = null;
		if (dates.equals("all")) {
			// Tabelle mit allen Messungen zurueckgeben
			return erstelleResultTabelle(typ, messungen);
		} else {
			messung = sucheMessung(messungen, dates);
		}
		
		// Schade, war wohl nichts...
		if (messung == null) {
			return new Result<Object>(Result.SEVERITY.ERROR,
				IDataAccess.OBJECT_NOT_FOUND, "Nicht gefunden", params, true);
		}
		
		
		
		// Wir haben eine passende Messung gefunden. Jetzt gibt es zwei
		// verschiedene Moeglichkeiten, entweder ein bestimmtes Feld wird
		// erwartet, oder wir geben den Kram als Tabelle zurueck.
		if (feldnamen == null) {
			// Tablelle mit allen Messwerten
			ArrayList<Messung> liste = new ArrayList<Messung>();
			liste.add(messung);
			return erstelleResultTabelle(typ, liste);
		}

		// TODO: Im Moment kommen nur "einstellige" Feldnamen in Frage, sobald
		//       aber Referenzen eingefuehrt werden, wird sowas durchaus
		//       notwendig sein.
		Messwert messwert = messung.getMesswert(feldnamen[0]);
		for (int i = 1; (i < feldnamen.length) && (messwert != null); i++) {
			if (!(messwert.getTyp() instanceof MesswertTypData)) {
				return new Result<Object>(Result.SEVERITY.ERROR,
					IDataAccess.OBJECT_NOT_FOUND, "Dereferenziertes Feid "+
					"ist kein Data-Feld.", params, true);
			}
			MesswertTypData t = (MesswertTypData) messwert.getTyp();
			Messung m = t.getMessung(messwert);
			messwert = m.getMesswert(feldnamen[i]);
		}
		
		if (messwert == null) {
			return new Result<Object>(Result.SEVERITY.ERROR,
				IDataAccess.OBJECT_NOT_FOUND, "Ungueltiger Feldname", params,
				true);
		}
		return new Result<Object>(messwert.getDarstellungswert());
	}
}
