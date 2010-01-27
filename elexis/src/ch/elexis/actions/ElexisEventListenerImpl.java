package ch.elexis.actions;

import ch.elexis.Desk;
import ch.elexis.data.PersistentObject;

/**
 * An implementation of the most common uses of ElexisSventListeners. Subclasses must override
 * one of catchElexisEvent (non ui thread) or runInUi (the event is forwarded in an async UI thread)
 * @author gerry
 *
 */
public class ElexisEventListenerImpl implements ElexisEventListener{
	private final ElexisEvent template;

	public ElexisEventListenerImpl(final Class<?> clazz){
		template=new ElexisEvent(null,clazz,ElexisEvent.EVENT_SELECTED|ElexisEvent.EVENT_DESELECTED);		
	}

	public ElexisEventListenerImpl(final Class<?> clazz, int mode){
		template=new ElexisEvent(null,clazz,mode);
	}
	
	public ElexisEventListenerImpl(final PersistentObject obj, final Class<?> clazz, final int mode){
		template=new ElexisEvent(obj,clazz,mode);
	}
	public ElexisEvent getElexisEventFilter() {
		return template;
	}

	/**
	 * This catches the Event from the EventDispatcher, which is in a Non-UI Thread by
	 * definition
	 */
	public void catchElexisEvent(final ElexisEvent ev) {
		Desk.asyncExec(new Runnable() {
			public void run() {
				runInUi(ev);
			}
		});
	}
	
	/**
	 * This runs the event in an UI Thread
	 * @param ev
	 */
	public void runInUi(ElexisEvent ev){}
	
}
