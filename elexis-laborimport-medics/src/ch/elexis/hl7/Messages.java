package ch.elexis.hl7;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.hl7.messages"; //$NON-NLS-1$
	public static String HL7Parser_valueTypeNotImplemented;
	public static String HL7Parser_wrongMessageType;
	public static String HL7Parser_wrongObservationId;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
