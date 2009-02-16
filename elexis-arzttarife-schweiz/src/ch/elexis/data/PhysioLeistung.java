/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * $Id: PhysioLeistung.java 5138 2009-02-16 18:27:19Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import ch.rgw.tools.TimeTool;

/**
 * Implementatuion of the swiss physiotherapy-tariff
 * 
 * @author gerry
 * 
 */
public class PhysioLeistung extends VerrechenbarAdapter {
	public static final String VERSION = "0.0.1";
	private static final String TABLENAME = "CH_ELEXIS_ARZTTARIFE_CH_PHYSIO";
	private static final String XIDDOMAIN = "www.xid.ch/id/physiotarif";
	private static final String createDB =
		"CREATE TABLE " + TABLENAME + " (" + "ID			VARCHAR(25) primary key," + "lastupdate BIGINT,"
			+ "deleted  CHAR(1)," + "validFrom	CHAR(8)," + "validUntil CHAR(8)," + "TP CHAR(8),"
			+ "ziffer		VARCHAR(6)," + "titel		VARCHAR(255)," + "description TEXT);"
			+ "CREATE INDEX cheacp on " + TABLENAME + " (ziffer);";
	
	static {
		addMapping(TABLENAME, "von=S:D:validFrom", "bis=S:D:validUntil", "Ziffer", "Titel",
			"text=description", "TP");
		Xid.localRegisterXIDDomainIfNotExists(XIDDOMAIN, "Physiotarif", Xid.ASSIGNMENT_LOCAL);
		PhysioLeistung pv = PhysioLeistung.load("VERSION");
		if (!pv.exists()) {
			createTable(TABLENAME, createDB);
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
		return getVKMultiplikator(date, fall);
	}
	
	public int getTP(TimeTool date, Fall fall){
		return 0;
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
	
}
