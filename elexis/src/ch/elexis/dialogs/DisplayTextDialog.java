/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: DisplayTextDialog.java 4634 2008-10-27 05:22:55Z rgw_ch $
 *******************************************************************************/

package ch.elexis.dialogs;

import java.awt.FlowLayout;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.util.SWTHelper;

public class DisplayTextDialog extends TitleAreaDialog {
	String t,m,cnt;
	
	public DisplayTextDialog(Shell parentShell, String title, String message, String content) {
		super(parentShell);
		t=title;
		m=message;
		cnt=content;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		ScrolledForm form=Desk.getToolkit().createScrolledForm(parent);
		form.getBody().setLayout(new ColumnLayout());
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Control ret=null;
		if(cnt.startsWith("<html>")){
			ret=new Browser(form.getBody(),SWT.NONE);
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			((Browser)ret).setText(cnt);
			if(cnt.length()>300){
				getShell().setSize(800, 600);
			}

		}else{
			cnt=cnt.replaceAll("<","&lt;");
			cnt=cnt.replaceAll(">","&gt;");
			cnt=cnt.replaceAll("\n","<br />");
			cnt=cnt.replaceAll("\\*\\.(.{1,30})\\.\\*", "<b>$1</b>");
			cnt=cnt.replaceAll("\\\\\\.br\\\\", "<br/>");
			cnt=cnt.replaceAll("\\\\\\.BR\\\\", "<br/>");
			cnt=cnt.replaceAll("\\n\\n", "\\n");

			ret=Desk.getToolkit().createFormText(form.getBody(),false);
			//ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			((FormText)ret).setText("<form><p>"+cnt+"</p></form>", true, true);
		}
		SWTHelper.center(Desk.getTopShell(),getShell());
		return ret;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText(t);
		setTitle(GlobalEvents.getSelectedPatient().getLabel());
		setMessage(m);
		setTitleImage(Desk.getImage(Desk.IMG_LOGO48));
		Rectangle screen=Desk.getDisplay().getBounds();
		int w=screen.width-screen.width/4;
		int h=screen.width-screen.width/4;
		getShell().setBounds(0, 0, w, h);
		SWTHelper.center(getShell());
	}	
	

}
