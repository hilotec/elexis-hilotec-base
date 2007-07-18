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
 *  $Id: BlockDetailDisplay.java 2839 2007-07-18 17:44:17Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views.codesystems;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Eigenleistung;
import ch.elexis.data.ICodeElement;
import ch.elexis.data.IVerrechenbar;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.Mandant;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;
import ch.elexis.views.IDetailDisplay;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class BlockDetailDisplay implements IDetailDisplay {
	ScrolledForm form;
	FormToolkit tk;
	Text tName;
	Combo cbMandant;
	ListViewer lLst;
	Button bNew,bEigen;
	List<Mandant> lMandanten;
	private static Log log=Log.get("BlockDetail");
	private Action removeLeistung,moveUpAction,moveDownAction,editAction;
	IViewSite site;
	
	public Composite createDisplay(final Composite parent, final IViewSite site) {
		tk=Desk.theToolkit;
		this.site=site;
		form=tk.createScrolledForm(parent);
		Composite body=form.getBody();
		body.setLayout(new GridLayout(2,false));
		tk.createLabel(body,"Name");
		tName=tk.createText(body,"",SWT.BORDER);
		tName.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		tk.createLabel(body,"Mandant");
		cbMandant=new Combo(body,SWT.NONE);
		cbMandant.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		tk.adapt(cbMandant);
		Query<Mandant> qm=new Query<Mandant>(Mandant.class);
		lMandanten=qm.execute();
		cbMandant.add("Alle");
		for(PersistentObject m:lMandanten){
			cbMandant.add(m.getLabel());
		}
		cbMandant.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(final SelectionEvent e) {
				int idx=cbMandant.getSelectionIndex();
				Leistungsblock lb=(Leistungsblock) GlobalEvents.getInstance().getSelectedObject(Leistungsblock.class);
				if(idx>0){
					PersistentObject m=lMandanten.get(idx-1);
					lb.set("MandantID",m.getId());
				}else{
					lb.set("MandantID","");
				}
				
				
			}
			
		});
		Group gList=new Group(body,SWT.BORDER);
		gList.setText("Leistungen");
		gList.setLayoutData(SWTHelper.getFillGridData(2,true,1,true));
		gList.setLayout(new FillLayout());
		tk.adapt(gList);
		lLst=new ListViewer(gList,SWT.NONE);
		tk.adapt(lLst.getControl(),true,true);
		
		lLst.setContentProvider(new IStructuredContentProvider(){
			public void dispose() {}
			public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput){	}
			public Object[] getElements(final Object inputElement) {
				Leistungsblock lb=(Leistungsblock) GlobalEvents.getInstance().getSelectedObject(Leistungsblock.class);
				if(lb==null){
					return new Object[0];
				}
				List lst=lb.getElements();
				if(lst==null){
					return new Object[0];
				}
				return lst.toArray();
			}
			
		});
		lLst.setLabelProvider(new LabelProvider(){

			@Override
			public String getText(final Object element) {
				ICodeElement v=(ICodeElement)element;
				return v.getCode()+" "+v.getText();
			}
			
		});
        final TextTransfer textTransfer = TextTransfer.getInstance();
        Transfer[] types = new Transfer[] {textTransfer};
		lLst.addDropSupport(DND.DROP_COPY,types,new DropTargetListener(){
			public void dragEnter(final DropTargetEvent event) {
                event.detail=DND.DROP_COPY;
			}

			public void dragLeave(final DropTargetEvent event) {
			}

			public void dragOperationChanged(final DropTargetEvent event) {
			}

			public void dragOver(final DropTargetEvent event) {
			}

			public void drop(final DropTargetEvent event) {
			    String drp=(String)event.data;
                String[] dl=drp.split(",");
                for(String obj:dl){
                    PersistentObject dropped=Hub.poFactory.createFromString(obj);
                    if(dropped instanceof ICodeElement){
                    	Leistungsblock lb=(Leistungsblock) GlobalEvents.getInstance().getSelectedObject(Leistungsblock.class);
                    	if(lb!=null){
                    		lb.addElement((ICodeElement)dropped);
                    		lLst.refresh();
                    		GlobalEvents.getInstance().fireUpdateEvent(Leistungsblock.class);
                    	}
                    }
                }
            				
			}

			public void dropAccept(final DropTargetEvent event) {
				// TODO Automatisch erstellter Methoden-Stub
				
			}
			
		});
		bNew=tk.createButton(body,"Vordefinierte Leistungen hinzufügen",SWT.PUSH);
		bNew.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		bNew.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(final SelectionEvent e) {
				try{
					site.getPage().showView(LeistungenView.ID);
				}catch(Exception ex){
					ExHandler.handle(ex);
					log.log("Fehler beim Starten des Leistungscodes "+ex.getMessage(),Log.ERRORS );
				}

			}
			
		});
		
		bEigen=tk.createButton(body,"Eigene Leistungen hinzufügen",SWT.PUSH);
		bEigen.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		bEigen.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(final SelectionEvent e) {
				EigenLeistungDlg dlg=new EigenLeistungDlg(site.getShell(),null);
				if(dlg.open()==Dialog.OK){
                	Leistungsblock lb=(Leistungsblock) GlobalEvents.getInstance().getSelectedObject(Leistungsblock.class);
                	if(lb!=null){
                		lb.addElement(dlg.result);
                		lLst.refresh();
                	}
				}
			}
		});
		makeActions();
		ViewMenus menus=new ViewMenus(site);
		menus.createControlContextMenu(lLst.getControl(), new ViewMenus.IMenuPopulator(){
			public IAction[] fillMenu() {
				IVerrechenbar iv=(IVerrechenbar)((IStructuredSelection)lLst.getSelection()).getFirstElement();
				if(iv instanceof Eigenleistung){
					return new IAction[]{moveUpAction,moveDownAction,null,removeLeistung,editAction};	
				}else{
					return new IAction[]{moveUpAction,moveDownAction,null,removeLeistung};
				}
				
			}});
		//menus.createViewerContextMenu(lLst,moveUpAction,moveDownAction,null,removeLeistung,editAction);
		lLst.setInput(site);
		return body;
	}

	public Class getElementClass() {
		return Leistungsblock.class;
	}

	public void display(final Object obj) {
		if(obj==null){
			bNew.setEnabled(false);
			tName.setText("");
			cbMandant.select(0);
		}else{
			Leistungsblock lb=(Leistungsblock)obj;
			tName.setText(lb.get("Name"));
			String mId=lb.get("MandantID");
			int sel=0;
			if(!StringTool.isNothing(mId)){
				String[] items=cbMandant.getItems();
				sel=StringTool.getIndex(items,Mandant.load(mId).getLabel());
			}
			cbMandant.select(sel);
			bNew.setEnabled(true);
		}
		lLst.refresh(true);
	}

	public String getTitle() {
		return "Blöcke";
	}
	class EigenLeistungDlg extends TitleAreaDialog{
		Text tName,tKurz,tEK,tVK,tTime;
		//Eigenleistung result;
		IVerrechenbar result;
		EigenLeistungDlg(final Shell shell, final IVerrechenbar lstg){
			super(shell);
			result=lstg;
		}
		@Override
		public void create(){
			super.create();
			if(result instanceof Eigenleistung){
				setTitle("Eigenleistung anpassen");
				setMessage("Bitte geben Sie Name, Kürzel und Daten für diese Eigenleistung ein");
			}else if(result==null){
				setTitle("Eigenleistung definieren");
				setMessage("Bitte geben Sie Name, Kürzel und Daten für die gewünschte Eigenleistung ein");
			}
			getShell().setText("Eigenleistung");
		}
		@Override
		protected Control createDialogArea(final Composite parent) {
			Composite ret=new Composite(parent,SWT.NONE);
			ret.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
			ret.setLayout(new GridLayout(2,false));
			new Label(ret,SWT.NONE).setText("Name");
			tName=new Text(ret,SWT.BORDER);
			tName.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			new Label(ret,SWT.NONE).setText("Kürzel");
			tKurz=new Text(ret,SWT.BORDER);
			tKurz.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			new Label(ret,SWT.NONE).setText("Kosten (Rp.)");
			tEK=new Text(ret,SWT.BORDER);
			tEK.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			new Label(ret,SWT.NONE).setText("Preis (Rp.)");
			tVK=new Text(ret,SWT.BORDER);
			tVK.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			new Label(ret,SWT.NONE).setText("Zeitbedarf");
			tTime=new Text(ret,SWT.BORDER);
			tTime.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			if(result instanceof Eigenleistung){
				Eigenleistung el=(Eigenleistung)result;
				tName.setText(el.get("Bezeichnung"));
				tKurz.setText(el.get("Code"));
				tEK.setText(el.getKosten(new TimeTool()).getCentsAsString());
				tVK.setText(el.getPreis(new TimeTool(), null).getCentsAsString());
			}
			return ret;
		}
		@Override
		protected void okPressed() {
			if(result==null){
				result=new Eigenleistung(tKurz.getText(),tName.getText(),tEK.getText(),tVK.getText());
			}else if(result instanceof Eigenleistung){
				((Eigenleistung)result).set(new String[]{"Code","Bezeichnung","EK_Preis","VK_Preis"},
						new String[]{tKurz.getText(),tName.getText(),tEK.getText(),tVK.getText()});
			}
			super.okPressed();
		}
		
		
	};
	private void makeActions(){
		removeLeistung=new Action("Entfernen"){
			@Override
			public void run(){
				Leistungsblock lb=(Leistungsblock) GlobalEvents.getInstance().getSelectedObject(Leistungsblock.class);
            	if(lb!=null){
            		IStructuredSelection sel=(IStructuredSelection) lLst.getSelection();
            		Object o=sel.getFirstElement();
            		if(o!=null){
                		lb.removeElement((ICodeElement)o);
                		lLst.refresh();
            		}
            	}				
			}
		};
		moveUpAction=new Action("nach oben"){
			@Override
			public void run(){
				moveElement(-1);
			}
		};
		moveDownAction=new Action("nach unten"){
			@Override
			public void run(){
				moveElement(1);
			}
		};
		editAction=new Action("Ändern..."){
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_EDIT));
				setToolTipText("Dieses Element anpassen");
			}
			@Override
			public void run(){
				IVerrechenbar iv=(IVerrechenbar)((IStructuredSelection)lLst.getSelection()).getFirstElement();
				EigenLeistungDlg eld=new EigenLeistungDlg(site.getShell(),iv);
				eld.open();
			}
		};
	}
	private void moveElement(final int off){
		Leistungsblock lb=(Leistungsblock) GlobalEvents.getInstance().getSelectedObject(Leistungsblock.class);
    	if(lb!=null){
    		IStructuredSelection sel=(IStructuredSelection) lLst.getSelection();
    		Object o=sel.getFirstElement();
    		if(o!=null){
        		lb.moveElement((ICodeElement)o,off);
        		lLst.refresh();
    		}
    	}
		
	}
}
