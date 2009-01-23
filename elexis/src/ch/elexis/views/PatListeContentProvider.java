package ch.elexis.views;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.ViewerConfigurer.CommonContentProvider;
import ch.rgw.tools.StringTool;

public class PatListeContentProvider implements CommonContentProvider,
		ILazyContentProvider {
	CommonViewer viewer;
	Query<Patient> qbe;
	Object[] pats;
	boolean bValid=false;
	boolean bUpdating=false;
	String[] order;
	String firstOrder;
	PatListFilterBox pfilter;
	ViewPart site;
	
	public PatListeContentProvider(CommonViewer cv, String[] fieldsToOrder, ViewPart s){
		viewer=cv;
		site=s;
		order=fieldsToOrder;
		firstOrder=fieldsToOrder[0];
	}
	public void startListening() {
		viewer.getConfigurer().getControlFieldProvider().addChangeListener(this);
		qbe=new Query<Patient>(Patient.class);
	}

	public void stopListening() {
		if(viewer!=null){
			viewer.getConfigurer().getControlFieldProvider().removeChangeListener(this);
		}
	}

	public void setFilter(PatListFilterBox f){
		qbe.addPostQueryFilter(f);
		pfilter=f;
		bValid=false;
	}
	public void removeFilter(PatListFilterBox f){
		qbe.removePostQueryFilter(f);
		pfilter=null;
		bValid=false;
	}
	public Object[] getElements(Object inputElement) {
		if(bValid || bUpdating){
			return pats;
		}
		if(pfilter!=null){
			pats=new String[]{"Lade Daten..."};
			((TableViewer)viewer.getViewerWidget()).setItemCount(1);
		}
		
		//viewer.getViewerWidget().refresh(true);
		if(!Hub.acl.request(AccessControlDefaults.PATIENT_DISPLAY)){
			return new Object[0];
		}
		
		Job job=new Job("Lade Patientenliste"){

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Patientenliste laden...", IProgressMonitor.UNKNOWN);
				
				qbe.clear();
				if(pfilter!=null){
					pfilter.aboutToStart();
				}
				viewer.getConfigurer().getControlFieldProvider().setQuery(qbe);
				String[] actualOrder;
				int idx=StringTool.getIndex(order, firstOrder);
				if( (idx==-1) || (idx==0)){
					actualOrder=order;
				}else{
					actualOrder=new String[order.length];
					int n=0;
					int begin=idx;
					do{
						actualOrder[n++]=order[idx++];
						if(idx>=order.length){
							idx=0;
						}
					}while(idx!=begin);
				}
				qbe.orderBy(false, actualOrder);
				List<Patient> lPats=qbe.execute();
				if(lPats==null){
					pats=new Patient[0];
				}else{
					pats= lPats.toArray(new Patient[0]);
				}
				Desk.getDisplay().syncExec(new Runnable(){

					public void run() {
						((TableViewer)viewer.getViewerWidget()).setItemCount(pats.length);
						bValid=true;
						if(pfilter!=null){
							pfilter.finished();
						}
						viewer.getViewerWidget().refresh();
						bUpdating=false;
						
					}
					
				});
				monitor.done();
				return Status.OK_STATUS;
			}
			
		};
		job.setPriority(Job.SHORT);
		job.setUser(false);
		//job.setSystem(true);
		bUpdating=true;
		IWorkbenchSiteProgressService siteService =
		      (IWorkbenchSiteProgressService)site.getSite().getAdapter(IWorkbenchSiteProgressService.class);
		   siteService.schedule(job, 0 /* now */, true /* use the half-busy cursor in the part */);

	   job.setProperty(IProgressConstants.ICON_PROPERTY, Desk.getImage(Desk.IMG_AUSRUFEZ_ROT));


		// job.schedule();
		return pats;
	}

	public void dispose() {
		stopListening();
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {	}

	public void changed(String[] fields, String[] values) {
		bValid=false;
		getElements(viewer);
		if(viewer.getConfigurer().getControlFieldProvider().isEmpty()){
			viewer.notify(CommonViewer.Message.empty);
		}else{
			viewer.notify(CommonViewer.Message.notempty);
		}
		//viewer.notify(CommonViewer.Message.update);
	}

	public void reorder(String field) {
		firstOrder=field;
		changed(null,null);
	}

	public void selected() {
		// TODO Auto-generated method stub

	}

	public void updateElement(int index) {
		if(!bValid){
			getElements(viewer);
		}
		if(pats.length>index){
			((TableViewer)viewer.getViewerWidget()).replace(pats[index], index);
		}else{
			((TableViewer)viewer.getViewerWidget()).replace("-", index);
		}
	}
	public void invalidate(){
		bValid=false;
	}

}
