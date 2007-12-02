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
 * $Id$
 *******************************************************************************/

package ch.elexis.matchers;

import java.util.List;

import ch.elexis.data.Anschrift;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Organisation;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class KontaktMatcher {

	public static Organisation findOrganisation(final String name, final String strasse, final String plz, final String ort, final boolean createIfNotExists){
		Query<Organisation> qbe=new Query<Organisation>(Organisation.class);
		qbe.add("Name", "=", name);
		List<Organisation> found=qbe.execute();
		if(found.size()==0){
			if(createIfNotExists){
				Organisation org= new Organisation(name,"");
				addAddress(org,strasse,plz,ort);
				return org;
			}
		}
		if(found.size()==1){
			return found.get(0);
		}
		return (Organisation)matchAddress(found.toArray(new Kontakt[0]),strasse,plz,ort, null);
	}
	
	public static Person findPerson(final String name, final String vorname, final String gebdat,
				final String gender, final String strasse, final String plz, final String ort,
				final String natel, final boolean createIfNotExists){
		
		Query<Person> qbe=new Query<Person>(Person.class);
		String sex="";
		String birthdate="";
		
		if(!StringTool.isNothing(name)){
			qbe.add("Name", "=", StringTool.normalizeCase(name));
		}
		if(!StringTool.isNothing(vorname)){
			qbe.add("Vorname", "LIKE", StringTool.normalizeCase(vorname)+"%");
		}
		if(!StringTool.isNothing(gebdat)){
			TimeTool tt=new TimeTool();
			if(tt.set(gebdat)){
				birthdate=tt.toString(TimeTool.DATE_GER);
				qbe.add("Geburtsdatum", "=", tt.toString(TimeTool.DATE_COMPACT));
			}
		}
		if(!StringTool.isNothing(gender)){
			String gl=gender.toLowerCase();
			if(gender.startsWith("f") || gender.startsWith("w")){
				sex="w";
			}else if(gender.startsWith("m")){
				sex="m";
			}else{
				sex="?";
			}
			qbe.add("Geschlecht", "=", sex);
		}
		List<Person> found=qbe.execute();
		if(found.size()==0){
			if(createIfNotExists){
				Person ret=new Person(name,vorname,birthdate,sex);
				addAddress(ret,strasse,plz,ort);
				return ret;
			}
			return null;
		}
		if(found.size()==1){
			return found.get(0);
		}
		return (Person)matchAddress(found.toArray(new Kontakt[0]),strasse,plz,ort,natel);
	}
	
	public static Kontakt matchAddress(final Kontakt[] kk, final String strasse, final String plz, final String ort,
			final String natel){
		
		int[] score=new int[kk.length];
		
		for(int i=0;i<kk.length;i++){
			
			if(!StringTool.isNothing(natel)){
				if(normalizePhone(kk[i].get("NatelNr")).equals(normalizePhone(natel))){
					score[i]+=5;
				}
			}
			if(!StringTool.isNothing(strasse)){
				if(sameStreet(kk[i].get("Strasse"),strasse)){
					score[i]+=3;
				}
			}
			if(!StringTool.isNothing(plz)){
				if(plz.equals(kk[i].get("Plz"))){
					score[i]+=2;
				}
			}
			if(!StringTool.isNothing(ort)){
				if(ort.equals(kk[i].get("Ort"))){
					score[i]+=1;
				}
			}

		}
		Kontakt found=null;
		for(int i=0;i<score.length;i++){
			if(score[i]>0){
				if(found!=null){
					return null;
				}
				found=kk[i];
			}
		}
		return found;
	}
	
	static String normalizePhone(final String nr){
		return nr.replaceAll("[\\s-:\\.]", "");
	}
	
	static boolean sameStreet(final String s1, final String s2){
		String[] ns1=normalizeStrasse(s1);
		String[] ns2=normalizeStrasse(s2);
		if(!(ns1[0].matches(ns2[0]))){
			return false;
		}
		if(!(ns1[1].matches(ns2[1]))){
			return false;
		}
		return true;
	}
	static String[] normalizeStrasse(final String strasse){
		String[] m1=StringTool.normalizeCase(strasse).split("\\s");
		int m1l=m1.length;
		StringBuilder m2=new StringBuilder();
		m2.append(m1[0]);
		String nr="0";
		if(m1l>1){
			if(m1[m1l-1].matches("[0-9]+[a-zA-Z]")){
				nr=m1[m1l-1];
				m1l-=1;
			}
			if(m1l>1){
				for(int i=1;i<m1l;i++){
					m2.append(" ").append(m1[i]);
				}
			}
		}
		return new String[]{m2.toString(),nr};
		
	}
	
	static void addAddress(final Kontakt k, final String str, final String plz, final String ort){
		Anschrift an=k.getAnschrift();
		if(!StringTool.isNothing(str)){
			an.setStrasse(str);
		}
		if(!StringTool.isNothing(plz)){
			an.setPlz(plz);
		}
		if(!StringTool.isNothing(ort)){
			an.setOrt(ort);
		}
		k.setAnschrift(an);
	}

	/**
	 * Decide whether a person is identical tio given personal data. Normalize all names:
	 * Ulmlaute will be converted, ccents will be eliminatet and double names will be reduced
	 * to their first part. 
	 * @return true if the given person seems to be the same tha the given personalia
	 */
	public static boolean isSame(Person a, String nameB, String firstnameB, String gebDatB){
		try{
			String name1=simpleName(StringTool.unambiguify(a.getName()));
			String name2=simpleName(StringTool.unambiguify(nameB));
			if(name1.equals(name2)){
				String vorname1=simpleName(StringTool.unambiguify(a.getVorname()));
				String vorname2=simpleName(StringTool.unambiguify(firstnameB));
				if(vorname1.equals(vorname2)){
					String gd1=a.getGeburtsdatum();
					if(StringTool.isNothing(gd1)){
						return true;
					}
					if(StringTool.isNothing(gebDatB)){
						return true;
					}
					String gd2=new TimeTool(gebDatB).toString(TimeTool.DATE_GER);
					if(gd1.equals(gd2)){
						return true;
					}
				}
			}
			
		}catch(Throwable t){
			ExHandler.handle(t);

		}
		return false;
	}
	static String simpleName(String name){
		String[] ret=name.split("\\s*[- ]\\s*");
		return ret[0];
	}
}
