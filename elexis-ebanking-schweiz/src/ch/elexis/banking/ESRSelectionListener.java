package ch.elexis.banking;

import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;

import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.PersistentObject;

/**
 * Eigentlich nur zur Demonstration, dass ein Selectionlistener auch unabhängig von einer View existieren kann
 * @author gerry
 *
 */
public class ESRSelectionListener implements SelectionListener {
	private IWorkbenchWindow win;

	ESRSelectionListener(IViewSite site) {
		win=site.getWorkbenchWindow();
	}
	
	void activate(boolean mode){
		if(mode){
			GlobalEvents.getInstance().addSelectionListener(this);
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
	}
	public void clearEvent(Class template) {
		

	}

	public void selectionEvent(PersistentObject obj) {
		if(obj instanceof ESRRecord){
			ESRRecord esr=(ESRRecord)obj;
			GlobalEvents.getInstance().fireSelectionEvent(esr.getRechnung());
		}

	}

}
