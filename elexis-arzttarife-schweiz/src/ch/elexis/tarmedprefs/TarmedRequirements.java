/*******************************************************************************
 * Copyright (c) 2007-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: TarmedRequirements.java 3598 2008-01-30 22:01:02Z rgw_ch $
 *******************************************************************************/
package ch.elexis.tarmedprefs;

import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Person;
import ch.elexis.data.TrustCenters;
import ch.elexis.data.Xid;
import ch.rgw.tools.StringTool;

public class TarmedRequirements {

	public static final String INSURANCE="Kostenträger";
	public static final String INSURANCE_NUMBER="Versicherungsnummer";
	public static final String CASE_NUMBER="Fallnummer";
	public static final String ACCIDENT_NUMBER="Unfallnummer";
	public final static String SSN ="AHV-Nummer";
	
	public static final String ACCIDENT_DATE="Unfalldatum";
	
	public static final String BILLINGSYSTEM_NAME="TarmedLeistung";
	public static final String OUTPUTTER_NAME="Tarmed-Drucker";
	
	public static final String DOMAIN_KSK="www.xid.ch/id/ksk";
	public static final String DOMAIN_NIF="www.xid.ch/id/nif";

	static{
		Xid.localRegisterXIDDomainIfNotExists(DOMAIN_KSK, "KSK/ZSR-Nr", Xid.ASSIGNMENT_REGIONAL);
		Xid.localRegisterXIDDomainIfNotExists(DOMAIN_NIF, "NIF", Xid.ASSIGNMENT_REGIONAL);
	}
	
	public static String getEAN(final Kontakt k){
		String ret= k.getXid(Xid.DOMAIN_EAN);
		// compatibility layer
		if(ret.length()==0){
			ret=k.getInfoString("EAN");
			if(ret.length()>0){
				setEAN(k,ret);
			}
		}
		// end
		if(ret.length()==0){
			ret="2000000000000";
		}
		return ret;
	}
	
	/**
	 * wandelt KSK's von der G123456-Schreibweise in die G 1234.56 Schreibweise um
	 * und umgekehrt
	 * @param KSK die KSK, welche aus exakt einem Buchstaben, exakt 6 Ziffern und optional
	 * exakt einem Leerzeichen nach dem Buchstaben und einem Punkt vor den letzten beiden Ziffern 
	 * besteht.
	 * @return bei bCompact true eine KSK wie G123456, sonst eine wie G 1234.56 
	 */
	public static String normalizeKSK(String KSK, boolean bCompact){
		if(!KSK.matches("[a-zA-Z] ?[0-9]{4,4}\\.?[0-9]{2,2}")){
			return "invalid";
		}
		KSK=KSK.replaceAll("[^a-zA-Z0-9]", "");
		if(bCompact){
			return KSK;
		}
		KSK=KSK.substring(0,1)+" "+KSK.substring(1, 5)+"."+KSK.substring(5);
		return KSK;
	}
	
	public static String getKSK(final Kontakt k){
		String ret= k.getXid(DOMAIN_KSK);
		// compatibility layer
		if(ret.length()==0){
			ret=k.getInfoString("KSK");
			if(ret.length()>0){
				setKSK(k,ret);
			}
		}
		// end
		return ret.replaceAll("[\\s\\.\\-]", "");
	}
	
	public static String getNIF(final Kontakt k){
		String ret= k.getXid(DOMAIN_NIF);
		// compatibility layer
		if(ret.length()==0){
			ret=k.getInfoString("NIF");
			if(ret.length()>0){
				setNIF(k,ret);
			}
		}
		// end
		return ret;
	}
	
	public static boolean setEAN(final Kontakt k,final String ean){
		if(!ean.matches("[0-9]{13,13}")){
			return false;
		}
		k.addXid(Xid.DOMAIN_EAN, ean, true);
		return true;
	}
	
	public static void setKSK(final Kontakt k, final String ksk){
		k.addXid(DOMAIN_KSK, ksk,true);
	}
	
	public static void setNIF(final Kontakt k, final String nif){
		k.addXid(DOMAIN_NIF, nif, true);
	}
	
	public static String getAHV(final Person p){
		String ahv=p.getXid(Xid.DOMAIN_AHV);
		if(ahv.length()==0){
			ahv=p.getInfoString(SSN);
			if(ahv.length()==0){
				ahv=p.getInfoString(INSURANCE_NUMBER);
			}
			if(ahv.length()>0){
				setAHV(p,ahv);
			}
		}
		return ahv;
	}
	
	public static void setAHV(final Person p, final String ahv){
		p.addXid(Xid.DOMAIN_AHV, ahv, true);
	}
	
	public static String getGesetz(final Fall fall) {
		String gesetz=fall.getAbrechnungsSystem();															// 16000
		String g1=fall.getRequiredString("Gesetz");
		if(g1.length()>0){
			gesetz=g1;
		}else{
			if(!gesetz.matches("KVG|UVG|MV|VVG")){
				gesetz=Fall.getBillingSystemAttribute(gesetz, "gesetz");
			}
		}
		if(gesetz.equalsIgnoreCase("iv")){
			gesetz="ivg";
		}
		if(StringTool.isNothing(gesetz)){
			gesetz="KVG";
		}
		return gesetz;
	}

	public static String getTCName(Kontakt mandant){
		String tc=mandant.getInfoString(PreferenceConstants.TARMEDTC);
		return tc;
	}
	
	public static String getTCCode(Kontakt mandant){
		String tcname=getTCName(mandant);
		Integer nr=TrustCenters.tc.get(tcname);
		if(nr==null){
			return "00";
		}
		return Integer.toString(nr);
	}
	
	public static void setTC(Kontakt mandant, String tc){
		mandant.setInfoElement(PreferenceConstants.TARMEDTC, tc);
	}
	
	public static boolean hasTCContract(Kontakt mandant){
		String hc=(String)mandant.getInfoElement(PreferenceConstants.USETC);
		return "1".equals(hc);
	}
}
