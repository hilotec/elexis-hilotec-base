package ch.elexis.selectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class ComboField extends ActiveControl {
	Combo combo;
	
	public ComboField(Composite parent, int displayBits, String displayName, String... values){
		super(parent, displayBits, displayName);
		combo=new Combo(parent,SWT.READ_ONLY|SWT.SINGLE);
		combo.setItems(values);
		setControl(combo);
	}

	@Override
	public void clear(){
		combo.select(0);
	}

	@Override
	public String getText(){
		return combo.getText();
	}

	@Override
	public boolean isValid(){
		int idx=combo.getSelectionIndex();
		return idx!=-1;
	}

	@Override
	public void setText(String text){
		combo.setText(text);		
	}

}
