package ch.elexis.tarmedprefs;

import ch.elexis.data.Kontakt;
import ch.elexis.data.Xid;

public class TarmedRequirements {

	public static final String INSURANCE="Kostenträger";
	public static final String INSURANCE_NUMBER="Versicherungsnummer";
	public static final String CASE_NUMBER="Fallnummer";
	public static final String ACCIDENT_NUMBER="Unfallnummer";
	
	public static final String ACCIDENT_DATE="Unfalldatum";
	
	public static final String BILLINGSYSTEM_NAME="TarmedLeistung";
	public static final String OUTPUTTER_NAME="Tarmed-Drucker";
	
	public static String getEAN(Kontakt k){
		String ret= k.getXID(Xid.DOMAIN_EAN);
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
	
	public static String getKSK(Kontakt k){
		String ret= k.getXID(Xid.DOMAIN_KSK);
		// compatibility layer
		if(ret.length()==0){
			ret=k.getInfoString("KSK");
			if(ret.length()>0){
				setKSK(k,ret);
			}
		}
		// end
		return ret;
	}
	
	public static String getNIF(Kontakt k){
		String ret= k.getXID(Xid.DOMAIN_NIF);
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
	
	public static boolean setEAN(Kontakt k,String ean){
		if(!ean.matches("[0-9]{13,13}")){
			return false;
		}
		k.addXid(Xid.DOMAIN_EAN, ean, Xid.QUALITY_GLOBAL, true);
		return true;
	}
	
	public static void setKSK(Kontakt k, String ksk){
		k.addXid(Xid.DOMAIN_KSK, ksk, Xid.QUALITY_REGIONAL, true);
	}
	
	public static void setNIF(Kontakt k, String nif){
		k.addXid(Xid.DOMAIN_NIF, nif, Xid.QUALITY_REGIONAL, true);
	}
}
