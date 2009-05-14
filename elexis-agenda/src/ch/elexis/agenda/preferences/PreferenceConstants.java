/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: PreferenceConstants.java 5298 2009-05-14 22:11:19Z rgw_ch $
 *******************************************************************************/

package ch.elexis.agenda.preferences;

public class PreferenceConstants {
	public static final String AG_BEREICHE			= "agenda/bereiche";
	public static final String AG_TERMINTYPEN		= "agenda/TerminTypen";				
	public static final String AG_TERMINSTATUS		= "agenda/TerminStatus";
	public static final String AG_SHOWDELETED		= "agenda/zeige_geloeschte";
	//public static final String AG_USERS				= "agenda/anwender";
	public static final String AG_TYPCOLOR_PREFIX	= "agenda/farben/typ/";
	public static final String AG_STATCOLOR_PREFIX	= "agenda/farben/status/";
	public static final String AG_TYPIMAGE_PREFIX	= "agenda/bilder/typ/";
	public static final String AG_TIMEPREFERENCES	= "agenda/zeitvorgaben";
	public static final String AG_DAYPREFERENCES	= "agenda/tagesvorgaben";
	public static final String AG_SHOW_REASON       = "agenda/show_reason";
	public static final String AG_BEREICH           = "agenda/bereich";
	
	public static final String AG_PIXEL_PER_MINUTE  = "agenda/proportional/pixelperminute";
	public static final String AG_RESOURCESTOSHOW	= "agenda/proportional/bereichezeigen";
	public static final String AG_DAYSTOSHOW		= "agenda/wochenanzeige/tagezeigen";
	
	public static final String AG_SYNC_TYPE			= "agenda/sync/db_type";
	public static final String AG_SYNC_HOST			= "agenda/sync/db_host";
	public static final String AG_SYNC_CONNECTOR	= "agenda/sync/db_connect";
	public static final String AG_SYNC_DBUSER		= "agenda/sync/db_user";
	public static final String AG_SYNC_DBPWD		= "agenda/sync/db_pwd";
	public static final String AG_SYNC_MAPPING		= "agenda/sync/mapping";
	public static final String AG_SYNC_ENABLED		= "agenda/sync/enabled";
	
	public static final String AG_PRINT_APPOINTMENTCARD_TEMPLATE
			= "agenda/print/appointmentcard_template";
	public static final String AG_PRINT_APPOINTMENTCARD_TEMPLATE_DEFAULT
			= "Terminkarte";
	public static final String AG_PRINT_APPOINTMENTCARD_PRINTER_NAME
			= "agenda/print/appointmentcard_printer_name";
	public static final String AG_PRINT_APPOINTMENTCARD_PRINTER_TRAY
			= "agenda/print/appointmentcard_printer_tray";
	public static final String AG_PRINT_APPOINTMENTCARD_DIRECTPRINT
			= "agenda/print/appointmentcard_directprint";
	public static final boolean AG_PRINT_APPOINTMENTCARD_DIRECTPRINT_DEFAULT = false;
}
