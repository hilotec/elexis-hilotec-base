package ch.elexis.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.data.Kontakt;
import ch.elexis.data.LabItem;
import ch.elexis.data.Labor;
import ch.elexis.data.Query;
import ch.elexis.preferences.Messages;
import ch.elexis.scripting.ScriptEditor;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.WidgetFactory;
import ch.rgw.tools.StringTool;

public class EditLabItem extends TitleAreaDialog {
	
	// private String[]
	// fields={"Kürzel","Titel","Typ","Referenzbereich","Einheit"};
	Text iKuerzel, iTitel, iRef, iRfF, iUnit, iPrio;
	Combo cGroup;
	Button alph, numeric, abs, formula, document;
	String formel;
	org.eclipse.swt.widgets.List labors;
	Hashtable<String, Labor> lablist = new Hashtable<String, Labor>();
	Labor actLabor;
	LabItem result;
	ArrayList<String> groups;
	
	public EditLabItem(Shell parentShell, LabItem act){
		super(parentShell);
		
		groups = new ArrayList<String>();
		result = act;
		if (act == null) {
			String al =
				new Query<Labor>(Labor.class).findSingle(
					"istLabor", Messages.LaborPrefs_34, Messages.LaborPrefs_35); //$NON-NLS-1$
			if (al == null) {
				actLabor = new Labor(Messages.LaborPrefs_36, Messages.LaborPrefs_37);
			} else {
				actLabor = Labor.load(al);
			}
		} else {
			actLabor = act.getLabor();
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		getShell().setText(Messages.LaborPrefs_labParams);
		setTitle(Messages.LaborPrefs_enterNewLabParam);
		setMessage(Messages.LaborPrefs_pleaseEditParam);
		
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(4, false));
		labors = new org.eclipse.swt.widgets.List(ret, SWT.BORDER);
		labors.setLayoutData(SWTHelper.getFillGridData(4, true, 1, false));
		labors.addSelectionListener(new SelectionAdapter() {
			
			public void widgetSelected(SelectionEvent e){
				int i = labors.getSelectionIndex();
				if (i != -1) {
					actLabor = lablist.get(labors.getItem(i));
				}
				
			}
			
		});
		Query<Labor> qbe = new Query<Labor>(Labor.class);
		List<Labor> list = qbe.execute();
		int idx = 0, i = 0;
		String al = actLabor.getLabel();
		for (Labor o : list) {
			String lb = o.getLabel();
			lablist.put(lb, (Labor) o);
			labors.add(lb);
			if (lb.equals(al)) {
				idx = i;
			}
			i++;
		}
		labors.setSelection(idx);
		WidgetFactory.createLabel(ret, Messages.LaborPrefs_38);
		iKuerzel = new Text(ret, SWT.BORDER);
		iKuerzel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		WidgetFactory.createLabel(ret, Messages.LaborPrefs_39);
		iTitel = new Text(ret, SWT.BORDER);
		iTitel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		WidgetFactory.createLabel(ret, Messages.LaborPrefs_40);
		Group grp = new Group(ret, SWT.NONE);
		grp.setLayout(new FillLayout(SWT.HORIZONTAL));
		grp.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		numeric = new Button(grp, SWT.RADIO);
		numeric.setText(Messages.LaborPrefs_41);
		alph = new Button(grp, SWT.RADIO);
		alph.setText(Messages.LaborPrefs_42);
		abs = new Button(grp, SWT.RADIO);
		abs.setText(Messages.LaborPrefs_43);
		formula = new Button(grp, SWT.RADIO);
		formula.setText(Messages.LaborPrefs_44);
		formula.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				if (formula.getSelection()) {
					
					ScriptEditor se = new ScriptEditor(getShell(), formel, Messages.LaborPrefs_45);
					if (se.open() == Dialog.OK) {
						formel = se.getScript();
					}
				}
			}
			
		});
		document = new Button(grp, SWT.RADIO);
		document.setText(Messages.LaborPrefs_document);
		document.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				documentSelectionChanged();
			}
		});
		WidgetFactory.createLabel(ret, Messages.LaborPrefs_46);
		
		iRef = new Text(ret, SWT.BORDER);
		iRef.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		WidgetFactory.createLabel(ret, Messages.LaborPrefs_47);
		iRfF = new Text(ret, SWT.BORDER);
		iRfF.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		WidgetFactory.createLabel(ret, Messages.LaborPrefs_48);
		iUnit = new Text(ret, SWT.BORDER);
		iUnit.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		WidgetFactory.createLabel(ret, Messages.LaborPrefs_49);
		
		List<LabItem> labItems = LabItem.getLabItems();
		groups.clear();
		for (LabItem li : (List<LabItem>) labItems) {
			if (groups.contains(li.getGroup())) {
				continue;
			}
			groups.add(li.getGroup());
		}
		Collections.sort(groups);
		
		cGroup = new Combo(ret, SWT.SINGLE | SWT.DROP_DOWN);
		cGroup.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cGroup.setToolTipText(Messages.LaborPrefs_50);
		cGroup.setItems(groups.toArray(new String[0]));
		WidgetFactory.createLabel(ret, Messages.LaborPrefs_51);
		iPrio = new Text(ret, SWT.BORDER);
		iPrio.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		iPrio.setToolTipText(Messages.LaborPrefs_52);
		if (result != null) {
			iKuerzel.setText(result.getKuerzel());
			iTitel.setText(result.getName());
			if (result.getTyp() == LabItem.typ.NUMERIC) {
				numeric.setSelection(true);
			} else if (result.getTyp() == LabItem.typ.TEXT) {
				alph.setSelection(true);
			} else if (result.getTyp() == LabItem.typ.ABSOLUTE) {
				abs.setSelection(true);
			} else if (result.getTyp() == LabItem.typ.DOCUMENT) {
				document.setSelection(true);
				documentSelectionChanged();
			} else {
				formula.setSelection(true);
			}
			iUnit.setText(result.getEinheit());
			iRef.setText(result.get(Messages.LaborPrefs_53));
			iRfF.setText(result.getRefW());
			cGroup.setText(result.getGroup());
			iPrio.setText(result.getPrio());
			formel = result.getFormula();
		}
		return ret;
	}
	
	/**
	 * Event method is called when document radio button is selected or deselected
	 */
	private void documentSelectionChanged(){
		iRef.setEnabled(!document.getSelection());
		iRfF.setEnabled(!document.getSelection());
	}
	
	@Override
	protected void okPressed(){
		LabItem.typ typ;
		// String refmin="",refmax;
		// refmax=iRef.getText();
		if (iTitel.getText().length() < 1 && iPrio.getText().length() < 1) {
			setErrorMessage("Insert titel or sequenz number");
			return;
		}
		
		if (numeric.getSelection() == true) {
			typ = LabItem.typ.NUMERIC;
		} else if (abs.getSelection() == true) {
			typ = LabItem.typ.ABSOLUTE;
		} else if (formula.getSelection()) {
			typ = LabItem.typ.FORMULA;
		} else if (document.getSelection()) {
			typ = LabItem.typ.DOCUMENT;
		} else {
			typ = LabItem.typ.TEXT;
		}
		if (result == null) {
			result =
				new LabItem(iKuerzel.getText(), iTitel.getText(), actLabor, iRef.getText(), iRfF
					.getText(), iUnit.getText(), typ, cGroup.getText(), iPrio.getText());
		} else {
			String t = "0";
			if (typ == LabItem.typ.TEXT) {
				t = "1";
			} else if (typ == LabItem.typ.ABSOLUTE) {
				t = "2";
			} else if (typ == LabItem.typ.FORMULA) {
				t = "3";
			} else if (typ == LabItem.typ.DOCUMENT) {
				t = "4";
			}
			result.set(new String[] {
				Messages.LaborPrefs_58, Messages.LaborPrefs_59, Messages.LaborPrefs_60,
				Messages.LaborPrefs_61, Messages.LaborPrefs_62, Messages.LaborPrefs_63,
				Messages.LaborPrefs_64, Messages.LaborPrefs_65, Messages.LaborPrefs_66
			}, iKuerzel.getText(), iTitel.getText(), actLabor.getId(), iRef.getText(), iRfF
				.getText(), iUnit.getText(), t, cGroup.getText(), iPrio.getText());
		}
		if (!StringTool.isNothing(formel)) {
			result.setFormula(formel);
		}
		super.okPressed();
	}
	
	public void setShortDescText(String string){
		iKuerzel.setText(string);
	}
	
	public void setTitelText(String string){
		if (string != null)
			iTitel.setText(string);
	}
	
	public void setRefMText(String string){
		if (string != null)
			iRef.setText(string);
	}
	
	public void setRefFText(String string){
		if (string != null)
			iRfF.setText(string);
	}
	
	public void setUnitText(String string){
		if (string != null)
			iUnit.setText(string);
	}
	
	public void setSelectedLab(Kontakt lab){
		String[] items = labors.getItems();
		String searchItem = lab.getLabel();
		int idx = 0;
		for (; idx < items.length; idx++) {
			if (items[idx].equalsIgnoreCase(searchItem)) {
				labors.setSelection(idx);
				actLabor = lablist.get(labors.getItem(idx));
				return;
			}
		}
	}
}