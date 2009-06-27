package ch.elexis.text;

import java.io.File;

/**
 * A Service acting as DocumentManagement must implement this Interfache
 * @author gerry
 *
 */
public interface IDocumentManager {
	/** List Categories for Documents */
	public String[] getCategories();
	
	/** Add a Categorie */
	public boolean addCategorie(String categorie);
	
	public boolean addDocument(String name, String catecory, String keywords, File file);
}
