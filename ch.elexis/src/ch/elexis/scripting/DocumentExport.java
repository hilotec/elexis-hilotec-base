package ch.elexis.scripting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.data.Patient;
import ch.elexis.services.GlobalServiceDescriptors;
import ch.elexis.services.IDocumentManager;
import ch.elexis.status.ElexisStatus;
import ch.elexis.text.IOpaqueDocument;
import ch.elexis.util.Extensions;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;

public class DocumentExport {
	
	public String doExport(){
		IDocumentManager mgr =
			(IDocumentManager) Extensions.findBestService(
				GlobalServiceDescriptors.DOCUMENT_MANAGEMENT, null);
		if (mgr == null) {
			return "Keine Dokumente gefunden";
		}
		try {
			FileDialog fd = new FileDialog(Desk.getTopShell(), SWT.SAVE);
			fd.setFilterExtensions(new String[] {
				"*.csv"
			});
			fd.setFilterNames(new String[] {
				"Comma Separated Values (CVS)"
			});
			fd.setOverwrite(true);
			String filename = fd.open();
			if (filename != null) {
				File csv = new File(filename);
				File parent = csv.getParentFile();
				File dir = new File(parent, FileTool.getNakedFilename(filename));
				dir.mkdirs();
				
				CSVWriter writer = new CSVWriter(new FileWriter(csv));
				String[] header = new String[] {
					"Patient", "Name", "Kategorie", "Datum", "Stichwörter", "Pfad"
				};
				List<IOpaqueDocument> dox = mgr.listDocuments(null, null, null, null, null, null);
				writer.writeNext(header);
				for (IOpaqueDocument doc : dox) {
					Patient pat = doc.getPatient();
					if (pat != null) {
						String subdirname = pat.get(Patient.FLD_PATID);
						if (subdirname != null) {
							File subdir = new File(dir, subdirname);
							subdir.mkdirs();
							String[] line = new String[header.length];
							line[0] = pat.getId();
							line[1] = doc.getTitle();
							line[2] = doc.getCategory();
							line[3] = doc.getCreationDate();
							line[4] = doc.getKeywords();
							String docfilename = doc.getGUID() + "." + doc.getMimeType();
							line[5] =
								dir.getName() + File.separator + subdir.getName() + File.separator
									+ docfilename;
							byte[] bin = doc.getContentsAsBytes();
							if (bin != null) {
								File f = new File(subdir, docfilename);
								FileOutputStream fos = new FileOutputStream(f);
								fos.write(bin);
								fos.close();
								writer.writeNext(line);
							}
						}
						
					}
				}
				return "Export ok";
			} else {
				return "Abgebrochen.";
				
			}
		} catch (Exception e) {
			ElexisStatus status =
				new ElexisStatus(IStatus.ERROR, Hub.PLUGIN_ID, IStatus.ERROR,
					"Fehler beim Export: " + e.getMessage(), e);
			throw new ScriptingException(status);
		}
		
	}
}
