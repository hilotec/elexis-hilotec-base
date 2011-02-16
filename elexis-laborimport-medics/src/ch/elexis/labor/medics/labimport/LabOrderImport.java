package ch.elexis.labor.medics.labimport;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.hl7.HL7ParserV23;
import ch.elexis.hl7.model.EncapsulatedData;
import ch.elexis.hl7.model.IValueType;
import ch.elexis.hl7.model.ObservationMessage;
import ch.elexis.hl7.model.StringData;
import ch.elexis.labor.medics.MedicsPreferencePage;
import ch.elexis.labor.medics.Messages;
import ch.elexis.labor.medics.data.KontaktOrderManagement;
import ch.elexis.laborimport.medics.util.MedicsLogger;
import ch.elexis.util.ImporterPage;
import ch.elexis.util.SWTHelper;
import ch.rgw.io.FileTool;

public class LabOrderImport extends ImporterPage {
	protected final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); //$NON-NLS-1$
	
	@Override
	public IStatus doImport(IProgressMonitor monitor) throws Exception{
		MedicsLogger.getLogger().println(
			MessageFormat.format("{0}: Medics Laborimport gestartet", df.format(new Date()))); //$NON-NLS-1$
		MedicsLogger.getLogger().println(
			"=============================================================="); //$NON-NLS-1$
		
		int errorCount = 0;
		
		File downloadDir = new File(MedicsPreferencePage.getDownloadDir());
		MedicsLogger.getLogger().println(
			MessageFormat.format("HL7 Dateien in Verzeichnis {0} lesen..", downloadDir)); //$NON-NLS-1$
		if (downloadDir.isDirectory()) {
			File[] hl7Files = downloadDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name){
					return name.toLowerCase().endsWith(".hl7"); //$NON-NLS-1$
				}
			});
			monitor.beginTask(Messages.LabOrderImport_monitorImportiereHL7, hl7Files.length);
			if (hl7Files != null) {
				HL7ParserV23 hl7Parser = new HL7ParserV23();
				for (File hl7File : hl7Files) {
					if (monitor.isCanceled()) {
						break;
					}
					String msg = MessageFormat.format("Parse Datei {0}..", hl7File.getName());//$NON-NLS-1$
					monitor.subTask(msg);
					MedicsLogger.getLogger().println(msg);
					// HL7 Datei lesen
					boolean importOk = true;
					ObservationMessage observation = hl7Parser.readORU_R01(hl7File);
					for (String error : hl7Parser.getErrorList()) {
						importOk = false;
						MedicsLogger.getLogger().println(MessageFormat.format("ERROR: {0}", error)); //$NON-NLS-1$
					}
					for (String warn : hl7Parser.getWarningList()) {
						MedicsLogger.getLogger().println(MessageFormat.format("WARN: {0}", warn)); //$NON-NLS-1$
					}
					if (importOk) {
						importOk = addObservations(observation);
					}
					if (importOk) {
						// Archivieren
						moveToArchiv(hl7File);
					} else {
						errorCount++;
						monitor.subTask(MessageFormat.format(
							"Fehler beim Parsen der Datei {0}!", hl7File.getName()));//$NON-NLS-1$
					}
					
					monitor.worked(1);
				}
			}
		}
		
		if (errorCount > 0) {
			SWTHelper.showError(Messages.LabOrderImport_errorTitle,
				MessageFormat.format(Messages.LabOrderImport_errorMsgVerarbeitung, errorCount));
		}
		
		MedicsLogger.getLogger().println(
			MessageFormat.format("{0}: Medics Laborimport beendet", df.format(new Date()))); //$NON-NLS-1$
		MedicsLogger.getLogger().println(""); //$NON-NLS-1$
		
		return Status.OK_STATUS;
	}
	
	/**
	 * Datei wird ins Archiv Verzeichnis verschoben
	 * 
	 * @param file
	 */
	private boolean moveToArchiv(final File file){
		String archivDir = MedicsPreferencePage.getArchivDir();
		boolean ok = false;
		if (FileTool.copyFile(file, new File(archivDir + File.separator + file.getName()),
			FileTool.REPLACE_IF_EXISTS)) {
			ok = file.delete();
		}
		return ok;
	}
	
	/**
	 * Fügt Observations (Laboreinträge) zu Patient hinzu
	 * 
	 * @param observation
	 */
	private boolean addObservations(ObservationMessage observation){
		MedicsLogger.getLogger().println("Laboreinträge erstellen.."); //$NON-NLS-1$
		Patient patient = getPatient(observation);
		if (patient != null) {
			PatientLabor labor = new PatientLabor(patient);
			for (IValueType type : observation.getObservations()) {
				if (type.getDate() == null) {
					type.setDate(observation.getDateTimeOfMessage());
					MedicsLogger
						.getLogger()
						.println(
							MessageFormat
								.format(
									"WARN: Observation (OBX) ohne Datum (OBX-14). Verwende Datum aus MSH-7: {0}", //$NON-NLS-1$
									observation.getDateTimeOfMessage().toString()));
				}
				if (type instanceof StringData) {
					labor.addLaborItem((StringData) type);
				} else if (type instanceof EncapsulatedData) {
					try {
						labor.addDocument((EncapsulatedData) type);
					} catch (IOException e) {
						MedicsLogger.getLogger().println(
							MessageFormat.format("ERROR: Dokument hinzufügen: {0}", //$NON-NLS-1$
								e.getMessage()));
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Liest alle Patient mit einer bestimmten PatientenNr. Eigentlich sollte es nur 1 Patient
	 * geben, aber man weiss ja nie!
	 * 
	 * @param patId
	 * @return List der gefundenen Patienten
	 */
	private List<Patient> readPatienten(final String patId){
		Query<Patient> patientQuery = new Query<Patient>(Patient.class);
		patientQuery.add(Patient.FLD_PATID, Query.EQUALS, patId);
		return patientQuery.execute();
	}
	
	/**
	 * Liest Patient
	 * 
	 * @param observation
	 * @return
	 */
	private Patient getPatient(final ObservationMessage observation){
		// Suche Patient anhand internal PID
		Patient patient = null;
		String pid3 = observation.getInternalPid();
		String auftragsNr = observation.getOrderNumber();
		// Anhand observation.getOrderNumber() den Patienten suchen
		if (auftragsNr != null && auftragsNr.length() > 0) {
			Query<KontaktOrderManagement> patientOrderNrQuery =
				new Query<KontaktOrderManagement>(KontaktOrderManagement.class);
			patientOrderNrQuery.add(KontaktOrderManagement.FLD_ORDER_NR, Query.EQUALS, auftragsNr);
			List<KontaktOrderManagement> patientOrderNrList = patientOrderNrQuery.execute();
			if (patientOrderNrList.size() == 0) {
				MedicsLogger
					.getLogger()
					.println(
						MessageFormat
							.format(
								"ERROR: Kein Patient zu Auftragsnummer={0} gefunden. Import abgebrochen..!", //$NON-NLS-1$
								auftragsNr));
			} else {
				// Suche übereinstimmung
				List<Patient> patientList = readPatienten(pid3);
				for (KontaktOrderManagement kontaktOrderMgt : patientOrderNrList) {
					String kontaktId = kontaktOrderMgt.getKontakt().getId();
					for (Patient pat : patientList) {
						if (kontaktId.equals(pat.getId())) {
							patient = pat;
						}
					}
				}
				if (patient == null) {
					MedicsLogger
						.getLogger()
						.println(
							MessageFormat
								.format(
									"ERROR: Patient der Auftragsnummer ({0}) kann nicht zu Patient PID ({1}) zugeordnet werden. Import abgebrochen..!", //$NON-NLS-1$
									auftragsNr, pid3));
				}
			}
		} else {
			// Keine Auftragsnummer. Verwende PID
			MedicsLogger
				.getLogger()
				.println(
					MessageFormat
						.format(
							"WARN: Kein Auftragsnummer vorhanden. Patient wird anhand PID-3 ({0}) bestimmt !", //$NON-NLS-1$
							pid3));
			List<Patient> patientList = readPatienten(pid3);
			if (patientList.size() == 0) {
				MedicsLogger.getLogger().println(
					MessageFormat.format("ERROR: Kein Patient mit ID={0} gefunden!", pid3)); //$NON-NLS-1$
			} else if (patientList.size() > 0) {
				patient = patientList.get(0);
				if (patientList.size() > 1) {
					MedicsLogger.getLogger().println(
						MessageFormat.format(
							"WARN: Mehrere Patienten mit ID={0} gefunden! Verwende Erster: {1}", //$NON-NLS-1$
							pid3, patient.getLabel()));
				}
			}
		}
		
		return patient;
	}
	
	@Override
	public String getTitle(){
		return Messages.LabOrderImport_titleImport;
	}
	
	@Override
	public String getDescription(){
		return Messages.LabOrderImport_descriptionImport;
	}
	
	@Override
	public Composite createPage(Composite parent){
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		composite.setLayout(new GridLayout(2, false));
		
		// Rechnung Verzeichnis
		Label lblDownloadDir = new Label(composite, SWT.NONE);
		lblDownloadDir.setText(Messages.LabOrderImport_labelDownloadDir);
		
		final Text txtDownloadDir = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		txtDownloadDir.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		String downloadDir = MedicsPreferencePage.getDownloadDir();
		if (downloadDir != null) {
			txtDownloadDir.setText(downloadDir);
		}
		
		// Kategorie Verzeichnis
		Label lblKategorie = new Label(composite, SWT.NONE);
		lblKategorie.setText(Messages.LabOrderImport_labelDocumentCategory);
		
		final Text txtKategorie = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		txtKategorie.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		String kategorie = MedicsPreferencePage.getDokumentKategorie();
		if (kategorie != null) {
			txtKategorie.setText(kategorie);
		}
		
		return composite;
	}
	
}
