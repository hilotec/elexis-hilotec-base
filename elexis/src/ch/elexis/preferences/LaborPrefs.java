/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: LaborPrefs.java 3862 2008-05-05 16:14:14Z rgw_ch $
 *******************************************************************************/

package ch.elexis.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;
import ch.elexis.data.Labor;
import ch.elexis.data.Query;
import ch.elexis.scripting.ScriptEditor;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.WidgetFactory;
import ch.rgw.tools.StringTool;

public class LaborPrefs extends PreferencePage implements
		IWorkbenchPreferencePage {

	//DynamicListDisplay params;
	//Composite definition;
	//FormToolkit tk;
	private TableViewer tv;
	private Table table;
	ArrayList<String> groups;
	int sortC=1;
	private String[] headers={Messages.LaborPrefs_lab,Messages.LaborPrefs_name,Messages.LaborPrefs_short,Messages.LaborPrefs_type, Messages.LaborPrefs_unit, Messages.LaborPrefs_refM, Messages.LaborPrefs_refF, Messages.LaborPrefs_sortmode};
	private int[] colwidth={100,100,50,50,50,100,100,100};
	
	public LaborPrefs() {
		super(Messages.LaborPrefs_labTitle);
		groups=new ArrayList<String>();
	}

	protected Control createContents(Composite parn){
		//parn.setLayout(new FillLayout());
		noDefaultAndApplyButton();
		
		Composite ret=new Composite(parn,SWT.NONE);
		ret.setLayout(new GridLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		table=new Table(ret,SWT.SINGLE|SWT.V_SCROLL);
		for(int i=0;i<headers.length;i++){
			TableColumn tc=new TableColumn(table,SWT.LEFT);
			tc.setText(headers[i]);
			tc.setWidth(colwidth[i]);
			tc.setData(i);
			tc.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					sortC=(Integer)((TableColumn)e.getSource()).getData();
					tv.refresh(true);
				}
				
			});
		}
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		tv=new TableViewer(table);
		tv.setContentProvider(new IStructuredContentProvider(){

			@SuppressWarnings("unchecked")
			public Object[] getElements(Object inputElement) {
				Query qbe=new Query(LabItem.class);
				List list=qbe.execute();
				groups.clear();
				for(LabItem li:(List<LabItem>)list){
					if(groups.contains(li.getGroup())){
						continue;
					}
					groups.add(li.getGroup());
				}
				Collections.sort(groups);
				return list.toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
		});
		tv.setLabelProvider(new LabListLabelProvider());
		tv.addDoubleClickListener(new IDoubleClickListener(){

			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel=(IStructuredSelection)tv.getSelection();
				Object o=sel.getFirstElement();
				if(o instanceof LabItem){
					LabItem li=(LabItem)o;
					editLabItem eli=new editLabItem(getShell(),li);
					eli.create();
					eli.getShell().setText(Messages.LaborPrefs_labParams);
					eli.setTitle(Messages.LaborPrefs_enterNewLabParam);
					eli.setMessage(Messages.LaborPrefs_pleaseEditParam);
					if(eli.open()==Dialog.OK){
						tv.refresh();
					}
				}				
			}
			
		});
		tv.setSorter(new ViewerSorter(){

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				LabItem li1=(LabItem)e1;
				LabItem li2=(LabItem)e2;
				String s1="",s2=""; //$NON-NLS-1$ //$NON-NLS-2$
				switch (sortC) {
				case 0:
					s1=li1.getLabor().getLabel();
					s2=li2.getLabor().getLabel();
					break;
				case 2:
					s1=li1.getKuerzel();
					s2=li2.getKuerzel();
					break;
				case 3:
					s1=li1.getTyp().toString();
					s2=li2.getTyp().toString();
					break;
				case 7:
					s1=li1.getGroup();
					s2=li2.getGroup();
					break;
				default:
					s1=li1.getName();
					s2=li2.getName();
				} 
				int res=s1.compareToIgnoreCase(s2);
				if(res==0){
					return li1.getPrio().compareToIgnoreCase(li2.getPrio());
				}
				return res;
			}
			
		});
		tv.setInput(this);
		Composite buttons=new Composite(ret,SWT.BORDER);
		RowLayout rl=new RowLayout();
		rl.justify=true;
		buttons.setLayout(rl);
		Button bNewItem=new Button(buttons,SWT.PUSH);
		bNewItem.setText(Messages.LaborPrefs_labValue);
		bNewItem.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				editLabItem eli=new editLabItem(getShell(),null);
				eli.create();
				eli.getShell().setText(Messages.LaborPrefs_labParam);
				eli.setTitle(Messages.LaborPrefs_enterNewLabParam);
				eli.setMessage(Messages.LaborPrefs_pleaseEnterLabParam);
				if(eli.open()==Dialog.OK){
						tv.refresh();
				}
			}
			
		});
		Button bDelItem=new Button(buttons,SWT.PUSH);
		bDelItem.setText(Messages.LaborPrefs_deleteItem);
		bDelItem.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				IStructuredSelection sel=(IStructuredSelection)tv.getSelection();
				Object o=sel.getFirstElement();
				if(o instanceof LabItem){
					LabItem li=(LabItem)o;
					Query<LabResult> qbe=new Query<LabResult>(LabResult.class);
					qbe.add("ItemID","=",li.getId()); //$NON-NLS-1$ //$NON-NLS-2$
					List<LabResult> list=qbe.execute();
					for(LabResult po:list){
						po.delete();
					}
					li.delete();
					tv.remove(o);
				}
			}
		});
		Button bDelAllItems=new Button(buttons,SWT.PUSH);
		bDelAllItems.setText(Messages.LaborPrefs_deleteAllItems);
		bDelAllItems.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				if(SWTHelper.askYesNo(Messages.LaborPrefs_deleteReallyAllItems, Messages.LaborPrefs_deleteAllExplain)){
					Query<LabItem> qbli=new Query<LabItem>(LabItem.class);
					List<LabItem> items=qbli.execute();
					for(LabItem li:items){
						Query<LabResult> qbe=new Query<LabResult>(LabResult.class);
						qbe.add("ItemID","=",li.getId()); //$NON-NLS-1$ //$NON-NLS-2$
						List<LabResult> list=qbe.execute();
						for(LabResult po:list){
							po.delete();
						}
						li.delete();
					}
					tv.refresh();
				}
			}
		});
		if(Hub.acl.request(AccessControlDefaults.DELETE_LABITEMS)==false){
			bDelAllItems.setEnabled(false);
		}
		return ret;
	}
	static class LabListLabelProvider extends LabelProvider implements ITableLabelProvider{

		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Automatisch erstellter Methoden-Stub
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			LabItem li=(LabItem)element;
			switch (columnIndex) {
			case 0:	return li.getLabor()==null ? Messages.LaborPrefs_unkown : li.getLabor().getLabel();
			case 1: return li.getName();
			case 2: return li.getKuerzel();
			case 3: LabItem.typ typ=li.getTyp(); 
				if(typ==LabItem.typ.NUMERIC){
						return Messages.LaborPrefs_numeric;
					}else if(typ==LabItem.typ.TEXT){
						return Messages.LaborPrefs_alpha;
					}
					return Messages.LaborPrefs_absolute;
			case 4: return li.getEinheit();
			case 5: return li.get("RefMann"); //$NON-NLS-1$
			case 6: return li.getRefW();
			case 7: return li.getGroup()+" - "+li.getPrio(); //$NON-NLS-1$
			default:
				return "?col?"; //$NON-NLS-1$
			}
		}
		
	};
	
	/* (Kein Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		//tk.dispose();
		//labors.dispose();
		super.dispose();
	}

	public void init(IWorkbench workbench) {
		// TODO Automatisch erstellter Methoden-Stub

	}
	

	class editLabItem extends TitleAreaDialog{
		//private String[] fields={"Kürzel","Titel","Typ","Referenzbereich","Einheit"};
		Text iKuerzel,iTitel,iRef,iRfF, iUnit,iPrio;
		Combo cGroup;
		Button alph,numeric,abs,formula;
		String formel;
		org.eclipse.swt.widgets.List labors;
		Hashtable<String,Labor> lablist=new Hashtable<String,Labor>();
		Labor actLabor;
		LabItem result;
		
		public editLabItem(Shell parentShell,LabItem act) {
			super(parentShell);
			result=act;
			if(act==null){
				String al=new Query<Labor>(Labor.class).findSingle("istLabor",Messages.LaborPrefs_34,Messages.LaborPrefs_35); //$NON-NLS-1$
				if(al==null){
					actLabor=new Labor(Messages.LaborPrefs_36,Messages.LaborPrefs_37);
				}else{
					actLabor=Labor.load(al);
				}
			}else{
				actLabor=act.getLabor();
			}
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite ret=new Composite(parent,SWT.NONE);
			ret.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
			ret.setLayout(new GridLayout(4,false));
			labors=new org.eclipse.swt.widgets.List(ret,SWT.BORDER);
			labors.setLayoutData(SWTHelper.getFillGridData(4,true,1,false));
			labors.addSelectionListener(new SelectionAdapter(){

				public void widgetSelected(SelectionEvent e) {
					int i=labors.getSelectionIndex();
					if(i!=-1){
						actLabor=lablist.get(labors.getItem(i));
					}

				}
				
			});
			Query<Labor> qbe=new Query<Labor>(Labor.class);
			List<Labor> list=qbe.execute();
			int idx=0,i=0;
			String al=actLabor.getLabel();
			for(Labor o:list){
				String lb=o.getLabel();
				lablist.put(lb,(Labor)o);
				labors.add(lb);
				if(lb.equals(al)){
					idx=i;
				}
				i++;
			}
			labors.setSelection(idx);
			WidgetFactory.createLabel(ret,Messages.LaborPrefs_38);
			iKuerzel=new Text(ret,SWT.BORDER);
			iKuerzel.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			WidgetFactory.createLabel(ret,Messages.LaborPrefs_39);
			iTitel=new Text(ret,SWT.BORDER);
			iTitel.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			
			WidgetFactory.createLabel(ret,Messages.LaborPrefs_40);
			Group grp=new Group(ret,SWT.NONE);
			grp.setLayout(new FillLayout(SWT.HORIZONTAL));
			grp.setLayoutData(SWTHelper.getFillGridData(3,true,1,false));
			numeric=new Button(grp,SWT.RADIO);
			numeric.setText(Messages.LaborPrefs_41);
			alph=new Button(grp,SWT.RADIO);
			alph.setText(Messages.LaborPrefs_42);
			abs=new Button(grp,SWT.RADIO);
			abs.setText(Messages.LaborPrefs_43);
			formula=new Button(grp,SWT.RADIO);
			formula.setText(Messages.LaborPrefs_44);
			formula.addSelectionListener(new SelectionAdapter(){

				@Override
				public void widgetSelected(SelectionEvent e) {
					if(formula.getSelection()){
						
						ScriptEditor se=new ScriptEditor(getShell(),formel,Messages.LaborPrefs_45);
						if(se.open()==Dialog.OK){
							formel=se.getScript();
						}
					}
				}
				
			});
			WidgetFactory.createLabel(ret,Messages.LaborPrefs_46);
	
			iRef=new Text(ret,SWT.BORDER);
			iRef.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			WidgetFactory.createLabel(ret,Messages.LaborPrefs_47);
			iRfF=new Text(ret,SWT.BORDER);
			iRfF.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			WidgetFactory.createLabel(ret,Messages.LaborPrefs_48);
			iUnit=new Text(ret,SWT.BORDER);
			iUnit.setLayoutData(SWTHelper.getFillGridData(3,true,1,false));
			WidgetFactory.createLabel(ret,Messages.LaborPrefs_49);
			cGroup=new Combo(ret,SWT.SINGLE|SWT.DROP_DOWN);
			cGroup.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			cGroup.setToolTipText(Messages.LaborPrefs_50);
			cGroup.setItems(groups.toArray(new String[0]));
			WidgetFactory.createLabel(ret,Messages.LaborPrefs_51);
			iPrio=new Text(ret,SWT.BORDER);
			iPrio.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			iPrio.setToolTipText(Messages.LaborPrefs_52);
			if(result!=null){
				iKuerzel.setText(result.getKuerzel());
				iTitel.setText(result.getName());
				if(result.getTyp()==LabItem.typ.NUMERIC){
					numeric.setSelection(true);
				}else if(result.getTyp()==LabItem.typ.TEXT){
					alph.setSelection(true);
				}else if(result.getTyp()==LabItem.typ.ABSOLUTE){
					abs.setSelection(true);
				}else{
					formula.setSelection(true);
				}
				iUnit.setText(result.getEinheit());
				iRef.setText(result.get(Messages.LaborPrefs_53));
				iRfF.setText(result.getRefW());
				cGroup.setText(result.getGroup());
				iPrio.setText(result.getPrio());
				formel=result.getFormula();
			}
			return ret;
		}

		@Override
		protected void cancelPressed() {
			// TODO Automatisch erstellter Methoden-Stub
			super.cancelPressed();
		}

		@Override
		protected void okPressed() {
			LabItem.typ typ;
			//String refmin="",refmax;
			//refmax=iRef.getText();
			if(numeric.getSelection()==true){
				typ=LabItem.typ.NUMERIC;
			}else if (abs.getSelection()==true){
				typ=LabItem.typ.ABSOLUTE;
			}else if(formula.getSelection()){
				typ=LabItem.typ.FORMULA;
			}else{
				typ=LabItem.typ.TEXT;
			}
			if(result==null){
				result=new LabItem(iKuerzel.getText(),iTitel.getText(),
					actLabor,iRef.getText(),iRfF.getText(),iUnit.getText(),typ,cGroup.getText(),iPrio.getText());
			}else{
				String t=Messages.LaborPrefs_54;
				if(typ==LabItem.typ.TEXT){
					t=Messages.LaborPrefs_55;
				}else if(typ==LabItem.typ.ABSOLUTE){
					t=Messages.LaborPrefs_56;
				}else if(typ==LabItem.typ.FORMULA){
					t=Messages.LaborPrefs_57;
				}
				result.set(new String[]{Messages.LaborPrefs_58,Messages.LaborPrefs_59,Messages.LaborPrefs_60,
				Messages.LaborPrefs_61,Messages.LaborPrefs_62,Messages.LaborPrefs_63,Messages.LaborPrefs_64,Messages.LaborPrefs_65,Messages.LaborPrefs_66},
				iKuerzel.getText(),iTitel.getText(),actLabor.getId(),iRef.getText(),iRfF.getText(),
				iUnit.getText(),t,cGroup.getText(),iPrio.getText()
				);
			}
			if(!StringTool.isNothing(formel)){
				result.setFormula(formel);
			}
			super.okPressed();
		}
		
	}
	

}
