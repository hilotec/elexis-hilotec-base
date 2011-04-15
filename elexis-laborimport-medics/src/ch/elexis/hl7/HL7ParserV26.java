package ch.elexis.hl7;

import java.util.Date;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v26.datatype.CWE;
import ca.uhn.hl7v2.model.v26.datatype.XAD;
import ca.uhn.hl7v2.model.v26.datatype.XPN;
import ca.uhn.hl7v2.model.v26.datatype.XTN;
import ca.uhn.hl7v2.model.v26.message.OML_O21;
import ca.uhn.hl7v2.model.v26.segment.IN1;
import ca.uhn.hl7v2.model.v26.segment.MSH;
import ca.uhn.hl7v2.model.v26.segment.NK1;
import ca.uhn.hl7v2.model.v26.segment.ORC;
import ca.uhn.hl7v2.model.v26.segment.PID;
import ca.uhn.hl7v2.model.v26.segment.PV1;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import ch.elexis.Hub;
import ch.elexis.data.Anwender;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.Xid;
import ch.elexis.hl7.util.HL7Helper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class HL7ParserV26 extends HL7Parser {
	
	public HL7ParserV26(String receivingApplication, String receivingFacility){
		super(receivingApplication, receivingFacility);
	}
	
	/**
	 * Creates an OMG_O19 message
	 * 
	 * @param patient
	 * @param kostentraeger
	 * @param rechnungsempfaenger
	 * @param auftragsNummer
	 * @param plan
	 *            Abrechnungssystem (MV, UVG, VVG, KVG, usw)
	 * @param beginDate
	 * @param vnr
	 *            Versicherungs-, Fall- oder Unfallnr
	 * @return
	 */
	public String createOML_O21(final Patient patient, final Kontakt rechnungsempfaenger,
		final Kontakt kostentraeger, final String plan, final Date beginDate, final String fallNr,
		final long auftragsNummer) throws DataTypeException, HL7Exception{
		
		OML_O21 omg = new OML_O21();
		fillMSH(omg.getMSH(), "OML", "O21", patient); //$NON-NLS-1$ //$NON-NLS-2$
		fillPID(omg.getPATIENT().getPID(), patient);
		fillNK1(omg.getPATIENT().getNK1(), rechnungsempfaenger);
		fillPV1(omg.getPATIENT().getPATIENT_VISIT().getPV1(), patient, beginDate);
		fillIN1(omg.getPATIENT().getINSURANCE().getIN1(), patient, kostentraeger, plan, fallNr);
		fillORC(omg.getORDER().getORC(), auftragsNummer);
		
		// Now, let's encode the message and look at the output
		Parser parser = new PipeParser();
		return parser.encode(omg);
	}
	
	@Override
	public String getVersion(){
		return "2.6"; //$NON-NLS-1$
	}
	
	/**
	 * Adds patient data to XPN segment
	 * 
	 * @param xpn
	 * @param patient
	 * @throws DataTypeException
	 */
	private void addKontaktToXPN(XPN xpn, final Kontakt kontakt) throws DataTypeException{
		String name = ""; //$NON-NLS-1$
		String vorname = ""; //$NON-NLS-1$
		String title = ""; //$NON-NLS-1$
		if (kontakt != null) {
			name = kontakt.get(Kontakt.FLD_NAME1);
			if (name == null) {
				name = ""; //$NON-NLS-1$
			}
			vorname = kontakt.get(Kontakt.FLD_NAME2);
			if (vorname == null) {
				vorname = ""; //$NON-NLS-1$
			}
			title = kontakt.get("Titel"); //$NON-NLS-1$
			if (title == null) {
				title = ""; //$NON-NLS-1$
			}
		}
		xpn.getXpn1_FamilyName().getSurname().setValue(name.trim());
		xpn.getXpn2_GivenName().setValue(vorname.trim());
		xpn.getXpn3_SecondAndFurtherGivenNamesOrInitialsThereof().setValue(""); //$NON-NLS-1$
		xpn.getXpn4_SuffixEgJRorIII().setValue(""); //$NON-NLS-1$
		xpn.getXpn5_PrefixEgDR().setValue(""); //$NON-NLS-1$
		xpn.getXpn6_DegreeEgMD().setValue(title.trim());
		xpn.getXpn7_NameTypeCode().setValue(""); //$NON-NLS-1$
		xpn.getXpn8_NameRepresentationCode().setValue(""); //$NON-NLS-1$
		xpn.getXpn9_NameContext().getCwe1_Identifier().setValue(""); //$NON-NLS-1$
	}
	
	/**
	 * Adds address of kontakt to XAD segment
	 * 
	 * @param xad
	 * @param kontakt
	 * @throws DataTypeException
	 */
	private void addAddressToXAD(XAD xad, final Kontakt kontakt) throws DataTypeException{
		String street = ""; //$NON-NLS-1$
		String other = ""; //$NON-NLS-1$
		String city = ""; //$NON-NLS-1$
		String zip = ""; //$NON-NLS-1$
		String country = ""; //$NON-NLS-1$
		if (kontakt != null) {
			street = kontakt.get(Kontakt.FLD_STREET);
			if (street == null) {
				street = ""; //$NON-NLS-1$
			}
			other = kontakt.get(Patient.FLD_NAME3);
			if (other == null) {
				other = ""; //$NON-NLS-1$
			}
			city = kontakt.get(Patient.FLD_PLACE);
			if (city == null) {
				city = ""; //$NON-NLS-1$
			}
			zip = kontakt.get(Patient.FLD_ZIP);
			if (zip == null) {
				zip = ""; //$NON-NLS-1$
			}
			country = kontakt.get(Patient.FLD_COUNTRY);
			if (country != null) {
				country = ""; //$NON-NLS-1$
			}
		}
		xad.getXad1_StreetAddress().getSad1_StreetOrMailingAddress().setValue(street.trim());
		xad.getXad2_OtherDesignation().setValue(other.trim());
		xad.getXad3_City().setValue(city.trim());
		xad.getXad4_StateOrProvince().setValue(""); //$NON-NLS-1$
		xad.getXad5_ZipOrPostalCode().setValue(zip.trim());
		xad.getXad6_Country().setValue(country.trim());
	}
	
	/**
	 * Adds contact informations to XTN segment
	 * 
	 * @param xtn
	 * @param kontakt
	 * @throws DataTypeException
	 */
	private void addPhone1ToXTN(XTN xtn, final Kontakt kontakt) throws DataTypeException{
		String phone1 = ""; //$NON-NLS-1$
		String email = ""; //$NON-NLS-1$
		String fax = ""; //$NON-NLS-1$
		if (kontakt != null) {
			phone1 = kontakt.get(Kontakt.FLD_PHONE1);
			if (phone1 == null) {
				phone1 = ""; //$NON-NLS-1$
			}
			email = kontakt.get(Kontakt.FLD_E_MAIL);
			if (email == null) {
				email = ""; //$NON-NLS-1$
			}
			fax = kontakt.get(Kontakt.FLD_FAX);
			if (fax == null) {
				fax = ""; //$NON-NLS-1$
			}
		}
		xtn.getXtn1_TelephoneNumber().setValue(phone1.trim());
		xtn.getXtn2_TelecommunicationUseCode().setValue(""); //$NON-NLS-1$
		xtn.getXtn3_TelecommunicationEquipmentType().setValue(""); //$NON-NLS-1$
		xtn.getXtn4_CommunicationAddress().setValue(email.trim());
		xtn.getXtn5_CountryCode().setValue(""); //$NON-NLS-1$
		xtn.getXtn6_AreaCityCode().setValue(""); //$NON-NLS-1$
		xtn.getXtn7_LocalNumber().setValue(""); //$NON-NLS-1$
		xtn.getXtn8_Extension().setValue(""); //$NON-NLS-1$
		xtn.getXtn9_AnyText().setValue(""); //$NON-NLS-1$
		xtn.getXtn10_ExtensionPrefix().setValue(""); //$NON-NLS-1$
		xtn.getXtn11_SpeedDialCode().setValue(fax.trim());
	}
	
	/**
	 * Adds contact informations to XTN segment
	 * 
	 * @param xtn
	 * @param kontakt
	 * @throws DataTypeException
	 */
	private void addPhone2ToXTN(XTN xtn, final Kontakt kontakt) throws DataTypeException{
		if (kontakt != null) {
			xtn.getXtn1_TelephoneNumber().setValue(kontakt.get(Kontakt.FLD_PHONE2));
		}
	}
	
	/**
	 * Fills MSH segment
	 * 
	 * @param msh
	 * @param patient
	 */
	private void fillMSH(final MSH msh, final String messageId, final String event,
		final Patient patient) throws DataTypeException{
		msh.getMsh1_FieldSeparator().setValue("|"); //$NON-NLS-1$
		msh.getMsh2_EncodingCharacters().setValue("^~\\&"); //$NON-NLS-1$
		// Name der sendenden Anwendung. Dessen Eindeutigkeit im Kommunikations-Netzwerk liegt
		// in der Verantwortung des jeweiligen Systemadministrators. Nimm diesen Text: CHELEXIS
		msh.getMsh3_SendingApplication().getHd1_NamespaceID().setValue("CHELEXIS"); //$NON-NLS-1$
		msh.getMsh3_SendingApplication().getHd2_UniversalID().setValue(""); //$NON-NLS-1$
		msh.getMsh3_SendingApplication().getHd3_UniversalIDType().setValue("PATDATA"); //$NON-NLS-1$
		// Name der sendenden Institution. Optional (Beschreibung gemäss HL7 Standard).
		// Gemäss HD Type Definition von HL7 folgendermassen:
		// <mandantenkürzel>^<EAN des Mandanten>^L Beispiel: mf7601234567890^L
		msh.getMsh4_SendingFacility().getHd1_NamespaceID().setValue(
			Hub.actMandant.get(Anwender.LABEL));
		String ean = Hub.actMandant.getXid(Xid.DOMAIN_EAN);
		msh.getMsh4_SendingFacility().getHd2_UniversalID().setValue(ean);
		msh.getMsh4_SendingFacility().getHd3_UniversalIDType().setValue("L"); //$NON-NLS-1$
		// Name der empfangenden Anwendung. Eindeutigkeit dito MSH.3
		// MSH-5: IMED
		msh.getMsh5_ReceivingApplication().getHd1_NamespaceID().setValue(this.receivingApplication);
		msh.getMsh5_ReceivingApplication().getHd2_UniversalID().setValue(""); //$NON-NLS-1$
		// Name der empfangenden Institution . Optional (Beschreibung gemäss HL7 Standard).
		// Vergleiche auch MSH.5
		// MSH-6: PRAXIS
		msh.getMsh6_ReceivingFacility().getHd1_NamespaceID().setValue(this.receivingFacility);
		msh.getMsh6_ReceivingFacility().getHd2_UniversalID().setValue(""); //$NON-NLS-1$
		msh.getMsh7_DateTimeOfMessage().setValue(HL7Helper.dateToString(new Date()));
		msh.getMsh8_Security().setValue(""); //$NON-NLS-1$
		msh.getMsh9_MessageType().getMessageCode().setValue(messageId);
		msh.getMsh9_MessageType().getTriggerEvent().setValue(event);
		msh.getMsh9_MessageType().getMessageStructure().setValue(""); //$NON-NLS-1$
		// Eindeutige Nachrichtennummer: GUID
		msh.getMsh10_MessageControlID().setValue(StringTool.unique("MessageControlID")); //$NON-NLS-1$
		msh.getMsh11_ProcessingID().getPt1_ProcessingID().setValue(
			StringTool.unique("ProcessingID")); //$NON-NLS-1$
		msh.getMsh12_VersionID().getVid1_VersionID().setValue(getVersion());
	}
	
	/**
	 * Fills PID segment
	 * 
	 * @param pid
	 * @param patient
	 * @throws DataTypeException
	 * @throws HL7Exception
	 */
	private void fillPID(final PID pid, final Patient patient) throws DataTypeException,
		HL7Exception{
		String sex = ""; //$NON-NLS-1$
		String geschlecht = patient.getGeschlecht();
		if (geschlecht != null && geschlecht.length() > 0) {
			sex = "M"; //$NON-NLS-1$
			if (Patient.FEMALE.toUpperCase().equals(patient.getGeschlecht().toUpperCase())) {
				sex = "F"; //$NON-NLS-1$
			}
		}
		pid.getPid1_SetIDPID().setValue("1"); //$NON-NLS-1$
		pid.getPid2_PatientID().getIDNumber().setValue(patient.getPatCode());
		pid.getPid3_PatientIdentifierList(0).getIDNumber().setValue(patient.getPatCode());
		pid.getPid4_AlternatePatientIDPID(0).getIDNumber().setValue(patient.getPatCode());
		addKontaktToXPN(pid.getPid5_PatientName(0), patient);
		pid.getPid16_MaritalStatus().getCwe1_Identifier().setValue(""); //$NON-NLS-1$
		pid.getPid7_DateTimeOfBirth().setValue(
			HL7Helper.dateToString(new TimeTool(patient.getGeburtsdatum()).getTime()));
		pid.getPid8_AdministrativeSex().setValue(sex);
		pid.getPid9_PatientAlias(0).getXpn1_FamilyName().getFn1_Surname().setValue(""); //$NON-NLS-1$
		pid.getPid10_Race(0).getCwe1_Identifier().setValue(""); //$NON-NLS-1$
		addAddressToXAD(pid.getPid11_PatientAddress(0), patient);
		pid.getPid12_CountyCode().setValue(""); //$NON-NLS-1$
		addPhone1ToXTN(pid.getPid13_PhoneNumberHome(0), patient);
		addPhone2ToXTN(pid.getPid14_PhoneNumberBusiness(0), patient);
	}
	
	/**
	 * Fills NK1 segment
	 * 
	 * @param nk1
	 * @param rechnungsempfaenger
	 * @throws DataTypeException
	 * @throws HL7Exception
	 */
	private void fillNK1(final NK1 nk1, final Kontakt rechnungsempfaenger)
		throws DataTypeException, HL7Exception{
		nk1.getNk11_SetIDNK1().setValue("1"); //$NON-NLS-1$
		addKontaktToXPN(nk1.getNk12_Name(0), rechnungsempfaenger);
		
		CWE cwe = nk1.getNk13_Relationship();
		cwe.getCwe1_Identifier().setValue(""); //$NON-NLS-1$
		cwe.getCwe2_Text().setValue("INVOICERECEIPT"); //$NON-NLS-1$
		
		addAddressToXAD(nk1.getNk14_Address(0), rechnungsempfaenger);
		addPhone1ToXTN(nk1.getNk15_PhoneNumber(0), rechnungsempfaenger);
		addPhone2ToXTN(nk1.getNk16_BusinessPhoneNumber(0), rechnungsempfaenger);
	}
	
	/**
	 * Fills PV1 segment
	 * 
	 * @param pv1
	 * @param patient
	 * @throws DataTypeException
	 * @throws HL7Exception
	 */
	private void fillPV1(final PV1 pv1, final Patient patient, final Date beginDate)
		throws DataTypeException, HL7Exception{
		pv1.getPv11_SetIDPV1().setValue("1"); //$NON-NLS-1$
		pv1.getPv12_PatientClass().setValue("O"); //$NON-NLS-1$
		
		// PLV-13: Aktueller Aufenthaltsort des Patienten, optional
		// Empfehlung: Wenn vorhanden, dann ausfüllen -> In unserem Fall leer lassen
		pv1.getPv14_AdmissionType().setValue(""); //$NON-NLS-1$
		pv1.getPv15_PreadmitNumber().getCx1_IDNumber().setValue(""); //$NON-NLS-1$
		pv1.getPv16_PriorPatientLocation().getPl1_PointOfCare().setValue(""); //$NON-NLS-1$
		
		// Fallnummer, optional (Beschreibung gemäss HL7 Standard)
		// Empfehlung: Wenn vorhanden, dann ausfüllen -> In unserem Fall leer lassen oder den Key
		// des Falles nehmen
		pv1.getPv119_VisitNumber().getIDNumber().setValue(""); //$NON-NLS-1$
		// ...
		pv1.getPv144_AdmitDateTime().setValue(HL7Helper.dateToString(beginDate));
	}
	
	/**
	 * Fills IN1 segment
	 * 
	 * @param in1
	 * @param patient
	 * @param kostentraeger
	 * @throws DataTypeException
	 * @throws HL7Exception
	 */
	private void fillIN1(final IN1 in1, final Patient patient, final Kontakt kostentraeger,
		final String plan, final String fallNr) throws DataTypeException, HL7Exception{
		in1.getIn11_SetIDIN1().setValue("1"); //$NON-NLS-1$
		in1.getIn12_InsurancePlanID().getCwe1_Identifier().setValue(plan);
		// EAN Nummer der Versicherung
		// Beispiel: EAN123456789^^^CHEMEDIAT;
		String ean = kostentraeger.getXid(Xid.DOMAIN_EAN);
		in1.getIn13_InsuranceCompanyID(0).getCx1_IDNumber().setValue("EAN" + ean); //$NON-NLS-1$
		in1.getIn13_InsuranceCompanyID(0).getCx4_AssigningAuthority().getHd1_NamespaceID()
			.setValue("CHEMEDIAT"); //$NON-NLS-1$
		in1.getIn14_InsuranceCompanyName(0).getXon1_OrganizationName().setValue(
			kostentraeger.get(Kontakt.FLD_NAME1));
		
		addAddressToXAD(in1.getIn15_InsuranceCompanyAddress(0), kostentraeger);
		addKontaktToXPN(in1.getIn116_NameOfInsured(0), patient);
		in1.getIn136_PolicyNumber().setValue(fallNr);
		
		addAddressToXAD(in1.getIn119_InsuredSAddress(0), patient);
	}
	
	/**
	 * Fills ORC segment
	 * 
	 * @param orc
	 * @param auftragsNummer
	 * @throws DataTypeException
	 */
	private void fillORC(final ORC orc, final long auftragsNummer) throws DataTypeException{
		orc.getOrc1_OrderControl().setValue("1"); //$NON-NLS-1$
		orc.getOrc2_PlacerOrderNumber().getEi1_EntityIdentifier().setValue(
			new Long(auftragsNummer).toString());
	}
}
