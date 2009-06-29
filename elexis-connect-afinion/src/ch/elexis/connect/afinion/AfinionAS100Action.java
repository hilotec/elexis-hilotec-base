package ch.elexis.connect.afinion;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
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
	}
	
	private void initConnection(){
		if (_ctrl != null && _ctrl.isOpen()) {
			_ctrl.close();
		}
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
	
	/**
	 * Eine Standard-Fehlermeldung asynchron im UI-Thread zeigen
	 * 
	 * @param title
	 *            Titel
	 * @param message
	 *            Nachricht
	 */
	public static void showError(final String title, final String message){
		Desk.getDisplay().asyncExec(new Runnable() {
			
			public void run(){
				Shell shell = Desk.getTopShell();
				MessageDialog.openError(shell, title, message);
			}
		});
	}
	
	@Override
	public void run(){
		if (isChecked()) {
			initConnection();
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
				_ctrl.awaitFrame(Desk.getTopShell(),
					"Daten werden aus dem Afinion AS100 gelesen..", 1, 4, 0, timeout);
				return;
			} else {
				_log.log("Error");
				SWTHelper
					.showError(Messages.getString("AfinionAS100Action.RS232.Error.Title"), msg);
			}
		} else {
			if (_ctrl.isOpen()) {
				_ctrl.sendBreak();
				_ctrl.close();
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
		
		// Record lesen
		Record lastRecord = null;
		Record[] records = new Record[10];
		int pos = 0;
		int i=0;
		while (i < 10 && lastRecord == null) {
			byte[] subbytes = subBytes(data, pos, 256);
			records[i] = new Record(subbytes);
			if (records[i].isValid()) {
				lastRecord = records[i];
			}
			pos += 256;
			i++;
		}
		_ctrl.close();
		
		lastRecord = new Record(new byte[256]);
		
		if (lastRecord != null) {
			selectedPatient = GlobalEvents.getSelectedPatient();
			Patient probePat = null;
			// TODO: Filter fuer KontaktSelektor
			String filter = null;
			if (lastRecord.getId() != null) {
				String patId = lastRecord.getId();
				
				// Wenn erstes Zeichen nicht Zahl, dann wahrscheinlich Pat-ID
				if (patId != null && patId.length() > 0) {
					Query<Patient> patQuery = new Query<Patient>(Patient.class);
					if (!Character.isDigit(patId.charAt(0))) {
						String patName = patId.toUpperCase();
						if (patId.length() > 1) {
							patName = patId.substring(0, 1).toUpperCase() + patId.substring(1);
						}
						patQuery.add(Patient.NAME, "like", patName + "%");
					} else {
						patQuery.add(Patient.PATID, "=", patId);
					}
					List<Patient> patientList = patQuery.execute();
					
					if (patientList.size() == 1) {
						probePat = patientList.get(0);
					} else if (!Character.isDigit(patId.charAt(0))) {
						filter = lastRecord.getId();
					}
				}
			}
			
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
				} else if (filter != null) {
					if (!filter.toLowerCase().equals(selectedPatient.getName().toLowerCase())) {
						showSelectionDialog = true;
					}
				}
			}
			
			if (showSelectionDialog) {
				Desk.getDisplay().syncExec(new Runnable() {
					public void run(){
						KontaktSelektor ksl =
							new KontaktSelektor(Hub.getActiveShell(), Patient.class, Messages
								.getString("ReflotronSprintAction.Patient.Title"), Messages
								.getString("ReflotronSprintAction.Patient.Text"));
						ksl.create();
						ksl.getShell().setText(
							Messages.getString("ReflotronSprintAction.Patient.Title"));
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
					lastRecord.write(selectedPatient);
				} catch (PackageException e) {
					SWTHelper.showError(Messages
						.getString("ReflotronSprintAction.ProbeError.Title"), e.getMessage());
				}
			} else {
				SWTHelper.showError(Messages.getString("ReflotronSprintAction.Patient.Title"),
					"Kein Patient ausgewählt!");
			}
		} else {
			SWTHelper.showInfo("Afinion AS100", "Keine Messdaten vorhanden!");
		}
		
		_log.log("Saved");
		GlobalEvents.getInstance().fireUpdateEvent(LabItem.class);
	}
	
	public void closed() {
		_ctrl.close();
		_log.log("Closed");
		setChecked(false);
		_log.logEnd();
	}
	
	public void cancelled() {
		_ctrl.close();
		_log.log("Cancelled");
		setChecked(false);
		_log.logEnd();
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
