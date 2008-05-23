/*******************************************************************************
 * Copyright (c) 2005-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: BestellView.java 3959 2008-05-23 12:17:33Z danlutz $
 *******************************************************************************/

package ch.elexis.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalActions;
import ch.elexis.data.Artikel;
import ch.elexis.data.Bestellung;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Bestellung.Item;
import ch.elexis.dialogs.OrderImportDialog;
import ch.elexis.exchange.IDataSender;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.DefaultLabelProvider;
import ch.elexis.util.Extensions;
import ch.elexis.util.LabeledInputField;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;
import ch.rgw.tools.ExHandler;

public class BestellView extends ViewPart implements ISaveablePart2{
	public static final String ID="ch.elexis.BestellenView";
	Form form;
	FormToolkit tk=Desk.theToolkit;
	//LabeledInputField.AutoForm tblArtikel;
	TableViewer tv;
	Bestellung actBestellung;
	ViewMenus viewmenus;
	private IAction removeAction, wizardAction, countAction, loadAction,saveAction, printAction, sendAction;
	private IAction exportClipboardAction, checkInAction;
	
	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new FillLayout());
		form=tk.createForm(parent);
		Composite body=form.getBody();
		body.setLayout(new GridLayout());
		Table table=new Table(body,SWT.V_SCROLL|SWT.FULL_SELECTION|SWT.SINGLE);
		TableColumn tc0=new TableColumn(table,SWT.CENTER);
		tc0.setText("Zahl");
		tc0.setWidth(40);
		TableColumn tc1=new TableColumn(table,SWT.LEFT);
		tc1.setText("Artikel");
		tc1.setWidth(280);
		TableColumn tc2=new TableColumn(table,SWT.LEFT);
		tc2.setText("Lieferant");
		tc2.setWidth(250);
		table.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tv=new TableViewer(table);
		tv.setContentProvider(new IStructuredContentProvider(){
			public Object[] getElements(final Object inputElement) {
				if(actBestellung!=null){
					return actBestellung.asList().toArray();
				}
				return new Object[0];
			}

			public void dispose() {	}
			public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {			}
			
		});
		tv.setLabelProvider(new BestellungLabelProvider());
		tv.setSorter(new ViewerSorter(){
			@Override
			public int compare(final Viewer viewer, final Object e1, final Object e2) {
				String s1=((Item) e1).art.getName();
				String s2=((Item) e2).art.getName();
				return s1.compareTo(s2);
			}
			
		});
		Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
		tv.addDropSupport(DND.DROP_COPY,types,new DropTargetAdapter(){
			
			@Override
			public void dragEnter(final DropTargetEvent event) {
				 event.detail=DND.DROP_COPY;
			}

			@Override
			public void drop(final DropTargetEvent event) {
			   String drp=(String)event.data;
                String[] dl=drp.split(",");
                if(actBestellung==null){
                	InputDialog dlg=new InputDialog(getViewSite().getShell(),"Neue Bestellung anlegen","Geben Sie bitte einen Titel für die neue Bestellung an","",null);
                	if(dlg.open()==Dialog.OK){
                		setBestellung(new Bestellung(dlg.getValue(),Hub.actUser));
                	}else{
                		return;
                	}
                }
                for(String obj:dl){
                    PersistentObject dropped=Hub.poFactory.createFromString(obj);
                    if(dropped instanceof Artikel){
                    	actBestellung.addItem((Artikel)dropped,1);
                    }
                }
                tv.refresh();
			}
			
		});
		makeActions();
		viewmenus=new ViewMenus(getViewSite());
		viewmenus.createToolbar(wizardAction,saveAction,loadAction,printAction,sendAction);
		viewmenus.createMenu(wizardAction,saveAction,loadAction,printAction,sendAction, exportClipboardAction);
		viewmenus.createViewerContextMenu(tv,new IAction[]{removeAction,countAction});
		form.getToolBarManager().add(checkInAction);
		form.updateToolBar();
		setBestellung(null);
		tv.setInput(getViewSite());
	}

	private void setBestellung(final Bestellung b){
		actBestellung=b;
		if(b!=null){
			form.setText(b.getLabel());
			tv.refresh();
			saveAction.setEnabled(true);
			checkInAction.setEnabled(true);
		}else{
			saveAction.setEnabled(false);
			checkInAction.setEnabled(false);
		}
	}
	@Override
	public void dispose(){
		/*
		GlobalEvents.getInstance().removeSelectionListener(this);
		cv.getConfigurer().getContentProvider().stopListening();
		*/
		super.dispose();
	}
	@Override
	public void setFocus() {
	
	}
	class BestellungLabelProvider extends LabelProvider implements ITableLabelProvider{

		public Image getColumnImage(final Object element, final int columnIndex) {
			return null;
		}

		public String getColumnText(final Object element, final int columnIndex) {
			if(element instanceof Bestellung.Item){
				Item it=(Item)element;
				switch (columnIndex) {
				case 0:	return Integer.toString(it.num);
				case 1:	return it.art.getLabel();
				case 2: Kontakt k=it.art.getLieferant();
					return k.exists() ? k.getLabel()  : "unbekannt";
				default: return "?";
				}					
			}
			return "??";
		}
		
	}
	private void makeActions(){
		removeAction=new Action("Artikel entfernen"){
			@Override
			public void run(){
				IStructuredSelection sel=(IStructuredSelection)tv.getSelection();
				if((sel!=null) && (!sel.isEmpty())){
					if(actBestellung!=null){
						actBestellung.removeItem((Item)sel.getFirstElement());
					}
					tv.refresh();
				}
			}
		};
		wizardAction=new Action("Bestellung automatisch"){
			{	setToolTipText("Bestellung automatisch anhand des Lagebestandes erstellen");
				setImageDescriptor(Hub.getImageDescriptor("rsc/wizard.ico"));
			}
			@Override
			public void run(){
				if(actBestellung==null){
					setBestellung(new Bestellung("Automatisch",Hub.actUser));
				}
				/*
				Query<Artikel> qbe=new Query<Artikel>(Artikel.class);
				qbe.add("Minbestand","<>","0");
				List<Artikel> l=qbe.execute();
				*/

				int trigger = Hub.globalCfg.get(PreferenceConstants.INVENTORY_ORDER_TRIGGER, PreferenceConstants.INVENTORY_ORDER_TRIGGER_DEFAULT);

				List<Artikel> l = Artikel.getLagerartikel();
				for(Artikel a:l){
					if((a==null) || (!a.exists())){
						continue;
					}
				String name=a.getLabel();
					int ist=a.getIstbestand();
					int min=a.getMinbestand();
					int max=a.getMaxbestand();
					
					boolean order = false;
					switch (trigger) {
					case PreferenceConstants.INVENTORY_ORDER_TRIGGER_BELOW:
						order = (ist < min);
						break;
					case PreferenceConstants.INVENTORY_ORDER_TRIGGER_EQUAL:
						order = (ist <= min);
						break;
					default:
						order = (ist < min);
					}
					if (order) {
						int toOrder=max-ist;
						if(toOrder>0){
							actBestellung.addItem(a,toOrder);
						}
					}
				}
				tv.refresh(true);
			}
		};
		countAction=new Action("Zahl ändern"){
			@Override
			public void run(){
				IStructuredSelection sel=(IStructuredSelection)tv.getSelection();
				if((sel!=null) && (!sel.isEmpty())){
					Item it=(Item)sel.getFirstElement();
					int old=it.num;
					InputDialog in=new InputDialog(getViewSite().getShell(),"Zahl ändern","Geben Sie bitte an, wieviele Sie bestellen wollen",Integer.toString(old),null);
					if(in.open()==Dialog.OK){
						it.num=Integer.parseInt(in.getValue());
						tv.refresh(it,true);
					}
				}
			}
		};
		saveAction=new Action("Liste speichern"){
			@Override
			public void run(){
				if(actBestellung!=null){
					actBestellung.save();
				}
			}
		};
		printAction=new Action("Bestellung drucken"){
			@Override
			public void run(){
				if(actBestellung!=null){
					actBestellung.save();
					List<Item> list=actBestellung.asList();
					ArrayList<Item> best=new ArrayList<Item>();
					Kontakt adressat=null;
					Iterator iter=list.iterator();
					while(iter.hasNext()){
						Item it=(Item)iter.next();
						if(adressat==null){
							adressat=it.art.getLieferant();
							if(!adressat.exists()){
								adressat=null;
								continue;
							}
						}
						if(it.art.getLieferant().getId().equals(adressat.getId())){
							best.add(it);
							iter.remove();
						}
					}
					
					try {
						BestellBlatt bb=(BestellBlatt)getViewSite().getPage().showView(BestellBlatt.ID);
						bb.createOrder(adressat,best);
						tv.refresh();
					} catch (PartInitException e) {
						ExHandler.handle(e);

					}
				}
			}
		};
		sendAction=new Action("Bestellung senden"){
			@Override
			public void run(){
				actBestellung.save();
				List<IConfigurationElement> list=Extensions.getExtensions("ch.elexis.Transporter");
				for(IConfigurationElement ic:list){
					String handler=ic.getAttribute("AcceptableTypes");
					if(handler.contains("ch.elexis.data.Bestellung")){
						try {
							IDataSender sender=(IDataSender) ic.createExecutableExtension("ExporterClass");
							if(sender.store(actBestellung).isOK()){
								if(sender.finalizeExport()){
									SWTHelper.showInfo("Bestellung durchgeführt", "Der Bestellvorgang ist abgeschlossen.");
									tv.refresh();
								}
							}else{
								SWTHelper.showError("Bestellung nicht möglich", "Für keinen der bestellten Artikel konnte ein passendes Direktbestell-Plugin gefunden werden.");
							}
						} catch (CoreException ex) {
							ExHandler.handle(ex);
						}
					}
				}
			}
		};
		loadAction=new Action("Bestellung öffnen"){
			@Override
			public void run(){
				
				ListDialog dlg=new ListDialog(getViewSite().getShell());
				dlg.setContentProvider(new BestellContentProvider());
				dlg.setLabelProvider(new DefaultLabelProvider());
				dlg.setMessage("Bitte Bestellung auswählen");
				dlg.setTitle("Bestellung einlesen");
				dlg.setInput(this);
				if(dlg.open()==Dialog.OK){
					Bestellung res=(Bestellung)dlg.getResult()[0];
					setBestellung(res);
				}
			}
		};
		printAction.setImageDescriptor(Desk.theImageRegistry.getDescriptor("print"));
		printAction.setToolTipText("Bestellung drucken");
		
		saveAction.setImageDescriptor(Hub.getImageDescriptor("rsc/save.gif"));
		saveAction.setToolTipText("Bestellung speichern");
		sendAction.setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_NETWORK));
		sendAction.setToolTipText("Bestellung automatisch übermitteln");
		loadAction.setImageDescriptor(Hub.getImageDescriptor("rsc/open.gif"));
		loadAction.setToolTipText("Eine früher gespeicherte Bestellung öffnen");
		
		exportClipboardAction = new Action("Bestellung in Zwischenablage exportieren") {
			{
				setToolTipText("Bestellung in Zwischenablage exportieren für Galexis");
			}
			
			@Override
			public void run(){
				if (actBestellung != null) {
					List<Item> list = actBestellung.asList();
					ArrayList<Item> best = new ArrayList<Item>();
					Kontakt adressat = null;
					Iterator iter = list.iterator();
					while(iter.hasNext()){
						Item it=(Item)iter.next();
						if(adressat==null){
							adressat=it.art.getLieferant();
							if(!adressat.exists()){
								adressat=null;
								continue;
							}
						}
						if(it.art.getLieferant().getId().equals(adressat.getId())){
							best.add(it);
							iter.remove();
						}
					}
					
					StringBuffer export = new StringBuffer();
					for (Item item : best) {
						String pharmaCode = item.art.get("SubID");
						int num = item.num;
						String name = item.art.getName();
						String line = pharmaCode + ", " + num + ", " + name;
						
						export.append(line);
						export.append(System.getProperty("line.separator"));
					}
					
					String clipboardText = export.toString();
					Clipboard clipboard = new Clipboard(Desk.theDisplay);
					TextTransfer textTransfer = TextTransfer.getInstance();
					Transfer[] transfers = new Transfer[] {textTransfer};
					Object[] data = new Object[] {clipboardText};
					clipboard.setContents(data, transfers);
					clipboard.dispose();
				}
			}
		};
		checkInAction=new Action("Einbuchen"){
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_TICK));
				setToolTipText("Artikel dieser Bestellung im Lager einbuchen");
			}
			@Override
			public void run(){
				if (actBestellung != null && actBestellung.exists()) {
					OrderImportDialog dialog = new OrderImportDialog(getSite().getShell(), actBestellung);
					dialog.open();
				} else {
					SWTHelper.alert("Keine Bestellung", "Es ist keine Bestellung geladen.");
				}
			}
			
		};
	}
	class BestellContentProvider implements IStructuredContentProvider{

		public Object[] getElements(final Object inputElement) {
			Query<Bestellung> qbe=new Query<Bestellung>(Bestellung.class);
			return qbe.execute().toArray();
			
		}
		public void dispose() {		}
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {}
		
		
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
