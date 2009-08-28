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
 *  $Id: Person.java 5688 2009-08-28 06:26:36Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import ch.elexis.StringConstants;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Eine Person ist ein Kontakt mit zusätzlich Namen, Geburtsdatum und
 * Geschlecht.
 * 
 * @author gerry
 * 
 */
public class Person extends Kontakt {
	public static final String TITLE = "Titel"; //$NON-NLS-1$
	public static final String MOBILE = "Natel"; //$NON-NLS-1$
	public static final String SEX = "Geschlecht"; //$NON-NLS-1$
	public static final String BIRTHDATE = "Geburtsdatum"; //$NON-NLS-1$
	public static final String FIRSTNAME = "Vorname"; //$NON-NLS-1$
	public static final String NAME = "Name"; //$NON-NLS-1$
	public static final String MALE = "m"; //$NON-NLS-1$
	public static final String FEMALE = "w"; //$NON-NLS-1$

	static {
		addMapping(Kontakt.TABLENAME, "Name			=	Bezeichnung1", //$NON-NLS-1$
				"Vorname		=   Bezeichnung2", "Zusatz 		=	Bezeichnung3", //$NON-NLS-1$ //$NON-NLS-2$
				"Geburtsdatum	=	S:D:Geburtsdatum", SEX, "Natel=NatelNr", //$NON-NLS-1$ //$NON-NLS-2$
				Kontakt.IS_PERSON, TITLE);
	}

	public String getName() {
		return checkNull(get(NAME));
	}

	public String getVorname() {
		return checkNull(get(FIRSTNAME));
	}

	public String getGeburtsdatum() {
		return checkNull(get(BIRTHDATE));
	}

	public String getGeschlecht() {
		return checkNull(get(SEX));
	}

	public String getNatel() {
		return get(MOBILE);
	}

	public boolean isValid() {
		return super.isValid();
	}

	/** Eine Person mit gegebener Id aus der Datenbank einlesen */
	public static Person load(String id) {
		Person ret = new Person(id);
		if (ret.get(NAME) == null) {
			return null;
		}
		return ret;
	}

	protected Person(String id) {
		super(id);
	}

	public Person() {
		// System.out.println("Person");
	}

	/** Eine neue Person erstellen */
	public Person(String Name, String Vorname, String Geburtsdatum, String s) {
		create(null);
		// String[] vals=new String[]{Name,Vorname,new
		// TimeTool(Geburtsdatum).toString(TimeTool.DATE_COMPACT),s};
		String[] vals = new String[] { Name, Vorname, Geburtsdatum, s };
		String[] fields = new String[] { NAME, FIRSTNAME, BIRTHDATE, SEX };
		set(fields, vals);
	}

	/**
	 * This constructor is more critical than the previous one
	 * 
	 * @param name
	 *            will be checked for non-alphabetic characters and may not be
	 *            empty
	 * @param vorname
	 *            will be checked for non alphabetic characters but may be empty
	 * @param gebDat
	 *            will be checked for unplausible values but may be null
	 * @param s
	 *            will be checked for undefined values and may not be empty
	 * @throws PersonDataException
	 */
	public Person(String name, String vorname, TimeTool gebDat, String s)
	throws PersonDataException {
		name = name.trim();
		vorname = vorname.trim();
		if ((StringTool.isNothing(name))
				|| (!name.matches("[" + StringTool.wordChars + "\\s-]+"))) { //$NON-NLS-1$ //$NON-NLS-2$
			throw new PersonDataException(PersonDataException.CAUSE.LASTNAME);
		}
		if ((!StringTool.isNothing(vorname))
				&& (!vorname.matches("[" + StringTool.wordChars + "\\s-]+"))) { //$NON-NLS-1$ //$NON-NLS-2$
			throw new PersonDataException(PersonDataException.CAUSE.FIRSTNAME);
		}
		String dat = StringTool.leer;
		if (gebDat != null) {
			TimeTool now = new TimeTool();
			int myYear = now.get(TimeTool.YEAR);
			int oYear = gebDat.get(TimeTool.YEAR);
			if (oYear > myYear || oYear < myYear - 120) {
				throw new PersonDataException(
						PersonDataException.CAUSE.BIRTHDATE);
			}
			dat = gebDat.toString(TimeTool.DATE_COMPACT);
		}
		if (!s.matches("[mw]")) { //$NON-NLS-1$
			throw new PersonDataException(PersonDataException.CAUSE.SEX);
		}
		create(null);
		String[] fields = new String[] { NAME, FIRSTNAME, BIRTHDATE, SEX };
		String[] vals = new String[] { name, vorname, dat, s };
		set(fields, vals);
	}

	/**
	 * Return a short or long label for this Person
	 * 
	 * @return a label describing this Person
	 */
	public String getLabel(boolean shortLabel) {
		StringBuilder sb = new StringBuilder();

		if (shortLabel) {
			sb.append(getVorname()).append(StringTool.space).append(getName());
			return sb.toString();
		} else {
			return getPersonalia();
		}

	}

	/**
	 * Initialen holen
	 */
	public String getInitials(){
		StringBuilder ret=new StringBuilder();
		String name=getName();
		String vorname=getVorname();
		String sex=getGeschlecht();
		String geb=getGeburtsdatum();
		ret.append((name.length()>0) ? name.substring(0,1): name).append(".");
		ret.append((vorname.length()>0) ? vorname.substring(0,1) : vorname).append(".(")
		.append(sex).append("), ").append(geb);
		return ret.toString();
	}
	/** Einen String mit den Personalien holen */
	public String getPersonalia() {
		StringBuffer ret = new StringBuffer(200);
		String[] fields = new String[] { NAME, FIRSTNAME, BIRTHDATE, SEX,
				TITLE };
		String[] vals = new String[fields.length];
		get(fields, vals);
		if (!StringTool.isNothing(vals[4])) {
			ret.append(vals[4]).append(StringTool.space);
		}
		ret.append(vals[0]);
		if (!StringTool.isNothing(vals[1])) {
			ret.append(StringTool.space).append(vals[1]);
		}
		if (StringTool.isNothing(vals[3])) {
			ret.append(StringTool.space);
		} else {
			ret.append("(").append(vals[3]).append("), "); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (!StringTool.isNothing(vals[2])) {
			ret.append(new TimeTool(vals[2]).toString(TimeTool.DATE_GER));
		}
		return ret.toString();
	}

	@Override
	protected String getConstraint() {
		return new StringBuilder(Kontakt.IS_PERSON).append(StringTool.equals)
		.append(JdbcLink.wrap(StringConstants.ONE)).toString();
	}

	@Override
	protected void setConstraint() {
		set(Kontakt.IS_PERSON, StringConstants.ONE);
	}

	/**
	 * Statistik für ein bestimmtes Objekt führen
	 * 
	 * @param ice
	 */
	public void countItem(ICodeElement ice) {
		statForItem((PersistentObject) ice);
	}

	@SuppressWarnings("serial")
	public static class PersonDataException extends Exception {
		enum CAUSE {
			LASTNAME, FIRSTNAME, BIRTHDATE, SEX
		}

		static final String[] causes = new String[] { NAME, FIRSTNAME,
			BIRTHDATE, "Geschlecht (m oder w)" }; //$NON-NLS-1$

		public CAUSE cause;

		PersonDataException(CAUSE cause) {
			super(causes[cause.ordinal()]);
			this.cause = cause;
		}
	}

}
