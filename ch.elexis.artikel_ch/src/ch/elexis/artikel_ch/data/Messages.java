package ch.elexis.artikel_ch.data;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.artikel_ch.data.messages"; //$NON-NLS-1$
	public static String Medikament_CodeSystemNameMedicaments;
	public static String MedikamentImporter_BadFileFormat;
	public static String MedikamentImporter_BadPharmaCode;
	public static String MedikamentImporter_MedikamentImportTitle;
	public static String MedikamentImporter_ModeOfImport;
	public static String MedikamentImporter_OnlyIGM10AndIGM11;
	public static String MedikamentImporter_PleaseChoseFile;
	public static String MedikamentImporter_WindowTitleMedicaments;
	public static String MedikamentImporter_SuccessTitel;
	public static String MedikamentImporter_SuccessContent;
	public static String MiGelImporter_ClearAllData;
	public static String MiGelImporter_ModeCreateNew;
	public static String MiGelImporter_ModeUpdateAdd;
	public static String MiGelImporter_PleaseSelectFile;
	public static String MiGelImporter_ReadMigel;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
