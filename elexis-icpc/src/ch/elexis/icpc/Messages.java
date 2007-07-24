package ch.elexis.icpc;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.icpc.messages"; //$NON-NLS-1$
	
	public static String StartDate;
	public static String Title;
	public static String Number;
	public static String Status;
	public static String Active;
	public static String Inactive;

	public static String EpisodeEditDialog_Title;
	public static String EpisodeEditDialog_Create;
	public static String EpisodeEditDialog_Edit;
	public static String EpisodeEditDialog_EnterData;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
