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
 *  $Id: KGExportDialog.java 1153 2006-10-22 19:10:08Z rgw_ch $
 *******************************************************************************/

package ch.elexis.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import ch.elexis.data.Patient;
import ch.elexis.exchange.Container;
import ch.elexis.util.Extensions;
import ch.elexis.util.SWTHelper;

public class KGExportDialog extends TitleAreaDialog {
	private Patient pat;
	private Container cnt;
	
	private Button[] transporters;
	List<Container> list;
	
	public KGExportDialog(Shell parentShell, Patient pat) {
		super(parentShell);
		this.pat=pat;
		
		
	}
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret=new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		list=Extensions.getClasses("ch.elexis.SecureTransport", "Transporter");
		ret.setLayout(new GridLayout());
		if(list.isEmpty()){
			new Label(ret,SWT.NONE).setText(Messages.getString("KGExportDialog.noSecureTransportFound")); //$NON-NLS-1$
		}else{
			transporters=new Button[list.size()];
			for(int i=0;i<list.size();i++){
				transporters[i]=new Button(ret,SWT.RADIO);
				transporters[i].setText(list.get(i).getDescription());
			}
		}
		return ret;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.getString("KGExportDialog.3")+pat.getLabel()+Messages.getString("KGExportDialog.4")); //$NON-NLS-1$ //$NON-NLS-2$
		setMessage(Messages.getString("KGExportDialog.pleaseEnterFile")); //$NON-NLS-1$
		getShell().setText(Messages.getString("KGExportDialog.EMRExport")); //$NON-NLS-1$
	}

	@Override
	protected void okPressed() {
		if(transporters!=null){
			for(int i=0;i<transporters.length;i++){
				if(transporters[i].getSelection()){
					cnt=list.get(i);
				}
			}
		}
		super.okPressed();
	}

	public Container getResult(){
		return cnt;
	}
}
