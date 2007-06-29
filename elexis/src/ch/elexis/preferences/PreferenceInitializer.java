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
 *  $Id: PreferenceInitializer.java 2677 2007-06-29 15:18:00Z rgw_ch $
 *******************************************************************************/
package ch.elexis.preferences;

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Brief;
import ch.elexis.data.PersistentObject;
import ch.elexis.util.Log;

/**
 * Vorgabewerte setzen, wo nötig. Bitte in den drei Funktionen dieser Klasse
 * alle notwendigen Voreinstellungen eintragen.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * Diese Funktion wird automatisch beim Programmstart aufgerufen, und setzt
	 * alle hier definierten Einstellungswerte auf Voreinstellungen, sofern noch keine
	 * vom Anwender erstellten Werte vorhanden sind.
	 * Hier alle Benutzerspezifischen Voreinstellungen eintragen 
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore localstore = new SettingsPreferenceStore(Hub.localCfg);
		
		// Datenbank
		localstore.setDefault(PreferenceConstants.DB_NAME,"hsql");
        localstore.setDefault(PreferenceConstants.DB_CLASS,"org.hsqldb.jdbcDriver");
        String base=getDefaultDBPath();
        
        localstore.setDefault(PreferenceConstants.DB_CONNECT,"jdbc:hsqldb:"+base+"/db");
        localstore.setDefault(PreferenceConstants.DB_USERNAME,"sa");
        localstore.setDefault(PreferenceConstants.DB_PWD,"");
        localstore.setDefault(PreferenceConstants.DB_TYP,"hsqldb");
        
        //Ablauf
        File userhome=new File(System.getProperty("user.home")+File.separator+"elexis");
        if(!userhome.exists()){
        	userhome.mkdirs();
        }
        localstore.setDefault(PreferenceConstants.ABL_LOGFILE,userhome.getAbsolutePath()+File.separator+"elexis.log");
        localstore.setDefault(PreferenceConstants.ABL_LOGFILE_MAX_SIZE, new Integer(Log.DEFAULT_LOGFILE_MAX_SIZE).toString());
        localstore.setDefault(PreferenceConstants.ABL_LOGLEVEL,2);
        localstore.setDefault(PreferenceConstants.ABL_LOGALERT,1);
        localstore.setDefault(PreferenceConstants.ABL_TRACE,"none");
        localstore.setDefault(PreferenceConstants.ABL_BASEPATH, userhome.getAbsolutePath());
        localstore.setDefault(PreferenceConstants.ABL_CACHELIFETIME, PersistentObject.CACHE_DEFAULT_LIFETIME);
        localstore.setDefault(PreferenceConstants.ABL_HEARTRATE, 30);
        Hub.localCfg.set(PreferenceConstants.ABL_BASEPATH, userhome.getAbsolutePath());
        
        // Texterstellung
        if(System.getProperty("os.name").toLowerCase().startsWith("win")){
        	localstore.setDefault(PreferenceConstants.P_TEXTMODUL,"NOA-Text");
        	localstore.setValue(PreferenceConstants.P_TEXTMODUL,"NOA-Text");
		}else{
			localstore.setDefault(PreferenceConstants.P_TEXTMODUL, "OpenOffice Wrapper");
			localstore.setValue(PreferenceConstants.P_TEXTMODUL,"OpenOffice Wrapper");
		}
        File elexisbase=new File(Hub.getBasePath());
    	File fDef=new File(elexisbase.getParentFile().getParent()+"/ooo");
    	String defaultbase;
    	if(fDef.exists()){
    		defaultbase=fDef.getAbsolutePath();
    	}else{
    		defaultbase=Hub.localCfg.get(PreferenceConstants.P_OOBASEDIR,".");
    	}
		System.setProperty("openoffice.path.name",defaultbase);
		localstore.setDefault(PreferenceConstants.P_OOBASEDIR,defaultbase);	
        localstore.setValue(PreferenceConstants.P_OOBASEDIR,defaultbase);
		
		// Dokument
		StringBuilder sb=new StringBuilder();
		sb.append("Alle,").append(Brief.UNKNOWN).append(",").append(Brief.AUZ).append(",")
			.append(Brief.RP).append(",").append(Brief.LABOR);

		localstore.setDefault(PreferenceConstants.DOC_CATEGORY,sb.toString());
        Hub.localCfg.flush();
	}
	public static String getDefaultDBPath() {
		String base;
		File f=new File(Hub.getBasePath()+"/rsc/demodata");
        if(f.exists() && f.canWrite()){
        	base=f.getAbsolutePath();
        }else{
        	base=System.getenv("TEMP");
        	if(base==null){
        		base=System.getenv("TMP");
        		if(base==null){
        			base=System.getProperty("user.home");
        		}
        	}
        	base+="/elexisdata";
        	f=new File(base);
        	if(!f.exists()){
        		f.mkdirs();
        	}
        }
		return base;
	}
	/**
	 * Diese Funktion wird nach dem Erstellen des Display aufgerufen und dient zum
	 * Initialiseren früh benötigter Einstellungen, die bereits ein Display benötigen
	 *
	 */
	public void initializeDisplayPreferences(Display display){
		Desk.theColorRegistry.put(Desk.COL_RED, new RGB(255,0,0));
		Desk.theColorRegistry.put(Desk.COL_GREEN,new RGB(0,255,0));
		Desk.theColorRegistry.put(Desk.COL_BLUE,new RGB(0,0,255));
		Desk.theColorRegistry.put(Desk.COL_SKYTBLUE, new RGB(135,206,250));
		Desk.theColorRegistry.put(Desk.COL_LIGHTBLUE, new RGB(0,191,255));
		Desk.theColorRegistry.put(Desk.COL_BLACK, new RGB(0,0,0));
		Desk.theColorRegistry.put(Desk.COL_GREY, new RGB(0x60,0x60,0x60));
		Desk.theColorRegistry.put(Desk.COL_WHITE, new RGB(255,255,255));
		Desk.theColorRegistry.put(Desk.COL_DARKGREY, new RGB(50,50,50));
		Desk.theColorRegistry.put(Desk.COL_LIGHTGREY, new RGB(180,180,180));
		Desk.theColorRegistry.put(Desk.COL_GREY60, new RGB(153,153,153));
		Desk.theColorRegistry.put(Desk.COL_GREY20, new RGB(51,51,51));
		
		Desk.theFontRegistry.put(Desk.FONT_SMALL, new FontData[]{new FontData("Helvetica",7,SWT.NORMAL),null});
	}
	
	/** 
	 * Diese Funktion wird nach erstem Erstellen der Datenbank (d.h. nur ein einziges Mal) 
	 * aufgerufen und belegt globale Voreinstellungen.
	 * Hier alle im ganzen Netzwerk und für alle Benutzer gültigen Voreinstellungen
	 * eintragen 
	 *
	 */
	public void initializeGlobalPreferences(){
		IPreferenceStore global = new SettingsPreferenceStore(Hub.globalCfg);
		global.setDefault(PreferenceConstants.ABL_TRACE,"none");
		Hub.globalCfg.flush();
	}
	
	/**
	 * Diese Funktion wird ebenfalls nur beim ersten Mal nach dem Erstellen der Datenbank
	 * aufgerufen und erledigt die Vorkonfiguration der Zugriffsrechte
	 * Hier alle Zugriffsrechte voreinstellen
	 */
	public void initializeGrants(){
		Hub.globalCfg.set("groups", "Alle,Admin,Anwender");
		Hub.acl.grant("Alle",AccessControlDefaults.Alle);
       	//Hub.acl.grant("Admin",AccessControlDefaults.Admin);
       	Hub.acl.grant("Anwender",AccessControlDefaults.Anwender);
        Hub.acl.flush();
	}
}
