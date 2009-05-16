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
 *  $Id: AgendaParallel.java 5302 2009-05-16 08:51:07Z rgw_ch $
 *******************************************************************************/

package ch.elexis.agenda.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.Activator;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.agenda.data.IPlannable;
import ch.elexis.agenda.data.Termin;
import ch.elexis.agenda.preferences.PreferenceConstants;
import ch.elexis.data.PersistentObject;
import ch.elexis.dialogs.DateSelectorDialog;
import ch.elexis.util.PersistentObjectDragSource2;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * A View to display ressources side by side in the same view.
 * 
 * @author gerry
 * 
 */
public class AgendaParallel extends BaseView {

	private IAction dayFwdAction, dayBackAction,showCalendarAction;
	private ProportionalSheet sheet;
	private ColumnHeader header;
	
	public AgendaParallel(){

	}
	
	public ColumnHeader getHeader(){
		return header;
	}
	
	@Override
	protected void create(Composite parent){
		makePrivateActions();
		Composite wrapper = new Composite(parent, SWT.NONE);
		wrapper.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		wrapper.setLayout(new GridLayout());
		header = new ColumnHeader(wrapper, this);
		header.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		ScrolledComposite bounding = new ScrolledComposite(wrapper, SWT.V_SCROLL);
		bounding.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		// bounding.setBackground(Desk.getColor(Desk.COL_RED));
		sheet = new ProportionalSheet(bounding, this);
		// sheet.setSize(sheet.computeSize(SWT.DEFAULT,SWT.DEFAULT));
		bounding.setContent(sheet);
		bounding.setMinSize(sheet.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		bounding.setExpandHorizontal(true);
		bounding.setExpandVertical(true);
		new PersistentObjectDragSource2(bounding,new PersistentObjectDragSource2.Draggable(){

			public List<PersistentObject> getSelection() {
				System.out.println("Dragging");
				ArrayList<PersistentObject> ret=new ArrayList<PersistentObject>(1);
				ret.add(GlobalEvents.getInstance().getSelectedObject(Termin.class));
				return ret;
			}});
		
	}
	
	@Override
	public void setFocus(){
		sheet.setFocus();
	}
	@Override
	protected IPlannable getSelection(){
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Return the resources to display. This are by default all defined resources, but users
	 * can exclude some of them from display 
	 * @return a stering array with all resources to display
	 */
	public String[] getDisplayedResources(){
		String resources =
			Hub.localCfg.get(PreferenceConstants.AG_RESOURCESTOSHOW, StringTool.join(agenda
				.getResources(), ","));
		if (resources == null) {
			return new String[0];
		} else {
			return resources.split(",");
		}
	}
	
	void clear(){
		sheet.clear();
	}
	@Override
	protected void refresh(){
		sheet.refresh();
	}
	
	private void internalRefresh(){
		showCalendarAction.setText(agenda.getActDate().toString(
			TimeTool.WEEKDAY)
			+ ", " + agenda.getActDate().toString(TimeTool.DATE_GER));
		refresh();
	}

	
	private void makePrivateActions(){
		dayFwdAction = new Action("Tag vorwärts") {
			{
				setToolTipText("Nächsten Tag anzeigen");
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_NEXT));
			}

			@Override
			public void run() {
				agenda.addDays(1);
				internalRefresh();
			}
		};

		dayBackAction = new Action("Tag zurück") {
			{
				setToolTipText("Vorherigen Tag anzeigen");
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_PREVIOUS));
			}

			@Override
			public void run() {
				agenda.addDays(-1);
				internalRefresh();
			}
		};
		showCalendarAction = new Action("Tag auswählen") {
			{
				setToolTipText("Einen Kalender zur Auswahl des Tages anzeigen");
				// setImageDescriptor(Activator.getImageDescriptor("icons/calendar.png"));
			}

			@Override
			public void run() {
				DateSelectorDialog dsl = new DateSelectorDialog(getViewSite()
						.getShell(), agenda.getActDate());
				if (dsl.open() == Dialog.OK) {
					agenda.setActDate(dsl.getSelectedDate());
					internalRefresh();
				}
			}
		};

		final IAction zoomAction=new Action("Zoom",Action.AS_DROP_DOWN_MENU){
			Menu mine;
			{
				setToolTipText("Massstab einstellen");
				setImageDescriptor(Activator.getImageDescriptor("icons/zoom.png"));
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
				for(String s:new String[]{"40","60","80","100","120","140","160","200","300"}){
					MenuItem it=new MenuItem(mine,SWT.RADIO);
					it.setText(s+"%");
					it.addSelectionListener(new SelectionAdapter(){

						@Override
						public void widgetSelected(SelectionEvent e) {
							MenuItem mi=(MenuItem)e.getSource();
							int scale=Integer.parseInt(mi.getText().split("%")[0]);
							double factor=scale/100.0;
							Hub.localCfg.set(PreferenceConstants.AG_PIXEL_PER_MINUTE, Double.toString(factor));
							sheet.recalc();
						}
						
					});
				}
			}
		};
		IToolBarManager tmr=getViewSite().getActionBars().getToolBarManager();
		tmr.add(new Separator());
		tmr.add(dayBackAction);
		tmr.add(showCalendarAction);
		tmr.add(dayFwdAction);
		tmr.add(new Separator());
		tmr.add(zoomAction);
		
	}
}
