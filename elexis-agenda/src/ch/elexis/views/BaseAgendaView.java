/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: BaseAgendaView.java 3476 2007-12-22 05:28:44Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;
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
import ch.elexis.data.Anwender;
import ch.elexis.data.IPlannable;
import ch.elexis.data.Termin;
import ch.elexis.dialogs.TagesgrenzenDialog;
import ch.elexis.dialogs.TerminDialog;
import ch.elexis.dialogs.TerminListeDruckenDialog;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.Plannables;
import ch.rgw.tools.TimeTool;

public abstract class BaseAgendaView extends ViewPart implements BackingStoreListener,  HeartListener, ActivationListener {

	protected TimeTool actDate;	
	protected  String actBereich;
	protected Synchronizer pinger;
	protected SelectionListener sListen=new SelectionListener();
	TableViewer tv;
	BaseAgendaView self;
	protected IAction newTerminAction, blockAction,terminKuerzenAction,terminVerlaengernAction,terminAendernAction;
	protected IAction dayLimitsAction, newViewAction, printAction;
	MenuManager menu=new MenuManager();
	String[] bereiche;
	
	protected BaseAgendaView(){
		bereiche=Hub.globalCfg.get(PreferenceConstants.AG_BEREICHE, Messages.TagesView_14).split(","); 
		actBereich=bereiche[0];
	}
	abstract public void create(Composite parent);
	
	@Override
	public void createPartControl(Composite parent) {
		self=this;
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
		}
	}

	public String getBereich() {
		return actBereich;
	}
	public void setBereich(String b){
		actBereich=b;
		setPartName("Agenda "+b); //$NON-NLS-1$
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
	private void makeActions(){
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
				tv.refresh(true);
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
				tv.refresh(true);
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
				tv.refresh(true);
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
	}


}
