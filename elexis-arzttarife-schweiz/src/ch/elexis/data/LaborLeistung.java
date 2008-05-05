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
 * $Id: LaborLeistung.java 3870 2008-05-05 16:59:14Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.util.Hashtable;

import ch.elexis.util.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;


public class LaborLeistung extends VerrechenbarAdapter{
	
	static{
		addMapping("ARTIKEL", "Name","Text=Name","EK_Preis","VK_Preis","Typ","Code=SubID","ExtInfo"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
	}
	public static void createTable(){
		getConnection().exec("DELETE FROM ARTIKEL WHERE TYP='Laborleistung'"); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked") 
	LaborLeistung(final String code, String text, final String tp_vk){
		create(null);
		if(text.length()>78){
			Hashtable<String,String> ex=getHashtable("ExtInfo"); //$NON-NLS-1$
			ex.put("FullText",text); //$NON-NLS-1$
			text=text.substring(0,75);
			setHashtable("ExtInfo",ex); //$NON-NLS-1$
		}
		set(new String[]{"Code","Text","VK_Preis"},code,text.trim(),tp_vk); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	@Override
	protected String getTableName() {
		return "ARTIKEL"; //$NON-NLS-1$
	}
	public String[] getDisplayedFields(){
		return new String[]{"Code","Text"}; //$NON-NLS-1$ //$NON-NLS-2$
	}
	@Override
	public String getCode() {
		return get("Code"); //$NON-NLS-1$
	}

	@Override
	public String getText() {
		return checkNull(get("Text")); //$NON-NLS-1$
	}

	@Override
	public String getCodeSystemName() {
		return "Laborleistung"; //$NON-NLS-1$
	}

	/*
	public int getPreis(final TimeTool dat, final String subgroup) {
		String r=get("VK_Preis"); //$NON-NLS-1$
		return StringTool.isNothing(r) ? 0 : 
			(int)Math.round(Double.parseDouble(r)*getVKMultiplikator(dat, "EAL")*100); 
	}
	 */
	@Override
	public Money getKosten(final TimeTool dat) {
		String r=get("EK_Preis"); //$NON-NLS-1$
		return StringTool.isNothing(r) ? new Money(0) : 
			new Money((int)Math.round(Double.parseDouble(r)*getEKMultiplikator(dat, null)*100)); 
	}
	public static LaborLeistung load(final String id){
		return new LaborLeistung(id);
	}
	public LaborLeistung(){}
	protected LaborLeistung(final String id){
		super(id);
	}

	@Override
	protected String getConstraint() {
		return "Typ='Laborleistung'"; //$NON-NLS-1$
	}

	@Override
	protected void setConstraint() {
		set("Typ","Laborleistung"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getLabel() {
		StringBuilder ret=new StringBuilder();
		ret.append(getCode()).append(" ").append(getText()); //$NON-NLS-1$
		return ret.toString();
	}
	
	@Override
	public boolean isDragOK() {
		return true;
	}

	public int getTP(final TimeTool date, final Fall fall) {
		return checkZero(get("VK_Preis"))*100; //$NON-NLS-1$
	}

	public double getFactor(final TimeTool date, final Fall fall) {
		double ret= getVKMultiplikator(date, "EAL");
		if(ret==1.0){	// compatibility layer
			ret=getVKMultiplikator(date, "ch.elexis.data.LaborLeistung");
			if(ret!=1.0){
				PersistentObject.getConnection().exec("UPDATE VK_PREISE set typ='EAL' WHERE typ='ch.elexis.data.LaborLeistung'");
			}
		}
		return ret;
	}

}
