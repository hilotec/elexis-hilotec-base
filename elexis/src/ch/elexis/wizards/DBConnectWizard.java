/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: DBConnectWizard.java 5313 2009-05-17 17:07:36Z rgw_ch $
 *******************************************************************************/

package ch.elexis.wizards;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.Wizard;

import ch.elexis.Hub;
import ch.elexis.data.PersistentObject;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.preferences.SettingsPreferenceStore;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.JdbcLink;

public class DBConnectWizard extends Wizard {
	DBConnectFirstPage first=new DBConnectFirstPage(Messages.getString("DBConnectWizard.typeOfDB")); //$NON-NLS-1$
	DBConnectSecondPage sec=new DBConnectSecondPage(Messages.getString("DBConnectWizard.Credentials")); //$NON-NLS-1$
	public DBConnectWizard() {
		super();
		setWindowTitle(Messages.getString("DBConnectWizard.connectDB")); //$NON-NLS-1$
	}

	
	@Override
	public void addPages() {
		addPage(first);
		addPage(sec);
	}


	@Override
	public boolean performFinish() {
		int ti=first.dbTypes.getSelectionIndex();
		String server=first.server.getText();
		String db=first.dbName.getText();
		String user=sec.name.getText();
		String pwd=sec.pwd.getText();
		JdbcLink j=null;
		switch (ti) {
		case 0: j=JdbcLink.createMySqlLink(server,db);	break;
		case 1:	j=JdbcLink.createPostgreSQLLink(server,db); break;
		case 2: j=JdbcLink.createInProcHsqlDBLink(db); break;
		case 4: j=JdbcLink.createH2Link(db); break;
		default:
			j=null;
			return false;
		}
		if(j.connect(user,pwd)==true){
			IPreferenceStore localstore = new SettingsPreferenceStore(Hub.localCfg);
			localstore.setValue(PreferenceConstants.DB_CLASS,j.getDriverName());
		    localstore.setValue(PreferenceConstants.DB_CONNECT,j.getConnectString());
		    localstore.setValue(PreferenceConstants.DB_USERNAME,user);
		    localstore.setValue(PreferenceConstants.DB_PWD,pwd);
		    localstore.setValue(PreferenceConstants.DB_TYP,first.dbTypes.getItem(ti));
		    Hub.localCfg.flush();
			return PersistentObject.connect(j);
		}
		else{
			SWTHelper.alert(Messages.getString("DBConnectWizard.couldntConnect"),j.lastErrorString); //$NON-NLS-1$
			return false;
		}
			
	}

}
