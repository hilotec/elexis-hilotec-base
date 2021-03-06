/*******************************************************************************
 * Copyright (c) 2009-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Sponsoring:
 * 	 mediX Notfallpaxis, diepraxen Stauffacher AG, Zürich
 * 
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: BaseView.java 6194 2010-03-14 12:13:27Z rgw_ch $
 *******************************************************************************/

package ch.elexis.agenda.ui;

import java.util.Calendar;
import java.util.Hashtable;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.Activator;
import ch.elexis.actions.AgendaActions;
import ch.elexis.actions.ElexisEvent;
import ch.elexis.actions.ElexisEventDispatcher;
import ch.elexis.actions.ElexisEventListenerImpl;
import ch.elexis.actions.GlobalEventDispatcher;
import ch.elexis.actions.GlobalEventDispatcher.IActivationListener;
import ch.elexis.actions.Heartbeat.HeartListener;
import ch.elexis.agenda.Messages;
import ch.elexis.agenda.acl.ACLContributor;
import ch.elexis.agenda.data.ICalTransfer;
import ch.elexis.agenda.data.IPlannable;
import ch.elexis.agenda.data.Termin;
import ch.elexis.agenda.preferences.PreferenceConstants;
import ch.elexis.agenda.util.Plannables;
import ch.elexis.data.Anwender;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.dialogs.TagesgrenzenDialog;
import ch.elexis.dialogs.TerminDialog;
import ch.elexis.dialogs.TerminListeDruckenDialog;
import ch.elexis.dialogs.TermineDruckenDialog;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Abstract base class for an agenda window.
 * 
 * @author Gerry
 * 
 */
public abstract class BaseView extends ViewPart implements HeartListener, IActivationListener {
	private static final String DEFAULT_PIXEL_PER_MINUTE = "1.0"; //$NON-NLS-1$
	
	public IAction newTerminAction, blockAction;
	public IAction dayLimitsAction, newViewAction, printAction, exportAction, importAction;
	public IAction printPatientAction, todayAction;
	MenuManager menu = new MenuManager();
	protected Activator agenda = Activator.getDefault();
	
	private final ElexisEventListenerImpl eeli_termin =
		new ElexisEventListenerImpl(Termin.class, ElexisEvent.EVENT_RELOAD) {
			public void runInUi(ElexisEvent ev){
				internalRefresh();
			}
		};
	
	private final ElexisEventListenerImpl eeli_user =
		new ElexisEventListenerImpl(Anwender.class, ElexisEvent.EVENT_USER_CHANGED) {
			public void runInUi(ElexisEvent ev){
				updateActions();
				agenda.setActResource(Hub.userCfg.get(PreferenceConstants.AG_BEREICH, agenda
					.getActResource()));
				
			}
		};
	
	@Override
	public void createPartControl(Composite parent){
		makeActions();
		create(parent);
		GlobalEventDispatcher.addActivationListener(this, this);
		internalRefresh();
	}
	
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}
	
	abstract protected void create(Composite parent);
	
	abstract protected void refresh();
	
	abstract protected IPlannable getSelection();
	
	private void internalRefresh(){
		if (Hub.acl.request(ACLContributor.DISPLAY_APPOINTMENTS)) {
			refresh();
		}
	}
	
	protected void checkDay(String resource, TimeTool date){
		if (date == null) {
			date = agenda.getActDate();
		}
		String day = date.toString(TimeTool.DATE_COMPACT);
		if (resource == null) {
			resource = agenda.getActResource();
		}
		Query<Termin> qbe = new Query<Termin>(Termin.class);
		qbe.add("Tag", "=", day);
		qbe.add("BeiWem", "=", resource);
		if (qbe.execute().isEmpty()) {
			Hashtable<String, String> map = Plannables.getDayPrefFor(resource);
			int d = date.get(Calendar.DAY_OF_WEEK);
			String ds = map.get(TimeTool.wdays[d - 1]);
			if (StringTool.isNothing(ds)) {
				ds = "0000-0800\n1800-2359"; //$NON-NLS-1$
			}
			String[] flds = ds.split("\r*\n\r*"); //$NON-NLS-1$
			for (String fld : flds) {
				String from = fld.substring(0, 4);
				String until = fld.replaceAll("-", "").substring(4); //$NON-NLS-1$ //$NON-NLS-2$
				new Termin(resource, day, TimeTool.getMinutesFromTimeString(from), TimeTool
					.getMinutesFromTimeString(until), Termin.typReserviert(), Termin.statusLeer());
			}
			
		}
		
	}
	
	protected void updateActions(){
		dayLimitsAction.setEnabled(Hub.acl.request(ACLContributor.CHANGE_DAYSETTINGS));
		boolean canChangeAppointments = Hub.acl.request(ACLContributor.CHANGE_APPOINTMENTS);
		newTerminAction.setEnabled(canChangeAppointments);
		AgendaActions.updateActions();
		internalRefresh();
	}
	
	public void heartbeat(){
		internalRefresh();
	}
	
	public void activation(boolean mode){

	}
	
	public void visible(boolean mode){
		if (mode) {
			Hub.heart.addListener(this);
			ElexisEventDispatcher.getInstance().addListeners(eeli_termin, eeli_user);
		} else {
			Hub.heart.removeListener(this);
			ElexisEventDispatcher.getInstance().removeListeners(eeli_termin, eeli_user);
			
		}
		
	}
	
	/**
	 * Return the scale factor, i.e. the number of Pixels to use for one minute.
	 * 
	 * @return thepixel-per-minute scale.
	 */
	public static double getPixelPerMinute(){
		String ppm =
			Hub.localCfg.get(PreferenceConstants.AG_PIXEL_PER_MINUTE, DEFAULT_PIXEL_PER_MINUTE);
		try {
			double ret = Double.parseDouble(ppm);
			return ret;
		} catch (NumberFormatException ne) {
			Hub.localCfg.set(PreferenceConstants.AG_PIXEL_PER_MINUTE, DEFAULT_PIXEL_PER_MINUTE);
			return Double.parseDouble(DEFAULT_PIXEL_PER_MINUTE);
		}
	}
	
	protected void makeActions(){
		dayLimitsAction = new Action(Messages.BaseView_dayLimits) {
			@Override
			public void run(){
				new TagesgrenzenDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getShell(), agenda.getActDate().toString(TimeTool.DATE_COMPACT), agenda
					.getActResource()).open();
				refresh();
			}
		};
		
		blockAction = new Action(Messages.TagesView_lockPeriod) {
			@Override
			public void run(){
				IPlannable p = getSelection();
				if (p != null) {
					if (p instanceof Termin.Free) {
						new Termin(agenda.getActResource(), agenda.getActDate().toString(
							TimeTool.DATE_COMPACT), p.getStartMinute(), p.getDurationInMinutes()
							+ p.getStartMinute(), Termin.typReserviert(), Termin.statusLeer());
						ElexisEventDispatcher.reload(Termin.class);
					}
				}
				
			}
		};
		newTerminAction = new Action(Messages.TagesView_newTermin) {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_NEW));
				setToolTipText(Messages.TagesView_createNewTermin);
			}
			
			@Override
			public void run(){
				new TerminDialog(null).open();
				internalRefresh();
			}
		};
		printAction = new Action(Messages.BaseView_printDayPaapintments) {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_PRINTER));
				setToolTipText(Messages.BaseView_printAPpointmentsOfSelectedDay);
			}
			
			@Override
			public void run(){
				IPlannable[] liste =
					Plannables.loadDay(agenda.getActResource(), agenda.getActDate());
				new TerminListeDruckenDialog(getViewSite().getShell(), liste).open();
				internalRefresh();
			}
		};
		printPatientAction = new Action(Messages.BaseView_printAppointments) {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_PRINTER));
				setToolTipText(Messages.BaseView_printFutureAppointmentsOfSelectedPatient);
			}
			
			@Override
			public void run(){
				Patient patient = ElexisEventDispatcher.getSelectedPatient();
				if (patient != null) {
					Query<Termin> qbe = new Query<Termin>(Termin.class);
					qbe.add("Wer", "=", patient.getId());
					qbe.add("deleted", "<>", "1");
					qbe.add("Tag", ">=", new TimeTool().toString(TimeTool.DATE_COMPACT));
					qbe.orderBy(false, "Tag", "Beginn");
					java.util.List<Termin> list = qbe.execute();
					if (list != null) {
						boolean directPrint =
							Hub.localCfg.get(
								PreferenceConstants.AG_PRINT_APPOINTMENTCARD_DIRECTPRINT,
								PreferenceConstants.AG_PRINT_APPOINTMENTCARD_DIRECTPRINT_DEFAULT);
						
						TermineDruckenDialog dlg =
							new TermineDruckenDialog(getViewSite().getShell(), list
								.toArray(new Termin[0]));
						if (directPrint) {
							dlg.setBlockOnOpen(false);
							dlg.open();
							if (dlg.doPrint()) {
								dlg.close();
							} else {
								SWTHelper.alert(Messages.BaseView_errorWhilePrinting,
									Messages.BaseView_errorHappendPrinting);
							}
						} else {
							dlg.setBlockOnOpen(true);
							dlg.open();
						}
					}
				}
			}
		};
		exportAction = new Action(Messages.BaseView_exportAgenda) {
			{
				setToolTipText(Messages.BaseView_exportAppojntmentsOfMandator);
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_GOFURTHER));
			}
			
			@Override
			public void run(){
				ICalTransfer ict = new ICalTransfer();
				ict.doExport(agenda.getActDate(), agenda.getActDate(), agenda.getActResource());
			}
		};
		
		importAction = new Action(Messages.BaseView_importAgenda) {
			{
				setToolTipText(Messages.BaseView_importFromICal);
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_IMPORT));
			}
			
			@Override
			public void run(){
				ICalTransfer ict = new ICalTransfer();
				ict.doImport(agenda.getActResource());
			}
		};
		
		todayAction = new Action(Messages.BaseView_today) {
			{
				setToolTipText(Messages.BaseView_showToday);
				setImageDescriptor(Activator.getImageDescriptor("icons/calendar_view_day.png")); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				agenda.setActDate(new TimeTool());
				internalRefresh();
			}
		};
		
		IMenuManager mgr = getViewSite().getActionBars().getMenuManager();
		mgr.add(dayLimitsAction);
		mgr.add(exportAction);
		mgr.add(importAction);
		mgr.add(printAction);
		mgr.add(printPatientAction);
		IToolBarManager tmr = getViewSite().getActionBars().getToolBarManager();
		tmr.add(todayAction);
		
	}
	
}
