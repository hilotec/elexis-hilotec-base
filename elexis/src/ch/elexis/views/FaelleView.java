/*******************************************************************************
 * Copyright (c) 2006, G. Weirich, D. Lutz, P. Schönbucher and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: KonsultationView.java 535 2006-07-07 20:17:04Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import static ch.elexis.actions.GlobalActions.*;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.BackingStoreListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.Fall;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.util.DefaultLabelProvider;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;

/**
 * Eine alternative, platzsparendere Fälle-View
 */
public class FaelleView extends ViewPart implements ActivationListener, SelectionListener, BackingStoreListener{
	public static final String ID="ch.elexis.schoebufaelle"; //$NON-NLS-1$
	TableViewer tv;
	ViewMenus menus;
	
	public FaelleView() {

	}

	@Override
	public void createPartControl(Composite parent) {
		setPartName(Messages.getString("FaelleView.partName")); //$NON-NLS-1$
		parent.setLayout(new GridLayout());
		tv=new TableViewer(parent);
		tv.getControl().setLayoutData(SWTHelper.getFillGridData(1,true, 1, true));
		tv.setContentProvider(new FaelleContentProvider());
		tv.setLabelProvider(new FaelleLabelProvider());
		tv.addSelectionChangedListener(GlobalEvents.getInstance().getDefaultListener());
		menus=new ViewMenus(getViewSite());
		menus.createToolbar(neuerFallAction);
		menus.createViewerContextMenu(tv, delFallAction,openFallaction,reopenFallAction,makeBillAction);
		GlobalEvents.getInstance().addActivationListener(this, this);
		GlobalEvents.getInstance().addBackingStoreListener(this);
		tv.setInput(getViewSite());
	}

	@Override
	public void dispose(){
		GlobalEvents.getInstance().removeActivationListener(this, this);
		GlobalEvents.getInstance().removeBackingStoreListener(this);
		super.dispose();
	}
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	class FaelleLabelProvider extends DefaultLabelProvider{

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if(element instanceof Fall){
				Fall fall=(Fall)element;
				if(fall.isValid()){
					return Desk.theImageRegistry.get(Desk.IMG_OK);
				}else{
					return Desk.theImageRegistry.get(Desk.IMG_FEHLER);
				}
			}
			return super.getColumnImage(element, columnIndex);
		}
		
	}
	class FaelleContentProvider implements IStructuredContentProvider{

		public Object[] getElements(Object inputElement) {
			Patient act=(Patient)GlobalEvents.getInstance().getSelectedObject(Patient.class);
			if(act==null){
				return new Object[0];
			}else{
				return act.getFaelle();
			}

		}

		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public void activation(boolean mode) {
		
	}

	public void visible(boolean mode) {
		if(mode){
			tv.refresh(true);
			GlobalEvents.getInstance().addSelectionListener(this);
		}else {
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
	}

	public void clearEvent(Class template) {
		if(template.equals(Patient.class)){
			tv.refresh();
		}
	}

	public void selectionEvent(PersistentObject obj) {
		if((obj instanceof Patient) ||
				(obj instanceof Fall)){
			tv.refresh(true);
		}
	}

	public void reloadContents(Class clazz) {
		if(clazz.equals(Fall.class)){
			tv.refresh(true);
		}
		
	}

}
