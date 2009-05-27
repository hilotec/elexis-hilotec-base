package ch.elexis.text;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.text.messages"; //$NON-NLS-1$
	public static String EnhancedTextField_5;
	public static String EnhancedTextField_asMacro;
	public static String EnhancedTextField_copyAction;
	public static String EnhancedTextField_cutAction;
	public static String EnhancedTextField_enterNameforMacro;
	public static String EnhancedTextField_newMacro;
	public static String EnhancedTextField_pasteAction;
	public static String EnhancedTextField_RemoveXref;
	public static String EnhancedTextField_ThisChargeIsInvalid;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
