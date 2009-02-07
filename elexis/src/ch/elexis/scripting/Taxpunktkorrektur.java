package ch.elexis.scripting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;

public class Taxpunktkorrektur {
	FileWriter writer;

	
	public String recalc(String dateFrom, String dateUntil, String abrsystem, double newTP){
		File file =
			new File(System.getProperty("user.home") + File.separator + "elexis" + File.separator
				+ "taxpunktkorrektur.log");
		try {
			writer = new FileWriter(file);
			if (SWTHelper.askYesNo("WARNUNG", "Wirklich alle Konsultationen von " + dateFrom
				+ " bis " + dateUntil + " auf " + Double.toString(newTP) + " umrechnen?")) {
				;
			} else {
				writer.write("aborted by user");
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return ex.getMessage();
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {}
		}
		return "allgemeiner Fehler";
	}
}
