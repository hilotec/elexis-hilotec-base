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
 * $Id: DBUpdate.java 3786 2008-04-19 09:57:12Z rgw_ch $
 *******************************************************************************/

package ch.elexis.util;

import java.util.List;

import ch.elexis.Hub;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionInfo;
import ch.rgw.tools.JdbcLink.Stm;

/**
 * Änderungen der Datenbank im Rahmen eines update durchführen.
 * @author Gerry
 *
 */
public class DBUpdate {

	  static final String[] versions={"1.3.0","1.3.1","1.3.2","1.3.3","1.3.4","1.3.5","1.3.6","1.3.7",
		  							"1.3.8","1.3.9","1.3.10","1.3.11","1.3.12","1.3.13",
		  							"1.4.0","1.4.1","1.4.2","1.4.3","1.4.4","1.4.5","1.4.6",
		  							"1.5.0","1.6.0","1.6.1","1.6.2","1.6.3","1.6.4",
		  							"1.7.0"};
	  static final String[] cmds={"CREATE TABLE EIGENLEISTUNGEN("+
			"ID			VARCHAR(25) primary key,"+
			"Code		VARCHAR(20),"+
			"Bezeichnung VARCHAR(80),"+
			"EK_PREIS	CHAR(6),"+
			"VK_PREIS	CHAR(6),"+
			"ZEIT		CHAR(4)	);",
			"ALTER TABLE PATIENT_ARTIKEL_JOINT DROP COLUMN PATIENTID;"+
			"ALTER TABLE PATIENT_ARTIKEL_JOINT add ID VARCHAR(25);"+
			"ALTER TABLE PATIENT_ARTIKEL_JOINT add PATIENTID VARCHAR(25);"+
			"CREATE INDEX PAJ1 ON PATIENT_ARTIKEL_JOINT (PATIENTID);",
			
			"CREATE TABLE HEAP2("+
			"ID			VARCHAR(50) primary key,"+
			"Contents   BLOB);",
			
			"ALTER TABLE FAELLE ADD EXTINFO BLOB;"+
			"ALTER TABLE LEISTUNGEN ADD SCALE CHAR(4) DEFAULT '100';"+
			"ALTER TABLE LEISTUNGEN ADD DETAIL BLOB;",
			
			"ALTER TABLE LEISTUNGEN ADD VK_TP CHAR(6);"+
			"ALTER TABLE LEISTUNGEN ADD VK_SCALE CHAR(6);"+
			"ALTER TABLE KONTAKT ADD TITEL VARCHAR(20);",
			
			"ALTER TABLE FAELLE ADD Status VARCHAR(80);"+
			"ALTER TABLE REZEPTE ADD RpZusatz VARCHAR(80);"+
			"ALTER TABLE AUF ADD AUFZusatz VARCHAR(80)",
			
			"ALTER TABLE REZEPTE ADD BriefID VARCHAR(25);"+
			"ALTER TABLE AUF ADD BriefID VARCHAR(25);",
			
			"ALTER TABLE ARTIKEL ADD Name_intern VARCHAR(80);",
			
			"ALTER TABLE PATIENT_ARTIKEL_JOINT ADD REZEPTID VARCHAR(25);"+
			"ALTER TABLE PATIENT_ARTIKEL_JOINT ADD DATEFROM CHAR(8);"+
			"ALTER TABLE PATIENT_ARTIKEL_JOINT ADD DATEUNTIL CHAR(8);"+
			"ALTER TABLE PATIENT_ARTIKEL_JOINT ADD ANZAHL CHAR(3);"+
			"CREATE INDEX PAJ2 ON PATIENT_ARTIKEL_JOINT(REZEPTID);",
			
			"ALTER TABLE REMINDERS ADD RESPONSIBLE VARCHAR(25);"+
			"CREATE INDEX rem3 ON REMINDERS (RESPONSIBLE);"+
			"ALTER TABLE TARMED ADD NICKNAME VARCHAR(25);",
			
			"ALTER TABLE RECHNUNGEN ADD STATUSDATUM CHAR(8);"+
			"CREATE TABLE USERCONFIG("+
					"UserID		VARCHAR(25) primary key,"+
					"Param		VARCHAR(80),"+
					"Value		BLOB);"+
			"CREATE INDEX UCFG ON USERCONFIG(Param);",
			
			"ALTER TABLE USERCONFIG DROP Value;"+
			"ALTER TABLE USERCONFIG ADD VALUE TEXT;",
			
			"DROP TABLE USERCONFIG;"+
			"CREATE TABLE USERCONFIG("+
			"UserID		VARCHAR(25),"+
			"Param		VARCHAR(80),"+
			"Value		TEXT);"+
			"CREATE INDEX UCFG ON USERCONFIG(Param);"+
			"CREATE INDEX UCFG2 ON USERCONFIG(UserID)",
			
			"S1",
			
			"ALTER TABLE BRIEFE DROP format;"+
			"ALTER TABLE BRIEFE ADD MimeType VARCHAR(80);"+
			"ALTER TABLE BRIEFE ADD Path TEXT;",
			
			"ALTER TABLE KONTO ADD RechnungsID VARCHAR(25);"+
			"ALTER TABLE KONTO ADD ZahlungsID  VARCHAR(25);"+
			"ALTER TABLE TARMED ADD GueltigVon CHAR(8);"+
			"ALTER TABLE TARMED ADD GueltigBis CHAR(8);",
			
			"ALTER TABLE LABORWERTE ADD Flags VARCHAR(10);",
			
			"CREATE TABLE LABGROUPS( ID VARCHAR(25) primary key, name VARCHAR(30));"+ 
			"CREATE TABLE LABGROUP_ITEM_JOINT(GroupID VARCHAR(25),"+
					"ItemID VARCHAR(25), Comment TEXT );",
					
			// 1.4.4
			"ALTER TABLE REMINDERS ADD OriginID VARCHAR(25);"+
			"CREATE TABLE REMINDERS_RESPONSIBLE_LINK("+
					"ID				VARCHAR(25) primary key,"+
					"ReminderID		VARCHAR(25),"+
					"ResponsibleID	VARCHAR(25)"+
				");"+
			"CREATE INDEX rrl1 on REMINDERS_RESPONSIBLE_LINK (ReminderID);"+
			"CREATE INDEX rrl2 on REMINDERS_RESPONSIBLE_LINK (ResponsibleID);"+
			"ALTER TABLE PATIENT_ARTIKEL_JOINT ADD ExtInfo BLOB;",
			
			// 1.4.5
			"ALTER TABLE ARTIKEL ADD Klasse VARCHAR(80);",

			// 1.4.6
			"ALTER TABLE LABORITEMS MODIFY titel VARCHAR(80);"+
			"ALTER TABLE LABORITEMS MODIFY kuerzel VARCHAR(80);",
			
			// 1.5.0
			"ALTER TABLE HEAP MODIFY ID VARCHAR(80);",
			
			
			// 1.6.0
			"ALTER TABLE HEAP ADD datum CHAR(8);"+
			"ALTER TABLE KONTAKT ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE KONTAKT_ADRESS_JOINT ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE FAELLE ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE BEHANDLUNGEN ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE LABORWERTE ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE ARTIKEL ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE PATIENT_ARTIKEL_JOINT ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE KONTO ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE LEISTUNGEN ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE LEISTUNGSBLOCK ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE DIAGNOSEN ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE BEHDL_DG_JOINT ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE BRIEFE ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE RECHNUNGEN ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE ZAHLUNGEN ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE REMINDERS ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE REMINDERS_RESPONSIBLE_LINK ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE BBS ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE LABORITEMS ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE LABGROUPS ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE REZEPTE ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE HEAP ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE AUF ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE EIGENLEISTUNGEN ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE HEAP2 ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE HEAP2 MODIFY ID VARCHAR(80);"+
			"ALTER TABLE HEAP2 ADD datum CHAR(8);"+
			"ALTER TABLE TARMED ADD deleted CHAR(1) default '0';"+
			"ALTER TABLE LABORWERTE ADD Origin VARCHAR(30);"+
			"INSERT INTO TARMED (ID,Nickname) VALUES ('Version','1.0.1');"+
			"CREATE TABLE LOGS(ID			VARCHAR(25) primary key,"+
						"OID		VARCHAR(80),"+
						"datum		CHAR(8),"+
						"typ		VARCHAR(20),"+				
						"userID		VARCHAR(25),"+
						"station	VARCHAR(25),"+
						"ExtInfo		BLOB);",
						
			// 1.6.1
			"CREATE TABLE XID("+
					"ID			VARCHAR(25) primary key,"+
					"deleted	CHAR(1) default '0',"+
					"type		VARCHAR(80),"+
					"object		VARCHAR(25),"+
					"domain		VARCHAR(255),"+
					"domain_id	VARCHAR(255),"+
					"quality	CHAR(1) default '0'"+	
			");"+
			"CREATE INDEX XIDIDX1 on XID(domain);"+
			"CREATE INDEX XIDIDX2 on XID(domain_id);"+
			"CREATE INDEX XIDIDX3 on XID(object);",
			
			// 1.6.2
			"ALTER TABLE AUF ADD DatumAUZ CHAR(8);"+
			"ALTER TABLE ARTIKEL ADD LastUpdate CHAR(8);",
			
			// 1.6.3.
			"ALTER TABLE ARTIKEL ADD EAN VARCHAR(15);",
			
			// 1.6.4
			"ALTER TABLE HEAP ADD lastupdate CHAR(14);"+
			"ALTER TABLE HEAP2 ADD lastupdate CHAR(14)",
			
			// 1.7.0
			"CREATE TABLE ETIKETTEN("+  
			"ID          VARCHAR(25) primary key,"+
			"Image       VARCHAR(25),"+
			"deleted     CHAR(1) default '0',"+
			"Name        VARCHAR(40),"+
			"foreground  CHAR(6),"+
			"background  CHAR(6)"+
			");"+
			"CREATE INDEX ETIKETTE1 on ETIKETTEN(Name);"+

			"CREATE TABLE DBIMAGE ("+
			"ID				VARCHAR(25) primary key,"+
			"deleted		CHAR(1) default '0',"+
			"Datum			CHAR(8),"+
			"Title 			VARCHAR(80),"+	
			"Bild			BLOB)"+
			");"+
			"CREATE INDEX DBIMAGE1 on DBIMAGE(Title);"	
			
	  };
	  static Log log=Log.get("DBUpdate");
	  
	  static VersionInfo vi;
	  
	  /**
	   * Diese Methode erledigt Datenbankänderungen, die im Rahmen eines Updates nötig sind
	   * Versions enthält eine Versionsliste, cmds ein Kommando für jede dieser Versionen.
	   * Ein Kommando ist entweder <ul>
	   * <li>direkt ein SQL-Befehl,
	   * <li>eine ; getrennte Liste von SQL-Befehlen
	   * </li>
	   */
	  public static void doUpdate()
	   {   
	        final JdbcLink j=PersistentObject.getConnection();
	        String dbv=Hub.globalCfg.get("dbversion",null);
	        if(dbv==null)
	        {   log.log("Kann keine Version lesen",Log.ERRORS);
	            SWTHelper.alert("Fataler Fehler bei Datenbank-Update","Kann keine Versionsinformation lesen. Abbruch");
	            System.exit(0);
	        }
	        else
	        {   vi=new VersionInfo(dbv);
	        }
	        Stm stm=j.getStatement();
	        for(int i=0;i<versions.length;i++)
	        {   if(vi.isOlder(versions[i]))
	            {   String[] cmd=cmds[i].split(";");
	            	log.log("Update auf "+versions[i],Log.WARNINGS);
	            	for(int c=0;c<cmd.length;c++){
	            	    if(cmd[c].matches("S[0-9]+")) {
	                        int cnum=Integer.parseInt(cmd[c].substring(1));
	                        switch(cnum) {
	                            case 1:
	                            {
	                            	Query<Rechnung> qbe=new Query<Rechnung>(Rechnung.class);
	                        		List<Rechnung> alle=qbe.execute();
	                        		for(Rechnung rn:alle){
	                        			List<String> traces=rn.getTrace(Rechnung.STATUS_CHANGED);
	                        			if(traces.isEmpty()){
	                        				rn.set("StatusDatum", rn.getDatumRn());
	                        				
	                        			}else{
		                        			String trace=traces.get(traces.size()-1);
		                        			String[] split=trace.split(", *");
		                        			TimeTool tim=new TimeTool(split[0]);
		                        			rn.set("StatusDatum",tim.toString(TimeTool.DATE_GER));
	                        			}
	                        		}
	                        		break;
	                            }
	                        }
	                        
	                    } else {	// direkt SQL-Kommando
	            	        stm.exec(j.translateFlavor(cmd[c]));
	            	    }
	            	}
	            }
	        }
	        Hub.globalCfg.set("dbversion",Hub.DBVersion);
	        Hub.globalCfg.set("ElexisVersion",Hub.Version);
	        Hub.globalCfg.flush();
	        PersistentObject.getConnection().releaseStatement(stm);
	    }

}
