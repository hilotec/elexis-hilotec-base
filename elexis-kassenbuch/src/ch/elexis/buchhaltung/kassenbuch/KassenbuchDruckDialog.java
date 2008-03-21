package ch.elexis.buchhaltung.kassenbuch;

import java.util.SortedSet;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.Hub;
import ch.elexis.data.Brief;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.text.ITextPlugin;
import ch.elexis.text.TextContainer;
import ch.elexis.text.ITextPlugin.ICallback;
import ch.elexis.util.Money;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

public class KassenbuchDruckDialog extends Dialog implements ICallback {
	TimeTool ttVon, ttBis;
	
	public KassenbuchDruckDialog(Shell shell, TimeTool von, TimeTool bis){
		super(shell);
		ttVon=von;
		ttBis=bis;
	}
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new FillLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
	
		TextContainer text=new TextContainer(getShell());
		text.getPlugin().createContainer(ret, this);
		text.getPlugin().showMenu(false);
		text.getPlugin().showToolbar(false);
		text.createFromTemplateName(null, "Liste", Brief.UNKNOWN, Hub.actUser, "Kassenbuch");
		SortedSet<KassenbuchEintrag> set=KassenbuchEintrag.getBookings(ttVon,ttBis);
		if(set==null){
			return ret;
		}
		KassenbuchEintrag[] lines=set.toArray(new KassenbuchEintrag[0]);
		String[][] table=new String[lines.length+1][];
		table[0]=new String[]{"Nr","Datum","Soll","Haben","Betrag","Text"};
		for(int i=1;i<=lines.length;i++){
			table[i]=new String[6];
			KassenbuchEintrag kb=lines[i-1];
			Money betrag= kb.getAmount();
			table[i][0]=kb.get("BelegNr");
			table[i][1]=kb.getDate();
			table[i][2]=betrag.isNegative() ? new Money(betrag).negate().getAmountAsString() : "";
			table[i][3]=betrag.isNegative() ? "" : betrag.getAmountAsString();
			table[i][4]=kb.getSaldo().getAmountAsString();
			table[i][5]=kb.getText();
		}
		text.getPlugin().setFont("Helvetica", SWT.NORMAL, 9);
		text.getPlugin().insertTable("[Liste]", ITextPlugin.FIRST_ROW_IS_HEADER, table, new int[]{5,15,15,15,20,30});
		return ret;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText("Kassenbuch");
		getShell().setSize(800, 700);
	
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

	public void save() {
	}

	public boolean saveAs() {
		return false;
	}

}
