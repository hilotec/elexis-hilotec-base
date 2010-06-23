package ch.elexis.artikel_at.preferences;


import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import ch.elexis.Hub;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.artikel_at.Activator;
import ch.elexis.artikel_at.PreferenceConstants;
import ch.elexis.artikel_at.data.Medikament;
import ch.elexis.data.Artikel;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.data.Prescription;
import ch.elexis.data.Query;
import ch.elexis.data.Rezept;
import ch.elexis.util.SWTHelper;


public class Artikel_AT
	extends PreferencePage
	implements IWorkbenchPreferencePage {

	public Artikel_AT() {
		super();
		setDescription("Informationen zum Modul Medikamente (AT)");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		final Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(2, false));
		new Label(ret, SWT.NONE).setText("Datum und Uhrzeit der Ver\u00f6ffentlichung:");
		new Text(ret, SWT.READ_ONLY).setText(Hub.globalCfg.get(PreferenceConstants.ARTIKEL_AT_RPHEADER_PUBDATE, "nicht gesetzt"));
		new Label(ret, SWT.NONE).setText("Dateiname der Datenaustauschdatei:");
		new Text(ret, SWT.READ_ONLY).setText(Hub.globalCfg.get(PreferenceConstants.ARTIKEL_AT_RPHEADER_FILENAME, "nicht gesetzt"));
		new Label(ret, SWT.NONE).setText("Author der Informationen:");
		new Text(ret, SWT.READ_ONLY).setText(Hub.globalCfg.get(PreferenceConstants.ARTIKEL_AT_RPHEADER_PUBAUTHOR, "nicht gesetzt"));
		new Label(ret, SWT.NONE).setText("Copyright-Notizen:");
		new Text(ret, SWT.READ_ONLY).setText(Hub.globalCfg.get(PreferenceConstants.ARTIKEL_AT_RPHEADER_PUBCOPYRIGHT, "nicht gesetzt"));
		SWTHelper.addSeparator(ret);
		//Anzahl Substanzen
		
		//Anzahl Artikel gesamt davon Medikamente (AT)
//		Query<Medikament> qbe = new Query<Medikament>(Medikament.class);
//		List<Medikament> list = qbe.execute();
		Button cleanArtikelTable = new Button(ret, INFORMATION);
		if(!Hub.acl.request(AccessControlDefaults.DELETE_MEDICATION)) {
			cleanArtikelTable.setEnabled(false);
		}
		cleanArtikelTable.setText("Rezept Tabelle reinigen");
		cleanArtikelTable.setToolTipText("Reinigt die Rezepte Tabelle. " +
				"Datensätze die weder einen existenten Patient, einen existenten " +
				"Artikel noch ein existentes Rezept vorweisen werden gelöscht.");
		cleanArtikelTable.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
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
					if(refPerID==null || refPerID.equals("")) validPerson=false;
					String refArtID = prescription.get(Prescription.ARTICLE);
					if(refArtID==null || refArtID.equals("")) validArticle=false;
					String refRezID = prescription.get(Prescription.REZEPT_ID);
					if(refRezID==null || refRezID.equals("")) validRecipe=false;
									
					Kontakt refPer = Kontakt.load(refPerID);
					if(refPer.state()==Kontakt.INEXISTENT) validPerson=false;
					Artikel refArt = Artikel.load(refArtID);
					if(refArt.state()==Artikel.INEXISTENT) validArticle=false;
					Rezept refRez = Rezept.load(refRezID);		
					if(refRez.state()==Rezept.INEXISTENT) validRecipe=false;

					if(!validPerson && !validArticle && !validRecipe) {
						invalidRecipes++;
						// Werden nicht REAL aus der DB entfernt, wuerde hier aber Sinn machen!
						prescription.remove();
					}
				}
				SWTHelper.showInfo(invalidRecipes +" Rezepte gelöscht.", invalidRecipes+" Rezepte wurden als ungültig gelöscht.");
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});

		
		
		Button updateRefDB = new Button(ret, INFORMATION);
		updateRefDB.setText("Update References in DB");
		updateRefDB.addSelectionListener(new SelectionListener() {
			
//			 * PSEUDOCODE:
//				 * 
//				 * FOR ALL m: artikelid IN patient_artikel_joint {
//				 * 	String PhZNr = m.artikelid->subid;
//				 *  Medikament[] medis = artikel.getsubid(PhZNr);
//				 *  Medikament current = medis.getNewest(); // Last updated
//				 *  m.artikelid = current.id;
//				 *  medis.remove(!=current && current.PhZNr == medis[i].PhZNr);
//				 *  }
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				//Query<Artikel> qbe = new Query<Artikel>(Artikel.class);
				//List<Artikel> artikelList = qbe.execute(); // does not return elements marked as deleted
				Query<Prescription> qPres = new Query<Prescription>(Prescription.class);
				List<Prescription> presList = qPres.execute();
				Artikel foo;
				
				File outfile = new File("updateReferences.txt");
				try {
					PrintWriter pen = new PrintWriter(outfile);
					int noOfPrescriptions = 0;
				
				
				
				for (Iterator<Prescription> iterator = presList.iterator(); iterator
						.hasNext();) {
					Prescription prescription = (Prescription) iterator.next();
					noOfPrescriptions++;
					foo = prescription.getArtikel();
					String PhZNr = foo.get(Artikel.FLD_SUB_ID);
					
					if(PhZNr!="") {
						//System.out.print(prescription.getId()+"::"+PhZNr+": ");
						pen.print(prescription.getId()+"::"+PhZNr+": ");
						
						Query<Artikel> qArt = new Query<Artikel>(Artikel.class);
						qArt.clear();
						qArt.add(Artikel.FLD_SUB_ID, "=", PhZNr);
						List<Artikel> artList = qArt.execute();
						
						try {
						Artikel newest = artList.get(0);
						long newestint=0;
						for (Artikel artikel : artList) {
							String updateTime = artikel.get(Artikel.FLD_LASTUPDATE);
							if(updateTime.equalsIgnoreCase("")) continue;
							long time = Long.parseLong(updateTime);
							if(time > newestint) newest = artikel;
							}
							pen.println("Selected: "+newest.getName()+" from "+newest.get(Artikel.FLD_LASTUPDATE));
						} catch(IndexOutOfBoundsException e) {
							pen.println("KEIN MEDIKAMENT GEFUNDEN!! artList.size():"+artList.size());
						}
					} else {
						pen.println(prescription.getId()+"::"+"REFERENZIERTER ARTIKEL NICHT EXISTENT");
					}
				}
				pen.println("Anzahl Verschreibungen: "+noOfPrescriptions);
				pen.close();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	
		return ret;
	}
	
}