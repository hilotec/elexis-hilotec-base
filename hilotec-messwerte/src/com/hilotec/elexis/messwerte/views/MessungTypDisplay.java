package com.hilotec.elexis.messwerte.views;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.hilotec.elexis.messwerte.data.Messung;
import com.hilotec.elexis.messwerte.data.MessungTyp;
import com.hilotec.elexis.messwerte.data.Messwert;

public class MessungTypDisplay extends Composite {
	MessungTyp mt;
	List<Messwert> mw;
	
	MessungTypDisplay(Composite parent, MessungTyp mt){
		super(parent,SWT.NONE);
		this.mt=mt;
	}
	
	void layout(Messung m){
		mw=m.getMesswerte();
		
	}
}
