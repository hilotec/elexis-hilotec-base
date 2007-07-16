/*******************************************************************************
 * Copyright (c) 2005-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: AccessControlDefaults.java 2820 2007-07-16 14:32:17Z rgw_ch $
 *******************************************************************************/

package ch.elexis.admin;

/**
 * Hier werden Grundeinstellungen für Zugriffsrechte definiert. Diese werden
 * nur beim allerersten Programmstart (Beim Einrichten der Datenbank), und beim
 * Auswählen des Buttons "Defaults" im Zugriffs-Konfigurationsdialog eingelesen.
 * Rechte, die mit ACTION beginnen, beziehen sich auf Menu- Toolbar- und Shortcut-
 * Actionen. Präfix READ_ ist ein Recht, eine bestimmte Property eines Kontakts
 * (aus den ExtInfo) zu lesen, WRITE_ ist das Recht, eine solche Property zu schreiben.
 * Andere Bezeichnungen sind unterschiedliche Rechte und sollten möglichst Deskriptiv
 * sein (Man muss nicht lange überlegen, welches Recht wohl mit LEISTUNGEN_VERRECHNEN
 * verliehen wird). 
 * Es werden bei der Einrichtung 3 Gruppen angelegt: Alle, Anwender und Admin. Weitere Gruppen können
 * nachträglich beliebig erstellt werden. 
 * @author gerry
 *
 */
public class AccessControlDefaults {
	
	/** 
	 * Obsolete ACL-Constants see below for new code
	 */
	@Deprecated public static final String ACTION_EXIT="exitAction";
	@Deprecated public static final String ACTION_ABOUT="aboutAction";
	@Deprecated public static final String ACTION_HELP="helpAction";
	@Deprecated public static final String ACTION_IMPORT="importAction";
	@Deprecated public static final String ACTION_UPDATE="updateAction";
	@Deprecated public static final String ACTION_NEW_WINDOW="newWindowAction";
	@Deprecated public static final String ACTION_PREFS="prefsAction";
	@Deprecated public static final String ACTION_LOGIN="loginAction";
	@Deprecated public static final String ACTION_TARMEDIMPORT="importTarmedAction";
	@Deprecated public static final String ACTION_ARTIKELIMPORT="importArtikelAction";
	@Deprecated public static final String ACTION_CHANGEPWD="changePasswordAction";
	@Deprecated public static final String ACTION_SHOWPERSPECTIVE="showPerspectiveAction";
	@Deprecated public static final String ACTION_SHOWVIEW="showViewAction";
	@Deprecated public final static String ACTION_CONNECT="connectWizardAction";
	@Deprecated public final static String ACTION_PURGE="PurgeKGAction";
	@Deprecated public final static String ACTION_CHANGEMANDANT="changeMandant";
	@Deprecated public final static String ACTION_DELKONTAKT="deleteKontakt";
	
	@Deprecated public static final String LEISTUNGEN_VERRECHNEN="LeistungenVerrechnen"; 
	
	@Deprecated  public static final String READ_DIGNIQUALI="ReadDigniQuali";
	@Deprecated  public static final String READ_DIGNIQUANTI="ReadDigniQuanti";
	@Deprecated  public static final String READ_SPARTEN="ReadSparten";
	@Deprecated  public static final String READ_QUANT_DIGNITAET="ReadQuantDignität";
	@Deprecated  public static final String READ_QUALI_DIGNITAET="ReadQualiDignität";
	@Deprecated  public static final String READ_SPARTE="ReadSparte";
	@Deprecated public static final String READ_LETZTE_BEHANDLUNG="ReadLetzteBehandlung";
	@Deprecated public static final String WRITE_LETZTE_BEHANDLUNG="WriteLetzteBehandlung";
    @Deprecated  public static final String READ_EAN="ReadEAN";
    @Deprecated  public final static String READ_CURRENCY="ReadWährung";
    @Deprecated  public final static String WRITE_CURRENCY="WriteWährung";
    @Deprecated  public static final String WRITE_EAN="WriteEAN";
    
    @Deprecated public static final String LIST_PATIENTEN="ListPatient";
    @Deprecated public static final String LIST_ADRESSEN="ListAdresse";
    
    @Deprecated public final static String DISPLAY_KONTAKT="DisplayKontakt";
    @Deprecated public final static String DISPLAY_PERSON="DisplayPerson";
    @Deprecated public final static String DISPLAY_PATIENT="DisplayPatient";
    @Deprecated public final static String DISPLAY_ORGANISATION="DisplayOrganisation";
    @Deprecated public final static String DISPLAY_ANWENDER="DisplayAnwender";
    @Deprecated public final static String DISPLAY_MANDANT="DisplayMandant";
    @Deprecated public final static String DISPLAY_ARTIKEL="DisplayArtikel";
    
    @Deprecated public final static String CHANGE_KONTAKTTYPE="ChangeKontaktType";
    @Deprecated public final static String CHANGE_ADMINUSERSTATE="ChangeAdminUserState";
    
    @Deprecated public final static String FORCE_REMOVE="ForceRemoveObjects";

    /**
     * New Code: Use only the following constants
     */
    
    public static final String ACCOUNTING_GLOBAL="AccountingGlobal";
    public static final String ACCOUNTING_READ=ACCOUNTING_GLOBAL+"/read";
    public static final String ACCOUNTING_BILLCREATE=ACCOUNTING_GLOBAL+"/createBills";
    public static final String ACCOUNTING_BILLMODIFY= ACCOUNTING_GLOBAL+"/modifyBills";
        
    public static final String ACL_USERS="Zugriff/Rechte erteilen";
    public static final String DELETE= "Löschen";
    public final static String DELETE_FORCED="Löschen/Absolut";
    public static final String DELETE_BILLS= "Löschen/Rechnungen";
    public static final String DELETE_MEDICATION="Löschen/Dauermedikation";
    public static final String DELETE_LABITEMS="Löschen/Laborwerte";
    
    public static final String DATA	= "Daten";
    public static final String KONTAKT= DATA+"/Kontakt";
    public static final String KONTAKT_DISPLAY=KONTAKT+"/Anzeigen";
    public static final String KONTAKT_EXPORT= KONTAKT+"/Exportieren";
    public static final String KONTAKT_INSERT=KONTAKT+"/Erstellen";
    public static final String KONTAKT_MODIFY=KONTAKT+ "/Ändern";
    public static final String KONTAKT_DELETE= DELETE+"/Kontakt";
    
    public static final String PATIENT=DATA+"/Patient";
    public static final String PATIENT_DISPLAY=PATIENT+"/Anzeigen";
    public static final String PATIENT_INSERT=PATIENT+"/Erstellen";
    public static final String PATIENT_MODIFY=PATIENT+ "/Ändern";
    public static final String MEDICATION_MODIFY=PATIENT+"/Medikation ändern";
    public static final String LAB_SEEN=PATIENT+"/Labor abhaken";
    
    public static final String MANDANT=DATA+"/Mandant";
    public static final String MANDANT_CREATE= MANDANT+"/Erstellen";
    
    public static final String USER=DATA+"/Anwender";
    public static final String USER_CREATE= USER+"/Erstellen";
    
    public static final String LEISTUNGEN= "Leistungen";
    public static final String LSTG_VERRECHNEN= LEISTUNGEN+"/Verrechnen";
    
    public static final String KONS="Konsultation";
    public static final String KONS_CREATE= KONS+"/Erstellen";
    public static final String KONS_EDIT= KONS+"/Barbeiten";
    public static final String KONS_DELETE= DELETE+"/Konsultation";
    
    // allows to change the text of an already billed consultation
    // TODO: maybe we should just use KONS_EDIT 
    public static final String ADMIN_KONS_EDIT_IF_BILLED = "Admin/Konsultation/Ändern/Verrechnet";
    public static final String ADMIN_VIEW_ALL_REMINDERS = "Admin/Reminders/Alle Anzeigen";
    
    public static final String DOCUMENT= "Dokumente";
    public static final String DOCUMENT_CREATE= DOCUMENT+"/Erstellen";
    public static final String DOCUMENT_TEMPLATE= DOCUMENT+"/Vorlagen ändern";
    public static final String DOCUMENT_SYSTEMPLATE= DOCUMENT+"/Systemvorlagen ändern";
    
    public static final String ACTIONS= "Aktionen";
    public static final String AC_EXIT= ACTIONS+"/Beenden";
    public static final String AC_ABOUT=ACTIONS+"/Über";
    public static final String AC_HELP= ACTIONS+"/Hilfe";
    //public static final String AC_UPDATE=ACTIONS+ "/Update";
    public static final String AC_IMORT= ACTIONS+"/Fremddatenimport";
    public static final String AC_PREFS= ACTIONS+"/Einstellungen";
    public static final String AC_LOGIN= ACTIONS+"/Anmelden";
    public static final String AC_CONNECT= ACTIONS+"/Datenbankverbindung";
    public static final String AC_PURGE= ACTIONS+"/Datenbankbereinigung";
    public static final String AC_CHANGEMANDANT= ACTIONS+"/Mandantwechsel";
    public static final String AC_NEWWINDOW = ACTIONS+ "/NeuesFenster";
    public static final String AC_SHOWPERSPECTIVE= ACTIONS +"/Perspektivenauswahl";
    public static final String AC_SHOWVIEW= ACTIONS +"/Viewauswahl";
    
	public static final String[] Alle={AC_EXIT,AC_ABOUT,AC_HELP,AC_LOGIN,"LoadInfoStore"};
	
    public static final String[] Anwender={DATA,ACTIONS,DOCUMENT,KONS,LEISTUNGEN};
	
    // Admin has all rights anyway
    /*
    public static final String[] Admin={ACTION_TARMEDIMPORT,
    	DISPLAY_ANWENDER,DISPLAY_MANDANT,ACTION_CONNECT,ACTION_PURGE,
    	CHANGE_ADMINUSERSTATE,ACTION_ARTIKELIMPORT,ACTION_PREFS,FORCE_REMOVE,
    	WRITE_EAN,WRITE_CURRENCY,ACTION_DELKONTAKT,ACTION_UPDATE};
    	*/
}
