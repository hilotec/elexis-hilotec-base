/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    
 *    $Id$
 *******************************************************************************/

package ch.elexis;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.util.Extensions;
import ch.elexis.data.Anwender;
import ch.elexis.data.Query;
import ch.elexis.util.SWTHelper;

public class LoginDialog extends TitleAreaDialog {
	Text usr, pwd;
	boolean hasUsers;
	ButtonEnabler be = new ButtonEnabler();
	
	public LoginDialog(Shell parentShell){
		super(parentShell);
		
		Query<Anwender> qbe = new Query<Anwender>(Anwender.class);
		List<Anwender> list = qbe.execute();
		hasUsers = (list.size() > 1);
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(2, false));
		Label lu = new Label(ret, SWT.NONE);
		
		lu.setText(Messages.LoginDialog_0);
		usr = new Text(ret, SWT.BORDER);
		usr.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.LoginDialog_1);
		pwd = new Text(ret, SWT.BORDER | SWT.PASSWORD);
		pwd.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		if (hasUsers == false) {
			usr.setText("Administrator"); //$NON-NLS-1$
			pwd.setText("admin"); //$NON-NLS-1$
		}
		// usr.addModifyListener(be);
		// pwd.addModifyListener(be);
		/*
		List<IConfigurationElement> newsModules = Extensions.getExtensions("ch.elexis.LoginNews");
		for(IConfigurationElement ice:newsModules){
			System.out.println(ice.getAttribute("name"));
			System.out.println(ice.getAttribute("class"));
		}
		*/
		List newsModules=Extensions.getClasses("ch.elexis.LoginNews", "class");
		
		if(newsModules.size()>0){
			Composite cNews=new Composite(ret,SWT.NONE);
			cNews.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
			cNews.setLayout(new GridLayout());
			for(ILoginNews lm:(List<ILoginNews>)newsModules){
				Composite comp=lm.getComposite(cNews);
				comp.setLayoutData(SWTHelper.getFillGridData());
			}
		}
		
		return ret;
	}
	
	@Override
	protected void okPressed(){
		if (Anwender.login(usr.getText(), pwd.getText()) == true) {
			super.okPressed();
		} else {
			setMessage(Messages.LoginDialog_4, IMessageProvider.ERROR);
			// getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
	}
	
	@Override
	protected void cancelPressed(){
		Hub.actUser = null;
		Hub.actMandant = null;
		Hub.mainActions.adaptForUser();
		super.cancelPressed();
	}
	
	@Override
	public void create(){
		super.create();
		getButton(IDialogConstants.OK_ID).setText(Messages.LoginDialog_login);
		getButton(IDialogConstants.CANCEL_ID).setText(Messages.LoginDialog_terminate);
		// getButton(IDialogConstants.OK_ID).setEnabled(false); 
		
		
	}
	
	class ButtonEnabler implements ModifyListener {
		
		@Override
		public void modifyText(ModifyEvent e){
			if (usr.getText().length() == 0 || pwd.getText().length() == 0) {
				// getButton(IDialogConstants.OK_ID).setEnabled(false);
			} else {
				// getButton(IDialogConstants.OK_ID).setEnabled(true);
			}
			
		}
		
	}

	
}
