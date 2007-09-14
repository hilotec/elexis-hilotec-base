/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: PreferenceConstants.java 3151 2007-09-14 09:32:43Z danlutz $
 *******************************************************************************/

package ch.elexis.preferences;

/**
 * Konstanten für die Namen der verschiedenen Einstellungen
 */
public class PreferenceConstants {

	// Datenbank
    public static final String DB_CLASS=	"verbindung/Connector";
    public static final String DB_CONNECT=	"verbindung/Connectstring";
    public static final String DB_USERNAME=	"verbindung/Username";
    public static final String DB_PWD=		"verbindung/Passwort";
    public static final String DB_TYP=		"verbindung/Datenbanktyp";
	public static final String DB_NAME = 	"verbindung/Datenbankname";
	public final static String DB_WIZARD=	"verbindung/ass";

	// Ablauf
	public static final String ABL_LANGUAGE="ablauf/sprache";
	public static final String ABL_LOGFILE=	"ablauf/Log-Datei";
	public static final String ABL_LOGFILE_MAX_SIZE = "ablauf/logfile_max_size";
	public static final String ABL_LOGLEVEL="ablauf/LogLevel";
	public static final String ABL_LOGALERT = "ablauf/LogAlertLevel";
    public static final String ABL_TRACE=	"ablauf/Trace";
    public static final String ABL_BASEPATH="ablauf/basepath";
    public static final String ABL_CACHELIFETIME="ablauf/cachelifetime";
    public static final String ABL_UPDATESITE="ablauf/updatesite";
    public static final String ABL_HEARTRATE="ablauf/heartrate";
    
    // Sample
	public static final String P_PATH = 	"sample/pathPreference";
	public static final String P_BOOLEAN = 	"sample/booleanPreference";
	public static final String P_CHOICE = 	"sample/choicePreference";
	public static final String P_STRING = 	"sample/stringPreference";
	
	// Texterstellung
	public static final String P_TEXTMODUL=	"briefe/Textmodul";
	public final static String P_OOBASEDIR=  "briefe/OOBasis";
	

	public static final String ACC_GROUPS=		"groupNames";

	// Zugriffsrechte -> Diese gehören sowieso nach AccessControlDefaults
	@Deprecated public static final String ACC_EXIT=		"exitAction";
	@Deprecated public static final String ACC_LOGIN=		"loginAction";
	@Deprecated public static final String ACC_PREFS=		"prefsAction";
	@Deprecated public static final String ACC_SHOWVIEW=	"showViewAction";
	
	// Briefe
	public static final String DOC_CATEGORY=	"dokumente/kategorien";
	// Sidebar/Perspektivenauswahl
	public final static String SIDEBAR=			"sidebar/pages";
	public static final String SHOWSIDEBAR = 	"sidebar/show";
	public static final String SHOWPERSPECTIVESELECTOR= "sidebar/perspective";
	public static final String SHOWTOOLBARITEMS ="sidebar/toolbaritems";
	
	// Persönliche Präferenzen
	public static final String USR_DEFCASELABEL= "fall/std_label";
	public static final String USR_DEFCASEREASON= "fall/std_grund";
	public static final String USR_DEFLAW=		  "fall/std_gesetz";
	public static final String USR_REMINDERCOLORS="reminder/colors";
	public static final String USR_REMINDERSOPEN = "reminder/onlyopen";
	public static final String USR_REMINDEROWN= "reminder/originator";
	public static final String USR_REMINDEROTHERS= "reminder/others";
	public static final String USR_MFU_LIST_SIZE= "mfulist/size";
	
	// Menu item "lock perspectives" (GlobalActions.fixLayoutAction)
	public static final String USR_FIX_LAYOUT = "perspectives/fix_layout";
	public static final boolean USR_FIX_LAYOUT_DEFAULT = false;
	
	// Rechnungen
	public static final String RNN_DEFAULTEXPORTMODE="rechnung/default_target";
	public static final String RNN_DAYSUNTIL1ST="rechnung/days_until_1st";
	public static final String RNN_DAYSUNTIL2ND="rechnung/days_until_2nd";
	public static final String RNN_DAYSUNTIL3RD="rechnung/days_until_3rd";
	public static final String RNN_AMOUNT1ST="rechnung/amount_1st";
	public static final String RNN_AMOUNT2ND="rechnung/amount_2nd";
	public static final String RNN_AMOUNT3RD="rechnung/amount_3rd";
	
	public static final String INVENTORY_ORDER_TRIGGER = "inventory/order_trigger";
	public static final int INVENTORY_ORDER_TRIGGER_BELOW = 0;
	public static final String INVENTORY_ORDER_TRIGGER_BELOW_VALUE = "0";
	public static final int INVENTORY_ORDER_TRIGGER_EQUAL = 1;
	public static final String INVENTORY_ORDER_TRIGGER_EQUAL_VALUE = "1";
	public static final int INVENTORY_ORDER_TRIGGER_DEFAULT = INVENTORY_ORDER_TRIGGER_BELOW;
}
