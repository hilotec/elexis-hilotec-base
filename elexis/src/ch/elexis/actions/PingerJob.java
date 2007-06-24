/*******************************************************************************
 * Copyright (c) 2005, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: PingerJob.java 23 2006-03-24 15:36:01Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import ch.elexis.Hub;

/** 
 * Ein Job, der in regelmässigen Abständen interessierte Klienten
 * benachrichtigt
 * @author Gerry
 *
 */
public class PingerJob extends Job {

	private LinkedList<ListenerDef> listeners;
	
	public PingerJob(){
		super("PingerJob");
		setSystem(true);
		setPriority(Job.DECORATE);
		listeners=new LinkedList<ListenerDef>();
	}

	/** 
	 * Interesse für regelmässige Benachrichtigung anmelden. Es wird
	 * garantiert, dass der nächste ping nicht früher als nach freqInSeconds
	 * Sekunden erfolgt, aber er kann auch später erfolgen.
	 * @param lis derf listener
	 * @param freqInSeconds alle wieviele Sekunden benachrichtig werden
	 * soll
	 */
	public void addListener(Listener lis, int freqInSeconds){
		listeners.add(new ListenerDef(lis,1000*freqInSeconds));
	}
	
	public void removeListener(Listener lis){
		for(ListenerDef l:listeners){
			if(l.listen.equals(lis)){
				listeners.remove(l);
			}
		}
	}
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		for(ListenerDef l:listeners){
			l.ping();
		}
		schedule(Hub.localCfg.get("pingfreq",30)*1000);
		return Status.OK_STATUS;
	}
	
	class ListenerDef{
		private long freq;
		private long nextPing;
		Listener listen;
		
		ListenerDef(Listener lis, int freqInMillis){
			freq=freqInMillis;
			listen=lis;
		}
		
		void ping(){
			long act=System.currentTimeMillis();
			if(nextPing<act){
				listen.pingEvent();
				nextPing=act+freq;
			}
		}
	}
	
	public static interface Listener{
		public void pingEvent();
	}
	

}
