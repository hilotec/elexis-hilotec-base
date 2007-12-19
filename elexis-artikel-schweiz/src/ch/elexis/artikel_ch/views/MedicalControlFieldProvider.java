package ch.elexis.artikel_ch.views;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;

import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.ScannerEvents;
import ch.elexis.data.Artikel;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Query;
import ch.elexis.text.ElexisText;
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
		for(final ElexisText selector: selectors){
			selector.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.character == SWT.CR) {
						String text = selector.getText();
						text = text.replaceAll(new Character(SWT.CR).toString(), "");
						text = text.replaceAll(new Character(SWT.LF).toString(), "");
						text = text.replaceAll(new Character((char)0).toString(), "");
						Event scannerEvent = new Event();
						scannerEvent.text = selector.getText();
						scannerEvent.widget = selector.getWidget();
						scannerInput(scannerEvent);
					}
				}
            });
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
			if (artikelList.size() == 0) {
				ScannerEvents.beep();
			}
			for (Artikel artikel: artikelList) {
				Konsultation kons=GlobalEvents.getSelectedKons();
				if(kons!=null){
					detailView.addToVerechnung(artikel);
				}else{
					ScannerEvents.beep();
				}
			}
			text.selectAll();
		} else {
			ScannerEvents.beep();
		}
	}	
}
