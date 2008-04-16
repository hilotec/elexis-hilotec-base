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
 * $Id: TarmedOptifier.java 3774 2008-04-16 19:00:57Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.util.Hashtable;
import java.util.List;

import ch.elexis.arzttarife_schweiz.Messages;
import ch.elexis.util.*;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
/**
 * Ersetzt den TarmedVerifier durch das bessere Optifier-konzept
 * @author gerry
 *
 */
public class TarmedOptifier implements IOptifier {
	public static final int OK=0;
	public static final int PREISAENDERUNG=1;
	public static final int KUMULATION=2;
	public static final int KOMBINATION=3;
	public static final int EXKLUSION=4;
	public static final int INKLUSION=5;
	public static final int LEISTUNGSTYP=6;
	public static final int NOTYETVALID=7;
	public static final int NOMOREVALID=8;
	
	
	public Result<Konsultation> optify(Konsultation kons) {
		// TODO Auto-generated method stub
		return null;
	}

	public Result<IVerrechenbar> add(IVerrechenbar code, Konsultation kons) {
		if(code instanceof TarmedLeistung){
			TarmedLeistung tc=(TarmedLeistung)code;
			List<Verrechnet> lst=kons.getLeistungen();
			boolean checkBezug=false;
			boolean bezugOK=true;
			/* TODO Hier checken, ob dieser code mit der Dignität 
			 * und Fachspezialisierung des  aktuellen Mandanten usw. 
			 * vereinbar ist */

			Hashtable ext=((TarmedLeistung)code).loadExtension();
//			 Bezug
			String bezug=(String)ext.get("Bezug"); //$NON-NLS-1$
			if(!StringTool.isNothing(bezug)){
				checkBezug=true;
				bezugOK=false;
			}
// Datum
			TimeTool date=new TimeTool(kons.getDatum());
			String dVon=((TarmedLeistung)code).get("GueltigVon");
			if(!StringTool.isNothing(dVon)){
				TimeTool tVon=new TimeTool(dVon);
				if(date.isBefore(tVon)){
					return new Result<IVerrechenbar>(Log.WARNINGS,NOTYETVALID,code.getCode()+" noch nicht gültig",null,false);
				}
			}
			String dBis=((TarmedLeistung)code).get("GueltigBis");
			if(!StringTool.isNothing(dBis)){
				TimeTool tBis=new TimeTool(dBis);
				if(date.isAfter(tBis)){
					return new Result<IVerrechenbar>(Log.WARNINGS,NOMOREVALID,code.getCode()+" nicht mehr gültig",null,false);
				}
			}
			Verrechnet check=null;
			// Ist der Hinzuzufügende Code vielleicht schon in der Liste? Dann nur Zahl erhöhen.
			for(Verrechnet v:lst){
				if(v.isInstance(code)){
					check=v;
					check.setZahl(check.getZahl()+1);
					if(bezugOK){
						break;
					}
				}
				// "Nur zusammen mit" - Bedingung erfüllt.
				if(checkBezug){
					if(v.getCode().equals(bezug)){
						bezugOK=true;
						if(check!=null){
							break;
						}
					}
				}
			}
			// Ausschliessende Kriterien prüfen ("Nicht zusammen mit")
			if(check==null){
				check=new Verrechnet(code,kons,1);
				// Exclusionen
				String excl=(String)ext.get("exclusion"); //$NON-NLS-1$
				if(!StringTool.isNothing(excl)){
					for(String e:excl.split(",")){ //$NON-NLS-1$
						for(Verrechnet v:lst){
							if(v.getCode().equals(e)){
								check.delete();
								return new Result<IVerrechenbar>(Log.WARNINGS,EXKLUSION,code.getCode()+" nicht kombinierbar mit "+e,null,false); //$NON-NLS-1$
							}
							if(v.getVerrechenbar() instanceof TarmedLeistung){
								String ex2=((TarmedLeistung)v.getVerrechenbar()).getExclusion();
								for(String e2:ex2.split(",")){ //$NON-NLS-1$
									if(e2.equals(code.getCode())){
										check.delete();
										return new Result<IVerrechenbar>(Log.WARNINGS,EXKLUSION,code.getCode()+" nicht kombinierbar mit "+e,null,false); //$NON-NLS-1$
									}
								}
							}
						}
					}
				}
				check.setExtInfo("AL", Integer.toString(tc.getAL()));
				check.setExtInfo("TL", Integer.toString(tc.getTL()));
				lst.add(check);
			}
			/* Dies führt zu Fehlern bei Codes mit mehreren Master-Möglichkeiten -> virerst raus
			// "Zusammen mit" - Bedingung nicht erfüllt -> Hauptziffer einfügen.
			if(checkBezug){
				if(bezugOK==false){
					TarmedLeistung tl=TarmedLeistung.load(bezug);
					Result<IVerrechenbar> r1=add(tl,kons);
					if(!r1.isOK()){
						r1.add(Log.WARNINGS,KOMBINATION,code.getCode()+" nur zusammen mit "+bezug,null,false); //$NON-NLS-1$
						return r1;
					}
				}
			}
			*/
			// Prüfen, ob zu oft verrechnet
			String lim=(String)ext.get("limits"); //$NON-NLS-1$
			if(lim!=null){
				String[] lin=lim.split("#"); //$NON-NLS-1$
				for(String line:lin){
					String[] f=line.split(","); //$NON-NLS-1$
					if(f.length==5){
						switch (Integer.parseInt(f[4].trim())) {
						case 7:	// Pro Sitzung		
							if(f[2].equals("1")){ // 1 Sitzung //$NON-NLS-1$
								int menge=Math.round(Float.parseFloat(f[1]));
								if(check.getZahl()>menge){
									check.setZahl(menge);
									return new Result<IVerrechenbar>(Log.WARNINGS,KUMULATION,Messages.TarmedOptifier_codemax+menge+Messages.TarmedOptifier_perSession,null,false); //$NON-NLS-1$ //$NON-NLS-2$
								}
							}
							break;

						default:
							break;
						}
					}
				}
			}
			
			//Notfall-Zuschlag
			String tcid=code.getCode();
			//double sum=0;
			Money sum=new Money(0);
			if(tcid.startsWith("00.25")){ //$NON-NLS-1$
				int subcode=Integer.parseInt(tcid.substring(5));
				double factor=PersistentObject.checkZeroDouble(check.get("VK_Scale"));
				switch(subcode){
				case 10:	// Mo-Fr 7-19, Sa 7-12: 60 TP
					break;
				case 20:	// Mo-Fr 19-22, Sa 12-22, So 7-22: 120 TP
					break;
				case 30:	// 25% zu allen AL von 20
				case 70:	// 25% zu allen AL von 60 (tel.)
					for(Verrechnet v:lst){
						if(v.getVerrechenbar() instanceof TarmedLeistung){
							TarmedLeistung tl=(TarmedLeistung) v.getVerrechenbar();
							if(tl.getCode().startsWith("00.25")){ //$NON-NLS-1$
								continue;
							}
							sum.addCent(tl.getAL()>>2);
							//sum+=(tl.getAL()/4.0);
						}
					}
					//check.setPreisInRappen((int)Math.round(sum));
					check.setPreis(sum.multiply(factor));
					break;
				case 40:	// 22-7: 180 TP
					break;
				case 50:	// 50% zu allen AL von 40
				case 90:	// 50% zu allen AL von 70 (tel.)
					for(Verrechnet v:lst){
						if(v.getVerrechenbar() instanceof TarmedLeistung){
							TarmedLeistung tl=(TarmedLeistung) v.getVerrechenbar();
							if(tl.getCode().startsWith("00.25")){ //$NON-NLS-1$
								continue;
							}
							//sum+=(tl.getAL()/2.0);
							int summand=tl.getAL()>>1;
							sum.addCent(summand*v.getZahl());
						}
					}
					//check.setPreisInRappen((int)Math.round(sum));
					check.setPreis(sum.multiply(factor));
					break;

				case 60:	// Tel. Mo-Fr 19-22, Sa 12-22, So 7-22: 30 TP
					break;
				case 80:	// Tel. von 22-7: 70 TP
					break;

				}
				return new Result<IVerrechenbar>(0,PREISAENDERUNG,"Preis",null,false); //$NON-NLS-1$
			}
			return new Result<IVerrechenbar>(null);
		}
		return new Result<IVerrechenbar>(Log.ERRORS,LEISTUNGSTYP,Messages.TarmedOptifier_BadType,null,true); //$NON-NLS-1$
	}


	public Result<Verrechnet> remove(Verrechnet code, Konsultation kons) {
		List<Verrechnet> l=kons.getLeistungen();
		l.remove(code);
		code.delete();
		return new Result<Verrechnet>(code);
	}

}
