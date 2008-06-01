/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: Leistungscodes.java 3994 2008-06-01 18:08:38Z rgw_ch $
 *******************************************************************************/
package ch.elexis.preferences;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
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
import ch.elexis.data.Fall;
import ch.elexis.data.PersistentObject;
import ch.elexis.preferences.inputs.MultiplikatorEditor;
import ch.elexis.util.Extensions;
import ch.elexis.util.ListDisplay;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class Leistungscodes extends PreferencePage implements
		IWorkbenchPreferencePage {
	public final static String CFG_KEY="billing/systems";
	public final static String BILLING_STRICT="billing/strict";
	List<IConfigurationElement> lo=Extensions.getExtensions("ch.elexis.RechnungsManager");
	List<IConfigurationElement> ll=Extensions.getExtensions("ch.elexis.Verrechnungscode");
	String[] systeme=Hub.globalCfg.nodes(CFG_KEY);
	Table table;
	String[] tableCols={"Name","Leistungscode-System","Standard-Ausgabe","Multiplikator"};
	int[] tableWidths={60,120,120,70};
	Button bStrictCheck;
	
	@Override
	protected Control createContents(final Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout(2,false));
		Label l1=new Label(ret,SWT.NONE);
		l1.setText("Konfigurierte Abrechnungssysteme");
		l1.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		
		SWTHelper.createHyperlink(ret, "Neu...", new HyperlinkAdapter(){
			@Override
			public void linkActivated(final HyperlinkEvent e) {
				AbrechnungsTypDialog at=new AbrechnungsTypDialog(getShell(),null);
				if(at.open()==Dialog.OK){
					String[] result=at.getResult();
					String key=CFG_KEY+"/"+result[0];
					Hub.globalCfg.set(key+"/name",result[0]);
					Hub.globalCfg.set(key+"/leistungscodes",result[1]);
					Hub.globalCfg.set(key+"/standardausgabe", result[2]);
					Hub.globalCfg.set(key+"/bedingungen",result[3]);
					systeme=Hub.globalCfg.nodes(CFG_KEY);
					reload();
				}
			}
			
		});
		SWTHelper.createHyperlink(ret, "Löschen...", new HyperlinkAdapter(){

			@Override
			public void linkActivated(HyperlinkEvent e) {
				TableItem sel=table.getSelection()[0];
				String bName=sel.getText(0);
				if(SWTHelper.askYesNo("Wirklich "+bName+" Löschen?", "Diese Aktion lässt sich nicht rückgängig\nmachen und kann fehlehafte Fälle bewirken!")){
					Fall.removeAbrechnungssystem(bName);
					systeme=Hub.globalCfg.nodes(CFG_KEY);
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
						if(s1.equals(ssel)){
							String[] pre=new String[4];
							pre[0]=s1;
							pre[1]=Fall.getCodeSystem(s1);
							pre[2]=Fall.getDefaultPrintSystem(s1);
							pre[3]=Fall.getRequirements(s1);
							AbrechnungsTypDialog at=new AbrechnungsTypDialog(getShell(),pre);
							if(at.open()==Dialog.OK){
								String[] result=at.getResult();
								String key=CFG_KEY+"/"+result[0];
								Hub.globalCfg.set(key+"/name",result[0]);
								Hub.globalCfg.set(key+"/leistungscodes",result[1]);
								Hub.globalCfg.set(key+"/standardausgabe", result[2]);
								Hub.globalCfg.set(key+"/bedingungen",result[3]);
								systeme=Hub.globalCfg.nodes(CFG_KEY);
								reload();
							}
							
						}
					}
				}
			}
		});
		table.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		bStrictCheck=new Button(ret,SWT.CHECK);
		bStrictCheck.setText("Strenge Gültigkeitsprüfung beim Rechnung erstellen");
		bStrictCheck.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(final SelectionEvent e) {
				Hub.userCfg.set(BILLING_STRICT, bStrictCheck.getSelection());
			}
			
		});
		bStrictCheck.setSelection(Hub.userCfg.get(BILLING_STRICT, true));
		reload();
		return ret;
	}
	public void reload(){
		table.removeAll();
		//new Label(ret,SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(4, true, 1, false));
		if(systeme!=null){
			for(String s:systeme){
				String cfgkey=CFG_KEY+"/"+s+"/";
				TableItem it=new TableItem(table,SWT.NONE);
				String name=Hub.globalCfg.get(cfgkey+"name", "default");
				it.setText(0,name);
				it.setText(1,Hub.globalCfg.get(cfgkey+"leistungscodes", "?"));
				it.setText(2,Hub.globalCfg.get(cfgkey+"standardausgabe", "?"));
				StringBuilder sql=new StringBuilder();
				String actdat=new TimeTool().toString(TimeTool.DATE_COMPACT);
				sql.append("SELECT MULTIPLIKATOR FROM ").append("VK_PREISE").append(" WHERE TYP=")
					.append(JdbcLink.wrap(name))
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
		String[] result;
		MultiplikatorEditor mke;
		ListDisplay<String> ldRequirements;
		ListDisplay<String> ldConstants;
		
		AbrechnungsTypDialog(final Shell shell, final String[] abrdef){
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
			new Label(ret,SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			lbTaxp=new Label(ret,SWT.NONE);
			lbTaxp.setText("Multiplikator");
			lbTaxp.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

			Label lbReq=new Label(ret,SWT.NONE);
			lbReq.setText("Notwendige Daten");
						
			String name="default";
			if(result!=null){
				tName.setText(result[0]);
				cbLstg.setText(result[1]);
				cbRechn.setText(result[2]);
				name=result[0];
			}
			
			mke=new MultiplikatorEditor(ret,name);
			mke.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			ldRequirements=new ListDisplay<String>(ret, SWT.NONE, new ListDisplay.LDListener(){

				public void hyperlinkActivated(final String l) {
					String msg="Bitte geben Sie den Namen für diese Vorbedingung ein";
					InputDialog inp=new InputDialog(getShell(),l+" hinzufügen",msg,"",null);
					if(inp.open()==Dialog.OK){
						String req=inp.getValue();
						if(l.startsWith("Ko")){
							req+=":K";							// Kontakt
						}else if(l.trim().startsWith("T")){
							req+=":T";							// Text
						}else{
							req+=":D";							// Date
						}
						ldRequirements.add(req);
					}
				}

				public String getLabel(final Object o) {
					String[] l=((String)o).split(":");
					if(l.length>1){
						String type="Datum: ";
						if(l[1].equals("T")){
							type="Text: ";
						}else if(l[1].equals("K")){
							type="Kontakt: ";
						}
						return type+l[0];
					}else{
						return "? "+l[0];
					}
				}
				
			});
			ldRequirements.addHyperlinks("Kontakt... ","  Text... "," Datum... ");
			ldRequirements.setLayoutData(SWTHelper.getFillGridData(1, true, 4, true));
			if((result!=null) && (result.length>3) &&(result[3]!=null)){
				String[] reqs=result[3].split(";");
				for(String req:reqs){
					ldRequirements.add(req);
				}
			}
			Menu menu=new Menu(ldRequirements);
			MenuItem del=new MenuItem(menu,SWT.NONE);
			del.setText("Löschen");
			del.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(final SelectionEvent e) {
					String sel=ldRequirements.getSelection();
					ldRequirements.remove(sel);
				}
				
			});
			ldRequirements.setMenu(menu);
			new Label(ret,SWT.SEPARATOR|SWT.HORIZONTAL);
			new Label(ret,SWT.NONE).setText("Fallkonstanten");
			ldConstants=new ListDisplay<String>(ret,SWT.NONE,new ListDisplay.LDListener(){

				public String getLabel(Object o) {
					return (String)o;
				}

				public void hyperlinkActivated(String l) {
					String msg="Bitte geben Sie den Namen und Wert als 'Name=Wert' für diese Konstante ein";
					InputDialog inp=new InputDialog(getShell(),l+" hinzufügen",msg,"",null);
					if(inp.open()==Dialog.OK){
						String[] req=inp.getValue().split("=");
						if(req.length!=2){
							SWTHelper.showError("Falscheingabe", "Sie müssen eine Konstante in der Form Name=Wert eingeben");
						}else{
							ldConstants.add(inp.getValue());
							String bs=result[0];
							if(bs==null){
								bs=tName.getText();
							}
							if(StringTool.isNothing(bs)){
								SWTHelper.showError("Konstante anlegen nicht möglich", "Sie müssne zuerst einen Namen eingeben");
							}else{
								Fall.addBillingSystemConstant(bs, inp.getValue());
							}
						}
					}
					
				}});
			ldConstants.addHyperlinks("Konstante...");
			if(result!=null){
				for(String con:Fall.getBillingSystemConstants(result[0])){
					ldConstants.add(con);
				}
			}
			ldConstants.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			Menu menu2=new Menu(ldConstants);
			MenuItem del2=new MenuItem(menu2,SWT.NONE);
			del2.setText("Löschen");
			del2.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(final SelectionEvent e) {
					String sel=ldConstants.getSelection();
					ldConstants.remove(sel);
					Fall.removeBillingSystemConstant(result[0], sel);
				}
				
			});
			ldConstants.setMenu(menu2);
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
			result=new String[4];
			result[0]=tName.getText();
			result[1]=cbLstg.getText();
			result[2]=cbRechn.getText();
			result[3]=StringTool.join(ldRequirements.getAll(), ";");
			super.okPressed();
		}
		public String[] getResult(){
			return result;
		}
		
	}

}
