package ch.elexis.importers;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class ImporterActionDelegate implements IWorkbenchWindowActionDelegate {

	IWorkbenchWindow myWindow;
	
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		myWindow=window;
	}

	public void run(IAction action) {
		KontaktImporterDialog kib=new KontaktImporterDialog(myWindow.getShell());
		kib.open();

	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
