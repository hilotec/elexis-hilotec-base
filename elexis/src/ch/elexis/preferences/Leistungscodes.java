/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: Leistungscodes.java 2841 2007-07-19 05:19:56Z rgw_ch $
 *******************************************************************************/
package ch.elexis.preferences;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
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
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ch.elexis.Hub;
import ch.elexis.data.PersistentObject;
import ch.elexis.preferences.inputs.MultiplikatorEditor;
import ch.elexis.util.Extensions;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class Leistungscodes extends PreferencePage implements
		IWorkbenchPreferencePage {
	public final static String CFG_KEY="billing/systems";
	List<IConfigurationElement> lo=Extensions.getExtensions("ch.elexis.RechnungsManager");
	List<IConfigurationElement> ll=Extensions.getExtensions("ch.elexis.Verrechnungscode");
	String[] systeme=Hub.globalCfg.keys(CFG_KEY);
	Table table;
	String[] tableCols={"Name","Leistungscode-System","Standard-Ausgabe","Multiplikator"};
	int[] tableWidths={60,120,120,70};
	
	@Override
	protected Control createContents(final Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout(2,false));
		Label l1=new Label(ret,SWT.NONE);
		l1.setText("Konfigurierte Abrechnungssysteme");
		l1.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		
		SWTHelper.createHyperlink(ret, "Neu...", new HyperlinkAdapter(){
			@Override
			public void linkActivated(final HyperlinkEvent e) {
				AbrechnungsTypDialog at=new AbrechnungsTypDialog(getShell(),null);
				if(at.open()==Dialog.OK){
					Hub.globalCfg.set(CFG_KEY+"/"+at.getResult(), "1");
					systeme=Hub.globalCfg.keys(CFG_KEY);
					reload();
				}
			}
			
		});
		table=new Table(ret,SWT.FULL_SELECTION|SWT.SINGLE);
		for(int i=0;i<tableCols.length;i++){
			TableColumn tc=new TableColumn(table,SWT.NONE);
			tc.setText(tableCols[i]);
			tc.setWidth(tableWidths[i]);
		}
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseDoubleClick(final MouseEvent e) {
				int idx=table.getSelectionIndex();
				if(idx!=-1){
					TableItem sel=table.getItem(idx);
					String ssel=sel.getText(0);
					for(String s1:systeme){
						if(s1.startsWith(ssel)){
							AbrechnungsTypDialog at=new AbrechnungsTypDialog(getShell(),s1);
							if(at.open()==Dialog.OK){
								reload();
							}
							
						}
					}
				}
			}
		});
		table.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		reload();
		return ret;
	}
	public void reload(){
		table.removeAll();
		//new Label(ret,SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(4, true, 1, false));
		if(systeme!=null){
			for(String s:systeme){
				String[] system=s.split(";");
				TableItem it=new TableItem(table,SWT.NONE);
				it.setText(0,system[0]);
				it.setText(1,system[1]);
				it.setText(2,system[2]);
				StringBuilder sql=new StringBuilder();
				String actdat=new TimeTool().toString(TimeTool.DATE_COMPACT);
				sql.append("SELECT MULTIPLIKATOR FROM ").append("VK_PREISE").append(" WHERE TYP=")
					.append(JdbcLink.wrap(system[0]))
					.append(" AND DATUM_VON <=").append(actdat)
				 .append(" AND DATUM_BIS >").append(actdat);
				String tp=PersistentObject.getConnection().queryString(sql.toString());
				if(StringTool.isNothing(tp)){
					tp="1.0";
				}
				it.setText(3,tp);
			}
		}
		//table.redraw();
	}

	
	public void init(final IWorkbench workbench) {
		// TODO Auto-generated method stub

	}
	class AbrechnungsTypDialog extends TitleAreaDialog{
		Text tName;
		Combo cbLstg;
		Combo cbRechn;
		Label lbTaxp;
		String result;
		MultiplikatorEditor mke;
		
		AbrechnungsTypDialog(final Shell shell, final String abrdef){
			super(shell);
			result=abrdef;
			
		}

		@Override
		protected Control createDialogArea(final Composite parent) {
			Composite ret=new Composite(parent,SWT.NONE);
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			ret.setLayout(new GridLayout(2,false));
			new Label(ret,SWT.NONE).setText("Name");
			tName=new Text(ret,SWT.BORDER);
			tName.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			tName.addFocusListener(new FocusAdapter(){

				@Override
				public void focusLost(final FocusEvent e) {
					mke.reload(tName.getText());
					super.focusLost(e);
				}
				
			});
			new Label(ret,SWT.NONE).setText("Leistungscode-System");
			cbLstg=new Combo(ret,SWT.READ_ONLY);
			cbLstg.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			for(IConfigurationElement ic:ll){
				cbLstg.add(ic.getAttribute("name"));
			}
			new Label(ret,SWT.NONE).setText("Standard-Rechnungsausgabe");
			cbRechn=new Combo(ret,SWT.READ_ONLY);
			cbRechn.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			for(IConfigurationElement ic:lo){
				cbRechn.add(ic.getAttribute("name"));
			}
			lbTaxp=new Label(ret,SWT.NONE);
			lbTaxp.setText("Multiplikator");
			lbTaxp.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			if(result!=null){
				String[] elem=result.split(";");
				tName.setText(elem[0]);
				cbLstg.setText(elem[1]);
				cbRechn.setText(elem[2]);
			}
			String name=result;
			if(StringTool.isNothing(name)){
				name="default";
			}else{
				name=result.split(";")[0];
			}
		
			mke=new MultiplikatorEditor(ret,name);
			mke.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		
			return ret;
		}

		@Override
		public void create() {
			super.create();
			setTitle("Abrechnungssystem definieren");
			setMessage("Geben Sie bitte die Daten für dieses Abrechnungssystem ein");
			getShell().setText("Abrechnungssystem");
			//mke.reload( (result==null) ? "default" : result.split(";")[0]);
		}

		@Override
		protected void okPressed() {
			result=tName.getText()+";"+cbLstg.getText()+";"+cbRechn.getText();
			super.okPressed();
		}
		public String getResult(){
			return result;
		}
		
	}

}
