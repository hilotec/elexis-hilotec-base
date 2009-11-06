/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and Elexis
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
import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import ch.elexis.ElexisException;
import ch.elexis.data.PersistentObject;

public class ElexisEventDispatcher {
	private final LinkedList<ElexisEventListener> listeners;
	private static ElexisEventDispatcher theInstance;
	private final LinkedList<DispatchJob> jobsWaiting;
	private final HashMap<Class<? extends PersistentObject>, IElexisEventDispatcher> dispatchers;

	public static ElexisEventDispatcher getInstance() {
		if (theInstance == null) {
			theInstance = new ElexisEventDispatcher();
		}
		return theInstance;
	}

	private ElexisEventDispatcher() {
		listeners = new LinkedList<ElexisEventListener>();
		jobsWaiting = new LinkedList<DispatchJob>();
		dispatchers = new HashMap<Class<? extends PersistentObject>, IElexisEventDispatcher>();
	}

	/**
	 * It is possible to register a dispatcher for a given class. If such a
	 * dispatcher exists, as an event of this class is fired, the event will be
	 * routed through that dispatcher. Only one dispatcher can be registered for
	 * a given class.
	 * 
	 * @param ec
	 *            A Subclass of PersistzentObject the dispatcher will take care
	 *            of
	 * @param ied
	 *            the dispatcher to register
	 * @throws ElexisException
	 *             if there is already a dispatcher registered for that class.
	 */

	public void registerDispatcher(Class<? extends PersistentObject> ec,
			IElexisEventDispatcher ied) throws ElexisException {
		if (dispatchers.get(ec) != null) {
			throw new ElexisException(getClass(), "Duplicate dispatcher for "
					+ ec.getName(), ElexisException.EE_DUPLICATE_DISPATCHER);
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
	 *             if the dispatcher was not registered, or if the class was
	 *             registered with a different dispatcher
	 */
	public void unregisterDispatcher(Class<? extends PersistentObject> ec,
			IElexisEventDispatcher ied) throws ElexisException {
		if (ied != dispatchers.get(ec)) {
			throw new ElexisException(getClass(),
					"Tried to remove unowned dispatcher " + ec.getName(),
					ElexisException.EE_BAD_DISPATCHER);
		}
	}

	/**
	 * Add a listener for ElexisEvents. The listener tells the system via its
	 * getElexisEventFilter method, what classes it will catch. If a dispatcher
	 * for that class was registered, the call will be routed to that
	 * dispatcher.
	 * 
	 * @param el
	 *            an ElexisEventListener that has to return valid values on
	 *            el.getElexisEventFilter()
	 */
	public void addListener(ElexisEventListener el) {
		Class<? extends PersistentObject> ec = el.getElexisEventFilter()
				.getObjectClass();
		IElexisEventDispatcher ed = dispatchers.get(ec);
		if (ed != null) {
			ed.addListener(el);
		} else {
			listeners.add(el);
		}
	}

	/**
	 * remove a listener. If the listener was added, it will be removed.
	 * Otherwise nothing will happen
	 * 
	 * @param el
	 *            The Listener to remove
	 */
	public void removeListener(ElexisEventListener el) {
		Class<? extends PersistentObject> ec = el.getElexisEventFilter()
				.getObjectClass();
		IElexisEventDispatcher ed = dispatchers.get(ec);
		if (ed != null) {
			ed.removeListener(el);
		} else {
			listeners.remove(el);
		}
	}

	/**
	 * Fire an ElexisEvent. The class concerned is named in ee.getObjectClass.
	 * If a dispatcher for that class was registered, the event will be
	 * forwarded to that dispatcher. Otherwise, it will be sent to all
	 * registered listeners. The call to the dispatcher or the listener will
	 * always be in a separate thread and not in the UI thread.So care has to be
	 * taken if the callee has to change the UI
	 * 
	 * @param ee
	 *            the event to fire.
	 */
	public void fire(ElexisEvent ee) {
		DispatchJob job = null;

		if (jobsWaiting.size() == 0) {
			job = new DispatchJob();
			job.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					jobsWaiting.add((DispatchJob) event.getJob());
					super.done(event);
				}

			});
		} else {
			job = jobsWaiting.remove();
		}
		job.setEvent(ee);
		job.schedule();
	}

	private class DispatchJob extends Job {
		private ElexisEvent event;

		public DispatchJob() {
			super("Dispatch Elexis events");
			setSystem(true);
			setUser(false);
			setPriority(Job.DECORATE);
		}

		public void setEvent(ElexisEvent event) {
			this.event = event;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			IElexisEventDispatcher ied = dispatchers.get(event.getClass());
			if (ied != null) {
				ied.fire(event);
			} else {
				for (ElexisEventListener l : listeners) {
					if (event.matches(l.getElexisEventFilter())) {
						l.catchElexisEvent(event);
					}
				}
			}
			return Status.OK_STATUS;
		}

	}
}
