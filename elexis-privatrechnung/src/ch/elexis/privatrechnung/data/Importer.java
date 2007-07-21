/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: Importer.java 2850 2007-07-21 05:00:02Z rgw_ch $
 *******************************************************************************/

package ch.elexis.privatrechnung.data;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;

import sun.util.logging.resources.logging;

import ch.elexis.util.ImporterPage;

/**
 * A class to import codes from an external source to this code system.
 * the external source must be an CSV or Excel(tm) file with the fields:
 * codeID,codeName,cost,price,validFrom,validUntil,factor
 * 
 * This Importer will be displayed, when the user selects "Import" from the
 * Details-View of the codes of this plugin
 */
public class Importer extends ImporterPage {

	
	/**
	 * Create the page that will let the user select a file to import.
	 * For simplicity, we use the default FileBasedImporter of our superclass.
	 */
	@Override
	public Composite createPage(Composite parent) {
		FileBasedImporter fbi=new FileBasedImporter(parent,this);
		fbi.setFilter(new String[]{"csv","xls","*"},
				new String[]{"Character Separated Values","Microsoft Excel 97","All Files"});
		return fbi;
	}

	/**
	 * The import process starts when the user has selected a file and clicked "OK".
	 * Warning: We can not read fields of the page created in createPage here! (The 
	 * page is already disposed when doImport is called). If we have to transfer field
	 * values between createPage and doImport, we must override collect().
	 * Our file based importer saves the user input in results[0]
	 */
	@Override
	public IStatus doImport(IProgressMonitor monitor) throws Exception {
		File file=new File(results[0]);
		if(!file.canRead()){
			log.log("Can't read "+results[0], log.ERRORS);
			return new Status(Status.ERROR,"ch.elexis.privatrechnung","Can't read "+results[0]);
		}
		
		return Status.OK_STATUS;
	}

	/**
	 * return a description to display in the messagr area of the import dialog
	 */
	@Override
	public String getDescription() {
		return "Import aus CSV und Excel";
	}

	/**
	 * return a title to display in the title bar of the import dialog
	 */
	@Override
	public String getTitle() {
		return "Privatleistungen";
	}

}
