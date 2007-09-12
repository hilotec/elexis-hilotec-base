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
 * $Id$
 *******************************************************************************/

package ch.elexis.importer.praxistar;

import java.sql.ResultSet;

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
			String name=res.getString("tx_Name");
			String id=res.getString("ID_Versicherung");
			if(Xid.findObject(GARANTID, id)!=null){
				continue;
			}
			Organisation o= new Organisation(name,"Versicherung");
			o.set(new String[]{"Strasse","Plz","Ort","Telefon1","Fax","Ansprechperson"},
					res.getString("tx_Strasse"),
					res.getString("tx_PLZ"),
					res.getString("tx_Ort"),
					res.getString("tx_Telefon"),
					res.getString("tx_Fax"),
					res.getString("tx_ZuHanden")
			);
			String ean=res.getString("tx_EANNr");
			o.setInfoElement("EAN", ean);
			o.addXid(Xid.DOMAIN_EAN, ean, false);
			moni.worked(PORTION);
		}
	}
	private void importAerzte(final IProgressMonitor moni) throws Exception{
		moni.subTask("importiere Ärzte");
		int num=stm.queryInt("SELECT COUNT(*) FROM Adressen_Ärzte");
		final int PORTION=Math.round((TOTALWORK/WORK_PORTIONS)/num);
		ResultSet res=stm.query("SELECT * FROM Adressen_Ärzte");
		while((res!=null) && res.next()){
			String name=res.getString("tx_Name");
			String vorname=res.getString("tx_Vorname");
			String geschlecht=res.getString("tx_Anrede").startsWith("Her") ? "m" : "w";
			String id=res.getString("ID_Arztadresse");
			if(Xid.findObject(ARZTID, id)!=null){
				continue;
			}
			Person p=new Person(name,vorname,"",geschlecht);
			moni.subTask(p.getLabel());
			p.set(new String[]{"Zusatz","Titel","Strasse","Plz","Ort","Telefon1","Telefon2","Natel","Fax"},
					res.getString("tx_Fachgebiet"),
					res.getString("tx_Titel"),
					res.getString("tx_Prax_Strasse"),
					res.getString("tx_Prax_PLZ"),
					StringTool.normalizeCase(res.getString("tx_Prax_Ort")),
					res.getString("tx_Prax_Telefon1"),
					res.getString("tx_Prax_Telefon2"),
					res.getString("tx_Prax_Natel"),
					res.getString("tx_Prax_Fax")
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
			String name=StringTool.normalizeCase(res.getString("tx_Name"));
			String vorname=res.getString("tx_Vorname");
			String gebdat=res.getString("tx_Geburtsdatum").split(" ")[0];
			int is=res.getInt("Geschlecht_ID");
			String id=res.getString("ID_Patient");
			if(Xid.findObject(PATID, id)!=null){
				continue; // avoid multiple imports
			}
			Patient pat=new Patient(name,vorname,new TimeTool(gebdat).toString(TimeTool.DATE_GER),
					is==1 ? "m" : "w");
			moni.subTask(pat.getLabel());
			pat.set(new String[]{"Strasse","Plz","Ort","Telefon1","Telefon2","Natel","Titel"},
							res.getString("tx_Strasse"),
							res.getString("tx_PLZ"),
							res.getString("tx_Ort"),
							res.getString("tx_TelefonP"),
							res.getString("tx_TelefonG"),
							res.getString("tx_TelefonN"),
							res.getString("tx_Titel")
					);
			StringBuilder sb=new StringBuilder();
			appendIfNotEmpty(sb, "Beruf: ", res.getString("tx_Beruf"));
			appendIfNotEmpty(sb, "Bemerkung: ", res.getString("mo_Bemerkung"));
			appendIfNotEmpty(sb, "Hausarzt: ", res.getString("tx_Hausarzt"));
			appendIfNotEmpty(sb, "Arbeitgeber", res.getString("tx_Arbeitgeber"));
			appendIfNotEmpty(sb, "Zuweisender Arzt: ", res.getString("tx_ZuwArzt"));
			if(sb.length()>0){
				pat.setBemerkung(sb.toString());
			}
			if(count++>500){
				PersistentObject.clearCache();
				System.gc();
				count=0;
			}
			pat.addXid(PATID, id, false);
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
	
}
