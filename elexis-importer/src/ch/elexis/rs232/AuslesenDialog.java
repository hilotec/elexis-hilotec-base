package ch.elexis.rs232;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.util.SWTHelper;

public class AuslesenDialog extends Dialog implements Connection.ComPortListener {
	Connection conn;
	final Runnable auslesenRunnable;
	Label label;
	final String schnittstelle;
	
	private class AuslesenRunnable implements Runnable {
		public void run() {
			if(conn.connect()){
				conn.awaitFrame(1, 4, 0, 3600);
				return;
			} else{
				String title = MessageFormat.format("{0} Schnittstelle auslesen", schnittstelle);
				String msg = MessageFormat.format("Fehler beim Auslesen!", new Object[0]);
				SWTHelper.showError(title, msg);
			}
		}
	}

	public AuslesenDialog(Shell parentShell, final String text) {
		super(parentShell);
		this.auslesenRunnable = new AuslesenRunnable();
		this.schnittstelle = text;
	}
	
	public void setConnection(Connection connection) {
		this.conn = connection;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=(Composite)super.createDialogArea(parent);
		label = new Label(ret,SWT.NONE);
		label.setText("Warten auf Daten..                 ");
		return ret;
	}



	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText(schnittstelle);
		super.configureShell(newShell);
		newShell.getDisplay().asyncExec(auslesenRunnable);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}
	
	

	@Override
	public int open() {
		int retVal = super.open();
		
		return retVal;
	}

	@Override
	protected void cancelPressed() {
		if (conn.isOpen()) {
			conn.close();
			conn = null;
		}
		super.cancelPressed();
	}

	@Override
	public void gotBreak(Connection conn) {
	}

	@Override
	public void gotChunk(Connection conn, String chunk) {
	}

	@Override
	public void timeout() {
	}
	
	
}
