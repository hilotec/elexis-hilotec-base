package com.hilotec.elexis.messwerte.views;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.hilotec.elexis.messwerte.data.Messung;
import com.hilotec.elexis.messwerte.data.MessungTyp;
import com.hilotec.elexis.messwerte.data.Messwert;
import com.hilotec.elexis.messwerte.data.Panel;

public class MessungTypDisplay extends Composite {
	MessungTyp mt;
	List<Messwert> mw;
	
	MessungTypDisplay(Composite parent, Messung m){
		super(parent,SWT.NONE);
		this.mt=m.getTyp();
		mw=m.getMesswerte();
		Panel panel=mt.getPanel();
		createComposite(panel,this);
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
				if(p.getAttribute("editable").equals("false")){
					Label lbl=new Label(ret,SWT.NONE);
					lbl.setText(mw.getDarstellungswert());
				}else{
					Text text=new Text(ret,SWT.BORDER);
					text.setText(mw.getDarstellungswert());
				}
			}
		}
		for(Panel panel:p.getPanels()){
			createComposite(panel,ret);
		}
		return ret;
	}
	
	
}
