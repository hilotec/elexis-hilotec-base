package ch.elexis.selectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

import ch.elexis.Desk;

public class SpinnerField extends ActiveControl {

	public SpinnerField(Composite parent, int displayBits, String displayName, int min, int max){
		super(parent,displayBits,displayName);
		Spinner spinner=new Spinner(this, SWT.NONE); 
		spinner.setMaximum(max);
		spinner.setMinimum(min);
		setControl(spinner);
		
	}
	@Override
	protected void push() {
		Desk.syncExec(new Runnable(){
			public void run(){
				Spinner spinner=(Spinner)ctl;
				spinner.setSelection(Integer.parseInt(textContents));
			}
		});
	}

}
