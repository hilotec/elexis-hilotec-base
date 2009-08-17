/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and medelexis AG
 * All rights reserved.
 * $Id: Importer.java 132 2009-06-14 17:34:31Z  $
 *******************************************************************************/

package ch.elexis.labortarif2009.data;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.Hub;
import ch.elexis.data.Query;
import ch.elexis.importers.ExcelWrapper;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.ImporterPage;
import ch.elexis.util.PlatformHelper;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;

public class Importer extends ImporterPage {
	int langdef = 0;

	public Importer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Composite createPage(Composite parent) {
		FileBasedImporter fis = new ImporterPage.FileBasedImporter(parent, this);
		fis.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		return fis;
	}

	@Override
	public IStatus doImport(IProgressMonitor monitor) throws Exception {
		String lang = JdbcLink.wrap(Hub.localCfg.get( // d,f,i
				PreferenceConstants.ABL_LANGUAGE, "d").toUpperCase()); //$NON-NLS-1$
		if (lang.startsWith("F")) {
			langdef = 1;
		} else if (lang.startsWith("I")) {
			langdef = 2;
		}
		Fachspec[] specs = loadFachspecs(langdef);
		if (specs != null) {
			ExcelWrapper exw = new ExcelWrapper();
			exw.setFieldTypes(new Class[] { String.class, String.class,
					String.class, String.class, String.class, String.class,
					String.class, String.class });
			if (exw.load(results[0], langdef)) {
				int first = exw.getFirstRow();
				int last = exw.getLastRow();
				int count = last - first;
				monitor.beginTask("Import EAL 2009", count);
				for (int i = first+1 ; i <= last; i++) {
					String[] line = exw.getRow(i).toArray(new String[0]);
					String chapter = StringTool.getSafe(line, 0);
					String code = StringTool.getSafe(line, 2);
					String tp = StringTool.getSafe(line, 4);
					String name = StringTool.limitLength(StringTool.getSafe(
							line, 5), 254);
					String lim = StringTool.getSafe(line, 6);
					String fach = StringTool.getSafe(line, 7);

					String id = new Query<Labor2009Tarif>(Labor2009Tarif.class)
							.findSingle(Labor2009Tarif.FLD_CODE, Query.EQUALS,
									code);
					if (id != null) {
						Labor2009Tarif lt = Labor2009Tarif.load(id);
						lt.set(new String[] { Labor2009Tarif.FLD_CHAPTER,
								Labor2009Tarif.FLD_CODE, Labor2009Tarif.FLD_TP,
								Labor2009Tarif.FLD_NAME,
								Labor2009Tarif.FLD_LIMITATIO,
								Labor2009Tarif.FLD_FACHBEREICH,
								Labor2009Tarif.FLD_FACHSPEC}, chapter,
								code, tp, name, lim, fach, Integer.toString(Fachspec.getFachspec(specs, i)));

					} else {
						new Labor2009Tarif(chapter, code, tp, name, lim, fach, Fachspec.getFachspec(specs, i));
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		}
		return new Status(Status.ERROR, "ch.elexis.labotarif.ch2009",
				"could not load file");
	}

	@Override
	public String getDescription() {
		return "Wählen Sie eine Excel-Datei mit dem Analysetarif 2009 aus";
	}

	@Override
	public String getTitle() {
		return "EAL 2009";
	}

	public static Fachspec[] loadFachspecs(int langdef) {
		String specs = PlatformHelper.getBasePath(Constants.pluginID)
				+ File.separator + "rsc" + File.separator + "arztpraxen.xls";
		ExcelWrapper x = new ExcelWrapper();
		x.setFieldTypes(new Class[]{Integer.class,String.class,Integer.class,Integer.class});
		if (x.load(specs, langdef)) {
			int first = x.getFirstRow();
			int last = x.getLastRow();
			Fachspec[] fspecs = new Fachspec[last - first + 1];
			for (int i = first ; i <= last; i++) {
				fspecs[i] = new Fachspec(x.getRow(i).toArray(
						new String[0]));
			}
			return fspecs;
		}
		return null;
	}

	public static class Fachspec {
		public int code, from, until;
		public String name;

		Fachspec(String[] line) {
			this(Integer.parseInt(StringTool.getSafe(line, 0)), StringTool
					.getSafe(line, 1), Integer.parseInt(StringTool.getSafe(
					line, 2)), Integer.parseInt(StringTool.getSafe(line, 3)));
		}

		Fachspec(int code, String name, int from, int until) {
			this.code = code;
			this.from = from;
			this.until = until;
			this.name = name;
		}
		
		/**
		 * Find the spec a given row belongs to
		 * @param specs a list of all specs
		 * @param row the row to match
		 * @return the spec number or -1 if no spec
		 */
		public static int getFachspec(Fachspec[] specs, int row){
			for(Fachspec spec:specs){
				if(spec.from<=row && spec.until>=row){
					return spec.code;
				}
			}
			return -1;
		}
	}
}
