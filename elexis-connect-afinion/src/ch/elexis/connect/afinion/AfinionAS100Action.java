package ch.elexis.connect.afinion;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

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
import ch.elexis.data.Query;
import ch.elexis.dialogs.KontaktSelektor;
import ch.elexis.rs232.AbstractConnection;
import ch.elexis.rs232.AbstractConnection.ComPortListener;
import ch.elexis.util.SWTHelper;

public class AfinionAS100Action extends Action implements ComPortListener {
	
	AbstractConnection _ctrl;
	Labor _myLab;
	Thread msgDialogThread;
	Thread infoDialogThread;
	Patient selectedPatient;
	Logger _log;
	
	public AfinionAS100Action(){
		super(Messages.getString("AfinionAS100Action.ButtonName"), AS_CHECK_BOX);
		setToolTipText(Messages.getString("AfinionAS100Action.ToolTip"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("ch.elexis.connect.afinion",
			"icons/afinion.png"));
		
		_ctrl =
			new AfinionConnection(Messages.getString("AfinionAS100Action.ConnectionName"),
				Hub.localCfg.get(Preferences.PORT, Messages
					.getString("AfinionAS100Action.DefaultPort")), Hub.localCfg.get(
					Preferences.PARAMS, Messages.getString("AfinionAS100Action.DefaultParams")),
				this);
		
		if (Hub.localCfg.get(Preferences.LOG, "n").equalsIgnoreCase("y")) {
			try {
				_log =
					new Logger(System.getProperty("user.home") + File.separator + "elexis"
						+ File.separator + "afinion.log");
			} catch (FileNotFoundException e) {
				SWTHelper.showError(Messages.getString("AfinionAS100Action.LogError.Title"),
					Messages.getString("AfinionAS100Action.LogError.Text"));
				_log = new Logger();
			}
		} else {
			_log = new Logger(false);
		}
	}
	
	public void showRunningInfo(){
		msgDialogThread = new Thread() {
			MessageDialog dialog;
			
			public void run(){
				dialog =
					new MessageDialog(Desk.getTopShell(), "Afinion AS100",
						null, // accept the default window icon
						"Messdaten werden gelesen. Dies kann einige Minuten dauern..",
						MessageDialog.INFORMATION, new String[] {
							"Ok"
						}, 0);
				dialog.open();
			}
		};
		Desk.getDisplay().asyncExec(msgDialogThread);
	}
	
	private void closeRunningInfo(){
		if (msgDialogThread != null && !msgDialogThread.isInterrupted()) {
			msgDialogThread.interrupt();
		}
	}
	
	public void writeRecord(final Record record){
		Desk.getDisplay().syncExec(new Runnable() {
			
			public void run(){
				Patient probePat = null;
				// TODO: Filter fuer KontaktSelektor
				String filter = null;
				String patientStr = "Patient: Unbekannt (" + record.getId() + ")\n";
				String patId = record.getId();
				Query<Patient> patQuery = new Query<Patient>(Patient.class);
				patQuery.add(Patient.PATID, "=", patId);
				
				List<Patient> patientList = patQuery.execute();
				
				if (patientList.size() == 1) {
					probePat = patientList.get(0);
					patientStr =
						"Patient: " + probePat.getName() + ", " + probePat.getVorname() + " ("
							+ record.getId() + ")\n";
				}
				
				String text = patientStr + "Run: " + record.getRunNr() + "\n" + record.getText();
				
				MessageDialog dialog = new MessageDialog(Desk.getTopShell(), "Afinion AS100", null, // accept
					// the
					// default
					// window
					// icon
					text, MessageDialog.INFORMATION, new String[] {
						"Ok", "Abbrechen"
					}, 0);
				if (dialog.open() != Dialog.CANCEL) {
					boolean showSelectionDialog = false;
					if (selectedPatient == null) {
						if (probePat != null) {
							selectedPatient = probePat;
						} else {
							showSelectionDialog = true;
						}
					} else {
						if (probePat != null) {
							if (!probePat.equals(selectedPatient)) {
								showSelectionDialog = true;
							}
						} else {
							showSelectionDialog = true;
						}
					}
					
					if (showSelectionDialog) {
						Desk.getDisplay().syncExec(new Runnable() {
							public void run(){
								KontaktSelektor ksl =
									new KontaktSelektor(Hub.getActiveShell(), Patient.class,
										Messages.getString("AfinionAS100Action.Patient.Title"),
										Messages.getString("AfinionAS100Action.Patient.Text"));
								ksl.create();
								ksl.getShell().setText(
									Messages.getString("AfinionAS100Action.Patient.Title"));
								if (ksl.open() == org.eclipse.jface.dialogs.Dialog.OK) {
									selectedPatient = (Patient) ksl.getSelection();
								} else {
									selectedPatient = null;
								}
								
							}
						});
					}
					if (selectedPatient != null) {
						try {
							record.write(selectedPatient);
						} catch (PackageException e) {
							SWTHelper.showError(Messages
								.getString("AfinionAS100Action.ProbeError.Title"), e.getMessage());
						}
					}
				}
			}
		});
	}
	
	@Override
	public void run(){
		if (isChecked()) {
			showRunningInfo();
			_log.logStart();
			String msg = _ctrl.connect();
			if (msg == null) {
				String timeoutStr =
					Hub.localCfg.get(Preferences.TIMEOUT, Messages
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
				SWTHelper
					.showError(Messages.getString("AfinionAS100Action.RS232.Error.Title"), msg);
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
	
	public void gotBreak(final AbstractConnection connection){
		connection.close();
		setChecked(false);
		_log.log("Break");
		_log.logEnd();
		SWTHelper.showError(Messages.getString("AfinionAS100Action.RS232.Break.Title"), Messages
			.getString("AfinionAS100Action.RS232.Break.Text"));
	}
	
	/**
	 * Liest Bytes aus einem Bytearray
	 */
	private byte[] subBytes(final byte[] bytes, final int pos, final int length){
		byte[] retVal = new byte[length];
		for (int i = 0; i < length; i++) {
			retVal[i] = bytes[pos + i];
		}
		return retVal;
	}
	
	/**
	 * Messagedaten von Afinion wurden gelesen
	 */
	public void gotData(final AbstractConnection connection, final byte[] data){
		_log.logRX(data.toString());
		
		closeRunningInfo();
		
		// Record lesen
		Record[] records = new Record[10];
		int pos = 0;
		for (int i = 0; i < 10; i++) {
			byte[] subbytes = subBytes(data, pos, 256);
			records[i] = new Record(subbytes);
			if (records[i].isValid()) {
				System.out.println(records[i].toString());
			}
			pos += 256;
		}
		
		selectedPatient = GlobalEvents.getSelectedPatient();
		
		for (Record record : records) {
			if (record.isValid()) {
				writeRecord(record);
			}
		}
		
		_log.log("Saved");
		GlobalEvents.getInstance().fireUpdateEvent(LabItem.class);
		
		SWTHelper.showInfo("Afinion AS100", "Alle Messwerte wurden gelesen");
		_ctrl.close();
	}
	
	public void timeout(){
		_ctrl.close();
		_log.log("Timeout");
		SWTHelper.showError(Messages.getString("AfinionAS100Action.RS232.Timeout.Title"), Messages
			.getString("AfinionAS100Action.RS232.Timeout.Text"));
		setChecked(false);
		_log.logEnd();
	}
}
