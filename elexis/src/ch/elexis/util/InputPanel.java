package ch.elexis.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.Desk;
import ch.elexis.util.LabeledInputField.AutoForm;
import ch.elexis.util.LabeledInputField.InputData;

public class InputPanel extends Composite{
	int min,max;
	Composite top;
	InputData[] fields;
	AutoForm af;
	
	public InputPanel(Composite parent, int minColumns, int maxColumns, InputData[] fields){
		super(parent,SWT.NONE);
		this.fields=fields;
		min=minColumns;
		max=maxColumns;
		for(InputData id:fields){
			LabeledInputField widget=id.getWidget();
			if(widget!=null){
				Label lbl=widget.getLabelComponent();
				Font lblFont=Desk.getFont("Helvetica", 8, SWT.ITALIC);
				lbl.setFont(lblFont);
			}
		}
		setLayout(new GridLayout());
		af=new LabeledInputField.AutoForm(this,fields,min,max);
        af.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
	}

	public AutoForm getAutoForm(){
		return af;
	}
	public void setLocked(boolean lock){
		for(InputData id:fields){
			id.setEditable(!lock);
		}
	}
	
}
