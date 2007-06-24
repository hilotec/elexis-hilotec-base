package ch.elexis.TarmedRechnung;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.TarmedRechnung.messages"; //$NON-NLS-1$

	public static String RechnungsDrucker_AllFinishedNoErrors;

	public static String RechnungsDrucker_Couldntbeprintef;

	public static String RechnungsDrucker_CouldntOpenPrintView;

	public static String RechnungsDrucker_ErrorsWhiilePrintingAdvice;

	public static String RechnungsDrucker_ErrorsWhilePrinting;

	public static String RechnungsDrucker_PrintAsTarmed;

	public static String RechnungsDrucker_PrintingBills;

	public static String RechnungsDrucker_PrintingFinished;

	public static String RechnungsDrucker_TheBill;

	public static String RechnungsDrucker_toPrinter;

	public static String RechnungsDrucker_WithESR;

	public static String RechnungsDrucker_WithForm;
	
	public static String RechnungsDrucker_IgnoreFaults;

	public static String Validator_NoCase;

	public static String Validator_NoDiagnosis;

	public static String Validator_NoEAN;

	public static String Validator_NoEAN2;

	public static String Validator_NoMandator;

	public static String Validator_NoName;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
