package ch.elexis;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ObjectListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.Anwender;
import ch.elexis.data.PersistentObject;

public abstract class ElexisViewPart extends ViewPart implements SelectionListener{
	
	public abstract void userChanged(Anwender actUser);

	@Override
	public void init(IViewSite site) throws PartInitException {
		GlobalEvents.getInstance().addSelectionListener(this);
		super.init(site);
	}


	public void clearEvent(Class<? extends PersistentObject> template) {
		if(template.equals(Anwender.class)){
			userChanged(null);
		}
		
	}

	public void selectionEvent(PersistentObject obj) {
		if(obj instanceof Anwender){
			userChanged((Anwender)obj);
		}
	}
	

}
