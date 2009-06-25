package ch.elexis.connect.afinion;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.connect.afinion.packages.PackageException;
import ch.elexis.connect.afinion.packages.Record;
import ch.elexis.data.LabItem;
import ch.elexis.data.Labor;
import ch.elexis.data.Patient;
import ch.elexis.rs232.AbstractConnection;
import ch.elexis.rs232.AbstractConnection.ComPortListener;
import ch.elexis.util.SWTHelper;

public class AfinionAS100Action extends Action implements ComPortListener {
	
	AbstractConnection _ctrl;
	Labor _myLab;
	Patient _actPatient;
	Thread msgDialogThread;
	Logger _log;
	
	public  AfinionAS100Action(){
		super(Messages.getString("AfinionAS100Action.ButtonName"),AS_CHECK_BOX);
		setToolTipText(Messages.getString("AfinionAS100Action.ToolTip"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("ch.elexis.connect.afinion", "icons/afinion.png"));
		
		_ctrl = new AfinionConnection(Messages.getString("AfinionAS100Action.ConnectionName"),Hub.localCfg.get(Preferences.PORT, Messages.getString("AfinionAS100Action.DefaultPort")),
				Hub.localCfg.get(Preferences.PARAMS, Messages.getString("AfinionAS100Action.DefaultParams")),this);
		
		if (Hub.localCfg.get(Preferences.LOG, "n").equalsIgnoreCase("y"))
		{
			try 
			{
				_log = new Logger(System.getProperty("user.home") + File.separator + "elexis" + File.separator + "afinion.log");
			}
			catch (FileNotFoundException e) 
			{
				SWTHelper.showError(Messages.getString("AfinionAS100Action.LogError.Title"), Messages.getString("AfinionAS100Action.LogError.Text"));
				_log = new Logger();			
			}
		}
		else
		{
			_log = new Logger(false);
		}		
	}
	
	public void showRunningInfo() {
		msgDialogThread = new Thread() {
			MessageDialog dialog;

			public void run() {
				dialog = new MessageDialog(Desk.getTopShell(),
						"Afinion AS100",
						null, // accept the default window icon
						"Daten können nun empfangen werden..",
						MessageDialog.INFORMATION, new String[] { "Ok",
								"Abbrechen" }, 0);
				if (dialog.open() == Dialog.CANCEL) {
					_ctrl.sendBreak();
					_ctrl.close();
					setChecked(false);
					_log.logEnd();
				}
			}
		};
		Desk.getDisplay().asyncExec(msgDialogThread);
	}

	private void closeRunningInfo() {
		if (msgDialogThread != null && !msgDialogThread.isInterrupted()) {
			msgDialogThread.interrupt();
		}
	}
	
	@Override
	public void run(){
		if (isChecked()) {
			showRunningInfo();
			_log.logStart();
			String msg = _ctrl.connect();
			if (msg == null) {
				String timeoutStr = Hub.localCfg
						.get(
								Preferences.TIMEOUT,
								Messages
										.getString("AfinionAS100Action.DefaultTimeout"));
				int timeout = 20;
				try {
					timeout = Integer.parseInt(timeoutStr);
				} catch (NumberFormatException e) {
					// Do nothing. Use default value
				}
				_ctrl.awaitFrame(1, 4, 0, timeout);
				return;
			} else {
				closeRunningInfo();
				_log.log("Error");
				SWTHelper.showError(Messages
						.getString("AfinionAS100Action.RS232.Error.Title"),
						msg);
			}
		} else {
			if (_ctrl.isOpen()) {
				_ctrl.sendBreak();
				_ctrl.close();
				closeRunningInfo();
			}
		}
		setChecked(false);
		_log.logEnd();
	}
	
	public void gotBreak(final AbstractConnection connection) {
		_actPatient=null;
		connection.close();
		setChecked(false);
		_log.log("Break");	
		_log.logEnd();
		SWTHelper.showError(Messages.getString("AfinionAS100Action.RS232.Break.Title"), Messages.getString("AfinionAS100Action.RS232.Break.Text"));
	}
	
	public void gotData(final AbstractConnection connection, final byte[] data) {
		_log.logRX(data.toString());
		
		// Record lesen
		Record record = new Record(data, _actPatient);
		try {
			record.write();
		} catch(PackageException e) {
			SWTHelper.showError(Messages.getString("AfinionAS100Action.ProbeError.Title"), e.getMessage());
		}

		_log.log("Saved");
		_actPatient=null;
		_ctrl.close();
		setChecked(false);
		GlobalEvents.getInstance().fireUpdateEvent(LabItem.class);
		_log.logEnd();
	}
	
	public void timeout() {
		_ctrl.close();
		_log.log("Timeout");
		SWTHelper.showError(Messages.getString("AfinionAS100Action.RS232.Timeout.Title"), Messages.getString("AfinionAS100Action.RS232.Timeout.Text"));
		setChecked(false);
		_log.logEnd();
	}
}
