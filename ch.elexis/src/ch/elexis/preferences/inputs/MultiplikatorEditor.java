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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import ch.elexis.data.PersistentObject;
import ch.elexis.dialogs.AddMultiplikatorDialog;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.TimeTool;

public class MultiplikatorEditor extends Composite {
	// String myClass;
	List list;
	final Stm stm = PersistentObject.getConnection().getStatement();
	String typeName;
	
	public MultiplikatorEditor(final Composite prnt, final String clazz){
		super(prnt, SWT.NONE);
		setLayout(new GridLayout());
		setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		list = new List(this, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
		list.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Button bNew = new Button(this, SWT.PUSH);
		bNew.setText(Messages.getString("MultiplikatorEditor.add")); //$NON-NLS-1$
		bNew.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				AddMultiplikatorDialog amd = new AddMultiplikatorDialog(getShell());
				if (amd.open() == Dialog.OK) {
					TimeTool t = amd.getBegindate();
					String mul = amd.getMult();
					
					MultiplikatorList multis = new MultiplikatorList("VK_PREISE", typeName);
					multis.insertMultiplikator(t, mul);

					list
						.add(Messages.getString("MultiplikatorEditor.from") + t.toString(TimeTool.DATE_GER) + Messages.getString("MultiplikatorEditor.14") + mul); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		});
		reload(clazz);
	}
	
	@Override
	public void dispose(){
		PersistentObject.getConnection().releaseStatement(stm);
	}
	
	@SuppressWarnings("unchecked")
	public void reload(final String typeName){
		this.typeName = typeName;
		ArrayList<String[]> daten = new ArrayList<String[]>();
		ResultSet res = stm.query("SELECT * FROM VK_PREISE WHERE TYP=" + JdbcLink.wrap(typeName)); //$NON-NLS-1$
		try {
			while ((res != null) && (res.next() == true)) {
				String[] row = new String[2];
				row[0] = res.getString("DATUM_VON"); //$NON-NLS-1$
				row[1] = res.getString("MULTIPLIKATOR"); //$NON-NLS-1$
				daten.add(row);
			}
			res.close();
			
			Collections.sort(daten, new Comparator() {
				
				public int compare(final Object o1, final Object o2){
					String[] s1 = (String[]) o1;
					String[] s2 = (String[]) o2;
					return s1[0].compareTo(s2[0]);
				}
				
			});
			
			TimeTool dis = new TimeTool();
			list.removeAll();
			for (String[] s : daten) {
				dis.set(s[0]);
				list
					.add(Messages.getString("MultiplikatorEditor.from") + dis.toString(TimeTool.DATE_GER) + Messages.getString("MultiplikatorEditor.5") + s[1]); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
		} catch (Exception ex) {
			ExHandler.handle(ex);
		} 
	}
	
	/**
	 * Class for manipulation of Multiplikator. <br>
	 * <b>ATTENTION</b> remember to call dispose to release the DB Statement.
	 * 
	 * @author thomashu
	 * 
	 */
	public static class MultiplikatorList {
		private ResultSet multiRes;
		private String typ;
		private String table;

		public MultiplikatorList(String table, String typ){
			this.typ = typ;
			this.table = table;
		}
		
		/**
		 * Update multiRes with ResultSet of all existing Multiplikators
		 */
		private void fetchResultSet(){
			Stm statement = PersistentObject.getConnection().getStatement();
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT * FROM ").append(table).append(" WHERE TYP=")
				.append(JdbcLink.wrap(typ));
			multiRes = statement.query(sql.toString());
			PersistentObject.getConnection().releaseStatement(statement);
		}
		
		public void insertMultiplikator(TimeTool dateFrom, String value){
			TimeTool dateTo = null;
			Stm statement = PersistentObject.getConnection().getStatement();
			try {
				fetchResultSet();
				// update existing multiplier for that date
				while (multiRes.next()) {
					TimeTool fromDate = new TimeTool(multiRes.getString("DATUM_VON"));
					TimeTool toDate = new TimeTool(multiRes.getString("DATUM_BIS"));
					if (dateFrom.isAfter(fromDate) && dateFrom.isBefore(toDate)) { // if contains update the to value of the existing multiplikator
						StringBuilder sql = new StringBuilder();
						// update the old to date
						TimeTool newToDate = new TimeTool(dateFrom);
						newToDate.addDays(-1);
						sql.append("UPDATE ")
							.append(table)
							.append(
								" SET DATUM_BIS="
									+ JdbcLink.wrap(newToDate.toString(TimeTool.DATE_COMPACT))
									+ " WHERE DATUM_VON="
									+ JdbcLink.wrap(fromDate.toString(TimeTool.DATE_COMPACT))
									+ " AND TYP=" + JdbcLink.wrap(typ));
						statement.exec(sql.toString());
						// set to date of new multiplikator to to date of old multiplikator
						dateTo = new TimeTool(toDate);
					} else if (dateFrom.isEqual(fromDate)) { // if from equals update the value
						StringBuilder sql = new StringBuilder();
						// update the value and return
						TimeTool newToDate = new TimeTool(dateFrom);
						newToDate.addDays(-1);
						sql.append("UPDATE ")
							.append(table)
							.append(
								" SET MULTIPLIKATOR="
									+ JdbcLink.wrap(value)
									+ " WHERE DATUM_VON="
									+ JdbcLink.wrap(fromDate.toString(TimeTool.DATE_COMPACT))
									+ " AND TYP=" + JdbcLink.wrap(typ));
						statement.exec(sql.toString());
						return;
					}
				}
				// if we have not found a to Date yet search for oldest existing
				if(dateTo == null) {
					fetchResultSet();
					dateTo = new TimeTool("99991231");
					while (multiRes.next()) {
						TimeTool fromDate = new TimeTool(multiRes.getString("DATUM_VON"));
						if(fromDate.isBefore(dateTo)) {
							dateTo.set(fromDate);
							dateTo.addDays(-1);
						}
					}
				}
				// create a new entry
				StringBuilder sql = new StringBuilder();
				sql.append("INSERT INTO ")
				.append(table)
					.append(
						" (DATUM_VON,DATUM_BIS,MULTIPLIKATOR,TYP) VALUES ("
							+ JdbcLink.wrap(dateFrom.toString(TimeTool.DATE_COMPACT)) + ","
							+ JdbcLink.wrap(dateTo.toString(TimeTool.DATE_COMPACT)) + ","
							+ JdbcLink.wrap(value) + "," + JdbcLink.wrap(typ) + ");");
				statement.exec(sql.toString());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				PersistentObject.getConnection().releaseStatement(statement);
			}
		}
		
		public double getMultiplikator(TimeTool date){
			// get Mutliplikator for date
			fetchResultSet();
			try {
				while (multiRes.next()) {
					TimeTool fromDate = new TimeTool(multiRes.getString("DATUM_VON"));
					TimeTool toDate = new TimeTool(multiRes.getString("DATUM_BIS"));
					if (date.isAfterOrEqual(fromDate) && date.isBeforeOrEqual(toDate)) {
						String value = multiRes.getString("MULTIPLIKATOR");
						if (value != null && !value.isEmpty()) {
							return Double.parseDouble(value);
						}
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 1.0;
		}
	}
}
