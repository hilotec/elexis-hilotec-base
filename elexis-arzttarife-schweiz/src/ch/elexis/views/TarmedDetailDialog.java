package ch.elexis.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.data.TarmedLeistung;
import ch.elexis.data.Verrechnet;
import ch.elexis.util.Money;
import ch.elexis.util.SWTHelper;

public class TarmedDetailDialog extends Dialog {
	Verrechnet v;
	TarmedDetailDisplay td;
	Combo cSide;
	Button bPflicht;
	
	public TarmedDetailDialog(Shell shell, Verrechnet tl){
		super(shell);
		v=tl;
		td=new TarmedDetailDisplay();
		
	}
	@Override
	protected Control createDialogArea(Composite parent) {
		//Composite ret=td.createDisplay(parent, null);
		//td.display(tl);
		TarmedLeistung tl=(TarmedLeistung)v.getVerrechenbar();
		Composite ret=(Composite)super.createDialogArea(parent);
		ret.setLayout(new GridLayout(6,true));
		
		double tpAL=tl.getAL()/100.0;
		double tpTL=tl.getTL()/100.0;
		double tpw=v.getTPW();
		Money mAL=new Money(tpAL*tpw);
		Money mTL=new Money(tpTL*tpw);
		double tpAll=tpAL+tpTL;
		Money mAll=new Money(tpAll);
		
		new Label(ret,SWT.NONE).setText("TP AL");
		new Label(ret,SWT.NONE).setText(Double.toString(tpAL));
		new Label(ret,SWT.NONE).setText("TP-Wert");
		new Label(ret,SWT.NONE).setText(Double.toString(tpw));
		new Label(ret,SWT.NONE).setText("CHF AL");
		new Label(ret,SWT.NONE).setText(mAL.getAmountAsString());
		
		new Label(ret,SWT.NONE).setText("TP TL");
		new Label(ret,SWT.NONE).setText(Double.toString(tpTL));
		new Label(ret,SWT.NONE).setText("TP-Wert");
		new Label(ret,SWT.NONE).setText(Double.toString(tpw));
		new Label(ret,SWT.NONE).setText("CHF TL");
		new Label(ret,SWT.NONE).setText(mTL.getAmountAsString());
		
		new Label(ret,SWT.NONE).setText("TP Total");
		new Label(ret,SWT.NONE).setText(Double.toString(tpAll));
		new Label(ret,SWT.NONE).setText("TP-Wert");
		new Label(ret,SWT.NONE).setText(Double.toString(tpw));
		new Label(ret,SWT.NONE).setText("CHF Total");
		new Label(ret,SWT.NONE).setText(mAll.getAmountAsString());
		
		String mins=Integer.toString(tl.getMinutes());
		new Label(ret,SWT.NONE).setText("Minutage:");
		new Label(ret,SWT.NONE).setText(mins);
		
		
		Label min=new Label(ret,SWT.NONE);
		min.setText(Integer.toString(tl.getMinutes())+" Minuten");
		min.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		
		new Label(ret,SWT.NONE).setText("Seite");
		cSide=new Combo(ret,SWT.SINGLE);
		
		new Label(ret,SWT.NONE).setText("Pflichtleist.");
		bPflicht=new Button(ret,SWT.CHECK);
		
		return ret;
	}
	@Override
	public void create(){
		super.create();
		getShell().setText("Tarmed-Details");
	}

	
}

