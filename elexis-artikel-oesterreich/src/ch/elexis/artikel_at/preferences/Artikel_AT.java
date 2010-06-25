package ch.elexis.artikel_at.preferences;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.artikel_at.PreferenceConstants;
import ch.elexis.data.Artikel;
import ch.elexis.data.Prescription;
import ch.elexis.data.Query;
import ch.elexis.util.SWTHelper;


public class Artikel_AT
	extends PreferencePage
	implements IWorkbenchPreferencePage {

	Text benutzerkennung;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		Hub.globalCfg.set(PreferenceConstants.ARTIKEL_AT_VIDAL_BENUTZERKENNUNG, benutzerkennung.getText().trim());
		return super.performOk();
	}

	public Artikel_AT() {
		super();
		setDescription("Informationen und Konfiguration zum Modul Medikamente (AT)");
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
		new Label(ret, SWT.NONE).setText("Benutzerkennung vidal.at:");
		benutzerkennung = new Text(ret, SWT.NONE);
		benutzerkennung.setEditable(true);
		benutzerkennung.setText(Hub.globalCfg.get(PreferenceConstants.ARTIKEL_AT_VIDAL_BENUTZERKENNUNG, "nicht gesetzt"));
		SWTHelper.addSeparator(ret);
		new Label(ret, SWT.NONE).setText("Datum und Uhrzeit der Ver\u00f6ffentlichung:");
		new Label(ret, SWT.READ_ONLY).setText(Hub.globalCfg.get(PreferenceConstants.ARTIKEL_AT_RPHEADER_PUBDATE, "nicht gesetzt"));
		new Label(ret, SWT.NONE).setText("Dateiname der Datenaustauschdatei:");
		new Label(ret, SWT.READ_ONLY).setText(Hub.globalCfg.get(PreferenceConstants.ARTIKEL_AT_RPHEADER_FILENAME, "nicht gesetzt"));
		new Label(ret, SWT.NONE).setText("Author der Informationen:");
		new Label(ret, SWT.READ_ONLY).setText(Hub.globalCfg.get(PreferenceConstants.ARTIKEL_AT_RPHEADER_PUBAUTHOR, "nicht gesetzt"));
		new Label(ret, SWT.NONE).setText("Copyright-Notizen:");
		new Label(ret, SWT.READ_ONLY).setText(Hub.globalCfg.get(PreferenceConstants.ARTIKEL_AT_RPHEADER_PUBCOPYRIGHT, "nicht gesetzt"));
		SWTHelper.addSeparator(ret);
		
		//---
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
				Utilities.cleanPrescriptionTable();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});

		//---
		Button updateRefDB = new Button(ret, INFORMATION);
		updateRefDB.setText("PhZNr Referenzen updaten");
		updateRefDB.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Utilities.updateMediReferences();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	
		return ret;
	}
	
}