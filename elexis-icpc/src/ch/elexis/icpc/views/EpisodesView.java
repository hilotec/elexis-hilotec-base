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
 *    $Id: EpisodesView.java 2899 2007-07-25 05:06:12Z rgw_ch $
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
import ch.elexis.text.Samdas;
import ch.elexis.text.Samdas.Record;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;

public class EpisodesView extends ViewPart implements SelectionListener, ActivationListener, ObjectListener {
	public static final String ID="ch.elexis.icpc.episodesView";
	EpisodesDisplay display;
	private IAction addEpisodeAction,removeEpisodeAction,editEpisodeAction;
	
	public EpisodesView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		display=new EpisodesDisplay(parent);
		display.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		makeActions();
		ViewMenus menu=new ViewMenus(getViewSite());
		menu.createViewerContextMenu(display.tvEpisodes, removeEpisodeAction, editEpisodeAction);
		menu.createToolbar(addEpisodeAction,editEpisodeAction);
		GlobalEvents.getInstance().addActivationListener(this, getViewSite().getPart());
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void clearEvent(Class template) {
		// TODO Auto-generated method stub
		
	}

	public void selectionEvent(PersistentObject obj) {
		if(obj instanceof Patient){
			display.setPatient((Patient)obj);
		}
		
	}

	public void activation(boolean mode) {
		// TODO Auto-generated method stub
		
	}

	public void visible(boolean mode) {
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
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_ADDITEM));
			}
			public void run(){
				EditEpisodeDialog dlg = new EditEpisodeDialog(getViewSite().getShell(), null);
				if (dlg.open() == Dialog.OK) {
					display.tvEpisodes.refresh();
				}
			}
		};
		removeEpisodeAction=new Action("Problem löschen"){
			{
				setToolTipText("Das gewählte Problem unwiderriflich löschen");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_DELETE));
			}
			public void run(){
				
			}
		};
		editEpisodeAction=new Action("Problem bearbeiten"){
			{
				setToolTipText("Titel des Problems ändern");
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_EDIT));
			}
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
	}

	public void objectChanged(PersistentObject o) {
		// TODO Auto-generated method stub
		
	}

	public void objectCreated(PersistentObject o) {
		if(o instanceof Konsultation){
			Konsultation k=(Konsultation)o;
			Samdas entry=k.getEntryRaw();
			Record record=entry.getRecord();
			
		}
	}

	public void objectDeleted(PersistentObject o) {
		// TODO Auto-generated method stub
		
	}
}
