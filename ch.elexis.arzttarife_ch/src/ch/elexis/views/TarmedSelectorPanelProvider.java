package ch.elexis.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredViewer;

import ch.elexis.Desk;
import ch.elexis.actions.ElexisEvent;
import ch.elexis.actions.ElexisEventDispatcher;
import ch.elexis.actions.ElexisEventListenerImpl;
import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObject;
import ch.elexis.selectors.FieldDescriptor;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.SelectorPanelProvider;
import ch.rgw.tools.TimeTool;

public class TarmedSelectorPanelProvider extends SelectorPanelProvider {
	private CommonViewer commonViewer;
	private StructuredViewer viewer;
	
	private Action validateDate;
	private TarmedValidDateFilter validDateFilter = new TarmedValidDateFilter();
	private FilterKonsultationListener konsFilter = new FilterKonsultationListener(
		Konsultation.class);

	public TarmedSelectorPanelProvider(CommonViewer cv,
		FieldDescriptor<? extends PersistentObject>[] fields, boolean bExlusive){
		super(fields, bExlusive);
		commonViewer = cv;

	}
	
	@Override
	public void setFocus(){
		super.setFocus();
		if (viewer == null) {
			validateDate =
				new Action("Nach Datum der aktuellen Konsultation filtern", Action.AS_CHECK_BOX) {
				{
					setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_FILTER));
				}
				
				@Override
				public void run(){
					boolean actState = validDateFilter.getDoFilter();
					validDateFilter.setDoFilter(!actState);
					if (!actState) {
						Konsultation selectedKons =
							(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
						// apply the filter
						if (selectedKons != null) {
							validDateFilter.setValidDate(new TimeTool(selectedKons.getDatum()));
						}
					}
					
					viewer.getControl().setRedraw(false);
					viewer.refresh();
					viewer.getControl().setRedraw(true);
				}
			};
			validateDate.setToolTipText("Nach Datum der aktuellen Konsultation filtern");
			validateDate.setChecked(false);
			
			getPanel().addActions(validateDate);

			viewer = commonViewer.getViewerWidget();
			viewer.addFilter(validDateFilter);
		}
		ElexisEventDispatcher.getInstance().addListeners(konsFilter);
	}
	
	private class FilterKonsultationListener extends ElexisEventListenerImpl {
		
		public FilterKonsultationListener(Class<?> clazz){
			super(clazz);
		}
		
		@Override
		public void runInUi(ElexisEvent ev){
			Konsultation selectedKons =
				(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
			// apply the filter
			if (selectedKons != null) {
				validDateFilter.setValidDate(new TimeTool(selectedKons.getDatum()));
				viewer.getControl().setRedraw(false);
				viewer.refresh();
				viewer.getControl().setRedraw(true);
			}
		}
	}
}
