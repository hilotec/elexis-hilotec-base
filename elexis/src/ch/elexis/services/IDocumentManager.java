/*******************************************************************************
 * Copyright (c) 2009-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *  $Id: IDocumentManager.java 5954 2010-01-23 09:34:48Z rgw_ch $
 *******************************************************************************/
package ch.elexis.services;

import java.io.File;
import java.io.InputStream;

import ch.elexis.ElexisException;
import ch.elexis.data.Patient;

/**
 * A Service acting as DocumentManagement must implement this Interfache
 * 
 * @author gerry
 * 
 */
public interface IDocumentManager {

	
	/** List Categories for Documents */
	public String[] getCategories();
	
	/** Add a Categorie */
	public boolean addCategorie(String categorie);
	
	/**
	 * Import a document from a stream
	 * 
	 * @param pat
	 *            the patient this dosucment should be associated with. if this parameter is null,
	 *            the implementation MUST let the user chose the patient.
	 * @param is
	 *            InputStream that contains the document
	 * @param name
	 *            name of the document. if the parameter is null, implementation SHOULD ask user
	 * @param category
	 *            category to store this document under. if the implementation does not support
	 *            categories, it may ignore the parameter. If the parameter is null, and the
	 *            implementation supports categories, it SHOULD ask the user.
	 * @param keywords
	 *            some comma-delimited keywords to assign to the document. May be null. If the
	 *            implementation does not support keywords, it may ignore the parameter silently.
	 * @param date
	 *            the date that should be attributed this document. If the parameter is null, the
	 *            implementation MUST use the current date.
	 * @return the ID of the generated PersistentObject
	 * @throws ElexisException
	 *             if anything goes wrong
	 */
	public String addDocument(Patient pat, InputStream is, String name, String category,
		String keywords, String date) throws ElexisException;
	
	public boolean addDocument(Patient pat, String name, String catecory, String keywords,
		File file, String date);
	
	/**
	 * Render a Document to a Stream
	 * @param id ID of the Object (as generated with importFromStream)
	 * @return
	 */
	public InputStream getDocument(String id);
}
