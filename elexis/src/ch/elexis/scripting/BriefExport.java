package ch.elexis.scripting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import ch.elexis.Desk;
import ch.elexis.data.Brief;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.elexis.util.SWTHelper;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;

public class BriefExport {

	public String doExport() {
		FileDialog fd = new FileDialog(Desk.getTopShell(), SWT.SAVE);
		fd.setFilterExtensions(new String[] { "*.csv" });
		fd.setFilterNames(new String[] { "Comma Separated Values (CVS)" });
		fd.setOverwrite(true);
		String filename = fd.open();
		if (filename != null) {
			List<Brief> briefe = new Query<Brief>(Brief.class).execute();
			File csv = new File(filename);
			File parent = csv.getParentFile();
			File dir = new File(parent, FileTool.getNakedFilename(filename));
			dir.mkdirs();
			try {
				CSVWriter writer = new CSVWriter(new FileWriter(csv));
				String[] header = new String[] { "Betreff", "Datum",
						"Adressat", "Mimetype", "Typ", "Patient", "Pfad" };
				String[] fields = new String[] { Brief.FLD_SUBJECT, Brief.FLD_DATE,
						Brief.FLD_DESTINATION_ID, Brief.FLD_MIME_TYPE, Brief.FLD_TYPE,
						Brief.FLD_PATIENT_ID, Brief.FLD_PATIENT_ID };
				writer.writeNext(header);
				for (Brief brief : briefe) {
					Person pat = brief.getPatient();
					if (pat != null) {
						String subdirname = pat.get(Patient.FLD_PATID);
						if (subdirname != null) {
							File subdir = new File(dir, subdirname);
							subdir.mkdirs();
							String[] line = new String[fields.length];
							brief.get(fields, line);
							byte[] bin = brief.loadBinary();
							if (bin != null) {
								File f = new File(subdir, brief.getId()
										+ ".odt");
								FileOutputStream fos = new FileOutputStream(f);
								fos.write(bin);
								fos.close();
								line[line.length - 1] = dir.getName()
										+ File.separator + subdir.getName()
										+ File.separator + f.getName();
								writer.writeNext(line);
							}
						}
					}
				}
				writer.close();
				return "Export ok";
			} catch (Exception ex) {
				ExHandler.handle(ex);
				SWTHelper.showError("Fehler beim Export", ex.getMessage());
				return "Fehler beim Export";
			}
		}
		return "Abgebrochen";
	}
}
