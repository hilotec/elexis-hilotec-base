/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: PatHeuteView.java 3773 2008-04-16 11:55:20Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views;

import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.AbstractDataLoaderJob;
import ch.elexis.actions.BackgroundJob;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.BackgroundJob.BackgroundJobListener;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Brief;
import ch.elexis.data.Fall;
import ch.elexis.data.IVerrechenbar;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Verrechnet;
import ch.elexis.text.ITextPlugin;
import ch.elexis.text.TextContainer;
import ch.elexis.text.ITextPlugin.ICallback;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.DefaultContentProvider;
import ch.elexis.util.DefaultLabelProvider;
import ch.elexis.util.ListDisplay;
import ch.elexis.util.Money;
import ch.elexis.util.PersistentObjectDropTarget;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.SimpleWidgetProvider;
import ch.elexis.util.ViewMenus;
import ch.elexis.util.ViewerConfigurer;
import ch.elexis.views.codesystems.LeistungenView;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

import com.tiff.common.ui.datepicker.DatePickerCombo;

public class PatHeuteView extends ViewPart implements SelectionListener, ActivationListener, ISaveablePart2, BackgroundJobListener {
	public static final String ID="ch.elexis.PatHeuteView"; //$NON-NLS-1$
	static final String LEISTUNG_HINZU="Hinzu";
	static final String STAT_LEEREN="Leeren";
	private IAction printAction, reloadAction, filterAction, statAction;
	CommonViewer cv;
	ViewerConfigurer vc;
	FormToolkit tk=Desk.theToolkit;
	Form form;
	Text tPat,tTime,tMoney, tTime2, tMoney2;
	TimeTool datVon,datBis;
	boolean bOnlyOpen;
	private Konsultation[] kons;
	private final KonsLoader kload;
	private int numPat;
	private double sumTime;
	private double sumAll;
	PersistentObjectDropTarget dropTarget;
	ListDisplay<IVerrechenbar> ldFilter;
	//private double sumSelected;
	private final Query<Konsultation> qbe;
	Composite parent;
	
	public PatHeuteView() {
		super();
		datVon=new TimeTool();
		datBis=new TimeTool();
		qbe=new Query<Konsultation>(Konsultation.class);
		kload=new KonsLoader(qbe);
		kload.addListener(this);
	}

	@Override
	public void createPartControl(final Composite parent) {
		setPartName(Messages.getString("PatHeuteView.partName")); //$NON-NLS-1$
		parent.setLayout(new GridLayout());
		this.parent=parent;
		makeActions();
		ldFilter=new ListDisplay<IVerrechenbar>(parent,SWT.NONE,new ListDisplay.LDListener(){

			public String getLabel(final Object o) {
				return ((IVerrechenbar)o).getCode();
			}

			public void hyperlinkActivated(final String l) {
				if(l.equals(LEISTUNG_HINZU)){
					try{
						if(StringTool.isNothing(LeistungenView.ID)){
							SWTHelper.alert("Fehler", "LeistungenView.ID");
						}
						getViewSite().getPage().showView(LeistungenView.ID);
						GlobalEvents.getInstance().setCodeSelectorTarget(dropTarget);
					}catch(Exception ex){
						ExHandler.handle(ex);
					}
				}else if(l.equals(STAT_LEEREN)){
					ldFilter.clear();
				}
				
			}
			
		});
		ldFilter.addHyperlinks(LEISTUNG_HINZU,STAT_LEEREN);
		ldFilter.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		((GridData)ldFilter.getLayoutData()).heightHint=0;
		dropTarget=new PersistentObjectDropTarget("Statfilter",ldFilter,new DropReceiver());
		Composite top=new Composite(parent,SWT.BORDER);
		top.setLayout(new RowLayout());
		final DatePickerCombo dpc=new DatePickerCombo(top,SWT.BORDER);
		dpc.setDate(datVon.getTime());
		dpc.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(final SelectionEvent e) {
				datVon.setTimeInMillis(dpc.getDate().getTime());
			}
			
		});
		final DatePickerCombo dpb=new DatePickerCombo(top,SWT.BORDER);
		dpb.setDate(datBis.getTime());
		dpb.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(final SelectionEvent e) {
				datBis.setTimeInMillis(dpb.getDate().getTime());
			}
			
		});
		final Button bKonsType=new Button(top,SWT.TOGGLE);
		bKonsType.setText(Messages.getString("PatHeuteView.onlyOpen")); //$NON-NLS-1$
		bKonsType.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(final SelectionEvent e){
				bOnlyOpen=bKonsType.getSelection();
			}
		});
		//ldFilter.setVisible(false);
		/*
		ExpandableComposite ecFilter=tk.createExpandableComposite(parent,ExpandableComposite.TWISTIE);
		ecFilter.setText("Filter");

		ecFilter.setClient(ldFilter);
		ecFilter.addExpansionListener(new ExpansionAdapter(){

			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				form.layout();
			}
			
		});
		*/
		cv=new CommonViewer();
		vc=new ViewerConfigurer(
				new DefaultContentProvider(cv,Patient.class){
					@Override
					public Object[] getElements(final Object inputElement) {
						if(kons==null){
							kons=new Konsultation[0];
							kload.schedule();
						}
						return kons;
					}
				},
				new DefaultLabelProvider(){

					@Override
					public String getText(final Object element) {
						if(element instanceof Konsultation){
							Fall fall=((Konsultation)element).getFall();
							if(fall==null){
								return Messages.getString("PatHeuteView.noCase")+((Konsultation)element).getLabel(); //$NON-NLS-1$
							}
							Patient pat=fall.getPatient();
							return pat.getLabel();
						}
						return super.getText(element);
					}
					
				},
				null, //new ViewerConfigurer.EmptyControlfieldProvider(),// DefaultControlFieldProvider(cv,new String[]{"Name","Vorname"}),
				new ViewerConfigurer.DefaultButtonProvider(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LIST,SWT.V_SCROLL,cv)
		);
		cv.create(vc,parent,SWT.BORDER,getViewSite());
		//cv.getViewerWidget().getControl().setLayoutData(SWTHelper.getFillGridData(1,true,1,true));


		//Group grpAll=new Group(parent,SWT.BORDER);
		//grpAll.setText("Alle Patienten");
		//grpAll.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		form=tk.createForm(parent);
		form.setText("Alle");
		form.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		Composite bottom=form.getBody();
		bottom.setLayout(new GridLayout(2,false));
		tk.createLabel(bottom,"Patienten");
		tPat=tk.createText(bottom,"",SWT.BORDER); //$NON-NLS-1$
		tPat.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		tPat.setEditable(false);
		tk.createLabel(bottom,Messages.getString("PatHeuteView.accTime")); //$NON-NLS-1$
		tTime=tk.createText(bottom,"",SWT.BORDER); //$NON-NLS-1$
		tTime.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		tTime.setEditable(false);
		tk.createLabel(bottom,Messages.getString("PatHeuteView.accAmount")); //$NON-NLS-1$
		tMoney=tk.createText(bottom,"",SWT.BORDER); //$NON-NLS-1$
		tMoney.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		tMoney.setEditable(false);
		//Group grpSel=new Group(parent,SWT.BORDER);
		//grpSel.setText("Markierte");
		//grpSel.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		Form fSel=tk.createForm(parent);
		fSel.setText(Messages.getString("PatHeuteView.marked")); //$NON-NLS-1$
		fSel.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		Composite cSel=fSel.getBody();
		cSel.setLayout(new GridLayout(2,false));
		tk.createLabel(cSel,Messages.getString("PatHeuteView.accTime")); //$NON-NLS-1$
		tTime2=tk.createText(cSel,"",SWT.BORDER); //$NON-NLS-1$
		tTime2.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		tk.createLabel(cSel,Messages.getString("PatHeuteView.accAmount")); //$NON-NLS-1$
		tMoney2=tk.createText(cSel,"",SWT.BORDER); //$NON-NLS-1$
		tMoney2.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		tTime2.setEditable(false);
		tMoney2.setEditable(false);
		ViewMenus menus=new ViewMenus(getViewSite());
		
		menus.createMenu(printAction,reloadAction, statAction);
		menus.createToolbar(reloadAction,filterAction);
		
		//setFocus();
		cv.getConfigurer().getContentProvider().startListening();
		GlobalEvents.getInstance().addActivationListener(this,this);
		kload.schedule();

	}
	
	@Override
	public void dispose(){
		cv.getConfigurer().getContentProvider().stopListening();
		kload.removeListener(this);
		GlobalEvents.getInstance().removeActivationListener(this,this);
		
	}
	@Override
	public void setFocus() {
		cv.notify(CommonViewer.Message.update);
		
	}

	public void selectionEvent(final PersistentObject obj) {
		if(obj instanceof Konsultation){
			tTime2.setText(Integer.toString(((Konsultation)obj).getMinutes()));
			double m=((Konsultation)obj).getUmsatz();
			DecimalFormat df=new DecimalFormat("0.00");
     		tMoney2.setText(df.format(m/100.0));
     		final Patient pat=(Patient)GlobalEvents.getInstance().getSelectedObject(Patient.class);
     		final Patient bPat=((Konsultation)obj).getFall().getPatient();
     		if((pat==null) || (!pat.getId().equals(bPat.getId()))){

     			GlobalEvents.getInstance().fireSelectionEvent(bPat);
     			
     		}
		}
		
	}

	public void activation(final boolean mode) { /* leer */}

	public void visible(final boolean mode) {
		if(mode==true){
			GlobalEvents.getInstance().addSelectionListener(this);
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
		
	}

	public void clearEvent(final Class<? extends PersistentObject> template) {
		// TODO Auto-generated method stub
		
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
	
	class StatLoader extends Job{

		StatLoader(){
			super("Berechne Statistik");
			setPriority(Job.LONG);
			setUser(true);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			HashMap<IVerrechenbar, StatCounter> counter=new HashMap<IVerrechenbar, StatCounter>();
			monitor.beginTask("Berechne Statistik", kons.length+20);
			for(Konsultation k:kons){
				List<Verrechnet> list=k.getLeistungen();
				for(Verrechnet v:list){
					StatCounter sc=counter.get(v.getVerrechenbar());
					if(sc==null){
						sc=new StatCounter(v.getVerrechenbar());
						counter.put(v.getVerrechenbar(), sc);
					}
					sc.add(v.getZahl(),v.getEffPreis());
				}
				monitor.worked(1);
				if(monitor.isCanceled()){
					return Status.CANCEL_STATUS;
				}
			}
			final List<StatCounter> sums=new LinkedList<StatCounter>(counter.values());
			Collections.sort(sums);
			monitor.worked(20);
			monitor.done();
			Desk.theDisplay.asyncExec(new Runnable(){
				public void run() {
					FileDialog fd=new FileDialog(getSite().getShell(),SWT.SAVE);
					fd.setFilterExtensions(new String[]{"*.csv","*.*"});
					fd.setFilterNames(new String[]{"CSV","Alle Dateien"});
					fd.setFileName("elexis-stat.csv");
					String fname=fd.open();
					if(fd!=null){
						try{
							FileWriter fw=new FileWriter(fname);
							fw.write("Position;Text;Anzahl;Betrag\r\n");
							for(StatCounter st:sums){
								StringBuilder sb=new StringBuilder();
								sb.append(st.v.getCodeSystemName())
									.append(": ").append(st.v.getCode()).append(";")
									.append(st.v.getText()).append(";")
									.append(st.num).append(";").append(st.sum.getAmountAsString())
									.append("\r\n");
								fw.write(sb.toString());
							}
							fw.close();
						}catch(Exception ex){
							ExHandler.handle(ex);
							SWTHelper.showError("Fehler beim Schreiben", ex.getMessage());
						}
					}
					
				}
			});
			return Status.OK_STATUS;
		}
		
	}
	class KonsLoader extends AbstractDataLoaderJob{
		IVerrechenbar[] lfiltered;
		KonsLoader(final Query<Konsultation> qbe){
			super("Lade Konsultationen",qbe,new String[]{"Datum"});
			setPriority(Job.LONG);
			setUser(true);
		}
		
		@Override
		public IStatus execute(final IProgressMonitor monitor) {
			if(Hub.actUser==null){
				return Status.CANCEL_STATUS;
			}
			monitor.beginTask(Messages.getString("PatHeuteView.loadKons"), 1000); //$NON-NLS-1$
			qbe.clear();
			qbe.add("Datum",">=",datVon.toString(TimeTool.DATE_COMPACT)); //$NON-NLS-1$ //$NON-NLS-2$
			qbe.add("Datum", "<=", datBis.toString(TimeTool.DATE_COMPACT)); //$NON-NLS-1$ //$NON-NLS-2$
			if(Hub.acl.request(AccessControlDefaults.ACCOUNTING_GLOBAL)==false){
				if(Hub.actMandant==null){
					monitor.done();
					return Status.OK_STATUS;
				}
				qbe.add("MandantID", "=", Hub.actMandant.getId());
			}
			if(bOnlyOpen){
				qbe.add("RechnungsID", "", null); //$NON-NLS-1$ //$NON-NLS-2$
			}
			qbe.addPostQueryFilter(new IFilter(){
				public boolean select(final Object toTest) {
					if(filterAction.isChecked()){
						Konsultation k=(Konsultation)toTest;
						List<IVerrechenbar> lFilt=ldFilter.getAll();
						for(Verrechnet v:k.getLeistungen()){
							if(lFilt.contains(v.getVerrechenbar())){
								return true;
							}
						}
						return false;
					}
					return true;
				}
				
			});
			@SuppressWarnings("unchecked") 
		    List<Konsultation> list=qbe.execute();
		    monitor.worked(100);
		    numPat=0;
		    sumAll=0.0;
		    sumTime=0.0;
		    if(list==null){
		    	result=new Konsultation[0];
		    }else{
		    	Konsultation[] ret=new Konsultation[list.size()];
		    	if(filterAction.isChecked()){
		    		lfiltered=ldFilter.getAll().toArray(new IVerrechenbar[0]);
		    		if(lfiltered.length==0){
		    			lfiltered=null;
		    		}
		    	}else{
		    		lfiltered=null;
		    	}
	        	int i=0;
	        	for(PersistentObject o:list){
	        		ret[i++]=(Konsultation)o;
	        		if(lfiltered!=null){
	        			List<Verrechnet> lstg=((Konsultation)o).getLeistungen();
	    				for(Verrechnet v:lstg){
	    					int num=v.getZahl();
	    					Money preis=v.getEffPreis().multiply(num);
	    					for(int j=0;j<lfiltered.length;j++){
	    						if(lfiltered[j].equals(v.getVerrechenbar())){
	    							sumAll+=preis.getCents();
	    							sumTime+=v.getVerrechenbar().getMinutes();
	    						}
	    					}
	    				}
					}else{
		        		sumAll+=((Konsultation)o).getUmsatz();
		        		sumTime+=((Konsultation)o).getMinutes();
					}
	        		monitor.worked(1);
	        		if(monitor.isCanceled()){
	        			monitor.done();
	        			result=new Konsultation[0];
	        			return Status.CANCEL_STATUS;
	        		}
					
	        	 }
	     		numPat=ret.length;
	     		result=ret;
	     		monitor.done();
	         }
			return Status.OK_STATUS;
		}

		@Override
		public int getSize() {
			return 100;
		}
		
	}
	public void jobFinished(final BackgroundJob j) {
		if(j.isValid()){
			kons=(Konsultation[])j.getData();
			tPat.setText(Integer.toString(numPat));
     		tTime.setText(Double.toString(sumTime));
     		DecimalFormat df=new DecimalFormat("0.00");
     		tMoney.setText(df.format(sumAll/100.0));
     		cv.notify(CommonViewer.Message.update);
		}else{
			kons=new Konsultation[0];
		}
		
	};
	
	private static class StatCounter implements Comparable<StatCounter>{
		IVerrechenbar v;
		Money sum;
		int num;
		StatCounter(IVerrechenbar vv){
			v=vv;
			sum=new Money();
			num=0;
		}
		void add(int num, Money price){
			
			Money total=price.multiply(num);
			this.num+=num;
			sum.addMoney(total);
		}
		public int compareTo(StatCounter o) {
			int vgroup=v.getCodeSystemName().compareTo(o.v.getCodeSystemName());
			if(vgroup!=0){
				return vgroup;
			}
			int vCode=v.getCode().compareTo(o.v.getCode());
			if(vCode!=0){
				return vCode;
			}
			return sum.getCents()-o.sum.getCents();
		}
	}
	private void makeActions() {
		statAction=new Action("Statistik"){
			{
				setToolTipText("Einfache Statistik erstellen und exportieren");
			}
			@Override
			public void run(){
				StatLoader loader=new StatLoader();
				loader.schedule();
			}
		};
		printAction=new Action("Liste drucken"){
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_PRINT));
				setToolTipText("Detaillierten Abrechnungsbericht drucken");
			}
			@Override
			public void run(){
				TerminListeDialog tld=new TerminListeDialog(getViewSite().getShell());
				tld.open();
			}
		};
	
		reloadAction=new Action("Neu einlesen"){
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_REFRESH));
				setToolTipText("Liste neu einlesen");
			}
			@Override
			public void run(){
				kons=null;
				kload.schedule();
			}
		};
		
		filterAction=new Action("Filter",Action.AS_CHECK_BOX){
			{
				setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_FILTER));
				setToolTipText("Verrechnungspositionen zum Zählen eingeben");
			}
			@Override
			public void run(){
				GridData gd=(GridData)ldFilter.getLayoutData();
				if(filterAction.isChecked()){
					gd.heightHint=50;
					//gd.minimumHeight=15;
				}else{
					gd.heightHint=0;
				}
				parent.layout(true);
			}
			
		};
		
	}
	
	class TerminListeDialog extends TitleAreaDialog implements ICallback{
		IVerrechenbar[] lfiltered;
		int[] numLeistung;
		Money[]	perLeistung;
		
		public TerminListeDialog(final Shell shell) {
			super(shell);
		}

		@Override
		protected Control createDialogArea(final Composite parent) {
			Composite ret=new Composite(parent,SWT.NONE);
			TextContainer text=new TextContainer(getShell());
			ret.setLayout(new FillLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			text.getPlugin().createContainer(ret, this);
			text.getPlugin().showMenu(false);
			text.getPlugin().showToolbar(false);
			int add=2;
			if(filterAction.isChecked()){
				lfiltered=ldFilter.getAll().toArray(new IVerrechenbar[0]);
				numLeistung=new int[lfiltered.length];
				add+=lfiltered.length;
				perLeistung=new Money[lfiltered.length];
				for(int i=0;i<lfiltered.length;i++){
					perLeistung[i]=new Money();
				}
			}
			text.createFromTemplateName(null, "Abrechnungsliste", Brief.UNKNOWN, Hub.actUser, "Abrechnung");
			String[][] table=new String[kons.length+add][];
			table[0]=new String[2];
			table[0][0]="Konsultation";
			table[0][1]="Verrechnung";
			Money total=new Money();
			for(int i=0;i<kons.length;i++){
				table[i+1]=new String[2];
				Konsultation k=kons[i];
				table[i+1][0]=k.getFall().getPatient().getLabel()+"\n"+k.getLabel();
				StringBuilder sb=new StringBuilder();
				List<Verrechnet> lstg=k.getLeistungen();
				Money subsum=new Money();
				for(Verrechnet v:lstg){
					int num=v.getZahl();
					Money preis=v.getEffPreis().multiply(num);
					if(lfiltered!=null){
						for(int j=0;j<lfiltered.length;j++){
							if(lfiltered[j].equals(v.getVerrechenbar())){
								numLeistung[j]+=num;
								perLeistung[j].addMoney(preis);
							}
						}
					}
					subsum.addMoney(preis);
					sb.append(num).append(" ").append(v.getLabel()).append(" ")
						.append(preis.getAmountAsString())
						.append("\n");
				}
				sb.append("Total: ").append(subsum.getAmountAsString());
				total.addMoney(subsum);
				table[i+1][1]=sb.toString();
			}
			table[kons.length+1]=new String[2];
			table[kons.length+1][0]="Summe:";
			table[kons.length+1][1]=total.getAmountAsString();
			if(lfiltered!=null){
				for(int i=0;i<lfiltered.length;i++){
					table[kons.length+2+i]=new String[2];
					table[kons.length+2+i][0]=lfiltered[i].getCode();
					StringBuilder sb=new StringBuilder();
					sb.append("Verrechnet ").append(numLeistung[i])
						.append(" mal, =").append(perLeistung[i].getAmountAsString());
					table[kons.length+2+i][1]=sb.toString();
				}
			}
			text.getPlugin().setFont("Helvetica", SWT.NORMAL, 9);
			text.getPlugin().insertTable("[Liste]", ITextPlugin.FIRST_ROW_IS_HEADER, table, new int[]{30,70});
			return ret;
		}

		@Override
		public void create() {
			super.create();
			getShell().setText("Abrechnungsliste");
			setTitle("Abrechnung drucken");
			setMessage("Dies druckt alle Abrechnungsdaten der Konsultationen im eingestellten Zeitraum");
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
	private final class DropReceiver implements PersistentObjectDropTarget.Receiver {
		public void dropped(final PersistentObject o, final DropTargetEvent ev) {
			if(o instanceof IVerrechenbar){
				ldFilter.add((IVerrechenbar)o);
			 }
		}

		public boolean accept(final PersistentObject o) {
			if(o instanceof IVerrechenbar){
				return true;
			}
			return false;
		}
	}
}
