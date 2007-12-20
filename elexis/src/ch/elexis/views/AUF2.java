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
 *  $Id: AUF2.java 3472 2007-12-20 20:57:14Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.AUF;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.dialogs.EditAUFDialog;
import ch.elexis.util.DefaultLabelProvider;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;
import ch.rgw.tools.ExHandler;

/**
 * Arbeitsunfähigkeitszeugnisse erstellen und verwalten.
 * @author gerry
 *
 */
public class AUF2 extends ViewPart implements ActivationListener, SelectionListener{
	public static final String ID = "ch.elexis.auf";
	TableViewer tv;
	private Action newAUF, delAUF, modAUF, printAUF;
	

	@Override
	public void createPartControl(Composite parent) {
		setPartName(Messages.getString("AUF2.certificate")); //$NON-NLS-1$
		tv=new TableViewer(parent);
		tv.setLabelProvider(new DefaultLabelProvider());
		tv.setContentProvider(new AUFContentProvider());
		makeActions();
		ViewMenus menus=new ViewMenus(getViewSite());
		menus.createMenu(newAUF,delAUF,modAUF,printAUF);
		menus.createToolbar(newAUF,delAUF,printAUF);
		tv.setUseHashlookup(true);
		GlobalEvents.getInstance().addActivationListener(this, this);
		tv.addSelectionChangedListener(GlobalEvents.getInstance().getDefaultListener());
		tv.addDoubleClickListener(new IDoubleClickListener(){
			public void doubleClick(DoubleClickEvent event) {
				modAUF.run();
			}});
		tv.setInput(getViewSite());
	}

	@Override
	public void dispose(){
		GlobalEvents.getInstance().removeActivationListener(this, this);
	}
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	 private void makeActions(){
		 newAUF=new Action(Messages.getString("AUF2.new")){ //$NON-NLS-1$
			 {
				 setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_NEW));
				 setToolTipText(Messages.getString("AUF2.createNewCert")); //$NON-NLS-1$
			 }
			@Override
			public void run() {
				if(GlobalEvents.getSelectedFall()==null){
					Konsultation kons=GlobalEvents.getSelectedKons();
					if(kons==null){
						SWTHelper.showError(Messages.getString("AUF2.noCaseSelected"), Messages.getString("AUF2.selectCase")); //$NON-NLS-1$ //$NON-NLS-2$
						return;
					}
					GlobalEvents.getInstance().fireSelectionEvent(kons.getFall());
				}
				new EditAUFDialog(getViewSite().getShell(),null).open();
				tv.refresh(false);
			}
		 };
		 delAUF=new Action(Messages.getString("AUF2.delete")){ //$NON-NLS-1$
			 {
				 setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_DELETE));
				 setToolTipText(Messages.getString("AUF2.deleteCertificate")); //$NON-NLS-1$
			 }
			 @Override
				public void run() {
					AUF sel=getSelectedAUF();
					if(sel!=null){
						if(MessageDialog.openConfirm(getViewSite().getShell(), Messages.getString("AUF2.deleteReally"), Messages.getString("AUF2.doyoywantdeletereally"))){ //$NON-NLS-1$ //$NON-NLS-2$
							sel.delete();
							tv.refresh(false);
						}
					}
				}
		 };
		 modAUF=new Action(Messages.getString("AUF2.edit")){ //$NON-NLS-1$
			 {
				 setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_EDIT));
				 setToolTipText(Messages.getString("AUF2.editCertificate")); //$NON-NLS-1$
			 }
			 @Override
				public void run() {
				 AUF sel=getSelectedAUF();
				 if(sel!=null){
					new EditAUFDialog(getViewSite().getShell(),sel).open();
					tv.refresh(true);
				 }
			 }
		 };
		 printAUF=new Action(Messages.getString("AUF2.print")){ //$NON-NLS-1$
			 {
				 setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_PRINT));
				 setToolTipText(Messages.getString("AUF2.createPrint")); //$NON-NLS-1$
			 }
				@Override
				public void run(){
				try{  
					AUFZeugnis az=(AUFZeugnis)getViewSite().getPage().showView(AUFZeugnis.ID);
					ch.elexis.data.AUF actAUF=(ch.elexis.data.AUF) GlobalEvents.getInstance().getSelectedObject(ch.elexis.data.AUF.class);
				    az.createAUZ(actAUF);
				}catch(Exception ex){
					ExHandler.handle(ex);
				}
					    
				}
			};
	 }
	 private ch.elexis.data.AUF getSelectedAUF(){
		 IStructuredSelection sel=(IStructuredSelection)tv.getSelection();
		 if((sel==null) || (sel.isEmpty()) ){
			 return null;
		 }
		 return (AUF)sel.getFirstElement();
	 }
	 class AUFContentProvider implements IStructuredContentProvider{

		public Object[] getElements(Object inputElement) {
			Patient pat=GlobalEvents.getSelectedPatient();
			if(pat==null){
				return new Object[0];
			}
			Query<AUF> qbe=new Query<AUF>(AUF.class);
			qbe.add("PatientID", "=", pat.getId());
			qbe.orderBy(true, "von","bis");
			List<AUF> list=qbe.execute();
			return list.toArray();
		}

		public void dispose() { /* leer */}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			/* leer */
		}
		 
	 }
	public void activation(boolean mode) {	/* egal */ }

	public void visible(boolean mode) {
		if(mode){
			GlobalEvents.getInstance().addSelectionListener(this);
			selectionEvent(GlobalEvents.getSelectedPatient());
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
	}

	public void clearEvent(Class template) {
		if(template.equals(AUF.class)){
			modAUF.setEnabled(false);
			delAUF.setEnabled(false);
		}else if(template.equals(Patient.class)){
			newAUF.setEnabled(false);
			modAUF.setEnabled(false);
			delAUF.setEnabled(false);
		}
		
	}

	public void selectionEvent(PersistentObject obj) {
		if(obj instanceof Patient){
			tv.refresh();
			GlobalEvents.getInstance().clearSelection(AUF.class);
			newAUF.setEnabled(true);
		}else if(obj instanceof AUF){
			modAUF.setEnabled(true);
			delAUF.setEnabled(true);
		}
		
	}
}
