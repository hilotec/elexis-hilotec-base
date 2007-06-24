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
 *  $Id: KassenbuchEintrag.java 2348 2007-05-07 14:57:47Z rgw_ch $
 *******************************************************************************/
package ch.elexis.buchhaltung.kassenbuch;

import java.io.ByteArrayInputStream;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.Money;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * A single cash journal entry.
 * @author Gerry
 *
 */
public class KassenbuchEintrag extends PersistentObject implements Comparable{
	private static final String TABLENAME="CH_ELEXIS_KASSENBUCH";
	public static final String VERSION="0.1.0";
	private static final String createDB="CREATE TABLE "+TABLENAME+"("+
		"ID		VARCHAR(25) primary key,"+
		"Nr	    VARCHAR(25),"+
		"Date   CHAR(8),"+
		"Amount CHAR(8),"+
		"Total  CHAR(8),"+
		"Entry  VARCHAR(80)"+
		");"+
		"INSERT INTO "+TABLENAME+" (ID,Nr,Date,Entry) VALUES ('1','-','"+
			new TimeTool().toString(TimeTool.DATE_COMPACT)+"','"+VERSION+
			"');";;
	
	static{
		addMapping(TABLENAME, "Betrag=Amount", "Text=Entry", "Datum=S:D:Date","Saldo=Total","BelegNr=Nr");
		KassenbuchEintrag version=KassenbuchEintrag.load("1");
		if(!version.exists()){
			try{
				ByteArrayInputStream bais=new ByteArrayInputStream(createDB.getBytes("UTF-8"));
				j.execScript(bais,true, false);
			}catch(Exception ex){
				ExHandler.handle(ex);
			}
		}
	}
	
	/**
	 * Create a new entry and recalculate the whole cash journal
	 * @param beleg identification of the new journal entry
	 * @param date date of transaction
	 * @param amount amount of transaction
	 * @param text title for the transaction
	 */
	public KassenbuchEintrag(String beleg, String date, Money amount, String text){
		create(null);
		set(new String[]{"BelegNr","Datum","Betrag","Text"},
				beleg,date,amount.getCentsAsString(),text);
		recalc();
	}
	/**
	 * create an identifier for the next journal entry. If the given previous entry kbe has an identifier
	 * that begins  with a numeric part, the next number of that numeric part will be generated. Otherwise
	 * the previous identifier, followed ba an "  a" will be returned 
	 * @param kbe the previous journal entry
	 * @return an identifier for the next journal entry.
	 */
	public static String nextNr(KassenbuchEintrag kbe){
		String ret="1";
		if(kbe!=null){
			String prev=kbe.getBelegNr().split("[^0-9]",2)[0];
			
			if(prev.matches("[0-9]+")){
				int num=Integer.parseInt(prev);
				ret=Integer.toString(num+1);
			}else{
				ret=kbe.getBelegNr()+" a";
			}
		}
		return ret;
	}
	/**
	 * create a new journal entry without recalculating the whole cash journal. Instead, take the balance out
	 * of the given last entry.
	 * @param beleg Identifier for the entry. Can be null or "", then it will be generated automatically
	 * @param date date for the transaction. Can be null, then today will be assumed
	 * @param amount sum of the transaction
	 * @param text title for the transction
	 * @param last previous transaction containing correct balance of the cash book
	 */
	public KassenbuchEintrag(String beleg, String date, Money amount, String text, KassenbuchEintrag last){
		create(null);
		Money sum=new Money(amount);
		if(last!=null){
			sum.addMoney(last.getSaldo());
		}
		if(date==null){
			date=new TimeTool().toString(TimeTool.DATE_GER);
		}
		if(StringTool.isNothing(beleg) || (!beleg.matches("[0-9]+.*"))){
			beleg=nextNr(last)+beleg;
		}
		set(new String[]{"BelegNr","Datum","Betrag","Text","Saldo"},
				beleg,date,amount.getCentsAsString(),text,sum.getCentsAsString());
	}
	/** return the identifier for the entry */
	public String getBelegNr(){
		return checkNull(get("BelegNr"));
	}
	/** return the date of the transaction */
	public String getDate(){
		return checkNull(get("Datum"));
	}
	
	/** return the amount of the transaction (may be positive or negative) */
	public Money getAmount(){
		return new Money(checkZero(get("Betrag")));
	}
	
	/** return the balance */
	public Money getSaldo(){
		return new Money(checkZero(get("Saldo")));
	}
	
	/** return the text of the transaction */
	public String getText(){
		return get("Text");
	}
	
	/**
	 *  recalculate the whole journal 
	 * 
	 * @return the last journal entry
	 */
	public static KassenbuchEintrag recalc(){
		KassenbuchEintrag ret=null;
		Money sum=new Money();
		for(KassenbuchEintrag kb:getBookings(null,null)){
			sum.addMoney(kb.getAmount());
			kb.set("Saldo",sum.getCentsAsString());
			ret=kb;
		}
		return ret;
	}
	
	/**
	 * return a sorted set of all entries. The bookings are sorted by BelegNr
	 * @return a set that is guaranteed to be sorted by BelegNr
	 */
	public static SortedSet<KassenbuchEintrag> getBookings(TimeTool from, TimeTool until){
		Query<KassenbuchEintrag> qbe=new Query<KassenbuchEintrag>(KassenbuchEintrag.class);
		qbe.add("BelegNr", "<>", "-");
		if(from!=null){
			qbe.add("Datum", ">=", from.toString(TimeTool.DATE_COMPACT));
		}
		if(until!=null){
			qbe.add("Datum", "<=", until.toString(TimeTool.DATE_COMPACT));
		}
		//qbe.orderBy(false, "BelegNr");
		TreeSet<KassenbuchEintrag> ts=new TreeSet<KassenbuchEintrag>();
		return (SortedSet<KassenbuchEintrag>)qbe.execute(ts);

	}
	@Override
	public String getLabel() {
		return getAmount().getAmountAsString()+" "+getText();
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}

	public static KassenbuchEintrag load(String id){
		return new KassenbuchEintrag(id);
	}
	protected KassenbuchEintrag(){
		
	}
	protected KassenbuchEintrag(String id){
		super(id);
		
	}

	/**
	 * The comparator is used to create a sorted set of the bookings. It scans the identifier (BelegNr) for
	 * a numerical part and compares these numbers. If the numbers are identical or not found, a textual
	 * comparison is used.
	 */
	public int compareTo(Object o) {
		KassenbuchEintrag k2=(KassenbuchEintrag)o;
		String [] s1=getBelegNr().split("[^0-9]",2);
		String [] s2=k2.getBelegNr().split("[^0-9]",2);
		int res=0;
		if(s1[0].matches("[0-9]+") && s2[0].matches("[0-9]+")){
			res=Integer.parseInt(s1[0])-Integer.parseInt(s2[0]);
		}else{
			res=s1[0].compareTo(s2[0]);
		}
		if(res==0){
			if(s1.length>s2.length){
				return 1;
			}
			if(s1.length<s2.length){
				return -1;
			}
			return s1[1].compareTo(s2[1]);
		}
		return res;
	}
}
