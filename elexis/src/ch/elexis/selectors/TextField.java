package ch.elexis.selectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class TextField extends ActiveControl {

	public TextField(Composite parent, int displayBits, String displayName){
		super(parent, displayBits, displayName);
		setControl(new Text(this,SWT.BORDER));
	}
	
	public Text getTextControl(){
		return (Text)ctl;
	}

	@Override
	public String getText(){
		return getTextControl().getText();
	}

	@Override
	public void setText(String text){
		getTextControl().setText(text);
	}

	@Override
	public boolean isValid(){

		return false;
	}

	@Override
	public void clear(){
		getTextControl().setText("");
	}
}
