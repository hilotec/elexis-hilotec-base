package ch.elexis.connect.afinion;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.connect.afinion.packages.PackageException;
import ch.elexis.connect.afinion.packages.Record;
import ch.elexis.data.LabItem;
import ch.elexis.data.Labor;
import ch.elexis.data.Patient;
import ch.elexis.dialogs.KontaktSelektor;
import ch.elexis.rs232.Connection;
import ch.elexis.rs232.Connection.ComPortListener;
import ch.elexis.util.SWTHelper;

public class AfinionAS100Action extends Action implements ComPortListener {
	
	Connection _ctrl;
	Labor _myLab;
	Patient _actPatient;
	Logger _log;
	
	public  AfinionAS100Action(){
		super(Messages.getString("AfinionAS100Action.ButtonName"),AS_CHECK_BOX);
		setToolTipText(Messages.getString("AfinionAS100Action.ToolTip"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("ch.elexis.connect.afinion", "icons/afinion.png"));
		
		_ctrl = new Connection(Messages.getString("AfinionAS100Action.ConnectionName"),Hub.localCfg.get(Preferences.PORT, Messages.getString("AfinionAS100Action.DefaultPort")),
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
	@Override
	public void run(){
		if(isChecked()){
			KontaktSelektor ksl=new KontaktSelektor(Hub.getActiveShell(),Patient.class,Messages.getString("AfinionAS100Action.Patient.Title"),Messages.getString("AfinionAS100Action.Patient.Text"));
			ksl.create();
			ksl.getShell().setText(Messages.getString("AfinionAS100Action.Patient.Title"));
			if(ksl.open()==org.eclipse.jface.dialogs.Dialog.OK){
				_actPatient=(Patient)ksl.getSelection();
				
				_log.logStart();
				if(_ctrl.connect()){
					_ctrl.awaitFrame(1, 4, 0, 6000);
					return;
				}else{
					_log.log("Error");	
					SWTHelper.showError(Messages.getString("AfinionAS100Action.RS232.Error.Title"), Messages.getString("AfinionAS100Action.RS232.Error.Text"));
				}
			}
		}else{
			if(_ctrl.isOpen()) {
				_actPatient=null;
				_ctrl.sendBreak();
				_ctrl.close();
			}
		}
		setChecked(false);
		_log.logEnd();
	}
	
	public void gotBreak(final Connection connection) {
		_actPatient=null;
		connection.close();
		setChecked(false);
		_log.log("Break");	
		_log.logEnd();
		SWTHelper.showError(Messages.getString("AfinionAS100Action.RS232.Break.Title"), Messages.getString("AfinionAS100Action.RS232.Break.Text"));
	}
	public void gotChunk(final Connection connection, final String data) 
	{
		_log.logRX(data);
		
		// Record lesen
		Record record = new Record(data, _actPatient);
		try {
			record.write();
		} catch(PackageException e) {
			SWTHelper.showError(Messages.getString("ReflotronSprintAction.ProbeError.Title"), e.getMessage());
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
