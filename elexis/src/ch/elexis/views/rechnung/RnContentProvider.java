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
 * $Id: RnContentProvider.java 2276 2007-04-19 20:07:18Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views.rechnung;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import ch.elexis.Hub;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.Log;
import ch.elexis.util.Money;
import ch.elexis.util.Tree;
import ch.elexis.util.ViewerConfigurer;
import ch.elexis.util.ViewerConfigurer.ControlFieldListener;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;

/**
 * Contentprovider used in "RechnungsListeView" to display bills selected by some criteria
 * @author gerry
 *
 */
class RnContentProvider implements ViewerConfigurer.CommonContentProvider, ITreeContentProvider, ControlFieldListener{
	private static final float PREVAL=50000f;
	private Query<Rechnung> q1;
	CommonViewer cv;
	Tree[] result;
	int iPat, iRn;
	Money mAmount;
	TreeComparator treeComparator=new TreeComparator();
	PatientComparator patientComparator=new PatientComparator();
	RechnungsListeView rlv;
	
	private Log log=Log.get("Rechnungenlader");
	
	RnContentProvider(RechnungsListeView l, CommonViewer cv){
		this.cv=cv;
		rlv=l;
	}
	
	public void startListening() {
		cv.getConfigurer().getControlFieldProvider().addChangeListener(this);
	}

	public void stopListening() {
		cv.getConfigurer().getControlFieldProvider().removeChangeListener(this);
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public Object[] getElements(Object inputElement) {
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.runInUI(
		      PlatformUI.getWorkbench().getProgressService(),
			      new IRunnableWithProgress() {
			         public void run(IProgressMonitor monitor) {
			        	 reload(monitor);
			         }
		      },null);
		} catch (Throwable ex) {
			ExHandler.handle(ex);
		}
		
		return result;
	}

	public void dispose() {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Automatisch erstellter Methoden-Stub
		
	}

	// Vom ControlFieldListener
	public void changed(String[] fields, String[] values) {
		cv.notify(CommonViewer.Message.update);
	}

	public void reorder(String field) {
		cv.getViewerWidget().setSorter(new ViewerSorter(){

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				TimeTool t1=getLatest((Tree)e1);
				TimeTool t2=getLatest((Tree)e2);
				return t1.compareTo(t2);
			}
			
		});
	}

	public void selected() {
		// nothing to do
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private TimeTool getLatest(Tree t){
		if(t.contents instanceof Rechnung){
			return new TimeTool(((Rechnung)t.contents).getDatumRn());
		}else if(t.contents instanceof Fall){
			return getLatestFromCase(t);
		}else if(t.contents instanceof Patient){
			Tree runner=t.getFirstChild();
			TimeTool latest=new TimeTool();
			while(runner!=null){
				TimeTool lff=getLatestFromCase(runner);
				if(lff.isBefore(latest)){
					latest.set(lff);
				}
				runner=runner.getNextSibling();
			}
			return latest;
		}
		return null;
	}
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private TimeTool getLatestFromCase(Tree c){
		List<Tree> tRn=(List<Tree>)c.getChildren();
		TimeTool tL=new TimeTool();
		for(Tree t:tRn){
			Rechnung rn=(Rechnung)t.contents;
			TimeTool ttR=new TimeTool(rn.getDatumRn());
			if(ttR.isBefore(tL)){
				tL.set(ttR);
			}
		}
		return tL;
	}
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof Tree){
			 Tree[] ret=(Tree[])((Tree)parentElement).getChildren().toArray(new Tree[0]);
			 Arrays.sort(ret,treeComparator);
			 return ret;
		}
		return new Object[0];
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public Object getParent(Object element) {
		if(element instanceof Tree){
			return ((Tree)element).getParent();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if(element instanceof Tree){
			if(((Tree)element).contents instanceof Rechnung){
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public void reload(IProgressMonitor monitor){
		monitor.beginTask(Messages.getString("RnContentProvider.collectInvoices"),Math.round(PREVAL)); //$NON-NLS-1$
		monitor.subTask(Messages.getString("RnContentProvider.prepare")); //$NON-NLS-1$
		Tree<Patient> root=new Tree<Patient>(null,null);
		Hashtable<String,Tree<Patient>> hPats=new Hashtable<String,Tree<Patient>>(367,0.75f);
		Hashtable<String,Tree<Fall>> hFaelle=new Hashtable<String,Tree<Fall>> (719,0.75f);
		
		final String[] val=cv.getConfigurer().getControlFieldProvider().getValues();
		q1=new Query<Rechnung>(Rechnung.class);
		if(Hub.acl.request(AccessControlDefaults.ACCOUNTING_GLOBAL)==false){
			q1.add("MandantID", "=", Hub.actMandant.getId());
		}
		if(Integer.parseInt(val[0])==RnStatus.ZU_DRUCKEN){
			q1.startGroup();
			q1.add("RnStatus", "=", Integer.toString(RnStatus.OFFEN));
			q1.or();
			q1.add("RnStatus","=", Integer.toString(RnStatus.MAHNUNG_1));
			q1.add("RnStatus", "=", Integer.toString(RnStatus.MAHNUNG_2));
			q1.add("RnStatus", "=", Integer.toString(RnStatus.MAHNUNG_3));
			q1.endGroup();
			q1.and();
		}else if(!val[0].equals("0")){
			q1.add("RnStatus","=",val[0]);  //$NON-NLS-1$
		}
		String datemode="RnDatum";
		RnControlFieldProvider rcfp=(RnControlFieldProvider) cv.getConfigurer().getControlFieldProvider();
		if(rcfp.getDateModeIsStatus()){
			datemode="StatusDatum";
		}
		if(val[1]!=null){
			q1.add(datemode,">=",val[1]); //$NON-NLS-1$
		}
		if(val[2]!=null){
			q1.add(datemode,"<=",val[2]); //$NON-NLS-1$
		}
		if(val[3]!=null){
			q1.add("RnNummer","LIKE", "%"+val[3]+"%"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if(val[4]!=null){
			Patient act=Patient.load(val[4]);
			if(act.exists()){
				Fall[] faelle=act.getFaelle();
				if((faelle!=null) && (faelle.length>0)){
					q1.startGroup();
					q1.insertFalse();
					q1.or();
					for(Fall fall:faelle){
						if(fall.isOpen()){
							q1.add("FallID", "=", fall.getId());
						}
					}
					q1.endGroup();
				}
			}
		}
		List<Rechnung> rechnungen=q1.execute();
		if(rechnungen==null){
			log.log("Fehler bei der Abfrage der Rechnungen", Log.ERRORS);
			return;
		}
		monitor.worked(100);
		monitor.subTask(Messages.getString("RnContentProvider.databseRequest")); //$NON-NLS-1$
		int multiplyer=Math.round(PREVAL/rechnungen.size());
		monitor.subTask(Messages.getString("RnContentProvider.load")); //$NON-NLS-1$
		iPat=0;
		iRn=rechnungen.size();
		mAmount=new Money();
		
		for(Rechnung rn:rechnungen){
			if(rn==null || (!rn.exists())){
				log.log("Fehlerhafte Rechnung", Log.ERRORS);
				continue;
			}
			mAmount.addMoney(rn.getOffenerBetrag());
			Fall fall=rn.getFall();
			if(fall==null){
				log.log("Rechnung "+rn.getId()+" hat keinen Fall", Log.WARNINGS);
				continue;
			}
			Tree<Fall> tFall=hFaelle.get(fall.getId());
			if(tFall==null){
				Patient pat=fall.getPatient();
				if(pat==null){
					log.log("Fall "+fall.getId()+" hat keinen Patienten", Log.WARNINGS);
					continue;
				}
				Tree<Patient> tPat=hPats.get(pat.getId());
				if(tPat==null){
					tPat=new Tree<Patient>(root,pat,patientComparator);
					hPats.put(pat.getId(),tPat);
					iPat++;
				}
				tFall=new Tree(tPat,fall);
				hFaelle.put(fall.getId(), tFall);
			}
			Tree<Rechnung> tRn=new Tree(tFall,rn);
			monitor.worked(multiplyer);
		}
	
		if(rlv.tPat!=null){
			rlv.tPat.setText(Integer.toString(iPat));
			rlv.tRn.setText(Integer.toString(iRn));
			rlv.tSum.setText(mAmount.getAmountAsString());
		}
		monitor.worked(1);
		monitor.subTask(Messages.getString("RnContentProvider.prepareSort")); //$NON-NLS-1$
		result=(Tree[])root.getChildren().toArray(new Tree[0]);
		monitor.worked(100);
		//monitor.subTask(Messages.getString("RnContentProvider.sort")); //$NON-NLS-1$
		//Arrays.sort(result,treeComparator);
		monitor.done();
		
	}
	private final class PatientComparator implements Comparator{
		public int compare(Object o1, Object o2){
			Patient p1=(Patient) o1;
			Patient p2=(Patient) o2;
			return p1.getLabel().compareTo(p2.getLabel());
		}
	}
	private final class TreeComparator implements Comparator {
		TimeTool tt0=new TimeTool();
		TimeTool tt1=new TimeTool();
	
		public int compare(Object arg0, Object arg1) {
			Tree t0=(Tree)arg0;
			Tree t1=(Tree)arg1;
			if(t0.contents instanceof Patient){
				Patient p0=(Patient)t0.contents;
				Patient p1=(Patient)t1.contents;
				String s0=p0.getLabel();
				String s1=p1.getLabel();
				return s0.compareTo(s1);	
			}else if(t0.contents instanceof Fall){
				Fall f0=(Fall)t0.contents;
				Fall f1=(Fall)t1.contents;
				tt0.set(f0.getBeginnDatum());
				tt1.set(f1.getBeginnDatum());
				return tt0.secondsTo(tt1);
			}else if(t0.contents instanceof Konsultation){
				Konsultation b0=(Konsultation)t0.contents;
				Konsultation b1=(Konsultation)t1.contents;
				tt0.set(b0.getDatum());
				tt1.set(b1.getDatum());
				return tt0.secondsTo(tt1);
			}else{
				return 0;
			}
			
		}
	}

}