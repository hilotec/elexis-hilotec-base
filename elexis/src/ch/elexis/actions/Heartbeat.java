/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: Heartbeat.java 2903 2007-07-25 10:34:21Z danlutz $
 *******************************************************************************/

package ch.elexis.actions;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.preferences.PreferenceConstants;

/**
 * Heartbeat is an event source, that fires events at user-definable intervals to all HeartListeners.
 * All actions that must be repeated regularly should be registered as HeartListener. They will all be 
 * called at about the specified rate, but not in a guaranteed particular order and not necessarily at
 * exactly identical intervals. 
 * 
 * Heartbeat löst das Pinger-Konzept ab. Der Heartbeat ist ein Singleton, das 
 * alle Hub.localCfg.get(heartbeatrate,30) Sekunden einen Event feuert. Wer reglmässige Aktionen
 * durchführen will, kann sich als HeartbeatListener registrieren.
 * Dieses Konzept hat gegenüber individuellen update-Threads den Vorteil, dass die Netzwerk- und
 * Datenbankbelastung, sowie die Zahl der gleichzeitig laufenden Threads limitiert wird.
 * Der Heartbeat sorgt dafür, dass die listener der Reihe nach (aber ncht in einer definierten Reihenfolge)
 * aufgerufen werden.  
 * @author gerry
 *
 */
public class Heartbeat {
	private beat theBeat;
	private Timer pacer;
	private boolean isSuspended;
	private static Heartbeat theHeartbeat;
	private LinkedList<HeartListener> listeners;
	
	
	private Heartbeat(){
		theBeat=new beat();
		listeners=new LinkedList<HeartListener>();
		pacer=new Timer(true);
		int interval=Hub.localCfg.get(PreferenceConstants.ABL_HEARTRATE, 30); //$NON-NLS-1$
		isSuspended=true;
		pacer.schedule(theBeat, 0, interval*1000L);
	}
	
	/**
	 * Das Singleton holen
	 * @return den Heartbeat der Anwendung
	 */
	public static Heartbeat getInstance(){
		if(theHeartbeat==null){
			theHeartbeat=new Heartbeat();		
		}
		return theHeartbeat;
	}
	
	/**
	 * Heartbeat (wieder) laufen lassen.
	 * @param immediately true: Sofort einen ersten beat losschicken, false: im normalen 
	 * Rhythmus bleiben.
	 */
	public void resume(boolean immediately){
		isSuspended=false;
		if(immediately){
			theBeat.run();
		}
	}
	/**
	 * Heartbeat aussetzen (geht im Hintergrund weiter, wird aber nicht mehr weitergeleitet)
	 */
	public void suspend(){
		isSuspended=true;
	}
	
	/**
	 * Heartbeat stoppen (kann dann nicht mehr gestartet werden)
	 */
	public void stop(){
		pacer.cancel();
	}
	/**
	 * Einen Listener registrieren. Achtung: Muss unbedingt mit removeListener deregistriert
	 * werden 
	 * @param listen der Listener
	 */
	public void addListener(HeartListener listen){
		listeners.add(listen);
	}
	
	/**
	 * Einen Listener wieder austragen
	 * @param listen
	 */
	public void removeListener(HeartListener listen){
		listeners.remove(listen);
	}
	/** 
	 * we beat asynchronously, because most listeners will update their
	 * views 
	 * @author Gerry
	 *
	 */
	private class beat extends TimerTask{
		@Override
		public void run() {
			if(!isSuspended){
				Desk.theDisplay.asyncExec(new Runnable(){
					public void run(){
						for(HeartListener l:listeners){
							l.heartbeat();
						}		
					}
				});
			}
		}
		
	}
	public interface HeartListener{
		/**
		 * Die Methode heartbeat wird in "einigermassen" regelmässigen 
		 * (aber nicht garantiert immer genau identischen) Abständen aufgerufen 
		 *
		 */
		public void heartbeat();
	}
}
