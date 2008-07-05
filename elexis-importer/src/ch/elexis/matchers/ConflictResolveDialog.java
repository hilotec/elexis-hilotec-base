package ch.elexis.matchers;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ConflictResolveDialog extends TitleAreaDialog {
	public ConflictResolveDialog(Shell shell) {
		super(shell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret= (Composite)super.createDialogArea(parent);
		Label lTell=new Label(ret,SWT.WRAP);
		lTell.setText(resolve1);
		
		return ret;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Nähere Angaben zum Import");
	}

	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		super.okPressed();
	}
	
	final static String resolve1="Es kann nicht automatisch entschieden werden, "+
	"ob # in der Datenbank enthalten ist, bzw. welchem existierenden Kontakt dies entspricht.\n"+
	"Bitte wählen Sie unten aus, welchem Kontakt dieser neue Eintrag entspricht oder ob ein Kontakt "+
	"für diesen Eintrag neu erstellt werden soll.";
}
