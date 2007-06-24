/*******************************************************************************
 * Copyright (c) 2007, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    
 *  $Id$
 *******************************************************************************/
package ch.elexis.importers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.Hub;
import ch.elexis.util.ImporterPage;
import ch.elexis.util.SWTHelper;

/**
 * Generic importer for any type of objects.
 * You can open an external source (e. g. an Excel Table), select the type
 * of the objects to be imported and map the fields.
 * 
 * @author Daniel Lutz <danlutz@watz.ch>
 *
 */
public class GenericImporter extends ImporterPage {
	public static final String TITLE = "Allgemein";
	
	GenericImporterBlatt importerBlatt;
	
	public GenericImporter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Composite createPage(Composite parent) {
		importerBlatt = new GenericImporterBlatt(parent);
		importerBlatt.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		return importerBlatt;
	}

	@Override
	public IStatus doImport(IProgressMonitor monitor) throws Exception {
		if(importerBlatt.doImport()){
			return Status.OK_STATUS;
		}
		return new Status(Status.ERROR,"ch.elexis.import.div",1,"Fehler beim Import",null);
	}

	@Override
	public String getDescription() {
		return "Import von beliebigen Daten aus verschiedener Quelle";
	}

	@Override
	public String getTitle() {
		return TITLE;
	}
}
