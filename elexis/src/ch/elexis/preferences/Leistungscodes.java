/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: Leistungscodes.java 4931 2009-01-13 11:43:07Z rgw_ch $
 *******************************************************************************/
package ch.elexis.preferences;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
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

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.data.Fall;
import ch.elexis.data.PersistentObject;
import ch.elexis.preferences.inputs.MultiplikatorEditor;
import ch.elexis.util.Extensions;
import ch.elexis.util.ListDisplay;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class Leistungscodes extends PreferencePage implements
		IWorkbenchPreferencePage {
	public final static String CFG_KEY="billing/systems"; //$NON-NLS-1$
	public final static String BILLING_STRICT="billing/strict"; //$NON-NLS-1$
	public final static String OPTIFY="billing/optify"; //$NON-NLS-1$
	List<IConfigurationElement> lo=Extensions.getExtensions("ch.elexis.RechnungsManager"); //$NON-NLS-1$
	List<IConfigurationElement> ll=Extensions.getExtensions("ch.elexis.Verrechnungscode"); //$NON-NLS-1$
	String[] systeme=Hub.globalCfg.nodes(CFG_KEY);
	Table table;
	String[] tableCols={Messages.Leistungscodes_nameOfBillingSystem,Messages.Leistungscodes_billingSystem,Messages.Leistungscodes_defaultOutput,Messages.Leistungscodes_multiplier};
	int[] tableWidths={60,120,120,70};
	Button bStrictCheck;
	
	@Override
	protected Control createContents(final Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayout(new GridLayout(2,false));
		Label l1=new Label(ret,SWT.NONE);
		l1.setText(Messages.Leistungscodes_billingSystems);
		l1.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		
		SWTHelper.createHyperlink(ret, Messages.Leistungscodes_new, new HyperlinkAdapter(){
			@Override
			public void linkActivated(final HyperlinkEvent e) {
				AbrechnungsTypDialog at=new AbrechnungsTypDialog(getShell(),null);
				if(at.open()==Dialog.OK){
					String[] result=at.getResult();
					String key=CFG_KEY+"/"+result[0]; //$NON-NLS-1$
					Hub.globalCfg.set(key+"/name",result[0]); //$NON-NLS-1$
					Hub.globalCfg.set(key+"/leistungscodes",result[1]); //$NON-NLS-1$
					Hub.globalCfg.set(key+"/standardausgabe", result[2]); //$NON-NLS-1$
					Hub.globalCfg.set(key+"/bedingungen",result[3]); //$NON-NLS-1$
					systeme=Hub.globalCfg.nodes(CFG_KEY);
					reload();
				}
			}
			
		});
		SWTHelper.createHyperlink(ret, Messages.Leistungscodes_delete, new HyperlinkAdapter(){

			@Override
			public void linkActivated(HyperlinkEvent e) {
				TableItem sel=table.getSelection()[0];
				String bName=sel.getText(0);
				if(SWTHelper.askYesNo(MessageFormat.format(Messages.Leistungscodes_reallyDelete,bName), Messages.Leistungscodes_notUndoable)){
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
								String key=CFG_KEY+"/"+result[0]; //$NON-NLS-1$
								Hub.globalCfg.set(key+"/name",result[0]); //$NON-NLS-1$
								Hub.globalCfg.set(key+"/leistungscodes",result[1]); //$NON-NLS-1$
								Hub.globalCfg.set(key+"/standardausgabe", result[2]); //$NON-NLS-1$
								Hub.globalCfg.set(key+"/bedingungen",result[3]); //$NON-NLS-1$
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
		bStrictCheck.setText(Messages.Leistungscodes_strictValidityCheck);
		bStrictCheck.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(final SelectionEvent e) {
				Hub.userCfg.set(BILLING_STRICT, bStrictCheck.getSelection());
			}
			
		});
		bStrictCheck.setSelection(Hub.userCfg.get(BILLING_STRICT, true));
		bStrictCheck.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		final Button bOptify=new Button(ret,SWT.CHECK);
		bOptify.setText(Messages.Leistungscodes_checkPositions);
		bOptify.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(final SelectionEvent e) {
				Hub.userCfg.set(OPTIFY, bOptify.getSelection());
			}
			
		});
		bOptify.setSelection(Hub.userCfg.get(OPTIFY, true));
		bOptify.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		reload();
		return ret;
	}
	public void reload(){
		table.removeAll();
		//new Label(ret,SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(4, true, 1, false));
		if(systeme!=null){
			for(String s:systeme){
				String cfgkey=CFG_KEY+"/"+s+"/"; //$NON-NLS-1$ //$NON-NLS-2$
				TableItem it=new TableItem(table,SWT.NONE);
				String name=Hub.globalCfg.get(cfgkey+"name", "default"); //$NON-NLS-1$ //$NON-NLS-2$
				it.setText(0,name);
				it.setText(1,Hub.globalCfg.get(cfgkey+Messages.Leistungscodes_0, "?"));  //$NON-NLS-1$
				it.setText(2,Hub.globalCfg.get(cfgkey+"standardausgabe", "?")); //$NON-NLS-1$ //$NON-NLS-2$
				StringBuilder sql=new StringBuilder();
				String actdat=new TimeTool().toString(TimeTool.DATE_COMPACT);
				sql.append("SELECT MULTIPLIKATOR FROM ").append("VK_PREISE").append(" WHERE TYP=") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					.append(JdbcLink.wrap(name))
					.append(" AND DATUM_VON <=").append(JdbcLink.wrap(actdat)) //$NON-NLS-1$
				 .append(" AND DATUM_BIS >").append(JdbcLink.wrap(actdat)); //$NON-NLS-1$
				String tp=PersistentObject.getConnection().queryString(sql.toString());
				if(StringTool.isNothing(tp)){
					if(Hub.getSystemLogLevel()>Log.INFOS){
						SWTHelper.alert(Messages.Leistungscodes_didNotFindMulitplier, Messages.Leistungscodes_query+sql.toString());
					}
					tp="1.0"; //$NON-NLS-1$
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
			new Label(ret,SWT.NONE).setText(Messages.Leistungscodes_nameLabel);
			tName=new Text(ret,SWT.BORDER);
			tName.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			tName.addFocusListener(new FocusAdapter(){

				@Override
				public void focusLost(final FocusEvent e) {
					mke.reload(tName.getText());
					super.focusLost(e);
				}
				
			});
			new Label(ret,SWT.NONE).setText(Messages.Leistungscodes_billingSystemLabel);
			cbLstg=new Combo(ret,SWT.READ_ONLY);
			cbLstg.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			for(IConfigurationElement ic:ll){
				cbLstg.add(ic.getAttribute("name")); //$NON-NLS-1$
			}
			new Label(ret,SWT.NONE).setText(Messages.Leistungscodes_defaultOutputLabel);
			cbRechn=new Combo(ret,SWT.READ_ONLY);
			cbRechn.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			for(IConfigurationElement ic:lo){
				cbRechn.add(ic.getAttribute("name")); //$NON-NLS-1$
			}
			new Label(ret,SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			lbTaxp=new Label(ret,SWT.NONE);
			lbTaxp.setText(Messages.Leistungscodes_multiplierLabel);
			lbTaxp.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

			Label lbReq=new Label(ret,SWT.NONE);
			lbReq.setText(Messages.Leistungscodes_necessaryData);
						
			String name="default"; //$NON-NLS-1$
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
					String msg=Messages.Leistungscodes_pleaseEnterName;
					InputDialog inp=new InputDialog(getShell(),l+Messages.Leistungscodes_add,msg,"",null); //$NON-NLS-1$
					if(inp.open()==Dialog.OK){
						String req=inp.getValue();
						String contactDef=Messages.Leistungscodes_contact.substring(0, 2);
						if(l.startsWith(contactDef)){ //$NON-NLS-1$
							req+=":K";							// Kontakt //$NON-NLS-1$
						}else if(l.trim().startsWith("T")){ //$NON-NLS-1$
							req+=":T";							// Text //$NON-NLS-1$
						}else{
							req+=":D";							// Date //$NON-NLS-1$
						}
						ldRequirements.add(req);
					}
				}

				public String getLabel(final Object o) {
					String[] l=((String)o).split(":"); //$NON-NLS-1$
					if(l.length>1){
						String type=Messages.Leistungscodes_date;
						if(l[1].equals("T")){ //$NON-NLS-1$
							type=Messages.Leistungscodes_text;
						}else if(l[1].equals("K")){ //$NON-NLS-1$
							type=Messages.Leistungscodes_contact;
						}
						return type+l[0];
					}else{
						return "? "+l[0]; //$NON-NLS-1$
					}
				}
				
			});
			ldRequirements.addHyperlinks(Messages.Leistungscodes_contactHL,Messages.Leistungscodes_textHL,Messages.Leistungscodes_dateHL);
			ldRequirements.setLayoutData(SWTHelper.getFillGridData(1, true, 4, true));
			if((result!=null) && (result.length>3) &&(result[3]!=null)){
				String[] reqs=result[3].split(";"); //$NON-NLS-1$
				for(String req:reqs){
					ldRequirements.add(req);
				}
			}
			/*
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
			*/
			Action delItemAction=new Action(Messages.Leistungscodes_deleteAction){
				{
					setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_REMOVEITEM));
					setToolTipText(Messages.Leistungscodes_removeConstraintTT);
				}

				@Override
				public void run(){
					String sel=ldRequirements.getSelection();
					ldRequirements.remove(sel);
				}
				
			};
			ldRequirements.setMenu(delItemAction);
			new Label(ret,SWT.SEPARATOR|SWT.HORIZONTAL);
			new Label(ret,SWT.NONE).setText(Messages.Leistungscodes_caseConstants);
			ldConstants=new ListDisplay<String>(ret,SWT.NONE,new ListDisplay.LDListener(){

				public String getLabel(Object o) {
					return (String)o;
				}

				public void hyperlinkActivated(String l) {
					String msg=Messages.Leistungscodes_pleaseEnterNameAndValue;
					InputDialog inp=new InputDialog(getShell(),l+Messages.Leistungscodes_add,msg,"",null); //$NON-NLS-1$
					if(inp.open()==Dialog.OK){
						String[] req=inp.getValue().split("="); //$NON-NLS-1$
						if(req.length!=2){
							SWTHelper.showError(Messages.Leistungscodes_badEntry, Messages.Leistungscodes_explainEntry);
						}else{
							ldConstants.add(inp.getValue());
							String bs=result[0];
							if(bs==null){
								bs=tName.getText();
							}
							if(StringTool.isNothing(bs)){
								SWTHelper.showError(Messages.Leistungscodes_badEntryCaptiob, Messages.Leistungscodes_badEntryText);
							}else{
								Fall.addBillingSystemConstant(bs, inp.getValue());
							}
						}
					}
					
				}});
			ldConstants.addHyperlinks(Messages.Leistungscodes_constantHL);
			if(result!=null){
				for(String con:Fall.getBillingSystemConstants(result[0])){
					ldConstants.add(con);
				}
			}
			ldConstants.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			Menu menu2=new Menu(ldConstants);
			MenuItem del2=new MenuItem(menu2,SWT.NONE);
			del2.setText(Messages.Leistungscodes_delText);
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
			setTitle(Messages.Leistungscodes_defineBillingSystem);
			setMessage(Messages.Leistungscodes_pleaseEnterDataForBillingSystem);
			getShell().setText(Messages.Leistungscodes_billingSystemCaption);
			//mke.reload( (result==null) ? "default" : result.split(";")[0]);
		}

		@Override
		protected void okPressed() {
			result=new String[4];
			result[0]=tName.getText();
			result[1]=cbLstg.getText();
			result[2]=cbRechn.getText();
			result[3]=StringTool.join(ldRequirements.getAll(), ";"); //$NON-NLS-1$
			super.okPressed();
		}
		public String[] getResult(){
			return result;
		}
		
	}

}
