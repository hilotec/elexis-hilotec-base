package ch.elexis.icpc.fire.handlers;

import java.util.Hashtable;
import java.util.List;

import org.jdom.Element;

import ch.elexis.befunde.Messwert;
import ch.elexis.befunde.xchange.BefundElement;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.rgw.tools.StringTool;

public class Analyzer {
	private Konsultation mine;
	private Hashtable<String, Object> hash;
	private Hashtable<String, String[]> params = new Hashtable<String, String[]>();
	private String[] paramNames;

	public Analyzer(Konsultation k) {
		mine = k;
		Patient pat = k.getFall().getPatient();
		if (pat != null) {
			Messwert setup = Messwert.getSetup();
			hash = setup.getHashtable("Befunde");
			String names = (String) hash.get("names");
			if (!StringTool.isNothing(names)) {
				paramNames = names.split(Messwert.SETUP_SEPARATOR);
				for (String n : paramNames) {
					String vals = (String) hash.get(n + "_FIELDS");
					if (vals != null) {
						String[] flds = vals.split(Messwert.SETUP_SEPARATOR);
						for (int i = 0; i < flds.length; i++) {
							flds[i] = flds[i]
									.split(Messwert.SETUP_CHECKSEPARATOR)[0];
							String[] header = flds[i].split("=", 2);
							flds[i] = header[0];
						}
						params.put(n, flds);
					}
				}

			}
			Query<Messwert> qbe = new Query<Messwert>(Messwert.class);
			qbe.add("PatientID", "=", pat.getId());
			List<Messwert> mw = qbe.execute();
			for (Messwert m : mw) {
				String name = m.get("Name");
				String[] fl = params.get(name);
				if (fl != null) {
					for (String field : fl) {
						// tuwat
					}
				}
			}
		}
	}

	public Element findVitalDaten() {
		return null;
	}
}
