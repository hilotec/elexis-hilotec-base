/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and Elexis
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
 *  $Id: BaseView.java 5302 2009-05-16 08:51:07Z rgw_ch $
 *******************************************************************************/

package ch.elexis.agenda.ui;

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
import ch.elexis.actions.GlobalEvents;
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
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.dialogs.TagesgrenzenDialog;
import ch.elexis.dialogs.TerminDialog;
import ch.elexis.dialogs.TerminListeDruckenDialog;
import ch.elexis.dialogs.TermineDruckenDialog;
import ch.elexis.util.Plannables;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

/**
 * Abstract base class for an agenda window.
 * 
 * @author Gerry
 * 
 */
public abstract class BaseView extends ViewPart implements
		BackingStoreListener, HeartListener, ActivationListener {
	private static final String DEFAULT_PIXEL_PER_MINUTE = "1.0";

	public IAction newTerminAction, blockAction, terminKuerzenAction,
			terminVerlaengernAction, terminAendernAction;
	public IAction dayLimitsAction, newViewAction, printAction, exportAction,
			importAction;
	public IAction printPatientAction, todayAction;
	MenuManager menu = new MenuManager();
	protected Activator agenda = Activator.getDefault();

	@Override
	public void createPartControl(Composite parent) {
		makeActions();
		create(parent);
		GlobalEvents.getInstance().addActivationListener(this, this);
		internalRefresh();
	}

	@Override
	public void dispose() {
		GlobalEvents.getInstance().removeActivationListener(this, this);
		super.dispose();
	}

	abstract protected void create(Composite parent);

	abstract protected void refresh();

	abstract protected IPlannable getSelection();

	private void internalRefresh() {
		if (Hub.acl.request(ACLContributor.DISPLAY_APPOINTMENTS)) {
			refresh();
		}
	}

	protected void updateActions() {
		dayLimitsAction.setEnabled(Hub.acl
				.request(ACLContributor.CHANGE_DAYSETTINGS));
		boolean canChangeAppointments = Hub.acl
				.request(ACLContributor.CHANGE_APPOINTMENTS);
		newTerminAction.setEnabled(canChangeAppointments);
		terminKuerzenAction.setEnabled(canChangeAppointments);
		terminVerlaengernAction.setEnabled(canChangeAppointments);
		terminAendernAction.setEnabled(canChangeAppointments);
		AgendaActions.updateActions();
		internalRefresh();
	}

	public void reloadContents(Class<? extends PersistentObject> clazz) {
		if (clazz.equals(Termin.class)) {
			Desk.getDisplay().asyncExec(new Runnable() {
				public void run() {
					internalRefresh();

				}
			});
		} else if (clazz.equals(Anwender.class)) {
			updateActions();
			agenda.setActResource(Hub.userCfg.get(
					PreferenceConstants.AG_BEREICH, agenda.getActResource()));
		}

	}

	public void heartbeat() {
		internalRefresh();
	}

	public void activation(boolean mode) {

	}

	public void visible(boolean mode) {
		if (mode) {
			Hub.heart.addListener(this);
			GlobalEvents.getInstance().addBackingStoreListener(this);
		} else {
			Hub.heart.removeListener(this);
			GlobalEvents.getInstance().removeBackingStoreListener(this);
		}

	}

	/**
	 * Return the scale factor, i.e. the number of Pixels to use for one minute.
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
	protected void makeActions() {
		dayLimitsAction = new Action("Tagesgrenzen") {
			@Override
			public void run() {
				new TagesgrenzenDialog(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(), agenda
						.getActDate().toString(TimeTool.DATE_COMPACT), agenda
						.getActResource()).open();
				refresh();
			}
		};

		blockAction = new Action(Messages.TagesView_lockPeriod) {
			@Override
			public void run() {
				IPlannable p = getSelection();
				if (p != null) {
					if (p instanceof Termin.Free) {
						new Termin(agenda.getActResource(), agenda.getActDate()
								.toString(TimeTool.DATE_COMPACT), p
								.getStartMinute(), p.getDurationInMinutes()
								+ p.getStartMinute(), Termin.typReserviert(),
								Termin.statusLeer());
						GlobalEvents.getInstance()
								.fireUpdateEvent(Termin.class);
					}
				}

			}
		};
		terminAendernAction = new Action(Messages.TagesView_changeTermin) {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_EDIT));
				setToolTipText(Messages.TagesView_changeThisTermin);
			}

			@Override
			public void run() {
				TerminDialog dlg = new TerminDialog((Termin) GlobalEvents
						.getInstance().getSelectedObject(Termin.class));
				dlg.open();
				internalRefresh();

			}
		};
		terminKuerzenAction = new Action(Messages.TagesView_shortenTermin) {
			@Override
			public void run() {
				Termin t = (Termin) GlobalEvents.getInstance()
						.getSelectedObject(Termin.class);
				if (t != null) {
					t.setDurationInMinutes(t.getDurationInMinutes() >> 1);
					GlobalEvents.getInstance().fireUpdateEvent(Termin.class);
				}
			}
		};
		terminVerlaengernAction = new Action(Messages.TagesView_enlargeTermin) {
			@Override
			public void run() {
				Termin t = (Termin) GlobalEvents.getInstance()
						.getSelectedObject(Termin.class);
				if (t != null) {
					agenda.setActDate(t.getDay());
					Termin n = Plannables.getFollowingTermin(agenda
							.getActResource(), agenda.getActDate(), t);
					if (n != null) {
						t.setEndTime(n.getStartTime());
						// t.setDurationInMinutes(t.getDurationInMinutes()+15);
						GlobalEvents.getInstance()
								.fireUpdateEvent(Termin.class);
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
			public void run() {
				new TerminDialog(null).open();
				internalRefresh();
			}
		};
		printAction = new Action("Tagesliste drucken") {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_PRINTER));
				setToolTipText("Termine des gewählten Tages ausdrucken");
			}

			@Override
			public void run() {
				IPlannable[] liste = Plannables.loadDay(
						agenda.getActResource(), agenda.getActDate());
				new TerminListeDruckenDialog(getViewSite().getShell(), liste)
						.open();
				internalRefresh();
			}
		};
		printPatientAction = new Action("Patienten-Termine drucken") {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_PRINTER));
				setToolTipText("Zukünftige Termine des ausgewählten Patienten drucken");
			}

			@Override
			public void run() {
				Patient patient = GlobalEvents.getSelectedPatient();
				if (patient != null) {
					Query<Termin> qbe = new Query<Termin>(Termin.class);
					qbe.add("Wer", "=", patient.getId());
					qbe.add("deleted", "<>", "1");
					qbe.add("Tag", ">=", new TimeTool()
							.toString(TimeTool.DATE_COMPACT));
					qbe.orderBy(false, "Tag", "Beginn");
					java.util.List<Termin> list = qbe.execute();
					if (list != null) {
						boolean directPrint = Hub.localCfg
								.get(
										PreferenceConstants.AG_PRINT_APPOINTMENTCARD_DIRECTPRINT,
										PreferenceConstants.AG_PRINT_APPOINTMENTCARD_DIRECTPRINT_DEFAULT);

						TermineDruckenDialog dlg = new TermineDruckenDialog(
								getViewSite().getShell(), list
										.toArray(new Termin[0]));
						if (directPrint) {
							dlg.setBlockOnOpen(false);
							dlg.open();
							if (dlg.doPrint()) {
								dlg.close();
							} else {
								SWTHelper
										.alert(
												"Fehler beim Drucken",
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
		exportAction = new Action("Agenda exportieren") {
			{
				setToolTipText("Termine eines Bereichs exportieren");
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_GOFURTHER));
			}

			@Override
			public void run() {
				ICalTransfer ict = new ICalTransfer();
				ict.doExport(agenda.getActDate(), agenda.getActDate(), agenda
						.getActResource());
			}
		};

		importAction = new Action("Termine importieren") {
			{
				setToolTipText("Termine aus einer iCal-Datei importieren");
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_IMPORT));
			}

			@Override
			public void run() {
				ICalTransfer ict = new ICalTransfer();
				ict.doImport(agenda.getActResource());
			}
		};

		
		todayAction = new Action("heute") {
			{
				setToolTipText("heutigen Tag anzeigen");
				setImageDescriptor(Activator
						.getImageDescriptor("icons/calendar_view_day.png"));
			}

			@Override
			public void run() {
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
