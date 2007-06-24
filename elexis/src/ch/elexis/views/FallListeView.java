/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: FallListeView.java 1832 2007-02-18 09:12:31Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import static ch.elexis.actions.GlobalActions.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.*;
import ch.elexis.data.FilterFactory.Filter;
import ch.elexis.util.*;
import ch.elexis.util.ViewerConfigurer.ButtonProvider;

/**
 * Eine View, die untereinander Fälle und zugehörigende Behandlungen des aktuell
 * ausgewählten Patienten anzeigt.
 * @author gerry
 *
 */
public class FallListeView extends ViewPart implements SelectionListener, ActivationListener, ISaveablePart2{
	public static final String ID="ch.elexis.FallListeView";
	CommonViewer fallViewer;
    CommonViewer behandlViewer;
    private ViewerConfigurer fallCf, behandlCf;
    private FormToolkit tk;
    private Form form;
    private Patient actPatient;
    private Fall actFall;
    private Konsultation actBehandlung;
    private Filter behandlungsFilter;
    private Image iClosed;
	
    
	public FallListeView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		iClosed=new Image(Desk.theDisplay,Hub.getBasePath()+"/rsc/schloss.gif");
		tk=new FormToolkit(Desk.theDisplay);
		form=tk.createForm(parent);
		form.getBody().setLayout(new GridLayout());
		SashForm sash=new SashForm(form.getBody(),SWT.VERTICAL);
		form.setText("Kein Patient ausgewählt");
		sash.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		ButtonProvider fallButton=new ButtonProvider(){

			public Button createButton(Composite parent1) {
				Button ret=tk.createButton(parent1,"Neuer Fall",SWT.PUSH);
				ret.addSelectionListener(new SelectionAdapter(){
					@Override
					public void widgetSelected(SelectionEvent e) {
						String bez=fallCf.getControlFieldProvider().getValues()[0];
						Fall fall=actPatient.neuerFall(bez,"Krankheit","KVG");
						Konsultation b=fall.neueKonsultation();
						b.setMandant(Hub.actMandant);
						fallCf.getControlFieldProvider().clearValues();
						fallViewer.getViewerWidget().refresh();
						fallViewer.setSelection(fall,true);

					}
					
				});
				return ret;
			}
			public boolean isAlwaysEnabled() {
				return false;
			}};
        fallViewer=new CommonViewer();
        fallCf=new ViewerConfigurer(new DefaultContentProvider(fallViewer,Fall.class){
	            @Override
	        	public Object[] getElements(Object inputElement){
	         
	            	if(actPatient!=null){
	            		if(fallCf.getControlFieldProvider().isEmpty()){
	            			return actPatient.getFaelle();
	            		}else{
	            			ViewerFilter filter=fallCf.getControlFieldProvider().createFilter();
	            			List<String> list=actPatient.getList("Faelle", true);
	            			ArrayList<Fall> arr=new ArrayList<Fall>();
	            			for(String s:list){
	            				Fall f=Fall.load(s);
	            				if(filter.select(null, null, f)){
	            					arr.add(f);
	            				}
	            			}
	            			return arr.toArray();
	            		}
	            	}
	            	return new Object[0];
	            }
        	},
        	new LabelProvider(){
				@Override
				public Image getImage(Object element) {
					if(element instanceof Fall){
						if(((Fall)element).isOpen()){
							return null;
						}else{
							return iClosed;							
						}
					}
					return super.getImage(element);
				}

				@Override
				public String getText(Object element) {
					return (((Fall)element).getLabel());
				}
				
        		
        	},
        	new DefaultControlFieldProvider(fallViewer, new String[]{"Bezeichnung"}),
        	fallButton,
        	new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_TABLE,SWT.SINGLE,fallViewer)
         );
        fallViewer.create(fallCf,sash,SWT.NONE,getViewSite());
        fallViewer.getViewerWidget().addSelectionChangedListener(GlobalEvents.getInstance().getDefaultListener());
        behandlViewer=new CommonViewer();
        ButtonProvider behandlButton=new ButtonProvider(){
			public Button createButton(Composite parent1) {
				Button ret=tk.createButton(parent1,"Neue Konsultation",SWT.PUSH);
				ret.addSelectionListener(new SelectionAdapter(){
					@Override
					public void widgetSelected(SelectionEvent e) {
						Konsultation b=actFall.neueKonsultation();
						if(b!=null){
							b.setMandant(Hub.actMandant);
							behandlCf.getControlFieldProvider().clearValues();
							behandlViewer.getViewerWidget().refresh();
							//behandlViewer.setSelection(b);
							setFall(actFall,b);
						}
                        
					}
					
				});
				return ret;
			}

			public boolean isAlwaysEnabled() {
				return true;
			}
        	
        };
        behandlCf=new ViewerConfigurer(
        		new ViewerConfigurer.ContentProviderAdapter(){
	            @Override
	        	public Object[] getElements(Object inputElement)
	            {
	            	if(actFall!=null){
	            		Konsultation[] alle=actFall.getBehandlungen(true);
	            		if(behandlungsFilter!=null){
	            			ArrayList<Konsultation> al=new ArrayList<Konsultation>(alle.length);
	            			for(int i=0;i<alle.length;i++){
	            				if(behandlungsFilter.select(alle[i])==true){
	            					al.add(alle[i]);
	            				}
	            			}
	            			return al.toArray();
	            		}
	            		return actFall.getBehandlungen(true);
	            	}
	            	return new Object[0];
	            }
        	},
	        new DefaultLabelProvider(),
	        new DefaultControlFieldProvider(behandlViewer, new String[]{"Datum"}),
	        behandlButton,
	        new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LIST,SWT.SINGLE|SWT.V_SCROLL,behandlViewer)
         );
        Composite cf=new Composite(sash,SWT.BORDER);
        cf.setLayout(new GridLayout());
        behandlViewer.create(behandlCf,cf,SWT.NONE,getViewSite());
        behandlViewer.getViewerWidget().addSelectionChangedListener(GlobalEvents.getInstance().getDefaultListener());
		tk.adapt(sash,false,false);
    	GlobalEvents.getInstance().addActivationListener(this,this);
	    sash.setWeights(new int[]{50,50});
	    behandlungsFilter=null;
	    createMenuAndToolbar();
		createContextMenu();
        ((DefaultContentProvider)fallCf.getContentProvider()).startListening();

	}

	private void createContextMenu() {
		MenuManager fallMenuMgr=new MenuManager();
		fallMenuMgr.setRemoveAllWhenShown(true);
		fallMenuMgr.addMenuListener(new IMenuListener(){
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(openFallaction);
				manager.add(reopenFallAction);
				manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
				manager.add(delFallAction);		
				manager.add(new Separator());
				manager.add(makeBillAction);
			}
		});
       
		Menu fallMenu=fallMenuMgr.createContextMenu(fallViewer.getViewerWidget().getControl());
		fallViewer.getViewerWidget().getControl().setMenu(fallMenu);
		getSite().registerContextMenu("ch.elexis.FallListeMenu",fallMenuMgr,fallViewer.getViewerWidget());
		
		MenuManager behdlMenuMgr=new MenuManager();
		behdlMenuMgr.setRemoveAllWhenShown(true);
		behdlMenuMgr.addMenuListener(new IMenuListener(){
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
				manager.add(delKonsAction);				
				manager.add(moveBehandlungAction);
				manager.add(redateAction);
			}
		});
		Menu behdlMenu=behdlMenuMgr.createContextMenu(behandlViewer.getViewerWidget().getControl());
		behandlViewer.getViewerWidget().getControl().setMenu(behdlMenu);
		getSite().registerContextMenu("ch.elexis.BehandlungsListeMenu",behdlMenuMgr,behandlViewer.getViewerWidget());
	}

	private void createMenuAndToolbar() {
		IMenuManager mgr=getViewSite().getActionBars().getMenuManager();
		mgr.add(delFallAction);
		mgr.add(delKonsAction);
		mgr.add(new Separator());
		//mgr.add(filterAction);
		IToolBarManager tmg=getViewSite().getActionBars().getToolBarManager();
		tmg.add(GlobalActions.helpAction);	
		//tmg.add(filterAction);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	
	public void setFall(Fall f, Konsultation b){
		actFall=f;
        if(f!=null){
        	actPatient=f.getPatient();
        	//System.out.println(actPatient.getLabel());
    		form.setText(actPatient.getLabel());
    		fallViewer.notify(CommonViewer.Message.update);
    		fallViewer.setSelection(f,false);
            if(b==null){
                b=f.getLetzteBehandlung();
            }
            if(b!=null){
                behandlViewer.setSelection(b,false);            	
            }
            //Hub.actBehandlung=b;
            actBehandlung=b;
           	reopenFallAction.setEnabled(!f.isOpen());
            behandlViewer.getViewerWidget().refresh(true);

        }else{
        	GlobalEvents.getInstance().clearSelection(Konsultation.class);
        	GlobalEvents.getInstance().clearSelection(Fall.class);
        	if(actPatient==null){
        		form.setText("Kein Patient ausgewählt");
        	}else{
        		form.setText(actPatient.getLabel());
        	}
        	fallViewer.notify(CommonViewer.Message.update);
           	reopenFallAction.setEnabled(false);
        	behandlViewer.getViewerWidget().refresh(true);
        }

	}
		
	public void selectionEvent(PersistentObject obj) {
		if(obj instanceof Patient){
			actPatient=(Patient)obj;
			form.setText(actPatient.getPersonalia());
			 fallViewer.getViewerWidget().refresh(false);
		}else if(obj instanceof Fall){
			Fall f=(Fall)obj;
			setFall(f,null);
		}
	}

	@Override
	public void dispose() {
		((DefaultContentProvider)fallCf.getContentProvider()).stopListening();
		GlobalEvents.getInstance().removeActivationListener(this,this);
		super.dispose();
	}

	public void activation(boolean mode) {
		// TODO Auto-generated method stub
		
	}

	public void visible(boolean mode) {
		if(mode==true){
			GlobalEvents.getInstance().addSelectionListener(this);
			actPatient=(Patient)GlobalEvents.getInstance().getSelectedObject(Patient.class);
			actFall=(Fall)GlobalEvents.getInstance().getSelectedObject(Fall.class);
			//System.out.println(actPatient.getLabel());
			if(actPatient!=null){
				if(actFall==null){
					actBehandlung=actPatient.getLetzteKons(false);
					if(actBehandlung==null){
						actFall=null;
					}else{
						actFall=actBehandlung.getFall();
					}
				}else{
					//System.out.println(actFall.getPatient().getLabel());
					if(actFall.getPatient().getId().equals(actPatient.getId())) {
						if(actBehandlung!=null){
							if((actBehandlung.getFall()==null) ||
							(!actBehandlung.getFall().getId().equals(actFall.getId()))){
								actBehandlung=actPatient.getLetzteKons(false);
							}
						}
					} else {
						actBehandlung=actPatient.getLetzteKons(false);
						if(actBehandlung==null){
							actFall=null;
						}else{
							actFall=actBehandlung.getFall();
						}
					}
				}
			}else{
				actFall=null;
				actBehandlung=null;
			}
			setFall(actFall,actBehandlung);
			
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
		
	}

	public void clearEvent(Class template) {
		// TODO Auto-generated method stub
		
	};
	/* ******
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2
	 * Wir benötigen das Interface nur, um das Schliessen einer View zu verhindern,
	 * wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */ 
	public int promptToSaveOnClose() {
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL : ISaveablePart2.NO;
	}
	public void doSave(IProgressMonitor monitor) { /* leer */ }
	public void doSaveAs() { /* leer */}
	public boolean isDirty() {
		return true;
	}
	public boolean isSaveAsAllowed() {
		return false;
	}
	public boolean isSaveOnCloseNeeded() {
		return true;
	}    
}
