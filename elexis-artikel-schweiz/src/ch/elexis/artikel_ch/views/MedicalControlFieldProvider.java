package ch.elexis.artikel_ch.views;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;

import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Artikel;
import ch.elexis.data.Query;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.DefaultControlFieldProvider;
import ch.elexis.util.IScannerListener;
import ch.elexis.views.KonsDetailView;

public class MedicalControlFieldProvider extends DefaultControlFieldProvider implements IScannerListener {
	
	public MedicalControlFieldProvider(CommonViewer viewer, String[] flds) {
		super(viewer, flds);
	}
	
	public Composite createControl(final Composite parent) {
		Composite composite = super.createControl(parent);
		for(int i=0;i<selectors.length;i++){
            selectors[i].addScannerListener(this);
		}
		return composite;
	}

	private KonsDetailView getKonsDetailView() {
		IViewReference[] viewReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
		for (IViewReference viewRef: viewReferences) {
			if (KonsDetailView.ID.equals(viewRef.getId())) {
				return (KonsDetailView)viewRef.getPart(false);
			}
		}
		return null;
	}
	
	private void beep() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay().beep();
	}
	
	

	public void scannerInput(Event e) {
		KonsDetailView detailView = getKonsDetailView();
		Text text = null;
		if (e.widget instanceof Text) {
			text = (Text)e.widget;
		}
		if (text != null) {
			Query<Artikel> query = new Query<Artikel>(Artikel.class);
			query.add("EAN", "=", e.text);
			query.add("Typ", "=", "Medical");
			List<Artikel> artikelList = query.execute();
			for (Artikel artikel: artikelList) {
				if (text != null) {
					text.setText(artikel.getName());
				}
				if (GlobalEvents.getSelectedPatient() != null && detailView != null && detailView.checkFallOffen()) {
					detailView.addToVerechnung(artikel);
				} else {
					beep();
				}
			}
			text.selectAll();
		} else {
			beep();
		}
	}	
}
