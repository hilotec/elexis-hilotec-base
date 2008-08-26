package ch.elexis.exchange;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import ch.elexis.Hub;

public class SoapConverter {
	public static final Namespace ns=Namespace.getNamespace("soap", "http://www.w3.org/2001/12/soap-envelope");

	private Document doc;

	public SoapConverter(){
		
	}
	
	public void create(){
		doc=new Document();
		Element eRoot=new Element("Envelope",ns);
		Element eHeader=new Element("Header",ns);
		Element eID=new Element("Creator",ns);
		eID.setAttribute("name","elexis");
		eID.setAttribute("version",Hub.Version);
		eID.setAttribute("provider","http://www.elexis.ch");
		eHeader.addContent(eID);
		eRoot.addContent(eHeader);
		
	}
	public Element StringToXml(String s){
		Element ret=new Element("parameter",ns);
		ret.setAttribute("type","string");
		ret.setText(s);
		return ret;
	}
	public String XmlToString(Element e){
		return e.getText();
	}
	
	public Element IntegralToXml(long x){
		Element ret=new Element("parameter",ns);
		ret.setAttribute("type","integral");
		ret.setText(Long.toString(x));
		return ret;
	}
	public Element FloatToXml(double x){
		Element ret=new Element("parameter",ns);
		ret.setAttribute("type","float");
		ret.setText(Double.toString(x));
		return ret;
	}	
}
