/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Sgam.informatics
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ExchangePreferences.java 1282 2006-11-14 17:00:03Z rgw_ch $
 *******************************************************************************/
package ch.sgam.informatics.exchange.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.preferences.SettingsPreferenceStore;
import ch.elexis.preferences.inputs.MultilineFieldEditor;
import ch.elexis.util.SWTHelper;
import ch.rgw.IO.Settings;
import ch.sgam.informatics.exchange.ui.GenerateKeyDialog;

public class ExchangePreferences extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	public static final String ID="ch.sgam.informatics.exchange.prefs"; //$NON-NLS-1$
	Settings cfg=Hub.localCfg.getBranch("exchange", true); //$NON-NLS-1$
	
	public ExchangePreferences() {
		super(GRID);
		if(cfg.get(PreferenceConstants.EXCH_REQUEST_SUBJECT, null)==null){
			cfg.set(PreferenceConstants.EXCH_REQUEST_SUBJECT, ch.sgam.informatics.exchange.ui.Messages.XChangeView_requestPublicKey);
		}
		if(cfg.get(PreferenceConstants.EXCH_REQUEST_TEXT, null)==null){
			cfg.set(PreferenceConstants.EXCH_REQUEST_TEXT, ch.sgam.informatics.exchange.ui.Messages.XChangeView_2);
		}
		if(cfg.get(PreferenceConstants.EXCH_REPLY_TEXT, null)==null){
			cfg.set(PreferenceConstants.EXCH_REPLY_TEXT, ch.sgam.informatics.exchange.ui.Messages.XChangeView_5);
		}
		if(cfg.get(PreferenceConstants.EXCH_REPLY_SUBJECT, null)==null){
			cfg.set(PreferenceConstants.EXCH_REPLY_SUBJECT, ch.sgam.informatics.exchange.ui.Messages.XChangeView_subjPublicKey);
		}

		setPreferenceStore(new SettingsPreferenceStore(cfg));
		setDescription(Messages.getString("ExchangePreferences.xchangeSettings")); //$NON-NLS-1$
	}


	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void createFieldEditors() {
		addField(new FileFieldEditor(PreferenceConstants.EXCH_GPG,Messages.getString("ExchangePreferences.gpgProgram"),getFieldEditorParent())); //$NON-NLS-1$
		addField(new DirectoryFieldEditor(PreferenceConstants.EXCH_DIRECTORY,Messages.getString("ExchangePreferences.directoryTransfer"),getFieldEditorParent())); //$NON-NLS-1$
		addField(new StringFieldEditor(PreferenceConstants.EXCH_REQUEST_SUBJECT,Messages.getString("ExchangePreferences.titleKeyRequests"),getFieldEditorParent())); //$NON-NLS-1$
		addField(new MultilineFieldEditor(PreferenceConstants.EXCH_REQUEST_TEXT,Messages.getString("ExchangePreferences.testKeyRequests"),getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.EXCH_REPLY_SUBJECT,Messages.getString("ExchangePreferences.titleAnswers"),getFieldEditorParent())); //$NON-NLS-1$
		addField(new MultilineFieldEditor(PreferenceConstants.EXCH_REPLY_TEXT,Messages.getString("ExchangePreferences.textAnswers"),getFieldEditorParent())); //$NON-NLS-1$
		addField(new StringFieldEditor(PreferenceConstants.EXCH_KEYNAME,Messages.getString("ExchangePreferences.sigKeyMail"),getFieldEditorParent())); //$NON-NLS-1$
		
		Button nKey=new Button(getFieldEditorParent(),SWT.PUSH);
		nKey.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				new GenerateKeyDialog(getShell()).open();
			}
		});
		nKey.setText(Messages.getString("ExchangePreferences.createNewPair")); //$NON-NLS-1$
		nKey.setLayoutData(SWTHelper.getFillGridData(2, false, 1, false));
	}


	@Override
	protected void performApply() {
		super.performApply();
		cfg.flush();
	}
	
	
}

