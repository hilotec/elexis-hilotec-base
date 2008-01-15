package ch.elexis.laborimport.teamw;


public class HL7 extends ch.elexis.importers.HL7 {

	public HL7(String labor, String kuerzel) {
		super(labor, kuerzel);
	}

	/** 
	 * Findet Kommentare zu einem OBX. 
	 * Bei Team-W sind die NTE Kommentar nachfolgend abgelegt.
	 * @param hl7Rows
	 * @return String
	 */
	public String getOBXComments(String[] hl7Rows, String[] obxFields) {
		StringBuilder ret=new StringBuilder();
		
		int i=0;
		boolean started = false;
		boolean end = false;
		while (i<hl7Rows.length && !end) {
			if (hl7Rows[i].startsWith("OBX")) {
				String[] obx=hl7Rows[i].split(getSeparator());
				if (started) {
					end = true;
				} else {
					started = (obx[1].equals(obxFields[1]) && obx[2].equals(obxFields[2]) && obx[3].equals(obxFields[3]));
				}
			} else if (hl7Rows[i].startsWith("OBR")) { // Nach OBR kommen OBR Kommentare
				if (started) {
					end = true;
				}
			} else if (started && hl7Rows[i].startsWith("NTE")) {
				String[] nte=hl7Rows[i].split(getSeparator());
				ret.append(nte[3]).append("\n");
			}
			i++;
		}
		return ret.toString();
	}
}
