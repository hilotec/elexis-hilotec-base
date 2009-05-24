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
 *  $Id: DBConnectFirstPage.java 5317 2009-05-24 15:00:37Z rgw_ch $
 *******************************************************************************/
package ch.elexis.wizards;
// 17.5.2009: added H2

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.preferences.SettingsPreferenceStore;
import ch.rgw.tools.JdbcLink;

public class DBConnectFirstPage extends WizardPage {
	
	Combo dbTypes;
	Text server, dbName;
	String defaultUser, defaultPassword;
	JdbcLink j = null;
	
	static final String[] supportedDB = new String[] {
		"mySQl", "PostgreSQL", "hsqlDB (inProc)", "hsqlDB (Server)", "H2"
	};
	
	public DBConnectFirstPage(String pageName){
		super(
			Messages.getString("DBConnectFirstPage.Connection"), Messages.getString("DBConnectFirstPage.typeOfDB"), Hub.getImageDescriptor("rsc/elexis48.png")); //$NON-NLS-1$ //$NON-NLS-2$
		setMessage(Messages.getString("DBConnectFirstPage.selectType")); //$NON-NLS-1$
		setDescription(Messages.getString("DBConnectFirstPage.theDescripotion")); //$NON-NLS-1$
		
	}
	
	public DBConnectFirstPage(String pageName, String title, ImageDescriptor titleImage){
		super(pageName, title, titleImage);
		// TODO Automatisch erstellter Konstruktoren-Stub
	}
	
	public void createControl(Composite parent){
		FormToolkit tk = Desk.getToolkit();
		Form form = tk.createForm(parent);
		form.setText(Messages.getString("DBConnectFirstPage.connectioNDetails")); //$NON-NLS-1$
		Composite body = form.getBody();
		body.setLayout(new TableWrapLayout());
		FormText alt = tk.createFormText(body, false);
		StringBuilder old = new StringBuilder();
		old.append("<form>Aktuelle Verbindung:<br/>"); //$NON-NLS-1$
		IPreferenceStore localstore = new SettingsPreferenceStore(Hub.localCfg);
		String driver = localstore.getString(PreferenceConstants.DB_CLASS);
		String connectstring = localstore.getString(PreferenceConstants.DB_CONNECT);
		String user = localstore.getString(PreferenceConstants.DB_USERNAME);
		// String pwd=localstore.getString(PreferenceConstants.DB_PWD);
		String typ = localstore.getString(PreferenceConstants.DB_TYP);
		if (ch.rgw.tools.StringTool.isNothing(connectstring)) {
			old.append("Keine.</form>"); //$NON-NLS-1$
		} else {
			old.append("<li><b>Typ:</b>       ").append(typ).append("</li>"); //$NON-NLS-1$ //$NON-NLS-2$
			old.append("<li><b>Treiber</b>    ").append(driver).append("</li>"); //$NON-NLS-1$ //$NON-NLS-2$
			old.append("<li><b>Verbinde</b>   ").append(connectstring).append("</li>"); //$NON-NLS-1$ //$NON-NLS-2$
			old.append("<li><b>Username</b>   ").append(user).append("</li>"); //$NON-NLS-1$ //$NON-NLS-2$
			old.append("</form>"); //$NON-NLS-1$
		}
		alt.setText(old.toString(), true, false);
		// Composite form=new Composite(parent, SWT.BORDER);
		Label sep = tk.createSeparator(body, SWT.NONE);
		TableWrapData twd = new TableWrapData();
		twd.heightHint = 5;
		sep.setLayoutData(twd);
		tk.createLabel(body, Messages.getString("DBConnectFirstPage.enterType")); //$NON-NLS-1$
		dbTypes = new Combo(body, SWT.BORDER|SWT.SIMPLE);
		dbTypes.setItems(supportedDB);
		dbTypes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				int it = dbTypes.getSelectionIndex();
				switch (it) {
				case 0:
				case 1:
					server.setEnabled(true);
					dbName.setEnabled(true);
					defaultUser = "elexis";
					defaultPassword = "elexisTest";
					break;
				case 2:
					server.setEnabled(false);
					dbName.setEnabled(true);
					defaultUser = "sa";
					defaultPassword = "";
					break;
				case 3:
					server.setEnabled(true);
					dbName.setEnabled(false);
					defaultUser = "sa";
					defaultPassword = "";
					break;
				case 4:
					server.setEnabled(false);
					dbName.setEnabled(true);
					defaultUser="sa";
					defaultPassword ="";
				default:
					break;
				}
				DBConnectSecondPage sec = (DBConnectSecondPage) getNextPage();
				sec.name.setText(defaultUser);
				sec.pwd.setText(defaultPassword);
				
			}
			
		});
		tk.adapt(dbTypes, true, true);
		tk.createLabel(body, Messages.getString("DBConnectFirstPage.serevrAddress")); //$NON-NLS-1$
		server = tk.createText(body, "", SWT.BORDER);
		TableWrapData twr = new TableWrapData(TableWrapData.FILL_GRAB);
		server.setLayoutData(twr);
		tk.createLabel(body, Messages.getString("DBConnectFirstPage.databaseName")); //$NON-NLS-1$
		dbName = tk.createText(body, "", SWT.BORDER);
		TableWrapData twr2 = new TableWrapData(TableWrapData.FILL_GRAB);
		dbName.setLayoutData(twr2);
		setControl(form);
	}
	
}
