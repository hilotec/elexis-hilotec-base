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
 *  $Id: DocHandle.java 1586 2007-01-09 06:01:16Z rgw_ch $
 *******************************************************************************/

package ch.elexis.omnivore.data;

import java.io.*;

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
	public static final String DBVERSION="1.0.0";
	public static final String createDB=
		"CREATE TABLE "+TABLENAME+" ("+
		"ID				VARCHAR(25) primary key,"+
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
			VersionInfo vi=new VersionInfo(DBVERSION);
			if(vi.isNewer(start.get("Titel"))){
				MessageDialog.openError(Desk.theDisplay.getActiveShell(), "Versionskonsflikt", 
						"Die Datentabelle für Dokumentspeicherung (Omnivore) hat eine zu alte Versionsnummer. Dies kann zu Fehlern führen");
			}
		}
	}
	
	public DocHandle(byte[] doc, Patient pat, String title, String mime, String keyw){
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
	
	public String execute(){
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
			return "";
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
				FileInputStream fis=new FileInputStream(file);
				ByteArrayOutputStream baos=new ByteArrayOutputStream();
				int in;
				while((in=fis.read())!=-1){
					baos.write(in);
				}
				new DocHandle(baos.toByteArray(),act,fid.title,file.getName(),fid.keywords);
			}catch(Exception ex){
				ExHandler.handle(ex);
			}
		}
		
	}
	

}
