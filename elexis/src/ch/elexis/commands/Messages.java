package ch.elexis.commands;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.commands.messages"; //$NON-NLS-1$
	public static String FallPlaneRechnung_PlanBillingAfterDays;
	public static String FallPlaneRechnung_PlanBillingHeading;
	public static String FallPlaneRechnung_PlanBillingPleaseEnterPositiveInteger;
	public static String MahnlaufCommand_Mahngebuehr1;
	public static String MahnlaufCommand_Mahngebuehr3;
	public static String MahnlaufCommand_Mahngebühr2;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
