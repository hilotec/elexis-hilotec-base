package ch.elexis.views;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.actions.ElexisEvent;
import ch.elexis.actions.ElexisEventListener;
import ch.elexis.actions.ElexisEventListenerImpl;
import ch.elexis.data.Konsultation;
import ch.elexis.data.RFE;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.viewers.ContentProviderAdapter;
import ch.elexis.util.viewers.TableLabelProvider;

public class RFEView extends ViewPart {
	
	TableViewer tv;

	ElexisEventListenerImpl eeli_kons = new ElexisEventListenerImpl(
			Konsultation.class) {

		@Override
		public void runInUi(ElexisEvent ev) {
			Konsultation k = (Konsultation) ev.getObject();
			RFE[] rfes=RFE.getRfeForKons(k.getId());
			
		}

	};

	@Override
	public void createPartControl(Composite parent) {

		tv = new TableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION);
		tv.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tv.setContentProvider(new ContentProviderAdapter(){
			@Override
			public Object[] getElements(Object inputElement) {
				return RFE.getRFETexts();
			}
			
		});

		tv.setLabelProvider(new TableLabelProvider(){
			
		});
		tv.setInput(this);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
