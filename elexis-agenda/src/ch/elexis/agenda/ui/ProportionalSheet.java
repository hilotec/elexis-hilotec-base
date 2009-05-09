package ch.elexis.agenda.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class ProportionalSheet extends Composite {
	private BaseView view;
	
	public ProportionalSheet(Composite parent, BaseView v){
		super(parent,SWT.NONE);
		view=v;
	}
}
