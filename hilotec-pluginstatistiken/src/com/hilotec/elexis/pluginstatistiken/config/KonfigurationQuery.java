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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.data.Patient;
import ch.elexis.data.Query;

import com.hilotec.elexis.pluginstatistiken.Datensatz;

/**
 * Einzelne Abfrage
 * 
 * @author Antoine Kaufmann
 */
public class KonfigurationQuery {
	String title;
	KonfigurationWhere where = null;
	ArrayList<String> colsName;
	ArrayList<String> colsSource;

	/**
	 * Neue Abfrage anlegen
	 * 
	 * @param t Titel der Abfrage
	 */
	public KonfigurationQuery(String t) {
		title = t;
		colsName = new ArrayList<String>();
		colsSource = new ArrayList<String>();
	}
	
	/**
	 * Der Abfrage eine neue Spalte anfuegen
	 * 
	 * @param name   Name der Spalte
	 * @param source Quelle fuer diese Spalte (fuer IDataAcees-Schnittstelle)
	 */
	public void addCol(String name, String source) {
		colsName.add(name);
		colsSource.add(source);
	}
	
	/**
	 * Liste mit den Namen aller Spalten
	 */
	public ArrayList<String> getColNames() {
		return colsName;
	}
	
	/**
	 * Quellen der Spalten in Liste zurueckgeben
	 * @return
	 */
	public ArrayList<String> getColSources() {
		return colsSource;
	}
	
	/**
	 * Where-Klausel fuer diese Abfrage setzen
	 */
	public void setWhere(KonfigurationWhere w) {
		where = w;
	}
	
	/**
	 * @return Titel dieser Abfrage
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Daten heraussuchen
	 * 
	 * @param startDatum Startdatum des angegebenen Bereichs
	 * @param endDatum   Enddatum des angegebenen Bereichs
	 * @param monitor    Archie-ProgressMonitor der es ermoeglcht, dem Benutzer
	 *                   den auktuellen Status der Abfrage angezeigt werden
	 *                   kann.
	 *
	 * @return Liste mit den gefundenen Datensaetzen
	 */
	public List<Datensatz> getDaten(String startDatum, String endDatum,
		IProgressMonitor monitor)
	{
		List<Datensatz> data = new LinkedList<Datensatz>();
		
		Query<Patient> q = new Query<Patient>(Patient.class);
		monitor.beginTask("Suche Patienten", 1);
		List<Patient> patienten = q.execute();
		
		monitor.beginTask("Verarbeite Patienten", patienten.size());
		for (Patient p: patienten) {
			Datensatz ds = new Datensatz(this, p, startDatum, endDatum);
			if (ds.isValid() && (where == null || where.matches(ds))) {
				data.add(ds);
			}
			monitor.worked(1);
		}
		return data;
	}
}
