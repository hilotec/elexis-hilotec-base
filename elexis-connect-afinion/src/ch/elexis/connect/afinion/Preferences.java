package ch.elexis.connect.afinion;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import ch.elexis.rs232.AuslesenDialog;
import ch.elexis.rs232.Connection;
import ch.elexis.rs232.LogConnection;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;

public class Preferences extends PreferencePage implements
	IWorkbenchPreferencePage {

public static final String AFINION_BASE="connectors/afinion/";
public static final String PORT=AFINION_BASE+"port";
public static final String PARAMS=AFINION_BASE+"params";
public static final String LOG=AFINION_BASE+"log";

Combo ports;
Text speed,data,stop, timeout, logFile;
Button parity, log;
public Preferences() {
	super(Messages.getString("AfinionAS100Action.ButtonName"));
	setPreferenceStore(new SettingsPreferenceStore(Hub.localCfg));
}
@Override
protected Control createContents(final Composite parent) {
	Hub.log.log("Start von createContents", Log.DEBUGMSG);
	String[] param=Hub.localCfg.get(PARAMS, "9600,8,n,1,20").split(",");
	
	Composite ret=new Composite(parent,SWT.NONE);
	ret.setLayout(new GridLayout(2,false));
	ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
	new Label(ret,SWT.NONE).setText(Messages.getString("Preferences.Port"));
	ports=new Combo(ret,SWT.SINGLE);
	ports.setItems(Connection.getComPorts());
	ports.setText(Hub.localCfg.get(PORT, Messages.getString("AfinionAS100Action.DefaultPort")));
	new Label(ret,SWT.NONE).setText(Messages.getString("Preferences.Baud"));
	speed=new Text(ret,SWT.BORDER);
	speed.setText(param[0]);
	new Label(ret,SWT.NONE).setText(Messages.getString("Preferences.Databits"));
	data=new Text(ret,SWT.BORDER);
	data.setText(param[1]);
	new Label(ret,SWT.NONE).setText(Messages.getString("Preferences.Parity"));
	parity=new Button(ret,SWT.CHECK);
	parity.setSelection(!param[2].equalsIgnoreCase("n"));
	new Label(ret,SWT.NONE).setText(Messages.getString("Preferences.Stopbits"));
	stop=new Text(ret,SWT.BORDER);
	stop.setText(param[3]);
	new Label(ret, SWT.NONE).setText(Messages
			.getString("Preferences.Timeout"));
	timeout = new Text(ret, SWT.BORDER);
	String timeoutStr = "20";
	if (param.length > 4) {
		timeoutStr = param[4];
	}
	timeout.setText(timeoutStr);
	new Label(ret,SWT.NONE).setText(Messages.getString("Preferences.Log"));
	log=new Button(ret,SWT.CHECK);
	log.setSelection(Hub.localCfg.get(LOG, "n").equalsIgnoreCase("y"));
	
	// Input lesen
	Group group = new Group(ret, SWT.FILL);
	group.setLayout(new GridLayout(2, false));
	group.setText("Daten auslesen");

	new Label(group, SWT.NONE).setText("Logdatei");
	logFile = new Text(group, SWT.BORDER);
	logFile.setText("C:/afinion_data.txt");

	Button btnStart = new Button(group, SWT.NONE);
	btnStart.setText("Start");

	btnStart.addSelectionListener(new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			startTest();
		}
	});

	return ret;
}

/**
 * Liest Wert des Text-Widgets
 */
private String getText(Text text, String defaultText) {
	String retText = "";
	if (text != null) {
		retText = text.getText();
	}
	if (retText == null || retText.length() == 0) {
		retText = defaultText;
	}
	return retText;
}

/**
 * Startet Auslesen der Schnittstelle
 */
private void startTest() {
	String logFileName = getText(logFile, null);
	if (logFileName == null) {
		SWTHelper
				.showError("Schnittstelle auslesen", "Logdatei unbekannt!");
	}
	File file = new File(logFileName);
	try {
		if (!file.exists()) {
			file.createNewFile();
		}
		AuslesenDialog dialog = new AuslesenDialog(getShell(), "Afinion S100");
		final LogConnection connection = new LogConnection(Messages
				.getString("ReflotronSprintAction.ConnectionName"),
				Hub.localCfg.get(Preferences.PORT, Messages
						.getString("ReflotronSprintAction.DefaultPort")),
				Hub.localCfg.get(Preferences.PARAMS, Messages
						.getString("ReflotronSprintAction.DefaultParams")),
						dialog, logFileName);
		dialog.setConnection(connection);
		dialog.open();
	} catch (IOException e) {
		SWTHelper.showError("Schnittstelle auslesen", e.getMessage());
	}
}

public void init(final IWorkbench workbench) {
	// TODO Auto-generated method stub

}

@Override
public boolean performOk() {
	StringBuilder sb=new StringBuilder();
	sb.append(speed.getText()).append(",")
		.append(data.getText()).append(",")
		.append(parity.getSelection() ? "y" : "n").append(",")
		.append(stop.getText()).append(",").append(timeout.getText());
	Hub.localCfg.set(PARAMS, sb.toString());
	Hub.localCfg.set(PORT, ports.getText());
	Hub.localCfg.set(LOG, log.getSelection() ? "y" : "n");
	Hub.localCfg.flush();
	return super.performOk();
}
}