package ch.sgam.informatics.exchange.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

import com.valhalla.misc.GnuPG;

public class GenerateKeyDialog extends TitleAreaDialog {

	Text tName, tMail, tPwd, tPwdContr, tBem;
	boolean bOK;
	public GenerateKeyDialog(Shell parentShell) {
		super(parentShell);
		
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		new Label(ret,SWT.NONE).setText(Messages.GenerateKeyDialog_nameForThisKey);
		tName=new Text(ret,SWT.BORDER);
		tName.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText(Messages.GenerateKeyDialog_mailFGorThisKey);
		tMail=new Text(ret,SWT.BORDER);
		tMail.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText(Messages.GenerateKeyDialog_passForKey);
		tPwd=new Text(ret,SWT.BORDER|SWT.PASSWORD);
		tPwd.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText(Messages.GenerateKeyDialog_repeatPass);
		tPwdContr=new Text(ret,SWT.BORDER|SWT.PASSWORD);
		tPwdContr.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.NONE).setText(Messages.GenerateKeyDialog_remark);
		tBem=new Text(ret,SWT.BORDER);
		tBem.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret,SWT.WRAP).setText("Nach Klick auf OK beginnt die Generierung des Schlüssels.\nDies kann bis zu ein oder zwei Minuten dauern.");
		return ret;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText(Messages.GenerateKeyDialog_createKeyPair);
		setTitle(Messages.GenerateKeyDialog_createNewKeyPair);
		setMessage(Messages.GenerateKeyDialog_thiscreatesNewPair);
		setTitleImage(Desk.theImageRegistry.get(Desk.IMG_LOGO48));
	}

	@Override
	protected void okPressed() {
		final GnuPG gpg=new GnuPG();
		final String pwd=tPwd.getText();
		if(!pwd.equals(tPwdContr.getText())){
			SWTHelper.showError(Messages.GenerateKeyDialog_passphrasesNotIdentical, Messages.GenerateKeyDialog_passPhrasenotSame);
		}else{
			final String name=tName.getText();
			final String mail=tMail.getText();
			final String bem=tBem.getText();
			if((pwd.length()==0) || (name.length()==0) || (mail.length()==0)){
				SWTHelper.showError(Messages.GenerateKeyDialog_pleaseEnterAll, Messages.GenerateKeyDialog_nameAddressAndPass);
			}else{
				if(!StringTool.isMailAddress(mail)){
					SWTHelper.showError("Ungültige Mail-Adresse", "Die angegebene Mail-Adresse ist nicht gültig. Bitte korrigieren");
				}else{
					try {
						Hub.plugin.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress(){
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								if(gpg.generateKey(name, mail, pwd.toCharArray(), bem)==false){
									SWTHelper.showError(Messages.GenerateKeyDialog_errorCreating, gpg.getErrorString());
									bOK=false;
								}else{
									bOK=true;
								}
							}
						});
					} catch (Exception e) {
						ExHandler.handle(e);
					} 
					if(bOK){
						super.okPressed();
					}
				}
			}
		}

	}
	

}
