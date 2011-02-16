package ch.elexis.labor.medics.order;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import ch.elexis.Hub;
import ch.elexis.actions.ElexisEventDispatcher;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.hl7.HL7ParserV26;
import ch.elexis.labor.medics.MedicsActivator;
import ch.elexis.labor.medics.MedicsPreferencePage;
import ch.elexis.labor.medics.Messages;
import ch.elexis.labor.medics.data.KontaktOrderManagement;
import ch.elexis.laborimport.medics.views.MedicsBrowserView;
import ch.elexis.tarmedprefs.TarmedRequirements;
import ch.elexis.util.SWTHelper;
import ch.rgw.io.FileTool;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.TimeTool;

/**
 * Labor Auftrag Aktion
 * 
 * @author immi
 * 
 */
public class LabOrderAction extends Action {
	
	public LabOrderAction(){
		setId("laborder"); //$NON-NLS-1$
		setImageDescriptor(MedicsActivator.getImageDescriptor("rsc/medics16.png")); //$NON-NLS-1$
		setText(Messages.LabOrderAction_nameAction);
	}
	
	/**
	 * Starte Labor Auftrag: <br>
	 * <ul>
	 * <li>Neue Auftragsnummer lösen</li>
	 * <li>HL7 Datei mit Patientendaten erstellen</li>
	 * <li>i/med Seite starten</li>
	 * </ul>
	 */
	@Override
	public void run(){
		Patient patient = ElexisEventDispatcher.getSelectedPatient();
		Kontakt kostentraeger = null;
		Kontakt rechnungsempfaenger = null;
		Date beginDate = null;
		String vnr = ""; //$NON-NLS-1$
		String plan = ""; //$NON-NLS-1$
		// Patient und Kostentraeger bestimmen
		if (patient == null) {
			MessageDialog.openError(new Shell(), Messages.LabOrderAction_errorTitleNoPatientSelected,
				Messages.LabOrderAction_errorMessageNoPatientSelected);
		} else {
			Fall fall = (Fall) ElexisEventDispatcher.getSelected(Fall.class);
			// Selected könnte noch vom vorangehendem Patienten sein
			if (fall != null && fall.getPatient() != null
				&& !patient.getId().equals(fall.getPatient().getId())) {
				fall = null;
			}
			// Wenn nur 1 Fall offen, dann wird dieser verwendet
			if (fall == null) {
				List<Fall> offeneFaelleList = new Vector<Fall>();
				for (Fall tmpFall : patient.getFaelle()) {
					if (tmpFall.isOpen()) {
						offeneFaelleList.add(tmpFall);
					}
				}
				if (offeneFaelleList.size() == 1) {
					fall = offeneFaelleList.get(0);
				}
			}
			if (fall == null) {
				MessageDialog.openError(new Shell(), Messages.LabOrderAction_errorTitleNoFallSelected,
					Messages.LabOrderAction_errorMessageNoFallSelected);
			} else {
				kostentraeger = fall.getRequiredContact("Kostenträger"); //$NON-NLS-1$
				if (kostentraeger == null) {
					kostentraeger = fall.getGarant();
				}
				rechnungsempfaenger = fall.getRequiredContact("Rechnungsempfänger"); //$NON-NLS-1$
				if (rechnungsempfaenger == null) {
					rechnungsempfaenger = fall.getGarant();
				}
				plan = fall.getAbrechnungsSystem();
				beginDate = new TimeTool(fall.getBeginnDatum()).getTime();
				vnr = getVersicherungOderFallNummer(fall);
				
			}
		}
		
		// Auftrag auslösen
		boolean ok = false;
		long orderNr = -1;
		String filenamePath = "-"; //$NON-NLS-1$
		if (patient != null && kostentraeger != null) {
			orderNr = getNextOrderNr(patient);
			filenamePath =
				writeHL7File(patient, rechnungsempfaenger, kostentraeger, plan, beginDate, vnr,
					orderNr);
			if (filenamePath != null) {
				ok = true; // @deprecated openBrowser();
			}
		}
		
		// Meldung ausgeben wenn ok
		if (ok) {
			String patLabel = "-"; //$NON-NLS-1$
			if (patient != null) {
				patLabel = patient.getLabel();
			}
			String orderNrText = ""; //$NON-NLS-1$
			if (orderNr >= 0) {
				orderNrText = new Long(orderNr).toString();
			}
			MessageDialog.openInformation(Hub.getActiveShell(), Messages.LabOrderAction_infoTitleLabOrderFinshed,
				MessageFormat.format(Messages.LabOrderAction_infoMessageLabOrderFinshed,
					orderNrText, patLabel, filenamePath));
		}
	}
	
	/**
	 * Anhand Fall wird die Versicherungs-, bzw Fall-Nr retourniert.
	 * <ul>
	 * <li>KVG: Versicherungsnummer</li>
	 * <li>UVG: Unfallnummer</li>
	 * <li>IV: Fallnummer</li>
	 * <li>VVG: Versicherungsnummer</li>
	 * <li>MV: -</li>
	 * <li>privat: Versicherungsnummer</li>
	 * </ul>
	 * 
	 * @return
	 */
	private String getVersicherungOderFallNummer(final Fall fall){
		String nummer = null;
		String gesetz = fall.getAbrechnungsSystem();
		if (gesetz != null) {
			// Suche über Gesetz
			if (gesetz.trim().toLowerCase().equalsIgnoreCase("ivg")) { //$NON-NLS-1$
				nummer = fall.getRequiredString(TarmedRequirements.CASE_NUMBER);
			} else if (gesetz.trim().toLowerCase().equalsIgnoreCase("uvg")) { //$NON-NLS-1$
				nummer = fall.getRequiredString(TarmedRequirements.ACCIDENT_NUMBER);
			} else {
				nummer = fall.getRequiredString(TarmedRequirements.INSURANCE_NUMBER);
			}
		}
		if (nummer == null) {
			// Zweiter Algorithmus (von Tony)
			nummer = fall.getInfoString(TarmedRequirements.CASE_NUMBER);
			if ("".equals(nummer)){ //$NON-NLS-1$
				nummer=fall.getInfoString(TarmedRequirements.ACCIDENT_NUMBER);
			}
			if ("".equals(nummer)){ //$NON-NLS-1$
				nummer=fall.getInfoString(TarmedRequirements.INSURANCE_NUMBER);
			}
		}
		
		return nummer;
	}
	
	/**
	 * Creates HL7 File (V2.6)
	 * 
	 * @return full filename path of the file. Null if error happens
	 */
	private String writeHL7File(final Patient patient, final Kontakt rechnungsempfaenger,
		final Kontakt kostentraeger, final String plan, final Date beginDate, final String vnr,
		final long orderNr){
		HL7ParserV26 hl7Parser = new HL7ParserV26(Messages.LabOrderAction_receivingApplication, Messages.LabOrderAction_receivingFacility);
		try {
			String encodedMessage =
				hl7Parser.createOML_O21(patient, rechnungsempfaenger, kostentraeger, plan,
					beginDate, vnr, orderNr);
			
			// File speichern
			String filename =
				new Long(orderNr).toString() + "_" + patient.get(Patient.FLD_PATID) + ".hl7"; //$NON-NLS-1$ //$NON-NLS-2$
			File hl7File =
				new File(MedicsPreferencePage.getUploadDir() + File.separator + filename);
			FileTool.writeTextFile(hl7File, encodedMessage);
			
			return hl7File.getPath();
		} catch (Exception e) {
			SWTHelper.showError(
				MessageFormat.format(Messages.LabOrderAction_errorTitleCannotCreateHL7,
					hl7Parser.getVersion()), e.getMessage());
		}
		return null;
	}
	
	/**
	 * Opens a browser in or outside of elexis
	 * 
	 * @return
	 */
	private long getNextOrderNr(final Patient patient) throws NumberFormatException{		
		// Next order number
		long nextOrderNr = 0;
		JdbcLink connection = PersistentObject.getConnection();
		String maxStr =
			connection.getStatement().queryString(
				"SELECT MAX(" + KontaktOrderManagement.FLD_ORDER_NR + ") FROM " //$NON-NLS-1$ //$NON-NLS-2$
					+ KontaktOrderManagement.TABLENAME);
		if (maxStr != null && maxStr.length() > 0) {
			long maxOrderNr = new Long(maxStr).intValue();
			nextOrderNr = maxOrderNr + 1;
		}
		if (nextOrderNr < KontaktOrderManagement.FIRST_ORDER_NR) {
			nextOrderNr = KontaktOrderManagement.FIRST_ORDER_NR;
		}
		
		// Speichern
		KontaktOrderManagement kontaktOrder = new KontaktOrderManagement(patient);
		kontaktOrder.setOrderNr(nextOrderNr);
		
		return nextOrderNr;
	}
	
	/**
	 * Opens a browser in or outside of elexis
	 * 
	 * @return
	 * @deprecated
	 */
	protected boolean openBrowser(){
		try {
			URL url = MedicsPreferencePage.getIMedUrl();
			if (!MedicsPreferencePage.showExtern()) {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(MedicsBrowserView.ID);
			} else {
				IWorkbenchBrowserSupport browserSupport =
					PlatformUI.getWorkbench().getBrowserSupport();
				IWebBrowser browser;
				
				browser = browserSupport.createBrowser(null);
				browser.openURL(url);
			}
			return true;
		} catch (Exception e) {
			SWTHelper.showError(Messages.LabOrderAction_errorTitleCannotShowURL, e.getMessage());
		}
		return false;
	}
	
}
