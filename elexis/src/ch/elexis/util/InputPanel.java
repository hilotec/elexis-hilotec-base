package ch.elexis.util;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

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
				FontRegistry fr=JFaceResources.getFontRegistry();
				Font lblFont=fr.get("lblFont");
				if(lblFont==null){
					//Font old=lbl.getFont();
					lblFont=new Font(Desk.theDisplay,"Helvetica",8,SWT.ITALIC);
					fr.put("lblFont", lblFont.getFontData());
				}
				lbl.setFont(lblFont);
			}
		}
		setLayout(new GridLayout());
		top=new Composite(this,SWT.BORDER);
		top.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		//Composite bottom=new Composite(this,SWT.NONE);
		//bottom.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
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
