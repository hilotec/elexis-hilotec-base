package ch.elexis.buchhaltung.model;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.buchhaltung.model.messages"; //$NON-NLS-1$
	public static String FakturaJournal_Amount;
	public static String FakturaJournal_DatabaseQuery;
	public static String FakturaJournal_Date;
	public static String FakturaJournal_FA;
	public static String FakturaJournal_Faktura;
	public static String FakturaJournal_FakturaJournal;
	public static String FakturaJournal_GU;
	public static String FakturaJournal_PatientNr;
	public static String FakturaJournal_ST;
	public static String FakturaJournal_Text;
	public static String FakturaJournal_Type;
	public static String FakturaJournalDetail_Beschreibung;
	public static String FakturaJournalDetail_Name;
	public static String FakturaJournalDetail_Patient;
	public static String FakturaJournalDetail_Rechnungsempfaenger;
	public static String FakturaJournalDetail_Rechnungssteller;
	public static String ListeNachFaelligkeit_Amount;
	public static String ListeNachFaelligkeit_AnalyzingBills;
	public static String ListeNachFaelligkeit_BillNr;
	public static String ListeNachFaelligkeit_BillsAfterDaysDue;
	public static String ListeNachFaelligkeit_DatabaseQuery;
	public static String ListeNachFaelligkeit_Due;
	public static String ListeNachFaelligkeit_PatientNr;
	public static String OffenePostenListe_AnalyzingBills;
	public static String OffenePostenListe_BillNr;
	public static String OffenePostenListe_BillState;
	public static String OffenePostenListe_DatabaseQuery;
	public static String OffenePostenListe_Open;
	public static String OffenePostenListe_OpenAmount;
	public static String OffenePostenListe_OpenBillsPer;
	public static String OffenePostenListe_PatientNr;
	public static String ZahlungsJournal_AD;
	public static String ZahlungsJournal_Amount;
	public static String ZahlungsJournal_DatabaseQuery;
	public static String ZahlungsJournal_Date;
	public static String ZahlungsJournal_PatientNr;
	public static String ZahlungsJournal_PaymentJournal;
	public static String ZahlungsJournal_Text;
	public static String ZahlungsJournal_Type;
	public static String ZahlungsJournal_TZ;
	public static String ZahlungsJournal_ZA;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
