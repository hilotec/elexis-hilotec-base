package ch.elexis.selectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class BooleanField extends ActiveControl {

	public BooleanField(Composite parent, int displayBits, String displayName){
		super(parent,displayBits|ActiveControl.HIDE_LABEL,displayName);
		Button b=new Button(this,SWT.CHECK);
		b.setText(displayName);
	}
	@Override
	protected void push() {
		if(textContents.equals("true")){
			((Button)ctl).setSelection(true);
		}else{
			((Button)ctl).setSelection(false);
		}

	}

}
