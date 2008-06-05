/*******************************************************************************
 * Copyright (c) 2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: PatListFilterBox.java 4005 2008-06-05 12:14:42Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.util.ListDisplay;
import ch.elexis.util.PersistentObjectDropTarget;

/**
 * This will be displayed on top of the PatientListeView. It allows to drop Objects (Artikel, 
 * IVerrechnet, IDiagnose etc.) as filter conditions.
 * The PatListFilterBox will also be added as an IFilter to the StructuredViewer that displays
 * the patients thus allowing to filter the list according to the conditions.
 * 
 * The Objects that can act as filter conditions must e declared as IPatFilter. Later, we'll define
 * an extension point for Plugins to connect their classes.
 * @author Gerry
 *
 */
public class PatListFilterBox extends ListDisplay<PersistentObject> implements IFilter{
    PersistentObjectDropTarget dropTarget;
    private static final String FELD_HINZU="Feld...";
    private static final String LEEREN="Leeren";
    private ArrayList<IPatFilter> filters=new ArrayList<IPatFilter>();
    private IPatFilter defaultFilter=new PatFilterImpl();
    private boolean parseError=false;
    
	PatListFilterBox(Composite parent){
		super(parent,SWT.NONE,null);
		setDLDListener(new LDListener(){

			public String getLabel(Object o) {
				if(o instanceof PersistentObject){
					return o.getClass().getSimpleName()+":"+((PersistentObject)o).getLabel();
				}else{
					return o.toString();
				}
			}

			public void hyperlinkActivated(String l) {
				clear();
			}});
		addHyperlinks(LEEREN);
		dropTarget=new PersistentObjectDropTarget("Statfilter",this,new DropReceiver());

	}
	private  class DropReceiver implements PersistentObjectDropTarget.Receiver {
		public void dropped(final PersistentObject o, final DropTargetEvent ev) {
			PatListFilterBox.this.add(o);
		}

		public boolean accept(final PersistentObject o) {
			return true;
		}
	}
	
	public void reset(){
		parseError=false;
	}
	/**
	 * We select the Patient with an AND operation running over all filter conditions
	 * If no filter was registered for a type, we use our defaultFilter
	 * @throws Exception 
	 */
	public boolean select(Object toTest) {
		if(parseError){
			return false;
		}
		if(toTest instanceof Patient){
			Patient p=(Patient)toTest;

			for(PersistentObject cond:getAll()){
				boolean handled=false;
				for(IPatFilter filter:filters){
					int result=filter.accept(p, cond);
					if(result==IPatFilter.REJECT){
						return false;
					}
					if(result==IPatFilter.ACCEPT){
						handled=true;
					}else if(result==IPatFilter.FILTER_FAULT){
						parseError=true;
					}
					
				}
				if(!handled){
					int result=defaultFilter.accept(p, cond);
					if(result==IPatFilter.REJECT){
						return false;
					}
					if(result==IPatFilter.FILTER_FAULT){
						parseError=true;
					}
				}

			}
			return true; // Only if all conditions accept or don't handle
		}
		return false;
	}

	public void addPatFilter(IPatFilter filter){
		filters.add(filter);
	}
	public void removeFilter(IPatFilter filter){
		filters.remove(filter);
	}
	
	public interface IPatFilter{
		/** The Patient is not selected with the filter object */
		public static final int REJECT=-1;
		/** The Patient is selected with the filter objct */
		public static final int ACCEPT=1;
		/** We do not handle this type of filter object */
		public static final int DONT_HANDLE=0;
		/** We encountered an error while trying to filter */
		public static final int FILTER_FAULT=-2;
		
		/**
		 * Will the Patient be accepted for the Filter depending on the Object? 
		 * @param p The Patient to consider
		 * @param o The Object to check
		 * @return one of REJECT, ACCEPT, DONT_HANDLE
		 * @throws Exception 
		 */
		public int accept(Patient p, PersistentObject o);
	}
}
