/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: VerrechenbarAdapter.java 1204 2006-11-01 15:56:38Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.util.ArrayList;
import java.util.Comparator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IFilter;

import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.dialogs.AddElementToBlockDialog;
import ch.elexis.util.IOptifier;
import ch.elexis.util.Money;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.TimeTool;

public abstract class VerrechenbarAdapter extends PersistentObject implements
		IVerrechenbar {

	protected IAction addToBlockAction;
	
		@Override
	public String getLabel() {
		return getText();
	}


	@Override
	protected abstract String getTableName();
	
	
	public String getCode() {
		return null;
	}


	public String getCodeSystemName() {
		return null;
	}


	public String getText() {
		return null;
	}

	public IOptifier getOptifier() {
		return optifier;
	}

	public Comparator getComparator() {
		return comparator;
	}

	public IFilter getFilter(Mandant m) {
		return ifilter;
	}
	

	public Iterable<IAction> getActions() {
		ArrayList<IAction> actions=new ArrayList<IAction>(1);
		actions.add(addToBlockAction);
		return actions;
	}


	public Double getVKMultiplikator(TimeTool date, String subgroup){
		return getMultiplikator(date,"VK_PREISE",subgroup);
	}
	public Double getEKMultiplikator(TimeTool date, String subgroup){
		return getMultiplikator(date,"EK_PREISE",subgroup);
	}
	private Double getMultiplikator(TimeTool date, String table,String subgroup){
		String actdat=JdbcLink.wrap(date.toString(TimeTool.DATE_COMPACT));
		StringBuilder sql=new StringBuilder();
		sql.append("SELECT MULTIPLIKATOR FROM ").append(table).append(" WHERE TYP=");
		if(subgroup==null){
			sql.append(JdbcLink.wrap(getClass().getName()));
		}else{
			sql.append(JdbcLink.wrap(getClass().getName()+subgroup));
		}
		 sql.append(" AND DATUM_VON <=").append(actdat)
		 .append(" AND DATUM_BIS >").append(actdat);
		String res=j.queryString(sql.toString()+" AND ID="+getWrappedId());
		if(res==null){
			res=j.queryString(sql.toString());
		}
		return res==null ? 1.0 : Double.parseDouble(res);
	}
	public Money getKosten(TimeTool dat) {
		return new Money(0);
	}
	public int getMinutes(){
		return 0;
	}
	protected VerrechenbarAdapter(String id){
		super(id);
	}
	public String getCodeSystemCode(){
		return "999";
	}
	protected VerrechenbarAdapter(){
		makeActions(this);
	}
	
	private void makeActions(final ICodeElement el){
		addToBlockAction=new Action("Zu Leistungsblock..."){
			public void run(){
				AddElementToBlockDialog adb=new AddElementToBlockDialog(Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getShell());
				if(adb.open()==Dialog.OK){
					ICodeElement ice=(ICodeElement) GlobalEvents.getInstance().getSelectedObject(el.getClass());
					Leistungsblock lb=adb.getResult();
					lb.addElement(ice);
					GlobalEvents.getInstance().fireUpdateEvent(Leistungsblock.class);
				}
			}
		};
	}
}
