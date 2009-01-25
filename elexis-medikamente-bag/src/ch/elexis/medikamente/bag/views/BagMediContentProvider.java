package ch.elexis.medikamente.bag.views;

import java.sql.PreparedStatement;
import java.util.ArrayList;
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
import ch.rgw.tools.StringTool;

public class BagMediContentProvider extends FlatDataLoader {
	PreparedStatement psSubst, psNotes, psMedi;
	private List<String> ids;
	private BAGMedi[] medis;
	
	public BagMediContentProvider(CommonViewer cv, Query<? extends PersistentObject> qbe){
		super(cv, qbe);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT m.product FROM ").append(BAGMedi.JOINTTABLE).append(" m, ").append(
			Substance.TABLENAME).append(" s WHERE m.Substance=s.ID AND s.name LIKE ?;");
		psSubst = PersistentObject.getConnection().prepareStatement(sql.toString());
		sql.setLength(0);
		sql.append("SELECT id FROM ").append(BAGMedi.EXTTABLE).append(" WHERE Keywords LIKE ?;");
		psNotes = PersistentObject.getConnection().prepareStatement(sql.toString());
	}
	
	@Override
	public IStatus work(IProgressMonitor monitor, HashMap<String, Object> params){
		final TableViewer tv = (TableViewer) cv.getViewerWidget();
		qbe.clear();
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		// String[] values=cv.getConfigurer().getControlFieldProvider().getValues();
		HashMap<String, String> values = (HashMap<String, String>) params.get(PARAM_VALUES);
		if (values == null) {
			values = new HashMap<String, String>();
		}
		String subst = values.get(BAGMediSelector.FIELD_SUBSTANCE);
		String notes = values.get(BAGMediSelector.FIELD_NOTES);
		String names = values.get(BAGMediSelector.FIELD_NAME);
		if (!StringTool.isNothing(names)) {
			qbe.add(BAGMedi.NAME, "Like", names + "%");
			medis = qbe.execute().toArray(new BAGMedi[0]);
		} else {
			if (!StringTool.isNothing(subst)) {
				ids = qbe.execute(psSubst, new String[] {
					subst + "%"
				});
			} else if (!StringTool.isNothing(notes)) {
				ids = qbe.execute(psNotes, new String[] {
					"%" + notes + "%"
				});
			} else {
				ids = new ArrayList<String>();
			}
			medis = new BAGMedi[ids.size()];
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
		}
		Desk.asyncExec(new Runnable() {
			public void run(){
				tv.setItemCount(0);
				// tv.remove(LOADMESSAGE);
				tv.setItemCount(medis.length);
			}
		});
		
		return Status.OK_STATUS;
		
	}
	
	@Override
	public void updateElement(int index){
		if (medis[index] == null) {
			medis[index] = BAGMedi.load(ids.get(index));
		}
		TableViewer tv = (TableViewer) cv.getViewerWidget();
		tv.replace(medis[index], index);
		
	}
	
}
