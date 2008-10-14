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
 *  $Id: ESRRecord.java 4599 2008-10-14 10:19:17Z rgw_ch $
 *******************************************************************************/
package ch.elexis.banking;

import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.elexis.util.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Ein ESRRecord ist eine einzelne Buchung aus einem ESR-File-
 * @author Gerry
 *
 */
public class ESRRecord extends PersistentObject{
	private static final String VERSION="1";
	private static final String TABLENAME="ESRRECORDS";
	private static final int POSITION_PAT_NR=11;
	private static final int POSITION_RN_NR=20;
	
	public static enum MODE{Gutschrift_edv,Storno_edv,Korrektur_edv,
			Gutschrift_Schalter,Storno_Schalter,Korrektur_Schalter,
			Summenrecord,Unbekannt};
			
	public static enum REJECT{OK,ESRREJECT,MASSENREJECT,BETRAG,
		MANDANT,RN_NUMMER,PAT_NUMMER,DUPLIKAT,ANDERE,PAT_FALSCH};

	private static final String createDB="DROP TABLE "+TABLENAME+";" +
		"DROP INDEX ESR1;"+
		"DROP INDEX ESR2;"+
		"DROP INDEX ESR3;"+
		"CREATE TABLE "+TABLENAME+"(" +
		"ID			VARCHAR(25) PRIMARY KEY," +
		"deleted	CHAR(1) default '0',"+
		"DATUM			CHAR(8)," +
		"EINGELESEN		CHAR(8)," +
		"VERARBEITET	CHAR(8)," +
		"GUTSCHRIFT		CHAR(8)," +
		"BETRAGINRP		CHAR(8)," +
		"CODE			CHAR(3)," +
		"RECHNUNGSID VARCHAR(25)," +
		"PATIENTID	 VARCHAR(25)," +
		"MANDANTID	 VARCHAR(25)," +
		"REJECTCODE	 CHAR(3)," +
		"KOSTEN		 CHAR(4)," +
		"GEBUCHT	 CHAR(8)," +
		"FILE		 VARCHAR(80));"+
		"CREATE INDEX ESR1 ON "+TABLENAME+" (DATUM);"+
		"CREATE INDEX ESR2 ON "+TABLENAME+" (PATIENTID);"+
		"CREATE INDEX ESR3 ON "+TABLENAME+" (REJECTCODE);" +
		"INSERT INTO "+TABLENAME+" (ID,FILE) VALUES ('1','"+VERSION+"');";

		
	static{
		addMapping(TABLENAME,"ID","Datum=S:D:DATUM",
				"Eingelesen=S:D:EINGELESEN",
				"Verarbeitet=S:D:VERARBEITET",
				"Gutgeschrieben=S:D:GUTSCHRIFT",
				"BetragInRp=BETRAGINRP",
				"Code","RechnungsID","PatientID","MandantID","RejectCode","Gebucht=S:D:GEBUCHT","File"
		);
		ESRRecord init=load("1");
		if(init==null){
			createTable(TABLENAME, createDB);
		}else{
			String v=init.get("File");
			if(StringTool.isNothing(v)){ // < version 1
				getConnection().exec("ALTER TABLE "+TABLENAME+" ADD deleted CHAR(1) default '0';");
				init.set("File", VERSION);
			}
		}
	}
	@Override
	public String getLabel() {
		
		return null;
	}
	public Rechnung getRechnung() {
		String rnid=get("RechnungsID");
		return Rechnung.load(rnid);
	}
	public Money getBetrag(){
		return new Money(checkZero(get("BetragInRp")));
	}
	public MODE getTyp(){
		int m=getInt("Code");
		MODE ret=MODE.values()[m];
		return ret;
	}
	public REJECT getRejectCode() {
		int code=getInt("RejectCode");
		return REJECT.values()[code];
	}
	public void setGebucht(TimeTool date){
		if(date==null){
			date=new TimeTool();
		}
		set("Gebucht",date.toString(TimeTool.DATE_GER));
		set("RejectCode","0");
	}
	
	/**
		 * Der Konstruktor liest eine ESR-Zeile ein und konstruiert daraus den Datensatz.
		 */
	public ESRRecord(final String file,final String codeline){
		super.create(null);
		Mandant m;
		Rechnung rn=null;
		String mandantID;
		REJECT rejectCode;

		String[] vals=new String[11];
		vals[0]=new TimeTool().toString(TimeTool.DATE_COMPACT);
		vals[10]=file;
		
		rejectCode=REJECT.OK;
		
		// Code/Modus. 
		MODE mode=MODE.Unbekannt;
		String smd=codeline.substring(0,3);
		if(smd.equals("002")){
			mode=MODE.Gutschrift_edv;
		}else if(smd.equals("012")){
			mode=MODE.Gutschrift_Schalter;
		}else if(smd.equals("005")){
			mode=MODE.Storno_edv;
		}else if(smd.equals("015")){
			mode=MODE.Storno_Schalter;
		}else if(smd.equals("008")){
			mode=MODE.Korrektur_edv;
		}else if(smd.equals("018")){
			mode=MODE.Korrektur_Schalter;
		}else if(smd.equals("999")){
			mode=MODE.Summenrecord;
		}
		vals[5]=Integer.toString(mode.ordinal());	


		
		// Daten parsen. Der ESR-Record liefert 6-stellige Daten, wir wollen 8-stellige
		String prefix=vals[0].substring(0,2);
		// TODO Das funktioniert nur bis ins Jahr 2099 :-)
		TimeTool dat=new TimeTool(prefix+codeline.substring(59,65));
		vals[1]=dat.toString(TimeTool.DATE_GER);
		dat.set(prefix+codeline.substring(65,71));
		vals[2]=dat.toString(TimeTool.DATE_GER);
		dat.set(prefix+codeline.substring(71,77));
		vals[3]=(dat.toString(TimeTool.DATE_GER));
		
		if(mode.equals(MODE.Summenrecord)){
			// Betrag (führende Nullen entfernen)
			vals[4]=Integer.toString(Integer.parseInt(codeline.substring(39,51).trim())); // Totalbetrag 12-stellig
		}else{
			vals[4]=Integer.toString(Integer.parseInt(codeline.substring(39,49).trim())); // Zeilenbetrag 10-stellig
			String esrline=codeline.substring(12,39);
			
			// Von der RechnungsNummer führende Nullen wegbringen
			int rnnr=Integer.parseInt(esrline.substring(POSITION_RN_NR,26));
			Query<Rechnung> qbe_r=new Query<Rechnung>(Rechnung.class);
			String rnid=qbe_r.findSingle("RnNummer","=",Integer.toString(rnnr));
			if(rnid==null){
				rejectCode=REJECT.RN_NUMMER;
				vals[6]="";
				mandantID="";
			}else{
				vals[6]=rnid;
				rn=Rechnung.load(rnid);
				if(rn==null){
					rejectCode=REJECT.RN_NUMMER;
					vals[6]="";
					mandantID="";
				}else{
					m=rn.getMandant();
					mandantID=m.getId();
				}
				 
			}
			String PatNr=esrline.substring(POSITION_PAT_NR,POSITION_RN_NR);
			long patnr=Long.parseLong(PatNr);	// führende Nullen wegbringen
			String PatID=new Query<Patient>(Patient.class).findSingle("PatientNr","=",Long.toString(patnr));
			if(PatID==null){
				if(rejectCode==REJECT.OK){
						rejectCode=REJECT.PAT_NUMMER;
				}
				vals[7]="";
			}else if ( (rn!=null) && (!rn.getFall().getPatient().getId().equals(PatID))){
				if(rejectCode==REJECT.OK){
					rejectCode=REJECT.PAT_FALSCH;
				}
				vals[7]="";
			}else{
			
				vals[7]=PatID;
				
			}
			vals[8]=mandantID;
		}
		vals[9]=Integer.toString(rejectCode.ordinal());
		set(new String[]{"Datum","Eingelesen","Verarbeitet","Gutgeschrieben",
				"BetragInRp","Code","RechnungsID","PatientID","MandantID","RejectCode","File"},vals);
		
	}

	
	@Override
	protected String getTableName() {
		return "ESRRECORDS";
	}
	public static ESRRecord load(final String id){
		ESRRecord ret=new ESRRecord(id);
		if(ret.exists()){
			return ret;
		}
		return null;
	}
	protected ESRRecord(final String id){
		super(id);
	}
	public ESRRecord(){
	}

	public String getEinlesedatatum() {
		return get("Eingelesen");
	}
	public String getVerarbeitungsdatum(){
		return get("Verarbeitet");
	}
	public String getValuta(){
		return get("Gutgeschrieben");
	}
	public Patient getPatient(){
		String pid=get("PatientID");
		return Patient.load(pid);
	}
	public String getGebucht() {
		return get("Gebucht");
	}
	public String getFile(){
		return checkNull(get("File"));
	}

	public String getESRCode(){
		return MODE.values()[checkZero(get("Code"))].toString();
	}
}