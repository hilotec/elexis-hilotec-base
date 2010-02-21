package ch.elexis.text.model;

import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import ch.elexis.ElexisException;
import ch.elexis.exchange.text.IRange;
import ch.rgw.tools.ExHandler;

/**
 * SimpleStructuredText is an XML format to define structured texts. To simplify interretation for different
 * readers with different capabilities, text and structure are strictly separated.
 * @author gerry
 *
 */
public class SimpleStructuredText {
	public static final String ELEM_ROOT = "SST"; //$NON-NLS-1$
	public static final String ELEM_TEXT = "text"; //$NON-NLS-1$
	public static final String ELEM_RECORD = "record"; //$NON-NLS-1$
	private static final int EE_BASE=100;
	public static final Namespace ns = Namespace.getNamespace("SimpleStructuredText", "http://www.elexis.ch/XSD"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final Namespace nsxsi =
		Namespace.getNamespace("xsi", "http://www.w3.org/2001/XML Schema-instance"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final Namespace nsschema =
		Namespace.getNamespace("schemaLocation", "http://www.elexis.ch/XSD sst.xsd"); //$NON-NLS-1$ //$NON-NLS-2$
	
	private final StringBuilder text;
	private final ArrayList<IRange> ranges;
	
	public SimpleStructuredText(){
		text=new StringBuilder();
		ranges=new ArrayList<IRange>();
	}
	
	public void loadSST(String input) throws ElexisException{
		SAXBuilder builder = new SAXBuilder();
		try{
			CharArrayReader car = new CharArrayReader(input.toCharArray());
			Document doc = builder.build(car);
			Element eRoot = doc.getRootElement();
			Element eText=eRoot.getChild(ELEM_TEXT,ns);
			text.append(eText.getText());
			List<Element> eSections=eRoot.getChildren("section",ns);
			for(Element e:eSections){
				ranges.add(new Section(e.getAttributeValue("name"),
					Integer.parseInt(e.getAttributeValue("startOffset")),
					Integer.parseInt(e.getAttributeValue("length")))
				);
			}
		}catch(JDOMException jde){
			ExHandler.handle(jde);
			throw new ElexisException(getClass(), "Cannot Parse SST File "+jde.getMessage(), EE_BASE);
		} catch (IOException e) {
			ExHandler.handle(e);
			throw new ElexisException(getClass(), "Read error "+e.getMessage(), EE_BASE+1);
		}
	}
	
	public String toXML(){
		Document doc=new Document();
		Element eRoot=new Element(ELEM_ROOT,ns);
		Element eText=new Element(ELEM_TEXT,ns);
		eText.setText(text.toString());
		doc.setRootElement(eRoot);
		eRoot.addContent(eText);
		for(IRange r:ranges){
			if(r instanceof Section){
				Element eSection=new Element("section",ns);
				eSection.setAttribute("name", ((Section) r).getName());
				eSection.setAttribute("startOffset",Integer.toString(r.getPosition()));
				eSection.setAttribute("length",Integer.toString(r.getLength()));
				eRoot.addContent(eSection);
			}
		}
		XMLOutputter xout = new XMLOutputter(Format.getCompactFormat());
		return xout.outputString(doc);
	}
	
	public void insert(String t, int pos){
		if(pos>text.length() || pos<0){
			text.append(t);
		}else{
			for(IRange r:ranges){
				int p=r.getPosition();
				int l=r.getLength();
				if(p<pos){
					if(p+l<pos){
						continue;	// range is before insert position
					}else{
						r.setLength(l+t.length());
					}
				}else{
					r.setPosition(p+t.length());
				}
			}
			text.insert(pos, t);
		}
	}
	
	public String remove(int pos, int len){
		if(pos>text.length()){
			return "";
		}
		int end=pos+len;
		String ret=text.substring(pos, end);
		text.delete(pos, end);
		return ret;
	}
	
	public void addRange(IRange r){
		ranges.add(r);
	}
	
	public List<IRange> getRanges(){
		return Collections.unmodifiableList(ranges);
	}
}
