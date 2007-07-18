/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: MultiplikatorEditor.java 2832 2007-07-18 15:05:58Z rgw_ch $
 *******************************************************************************/
package ch.elexis.preferences.inputs;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.data.PersistentObject;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.JdbcLink.Stm;

import com.tiff.common.ui.datepicker.DatePickerCombo;

public class MultiplikatorEditor extends Composite {
	//String myClass;
	List list;
	final Stm stm=PersistentObject.getConnection().getStatement();
	String typeName;
	
	public MultiplikatorEditor(final Composite prnt, final String clazz){
		super(prnt,SWT.NONE);
		setLayout(new GridLayout());
		setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		list=new List(this,SWT.BORDER|SWT.V_SCROLL|SWT.SINGLE);
		list.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		Button bNew =new Button(this,SWT.PUSH);
		bNew.setText(Messages.getString("MultiplikatorEditor.add")); //$NON-NLS-1$
		bNew.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(final SelectionEvent e) {
				AddMultiplikatorDialog amd=new AddMultiplikatorDialog(getShell());
				if(amd.open()==Dialog.OK){
					TimeTool t=amd.begindate;
					String mul=amd.mult;
					String datestring=JdbcLink.wrap(t.toString(TimeTool.DATE_COMPACT));
					stm.exec("UPDATE VK_PREISE SET DATUM_BIS="+datestring+" WHERE DATUM_BIS='99991231' AND TYP="+JdbcLink.wrap(typeName)); //$NON-NLS-1$ //$NON-NLS-2$
					stm.exec("INSERT INTO VK_PREISE (DATUM_VON,DATUM_BIS,MULTIPLIKATOR,TYP) VALUES (" //$NON-NLS-1$
							+t.toString(TimeTool.DATE_COMPACT)+",'99991231'," //$NON-NLS-1$
							+JdbcLink.wrap(mul)+","+JdbcLink.wrap(typeName)+");"); //$NON-NLS-1$ //$NON-NLS-2$
					list.add(Messages.getString("MultiplikatorEditor.from")+t.toString(TimeTool.DATE_GER)+Messages.getString("MultiplikatorEditor.14")+mul); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			
		});

		reload(clazz);
	}

	
	@Override
	public void dispose() {
		PersistentObject.getConnection().releaseStatement(stm);
	}


	@SuppressWarnings("unchecked")
	public void reload(final String typeName){
		this.typeName=typeName;
		ArrayList<String[]> daten= new ArrayList<String[]>();
		ResultSet res=stm.query("SELECT * FROM VK_PREISE WHERE TYP="+JdbcLink.wrap(typeName)); //$NON-NLS-1$
		try{
			while((res!=null) && (res.next()==true)){
				String[] row=new String[2];
				row[0]=res.getString("DATUM_VON"); //$NON-NLS-1$
				row[1]=res.getString("MULTIPLIKATOR"); //$NON-NLS-1$
				daten.add(row);
			}
			res.close();
			
			Collections.sort(daten,new Comparator(){

				public int compare(final Object o1, final Object o2) {
					String[] s1=(String[])o1;
					String[] s2=(String[])o2;
					return s1[0].compareTo(s2[0]);
				}
				
			});

			TimeTool dis=new TimeTool();
			list.removeAll();
			for(String[] s:daten){
				dis.set(s[0]);
				list.add(Messages.getString("MultiplikatorEditor.from")+dis.toString(TimeTool.DATE_GER)+Messages.getString("MultiplikatorEditor.5")+s[1]); //$NON-NLS-1$ //$NON-NLS-2$
			}
					
		}catch(Exception ex){
			ExHandler.handle(ex);
		}finally{

		}
	}
	class AddMultiplikatorDialog extends TitleAreaDialog{
		DatePickerCombo dpc;
		Text multi;
		TimeTool begindate;
		String mult;
		AddMultiplikatorDialog(final Shell shell){
			super(shell);
		}

		@Override
		protected Control createDialogArea(final Composite parent) {
			Composite ret=new Composite(parent,SWT.NONE);
			ret.setLayout(new GridLayout(2,false));
			ret.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
			dpc=new DatePickerCombo(ret,SWT.BORDER);
			dpc.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			multi=new Text(ret,SWT.BORDER);
			multi.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			return ret;
		}

		@Override
		public void create() {
			super.create();
			setTitle(Messages.getString("MultiplikatorEditor.BegiNDate")); //$NON-NLS-1$
			setMessage(Messages.getString("MultiplikatorEditor.PleaseEnterBeginDate")); //$NON-NLS-1$
			getShell().setText(Messages.getString("MultiplikatorEditor.NewMultipilcator")); //$NON-NLS-1$
		}

		@Override
		protected void okPressed() {
			begindate=new TimeTool(dpc.getDate().getTime());
			mult=multi.getText();
			super.okPressed();
		}
		
		
	};

}
