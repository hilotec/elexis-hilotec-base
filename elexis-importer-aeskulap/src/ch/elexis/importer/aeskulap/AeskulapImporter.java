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
 * $Id: AeskulapImporter.java 231 2007-08-23 19:12:43Z Gerry $
 *******************************************************************************/

package ch.elexis.importer.aeskulap;

import java.io.File;
import java.io.FileReader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import au.com.bytecode.opencsv.CSVReader;
import ch.elexis.data.Anschrift;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Organisation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.data.Xid;
import ch.elexis.importers.ExcelWrapper;
import ch.elexis.tarmedprefs.TarmedRequirements;
import ch.elexis.util.ImporterPage;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Importer for Data from the practice program "Aeskulap" by Kern AG
 * @author Gerry
 *
 */
public class AeskulapImporter extends ImporterPage {
	// we'll use these local XID's to reference the external data
	private final static String IMPORT_XID="elexis.ch/aeskulap_import";
	private final static String PATID=IMPORT_XID+"/PatID";
	private final static String GARANTID=IMPORT_XID+"/garantID";
	
	Button bFile, bDir, bOnlyF, bOnlyM, bGuess;
	FileBasedImporter fbi;
	DirectoryBasedImporter dbi;
	String fname;
	String[] actLine;
	
	int assumeGender;
	
	boolean bType;
	
	static{
		Xid.localRegisterXIDDomainIfNotExists(PATID, "Alte KG-ID", Xid.ASSIGNMENT_LOCAL);
		Xid.localRegisterXIDDomainIfNotExists(GARANTID, "Alte Garant-ID", Xid.ASSIGNMENT_LOCAL);
	}
	public AeskulapImporter() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * We accept two possible sources for data: a ';' delimited file (*.csv) containing only basic
	 * personal patient data, or a more elaborate source consisting of five microsoft(tm) excel(tm)
	 * files containing personal data and insurance data.
	 * 
	 *  This method creates the contents of one tab in the import dialog.
	 */
	@Override
	public Composite createPage(final Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout());
		bFile=new Button(ret,SWT.RADIO);
		bFile.setText("Import aus einer CSV-Datei");
		fbi=new FileBasedImporter(ret,this);
		fbi.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Group gSex=new Group(ret,SWT.BORDER);
		gSex.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		gSex.setLayout(new FillLayout());
		gSex.setText("Auswahl der Patientendaten in dieser Datei");
		bOnlyF=new Button(gSex,SWT.RADIO);
		bOnlyF.setText("Alles Frauen");
		bOnlyM=new Button(gSex,SWT.RADIO);
		bOnlyM.setText("Alles Männer");
		bGuess=new Button(gSex,SWT.RADIO);
		bGuess.setText("Gemischt");
		new Label(ret,SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bDir=new Button(ret,SWT.RADIO);
		bDir.setText("Import aus 5 Excel-Dateien in einem Verzeichnis");
		dbi=new DirectoryBasedImporter(ret,this);
		dbi.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		return ret;
	}
	
	

	/**
	 * Because the doImport method is called after the dialog containing this importer has been
	 * closed, we cannot read the input fields directrly. The collect method is called after the
	 * user pressed ok but before the dialog is closed. So this is the right place to collect
	 * data the user entered.
	 */
	@Override
	public void collect() {
		bType=bFile.getSelection();
		if(bType){
			fname=fbi.tFname.getText();
		}else{
			fname=dbi.tFname.getText();
		}
		if(bOnlyF.getSelection()){
			assumeGender=0;
		}else if(bOnlyM.getSelection()){
			assumeGender=1;
		}else{
			assumeGender=2;
		}
		super.collect();
	}

	/**
	 * This method is called after the user has pressed ok and the dialog has been closed.
	 * 
	 */
	@Override
	public IStatus doImport(final IProgressMonitor monitor) throws Exception {
		File dir=new File(fname);
		if(!dir.exists()){
			SWTHelper.alert("Import Fehler", "Datei oder Verzeichnis nicht gefunden");
			return Status.CANCEL_STATUS;
		}
		
		monitor.beginTask("Importiere Aeskulap Stammdaten", 50000);
		if(bType){
			monitor.subTask("Importiere Patienten");
			CSVReader reader=new CSVReader(new FileReader(dir),';');
			String[] line=reader.readNext();	// skip first line
			while((line=reader.readNext())!=null){
				if(Xid.findXID(PATID, line[0])!=null){		// avoid duplicate import
					continue;
				}
				if(line.length<6){
					continue;
				}
				String s;
				if(assumeGender==0){
					s="w";
				}else if(assumeGender==1){
					s="m";
				}else{
					// the user didn't help us, so we'll have to guess the patient's gender.
					s=StringTool.isFemale(line[2]) ? "w" : "m";
				}
				
				Patient pat=new Patient(line[1],line[2],line[6],s);
				monitor.subTask(line[1]);
				Anschrift an=pat.getAnschrift();
				an.setStrasse(line[3]);
				an.setPlz(line[4]);
				an.setOrt(line[5]);
				pat.setAnschrift(an);
				pat.addXid(PATID, line[0], true);
				monitor.worked(10);
			}
		}else{
			monitor.subTask("Importiere Adressen");
			ExcelWrapper hofs=checkImport(dir+File.separator+"adressen.xls");
			if(hofs!=null){
				importAdressen(hofs,monitor);
			}
			monitor.subTask("Importiere Firmen");
			hofs=checkImport(dir+File.separator+"firma.xls");
			if(hofs!=null){
				importFirmen(hofs,monitor);
			}
			monitor.subTask("Importiere Garanten");
			hofs=checkImport(dir+File.separator+"garant.xls");
			if(hofs!=null){
				importGaranten(hofs,monitor);
			}
			PersistentObject.clearCache();
			System.gc();
			monitor.subTask("Importiere Patienten");
			hofs=checkImport(dir+File.separator+"patienten.xls");
			if(hofs!=null){
				importPatienten(hofs,monitor);
			}
			monitor.subTask("Importiere Fälle");
			hofs=checkImport(dir+File.separator+"pat_garanten.xls");
			if(hofs!=null){
				importPatGaranten(hofs,monitor);
			}
			monitor.worked(1);
		}
		monitor.done();
		return Status.OK_STATUS;
	}

	String getField(final int i){
		if(actLine.length>i){
			return actLine[i];
		}
		return "";
	}
	private boolean importPatienten(final ExcelWrapper hofs, final IProgressMonitor moni){
		float last=hofs.getLastRow();
		float first=hofs.getFirstRow();
		int perLine=Math.round(10000f/(last-first));
		int counter=0;
		for(int line=Math.round(first+1);line<=last;line++){
			actLine=hofs.getRow(line).toArray(new String[0]);
			if(Xid.findXID(PATID, getField(0))!=null){		// avoid duplicate import
				continue;
			}

			TimeTool tt=new TimeTool(getField(12));
			String s=getField(13).equals("1") ? "m" : "w";
			Patient p=new Patient(StringTool.normalizeCase(getField(2)),getField(3),tt.toString(TimeTool.DATE_GER),s);
			p.set("PatientNr", getField(0));
			moni.subTask(new StringBuilder().append(p.getLabel()).append(" ").append(p.getPatCode()).toString());
			Anschrift an=p.getAnschrift();
			an.setStrasse(getField(5));
			an.setPlz(getField(6));
			an.setOrt(getField(7));
			p.setAnschrift(an);
			p.set("Telefon1", getField(18));
			p.set("Telefon2", getField(17));
			p.set("NatelNr", getField(19));
			p.set("E-Mail", getField(20));
			StringBuilder sb=new StringBuilder();
			String gestorben=getField(21);
			if(!StringTool.isNothing(gestorben)){
				sb.append("Verstorben: ").append(gestorben).append("\n");
				
			}
			// In elexis, we have a multi purpose comment field "Bemerkung".
			// We'll collect several fields there
			String comment=getField(14);
			if(!StringTool.isNothing(comment)){
				sb.append("Kommentar: ").append(comment).append("\n");
			}
			String warning=getField(15);
			if(!StringTool.isNothing(warning)){
				sb.append("Warnung: ").append(warning).append("\n");
			}
			String beruf=getField(11);
			if(!StringTool.isNothing(beruf)){
				sb.append("Beruf: ").append(beruf).append("\n");
			}
			p.setBemerkung(sb.toString());

			// If the patient has the ahv field set, this is a good opportunity to
			// create a standard XID of national validity
			String ahv=getField(22);
			if(!StringTool.isNothing(ahv)){
				p.addXid(Xid.DOMAIN_AHV, ahv, true);
			}
			// We use also the original patient number as a XID to solce later 
			// references to this patient.
			p.addXid(PATID, getField(0), true);
			moni.worked(perLine);
			if(counter++>1000){	// some doctors do have a lot of patients but not enough memory...
				counter=0;
				PersistentObject.clearCache();
				System.gc();
			}
		}
		return true;
	}

	private boolean importPatGaranten(final ExcelWrapper hofs, final IProgressMonitor moni){
		float last=hofs.getLastRow();
		float first=hofs.getFirstRow();
		int perLine=Math.round(10000f/(last-first));
		for(int line=Math.round(first+1);line<=last;line++){
			actLine=hofs.getRow(line).toArray(new String[0]);
			String patno=getField(0);
			String garantBez=getField(1);
			String kknr=getField(2);
			// luckily, we created a XID for every patient and every garant imported earlier
			Patient pat=(Patient)Xid.findObject(PATID, patno);
			if(pat!=null){
				Kontakt garant=(Kontakt)Xid.findObject(GARANTID, garantBez);
				if(garant!=null){
					Fall fall=pat.neuerFall(Fall.getDefaultCaseLabel(), Fall.getDefaultCaseReason(), "KVG");
					fall.setGarant(pat);
					fall.setRequiredContact(TarmedRequirements.INSURANCE, garant);
					fall.setRequiredString(TarmedRequirements.INSURANCE_NUMBER, kknr);
				}
			}
			moni.worked(perLine);
		}
		return true;
	}
	private boolean importGaranten(final ExcelWrapper hofs, final IProgressMonitor moni){
		float last=hofs.getLastRow();
		float first=hofs.getFirstRow();
		int perLine=Math.round(10000f/(last-first));
		for(int line=Math.round(first+1);line<=last;line++){
			actLine=hofs.getRow(line).toArray(new String[0]);
			Organisation o=new Organisation(getField(1),getField(2));
			Anschrift an=o.getAnschrift();
			an.setStrasse(getField(3));
			an.setPlz(getField(4));
			an.setOrt(getField(5));
			an.setLand(getField(9));
			o.setAnschrift(an);
			o.set("E-Mail", getField(10));
			o.set("Telefon1", getField(7));
			o.set("Fax", getField(8));
			o.addXid(Xid.DOMAIN_EAN, getField(12), false);
			o.addXid(GARANTID, getField(0),  true);
			moni.worked(perLine);
		}
		return true;
	}
	private boolean importFirmen(final ExcelWrapper hofs, final IProgressMonitor moni){
		float last=hofs.getLastRow();
		float first=hofs.getFirstRow();
		int perLine=Math.round(10000f/(last-first));
		for(int line=Math.round(first+1);line<=last;line++){
			actLine=hofs.getRow(line).toArray(new String[0]);
			Organisation o=new Organisation(getField(1),getField(2));
			Anschrift an=o.getAnschrift();
			an.setLand(getField(6));
			an.setOrt(getField(5));
			an.setPlz(getField(4));
			an.setStrasse(getField(3));
			o.setAnschrift(an);
			o.set("Telefon1", getField(7));
			o.set("Fax", getField(8));
			o.set("E-Mail", getField(9));
			moni.worked(perLine);
		}
		return true;
	}
	private boolean importAdressen(final ExcelWrapper hofs, final IProgressMonitor moni){
		float last=hofs.getLastRow();
		float first=hofs.getFirstRow();
		int perLine=Math.round(10000f/(last-first));
		
		for(int line=Math.round(first+1);line<=last;line++){
			Kontakt k;
			actLine=hofs.getRow(line).toArray(new String[0]);
			String vorname=getField(1);
			String name=getField(2);
			String abteilung=getField(3);
			String strasse1=getField(5);
			// Wir wissen nicht, wie der Aeskulap-Anwender die Felder name/vorname/abteilung belegt hat, und was Organisationen
			// und was personen sind. Wir gehen pragmatisch so vor: Alles was vorname und name hat ist eine Person, alles
			// andere ist eine Organisation.
			if(StringTool.isNothing(vorname) || StringTool.isNothing(name)){
				String bez=vorname==null ? "" : vorname;
				if(bez.length()>0){
					bez+=" ";
				}
				bez+=name;
				k=new Organisation(bez,abteilung);
			}else{
				k=new Person(name,vorname,"",StringTool.isFemale(vorname) ? "w" : "m");
			}
			Anschrift an=k.getAnschrift();
			an.setStrasse(getField(4));
			an.setPlz(getField(6));
			an.setOrt(getField(7));
			an.setLand(getField(8));
			k.setAnschrift(an);
			k.set("Kuerzel", getField(9));
			k.set("Telefon1", getField(16));
			k.set("Telefon2", getField(15));
			k.set("NatelNr", getField(14));
			k.set("Fax", getField(17));
			k.set("E-Mail", getField(18));
			String ean=getField(19);
			if(!StringTool.isNothing(ean)){
				k.addXid(Xid.DOMAIN_EAN, ean, false);
			}
			moni.worked(perLine);
		}
		return true;
	}
	
	
	ExcelWrapper checkImport(final String file){
		ExcelWrapper hofs=new ExcelWrapper();
		if(hofs.load(file, 0)){
			return hofs;
		}else{
			SWTHelper.showError("Fehler beim Import","Konnte "+file+" nicht lesen");
		}
		return null;
	}
	@Override
	public String getDescription() {
		return "Stammdatenimport Aeskulap";
	}

	@Override
	public String getTitle() {
		return "Aeskulap";
	}

}
