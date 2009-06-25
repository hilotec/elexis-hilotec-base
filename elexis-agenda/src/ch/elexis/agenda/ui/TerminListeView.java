package ch.elexis.agenda.ui;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.actions.FlatDataLoader;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.PersistentObjectLoader.QueryFilter;
import ch.elexis.agenda.data.Termin;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.viewers.CommonViewer;

public class TerminListeView extends ViewPart {
	ScrolledForm form;
	CommonViewer cv;
	
	public TerminListeView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		form=Desk.getToolkit().createScrolledForm(parent);
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Composite body=form.getBody();
		body.setLayout(new GridLayout());
		FlatDataLoader fdl=new FlatDataLoader(cv,new Query<Termin>(Termin.class));
		fdl.addQueryFilter(new QueryFilter(){

			public void apply(Query<? extends PersistentObject> qbe) {
				qbe.add(Termin.FLD_PATIENT, Query.EQUALS, GlobalEvents.getSelectedPatient().getId());
			}});
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
