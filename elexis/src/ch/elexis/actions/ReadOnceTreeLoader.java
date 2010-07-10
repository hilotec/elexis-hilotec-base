package ch.elexis.actions;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.elexis.Desk;
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
		Desk.asyncExec(new Runnable() {

			@Override
			public void run() {
				tv.setChildCount("", root.length);
				//tv.setFilters(new ViewerFilter[] { filter });
				tv.refresh(true);
			}
		});
		return Status.OK_STATUS;
	}

	@Override
	public void updateElement(Object parent, int index) {
		PersistentObject elem;
		
		if (parent instanceof PersistentObject) {
			elem=getChildren((PersistentObject) parent)[index];
		} else {
			elem=root[index];
		}
		tv.replace(parent, index, elem);
		updateChildCount(elem,0);
	}

	@Override
	public void updateChildCount(Object element, int currentChildCount) {
		if (element instanceof PersistentObject) {
			tv.setChildCount(element,
					getChildren((PersistentObject) element).length);
		} else {
			tv.setChildCount(element, root.length);
		}
	}

	@Override
	public Object getParent(Object element) {
		PersistentObject po = (PersistentObject) element;
		return po.get(parentColumn);
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
			qbe.orderBy(false, orderBy);
		}
	}

	@Override
	public void init() {
		if (slp == null) {
			slp = (SelectorPanelProvider) cv.getConfigurer()
					.getControlFieldProvider();
		}
		if (filter == null) {
			filter = (ViewerFilter) slp.createFilter();
		}
		tv = (TreeViewer) cv.getViewerWidget();
	}

}
