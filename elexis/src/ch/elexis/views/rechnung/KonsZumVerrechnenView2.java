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
 * $Id: KonsZumVerrechnenView.java 853 2006-09-01 17:02:45Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views.rechnung;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.Tree;
import ch.elexis.util.ViewMenus;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink.Stm;

@Deprecated
public class KonsZumVerrechnenView2 extends ViewPart {
	TreeViewer tv;
	Tree<PersistentObject> tree;
	private IAction refreshAction,makeBillAction;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		tv=new TreeViewer(parent);
		tv.setContentProvider(new TreeContentProvider());
		tv.setLabelProvider(new TreeLabelProvider());
		tv.setUseHashlookup(true);
		tv.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		makeActions();
		ViewMenus menu=new ViewMenus(getViewSite());
		menu.createToolbar(makeBillAction,refreshAction);
		tv.addSelectionChangedListener(GlobalEvents.getInstance().getDefaultListener());
		tv.setInput(getViewSite());
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	class TreeContentProvider implements ITreeContentProvider{
   	 	Query<Konsultation> qKons=new Query<Konsultation>(Konsultation.class);
		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof Tree){
				return ((Tree)parentElement).getChildren().toArray();
			}
			return null;
		}

		public Object getParent(Object element) {
			if(element instanceof Tree){
				return ((Tree)element).getParent();
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			if(element instanceof Tree){
				if(((Tree)element).contents instanceof Konsultation){
					return false;
				}
			}
			return true;
		}

		public Object[] getElements(Object inputElement) {
			tree=new Tree<PersistentObject>(null,null);
			IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
			final Stm stm=PersistentObject.getConnection().getStatement();
			try {
				progressService.runInUI(
				      PlatformUI.getWorkbench().getProgressService(),
					      new IRunnableWithProgress() {
					         public void run(IProgressMonitor monitor) {
					        	 monitor.beginTask(Messages.getString("KonsZumVerrechnenView2.collectConsultations"),1000); //$NON-NLS-1$
					        	 monitor.subTask(Messages.getString("KonsZumVerrechnenView2.databaseRequest")); //$NON-NLS-1$
					        	 qKons.clear();
					        	 qKons.add("RechnungsID", "", null); //$NON-NLS-1$ //$NON-NLS-2$
					        	 qKons.orderBy(true, "Datum"); //$NON-NLS-1$
					        	 List<Konsultation> lKons=qKons.execute();
					        	 int total=lKons.size();
					        	 int iter=900/total;
					        	 monitor.worked(100);
					        	 for(Konsultation k:lKons){
					        		 Fall fall=k.getFall();
					        		 if(fall==null){
					        			 continue;
					        		 }
					        		 Patient pat=fall.getPatient();
					        		 if(pat==null){
					        			 continue;
					        		 }
					        		 Tree<PersistentObject> tPat=tree.find(pat, false);
					        		 if(tPat==null){
					        			 tPat=new Tree<PersistentObject>(tree,pat,new Comparator<PersistentObject>(){

											public int compare(PersistentObject o1, PersistentObject o2) {
													return(o1.getLabel().compareToIgnoreCase(o2.getLabel()));

											}});
					        		 }
					        		 Tree<PersistentObject> tFall=tPat.find(fall, false);
					        		 if(tFall==null){
					        			 tFall=new Tree<PersistentObject>(tPat,fall); 
					        		 }
					        		 /*Tree<PersistentObject> tKons=*/new Tree<PersistentObject>(tFall,k);
					        		 monitor.worked(iter);
					        	 }
					        	 
					         }
			
				      },null);
				} catch (Throwable ex) {
					ExHandler.handle(ex);
				}finally{
					PersistentObject.getConnection().releaseStatement(stm);
				}
				return tree.getChildren().toArray();
		}

		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
			
		}
		
	}
	private static class TreeLabelProvider extends LabelProvider{

		@Override
		public Image getImage(Object element) {
			// TODO Auto-generated method stub
			return super.getImage(element);
		}

		@Override
		public String getText(Object element) {
			if(element instanceof Tree){
				if(((Tree)element).contents instanceof Patient){
					Patient pat=(Patient)((Tree)element).contents;
					return pat.getLabel();
				}else if(((Tree)element).contents instanceof Fall){
					Fall fall=(Fall)((Tree)element).contents;
					return fall.getLabel();
				}else if(((Tree)element).contents instanceof Konsultation){
					Konsultation k=(Konsultation)((Tree)element).contents;
					return k.getLabel();
				}
			}
			return super.getText(element);
		}

				
	}
	private void makeActions(){
		refreshAction=new Action(Messages.getString("KonsZumVerrechnenView2.refresh")){ //$NON-NLS-1$
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_REFRESH));
				setToolTipText(Messages.getString("KonsZumVerrechnenView2.refreshList")); //$NON-NLS-1$
			}
			public void run(){
				tv.refresh();
			}
		};
		makeBillAction=new Action(Messages.getString("KonsZumVerrechnenView2.createInvoicesAction")){ //$NON-NLS-1$
			Hashtable<String,Tree<PersistentObject>> hFall;
			@SuppressWarnings("unchecked") //$NON-NLS-1$
			@Override
			public void run(){
				hFall=new Hashtable<String,Tree<PersistentObject>>();
				IStructuredSelection sel=(IStructuredSelection)tv.getSelection();
				for(Object o:sel.toList()){
					if(o instanceof Tree){
						Tree<PersistentObject> t=(Tree)o;
						if(t.contents instanceof Patient){
							for(Tree<PersistentObject> f:t.getChildren()){
								hFall.put(f.contents.getId(), f);
							}
							
						}else if(t.contents instanceof Fall){
							hFall.put(t.contents.getId(), t);
						}else{
							Konsultation k=(Konsultation)t.contents;
							Fall fall=k.getFall();
							if(fall!=null){
								Tree<PersistentObject> tF=hFall.get(fall.getId());
								if(tF==null){
									tF=new Tree<PersistentObject>(null,fall);
								}
								Tree<PersistentObject> tK= tF.find(k, false);
								if(tK==null){
									tK=new Tree<PersistentObject>(tF,k);
								}
								hFall.put(fall.getId(), tF);
							}
							
						}
					}
				}
				for(Tree<PersistentObject> tF:hFall.values()){
					LinkedList<Konsultation> lKons=new LinkedList<Konsultation>();
					for(Tree<PersistentObject> tK:tF.getChildren()){
						lKons.add((Konsultation)tK.contents);
					}
					/*Result<Rechnung> res=*/Rechnung.build(lKons);
					// TODO error handling
				}
			}
				
				/*
				for(Tree tPat=tSelection.getFirstChild();tPat!=null;tPat=tPat.getNextSibling()){
					for(Tree tFall=tPat.getFirstChild();tFall!=null;tFall=tFall.getNextSibling()){
						Collection<Tree> lt=tFall.getChildren();
						ArrayList<Konsultation> lb=new ArrayList<Konsultation>(lt.size()+1);
						for(Tree t:lt){
							lb.add((Konsultation)t.contents);
						}
						Result res=Rechnung.build(lb);
						if(res.getSeverity()>Log.WARNINGS){
							Fall fall=(Fall)tFall.contents;
						
							ErrorDialog.openError(getViewSite().getShell(),"Fehler bei Rechnung",
									"Rechnung für den Fall "+fall.getLabel()+" Konnte nicht erstellt werden",res.asStatus(),0);
						}else{
							tPat.remove(tFall);
						}
					}
					tSelection.remove(tPat);
				}
				tvSel.refresh();
				*/
			
		};
	}
}
