package ch.elexis.actions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.viewers.CommonViewer;

/**
 * A TreeLoader designed to read only once (for immutable data)
 * @author gerry
 *
 */
public class ReadOnceTreeLoader extends PersistentObjectLoader implements
		ILazyTreeContentProvider {
	
	TreeViewer tv;
	HashMap<PersistentObject, HashMap> tree=new HashMap<PersistentObject,HashMap>();
	public ReadOnceTreeLoader(CommonViewer cv,
			Query<? extends PersistentObject> qbe, String parentField) {
		super(cv, qbe);
		
		tv=(TreeViewer)cv.getViewerWidget();
		
	}

	@Override
	public IStatus work(IProgressMonitor monitor, HashMap<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateElement(Object parent, int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateChildCount(Object element, int currentChildCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

}
