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

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.Dialog;
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
	
	
	public ArtikelContextMenu(Artikel template, CommonViewer cv){
		this.cv=cv;
		makeActions(template);
		final MenuManager menu=new MenuManager();
		menu.addMenuListener(new IMenuListener(){
			public void menuAboutToShow(IMenuManager manager) {
				menu.removeAll();
				menu.add(deleteAction);
				menu.add(createAction);
				menu.add(editAction);
			}
			
		});
		cv.setContextMenu(menu);
	}
	
	public ArtikelContextMenu(Artikel template, CommonViewer cv, ArtikelDetailDisplay add){
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
}
