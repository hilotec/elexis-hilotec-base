package ch.elexis.labor.medics.labimport;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import ch.elexis.ElexisException;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;
import ch.elexis.data.Labor;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.hl7.model.EncapsulatedData;
import ch.elexis.hl7.model.StringData;
import ch.elexis.labor.medics.MedicsPreferencePage;
import ch.elexis.labor.medics.Messages;
import ch.elexis.services.GlobalServiceDescriptors;
import ch.elexis.services.IDocumentManager;
import ch.elexis.text.GenericDocument;
import ch.elexis.text.IOpaqueDocument;
import ch.elexis.util.Extensions;
import ch.rgw.io.FileTool;
import ch.rgw.tools.TimeSpan;
import ch.rgw.tools.TimeTool;

public class PatientLabor {
	private static String KUERZEL = Messages.PatientLabor_kuerzelMedics;
	private static String LABOR_NAME = Messages.PatientLabor_nameMedicsLabor;
	
	private Labor myLab = null;

	private final Patient patient;
	
	private IDocumentManager docManager;
	
	public PatientLabor(Patient patient){
		super();
		this.patient = patient;
		initLabor();
		initDocumentManager();
	}
	
	/**
	 * Initialisiert document manager (omnivore) falls vorhanden
	 */
	private void initDocumentManager(){
		Object os = Extensions.findBestService(GlobalServiceDescriptors.DOCUMENT_MANAGEMENT);
		if (os != null) {
			this.docManager = (IDocumentManager) os;
		}
	}
	
	/**
	 * Check if category exists. If not, the category is created
	 */
	private void checkCreateCategory(final String category){
		if (category != null) {
			boolean catExists = false;
			for (String cat : this.docManager.getCategories()) {
				if (category.equals(cat)) {
					catExists = true;
				}
			}
			if (!catExists) {
				this.docManager.addCategorie(category);
			}
		}
	}
	
	/**
	 * Adds a document to omnivore (if it not already exists)
	 * 
	 * @return boolean. True if added false if not
	 * @throws ElexisException
	 * @throws IOException
	 */
	private boolean addDocument(final String title, final String category, final String dateStr, final File file)
		throws IOException, ElexisException{
		checkCreateCategory(category);
		
		List<IOpaqueDocument> documentList =
			this.docManager.listDocuments(this.patient, category, title, null,
				new TimeSpan(dateStr + "-" + dateStr), null);
		
		if (documentList == null || documentList.size() == 0) {
			this.docManager.addDocument(new GenericDocument(this.patient, title, category, file,
				dateStr, null, null));
			return true;
		}
		return false;
	}
	
	private void initLabor(){
		Query<Labor> qbe = new Query<Labor>(Labor.class);
		qbe.add("Kuerzel", "LIKE", "%" + KUERZEL + "%"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		List<Labor> list = qbe.execute();
		
		if (list.size() < 1) {
			myLab = new Labor(KUERZEL, LABOR_NAME); //$NON-NLS-1$
		} else {
			myLab = list.get(0);
		}
	}
	
	/**
	 * Liest LabItem
	 * 
	 * @param kuerzel
	 * @param type
	 * @return LabItem falls exisitiert. Sonst null
	 */
	private LabItem getLabItem(String kuerzel, LabItem.typ type){
		Query<LabItem> qli = new Query<LabItem>(LabItem.class);
		qli.add(LabItem.SHORTNAME, "=", kuerzel); //$NON-NLS-1$ //$NON-NLS-2$
		qli.and();
		qli.add(LabItem.LAB_ID, "=", myLab.get("ID")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		qli.and();
		qli.add(LabItem.TYPE, "=", new Integer(type.ordinal()).toString()); //$NON-NLS-1$
		
		LabItem labItem = null;
		List<LabItem> itemList = qli.execute();
		if (itemList.size() > 0) {
			labItem = itemList.get(0);
		}
		return labItem;
	}
	
	/**
	 * Fügt Laborwert zu Patientenlabor hinzu
	 * 
	 * @param data
	 */
	public void addLaborItem(final StringData data){
		String refMann = ""; //$NON-NLS-1$
		String refFrau = ""; //$NON-NLS-1$
		if (Patient.MALE.equals(patient.getGeschlecht())) {
			refMann = data.getRange();
		} else {
			refFrau = data.getRange();
		}
		
		LabItem labItem = getLabItem(data.getName(), LabItem.typ.NUMERIC);
		if (labItem == null) {
			labItem =
				new LabItem(data.getName(), data.getName(), myLab, refMann, refFrau,
					data.getUnit(), LabItem.typ.NUMERIC, LABOR_NAME, "50"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		TimeTool dateTime = new TimeTool();
		dateTime.setTime(data.getDate());
		LabResult lr = new LabResult(patient, dateTime, labItem, data.getValue(), ""); //$NON-NLS-1$
		lr.set("Quelle", LABOR_NAME); //$NON-NLS-1$
	}
	
	/**
	 * Fügt Dokument zu Patientenlabor hinzu
	 * 
	 * @param data
	 */
	public void addDocument(EncapsulatedData data) throws IOException{
		if (this.docManager == null) {
			throw new IOException(
				MessageFormat
					.format(
						Messages.PatientLabor_errorKeineDokumentablage,
						data.getName(), this.patient.getLabel()));
		}
		
		// Kategorie überprüfen/ erstellen
		String category = MedicsPreferencePage.getDokumentKategorie();
		checkCreateCategory(category);
		
		String downloadDir = MedicsPreferencePage.getDownloadDir();
		
		// Tmp Verzeichnis überprüfen
		File tmpDir = new File(downloadDir + File.separator + "tmp"); //$NON-NLS-1$
		if (!tmpDir.exists()) {
			if (!tmpDir.mkdirs()) {
				throw new IOException(MessageFormat.format(
					Messages.PatientLabor_errorCreatingTmpDir, tmpDir.getName()));
			}
		}
		String filename = data.getName();
		File tmpPdfFile =
			new File(downloadDir + File.separator + "tmp" + File.separator + filename); //$NON-NLS-1$
		tmpPdfFile.deleteOnExit();
		FileTool.writeFile(tmpPdfFile, data.getData());
		
		TimeTool dateTime = new TimeTool();
		dateTime.setTime(data.getDate());
		String dateTimeStr = dateTime.toString(TimeTool.DATE_GER);
		
		try {
			// Zu Dokumentablage hinzufügen
			addDocument(filename, category, dateTimeStr, tmpPdfFile);
			
			// Labor Item erstellen
			String kuerzel = "doc"; //$NON-NLS-1$
			LabItem labItem = getLabItem(kuerzel, LabItem.typ.DOCUMENT);
			if (labItem == null) {
				labItem =
					new LabItem(kuerzel, Messages.PatientLabor_nameDokumentLaborParameter, myLab, "", "",  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
						FileTool.getExtension(filename), LabItem.typ.DOCUMENT, LABOR_NAME, "50"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			LabResult lr = new LabResult(patient, dateTime, labItem, filename, ""); //$NON-NLS-1$
			lr.set("Quelle", LABOR_NAME); //$NON-NLS-1$
		} catch (ElexisException e) {
			throw new IOException(MessageFormat.format(Messages.PatientLabor_errorAddingDocument,
				tmpPdfFile.getName()), e);
		}
	}
}
