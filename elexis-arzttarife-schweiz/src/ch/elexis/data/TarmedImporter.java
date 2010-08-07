/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    D. Lutz	 - Import from different DBMS
 * 
 * $Id: TarmedImporter.java 6257 2010-04-07 18:13:21Z rgw_ch $
 *******************************************************************************/

// 8.12.07 G.Weirich avoid duplicate imports
package ch.elexis.data;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.ElexisException;
import ch.elexis.Hub;
import ch.elexis.arzttarife_schweiz.Messages;
import ch.elexis.importers.AccessWrapper;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.ImporterPage;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.TimeSpan;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.JdbcLink.Stm;

/**
 * Import des Tarmed-Tarifsystems aus der Datenbank der ZMT. Diese Datenbank ist im Microsoft
 * Access-Format und muss zunächst als user- oder systemdatenquelle angemeldet werden, bevor sie
 * hier importiert werden kann. (Download der Dtaenbank z.B.: <a
 * href="http://www.zmt.ch/de/tarmed/tarmed_tarifstruktur/tarmed_database.htm">hier</a> oder <a
 * href="http://www.tarmedsuisse.ch/site_tarmed/pages/edito/public/e_02_03.htm">hier</a>.)
 * 
 * @author gerry
 * 
 */
public class TarmedImporter extends ImporterPage {

	private static final String SRC_ENCODING = "iso-8859-1"; //$NON-NLS-1$

	AccessWrapper aw;
	JdbcLink j, pj;
	Stm source, dest;
	// Text tDb;
	private String lang;

	public TarmedImporter(){}

	/**
	 * Verbindungsversuch
	 * 
	 * @return true bei Erfolg
	 */
	public boolean connect(){
		String type = results[0];
		if (type != null) {
			String server = results[1];
			String db = results[2];
			String user = results[3];
			String password = results[4];

			if (type.equals("MySQL")) { //$NON-NLS-1$
				j = JdbcLink.createMySqlLink(server, db);
				return j.connect(user, password);
			} else if (type.equals("PostgreSQL")) { //$NON-NLS-1$
				j = JdbcLink.createPostgreSQLLink(server, db);
				return j.connect(user, password);
			} else if (type.equals("ODBC")) { //$NON-NLS-1$
				j = JdbcLink.createODBCLink(db);
				return j.connect(user, password);
			}
		}

		return false;
	}

	@Override
	public String getTitle(){
		return "TarMed code"; //$NON-NLS-1$
	}

	@Override
	public IStatus doImport(final IProgressMonitor monitor) throws Exception{
		/*
		if (connect() == false) {
			return new Status(Status.ERROR,
					"tarmed", 1, Messages.TarmedImporter_couldntConnect, null); //$NON-NLS-1$
		}
		 */
		File h2=File.createTempFile("elexis", "h2");
		j=JdbcLink.createH2Link(h2.getAbsolutePath());
		if(!j.connect("sa", "")){
			throw new ElexisException(getClass(), "Can't connect to H2", ElexisException.EE_UNEXPECTED_RESPONSE);
		}
		aw=new AccessWrapper(new File(results[0]));
		aw.convertTable("LEISTUNG", j);
		aw.convertTable("LEISTUNG_TEXT", j);
		aw.convertTable("KAPITEL_TEXT", j);
		aw.convertTable("LEISTUNG_DIGNIQUALI", j);
		aw.convertTable("LEISTUNG_HIERARCHIE", j);
		aw.convertTable("LEISTUNG_KOMBINATION", j);
		aw.convertTable("LEISTUNG_KUMULATION", j);
		aw.convertTable("LEISTUNG_MENGEN_ZEIT", j);
	
		
		
		
		pj = PersistentObject.getConnection();
		lang = JdbcLink.wrap(Hub.localCfg.get(PreferenceConstants.ABL_LANGUAGE, "d").toUpperCase()); //$NON-NLS-1$

		// pj.exec("DROP TABLE TARMED");
		int count = j.queryInt("SELECT COUNT(*) FROM LEISTUNG"); //$NON-NLS-1$
		count += j.queryInt("SELECT COUNT(*) FROM KAPITEL_TEXT") + 13; //$NON-NLS-1$
		monitor.beginTask(Messages.TarmedImporter_importLstg, count);
		monitor.subTask(Messages.TarmedImporter_connecting);

		try {
			source = j.getStatement();
			dest = pj.getStatement();
			monitor.subTask(Messages.TarmedImporter_deleteOldData);
			pj.exec("DELETE FROM TARMED"); //$NON-NLS-1$
			pj.exec("DELETE FROM TARMED_DEFINITIONEN"); //$NON-NLS-1$
			pj.exec("DELETE FROM TARMED_EXTENSION"); //$NON-NLS-1$
			monitor.subTask(Messages.TarmedImporter_definitions);
			importDefinition("ANAESTHESIE", "DIGNI_QUALI", "DIGNI_QUANTI", "LEISTUNG_BLOECKE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					"LEISTUNG_GRUPPEN", "LEISTUNG_TYP", "PFLICHT", "REGEL_EL_ABR", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					"SEITE", "SEX", "SPARTE", "ZR_EINHEIT"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			monitor.worked(13);
			monitor.subTask(Messages.TarmedImporter_chapter);
			ResultSet res = source.query("SELECT * FROM KAPITEL_TEXT WHERE SPRACHE=" + lang); //$NON-NLS-1$
			while (res.next()) {
				String code = res.getString("KNR"); //$NON-NLS-1$

				if (code.trim().equals("I")) { //$NON-NLS-1$
					continue;
				}
				TarmedLeistung tl = TarmedLeistung.load(code);
				String txt = convert(res, "BEZ_255"); //$NON-NLS-1$
				int subcap = code.lastIndexOf('.');
				String parent = "NIL"; //$NON-NLS-1$
				if (subcap != -1) {
					parent = code.substring(0, subcap);
				}
				if ((!tl.exists()) || (!parent.equals(tl.get("Parent")))) { //$NON-NLS-1$
					tl = new TarmedLeistung(code, parent, "", "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				tl.setText(txt);
				monitor.worked(1);
			}
			res.close();
			monitor.subTask(Messages.TarmedImporter_singleLst);
			res = source.query("SELECT * FROM LEISTUNG"); //$NON-NLS-1$
			PreparedStatement preps_extension =
				pj
				.prepareStatement("UPDATE TARMED_EXTENSION SET MED_INTERPRET=?,TECH_INTERPRET=? WHERE CODE=?"); //$NON-NLS-1$
			count = 0;
			TimeTool ttToday=new TimeTool();
			while (res.next() == true) {
				String cc = res.getString("LNR"); //$NON-NLS-1$
				//System.out.println(cc);
				TarmedLeistung tl = TarmedLeistung.load(cc);
				if (tl.exists()) {
					tl.set("DigniQuanti", convert(res, "QT_DIGNITAET")); //$NON-NLS-1$ //$NON-NLS-2$
					tl.set("Sparte", convert(res, "Sparte")); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					tl = new TarmedLeistung(cc, res.getString("KNR"), //$NON-NLS-1$
							"0000", convert(res, "QT_DIGNITAET"), convert(res, "Sparte")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				//TimeSpan tsValid1=new TimeSpan(new TimeTool(tl.get("GueltigVon")), new TimeTool(tl.get("GueltigBis")));
				TimeSpan tsValid=new TimeSpan(new TimeTool(res.getString("GUELTIG_VON")), new TimeTool(res.getString("GUELTIG_BIS")));
				if(tsValid.contains(ttToday)){
					tl.set(new String[] {
							"GueltigVon", "GueltigBis" //$NON-NLS-1$ //$NON-NLS-2$
					}, tsValid.from.toString(TimeTool.DATE_COMPACT), tsValid.until.toString(TimeTool.DATE_COMPACT)); //$NON-NLS-1$ //$NON-NLS-2$
					Stm sub = j.getStatement();
					String dqua =
						sub
						.queryString("SELECT QL_DIGNITAET FROM LEISTUNG_DIGNIQUALI WHERE LNR=" + tl.getWrappedId()); //$NON-NLS-1$
					String kurz = ""; //$NON-NLS-1$
					ResultSet rsub =
						sub
						.query("SELECT * FROM LEISTUNG_TEXT WHERE SPRACHE=" + lang + " AND LNR=" + tl.getWrappedId()); //$NON-NLS-1$ //$NON-NLS-2$
					if (rsub.next() == true) {
						kurz = convert(rsub, "BEZ_255"); //$NON-NLS-1$
						String med = convert(rsub, "MED_INTERPRET"); //$NON-NLS-1$
						String tech = convert(rsub, "TECH_INTERPRET"); //$NON-NLS-1$
						preps_extension.setString(1, med);
						preps_extension.setString(2, tech);
						preps_extension.setString(3, tl.getId());
						preps_extension.execute();
					}
					rsub.close();
					tl.set(new String[] {
							"DigniQuali", "Text"}, dqua, kurz); //$NON-NLS-1$ //$NON-NLS-2$
					Hashtable<String, String> ext = tl.loadExtension();
					put(ext, res, "LEISTUNG_TYP", "SEITE", "SEX", "ANAESTHESIE", "K_PFL", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
							"BEHANDLUNGSART", "TP_AL", "TP_ASSI", "TP_TL", "ANZ_ASSI", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
							"LSTGIMES_MIN", "VBNB_MIN", "BEFUND_MIN", "RAUM_MIN", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							"WECHSEL_MIN", "F_AL", "F_TL"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

					rsub =
						sub
						.query("SELECT LNR_MASTER FROM LEISTUNG_HIERARCHIE WHERE LNR_SLAVE=" + tl.getWrappedId()); //$NON-NLS-1$
					if (rsub.next()) {
						ext.put("Bezug", rsub.getString(1)); //$NON-NLS-1$
					}
					rsub.close();
					rsub =
						sub
						.query("SELECT LNR_SLAVE,TYP FROM LEISTUNG_KOMBINATION WHERE LNR_MASTER=" + tl.getWrappedId()); //$NON-NLS-1$
					String kombination_and = ""; //$NON-NLS-1$
					String kombination_or = ""; //$NON-NLS-1$
					while (rsub.next()) {
						String typ = rsub.getString(2);
						String slave = rsub.getString(1);
						if (typ != null) {
							if (typ.equals("and")) { //$NON-NLS-1$
								kombination_and += slave + ","; //$NON-NLS-1$
							} else if (typ.equals("or")) { //$NON-NLS-1$
								kombination_or += slave + ","; //$NON-NLS-1$
							}
						}
					}
					rsub.close();
					if (!kombination_and.equals("")) { //$NON-NLS-1$
						String k = kombination_and.replaceFirst(",$", ""); //$NON-NLS-1$ //$NON-NLS-2$
						ext.put("kombination_and", k); //$NON-NLS-1$
					}
					if (!kombination_or.equals("")) { //$NON-NLS-1$
						String k = kombination_or.replaceFirst(",$", ""); //$NON-NLS-1$ //$NON-NLS-2$
						ext.put("kombination_or", k); //$NON-NLS-1$
					}
					rsub =
						sub
						.query("SELECT * FROM LEISTUNG_KUMULATION WHERE LNR_MASTER=" + tl.getWrappedId()); //$NON-NLS-1$
					String exclusion = ""; //$NON-NLS-1$
					String inclusion = ""; //$NON-NLS-1$
					String exclusive = ""; //$NON-NLS-1$
					while (rsub.next()) {
						String typ = rsub.getString("typ"); //$NON-NLS-1$
						String slave = rsub.getString("LNR_SLAVE"); //$NON-NLS-1$
						if (typ != null) {
							if (typ.equals("E")) { //$NON-NLS-1$
								exclusion += slave + ","; //$NON-NLS-1$
							} else if (typ.equals("I")) { //$NON-NLS-1$
								inclusion += slave + ","; //$NON-NLS-1$
							} else if (typ.equals("X")) { //$NON-NLS-1$
								exclusive += slave + ","; //$NON-NLS-1$
							}
						}
					}
					rsub.close();
					if (!exclusion.equals("")) { //$NON-NLS-1$
						String k = exclusion.replaceFirst(",$", ""); //$NON-NLS-1$ //$NON-NLS-2$
						ext.put("exclusion", k); //$NON-NLS-1$
					}
					if (!inclusion.equals("")) { //$NON-NLS-1$
						String k = inclusion.replaceFirst(",$", ""); //$NON-NLS-1$ //$NON-NLS-2$
						ext.put("inclusion", k); //$NON-NLS-1$
					}
					if (!exclusive.equals("")) { //$NON-NLS-1$
						String k = exclusive.replaceFirst(",$", ""); //$NON-NLS-1$ //$NON-NLS-2$
						ext.put("exclusive", k); //$NON-NLS-1$
					}
					rsub =
						sub.query("SELECT * FROM LEISTUNG_MENGEN_ZEIT WHERE LNR=" + tl.getWrappedId()); //$NON-NLS-1$
					String limits = ""; //$NON-NLS-1$
					while (rsub.next()) {
						StringBuilder sb = new StringBuilder();
						sb.append(rsub.getString("Operator")).append(","); //$NON-NLS-1$ //$NON-NLS-2$
						sb.append(rsub.getString("Menge")).append(","); //$NON-NLS-1$ //$NON-NLS-2$
						sb.append(rsub.getString("ZR_ANZAHL")).append(","); //$NON-NLS-1$ //$NON-NLS-2$
						sb.append(rsub.getString("PRO_NACH")).append(","); //$NON-NLS-1$ //$NON-NLS-2$
						sb.append(rsub.getString("ZR_EINHEIT")).append("#"); //$NON-NLS-1$ //$NON-NLS-2$
						limits += sb.toString();
					}
					rsub.close();
					if (!limits.equals("")) { //$NON-NLS-1$
						ext.put("limits", limits); //$NON-NLS-1$
					}
					tl.flushExtension();
					j.releaseStatement(sub);

				}
				monitor.worked(1);
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
			}
			res.close();
			monitor.done();
			return Status.OK_STATUS;

		} catch (Exception ex) {
			ExHandler.handle(ex);
		} finally {
			if (source != null) {
				j.releaseStatement(source);
			}
			if (dest != null) {
				pj.releaseStatement(dest);
			}
		}
		return Status.CANCEL_STATUS;
	}

	private void put(final Hashtable<String, String> h, final ResultSet r, final String... vv)
	throws Exception{
		for (String v : vv) {
			String val = r.getString(v);
			if (val != null) {
				h.put(v, val);
			}
		}
	}

	private void importDefinition(final String... strings) throws IOException, SQLException{
		for(String s:strings){
			aw.convertTable("CT_"+s, j);
		}
		Stm stm = j.getStatement();
		PreparedStatement ps =
			pj
			.prepareStatement("INSERT INTO TARMED_DEFINITIONEN (Spalte,Kuerzel,Titel) VALUES (?,?,?)"); //$NON-NLS-1$
		try {
			for (String s : strings) {
				ResultSet res = stm.query("SELECT * FROM CT_" + s + " WHERE SPRACHE=" + lang); //$NON-NLS-1$ //$NON-NLS-2$
				while (res.next()) {
					ps.setString(1, s);
					ps.setString(2, res.getString(1));
					ps.setString(3, res.getString(3));
					ps.execute();
				}
				res.close();
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
		} finally {
			j.releaseStatement(stm);
		}
	}

	@Override
	public String getDescription(){
		return Messages.TarmedImporter_enterSource + Messages.TarmedImporter_setupSource
		+ Messages.TarmedImporter_setupSource2;
	}

	@Override
	public Composite createPage(final Composite parent){
		/*
		DBBasedImporter obi = new ImporterPage.DBBasedImporter(parent, this);
		obi.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		return obi;
		*/
		FileBasedImporter fbi=new FileBasedImporter(parent, this);
		fbi.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		return fbi;
	}

	private String convert(ResultSet res, String field) throws Exception{
		String orig=res.getString(field);
		byte[] raw = orig.getBytes(SRC_ENCODING);
		if (raw == null) {
			return ""; //$NON-NLS-1$
		}
		return new String(raw);
	}
}
