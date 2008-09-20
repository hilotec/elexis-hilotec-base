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
 *  $Id: KonsZumVerrechnenWizardDialog.java 4422 2008-09-20 09:03:09Z rgw_ch $
 *******************************************************************************/
package ch.elexis.dialogs;

import java.util.Date;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import ch.elexis.Hub;
import ch.elexis.util.Money;
import ch.elexis.util.MoneyInput;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

import com.tiff.common.ui.datepicker.DatePickerCombo;

public class KonsZumVerrechnenWizardDialog extends TitleAreaDialog {
	private static final String CONFIG = "dialogs/konszumverrechnen/";
	private static final String ALLMARKED =
		"Alle Fälle vorschlagen, die zum Abrechnen vorgemerkt sind und ausserdem:";
	private static final String TAGEN_BZW_DEM = "Tagen bzw. dem";
	private static final String RECHNUNGEN_ERSTELLEN = "Rechnungen erstellen";
	private static final String BEHANDLUNGEN_ZUM_VERRECHNEN_AUTOMATISCH_AUSWÄHLEN =
		"Verrechnungsvorschlag erstellen";
	private static final String RECHNUNGS_AUTOMATIK = "Rechnungs-Automatik";
	private static final String TREATMENT_TRIMESTER =
		"Alle Behandlungen des vergangenen Quartals vorschlagen";
	private static final String TREATMENT_AMOUNTHIGHER =
		"Alle Behandlungsserien vorschlagen, deren Betrag höher ist als:";
	private static final String TREATMENTENDBEFORE =
		"Alle Behandlungsserien vorschlagen, die geendet haben vor:";
	private final static String TREATMENTBEGINBEFORE =
		"Alle Behandlungsserien vorschlagen, welche angefangen haben vor:";
	
	Button cbMarked, cbBefore, cbAmount, cbTime, cbQuartal;
	DatePickerCombo dp1, dp2;
	Spinner sp1, sp2;
	MoneyInput mi1;
	
	public TimeTool ttFirstBefore, ttLastBefore;
	public Money mAmount;
	public boolean bQuartal, bMarked;
	
	public KonsZumVerrechnenWizardDialog(final Shell parentShell){
		super(parentShell);
		
	}
	
	@Override
	protected Control createDialogArea(final Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(4, false));
		cbMarked = new Button(ret, SWT.CHECK);
		cbMarked.setText(ALLMARKED);
		cbMarked.setLayoutData(SWTHelper.getFillGridData(4, true, 1, false));
		cbMarked.setSelection(true);
		cbBefore = new Button(ret, SWT.CHECK);
		cbBefore.setText(TREATMENTBEGINBEFORE);
		sp1 = new Spinner(ret, SWT.NONE);
		int prev = Hub.localCfg.get(CONFIG + "beginBefore", 30);
		sp1.setValues(prev, 0, 365, 0, 1, 10);
		sp1.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e){
				int days = sp1.getSelection();
				TimeTool tt = new TimeTool();
				tt.add(TimeTool.DAY_OF_YEAR, -1 * days);
				dp1.setDate(tt.getTime());
				Hub.localCfg.set(CONFIG + "beginBefore", (int) days);
			}
			
		});
		new Label(ret, SWT.NONE).setText(TAGEN_BZW_DEM);
		dp1 = new DatePickerCombo(ret, SWT.NONE);
		dp1.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		((GridData) dp1.getLayoutData()).widthHint = 50;
		Date d = new Date();
		d.setTime(d.getTime() - prev * 86400);
		dp1.setDate(d);
		
		cbTime = new Button(ret, SWT.CHECK);
		cbTime.setText(TREATMENTENDBEFORE);
		prev = Hub.localCfg.get(CONFIG + "endBefore", 20);
		sp2 = new Spinner(ret, SWT.NONE);
		sp2.setValues(prev, 0, 365, 0, 1, 10);
		sp2.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e){
				int days = sp2.getSelection();
				TimeTool tt = new TimeTool();
				tt.add(TimeTool.DAY_OF_YEAR, -1 * days);
				dp2.setDate(tt.getTime());
				Hub.localCfg.set(CONFIG + "endBefore", (int) days);
			}
			
		});
		new Label(ret, SWT.NONE).setText(TAGEN_BZW_DEM);
		dp2 = new DatePickerCombo(ret, SWT.NONE);
		dp2.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		cbAmount = new Button(ret, SWT.CHECK);
		cbAmount.setText(TREATMENT_AMOUNTHIGHER);
		mi1 = new MoneyInput(ret);
		mi1.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		new Label(ret, SWT.NONE);
		cbQuartal = new Button(ret, SWT.CHECK);
		cbQuartal.setText(TREATMENT_TRIMESTER);
		new Label(ret, SWT.NONE);
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle(RECHNUNGS_AUTOMATIK);
		setMessage(BEHANDLUNGEN_ZUM_VERRECHNEN_AUTOMATISCH_AUSWÄHLEN);
		getShell().setText(RECHNUNGEN_ERSTELLEN);
	}
	
	@Override
	protected void okPressed(){
		
		if (cbBefore.getSelection()) {
			ttFirstBefore = new TimeTool(dp1.getDate().getTime());
		}
		if (cbTime.getSelection()) {
			ttLastBefore = new TimeTool(dp2.getDate().getTime());
		}
		if (cbAmount.getSelection()) {
			mAmount = mi1.getMoney(false);
		}
		bQuartal = cbQuartal.getSelection();
		bMarked = cbMarked.getSelection();
		super.okPressed();
	}
	
}
