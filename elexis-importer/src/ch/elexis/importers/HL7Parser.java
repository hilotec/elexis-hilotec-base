/**
 * (c) 2007-2008 by G. Weirich
 * All rights reserved
 * $Id: HL7.java 4431 2008-09-23 13:55:57Z rgw_ch $
 */

package ch.elexis.importers;

import java.io.File;
import java.util.List;

import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Kontakt;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.util.ResultAdapter;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class HL7Parser {
	private static final String COMMENT_NAME = "Kommentar";
	private static final String COMMENT_CODE = "kommentar";
	private static final String COMMENT_GROUP = "00 Kommentar";
	
	public String myLab="?";
	
	public HL7Parser(String mylab){
		myLab=mylab;
	}
	
	public Result<String> parse(final HL7 hl7,boolean createPatientIfNotFound){
		Result<Kontakt> res=hl7.getLabor();
		if(!res.isOK()){
			return new Result<String>(Result.SEVERITY.ERROR,1,"Lab not found",hl7.getFilename(),true);
		}
		final Kontakt labor=res.get();
		Result<Patient> r2=hl7.getPatient(createPatientIfNotFound);
		if(!r2.isOK()){
			return new Result<String>(Result.SEVERITY.ERROR,1,"Patient not found",hl7.getFilename(),true);
		}
		Patient pat=r2.get();
		
		HL7.OBR obr = hl7.firstOBR();
		
		int nummer = 0;
		String dat = new TimeTool().toString(TimeTool.DATE_GER);
		while (obr != null) {
			HL7.OBX obx = obr.firstOBX();
			while (obx != null) {
				String itemname = obx.getItemName();
				Query<LabItem> qbe = new Query<LabItem>(LabItem.class);
				qbe.add("LaborID", "=", labor.getId());
				// disabled, this would avoid renaming the title
				// qbe.add("titel", "=", itemname);
				qbe.add("kuerzel", "=", obx.getItemCode());
				List<LabItem> list = qbe.execute();
				LabItem li = null;
				if (list.size() < 1) {
					LabItem.typ typ = LabItem.typ.NUMERIC;
					if (obx.isFormattedText()) {
						typ = LabItem.typ.TEXT;
					}
					li =
						new LabItem(obx.getItemCode(), itemname, labor, obx.getRefRange(), obx
							.getRefRange(), obx.getUnits(), typ, "Z Automatisch_" + dat, Integer
							.toString(nummer++));
				} else {
					li = list.get(0);
				}
				LabResult lr;
				Query<LabResult> qr = new Query<LabResult>(LabResult.class);
				qr.add("PatientID", "=", pat.getId());
				qr.add("Datum", "=", obr.getDate().toString(TimeTool.DATE_GER));
				qr.add("ItemID", "=", li.getId());
				if (qr.execute().size() != 0) {
					if (SWTHelper.askYesNo("Dieser Laborwert wurde schon importiert",
						"Weitermachen?")) {
						obx = obr.nextOBX(obx);
						continue;
					} else {
						return new Result<String>("Cancelled");
					}
				}
				if (obx.isFormattedText()) {
					lr = new LabResult(pat, obr.getDate(), li, "text", obx.getResultValue());
				} else {
					lr =
						new LabResult(pat, obr.getDate(), li, obx.getResultValue(), obx
							.getComment());
				}
				
				if (obx.isPathologic()) {
					lr.setFlag(LabResult.PATHOLOGIC, true);
				}
				obx = obr.nextOBX(obx);
			}
			obr = obr.nextOBR(obr);
		}
		
		// add comments as a LabResult
		
		String comments = hl7.getComments();
		if (!StringTool.isNothing(comments)) {
			obr = hl7.firstOBR();
			if (obr != null) {
				TimeTool commentsDate = obr.getDate();
				
				// find LabItem
				Query<LabItem> qbe = new Query<LabItem>(LabItem.class);
				qbe.add("LaborID", "=", labor.getId());
				qbe.add("titel", "=", COMMENT_NAME);
				qbe.add("kuerzel", "=", COMMENT_CODE);
				List<LabItem> list = qbe.execute();
				LabItem li = null;
				if (list.size() < 1) {
					// LabItem doesn't yet exist
					LabItem.typ typ = LabItem.typ.TEXT;
					li =
						new LabItem(COMMENT_CODE, COMMENT_NAME, labor, "", "", "", typ,
							COMMENT_GROUP, Integer.toString(nummer++));
				} else {
					li = list.get(0);
				}
				
				// add LabResult
				Query<LabResult> qr = new Query<LabResult>(LabResult.class);
				qr.add("PatientID", "=", pat.getId());
				qr.add("Datum", "=", commentsDate.toString(TimeTool.DATE_GER));
				qr.add("ItemID", "=", li.getId());
				if (qr.execute().size() == 0) {
					// only add coments not yet existing
					
					new LabResult(pat, commentsDate, li, "Text", comments);
				}
			}
		}
		
		return new Result<String>("OK");
	}
	
	/**
	 * Import the given HL7 file. Optionally, move the file into the given archive directory
	 * 
	 * @param file
	 *            the file to be imported (full path)
	 * @param archiveDir
	 *            a directory where the file should be moved to on success, or null if it should not
	 *            be moved.
	 * @return the result as type Result
	 */
	public Result<?> importFile(final File file, final File archiveDir, boolean bCreatePatientIfNotExists){
		HL7 hl7 = new HL7("Labor " + myLab, myLab);
		Result<String> r = hl7.load(file.getAbsolutePath());
		if (r.isOK()) {
			Result<Patient> res = hl7.getPatient(false);
			if (res.isOK()) {
				Result<Kontakt> rk = hl7.getLabor();
				if (rk.isOK()) {
					Patient pat = res.get();
					Kontakt labor = rk.get();
					
					Result<?> ret = parse(hl7, bCreatePatientIfNotExists);
					
					// move result to archive
					if (ret.isOK()) {
						if (archiveDir != null) {
							if (archiveDir.exists() && archiveDir.isDirectory()) {
								if (file.exists() && file.isFile() && file.canRead()) {
									File newFile = new File(archiveDir, file.getName());
									if (!file.renameTo(newFile)) {
										SWTHelper.showError("Fehler beim Archivieren", "Die Datei "
											+ file.getAbsolutePath()
											+ " konnte nicht ins Archiv verschoben werden.");
									}
								}
							}
						}
					}
					
					GlobalEvents.getInstance().fireUpdateEvent(LabItem.class);
					return ret;
				} else {
					return rk;
				}
			} else {
				ResultAdapter.displayResult(res, "Fehler beim Import");
				return res;
			}
		}
		return r;
		
	}

	/**
	 * Equivalent to importFile(new File(file), null)
	 * 
	 * @param filepath
	 *            the file to be imported (full path)
	 * @return
	 */
	public Result<?> importFile(final String filepath, boolean bCreatePatientIfNotExists){
		return importFile(new File(filepath), null, bCreatePatientIfNotExists);
	}
}
