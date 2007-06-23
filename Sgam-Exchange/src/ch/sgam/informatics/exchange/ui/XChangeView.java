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
 *  $Id: Transporter.java 1143 2006-10-21 19:06:51Z rgw_ch $
 *******************************************************************************/
package ch.sgam.informatics.exchange.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.util.Result;
import ch.elexis.util.SWTHelper;
import ch.sgam.informatics.exchange.KeyRequester;
import ch.sgam.informatics.exchange.preferences.PreferenceConstants;

public class XChangeView extends ViewPart {
	Form top,bottom;
	FormToolkit tk=Desk.theToolkit;
	Text tAddress, tSubject, tMessage;
	Button bAsk,bAnswer;
	Button bSend, bImportFile,bImportClipboard;
	KeyRequester kr;
	private static final String REQ_SUBJECT=Messages.XChangeView_requestPublicKey;
	private static final String REQ_TEXT=Messages.XChangeView_2;
	
	private static final String ANS_SUBJECT=Messages.XChangeView_subjPublicKey;
	private static final String ANS_TEXT=Messages.XChangeView_5;
	public XChangeView() {
		kr=new KeyRequester(Hub.localCfg.get("exchange/"+PreferenceConstants.EXCH_KEYNAME, null)); //$NON-NLS-1$
	}
	boolean mode=false;
	
	@Override
	public void createPartControl(final Composite parent) {
		Composite ret=new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout());
		top=tk.createForm(ret);
		bottom=tk.createForm(ret);
		top.setText(Messages.XChangeView_sendKey);
		top.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		top.getBody().setLayout(new GridLayout());
		Group group=new Group(top.getBody(),SWT.BORDER);
		group.setText("Modus");
		group.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		group.setLayout(new FillLayout());
		bAsk=tk.createButton(group, "Anfrage", SWT.RADIO);
		bAnswer=tk.createButton(group, "Antwort", SWT.RADIO);
		bAsk.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				tSubject.setText(REQ_SUBJECT);
				tMessage.setText(REQ_TEXT);
				bSend.setText(Messages.XChangeView_sendRequest);
				mode=false;
			}
			
		});
		bAnswer.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				tSubject.setText(ANS_SUBJECT);
				tMessage.setText(ANS_TEXT);
				bSend.setText(Messages.XChangeView_sendAnswer);
				mode=true;
			}
			
		});
		tk.adapt(group);
		tk.createLabel(top.getBody(), Messages.XChangeView_whereToSend);
		tAddress=SWTHelper.createText(tk, top.getBody(), 1, SWT.BORDER);
		tk.createLabel(top.getBody(),Messages.XChangeView_mailTitle);
		tSubject=SWTHelper.createText(tk,top.getBody(), 1, SWT.BORDER);
		tk.createLabel(top.getBody(),Messages.XChangeView_mailText);
		tMessage=SWTHelper.createText(tk,top.getBody(), 4, SWT.BORDER);
		bSend=tk.createButton(top.getBody(), Messages.XChangeView_sendRequest, SWT.PUSH);
		bSend.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				Result<String> res=kr.sendKey(tAddress.getText(), tSubject.getText(), tMessage.getText());
				if(res.isOK()){
					MessageDialog.openInformation(parent.getShell(), Messages.XChangeView_sentRequest, Messages.XChangeView_didSendRequest);
				}
			}
			
		});
		bottom.setText(Messages.XChangeView_importKey);
		bottom.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		bottom.getBody().setLayout(new GridLayout(2,true));
		Label exp=tk.createLabel(bottom.getBody(), "Sie können einen Schlüssel entweder aus einer Datei oder aus der Zwischenablage importieren",SWT.WRAP);
		
		exp.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		bImportFile=tk.createButton(bottom.getBody(), Messages.XChangeView_resdKey, SWT.PUSH);
		//tk.createLabel(bottom.getBody(), Messages.XChangeView_pleaseCopyKey, SWT.WRAP);
		bImportFile.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd=new FileDialog(parent.getShell(),SWT.OPEN);
				String fname=fd.open();
				if(fname!=null){
					Result<String> res=kr.importKey(fname);
					if(!res.isOK()){
						SWTHelper.showError(Messages.XChangeView_errorReading, Messages.XChangeView_couldNotImport+res.toString());
					}else{
						MessageDialog.openInformation(parent.getShell(), Messages.XChangeView_importSuccess, Messages.XChangeView_keyIsImported+res.get());
					}
				}
			}
			
		});
		bImportClipboard=tk.createButton(bottom.getBody(),"Import aus Zwischenablage", SWT.PUSH);
		bImportClipboard.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				Result<String> res=kr.importKeyFromClipboard();
				if(!res.isOK()){
					SWTHelper.showError(Messages.XChangeView_errorReading, Messages.XChangeView_couldNotImport+res.toString());
				}else{
					MessageDialog.openInformation(parent.getShell(), Messages.XChangeView_importSuccess, Messages.XChangeView_keyIsImported+res.get());
				}
			}
			
		});
		tSubject.setText(REQ_SUBJECT);
		tMessage.setText(REQ_TEXT);
		bAsk.setSelection(true);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
