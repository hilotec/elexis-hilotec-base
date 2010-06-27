package ch.elexis.text;

import java.io.InputStream;

import ch.elexis.ElexisException;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;

public interface IDocument {
	public String getTitle();
	public String getMimeType();
	public InputStream getContentsAsStream() throws ElexisException;
	public byte[] getContentsAsBytes() throws ElexisException;
	public String getKeywords();
	public String getCategory();
	public String getCreationDate();
	public Patient getPatient();
}
