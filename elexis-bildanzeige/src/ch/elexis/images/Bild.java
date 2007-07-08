/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: Bild.java 2759 2007-07-08 11:28:43Z rgw_ch $
 *******************************************************************************/

package ch.elexis.images;

import java.io.ByteArrayInputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;

import ch.elexis.Desk;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionInfo;

public class Bild extends PersistentObject {
	public static final String DBVERSION="1.1.0";
	public static final String TABLENAME="BILDANZEIGE";
	public static final String createDB=
		"CREATE TABLE "+TABLENAME+" ("+
		"ID				VARCHAR(25) primary key,"+
		"deleted		CHAR(1) default '0',"+
		"PatID			VARCHAR(25),"+
		"Datum			CHAR(8),"+
		"Title 			VARCHAR(30),"+	
		"Info			TEXT,"+
		"Keywords		VARCHAR(80),"+
		"isRef			char(2),"+
		"Bild			BLOB);"+
		"CREATE INDEX BANZ1 ON "+TABLENAME+" (PatID);"+
		"CREATE INDEX BANZ2 ON "+TABLENAME+" (Keywords);" +
		"INSERT INTO "+TABLENAME+" (ID, TITLE) VALUES ('1','"+DBVERSION+"');";
		

	static{
		addMapping(
			TABLENAME,"PatID","Datum=S:D:Datum","Titel=Title","Keywords","Bild","Info"
				);
		Bild start=load("1");
		if(start==null){
			init();
		}else{
			VersionInfo vi=new VersionInfo(start.get("Titel"));
			if(vi.isOlder(DBVERSION)){
				if(vi.isOlder("1.1.0")){
					PersistentObject.j.exec("ALTER TABLE "+TABLENAME+" ADD deleted CHAR(1) default '0';");
					start.set("Titel", DBVERSION);
				}else{
					MessageDialog.openError(Desk.theDisplay.getActiveShell(), "Versionskonsflikt", 
						"Die Datentabelle für Bildanzeige hat eine zu alte Versionsnummer. Dies kann zu Fehlern führen");
				}
			}
		}
	}
	
	/**
	   * Tabelle neu erstellen
	   */
	  public static void init(){
			try{
				ByteArrayInputStream bais=new ByteArrayInputStream(createDB.getBytes("UTF-8"));
				j.execScript(bais,true, false);
			}catch(Exception ex){
				ExHandler.handle(ex);
			}
	  }

	  public Bild(Patient patient, String Titel, byte[] data){
		  if(patient==null){
			  MessageDialog.openError(Desk.theDisplay.getActiveShell().getShell(), "Kein Patient ausgewählt", 
					  "Sie müssen einen Patienten auswählen, um ein Bildzuzuordnen");
			  return;
		  }
		  create(null);
		  set(new String[]{"PatID","Titel","Datum"},patient.getId(),Titel,new TimeTool().toString(TimeTool.DATE_COMPACT));
		  setBinary("Bild", data);
	  }
	  public Patient getPatient(){
		  return Patient.load(get("PatID"));
	  }
	@Override
	public String getLabel() {
		StringBuilder sb=new StringBuilder();
		sb.append(checkNull(get("Titel"))).append(" (").append(get("Datum")).append(")"); 
		return sb.toString();
	}

	/**
	 * Image des Bildes erzeugen. Achtung: dieses muss nach Gebrauch 
	 * mit dispose() wieder entsorgt werden.
	 * @return ein SWT-Image
	 */
	public Image createImage(){
		byte[] data=getBinary("Bild");
		ByteArrayInputStream bais=new ByteArrayInputStream(data);
		Image ret=new Image(Desk.theDisplay,bais);
		return ret;
	}
	public static Bild load(String ID) {
		Bild ret=new Bild(ID);
		if(ret.exists()){
			return ret;
		}
		return null;
	}

	public byte[] getData(){
		return  getBinary("Bild");
	}
	
	@Override
	protected String getTableName() {
		return "BILDANZEIGE";
	}
	protected Bild(String id){
		super(id);
	}
	protected Bild(){}
}
