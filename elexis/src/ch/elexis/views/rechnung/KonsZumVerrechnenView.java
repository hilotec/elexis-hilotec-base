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
 *  $Id: KonsZumVerrechnenView.java 3844 2008-04-25 20:48:42Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views.rechnung;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.RestrictedAction;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Brief;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Rechnung;
import ch.elexis.dialogs.KonsZumVerrechnenWizardDialog;
import ch.elexis.preferences.Leistungscodes;
import ch.elexis.text.TextContainer;
import ch.elexis.text.ITextPlugin.ICallback;
import ch.elexis.util.BasicTreeContentProvider;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.LazyTree;
import ch.elexis.util.Result;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.SimpleWidgetProvider;
import ch.elexis.util.Tree;
import ch.elexis.util.ViewMenus;
import ch.elexis.util.ViewerConfigurer;
import ch.elexis.util.LazyTree.LazyTreeListener;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.JdbcLink.Stm;

import com.tiff.common.ui.datepicker.DatePickerCombo;

/**
 * Anzeige aller Behandlungen, für die noch keine Rechnung erstellt wurde. Die Behandlungen werden nach Patient und 
 * Fall gruppiert. Patienten, Fälle und Behandlungen können einzeln oder in Gruppen in eine Auswahl übertragen 
 * werden, aus der später Rechnungen erstellt werden können. 
 * @author Gerry
 *
 */
public class KonsZumVerrechnenView extends ViewPart implements ISaveablePart2{
    public static final String ID="ch.elexis.BehandlungenVerrechnenView"; //$NON-NLS-1$
    CommonViewer cv;
    ViewerConfigurer vc;
    TreeViewer tv;
    FormToolkit tk=Desk.theToolkit;
    Form left,right;
    FormText expl;
    @SuppressWarnings("unchecked")	LazyTree tAll;
    @SuppressWarnings("unchecked")	Tree tSelection;
    TreeViewer tvSel;
    Hyperlink hAction, hSelection;
    LazyTreeListener ltl;
    ViewMenus menu;
    private IAction billAction,printAction,clearAction,wizardAction,refreshAction, detailAction;
    private IAction removeAction;
    private IAction expandSelAction;
    private IAction expandSelAllAction;
    private IAction selectByDateAction;
    KonsZumVerrechnenView self;
    
    public KonsZumVerrechnenView() {
        cv=new CommonViewer();
        ltl=new RLazyTreeListener();
        tSelection=new Tree<PersistentObject>(null,null);
        tAll=new LazyTree<PersistentObject>(null,null,ltl);
        self=this;
    }
    
    @Override
    public void dispose()
    {
    	//GlobalEvents.getInstance().removeActivationListener(this,this);
    	super.dispose();
    }

    @Override
    public void createPartControl(final Composite parent)
    {
        vc=new ViewerConfigurer(new BasicTreeContentProvider(),
                new ViewerConfigurer.TreeLabelProvider() {
        			// extend the TreeLabelProvider by getImage()
        	
            		@SuppressWarnings("unchecked")
					@Override
					public Image getImage(final Object element) {
            			if (element instanceof Tree) {
            				Tree tree = (Tree) element;
            				PersistentObject po = (PersistentObject) tree.contents;
            				if(po instanceof Fall){
            					if(po.isValid()){
            						return Desk.theImageRegistry.get(Desk.IMG_OK);
            					}else{
            						return Desk.theImageRegistry.get(Desk.IMG_FEHLER);
            					}
            				}
            			}
            			return null;
            		}
        		},
                null, //new DefaultControlFieldProvider(cv, new String[]{"Datum","Name","Vorname","Geb. Dat"}),
                new ViewerConfigurer.DefaultButtonProvider(),
                new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_TREE,SWT.MULTI|SWT.V_SCROLL,cv)
                );
        SashForm sash=new SashForm(parent,SWT.NULL);
        left=tk.createForm(sash);
        Composite cLeft=left.getBody();
        left.setText(Messages.getString("KonsZumVerrechnenView.allOpenCons")); //$NON-NLS-1$
        cLeft.setLayout(new GridLayout());
        cv.create(vc,cLeft,SWT.NONE,tAll);
        cv.getViewerWidget().setSorter(new ViewerSorter(){

			@SuppressWarnings("unchecked")
			@Override
			public int compare(final Viewer viewer, final Object e1, final Object e2) {
				PersistentObject o1=(PersistentObject)((Tree)e1).contents;
				PersistentObject o2=(PersistentObject)((Tree)e2).contents;
				return o1.getLabel().compareTo(o2.getLabel());
			}
        	
        });
        right=tk.createForm(sash);
        Composite cRight=right.getBody();
        right.setText(Messages.getString("KonsZumVerrechnenView.selected")); //$NON-NLS-1$
        cRight.setLayout(new GridLayout());
         
        tvSel=new TreeViewer(cRight,SWT.V_SCROLL);
        //tvSel.getControl().setLayoutData(SWTHelper.getFillGridData(1,true,t,true));
        tvSel.getControl().setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
        tvSel.setContentProvider(new BasicTreeContentProvider());
        tvSel.setLabelProvider(new LabelProvider(){
            @SuppressWarnings("unchecked")
			@Override
			public String getText(final Object element)
            {
            	return ((PersistentObject)((Tree)element).contents).getLabel();
            }
            
        });
        tvSel.addDropSupport(DND.DROP_MOVE|DND.DROP_COPY,new Transfer[]{TextTransfer.getInstance()},new DropTargetAdapter(){

			@Override
			public void dragEnter(final DropTargetEvent event) {
					event.detail=DND.DROP_COPY;
			}

			@Override
			public void drop(final DropTargetEvent event) {
				String drp=(String)event.data;
                String[] dl=drp.split(","); //$NON-NLS-1$
                for(String obj:dl){
                    PersistentObject dropped=Hub.poFactory.createFromString(obj);
                	if(dropped instanceof Patient){
                		selectPatient((Patient)dropped, tAll, tSelection);
    				}else if(dropped instanceof Fall){
    					selectFall((Fall)dropped, tAll, tSelection);
    				}else if(dropped instanceof Konsultation){
    					selectBehandlung((Konsultation)dropped, tAll, tSelection);
    				}
    			
                }
                tvSel.refresh(true);
                
			}
        	
        });
        tvSel.addSelectionChangedListener(GlobalEvents.getInstance().getDefaultListener());
        tvSel.setInput(tSelection);
           //GlobalEvents.getInstance().addActivationListener(this,this);
        sash.setWeights(new int[]{60,40});
        makeActions();
        MenuManager selMenu=new MenuManager();
        selMenu.setRemoveAllWhenShown(true);
        selMenu.addMenuListener(new IMenuListener(){

			public void menuAboutToShow(final IMenuManager manager) {
				manager.add(removeAction);
				manager.add(expandSelAction);
				manager.add(expandSelAllAction);
				
			}
        	
        });
        tvSel.getControl().setMenu(selMenu.createContextMenu(tvSel.getControl()));
        menu=new ViewMenus(getViewSite());
        menu.createToolbar(billAction,printAction,clearAction,wizardAction,refreshAction);
        menu.createMenu(wizardAction, selectByDateAction);
        menu.createViewerContextMenu(cv.getViewerWidget(), detailAction);
    }
    @Override
    public void setFocus()
    {
        // TODO Auto-generated method stub

    }
    class RLazyTreeListener implements LazyTreeListener{
    	final LazyTreeListener self=this;
		@SuppressWarnings("unchecked") 
		public void fetchChildren(final LazyTree l) {
			PersistentObject cont=(PersistentObject)l.contents;
    		final Stm stm=PersistentObject.getConnection().getStatement();
    		if(cont==null){
				IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
				try {
					progressService.runInUI(
					      PlatformUI.getWorkbench().getProgressService(),
						      new IRunnableWithProgress() {
						         public void run(final IProgressMonitor monitor) {
						        	 monitor.beginTask(Messages.getString("KonsZumVerrechnenView.findCons"),100); //$NON-NLS-1$
						        	 monitor.subTask(Messages.getString("KonsZumVerrechnenView.databaseRequest")); //$NON-NLS-1$
						        	 String sql="SELECT distinct PATIENTID FROM FAELLE "+ //$NON-NLS-1$
							        	"JOIN BEHANDLUNGEN ON BEHANDLUNGEN.FALLID=FAELLE.ID WHERE BEHANDLUNGEN.deleted='0' AND BEHANDLUNGEN.RECHNUNGSID is null ";  //$NON-NLS-1$
						        	 	if(Hub.acl.request(AccessControlDefaults.ACCOUNTING_GLOBAL)==false){
						        	 		sql+="AND BEHANDLUNGEN.MANDANTID="+Hub.actMandant.getWrappedId();
						        	 	}
							    		ResultSet rs=stm.query(sql);
							    		monitor.worked(10);
							    		monitor.subTask(Messages.getString("KonsZumVerrechnenView.readIn")); //$NON-NLS-1$
										try {
											while((rs!=null) && rs.next()){
												String s=rs.getString(1);
												Patient p=Patient.load(s);
												if(p.exists() && (tSelection.find(p,false)==null)){
													new LazyTree(l,p,self);
												}
												monitor.worked(1);
											}
											monitor.done();
										} catch (SQLException e) {
											ExHandler.handle(e);
										}			        	 
						         }
					      },null);
					} catch (Throwable ex) {
						ExHandler.handle(ex);
					}
		        	
				}else{
					ResultSet rs=null;
		    		String sql;
		    		try{
						if(cont instanceof Patient){
							sql="SELECT distinct FAELLE.ID FROM FAELLE join BEHANDLUNGEN ON BEHANDLUNGEN.FALLID=FAELLE.ID "+ //$NON-NLS-1$
							"WHERE BEHANDLUNGEN.RECHNUNGSID is null AND BEHANDLUNGEN.DELETED='0' AND FAELLE.PATIENTID="+cont.getWrappedId(); //$NON-NLS-1$
							if(Hub.acl.request(AccessControlDefaults.ACCOUNTING_GLOBAL)==false){
								sql+=" AND BEHANDLUNGEN.MANDANTID="+Hub.actMandant.getWrappedId();
							}
				    		rs=stm.query(sql);
							while((rs!=null) && rs.next()){
								String s=rs.getString(1);
								Fall f=Fall.load(s);
								if(f.exists() && (tSelection.find(f,true)==null)){
									new LazyTree(l,f,this);
								}
							}
						}else if(cont instanceof Fall){
							sql="SELECT ID FROM BEHANDLUNGEN WHERE RECHNUNGSID is null AND deleted='0' AND FALLID="+cont.getWrappedId(); //$NON-NLS-1$
							if(Hub.acl.request(AccessControlDefaults.ACCOUNTING_GLOBAL)==false){
								sql+=" AND MANDANTID="+Hub.actMandant.getWrappedId();
							}
				    		rs=stm.query(sql);
							while((rs!=null) && rs.next()){
								String s=rs.getString(1);
								Konsultation b=Konsultation.load(s);
								if(b.exists() && (tSelection.find(b,true)==null)){
									new LazyTree(l,b,this);
								}
							}
						}
		    		if(rs!=null){
	    				rs.close();
	    			}
	    		} catch (Exception e) {
					ExHandler.handle(e);
				}finally{
					PersistentObject.getConnection().releaseStatement(stm);
				}
			}
		}

		@SuppressWarnings("unchecked")
		public boolean hasChildren(final LazyTree l) {
			Object po=l.contents;
			if(po instanceof Konsultation){
				return false;
			}
			return true;
		}
    	
    }
    
   
    public void selectKonsultation(final Konsultation k){
    	selectBehandlung(k,tAll,tSelection);
    }
     /**
      * Patienten in von tAll nach tSelection verschieben bzw. falls
      * noch nicht vorhanden, neu anlegen.
      */
	@SuppressWarnings("unchecked") 
	private Tree selectPatient(final Patient pat, final Tree tSource, final Tree tDest) {
		Tree pSource=tSource.find(pat,false);
		Tree pDest=tDest.find(pat,false);
	    if(pDest==null){
	    	if(pSource==null){
	    		pDest=tDest.add(pat);
	    	}else{
	    		pDest=pSource.move(tDest);
	    	}
	    }else{
	    	if(pSource!=null){
	    		List<Tree> fs=(List<Tree>)pSource.getChildren();
	    		for(Tree t:fs){
	    			selectFall((Fall)t.contents, tSource, tDest);
	    		}
	    	}
	    }
	    cv.getViewerWidget().refresh(tSource);
	    return pDest;
	}

	@SuppressWarnings("unchecked") 
	private Tree selectFall(final Fall f, final Tree tSource, final Tree tDest) {
		Patient pat=f.getPatient();
		Tree tPat=tDest.find(pat,false);
		if(tPat==null){
			tPat=tDest.add(pat);
		}
		Tree tFall=tSource.find(f,true);
		if(tFall==null){
			tFall=tPat.add(f);
		}else{
			Tree tOld=tFall.getParent();
			tPat.merge(tFall);
			if(tOld.getFirstChild()==null){
				tSource.remove(tOld);
			}
			cv.getViewerWidget().refresh(tOld);
		}
		return tFall;
	}

	@SuppressWarnings("unchecked") 
	private Tree selectBehandlung(final Konsultation bh, final Tree tSource, final Tree tDest){
		Fall f=bh.getFall();
		Patient pat=f.getPatient();
		Tree tPat=tDest.find(pat,false);
		if(tPat==null){
			tPat=tDest.add(pat);
		}
		Tree tFall=tPat.find(f,false);
		if(tFall==null){
			tFall=tPat.add(f);
		}
		Tree tBeh=tFall.find(bh,false);
		if(tBeh==null){
			tBeh = tFall.add(bh);
		}
		
		Tree tps=tSource.find(pat,false);
		if(tps!=null){
			Tree tfs=tps.find(f,false);
			if(tfs!=null){
				Tree tbs=tfs.find(bh,false);
				if(tbs!=null){
					tfs.remove(tbs);
					cv.getViewerWidget().refresh(tfs);
				}
				if(tfs.hasChildren()==false){
					tps.remove(tfs);
					cv.getViewerWidget().refresh(tps);
				}
			}
			if(tps.hasChildren()==false){
				tSource.remove(tps);
				cv.getViewerWidget().refresh(tSource);
			}
		}
		return tBeh;
	}

	private void makeActions(){
		billAction=new Action(Messages.getString("KonsZumVerrechnenView.createInvoices")){ //$NON-NLS-1$
			{
				setImageDescriptor(Hub.getImageDescriptor("rsc/rechnung.gif")); //$NON-NLS-1$
				setToolTipText(Messages.getString("KonsZumVerrechnenView.createInvoices")); //$NON-NLS-1$
			}
			@SuppressWarnings("unchecked") 
			@Override
			public void run(){
				int rejected=0;
				if(((StructuredSelection)tvSel.getSelection()).size()>0){
					if(!SWTHelper.askYesNo("Dies erstellt alle Rechnungen in der Auswahl", "Wollen Sie wirklich aus allen Konsultationen im rechten Feld Rechnungen erstellen?")){
						return;
					}
				}
				for(Tree tPat=tSelection.getFirstChild();tPat!=null;tPat=tPat.getNextSibling()){
					for(Tree tFall=tPat.getFirstChild();tFall!=null;tFall=tFall.getNextSibling()){
						Fall fall=(Fall)tFall.contents;
						if(Hub.userCfg.get(Leistungscodes.BILLING_STRICT, true)){
							if(!fall.isValid()){
								rejected++;
								continue;
							}
						}
						Collection<Tree> lt=tFall.getChildren();
						ArrayList<Konsultation> lb=new ArrayList<Konsultation>(lt.size()+1);
						for(Tree t:lt){
							lb.add((Konsultation)t.contents);
						}
						Result<Rechnung> res=Rechnung.build(lb);
						if(!res.isOK()){
							ErrorDialog.openError(getViewSite().getShell(),Messages.getString("KonsZumVerrechnenView.errorInInvoice"), //$NON-NLS-1$
									Messages.getString("KonsZumVerrechnenView.invoiceForCase", new Object[] {fall.getLabel()}),res.asStatus()); //$NON-NLS-1$
						}else{
							tPat.remove(tFall);
						}
					}
					if(rejected!=0){
						SWTHelper.showError("Fehlerhafte Falldefinitionen", Integer.toString(rejected)+
								" Rechnungen wurden nicht erstellt, weil die Fälle nicht alle notwendigen Angaben enthalten. " +
								"Bitte kontrollieren Sie die Fall-Details");
					}else{
						tSelection.remove(tPat);
					}
				}
				tvSel.refresh();
			}
		};
		clearAction=new Action(Messages.getString("KonsZumVerrechnenView.clearSelection")){ //$NON-NLS-1$
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor("delete")); //$NON-NLS-1$
				setToolTipText(Messages.getString("KonsZumVerrechnenView.deleteList")); //$NON-NLS-1$

			}
			@Override
			public void run(){
				tSelection.clear();
				tvSel.refresh();
			}
		};
		refreshAction=new Action(Messages.getString("KonsZumVerrechnenView.reloadAction")){ //$NON-NLS-1$
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_REFRESH));
				setToolTipText(Messages.getString("KonsZumVerrechnenView.reloadToolTip")); //$NON-NLS-1$
			}
			@Override
			public void run(){
				tAll.clear();
				cv.notify(CommonViewer.Message.update);
				tvSel.refresh(true);
			}
		};
		wizardAction=new Action(Messages.getString("KonsZumVerrechnenView.autoAction")){ //$NON-NLS-1$
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_WIZARD));
				setToolTipText(Messages.getString("KonsZumVerrechnenView.autoToolTip")); //$NON-NLS-1$
			}
			@Override
			public void run(){
				KonsZumVerrechnenWizardDialog kzvd=new KonsZumVerrechnenWizardDialog(getViewSite().getShell());
				if(kzvd.open()==Dialog.OK){
					IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
					try {
						progressService.runInUI(
								PlatformUI.getWorkbench().getProgressService(),
								new Rechnungslauf(self, kzvd.ttFirstBefore,kzvd.ttLastBefore,kzvd.mAmount,kzvd.bQuartal),
							    null);
					} catch (Throwable ex) {
						ExHandler.handle(ex);
					}	
					tvSel.refresh();
					cv.notify(CommonViewer.Message.update);
				}
			}
		};
		printAction=new Action(Messages.getString("KonsZumVerrechnenView.printSelection")){ //$NON-NLS-1$
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_PRINT)); 
				setToolTipText(Messages.getString("KonsZumVerrechnenView.printToolTip")); //$NON-NLS-1$
			}
			@Override
			public void run(){
				new SelectionPrintDialog(getViewSite().getShell()).open();
				
			}
		};
		removeAction=new Action(Messages.getString("KonsZumVerrechnenView.removeFromSelection")){ //$NON-NLS-1$
			@SuppressWarnings("unchecked")
			@Override
			public void run(){
				IStructuredSelection sel=(IStructuredSelection)tvSel.getSelection();
				if(!sel.isEmpty()){
					for(Object o:sel.toList()){
						if(o instanceof Tree){
							Tree t=(Tree)o;
							if(t.contents instanceof Patient){
								selectPatient((Patient)t.contents, tSelection, tAll); 
							}else if(t.contents instanceof Fall){
								selectFall((Fall)t.contents, tSelection, tAll);
							}else if(t.contents instanceof Konsultation){
								selectBehandlung((Konsultation)t.contents, tSelection, tAll);
							}
						}
					}
					tvSel.refresh();
					cv.notify(CommonViewer.Message.update);
				}
			}
		};
		
		// expand action for tvSel
		expandSelAction = new Action(Messages.getString("KonsZumVerrechnenView.expand")){ //$NON-NLS-1$
			@SuppressWarnings("unchecked")
			@Override
			public void run(){
				IStructuredSelection sel=(IStructuredSelection)tvSel.getSelection();
				if(!sel.isEmpty()){
					for(Object o:sel.toList()){
						if(o instanceof Tree){
							Tree t=(Tree)o;
							tvSel.expandToLevel(t, TreeViewer.ALL_LEVELS);
						}
					}
				}
			}
		};
		// expandAll action for tvSel
		expandSelAllAction = new Action(Messages.getString("KonsZumVerrechnenView.expandAll")){ //$NON-NLS-1$
			@Override
			public void run(){
				tvSel.expandAll();
			}
		};
		
		selectByDateAction=new Action(Messages.getString("KonsZumVerrechnenView.selectByDateAction")){ //$NON-NLS-1$
			TimeTool fromDate;
			TimeTool toDate;
			
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_WIZARD));
				setToolTipText(Messages.getString("KonsZumVerrechnenView.selectByDateActionToolTip")); //$NON-NLS-1$
			}
			@Override
			public void run(){
				// select date
				SelectDateDialog dialog = new SelectDateDialog(getViewSite().getShell());
				if (dialog.open() == TitleAreaDialog.OK) {
					fromDate = dialog.getFromDate();
					toDate = dialog.getToDate();
					
					IProgressService progressService = PlatformUI
							.getWorkbench().getProgressService();
					try {
						progressService.runInUI(PlatformUI.getWorkbench()
								.getProgressService(),
								new IRunnableWithProgress() {
									public void run(final IProgressMonitor monitor) {
										doSelectByDate(monitor, fromDate, toDate);
									}
								}, null);
					} catch (Throwable ex) {
						ExHandler.handle(ex);
					}
					tvSel.refresh();
					cv.notify(CommonViewer.Message.update);
				}
			}
			
		};
		detailAction=new RestrictedAction(AccessControlDefaults.LSTG_VERRECHNEN,"Abrechnungsdetails"){
			@SuppressWarnings("unchecked")
			@Override
			public void doRun(){
				Object[] sel=cv.getSelection();
				if((sel!=null) && (sel.length>0)){
					new VerrDetailDialog(getViewSite().getShell(),(Tree)sel[0]).open();
				}
			}
		};
	}
	
	/**
	 * Automatische Auwahl der Konsultationen, die verrechnet werden sollen
	 * Regel 1: Wer weniger als zwei Konsultationen hat, wird dann verrechnet, wenn
	 * die letzte Konsultation mehr als einen Monat her ist
	 * Regel 2: Wer mehr als zwei Konsultationen hat, bekommt eine Rechnung über alle
	 * Konsultationen des vergangenen Quartals
	 
	@SuppressWarnings("unchecked") 
	private void doSelect(final IProgressMonitor monitor){
		//Letzte Quartalsgrenze finden
		TimeTool limitQuartal=new TimeTool();;
		limitQuartal.set(TimeTool.DAY_OF_MONTH,1);
		TimeTool limitMonat;
		TimeTool act=new TimeTool();
		TimeTool dat=new TimeTool();
		limitMonat=new TimeTool(act);
		limitMonat.add(TimeTool.MONTH, -1);
		limitMonat.set(TimeTool.DAY_OF_MONTH, 1);
		String heute=act.toString(TimeTool.DATE_COMPACT).substring(4);
		
		if(heute.compareTo("0930")>0){ //$NON-NLS-1$
			limitQuartal.set(TimeTool.MONTH,9);	// 1.10.
		}else if(heute.compareTo("0630")>0){ //$NON-NLS-1$
			limitQuartal.set(TimeTool.MONTH,6);
		}else if(heute.compareTo("0331")>0){ //$NON-NLS-1$
			limitQuartal.set(TimeTool.MONTH,3);
		}else{
			limitQuartal.set(TimeTool.MONTH,1);
		}
		List<Tree> lAll=(List<Tree>)tAll.getChildren();
		monitor.beginTask(Messages.getString("KonsZumVerrechnenView.analyzeCons"), lAll.size()+1); //$NON-NLS-1$
		for(Tree tP:lAll){
			monitor.worked(1);
			for(Tree tF:(List<Tree>)tP.getChildren()){
				List<Tree> tK=(List<Tree>)tF.getChildren();
				if(tK.size()<3){
					boolean isLater=false;
					for(Tree tk:tK){
						Konsultation k=(Konsultation)tk.contents;
						dat.set(k.getDatum());
						if(dat.isAfter(limitMonat)){
							isLater=true;
							break;
						}
					}
					if(isLater==false){		// Weniger als 3 Kons, alle mehr als einen Monat her -> auswahl.
						for(Tree tk:tK){
							selectBehandlung((Konsultation)tk.contents, tAll, tSelection);
						}
					}
				}else{						// Mehr als 3 Kons
					for(Tree tk:tK){
						Konsultation k=(Konsultation)tk.contents;
						dat.set(k.getDatum());
						if(dat.isBefore(limitQuartal)){
							selectBehandlung(k, tAll, tSelection);
						}
					}
				}
				if(monitor.isCanceled()){
					monitor.done();
					return;
				}
			}
		}
		monitor.done();
	}
	*/
	/**
	 * Auwahl der Konsultationen, die verrechnet werden sollen, nach Datum.
	 * Es erscheint ein Dialog, wo man den gewünschten Bereich wählen kann.
	 */
	@SuppressWarnings("unchecked") 
	private void doSelectByDate(final IProgressMonitor monitor, final TimeTool fromDate, final TimeTool toDate){
		TimeTool actDate = new TimeTool();

		// set dates to midnight
		TimeTool date1 = new TimeTool(fromDate);
		TimeTool date2 = new TimeTool(toDate);
		date1.chop(3);
		date2.add(TimeTool.DAY_OF_MONTH, 1);
		date2.chop(3);
		
		List<Tree> lAll=(List<Tree>)tAll.getChildren();
		monitor.beginTask(Messages.getString("KonsZumVerrechnenView.selectByDateTask"), lAll.size()+1); //$NON-NLS-1$
		for(Tree tP:lAll){
			monitor.worked(1);
			for(Tree tF:(List<Tree>)tP.getChildren()){
				List<Tree> tK=(List<Tree>)tF.getChildren();
					for(Tree tk:tK){
						Konsultation k=(Konsultation)tk.contents;
						actDate.set(k.getDatum());
						if (actDate.isAfterOrEqual(date1) && actDate.isBefore(date2)) {
							selectBehandlung((Konsultation)tk.contents, tAll, tSelection);
						}
					}
				if(monitor.isCanceled()){
					monitor.done();
					return;
				}
			}
		}
		monitor.done();
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
	
	
	/**
	 * SelectDateDialog
	 * 
	 * @author danlutz
	 */
	public class SelectDateDialog extends TitleAreaDialog {
		DatePickerCombo dpFromDate;
		DatePickerCombo dpToDate;
		
		TimeTool fromDate = null;
		TimeTool toDate = null;

		public SelectDateDialog(final Shell parentShell) {
			super(parentShell);
		}

		@Override
		public void create(){
			super.create();
			setTitle(Messages.getString("SelectDateDialog.choosePeriodTitle")); //$NON-NLS-1$
			setMessage(Messages.getString("SelectDateDialog.choosePeriodMessage")); //$NON-NLS-1$
			getShell().setText(Messages.getString("SelectDateDialog.description")); //$NON-NLS-1$
		}
		
		@Override
		protected Control createDialogArea(final Composite parent) {
			Composite com=new Composite(parent,SWT.NONE);
			com.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
			com.setLayout(new GridLayout(2,false));

			new Label(com,SWT.NONE).setText(Messages.getString("SelectDateDialog.from")); //$NON-NLS-1$
			new Label(com, SWT.NONE).setText(Messages.getString("SelectDateDialog.to")); //$NON-NLS-1$
			
			dpFromDate = new DatePickerCombo(com, SWT.NONE);
			dpToDate = new DatePickerCombo(com, SWT.NONE);
			
			return com;
		}

		/* (Kein Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
		 */
		@Override
		protected void okPressed() {
			Date date=dpFromDate.getDate();
			if(date==null){
				fromDate=new TimeTool(TimeTool.BEGINNING_OF_UNIX_EPOCH);
			}else{
				fromDate = new TimeTool(date.getTime());
			}
			date=dpToDate.getDate();
			if(date==null){
				toDate=new TimeTool(TimeTool.END_OF_UNIX_EPOCH);
			}else{
				toDate = new TimeTool(date.getTime());
			}
			super.okPressed();
		}
		
		public TimeTool getFromDate(){
			return fromDate;
		}
		
		public TimeTool getToDate() {
			return toDate;
		}

	}
	class SelectionPrintDialog extends TitleAreaDialog implements ICallback{
		
		public SelectionPrintDialog(final Shell shell) {
			super(shell);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Control createDialogArea(final Composite parent) {
			Composite ret=new Composite(parent,SWT.NONE);
			TextContainer text=new TextContainer(getShell());
			ret.setLayout(new FillLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			text.getPlugin().createContainer(ret, this);
			text.getPlugin().showMenu(false);
			text.getPlugin().showToolbar(false);
			text.createFromTemplateName(null, "Liste", Brief.UNKNOWN, Hub.actUser, "Rechnungen");
			Tree[] all=(Tree[])tSelection.getChildren().toArray(new Tree[0]);
			String[][] table=new String[all.length][];
			
			for(int i=0;i<all.length;i++){
				table[i]=new String[2];
				Tree tr=all[i];
				if(tr.contents instanceof Konsultation){
					tr=tr.getParent();
				}
				if(tr.contents instanceof Fall){
					tr=tr.getParent();
				}
				Patient p=(Patient)tr.contents;
				StringBuilder sb=new StringBuilder();
				sb.append(p.getLabel());
				for(Tree tFall:(Tree[])tr.getChildren().toArray(new Tree[0])){
					Fall fall=(Fall)tFall.contents;
					sb.append("\n -- Fall: ").append(fall.getLabel());
					for(Tree tRn:(Tree[])tFall.getChildren().toArray(new Tree[0])){
						Konsultation k=(Konsultation)tRn.contents;
						sb.append("\n -- -- Kons: ").append(k.getLabel());
					}
				}
				table[i][0]=sb.toString();
			}
			text.getPlugin().setFont("Helvetica", SWT.NORMAL, 9);
			text.getPlugin().insertTable("[Liste]", 0, table, new int[]{90,10});
			return ret;
		}

		@Override
		public void create() {
			super.create();
			getShell().setText("Rechnungsliste");
			setTitle("Liste drucken");
			setMessage("Dies druckt alle aufgelisteten Patienten");
			getShell().setSize(900,700);
			SWTHelper.center(Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getShell(), getShell());
		}

		@Override
		protected void okPressed() {
			super.okPressed();
		}

		public void save() {
			// TODO Auto-generated method stub
			
		}

		public boolean saveAs() {
			// TODO Auto-generated method stub
			return false;
		}
		
		
	}
}
