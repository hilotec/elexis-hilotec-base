package ch.elexis.scripting;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ScriptEditor extends TitleAreaDialog {
	String script;
	String title;
	Text text;
	
	public ScriptEditor(Shell shell, String vorgabe, String titel){
		super(shell);
		script=vorgabe;
		title=titel;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		GridData full=new GridData(GridData.FILL_BOTH|GridData.GRAB_HORIZONTAL|GridData.GRAB_VERTICAL);
		ret.setLayoutData(full);
		ret.setLayout(new FillLayout());
		text=new Text(ret,SWT.MULTI|SWT.BORDER);
		text.setText(script);
		return ret;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Script editieren");
		setMessage(title);
		getShell().setText("Elexis Script");
	}

	@Override
	protected void okPressed() {
		script=text.getText();
		super.okPressed();
	}
	
	public String getScript(){
		return script;
	}
}
