package ch.elexis.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.data.Organisation;
import ch.elexis.data.Person;
import ch.elexis.data.Xid;
import ch.elexis.data.Xid.XIDDomain;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;

public class XIDEdit extends PreferencePage implements IWorkbenchPreferencePage {
	Table table;
	
	@Override
	protected Control createContents(Composite parent) {
		table=new Table(parent,SWT.FULL_SELECTION);
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		TableColumn tc0=new TableColumn(table,SWT.NONE);
		tc0.setText("Kurzname");
		tc0.setWidth(150);
		TableColumn tc1=new TableColumn(table,SWT.NONE);
		tc1.setText("Domainname");
		tc1.setWidth(300);
		TableColumn tc2=new TableColumn(table,SWT.NONE);
		tc2.setText("Anzeige");
		tc2.setWidth(50);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		for(String dom:Xid.getXIDDomains()){
			TableItem it=new TableItem(table,SWT.NONE);
			XIDDomain xd=Xid.getDomain(dom);
			it.setText(0,Xid.getSimpleNameForXIDDomain(dom));
			it.setText(1,dom);
			StringBuilder sb=new StringBuilder();
			if(xd.isDisplayedFor(Person.class)){
				sb.append("P");
			}
			if(xd.isDisplayedFor(Organisation.class)){
				sb.append("O");
			}
			it.setText(2,sb.toString());
			
		}
		table.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				TableItem[] sel=table.getSelection();
				if(sel!=null && sel.length>0){
					new XidEditDialog(getShell(),sel[0].getText(1)).open();
					for(TableItem it:table.getItems()){
						XIDDomain xd=Xid.getDomain(it.getText(1));
						//it.setText(0,Xid.getSimpleNameForXIDDomain(dom));
						StringBuilder sb=new StringBuilder();
						if(xd.isDisplayedFor(Person.class)){
							sb.append("P");
						}
						if(xd.isDisplayedFor(Organisation.class)){
							sb.append("O");
						}
						it.setText(2,sb.toString());
					}
					table.redraw();
				}
			}
			
		});
		return table;
	}

	public void init(IWorkbench workbench) {
		
		
	}

	static class XidEditDialog extends Dialog{
		Text tShort;
		Button bPerson, bOrg;
		XIDDomain mine;
		public XidEditDialog(Shell shell, String myDomain) {
			super(shell);
			mine=Xid.getDomain(myDomain);
		}

		@Override
		public void create() {
			super.create();
			getShell().setText("XID Optionen");
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite ret=(Composite)super.createDialogArea(parent);
			ret.setLayout(new RowLayout(SWT.VERTICAL));
			new Label(ret,SWT.NONE).setText(mine.getDomainName());
			tShort=new Text(ret,SWT.BORDER);
			tShort.setText(mine.getSimpleName());
			new Label(ret,SWT.SEPARATOR|SWT.HORIZONTAL);
			new Label(ret,SWT.NONE).setText("Anzeigen bei:");
			bPerson=new Button(ret,SWT.CHECK);
			bOrg=new Button(ret,SWT.CHECK);
			bPerson.setText("Personen");
			bOrg.setText("Organisationen");
			if(mine.isDisplayedFor(Person.class)){
				bPerson.setSelection(true);
			}
			if(mine.isDisplayedFor(Organisation.class)){
				bOrg.setSelection(true);
			}
			return ret;
		}

		@Override
		protected void okPressed() {
			if(bPerson.getSelection()){
				mine.addDisplayOption(Person.class);
			}
			if(bOrg.getSelection()){
				mine.addDisplayOption(Organisation.class);
			}
			mine.setSimpleName(tShort.getText());
			super.okPressed();
		}
		
	}
	
}
