/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: KontakteView.java 4019 2008-06-10 16:05:22Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.ListLoader;
import ch.elexis.actions.GlobalActions;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.*;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.DefaultControlFieldProvider;
import ch.elexis.util.DefaultLabelProvider;
import ch.elexis.util.LazyContentProvider;
import ch.elexis.util.SimpleWidgetProvider;
import ch.elexis.util.ViewMenus;
import ch.elexis.util.ViewerConfigurer;
import ch.elexis.util.ViewerConfigurer.ControlFieldListener;
import ch.rgw.tools.ExHandler;

public class KontakteView extends ViewPart implements ControlFieldListener, ISaveablePart2{
	public static final String ID="ch.elexis.Kontakte";
	private CommonViewer cv;
	private ViewerConfigurer vc;
	private String[] fields={"Kuerzel","Bezeichnung1","Bezeichnung2","Strasse","Plz","Ort"};
	private ViewMenus menu;
	
    ListLoader dataloader=new ListLoader("Kontakte",new Query<Kontakt>(Kontakt.class),new String[]{"Bezeichnung1","Bezeichnung2"});
	
    public KontakteView() {
        Hub.jobPool.addJob(dataloader);
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		cv=new CommonViewer();
		 vc=new ViewerConfigurer(
	         		//new ViewerConfigurer.DefaultContentProvider(cv, Anschrift.class),
	         		new LazyContentProvider(cv,dataloader, AccessControlDefaults.KONTAKT_DISPLAY),
	         		new DefaultLabelProvider(),
	         		new DefaultControlFieldProvider(cv, fields),
	         		new ViewerConfigurer.DefaultButtonProvider(cv,Kontakt.class),
	         		new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST, SWT.NONE,null)
	         );
         cv.create(vc,parent,SWT.NONE,getViewSite());
         menu=new ViewMenus(getViewSite());
         Action delKontakt=new Action("Löschen"){
        	@Override
        	public void run(){
        		Object[] o=cv.getSelection();
        		if(o!=null){
        			Kontakt k=(Kontakt)o[0];
        			k.delete();
        			cv.getConfigurer().getControlFieldProvider().fireChangedEvent();
        		}
        	}
         };
         Action dupKontakt=new Action("Kontakt duplizieren"){
        	 @Override
         	public void run(){
         		Object[] o=cv.getSelection();
         		if(o!=null){
         			Kontakt k=(Kontakt)o[0];
         			Kontakt dup;
         			if(k.istPerson()){
         				Person p=Person.load(k.getId());
         				dup=new Person(p.getName(),p.getVorname(),p.getGeburtsdatum(),p.getGeschlecht());
         			}else{
         				Organisation org=Organisation.load(k.getId());
         				dup=new Organisation(org.get("Name"),org.get("Zusatz1"));
         			}
         			dup.setAnschrift(k.getAnschrift());
         			cv.getConfigurer().getControlFieldProvider().fireChangedEvent();
         			//cv.getViewerWidget().refresh();
         		}
         	}
         };
         menu.createViewerContextMenu(cv.getViewerWidget(),delKontakt,dupKontakt);
         menu.createMenu(GlobalActions.printKontaktEtikette);
         menu.createToolbar(GlobalActions.printKontaktEtikette);
         //cv.getViewerWidget().addSelectionChangedListener(GlobalEvents.getInstance().getDefaultListener());
         ((LazyContentProvider)vc.getContentProvider()).startListening();
         vc.getControlFieldProvider().addChangeListener(this);
         cv.addDoubleClickListener(new CommonViewer.DoubleClickListener(){
			public void doubleClicked(PersistentObject obj, CommonViewer cv) {
				try {
					KontaktDetailView kdv=(KontaktDetailView)getSite().getPage().showView(KontaktDetailView.ID);
					kdv.kb.selectionEvent(obj);
				} catch (PartInitException e) {
					ExHandler.handle(e);
				}
				
			}
         });
	}
	public void dispose(){
		((LazyContentProvider)vc.getContentProvider()).stopListening();
		vc.getControlFieldProvider().removeChangeListener(this);
		super.dispose();
	}
	
	@Override
	public void setFocus() {
		vc.getControlFieldProvider().setFocus();
	}

	public void changed(String[] fields, String[] values) {
		GlobalEvents.getInstance().clearSelection(Kontakt.class);
	}

	public void reorder(String field) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * ENTER has been pressed in the control fields, select the first listed patient
	 */
	// this is also implemented in PatientenListeView
	public void selected() {
    	StructuredViewer viewer = cv.getViewerWidget();
    	Object[] elements = cv.getConfigurer().getContentProvider().getElements(viewer.getInput());
    	
    	if (elements != null && elements.length > 0) {
    		Object element = elements[0];
    		/*
    		 * just selecting the element in the viewer doesn't work if the
    		 * control fields are not empty (i. e. the size of items changes):
    		 *   cv.setSelection(element, true);
    		 * bug in TableViewer with style VIRTUAL?
    		 * work-arount: just globally select the element without visual
    		 * representation in the viewer
    		 */
    		if (element instanceof PersistentObject) {
    			// globally select this object
    			GlobalEvents.getInstance().fireSelectionEvent((PersistentObject) element);
    		}
    	}
	}

	/* ******
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2
	 * Wir benötigen das Interface nur, um das Schliessen einer View zu verhindern,
	 * wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */ 
	public int promptToSaveOnClose() {
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL : ISaveablePart2.NO;
	}
	public void doSave(IProgressMonitor monitor) { /* leer */ }
	public void doSaveAs() { /* leer */}
	public boolean isDirty() {
		return true;
	}
	public boolean isSaveAsAllowed() {
		return false;
	}
	public boolean isSaveOnCloseNeeded() {
		return true;
	}
}
