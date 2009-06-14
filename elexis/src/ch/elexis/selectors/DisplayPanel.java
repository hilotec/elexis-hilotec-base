package ch.elexis.selectors;

import java.util.List;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ColumnLayout;

import ch.elexis.data.PersistentObject;

/**
 * A Panel to display ActiveControls as views to fields of a single PersistentObject
 * 
 * @author gerry
 * 
 */
public class DisplayPanel extends Composite {
	SelectorPanel panel;
	
	public DisplayPanel(Composite parent,
			FieldDescriptor<? extends PersistentObject>[] fields, int minCols,
			int maxCols) {
		super(parent, SWT.NONE);
		ColumnLayout cl = new ColumnLayout();
		cl.minNumColumns = minCols > 0 ? minCols : 1;
		cl.maxNumColumns = maxCols > minCols ? maxCols : minCols + 2;
		setLayout(cl);
		panel=new SelectorPanel(this);
		for (FieldDescriptor<? extends PersistentObject> field : fields) {
			ActiveControl ac = null;
			switch (field.getFieldType()) {
			case HYPERLINK:
			case STRING:
				ac = new TextField(panel.getFieldParent(), 0, field.getLabel());
				break;
			case CURRENCY:
				ac = new MoneyField(panel.getFieldParent(), 0, field.getLabel());
				break;
			case DATE:
				ac = new DateField(panel.getFieldParent(), 0, field.getLabel());
				break;
			
			case COMBO:
				ac =
					new ComboField(panel.getFieldParent(), 0, field.getLabel(), (String[]) field.getExtension());
				break;
			case INT:
				ac = new IntegerField(panel.getFieldParent(), 0, field.getLabel());
			}
			ac.setData(ActiveControl.PROP_FIELDNAME, field.getFieldname());
			ac.setData(ActiveControl.PROP_HASHNAME, field.getHashname());
			panel.addField(ac);
		}
	}
	
	/**
	 * Set the Object to display
	 * @param po a PersistentObject that must have all fields defined, that are referenced by ActiveControls
	 * of this Panel
	 */
	public void setObject(PersistentObject po){
		List<ActiveControl> ctls=panel.getControls();
		for(ActiveControl ac:ctls){
			String field=ac.getProperty(ActiveControl.PROP_FIELDNAME);
			ac.setText(po.get(field));
		}
	}
}
