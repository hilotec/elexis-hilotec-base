/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: AccessControlDefaults.java 4967 2009-01-18 16:52:11Z rgw_ch $
 *******************************************************************************/

package ch.elexis.admin;

/**
 * Hier werden Grundeinstellungen für Zugriffsrechte definiert. Diese werden nur beim allerersten
 * Programmstart (Beim Einrichten der Datenbank), und beim Auswählen des Buttons "Defaults" im
 * Zugriffs-Konfigurationsdialog eingelesen. Rechte, die mit ACTION beginnen, beziehen sich auf
 * Menu- Toolbar- und Shortcut- Actionen. Präfix READ_ ist ein Recht, eine bestimmte Property eines
 * Kontakts (aus den ExtInfo) zu lesen, WRITE_ ist das Recht, eine solche Property zu schreiben.
 * Andere Bezeichnungen sind unterschiedliche Rechte und sollten möglichst Deskriptiv sein (Man muss
 * nicht lange überlegen, welches Recht wohl mit LEISTUNGEN_VERRECHNEN verliehen wird). Es werden
 * bei der Einrichtung 3 Gruppen angelegt: Alle, Anwender und Admin. Weitere Gruppen können
 * nachträglich beliebig erstellt werden.
 * 
 * @author gerry
 * 
 */
public class AccessControlDefaults {
	
	public static final ACE ADMIN = new ACE(ACE.ACE_ROOT, "Admin", "Administration");
	public static final ACE ACCOUNTING = new ACE(ACE.ACE_ROOT, "Rechnungen", "Rechnungen");
	public static final ACE ACCOUNTING_CREATE = new ACE(ACCOUNTING, "erstellen", "erstellen");;
	public static final ACE ACCOUNTING_MODIFY = new ACE(ACCOUNTING, "bearbeiten", "bearbeiten");
	
	public static final ACE ACCOUNTING_GLOBAL =
		new ACE(ACE.ACE_ROOT, "AccountingGlobal", "Globales Verrechnen");
	public static final ACE ACCOUNTING_READ = new ACE(ACCOUNTING_GLOBAL, "read", "lesen");
	public static final ACE ACCOUNTING_BILLCREATE =
		new ACE(ACCOUNTING_GLOBAL, "createBills", "Rnn. erstellen");
	public static final ACE ACCOUNTING_BILLMODIFY =
		new ACE(ACCOUNTING_GLOBAL, "modifyBills", "Rnn. ändern");
	
	public static final ACE ACE_ACCESS = new ACE(ACE.ACE_ROOT, "Zugriff", "Zugriff");
	public static final ACE ACL_USERS = new ACE(ACE_ACCESS, "Rechte erteilen", "Rechte erteilen");
	public static final ACE DELETE = new ACE(ACE.ACE_ROOT, "Löschen", "Löschen");
	public final static ACE DELETE_FORCED = new ACE(DELETE, "Absolut", "absolut");
	public static final ACE DELETE_BILLS = new ACE(DELETE, "Rechnungen", "Rechnungen");
	public static final ACE DELETE_MEDICATION =
		new ACE(DELETE, "Dauermedikation", "Dauermedikation");
	public static final ACE DELETE_LABITEMS = new ACE(DELETE, "Laborwerte", "Laborwerte");
	
	public static final ACE DATA = new ACE(ACE.ACE_ROOT, "Daten", "Daten");
	public static final ACE KONTAKT = new ACE(DATA, "Kontakt", "Kontakt");
	public static final ACE KONTAKT_DISPLAY = new ACE(KONTAKT, "Anzeigen", "Anzeigen");
	public static final ACE KONTAKT_EXPORT = new ACE(KONTAKT, "Exportieren", "exportieren");
	public static final ACE KONTAKT_INSERT = new ACE(KONTAKT, "Erstellen", "erstellen");
	public static final ACE KONTAKT_MODIFY = new ACE(KONTAKT, "Ändern", "ändern");
	public static final ACE KONTAKT_DELETE = new ACE(DELETE, "Kontakt", "Kontakt");
	public static final ACE KONTAKT_ETIKETTE = new ACE(KONTAKT, "etikettieren", "Sticker ändern");
	
	public static final ACE PATIENT = new ACE(DATA, "Patient", Plafs.get("main::Patient"));
	public static final ACE PATIENT_DISPLAY = new ACE(PATIENT, "Anzeigen", "anzeigen");
	public static final ACE PATIENT_INSERT = new ACE(PATIENT, "/Erstellen", "erstellen");
	public static final ACE PATIENT_MODIFY = new ACE(PATIENT, "Ändern", "ändern");
	public static final ACE MEDICATION_MODIFY =
		new ACE(PATIENT, "Medikation ändern", "Medikation ändern");
	public static final ACE LAB_SEEN = new ACE(PATIENT, "Labor abhaken", "Labor abhaken");
	
	public static final ACE MANDANT = new ACE(DATA, "Mandant", "Mandant");
	public static final ACE MANDANT_CREATE = new ACE(MANDANT, "Erstellen", "erstellen");
	
	public static final ACE USER = new ACE(DATA, "Anwender", "Anwender");
	public static final ACE USER_CREATE = new ACE(USER, "Erstellen", "erstellen");
	
	public static final ACE LEISTUNGEN = new ACE(ACE.ACE_ROOT, "Leistungen", "Leistungen");
	public static final ACE LSTG_VERRECHNEN = new ACE(LEISTUNGEN, "Verrechnen", "verrechnen");
	
	public static final ACE KONS =
		new ACE(ACE.ACE_ROOT, "Konsultation", Plafs.get("main::Konsultation"));
	public static final ACE KONS_CREATE = new ACE(KONS, "Erstellen", "erstellen");
	public static final ACE KONS_EDIT = new ACE(KONS, "Bearbeiten", "bearbeiten");
	public static final ACE KONS_DELETE = new ACE(DELETE, "Konsultation", "Konsultation");
	public static final ACE KONS_REASSIGN = new ACE(KONS, "zuordnen", "zuordnen");
	
	public static final ACE SCRIPT = new ACE(ACE.ACE_ROOT, "Script", "Script");
	public static final ACE SCRIPT_EXECUTE = new ACE(SCRIPT, "ausführen", "ausführen");
	public static final ACE SCRIPT_EDIT = new ACE(SCRIPT, "bearbeiten", "bearbeiten");
	
	public static final ACE CASE = new ACE(ACE.ACE_ROOT, "Fall", Plafs.get("main::Fall"));
	public static final ACE CASE_MODIFY = new ACE(CASE, "Ändern", "ändern");
	
	// allows to change the text of an already billed consultation
	// TODO: maybe we should just use KONS_EDIT
	public static final ACE ADMIN_KONS =
		new ACE(ADMIN, "Konsultation", Plafs.get("main::Konsultation"));
	public static final ACE ADMIN_REMINDERS = new ACE(ADMIN, "Reminders", "Reminders");
	public static final ACE ADMIN_BILLS = new ACE(ADMIN, "Rechnungen", "Rechnungen");
	
	public static final ACE ADMIN_KONS_EDIT_IF_BILLED =
		new ACE(ADMIN_KONS, "change_billed", "Verrechnete ändern");
	public static final ACE ADMIN_VIEW_ALL_REMINDERS =
		new ACE(ADMIN_REMINDERS, "viewAll", "Alle Anzeigen");
	public static final ACE ADMIN_CHANGE_BILLSTATUS_MANUALLY =
		new ACE(ADMIN_BILLS, "changeManually", "manuelle Statusänderung");
	
	public static final ACE DOCUMENT = new ACE(ACE.ACE_ROOT, "Dokumente", "Dokumente");
	public static final ACE DOCUMENT_CREATE = new ACE(DOCUMENT, "Erstellen", "erstellen");
	public static final ACE DOCUMENT_TEMPLATE =
		new ACE(DOCUMENT, "Vorlagen ändern", "Vorlagen ändern");
	public static final ACE DOCUMENT_SYSTEMPLATE =
		new ACE(DOCUMENT, "Systemvorlagen ändern", "Systemvorlagen ändern");
	
	public static final ACE ACTIONS = new ACE(ACE.ACE_ROOT, "Aktionen", "Aktionen");
	public static final ACE AC_EXIT = new ACE(ACTIONS, "Beenden", "beenden");
	public static final ACE AC_ABOUT = new ACE(ACTIONS, "Über", "über");
	public static final ACE AC_HELP = new ACE(ACTIONS, "Hilfe", "Hilfe");
	
	public static final ACE AC_IMORT = new ACE(ACTIONS, "Fremddatenimport", "Fremddatenimport");
	public static final ACE AC_PREFS = new ACE(ACTIONS, "Einstellungen", "Einstellungen");
	public static final ACE AC_LOGIN = new ACE(ACTIONS, "Anmelden", "Anmelden");
	public static final ACE AC_CONNECT =
		new ACE(ACTIONS, "Datenbankverbindung", "Datenbankverbindung");
	public static final ACE AC_PURGE =
		new ACE(ACTIONS, "Datenbankbereinigung", "Datenbankbereinigung");
	public static final ACE AC_CHANGEMANDANT = new ACE(ACTIONS, "Mandantwechsel", "Mandantwechsel");
	public static final ACE AC_NEWWINDOW = new ACE(ACTIONS, "NeuesFenster", "NeuesFenster");
	public static final ACE AC_SHOWPERSPECTIVE =
		new ACE(ACTIONS, "Perspektivenauswahl", "Perspektivenauswahl");
	public static final ACE AC_SHOWVIEW = new ACE(ACTIONS, "Viewauswahl", "Viewauswahl");
	
	private static final ACE[] Alle =
		{
			AC_EXIT, AC_ABOUT, AC_HELP, AC_LOGIN,
			new ACE(ACE.ACE_ROOT, "LoadInfoStore", "LoadInfoStore")
		};
	
	private static final ACE[] Anwender = {
		DATA, ACTIONS, DOCUMENT, KONS, LEISTUNGEN, ACCOUNTING
	};
	
	public static ACE[] getAlle(){
		return Alle;
	}
	
	public static ACE[] getAnwender(){
		return Anwender;
	}

}
