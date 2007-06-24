/*******************************************************************************
 * Copyright (c) 2005-2006, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich -  initial implementation (PatientErfassenDialog)
 *    Daniel Lutz - adapted to ProblemErfassenDialog
 *    
 *  $Id$
 *******************************************************************************/

package org.iatrix.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.iatrix.data.Problem;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

public class ProblemErfassenDialog extends TitleAreaDialog {
	Text tBezeichnung;
	Text tNummer;
	Text tDatum;
	Problem problem = null;
	
	public Problem getResult(){
		return problem;
	}
	
	public ProblemErfassenDialog(Shell parent){
		super(parent);
	}
	
	public ProblemErfassenDialog(Shell parent, Problem problem) {
		this(parent);
		
		this.problem = problem;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		ret.setLayout(new GridLayout(2,false));
		
		new Label(ret, SWT.NONE).setText("Problem");
		tBezeichnung = new Text(ret, SWT.BORDER);
		tBezeichnung.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		
		new Label(ret, SWT.NONE).setText("Nummer");
		tNummer = new Text(ret, SWT.BORDER);
		tNummer.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		
		String currentDate = new TimeTool().toString(TimeTool.DATE_GER);
		
		new Label(ret, SWT.NONE).setText("Datum");
		tDatum = new Text(ret, SWT.BORDER);
		tDatum.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		tDatum.setText(currentDate);
		
		if (problem != null) {
			tBezeichnung.setText(problem.get("Bezeichnung"));
			tNummer.setText(problem.get("Nummer"));
			tDatum.setText(problem.get("Datum"));
		}
		
		return ret;
	}
	@Override
	public void create() {
		super.create();
		setMessage("Bitte die Problem-Datails - soweit bekannt - eingeben.");
		setTitle("Problem-Details eingeben");
		getShell().setText("Problem erfassen");
		setTitleImage(Desk.theImageRegistry.get("elexislogo48"));
	}
	@Override
	protected void okPressed() {
		String bezeichnung = tBezeichnung.getText();
		String nummer = tNummer.getText();
		String datum = tDatum.getText();

		if (problem == null) {
			problem = new Problem(GlobalEvents.getSelectedPatient(), bezeichnung);
			problem.setStatus(Problem.ACTIVE);
		} else {
			problem.set("Bezeichnung", bezeichnung);
		}
		problem.set(new String[] {"Nummer", "Datum"},
				new String[] {nummer, datum});
		GlobalEvents.getInstance().fireSelectionEvent(problem);
		super.okPressed();
	}
	
}
