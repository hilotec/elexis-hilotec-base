/*******************************************************************************
 * Copyright (c) 2005-2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Person.java 2517 2007-06-12 20:07:18Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;


import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.TimeTool.TimeFormatException;

/**
 * Eine Person ist ein Kontakt mit zusätzlich Namen, Geburtsdatum und Geschlecht.
 * @author gerry
 *
 */
public class Person extends Kontakt {
	public static final String MALE = "m";
	public static final String FEMALE = "w";
	
	static{
		addMapping("KONTAKT",
				"Name			=	Bezeichnung1",
				"Vorname		=   Bezeichnung2",
				"Zusatz 		=	Bezeichnung3",
				"Geburtsdatum	=	S:D:Geburtsdatum",
				"Geschlecht",	"Natel=NatelNr", "istPerson", "Titel"
				);
	}
	
	public String getName(){
		return checkNull(get("Name"));
	}
	public String getVorname(){
		return checkNull(get("Vorname"));
	}
	public String getGeburtsdatum(){
		return checkNull(get("Geburtsdatum"));
	}
	public String getGeschlecht(){
		return checkNull(get("Geschlecht"));
	}
	public String getNatel(){
		return get("Natel");
	}
	
	public boolean isValid(){
		if(!super.isValid()){
			return false;
		}
		String geb=(get("Geburtsdatum"));
		if(geb.equals("WERT?")){
			return false;
		}
		String g=get("Geschlecht");
		if(g.equals("m") || g.equals("w")){
			return true;
		}
		return false;
	}
	/** Eine Person mit gegebener Id aus der Datenbank einlesen */
	public static Person load(String id){
	    Person ret=new Person(id);
        if(ret.get("Name")==null){
            return null;
        }
        return ret;
    }
	protected Person(String id){
		super(id);
	}
	public Person(){
		// System.out.println("Person");
	}
	
	/** Eine neue Person erstellen */
	public Person(String Name, String Vorname, String Geburtsdatum, String s)
	{
		create(null);
		//String[] vals=new String[]{Name,Vorname,new TimeTool(Geburtsdatum).toString(TimeTool.DATE_COMPACT),s};
		String[] vals=new String[]{Name,Vorname,Geburtsdatum,s};
		String[] fields=new String[]{"Name","Vorname","Geburtsdatum","Geschlecht"};
		set(fields,vals);
	}
	
	/**
     * This constructor is more critical than the previous one
     * @param name will be checked for non-alphabetic characters and may not be empty
     * @param vorname will be checked for non alphabetic characters but may be empty
     * @param gebDat will be checked for unplausible values but may be null
     * @param s will be checked for undefined values and may not be empty
     * @throws PersonDataException
     */
    public Person(String name, String vorname, TimeTool gebDat, String s) throws PersonDataException{
    	if((StringTool.isNothing(name)) || (!name.matches("["+StringTool.wordChars+"\\s-]+"))){
    		throw new PersonDataException(PersonDataException.CAUSE.LASTNAME);
    	}
    	if((!StringTool.isNothing(vorname)) && (!vorname.matches("["+StringTool.wordChars+"\\s-]+"))){
    		throw new PersonDataException(PersonDataException.CAUSE.FIRSTNAME);
    	}
    	String dat="";
    	if(gebDat!=null){
    		TimeTool now=new TimeTool();
	    	int myYear=now.get(TimeTool.YEAR);
	    	int oYear=gebDat.get(TimeTool.YEAR);
    		if(oYear>myYear || oYear<myYear-120){
    			throw new PersonDataException(PersonDataException.CAUSE.BIRTHDATE);
    		}
    		dat=gebDat.toString(TimeTool.DATE_COMPACT);
    	}
    	if(!s.matches("[mw]")){
    		throw new PersonDataException(PersonDataException.CAUSE.SEX);
    	}
    	create(null);
		String[] fields=new String[]{"Name","Vorname","Geburtsdatum","Geschlecht"};
    	String[] vals=new String[]{name,vorname,dat,s};
		set(fields,vals);
    }
	/**
	 * Return a short or long label for this Person
	 * 
	 * This implementation returns "<Vorname> <Name>" for both label types.
	 * 
	 * @return a label describing this Person
	 */
	public String getLabel(boolean shortLabel) {
		StringBuilder sb=new StringBuilder();
		
		if (shortLabel) {
			sb.append(getVorname()).append(" ").append(getName());
		} else {
			sb.append(getVorname()).append(" ").append(getName());
		}
		return sb.toString();
	}
	
	/** Einen String mit den Personalien holen */
	public String getPersonalia(){
		StringBuffer ret=new StringBuffer(200);
		String[] fields=new String[]{"Name","Vorname","Geburtsdatum","Geschlecht","Titel"};
		String[] vals=new String[fields.length];
		get(fields,vals);
		if(!StringTool.isNothing(vals[4])){
			ret.append(vals[4]).append(" ");
		}
		ret.append(vals[0]);
        if(!StringTool.isNothing(vals[1])){
            ret.append(" ").append(vals[1]);
        }
        if(StringTool.isNothing(vals[3])){
            ret.append(" ");
        }else {
            ret.append("(").append(vals[3]).append("), ");
        }
        if(!StringTool.isNothing(vals[2])){
			ret.append(new TimeTool(vals[2]).toString(TimeTool.DATE_GER));
        }
		return ret.toString();
	}
	@Override
	protected String getConstraint() {
		return "istPerson='1'";
	}
	@Override
	protected void setConstraint() {
		set("istPerson","1");
	}
	
	/**
	 * Statistik für ein bestimmtes Objekt führen
	 * @param ice
	 */
	public void countItem(ICodeElement ice){
		statForItem((PersistentObject)ice);	
	}
	
	public static class PersonDataException extends Exception{
		enum CAUSE{LASTNAME,FIRSTNAME,BIRTHDATE,SEX}
		static final String[] causes=new String[]{"Name","Vorname","Geburtsdatum","Geschlecht (m oder w)"};
		
		public CAUSE cause;
		PersonDataException(CAUSE cause){
			super(causes[cause.ordinal()]);
			this.cause=cause;
		}
	}
}
