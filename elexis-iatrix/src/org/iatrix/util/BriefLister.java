package org.iatrix.util;

import java.util.List;

import ch.elexis.data.Brief;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.util.Log;

public class BriefLister {
    static Log log = Log.get("Problemliste");

    public static void list() {
    	Query<Brief> query = new Query<Brief>(Brief.class);
    	List<Brief> list = query.execute();
    	
    	StringBuilder output = new StringBuilder();
    	
    	for (Brief brief : list) {
    		String betreff = brief.get("Betreff");
    		String datum = brief.get("Datum");
    		String typ = brief.get("Typ");
    		
    		String id;
    		
    		Konsultation kons = null;
    		id = brief.get("BehandlungsID");
    		if (id != null) {
    			kons = Konsultation.load(id);
    		}
    		
    		Patient patientKons = null;
    		if (kons != null) {
    			patientKons = kons.getFall().getPatient();
    		}
    		
    		Patient patientBrief = null;
    		id = brief.get("PatientID");
    		if (id != null) {
    			patientBrief = Patient.load(id);;
    		}
    		
    		Kontakt absender = null;
    		id = brief.get("AbsenderID");
    		if (id != null) {
    			absender = Kontakt.load(id);
    		}
    		
    		Kontakt dest = null;
    		id = brief.get("DestID");
    		if (id != null) {
    			dest = Kontakt.load(id);
    		}
    		
    		// Output
    		StringBuilder sb = new StringBuilder();
    		sb.append("Brief [" + brief.getId() + "]");
    		sb.append("\n");
    		sb.append("  Patient(Brief): ");
   			sb.append(patientBrief != null ? patientBrief.getName() + " " + patientBrief.getVorname() : "null");
   			sb.append(patientBrief != null ? "[" + patientBrief.getId() + "]" : "");
    		sb.append(" | Patient(Kons): ");
   			sb.append(patientKons != null ? patientKons.getName() + " " + patientKons.getVorname() : "null");
   			sb.append(patientKons != null ? "[" + patientKons.getId() + "]" : "");
   			sb.append("\n");
   			sb.append("  Konsultation: ");
   			sb.append(kons != null ? kons.getLabel() : "null");
   			sb.append(kons != null ? "[" + kons.getId() + "]" : "");
   			sb.append("\n");
   			sb.append("  Betreff: ");
   			sb.append(betreff != null ? betreff : "null");
   			sb.append(" | Datum: ");
   			sb.append(datum != null ? datum : "null");
   			sb.append(" | Typ: ");
   			sb.append(typ != null ? typ : "null");
   			sb.append("\n");
   			sb.append("  Absender: ");
   			sb.append(absender != null ? absender.get("Bezeichnung1") + " " + absender.get("Bezeichnung2") : "null");
   			sb.append(absender != null ? "[" + absender.getId() + "]" : "");
   			sb.append(" | Dest: ");
   			sb.append(dest != null ? dest.get("Bezeichnung1") + " " + dest.get("Bezeichnung2") : "null");
   			sb.append(dest != null ? "[" + dest.getId() + "]" : "");
   			
   			output.append(sb);
   			output.append("\n");
    	}
    	
		System.err.println(output.toString());
    }
}
