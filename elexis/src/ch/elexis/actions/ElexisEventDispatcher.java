/*******************************************************************************
 * Copyright (c) 2009-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 * $Id: LazyTreeLoader.java 5317 2009-05-24 15:00:37Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import ch.elexis.ElexisException;
import ch.elexis.Hub;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.util.Log;

/**
 * The Elexis event dispatcher system manages and distributes the information of changing, creating,
 * deleting and selecting PersistentObjects. An event is fired when such an action occures. This
 * might be due to a user interaction or to an non-interactive job.
 * 
 * A view that handles user selection of PersistentObjects MUST fire an appropriate Event through
 * ElexisEventdispatcher.getinstance().fire(ElexisEvent ee) Notification of deletion, modification
 * and creation of PeristentObjects occurs transparently via the PersistentObject base class.
 * 
 * A client that wishes to be informed on such events must register an ElexisEventListener. The
 * catchElexisEvent() Method of this listener is called in a non-UI-thread an should be finished as
 * fast as possible. If lengthy operations are neccessary, these must be sheduled in a separate
 * thread, The Listener can specify objects, classes and event types it wants to be informed. If no
 * such filter is given, it will be informed about all events.
 * 
 * @author gerry
 * 
 */
public class ElexisEventDispatcher extends Job {
	private final LinkedList<ElexisEventListener> listeners;
	private static ElexisEventDispatcher theInstance;
	private final HashMap<Class<?>, IElexisEventDispatcher> dispatchers;
	private final HashMap<Class<?>, PersistentObject> lastSelection;
	private final LinkedList<ElexisEvent> eventQueue;
	private boolean bStop = false;
	private final Lock eventQueueLock = new ReentrantLock(true);
	private final Log log = Log.get("EventDispatcher");
	
	public static ElexisEventDispatcher getInstance(){
		if (theInstance == null) {
			theInstance = new ElexisEventDispatcher();
			theInstance.schedule();
		}
		return theInstance;
	}
	
	private ElexisEventDispatcher(){
		super("elexis event dispatcher");
		setSystem(true);
		setUser(false);
		setPriority(Job.DECORATE);
		listeners = new LinkedList<ElexisEventListener>();
		dispatchers = new HashMap<Class<?>, IElexisEventDispatcher>();
		lastSelection = new HashMap<Class<?>, PersistentObject>();
		eventQueue = new LinkedList<ElexisEvent>();
	}
	
	/**
	 * It is possible to register a dispatcher for a given class. If such a dispatcher exists, as an
	 * event of this class is fired, the event will be routed through that dispatcher. Only one
	 * dispatcher can be registered for a given class. The main purpose of this feature is to allow
	 * plugins to take care of their data classes by themselves.
	 * 
	 * @param ec
	 *            A Subclass of PersistentObject the dispatcher will take care of
	 * @param ied
	 *            the dispatcher to register
	 * @throws ElexisException
	 *             if there is already a dispatcher registered for that class.
	 */
	
	public void registerDispatcher(Class<? extends PersistentObject> ec, IElexisEventDispatcher ied)
	throws ElexisException{
		if (dispatchers.get(ec) != null) {
			throw new ElexisException(getClass(), "Duplicate dispatcher for " + ec.getName(),
				ElexisException.EE_DUPLICATE_DISPATCHER);
		}
		dispatchers.put(ec, ied);
	}
	
	/**
	 * Unregister a previosly registered dispatcher
	 * 
	 * @param ec
	 *            th class the dispatcher takes care of
	 * @param ied
	 *            the dispatcher to unregister
	 * @throws ElexisException
	 *             if the dispatcher was not registered, or if the class was registered with a
	 *             different dispatcher
	 */
	public void unregisterDispatcher(Class<? extends PersistentObject> ec,
		IElexisEventDispatcher ied) throws ElexisException{
		if (ied != dispatchers.get(ec)) {
			throw new ElexisException(getClass(), "Tried to remove unowned dispatcher "
				+ ec.getName(), ElexisException.EE_BAD_DISPATCHER);
		}
	}
	
	/**
	 * Add listeners for ElexisEvents. The listener tells the system via its getElexisEventFilter
	 * method, what classes it will catch. If a dispatcher for that class was registered, the call
	 * will be routed to that dispatcher.
	 * 
	 * @param el
	 *            one ore more ElexisEventListeners that have to return valid values on
	 *            el.getElexisEventFilter()
	 */
	public void addListeners(ElexisEventListener... els){
		synchronized (listeners) {
			for (ElexisEventListener el : els) {
				ElexisEvent ev = el.getElexisEventFilter();
				Class<?> cl = ev.getObjectClass();
				IElexisEventDispatcher ed = dispatchers.get(cl);
				if (ed != null) {
					ed.addListener(el);
				} else {
					listeners.add(el);
				}
			}
		}
	}
	
	/**
	 * remove listeners. If a listener was added before, it will be removed. Otherwise nothing will
	 * happen
	 * 
	 * @param el
	 *            The Listener to remove
	 */
	public void removeListeners(ElexisEventListener... els){
		synchronized (listeners) {
			for (ElexisEventListener el : els) {
				ElexisEvent ev = el.getElexisEventFilter();
				Class<?> cl = ev.getObjectClass();
				IElexisEventDispatcher ed = dispatchers.get(cl);
				if (ed != null) {
					ed.removeListener(el);
				} else {
					listeners.remove(el);
				}
			}
		}
	}
	
	/**
	 * Fire an ElexisEvent. The class concerned is named in ee.getObjectClass. If a dispatcher for
	 * that class was registered, the event will be forwarded to that dispatcher. Otherwise, it will
	 * be sent to all registered listeners. The call to the dispatcher or the listener will always
	 * be in a separate thread and not in the UI thread.So care has to be taken if the callee has to
	 * change the UI Note: Only one Event is dispatched at a given time. If more events arrive, they
	 * will be pushed into a FIFO-Queue. If more than one equivalent event is pushed into the queue,
	 * only the last entered will be dispatched.
	 * 
	 * @param ee
	 *            the event to fire.
	 */
	public void fire(ElexisEvent ee){
		if (ee.getType() == ElexisEvent.EVENT_SELECTED) {
			PersistentObject po = lastSelection.get(ee.getObjectClass());
			if (po != null) {
				if (po.equals(ee.getObject())) {
					return;
				}
			}
			lastSelection.put(ee.getObjectClass(), ee.getObject());
		} else if (ee.getType() == ElexisEvent.EVENT_DESELECTED) {
			lastSelection.remove(ee.getObjectClass());
		}
		IElexisEventDispatcher ied = dispatchers.get(ee.getObjectClass());
		if (ied != null) {
			ied.fire(ee);
		}
		eventQueueLock.lock();
		try {
			Iterator<ElexisEvent> it = eventQueue.iterator();
			while (it.hasNext()) {
				if (it.next().isSame(ee)) {
					it.remove();
				}
			}
			eventQueue.add(ee);
		} finally {
			eventQueueLock.unlock();
		}
	}
	
	/**
	 * find the last selected object of a given type
	 * 
	 * @param template
	 *            tha class defining the object to find
	 * @return the last object of the given type or null if no such object is selected
	 */
	public static PersistentObject getSelected(final Class<?> template){
		return getInstance().lastSelection.get(template);
	}
	
	/**
	 * inform the system that an object has been selected
	 * 
	 * @param po
	 *            the object that is selected now
	 */
	public static void fireSelectionEvent(PersistentObject po){
		if (po != null) {
			getInstance().fire(new ElexisEvent(po, po.getClass(), ElexisEvent.EVENT_SELECTED));
		}
	}
	
	/**
	 * inform the syste, that several objects have been selected
	 * @param objects
	 */
	public static void fireSelectionEvents(PersistentObject... objects ){
		if(objects!=null){
			for(PersistentObject po:objects){
				getInstance().fire(new ElexisEvent(po,po.getClass(),ElexisEvent.EVENT_SELECTED));
			}
		}
	}
	
	
	/**
	 * inform the system, that no object of the specified type is selected anymore
	 * 
	 * @param clazz
	 *            the class of which selection was removed
	 */
	public static void clearSelection(Class<?> clazz){
		if (clazz != null) {
			getInstance().fire(new ElexisEvent(null, clazz, ElexisEvent.EVENT_DESELECTED));
		}
	}
	
	/**
	 * inform the system, that all object of a specified class have to be reloaded from storage
	 * 
	 * @param clazz
	 *            the clazz whose objects are invalidated
	 */
	public static void reload(Class<?> clazz){
		if (clazz != null) {
			getInstance().fire(new ElexisEvent(null, clazz, ElexisEvent.EVENT_RELOAD));
		}
	}
	
	/**
	 * inform the system, that the specified object has changed some values or properties
	 * 
	 * @param po
	 *            the object that was modified
	 */
	public static void update(PersistentObject po){
		if (po != null) {
			getInstance().fire(new ElexisEvent(po, po.getClass(), ElexisEvent.EVENT_UPDATE));
		}
	}
	
	/** shortcut */
	public static Patient getSelectedPatient(){
		return (Patient) getSelected(Patient.class);
	}
	
	public void shutDown(){
		bStop = true;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor){
		while (!bStop) {
			ElexisEvent ee = null;
			if (eventQueueLock.tryLock()) {
				try {
					if (!eventQueue.isEmpty()) {
						ee = eventQueue.removeFirst();
					}
				} finally {
					eventQueueLock.unlock();
				}
			}
			if (ee != null) {
				if (Hub.plugin.DEBUGMODE) {
					log.log(ee.getObjectClass().getName(), Log.DEBUGMSG);
				}
				synchronized (listeners) {
					for (ElexisEventListener l : listeners) {
						if (ee.matches(l.getElexisEventFilter())) {
							l.catchElexisEvent(ee);
						}
					}
				}
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException iex) {
					// janusode
				}
			}
			
		}
		return Status.OK_STATUS;
	}
}
