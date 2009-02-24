/*******************************************************************************
 * Copyright (c) 2009, G. Weirich, medshare and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * $Id: PhysioLeistung.java 5192 2009-02-24 15:48:29Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import ch.elexis.tarmedprefs.PhysioPrefs;
import ch.rgw.tools.TimeTool;

/**
 * Implementation of the swiss physiotherapy-tariff
 * 
 * @author gerry
 * 
 */
public class PhysioLeistung extends VerrechenbarAdapter {
	public static final String VERSION = "0.0.1";
	private static final String TABLENAME = "CH_ELEXIS_ARZTTARIFE_CH_PHYSIO";
	private static final String XIDDOMAIN = "www.xid.ch/id/physiotarif";
	public static final String CODESYSTEMNAME = "Physiotherapie";
	
	private static final String createDB =
		"CREATE TABLE " + TABLENAME + " (" + "ID			VARCHAR(25) primary key," + "lastupdate BIGINT,"
			+ "deleted  CHAR(1) default '0'," + "validFrom	CHAR(8)," + "validUntil CHAR(8),"
			+ "TP CHAR(8)," + "ziffer		VARCHAR(6)," + "titel		VARCHAR(255)," + "description TEXT);"
			+ "CREATE INDEX cheacp on " + TABLENAME + " (ziffer);";
	
	static {
		addMapping(TABLENAME, "von=S:D:validFrom", "bis=S:D:validUntil", "Ziffer", "Titel",
			"text=description", "TP");
		Xid.localRegisterXIDDomainIfNotExists(XIDDOMAIN, "Physiotarif", Xid.ASSIGNMENT_LOCAL);
		PhysioLeistung pv = PhysioLeistung.load("VERSION");
		if (!pv.exists()) {
			createOrModifyTable(createDB);
			pv.create("VERSION");
			pv.set("Ziffer", VERSION);
		}
	}
	
	public PhysioLeistung(String code, String text, String tp, String validFrom, String validUntil){
		create(null);
		set(new String[] {
			"Ziffer", "Titel", "TP", "von", "bis"
		}, code, text, tp, TimeTool.BEGINNING_OF_UNIX_EPOCH, TimeTool.END_OF_UNIX_EPOCH);
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public String[] getDisplayedFields(){
		return new String[] {
			"Ziffer", "Titel"
		};
	}
	
	public double getFactor(TimeTool date, Fall fall){
		return getVKMultiplikator(date, PhysioPrefs.TP_ID);
	}
	
	public int getTP(TimeTool date, Fall fall){
		return checkZero(get("TP"));
	}
	
	public static PhysioLeistung load(String id){
		return new PhysioLeistung(id);
	}
	
	protected PhysioLeistung(String id){
		super(id);
	}
	
	protected PhysioLeistung(){}
	
	public String getXidDomain(){
		return XIDDOMAIN;
	}
	
	@Override
	public String getCodeSystemCode(){
		return "311";
	}
	
	@Override
	public String getLabel(){
		String[] res = new String[2];
		get(new String[] {
			"Ziffer", "Titel"
		}, res);
		return new StringBuilder().append(res[0]).append(" ").append(res[1]).toString();
	}
	
	@Override
	public String getText(){
		return get("Titel");
	}
	
	@Override
	public String getCode(){
		return get("Ziffer");
	}
	
	@Override
	public String getCodeSystemName(){
		return CODESYSTEMNAME;
	}
	
	@Override
	public boolean isDragOK(){
		return true;
	}
	
}
