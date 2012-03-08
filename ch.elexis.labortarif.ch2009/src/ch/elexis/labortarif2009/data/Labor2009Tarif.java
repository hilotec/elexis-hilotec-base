/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and medelexis AG
 * All rights reserved.
 * $Id: Labor2009Tarif.java 175 2009-07-22 11:20:45Z  $
 *******************************************************************************/

package ch.elexis.labortarif2009.data;

import ch.elexis.data.Fall;
import ch.elexis.data.VerrechenbarAdapter;
import ch.elexis.data.Xid;
import ch.elexis.util.IOptifier;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class Labor2009Tarif extends VerrechenbarAdapter {
	public static final String FLD_GUELTIG_BIS = "GueltigBis";
	public static final String FLD_GUELTIG_VON = "GueltigVon";
	public static final String CODESYSTEM_CODE_LAB2009 = "317"; //$NON-NLS-1$
	public static final String CODESYSTEM_CODE_TARMED = "300"; //$NON-NLS-1$
	public static final String MULTIPLICATOR_NAME = "EAL2009"; //$NON-NLS-1$
	public static final String CODESYSTEM_NAME = "EAL 2009"; //$NON-NLS-1$
	public static final String FLD_FACHBEREICH = "fachbereich"; //$NON-NLS-1$
	public static final String FLD_LIMITATIO = "limitatio"; //$NON-NLS-1$
	public static final String FLD_NAME = "name"; //$NON-NLS-1$
	public static final String FLD_TP = "tp"; //$NON-NLS-1$
	public static final String FLD_CODE = "code"; //$NON-NLS-1$
	public static final String FLD_CHAPTER = "chapter"; //$NON-NLS-1$
	public static final String FLD_FACHSPEC = "praxistyp"; //$NON-NLS-1$
	public static final String XIDDOMAIN = "www.xid.ch/id/analysenliste_ch2009/"; //$NON-NLS-1$
	private final static String TABLENAME = "CH_MEDELEXIS_LABORTARIF2009"; //$NON-NLS-1$
	public static final String VERSION010 = "0.1.0"; //$NON-NLS-1$
	public static final String VERSION = "0.1.1"; //$NON-NLS-1$

	// @formatter:off
	private static final String createTable = "create table " + TABLENAME + "(" //$NON-NLS-1$ //$NON-NLS-2$
		+ "ID		VARCHAR(25) primary key," //$NON-NLS-1$
		+ "lastupdate BIGINT," //$NON-NLS-1$
		+ "deleted	 CHAR(1) default '0'," //$NON-NLS-1$
		+ "chapter   VARCHAR(10)," //$NON-NLS-1$
		+ "code		 VARCHAR(12)," //$NON-NLS-1$
		+ "tp		 VARCHAR(10)," //$NON-NLS-1$
		+ "name		 VARCHAR(255)," //$NON-NLS-1$
		+ "limitatio TEXT," //$NON-NLS-1$
		+ "fachbereich VARCHAR(10)," //$NON-NLS-1$
		+ "GueltigVon CHAR(8)," //$NON-NLS-1$
		+ "GueltigBis CHAR(8)," //$NON-NLS-1$
		+ "praxistyp VARCHAR(2));" //$NON-NLS-1$
		+ "INSERT INTO " + TABLENAME + "(ID,code) VALUES (1,'" + VERSION + "');"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// @formatter:on
	
	private static final IOptifier l09optifier = new Optifier();
	
	static {
		createTable();
	}
	
	static void createTable(){
		addMapping(TABLENAME, FLD_CHAPTER, FLD_CODE, FLD_TP, FLD_NAME, FLD_LIMITATIO,
			FLD_FACHBEREICH, FLD_FACHSPEC, FLD_GUELTIG_BIS, FLD_GUELTIG_VON);
		Labor2009Tarif version = load("1"); //$NON-NLS-1$
		if (!version.exists()) {
			createOrModifyTable(createTable);
		} else if (version.get(FLD_CODE).equals(VERSION010)) {
			createOrModifyTable("ALTER TABLE " + TABLENAME
				+ " ADD GueltigVon CHAR(8); ALTER TABLE " + TABLENAME + " ADD GueltigBis CHAR(8);");
			version.set(FLD_CODE, VERSION);
		}
		Xid.localRegisterXIDDomainIfNotExists(XIDDOMAIN,
			"Analysenliste 2009", Xid.ASSIGNMENT_REGIONAL); //$NON-NLS-1$
	}
	
	/** Only needed by the importer */
	Labor2009Tarif(String chapter, String code, String tp, String name, String lim, String fach,
		int fachspec){
		create(null);
		set(new String[] {
			FLD_CHAPTER, FLD_CODE, FLD_TP, FLD_NAME, FLD_LIMITATIO, FLD_FACHBEREICH, FLD_FACHSPEC
		}, chapter, code, tp, name, lim, fach, Integer.toString(fachspec));
	}
	
	@Override
	public String getLabel(){
		String code = getCode();
		if (!StringTool.isNothing(code)) {
			return new StringBuilder(code).append(" ").append(getText()) //$NON-NLS-1$
				.append(" (").append(get(FLD_FACHBEREICH)).append(")") //$NON-NLS-1$ //$NON-NLS-2$
				.toString();
		} else {
			return "?"; //$NON-NLS-1$
		}
	}
	
	@Override
	public String getCode(){
		return get(FLD_CODE);
	}
	
	@Override
	public String getText(){
		return StringTool.getFirstLine(get(FLD_NAME), 80);
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public static Labor2009Tarif load(final String id){
		return new Labor2009Tarif(id);
	}
	
	protected Labor2009Tarif(final String id){
		super(id);
	}
	
	public Labor2009Tarif(){}
	
	public String getXidDomain(){
		return XIDDOMAIN;
	}
	
	public double getFactor(TimeTool date, Fall fall){
		double ret = getVKMultiplikator(date, MULTIPLICATOR_NAME);
		return ret;
	}
	
	public int getTP(TimeTool date, Fall fall){
		double tp = checkZeroDouble(get(FLD_TP));
		return (int) Math.round(tp * 100.0);
	}
	
	public boolean isValidOn(TimeTool date){
		String validFromString = get(FLD_GUELTIG_VON);
		String validToString = get(FLD_GUELTIG_BIS);
		if(validFromString != null && validFromString.trim().length() > 0) {
			TimeTool validFrom = new TimeTool(validFromString);
			if (validFrom.after(date))
				return false;
		}
		if (validToString != null && validToString.trim().length() > 0) {
			TimeTool validTo = new TimeTool(validToString);
			if (validTo.before(date) || validTo.equals(date))
				return false;
		}
		return true;
	}

	@Override
	public boolean isDragOK(){
		return true;
	}
	
	@Override
	public String getCodeSystemName(){
		return CODESYSTEM_NAME;
	}
	
	public String getCodeSystemCode(){
		return CODESYSTEM_CODE_LAB2009;
	}
	
	@Override
	public IOptifier getOptifier(){
		return l09optifier;
	}
	
}