/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and medelexis AG
 * All rights reserved.
 * $Id: Labor2009Tarif.java 175 2009-07-22 11:20:45Z  $
 *******************************************************************************/

package ch.elexis.labortarif2009.data;

import ch.elexis.data.Fall;
import ch.elexis.data.VerrechenbarAdapter;
import ch.elexis.util.IOptifier;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class Labor2009Tarif extends VerrechenbarAdapter {
	public static final String CODESYSTEM_CODE_LAB2009 = "317";
	public static final String CODESYSTEM_CODE_TARMED = "300";
	public static final String MULTIPLICATOR_NAME = "EAL2009";
	public static final String CODESYSTEM_NAME = "EAL 2009";
	public static final String FLD_FACHBEREICH = "fachbereich";
	public static final String FLD_LIMITATIO = "limitatio";
	public static final String FLD_NAME = "name";
	public static final String FLD_TP = "tp";
	public static final String FLD_CODE = "code";
	public static final String FLD_CHAPTER = "chapter";
	public static final String FLD_FACHSPEC = "praxistyp";
	public static final String XIDDOMAIN = "www.xid.ch/id/analysenliste_ch2009/";
	private final static String TABLENAME = "CH_MEDELEXIS_LABORTARIF2009";
	public static final String VERSION = "0.1.0";
	private static final String createTable = "create table " + TABLENAME + "("
			+ "ID		VARCHAR(25) primary key," + "lastupdate BIGINT,"
			+ "deleted	 CHAR(1) default '0'," + "chapter   VARCHAR(10),"
			+ "code		 VARCHAR(12)," + "tp		 VARCHAR(10),"
			+ "name		 VARCHAR(255)," + "limitatio TEXT,"
			+ "fachbereich VARCHAR(10)," + "praxistyp VARCHAR(2)" + ");"
			+ "INSERT INTO "+TABLENAME+"(ID,code) VALUES (1,'"+VERSION+"');";

	private static final IOptifier l09optifier = new Optifier();

	static {
		addMapping(TABLENAME, FLD_CHAPTER, FLD_CODE, FLD_TP, FLD_NAME,
				FLD_LIMITATIO, FLD_FACHBEREICH, FLD_FACHSPEC);
		Labor2009Tarif version = load("1");
		if (!version.exists()) {
			createOrModifyTable(createTable);
		}

	}

	/** Only needed by the importer */
	Labor2009Tarif(String chapter, String code, String tp, String name,
			String lim, String fach, int fachspec) {
		create(null);
		set(new String[] { FLD_CHAPTER, FLD_CODE, FLD_TP, FLD_NAME,
				FLD_LIMITATIO, FLD_FACHBEREICH, FLD_FACHSPEC }, chapter, code,
				tp, name, lim, fach, Integer.toString(fachspec));
	}

	@Override
	public String getLabel() {
		String code=getCode();
		if(!StringTool.isNothing(code)){
		return new StringBuilder(code).append(" ").append(getText())
				.append(" (").append(get(FLD_FACHBEREICH)).append(")")
				.toString();
		}else{
			return "?";
		}
	}

	@Override
	public String getCode() {
		return get(FLD_CODE);
	}

	@Override
	public String getText() {
		return StringTool.getFirstLine(get(FLD_NAME),80);
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}

	public static Labor2009Tarif load(final String id) {
		return new Labor2009Tarif(id);
	}

	protected Labor2009Tarif(final String id) {
		super(id);
	}

	public Labor2009Tarif() {
	}

	public String getXidDomain() {
		return XIDDOMAIN;
	}

	public double getFactor(TimeTool date, Fall fall) {
		double ret = getVKMultiplikator(date, MULTIPLICATOR_NAME);
		return ret;
	}

	public int getTP(TimeTool date, Fall fall) {
		double tp = checkZeroDouble(get(FLD_TP));
		return (int) Math.round(tp * 100.0);
	}

	@Override
	public boolean isDragOK() {
		return true;
	}

	@Override
	public String getCodeSystemName() {
		return CODESYSTEM_NAME;
	}

	public String getCodeSystemCode() {
		return CODESYSTEM_CODE_LAB2009;
	}

	@Override
	public IOptifier getOptifier() {
		return l09optifier;
	}

}