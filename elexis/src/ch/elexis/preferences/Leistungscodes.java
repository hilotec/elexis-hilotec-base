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
 * $Id: Leistungscodes.java 2864 2007-07-22 08:59:41Z rgw_ch $
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
	List<IConfigurationElement> lo=Extensions.getExtensions("ch.elexis.RechnungsManager");
	List<IConfigurationElement> ll=Extensions.getExtensions("ch.elexis.Verrechnungscode");
	String[] systeme=Hub.globalCfg.nodes(CFG_KEY);
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
		ListDisplay<String> ld;
		
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
			ld=new ListDisplay<String>(ret, SWT.NONE, new ListDisplay.LDListener(){

				public void hyperlinkActivated(String l) {
					String msg="Bitte geben Sie den Namen für diese Vorbedingung ein";
					InputDialog inp=new InputDialog(getShell(),l+" hinzufügen",msg,"",null);
					if(inp.open()==Dialog.OK){
						String req=inp.getValue();
						if(l.startsWith("Ko")){
							req+=":K";
						}else{
							req+=":T";
						}
						ld.add(req);
					}
				}

				public String getLabel(Object o) {
					String[] l=((String)o).split(":");
					if(l.length>1){
						return (l[1].equals("T") ? "Text: " : "Kontakt: ")+l[0];
					}else{
						return "? "+l[0];
					}
				}
				
			});
			ld.addHyperlinks("Kontakt... "," Text...");
			ld.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			if((result!=null) && (result.length>3) &&(result[3]!=null)){
				String[] reqs=result[3].split(";");
				for(String req:reqs){
					ld.add(req);
				}
			}
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
			result[3]=StringTool.join(ld.getAll(), ";");
			super.okPressed();
		}
		public String[] getResult(){
			return result;
		}
		
	}

}
