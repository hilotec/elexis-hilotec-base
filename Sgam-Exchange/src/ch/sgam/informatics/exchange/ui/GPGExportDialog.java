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
 *  $Id: GPGExportDialog.java 1277 2006-11-13 21:02:11Z rgw_ch $
 *******************************************************************************/
package ch.sgam.informatics.exchange.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;

import javax.mail.Message;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ch.elexis.Hub;
import ch.elexis.data.Kontakt;
import ch.elexis.dialogs.KontaktSelektor;
import ch.elexis.mail.Mailer;
import ch.elexis.util.Result;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.sgam.informatics.exchange.preferences.PreferenceConstants;

import com.valhalla.misc.GnuPG;

public class GPGExportDialog extends TitleAreaDialog {
	private Label lbExportfile, lbAdressat;
	private Button bSave, bSend;
	private Kontakt kAdressat;
	private String gpgIdent;
	private GnuPG gpg=new GnuPG();
	private String toEncrypt;
	private File iFile;
	private Text tMailadr;
	private Text tPasswd;
	private String bname;
	
	public GPGExportDialog(Shell parentShell, String data, String basename) {
		super(parentShell);
		toEncrypt=data;
		bname=basename;
	}
	
	public GPGExportDialog(Shell parentShell, File file, String basename){
		super(parentShell);
		bname=basename;
		iFile=file;
		
	}
	public GPGExportDialog(Shell parent, byte[] data, String basename) throws UnsupportedEncodingException{
		this(parent,new String(data,"utf-8"),basename); //$NON-NLS-1$
	}
	@Override
	protected Control createDialogArea(Composite parent) {

		Composite ret=new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(2,false));
		new Label(ret,SWT.NONE).setText(Messages.GPGExportDialog_receiver);
		lbAdressat=SWTHelper.createHyperlink(ret, Messages.GPGExportDialog_pleasselect, new HyperlinkAdapter(){

			@Override
			public void linkActivated(HyperlinkEvent e) {
				KontaktSelektor ksl=new KontaktSelektor(getShell(),Kontakt.class,Messages.GPGExportDialog_adressee,Messages.GPGExportDialog_pleaseEnterAdressee);
				if(ksl.open()==Dialog.OK){
					kAdressat=(Kontakt)ksl.getSelection();
					gpgIdent=kAdressat.get(Messages.GPGExportDialog_mail);
					if(StringTool.isNothing(gpgIdent)){
						gpgIdent=kAdressat.get("Bezeichnung2")+" "+kAdressat.get("Bezeichnung1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
					if(!gpg.listKeys(gpgIdent)){
						SWTHelper.showError(Messages.GPGExportDialog_noKeyFound, Messages.GPGExportDialog_noKeyFoundForThis);
						lbAdressat.setText(Messages.GPGExportDialog_pleaseSelect);
					}else{
						lbAdressat.setText(kAdressat.getLabel());
						tMailadr.setText(StringTool.unNull(kAdressat.get(Messages.GPGExportDialog_12)));
					}
				}else{
					lbAdressat.setText(Messages.GPGExportDialog_pleaseSelect);
				}
			}

			
		});
		lbAdressat.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText(Messages.GPGExportDialog_signPasswd);
		tPasswd=new Text(ret,SWT.PASSWORD|SWT.BORDER);
		tPasswd.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		bSend=new Button(ret,SWT.RADIO);
		bSend.setText(Messages.GPGExportDialog_sendDirect);
		tMailadr=new Text(ret,SWT.BORDER);
		tMailadr.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bSave=new Button(ret,SWT.RADIO);
		bSave.setText(Messages.GPGExportDialog_saveEncrypted);
		lbExportfile=SWTHelper.createHyperlink(ret, Messages.GPGExportDialog_pleaseSelect, new HyperlinkAdapter(){
	
			@Override
			public void linkActivated(HyperlinkEvent e) {
				String file=new FileDialog(getShell(),SWT.SAVE).open();
				if(file!=null){
					lbExportfile.setText(file);
				}else{
					lbExportfile.setText(Messages.GPGExportDialog_pleaseSelect);
				}
			}
			
		});
		lbExportfile.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		return ret;
	
	}
	@Override
	public void create() {
		super.create();
		setTitle(Messages.GPGExportDialog_encrypt);
		setMessage(Messages.GPGExportDialog_enterAdressee);
		getShell().setText(Messages.GPGExportDialog_encryptExport);
	}
	@Override
	protected void okPressed() {

		String result=null;
		String senderKey=Hub.localCfg.get("exchange/"+PreferenceConstants.EXCH_KEYNAME, null); //$NON-NLS-1$
		boolean gpgResult;
		if(iFile!=null){
			gpgResult=gpg.signAndEncrypt(iFile, senderKey, gpgIdent, tPasswd.getText());
		}else{
			gpgResult=gpg.signAndEncrypt(toEncrypt, senderKey, gpgIdent,tPasswd.getText());
		}
		if(gpgResult){
			 result=gpg.getResult();
			 if(bSave.getSelection()==true){ 
				try{
					FileWriter fw=new FileWriter(lbExportfile.getText());
					fw.write(result);
					fw.close();
					MessageDialog.openInformation(getShell(), Messages.GPGExportDialog_success, Messages.GPGExportDialog_encryptedSuccess);
					
				}catch(Exception ex){
					SWTHelper.showError(Messages.GPGExportDialog_errorWriting, Messages.GPGExportDialog_26+lbExportfile.getText()+Messages.GPGExportDialog_27+ex.getMessage()+Messages.GPGExportDialog_28);
				}
			}else{
				SendMailDialog smd=new SendMailDialog(getShell());
				if(smd.open()==Dialog.OK){
					Mailer mailer=new Mailer();
					Message msg=mailer.createMultipartMessage(smd.subject, senderKey);
					try{
						mailer.addTextPart(msg,smd.text);
						mailer.addBinaryPart(msg, bname+".xChange", result.getBytes("utf-8")); //$NON-NLS-1$ //$NON-NLS-2$
					}catch(UnsupportedEncodingException ex){
						// will never happen
					}
					Result<String> r=mailer.send(msg, tMailadr.getText());
					if(r.isOK()){
						MessageDialog.openInformation(getShell(), Messages.GPGExportDialog_success, Messages.GPGExportDialog_successEncryptAndSend);
					}else{
						SWTHelper.showError(Messages.GPGExportDialog_errorSend, Messages.GPGExportDialog_couldntzSend+r.get());
					}
				}
			}
		}else{
			SWTHelper.showError(Messages.GPGExportDialog_errorEncrypt, Messages.GPGExportDialog_couldntEncrypt+gpg.getErrorString()+")"); //$NON-NLS-3$
		}
		super.okPressed();		

	}
	
}
