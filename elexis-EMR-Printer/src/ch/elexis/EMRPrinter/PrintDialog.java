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
 *  $Id: PrintDialog.java 2626 2007-06-24 14:23:19Z rgw_ch $
 *******************************************************************************/
package ch.elexis.EMRPrinter;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.jdom.Document;
import org.jdom.Element;

import ch.elexis.Hub;
import ch.elexis.data.Brief;
import ch.elexis.exchange.XChangeExporter;
import ch.elexis.text.TextContainer;
import ch.elexis.text.ITextPlugin.ICallback;
import ch.elexis.util.SWTHelper;

public class PrintDialog extends TitleAreaDialog implements ICallback{
	XChangeExporter exporter;	
	
	public PrintDialog(XChangeExporter exp){
		super(Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getShell());
		exporter=exp;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		TextContainer text=new TextContainer(getShell());
		ret.setLayout(new FillLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		text.getPlugin().createContainer(ret, this);
		text.getPlugin().showMenu(false);
		text.getPlugin().showToolbar(false);
		text.createFromTemplateName(null, "KG-Ausdruck", Brief.UNKNOWN, Hub.actUser, "KG");
		
		//text.getPlugin().insertText("[Einträge]", title.toString(), SWT.RIGHT);
		return ret;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText("Export auf Drucker");
		setTitle("KG ausdrucken");
		setMessage("Klicken Sie auf das Drucker-Symbol, um den Ausdruck zu starten");
		getShell().setSize(900,700);
		SWTHelper.center(Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getShell(), getShell());
	}

	public void save() {
		// TODO Auto-generated method stub
		
	}

	public boolean saveAs() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
