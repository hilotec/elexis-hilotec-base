package ch.elexis.connect.reflotron;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
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
import ch.elexis.rs232.AbstractConnection;
import ch.elexis.rs232.AbstractConnection.ComPortListener;
import ch.elexis.util.SWTHelper;

public class ReflotronSprintAction extends Action implements ComPortListener {
	
	AbstractConnection _ctrl;
	Labor _myLab;
	Logger _log;
	Thread msgDialogThread;
	Patient selectedPatient;
	
	public ReflotronSprintAction(){
		super(Messages.getString("ReflotronSprintAction.ButtonName"), AS_CHECK_BOX);
		setToolTipText(Messages.getString("ReflotronSprintAction.ToolTip"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			"ch.elexis.connect.reflotron", "icons/reflotron.png"));
	}
	
	/**
	 * Serielle Verbindung wird initialisiert
	 */
	private void initConnection(){
		if (_ctrl != null && _ctrl.isOpen()) {
			_ctrl.close();
		}
		_ctrl =
			new ReflotronConnection(Messages.getString("ReflotronSprintAction.ConnectionName"),
				Hub.localCfg.get(Preferences.PORT, Messages
					.getString("ReflotronSprintAction.DefaultPort")), Hub.localCfg.get(
					Preferences.PARAMS, Messages.getString("ReflotronSprintAction.DefaultParams")),
				this);
		
		if (Hub.localCfg.get(Preferences.LOG, "n").equalsIgnoreCase("y")) {
			try {
				_log =
					new Logger(System.getProperty("user.home") + File.separator + "elexis"
						+ File.separator + "reflotron.log");
			} catch (FileNotFoundException e) {
				SWTHelper.showError(Messages.getString("ReflotronSprintAction.LogError.Title"),
					Messages.getString("ReflotronSprintAction.LogError.Text"));
				_log = new Logger();
			}
		} else {
			_log = new Logger(false);
		}
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
						.getString("ReflotronSprintAction.DefaultTimeout"));
				int timeout = 20;
				try {
					timeout = Integer.parseInt(timeoutStr);
				} catch (NumberFormatException e) {
					// Do nothing. Use default value
				}
				_ctrl.awaitFrame(Desk.getTopShell(),
					"Elexis wartet auf Daten aus dem Reflotrongerät..", 1, 4, 0, timeout);
				return;
			} else {
				_log.log("Error");
				SWTHelper.showError(Messages.getString("ReflotronSprintAction.RS232.Error.Title"),
					msg);
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
	
	/**
	 * Eine Standard-Fehlermeldung asynchron im UI-Thread zeigen
	 */
	private static void showError(final String title, final String message){
		Desk.getDisplay().asyncExec(new Runnable() {
			
			public void run(){
				Shell shell = Desk.getTopShell();
				MessageDialog.openError(shell, title, message);
			}
		});
	}
	
	/**
	 * Unterbruche wird von serieller Schnittstelle geschickt.
	 */
	public void gotBreak(final AbstractConnection connection){
		connection.close();
		setChecked(false);
		_log.log("Break");
		_log.logEnd();
		SWTHelper.showError(Messages.getString("ReflotronSprintAction.RS232.Break.Title"), Messages
			.getString("ReflotronSprintAction.RS232.Break.Text"));
	}
	
	/**
	 * Einzelne Probe wird verarbeitet
	 * @param probe
	 */
	private void processProbe(final Probe probe) {
			Desk.getDisplay().syncExec(new Runnable() {
				
				public void run(){
					selectedPatient = GlobalEvents.getSelectedPatient();
					Patient probePat = null;
					// TODO: Filter fuer KontaktSelektor
					String vorname = null;
					String name = null;
					String patientStr = "Patient: Unbekannt (" + probe.getIdent() + ")\n";
					if (probe.getIdent() != null) {
						String patIdStr = probe.getIdent();
						Long patId = null;
						try {
							patId = new Long(patIdStr);
						} catch(NumberFormatException e) {
							// Do nothing
						}
						
						// Patient-ID oder Name?
						Query<Patient> patQuery = new Query<Patient>(Patient.class);
						if (patId != null) {
							patQuery.add(Patient.PATID, "=", patIdStr);
						} else {
							String[] parts = patIdStr.split(",");
							if (parts.length > 1) {
								vorname = parts[1].toUpperCase();
								if (parts[1].length() > 1) {
									vorname = parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1);
								}
								patQuery.add(Patient.FIRSTNAME, "like", vorname + "%");
							}
							if (parts.length > 0) {
								name = parts[0].toUpperCase();
								if (parts[0].length() > 1) {
									name = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);
								}
								patQuery.add(Patient.NAME, "like", name + "%");
							}
						}
						
						List<Patient> patientList = patQuery.execute();
						
						if (patientList.size() == 1) {
							probePat = patientList.get(0);
							patientStr =
								"Patient: " + probePat.getName() + ", " + probePat.getVorname() + " ("
									+ probe.getIdent() + ")\n";
						}
					}
					
					String text = patientStr + "Wert: " + probe.getResultat() + "\n";
					
					boolean ok = MessageDialog.openConfirm(Desk.getTopShell(), "Afinion AS100", text);
					if (ok) {
						boolean showSelectionDialog = false;
						if (selectedPatient == null) {
							if (probePat != null) {
								selectedPatient = probePat;
							} else {
								showSelectionDialog = true;
							}
						} else {
							if (probePat == null) {
								showSelectionDialog = true;
							}
						}
						
						if (showSelectionDialog) {
							Desk.getDisplay().syncExec(new Runnable() {
								public void run(){
									// TODO: Filter vorname/name in KontaktSelektor einbauen
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
								probe.write(selectedPatient);
							} catch (PackageException e) {
								showError(Messages
									.getString("ReflotronSprintAction.ProbeError.Title"), e.getMessage());
							}
						} else {
							showError(Messages.getString("ReflotronSprintAction.Patient.Title"),
								"Kein Patient ausgewählt!");
						}
					}
				}
			});
		}
	
	/**
	 * Daten werden von der Seriellen Schnittstelle geliefert
	 */
	public void gotData(final AbstractConnection connection, final byte[] data){
		String content = new String(data);
		_log.logRX(content);
		
		String[] strArray = content.split("\r\n");
		if (strArray.length > 3) {
			Probe probe = new Probe(strArray);
			processProbe(probe);
		} else {
			if (content != null && content.length() > 0) {
				showError("Reflotron", "Unvollständige Daten: " + content + "\nBitte schicken sie die Daten nochmals (F5)!");
			}
		}
		
		_log.log("Saved");
		GlobalEvents.getInstance().fireUpdateEvent(LabItem.class);
	}
	
	/**
	 * Verbindung zu serieller Schnittstelle wurde getrennt
	 */
	public void closed() {
		_ctrl.close();
		_log.log("Closed");
		setChecked(false);
		_log.logEnd();
	}
	
	/**
	 * Verbindung zu serieller Schnittstelle wurde vom Benutzer abgebrochen
	 */
	public void cancelled(){
		_ctrl.close();
		_log.log("Cancelled");
		setChecked(false);
		_log.logEnd();
	}
	
	/**
	 * Verbindung zu serieller Schnittstelle hat timeout erreicht.
	 */
	public void timeout(){
		_ctrl.close();
		_log.log("Timeout");
		SWTHelper.showError(Messages.getString("ReflotronSprintAction.RS232.Timeout.Title"),
			Messages.getString("ReflotronSprintAction.RS232.Timeout.Text"));
		setChecked(false);
		_log.logEnd();
	}
}
