package ch.elexis.banking;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.banking.messages"; //$NON-NLS-1$
	public static String ESR_bad_user_defin;
	public static String ESR_esr_invalid;
	public static String ESR_warning_esr_not_correct;
	public static String ESRFile_cannot_read_esr;
	public static String ESRFile_esrfile_not_founde;
	public static String ESRFile_file_already_read;
	public static String ESRView_booked;
	public static String ESRView_couldnotread;
	public static String ESRView_errorESR;
	public static String ESRView_errorESR2;
	public static String ESRView_errrorESR2;
	public static String ESRView_ESR_finished;
	public static String ESRView_headline;
	public static String ESRView_interrupted;
	public static String ESRView_ispaid;
	public static String ESRView_loadESR;
	public static String ESRView_morethan;
	public static String ESRView_not_booked;
	public static String ESRView_paid;
	public static String ESRView_paymentfor;
	public static String ESRView_read_ESR;
	public static String ESRView_read_ESR_explain;
	public static String ESRView_reading_ESR;
	public static String ESRView_rechnung;
	public static String ESRView_selectESR;
	public static String ESRView_storno_for;
	public static String ESRView_toohigh;
	public static String ESRView_vesrfor;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
