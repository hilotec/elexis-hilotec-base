/*******************************************************************************
 * Copyright (c) 2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *    $Id: SampleVIew.java 6094 2010-02-09 06:06:06Z rgw_ch $
 *******************************************************************************/

package ch.elexis.developer.resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.actions.ElexisEvent;
import ch.elexis.actions.ElexisEventDispatcher;
import ch.elexis.actions.ElexisEventListenerImpl;
import ch.elexis.actions.FlatDataLoader;
import ch.elexis.actions.GlobalEventDispatcher;
import ch.elexis.actions.GlobalEventDispatcher.IActivationListener;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.SimpleWidgetProvider;
import ch.elexis.util.viewers.ViewerConfigurer;

/**
 * This is a sample view to demonstrate how to connect to elexis's event scheduler and how to
 * display elexis's data types.
 * We implement IActivationListener to be informed, wenn the user can see our View. All UI funktions 
 * should only be active in that case.
 * @author gerry
 *
 */
public class SampleVIew extends ViewPart implements IActivationListener{

	/**
	 * CommonViewer is a "golden hammer". Use it for fast prototyping. In many cases
	 * you probably want to develop more spezialiced viewers.
	 */
	CommonViewer cv;
	
	/**
	 * A Common Viewer is always controlled by a ViewerConfigurer. So you'll never have only
	 * one of them
	 */
	ViewerConfigurer vc;
	
	/**
	 * A FlatDataLoader reads data from a table into a list of Objects
	 */
	FlatDataLoader fdl;

	/**
	 * This is a default implemetation of an ElexisEventListener. It listens for SelectionEVents
	 * and UnselectionEvents on Instances of the Patient class.
	 */

	ElexisEventListenerImpl eeli_pat=new ElexisEventListenerImpl(Patient.class){
		/**
		 * Wen can override runInUi (which runs in the UI thread) or catchElexisEvent which runs
		 * in a non-UI thread. In any case the method must return quickly. For lenghty operations, start
		 * a seperate thread. 
		 */
	
		@Override
		public void catchElexisEvent(ElexisEvent ev) {
			// do something inmtelligent, that needs not to run in the UI thread
		}
		
		
	};

	/**
	 * This is a default implemetation of an ElexisEventListener. It listens for several types of
	 * events on Instances of the SampleDataType class.
	 */
	ElexisEventListenerImpl eeli_sdt=new ElexisEventListenerImpl(SampleDataType.class,ElexisEvent.EVENT_SELECTED|ElexisEvent.EVENT_RELOAD|ElexisEvent.EVENT_UPDATE){

		@Override
		public void runInUi(ElexisEvent ev) {
			// do Something intelligent that must run in the UI thread
			switch(ev.getType()){
			case ElexisEvent.EVENT_SELECTED:
				// do somenthing great with selection;
				break;
			case ElexisEvent.EVENT_RELOAD:
				// reload the contents of the viewer
				break;
			case ElexisEvent.EVENT_UPDATE:
				SampleDataType sdt=(SampleDataType)ev.getObject();
				// sdt has been modified. We do react on this action here
				break;
			}
		}
		
	};
	
		public SampleVIew() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * This is the right place to
	 * create all UI elements. The parent composite already has a GridLayout.
	 */
	@Override
	public void createPartControl(Composite parent) {
		cv=new CommonViewer();
		Query<SampleDataType> qbe=new Query<SampleDataType>(SampleDataType.class);
		fdl=new FlatDataLoader(cv,qbe);
		
		vc=new ViewerConfigurer(fdl, new SampleDataLabelProvider(), new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST, SWT.NONE, cv));
		// At this point the viewer is created
		cv.create(vc, parent, SWT.NONE, getViewSite());
		// We want to be informed, when the part becomes visible
		GlobalEventDispatcher.addActivationListener(this, this);
	}

	/**
	 * On disposal,the IActivationListener MUST be unregistered
	 */
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	/**
	 * From IActivationListener: the view was activated or inactivated
	 * @param mode
	 */
	public void activation(boolean mode) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * From IActivationListener: The View changes visibility
	 * Our listeners need only to be active,if gthe view is visible. So we untregister them if the
	 * view disappears to save resources.
	 * If the view becomes visible again, we must send an event, because the listeners don't know
	 * what happend, before the start listening
	 * @param mode true: the view becomes visible. false: the view becomes invisible
	 */
	public void visible(boolean mode) {
		if(mode){
			eeli_pat.catchElexisEvent(ElexisEvent.createPatientEvent());
			eeli_sdt.catchElexisEvent(new ElexisEvent(null,SampleDataType.class,ElexisEvent.EVENT_RELOAD));
			ElexisEventDispatcher.getInstance().addListeners(eeli_pat,eeli_sdt);
		}else{
			ElexisEventDispatcher.getInstance().removeListeners(eeli_pat,eeli_sdt);
		}
		
	}

}
