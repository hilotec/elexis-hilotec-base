/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: KonsListeView2.java 5175 2009-02-22 17:54:06Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.FlatDataLoader;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.KonsFilter;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.dialogs.KonsFilterDialog;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.SimpleWidgetProvider;
import ch.elexis.util.viewers.ViewerConfigurer;
import ch.elexis.util.viewers.CommonViewer.Message;
import ch.rgw.tools.ExHandler;

public class KonsListeView2 extends ViewPart implements ActivationListener, SelectionListener {
	public static final String ID="ch.elexis.HistoryViewV2";
	private IAction newKonsAction, filterAction;
	Query<Konsultation> qbe;
	CommonViewer cv;
	ViewerConfigurer vc;
	private Patient actPatient;
	private KonsFilter filter=null;
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new GridLayout());
		cv=new CommonViewer();
		qbe=new Query<Konsultation>(Konsultation.class);
		vc=new ViewerConfigurer(
			new KonsLoader(cv,qbe),
			new KonsListeTextProvider(),
			new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST,0,cv)
			);
		makeActions();
		ViewMenus menus = new ViewMenus(getViewSite());
		menus.createToolbar(newKonsAction, filterAction);
		// GlobalEvents.getInstance().addSelectionListener(this,
		// getViewSite().getWorkbenchWindow());
		GlobalEvents.getInstance().addActivationListener(this, this);
		cv.create(vc, parent, SWT.NONE, parent);
	}
	
	@Override
	public void setFocus(){
	// TODO Auto-generated method stub
	
	}
	
	private void makeActions(){
		newKonsAction = new Action("Neue Konsultation") {
			{
				setToolTipText("Neue Konsultation erstellen");
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_NEW));
			}
			
			@Override
			public void run(){
				if (actPatient == null) {
					return;
				}
				Fall fall = GlobalEvents.getSelectedFall();
				if (fall == null) {
					
					Konsultation k = actPatient.getLetzteKons(false);
					if (k != null) {
						fall = k.getFall();
					} else {
						if (SWTHelper.askYesNo("Kein Fall ausgewählt",
							"Soll ein neuern Fall für diese Konsultation erstellt werden?")) {
							fall = actPatient.neuerFall("Allgemein", "Krankheit", "KVG");
						} else {
							return;
						}
					}
				}
				Konsultation k = fall.neueKonsultation();
				k.setMandant(Hub.actMandant);
				GlobalEvents.getInstance().fireSelectionEvent(k);
			}
		};
		filterAction = new Action("Liste Filtern", Action.AS_CHECK_BOX) {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_FILTER));
				setToolTipText("Liste der Konsultationen filtern");
			}
			
			@Override
			public void run(){
				if (!isChecked()) {
					filter = null;
				} else {
					KonsFilterDialog kfd = new KonsFilterDialog(actPatient, filter);
					if (kfd.open() == Dialog.OK) {
						filter = kfd.getResult();
					} else {
						kfd = null;
						setChecked(false);
					}
				}
	
			}
		};

	}

	public void activation(boolean mode){
		// TODO Auto-generated method stub
		
	}

	public void visible(boolean mode){
		if(mode){
			GlobalEvents.getInstance().addSelectionListener(this);
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
		
	}

	public void clearEvent(Class<? extends PersistentObject> template){
		// TODO Auto-generated method stub
		
	}

	public void selectionEvent(PersistentObject obj){
		if(obj instanceof Patient){
			actPatient=(Patient)obj;
			//((TableViewer)cv.getViewerWidget()).setItemCount(0);
			cv.notify(Message.update);
		}
		
	}
	private class KonsLoader extends FlatDataLoader{
		PreparedStatement ps;
		LinkedList<String> kids;
		private KonsLoader(CommonViewer cv, Query<Konsultation> qbe){
			super(cv,qbe);
			String sql="SELECT k.id from behandlungen as k, faelle as f where f.PatientID=? AND k.FallID=f.ID order by k.Datum";
			ps=PersistentObject.getConnection().prepareStatement(sql);
		}

		
		@Override
		public IStatus work(final IProgressMonitor monitor, final HashMap<String, Object> params){
			try{
				ps.setString(1, actPatient.getId());
				ResultSet rs=ps.executeQuery();
				kids=new LinkedList<String>();
				while(rs!=null && rs.next()){
					kids.add(rs.getString(1));
				}
				rs.close();
				TableViewer tv = (TableViewer) cv.getViewerWidget();
				tv.setItemCount(kids.size());
			}catch(Exception ex){
				ExHandler.handle(ex);
			}
			
			return Status.CANCEL_STATUS;
		}


		@Override
		public void updateElement(int index){
			if(index>0 && index<kids.size()){
				String kid=kids.get(index);
				if(kid!=null){
					Konsultation k=Konsultation.load(kid);
					if(k.isValid()){
						TableViewer tv = (TableViewer) cv.getViewerWidget();
						tv.replace(k, index);
					}
				}
			}
		}


		
		
	}
	private static class KonsListeTextProvider extends LabelProvider implements ITableLabelProvider{

		public Image getColumnImage(Object element, int columnIndex){
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(Object element, int columnIndex){
			if(element instanceof Konsultation){
				return ((Konsultation)element).getLabel();
			}
			return "?";
		}
		
	}
}
