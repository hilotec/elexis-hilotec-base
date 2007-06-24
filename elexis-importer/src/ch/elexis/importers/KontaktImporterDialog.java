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
 *  $Id: KontaktImporterDialog.java 1917 2007-02-23 13:33:46Z rgw_ch $
 *******************************************************************************/
package ch.elexis.importers;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.Desk;
import ch.elexis.util.SWTHelper;

public class KontaktImporterDialog extends TitleAreaDialog {
	KontaktImporterBlatt kib;
	public KontaktImporterDialog(Shell shell){
		super(shell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		kib=new KontaktImporterBlatt(parent);
		kib.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		return kib;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Importiere Kontakt");
		setMessage("Geben Sie bitte Dateityp und Datei zum importieren an");
		setTitleImage(Desk.theImageRegistry.get(Desk.IMG_LOGO48));
		getShell().setText("Importer");
	}

	@Override
	protected void okPressed() {
		if(kib.doImport()){
			super.okPressed();
		}
		
	}
	
}
