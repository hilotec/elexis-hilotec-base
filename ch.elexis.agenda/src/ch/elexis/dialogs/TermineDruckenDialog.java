/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: TermineDruckenDialog.java 6354 2010-05-13 11:47:12Z rgw_ch $
 *******************************************************************************/
package ch.elexis.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.Hub;
import ch.elexis.agenda.data.Termin;
import ch.elexis.agenda.preferences.PreferenceConstants;
import ch.elexis.agenda.util.Plannables;
import ch.elexis.data.Brief;
import ch.elexis.text.TextContainer;
import ch.elexis.text.ITextPlugin.ICallback;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

public class TermineDruckenDialog extends TitleAreaDialog implements ICallback {
	Termin[] liste;
	
	private TextContainer text = null;
	
	public TermineDruckenDialog(Shell shell, Termin[] liste){
		super(shell);
		this.liste = liste;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new FillLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		String template =
			Hub.localCfg.get(PreferenceConstants.AG_PRINT_APPOINTMENTCARD_TEMPLATE,
				PreferenceConstants.AG_PRINT_APPOINTMENTCARD_TEMPLATE_DEFAULT);
		
		text = new TextContainer(getShell());
		text.getPlugin().createContainer(ret, this);
		text.getPlugin().showMenu(true);
		text.getPlugin().showToolbar(true);
		text.createFromTemplateName(null, template, Brief.UNKNOWN, Hub.actUser, "Agenda");
		/*
		 * String[][] termine=new String[liste.length+1][3]; termine[0]=new String[]{"Datum",
		 * "Zeit","Bei"}; for(int i=0;i<liste.length;i++){ TimeTool day=new
		 * TimeTool(liste[i].getDay()); termine[i+1][0]=day.toString(TimeTool.DATE_GER);
		 * termine[i+1][1]=Plannables.getStartTimeAsString(liste[i]);
		 * termine[i+1][2]=liste[i].getBereich(); } text.getPlugin().setFont("Helvetica",
		 * SWT.NORMAL, 9); text.getPlugin().insertTable("[Termine]",
		 * ITextPlugin.FIRST_ROW_IS_HEADER, termine, new int[]{20,20,60});
		 */
		StringBuilder sb = new StringBuilder();
		for (Termin t : liste) {
			TimeTool day = new TimeTool(t.getDay());
			sb.append(day.toString(TimeTool.WEEKDAY)).append(", ").append(
				day.toString(TimeTool.DATE_GER)).append(" - ").append(
				Plannables.getStartTimeAsString(t)).append("\n");
		}
		text.replace("\\[Termine\\]", sb.toString());
		if (text.getPlugin().isDirectOutput()) {
			text.getPlugin().print(null, null, true);
			okPressed();
		}
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setMessage("Terminliste ausdrucken");
		setTitle("Terminliste");
		getShell().setText("Agenda");
		getShell().setSize(800, 700);
		
	}
	
	@Override
	protected void okPressed(){
		super.okPressed();
	}
	
	public void save(){}
	
	public boolean saveAs(){
		return false;
	}
	
	public boolean doPrint(){
		if (text == null) {
			// text container is not initialized
			return false;
		}
		
		String printer =
			Hub.localCfg.get(PreferenceConstants.AG_PRINT_APPOINTMENTCARD_PRINTER_NAME, "");
		String tray =
			Hub.localCfg.get(PreferenceConstants.AG_PRINT_APPOINTMENTCARD_PRINTER_TRAY, null);
		
		return text.getPlugin().print(printer, tray, false);
	}
}
