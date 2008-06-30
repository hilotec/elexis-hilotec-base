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
 *  $Id: CodeSelectorFactory.java 4089 2008-06-30 14:21:21Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views.codesystems;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IViewSite;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Anwender;
import ch.elexis.data.ICodeElement;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.PersistentObjectFactory;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.Extensions;
import ch.elexis.util.Log;
import ch.elexis.util.PersistentObjectDragSource2;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewerConfigurer;
import ch.elexis.util.CommonViewer.DoubleClickListener;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * Bereitstellung der Auswahlluste für Codes aller Art: Oben häufigste des Anwenders, in der Mitte
 * häufigste des Patienten, unten ganze Systenatik
 * @author Gerry
 *
 */
public abstract class CodeSelectorFactory implements IExecutableExtension{
	/** Anzahl der in den oberen zwei Listen zu haltenden Elemente */
	public static int ITEMS_TO_SHOW_IN_MFU_LIST=15;
	
	public CodeSelectorFactory(){}

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		
	}
	
	public abstract ViewerConfigurer createViewerConfigurer(CommonViewer cv);
	public abstract Class<? extends PersistentObject> getElementClass();
	public abstract void  dispose();
	public abstract String getCodeSystemName();
	public String getCodeSystemCode(){
		return "999";
	}
	
	
	public static void makeTabs(CTabFolder ctab, IViewSite site, String point){
		ITEMS_TO_SHOW_IN_MFU_LIST=Hub.userCfg.get(PreferenceConstants.USR_MFU_LIST_SIZE, 15);
		java.util.List<IConfigurationElement> list=Extensions.getExtensions(point);
		ctab.setSimple(false);

		if(list!=null){
			for(IConfigurationElement ic:list){
				try {
					PersistentObjectFactory po=(PersistentObjectFactory)ic.createExecutableExtension("ElementFactory");
					CodeSelectorFactory cs=(CodeSelectorFactory)ic.createExecutableExtension("CodeSelectorFactory");
					if(cs==null){
						SWTHelper.alert("Fehler", "CodeSelectorFactory is null");
					}
					ICodeElement ics=(ICodeElement)po.createTemplate(cs.getElementClass());
					if(ics==null){
						SWTHelper.alert("Fehler", "CodeElement is null");
					}
					String cname=ics.getCodeSystemName();
					if(StringTool.isNothing(cname)){
						SWTHelper.alert("Fehler", "codesystemname");
						cname="??";
					}
					CTabItem ct=new CTabItem(ctab,SWT.NONE);
									
					ct.setText(cname);
					ct.setData(ics);
					cPage page=new cPage(ctab, site,ics,cs);	
					ct.setControl(page);
					
				} catch (CoreException ex) {
					ExHandler.handle(ex);
				}
			}
			if(ctab.getItemCount()>0){
				ctab.setSelection(0);
			}
		}
	}

	private static class ResizeListener extends ControlAdapter{
		private String k;
		private SashForm mine;
		
		ResizeListener(SashForm form, String key){
			k=key;
			mine=form;
		}
		
		@Override
		public void controlResized(ControlEvent e) {
			int[] weights=mine.getWeights();
			StringBuilder v=new StringBuilder();
			v.append(Integer.toString(weights[0])).append(",")
				.append(Integer.toString(weights[1])).append(",")
				.append(Integer.toString(weights[2]));
			Hub.localCfg.set(k, v.toString());
			
		}
		
	}

	public static class cPage extends Composite implements GlobalEvents.SelectionListener{
		IViewSite site;
		ICodeElement template;
		java.util.List<String> lUser,lPatient;
		ArrayList<PersistentObject> alPatient;
		ArrayList<PersistentObject> alUser;
		List lbPatient, lbUser;
		CommonViewer cv;
		ViewerConfigurer vc;
		int[] sashWeights=null;
		ResizeListener resizeListener;
		
		protected cPage(CTabFolder ctab){
			super(ctab,SWT.NONE);
		}
		cPage(final CTabFolder ctab, final IViewSite s, final ICodeElement v, final CodeSelectorFactory cs){
			super(ctab,SWT.NONE);
			template=v;
			site=s;
			setLayout(new FillLayout());
			SashForm sash=new SashForm(this,SWT.VERTICAL|SWT.SMOOTH);
			String cfgKey="ansicht/codesystem/"+v.getCodeSystemName();
			resizeListener=new ResizeListener(sash,cfgKey);
			String sashW=Hub.localCfg.get(cfgKey, "20,20,60");
			sashWeights=new int[3];
			int i=0;
			for(String sw:sashW.split(",")){
				sashWeights[i++]=Integer.parseInt(sw);
			}
			Group gUser=new Group(sash,SWT.NONE);
			gUser.addControlListener(resizeListener);
			gUser.setText("Ihre häufigsten");
			gUser.setLayout(new FillLayout());
			gUser.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
			lbUser=new List(gUser,SWT.MULTI|SWT.V_SCROLL);

			Group gPatient=new Group(sash,SWT.NONE);
			gPatient.addControlListener(resizeListener);
			gPatient.setText("Häufigste des Patienten");
			gPatient.setLayout(new FillLayout());
			gPatient.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
			lbPatient=new List(gPatient,SWT.MULTI|SWT.V_SCROLL);

			Group gAll=new Group(sash,SWT.NONE);
			gAll.setText("Alle");
			gAll.setLayout(new GridLayout());
			cv=new CommonViewer();
			Iterable<IAction> actions=v.getActions();
			if(actions!=null){
				MenuManager menu=new MenuManager();
				menu.setRemoveAllWhenShown(true);
				menu.addMenuListener(new IMenuListener(){
					public void menuAboutToShow(IMenuManager manager) {
						Iterable<IAction> actions=v.getActions();
						for(IAction ac:actions){
							manager.add(ac);
						}
						
					}});
				cv.setContextMenu(menu);
			}
			vc=cs.createViewerConfigurer(cv);
			Composite cvc=new Composite(gAll,SWT.NONE);
			cvc.setLayout(new GridLayout());
			cvc.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
			cv.create(vc,cvc,SWT.NONE,this);
			cv.getViewerWidget().getControl().setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
			vc.getContentProvider().startListening();
			
			// add double click listener for CodeSelectorTarget
			cv.addDoubleClickListener(new DoubleClickListener() {
				public void doubleClicked(PersistentObject obj, CommonViewer cv) {
					ICodeSelectorTarget target = GlobalEvents.getInstance().getCodeSelectorTarget();
					if (target != null) {
							target.codeSelected(obj);
					}
				}
			});
			
			doubleClickEnable(lbUser);
			doubleClickEnable(lbPatient);
			
			//dragEnable(lbUser);
			//dragEnable(lbPatient);
			new PersistentObjectDragSource2(lbUser,new DragEnabler(lbUser));
			new PersistentObjectDragSource2(lbPatient,new DragEnabler(lbPatient));
			
			try{
				sash.setWeights(sashWeights);
			}catch(Throwable t){
				ExHandler.handle(t);
				sash.setWeights(new int[]{20,20,60});
			}
			refresh();
		}
		public void selectionEvent(PersistentObject obj) {
			if(obj instanceof Patient){
				refresh();
			}
			if(obj instanceof Anwender){
				lbPatient.setFont(Desk.getFont(PreferenceConstants.USR_DEFAULTFONT));
				lbUser.setFont(Desk.getFont(PreferenceConstants.USR_DEFAULTFONT));
				cv.getViewerWidget().getControl().setFont(Desk.getFont(PreferenceConstants.USR_DEFAULTFONT));
				refresh();
			}
		}
	
	
		/* (Kein Javadoc)
		 * @see org.eclipse.swt.widgets.Widget#dispose()
		 */
		@Override
		public void dispose() {
			vc.getContentProvider().stopListening();
			super.dispose();
		}
		public void refresh(){
			lbUser.removeAll();
			if(Hub.actUser==null){
				//Hub.log.log("ActUser ist null!", Log.ERRORS);
				return;
			}
			if(template==null){
				Hub.log.log("Template ist null!", Log.ERRORS);
				return;
			}
			lUser=Hub.actUser.getStatForItem(template.getClass().getName());
			alUser=new ArrayList<PersistentObject>();
			lbUser.setData(alUser);
			for(int i=0;i<ITEMS_TO_SHOW_IN_MFU_LIST;i++){
				if(i>=lUser.size()){
					break;
				}
				PersistentObject po=Hub.poFactory.createFromString(lUser.get(i));
				alUser.add(po);
				String lbl=po.getLabel();
				if(StringTool.isNothing(lbl)){
					lbl="?";
					continue;
				}
				lbUser.add(lbl);
			}
			lbPatient.removeAll();
	
			Patient act=GlobalEvents.getSelectedPatient();
			if(act!=null){
				lPatient=act.getStatForItem(template.getClass().getName());
			}else{
				lPatient=new java.util.ArrayList<String>();
			}
			alPatient=new ArrayList<PersistentObject>();
			lbPatient.setData(alPatient);
			for(int i=0;i<ITEMS_TO_SHOW_IN_MFU_LIST;i++){
				if(i>=lPatient.size()){
					break;
				}
				PersistentObject po=Hub.poFactory.createFromString(lPatient.get(i));
				if(po!=null){
					alPatient.add(po);
					String label=po.getLabel();
					if(label==null){
						lbPatient.add("?");
					}else{
						lbPatient.add(label);
					}
				}
			}
			
		}
		public void clearEvent(Class<? extends PersistentObject> template) {
			// TODO Auto-generated method stub
			
		}

	}
	static class DragEnabler implements PersistentObjectDragSource2.Draggable{
		List list;
		DragEnabler(final List list){
			this.list=list;
		}
		public java.util.List<PersistentObject> getSelection() {
			int sel=list.getSelectionIndex();
			 ArrayList<PersistentObject> backing=(ArrayList<PersistentObject>)list.getData();
			 PersistentObject po=backing.get(sel);
			 ArrayList<PersistentObject> ret=new ArrayList<PersistentObject>();
			 ret.add(po);
			 return ret;
		}
		
	}
	/*
	static DragSource dragEnable(final List list) {
	DragSource src;
	src=new DragSource(list, DND.DROP_COPY);
	src.setTransfer(new Transfer[]{TextTransfer.getInstance()});
	src.addDragListener(new DragSourceListener(){

		public void dragStart(DragSourceEvent event) {
			int sel=list.getSelectionIndex();
			if(sel!=-1){
				event.doit=true;
				list.setData("sel", sel);		// Workaraound für MacOS X
			}
			
		}

		@SuppressWarnings("unchecked")
		public void dragSetData(DragSourceEvent event) {
			 if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
				 //int sel=list.getSelectionIndex();		// Mac OS-X hat zu diesem Zeitpunkt die Selection verloren ?!?
				 Integer sel=(Integer)list.getData("sel");
				 if(sel==null){
					 event.data=null;
				 	return;
				 }
				 ArrayList<PersistentObject> backing=(ArrayList<PersistentObject>)list.getData();
				 PersistentObject po=backing.get(sel);
				 event.data = po.storeToString();
			 }
			
		}

		public void dragFinished(DragSourceEvent event) {
			
			
		}
		
	});
	
	return src;
	}
	*/
	// add double click listener for ICodeSelectorTarget
	static void doubleClickEnable(final List list) {
		list.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				// normal selection, do nothing
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// double clicked

				int sel = list.getSelectionIndex();
				if (sel != -1) {
					ArrayList<PersistentObject> backing = (ArrayList<PersistentObject>)list.getData();
					PersistentObject po = backing.get(sel);

					ICodeSelectorTarget target = GlobalEvents.getInstance().getCodeSelectorTarget();
					if (target != null) {
							target.codeSelected(po);
					}

				}
			}
		});
	}
}
