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
 *  $Id: PatientMenuPopulator.java 4176 2008-07-24 19:50:11Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ch.elexis.Hub;
import ch.elexis.actions.RestrictedAction;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Patient;
import ch.elexis.dialogs.AssignEtiketteDialog;
import ch.elexis.exchange.IDataSender;
import ch.elexis.util.Extensions;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus.IMenuPopulator;
import ch.rgw.tools.ExHandler;

public class PatientMenuPopulator implements IMenuPopulator {
	IAction exportKGAction, delPatAction,etiketteAction;
	PatientenListeView mine;
	
	public IAction[] fillMenu() {
		LinkedList<IAction> ret=new LinkedList<IAction>();
		ret.add(etiketteAction);
		if(Hub.acl.request(AccessControlDefaults.KONTAKT_DELETE)){
			ret.add(delPatAction);
		}
		if(Hub.acl.request(AccessControlDefaults.KONTAKT_EXPORT)){
			ret.add(exportKGAction);
		}
		delPatAction.setEnabled(Hub.acl.request(AccessControlDefaults.KONTAKT_DELETE));
		exportKGAction.setEnabled(Hub.acl.request(AccessControlDefaults.KONTAKT_EXPORT));
		return ret.toArray(new IAction[0]);
	}
	
	PatientMenuPopulator(PatientenListeView plv){
		mine=plv;
		etiketteAction=new RestrictedAction(AccessControlDefaults.KONTAKT_ETIKETTE, "Etiketten..."){
			{
				setToolTipText("Etiketten anheften oder entfernen");
			}
			@Override
			public void doRun() {
				Patient p=mine.getSelectedPatient();
				AssignEtiketteDialog aed=new AssignEtiketteDialog(Hub.getActiveShell(),p);
				aed.open();
			}
			
		};
		delPatAction=new Action("Patient löschen"){
            @Override
            public void run()
            {
            	// access rights guard
            	if (!Hub.acl.request(AccessControlDefaults.KONTAKT_DELETE)) {
            		SWTHelper.alert("Fehlende Rechte",
            				"Sie dürfen diesen Patienten nicht löschen.");
            		return;
            	}
            	
                Patient p=mine.getSelectedPatient();
                if(p!=null){
                    if(MessageDialog.openConfirm(mine.getViewSite().getShell(),"Wirklich löschen?",p.getLabel())==true){
                    	if(p.delete(false)==false){
                            SWTHelper.alert("Konnte Patient nicht löschen",
                                    "Ein Patient kann nur gelöscht werden, wenn keine Fälle mehr dazu existieren.");
                        }else{
                            mine.reload();
                        }	
                    }
                }
            }
            
        };
	    exportKGAction=new Action("KG exportieren", Action.AS_DROP_DOWN_MENU){
	    	Menu menu=null;
	    	{
	    		setToolTipText("Gesamte KG dieses Patienten exportieren");
	    		setMenuCreator(new IMenuCreator(){

					public void dispose() {
						if(menu!=null){
							menu.dispose();
							menu=null;
						}
						
					}

					public Menu getMenu(Control parent) {
						menu=new Menu(parent);
						createMenu();
						return menu;
					}

					public Menu getMenu(Menu parent) {
						menu=new Menu(parent);
						createMenu();
						return menu;
					}
	    			
	    		});
	    	}
	    	void createMenu(){
	    		Patient p=mine.getSelectedPatient();
	            if(p!=null){
	                List<IConfigurationElement> list=Extensions.getExtensions("ch.elexis.Transporter");
					for(final IConfigurationElement ic:list){
						String name=ic.getAttribute("name");
						System.out.println(name);
						String handler=ic.getAttribute("AcceptableTypes");
						if(handler==null){
							continue;
						}
						if(handler.contains("ch.elexis.data.Patient") || (handler.contains("ch.elexis.data.*"))){
							MenuItem it=new MenuItem(menu,SWT.NONE);
							it.setText(ic.getAttribute("name"));
							it.addSelectionListener(new SelectionAdapter(){
								@Override
								public void widgetSelected(SelectionEvent e) {
									Patient pat=mine.getSelectedPatient();
									try {
										IDataSender sender = (IDataSender)ic.createExecutableExtension("ExporterClass");
										if(sender.store(pat).isOK() && sender.finalizeExport()){
											SWTHelper.showInfo("KG exportiert", "Die KG "+pat.getLabel()+" wurde erfolgreich exportiert");
										}else{
											SWTHelper.showError("Fehler", "Beim Export der KG "+pat.getLabel()+" ist ein Fehler aufgetreten");
										}
									} catch (CoreException e1) {
										ExHandler.handle(e1);
									}
								}
							});
							
						}
					}
	            }
	    	}
	    	/*
	    	        	KGExportDialog kge=new KGExportDialog(getViewSite().getShell(),p);
	                	if(kge.open()!=Dialog.OK){
	                		return;
	                	}
	                	cnt=kge.getResult();
	                }
	                if(cnt!=null){
	                	cnt.addContact(p,true);
	                	cnt.doExport();
	                }
	            }
	    	}
	    	*/
	    };
	}
	
}
