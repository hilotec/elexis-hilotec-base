package ch.elexis.actions;

public abstract class ElexisReloadEventListenerImpl implements ElexisEventListener {
	private final ElexisEvent eetmpl;
	
	public ElexisReloadEventListenerImpl(Class<?>clazz){
		eetmpl=new ElexisEvent(null,clazz,ElexisEvent.EVENT_RELOAD);
	}

	public ElexisEvent getElexisEventFilter() {
		return eetmpl;
	}

}
