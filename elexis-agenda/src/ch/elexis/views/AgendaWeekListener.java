package ch.elexis.views;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.BackingStoreListener;
import ch.elexis.actions.GlobalEvents.ObjectListener;
import ch.elexis.actions.Heartbeat.HeartListener;
import ch.elexis.agenda.data.Termin;
import ch.elexis.data.Anwender;
import ch.elexis.data.PersistentObject;

public class AgendaWeekListener implements BackingStoreListener, HeartListener,
		ActivationListener, ObjectListener {

	AgendaWeek parent;
	
	AgendaWeekListener(AgendaWeek mine){
		parent=mine;
		GlobalEvents.getInstance().addBackingStoreListener(this);
		GlobalEvents.getInstance().addActivationListener(this, mine.getViewSite().getPart());
		parent.pinger=new ch.elexis.actions.Synchronizer(parent);
	}
	public void reloadContents(Class<? extends PersistentObject> clazz) {
		if(clazz.equals(Termin.class)){
			Desk.theDisplay.asyncExec(new Runnable(){
				public void run() {
					parent.reload();
				}});
		}else if(clazz.equals(Anwender.class)){
			parent.updateActions();
		}

	}

	public void heartbeat() {
		parent.pinger.doSync();
	}

	public void activation(boolean mode) {
		// nothing
	}

	public void visible(boolean mode) {
		if(mode==true){
			Hub.heart.addListener(this);
			heartbeat();
		}else{
			Hub.heart.removeListener(this);
		}
	}
	public void objectChanged(PersistentObject o) {
	
		
	}
	public void objectCreated(PersistentObject o) {
		// TODO Auto-generated method stub
		
	}
	public void objectDeleted(PersistentObject o) {
		// TODO Auto-generated method stub
		
	}

}
