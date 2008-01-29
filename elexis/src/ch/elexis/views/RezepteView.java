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
 *  $Id: RezepteView.java 3593 2008-01-29 10:16:30Z rgw_ch $
 *******************************************************************************/


package ch.elexis.views;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
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
import ch.elexis.data.Artikel;
import ch.elexis.data.ICodeElement;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Query;
import ch.elexis.data.Rezept;
import ch.elexis.util.Extensions;
import ch.elexis.util.PersistentObjectDragSource;
import ch.elexis.util.PersistentObjectDropTarget;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;
import ch.elexis.views.codesystems.LeistungenView;
import ch.rgw.tools.ExHandler;

/**
 * Eine View zum Anzeigen von Rezepten. Links wird eine Liste mit allen Rezepten des aktuellen Patienten
 * angezeigt, rechts die Prescriptions des aktuellen Rezepts.
 * @author Gerry
 *
 */
public class RezepteView extends ViewPart implements SelectionListener, ActivationListener, ISaveablePart2 {
	public static final String ID="ch.elexis.Rezepte";
	private final FormToolkit tk=Desk.theToolkit;
	private Form master;
	ListViewer lv;
	Label ausgestellt;
	ListViewer lvRpLines;
	private Action newRpAction,deleteRpAction;
	private Action addLineAction, removeLineAction;
	private ViewMenus menus;
	private Action printAction;
	private Patient actPatient;
	private PersistentObjectDropTarget dropTarget;
	
	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new GridLayout());
		master=tk.createForm(parent);
		master.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		master.getBody().setLayout(new FillLayout());
		SashForm sash=new SashForm(master.getBody(), SWT.NONE);
		lv=new ListViewer(sash,SWT.V_SCROLL);
		lv.setContentProvider(new IStructuredContentProvider(){

			public Object[] getElements(final Object inputElement) {
				Query<Rezept> qbe=new Query<Rezept>(Rezept.class);
				Patient act=(Patient)GlobalEvents.getInstance().getSelectedObject(Patient.class);
				if(act!=null){
					qbe.add("PatientID","=",act.getId());
					qbe.orderBy(true, new String[]{"Datum"});
					List<Rezept> list=qbe.execute();
					return list.toArray();
				}else{
					return new Object[0];
				}
			}

			public void dispose() { /* leer */}
			public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) { /* leer */ }
			
		});
		lv.setLabelProvider(new LabelProvider(){
			@Override
			public String getText(final Object element) {
				if(element instanceof Rezept){
					Rezept rp=(Rezept)element;
					return rp.getLabel();
				}
				return element.toString();
			}
			
		});
		lv.addSelectionChangedListener(GlobalEvents.getInstance().getDefaultListener());
		lvRpLines=new ListViewer(sash);
		makeActions();
		menus=new ViewMenus(getViewSite());
		//menus.createToolbar(newRpAction, addLineAction, printAction );
		menus.createMenu(newRpAction,addLineAction,printAction, deleteRpAction);
		menus.createViewerContextMenu(lvRpLines, removeLineAction);
		IToolBarManager tm=getViewSite().getActionBars().getToolBarManager();
		List<IAction> importers=Extensions.getClasses(Extensions.getExtensions("ch.elexis.RezeptHook"), "RpToolbarAction",false);
		for(IAction ac:importers){
			tm.add(ac);
		}
		if(importers.size()>0){
			tm.add(new Separator());
		}
		tm.add(newRpAction);
		tm.add(addLineAction);
		tm.add(printAction);
		lv.setInput(getViewSite());

		/* Implementation Drag&Drop */
		PersistentObjectDropTarget.Receiver dtr=new PersistentObjectDropTarget.Receiver(){

			public boolean accept(PersistentObject o) {
				// TODO Auto-generated method stub
				return true;
			}

			public void dropped(PersistentObject o, DropTargetEvent ev) {
				Rezept actR=(Rezept)GlobalEvents.getInstance().getSelectedObject(Rezept.class);
				if(actR==null){
					SWTHelper.showError("Kein Rezept ausgewählt", "Bitte wählen Sie zuerst ein Rezept aus, dem dieser Artikel zugefügt werden soll");
					return;
				}
				if(o instanceof Artikel){
					Artikel art=(Artikel)o;

					Prescription p=new Prescription(art,actR.getPatient(),"","");
					p.setBeginDate(null);
					actR.addPrescription(p);
					refresh();
				}else if(o instanceof Prescription){
					Prescription pre=(Prescription)o;
					Prescription now=new Prescription(pre.getArtikel(),actR.getPatient(),pre.getDosis(),pre.getBemerkung());
					now.setBeginDate(null);
					actR.addPrescription(now);
					refresh();
				}


			}};

		
		//final TextTransfer textTransfer = TextTransfer.getInstance();
        //Transfer[] types = new Transfer[] {textTransfer};
        dropTarget=new PersistentObjectDropTarget("Rezept",lvRpLines.getControl(),dtr);
        
		lvRpLines.setContentProvider(new RezeptContentProvider());
		lvRpLines.setLabelProvider(new RezeptLabelProvider());
		lvRpLines.getControl().setToolTipText("Ziehen Sie Medikamente zum Hinzufügen mit der Maus auf diese Fläche");
		/*lvRpLines.addDragSupport(DND.DROP_COPY,types,*/
			new PersistentObjectDragSource(lvRpLines);
		lvRpLines.setInput(getViewSite());
		addLineAction.setEnabled(false);
		printAction.setEnabled(false);
		GlobalEvents.getInstance().addActivationListener(this,this);
	}

	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose(){
		GlobalEvents.getInstance().removeSelectionListener(this);
		GlobalEvents.getInstance().removeActivationListener(this,this);
		lv.removeSelectionChangedListener(GlobalEvents.getInstance().getDefaultListener());
	}
	public void refresh(){
		Rezept rp=(Rezept)GlobalEvents.getInstance().getSelectedObject(Rezept.class);
		if(rp==null){
			/*form.setText("");
			ausgestellt.setText("");
			rpText.setText("",false,false);*/
			lvRpLines.refresh(true);
			addLineAction.setEnabled(false);
			printAction.setEnabled(false);
		}else{
			//form.setText("Rezept vom "+rp.getDate());
			//ausgestellt.setText(rp.getMandant().getLabel());
			lvRpLines.refresh(true);
			addLineAction.setEnabled(true);
			printAction.setEnabled(true);
			master.setText(rp.getPatient().getLabel());
		}
	}
	public void selectionEvent(final PersistentObject obj) {
		if(obj instanceof Patient){
			actPatient=(Patient)obj;
			GlobalEvents.getInstance().clearSelection(Rezept.class);
			addLineAction.setEnabled(false);
			printAction.setEnabled(false);
			lv.refresh(true);
			refresh();
			master.setText(actPatient.getLabel());
		}else if(obj instanceof Rezept){
			actPatient=((Rezept)obj).getPatient();
			refresh();
		}

		//Patient p=(Patient)GlobalEvents.getInstance().getSelectedObject(getViewSite(),Patient.class);
		//master.setText(p.getLabel());
		
	}
	private void makeActions(){
		newRpAction = new Action("Neues Rezept"){
			@Override
			public void run(){
				Patient act=(Patient)GlobalEvents.getInstance().getSelectedObject(Patient.class);
				if(act==null){
					MessageBox mb=new MessageBox(getViewSite().getShell(),SWT.ICON_INFORMATION|SWT.OK);
					mb.setText("Kann kein Rezept erstellen");
					mb.setMessage("Es ist kein Patient selektiert.");
					mb.open();
					return;
				}
				new Rezept(act);
				lv.refresh();
			}
		};
		deleteRpAction=new Action("Rezept löschen"){
			@Override
			public void run(){
				Rezept rp=(Rezept)GlobalEvents.getInstance().getSelectedObject(Rezept.class);
				if(MessageDialog.openConfirm(getViewSite().getShell(), "Rezept löschen", "Wollen Sie wirklich das Rezept vom "+rp.getDate()+" unwiderruflich löschen?")){
					rp.delete();
					lv.refresh();
				}
			}
		};
		removeLineAction=new Action("Zeile löschen"){
				@Override
				public void run(){
					Rezept rp=(Rezept)GlobalEvents.getInstance().getSelectedObject(Rezept.class);
					IStructuredSelection sel=(IStructuredSelection)lvRpLines.getSelection();
					Prescription p=(Prescription)sel.getFirstElement();
					if((rp!=null) && (p!=null)){
						rp.removePrescription(p);
						lvRpLines.refresh();
					}
					/*
					RpZeile z=(RpZeile)sel.getFirstElement();
					if((rp!=null) && (z!=null)){
						rp.removeLine(z);
						lvRpLines.refresh();
					}
					*/
				}
		};
		addLineAction=new Action("Neue Zeile"){
			@Override
			public void run(){
				try {
					LeistungenView lv1=(LeistungenView)getViewSite().getPage().showView(LeistungenView.ID);
					GlobalEvents.getInstance().setCodeSelectorTarget(dropTarget);
					CTabItem[] tabItems=lv1.ctab.getItems();
					for(CTabItem tab:tabItems){
						ICodeElement ics=(ICodeElement)tab.getData();
						if(ics instanceof Artikel){
							lv1.ctab.setSelection(tab);
							break;
						}
					}
				} catch (PartInitException ex) {
					ExHandler.handle(ex);
				}
			}
		};
		printAction=new Action("Drucken"){
			@Override
			public void run(){
				try{
				    RezeptBlatt rp=(RezeptBlatt)getViewSite().getPage().showView(RezeptBlatt.ID);
				    Rezept actR=(Rezept)GlobalEvents.getInstance().getSelectedObject(Rezept.class);
				    rp.createRezept(actR);
				}catch(Exception ex){
					ExHandler.handle(ex);
				}
			}
		};
		addLineAction.setImageDescriptor(Hub.getImageDescriptor("rsc/add.gif"));
		printAction.setImageDescriptor(Desk.theImageRegistry.getDescriptor("print"));
		newRpAction.setImageDescriptor(Hub.getImageDescriptor("rsc/rpneu.ico"));
	}
	public void activation(final boolean mode) {
		// TODO Auto-generated method stub
		
	}

	public void visible(final boolean mode) {
		if(mode==true){
			GlobalEvents.getInstance().addSelectionListener(this);
			Rezept actRezept=(Rezept)GlobalEvents.getInstance().getSelectedObject(Rezept.class);
			Patient global=(Patient)GlobalEvents.getInstance().getSelectedObject(Patient.class);
			if((actRezept==null) || (!actRezept.getPatient().getId().equals(global.getId()))){
				selectionEvent(global);
			}else{
				selectionEvent(actRezept);
			}
			addLineAction.setEnabled(actRezept!=null);
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
	}

	class RezeptContentProvider implements IStructuredContentProvider{

		public Object[] getElements(final Object inputElement) {
			Rezept rp=(Rezept)GlobalEvents.getInstance().getSelectedObject(Rezept.class);
			if(rp==null){
				return new Prescription[0];
			}
			List<Prescription> list=rp.getLines();
			return list.toArray();
		}
		public void dispose() { /* leer */}
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) { /*leer*/}
	}

	class RezeptLabelProvider extends LabelProvider{

		@Override
		public String getText(final Object element) {
			if(element instanceof Prescription){
				Prescription z=(Prescription)element;
				return z.getLabel();
			}
			return "?";
		}
		
	}
	public void clearEvent(final Class<? extends PersistentObject> template) {
		lvRpLines.refresh();
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

