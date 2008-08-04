package ch.elexis.exchange.elements;

import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.exchange.XChangeContainer;
import ch.rgw.tools.TimeTool;

@SuppressWarnings("serial")
public class InsuranceElement extends XChangeElement {
	public static final String XMLNAME="insurance";
	public static final String ATTR_COMPANYREF="companyref";
	public static final String ATTR_REASON="reason";
	public static final String ATTR_DIAGNOSIS="publicDiagnosis";
	public static final String ATTR_DATEFROM="dateFrom";
	public static final String ATTR_DATEUNTIL="dateUntil";
	
	@Override
	public String getXMLName(){
		return  XMLNAME;
	}
	
	public InsuranceElement(XChangeContainer p) {
		super(p);
	}

	public InsuranceElement(XChangeContainer p, Konsultation k){
		super(p);
		Fall fall=k.getFall();
		Kontakt garant=fall.getGarant();
		setAttribute(ATTR_DATEFROM, new TimeTool(fall.getBeginnDatum()).toString(TimeTool.DATE_ISO));
		if(!fall.isOpen()){
			setAttribute(ATTR_DATEUNTIL,new TimeTool(fall.getEndDatum()).toString(TimeTool.DATE_ISO));
		}
		setAttribute(ATTR_REASON, translateReason(fall.getGrund()));
		ContactElement eGarant=p.addContact(garant);
		setAttribute(ATTR_COMPANYREF,eGarant.getID());
		ContractElement eContract=new ContractElement(p);
		addContent(eContract);
	}
	
	public String translateReason(String grund){
		if(grund.equals(Fall.TYPE_ACCIDENT)){
			return "accident";
		}else if(grund.equals(Fall.TYPE_BIRTHDEFECT)){
			return "birthdefect";
		}else if(grund.equals(Fall.TYPE_DISEASE)){
			return "disease";
		}else if(grund.equals(Fall.TYPE_MATERNITY)){
			return "maternity";
		}else if(grund.equals(Fall.TYPE_PREVENTION)){
			return "prevention";
		}else{
			return "other";
		}
	}
	static class ContractElement extends XChangeElement{
		public static final String XMLNAME="contract";
		public static final String ATTR_COUNTRY="country";
		public static final String ATTR_NAME="name";
		public static final String ATTR_CASEID="caseID";
		
		public String getXMLName(){
			return XMLNAME;
		}
		public ContractElement(XChangeContainer parent){
			super(parent);
		}
		public ContractElement(XChangeContainer p, Fall fall){
			super(p);
			setAttribute(ATTR_COUNTRY,"CH");
			setAttribute(ATTR_NAME, fall.getAbrechnungsSystem());
			setAttribute(ATTR_CASEID, fall.getId());
		}
	}


}
