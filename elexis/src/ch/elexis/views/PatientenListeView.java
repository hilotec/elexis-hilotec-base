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
 * $Id: PatientenListeView.java 3800 2008-04-20 12:44:30Z rgw_ch $
 *******************************************************************************/


package ch.elexis.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.AbstractDataLoaderJob;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.Heartbeat.HeartListener;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.FilterFactory;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Reminder;
import ch.elexis.data.FilterFactory.Filter;
import ch.elexis.dialogs.PatientErfassenDialog;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.DefaultControlFieldProvider;
import ch.elexis.util.DefaultLabelProvider;
import ch.elexis.util.LazyContentProvider;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.SimpleWidgetProvider;
import ch.elexis.util.ViewMenus;
import ch.elexis.util.ViewerConfigurer;
import ch.elexis.util.ViewerConfigurer.ControlFieldListener;

public class PatientenListeView extends ViewPart implements ActivationListener, ISaveablePart2, HeartListener{
    public static final String  ID="ch.elexis.PatListView";
    private CommonViewer cv;
    private ViewerConfigurer vc;
    private AbstractDataLoaderJob loader;
    private ViewMenus menus;
    private IAction filterAction,newPatAction;
    Filter patFilter;
    private Patient actPatient;
    //private SortedSet<Reminder> myReminders;

    @Override
	public void dispose() {
    	//cv.getViewerWidget().removeSelectionChangedListener(GlobalEvents.getInstance().getDefaultListener());
    	((LazyContentProvider)cv.getConfigurer().getContentProvider()).stopListening();
    	GlobalEvents.getInstance().removeActivationListener(this,this);
    	super.dispose();
	}

    public Patient getSelectedPatient(){
    	Object[] sel=cv.getSelection();
    	if(sel!=null){
    		return (Patient)sel[0];
    	}
    	return null;
    }
    
    public void reload(){
    	loader.invalidate();
    	cv.notify(CommonViewer.Message.update);
    }
	@Override
    public void createPartControl(final Composite parent)
    {
		cv=new CommonViewer();
		
		loader=(AbstractDataLoaderJob)Hub.jobPool.getJob("PatientenListe");
		vc=new ViewerConfigurer(
				new LazyContentProvider(cv,loader, AccessControlDefaults.PATIENT_DISPLAY),
				new PatLabelProvider(),
				new DefaultControlFieldProvider(cv, new String[]{"Name","Vorname","Geburtsdatum"}),
				new ViewerConfigurer.DefaultButtonProvider(), //cv,Patient.class),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST,SWT.SINGLE,cv)
		);
        cv.create(vc,parent,SWT.NONE,getViewSite());
        // let user select patient by pressing ENTER in the control fields
        cv.getConfigurer().getControlFieldProvider().addChangeListener(new ControlFieldSelectionListener());
        makeActions();
        menus=new ViewMenus(getViewSite());
        menus.createToolbar(newPatAction,filterAction);
        menus.createControlContextMenu(cv.getViewerWidget().getControl(), new PatientMenuPopulator(this));
        ((LazyContentProvider)vc.getContentProvider()).startListening();
        patFilter=FilterFactory.createFilter(Patient.class,"Diagnosen","PersAnamnese","SystemAnamnese","Dauermedikation",
        		"Allergien","Risiken","Bemerkung","PatientNr","Strasse","Ort");
        GlobalEvents.getInstance().addActivationListener(this,this);
    }
	
    @Override
    public void setFocus()
    {     vc.getControlFieldProvider().setFocus();
    }
    class PatLabelProvider extends DefaultLabelProvider implements ITableColorProvider{

    	
		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			if(element instanceof Patient){
				Patient pat=(Patient)element;
				
				if(Reminder.findRemindersDueFor(pat, Hub.actUser,false).size()>0){
					return Desk.theImageRegistry.get(Desk.IMG_AUSRUFEZ);
				}
				/*
				if(pat.getBemerkung().contains(":VIP:")){
					return Desk.theImageRegistry.get(Desk.IMG_VIP);
				}
				*/
				if(pat.getGeschlecht().equals("m")){
					return Desk.theImageRegistry.get(Desk.IMG_MANN);
				}else{
					return Desk.theImageRegistry.get(Desk.IMG_FRAU);
				}
			}else{
				return super.getColumnImage(element, columnIndex);
			}
		}

		public Color getBackground(final Object element, final int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public Color getForeground(final Object element, final int columnIndex) {
			if(element instanceof Patient){
				if(((Patient)element).getBemerkung().contains(":VIP:")){
					return Desk.theColorRegistry.get(Desk.COL_RED);
				}
			}
		
			return null;
		}
    	
    }
    public void reset(){
    	vc.getControlFieldProvider().clearValues();
    }
    private void makeActions(){
        
        filterAction=new Action("Liste filtern",Action.AS_CHECK_BOX){
			@Override
			public void run() {
			 	loader.getQuery().removePostQueryFilter(patFilter);
			 	if(isChecked()){
			 		if(FilterFactory.createFilterDialog(patFilter,getViewSite().getShell()).open()==Dialog.OK){
			 			loader.getQuery().addPostQueryFilter(patFilter);
			 		}
			 	}
                loader.invalidate();
                cv.notify(CommonViewer.Message.update);
				
			}
			
        };
       
        filterAction.setImageDescriptor(Desk.theImageRegistry.getDescriptor("filter"));
        newPatAction=new Action("Neuer Patient"){
        	{
        		setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_NEW));
        		setToolTipText("Neuen Patienteneintrag erstellen");
        	}
        	@Override
			public void run(){
            	// access rights guard
            	if (!Hub.acl.request(AccessControlDefaults.PATIENT_INSERT)) {
            		SWTHelper.alert("Fehlende Rechte",
    						"Sie dürfen keinen neuen Patienten anlegen.");
            		return;
            	}
            	
        		PatientErfassenDialog ped=new PatientErfassenDialog(getViewSite().getShell(),vc.getControlFieldProvider().getValues());
        		if(ped.open()==Dialog.OK){
        			vc.getControlFieldProvider().clearValues();
        			actPatient=ped.getResult();
        			loader.invalidate();
        			cv.notify(CommonViewer.Message.update);
        			cv.setSelection(actPatient,true);
        		}
        	}
        };
        /*
        importVCardAction=new Action("Aus vCard importieren"){
        	
        	@Override
        	public void run(){
        		FileDialog fd=new FileDialog(getViewSite().getShell(),SWT.OPEN);
        		String cardname=fd.open();
        		if(cardname!=null){
        			try {
						VCard card=new VCard(new FileInputStream(cardname));
						String name=card.getElement("N");
					} catch (Exception e) {
						ExHandler.handle(e);
					}
        		}
        	}
        };
        */
    }

	public void activation(final boolean mode) {
		if(mode==true){
			newPatAction.setEnabled(Hub.acl.request(AccessControlDefaults.PATIENT_INSERT));
			
	    	heartbeat();
			Hub.heart.addListener(this);
		}else{
			Hub.heart.removeListener(this);
		}
		
	}

	public void visible(final boolean mode) {
		// TODO Auto-generated method stub
		
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
	public void doSave(final IProgressMonitor monitor) { /* leer */ }
	public void doSaveAs() { /* leer */}
	public boolean isDirty() {
		return GlobalActions.fixLayoutAction.isChecked();
	}
	public boolean isSaveAsAllowed() {
		return false;
	}
	public boolean isSaveOnCloseNeeded() {
		return true;
	}

	public void heartbeat() {
		cv.notify(CommonViewer.Message.update);
	}

    /*
	public void selectionEvent(PersistentObject obj, IViewSite site) {
		if(obj instanceof Patient){
			actPatient=(Patient)obj;
			Konsultation b=actPatient.getLetzteBehandlung();
			Fall f=b.getFall();
			GlobalEvents.getInstance().fireSelectionEvent(f,getViewSite());
			GlobalEvents.getInstance().fireSelectionEvent(b,getViewSite());
		}
		
	}*/

	/**
	 * Select Patient when user presses ENTER in the control fields.
	 * If mor than one Patients are listed, the first one is selected.
	 * (This listener only implements selected().)
	 */
	class ControlFieldSelectionListener implements ControlFieldListener {
		public void changed(final String[] fields, final String[] values) {
			// nothing to do (handled by LazyContentProvider) 
		}

		public void reorder(final String field) {
			// nothing to do (handled by LazyContentProvider) 
		}
		
		/**
		 * ENTER has been pressed in the control fields, select the first listed patient
		 */
		// this is also implemented in KontakteView
		public void selected() {
	    	StructuredViewer viewer = cv.getViewerWidget();
	    	Object[] elements = cv.getConfigurer().getContentProvider().getElements(viewer.getInput());
	    	if ((elements != null) && (elements.length > 0)) {
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
	}
}
