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
 *  $Id: LaborPrefs.java 2812 2007-07-15 15:25:59Z rgw_ch $
 *******************************************************************************/

package ch.elexis.preferences;

import java.util.*;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.Hub;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.*;
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
	private String[] headers={"Labor","Name","Kürzel","Typ", "Einheit", "Referenz M", "Referenz F", "Sortierung"};
	private int[] colwidth={100,100,50,50,50,100,100,100};
	
	public LaborPrefs() {
		super("Laborvorgaben");
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
					eli.getShell().setText("Laborparameter");
					eli.setTitle("Neuen Laborarameter eingeben");
					eli.setMessage("Bitte editieren Sie den Parameter und klicken Sie OK.");
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
				String s1="",s2="";
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
		bNewItem.setText("Neuer Laborparameter");
		bNewItem.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				editLabItem eli=new editLabItem(getShell(),null);
				eli.create();
				eli.getShell().setText("Laborparameter");
				eli.setTitle("Neuen Laborarameter eingeben");
				eli.setMessage("Bitte wählen sie ein Labor und geben Sie die Parameter ein.");
				if(eli.open()==Dialog.OK){
						tv.refresh();
				}
			}
			
		});
		Button bDelItem=new Button(buttons,SWT.PUSH);
		bDelItem.setText("Laboritem löschen");
		bDelItem.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				IStructuredSelection sel=(IStructuredSelection)tv.getSelection();
				Object o=sel.getFirstElement();
				if(o instanceof LabItem){
					LabItem li=(LabItem)o;
					Query<LabResult> qbe=new Query<LabResult>(LabResult.class);
					qbe.add("ItemID","=",li.getId());
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
		bDelAllItems.setText("Alle Items löschen");
		bDelAllItems.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				if(SWTHelper.askYesNo("Wirklich alle Items löschen", "Dies löscht alle Items und davon abhängigen Werte aller Patienten!")){
					Query<LabItem> qbli=new Query<LabItem>(LabItem.class);
					List<LabItem> items=qbli.execute();
					for(LabItem li:items){
						Query<LabResult> qbe=new Query<LabResult>(LabResult.class);
						qbe.add("ItemID","=",li.getId());
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
	class LabListLabelProvider extends LabelProvider implements ITableLabelProvider{

		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Automatisch erstellter Methoden-Stub
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			LabItem li=(LabItem)element;
			switch (columnIndex) {
			case 0:	return li.getLabor()==null ? "unbekannt" : li.getLabor().getLabel();
			case 1: return li.getName();
			case 2: return li.getKuerzel();
			case 3: LabItem.typ typ=li.getTyp(); 
				if(typ==LabItem.typ.NUMERIC){
						return "Zahl";
					}else if(typ==LabItem.typ.TEXT){
						return "Text";
					}
					return "Absolut";
			case 4: return li.getEinheit();
			case 5: return li.get("RefMann");
			case 6: return li.getRefW();
			case 7: return li.getGroup()+" - "+li.getPrio();
			default:
				return "?col?";
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
				String al=new Query<Labor>(Labor.class).findSingle("istLabor","=","1");
				if(al==null){
					actLabor=new Labor("Intern","Praxislabor");
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
			WidgetFactory.createLabel(ret,"Kürzel");
			iKuerzel=new Text(ret,SWT.BORDER);
			iKuerzel.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			WidgetFactory.createLabel(ret,"Titel");
			iTitel=new Text(ret,SWT.BORDER);
			iTitel.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			
			WidgetFactory.createLabel(ret,"Typ");
			Group grp=new Group(ret,SWT.NONE);
			grp.setLayout(new FillLayout(SWT.HORIZONTAL));
			grp.setLayoutData(SWTHelper.getFillGridData(3,true,1,false));
			numeric=new Button(grp,SWT.RADIO);
			numeric.setText("Zahl");
			alph=new Button(grp,SWT.RADIO);
			alph.setText("Text");
			abs=new Button(grp,SWT.RADIO);
			abs.setText("Absolut");
			formula=new Button(grp,SWT.RADIO);
			formula.setText("Formel");
			formula.addSelectionListener(new SelectionAdapter(){

				@Override
				public void widgetSelected(SelectionEvent e) {
					if(formula.getSelection()){
						InputDialog inp=new InputDialog(getShell(),"Formel für Laborwert eingeben",
								"Geben Sie bitte an, wie dieser Parameter errechnet werden soll",formel,null);
						if(inp.open()==Dialog.OK){
							formel=inp.getValue();
						}
					}
				}
				
			});
			WidgetFactory.createLabel(ret,"Referenz M");
	
			iRef=new Text(ret,SWT.BORDER);
			iRef.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			WidgetFactory.createLabel(ret,"Referenz F");
			iRfF=new Text(ret,SWT.BORDER);
			iRfF.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			WidgetFactory.createLabel(ret,"Einheit");
			iUnit=new Text(ret,SWT.BORDER);
			iUnit.setLayoutData(SWTHelper.getFillGridData(3,true,1,false));
			WidgetFactory.createLabel(ret,"Gruppe");
			cGroup=new Combo(ret,SWT.SINGLE|SWT.DROP_DOWN);
			cGroup.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			cGroup.setToolTipText("Gruppe für Laborblatt (beliebiger Text oder Zahl)");
			cGroup.setItems(groups.toArray(new String[0]));
			WidgetFactory.createLabel(ret,"Sequenz-Nr.");
			iPrio=new Text(ret,SWT.BORDER);
			iPrio.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			iPrio.setToolTipText("Sequenz innerhalb der Gruppe für Laborblatt (beliebiger Text oder Zahl");
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
				iRef.setText(result.get("RefMann"));
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
				String t="0";
				if(typ==LabItem.typ.TEXT){
					t="1";
				}else if(typ==LabItem.typ.ABSOLUTE){
					t="2";
				}else if(typ==LabItem.typ.FORMULA){
					t="3";
				}
				result.set(new String[]{"kuerzel","titel","LaborID",
				"RefMann","RefFrauOrTx","Einheit","Typ","Gruppe","prio"},
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
