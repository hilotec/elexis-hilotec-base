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
import java.util.List;

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
	
	int assumeGender;
	
	boolean bType;
	
	static{
		Xid.localRegisterXIDDomainIfNotExists(PATID, Xid.ASSIGNMENT_LOCAL);
		Xid.localRegisterXIDDomainIfNotExists(GARANTID, Xid.ASSIGNMENT_LOCAL);
	}
	public AeskulapImporter() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * We accept to possible sources for data: a ';' delimited file (*.csv) containing only basic
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
			monitor.subTask("Importiere Patienten");
			hofs=checkImport(dir+File.separator+"patienten.xls");
			if(hofs!=null){
				importPatienten(hofs,monitor);
			}
			monitor.subTask("Importiere Fälle");
			hofs=checkImport(dir+File.separator+"pat._garanten.xls");
			if(hofs!=null){
				importPatGaranten(hofs,monitor);
			}
			monitor.worked(1);
		}
		monitor.done();
		return Status.OK_STATUS;
	}

	private boolean importPatienten(final ExcelWrapper hofs, final IProgressMonitor moni){
		float last=hofs.getLastRow();
		float first=hofs.getFirstRow();
		int perLine=Math.round(10000f/(last-first));
		for(int line=Math.round(first+1);line<=last;line++){
			List<String> fields=hofs.getRow(line);
			if(Xid.findXID(PATID, fields.get(0))!=null){		// avoid duplicate import
				continue;
			}

			TimeTool tt=new TimeTool(fields.get(12));
			String s=fields.get(13).equals("1") ? "m" : "w";
			Patient p=new Patient(StringTool.normalizeCase(fields.get(2)),fields.get(3),tt.toString(TimeTool.DATE_GER),s);
			Anschrift an=p.getAnschrift();
			an.setStrasse(fields.get(5));
			an.setPlz(fields.get(6));
			an.setOrt(fields.get(7));
			p.setAnschrift(an);
			p.set("Telefon1", fields.get(18));
			p.set("Telefon2", fields.get(17));
			p.set("NatelNr", fields.get(19));
			p.set("E-Mail", fields.get(20));
			StringBuilder sb=new StringBuilder();
			String gestorben=fields.get(21);
			if(!StringTool.isNothing(gestorben)){
				sb.append("Verstorben: ").append(gestorben).append("\n");
				
			}
			// In elexis, we have a multi purpose comment field "Bemerkung".
			// We'll collect several fields there
			String comment=fields.get(14);
			if(!StringTool.isNothing(comment)){
				sb.append("Kommentar: ").append(comment).append("\n");
			}
			String warning=fields.get(15);
			if(!StringTool.isNothing(warning)){
				sb.append("Warnung: ").append(warning).append("\n");
			}
			String beruf=fields.get(11);
			if(!StringTool.isNothing(beruf)){
				sb.append("Beruf: ").append(beruf).append("\n");
			}
			p.setBemerkung(sb.toString());

			// If the patient has the ahv field set, this is a good opportunity to
			// create a standard XID of national validity
			String ahv=fields.get(22);
			if(!StringTool.isNothing(ahv)){
				p.addXid(Xid.DOMAIN_AHV, ahv, true);
			}
			// We use also the original oatient number as a XID to solce later 
			// references to this patient.
			p.addXid(PATID, fields.get(0), true);
			moni.worked(perLine);
		}
		return true;
	}

	private boolean importPatGaranten(final ExcelWrapper hofs, final IProgressMonitor moni){
		float last=hofs.getLastRow();
		float first=hofs.getFirstRow();
		int perLine=Math.round(10000f/(last-first));
		for(int line=Math.round(first+1);line<=last;line++){
			List<String> fields=hofs.getRow(line);
			String patno=fields.get(0);
			String garantBez=fields.get(1);
			String kknr=fields.get(2);
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
			List<String> fields=hofs.getRow(line);
			Organisation o=new Organisation(fields.get(1),fields.get(2));
			Anschrift an=o.getAnschrift();
			an.setStrasse(fields.get(3));
			an.setPlz(fields.get(4));
			an.setOrt(fields.get(5));
			an.setLand(fields.get(9));
			o.setAnschrift(an);
			o.set("E-Mail", fields.get(10));
			o.set("Telefon1", fields.get(7));
			o.set("Fax", fields.get(8));
			o.addXid(Xid.DOMAIN_EAN, fields.get(12), false);
			o.addXid(GARANTID, fields.get(0),  true);
			moni.worked(perLine);
		}
		return true;
	}
	private boolean importFirmen(final ExcelWrapper hofs, final IProgressMonitor moni){
		float last=hofs.getLastRow();
		float first=hofs.getFirstRow();
		int perLine=Math.round(10000f/(last-first));
		for(int line=Math.round(first+1);line<=last;line++){
			List<String> fields=hofs.getRow(line);
			Organisation o=new Organisation(fields.get(1),fields.get(2));
			Anschrift an=o.getAnschrift();
			an.setLand(fields.get(6));
			an.setOrt(fields.get(5));
			an.setPlz(fields.get(4));
			an.setStrasse(fields.get(3));
			o.setAnschrift(an);
			o.set("Telefon1", fields.get(7));
			o.set("Fax", fields.get(8));
			o.set("E-Mail", fields.get(9));
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
			List<String> fields=hofs.getRow(line);
			String vorname=fields.get(1);
			String name=fields.get(2);
			String abteilung=fields.get(3);
			String strasse1=fields.get(5);
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
			an.setStrasse(fields.get(4));
			an.setPlz(fields.get(6));
			an.setOrt(fields.get(7));
			an.setLand(fields.get(8));
			k.setAnschrift(an);
			k.set("Kuerzel", fields.get(9));
			k.set("Telefon1", fields.get(16));
			k.set("Telefon2", fields.get(15));
			k.set("NatelNr", fields.get(14));
			k.set("fax", fields.get(17));
			k.set("E_Mail", fields.get(18));
			String ean=fields.get(19);
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
