package ch.elexis.labor.medics.order;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;

import ch.elexis.Hub;
import ch.elexis.labor.medics.MedicsActivator;

/**
 * Diese Aktion wird nur benötigt um die Funktionalität der DropDown-Buttons der View zu testen.
 * 
 * @author immi
 * 
 */
public class LabOrderAction2 extends Action {
	
	public LabOrderAction2(){
		setId("laborder2"); //$NON-NLS-1$
		setImageDescriptor(MedicsActivator.getImageDescriptor("rsc/dummy.png")); //$NON-NLS-1$
		setText("Zweiter Labor Auftrag (nur Test)"); //$NON-NLS-1$
	}
	
	@Override
	public void run(){
		MessageDialog.openInformation(Hub.getActiveShell(), "Info", "Zweiter Labor Auftrag (nur Test)"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
