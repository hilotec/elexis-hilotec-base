package ch.elexis.medikamente.bag.views;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;

import ch.elexis.Desk;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.medikamente.bag.data.BAGMedi;
import ch.elexis.medikamente.bag.data.BAGMediFactory;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.FieldDescriptor;
import ch.elexis.util.viewers.SelectorPanelProvider;
import ch.elexis.util.viewers.SimpleWidgetProvider;
import ch.elexis.util.viewers.ViewerConfigurer;
import ch.elexis.views.artikel.ArtikelContextMenu;
import ch.elexis.views.codesystems.CodeSelectorFactory;

public class BAGMediSelector extends CodeSelectorFactory {
	public static final String FIELD_NAME = "Name";
	public static final String FIELD_SUBSTANCE = "Substanz";
	public static final String FIELD_NOTES = "Notizen";
	
	private IAction sameOfGroupAction, genericsAction, onStockAction;
	CommonViewer cv;
	SelectorPanelProvider slp;
	FieldDescriptor<?>[] fields =
		{
			new FieldDescriptor<BAGMedi>(FIELD_NAME),
			new FieldDescriptor<BAGMedi>(FIELD_SUBSTANCE),
			new FieldDescriptor<BAGMedi>(FIELD_NOTES),
		};
	BagMediContentProvider fdl;
	
	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer cv){
		makeActions();
		ArtikelContextMenu menu =
			new ArtikelContextMenu((BAGMedi) new BAGMediFactory().createTemplate(BAGMedi.class), cv);
		menu.addAction(sameOfGroupAction);
		slp = new SelectorPanelProvider(fields, true);
		slp.addActions(genericsAction, onStockAction);
		fdl = new BagMediContentProvider(cv, new Query<BAGMedi>(BAGMedi.class));
		
		this.cv = cv;
		return new ViewerConfigurer(fdl, new BAGMediLabelProvider(), slp,
			new ViewerConfigurer.DefaultButtonProvider(), new SimpleWidgetProvider(
				SimpleWidgetProvider.TYPE_LAZYLIST, SWT.NONE, null));
		
	}
	
	@Override
	public void dispose(){
		cv.dispose();
		fdl.dispose();
	}
	
	@Override
	public String getCodeSystemName(){
		return BAGMedi.CODESYSTEMNAME;
	}
	
	@Override
	public Class<? extends PersistentObject> getElementClass(){
		return BAGMedi.class;
	}
	
	private void makeActions(){
		sameOfGroupAction = new Action("Selbe therap. Gruppe") {
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_FILTER));
				setToolTipText("Zeige alle Medikamente derselben therapeutischen Gruppe");
			}
			
			@Override
			public void run(){
				// ContentProvider cp = (ContentProvider) cv.getConfigurer().getContentProvider();
				BAGMedi selected = (BAGMedi) cv.getSelection()[0];
				fdl.setGroup(selected.get("Gruppe"));
				slp.fireChangedEvent();
			}
			
		};
		genericsAction = new Action("Nur Generika") {
			{
				String img = "icons" + File.separator + "ggruen.png";
				ImageDescriptor image = BAGMediFactory.loadImageDescriptor(img);
				setImageDescriptor(image);
				setToolTipText("Nur Generika anzeigen");
			}
			
			@Override
			public void run(){
				fdl.toggleGenericsOnly();
				slp.fireChangedEvent();
			}
		};
		onStockAction = new Action("Nur Lagerartikel") {
			{
				String img = "icons" + File.separator + "lager.png";
				ImageDescriptor image = BAGMediFactory.loadImageDescriptor(img);
				setImageDescriptor(image);
				setToolTipText("Nur Lagerartikel anzeigen");
			}
			
			@Override
			public void run(){
				fdl.toggleStockOnly();
				slp.fireChangedEvent();
			}
		};
	}
}
