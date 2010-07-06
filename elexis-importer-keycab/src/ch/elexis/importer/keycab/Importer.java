/*******************************************************************************
 * Copyright (c) 2010, Niklaus Giger niklaus.giger@member.fsf.org
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    N. Giger - used praxistar to borrow some ideas for Keycab import
 *    
 * $Id: Importer.java 3500 2008-01-05 16:20:58Z rgw_ch $
 *******************************************************************************/

package ch.elexis.importer.keycab;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;

import com.healthmarketscience.jackcess.*;

import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.data.Xid;
import ch.elexis.exchange.KontaktMatcher;
import ch.elexis.tarmedprefs.TarmedRequirements;
import ch.elexis.util.ImporterPage;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import com.healthmarketscience.jackcess.Database;

import java.util.*;

public class Importer extends ImporterPage {
	private static final float TOTALWORK = 100000;
	private static final float WORK_PORTIONS = 5;
	
	public static final String PLUGINID = "ch.elexis.importer.Keycab";
	
	// we'll use these local XID's to reference the external data
	private final static String IMPORT_XID = "elexis.ch/Keycab_import";
	private final static String PATID = IMPORT_XID + "/PatID";
	private final static String GARANTID = IMPORT_XID + "/garantID";
	private final static String ARZTID = IMPORT_XID + "/arztID";
	private final static String USERID = IMPORT_XID + "/userID";
		
	static {
		Fall.getAbrechnungsSysteme(); // make sure billing systems are
										// initialized
		Xid.localRegisterXIDDomainIfNotExists(PATID, "Alte Patientennummer", Xid.ASSIGNMENT_LOCAL);
		Xid.localRegisterXIDDomainIfNotExists(GARANTID, "Alte Garant-ID", Xid.ASSIGNMENT_LOCAL);
		Xid.localRegisterXIDDomainIfNotExists(ARZTID, "Alte Arzt-ID", Xid.ASSIGNMENT_LOCAL);
		Xid.localRegisterXIDDomainIfNotExists(USERID, "Alte Anwender-ID", Xid.ASSIGNMENT_LOCAL);
		Xid.localRegisterXIDDomainIfNotExists(TarmedRequirements.DOMAIN_KSK, "KSK",
			Xid.ASSIGNMENT_REGIONAL);
		Xid.localRegisterXIDDomainIfNotExists(TarmedRequirements.DOMAIN_NIF, "NIF",
			Xid.ASSIGNMENT_REGIONAL);
	}
	
	public Importer(){
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Composite createPage(final Composite parent){
		Composite ret = new ImporterPage.FileBasedImporter(parent, this);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		return ret;
	}
	
	private void showInfo(String filename, String tablename){
		System.out.println("keycab: showInfo for " + filename);
		try {
			Database db = Database.open(new File(filename));
			Table t = db.getTable(tablename);
			if (t != null) {
				
				System.out.println("table " + tablename + " has " + t.getRowCount() + " rows");
				System.out.println(t.display(5));
				// System.out.println(t.getColumns().toString());
			} else {
				System.out.println(tablename + " not found");
			}
			db.close();
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return;
		}
	}
	
	private String getCol(Map<String, Object> map, String colname){
		Object o = map.get(colname);
		if (o == null)
			return "";
		else
			return o.toString();
	}
	
	private boolean importDoctors(IProgressMonitor monitor, String filename, String tableName){
// AdresMed_ID AdresMed_Titre AdresMed_Nom AdresMed_Prenom AdresMed_Specialisation AdresMed_Adresse
// AdresMed_Case_Postale AdresMed_Localite_ID AdresMed_Tel AdresMed_Fax AdresMed_Email
// AdresMed_Natel AdresMed_TelConfid AdresMed_Comment AdresMed_Code_EAN AdresMed_Nr_Concordat
// AdresMed_Nr_NIF
// 1 null Schorer S Médecine générale FMH null null 444 0244260302 0217284049 null null null null
// null null 0
		
		monitor.subTask("importiere Ärzte");
		int counter = 0;
		try {
			System.out.println("importDoctors " + filename);
			Database db = Database.open(new File(filename));
			Table t = db.getTable("ADRES_MED_T009");
			if (t != null) {
				int num = t.getRowCount();
				final int PORTION = Math.round((TOTALWORK / WORK_PORTIONS) / num);
				System.out.println("importing " + num + " rows");
				System.out.println(t.display(5));
				Iterator<Map<String, Object>> it = t.iterator();
				while (it.hasNext()) {
					Map<String, Object> row = it.next();
					counter++;
					System.out.println("importDoctors " + counter + " " + row);
					String ID = "ARZT_" + getCol(row, ("AdresMed_ID"));
					String titel = getCol(row, ("AdresMed_Titre"));
					String vorname = getCol(row, ("AdresMed_Prenom"));
					String name = getCol(row, ("AdresMed_Nom"));
					String bez2 = getCol(row, ("AdresMed_Specialisation"));
					String EAN = getCol(row, ("AdresMed_Code_EAN"));
					String strasse = getCol(row, ("AdresMed_Adresse"));
					String plz = getCol(row, ("AdresMed_Localite_ID"));
					// TODO: Lookup Ort zu PLZ in table T_LOCALITE_T023
					String ort = "Ort zu PLZ";
					String email = getCol(row, ("AdresMed_Email"));
					String tel1 = getCol(row, ("AdresMed_Tel"));
					String tel2 = getCol(row, ("AdresMed_TelConfid"));
					String natel = getCol(row, ("AdresMed_Natel"));
					String zusatz = getCol(row, ("AdresMed_Case_Postale"));
					Kontakt k = null;
					System.out.println("Import Kontakt? " + name + " " + vorname);
					
					k =
						KontaktMatcher.findOrganisation(name, vorname, strasse, plz, ort,
							KontaktMatcher.CreateMode.CREATE);
					if (k == null) {
						continue;
					}
					k.set("Titel", titel); //$NON-NLS-1$
					k.set("Zusatz", bez2); //$NON-NLS-1$
					k.set(
						new String[] {
							"E-Mail", "Telefon1", "Telefon2", "Natel", "Strasse", "Plz", "Ort", "Anschrift"}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
						email, tel1, tel2, natel, strasse, plz, ort, zusatz);
					if (EAN.matches("[0-9]{13,13}")) { //$NON-NLS-1$
						k.addXid(Xid.DOMAIN_EAN, EAN, true);
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						return false;
					}
					if (counter++ > 200) {
						PersistentObject.clearCache();
						System.gc();
						try {
							Thread.sleep(100);
						} catch (Exception ex) {
							// no worries
						}
						counter = 0;
					}
					
					if (Xid.findObject(ARZTID, ID) != null) {
						continue;
					}
					// TODO: Muss eine Person erfasst werden?
					// TODO: Welches ist das Geschlecht dieser Person?
					Person p = new Person(name, vorname, "", "");
					System.out.println("Imported " + ID + " person " + p.toString());
					monitor.worked(PORTION);
					if (counter > 5)
						return true;
				}
			} else {
				System.out.println("ADRES_MED_T009 nicht gefunden");
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return false;
		}
		return false; //$NON-NLS-1$		
	}
	
	@Override
	public IStatus doImport(final IProgressMonitor monitor) throws Exception{
		String dateiName = results[0];
		String[] tableNames  = {
			"_____Prestation",
			"T_DIAGNOSTIC_T051",
			"T_DIAGNOSTIC_T054",
			"T_LOCALITE_T023",
			"T_MEDICAMENT_T027",
			"T_NATIONALITE_T046",
			"T_POSOLOGIE_T070",
			"T_PROFESSION_T026",
			"T_SPECIALISATION_T033",
			"T_TITRE_T024",
			"AGENDA_CALENDRIER_T050",
			"AGENDA_T048",
			"ASSURANCE_T015",
			"BANKING_Payement_SAVE",
			"BANKING_TMP_SAVE",
			"CERTIFICAT_T018",
			"DOSSIER_MED_01_T022",
			"FACTURE_DATE_EDITION_FACTURE_RAPPEL_T047",
			"FACTURE_T003",
			"FACTURE_T073_Histo_Acquittements_Partiels",
			"FICHIER_EXT_T014",
			"HISTO_AGENDA_T048",
			"HSCI_EAN_Assurances",
			"ORDONNANCE_Fiche_T040",
			"ORDONNANCE_Journal_T019",
			"PATIENT_ASSURANCE_T016",
			"PATIENT_COMMENTAIRE_ALARME_T074",
			"PATIENT_T001",
			"PRESTATIONS_T005",
			"RH_VACANCES_CALENDRIER_T035",
			"TRAITEMENT_MEDICAMENTAUX",
			};

		for (String tName : tableNames) { showInfo(dateiName, tName); }

		monitor.beginTask("Importiere keycab", Math.round(TOTALWORK));
		importDoctors(monitor, dateiName, "ADRES_MED_T009");
		return Status.OK_STATUS;
	}
	
	@Override
	public String getDescription(){
		return "Import Keycab Stammdaten";
	}
	
	@Override
	public String getTitle(){
		return "Keycab";
	}	
}
