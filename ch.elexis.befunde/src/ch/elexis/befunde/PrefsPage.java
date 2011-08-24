/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/
package ch.elexis.befunde;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.data.Query;
import ch.elexis.scripting.ScriptEditor;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;

public class PrefsPage extends Composite {
	
	String[] mNames;
	Text[] texts;
	Button[] checkboxes;
	Label[] labels;
	Map<Object, Object> hash;
	String name;
	
	PrefsPage(final Composite parent, final Map<Object, Object> fields, final String name){
		super(parent, SWT.NONE);
		setLayout(new GridLayout(3, false));
		this.hash = fields;
		this.name = name;
		
		load();
	}
	
	void load(){
		if (texts != null) {
			for (int i = 0; i < texts.length; i++) {
				texts[i].dispose();
				checkboxes[i].dispose();
				labels[i].dispose();
			}
			mNames = null;
			checkboxes = null;
			labels = null;
			texts = null;
		}
		String fields = (String)hash.get(name + Messwert._FIELDS);
		if (StringTool.isNothing(fields)) {
			texts = new Text[1];
			checkboxes = new Button[1];
			labels = new Label[1];
			labels[0] = new Label(this, SWT.NONE);
			labels[0].setText("F1"); //$NON-NLS-1$
			texts[0] = SWTHelper.createText(this, 1, SWT.NONE);
			checkboxes[0] = new Button(this, SWT.CHECK);
			checkboxes[0].setText(Messages.getString("PrefsPage.multilineCaption")); //$NON-NLS-1$
			
		} else {
			mNames = fields.split(Messwert.SETUP_SEPARATOR);
			texts = new Text[mNames.length + 1];
			checkboxes = new Button[texts.length];
			labels = new Label[texts.length];
			for (int i = 0; i < texts.length; i++) {
				labels[i] = new Label(this, SWT.NONE);
				labels[i].setText("F" + Integer.toString(i + 1)); //$NON-NLS-1$
				labels[i].setForeground(Desk.getColor(Desk.COL_BLUE));
				labels[i].addMouseListener(new ScriptListener(i));
				texts[i] = SWTHelper.createText(this, 1, SWT.NONE);
				checkboxes[i] = new Button(this, SWT.CHECK);
				checkboxes[i].setText(Messages.getString("PrefsPage.multilineCaption")); //$NON-NLS-1$
				if (i < mNames.length) {
					String[] line = mNames[i].split(Messwert.SETUP_CHECKSEPARATOR);
					texts[i].setText(line[0]);
					if (line.length > 1) {
						checkboxes[i].setSelection(line[1].equals("m")); //$NON-NLS-1$
					}
				}
			}
		}
		layout();
		// Button bAddLine=new Button(this,SWT.PUSH);
		// bAddLine.setText("neue Zeile");
	}
	
	void flush(){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < texts.length; i++) {
			String n = texts[i].getText();
			if (StringTool.isNothing(n)) {
				continue;
			}
			String m = "m"; //$NON-NLS-1$
			if (checkboxes[i].getSelection() == false) {
				m = "s"; //$NON-NLS-1$
			}
			sb.append(n).append(Messwert.SETUP_CHECKSEPARATOR).append(m).append(
				Messwert.SETUP_SEPARATOR);
		}
		if (sb.length() > Messwert.SETUP_SEPARATOR.length()) {
			sb.setLength(sb.length() - Messwert.SETUP_SEPARATOR.length());
			hash.put(name + Messwert._FIELDS, sb.toString());
		}
	}
	
	boolean remove(){
		if (Hub.acl.request(ACLContributor.DELETE_PARAM)
			&& SWTHelper.askYesNo(Messages.getString("PrefsPage.warningNotUndoableCaption"), //$NON-NLS-1$
				Messages.getString("PrefsPage.warningConfirmMessage"))) { //$NON-NLS-1$
			Query<Messwert> qbe = new Query<Messwert>(Messwert.class);
			qbe.add(Messwert.FLD_NAME, Query.EQUALS, name);
			for (Messwert m : qbe.execute()) {
				m.delete();
			}
			hash.remove(name + Messwert._FIELDS);
			return true;
		}
		return false;
	}
	
	private class ScriptListener extends MouseAdapter {
		private int i;
		
		ScriptListener(int x){
			i = x;
		}
		
		@Override
		public void mouseUp(MouseEvent e){
			ScriptEditor se =
				new ScriptEditor(getShell(), texts[i].getText(), Messages
					.getString("PrefsPage.enterCalculationForThis")); //$NON-NLS-1$
			if (se.open() == Dialog.OK) {
				texts[i].setText(se.getScript());
			}
			
			super.mouseUp(e);
		}
		
	}
}
