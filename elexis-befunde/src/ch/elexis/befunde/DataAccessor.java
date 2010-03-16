/*******************************************************************************
 * Copyright (c) 2007-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    A. Kaufmann - Allow extraction of single fields and of first occurance
 *    
 * $Id: DataAccessor.java 6204 2010-03-16 08:02:51Z michael_imhof $
 *******************************************************************************/

package ch.elexis.befunde;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.IDataAccess;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Access data stored in Befunde Access syntax is: Befunde-Data:Patient:all:BD
 * 
 * @see ch.elexis.util.IDataAccess
 * @author gerry
 * 
 */
public class DataAccessor implements IDataAccess {
	Hashtable<String, String> hash;
	Hashtable<String, String[]> columns;
	ArrayList<String> parameters;

	@SuppressWarnings("unchecked")
	public DataAccessor() {
		Messwert setup = Messwert.getSetup();
		columns = new Hashtable<String, String[]>();
		parameters = new ArrayList<String>();
		hash = setup.getHashtable("Befunde");
		String names = hash.get("names");
		if (!StringTool.isNothing(names)) {
			for (String n : names.split(Messwert.SETUP_SEPARATOR)) {
				String vals = hash.get(n + "_FIELDS");
				if (vals != null) {
					vals = "Datum" + Messwert.SETUP_SEPARATOR + vals;
					String[] flds = vals.split(Messwert.SETUP_SEPARATOR);
					parameters.add(n);
					columns.put(n, flds);
				}

			}
		}
	}
	
	public String getDescription() {
		return "Daten im Befunde Plugin";
	}

	public String getName() {
		return "Befunde-Data";
	}

	/**
	 * Retourniert Platzhalter für die Integration im Textsystem.
	 * @return
	 */
	private String getPlatzhalter(final String befund) {
		return "[Befunde-Data:Patient:" + befund + "]";
	}


	public List<Element> getList() {
		ArrayList<Element> ret = new ArrayList<Element>(parameters.size());
		for (String n : parameters) {
			ret.add(new IDataAccess.Element(IDataAccess.TYPE.STRING, n, getPlatzhalter(n),
					Patient.class, 1));
		}
		return ret;
	}

	/**
	 * return the Object denoted by the given description
	 * 
	 * @param descriptor
	 *            descrion of the data: dataname.row if row is omitted: all rows
	 * @param dependentObject
	 *            ad this time, only Patient is supported
	 * @param dates
	 *            one off all,first, last,date
	 * @param params
	 *            not used
	 */
	@SuppressWarnings("unchecked")
	public Result<Object> getObject(final String descriptor,
			final PersistentObject dependentObject, final String dates,
			final String[] params) {
		Result<Object> ret = null;
		if (!(dependentObject instanceof Patient)) {
			ret = new Result<Object>(Result.SEVERITY.ERROR,
					IDataAccess.INVALID_PARAMETERS, "Ungültiger Parameter",
					dependentObject, true);
		} else {
			Patient pat = (Patient) dependentObject;
			String[] data = descriptor.split("\\.");
			Query<Messwert> qbe = new Query<Messwert>(Messwert.class);
			qbe.add("PatientID", "=", pat.getId()); //$NON-NLS-1$ //$NON-NLS-2$
			qbe.add("Name", "=", data[0]); //$NON-NLS-1$ //$NON-NLS-2$
			List<Messwert> list = qbe.execute();
			String[][] values;
			String[] cols = columns.get(data[0]);
			String[] keys = new String[cols.length];
			if (dates.equals("all")) {
				values = new String[list.size() + 1][cols.length];
			} else {
				values = new String[2][cols.length];
			}
			for (int i = 0; i < cols.length; i++) { // Spaltenüberschriften
				keys[i] = cols[i].split(Messwert.SETUP_CHECKSEPARATOR)[0];
				values[0][i] = keys[i].split("=")[0];
			}
			int i = 1;
			Messwert mwrt = null;
			if (dates.equals("all")) {
				for (Messwert m : list) {
					String date = m.get("Datum");
					values[i][0] = new TimeTool(date)
							.toString(TimeTool.DATE_GER);
					Hashtable befs = m.getHashtable("Befunde");
					for (int j = 1; j < cols.length; j++) {
						String vv = (String) befs.get(keys[j]);
						values[i][j] = vv;
						if (values[i][j] == null) {
							values[i][j] = "";
						}
					}
					i++;
					if (i > values.length) {
						break;
					}
				}
				ret = new Result<Object>(values);
			} else if (dates.equals("last")) {
				TimeTool today = new TimeTool(TimeTool.BEGINNING_OF_UNIX_EPOCH);
				for (Messwert m : list) {
					TimeTool vgl = new TimeTool(m.get("Datum"));
					if (vgl.isAfter(today)) {
						today = vgl;
						mwrt = m;
					}
				}
				if (mwrt == null) {
					ret = new Result<Object>(Result.SEVERITY.ERROR,
							IDataAccess.OBJECT_NOT_FOUND, "Nicht gefunden",
							params, true);
				}

			} else if (dates.equals("first")) {
				TimeTool firstdate = null;

				if (list.size() > 0) {
					mwrt = list.get(0);
					firstdate = new TimeTool(mwrt.get("Datum"));
					for (Messwert m : list) {
						TimeTool vgl = new TimeTool(m.get("Datum"));
						if (vgl.isBefore(firstdate)) {
							mwrt = m;
							firstdate = vgl;
							break;
						}
					}
				}

				if (mwrt == null) {
					ret = new Result<Object>(Result.SEVERITY.ERROR,
							IDataAccess.OBJECT_NOT_FOUND, "Nicht gefunden",
							params, true);
				}
			} else { // bestimmtes Datum
				TimeTool find = new TimeTool();
				if (find.set(params[0]) == false) {
					ret = new Result<Object>(Result.SEVERITY.ERROR,
							IDataAccess.INVALID_PARAMETERS, "Datum erwartet",
							params, true);
				} else {
					for (Messwert m : list) {
						TimeTool vgl = new TimeTool(m.get("Datum"));
						if (vgl.isEqual(find)) {
							mwrt = m;
							break;
						}
					}
				}
			}
			if (mwrt != null) {
				values[1][0] = mwrt.get("Datum");
				Hashtable befs = mwrt.getHashtable("Befunde");
				for (int j = 1; j < keys.length; j++) {
					values[1][j] = (String) befs.get(keys[j]);
				}
				// Nachsehen ob Feldnamen angegeben wurden, wenn ja geben wir
				// nur das gewuenschte Feld zurueck.
				if (data.length > 1) {
					String fname = data[1];
					String num = fname.substring(1);
					// Bei Feldnamen in der Form Fn benutzen wir n als Index
					// sonst wird einfach die Spaltenueberschrift benutzt.
					// F0 entspricht dabei dem Datum

					if (fname.matches("F[0-9]*")) {
						int index = Integer.parseInt(num);
						if (index < values[1].length) {
							ret = new Result<Object>(values[1][index]);
						} else {
							ret = new Result<Object>(Result.SEVERITY.ERROR,
									IDataAccess.INVALID_PARAMETERS,
									"Ungueltiger Feldindex", fname, true);
						}
					} else {
						for (int j = 0; (j < keys.length) && (ret == null); j++) {
							if (values[0][j].compareTo(fname) == 0) {
								ret = new Result<Object>(values[1][j]);
							}
						}
						if (ret == null) {
							ret = new Result<Object>(Result.SEVERITY.ERROR,
									IDataAccess.INVALID_PARAMETERS,
									"Ungueltiger Feldname", fname, true);
						}
					}
				} else {
					ret = new Result<Object>(values);
				}
			}
		}

		return ret;
	}

}
