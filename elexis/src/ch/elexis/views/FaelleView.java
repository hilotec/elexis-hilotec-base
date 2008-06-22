/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich, D. Lutz, P. Schönbucher and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: FaelleView.java 4063 2008-06-22 16:51:46Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import static ch.elexis.actions.GlobalActions.delFallAction;
import static ch.elexis.actions.GlobalActions.makeBillAction;
import static ch.elexis.actions.GlobalActions.neuerFallAction;
import static ch.elexis.actions.GlobalActions.openFallaction;
import static ch.elexis.actions.GlobalActions.reopenFallAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IFilter;
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
import ch.elexis.actions.GlobalEvents.IObjectFilterProvider;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
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
	private IAction konsFilterAction;
	private final FallKonsFilter filter=new FallKonsFilter();
	
	public FaelleView() {
		makeActions();
	}

	@Override
	public void createPartControl(final Composite parent) {
		setPartName(Messages.getString("FaelleView.partName")); //$NON-NLS-1$
		parent.setLayout(new GridLayout());
		tv=new TableViewer(parent);
		tv.getControl().setLayoutData(SWTHelper.getFillGridData(1,true, 1, true));
		tv.setContentProvider(new FaelleContentProvider());
		tv.setLabelProvider(new FaelleLabelProvider());
		tv.addSelectionChangedListener(GlobalEvents.getInstance().getDefaultListener());
		menus=new ViewMenus(getViewSite());
		menus.createToolbar(konsFilterAction,neuerFallAction);
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
		public Image getColumnImage(final Object element, final int columnIndex) {
			if(element instanceof Fall){
				Fall fall=(Fall)element;
				if(fall.isValid()){
					return Desk.getImage(Desk.IMG_OK);
				}else{
					return Desk.getImage(Desk.IMG_FEHLER);
				}
			}
			return super.getColumnImage(element, columnIndex);
		}
		
	}
	class FaelleContentProvider implements IStructuredContentProvider{

		public Object[] getElements(final Object inputElement) {
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

		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public void activation(final boolean mode) {
		
	}

	public void visible(final boolean mode) {
		if(mode){
			tv.refresh(true);
			GlobalEvents.getInstance().addSelectionListener(this);
		}else {
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
	}

	public void clearEvent(final Class<? extends PersistentObject> template) {
		if(template.equals(Patient.class)){
			tv.refresh();
		}
	}

	public void selectionEvent(final PersistentObject obj) {
		if(obj instanceof Patient) {
			tv.refresh(true);
		}else if(obj instanceof Fall){
			tv.refresh(true);
			if(konsFilterAction.isChecked()){
				filter.setFall((Fall)obj);
			}
		}
	}

	public void reloadContents(final Class clazz) {
		if(clazz.equals(Fall.class)){
			tv.refresh(true);
		}
		
	}
	
	private void makeActions(){
		konsFilterAction=new Action("Konsultationen filtern",Action.AS_CHECK_BOX){
			{
				setToolTipText("Nur Konsultationen dieses Falls anzeigen");
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_FILTER));
			}
			@Override
			public void run(){
				if(!isChecked()){
					GlobalEvents.getInstance().getObjectFilters().unregisterObjectFilter(Konsultation.class, filter);
				}else{
					GlobalEvents.getInstance().getObjectFilters().registerObjectFilter(Konsultation.class, filter);
					filter.setFall(GlobalEvents.getSelectedFall());
				}
			}
			
		};
	}

	class FallKonsFilter implements IObjectFilterProvider, IFilter{

		Fall mine;
		boolean bDaempfung;
		
		void setFall(final Fall fall){
			mine=fall;
			GlobalEvents.getInstance().fireUpdateEvent(Konsultation.class);
		}
		
		public void activate() {
			bDaempfung=true;
			konsFilterAction.setChecked(true);
			bDaempfung=false;
		}

		public void changed() {
			// don't mind
		}

		public void deactivate() {
			bDaempfung=true;
			konsFilterAction.setChecked(false);
			bDaempfung=false;
		}

		public IFilter getFilter() {
			return this;
		}

		public String getId() {
			return "ch.elexis.FallFilter";
		}

		public boolean select(final Object toTest) {
			if(mine==null){
				return true;
			}
			if(toTest instanceof Konsultation){
				Konsultation k=(Konsultation)toTest;
				if(k.getFall().equals(mine)){
					return true;
				}
			}
			return false;
		}
		
	}
}
