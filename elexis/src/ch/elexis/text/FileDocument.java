/*******************************************************************************
 * Copyright (c) 2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import ch.elexis.ElexisException;
import ch.elexis.data.Patient;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * An IDocument based on a file.
 * 
 * @author gerry
 * 
 */
public class FileDocument implements IDocument {
	String title;
	String category;
	File file;
	String date;
	Patient pat;
	String keywords;
	String guid = StringTool.unique("FileDocument");

	/**
	 * Create a new FileDocument. Note: The underlying file will NOT be copied
	 * but only referenced.
	 * 
	 * @param pat
	 *            The patient thsi document belongs to. Can be null
	 * @param title
	 *            Title for the document. Never Null and Never empty
	 * @param category
	 *            Category for the document. Can be null or empty
	 * @param file
	 *            File to link to this document
	 * @param date
	 *            date of creation
	 * @param keywords
	 *            space- or comma- separated list of keywords. May be empty or
	 *            null
	 */
	public FileDocument(Patient pat, String title, String category, File file,
			String date, String keywords) {
		this.title = title;
		this.category = category;
		this.file = file;
		this.date = date;
		this.pat = pat;
		this.keywords = keywords;
	}

	/**
	 * Delete the underlying file
	 */
	public void delete() {
		file.delete();
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getMimeType() {
		return "binary/octet-stream";
	}

	@Override
	public InputStream getContentsAsStream() throws ElexisException {
		try {
			FileInputStream fis = new FileInputStream(file);
			return fis;
		} catch (FileNotFoundException e) {
			ExHandler.handle(e);
			throw new ElexisException(getClass(), e.getMessage(),
					ElexisException.EE_FILE_ERROR);
		}
	}

	public byte[] getContentsAsBytes() throws ElexisException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			FileTool.copyStreams(getContentsAsStream(), baos);
			return baos.toByteArray();
		} catch (IOException e) {
			throw new ElexisException(getClass(), e.getMessage(),
					ElexisException.EE_FILE_ERROR);
		}

	}

	@Override
	public String getKeywords() {
		return keywords;
	}

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public String getCreationDate() {
		return date;
	}

	@Override
	public Patient getPatient() {
		return pat;
	}

	@Override
	public String getGUID() {
		return guid;
	}

}
