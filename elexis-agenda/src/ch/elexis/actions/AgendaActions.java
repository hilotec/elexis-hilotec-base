/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation, adapted from JavaAgenda
 *    
 *  $Id: AgendaActions.java 2233 2007-04-17 13:04:14Z rgw_ch $
 *******************************************************************************/
package ch.elexis.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.agenda.Messages;
import ch.elexis.agenda.acl.ACLContributor;
import ch.elexis.data.Termin;
import ch.elexis.dialogs.TerminStatusDialog;

public class AgendaActions {
	public static IAction changeTerminStatusAction, delTerminAction, terminStatusAction;
	public static IAction terminLeerAction;
	public static IAction unblockAction;
	
	public static void updateActions(){
		changeTerminStatusAction.setEnabled(Hub.acl.request(ACLContributor.USE_AGENDA));
		terminStatusAction.setEnabled(Hub.acl.request(ACLContributor.USE_AGENDA));
		delTerminAction.setEnabled(Hub.acl.request(ACLContributor.DELETE_APPOINTMENTS));
	}
	
	static void makeActions(){

		unblockAction=new Action(Messages.AgendaActions_unblock){ 
			@Override
			public void run(){
				Termin t=(Termin) GlobalEvents.getInstance().getSelectedObject(Termin.class);
				if((t!=null) && (t.getType().equals(Termin.typReserviert()))){
					t.delete();
					GlobalEvents.getInstance().fireUpdateEvent(Termin.class);
				}
			}
		};
		
		
		changeTerminStatusAction=new Action(Messages.AgendaActions_state){ 
			public void run(){
				TerminStatusDialog dlg=new TerminStatusDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
				dlg.open();
			}
		};
		delTerminAction=new Action(Messages.AgendaActions_deleteDate){ 
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_DELETE));
				setToolTipText(Messages.AgendaActions_deleteDate); 
			}
			@Override
			public void run(){
				Termin t=(Termin)GlobalEvents.getInstance().getSelectedObject(Termin.class);
				t.delete();
				GlobalEvents.getInstance().fireUpdateEvent(Termin.class);
			}
		};
		terminStatusAction=new Action(Messages.AgendaActions_state,Action.AS_DROP_DOWN_MENU){ 
			Menu mine=null;
			{
				setMenuCreator(new IMenuCreator(){
					public void dispose() {
						if(mine!=null){
							mine.dispose();
						}
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
					}
					
				});
			}
			void fillMenu(){
				for(String t:Termin.TerminStatus){
					MenuItem it=new MenuItem(mine,SWT.NONE);
					it.setText(t);
					it.addSelectionListener(new SelectionAdapter(){
						@Override
						public void widgetSelected(SelectionEvent e) {
							Termin act=(Termin)GlobalEvents.getInstance().getSelectedObject(Termin.class);
							MenuItem it=(MenuItem)e.getSource();
							act.setStatus(it.getText());
							GlobalEvents.getInstance().fireUpdateEvent(Termin.class);
						}
					});
				}
			}
		};
	}
}
