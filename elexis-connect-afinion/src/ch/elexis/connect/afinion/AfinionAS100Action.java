package ch.elexis.connect.afinion;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.jface.action.Action;
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
	
	AfinionConnection _ctrl;
	Labor _myLab;
	Thread msgDialogThread;
	Thread infoDialogThread;
	Patient selectedPatient;
	Logger _log;
	Record lastRecord = null;
	
	public AfinionAS100Action(){
		super(Messages.getString("AfinionAS100Action.ButtonName"), AS_CHECK_BOX); //$NON-NLS-1$
		setToolTipText(Messages.getString("AfinionAS100Action.ToolTip")); //$NON-NLS-1$
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("ch.elexis.connect.afinion", //$NON-NLS-1$
			"icons/afinion.png")); //$NON-NLS-1$
	}
	
	private void initConnection(){
		if (_ctrl != null && _ctrl.isOpen()) {
			_ctrl.close();
		}
		_ctrl =
			new AfinionConnection(Messages.getString("AfinionAS100Action.ConnectionName"), //$NON-NLS-1$
				Hub.localCfg.get(Preferences.PORT, Messages
					.getString("AfinionAS100Action.DefaultPort")), Hub.localCfg.get( //$NON-NLS-1$
					Preferences.PARAMS, Messages.getString("AfinionAS100Action.DefaultParams")), //$NON-NLS-1$
				this);
		
		
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.MINUTE, -1);
		_ctrl.setCurrentDate(cal);
		
		if (Hub.localCfg.get(Preferences.LOG, "n").equalsIgnoreCase("y")) { //$NON-NLS-1$ //$NON-NLS-2$
			try {
				_log = new Logger(System.getProperty("user.home") + File.separator + "elexis" //$NON-NLS-1$ //$NON-NLS-2$
					+ File.separator + "afinion.log"); //$NON-NLS-1$
			} catch (FileNotFoundException e) {
				SWTHelper.showError(Messages.getString("AfinionAS100Action.LogError.Title"), //$NON-NLS-1$
					Messages.getString("AfinionAS100Action.LogError.Text")); //$NON-NLS-1$
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
						.getString("AfinionAS100Action.DefaultTimeout")); //$NON-NLS-1$
				int timeout = 20;
				try {
					timeout = Integer.parseInt(timeoutStr);
				} catch (NumberFormatException e) {
					// Do nothing. Use default value
				}
				_ctrl.awaitFrame(Desk.getTopShell(), Messages
					.getString("AfinionAS100Action.WaitMsg"), 1, 4, 0, timeout); //$NON-NLS-1$
				return;
			} else {
				_log.log("Error"); //$NON-NLS-1$
				SWTHelper
					.showError(Messages.getString("AfinionAS100Action.RS232.Error.Title"), msg); //$NON-NLS-1$
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
		_log.log("Break"); //$NON-NLS-1$
		_log.logEnd();
		SWTHelper.showError(Messages.getString("AfinionAS100Action.RS232.Break.Title"), Messages //$NON-NLS-1$
			.getString("AfinionAS100Action.RS232.Break.Text")); //$NON-NLS-1$
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
	 * Einzelner Messwert wird verarbeitet
	 * 
	 * @param probe
	 */
	private void processRecord(final Record record){
		Desk.getDisplay().syncExec(new Runnable() {
			
			public void run(){
				selectedPatient = GlobalEvents.getSelectedPatient();
				Patient probePat = null;
				String vorname = null;
				String name = null;
				String patientStr =
					Messages.getString("AfinionAS100Action.UnknownPatientHeaderString") + record.getId() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				
				if (record.getId() != null) {
					String patIdStr = record.getId();
					Long patId = null;
					try {
						patId = new Long(patIdStr);
					} catch (NumberFormatException e) {
						// Do nothing
					}
					
					// Patient-ID oder Name?
					Query<Patient> patQuery = new Query<Patient>(Patient.class);
					if (patId != null) {
						patQuery.add(Patient.PATID, "=", patIdStr); //$NON-NLS-1$
					} else {
						String[] parts = patIdStr.split(","); //$NON-NLS-1$
						if (parts.length > 1) {
							vorname = parts[1].trim().toUpperCase();
							if (parts[1].length() > 1) {
								vorname =
									parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1);
							}
							patQuery.add(Patient.FIRSTNAME, "like", vorname + "%"); //$NON-NLS-1$ //$NON-NLS-2$
						}
						if (parts.length > 0) {
							name = parts[0].trim().toUpperCase();
							if (parts[0].length() > 1) {
								name =
									parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);
							}
							patQuery.add(Patient.NAME, "like", name + "%"); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
					List<Patient> patientList = patQuery.execute();
					
					if (patientList.size() == 1) {
						probePat = patientList.get(0);
						patientStr =
							Messages.getString("AfinionAS100Action.PatientHeaderString") + probePat.getName() + ", " + probePat.getVorname() + " (" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								+ record.getId() + ")"; //$NON-NLS-1$
					}
				}
				
				String text =
					MessageFormat
						.format(
							Messages.getString("AfinionAS100Action.ValueInfoMsg"), patientStr, record.getRunNr(), record //$NON-NLS-1$
								.getText());
				
				boolean ok =
					MessageDialog.openConfirm(Desk.getTopShell(), Messages
						.getString("AfinionAS100Action.DeviceName"), text); //$NON-NLS-1$
				if (ok) {
					boolean showSelectionDialog = false;
					if (probePat != null) {
						selectedPatient = probePat;
					} else {
						showSelectionDialog = true;
					}
					
					if (showSelectionDialog) {
						Desk.getDisplay().syncExec(new Runnable() {
							public void run(){
								// TODO: Filter vorname/name in KontaktSelektor einbauen
								KontaktSelektor ksl =
									new KontaktSelektor(Hub.getActiveShell(), Patient.class,
										Messages.getString("AfinionAS100Action.Patient.Title"), //$NON-NLS-1$
										Messages.getString("AfinionAS100Action.Patient.Text")); //$NON-NLS-1$
								ksl.create();
								ksl.getShell().setText(
									Messages.getString("AfinionAS100Action.Patient.Title")); //$NON-NLS-1$
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
								.getString("AfinionAS100Action.ProbeError.Title"), e //$NON-NLS-1$
								.getMessage());
						}
					} else {
						SWTHelper.showError(Messages
							.getString("AfinionAS100Action.Patient.Title"), //$NON-NLS-1$
							Messages.getString("AfinionAS100Action.NoPatientSelectedMsg")); //$NON-NLS-1$
					}
					_log.log("Saved"); //$NON-NLS-1$
					GlobalEvents.getInstance().fireUpdateEvent(LabItem.class);
				}
			}
		});
	}
	
	/**
	 * Messagedaten von Afinion wurden gelesen
	 */
	public void gotData(final AbstractConnection connection, final byte[] data){
		_log.logRaw("--> \"");
		_log.logRaw("<DLE>");
		_log.logRaw("<STX>");
		_log.logRaw(new String(data));
		_log.logRaw("<DLE>");
		_log.logRaw("<ETB>");
		_log.logRaw("03A3");
		_log.logRaw("<DLE>");
		_log.logRaw("<ETX>");
		_log.logRaw(" \"");
		
		// Record lesen
		int pos = 0;
		int i = 0;
		int validRecords = 0;
		while (i < 10) {
			byte[] subbytes = subBytes(data, pos, 256);
			Record tmpRecord = new Record(subbytes);
			if (tmpRecord.isValid()) {
				lastRecord = tmpRecord;
				System.out.println(lastRecord.toString());
				validRecords++;
			}
			pos += 256;
			i++;
		}
		
		if (validRecords == 10) { // Read next 10 records
			Calendar cal = lastRecord.getCalendar();
			cal.add(Calendar.SECOND, 1);
			_ctrl.setCurrentDate(cal);
		} else {
			_ctrl.close();
			
			if (lastRecord != null) {
				processRecord(lastRecord);
			} else {
				SWTHelper
					.showInfo(
						Messages.getString("AfinionAS100Action.DeviceName"), Messages.getString("AfinionAS100Action.NoValuesMsg")); //$NON-NLS-2$
			}
			
			_log.log("Saved"); //$NON-NLS-1$
			GlobalEvents.getInstance().fireUpdateEvent(LabItem.class);
		}
	}
	
	public void closed(){
		_ctrl.close();
		_log.log("Closed"); //$NON-NLS-1$
		setChecked(false);
		_log.logEnd();
	}
	
	public void cancelled(){
		_ctrl.close();
		_log.log("Cancelled"); //$NON-NLS-1$
		setChecked(false);
		_log.logEnd();
	}
	
	public void timeout(){
		_ctrl.close();
		_log.log("Timeout"); //$NON-NLS-1$
		SWTHelper.showError(Messages.getString("AfinionAS100Action.RS232.Timeout.Title"), Messages //$NON-NLS-1$
			.getString("AfinionAS100Action.RS232.Timeout.Text")); //$NON-NLS-1$
		setChecked(false);
		_log.logEnd();
	}
}
