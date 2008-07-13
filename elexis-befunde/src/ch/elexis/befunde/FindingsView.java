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
 *    $Id: FindingsView.java 4134 2008-07-13 19:13:37Z rgw_ch $
 *******************************************************************************/
package ch.elexis.befunde;

import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * This is a replacement for "MesswerteView" wich is more flexible in displayable elements.
 * It can show arbitrary textual or numerical findings
 * @author gerry
 *
 */
public class FindingsView extends ViewPart implements ActivationListener,
		SelectionListener {

	public static final String ID="elexis-befunde.findingsView";
	private CTabFolder ctabs;
	private ScrolledForm form;
	private Hashtable hash;
	private Action newValueAction, editValueAction, deleteValueAction, printValuesAction;
	
	public FindingsView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new GridLayout());
		form=Desk.getToolkit().createScrolledForm(parent);
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Composite body=form.getBody();
		body.setLayout(new FillLayout());
		ctabs=new CTabFolder(body,SWT.NONE);
		ctabs.setLayout(new FillLayout());
		Messwert setup=Messwert.getSetup();
		hash=setup.getHashtable("Befunde");
		String names=(String)hash.get("names");
		if(!StringTool.isNothing(names)){
			for(String n:names.split(Messwert.SETUP_SEPARATOR)){
				CTabItem ci=new CTabItem(ctabs,SWT.NONE);
				ci.setText(n);
				FindingsPage fp=new FindingsPage(ctabs,n);
				ci.setControl(fp);
			}
		}
		makeActions();
		ViewMenus menu=new ViewMenus(getViewSite());
		menu.createToolbar(newValueAction,editValueAction,printValuesAction,deleteValueAction);
		ctabs.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(final SelectionEvent e) {
				CTabItem it=ctabs.getSelection();
				if(it!=null){
					FindingsPage page=(FindingsPage)it.getControl();
					page.setPatient(GlobalEvents.getSelectedPatient());
				}
			}
			
		});
		
		GlobalEvents.getInstance().addActivationListener(this, getViewSite().getPart());
		if(ctabs.getItemCount()>0){
			ctabs.setSelection(0);
			((FindingsPage)(ctabs.getItem(0)).getControl()).setPatient(GlobalEvents.getSelectedPatient());
		}

	}
	
	@Override
	public void dispose(){
		GlobalEvents.getInstance().removeActivationListener(this, getViewSite().getPart());
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void activation(final boolean mode) {

	}

	public void visible(final boolean mode) {
		if(mode){
			GlobalEvents.getInstance().addSelectionListener(this);
			setPatient(GlobalEvents.getSelectedPatient());
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
	}

	public void clearEvent(final Class template) {
		if(template.equals(Patient.class)){
			setPatient(null);
		}
	}

	public void selectionEvent(final PersistentObject obj) {
		if(obj instanceof Patient){
			setPatient((Patient)obj);
		}

	}

	private void setPatient(final Patient p){
		if(p==null){
			form.setText("Kein Patient ausgewählt");

		}else{
			form.setText(p.getLabel());
		}
		int idx=ctabs.getSelectionIndex();
		if(idx!=-1){
			CTabItem item=ctabs.getItem(idx);
			FindingsPage fp=(FindingsPage)item.getControl();
			fp.setPatient(p);
		}
	}
	
	class FindingsPage extends Composite{
		
		Table table;
		TableColumn[] tc;
		TableItem[] items;
		String myparm;
		String[] flds=null;
		
		FindingsPage(final Composite parent,final String param){
			super(parent,SWT.NONE);
			parent.setLayout(new FillLayout());
			myparm=param;
			setLayout(new GridLayout());
			table=new Table(this,SWT.FULL_SELECTION|SWT.V_SCROLL);
			table.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			String vals=(String)hash.get(param+"_FIELDS");
			if(vals!=null){
				flds=vals.split(Messwert.SETUP_SEPARATOR); 
				tc=new TableColumn[flds.length+1];
				tc[0]=new TableColumn(table,SWT.NONE);
				tc[0].setText("Datum"); //$NON-NLS-1$
				tc[0].setWidth(80);
				for(int i=1;i<=flds.length;i++){
					tc[i]=new TableColumn(table,SWT.NONE);
					flds[i-1]=flds[i-1].split(Messwert.SETUP_CHECKSEPARATOR)[0];
					String[] header=flds[i-1].split("=",2);
					tc[i].setText(header[0]);
					if(header.length>1){
						tc[i].setData("script", header[1]);
					}
					tc[i].setWidth(80);
				}
				tc[flds.length].setWidth(600);
			}
			table.addMouseListener(new MouseAdapter(){

				@Override
				public void mouseDoubleClick(final MouseEvent e) {
					TableItem[] it=table.getSelection();
					if(it.length==1){
						EditFindingDialog dlg=new EditFindingDialog(getSite().getShell(),(Messwert)it[0].getData(),myparm);
						if(dlg.open()==Dialog.OK){
							setPatient(GlobalEvents.getSelectedPatient());
						}
					}
				}
				
			});
		}
		public String[][] getFields(){
			if(flds!=null){
				String[][] ret=new String[table.getItemCount()+1][flds.length+1];
				ret[0][0]="Datum"; //$NON-NLS-1$
				for(int i=1;i<=flds.length;i++){
					ret[0][i]=flds[i-1];
				}
				for(int i=0;i<table.getItemCount();i++){
					//ret[i+1]=new String[flds.length+1];
					for(int j=0;j<=flds.length;j++){
						ret[i+1][j]=table.getItem(i).getText(j);
					}
				}
				return ret;
			}
			return new String[0][0];
		}
		void setPatient(final Patient pat){
			if(pat!=null){
				Query<Messwert> qbe=new Query<Messwert>(Messwert.class);
				qbe.add("PatientID","=",pat.getId()); //$NON-NLS-1$ //$NON-NLS-2$
				qbe.add("Name","=",myparm); //$NON-NLS-1$ //$NON-NLS-2$
				List<Messwert> list=qbe.execute();
				table.removeAll();
				Collections.sort(list,new Comparator<Messwert>(){

					public int compare(final Messwert o1, final Messwert o2) {
						TimeTool t1=new TimeTool(o1.get("Datum"));
						TimeTool t2=new TimeTool(o2.get("Datum"));
						return t1.compareTo(t2);
					}});
				for(Messwert m:list){
					TableItem item=new TableItem(table,SWT.NONE);
					item.setText(0,m.get("Datum")); //$NON-NLS-1$
					item.setData(m);
					Hashtable hash=m.getHashtable("Befunde"); //$NON-NLS-1$
					for(int i=0;i<flds.length;i++){
						item.setText(i+1,PersistentObject.checkNull((String)hash.get(flds[i])));
					}
				}
			}

		}
	}
	/**
	 * Actions are objects for user - interactions. An action can be displayd as a menun item or
	 * as toolbar item, and it can be active or inactive.
	 * Here we need only one action to add a new measurement for a selectable date.
	 * 
	 * Actions sind Objekte zur Benutzerinteraktion. Eine Action kann als Menueitem oder als 
	 * Toolbaritem dargestellt werden, und sie kann aktiv oder inaktiv sein.
	 * Diese Action hier dient einfach der Eingabe eines neuen Messwerts a einem wählbaren Datum.
	 *
	 */
	private void makeActions(){
		newValueAction=new Action(Messages.getString("MesswerteView.enterNewValue")){ //$NON-NLS-1$
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_ADDITEM));
				setToolTipText("Eine neue Messung hinzufügen");
			}
			@Override
			public void run() {
				CTabItem ci=ctabs.getSelection();
				if(ci!=null){
					FindingsPage page=(FindingsPage)ci.getControl();
					EditFindingDialog dlg=new EditFindingDialog(getSite().getShell(),null,page.myparm);
					if(dlg.open()==Dialog.OK){
						page.setPatient(GlobalEvents.getSelectedPatient());
					}
				}
			}
		};
		editValueAction=new Action("Edit..."){
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_EDIT));
				setToolTipText("Text ansehen oder ändern");
			}
			@Override
			public void run() {
				CTabItem ci=ctabs.getSelection();
				if(ci!=null){
					FindingsPage page=(FindingsPage)ci.getControl();
					TableItem[] it=page.table.getSelection();
					if(it.length==1){
						EditFindingDialog dlg=new EditFindingDialog(getSite().getShell(),(Messwert)it[0].getData(),page.myparm);
						if(dlg.open()==Dialog.OK){
							page.setPatient(GlobalEvents.getSelectedPatient());
						}
					}
				}
			}
		};
		deleteValueAction=new Action("Löschen"){
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_DELETE));
				setToolTipText("Die gewählte Messung löschen");
			}
			@Override
			public void run() {
				if(SWTHelper.askYesNo("Messwert löschen", "Wollen Sie wirklich diese Messung unwiderruflich löschen?")){
					CTabItem ci=ctabs.getSelection();
					if(ci!=null){
						FindingsPage page=(FindingsPage)ci.getControl();
						TableItem[] it=page.table.getSelection();
						if(it.length==1){
							Messwert mw=(Messwert)it[0].getData();
							mw.delete();
							page.setPatient(GlobalEvents.getSelectedPatient());
						}
					}
				}
			}
		};
		printValuesAction=new Action("Drucken"){
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_PRINTER));
				setToolTipText("Diese Messwerte drucken");
			}
			@Override
			public void run(){
				CTabItem top=ctabs.getSelection();
				if(top!=null){
					FindingsPage fp=(FindingsPage)top.getControl();
					String[][] table=fp.getFields();
					new PrintFindingsDialog(getViewSite().getShell(),table).open();
				}
			}
		};

	}
	
}
