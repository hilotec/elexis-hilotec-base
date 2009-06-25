/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: Termin.java 5415 2009-06-25 13:04:43Z rgw_ch $
 *******************************************************************************/

package ch.elexis.agenda.data;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.agenda.Messages;
import ch.elexis.agenda.acl.ACLContributor;
import ch.elexis.agenda.preferences.PreferenceConstants;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeSpan;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionInfo;

/**
 * Termin-Klasse für Agenda
 */

public class Termin extends PersistentObject implements Cloneable,
		Comparable<Termin>, IPlannable {
	public static final String FLD_LASTEDIT = "lastedit";
	public static final String FLD_GRUND = "Grund";
	public static final String FLD_DAUER = "Dauer";
	public static final String FLD_BEGINN = "Beginn";
	public static final String FLD_TAG = "Tag";
	public static final String VERSION = "1.2.4";
	public static String[] TerminTypes;
	public static String[] TerminStatus;
	public static String[] TerminBereiche;
	private static final JdbcLink j = getConnection();

	public static final String FLD_PATIENT="Wer";
	
	
	// static final String DEFTYPES="Frei,Reserviert,Normal,Extra,Besuch";
	// static final String
	// DEFSTATUS="-   ,geplant,eingetroffen,fertig,verpasst,abgesagt";
	public static final String createDB = "CREATE TABLE AGNTERMINE("
			+ "ID              VARCHAR(127) primary key,"
			+ "lastupdate BIGINT,"
			+ // we need that size to be able to import ics files
			"PatID			VARCHAR(80)," + "Bereich		VARCHAR(25),"
			+ "Tag             CHAR(8)," + "Beginn          CHAR(4),"
			+ "Dauer           CHAR(4)," + "Grund           TEXT,"
			+ "TerminTyp       VARCHAR(50)," + "TerminStatus    VARCHAR(50),"
			+ "ErstelltVon     VARCHAR(25)," + "Angelegt        VARCHAR(10),"
			+ "lastedit	     VARCHAR(10),"
			+ "PalmID          INTEGER default 0,"
			+ "flags           VARCHAR(10),"
			+ "deleted         CHAR(2) default '0'," + "Extension       TEXT,"
			+ "linkgroup	    VARCHAR(20)" + ");"
			+ "CREATE INDEX it on AGNTERMINE (Tag,Beginn,Bereich);"
			+ "CREATE INDEX pattern on AGNTERMINE (PatID);"
			+ "CREATE INDEX agnbereich on AGNTERMINE (Bereich);"
			+ "INSERT INTO AGNTERMINE (ID) VALUES ('1');";

	private static final String upd122 = "ALTER TABLE AGNTERMINE MODIFY TerminTyp VARCHAR(50);"
			+ "ALTER TABLE AGNTERMINE MODIFY TerminStatus VARCHAR(50);";

	private static final String upd124 = "ALTER TABLE AGNTERMINE ADD lastupdate BIGINT;";
	static {
		addMapping("AGNTERMINE", "BeiWem=Bereich", "Wer=PatID", FLD_TAG,
				FLD_BEGINN, FLD_DAUER, FLD_GRUND, "Typ=TerminTyp",
				"Status=TerminStatus", "ErstelltVon", "ErstelltWann=Angelegt",
				FLD_LASTEDIT, "PalmID", "flags", "deleted", "Extension",
				"linkgroup");
		TimeTool.setDefaultResolution(60000);
		TerminTypes = Hub.globalCfg
				.getStringArray(PreferenceConstants.AG_TERMINTYPEN);
		TerminStatus = Hub.globalCfg
				.getStringArray(PreferenceConstants.AG_TERMINSTATUS);
		TerminBereiche = Hub.globalCfg.get(PreferenceConstants.AG_BEREICHE,
				Messages.TagesView_14).split(",");
		if ((TerminTypes == null) || (TerminTypes.length < 3)) {
			TerminTypes = new String[] { "frei", "gesperrt", "normal" };
		}
		if ((TerminStatus == null) || (TerminStatus.length < 2)) {
			TerminStatus = new String[] { "-", "geplant" };
		}
		Termin Version = load("1");
		if (Version == null) {
			init();
		} else {
			VersionInfo vi = new VersionInfo(Version.get("Wer"));
			if (vi.isOlder(VERSION)) {
				if (vi.isOlder("1.1.0")) {
					if (j.DBFlavor.equalsIgnoreCase("postgresql")) {
						j
								.exec("ALTER TABLE AGNTERMINE ALTER angelegt TYPE VARCHAR(10);");
						j
								.exec("ALTER TABLE AGNTERMINE ALTER lastedit TYPE VARCHAR(10);");
						j
								.exec("ALTER TABLE AGNTERMINE ALTER flags TYPE VARCHAR(10);");
					} else if (j.DBFlavor.equalsIgnoreCase("mysql")) {
						j
								.exec("ALTER TABLE AGNTERMINE MODIFY angelegt VARCHAR(10);");
						j
								.exec("ALTER TABLE AGNTERMINE MODIFY lastedit VARCHAR(10);");
						j
								.exec("ALTER TABLE AGNTERMINE MODIFY flags VARCHAR(10);");
					}
				} else if (vi.isOlder("1.2.1")) {
					if (j.DBFlavor.equalsIgnoreCase("postgresql")) {
						j
								.exec("ALTER TABLE AGNTERMINE ALTER ID TYPE VARCHAR(127);");
					} else if (j.DBFlavor.equalsIgnoreCase("mysql")) {
						j
								.exec("ALTER TABLE AGNTERMINE MODIFY ID VARCHAR(127);");
					}
				} else if (vi.isOlder("1.2.3")) {
					createOrModifyTable(upd122);
				}
				if (vi.isOlder("1.2.4")) {
					createOrModifyTable(upd124);
				}
				Version.set("Wer", VERSION);
			}
		}
	}

	// Terminstatus fix
	static public final int LEER = 0;

	// Termintypen fix
	static public final int FREI = 0;
	static public final int RESERVIERT = 1;
	static public final int STANDARD = 2;

	// Status-Flags (dipSwitch)
	static final byte SW_NEW = 0; // 0x01
	static final byte SW_MODIFIED = 1; // 0x02
	static final byte SW_ARCHIVE = 2; // 0x04
	static public final byte SW_SELECTED = 3; // 0x08
	static public final byte SW_LOCKED = 4; // 0x10
	static public final byte SW_LINKED = 5; // 0x20

	// static String[] Users;

	/**
	 * Tabelle neu erstellen
	 */
	public static void init() {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(createDB
					.getBytes("UTF-8"));
			j.execScript(bais, true, false);
			Hub.userCfg.set(PreferenceConstants.AG_SHOWDELETED + "_default",
					"0");
			Hub.globalCfg.set(PreferenceConstants.AG_TERMINTYPEN + "_default",
					"Frei,Reserviert,Normal,Extra,Besuch");
			Hub.globalCfg.set(PreferenceConstants.AG_TERMINSTATUS + "_default",
					"-,geplant,eingetroffen,fertig,verpasst,abgesagt");
			Hub.userCfg.set(PreferenceConstants.AG_TYPIMAGE_PREFIX
					+ Termin.typFrei(), "icons/gruen.png");
			Hub.userCfg.set(PreferenceConstants.AG_TYPIMAGE_PREFIX
					+ Termin.typReserviert(), "icons/einbahn.png");
			Hub.userCfg.set(PreferenceConstants.AG_TYPIMAGE_PREFIX + "Normal",
					"icons/kons.ico");
			Hub.userCfg.set(PreferenceConstants.AG_TYPIMAGE_PREFIX + "Extra",
					"icons/blaulicht.ico");
			Hub.userCfg.set(PreferenceConstants.AG_TYPIMAGE_PREFIX + "Besuch",
					"icons/ambulanz.ico");
			ACLContributor.initialize();
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}

	}

	public static void addBereich(String bereich) {
		String nber = Hub.globalCfg.get(PreferenceConstants.AG_BEREICHE,
				Messages.TagesView_14);
		nber += "," + bereich;
		Hub.globalCfg.set(PreferenceConstants.AG_BEREICHE, nber);
		TerminBereiche = nber.split(",");
	}

	public static void addType(String typ) {
		String tt = StringTool.join(TerminTypes, ",") + "," + typ;
		Hub.globalCfg.set(PreferenceConstants.AG_TERMINTYPEN, tt);
		TerminTypes = tt.split(",");
	}

	public Termin() {/* leer */
	}

	public Termin(final String id) {
		super(id);
	}

	/**
	 * exists() liefert false wenn der Termin gelöscht ist...
	 * 
	 * @param id
	 * @return
	 */
	public static Termin load(final String id) {
		Termin ret = new Termin(id);
		if (ret.state() > PersistentObject.INVALID_ID) {
			return ret;
		}
		return null;

	}

	public Termin(final String bereich, final String Tag, final int von,
			final int bis, final String typ, final String status) {
		create(null);

		String ts = createTimeStamp();
		set(new String[] { "BeiWem", FLD_TAG, FLD_BEGINN, FLD_DAUER, "Typ", "Status",
				"ErstelltWann", FLD_LASTEDIT }, bereich, Tag, Integer
				.toString(von), Integer.toString(bis - von), typ, status, ts,
				ts);
	}

	/**
	 * Einen Termin mit vorgegebener ID erstellen. Wird nur vom Importer
	 * gebraucth
	 */
	public Termin(final String ID, final String bereich, final String Tag,
			final int von, final int bis, final String typ, final String status) {
		create(ID);
		String ts = createTimeStamp();
		set(new String[] { "BeiWem", FLD_TAG, FLD_BEGINN, FLD_DAUER, "Typ", "Status",
				"ErstelltWann", FLD_LASTEDIT }, bereich, Tag, Integer
				.toString(von), Integer.toString(bis - von), typ, status, ts,
				ts);
	}

	/*
	 * public Termin(Mandant BeiWem, String Tag,int von, int bis, String typ,
	 * String status){ create(null); String ts=getTimeStamp(); set(new
	 * String[]{"BeiWem"
	 * ,"Tag","Beginn","Dauer","Typ","Status","ErstelltWann","lastedit"},
	 * BeiWem.getId(),Tag,Integer.toString(von),Integer.toString(bis-von),
	 * typ,status,ts,ts); }
	 */
	@Override
	public Object clone() {
		Termin ret = new Termin(get("BeiWem"), get(FLD_TAG), getStartMinute(),
				getStartMinute() + getDauer(), getType(), getStatus());
		ret.setPatient(getPatient());
		return ret;
	}

	/** Den Standard-Termintyp holen */
	public static String typStandard() {
		return TerminTypes[STANDARD];
	}

	/** Den Termintyp mit der Bedeutung "frei" holen */
	public static String typFrei() {
		return TerminTypes[FREI];
	}

	/** Den Termintyp mit der Bedeutung "reserviert" holen */
	public static String typReserviert() {
		return TerminTypes[RESERVIERT];
	}

	/** Den Terminstatus mit der Bedeutung "undefiniert" holen */
	public static String statusLeer() {
		return TerminStatus[LEER];
	}

	/** Den Standard-Terminstatus für neue Termine holen */
	public static String statusStandard() {
		return TerminStatus[1];
	}

	/*
	 * public boolean isNew(){ return true; }
	 */
	public static List<Termin> getLinked(final Termin orig) {
		if (orig.getFlag(SW_LINKED) == false) {
			return null;
		}
		if (StringTool.isNothing(orig.get("linkgroup"))) {
			return null;
		}

		Query<Termin> qbe = new Query<Termin>(Termin.class);
		qbe.add("linkgroup", "=", orig.get("linkgroup"));
		List<Termin> ret = qbe.execute();
		if (ret.size() == 0) {
			ret.add(orig);
		} else if (ret.size() == 1) {
			orig.clrFlag(SW_LINKED);
			orig.set("linkgroup", null);
			// TODO orig.flush();
		}
		return ret;
	}

	/**
	 * Aktuelle Zeit in Minuten als int in einem String verpackt.
	 * 
	 * @return Timestamp
	 */
	public static String createTimeStamp() {
		return Integer.toString(TimeTool.getTimeInSeconds() / 60);
	}

	public TimeTool getModifyTime() {
		int min = checkZero(get(FLD_LASTEDIT));
		TimeTool ret = new TimeTool(min, 60000);
		return ret;
	}

	public TimeTool getCreateTime() {
		int min = checkZero(get("ErstelltWann"));
		return new TimeTool(min, 60000);
	}

	public void setFlag(final byte flag) {
		int flags = checkZero(get("flags"));
		flags |= 1 << flag;
		set(new String[] { "flags", FLD_LASTEDIT }, new String[] {
				Integer.toString(flags), createTimeStamp() });
	}

	public void clrFlag(final byte flag) {
		int flags = checkZero(get("flags"));
		flags &= ~(1 << flag);
		set(new String[] { "flags", FLD_LASTEDIT }, new String[] {
				Integer.toString(flag), createTimeStamp() });
	}

	public boolean getFlag(final byte flag) {
		int flags = checkZero(get("flags"));
		return ((flags & (1 << flag)) != 0);
	}

	public boolean isLocked() {
		return getFlag(SW_LOCKED);
	}

	public void setLocked(final boolean mode) {
		if (mode) {
			setFlag(SW_LOCKED);
		} else {
			clrFlag(SW_LOCKED);
		}
	}

	public boolean checkLock() {
		if (isLocked()) {
			SWTHelper.alert("Termin gesperrt",
					"Dieser Termin kann nicht geändert oder gelöscht werden.");
			return true;
		}
		return false;
	}

	@Override
	public boolean delete() {
		if (checkLock()) {
			return false;
		}
		List<Termin> linked = null;
		String linkgroup = get("linkgroup");
		if (getFlag(SW_LINKED) && (!StringTool.isNothing(linkgroup))) {
			MessageDialog msd = new MessageDialog(
					Desk.getTopShell(),
					"Terminserie löschen",
					null,
					"Dieser Termin gehört zu mehreren verknüpften Terminen. Soll die ganze Serie gelöscht werden?\n",
					MessageDialog.QUESTION, new String[] { "Ja", "Nein" }, 1);
			if (msd.open() == 0) {
				linked = getLinked(this);
				for (Termin ae : (List<Termin>) linked) {
					ae.set(new String[] { FLD_LASTEDIT, "deleted" },
							new String[] { createTimeStamp(), "1" });
				}
			}

		}

		String deleted = get("deleted");
		if (deleted.equals("1")) {
			deleted = "0";
		} else {
			deleted = "1";
		}
		set(new String[] { "deleted", FLD_LASTEDIT }, deleted, createTimeStamp());
		return true;
	}

	public void setType(final String Type) {
		if (!checkLock()) {
			if (StringTool.isNothing(Type)) {
				return;
			}
			if (Type.equals(typFrei())) {
				super.delete();
			} else {
				set(new String[] { "Typ", FLD_LASTEDIT }, Type, createTimeStamp());
			}
		}
	}

	public void setStatus(final String stat) {
		if (StringTool.isNothing(stat)) {
			return;
		}
		if (!checkLock()) {
			set(new String[] { "Status", FLD_LASTEDIT }, stat, createTimeStamp());
		}
	}

	public boolean isValid() {
		int l = checkZero(get(FLD_DAUER));
		if (l <= 0) {
			return false;
		}
		return true;
	}

	public void setGrund(final String grund) {
		if (!checkLock()) {
			set(new String[] { FLD_GRUND, FLD_LASTEDIT }, grund, createTimeStamp());
		}
	}

	public String getGrund() {
		return get(FLD_GRUND);
	}

	public void set(final String bereich, final String tag, final int von,
			final int bis, final String typ, final String status) {
		if (!checkLock()) {
			set(new String[] { "BeiWem", FLD_TAG, FLD_BEGINN, FLD_DAUER, "Typ",
					"Status", FLD_LASTEDIT }, bereich, tag,
					Integer.toString(von), Integer.toString(bis - von), typ,
					status, createTimeStamp());
		}
	}

	public void set(final String bereich, final TimeTool wann, final int dauer,
			final String typ, final String status, final Patient pat,
			final String Grund) {
		String Tag = wann.toString(TimeTool.DATE_COMPACT);
		int Beginn = wann.get(TimeTool.HOUR_OF_DAY) * 60
				+ wann.get(TimeTool.MINUTE);
		set(new String[] { "BeiWem", FLD_TAG, FLD_BEGINN, FLD_DAUER, "Typ", "Status",
				"Wer", FLD_GRUND, FLD_LASTEDIT }, bereich, Tag, Integer
				.toString(Beginn), Integer.toString(dauer), typ, status, pat
				.getId(), Grund, createTimeStamp());
	}

	/*
	 * public String getPersonalia(int width,FontRenderContext frc) { return "";
	 * }
	 */
	public String getPersonalia() {
		String patid = get("Wer");
		Patient pat = Patient.load(patid);
		String Personalien = "";
		if (pat.exists()) {
			Personalien = pat.getPersonalia();
		} else {
			Personalien = patid;
		}
		if (get("deleted").equals("1")) {
			return Personalien + " (gelöscht)";
		}
		return Personalien;
	}

	public String getStatus() {
		return get("Status");
	}

	public int getLastedit() {
		return getInt(FLD_LASTEDIT);
	}

	public void setPatient(final Patient pers) {
		if (!checkLock()) {
			set(new String[] { "Wer", FLD_LASTEDIT }, pers.getId(),
					createTimeStamp());
		}
	}

	public void setText(final String text) {
		if (!checkLock()) {
			set(new String[] { "Wer", FLD_LASTEDIT }, text, createTimeStamp());
		}
	}

	public Patient getPatient() {
		String pid = get("Wer");
		Patient pat = Patient.load(pid);
		if (pat.exists()) {
			return pat;
		}
		return null;
	}

	public String getText() {
		return get("Wer");
	}

	/*
	 * public Mandant getMandant(){ return Mandant.load(get("BeiWem")); }
	 */
	public String getBereich() {
		return get("BeiWem");
	}

	/**
	 * Algorithmus f�r Aufsplittung Name/Vorname/GebDat: was dem match
	 * [0-9][0-9]*\.[0-9][0-9]*\.[0-9]+ folgt, ist das Geburtsdatum Was davor
	 * steht, ist Name und Vorname, wobei das letzte Wort der Vorname ist und
	 * alles davor zum Namen gerechnet wird.
	 * 
	 * @return Ein StringArray mit 3 Elementen: Name, Vorname, GebDat. Jedes
	 *         Element kann "" sein, keines ist null.
	 */
	public static String[] findID(final String pers) {

		String[] ret = new String[3];
		ret[0] = "";
		ret[1] = "";
		ret[2] = "";
		if (StringTool.isNothing(pers)) {
			return ret;
		}
		String[] p1 = pers.split("[\\s,][\\s,]*[\\s,]*");
		if (p1.length == 1) {
			ret[0] = p1[0];
			return ret;
		}

		String nam, vn, gd;
		nam = "";
		vn = null;
		gd = null;
		for (int i = p1.length - 1; i >= 0; i--) {
			p1[i] = p1[i].trim();
			if (p1[i].matches("\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}")) {
				if (gd == null) {
					gd = p1[i];
				}
			} else {
				if (vn == null) {
					vn = p1[i];
				} else {
					nam = p1[i] + " " + nam;
				}
			}
		}
		if (nam != null) {
			ret[0] = nam;
		}
		if (vn != null) {
			ret[1] = vn;
		}
		if (gd != null) {
			TimeTool tt = new TimeTool(gd);
			ret[2] = tt.toString(TimeTool.DATE);
		}
		ret[0] = ret[0].trim();
		ret[1] = ret[1].trim();
		ret[2] = ret[2].trim();
		return ret;
	}

	public boolean isDeleted() {
		return get("deleted").equals("1");
	}

	/** standard equals: Gleiche Zeit, gleiche Dauer, gleicher Bereich */
	public boolean equals(final Object o) {
		if (o instanceof Termin) {
			return super.isMatching((Termin) o, 0, FLD_TAG, FLD_BEGINN, FLD_DAUER,
					"BeiWem");
		}
		return false;
	}

	/** Exakte Übereinstimmung */
	public boolean isEqual(final Termin ae) {
		return super.isMatching(ae, 0, FLD_TAG, FLD_BEGINN, FLD_DAUER, "BeiWem",
				"Typ", "Status", "ErstelltVon", "Wer");
	}

	public TimeTool getStartTime() {
		String[] res = new String[2];
		get(new String[] { FLD_TAG, FLD_BEGINN }, res);
		TimeTool start = new TimeTool(res[0]);
		start.addMinutes(checkZero(res[1]));
		return start;
	}

	public TimeSpan getTimeSpan() {
		String[] res = new String[3];
		get(new String[] { FLD_TAG, FLD_BEGINN, FLD_DAUER }, res);
		TimeTool start = new TimeTool(res[0]);
		start.addMinutes(checkZero(res[1]));
		return new TimeSpan(start, checkZero(res[2]));
	}

	public boolean setStartTime(final TimeTool t) {
		if (checkLock()) {
			return false;
		}
		String Tag = t.toString(TimeTool.DATE_COMPACT);
		int Beginn = (t.get(TimeTool.HOUR_OF_DAY) * 60)
				+ t.get(TimeTool.MINUTE);
		if (Beginn > 0) {
			set(new String[] { FLD_TAG, FLD_BEGINN, "lasetedit" }, Tag, Integer
					.toString(Beginn), createTimeStamp());
			return true;
		}
		return false;
	}

	public void setEndTime(final TimeTool o) {
		if (!checkLock()) {
			TimeSpan ts = getTimeSpan();
			ts.until = o;
			set(new String[] { FLD_DAUER, FLD_LASTEDIT }, Integer.toString(ts
					.getSeconds() / 60), createTimeStamp());
		}
	}

	/*
	 * public boolean setMandant(Mandant m) { set(new
	 * String[]{"BeiWem","lastedit"},m.getId(),getTimeStamp()); return true; }
	 */
	public void setBereich(final String bereich) {
		if (!checkLock()) {
			set(new String[] { "BeiWem", FLD_LASTEDIT }, bereich,
					createTimeStamp());
		}
	}

	public String toString() {
		return toString(2);
	}

	public String toString(final int level) {
		String[] vals = new String[4];
		get(new String[] { FLD_TAG, FLD_DAUER, FLD_BEGINN, "BeiWem" }, vals);
		TimeTool d = new TimeTool(vals[0]);
		d.addMinutes(checkZero(vals[2]));
		String f = d.toString(TimeTool.WEEKDAY) + ", "
				+ d.toString(TimeTool.LARGE_GER);
		if (level > 0) {
			d.addMinutes(checkZero(vals[1]));
			f += "-" + d.toString(TimeTool.TIME_SMALL);
		}
		if (level > 1) {
			f += " (" + vals[3] + ")";

		}
		return f;
	}

	public int getBeginn() {
		return getInt(FLD_BEGINN);
	}

	public static String intTimeToString(final int t) {
		int hour = t / 60;
		int minute = t - (hour * 60);
		StringBuffer ret = new StringBuffer();
		ret.append(StringTool.pad(StringTool.LEFT, '0', Integer.toString(hour),
				2));
		ret.append(":");
		ret.append(StringTool.pad(StringTool.LEFT, '0', Integer
				.toString(minute), 2));
		return ret.toString();
	}

	public int getDauer() {
		return getInt(FLD_DAUER);
	}

	static int TimeInMinutes(final TimeTool t) {
		return (t.get(TimeTool.HOUR_OF_DAY) * 60) + t.get(TimeTool.MINUTE);
	}

	public static class remark extends PersistentObject {

		public String bemerkung;
		static {
			addMapping("agnRemarks", "remark");
		}

		public remark(final String id) {
			super(id);
			if (exists()) {
				bemerkung = get("remark");
			} else {
				create(id);
				bemerkung = "";
			}
		}

		public void set(final String newval) {
			if (StringTool.isNothing(newval)) {
				j.exec("DELETE from agnRemarks WHERE ID=" + getWrappedId());
			} else {
				set("remark", newval);
			}
			bemerkung = newval;
		}

		@Override
		protected String getTableName() {
			return "agnRemarks";
		}

		@Override
		public String getLabel() {
			return bemerkung;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final Termin o) {
		TimeSpan t0 = getTimeSpan();
		TimeSpan t1 = o.getTimeSpan();
		if (t0.from.isAfter(t1.from)) {
			return 1;
		} else if (t0.from.isBefore(t1.from)) {
			return -1;
		} else {
			return 0;
		}
	}

	@Override
	protected String getTableName() {
		return "AGNTERMINE";
	}

	public String dump() {
		StringBuffer res = new StringBuffer(200);
		String[] fields = { FLD_TAG, "BeiWem", "Wer", "Typ", "Status" };
		String[] result = new String[fields.length];
		get(fields, result);
		// result[1]=Mandant.load(result[1]).getLabel();
		result[2] = Patient.load(result[2]).get("Name");
		for (int i = 0; i < fields.length; i++) {
			res.append(fields[i]).append("=").append(result[i]).append(",");
		}
		return res.toString();
	}

	@Override
	public String getLabel() {
		StringBuilder sb = new StringBuilder();
		TimeSpan ts = getTimeSpan();
		sb.append(new TimeTool(getDay()).toString(TimeTool.DATE_GER)).append(
				",");
		sb.append(ts.from.toString(TimeTool.TIME_SMALL)).append("-").append(
				ts.until.toString(TimeTool.TIME_SMALL)).append(" ").append(
				getPersonalia()).append(" (").append(getType()).append(",")
				.append(getStatus()).append(") ");
		return sb.toString();
	}

	public String getDay() {
		return get(FLD_TAG);
	}

	public int getDurationInMinutes() {
		return getInt(FLD_DAUER);
	}

	public int getStartMinute() {
		return checkZero(get(FLD_BEGINN));
	}

	public String getTitle() {
		return getPersonalia();
	}

	public String getType() {
		return get("Typ");
	}

	public void setStartMinute(final int min) {
		if (!checkLock()) {
			set(new String[] { FLD_BEGINN, FLD_LASTEDIT }, Integer.toString(min),
					createTimeStamp());
		}
	}

	public void setDurationInMinutes(final int min) {
		if (!checkLock()) {
			set(new String[] { FLD_DAUER, FLD_LASTEDIT }, Integer.toString(min),
					createTimeStamp());
		}
	}

	@Override
	public int getCacheTime() {
		return 5;
	}

	public static class Free implements IPlannable {
		String day;
		int start, length;

		public Free(final String d, final int s, final int l) {
			day = d;
			start = s;
			length = l;
		}

		public String getDay() {
			return day;
		}

		public int getDurationInMinutes() {
			return length;
		}

		public int getStartMinute() {
			return start;
		}

		public String getStatus() {
			return Termin.statusLeer();
		}

		public String getText() {
			return "";
		}

		public String getTitle() {
			// return "-";
			return String.format(Messages.MinutesFree, length);
		}

		public String getType() {
			return Termin.typFrei();
		}

		public void setStartMinute(final int min) {
			start = min;
		}

		public void setDurationInMinutes(final int min) {
			length = min;
		}
	}

	@Override
	public boolean isDragOK() {
		return true;
	}

}