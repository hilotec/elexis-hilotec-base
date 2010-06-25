package ch.elexis.artikel_at.preferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import ch.elexis.Hub;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Artikel;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Prescription;
import ch.elexis.data.Query;
import ch.elexis.data.Rezept;
import ch.elexis.util.SWTHelper;

public class Utilities {
	/**
	 * Dieses Skript reinigt die patient_artikel_joint Tabelle (in Elexis Prescription).
	 * Datensätze die weder einen existenten Patient, einen existenten
	 * Artikel noch ein existentes Rezept vorweisen werden gelöscht.
	 * 
	 * Benötigt DELETE_MEDICATION Rechte!
	 * 
	 * @author Marco Descher / Herzpraxis Dr. Thomas Wolber
	 */
	public static void cleanPrescriptionTable() {
		if(!Hub.acl.request(AccessControlDefaults.DELETE_MEDICATION)) {
			return;
		}
		Query<Prescription> qPres = new Query<Prescription>(Prescription.class);
		List<Prescription> presList = qPres.execute();
		int invalidRecipes = 0;
		for (Iterator<Prescription> iterator = presList.iterator(); iterator
				.hasNext();) {
			Prescription prescription = (Prescription) iterator.next();
			boolean validPerson = true;
			boolean validArticle = true;
			boolean validRecipe = true;

			String refPerID = prescription.get(Prescription.PATIENT_ID);
			if (refPerID == null || refPerID.equals(""))
				validPerson = false;
			String refArtID = prescription.get(Prescription.ARTICLE);
			if (refArtID == null || refArtID.equals(""))
				validArticle = false;
			String refRezID = prescription.get(Prescription.REZEPT_ID);
			if (refRezID == null || refRezID.equals(""))
				validRecipe = false;

			Kontakt refPer = Kontakt.load(refPerID);
			if (refPer.state() == Kontakt.INEXISTENT)
				validPerson = false;
			Artikel refArt = Artikel.load(refArtID);
			if (refArt.state() == Artikel.INEXISTENT)
				validArticle = false;
			Rezept refRez = Rezept.load(refRezID);
			if (refRez.state() == Rezept.INEXISTENT)
				validRecipe = false;

			if (!validPerson && !validArticle && !validRecipe) {
				invalidRecipes++;
				// Werden nicht REAL aus der DB entfernt, wuerde hier aber Sinn
				// machen!
				prescription.remove();
			}
		}
		SWTHelper.showInfo(invalidRecipes + " Rezepte gelöscht.",
				invalidRecipes + " Rezepte wurden als ungültig gelöscht.");
	}
	
	/**
	 * Dieses Skript such von jeder aktuellen Verordnung die zugehörige
	 * Pharma-Zentralnummer, anschliessend wird unter Artikel das aktuellste
	 * Medikament mit identischer Pharma-Zentralnummer ausgewählt und die
	 * Verknüpfung upgedatet.	
	 * 		 
	 * FOR ALL m: artikelid IN patient_artikel_joint {
	 * 	String PhZNr = m.artikelid->subid;
	 *  Medikament[] medis = artikel.getsubid(PhZNr);
	 *  Medikament current = medis.getNewest(); // Last updated
	 *  m.artikelid = current.id;
	 *  medis.remove(!=current && current.PhZNr == medis[i].PhZNr);
	 *  }
	 *  
	 *  Benötigt DELETE_MEDICATION Rechte!
	 *  
	 *  @author Marco Descher / Herzpraxis Dr. Thomas Wolber
	 */
	public static void updateMediReferences() {
		if(!Hub.acl.request(AccessControlDefaults.DELETE_MEDICATION)) {
			return;
		}
		//Query<Artikel> qbe = new Query<Artikel>(Artikel.class);
		//List<Artikel> artikelList = qbe.execute(); // does not return elements marked as deleted
		Query<Prescription> qPres = new Query<Prescription>(Prescription.class);
		List<Prescription> presList = qPres.execute();
		Artikel currArtikel;
		
		File outfile = new File("updateReferences.txt");
		try {
			PrintWriter pen = new PrintWriter(outfile);
			int noOfPrescriptions = 0;
			int noOfUpdates = 0;
		
		// FOR ALL prescription: patient_artikel_joint
		for (Iterator<Prescription> iterator = presList.iterator(); iterator
				.hasNext();) {
			Prescription prescription = (Prescription) iterator.next();
			noOfPrescriptions++;
			currArtikel = prescription.getArtikel();
			//
			String PhZNr = currArtikel.get(Artikel.FLD_SUB_ID);
			
			
			if(PhZNr!="") {						
				Query<Artikel> qArt = new Query<Artikel>(Artikel.class);
				qArt.clear();
				qArt.add(Artikel.FLD_SUB_ID, "=", PhZNr);
				List<Artikel> artList = qArt.execute();
				//List<Artikel> artList = qArt.executeWithDeleted();
				
				try {
				Artikel newest = artList.get(0);
				long newestint=0;
				for (Artikel artikel : artList) {
					String updateTime = artikel.get(Artikel.FLD_LASTUPDATE);
					if(updateTime.equalsIgnoreCase("")) continue;
					long time = Long.parseLong(updateTime);
					if(time > newestint) newest = artikel;
					}
				
					artList.remove((Artikel)newest);
					// Hier neuen Artikel setzen
					if(newest.equals(currArtikel)) continue; 
					prescription.set(Prescription.ARTICLE, newest.storeToString());
					noOfUpdates++;
					pen.println(prescription.getId()+"::"+PhZNr+" Update to "+newest.getName()+" from "+newest.get(Artikel.FLD_LASTUPDATE));
					
					
				} catch(IndexOutOfBoundsException e) {
					pen.println(prescription.getId()+"::"+PhZNr+" No Change - Kein passendes Medikament gefunden. artList.size():"+artList.size());
				}
			} else {
				pen.println(prescription.getId()+"::"+"No Change - Referenzierter Artikel hat keine Pharma-ZentralNr.");
			}
		}
		pen.println("Anzahl Verschreibungen: "+noOfPrescriptions);
		pen.println("Anzahl Updates:"+noOfUpdates);
		pen.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
}
