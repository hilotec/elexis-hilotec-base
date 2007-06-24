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
 *  $Id: DocumentSelectDialog.java 2305 2007-04-28 07:56:08Z rgw_ch $
 *******************************************************************************/
package ch.elexis.dialogs;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.data.Brief;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.elexis.util.DefaultLabelProvider;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;

/**
 * Select a Document or a template
 * Usage:
 * DocumentSelector dsl=new DocumentSelector(shell,mandant,TYPE_xxx);
 * if(dsl.open()==Dialog.OK){
 * 		doSomethingWith(dsl.result);
 * }
 * @author gerry
 *
 */
public class DocumentSelectDialog extends TitleAreaDialog {
	/** select an existing document out of the list of all documtents of the given mandator*/
	public static final int TYPE_LOAD_DOCUMENT=0;
	/** create a new document using one of the templates of the given mandator */
	public static final int TYPE_CREATE_DOC_WITH_TEMPLATE=1;
	/** open a user template of the given mandator for editing or export*/
	public static final int TYPE_LOAD_TEMPLATE=2;
	/** open a system template of the given  mandator for editing or export */ 
	public static final int TYPE_LOAD_SYSTEMPLATE=4;
	
	static final int TEMPLATE=TYPE_LOAD_TEMPLATE|TYPE_LOAD_SYSTEMPLATE;
	Person rel;
	int type;
	Brief result;
	Text tBetreff;
	String betreff;
	TableViewer tv;
	private MenuManager menu;
	private Action editNameAction;
	private Action deleteTemplateAction;
	private Action deleteTextAction;
	
	/**
	 * Create a new DocumentSelector. If the user clicks OK, the selected Brief will
	 * be in result.
	 * @param p the mandator whose templates/letters should be displayed
	 * @param typ type of the selector to display (see TYPE_ constants)
	 */
	public DocumentSelectDialog(Shell shell, Person p, int typ){
		super(shell);
		rel=p;
		type=typ;
	}
	@Override
	public void create(){
		super.create();
		setTitleImage(Desk.theImageRegistry.get(Desk.IMG_LOGO48));
		
		makeActions();
		switch(type){
		case TYPE_LOAD_DOCUMENT:
			setTitle("Dokument öffnen");
			setMessage("Bitte wählen Sie das gewünschte Dokument aus untenstehender Liste und klicken Sie auf OK");
			getShell().setText("Dokument öffnen");
			break;
		case TYPE_CREATE_DOC_WITH_TEMPLATE:
			setTitle("Brief mit Vorlage erstellen");
			setMessage("Geben Sie einen Betreff ein, wählen Sie eine Vorlage aus und klicken Sie auf OK");
			getShell().setText("Vorlage für den Brief wählen");
			break;
		case TYPE_LOAD_TEMPLATE:
			setTitle("Vorlage öffnen");
			setMessage("Bitte wählen Sie die gewünschte Vorlage aus untenstehender Liste und klicken Sie OK");
			getShell().setText("Vorlage öffnen");
			break;
		case TYPE_LOAD_SYSTEMPLATE:
			setTitle("Systemvorlage laden");
			setMessage("Dies sind die vom System benötigten Vorlagen");
			getShell().setText("Vorlage laden");
		}	
	}
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout());
		if((type&TEMPLATE) != 0){
			new Label(ret,SWT.NONE).setText("Betreff");
			tBetreff=SWTHelper.createText(ret, 1, SWT.NONE);
			new Label(ret,SWT.SEPARATOR|SWT.HORIZONTAL);
		}
		tv=new TableViewer(ret,SWT.V_SCROLL);
		tv.setContentProvider(new IStructuredContentProvider(){

			public Object[] getElements(Object inputElement) {
				Query<Brief> qbe=new Query<Brief>(Brief.class);
				if(type==TYPE_LOAD_DOCUMENT){
					qbe.add("Typ", "<>", Brief.TEMPLATE);
				}else{
					String sys= type==TYPE_LOAD_SYSTEMPLATE ? "=" : "<>";
					qbe.add("Typ","=",Brief.TEMPLATE);
					qbe.add("BehandlungsID",sys,"SYS");
					qbe.startGroup();
					qbe.add("DestID","=",Hub.actMandant.getId());
					qbe.or();
					qbe.add("DestID","=","");
					qbe.endGroup();
				}
				qbe.and();
				qbe.add("geloescht","<>","1");
				
				if(type!=TYPE_LOAD_DOCUMENT){
					qbe.orderBy(false, "Betreff");
				}else{
					qbe.orderBy(false, "Datum");
				}
				List<Brief> l=qbe.execute();
				return l.toArray();
			}

			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
		tv.setLabelProvider(new DefaultLabelProvider());
		makeActions();
		menu=new MenuManager();
		menu.add(editNameAction);
		menu.add((type&TEMPLATE)!=0 ? deleteTemplateAction : deleteTextAction);
		tv.getControl().setMenu(menu.createContextMenu(tv.getControl()));
		tv.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tv.setInput(this);
		return ret;
	}
	@Override
	protected void okPressed() {
		IStructuredSelection sel=(IStructuredSelection)tv.getSelection();
			if((sel!=null)&& (!sel.isEmpty())){
			result=(Brief)sel.getFirstElement();
			if((type&TEMPLATE)!=0){
				betreff=tBetreff.getText();
			}
			if(StringTool.isNothing(betreff)){
				betreff=result.getBetreff();
			}
		}
		super.okPressed();
	}
	public Brief getSelectedDocument(){
		return result;
	}
	public String getBetreff(){
		return betreff;
	}
	private void makeActions(){
		editNameAction=new Action("Betreff ändern..."){
			@Override
			public void run(){
				
			}
		};
		deleteTemplateAction=new Action("Vorlage löschen"){
			@Override
			public void run(){
				Brief sel=(Brief)((IStructuredSelection)tv.getSelection()).getFirstElement();
				if(MessageDialog.openConfirm(getShell(),"Vorlage löschen","Wirklich die Vorlage "+sel.getBetreff()+" löschen?")==true){
					sel.delete();
					tv.refresh();
				}
			}
		};
		deleteTextAction=new Action("Dokument löschen"){
			@Override
			public void run(){
				Brief sel=(Brief)((IStructuredSelection)tv.getSelection()).getFirstElement();
				if(MessageDialog.openConfirm(getShell(),"Dokument löschen","Wirklich das Dokument "+sel.getBetreff()+" löschen?")==true){
					sel.set("geloescht","1");
					tv.refresh();
				}
			}
		};
	}
}

