/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: DocHandle.java 5970 2010-01-27 16:43:04Z rgw_ch $
 *******************************************************************************/

package ch.elexis.omnivore.data;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.program.Program;

import ch.elexis.actions.ElexisEventDispatcher;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.omnivore.views.FileImportDialog;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionInfo;

public class DocHandle extends PersistentObject {
	public static final String TABLENAME = "CH_ELEXIS_OMNIVORE_DATA";
	public static final String DBVERSION = "1.2.1";
	public static final String createDB=
		"CREATE TABLE "+TABLENAME+" ("+
		"ID				VARCHAR(25) primary key,"+
		"lastupdate     BIGINT,"+
		"deleted        CHAR(1) default '0',"+
		"PatID			VARCHAR(25),"+
		"Datum			CHAR(8),"+
		"Title 			VARCHAR(80),"+	
		"Mimetype		VARCHAR(255),"+
		"Keywords		VARCHAR(255),"+
		"Path			VARCHAR(255),"+
		"Doc			BLOB);"+
		"CREATE INDEX OMN1 ON "+TABLENAME+" (PatID);"+
		"CREATE INDEX OMN2 ON "+TABLENAME+" (Keywords);" +
		"INSERT INTO "+TABLENAME+" (ID, TITLE) VALUES ('1','"+DBVERSION+"');";
		
	public static final String upd120=
		"ALTER TABLE "+TABLENAME+" MODIFY Mimetype VARCHAR(255);"+
		"ALTER TABLE "+TABLENAME+" MODIFY Keywords VARCHAR(255);"+
		"ALTER TABLE "+TABLENAME+" Modify Path VARCHAR(255);";
	
	private static final String upd121=
		"ALTER TABLE "+TABLENAME+" ADD lastupdate BIGINT;";
	
	static {
		addMapping(TABLENAME, "PatID", "Datum=S:D:Datum", "Titel=Title", "Keywords", "Path", "Doc",
			"Mimetype");
		DocHandle start = load("1");
		if (start == null) {
			init();
		} else {
			VersionInfo vi = new VersionInfo(start.get("Titel"));
			if (vi.isOlder(DBVERSION)) {
				if (vi.isOlder("1.1.0")) {
					getConnection().exec(
						"ALTER TABLE " + TABLENAME + " ADD deleted CHAR(1) default '0';");
					start.set("Titel", DBVERSION);
				} 
				if (vi.isOlder("1.2.0")) {
					createOrModifyTable(upd120);
					start.set("Titel", DBVERSION);
				} 
				if(vi.isOlder("1.2.1")){
					createOrModifyTable(upd121);
					start.set("Titel", DBVERSION);
				}
				
			}
		}
	}
	
	public DocHandle(byte[] doc, Patient pat, String title, String mime, String keyw){
		if ((doc == null) || (doc.length == 0)) {
			SWTHelper.showError("Fehler mit Dokument",
				"Das Dokument konnte nicht korrekt gelesen werden");
			return;
		}
		create(null);
		if (setBinary("Doc", doc) == 1) {
			set(new String[] {
				"PatID", "Datum", "Titel", "Keywords", "Mimetype"
			}, pat.getId(), new TimeTool().toString(TimeTool.DATE_GER), title, keyw, mime);
		} else {
			log.log("Der Datensatz wurde nicht geschrieben", Log.ERRORS);
		}
	}
	
	/**
	 * Tabelle neu erstellen
	 */
	public static void init(){
		createOrModifyTable(createDB);
	}
	
	public static DocHandle load(String id){
		DocHandle ret = new DocHandle(id);
		if (ret.exists()) {
			return ret;
		}
		return null;
	}
	
	@Override
	public String getLabel(){
		StringBuilder sb = new StringBuilder();
		sb.append(get("Datum")).append(" ").append(get("Titel"));
		return sb.toString();
	}
	
	@Override
	public boolean delete(){
		return super.delete();
	}
	
	public void execute(){
		try {
			String ext = "";
			String typname = get("Mimetype");
			int r = typname.lastIndexOf('.');
			if (r == -1) {
				typname = get("Titel");
				r = typname.lastIndexOf('.');
			}
			
			if (r != -1) {
				ext = typname.substring(r + 1);
			}
			File temp = File.createTempFile("omni_", "_vore." + ext);
			byte[] b = getBinary("Doc");
			if (b == null) {
				SWTHelper.showError("Fehler beim lesen",
					"Konnte das Dokument nicht aus der Datenbank laden");
				return;
			}
			FileOutputStream fos = new FileOutputStream(temp);
			fos.write(b);
			fos.close();
			Program proggie = Program.findProgram(ext);
			if (proggie != null) {
				proggie.execute(temp.getAbsolutePath());
			} else {
				if (Program.launch(temp.getAbsolutePath()) == false) {
					Runtime.getRuntime().exec(temp.getAbsolutePath());
				}
				
			}
			
		} catch (Exception ex) {
			ExHandler.handle(ex);
			SWTHelper.showError("Konnte Datei nicht starten", ex.getMessage());
		}
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	protected DocHandle(String id){
		super(id);
	}
	
	protected DocHandle(){}
	
	public static void assimilate(String f){
		Patient act = ElexisEventDispatcher.getSelectedPatient();
		if (act == null) {
			SWTHelper
				.showError("Kein Patient ausgewählt",
					"Bitte wählen Sie zuerst einen Patienten, dem dieses Doukentn zugeordnet werden soll");
			return;
		}
		File file = new File(f);
		if (!file.canRead()) {
			SWTHelper.showError("Kann Datei nicht lesen", "Die Datei " + f
				+ " kann nicht gelesen werden");
			return;
		}
		FileImportDialog fid = new FileImportDialog(file.getName());
		if (fid.open() == Dialog.OK) {
			try {
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int in;
				while ((in = bis.read()) != -1) {
					baos.write(in);
				}
				bis.close();
				baos.close();
				String nam = file.getName();
				if (nam.length() > 255) {
					SWTHelper.showError("Fehler beim Einlesen",
						"Der Dateiname ist zu lang (max. 255 Zeichen");
					return;
				}
				new DocHandle(baos.toByteArray(), act, fid.title, file.getName(), fid.keywords);
			} catch (Exception ex) {
				ExHandler.handle(ex);
				SWTHelper.showError("Fehler beim Einlesen",
					"Es ist ein Fehler beim Einlesen passiert. Bitte log prüfen");
			}
		}
		
	}
	
}
