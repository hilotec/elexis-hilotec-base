/*******************************************************************************
 * Copyright (c) 2007-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: KonsZumVerrechnenWizardDialog.java 4778 2008-12-08 17:04:45Z rgw_ch $
 *******************************************************************************/
package ch.elexis.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.Hub;
import ch.elexis.util.DayDateCombo;
import ch.elexis.util.MoneyInput;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class KonsZumVerrechnenWizardDialog extends TitleAreaDialog {
	private static final String CONFIG = "dialogs/konszumverrechnen/";
	private static final String ALLMARKED = "Alle Fälle vorschlagen, die zum Abrechnen vorgemerkt sind und ausserdem:";
	private static final String TAGEN_BZW_DEM = "Tagen bzw. dem";
	private static final String RECHNUNGEN_ERSTELLEN = "Rechnungen erstellen";
	private static final String BEHANDLUNGEN_ZUM_VERRECHNEN_AUTOMATISCH_AUSWAEHLEN = "Verrechnungsvorschlag erstellen";
	private static final String RECHNUNGS_AUTOMATIK = "Rechnungs-Automatik";
	private static final String TREATMENT_TRIMESTER = "Alle Behandlungen des vergangenen Quartals vorschlagen";
	private static final String TREATMENT_AMOUNTHIGHER = "Alle Behandlungsserien vorschlagen, deren Betrag höher ist als:";
	private static final String TREATMENTENDBEFORE = "Alle Behandlungsserien vorschlagen, die geendet haben vor:";
	private final static String TREATMENTBEGINBEFORE = "Alle Behandlungsserien vorschlagen, welche angefangen haben vor:";

	private static final String SKIPSELECTION = "Alle  so ermittelten Rechnungen direkt erstellen (Vorschlag überspringen).";
	private static final String CFG_SKIP=CONFIG+"skipselection";
	
	Button cbMarked, cbBefore, cbAmount, cbTime, cbQuartal, cbSkip;
	//DatePickerCombo dp1, dp2;
	//Spinner sp1, sp2;
	MoneyInput mi1;
	DayDateCombo ddc1,ddc2;

	public TimeTool ttFirstBefore, ttLastBefore;
	public Money mAmount;
	public boolean bQuartal, bMarked, bSkip;

	public KonsZumVerrechnenWizardDialog(final Shell parentShell) {
		super(parentShell);

	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(4, false));
		cbMarked = new Button(ret, SWT.CHECK);
		cbMarked.setText(ALLMARKED);
		cbMarked.setLayoutData(SWTHelper.getFillGridData(4, true, 1, false));
		cbMarked.setSelection(true);
		cbBefore = new Button(ret, SWT.CHECK);
		cbBefore.setText(TREATMENTBEGINBEFORE);
				ddc1=new DayDateCombo(ret,"",TAGEN_BZW_DEM);
		cbTime = new Button(ret, SWT.CHECK);
		cbTime.setText(TREATMENTENDBEFORE);

		ddc2=new DayDateCombo(ret, "",TAGEN_BZW_DEM);
		int prev = Hub.localCfg.get(CONFIG + "beginBefore", 30)*-1;
		TimeTool ttNow=new TimeTool();
		ttNow.addDays(prev);
		ddc1.setDays(prev);
		
		prev = Hub.localCfg.get(CONFIG + "endBefore", 20);
		ddc2.setDays(prev);
		ddc1.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));

		
		ddc2.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		cbAmount = new Button(ret, SWT.CHECK);
		cbAmount.setText(TREATMENT_AMOUNTHIGHER);
		mi1 = new MoneyInput(ret);
		mi1.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		new Label(ret, SWT.NONE);
		cbQuartal = new Button(ret, SWT.CHECK);
		cbQuartal.setText(TREATMENT_TRIMESTER);
		new Label(ret, SWT.NONE);
		new Label(ret, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(SWTHelper
				.getFillGridData(4, true, 1, false));
		cbSkip = new Button(ret, SWT.CHECK);
		cbSkip.setText(SKIPSELECTION);
		cbSkip.setSelection(Hub.globalCfg .get(CFG_SKIP, false));
		cbBefore.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e){
				ddc1.setEnabled(cbBefore.getSelection());
			}
			
		});
		cbTime.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e){
				ddc2.setEnabled(cbTime.getSelection());
			}
			
		});
		ddc1.setEnabled(false);
		ddc2.setEnabled(false);
		return ret;
	}

	@Override
	public void create() {
		super.create();
		setTitle(RECHNUNGS_AUTOMATIK);
		setMessage(BEHANDLUNGEN_ZUM_VERRECHNEN_AUTOMATISCH_AUSWAEHLEN);
		getShell().setText(RECHNUNGEN_ERSTELLEN);
	}

	@Override
	protected void okPressed() {

		if (cbBefore.getSelection()) {
			ttFirstBefore = ddc1.getDate();
		}
		if (cbTime.getSelection()) {
			ttLastBefore = ddc2.getDate();
		}
		if (cbAmount.getSelection()) {
			mAmount = mi1.getMoney(false);
		}
		bQuartal = cbQuartal.getSelection();
		bMarked = cbMarked.getSelection();
		bSkip = cbSkip.getSelection();
		Hub.globalCfg.set(CFG_SKIP, bSkip);
		super.okPressed();
	}

}
