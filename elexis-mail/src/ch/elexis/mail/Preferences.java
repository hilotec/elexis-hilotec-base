/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Preferences.java 1625 2007-01-19 20:01:59Z rgw_ch $
 *******************************************************************************/

package ch.elexis.mail;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.preferences.SettingsPreferenceStore;


public class Preferences extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public Preferences() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(Hub.localCfg));
		setDescription(Messages.Preferences_MailSendSettings);
	}
	
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void createFieldEditors() {
		addField(
				new StringFieldEditor(PreferenceConstants.MAIL_SMTP, 
						Messages.Preferences_SMTPServer, getFieldEditorParent()));
		
		addField(
				new StringFieldEditor(PreferenceConstants.MAIL_SENDER, 
						Messages.Preferences_SenderEMail, getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(PreferenceConstants.MAIL_SEND_QFA,
				  Messages.Preferences_SendErrorMsg, getFieldEditorParent()));
		
		addField(
				new StringFieldEditor(PreferenceConstants.MAIL_QFA_ADDRESS, 
						Messages.Preferences_ErrorMailAdress, getFieldEditorParent()));
		addField(
				new FileFieldEditor(PreferenceConstants.MAIL_ELEXIS_LOG,
						Messages.Preferences_ElexisLogfile, getFieldEditorParent()));
		
		addField(new FileFieldEditor(PreferenceConstants.MAIL_ECLIPSE_LOG,
				Messages.Preferences_EclipseLogfile, getFieldEditorParent()));
		
	}

}
