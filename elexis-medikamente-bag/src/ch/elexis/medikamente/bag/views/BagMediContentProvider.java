package ch.elexis.medikamente.bag.views;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TableViewer;

import ch.elexis.Desk;
import ch.elexis.actions.FlatDataLoader;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.medikamente.bag.data.BAGMedi;
import ch.elexis.medikamente.bag.data.Substance;
import ch.elexis.util.viewers.CommonViewer;

public class BagMediContentProvider extends FlatDataLoader {
	PreparedStatement ps;
	private List<String> ids;
	private BAGMedi[] medis;
	
	public BagMediContentProvider(CommonViewer cv, Query<? extends PersistentObject> qbe){
		super(cv, qbe);
		StringBuilder sql=new StringBuilder();
		sql.append("SELECT m.product FROM ")
			.append(BAGMedi.JOINTTABLE)
			.append(" m, ")
			.append(Substance.TABLENAME)
			.append(" s WHERE m.Substance=s.ID AND s.name LIKE ?;");
		ps=PersistentObject.getConnection().prepareStatement(sql.toString());
	}

	@Override
	public IStatus work(IProgressMonitor monitor, HashMap<String, Object>params){
		final TableViewer tv = (TableViewer) cv.getViewerWidget();
		qbe.clear();
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		//String[] values=cv.getConfigurer().getControlFieldProvider().getValues();
		String[] values=(String[])params.get(PARAM_VALUES);
		if(values==null){
			values=new String[2];
		}
		ids= qbe.execute(ps, new String[]{values[1]});
		medis=new BAGMedi[ids.size()];
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		Desk.asyncExec(new Runnable() {
			public void run(){
				tv.setItemCount(0);
				// tv.remove(LOADMESSAGE);
				tv.setItemCount(ids.size());
			}
		});
		
		return Status.OK_STATUS;

	}

	@Override
	public void updateElement(int index){
		if(medis[index]==null){
			medis[index]=BAGMedi.load(ids.get(index));
		}
		TableViewer tv = (TableViewer) cv.getViewerWidget();
		tv.replace(medis[index], index);

	}

}
