package ch.elexis.actions;

public interface ElexisEventListener {
	/**
	 * An Event was fired
	 * @param ev the Event
	 */
	public void catchElexisEvent(ElexisEvent ev);
	
	/**
	 * Filter the events this listener wants to be informed
	 * @return An ElexisEvent with matching<ul>
	 * <li>object: Only events ov this object will be sent</li>
	 * <li>class: Only events ov this class wuill be sent</li>
	 * <li>type: Only eevnts matching to one ore more flags in type will be sent</li>
	 * </ul>
	 */
	public ElexisEvent getElexisEventFilter();
}
