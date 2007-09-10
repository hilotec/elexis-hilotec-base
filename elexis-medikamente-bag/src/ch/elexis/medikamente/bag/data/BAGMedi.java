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
 *  $Id: BAGMedi.java 3129 2007-09-10 12:52:40Z rgw_ch $
 *******************************************************************************/
package ch.elexis.medikamente.bag.data;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.elexis.Desk;
import ch.elexis.data.Artikel;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Organisation;
import ch.elexis.data.Query;
import ch.elexis.data.Xid;
import ch.elexis.util.Log;
import ch.elexis.util.Money;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.VersionInfo;

/**
 * This Article is a medicament taken from the BAG (Swiss federal dep. of health)
 * @author Gerry
 *
 */
public class BAGMedi extends Artikel implements Comparable<BAGMedi>{
	//static final String EXTTABLE="CH_ElEXIS_MEDIKAMENTE_BAG_EXT";
	static final String JOINTTABLE="CH_ELEXIS_MEDIKAMENTE_BAG_JOINT";
	static final String VERSION="0.1.0";
	public static final String IMG_GENERIKUM="ch.elexis.medikamente.bag.generikum";
	public static final String IMG_HAS_GENERIKA="ch.elexis.medikamente.bag.has_generika";
	public static final String IMG_ORIGINAL="ch.elexis.medikamente.bag.original";
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
		+"ID				VARCHAR(25) primary key,"
		+"product			VARCHAR(25),"
		+"substance         VARCHAR(25)"
		+");"
		+"CREATE INDEX CHEMBJ1 ON "+JOINTTABLE+" (product);"
		+"CREATE INDEX CHEMBJ2 ON "+JOINTTABLE+" (substance);" 
		+"INSERT INTO "+JOINTTABLE+" (ID,substance) VALUES('VERSION','"+VERSION+"');";
	
	public static final String CODESYSTEMNAME="Medikament";
	public static final String DOMAIN_PHARMACODE="www.xid.ch/id/pk";
	
	static{
		addMapping(Artikel.TABLENAME,"Gruppe=ExtId","Generikum=Codeclass","inhalt=JOINT:substance:product:"+JOINTTABLE);
		
		Xid.localRegisterXIDDomainIfNotExists(DOMAIN_PHARMACODE	, Xid.ASSIGNEMENT_REGIONAL);
		String v=j.queryString("SELECT substance FROM "+JOINTTABLE+" WHERE ID='VERSION';");
		
		if(v==null){
			createTable("BAGMedi",jointDB);
		}else{
			VersionInfo vi=new VersionInfo(v);
			if(vi.isOlder(VERSION)){
				SWTHelper.showError("Datenbank Fehler", "Die Versin von BAG-Medi ist zu alt");
			}
		}
		// make sure, the substances table is created
		Substance.load("VERSION");
		Desk.theImageRegistry.put(IMG_GENERIKUM, BAGMediFactory.loadImageDescriptor("icons/ggruen.png"));
		Desk.theImageRegistry.put(IMG_HAS_GENERIKA, BAGMediFactory.loadImageDescriptor("icons/orot.png"));
		Desk.theImageRegistry.put(IMG_ORIGINAL, BAGMediFactory.loadImageDescriptor("icons/oblau.ico"));
	}
	
	/**
	 * Create a BAGMEdi from a line of the BAG file
	 * @param row the line
	 */
	public BAGMedi(final String name, final String pharmacode){
		super(name,CODESYSTEMNAME,pharmacode);
		set("Klasse",getClass().getName());
	}
	
	public boolean isGenericum(){
		return get("Generikum").startsWith("G");
	}
	public boolean hasGenerica(){
		return get("Generikum").startsWith("O");
	}
	public List<Substance> getSubstances(){
		List<String[]> cnt= getList("inhalt",new String[0]);
		ArrayList<Substance> ret=new ArrayList<Substance>(cnt.size());
		for(String[] s:cnt){
			ret.add(Substance.load(s[0]));
		}
		return ret;
	}
	
	public SortedSet<Interaction> getInteraktionen(){
		List<Substance> substances=getSubstances();
		SortedSet<Interaction> ret=new TreeSet<Interaction>();
		for(Substance s:substances){
			List<Interaction> interactions=s.getInteractions();
			ret.addAll(interactions);
		}
		return ret;
	}
	public Kontakt getHersteller(){
		return Kontakt.load(getExt("HerstellerID"));
	}
	
	public void update(final String[] row){
		Query<Organisation> qo=new Query<Organisation>(Organisation.class);
		String id=qo.findSingle("Name","=", row[0]);
		if(id==null){
			Organisation o=new Organisation(row[0],"Pharma");
			id=o.getId();
		}
		setExt("HerstellerID", id);
		set("Generikum",row[1]);
		setExt("Pharmacode",row[2]);
		setExt("BAG-Dossier",row[3]);
		setExt("Swissmedic-Nr.",row[4]);
		setExt("Swissmedic-Liste",row[5]);
		try{
			setEKPreis(new Money(row[8]));
			setVKPreis(new Money(row[9]));
		}catch(ParseException ex){
			log.log("Parse error preis "+row[7], Log.ERRORS);
		}
		if(row[10].equals("Y")){
			setExt("Limitatio","Y");
			setExt("LimitatioPts",row[11]);
		}else{
			setExt("Limitation", null);
		}
		if(row.length>13){
			if(!StringTool.isNothing(row[13])){
				String[] substName=row[13].split("\\|");
				LinkedList<Substance> substances=new LinkedList<Substance>();
				for(String n:substName){
					Substance s=Substance.find(n);
					if(s==null){
						s=new Substance(n,row[12]);
					}
					substances.add(s);
				}
				deleteList("inhalt");
				for(Substance s:substances){
					addToList("inhalt", s.getId(), new String[0]);
				}
				
				
			}
		}
		if(row.length>12){
			set("Gruppe",row[12]);
		}
		
	}
	
	@Override
	protected String getConstraint(){
		return "Typ='Medikament'";
	}
	
	@Override
	protected void setConstraint(){
		set("Typ","Medikament");
	}

	@Override
	public String getCodeSystemName() {
		return CODESYSTEMNAME;
	}
	
	
	
	public static BAGMedi load(final String id){
		return new BAGMedi(id);
	}
	protected BAGMedi(final String id){
		super(id);
	}
	protected BAGMedi(){
	}

	public int compareTo(final BAGMedi arg0) {
		return(getLabel().compareTo(arg0.getLabel()));
	}

	@Override
	public boolean isDragOK() {
		return true;
	}
	
}
