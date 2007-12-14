/*******************************************************************************
 * Copyright (c) 2007, medshare and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    M. Imhof - initial implementation
 *    
 * $Id: KontaktEntry.java 3442 2007-12-14 07:39:59Z michael_imhof $
 *******************************************************************************/

package ch.medshare.elexis.directories;

public class KontaktEntry {
	private final String vorname;
	private final String name;
	private final String zusatz;
	private final String adresse;
	private final String plz;
	private final String ort;
	private final String tel;
	private final String fax;
	private final String email;
	private final boolean isDetail; // List Kontakt oder Detail Kontakt

	public KontaktEntry(final String vorname, final String name,
			final String zusatz, final String adresse, final String plz,
			final String ort, final String tel, String fax, String email,
			boolean isDetail) {
		super();
		this.vorname = vorname;
		this.name = name;
		this.zusatz = zusatz;
		this.adresse = adresse;
		this.plz = plz;
		this.ort = ort;
		this.tel = tel;
		this.fax = fax;
		this.email = email;
		this.isDetail = isDetail;
	}

	public String getName() {
		return this.name;
	}

	public String getVorname() {
		return this.vorname;
	}

	public String getZusatz() {
		return this.zusatz;
	}

	public String getAdresse() {
		return this.adresse;
	}

	public String getPlz() {
		return this.plz;
	}

	public String getOrt() {
		return this.ort;
	}

	public String getTelefon() {
		return this.tel;
	}

	public String getFax() {
		return fax;
	}

	public String getEmail() {
		return email;
	}

	public boolean isDetail() {
		return this.isDetail;
	}

	private int countValue(String value) {
		if (value != null && value.length() > 0) {
			return 1;
		}
		return 0;
	}

	public int countNotEmptyFields() {
		return countValue(getVorname()) + countValue(getName())
				+ countValue(getZusatz()) + countValue(getAdresse())
				+ countValue(getPlz()) + countValue(getOrt())
				+ countValue(getTelefon()) + countValue(getFax())
				+ countValue(getEmail());
	}

	public String toString() {
		return getName() + ", " + getZusatz() + ", " + getAdresse() + ", " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ getPlz() + " " + getOrt() + " " + getTelefon(); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
