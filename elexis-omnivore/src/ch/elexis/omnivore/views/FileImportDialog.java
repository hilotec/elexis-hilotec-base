/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: FileImportDialog.java 1584 2007-01-08 18:05:36Z rgw_ch $
 *******************************************************************************/

package ch.elexis.omnivore.views;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import ch.elexis.Hub;
import ch.elexis.omnivore.data.DocHandle;
import ch.elexis.util.SWTHelper;

public class FileImportDialog extends TitleAreaDialog {
	String file;
	DocHandle dh;
	Text tTitle;
	Text tKeywords;
	public String title;
	public String keywords;
	
	public FileImportDialog(DocHandle dh){
		super(Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getShell());
		this.dh=dh;
		file=dh.get("Titel");
	}
	public FileImportDialog(String name){
		super(Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getShell());
		file=name;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		new Label(ret,SWT.NONE).setText("Titel");
		tTitle=SWTHelper.createText(ret, 1, SWT.NONE);
		new Label(ret,SWT.NONE).setText("Stichwörter");
		tKeywords=SWTHelper.createText(ret,4, SWT.NONE);
		tTitle.setText(file);
		if(dh!=null){
			tKeywords.setText(dh.get("Keywords"));
		}
		return ret;
	}

	@Override
	public void create() {
		super.create();
		setTitle(file);
		getShell().setText("Datei importieren");
		setMessage("Geben Sie bitte einen Titel und ggf. einige Stichwörter für dieses Dokument ein");
	}

	@Override
	protected void okPressed() {
		keywords=tKeywords.getText();
		title=tTitle.getText();
		if(dh!=null){
			dh.set("Titel", title);
			dh.set("Keywords", keywords);
		}
		super.okPressed();
	}
	
	
}
