package ch.elexis.privatnotizen;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.privatnotizen.messages"; //$NON-NLS-1$
	public static String KonsExtension_noteActionLabel;
	public static String KonsExtension_noteActionXREFText;
	public static String NotizInputDialog_noteDlgMessage;
	public static String NotizInputDialog_noteDlgText;
	public static String NotizInputDialog_noteDlgTitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
