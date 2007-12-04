/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: BriefAuswahl.java 3412 2007-12-04 13:33:29Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.Brief;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.dialogs.DocumentSelectDialog;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.DefaultContentProvider;
import ch.elexis.util.DefaultControlFieldProvider;
import ch.elexis.util.DefaultLabelProvider;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.SimpleWidgetProvider;
import ch.elexis.util.ViewMenus;
import ch.elexis.util.ViewerConfigurer;
import ch.rgw.tools.ExHandler;

public class BriefAuswahl extends ViewPart implements SelectionListener, ActivationListener, ISaveablePart2{
	public final static String ID="ch.elexis.BriefAuswahlView";
	private final FormToolkit tk;
	private Form form;
	private Action briefNeuAction,briefLadenAction, editNameAction;
	private Action deleteAction;
	private ViewMenus menus;
	CTabFolder ctab;
	//private ViewMenus menu;
	//private IAction delBriefAction;
	public BriefAuswahl() {
		tk=Desk.theToolkit;
	}

	@Override
	public void createPartControl(final Composite parent){
		StringBuilder sb=new StringBuilder();
		sb.append("Alle,").append(Brief.UNKNOWN).append(",").append(Brief.AUZ).append(",")
			.append(Brief.RP).append(",").append(Brief.LABOR);
		String cats=Hub.globalCfg.get(PreferenceConstants.DOC_CATEGORY,sb.toString());
		parent.setLayout(new GridLayout());
		form=tk.createForm(parent);
		form.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		Composite body=form.getBody();
		body.setLayout(new GridLayout());
	
		ctab=new CTabFolder(body,SWT.BOTTOM);
		ctab.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		makeActions();
		menus=new ViewMenus(getViewSite());

		for(String cat:cats.split(",")){
			CTabItem ct=new CTabItem(ctab,SWT.NONE);
			ct.setText(cat);
			sPage page=new sPage(ctab,cat);
			menus.createViewerContextMenu(page.cv.getViewerWidget(),editNameAction,deleteAction);
			ct.setData(page.cv);
			ct.setControl(page);
		}
		ctab.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(final SelectionEvent e) {
				relabel();
			}
			
		});
		GlobalEvents.getInstance().addActivationListener(this,this);
		menus.createMenu(briefNeuAction,briefLadenAction,editNameAction,deleteAction);
		menus.createToolbar(briefNeuAction,briefLadenAction,deleteAction);
		ctab.setSelection(0);
		relabel();
	}
	
	@Override
	public void dispose(){
		GlobalEvents.getInstance().removeSelectionListener(this);
		GlobalEvents.getInstance().removeActivationListener(this,this);
		for(CTabItem it:ctab.getItems()){
			CommonViewer cv=(CommonViewer)it.getData();
			cv.getConfigurer().getContentProvider().stopListening();
		}
	}
	@Override
	public void setFocus() {
		
	}
	public void relabel(){
		Patient pat=GlobalEvents.getSelectedPatient();
		if(pat==null){
			form.setText("Kein Patient ausgewählt");
		}else{
			form.setText(pat.getLabel());
			CTabItem sel=ctab.getSelection();
			if(sel!=null){
				CommonViewer cv=(CommonViewer)sel.getData();
				cv.notify(CommonViewer.Message.update);
			}
		}
		
	}
	public void selectionEvent(final PersistentObject obj) {
		if(obj instanceof Patient){
			relabel();
		}
		
	}
	
	
	class sPage extends Composite{
		private final CommonViewer cv;
		private final ViewerConfigurer vc;
		
		sPage(final Composite parent,final String cat){
			super(parent,SWT.NONE);
			setLayout(new GridLayout());
			cv=new CommonViewer();
			vc=new ViewerConfigurer(
				new DefaultContentProvider(cv,Brief.class){

					@Override
					public Object[] getElements(final Object inputElement) {
						Patient actPat=GlobalEvents.getSelectedPatient();
						if(actPat!=null){
							Query<Brief> qbe=new Query<Brief>(Brief.class);
							qbe.add("PatientID","=",actPat.getId());
							if(cat.equals("Alle")){
								qbe.add("Typ","<>",Brief.TEMPLATE);
							}else{
								qbe.add("Typ","=",cat);
							}
							cv.getConfigurer().getControlFieldProvider().setQuery(qbe);
							List list=qbe.execute();
							return list.toArray();
						}else{
							return new Brief[0];
						}
					}

				},
				new DefaultLabelProvider(),
				new DefaultControlFieldProvider(cv,new String[]{"Betreff=Titel"}),
				new ViewerConfigurer.DefaultButtonProvider(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LIST,SWT.V_SCROLL,cv)
			);
			cv.create(vc,this,SWT.NONE,getViewSite());
			vc.getContentProvider().startListening();
			Button bLoad=tk.createButton(this,"Laden",SWT.PUSH);
			bLoad.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(final SelectionEvent e) {
					try{
						TextView tv=(TextView)getViewSite().getPage().showView(TextView.ID);
						Object[] o=cv.getSelection();
						if((o!=null) && (o.length>0)){
							Brief brief=(Brief)o[0];
							if(tv.openDocument(brief)==false){
								SWTHelper.alert("Fehler","Konnte Text nicht laden");
							}
						}else{
							tv.createDocument(null,null);
						}
					}catch(Throwable ex){
						ExHandler.handle(ex);
					}
				}
			
			});
			bLoad.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));

		}
	}
	private void makeActions(){
		briefNeuAction=new Action("Neu..."){
			@Override
			public void run(){
				TextView tv=null;
				try{
					tv=(TextView)getSite().getPage().showView(TextView.ID /*,StringTool.unique("textView"),IWorkbenchPage.VIEW_ACTIVATE */);
					DocumentSelectDialog bs=new DocumentSelectDialog(getViewSite().getShell(),Hub.actMandant,DocumentSelectDialog.TYPE_CREATE_DOC_WITH_TEMPLATE);
					if(bs.open()==Dialog.OK){
						tv.createDocument(bs.getSelectedDocument(),bs.getBetreff());
						tv.setName();
						CTabItem sel=ctab.getSelection();
						if(sel!=null){
							CommonViewer cv=(CommonViewer)sel.getData();
							cv.notify(CommonViewer.Message.update_keeplabels);
						}

					}	
				}catch(Exception ex){
					ExHandler.handle(ex);
				}
			}
		};
		briefLadenAction=new Action("Öffnen"){
			@Override
			public void run(){
				try {
					TextView tv=(TextView)getViewSite().getPage().showView(TextView.ID);
					CTabItem sel=ctab.getSelection();
					if(sel!=null){
						CommonViewer cv=(CommonViewer)sel.getData();
						Object[] o=cv.getSelection();
						if((o!=null) && (o.length>0)){
							Brief brief=(Brief)o[0];
							if(tv.openDocument(brief)==false){
								SWTHelper.alert("Fehler","Konnte Text nicht laden");
							}
						}else{
							tv.createDocument(null,null);
						}
						cv.notify(CommonViewer.Message.update);
					}
				} catch (PartInitException e) {
					ExHandler.handle(e);
				}
	
			}
		};
		deleteAction=new Action("Löschen"){
			@Override
			public void run(){
				CTabItem sel=ctab.getSelection();
				if(sel!=null){
					CommonViewer cv=(CommonViewer)sel.getData();
					Object[] o=cv.getSelection();
					if((o!=null) && (o.length>0)){
						Brief brief=(Brief)o[0];
						brief.delete();
					}
					cv.notify(CommonViewer.Message.update);
				}
			
			}
		};
		editNameAction=new Action("Umbenennen..."){
			@Override
			public void run(){
				CTabItem sel=ctab.getSelection();
				if(sel!=null){
					CommonViewer cv=(CommonViewer)sel.getData();
					Object[] o=cv.getSelection();
					if((o!=null) && (o.length>0)){
						Brief brief=(Brief)o[0];
						InputDialog id=new InputDialog(getViewSite().getShell(),"Neuer Betreff","Geben Sie bitte den neuen Betreff für das Dokument ein",brief.getBetreff(),null);
						if(id.open()==Dialog.OK){
							brief.setBetreff(id.getValue());
						}
					}
					cv.notify(CommonViewer.Message.update);
				}
			}
		};
		/*
		importAction=new Action("Importieren..."){
			public void run(){
				
			}
		};
		*/
        briefLadenAction.setImageDescriptor(Hub.getImageDescriptor("rsc/file.gif"));
        briefLadenAction.setToolTipText("Dokument zum Bearbeiten öffnen");
        briefNeuAction.setImageDescriptor(Hub.getImageDescriptor("rsc/fileneu.gif"));
        briefNeuAction.setToolTipText("Einen neues Dokument erstellen");
        editNameAction.setImageDescriptor(Hub.getImageDescriptor("rsc/rename.gif"));
        editNameAction.setToolTipText("Dokument umbenennen");
        deleteAction.setImageDescriptor(Hub.getImageDescriptor("rsc/filedel.gif"));
        deleteAction.setToolTipText("Dokument löschen");
	}

	public void activation(final boolean mode) {
		// TODO Auto-generated method stub
		
	}

	public void visible(final boolean mode) {
		if(mode==true){
			selectionEvent(GlobalEvents.getInstance().getSelectedObject(Patient.class));
			GlobalEvents.getInstance().addSelectionListener(this);
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
		
	}

	public void clearEvent(final Class template) {
		// TODO Auto-generated method stub
		
	}
	/* ******
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2
	 * Wir benötigen das Interface nur, um das Schliessen einer View zu verhindern,
	 * wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */ 
	public int promptToSaveOnClose() {
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL : ISaveablePart2.NO;
	}
	public void doSave(final IProgressMonitor monitor) { /* leer */ }
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
