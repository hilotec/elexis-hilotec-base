package ch.elexis.views;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.ViewerConfigurer.CommonContentProvider;
import ch.rgw.tools.StringTool;

public class PatListeContentProvider implements CommonContentProvider,
		ILazyContentProvider {
	CommonViewer viewer;
	Query<Patient> qbe;
	Patient[] pats;
	boolean bValid=false;
	String[] order;
	String firstOrder;
	
	public PatListeContentProvider(CommonViewer cv, String[] fieldsToOrder){
		viewer=cv;
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

	public Object[] getElements(Object inputElement) {
		if(bValid){
			return pats;
		}
		qbe.clear();
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
		pats= qbe.execute().toArray(new Patient[0]);
		((TableViewer)viewer.getViewerWidget()).setItemCount(pats.length);
		viewer.getViewerWidget().refresh();
		bValid=true;
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
		((TableViewer)viewer.getViewerWidget()).replace(pats[index], index);
	}

}
