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
 *  $Id: Artikeldetail.java 590 2006-07-24 15:40:40Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views.artikel;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Artikel;
import ch.elexis.dialogs.ArtikelDetailDialog;
import ch.elexis.util.CommonViewer;

public class ArtikelContextMenu {
	private IAction deleteAction, createAction, editAction;
	CommonViewer cv;
	ArtikelDetailDisplay add;
	ArtikelMenuListener menuListener=new ArtikelMenuListener();
	MenuManager menu;
	ArrayList<IAction> actions=new ArrayList<IAction>(); 
	
	public ArtikelContextMenu(final Artikel template, final CommonViewer cv){
		this.cv=cv;
		makeActions(template);
		actions.add(deleteAction);
		actions.add(createAction);
		actions.add(editAction);
		menu=new MenuManager();
		menu.addMenuListener(menuListener);
		cv.setContextMenu(menu);
	}
	
	public void addAction(final IAction ac){
		actions.add(ac);
	}
	public void removeAction(final IAction ac){
		actions.remove(ac);
	}
	
	public ArtikelContextMenu(final Artikel template, final CommonViewer cv, final ArtikelDetailDisplay add){
		this(template,cv);
		this.add=add;
	}
	
	private void makeActions(final Artikel art){
		deleteAction=new Action("Löschen"){
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_DELETE));
				setToolTipText(art.getClass().getName()+" löschen");
			}
			@Override
			public void run(){
				Artikel act=(Artikel)GlobalEvents.getInstance().getSelectedObject(art.getClass());
				if(MessageDialog.openConfirm(cv.getViewerWidget().getControl().getShell(), "Löschen bestätigen", 
						"Wollen Sie wirklich "+act.getName()+" löschen?")){
					act.delete();
					cv.notify(CommonViewer.Message.update);
				}

			}
		};
		createAction=new Action("Neu..."){
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_NEW));
				setToolTipText("Einen neuen Artikel erstellen");
			}
			@Override
			public void run(){
				InputDialog inp=new InputDialog(cv.getViewerWidget().getControl().getShell(),
						art.getClass().getName()+" erstellen","Bitte geben Sie einen Namen für den neuen Artikel ein","",null);
				if(inp.open()==InputDialog.OK){
					String name=inp.getValue();
					Artikel n=new Artikel(name,art.getCodeSystemName(),"");
					if(add==null){
						ArtikelDetailDialog ad=new ArtikelDetailDialog(cv.getViewerWidget().getControl().getShell(),n);
						ad.open();
					}else{
						add.show(n);
					}
				}

			}
		};
		editAction=new Action("Eigenschaften..."){
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_EDIT));
				setToolTipText("Eigenschaften dieses Artikels bearbeiten");
			}
			@Override
			public void run(){
				Artikel n=(Artikel)GlobalEvents.getInstance().getSelectedObject(art.getClass());
				if(add==null){
					new ArtikelDetailDialog(cv.getViewerWidget().getControl().getShell(),n).open();
				}else{
					add.show(n);
				}
			}
		};
	}

	public interface ArtikelDetailDisplay{
		public boolean show(Artikel art);
	}
	
	class ArtikelMenuListener implements IMenuListener{
		public void menuAboutToShow(final IMenuManager manager) {
			menu.removeAll();
			for(IAction ac:actions){
				if(ac==null){
					menu.add(new Separator());
				}else{
					menu.add(ac);
				}
			}
		}
	}
}
