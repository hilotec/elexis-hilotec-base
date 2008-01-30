/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: DocHandle.java 3595 2008-01-30 12:01:00Z rgw_ch $
 *******************************************************************************/

package ch.elexis.omnivore.data;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.program.Program;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.omnivore.views.FileImportDialog;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionInfo;

public class DocHandle extends PersistentObject {
	public static final String TABLENAME="CH_ELEXIS_OMNIVORE_DATA";
	public static final String DBVERSION="1.1.0";
	public static final String createDB=
		"CREATE TABLE "+TABLENAME+" ("+
		"ID				VARCHAR(25) primary key,"+
		"deleted        CHAR(1) default '0',"+
		"PatID			VARCHAR(25),"+
		"Datum			CHAR(8),"+
		"Title 			VARCHAR(80),"+	
		"Mimetype		VARCHAR(50),"+
		"Keywords		VARCHAR(80),"+
		"Path			VARCHAR(80),"+
		"Doc			BLOB);"+
		"CREATE INDEX OMN1 ON "+TABLENAME+" (PatID);"+
		"CREATE INDEX OMN2 ON "+TABLENAME+" (Keywords);" +
		"INSERT INTO "+TABLENAME+" (ID, TITLE) VALUES ('1','"+DBVERSION+"');";
		

	static{
		addMapping(
			TABLENAME,"PatID","Datum=S:D:Datum","Titel=Title","Keywords","Path","Doc","Mimetype"
				);
		DocHandle start=load("1");
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
						"Die Datentabelle für Dokumentspeicherung (Omnivore) hat eine zu alte Versionsnummer. Dies kann zu Fehlern führen");
				}
			}
		}
	}
	
	public DocHandle(byte[] doc, Patient pat, String title, String mime, String keyw){
		if((doc==null) || (doc.length==0)){
			SWTHelper.showError("Fehler mit Dokument", "Das Dokument konnte nicht korrekt gelesen werden");
			return;
		}
		create(null);
		setBinary("Doc", doc);
		set(new String[]{"PatID","Datum","Titel","Keywords","Mimetype"},
				pat.getId(),new TimeTool().toString(TimeTool.DATE_GER),title,keyw,mime);
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
	public static DocHandle load(String id){
		DocHandle ret= new DocHandle(id);
		if(ret.exists()){
			return ret;
		}
		return null;
	}
	
	@Override
	public String getLabel() {
		StringBuilder sb=new StringBuilder();
		sb.append(get("Datum")).append(" ").append(get("Titel"));
		return sb.toString();
	}

	@Override
	public boolean delete() {
		return super.delete();
	}
	
	public void execute(){
		try{
			String ext="";
			String typname=get("Mimetype");
			int r=typname.lastIndexOf('.');
			if(r==-1){
				typname=get("Titel");
				r=typname.lastIndexOf('.');
			}
			
			if(r!=-1){
				ext=typname.substring(r+1);
			}
			File temp=File.createTempFile("omni_", "_vore."+ext);
			byte[] b=getBinary("Doc");
			if(b==null){
				SWTHelper.showError("Fehler beim lesen", "Konnte das Dokument nicht aus der Datenbank laden");
				return;
			}
			FileOutputStream fos=new FileOutputStream(temp);
			fos.write(b);
			fos.close();
			Program proggie=Program.findProgram(ext);
			if(proggie!=null){
				proggie.execute(temp.getAbsolutePath());
			}else{
				if(Program.launch(temp.getAbsolutePath())==false){
					Runtime.getRuntime().exec(temp.getAbsolutePath());	
				}
								
			}

		}catch(Exception ex){
			ExHandler.handle(ex);
			SWTHelper.showError("Konnte Datei nicht starten", ex.getMessage());
		}
	}
	@Override
	protected String getTableName() {
		return TABLENAME;
	}
	
	protected DocHandle(String id){
		super(id);
	}
	protected DocHandle(){}
	
	public static void assimilate(String f) {
		Patient act=GlobalEvents.getSelectedPatient();
		if(act==null){
			SWTHelper.showError("Kein Patient ausgewählt", "Bitte wählen Sie zuerst einen Patienten, dem dieses Doukentn zugeordnet werden soll");
			return;
		}
		File file=new File(f);
		if(!file.canRead()){
			SWTHelper.showError("Kann Datei nicht lesen", "Die Datei "+f+" kann nicht gelesen werden");
			return;
		}
		FileImportDialog fid=new FileImportDialog(file.getName());
		if(fid.open()==Dialog.OK){
			try{
				BufferedInputStream bis=new BufferedInputStream(new FileInputStream(file));
				ByteArrayOutputStream baos=new ByteArrayOutputStream();
				int in;
				while((in=bis.read())!=-1){
					baos.write(in);
				}
				bis.close();
				baos.close();
				new DocHandle(baos.toByteArray(),act,fid.title,file.getName(),fid.keywords);
			}catch(Exception ex){
				ExHandler.handle(ex);
				SWTHelper.showError("Fehler beim Einlesen", "Es ist ein Fehler beim Einlesen passiert. Bitte log prüfen");
			}
		}
		
	}
	

}
