package ch.elexis.agenda.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.actions.FlatDataLoader;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.actions.PersistentObjectLoader.QueryFilter;
import ch.elexis.agenda.data.Termin;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.DefaultLabelProvider;
import ch.elexis.util.viewers.SimpleWidgetProvider;
import ch.elexis.util.viewers.ViewerConfigurer;
import ch.elexis.util.viewers.CommonViewer.Message;

public class TerminListeView extends ViewPart implements ActivationListener, SelectionListener {
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
				Patient p=GlobalEvents.getSelectedPatient();
				if(p==null){
					qbe.add(Termin.FLD_PATIENT, Query.EQUALS, "--");
				}else{
					qbe.add(Termin.FLD_PATIENT, Query.EQUALS, p.getId());
				}
			}});
		cv=new CommonViewer();
		ViewerConfigurer vc=new ViewerConfigurer(fdl,new DefaultLabelProvider(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_TABLE,SWT.NONE,cv));
		GlobalEvents.getInstance().addActivationListener(this, this);
		cv.create(vc, body, SWT.NONE, this);
		
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void activation(boolean mode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		GlobalEvents.getInstance().removeActivationListener(this, this);
		super.dispose();
	}

	public void visible(boolean mode) {
		if(mode){
			GlobalEvents.getInstance().addSelectionListener(this);
		}else{
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
	}

	public void clearEvent(Class<? extends PersistentObject> template) {
		if(template.equals(Patient.class)){
			form.setText("No Patient selected");
		}
	}

	public void selectionEvent(PersistentObject obj) {
		if(obj instanceof Patient){
			form.setText(((Patient)obj).getLabel());
			cv.notify(Message.update);
		}
	}
	
	

}
