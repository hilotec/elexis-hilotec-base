/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: PhysioImporter.java 5138 2009-02-16 18:27:19Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import java.io.FileReader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Composite;

import au.com.bytecode.opencsv.CSVReader;
import ch.elexis.util.ImporterPage;

public class PhysioImporter extends ImporterPage {
	
	@Override
	public Composite createPage(Composite parent){
		return new FileBasedImporter(parent, this);
	}
	
	@Override
	public IStatus doImport(IProgressMonitor monitor) throws Exception{
		CSVReader reader = new CSVReader(new FileReader(results[0]), ';');
		String[] line = reader.readNext();
		while ((line = reader.readNext()) != null) {
			/* PhysioLeistung pl = */new PhysioLeistung(line[0], line[1], line[2], null, null);
		}
		return null;
	}
	
	@Override
	public String getDescription(){
		return "Physiotherapie-Tarif";
	}
	
	@Override
	public String getTitle(){
		return "Physio";
	}
	
}
