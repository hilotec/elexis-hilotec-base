/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: ReminderView.java 5024 2009-01-23 16:36:39Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views;

import java.util.List;
import java.util.SortedSet;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.RestrictedAction;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.BackingStoreListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.actions.Heartbeat.HeartListener;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Anwender;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Reminder;
import ch.elexis.dialogs.EditReminderDialog;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.DefaultLabelProvider;
import ch.elexis.util.viewers.SimpleWidgetProvider;
import ch.elexis.util.viewers.ViewerConfigurer;
import ch.rgw.io.Settings;
import ch.rgw.tools.TimeTool;

public class ReminderView extends ViewPart implements ActivationListener, BackingStoreListener,
		SelectionListener, HeartListener {
	public static final String ID = "ch.elexis.reminderview";
	private IAction newReminderAction, deleteReminderAction, onlyOpenReminderAction,
			ownReminderAction;
	private RestrictedAction othersReminderAction;
	private RestrictedAction selectPatientAction;
	private boolean bVisible;
	
	CommonViewer cv;
	ViewerConfigurer vc;
	// Patient pat;
	// String dateDue;
	// Reminder.Status status;
	// Reminder.Typ typ;
	Query<Reminder> qbe;
	Settings cfg;
	ReminderFilter filter;
	private Patient actPatient;
	
	public ReminderView(){
		qbe = new Query<Reminder>(Reminder.class);
		
	}
	
	@Override
	public void createPartControl(final Composite parent){
		cv = new CommonViewer();
		filter = new ReminderFilter();
		vc = new ViewerConfigurer(new ViewerConfigurer.ContentProviderAdapter() {
			@Override
			public Object[] getElements(final Object inputElement){
				// Display reminders only if one is logged in
				if (Hub.actUser == null) {
					return new Object[0];
				}
				SortedSet<Reminder> allReminders = Hub.actUser.getReminders(null);
				if (othersReminderAction.isChecked()
					&& Hub.acl.request(AccessControlDefaults.ADMIN_VIEW_ALL_REMINDERS)) {
					qbe.clear();
					allReminders.addAll(qbe.execute());
				} else {
					if (ownReminderAction.isChecked()) {
						qbe.clear();
						qbe.add("Creator", "=", Hub.actUser.getId());
						allReminders.addAll(qbe.execute());
					}
					// compatibility to old reminders where responsible
					// was given instead of n:m
					qbe.clear();
					qbe.add("Responsible", "=", Hub.actUser.getId());
					allReminders.addAll(qbe.execute());
					// ..to be removed later
				}
				return allReminders.toArray();
				
			}
		}, new ReminderLabelProvider(), null, // new DefaultControlFieldProvider(cv,new
			// String[]{"Fällig"}),
			new ViewerConfigurer.DefaultButtonProvider(), new SimpleWidgetProvider(
				SimpleWidgetProvider.TYPE_TABLE, SWT.SINGLE, cv));
		makeActions();
		ViewMenus menu = new ViewMenus(getViewSite());
		menu.createToolbar(newReminderAction);
		menu.createMenu(newReminderAction, deleteReminderAction, onlyOpenReminderAction,
			ownReminderAction, othersReminderAction, selectPatientAction);
		
		if (Hub.acl.request(AccessControlDefaults.ADMIN_VIEW_ALL_REMINDERS)) {
			othersReminderAction.setEnabled(true);
			othersReminderAction.setChecked(Hub.userCfg.get(PreferenceConstants.USR_REMINDEROTHERS,
				false));
		} else {
			othersReminderAction.setEnabled(false);
		}
		cv.create(vc, parent, SWT.NONE, getViewSite());
		cv.addDoubleClickListener(new CommonViewer.DoubleClickListener() {
			public void doubleClicked(final PersistentObject obj, final CommonViewer cv){
				new EditReminderDialog(getViewSite().getShell(), (Reminder) obj).open();
				cv.notify(CommonViewer.Message.update);
			}
		});
		menu.createViewerContextMenu(cv.getViewerWidget(), selectPatientAction,
			deleteReminderAction);
		cv.getViewerWidget().addFilter(filter);
		GlobalEvents.getInstance().addActivationListener(this, getViewSite().getPart());
		GlobalEvents.getInstance().addSelectionListener(this);
	}
	
	@Override
	public void setFocus(){
	// TODO Auto-generated method stub
	
	}
	
	@Override
	public void dispose(){
		GlobalEvents.getInstance().removeActivationListener(this, getViewSite().getPart());
		GlobalEvents.getInstance().removeSelectionListener(this);
		Hub.userCfg.set(PreferenceConstants.USR_REMINDERSOPEN, onlyOpenReminderAction.isChecked());
	}
	
	class ReminderLabelProvider extends DefaultLabelProvider implements IColorProvider {
		
		public Color getBackground(final Object element){
			if (element instanceof Reminder) {
				Reminder.Status stat = ((Reminder) element).getStatus();
				cfg = Hub.userCfg.getBranch(PreferenceConstants.USR_REMINDERCOLORS, true);
				if (stat == Reminder.Status.faellig) {
					return Desk.getColorFromRGB(cfg.get("fällig", "FFFFFF"));
				} else if (stat == Reminder.Status.ueberfaellig) {
					return Desk.getColorFromRGB(cfg.get("überfällig", "FF0000"));
				} else if (stat == Reminder.Status.geplant) {
					return Desk.getColorFromRGB(cfg.get("geplant", "00FF00"));
				} else {
					return null;
				}
			}
			return null;
		}
		
		public Color getForeground(final Object element){
			return null;
		}
		
	}
	
	private void makeActions(){
		newReminderAction = new Action("Neu...") {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_NEW));
				setToolTipText("Einen neuen Reminder erstellen");
			}
			
			@Override
			public void run(){
				EditReminderDialog erd = new EditReminderDialog(getViewSite().getShell(), null);
				erd.open();
				cv.notify(CommonViewer.Message.update_keeplabels);
			}
		};
		deleteReminderAction = new Action("Löschen") {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_DELETE));
				setToolTipText("Markierten Reminder löschen");
			}
			
			@Override
			public void run(){
				Object[] sel = cv.getSelection();
				if ((sel != null) && (sel.length > 0)) {
					Reminder r = (Reminder) sel[0];
					r.delete();
					cv.notify(CommonViewer.Message.update_keeplabels);
				}
			}
		};
		onlyOpenReminderAction = new Action("Nur fällige", Action.AS_CHECK_BOX) {
			{
				setToolTipText("Nur aktive Reminder anzeigen");
			}
			
			@Override
			public void run(){
				boolean bChecked = onlyOpenReminderAction.isChecked();
				Hub.userCfg.set(PreferenceConstants.USR_REMINDERSOPEN, bChecked);
				cv.notify(CommonViewer.Message.update_keeplabels);
			}
		};
		ownReminderAction = new Action("Von mir erstellte", Action.AS_CHECK_BOX) {
			{
				setToolTipText("Auch von mir erstellte Reminder für andere Anwender anzeigen");
			}
			
			@Override
			public void run(){
				boolean bChecked = ownReminderAction.isChecked();
				Hub.userCfg.set(PreferenceConstants.USR_REMINDEROWN, bChecked);
				cv.notify(CommonViewer.Message.update_keeplabels);
			}
		};
		othersReminderAction =
			new RestrictedAction(AccessControlDefaults.ADMIN_VIEW_ALL_REMINDERS, "Fremde",
				Action.AS_CHECK_BOX) {
				{
					setToolTipText("Auch Reminders anderer Anwender anzeigen");
				}
				
				@Override
				public void doRun(){
					Hub.userCfg.set(PreferenceConstants.USR_REMINDEROTHERS, othersReminderAction
						.isChecked());
					cv.notify(CommonViewer.Message.update_keeplabels);
				}
			};
		
		selectPatientAction =
			new RestrictedAction(AccessControlDefaults.PATIENT_DISPLAY, "Patient aktivieren",
				Action.AS_UNSPECIFIED) {
				{
					setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_PERSON));
					setToolTipText("Patient aktivieren, der zu dieser Pendenz gehört");
				}
				
				public void doRun(){
					Object[] sel = cv.getSelection();
					if (sel != null && sel.length > 0) {
						Reminder reminder = (Reminder) sel[0];
						Patient patient = reminder.getKontakt();
						if (patient != null) {
							GlobalEvents.getInstance().fireSelectionEvent(patient);
						}
					}
				}
			};
		
	}
	
	public void activation(final boolean mode){
	/* egal */
	}
	
	public void visible(final boolean mode){
		bVisible = mode;
		if (mode) {
			
			// GlobalEvents.getInstance().addSelectionListener(this);
			GlobalEvents.getInstance().addBackingStoreListener(this);
			Hub.heart.addListener(this);
			cv.notify(CommonViewer.Message.update);
			heartbeat();
		} else {
			// GlobalEvents.getInstance().removeSelectionListener(this);
			GlobalEvents.getInstance().removeBackingStoreListener(this);
			Hub.heart.removeListener(this);
		}
		
	}
	
	public void reloadContents(final Class<? extends PersistentObject> clazz){
		if (clazz.equals(Reminder.class)) {
			cv.notify(CommonViewer.Message.update);
		}
	}
	
	public void clearEvent(final Class<? extends PersistentObject> template){
		if (template.equals(Patient.class)) {

		}
	}
	
	public void selectionEvent(final PersistentObject obj){
		if (obj instanceof Patient) {
			if (((Patient) obj).equals(actPatient)) {
				return;
			}
			actPatient = (Patient) obj;
			if (bVisible) {
				cv.notify(CommonViewer.Message.update);
			}
			Desk.asyncExec(new Runnable() {
				
				public void run(){
					List<Reminder> list =
						Reminder.findRemindersDueFor((Patient) obj, Hub.actUser, true);
					if (list.size() != 0) {
						StringBuilder sb = new StringBuilder();
						for (Reminder r : list) {
							sb.append(r.getMessage()).append("\n\n");
						}
						SWTHelper.alert("Wichtige Reminders zu diesem Patienten", sb.toString());
					}
				}
				
			});
		} else if (obj instanceof Anwender) {
			boolean bChecked = Hub.userCfg.get(PreferenceConstants.USR_REMINDERSOPEN, true);
			onlyOpenReminderAction.setChecked(bChecked);
			ownReminderAction.setChecked(Hub.userCfg
				.get(PreferenceConstants.USR_REMINDEROWN, false));
			
			// get state from user's configuration
			othersReminderAction.setChecked(Hub.userCfg.get(PreferenceConstants.USR_REMINDEROTHERS,
				false));
			
			// update action's access rights
			othersReminderAction.reflectRight();
			
			if (bVisible) {
				cv.notify(CommonViewer.Message.update);
			}
		}
	}
	
	public void heartbeat(){
		cv.notify(CommonViewer.Message.update_keeplabels);
	}
	
	class ReminderFilter extends ViewerFilter {
		@Override
		public boolean select(final Viewer viewer, final Object parentElement, final Object element){
			if (element instanceof Reminder) {
				Reminder check = (Reminder) element;
				if (onlyOpenReminderAction.isChecked()) {
					if (check.getDateDue().isAfter(new TimeTool())) {
						return false;
					}
					if (check.getStatus().ordinal() > 2) {
						return false;
					}
				}
				Patient act = GlobalEvents.getSelectedPatient();
				if (act != null) {
					if (!check.get("IdentID").equals(act.getId())) {
						if (check.getTyp() != Reminder.Typ.anzeigeTodoAll) {
							return false;
						}
					}
				}
				
			}
			return true;
		}
		
	}
}
