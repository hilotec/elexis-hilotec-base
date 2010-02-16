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
 *    $Id: ElexisEventCascade.java 6147 2010-02-16 14:49:46Z rgw_ch $
 *******************************************************************************/
package ch.elexis.actions;

import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;

/**
 * This cascade makes sure, that the three central elements are always selected synchroneusly: Patient,
 * Case and consultation.
 * @author gerry
 *
 */
public class ElexisEventCascade {
	private static ElexisEventCascade theInstance;
	
	public static ElexisEventCascade getInstance(){
		if (theInstance == null) {
			theInstance = new ElexisEventCascade();
		}
		return theInstance;
	}
	
	private final ElexisEventListenerImpl eeli_pat =
		new ElexisEventListenerImpl(Patient.class, ElexisEvent.EVENT_DESELECTED
			| ElexisEvent.EVENT_SELECTED) {
		@Override
		public void catchElexisEvent(ElexisEvent ev){
			if (ev.getType() == ElexisEvent.EVENT_SELECTED) {
				Patient pat = (Patient) ev.getObject();
				Konsultation k = pat.getLetzteKons(false);
				if (k == null) {
					ElexisEventDispatcher.getInstance()
					.fire(
						new ElexisEvent(null, Konsultation.class,
							ElexisEvent.EVENT_DESELECTED));
					ElexisEventDispatcher.getInstance().fire(
						new ElexisEvent(null, Fall.class, ElexisEvent.EVENT_DESELECTED));
				} else {
					ElexisEventDispatcher.fireSelectionEvents(k, k.getFall());
				}
			}else if(ev.getType()==ElexisEvent.EVENT_DESELECTED){
				ElexisEventDispatcher.getInstance().fire(
					new ElexisEvent(null, Fall.class, ElexisEvent.EVENT_DESELECTED));
				ElexisEventDispatcher.getInstance()
				.fire(
					new ElexisEvent(null, Konsultation.class,
						ElexisEvent.EVENT_DESELECTED));
			}
		}
		
	};
	private final ElexisEventListenerImpl eeli_fall=new ElexisEventListenerImpl(Fall.class,ElexisEvent.EVENT_SELECTED){
		public void catchElexisEvent(ElexisEvent ev){
			Fall fall=(Fall)ev.getObject();
			Patient pat=fall.getPatient();
			if(pat!=null){
				ElexisEventDispatcher.fireSelectionEvent(pat);
				Konsultation[] k=fall.getBehandlungen(true);
				if(k!=null && k.length>0){
					ElexisEventDispatcher.fireSelectionEvents(k[0]);
				}
			}
		}
	};
	
	private final ElexisEventListenerImpl eeli_kons=new ElexisEventListenerImpl(Konsultation.class,ElexisEvent.EVENT_SELECTED){
		public void catchElexisEvent(ElexisEvent ev){
			Konsultation k=(Konsultation) ev.getObject();
			Fall fall=k.getFall();
			if(fall!=null){
				Patient pat=fall.getPatient();
				ElexisEventDispatcher.fireSelectionEvents(pat,fall);
			}
		}
	};
	
	public void start(){
		ElexisEventDispatcher.getInstance().addListeners(eeli_fall,eeli_kons,eeli_pat);
	}
	
	public void stop(){
		ElexisEventDispatcher.getInstance().removeListeners(eeli_fall,eeli_kons,eeli_pat);
	}
	
	private ElexisEventCascade(){}
}
