package com.hilotec.elexis.messwerte.views;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.Desk;
import ch.elexis.selectors.TextField;
import ch.elexis.util.LabeledInputField;
import ch.elexis.util.SWTHelper;

import com.hilotec.elexis.messwerte.data.Messung;
import com.hilotec.elexis.messwerte.data.MessungTyp;
import com.hilotec.elexis.messwerte.data.Messwert;
import com.hilotec.elexis.messwerte.data.Panel;

public class MessungTypDisplay extends Composite {
	MessungTyp mt;
	List<Messwert> mw;
	
	MessungTypDisplay(Composite parent, Messung m){
		super(parent,SWT.NONE);
		setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		setLayout(new FillLayout());
		this.mt=m.getTyp();
		mw=m.getMesswerte();
		Panel panel=mt.getPanel();
		Composite ret=createComposite(panel,this);
		//ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		parent.layout(true);
	}
	
	public Messwert getMesswert(String name){
		for(Messwert m:mw){
			if(m.getName().equals(name)){
				return m;
			}
		}
		return null;
	}
	
	
	public Composite createComposite(Panel p, Composite parent){
		Composite ret=new Composite(parent,SWT.NONE);
		//ret.setBackground(Desk.getColor(Desk.COL_BLUE));
		if(p.getType().equals("plain")){
			ret.setLayout(new FillLayout());
		}else if(p.getType().equals("grid")){
			String cols=p.getAttribute("columns");
			if(cols==null){
				ret.setLayout(new GridLayout());
			}else{
				ret.setLayout(new GridLayout(Integer.parseInt(cols),false));
			}
		}else if(p.getType().equals("field")){
			String fieldref=p.getAttribute("ref");
			Messwert mw=getMesswert(fieldref);
			if(mw!=null){
				int flags=0;
				if(p.getAttribute("editable").equals("false")){
					flags|=TextField.READONLY;
				}
				TextField tf=new TextField(ret,flags,mt.getTitle());
				tf.setText(mw.getDarstellungswert());
				setLayoutData(tf);

			}
		}
		for(Panel panel:p.getPanels()){
			setLayoutData(createComposite(panel,ret));
		}
		return ret;
	}
	
	private void setLayoutData(Control c){
		if(c.getParent().getLayout() instanceof GridLayout){
			c.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		}
		c.pack();
	}
	
}
