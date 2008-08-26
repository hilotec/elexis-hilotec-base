package ch.elexis.exchange;

import java.io.CharArrayReader;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import ch.elexis.Hub;
import ch.rgw.tools.ExHandler;

public class SoapConverter {
	public static final Namespace ns=Namespace.getNamespace("soap", "http://www.w3.org/2001/12/soap-envelope");
	public static final String TYPE_STRING="string";
	public static final String TYPE_INTEGRAL="integral";
	public static final String TYPE_FLOAT="float";
	public static final String TYPE_ARRAY="array";
	public static final String TYPE_HASH="hash";
	
	private Document doc;
	private Element eRoot;
	private Element eBody;
	private boolean bValid;
	
	public SoapConverter(){
	}
	
	
	public boolean load(String input) {
		SAXBuilder builder = new SAXBuilder();
		try{
			CharArrayReader car=new CharArrayReader(input.toCharArray());
			doc = builder.build(car);
			eRoot=doc.getRootElement();
			bValid=true;
		} catch (Exception e) {
			ExHandler.handle(e);
			bValid=false;
		}
		return bValid;
		
	}
	
	public HashMap<String, Object> getParameters(){
		if(bValid){
			HashMap<String, Object> ret=new HashMap<String, Object>();
			Element body=eRoot.getChild("Body",ns);
			List<Element> params=body.getChildren("parameter", ns);
			for(Element param:params){
				String type=param.getAttributeValue("type");
				String s=param.getText();
				Object res=null;
				try{
					if(type.equals(TYPE_STRING)){
						res=s;
					}else if(type.equals(TYPE_INTEGRAL)){
						res=Long.parseLong(s);
					}else if(type.equals(TYPE_FLOAT)){
						res=Double.parseDouble(s);
					}else if(type.equals(TYPE_ARRAY)){
						res=new BASE64Decoder().decodeBuffer(s);
					}else{
						res="** unsupported type **";
					}
				}catch(Exception ex){
					ExHandler.handle(ex);
					res="** parse error **";
				}
				ret.put(param.getAttributeValue("name"), res);
			}
			return ret;
		}
		return null;
	}
	public void create(){
		eRoot=new Element("Envelope",ns);
		Element eHeader=new Element("Header",ns);
		Element eID=new Element("Creator",ns);
		eID.setAttribute("name","elexis");
		eID.setAttribute("version",Hub.Version);
		eID.setAttribute("provider","http://www.elexis.ch");
		eHeader.addContent(eID);
		eRoot.addContent(eHeader);
		eBody=new Element("Body",ns);
		eRoot.addContent(eBody);
		doc=new Document(eRoot);
		bValid=true;
	}
	
	public String toXML(){
		if(doc!=null && eRoot!=null){
			Format format=Format.getPrettyFormat();
			format.setEncoding("utf-8");
			XMLOutputter xmlo=new XMLOutputter(format);
			return xmlo.outputString(doc);
		}
		return null;
	}
	
	private Element createParameter(String name, String type){
		Element ret=new Element("parameter",ns);
		ret.setAttribute("type",type);
		ret.setAttribute("name",name);
		eBody.addContent(ret);
		return ret;
	}
	public void addString(String name, String s){
		createParameter(name,TYPE_STRING).setText(s);
	}
	
	public void addIntegral(String name, long x){
		createParameter(name,TYPE_INTEGRAL).setText(Long.toString(x));
	}
	public void addFloat(String name, double x){
		createParameter(name, TYPE_FLOAT).setText(Double.toString(x));
	}	
	
	public void addArray(String name, byte[] arr){
		String res=new BASE64Encoder().encode(arr);
		createParameter(name,TYPE_ARRAY).setText(res);
	}
	public void addObject(String name, Object obj){
		if(obj instanceof String){
			addString(name,(String)obj);
		}else if((obj instanceof Double) ||
				 (obj instanceof Float)){
			addFloat(name, (Double)obj);
		}else if((obj instanceof Integer) ||
				 (obj instanceof Long) ||
				 (obj instanceof Byte) ){
			addIntegral(name, (Long)obj);
		}else if(obj instanceof byte[]){
			addArray(name, (byte[])obj);
		}
	}
	public void addHashMap(String name, HashMap<String, Object> hash){
		//Element ret=createParameter(name, TYPE_HASH);
		Set<Entry<String, Object>> entries=hash.entrySet();
		for(Entry<String,Object> entry:entries){
			addObject(entry.getKey(),entry.getValue());
		}
	}
}
