/*******************************************************************************
 * Copyright (c) 2007-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: BaseAgendaView.java 3951 2008-05-22 19:34:27Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.AgendaActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.Synchronizer;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.BackingStoreListener;
import ch.elexis.actions.Heartbeat.HeartListener;
import ch.elexis.agenda.Messages;
import ch.elexis.agenda.acl.ACLContributor;
import ch.elexis.agenda.data.ICalTransfer;
import ch.elexis.agenda.data.IPlannable;
import ch.elexis.agenda.data.Termin;
import ch.elexis.agenda.preferences.PreferenceConstants;
import ch.elexis.data.Anwender;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.dialogs.TagesgrenzenDialog;
import ch.elexis.dialogs.TerminDialog;
import ch.elexis.dialogs.TerminListeDruckenDialog;
import ch.elexis.dialogs.TermineDruckenDialog;
import ch.elexis.util.Log;
import ch.elexis.util.Plannables;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

public abstract class BaseAgendaView extends ViewPart implements BackingStoreListener,  HeartListener, ActivationListener{

	protected TimeTool actDate;	
	protected  String actBereich;
	protected Synchronizer pinger;
	protected SelectionListener sListen=new SelectionListener();
	TableViewer tv;
	BaseAgendaView self;
	protected IAction newTerminAction, blockAction,terminKuerzenAction,terminVerlaengernAction,terminAendernAction;
	protected IAction dayLimitsAction, newViewAction, printAction, exportAction, importAction;
	protected IAction printPatientAction;
	MenuManager menu=new MenuManager();
	String[] bereiche;
	protected Log log=Log.get("Agenda");
	
	protected BaseAgendaView(){
		self=this;
		bereiche=Hub.globalCfg.get(PreferenceConstants.AG_BEREICHE, Messages.TagesView_14).split(","); 
		actBereich=Hub.userCfg.get(PreferenceConstants.AG_BEREICH, bereiche[0]);
	}
	abstract public void create(Composite parent);
	
	@Override
	public void createPartControl(Composite parent) {
		setBereich(actBereich);
		create(parent);
		makeActions();
		tv.setContentProvider(new AgendaContentProvider());
		tv.setUseHashlookup(true);
		tv.addDoubleClickListener(new IDoubleClickListener(){
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel=(IStructuredSelection)tv.getSelection();
				if((sel==null || (sel.isEmpty()))){
					newTerminAction.run();					
				}else{
					IPlannable pl=(IPlannable)sel.getFirstElement();
					TerminDialog dlg=new TerminDialog(self,pl);
					dlg.open();
					tv.refresh(true);
				}

			}});

		menu.setRemoveAllWhenShown(true);
		menu.addMenuListener(new IMenuListener(){
			public void menuAboutToShow(IMenuManager manager) {
				if(GlobalEvents.getInstance().getSelectedObject(Termin.class)==null){
					manager.add(newTerminAction);
					manager.add(blockAction);
				}else{
					manager.add(AgendaActions.terminStatusAction);
					manager.add(terminKuerzenAction);
					manager.add(terminVerlaengernAction);
					manager.add(terminAendernAction);
					manager.add(AgendaActions.delTerminAction);
				}
			}
			
		});

		Menu cMenu=menu.createContextMenu(tv.getControl());
		tv.getControl().setMenu(cMenu);

		GlobalEvents.getInstance().addBackingStoreListener(this);
		GlobalEvents.getInstance().addActivationListener(this, getViewSite().getPart());
		tv.setInput(getViewSite());
		pinger=new ch.elexis.actions.Synchronizer(this);
		updateActions();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}


	public void heartbeat() {
		log.log("Heartbeat", Log.DEBUGMSG);
		pinger.doSync();
	}

	public void activation(boolean mode) {/* leer */}

	public void visible(boolean mode) {
		if(mode==true){
			Hub.heart.addListener(this);
			tv.addSelectionChangedListener(sListen);
			heartbeat();
		}else{
			Hub.heart.removeListener(this);
			tv.removeSelectionChangedListener(sListen);
		}
		
	};

	// BackingStoreListener
	public void reloadContents(Class clazz) {
		if(clazz.equals(Termin.class)){
			Desk.theDisplay.asyncExec(new Runnable(){
				public void run() {
					if(!tv.getControl().isDisposed()){
						tv.refresh(true);
					}
				}});
		}else if(clazz.equals(Anwender.class)){
			updateActions();
			setBereich(Hub.userCfg.get(PreferenceConstants.AG_BEREICH, bereiche[0]));
		}
	}

	public String getBereich() {
		return actBereich;
	}
	public void setBereich(String b){
		actBereich=b;
		setPartName("Agenda "+b); //$NON-NLS-1$
		Hub.userCfg.set(PreferenceConstants.AG_BEREICH, b);
		if(pinger!=null){
			pinger.doSync();
		}
	}

	public TimeTool getDate() {
		return actDate;
	}
	public abstract void setTermin(Termin t);
	
	class AgendaContentProvider implements IStructuredContentProvider{

		public Object[] getElements(Object inputElement) {
			if(Hub.acl.request(ACLContributor.DISPLAY_APPOINTMENTS)){
				return Plannables.loadDay(actBereich,actDate);
			}else{
				return new Object[0];
			}

		}

		public void dispose() { /* leer */}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {/* leer */}
		
	};
	class SelectionListener implements ISelectionChangedListener{

		StructuredViewer sv;

		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection sel=(IStructuredSelection)event.getSelection();
			if(( sel==null) || sel.isEmpty()){
				GlobalEvents.getInstance().clearSelection(Termin.class);
			}else{
				Object o=sel.getFirstElement();
				GlobalEvents ev=GlobalEvents.getInstance();
				if(o instanceof Termin){
					setTermin((Termin)o);
				}else if(o instanceof Termin.Free){
					ev.clearSelection(Termin.class);
				}
			}
		}
		
	}
	protected void updateActions(){
		dayLimitsAction.setEnabled(Hub.acl.request(ACLContributor.CHANGE_DAYSETTINGS));
		boolean canChangeAppointments=Hub.acl.request(ACLContributor.CHANGE_APPOINTMENTS);
		newTerminAction.setEnabled(canChangeAppointments);
		terminKuerzenAction.setEnabled(canChangeAppointments);
		terminVerlaengernAction.setEnabled(canChangeAppointments);
		terminAendernAction.setEnabled(canChangeAppointments);
		AgendaActions.updateActions();
		tv.refresh();
	}
	
	protected void makeActions(){
		dayLimitsAction=new Action("Tagesgrenzen"){
			@Override
			public void run(){
				new TagesgrenzenDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						actDate.toString(TimeTool.DATE_COMPACT),actBereich)
						.open();
				tv.refresh(true);
			}
		};

		blockAction=new Action(Messages.TagesView_lockPeriod){ 
			@Override
			public void run(){
				IStructuredSelection sel=(IStructuredSelection)tv.getSelection();
				if(sel!=null && !sel.isEmpty()){
					IPlannable p=(IPlannable)sel.getFirstElement();
					if(p instanceof Termin.Free){
						new Termin(actBereich,actDate.toString(TimeTool.DATE_COMPACT),p.getStartMinute(),
								p.getDurationInMinutes()+p.getStartMinute(),Termin.typReserviert(),Termin.statusLeer());
						GlobalEvents.getInstance().fireUpdateEvent(Termin.class);
					}
				}

			}
		};
		terminAendernAction=new Action(Messages.TagesView_changeTermin){ 
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_EDIT));
				setToolTipText(Messages.TagesView_changeThisTermin); 
			}
			@Override
			public void run(){
				TerminDialog dlg=new TerminDialog(self,
						(Termin)GlobalEvents.getInstance().getSelectedObject(Termin.class));
				dlg.open();
				if(tv!=null){
					tv.refresh(true);
				}
			}
		};
		terminKuerzenAction=new Action(Messages.TagesView_shortenTermin){ 
			@Override
			public void run(){
				Termin t=(Termin) GlobalEvents.getInstance().getSelectedObject(Termin.class);
				if(t!=null) {
					t.setDurationInMinutes(t.getDurationInMinutes()>>1);
					GlobalEvents.getInstance().fireUpdateEvent(Termin.class);
				}
			}
		};
		terminVerlaengernAction=new Action(Messages.TagesView_enlargeTermin){ 
			@Override
			public void run(){
				Termin t=(Termin) GlobalEvents.getInstance().getSelectedObject(Termin.class);
				if(t!=null) {
					actDate.set(t.getDay());
					Termin n=Plannables.getFollowingTermin(actBereich, actDate, t);
					if(n!=null){
						t.setEndTime(n.getStartTime());
						//t.setDurationInMinutes(t.getDurationInMinutes()+15);
						GlobalEvents.getInstance().fireUpdateEvent(Termin.class);
					}
				}
			}
		};
		newTerminAction=new Action(Messages.TagesView_newTermin){ 
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_NEW));
				setToolTipText(Messages.TagesView_createNewTermin); 
			}
			@Override
			public void run(){
				TerminDialog dlg=new TerminDialog(self,null);
				dlg.open();
				if(tv!=null){
					tv.refresh(true);
				}
			}
		};
		printAction=new Action("Tagesliste drucken"){
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_PRINTER));
				setToolTipText("Termine des gewählten Tages ausdrucken");
			}
			@Override
			public void run(){
				IPlannable[] liste=Plannables.loadDay(actBereich, actDate);
				TerminListeDruckenDialog dlg=new TerminListeDruckenDialog(getViewSite().getShell(),liste);
				dlg.open();
				if(tv!=null){
					tv.refresh(true);
				}
			}
		};
		printPatientAction=new Action("Patienten-Termine drucken"){
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_PRINTER));
				setToolTipText("Zukünftige Termine des ausgewählten Patienten drucken");
			}
			@Override
			public void run(){
				Patient patient = GlobalEvents.getSelectedPatient();
				if (patient != null) {
					Query<Termin> qbe = new Query<Termin>(Termin.class);
					qbe.add("Wer", "=", patient.getId());
					qbe.add("deleted", "<>", "1");
					qbe.add("Tag", ">=", new TimeTool().toString(TimeTool.DATE_COMPACT));
					qbe.orderBy(false, "Tag", "Beginn");
					java.util.List<Termin> list=qbe.execute();
					if (list != null) {
						boolean directPrint = Hub.localCfg.get(PreferenceConstants.AG_PRINT_APPOINTMENTCARD_DIRECTPRINT,
								PreferenceConstants.AG_PRINT_APPOINTMENTCARD_DIRECTPRINT_DEFAULT);

						TermineDruckenDialog dlg = new TermineDruckenDialog(getViewSite().getShell(), list.toArray(new Termin[0]));
						if (directPrint) {
							dlg.setBlockOnOpen(false);
							dlg.open();
							if (dlg.doPrint()) {
								dlg.close();
							} else {
								SWTHelper.alert("Fehler beim Drucken",
										"Beim Drucken ist ein Fehler aufgetreten. Bitte überprüfen Sie die Einstellungen.");
							}
						} else {
							dlg.setBlockOnOpen(true);
							dlg.open();
						}
					}
				}
			}
		};
		exportAction=new Action("Agenda exportieren"){
			{
				setToolTipText("Termine eines Bereichs exportieren");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_EXPORT));
			}
			@Override
			public void run(){
				ICalTransfer ict=new ICalTransfer();
				ict.doExport(actDate, actDate, actBereich);
			}
		};
		
		importAction=new Action("Termine importieren"){
			{
				setToolTipText("Termine aus einer iCal-Datei importieren");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_IMPORT));
			}
			@Override
			public void run(){
				ICalTransfer ict=new ICalTransfer();
				ict.doImport(actBereich);
			}
		};
		final IAction bereichMenu=new Action(Messages.TagesView_bereich,Action.AS_DROP_DOWN_MENU){ 
			Menu mine;
			{
				setToolTipText(Messages.TagesView_selectBereich); 
				setMenuCreator(new IMenuCreator(){

					public void dispose() {
						mine.dispose();
					}

					public Menu getMenu(Control parent) {
						mine=new Menu(parent);
						fillMenu();
						return mine;
					}

					public Menu getMenu(Menu parent) {
						mine=new Menu(parent);
						fillMenu();
						return mine;
					}});
			}
			private void fillMenu(){
				String[] sMandanten=Hub.globalCfg.get(PreferenceConstants.AG_BEREICHE, Messages.TagesView_praxis).split(","); 
				for(String m:sMandanten){
					MenuItem it=new MenuItem(mine,SWT.NONE);
					it.setText(m);
					it.addSelectionListener(new SelectionAdapter(){

						@Override
						public void widgetSelected(SelectionEvent e) {
							MenuItem mi=(MenuItem)e.getSource();
							setBereich(mi.getText());
							tv.refresh();
						}
						
					});
				}
			}
			
		};
		
		IMenuManager mgr=getViewSite().getActionBars().getMenuManager();
		mgr.add(bereichMenu);
		mgr.add(dayLimitsAction);
		mgr.add(newViewAction);
		mgr.add(exportAction);
		mgr.add(importAction);
		mgr.add(printAction);
		mgr.add(printPatientAction);
	}


}
