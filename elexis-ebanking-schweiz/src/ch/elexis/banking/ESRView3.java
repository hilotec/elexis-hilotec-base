package ch.elexis.banking;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.actions.GlobalEventDispatcher.IActivationListener;
import ch.elexis.data.Query;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.ViewerConfigurer;

public class ESRView3 extends ViewPart implements IActivationListener {
	CommonViewer cv;
	ViewerConfigurer vc;
	ESRLoader loader;
	
	public ESRView3(){
		
	}
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new FillLayout());
		cv = new CommonViewer();
		loader = new ESRLoader(cv, new Query<ESRRecord>(ESRRecord.class));
		/*
		vc =
			new ViewerConfigurer(loader, new ESRLabelProvider(),
				new DefaultControlFieldProvider(cv, fields),
				new ViewerConfigurer.DefaultButtonProvider(), new SimpleWidgetProvider(
					SimpleWidgetProvider.TYPE_LAZYLIST, SWT.NONE, null));
		cv.create(vc, parent, SWT.NONE, getViewSite());
		makeActions();
		cv.setObjectCreateAction(getViewSite(), createKontakt);
		menu = new ViewMenus(getViewSite());
		menu.createViewerContextMenu(cv.getViewerWidget(), delKontakt, dupKontakt);
		menu.createMenu(GlobalActions.printKontaktEtikette);
		menu.createToolbar(GlobalActions.printKontaktEtikette);
		vc.getContentProvider().startListening();
		vc.getControlFieldProvider().addChangeListener(this);
		cv.addDoubleClickListener(new CommonViewer.DoubleClickListener() {
			public void doubleClicked(PersistentObject obj, CommonViewer cv){
				try {
					KontaktDetailView kdv =
						(KontaktDetailView) getSite().getPage().showView(KontaktDetailView.ID);
					kdv.kb.catchElexisEvent(new ElexisEvent(obj, obj.getClass(),
						ElexisEvent.EVENT_SELECTED));
				} catch (PartInitException e) {
					ExHandler.handle(e);
				}
				
			}
		});
		 */
	}
	
	@Override
	public void setFocus(){
		// TODO Auto-generated method stub
		
	}
	
	public void activation(boolean mode){
		// TODO Auto-generated method stub
		
	}
	
	public void visible(boolean mode){
		// TODO Auto-generated method stub
		
	}
	
}
