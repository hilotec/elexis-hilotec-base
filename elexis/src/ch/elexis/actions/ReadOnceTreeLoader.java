package ch.elexis.actions;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.SelectorPanelProvider;

/**
 * A TreeLoader designed to read only once (for immutable data)
 * 
 * @author gerry
 * 
 */
public class ReadOnceTreeLoader extends PersistentObjectLoader implements
		ILazyTreeContentProvider {

	protected String parentColumn;
	protected String orderBy;
	private PersistentObject[] root;
	TreeViewer tv;
	int size = 0;
	SelectorPanelProvider slp;
	ViewerFilter filter;

	public ReadOnceTreeLoader(CommonViewer cv,
			Query<? extends PersistentObject> qbe, String parentField,
			String orderBy) {
		super(cv, qbe);
		parentColumn = parentField;
		this.orderBy = orderBy;
		setQuery("NIL");
		root = qbe.execute().toArray(new PersistentObject[0]);

	}

	@Override
	public IStatus work(IProgressMonitor monitor, HashMap<String, Object> params) {
		if (slp == null) {
			slp = (SelectorPanelProvider) cv.getConfigurer()
					.getControlFieldProvider();
		}
		if (filter == null) {
			filter = (ViewerFilter) slp.createFilter();
		}
		tv = (TreeViewer) cv.getViewerWidget();
		tv.setChildCount("", root.length);
		tv.setFilters(new ViewerFilter[] {filter});
		return Status.OK_STATUS;
	}

	@Override
	public void updateElement(Object parent, int index) {

		if (parent instanceof PersistentObject) {
			tv.replace(parent, index,
					getChildren((PersistentObject) parent)[index]);
		} else {
			tv.replace("", index, root[index]);
		}

	}

	@Override
	public void updateChildCount(Object element, int currentChildCount) {
		if (element instanceof PersistentObject) {
			tv.setChildCount(element,
					getChildren((PersistentObject) element).length);
		} else {
			tv.setChildCount("", root.length);
		}
	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	private PersistentObject[] getChildren(PersistentObject parent) {
		if (parent == null) {
			setQuery("NIL");
		} else {
			setQuery(parent.getId());
		}
		return qbe.execute().toArray(new PersistentObject[0]);
	}

	protected void setQuery(String parent) {
		qbe.clear();
		qbe.add(parentColumn, Query.EQUALS, parent);
		applyQueryFilters();
		if (orderBy != null) {
			qbe.orderBy(true, orderBy);
		}
	}

}
