package ch.elexis.connect.sysmex;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.preferences.SettingsPreferenceStore;
import ch.elexis.rs232.Connection;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;

public class Preferences extends PreferencePage implements IWorkbenchPreferencePage {
	
	public static final String SYSMEX_BASE = "connectors/sysmex/"; //$NON-NLS-1$
	public static final String PORT = SYSMEX_BASE + "port"; //$NON-NLS-1$
	public static final String TIMEOUT = SYSMEX_BASE + "timeout"; //$NON-NLS-1$
	public static final String PARAMS = SYSMEX_BASE + "params"; //$NON-NLS-1$
	public static final String LOG = SYSMEX_BASE + "log"; //$NON-NLS-1$
	public static final String BACKGROUND = SYSMEX_BASE + "background"; //$NON-NLS-1$
	
	public static final String MODEL = SYSMEX_BASE + "model"; //$NON-NLS-1$
	public static final String RDW_TYP = SYSMEX_BASE + "rdwTyp"; //$NON-NLS-1$
	
	public static final String MODEL_KX21 = "KX-21"; //$NON-NLS-1$
	public static final String MODEL_KX21N = "KX-21N"; //$NON-NLS-1$
	public static final String MODEL_POCH = "pocH-100i"; //$NON-NLS-1$
	
	public static final String RDW_SD = "SD"; //$NON-NLS-1$
	public static final String RDW_CV = "CV"; //$NON-NLS-1$
	
	Label lblRdw;
	Combo ports;
	Text speed, data, stop, timeout, logFile;
	Button parity, log, background;
	Combo models, rdw_types;
	
	public Preferences(){
		super(Messages.getString("SysmexAction.ButtonName")); //$NON-NLS-1$
		setPreferenceStore(new SettingsPreferenceStore(Hub.localCfg));
	}
	
	@Override
	protected Control createContents(final Composite parent){
		Hub.log.log("Start von createContents", Log.DEBUGMSG); //$NON-NLS-1$
		
		Composite retComp = new Composite(parent, SWT.NONE);
		retComp.setLayout(new GridLayout(1, false));
		retComp.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		Composite mainComp = new Composite(retComp, SWT.NONE);
		mainComp.setLayout(new GridLayout(4, false));
		mainComp.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		new Label(mainComp, SWT.NONE).setText(Messages.getString("Preferences.Modell")); //$NON-NLS-1$
		models = new Combo(mainComp, SWT.SINGLE);
		models.setItems(new String[] {
			MODEL_KX21, MODEL_KX21N, MODEL_POCH
		});
		models.setText(Hub.localCfg.get(MODEL, MODEL_KX21));
		
		lblRdw = new Label(mainComp, SWT.NONE);
		lblRdw.setText(Messages.getString("Preferences.RDW")); //$NON-NLS-1$
		rdw_types = new Combo(mainComp, SWT.SINGLE);
		rdw_types.setItems(new String[] {
			RDW_SD, RDW_CV
		});
		rdw_types.setText(Hub.localCfg.get(RDW_TYP, RDW_SD));
		
		new Label(mainComp, SWT.NONE).setText(Messages.getString("Preferences.Backgroundprocess")); //$NON-NLS-1$
		background = new Button(mainComp, SWT.CHECK);
		background.setSelection(Hub.localCfg.get(BACKGROUND, "n").equalsIgnoreCase("y")); //$NON-NLS-1$ //$NON-NLS-2$
		GridDataFactory.swtDefaults().span(3, 1).applyTo(background);
		
		new Label(mainComp, SWT.NONE).setText(Messages.getString("Preferences.Log")); //$NON-NLS-1$
		log = new Button(mainComp, SWT.CHECK);
		log.setSelection(Hub.localCfg.get(LOG, "n").equalsIgnoreCase("y")); //$NON-NLS-1$ //$NON-NLS-2$
		GridDataFactory.swtDefaults().span(3, 1).applyTo(log);
		
		Group connectGroup = new Group(retComp, SWT.NONE);
		connectGroup.setText(Messages.getString("Preferences.Verbindung")); //$NON-NLS-1$
		connectGroup.setLayout(new GridLayout(2, false));
		connectGroup.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		Label lblPorts = new Label(connectGroup, SWT.NONE);
		lblPorts.setText(Messages.getString("Preferences.Port")); //$NON-NLS-1$
		lblPorts.setLayoutData(new GridData(SWT.NONE));
		ports = new Combo(connectGroup, SWT.SINGLE);
		ports.setItems(Connection.getComPorts());
		ports.setText(Hub.localCfg.get(PORT, Messages.getString("SysmexAction.DefaultPort"))); //$NON-NLS-1$
		
		String[] param = Hub.localCfg.get(PARAMS, "9600,8,n,1").split(","); //$NON-NLS-1$ //$NON-NLS-2$
		
		Label lblSpeed = new Label(connectGroup, SWT.NONE);
		lblSpeed.setText(Messages.getString("Preferences.Baud")); //$NON-NLS-1$
		lblSpeed.setLayoutData(new GridData(SWT.NONE));
		speed = new Text(connectGroup, SWT.BORDER);
		speed.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		speed.setText(param[0]);
		
		Label lblData = new Label(connectGroup, SWT.NONE);
		lblData.setText(Messages.getString("Preferences.Databits")); //$NON-NLS-1$
		lblData.setLayoutData(new GridData(SWT.NONE));
		data = new Text(connectGroup, SWT.BORDER);
		data.setText(param[1]);
		
		Label lblParity = new Label(connectGroup, SWT.NONE);
		lblParity.setText(Messages.getString("Preferences.Parity")); //$NON-NLS-1$
		lblParity.setLayoutData(new GridData(SWT.NONE));
		parity = new Button(connectGroup, SWT.CHECK);
		parity.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		parity.setSelection(!param[2].equalsIgnoreCase("n")); //$NON-NLS-1$
		
		Label lblStop = new Label(connectGroup, SWT.NONE);
		lblStop.setText(Messages.getString("Preferences.Stopbits")); //$NON-NLS-1$
		lblStop.setLayoutData(new GridData(SWT.NONE));
		stop = new Text(connectGroup, SWT.BORDER);
		stop.setText(param[3]);
		
		Label lblTimeout = new Label(connectGroup, SWT.NONE);
		lblTimeout.setText(Messages.getString("Preferences.Timeout")); //$NON-NLS-1$
		lblTimeout.setLayoutData(new GridData(SWT.NONE));
		String timeoutStr =
			Hub.localCfg.get(TIMEOUT, Messages.getString("SysmexAction.DefaultTimeout")); //$NON-NLS-1$
		timeout = new Text(connectGroup, SWT.BORDER);
		timeout.setText(timeoutStr);
		
		// Events
		models.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				modelChanged();
			}
		});
		
		modelChanged();
		return retComp;
	}
	
	private void modelChanged(){
		boolean visible = models.getText().equals(MODEL_KX21);
		lblRdw.setVisible(visible);
		rdw_types.setVisible(visible);
	}
	
	public void init(final IWorkbench workbench){
	// TODO Auto-generated method stub
	
	}
	
	@Override
	public boolean performOk(){
		StringBuilder sb = new StringBuilder();
		sb.append(speed.getText()).append(",").append(data.getText()).append( //$NON-NLS-1$
			",").append(parity.getSelection() ? "y" : "n").append(",") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			.append(stop.getText());
		Hub.localCfg.set(PARAMS, sb.toString());
		Hub.localCfg.set(PORT, ports.getText());
		Hub.localCfg.set(TIMEOUT, timeout.getText());
		Hub.localCfg.set(LOG, log.getSelection() ? "y" : "n"); //$NON-NLS-1$ //$NON-NLS-2$
		Hub.localCfg.set(BACKGROUND, background.getSelection() ? "y" : "n"); //$NON-NLS-1$ //$NON-NLS-2$
		Hub.localCfg.set(MODEL, models.getText());
		Hub.localCfg.set(RDW_TYP, rdw_types.getText());
		Hub.localCfg.flush();
		return super.performOk();
	}
}