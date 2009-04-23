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
 *  $Id: Anwender.java 5266 2009-04-23 13:41:31Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import java.util.Hashtable;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.PatientPerspektive;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.admin.ACE;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.rgw.io.SqlSettings;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * Ein Anwender ist eine Person (und damit auch ein Kontakt), die zusätzlich das
 * Recht hat, diese Software zu benützen. Ein Anwender hat Username und
 * Passwort, sowie ein AgendaLabel. Jeder Anwender gehört zu mindestens einer
 * Gruppe.
 * 
 * Diese Klasse enthält ausserdem die statische Methode "login", mit der ein
 * Anwender sich anmelden kann.
 * 
 * @author Gerry
 * 
 */
public class Anwender extends Person {

	static {
		addMapping("KONTAKT", "ExtInfo", "istAnwender", "Label=Bezeichnung3",
				"Reminders=JOINT:ReminderID:ResponsibleID:REMINDERS_RESPONSIBLE_LINK");
	}

	public Anwender(final String Username, final String Password) {
		create(null);
		set(new String[] { "Name" }, Username);
		setLabel(Username);
		setPwd(Password);
		setInfoElement("Groups", "Anwender");
	}

	public Anwender(final String Name, final String Vorname,
			final String Geburtsdatum, final String s) {
		super(Name, Vorname, Geburtsdatum, s);
	}

	/**
	 * Check if this Anwender is valid.
	 * <p>
	 * We check wheter the object exists in the database and whether the login
	 * name ("Label") is available.
	 * </p>
	 */
	@Override
	public boolean isValid() {
		String label = get("Label");
		if (StringTool.isNothing(label)) {
			return false;
		}
		if (label.equals("Administrator")) {
			return true; // Admin is always valid
		}
		return super.isValid();
	}

	/**
	 * Return a short or long label for this Anwender
	 * 
	 * This implementation returns the "Label" field for both label types
	 * 
	 * @return a label describing this Person
	 */
	@Override
	public String getLabel(final boolean shortLabel) {
		String l = get("Label");
		if (StringTool.isNothing(l)) {
			l = checkNull(get("Name")) + " " + checkNull(get("Vorname"));
			if (StringTool.isNothing(l)) {
				l = "unbekannt";
			}
		}
		return l;
	}

	/**
	 * Kurzname setzen. Zuerst prüfen, ob es wirklich ein neuer Name ist, um
	 * unnötigen Netzwerkverkehr zu vermeiden
	 */
	public void setLabel(final String label) {
		String oldlabel = getLabel();
		if (!label.equals(oldlabel)) {
			set("Label", label);
		}
	}

	/** Passwort setzen */
	public void setPwd(final String pwd) {
		setInfoElement("UsrPwd", pwd);
	}

	/**
	 * Get Reminders for this user, related to a specific Kontakt
	 * 
	 * @param k
	 *            related kontakt or null: all Reminders
	 * @return a List sorted by date
	 */
	public SortedSet<Reminder> getReminders(final Kontakt k) {
		TreeSet<Reminder> ret = new TreeSet<Reminder>();
		List<String[]> rem = getList("Reminders", (String[]) null);
		if (rem != null) {
			String kid = k == null ? null : k.getId();
			for (String[] l : rem) {
				Reminder r = Reminder.load(l[0]);
				if (kid != null) {
					if (!r.get("IdentID").equals(kid)) {
						continue;
					}
				}
				ret.add(r);
			}
		}
		return ret;
	}

	@Override
	public String getKuerzel() {
		String[] res = new String[2];
		get(new String[] { "Name", "Vorname" }, res);
		return res[0].substring(0, 1) + res[1].substring(0, 1);
	}

	public static Anwender load(final String id) {
		Anwender ret = new Anwender(id);
		if (ret.state() > PersistentObject.INVALID_ID) {
			return ret;
		}
		return null;
	}

	@Override
	protected String getConstraint() {
		return "istAnwender='1'";
	}

	@Override
	protected void setConstraint() {
		set("istAnwender", "1");
	}

	protected Anwender() {/* leer */
	}

	protected Anwender(final String id) {
		super(id);
	}

	/**
	 * Den ersten Benutzer anlegen und initiale Zugriffsrechte setzen Wird von
	 * PersistentObject() aufgerufen, wenn die Datenbank neu angelegt wurde.
	 */
	@SuppressWarnings("unchecked")
	protected static void init() {
		// Administrator muss "zu fuss" erstellt werden, da noch keine
		// Rechteverwaltung vorhanden ist
		Anwender admin = new Anwender();
		admin.create(null);
		admin.set(new String[] { "Name", "Label", "istAnwender" },
				"Administrator", "Administrator", "1");
		Hub.actUser = admin;
		Hub.acl.grant(admin, 
			new ACE(ACE.ACE_IMPLICIT,"WriteInfoStore"),
			new ACE(ACE.ACE_IMPLICIT,"LoadInfoStore"),
			new ACE(ACE.ACE_IMPLICIT,"WriteGroups"),
			new ACE(ACE.ACE_IMPLICIT,"ReadGroups"));
		Hashtable hash = admin.getInfoStore();
		hash.put("UsrPwd", "admin");
		hash.put("Groups", "Admin,Anwender");
		admin.flushInfoStore(hash);
		Hub.acl.grant("Admin", 
			new ACE(ACE.ACE_IMPLICIT,"ReadUsrPwd"),
			new ACE(ACE.ACE_IMPLICIT,"WriteUsrPwd"),
			new ACE(ACE.ACE_IMPLICIT,"CreateAndDelete"),
				new ACE(ACE.ACE_IMPLICIT,"WriteGroups"));
		Hub.acl.grant("System", new ACE(ACE.ACE_IMPLICIT,"ReadUsrPwd"));

	}

	/**
	 * Login: Anwender anmelden, passenden Mandanten anmelden. (Jeder Anwender
	 * ist entweder selber ein Mandant oder ist einem Mandanten zugeordnet)
	 * 
	 * @param text
	 *            Kurzname
	 * @param text2
	 *            Passwort
	 * @return true - erfolgreich angemeldet, Hub.actUser gesetzt.
	 */
	@SuppressWarnings("unchecked")
	public static boolean login(final String text, final String text2) {
		logoff();
		Hub.actUser = null;
		Hub.mainActions.adaptForUser();
		Query<Anwender> qbe = new Query<Anwender>(Anwender.class);
		qbe.add("Label", "=", text);
		List<Anwender> list = qbe.execute();
		if ((list == null) || (list.size() < 1)) {
			return false;
		}
		Anwender a = list.get(0);
		Hashtable km = a.getHashtable("ExtInfo");
		if (km == null) {
			log.log("Fehler in der Datenstruktur ExtInfo von " + a.getLabel(),
					Log.ERRORS);
			MessageDialog.openError(null, "Interner Fehler",
					"Die Datenstruktur ExtInfo von " + a.getLabel()
							+ " ist beschädigt.");
			a.setHashtable("ExtInfo", new Hashtable());
		}
		String pwd = (String) km.get("UsrPwd");
		if (pwd == null) {
			return false;
		}
		if (pwd.equals(text2)) {
			Hub.actUser = a;
			String MandantLabel = (String) km.get("Mandant");
			String MandantID = null;
			if (!StringTool.isNothing(MandantLabel)) {
				MandantID = new Query<Mandant>(Mandant.class).findSingle(
						"Label", "=", MandantLabel);
			}
			if (MandantID != null) {
				Hub.setMandant(Mandant.load(MandantID));
			} else {
				Mandant m = Mandant.load(a.getId());
				if ((m != null) && m.isValid()) {
					Hub.setMandant(m);
				} else {
					List<Mandant> ml = new Query<Mandant>(Mandant.class)
							.execute();
					if ((ml != null) && (ml.size() > 0)) {
						m = ml.get(0);
						Hub.setMandant(m);

					} else {
						SWTHelper
								.showError(
										"Kein Mandant definiert",
										"Sie können Elexis erst normal benutzen, wenn Sie mindestens einen Mandanten definiert haben");
						// new
						// ErrorDialog(Desk.theDisplay.getActiveShell(),"Kein
						// Mandant definiert","Sie können Elexis erst benutzen,
						// wenn Sie mindestens einen Mandanten definiert haben",
						// Status.CANCEL_STATUS,0).open();
					}
				}
			}

			Hub.userCfg = new SqlSettings(getConnection(), "USERCONFIG",
					"Param", "Value", "UserID=" + a.getWrappedId());

			Hub.mainActions.adaptForUser();

			// String perspektive=(String)km.get("StartPerspektive");
			String perspektive = Hub.localCfg.get(Hub.actUser
					+ GlobalActions.DEFAULTPERSPECTIVECFG, null);
			if (perspektive == null) {
				perspektive = PatientPerspektive.ID;
			}
			try {
				Desk.updateFont(PreferenceConstants.USR_DEFAULTFONT);
				IWorkbenchWindow win = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				PlatformUI.getWorkbench().showPerspective(perspektive, win);
				Hub.heart.resume(true);
				GlobalEvents.getInstance().fireSelectionEvent(Hub.actUser);
				GlobalEvents.getInstance().fireUpdateEvent(Anwender.class);
				GlobalEvents.getInstance().fireUserEvent();
				return true;
			} catch (Exception ex) {
				ExHandler.handle(ex);
				SWTHelper.showError("Perspektive nicht gefunden",
						"Konnte die eingestellte Startperspektive "
								+ perspektive + " nicht laden.");
				return true;
			}

		}
		return false;
	}

	public static void logoff() {
		if (Hub.userCfg != null) {
			Hub.userCfg.flush();
		}
		Hub.setMandant(null);
		Hub.heart.suspend();
		Hub.actUser = null;
		Hub.userCfg = Hub.localCfg;
	}
}
