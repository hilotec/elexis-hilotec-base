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
 *  $Id: Brief.java 4782 2008-12-09 18:10:43Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import ch.elexis.text.XrefExtension;
import ch.rgw.compress.CompEx;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Ein Brief ist ein mit einem externen Programm erstelles Dokument. (Im Moment immer
 * OpenOffice.org). Die Klasse Briefe mit der Tabelle Briefe enthält dabei die Meta-Informationen,
 * während die private Klasse contents mit der Tabelle HEAP die eigentlichen Dokumente als black
 * box, nämlich im Binärformat des erstellenden Programms, enthält. Ein Brief bezieht sich immer auf
 * eine bestimmte Konsultation, zu der er erstellt wurde.
 * 
 * @author Gerry
 * 
 */
public class Brief extends PersistentObject {
	public static final String TEMPLATE = "Vorlagen";
	public static final String AUZ = "AUF-Zeugnis";
	public static final String RP = "Rezept";
	public static final String UNKNOWN = "Allg.";
	public static final String LABOR = "Labor";
	public static final String BESTELLUNG = "Bestellung";
	public static final String RECHNUNG = "Rechnung";
	public static final String SYSTEMPLATE = "Systemvorlagen";
	
	public static final String MIMETYPE_OO2 = "application/vnd.oasis.opendocument.text";
	
	@Override
	protected String getTableName(){
		return "BRIEFE";
	}
	
	static {
		addMapping("BRIEFE", "Betreff", "PatientID", "Datum=S:D:Datum", "AbsenderID", "DestID",
			"BehandlungsID", "Typ", "modifiziert=S:D:modifiziert", "geloescht", "MimeType",
			"gedruckt=S:D:gedruckt", "Path");
	}
	
	protected Brief(){/* leer */
	}
	
	protected Brief(String id){
		super(id);
	}
	
	/** Einen Brief anhand der ID aus der Datenbank laden */
	public static Brief load(String id){
		return new Brief(id);
	}
	
	/** Einen neuen Briefeintrag erstellen */
	public Brief(String Betreff, TimeTool Datum, Kontakt Absender, Kontakt dest, Konsultation bh,
		String typ){
		getConnection().setAutoCommit(false);
		try {
			super.create(null);
			if (Datum == null) {
				Datum = new TimeTool();
			}
			String pat = "", bhdl = "";
			if (bh != null) {
				bhdl = bh.getId();
				pat = bh.getFall().getPatient().getId();
			}
			String dst = "";
			if (dest != null) {
				dst = dest.getId();
			}
			String dat = Datum.toString(TimeTool.DATE_GER);
			set(new String[] {
				"Betreff", "PatientID", "Datum", "AbsenderID", "modifiziert", "DestID",
				"BehandlungsID", "Typ", "geloescht"
			}, new String[] {
				Betreff, pat, dat, Absender == null ? "" : Absender.getId(), dat, dst, bhdl, typ,
				"0"
			});
			new contents(this);
			getConnection().commit();
		} catch (Throwable ex) {
			ExHandler.handle(ex);
			getConnection().rollback();
		} finally {
			getConnection().setAutoCommit(true);
		}
	}
	
	public void setPatient(Person k){
		set("PatientID", k.getId());
	}
	
	public void setTyp(String typ){
		set("Typ", typ);
	}
	
	public String getTyp(){
		String t = get("Typ");
		if (t == null) {
			return "Brief";
		}
		return t;
	}
	
	/** Speichern als Text */
	public boolean save(String cnt){
		contents c = contents.load(getId());
		c.save(cnt);
		set("modifiziert", new TimeTool().toString(TimeTool.DATE_COMPACT));
		return true;
	}
	
	/** Speichern in Binärformat */
	public boolean save(byte[] in, String mimetype){
		if (in != null) {
			// if(mimetype.equalsIgnoreCase(MIMETYPE_OO2)){
			contents c = contents.load(getId());
			c.save(in);
			set("modifiziert", new TimeTool().toString(TimeTool.DATE_COMPACT));
			set("MimeType", mimetype);
			return true;
			// }
			// return false;
		}
		return false;
	}
	
	/** Binärformat laden */
	public byte[] loadBinary(){
		contents c = contents.load(getId());
		return c.getBinary();
	}
	
	/** Textformat laden */
	public String read(){
		contents c = contents.load(getId());
		return c.read();
	}
	
	/** Mime-Typ des Inhalts holen */
	public String getMimeType(){
		String gm = get("MimeType");
		if (StringTool.isNothing(gm)) {
			return MIMETYPE_OO2;
		}
		return gm;
	}
	
	public static boolean canHandle(String mimetype){
		/*
		 * if(mimetype.equalsIgnoreCase(MIMETYPE_OO2)){ return true; }
		 */
		return true;
	}
	
	public boolean delete(){
		getConnection().exec("UPDATE HEAP SET deleted='1' WHERE ID=" + getWrappedId());
		String konsID = get("BehandlungsID");
		if (!StringTool.isNothing(konsID) && (!konsID.equals("SYS"))) {
			Konsultation kons = Konsultation.load(konsID);
			if ((kons != null) && (kons.isEditable(false))) {
				kons.removeXRef(XrefExtension.providerID, getId());
			}
		}
		return super.delete();
	}
	
	/** Einen Brief unwiederruflich löschen */
	public boolean remove(){
		getConnection().setAutoCommit(false);
		try {
			getConnection().exec("DELETE FROM HEAP WHERE ID=" + getWrappedId());
			getConnection().exec("DELETE FROM BRIEFE WHERE ID=" + getWrappedId());
			getConnection().commit();
		} catch (Throwable ex) {
			ExHandler.handle(ex);
			getConnection().rollback();
			return false;
		} finally {
			getConnection().setAutoCommit(true);
		}
		return true;
	}
	
	public String getBetreff(){
		return checkNull(get("Betreff"));
	}
	
	public void setBetreff(String nBetreff){
		set("Betreff", nBetreff);
	}
	
	public String getDatum(){
		return get("Datum");
	}
	
	public Kontakt getAdressat(){
		String dest = get("DestID");
		return dest == null ? null : Kontakt.load(dest);
	}
	
	public Person getPatient(){
		Person pat = Person.load(get("PatientID"));
		if ((pat != null) && (pat.state() > INVALID_ID)) {
			return pat;
		}
		return null;
	}
	
	public String getLabel(){
		return checkNull(get("Datum")) + " " + checkNull(get("Betreff"));
	}
	
	private static class contents extends PersistentObject {
		static {
			addMapping("HEAP", "inhalt");
		}
		
		private contents(Brief br){
			create(br.getId());
		}
		
		private contents(String id){
			super(id);
		}
		
		byte[] getBinary(){
			return getBinary("inhalt");
		}
		
		private String read(){
			byte[] raw = getBinary();
			if (raw != null) {
				byte[] ret = CompEx.expand(raw);
				return StringTool.createString(ret);
			}
			return "";
		}
		
		private void save(String contents){
			byte[] comp = CompEx.Compress(contents, CompEx.BZIP2);
			setBinary("inhalt", comp);
		}
		
		private void save(byte[] contents){
			setBinary("inhalt", contents);
		}
		
		@Override
		public String getLabel(){
			return getId();
		}
		
		static contents load(String id){
			return new contents(id);
		}
		
		@Override
		protected String getTableName(){
			return "HEAP";
		}
		
	}
}
