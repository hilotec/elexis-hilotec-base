package ch.elexis.connect.reflotron;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
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
import ch.elexis.rs232.Connection;
import ch.elexis.rs232.Connection.ComPortListener;
import ch.elexis.util.SWTHelper;

public class ReflotronSprintAction extends Action implements ComPortListener {

	Connection _ctrl;
	Labor _myLab;
	Logger _log;
	MessageDialog dialog;
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

	public void showRunning() {
		Desk.getDisplay().asyncExec(new Runnable() {

			public void run() {
				dialog = new MessageDialog(
						Desk.getTopShell(),
						"Reflotron",
						null, // accept
						// the
						// default
						// window
						// icon
						"Daten werden im Hintergrund gelesen..",
						MessageDialog.INFORMATION,
						new String[] { IDialogConstants.OK_LABEL }, 0);
				dialog.open();
			}
		});
	}

	public void closeRunning() {
		if (dialog != null) {
			dialog.close();
		}
	}

	@Override
	public void run() {
		if (isChecked()) {
			showRunning();
			_log.logStart();
			if (_ctrl.connect()) {
				_ctrl.awaitFrame(1, 4, 0, 6000);
				return;
			} else {
				_log.log("Error");
				SWTHelper
						.showError(
								Messages
										.getString("ReflotronSprintAction.RS232.Error.Title"),
								Messages
										.getString("ReflotronSprintAction.RS232.Error.Text"));
			}
			_log.logEnd();
		} else {
			if (_ctrl.isOpen()) {
				_ctrl.sendBreak();
				_ctrl.close();
			}
			closeRunning();
		}
		setChecked(false);
		_log.logEnd();
	}

	public void gotBreak(final Connection connection) {
		connection.close();
		setChecked(false);
		_log.log("Break");
		_log.logEnd();
		SWTHelper.showError(Messages
				.getString("ReflotronSprintAction.RS232.Break.Title"), Messages
				.getString("ReflotronSprintAction.RS232.Break.Text"));
	}

	public void gotChunk(final Connection connection, final String data) {
		_log.logRX(data);

		String[] strArray = data.split("\r\n");
		if (strArray.length > 3) {
			Probe probe = new Probe(strArray);
			selectedPatient = GlobalEvents.getSelectedPatient();
			Patient probePat = null;
			String filter = null;
			if (probe.getIdent() != null) {
				Query<Patient> patQuery = new Query<Patient>(Patient.class);
				patQuery.add(Patient.NAME, Query.EQUALS, probe.getIdent());

				List<Patient> patientList = patQuery.execute();

				if (patientList.size() == 1) {
					probePat = patientList.get(0);
				} else {
					filter = probe.getIdent();
				}
			}
			
			if ((selectedPatient == null && probe.getIdent().equals(""))
					|| (selectedPatient != null && !selectedPatient
							.equals(probePat))) {
				if (probePat != null) {
					filter = probePat.getName();
				}
				
				Desk.getDisplay().syncExec(new Runnable() {
					public void run(){
						KontaktSelektor ksl = new KontaktSelektor(
								Hub.getActiveShell(),
								Patient.class,
								Messages
										.getString("ReflotronSprintAction.Patient.Title"),
								Messages
										.getString("ReflotronSprintAction.Patient.Text"));
						ksl.create();
						ksl.getShell()
						.setText(
								Messages
										.getString("ReflotronSprintAction.Patient.Title"));
						if (ksl.open() == org.eclipse.jface.dialogs.Dialog.OK) {
							selectedPatient = (Patient) ksl.getSelection();
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
		_ctrl.close();
		setChecked(false);
		GlobalEvents.getInstance().fireUpdateEvent(LabItem.class);
		_log.logEnd();
	}

	public void timeout() {
		_ctrl.close();
		_log.log("Timeout");
		SWTHelper.showError(Messages
				.getString("ReflotronSprintAction.RS232.Timeout.Title"),
				Messages.getString("ReflotronSprintAction.RS232.Timeout.Text"));
		setChecked(false);
		_log.logEnd();
	}
}
