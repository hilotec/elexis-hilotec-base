package ch.elexis.ebanking_ch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import ch.elexis.Hub;
import ch.elexis.admin.ACE;
import ch.elexis.banking.ESRRecord;

public class ESRContentProvider extends ArrayContentProvider {
	
	private Label _lblSUMME;
	private Label _lblREADDATE;
	private ACE _rights;
	
	public ESRContentProvider(Label lblSUMME, Label lblREADDATE, ACE rights){
		_lblSUMME = lblSUMME;
		_lblREADDATE = lblREADDATE;
		_rights = rights;
	}
	
	@Override
	public Object[] getElements(Object inputElement){
		Object ret[] = super.getElements(inputElement);
		
		if (Hub.acl.request(_rights) == false) {
			return Collections.emptyList().toArray();
		}
		
		List<ESRRecord> retList = new ArrayList<ESRRecord>();
		
		for (Object object : ret) {
			ESRRecord rec = (ESRRecord) object;
			if (rec.getTyp().equals(ESRRecord.MODE.Summenrecord)) {
				final ESRRecord amount = rec;
				Display.getCurrent().asyncExec(new Runnable() {					
					@Override
					public void run(){
						_lblSUMME.setText(amount.getBetrag()+"");
						if(amount.getBetrag().isNegative()) {
							_lblSUMME.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
						} else {
							_lblSUMME.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
						}
						_lblREADDATE.setText(amount.get("Datum"));
					}
				});
				continue;
			}
			if (rec.getId().equals("1"))
				continue;
			
			retList.add(rec);
		}
		
		return retList.toArray();
	}
}
