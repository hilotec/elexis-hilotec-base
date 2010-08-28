package elexis_db_shaker.actions;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class SettingsDialog extends Dialog {
	Button bNames;
	Button bKons;
	boolean replaceNames;
	boolean replaceKons;
	
	protected SettingsDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=(Composite)super.createDialogArea(parent);
		bNames=new Button(ret,SWT.CHECK);
		bNames.setText("Namen durch echt wirkende Pseudos ersetzen");
		bKons=new Button(ret,SWT.CHECK);
		bKons.setText("Auch Konsultationstexte überschreiben");
		return ret;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText("Datenbank-Anonymisierer");
	}

	@Override
	protected void okPressed() {
		replaceNames=bNames.getSelection();
		replaceKons=bKons.getSelection();
		super.okPressed();
	}

}
