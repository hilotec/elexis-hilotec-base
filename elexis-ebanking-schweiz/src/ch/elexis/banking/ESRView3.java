package ch.elexis.banking;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Hub;
import ch.elexis.actions.FlatDataLoader;
import ch.elexis.actions.GlobalEventDispatcher.IActivationListener;
import ch.elexis.actions.PersistentObjectLoader.QueryFilter;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.ViewerConfigurer;

public class ESRView3 extends ViewPart implements IActivationListener {
	CommonViewer cv;
	ViewerConfigurer vc;
	FlatDataLoader loader;

	public ESRView3() {

	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		cv = new CommonViewer();
		loader = new FlatDataLoader(cv, new Query<ESRRecord>(ESRRecord.class));
		loader.addQueryFilter(new QueryFilter() {

			public void apply(Query<? extends PersistentObject> qbe) {
				if (Hub.acl.request(AccessControlDefaults.ACCOUNTING_GLOBAL) == false) {
					if (Hub.actMandant != null) {
						qbe.startGroup();
						qbe.add("MandantID", "=", Hub.actMandant.getId()); //$NON-NLS-1$ //$NON-NLS-2$
						qbe.or();
						qbe.add("MandantID", "", null); //$NON-NLS-1$ //$NON-NLS-2$
						qbe.add("RejectCode", "<>", "0"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						qbe.endGroup();
						qbe.and();
					}
				}

			}
		});
		/*
		 * vc = new ViewerConfigurer(loader, new ESRLabelProvider(), new
		 * DefaultControlFieldProvider(cv, fields), new
		 * ViewerConfigurer.DefaultButtonProvider(), new SimpleWidgetProvider(
		 * SimpleWidgetProvider.TYPE_LAZYLIST, SWT.NONE, null)); cv.create(vc,
		 * parent, SWT.NONE, getViewSite()); makeActions();
		 * cv.setObjectCreateAction(getViewSite(), createKontakt); menu = new
		 * ViewMenus(getViewSite());
		 * menu.createViewerContextMenu(cv.getViewerWidget(), delKontakt,
		 * dupKontakt); menu.createMenu(GlobalActions.printKontaktEtikette);
		 * menu.createToolbar(GlobalActions.printKontaktEtikette);
		 * vc.getContentProvider().startListening();
		 * vc.getControlFieldProvider().addChangeListener(this);
		 * cv.addDoubleClickListener(new CommonViewer.DoubleClickListener() {
		 * public void doubleClicked(PersistentObject obj, CommonViewer cv){ try
		 * { KontaktDetailView kdv = (KontaktDetailView)
		 * getSite().getPage().showView(KontaktDetailView.ID);
		 * kdv.kb.catchElexisEvent(new ElexisEvent(obj, obj.getClass(),
		 * ElexisEvent.EVENT_SELECTED)); } catch (PartInitException e) {
		 * ExHandler.handle(e); }
		 * 
		 * } });
		 */
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void activation(boolean mode) {
		// TODO Auto-generated method stub

	}

	public void visible(boolean mode) {
		// TODO Auto-generated method stub

	}

}
