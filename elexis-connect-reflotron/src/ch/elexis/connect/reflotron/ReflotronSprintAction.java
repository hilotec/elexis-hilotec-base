package ch.elexis.connect.reflotron;

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
import ch.elexis.connect.reflotron.packages.PackageException;
import ch.elexis.connect.reflotron.packages.Probe;
import ch.elexis.data.LabItem;
import ch.elexis.data.Labor;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.dialogs.KontaktSelektor;
import ch.elexis.rs232.AbstractConnection;
import ch.elexis.rs232.AbstractConnection.ComPortListener;
import ch.elexis.util.SWTHelper;

public class ReflotronSprintAction extends Action implements ComPortListener {

	AbstractConnection _ctrl;
	Labor _myLab;
	Logger _log;
	Thread msgDialogThread;
	Patient selectedPatient;

	public ReflotronSprintAction() {
		super(Messages.getString("ReflotronSprintAction.ButtonName"),
				AS_CHECK_BOX);
		setToolTipText(Messages.getString("ReflotronSprintAction.ToolTip"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				"ch.elexis.connect.reflotron", "icons/reflotron.png"));

		_ctrl = new ReflotronConnection(Messages
				.getString("ReflotronSprintAction.ConnectionName"),
				Hub.localCfg.get(Preferences.PORT, Messages
						.getString("ReflotronSprintAction.DefaultPort")),
				Hub.localCfg.get(Preferences.PARAMS, Messages
						.getString("ReflotronSprintAction.DefaultParams")),
				this);

		if (Hub.localCfg.get(Preferences.LOG, "n").equalsIgnoreCase("y")) {
			try {
				_log = new Logger(System.getProperty("user.home")
						+ File.separator + "elexis" + File.separator
						+ "reflotron.log");
			} catch (FileNotFoundException e) {
				SWTHelper
						.showError(
								Messages
										.getString("ReflotronSprintAction.LogError.Title"),
								Messages
										.getString("ReflotronSprintAction.LogError.Text"));
				_log = new Logger();
			}
		} else {
			_log = new Logger(false);
		}
	}

	public void showRunningInfo() {
		msgDialogThread = new Thread() {
			MessageDialog dialog;

			public void run() {
				dialog = new MessageDialog(Desk.getTopShell(),
						"Reflotron",
						null, // accept the default window icon
						"Refletrondaten können nun empfangen werden..",
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
	public void run() {
		if (isChecked()) {
			showRunningInfo();
			_log.logStart();
			String msg = _ctrl.connect();
			if (msg == null) {
				String timeoutStr = Hub.localCfg
						.get(
								Preferences.TIMEOUT,
								Messages
										.getString("ReflotronSprintAction.DefaultTimeout"));
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
						.getString("ReflotronSprintAction.RS232.Error.Title"),
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
		connection.close();
		setChecked(false);
		_log.log("Break");
		_log.logEnd();
		SWTHelper.showError(Messages
				.getString("ReflotronSprintAction.RS232.Break.Title"), Messages
				.getString("ReflotronSprintAction.RS232.Break.Text"));
	}

	public void gotData(final AbstractConnection connection, final byte[] data) {
		String content = new String(data);
		_log.logRX(content);

		String[] strArray = content.split("\r\n");
		if (strArray.length > 3) {
			Probe probe = new Probe(strArray);
			selectedPatient = GlobalEvents.getSelectedPatient();
			Patient probePat = null;
			//TODO: Filter fuer KontaktSelektor
			String filter = null;
			if (probe.getIdent() != null) {
				String patName = probe.getIdent();
				// Wenn erstes Zeichen nicht Zahl, dann wahrscheinlich Pat-ID
				if (!Character.isDigit(patName.charAt(0))) {
					patName = patName.substring(0, 1).toUpperCase()
							+ patName.substring(1);
				}
				Query<Patient> patQuery = new Query<Patient>(Patient.class);
				patQuery.add(Patient.NAME, "like", patName + "%");

				List<Patient> patientList = patQuery.execute();

				if (patientList.size() == 1) {
					probePat = patientList.get(0);
				} else if (!Character.isDigit(patName.charAt(0))) {
					filter = probe.getIdent();
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
					public void run() {
						KontaktSelektor ksl = new KontaktSelektor(
								Hub.getActiveShell(),
								Patient.class,
								Messages
										.getString("ReflotronSprintAction.Patient.Title"),
								Messages
										.getString("ReflotronSprintAction.Patient.Text"));
						ksl.create();
						ksl
								.getShell()
								.setText(
										Messages
												.getString("ReflotronSprintAction.Patient.Title"));
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
					probe.write(selectedPatient);
				} catch (PackageException e) {
					SWTHelper
							.showError(
									Messages
											.getString("ReflotronSprintAction.ProbeError.Title"),
									e.getMessage());
				}
			} else {
				SWTHelper.showError(Messages
						.getString("ReflotronSprintAction.Patient.Title"),
						"Kein Patient ausgewählt!");
			}
		}

		_log.log("Saved");
		GlobalEvents.getInstance().fireUpdateEvent(LabItem.class);
	}

	public void timeout() {
		_ctrl.close();
		_log.log("Timeout");
		SWTHelper.showError(Messages
				.getString("ReflotronSprintAction.RS232.Timeout.Title"),
				Messages.getString("ReflotronSprintAction.RS232.Timeout.Text"));
		setChecked(false);
		closeRunningInfo();
		_log.logEnd();
	}
}
