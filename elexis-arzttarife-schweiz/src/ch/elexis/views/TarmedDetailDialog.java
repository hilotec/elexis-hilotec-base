package ch.elexis.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
		Composite ret=td.createDisplay(parent, null);
		td.display(tl);
		return ret;
	}
	
}
