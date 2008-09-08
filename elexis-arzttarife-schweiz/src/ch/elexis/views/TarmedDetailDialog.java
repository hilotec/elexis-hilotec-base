package ch.elexis.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.data.TarmedLeistung;

public class TarmedDetailDialog extends Dialog {
	TarmedLeistung tl;
	TarmedDetailDisplay td;
	public TarmedDetailDialog(Shell shell, TarmedLeistung tl){
		super(shell);
		this.tl=tl;
		td=new TarmedDetailDisplay();
		
	}
	@Override
	protected Control createDialogArea(Composite parent) {
		//Composite ret=td.createDisplay(parent, null);
		//td.display(tl);
		Composite ret=(Composite)super.createDialogArea(parent);
		ret.setLayout(new GridLayout(3,true));
		new Label(ret,SWT.NONE).setText("tpAL: "+Integer.toString(tl.getAL()));
		new Label(ret,SWT.NONE).setText("TP: ");
		return ret;
	}
	@Override
	public void create(){
		super.create();
		getShell().setText("Tarmed-Details");
	}

	
}

