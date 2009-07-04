package ch.elexis.importer.aeskulap;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.agenda.data.Termin;
import ch.elexis.data.Patient;
import ch.elexis.data.Xid;
import ch.elexis.importers.ExcelWrapper;
import ch.elexis.util.Plannables;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class AgendaImporter {
	public AgendaImporter(File dir, IProgressMonitor moni) {
		moni.subTask("importiere Agenda");
		ExcelWrapper hofs = AeskulapImporter.checkImport(dir + File.separator
				+ "agenda.xls");
		if (hofs != null) {
			float last = hofs.getLastRow();
			float first = hofs.getFirstRow();
			hofs.setFieldTypes(new Class[] { Integer.class, String.class,
					String.class, String.class, String.class, String.class,
					String.class, String.class,String.class });
			int perLine = Math.round(AeskulapImporter.MONITOR_PERTASK
					/ (last - first));
			for (int line = Math.round(first + 1); line <= last; line++) {
				String[] actLine = hofs.getRow(line).toArray(new String[0]);
				String patno = StringTool.getSafe(actLine, 0);
				String arzt = StringTool.getSafe(actLine, 1);
				String datum = StringTool.getSafe(actLine, 2);
				String start = StringTool.getSafe(actLine, 3);
				String end = StringTool.getSafe(actLine, 4);
				String text = StringTool.getSafe(actLine, 5);
				String id = StringTool.getSafe(actLine, 8);
				Patient pat = (Patient) Xid.findObject(AeskulapImporter.PATID,
						patno);

				if (pat != null) {
					id = pat.getId();
				}
				if (StringTool.isNothing(id)) {
					continue;
				}
				String bereich=AeskulapImporter.getLabel(arzt);
				TimeTool ttDay=new TimeTool(datum);
				Plannables.loadTermine(bereich, ttDay);
				Termin t = new Termin(bereich, ttDay.toString(TimeTool.DATE_COMPACT), calcMinutes(start),
						calcMinutes(end), Termin.typStandard(), Termin
								.statusStandard());
				t.set(new String[]{"Wer","Grund"},id,text);

				moni.worked(perLine);
			}
		}
	}

	int calcMinutes(String time) {
		String[] flds = time.split(":");
		if (flds.length > 1) {
			try {
				int hr = Integer.parseInt(flds[0]);
				int min = Integer.parseInt(flds[1]);
				return 60 * hr + min;
			} catch (NumberFormatException ne) {
				ExHandler.handle(ne);

			}
		}
		return 0;
	}
}
