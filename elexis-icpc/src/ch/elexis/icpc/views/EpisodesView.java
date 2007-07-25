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
 *    $Id: EpisodesView.java 2919 2007-07-25 19:15:14Z rgw_ch $
 *******************************************************************************/

package ch.elexis.icpc.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.ObjectListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.icpc.Episode;
import ch.elexis.icpc.KonsFilter;
import ch.elexis.text.Samdas;
import ch.elexis.text.Samdas.Record;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;

public class EpisodesView extends ViewPart implements SelectionListener, ActivationListener, ObjectListener {
	public static final String ID="ch.elexis.icpc.episodesView";
	EpisodesDisplay display;
	KonsFilter episodesFilter=new KonsFilter(this);
	private IAction addEpisodeAction,removeEpisodeAction,editEpisodeAction,activateEpisodeAction,konsFilterAction;
		
	public EpisodesView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new GridLayout());
		display=new EpisodesDisplay(parent);
		display.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		makeActions();
		ViewMenus menu=new ViewMenus(getViewSite());

		menu.createViewerContextMenu(display.tvEpisodes, activateEpisodeAction, editEpisodeAction, null,removeEpisodeAction);
		menu.createToolbar(konsFilterAction,addEpisodeAction,editEpisodeAction);
		GlobalEvents.getInstance().addActivationListener(this, getViewSite().getPart());
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void clearEvent(final Class<? extends PersistentObject> template) {
		// TODO Auto-generated method stub
		
	}

	public void selectionEvent(final PersistentObject obj) {
		if(obj instanceof Patient){
			display.setPatient((Patient)obj);
		}else if(obj instanceof Episode){
			Episode ep=(Episode)obj;
			if(ep.getStatus()==Episode.ACTIVE){
				activateEpisodeAction.setChecked(true);
			}else{
				activateEpisodeAction.setChecked(false);
			}
			if(konsFilterAction.isChecked()){
				episodesFilter.setProblem(ep);
			}
		}
		
	}

	public void activation(final boolean mode) {
		// TODO Auto-generated method stub
		
	}

	public void visible(final boolean mode) {
		if(mode){
			display.setPatient(GlobalEvents.getSelectedPatient());
			GlobalEvents.getInstance().addSelectionListener(this);			
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
		
	}

	private void makeActions(){
		addEpisodeAction=new Action("Neues Problem"){
			{
				setToolTipText("Eine neues Problem erstellen");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_NEW));
			}
			@Override
			public void run(){
				EditEpisodeDialog dlg = new EditEpisodeDialog(getViewSite().getShell(), null);
				if (dlg.open() == Dialog.OK) {
					display.tvEpisodes.refresh();
				}
			}
		};
		removeEpisodeAction=new Action("Problem löschen"){
			{
				setToolTipText("Das gewählte Problem unwiderruflich löschen");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_DELETE));
			}
			@Override
			public void run(){
				Episode act=display.getSelectedEpisode();
				if(act!=null){
					act.delete();
					display.tvEpisodes.refresh();
				}
			}
		};
		editEpisodeAction=new Action("Problem bearbeiten"){
			{
				setToolTipText("Titel des Problems ändern");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_EDIT));
			}
			@Override
			public void run(){
				Episode ep=display.getSelectedEpisode();
				if(ep!=null){
					EditEpisodeDialog dlg = new EditEpisodeDialog(getViewSite().getShell(), ep);
					if (dlg.open() == Dialog.OK) {
						display.tvEpisodes.refresh();
					}
				}
			}
		};
		activateEpisodeAction=new Action("Aktiv",Action.AS_CHECK_BOX){
			{
				setToolTipText("Problem aktivieren oder deaktivieren");
			}
			@Override
			public void run(){
				Episode ep=display.getSelectedEpisode();
				if(ep!=null){
					ep.setStatus(activateEpisodeAction.isChecked() ? Episode.ACTIVE : Episode.INACTIVE);
					display.tvEpisodes.refresh();
				}
			}
			
		};
		
		konsFilterAction=new Action("Konsultationen filtern",Action.AS_CHECK_BOX){
			{
				setToolTipText("Konsultationslisten auf markiertes Problem gebrenzen");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_FILTER));
			}
			@Override
			public void run(){
				if(!isChecked()){
					GlobalEvents.getInstance().getObjectFilters().unregisterObjectFilter(Konsultation.class, episodesFilter);
				}else{
					GlobalEvents.getInstance().getObjectFilters().registerObjectFilter(Konsultation.class, episodesFilter);
					Episode ep=display.getSelectedEpisode();
					episodesFilter.setProblem(ep);
				}
			}
		};
	}

	public void activateKonsFilterAction(final boolean bActivate){
		konsFilterAction.setChecked(bActivate);
	}
	
	public void objectChanged(final PersistentObject o) {
		// TODO Auto-generated method stub
		
	}

	public void objectCreated(final PersistentObject o) {
		if(o instanceof Konsultation){
			Konsultation k=(Konsultation)o;
			Samdas entry=k.getEntryRaw();
			Record record=entry.getRecord();
			
		}
	}

	public void objectDeleted(final PersistentObject o) {
		// TODO Auto-generated method stub
		
	}
}
