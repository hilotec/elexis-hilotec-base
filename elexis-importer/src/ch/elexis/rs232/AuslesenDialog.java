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

/**
 * Dialog startet Connection und liest RS232 Input bis Benutzer dialog schliesst. Wird von
 * LogConnection verwendet.
 * 
 * @author immi
 * 
 */
public class AuslesenDialog extends Dialog implements Connection.ComPortListener {
	Connection conn;
	Label label;
	final String schnittstelle;
	
	public AuslesenDialog(Shell parentShell, final String text){
		super(parentShell);
		this.schnittstelle = text;
	}
	
	public void setConnection(Connection connection){
		this.conn = connection;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = (Composite) super.createDialogArea(parent);
		label = new Label(ret, SWT.NONE);
		label.setText("Daten vom Gerät werden fortlaufend ins Log geschrieben..");
		return ret;
	}
	
	@Override
	protected void configureShell(Shell newShell){
		newShell.setText(schnittstelle);
		super.configureShell(newShell);
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	@Override
	public int open(){
		if (conn.connect()) {
			conn.awaitFrame(1, 4, 0, 3600000);
			return super.open();
		} else {
			String title = MessageFormat.format("{0} Schnittstelle auslesen", schnittstelle);
			SWTHelper.showError(title, conn.getErrorMessage());
			close();
		}
		return CANCEL;
	}
	
	@Override
	protected void cancelPressed(){
		if (conn.isOpen()) {
			conn.close();
			conn = null;
		}
		super.cancelPressed();
	}
	
	public void gotBreak(Connection conn){}
	
	public void gotChunk(Connection conn, String chunk){}
	
	public void timeout(){}
	
}
