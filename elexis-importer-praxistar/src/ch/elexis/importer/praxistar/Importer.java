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
 * $Id: Importer.java 3147 2007-09-12 21:13:56Z rgw_ch $
 *******************************************************************************/

package ch.elexis.importer.praxistar;

import java.sql.ResultSet;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.data.Organisation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.data.Xid;
import ch.elexis.util.ImporterPage;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.JdbcLink.Stm;

public class Importer extends ImporterPage {
	private static final float TOTALWORK=100000;
	private static final float WORK_PORTIONS=3;
	
	public static final String PLUGINID="ch.elexis.importer.praxistar";
	
	// we'll use these local XID's to reference the external data
	private final static String IMPORT_XID="elexis.ch/praxistar_import";
	private final static String PATID=IMPORT_XID+"/PatID";
	private final static String GARANTID=IMPORT_XID+"/garantID";
	private final static String ARZTID=IMPORT_XID+"/arztID";
	
	private JdbcLink j;
	private Stm stm;
	
	static{
		Xid.localRegisterXIDDomainIfNotExists(PATID, Xid.ASSIGNMENT_LOCAL);
		Xid.localRegisterXIDDomainIfNotExists(GARANTID, Xid.ASSIGNMENT_LOCAL);
		Xid.localRegisterXIDDomainIfNotExists(ARZTID, Xid.ASSIGNMENT_LOCAL);
	}
	public Importer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Composite createPage(final Composite parent) {
		DBBasedImporter dbi=new DBBasedImporter(parent,this);
		dbi.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		return dbi;
	}

	@Override
	public IStatus doImport(final IProgressMonitor monitor) throws Exception {
		if(!connect()){
			return new Status(Log.ERRORS,PLUGINID,"Verbindung nicht möglich");
		}
		monitor.beginTask("Importiere PraxiStar", Math.round(TOTALWORK));
		stm=j.getStatement();
		try{
			importAerzte(monitor);
			importGaranten(monitor);
			importPatienten(monitor);
		}catch(Exception ex){
			return new Status(Log.ERRORS,PLUGINID,ex.getMessage());
		}finally{
			j.releaseStatement(stm);
		}
		return Status.OK_STATUS;
	}

	@Override
	public String getDescription() {
		return "Import PraxiStar Stammdaten";
	}

	@Override
	public String getTitle() {
		return "PraxiStar";
	}
	
	private void importGaranten(final IProgressMonitor moni) throws Exception{
		moni.subTask("importiere Garanten");
		int num=stm.queryInt("SELECT COUNT(*) FROM Adressen_Versicherungen");
		final int PORTION=Math.round((TOTALWORK/WORK_PORTIONS)/num);
		ResultSet res=stm.query("SELECT * FROM Adressen_Versicherungen");
		while((res!=null) && res.next()){
			String id=res.getString("ID_Versicherung");
			String name=res.getString("tx_Name");
			if(Xid.findObject(GARANTID, id)!=null){
				continue;
			}
			Organisation o= new Organisation(name,"Versicherung");
			o.set(new String[]{"Strasse","Plz","Ort","Telefon1","Fax"},
					StringTool.unNull(res.getString("tx_Strasse")),
					StringTool.unNull(res.getString("tx_PLZ")),
					StringTool.unNull(res.getString("tx_Ort")),
					StringTool.unNull(res.getString("tx_Telefon")),
					StringTool.unNull(res.getString("tx_Fax"))
			);
			moni.subTask(name);
			o.addXid(GARANTID, id, false);
			String ean=res.getString("tx_EANNr");
			if(!StringTool.isNothing(ean)){
				o.setInfoElement("EAN", ean);
				o.addXid(Xid.DOMAIN_EAN, ean, false);
			}
			o.set("Ansprechperson", StringTool.unNull(res.getString("tx_ZuHanden")));
			moni.worked(PORTION);
		}
	}
	private void importAerzte(final IProgressMonitor moni) throws Exception{
		
		moni.subTask("importiere Ärzte");
		int num=stm.queryInt("SELECT COUNT(*) FROM Adressen_Ärzte");
		final int PORTION=Math.round((TOTALWORK/WORK_PORTIONS)/num);
		ResultSet res=stm.query("SELECT * FROM Adressen_Ärzte");
		while((res!=null) && res.next()){
			// fetch all columns in given order to avoid funny error messages from 
			// odbc driver
			String[] row=new String[36];
			for(int i=0;i<36;i++){
				row[i]=StringTool.unNull(res.getString(i+1));
			}
			String anrede=row[3];
			String name=row[4];
			String vorname=row[5];
			
			String geschlecht=StringTool.isFemale(vorname) ? "w" : "m";
			if(!StringTool.isNothing(anrede)){
				geschlecht=anrede.startsWith("Her") ? "m" : "w";	
			}
			
			String id=row[0];
			if(Xid.findObject(ARZTID, id)!=null){
				continue;
			}
			Person p=new Person(name,vorname,"",geschlecht);
			moni.subTask(p.getLabel());
			p.set(new String[]{"Zusatz","Titel","Strasse","Plz","Ort","Telefon1","Telefon2","Natel","Fax"},
					row[7],row[6],row[9],row[10],
					StringTool.normalizeCase(row[11]),
					row[12],row[13],row[15],row[16]
					);
			p.set("Anschrift", createAnschrift(p));
			p.addXid(ARZTID, id, false);
			moni.worked(PORTION);
		}
	}
	
	private String createAnschrift(final Person p){
		StringBuilder sb=new StringBuilder();
		String salutation;
		if(p.getGeschlecht().equals("m")){
			salutation = "Herr";
		}else{
			salutation = "Frau";
		}
		sb.append(salutation);
		sb.append("\n");

		String titel=p.get("Titel");
		if(!StringTool.isNothing(titel)){
			sb.append(titel).append(" ");
		}
		sb.append(p.getVorname()).append(" ")
			.append(p.getName()).append("\n")
			.append(p.get("Zusatz")).append("\n");
		sb.append(p.getAnschrift().getEtikette(false,true));
		return sb.toString();

	}
	private void importPatienten(final IProgressMonitor moni) throws Exception{
		moni.subTask("Importiere Patienten");
		int num=stm.queryInt("SELECT COUNT(*) FROM Patienten_Personalien");
		final int PORTION=Math.round((TOTALWORK/WORK_PORTIONS)/num);
		ResultSet res=stm.query("SELECT * FROM Patienten_Personalien");
		int count=0;
		while((res!=null) && res.next()){
			HashMap<String,String> row=fetchRow(res, new String[]{"ID_Patient","tx_Name","tx_Vorname","tx_Geburtsdatum",
					"tx_Anrede","tx_Strasse","tx_PLZ","tx_Ort","tx_TelefonP","tx_TelefonN","Geschlecht_ID",
					"Zivilstand_ID","tx_Titel","tx_Arbeitgeber","tx_Beruf","tx_TelefonG","tx_ZuwArzt",
					"tx_Hausarzt","mo_Bemerkung", "KK_Garant_ID","tx_KK_MitgliedNr","UVG_Garant_ID","tx_UVG_MitgliedNr",
					"tx_AHV_Nr","tx_fakt_Anrede","tx_fakt_Name","tx_fakt_Vorname","tx_fakt_Strasse",
					"tx_fakt_PLZ","tx_fakt_Ort"});

			String name=StringTool.normalizeCase(row.get("tx_Name"));
			String vorname=row.get("tx_Vorname");
			String gebdat=row.get("tx_Geburtsdatum").split(" ")[0];
			
			

			if(Xid.findObject(PATID, row.get("ID_Patient"))!=null){
				continue; // avoid multiple imports
			}
			Patient pat=new Patient(name,vorname,new TimeTool(gebdat).toString(TimeTool.DATE_GER),
					row.get("Geschlecht_ID").equals("1") ? "m" : "w");
			moni.subTask(pat.getLabel());
			pat.set(new String[]{"Strasse","Plz","Ort","Telefon1","Telefon2","Natel","Titel"},
							row.get("tx_Strasse"),
							row.get("tx_PLZ"),
							row.get("tx_Ort"),
							row.get("tx_TelefonP"),
							row.get("tx_TelefonG"),
							row.get("tx_TelefonN"),
							row.get("tx_Titel")
					);
			StringBuilder sb=new StringBuilder();
			appendIfNotEmpty(sb, "Beruf: ", row.get("tx_Beruf"));
			appendIfNotEmpty(sb, "Bemerkung: ", row.get("mo_Bemerkung"));
			appendIfNotEmpty(sb, "Hausarzt: ", row.get("tx_Hausarzt"));
			appendIfNotEmpty(sb, "Arbeitgeber", row.get("tx_Arbeitgeber"));
			appendIfNotEmpty(sb, "Zuweisender Arzt: ", row.get("tx_ZuwArzt"));
			if(sb.length()>0){
				pat.setBemerkung(sb.toString());
			}
			if(count++>500){
				PersistentObject.clearCache();
				System.gc();
				count=0;
			}
			pat.addXid(PATID, row.get("ID_Patient"), false);
			moni.worked(PORTION);
		}
		
	}
	
	private void appendIfNotEmpty(final StringBuilder sb, final String title, final String value){
		if(!StringTool.isNothing(value)){
			sb.append(title).append(value).append("\n");
		}
	}
	
	public boolean connect(){
        String type = results[0];
        if (type != null) {
            String server = results[1];
            String db = results[2];
            String user = results[3];
            String password = results[4];
            
            if (type.equals("MySQL")) { //$NON-NLS-1$
                j = JdbcLink.createMySqlLink(server, db);
                return j.connect(user, password);
            } else if (type.equals("PostgreSQL")) { //$NON-NLS-1$
                j = JdbcLink.createPostgreSQLLink(server, db);
                return j.connect(user, password);
            } else if (type.equals("ODBC")) { //$NON-NLS-1$
                j = JdbcLink.createODBCLink(db);
                return j.connect(user, password);
            }
        }
        
        return false;
	}
	
	/**
	 * The ODBC driver sometimes fires funny exceptions if columns are not fetched in
	 * the native order. We circumvent this by converting the row into a hashmap.
	 * @param res A ResultSet pointing to the interesting row
	 * @param columns the names of the columns
	 * @return a hashmap of ol columne values with the column name as key
	 */
	public static HashMap<String, String> fetchRow(ResultSet res, String[] columns) throws Exception{
		HashMap<String,String> ret=new HashMap<String, String>(); 
		for(String col:columns){
			ret.put(col, StringTool.unNull(res.getString(col)));
		}
		return ret;
	}
}
