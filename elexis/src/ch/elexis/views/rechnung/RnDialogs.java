package ch.elexis.views.rechnung;

import java.text.ParseException;
import java.util.Date;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.Desk;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.util.Money;
import ch.elexis.util.SWTHelper;

import com.tiff.common.ui.datepicker.DatePickerCombo;

public class RnDialogs {
	
	public static class GebuehrHinzuDialog extends TitleAreaDialog {
		Rechnung rn;
		DatePickerCombo dp;
		Text amount;
		Text bemerkung;
		public GebuehrHinzuDialog(Shell shell, Rechnung r){
			super(shell);
			rn=r;
		}
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite ret=new Composite(parent,SWT.NONE);
			ret.setLayout(new GridLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			new Label(ret,SWT.NONE).setText(Messages.getString("RnDialogs.date")); //$NON-NLS-1$
			dp=new DatePickerCombo(ret,SWT.NONE);
			new Label(ret,SWT.NONE).setText(Messages.getString("RnDialogs.amount")); //$NON-NLS-1$
			//nf=NumberFormat.getCurrencyInstance();
			amount=new Text(ret,SWT.BORDER);
			amount.setText(rn.getOffenerBetrag().getAmountAsString());
			amount.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			new Label(ret,SWT.NONE).setText(Messages.getString("RnDialogs.remark")); //$NON-NLS-1$
			bemerkung=new Text(ret,SWT.MULTI|SWT.BORDER);
			bemerkung.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
			return ret;
		}
		@Override
		public void create() {
			super.create();
			setTitle(Messages.getString("RnDialogs.invoice")+rn.getNr()); //$NON-NLS-1$
			getShell().setText(Messages.getString("RnDialogs.addExpense")); //$NON-NLS-1$
			setMessage(Messages.getString("RnDialogs.enterAmount")); //$NON-NLS-1$
			setTitleImage(Desk.theImageRegistry.get(Desk.IMG_LOGO48));
		}
		@Override
		protected void okPressed() {
			try{
				//Number num=df.parse(amount.getText());
				Money ret=new Money(amount).multiply(-1.0);
				rn.addZahlung(ret,bemerkung.getText());
				super.okPressed();
			}catch(ParseException nex){
				ErrorDialog.openError(getShell(),Messages.getString("RnDialogs.amountInvalid"),Messages.getString("RnDialogs.invalidFormat"), //$NON-NLS-1$ //$NON-NLS-2$
						new Status(1,"ch.elexis",1,"CurrencyFormat",null)); //$NON-NLS-1$ //$NON-NLS-2$
			}

		}

	}
	public static class BuchungHinzuDialog extends TitleAreaDialog{
		Rechnung rn;
		DatePickerCombo dp;
		Text	amount,bemerkung;
		BuchungHinzuDialog(Shell shell, Rechnung r){
			super(shell);
			rn=r;
		}
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite ret=new Composite(parent,SWT.NONE);
			ret.setLayout(new GridLayout());
			new Label(ret,SWT.NONE).setText(Messages.getString("RnDialogs.date")); //$NON-NLS-1$
			dp=new DatePickerCombo(ret,SWT.NONE);
			dp.setDate(new Date());
			new Label(ret,SWT.NONE).setText(Messages.getString("RnDialogs.amount")); //$NON-NLS-1$
			//nf=NumberFormat.getCurrencyInstance();
			amount=new Text(ret,SWT.BORDER);
			amount.setText(rn.getOffenerBetrag().getAmountAsString());
			amount.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			new Label(ret,SWT.NONE).setText(Messages.getString("RnDialogs.remark")); //$NON-NLS-1$
			bemerkung=new Text(ret,SWT.MULTI|SWT.BORDER);
			bemerkung.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
			bemerkung.setFocus();
			return ret;
		}
		@Override
		public void create() {
			super.create();
			setTitle(Messages.getString("RnDialogs.invoice")+rn.getNr()); //$NON-NLS-1$
			getShell().setText(Messages.getString("RnDialogs.addTransaction")); //$NON-NLS-1$
			setMessage(Messages.getString("RnDialogs.enterAmount")); //$NON-NLS-1$
			setTitleImage(Desk.theImageRegistry.get(Desk.IMG_LOGO48));
		}
		@Override
		protected void okPressed() {
			try{
				//Number num=df.parse(amount.getText());
				Money ret=new Money(amount);
				rn.addZahlung(ret,bemerkung.getText());
				super.okPressed();
			}catch(ParseException nex){
				ErrorDialog.openError(getShell(),Messages.getString("RnDialogs.amountInvalid"),Messages.getString("RnDialogs.invalidFormat"), //$NON-NLS-1$ //$NON-NLS-2$
						new Status(1,"ch.elexis",1,"CurrencyFormat",null)); //$NON-NLS-1$ //$NON-NLS-2$
			}

		}
		
	}
	public static class StatusAendernDialog extends TitleAreaDialog{
		Rechnung rn;
		Combo cbStates;
		//RnStatus[] states=RnStatus.Text;
		
		StatusAendernDialog(Shell shell, Rechnung r){
			super(shell);
			rn=r;
		}
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite ret=new Composite(parent,SWT.NONE);
			ret.setLayout(new FillLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
			cbStates=new Combo(ret,SWT.NONE);
			cbStates.setItems(RnStatus.Text);
			return ret;
		}
		@Override
		public void create() {
			super.create();
			getShell().setText(Messages.getString("RnDialogs.invoiceNumber")+rn.getNr()); //$NON-NLS-1$
			setTitle(Messages.getString("RnDialogs.modifyInvoiceState")); //$NON-NLS-1$
			
			setMessage(rn.getFall().getPatient().getLabel()+Messages.getString("RnDialogs.pleaseNewState")); //$NON-NLS-1$
		}
		@Override
		protected void okPressed() {
			int idx=cbStates.getSelectionIndex();
			if(idx!=-1){
				rn.setStatus(idx);
			}
			super.okPressed();
		}
		
	}
	public static class StornoDialog extends TitleAreaDialog{
		Rechnung rn;
		Button bYes,bNo;
		StornoDialog(Shell shell, Rechnung r){
			super(shell);
			rn=r;
		}
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite ret=new Composite(parent,SWT.NONE);
			ret.setLayout(new GridLayout(2,true));
			bYes=new Button(ret,SWT.RADIO);
			bNo=new Button(ret,SWT.RADIO);
			bYes.setText(Messages.getString("RnDialogs.yes")); //$NON-NLS-1$
			bNo.setText(Messages.getString("RnDialogs.no")); //$NON-NLS-1$
			ret.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
			return ret;
		}
		@Override
		public void create() {
			super.create();
			getShell().setText(Messages.getString("RnDialogs.invoice")+rn.getNr()); //$NON-NLS-1$
			setTitle(Messages.getString("RnDialogs.reallyCancel")); //$NON-NLS-1$
			setMessage(Messages.getString("RnDialogs.reactivateConsultations")); //$NON-NLS-1$
		}
		@Override
		protected void okPressed() {
			rn.storno(bYes.getSelection());
			super.okPressed();
		}
		
	}
}
