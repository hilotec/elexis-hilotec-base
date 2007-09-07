/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: BAGMedi.java 3106 2007-09-07 05:14:37Z rgw_ch $
 *******************************************************************************/
package ch.elexis.medikamente.bag.data;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.jface.dialogs.MessageDialog;

import ch.elexis.data.Artikel;
import ch.elexis.data.Organisation;
import ch.elexis.data.Query;
import ch.elexis.data.Xid;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.VersionInfo;

/**
 * This Article is a medicament taken from the BAG (Swiss federal dep. of health)
 * @author Gerry
 *
 */
public class BAGMedi extends Artikel {
	//static final String EXTTABLE="CH_ElEXIS_MEDIKAMENTE_BAG_EXT";
	static final String JOINTTABLE="CH_ELEXIS_MEDIKAMENTE_BAG_JOINT";
	static final String VERSION="0.1.0";
	/*
	static final String extDB="CREATE TABLE "+EXTTABLE+" ("
		+"ID				VARCHAR(25) primary key,"
		+"deleted			CHAR(1) default '0',"
		+"generika			CHAR(1),"
		+"swissmedicliste	CHAR(1),"
		+"limitation		CHAR(1),"
		+"hersteller		VARCHAR(25),"
		+"bagdossier		VARCHAR(10),"
		+"swissmedicnr		VARCHAR(10),"
		+"limitationpts		VARCHAR(10)"
		+");";
	*/
	static final String jointDB="CREATE TABLE "+JOINTTABLE+"("
		+"product			VARCHAR(25),"
		+"substance         VARCHAR(25)"
		+");"
		+"INSERT INTO "+JOINTTABLE+" (product,substance) VALUES('VESRION','"+VERSION+"');";
	
	public static final String CODESYSTEMNAME="Medikamente";
	public static final String DOMAIN_PHARMACODE="www.xid.ch/id/pk";
	
	static{
		addMapping(Artikel.TABLENAME,"");
		Xid.localRegisterXIDDomainIfNotExists(DOMAIN_PHARMACODE	, Xid.ASSIGNEMENT_REGIONAL);
		String v=j.queryString("SELECT substance FROM "+JOINTTABLE+" WHERE product='VERSION';");
		if(v==null){
			createTable("BAGMedi",jointDB);
		}else{
			VersionInfo vi=new VersionInfo(v);
			if(vi.isOlder(VERSION)){
				SWTHelper.showError("Datenbank Fehler", "Die Versin von BAG-Medi ist zu alt");
			}
		}
			
	}
	
	/**
	 * Create a BAGMEdi from a line of the BAG file
	 * @param row the line
	 */
	public BAGMedi(String name, String pharmacode){
		super(name,CODESYSTEMNAME,pharmacode);
	}
	
	public void update(String[] row){
		Query<Organisation> qo=new Query<Organisation>(Organisation.class);
		String id=qo.findSingle("Name","=", row[0]);
		if(id==null){
			Organisation o=new Organisation(row[0],"Pharma");
			id=o.getId();
		}
		setExt("HerstellerID", id);
		setExt("Generika",row[1]);
		setExt("Pharmacode",row[2]);
		
	}
	@Override
	protected String getConstraint(){
		return "Typ=Medikament";
	}
	
	@Override
	protected void setConstraint(){
		set("Typ","Medikament");
	}

	@Override
	public String getCodeSystemName() {
		return CODESYSTEMNAME;
	}
	
	
	
	public static BAGMedi load(String id){
		return new BAGMedi(id);
	}
	protected BAGMedi(String id){
		super(id);
	}
	protected BAGMedi(){
	}
}
