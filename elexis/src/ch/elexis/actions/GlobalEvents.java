/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: GlobalEvents.java 2698 2007-07-03 12:51:47Z rgw_ch $
 *******************************************************************************/

package ch.elexis.actions;

import java.util.Hashtable;
import java.util.LinkedList;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.util.Log;
import ch.elexis.util.Tree;
import ch.elexis.views.codesystems.ICodeSelectorTarget;

/**
 * Central management and distribution of events.
 * To get informed about selection changes, register as GlobalEvents.Listener. 
 * To send a message about changed selection, call fireSelectionEvent. GlobalEvents
 * will distribute the event among all listeners.
 * 
 * To get informed about activation or deactivatioon of a workbench part, register as 
 * ActivationListener.
 * 
 * To get informed when a change of the databse occurs, register as BackingStoreListener.
 * 
 * To get informed about creation, deletion or modification of PersistentObject, register
 * as ObjectListener
 * 
 * Zentrale Verarbeitung und Verteilung von Ereignissen.
 * Wer z.B. über eine Änderung der Auswahl informiert werden möchte, registriert
 * sich als GlobalEvent.Listener.
 * Wer selber eine Nachricht senden möchte, ruft "fireEvent" auf. Dadurch wird
 * die Nachricht an alle Listeners weitergeleitet.
 * Wer informiert werden will, ob eine bestimmte Part aktiviert oder deaktiviert wurde, 
 * registriert sich als ActivationListener
 * Wer über eine Änderung einer Datenbasis informiert werden will (um z.B. eine Liste
 * neu einzulesen), registriert sich als BackingStoreListener
 * @author Gerry
 *
 */
public class GlobalEvents implements IPartListener2 {
    private static Log log = Log.get("GlobalEvents"); //$NON-NLS-1$
   
    private LinkedList<SelectionListener> selectionListeners;
    private LinkedList<BackingStoreListener> storeListeners;
    private LinkedList<ObjectListener> objectListeners;
    private Hashtable<IWorkbenchPart,LinkedList<ActivationListener>> activationListeners;
    
    private ICodeSelectorTarget codeSelectorTarget = null;
    
    private static GlobalEvents theInstance;
    private static GlobalListener theListener;
    
    private GlobalEvents(){
        selectionListeners=new LinkedList<SelectionListener>();
        storeListeners=new LinkedList<BackingStoreListener>();
        activationListeners=new Hashtable<IWorkbenchPart,LinkedList<ActivationListener>>();
        objectListeners=new LinkedList<ObjectListener>();
        theListener=new GlobalListener();
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(this);
     }
    /**
     * There is only one GlobalEvents-Object, which can be accessed only here.
     * 
     * Singleton. Kann nur über diese Funktion bezogen werden 
     * @return
     */
    public static GlobalEvents getInstance(){
        if(theInstance==null){
            theInstance=new GlobalEvents();
        }
        return theInstance;
    }
    /**
     * Convenience-Methoden
     * 
     */
    public static Patient getSelectedPatient(){
    	return (Patient)getInstance().getSelectedObject(Patient.class);
    }
    public static Fall getSelectedFall(){
    	return (Fall)getInstance().getSelectedObject(Fall.class);
    }
    public static Konsultation getSelectedKons(){
    	return (Konsultation)getInstance().getSelectedObject(Konsultation.class);
    }
    /**
     * Einen Standarldistener holen, der ISelectionEvents von StructuredViewers
     * der Workbench holt und an GlobalEvents weiterleitet. 
     * @return
     */
    public GlobalListener getDefaultListener(){
        return theListener;
    }
    /**
     * Einen ObjectListener hinzufügen. ObjectListeners werden informiert, wenn ein PersistentObject
     * geändert, neu erstellt oder gelöscht wird.
     * @param o ein ObjectListener oder ObjectListenerAdapter
     */
    
    public void addObjectListener(ObjectListener o){
    	objectListeners.add(o);
    }
    
    /**
     * Einen ObjectListener entfernen. Dies muss unbedingt spätestens bei Dispose gemacht werden.
     * @param o ein ObjectListener, der zuvor mit addObjectListener hinzugefügt wurde
     */
    public void removeObjectListener(ObjectListener o){
    	objectListeners.remove(o);
    }
    /**
     * Einen SelectionListener hinzufügen. Selection;Listeners werden informiert, wenn der Anwender ein Objekt
     * auswählt
     * @param l den Listener
     * @param win das Fenster, das beobachtet werden soll.
     */
    public void addSelectionListener(SelectionListener l /*, IWorkbenchWindow win */){
    	/*
    	if(win==null){
    		win=PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    	}
    	
    	LinkedList<SelectionListener> list=listeners.get(win);
    	if(list==null){
    		list=new LinkedList<SelectionListener>();
    		listeners.put(win, list);
    	}
        list.add(l);
        */
    	selectionListeners.add(l);
    }
    
    /**
     * Einen listener entfernen. Dies muss unbedingt bei dispose gemacht werden,
     * da es sonst beim nächsten Aufrufversuch eine Exception gibt.
     * @param l
     */
    public void removeSelectionListener(SelectionListener l /*,IWorkbenchWindow win */){
    	/*
    	if(win==null){
    		win=PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    	}
    	LinkedList<SelectionListener> list=listeners.get(win);
    	if(list!=null){
    		list.remove(l);
    	}
    	*/
    	selectionListeners.remove(l);
    }
    
    public void addBackingStoreListener(BackingStoreListener l){
    	storeListeners.add(l);
    }
    
    public void removeBackingStoreListener(BackingStoreListener l){
    	storeListeners.remove(l);
    }
    
    public void addActivationListener(ActivationListener l, IWorkbenchPart part){
    	LinkedList<ActivationListener> list=activationListeners.get(part);
    	if(list==null){
    		list=new LinkedList<ActivationListener>();
    		activationListeners.put(part,list);
    	}
    	list.add(l);
    }
    public void removeActivationListener(ActivationListener l, IWorkbenchPart part){
    	LinkedList<ActivationListener> list=activationListeners.get(part);
    	if(list!=null){
    		list.remove(l);
    	}
    }
    
    public enum CHANGETYPE{update,delete,create};
    public void fireObjectEvent(final PersistentObject o, final CHANGETYPE type){
    	Desk.theDisplay.asyncExec(new Runnable(){

			public void run() {
				for(ObjectListener ol:objectListeners){
					switch(type.ordinal()){
			    	case 0:	ol.objectChanged(o); break;
			    	case 1: ol.objectDeleted(o); break;
			    	case 2: ol.objectCreated(o); break;
			    	}	
				}
			}});
    }
    /**
     * Die Änderung einer Auswahl anzeigen. Im Fall eines Events aus der Patient-Fall-behandlung Kette
     * wird die Synchronisation jeweils hergestellt.
     * @param selected das neu ausgewählte Element
     * @param win wo die Auswahl erfolgte
     */
  
    public void fireSelectionEvent(final PersistentObject selected /*, IWorkbenchWindow win */){
    	
    	if(selected==null){
    		log.log("fireSelectionEvent mit Null Objekt ", Log.DEBUGMSG); //$NON-NLS-1$
    	}else{
    		log.log("fireSelectionEvent: " + selected.getClass().getName() + "::" + selected.getId(), Log.DEBUGMSG); //$NON-NLS-1$ //$NON-NLS-2$
    	}
    	/*
        if(win==null){
        	win=PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        }
        */
        // TODO Das ist unbefriedigend. Lieber Abhängigkeit Pat/Fall/Kons beim Klienten auflösen
        if(selected instanceof Patient){
        	Hub.setWindowText((Patient)selected);
        	Fall f=(Fall)SelectionTracker.getObject(Fall.class);
        	if((f==null) || (f.getPatient()==null) || (!f.getPatient().getId().equals(selected.getId()))){
        			/*
        			Konsultation b=((Patient)selected).getLetzteKons();
        			if(b!=null){
        				f=b.getFall();
        				doDispatchEvent(win,f);
        				doDispatchEvent(win,b);
        			}
        			*/
        		clearSelection(Fall.class);
        	}
        }else if(selected instanceof Fall){
        	Patient pat=(Patient)SelectionTracker.getObject(Patient.class);
        	Fall fall=(Fall)selected;
        	if((pat==null) || (!pat.getId().equals(fall.getPatient().getId()))){
        		doDispatchEvent(fall.getPatient());
        		SelectionTracker.clearObject(Konsultation.class);
        		
        	}
        }else if(selected instanceof Konsultation){
        	Fall fb=((Konsultation)selected).getFall();
        	Fall fo=(Fall)SelectionTracker.getObject(Fall.class);
        	if((fo!=null) && !fo.getId().equals(fb.getId())){
        		doDispatchEvent(fb);
        		doDispatchEvent(fb.getPatient());
        	}
        }
        doDispatchEvent(selected);
        
    }
    static PersistentObject sourceObj;
    //static IWorkbenchWindow sourceWin;
    private void doDispatchEvent(/*IWorkbenchWindow win,*/PersistentObject obj){
    	if((obj==null) || /*((sourceWin==win) &&*/ (sourceObj==obj)){
    		return;
    	}
    
    	log.log("doDispatch: " + obj.getClass().getName() + "::" + obj.getId(), Log.DEBUGMSG); //$NON-NLS-1$ //$NON-NLS-2$
    	
    		// Endlosschleife vermeiden.
    	//sourceWin=win;
    	sourceObj=obj;
       	if(SelectionTracker.change(obj) || true){
       		/*
	       	LinkedList<SelectionListener> list=listeners.get(win);
	       	if(list!=null){
	       		for(SelectionListener l:list){
	       			l.selectionEvent(obj);
	       		}
	       	}
	       	*/
       		for(SelectionListener l:selectionListeners){
       			l.selectionEvent(obj);
       		}
       	}
       	//sourceWin=null;
       	sourceObj=null;
    }
    /**
     * Eine Information schicken, dass der Datenbestand eines bestimmten Typs verändert wurde.
     * @param clazz
     */
    @SuppressWarnings("unchecked")
	public void fireUpdateEvent(Class clazz){
    	for(BackingStoreListener lis:storeListeners){
    		lis.reloadContents(clazz);
    	}
    }
    /**
     * Ein SelectionListener dient dazu, sich über die Auswal eines
     * PersistentObjects informieren zu lassen
     * @author Gerry
     *
     */
    public interface SelectionListener{
        public void selectionEvent(PersistentObject obj);
        @SuppressWarnings("unchecked")
		public void clearEvent(Class template);
    }
    
    /**
     * Der BackingStoreListener wird dann informiert, wenn die Datenbasis
     * neu eingelesen werden sollte (z.B. weil Änderungen erfolgt sein könnten)
     * Es ist nicht garantiert, dass tatsächlich Änderungen erfilgt sind.
     * @author Gerry
     *
     */
    public interface BackingStoreListener{
    	@SuppressWarnings("unchecked")
		public void reloadContents(Class clazz);
    }
    
    private class GlobalListener implements ISelectionChangedListener{
    	boolean daempfung;
        public void selectionChanged(SelectionChangedEvent event)
        {	
        	if(daempfung){
        		return;
        	}
        	daempfung=true;
            StructuredSelection sel=(StructuredSelection)event.getSelection();
            
            Object[] obj=sel.toArray();
            if(obj!=null && obj.length!=0){
                if(obj[0] instanceof PersistentObject){
                    GlobalEvents.getInstance().fireSelectionEvent((PersistentObject)obj[0]);
                }else if(obj[0] instanceof Tree){
                	Tree t=(Tree)obj[0];
                	if(t.contents instanceof PersistentObject){
                		GlobalEvents.getInstance().fireSelectionEvent((PersistentObject)t.contents);
                	}
                }
            }
            daempfung=false;
        }
        
    }
    /** Findet den im aktuellen Fenster gerade selektierten Patienten
     * oder null, wenn kein Patient selektiert ist
     * @return
     */
    @SuppressWarnings("unchecked")
	public PersistentObject getSelectedObject(Class template){
    	return SelectionTracker.getObject(template);
    }
    @SuppressWarnings("unchecked") //$NON-NLS-1$
	public void clearSelection(final Class template /*, IWorkbenchWindow win*/){
    	log.log("clearSelection: " + template.getName(), Log.DEBUGMSG); //$NON-NLS-1$

    	/*
    	if(win==null){
    		win=PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    	}
    	*/
    	SelectionTracker.clearObject(template);
    	//final LinkedList<SelectionListener> list=(LinkedList<SelectionListener>) (win==null ? listeners.values().toArray()[0] : listeners.get(win));
    	Desk.theDisplay.syncExec(new Runnable(){
    		public void run(){
    			for(SelectionListener l:selectionListeners){
    				l.clearEvent(template);
    			}
    		}
    	});
    }
    
    private static class SelectionTracker{
    	static SelectionTracker tracker;
    	@SuppressWarnings("unchecked")
		Hashtable<Class,PersistentObject> objects=new Hashtable<Class,PersistentObject>();
    	// IWorkbenchWindow window;
    	/*
    	SelectionTracker next;
    	static SelectionTracker find(IWorkbenchWindow win) {
    		if(root==null){
    			return null;
    		}
    		return root.findFor(win);
    	}
    	*/
    	@SuppressWarnings("unchecked")
		static PersistentObject getObject(/*IWorkbenchWindow win,*/ Class template){
    		//SelectionTracker t=find(win);
    		return tracker==null ? null : tracker.objects.get(template);
    	}
    	@SuppressWarnings("unchecked")
		static void clearObject(/*IWorkbenchWindow win,*/ Class template){
    		//SelectionTracker t=find(win);
    		if(tracker!=null){
    			tracker.objects.remove(template);
    		}
    	}
    	static boolean change(/* IWorkbenchWindow win,*/ PersistentObject sel){
    		if(/*(win==null) ||*/ (sel==null)){
    			return false;
    		}
    		/*
    		SelectionTracker found=find(win);
    		if(found==null){
    			found=new SelectionTracker(win);
    		}
    		*/
    		if(tracker==null){
    			tracker=new SelectionTracker();
    		}
    		PersistentObject old=tracker.objects.get(sel.getClass());
    		if((old!=null) && (old == sel)){
    			return false;
    		}
    		tracker.objects.put(sel.getClass(),sel);
    		return true;
    	}
    	/*
    	SelectionTracker(IWorkbenchWindow win){
    		next=root;
    		root=this;
    		window=win;
    	}
    	
    	SelectionTracker findFor(IWorkbenchWindow win){
    		if(window.equals(win)){
    			return this;
    		}
    		if(next==null){
    			return null;
    		}
    		return next.findFor(win);
    	}
    	*/
    }
    
    public interface ObjectListener{
    	public void objectChanged(PersistentObject o);
    	public void objectCreated(PersistentObject o);
    	public void objectDeleted(PersistentObject o);
    }
    
    public static class ObjectListenerAdapter implements ObjectListener{

		public void objectChanged(PersistentObject o) {}
		public void objectCreated(PersistentObject o) {}
		public void objectDeleted(PersistentObject o) {}
    	
    }
	public interface ActivationListener{
		public void activation(boolean mode);
		public void visible(boolean mode);
	}
	public void partActivated(IWorkbenchPartReference partRef) {
		LinkedList<ActivationListener> list=activationListeners.get(partRef.getPart(false));
		if(list!=null){
			for(ActivationListener l:list){
				l.activation(true);
			}
		}
	}
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}
	public void partClosed(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}
	public void partDeactivated(IWorkbenchPartReference partRef) {
		LinkedList<ActivationListener> list=activationListeners.get(partRef.getPart(false));
		if(list!=null){
			for(ActivationListener l:list){
				l.activation(false);
			}
		}

		
	}
	public void partOpened(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}
	public void partHidden(IWorkbenchPartReference partRef) {
		LinkedList<ActivationListener> list=activationListeners.get(partRef.getPart(false));
		if(list!=null){
			for(ActivationListener l:list){
				l.visible(false);
			}
		}
		
	}
	public void partVisible(IWorkbenchPartReference partRef) {
		LinkedList<ActivationListener> list=activationListeners.get(partRef.getPart(false));
		if(list!=null){
			for(ActivationListener l:list){
				l.visible(true);
			}
		}
			
	}
	public void partInputChanged(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	};
	
	/**
	 * Register a ICodeSelectorTarget. This is informed when an alement is chosen
	 * in a CodeSelector.
	 * 
	 * @param target the ICodeSelectorTarget to set.
	 */
	public void setCodeSelectorTarget(ICodeSelectorTarget target) {
		if (codeSelectorTarget != null) {
			codeSelectorTarget.registered(false);
		}
		codeSelectorTarget = target;
		codeSelectorTarget.registered(true);
	}
	
	/**
	 * Unregister the currently registered ICodeSelectorTarget.
	 */
	public void removeCodeSelectorTarget() {
		if (codeSelectorTarget != null) {
			codeSelectorTarget.registered(false);
		}
		
		codeSelectorTarget = null;
	}
	
	/**
	 * Reeturns the currently registered ICodeSelectorTarget.
	 * 
	 * @return the registered ICodeSelectorTarget
	 */
	public ICodeSelectorTarget getCodeSelectorTarget() {
		return codeSelectorTarget;
	}
}
