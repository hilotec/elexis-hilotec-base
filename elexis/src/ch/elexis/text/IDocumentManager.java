package ch.elexis.text;

import java.io.File;

import ch.elexis.data.Patient;

/**
 * A Service acting as DocumentManagement must implement this Interfache
 * 
 * @author gerry
 * 
 */
public interface IDocumentManager {
	public static final String NAME = "DocumentManagement";

	/** List Categories for Documents */
	public String[] getCategories();

	/** Add a Categorie */
	public boolean addCategorie(String categorie);

	public boolean addDocument(Patient pat, String name, String catecory,
			String keywords, File file, String date);
}
