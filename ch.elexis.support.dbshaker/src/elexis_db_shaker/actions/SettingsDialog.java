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
	Button bDocuments;
	Button bPurge;
	boolean replaceNames;
	boolean replaceKons;
	boolean deleteDocs;
	boolean purgeDB;
	
	protected SettingsDialog(Shell parentShell){
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = (Composite) super.createDialogArea(parent);
		bNames = new Button(ret, SWT.CHECK);
		bNames.setText("Namen durch echt wirkende Pseudos ersetzen");
		bKons = new Button(ret, SWT.CHECK);
		bKons.setText("Auch Konsultationstexte überschreiben");
		bDocuments = new Button(ret, SWT.CHECK);
		bDocuments.setText("Alle Dokumente löschen");
		bPurge = new Button(ret, SWT.CHECK);
		bPurge.setText("Gelöscht markierte Objekte definitiv löschen");
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		getShell().setText("Datenbank-Anonymisierer");
	}
	
	@Override
	protected void okPressed(){
		replaceNames = bNames.getSelection();
		replaceKons = bKons.getSelection();
		deleteDocs = bDocuments.getSelection();
		super.okPressed();
	}
	
}
