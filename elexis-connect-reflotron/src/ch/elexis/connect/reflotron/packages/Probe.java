package ch.elexis.connect.reflotron.packages;

import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

public class Probe {
	private TimeTool date;
	private String ident;
	private String resultat;
	private String hint;
	private String zusatztext;
	private Patient patient;

	public Probe(final String[] strArray, final Patient pat) {
		this.patient = pat;
		parse(strArray);
	}

	/**
	 * Liest Probendaten aus Array
	 * 
	 * @param strArray
	 *            Array[7]
	 */
	private void parse(final String[] strArray) {
		int dateIndex = strArray[1].indexOf(".");
		int timeIndex = strArray[1].indexOf(":");

		String dateStr = strArray[1].substring(dateIndex - 2, 8);
		String timeStr = strArray[1].substring(timeIndex - 2, 8);

		date = new TimeTool(dateStr);
		date.set(timeStr);

		ident = strArray[2];
		resultat = strArray[3];
		hint = strArray[4];
		zusatztext = strArray[5];
	}

	/**
	 * Schreibt Labordaten
	 */
	public void write() throws PackageException {
		if (getResultat().length() != 24) {
			throw new PackageException("Resultat der Probe zu klein!");
		}

		String paramName;
		String value;
		String unit;
		if (getResultat().length() > 19) {
			if (getResultat().charAt(20) == 'C') {
				// Enzym
				paramName = getResultat().substring(0, 4).trim().toUpperCase();
				value = getResultat().substring(4, 10).trim();
				unit = getResultat().substring(10, 16).trim();
			} else {
				// Substrat
				paramName = getResultat().substring(0, 4).trim().toUpperCase();
				value = getResultat().substring(5, 11).trim();
				unit = getResultat().substring(12, 21).trim();
			}
		} else {
			// Substrat
			paramName = getResultat().substring(0, 4).trim().toUpperCase();
			value = getResultat().substring(5, 11).trim();
			unit = getResultat().substring(12, 21).trim();
		}

		Value val = Value.getValue(paramName, unit);

		val.fetchValue(patient, value, "", getDate());
	}

	public TimeTool getDate() {
		return date;
	}

	public String getIdent() {
		return ident;
	}

	public String getResultat() {
		return resultat;
	}

	public String getHint() {
		return hint;
	}

	public String getZusatztext() {
		return zusatztext;
	}
}
