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
 *  $Id: BAGMediImporter.java 3103 2007-09-06 18:56:55Z rgw_ch $
 *******************************************************************************/
package ch.elexis.medikamente.bag.data;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.util.ImporterPage;

public class BAGMediImporter extends ImporterPage {

	public BAGMediImporter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Composite createPage(Composite parent) {
		FileBasedImporter fbi=new FileBasedImporter(parent,this);
		return fbi;
	}

	@Override
	public IStatus doImport(IProgressMonitor monitor) throws Exception {
		
		return null;
	}

	@Override
	public String getDescription() {
		return "Import Medikamentenliste BAG";
	}

	@Override
	public String getTitle() {
		return "Medi-BAG";
	}

}
