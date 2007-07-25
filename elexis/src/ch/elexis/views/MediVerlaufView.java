/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: MediVerlaufView.java 2908 2007-07-25 11:51:02Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;



import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredContentProvider;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;

import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Query;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;

/**
 * A view to display all medikaments administered or prescribed.
 * @author Gerry
 *
 */
public class MediVerlaufView extends ViewPart implements SelectionListener, ActivationListener{
	TableViewer tv;
	MediAbgabe[] mListe;
	private static final String[] columns={"Von","Bis","Medikament","Dosierung"};
	private static final int[] colwidth={90,90,300,200};
	int sortCol=0;
	MediSorter sorter=new MediSorter();
	public MediVerlaufView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new FillLayout());
		tv=new TableViewer(parent,SWT.NONE);
		Table table=tv.getTable();
		for(int i=0;i<columns.length;i++){
			TableColumn tc=new TableColumn(table,SWT.NONE);
			tc.setText(columns[i]);
			tc.setWidth(colwidth[i]);
			tc.setData(i);
			tc.addSelectionListener(new SelectionAdapter(){

				@Override
				public void widgetSelected(final SelectionEvent e) {
					int i=(Integer)((TableColumn)e.getSource()).getData();
					sortCol=i;
					reload();
				}
				
			});
		}
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tv.setUseHashlookup(true);
		tv.setContentProvider(new MediVerlaufContentProvider());
		tv.setLabelProvider(new MediVerlaufLabelProvider());
		GlobalEvents.getInstance().addActivationListener(this, getViewSite().getPart());
		tv.setSorter(sorter);
		tv.setInput(getViewSite());
	}

	@Override
	public void dispose(){
		GlobalEvents.getInstance().removeActivationListener(this, getViewSite().getPart());
	}
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	class MediVerlaufContentProvider implements IStructuredContentProvider{

		public Object[] getElements(final Object inputElement) {
			return mListe == null ? new MediAbgabe[0] : mListe;
		}

		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	static class MediVerlaufLabelProvider extends LabelProvider implements ITableLabelProvider{

		public Image getColumnImage(final Object element, final int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(final Object element, final int columnIndex) {
			if(element instanceof MediAbgabe){
				MediAbgabe ma=(MediAbgabe)element;
				switch(columnIndex){
				case 0: return  ma.von;
				case 1: return ma.bis;
				case 2: return ma.medi;
				case 3: return ma.dosis;
				default: return "??";
				}
			}
			return "?";
		}

	}

	public void clearEvent(final Class<? extends PersistentObject> template) {
		// TODO Auto-generated method stub
		
	}

	public void reload(){
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.runInUI(
					PlatformUI.getWorkbench().getProgressService(),
					new IRunnableWithProgress(){
						public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							monitor.beginTask("Medikamente einlesen", IProgressMonitor.UNKNOWN);
							monitor.subTask("Suche Verschreibungen...");
							Query<Prescription> qbe=new Query<Prescription>(Prescription.class);
							qbe.add("PatientID", "=", GlobalEvents.getSelectedPatient().getId());
							List<Prescription> list=qbe.execute();
							LinkedList<MediAbgabe> alle=new LinkedList<MediAbgabe>();
							monitor.subTask("suche Medikamente...");
							try{
							for(Prescription p:list){
								Map<TimeTool,String> terms=p.getTerms();
								TimeTool[] tts=terms.keySet().toArray(new TimeTool[0]);
								for(int i=0;i<tts.length-1;i++){
									if(i<tts.length-1){
										alle.add(new MediAbgabe(tts[i].toString(TimeTool.DATE_GER),tts[i+1].toString(TimeTool.DATE_GER),p));
									}else{
										alle.add(new MediAbgabe(tts[i].toString(TimeTool.DATE_GER)," ... ",p));
									}
								}
								alle.add(new MediAbgabe(tts[tts.length-1].toString(TimeTool.DATE_GER)," ... ",p));
							}
							}catch(Exception ex){
								ExHandler.handle(ex);
							}
							monitor.subTask("sortiere...");
							mListe=alle.toArray(new MediAbgabe[0]);
							tv.refresh(false);					
							monitor.done();
						}},
				    null);
		} catch (Throwable ex) {
			ExHandler.handle(ex);
		}
	}
	
	public void selectionEvent(final PersistentObject obj) {
		if(obj instanceof Patient){
			reload();		
		}
		
	}
	class MediAbgabe implements Comparable<MediAbgabe>{
		String orderA;
		String von,bis;
		String medi;
		String dosis;
		MediAbgabe(final String v, final String b, final Prescription p){
			von=v;
			bis=b;
			orderA=new TimeTool(v).toString(TimeTool.DATE_COMPACT);
			medi=p.getSimpleLabel();
			dosis=p.getDosis();
		}
		public int compareTo(final MediAbgabe o) {
			switch(sortCol){
			case 0:
				int res= orderA.compareTo(o.orderA);
				if(res==0){
					res= medi.compareTo(o.medi);
				}
				return res;
			default:
				res=medi.compareTo(o.medi);
				if(res==0){
					res=orderA.compareTo(o.orderA);
				}
				return res;
			}
			
		}
	}

	public void activation(final boolean mode) {
		// TODO Auto-generated method stub
		
	}

	public void visible(final boolean mode) {
		if(mode){
			GlobalEvents.getInstance().addSelectionListener(this);
			selectionEvent(GlobalEvents.getSelectedPatient());
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
		
	}
	
	class MediSorter extends ViewerSorter{

		@Override
		public int compare(final Viewer viewer, final Object e1, final Object e2) {
			return ((MediAbgabe)e1).compareTo((MediAbgabe)e2);
		}

		
	}
}
