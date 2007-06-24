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
 *  $Id: TermineDruckenDialog.java 2203 2007-04-12 12:47:00Z rgw_ch $
 *******************************************************************************/
package ch.elexis.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.Hub;
import ch.elexis.data.Brief;
import ch.elexis.data.Termin;
import ch.elexis.text.ITextPlugin;
import ch.elexis.text.TextContainer;
import ch.elexis.text.ITextPlugin.ICallback;
import ch.elexis.util.Plannables;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

public class TermineDruckenDialog extends TitleAreaDialog implements ICallback{
	Termin[] liste;
	public TermineDruckenDialog(Shell shell, Termin[] liste){
		super(shell);
		this.liste=liste;
	}
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new FillLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		TextContainer text=new TextContainer(getShell());
		text.getPlugin().createContainer(ret, this);
		text.getPlugin().showMenu(false);
		text.getPlugin().showToolbar(false);
		text.createFromTemplateName(null, "Terminkarte", Brief.UNKNOWN, Hub.actUser, "Agenda");
		String[][] termine=new String[liste.length+1][3];
		termine[0]=new String[]{"Datum", "Zeit","Bei"};
		for(int i=0;i<liste.length;i++){
			TimeTool day=new TimeTool(liste[i].getDay());
			termine[i+1][0]=day.toString(TimeTool.DATE_GER);
			termine[i+1][1]=Plannables.getStartTimeAsString(liste[i]);
			termine[i+1][2]=liste[i].getBereich();
		}
		text.getPlugin().setFont("Helvetica", SWT.NORMAL, 9);
		text.getPlugin().insertTable("[Termine]", ITextPlugin.FIRST_ROW_IS_HEADER, termine, new int[]{20,20,60});
		return ret;
	}

	@Override
	public void create() {
		super.create();
		setMessage("Terminliste ausdrucken");
		setTitle("Terminliste");
		getShell().setText("Agenda");
		getShell().setSize(800, 700);
	
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

	public void save() {
	}

	public boolean saveAs() {
		return false;
	}
	
}
