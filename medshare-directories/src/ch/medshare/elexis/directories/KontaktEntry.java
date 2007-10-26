package ch.medshare.elexis.directories;

public class KontaktEntry {
	private final String vorname;
	private final String name;
	private final String zusatz;
	private final String adresse;
	private final String plz;
	private final String ort;
	private final String tel;
	private final boolean isDetail; // List Kontakt oder Detail Kontakt

	public KontaktEntry(final String vorname, final String name, final String zusatz, final String adresse,
			final String plz, final String ort, final String tel, boolean isDetail) {
		super();
		this.vorname = vorname;
		this.name = name;
		this.zusatz = zusatz;
		this.adresse = adresse;
		this.plz = plz;
		this.ort = ort;
		this.tel = tel;
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
	
	public boolean isDetail() {
		return this.isDetail;
	}

	public String toString() {
		return getName() + ", " + getZusatz() + ", " + getAdresse() + ", " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ getPlz() + " " + getOrt() + " " + getTelefon(); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
