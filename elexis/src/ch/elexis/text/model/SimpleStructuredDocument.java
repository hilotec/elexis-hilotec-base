package ch.elexis.text.model;

import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.custom.TextChangeListener;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import ch.elexis.ElexisException;
import ch.elexis.Hub;
import ch.elexis.exchange.XChangeContainer;
import ch.elexis.text.Samdas.XRef;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;

/**
 * SimpleStructuredText is an XML format to define structured texts. To simplify
 * interpretation for different readers with different capabilities, text and
 * structure are strictly separated.
 * 
 * @author gerry
 * 
 */
public class SimpleStructuredDocument {
	private static final String ATTR_TYPE = "type";
	private static final String ATTR_START_OFFSET = "startOffset";
	private static final String ELEM_XREF = "xref";
	private static final String ELEM_SECTION = "section";
	private static final String ELEM_MARKUP = "markup";
	private static final String ATTR_TYPE_STRIKETHRU = "strikethru";
	private static final String ATTR_TYPE_UNDERLINED = "underlined";
	private static final String ATTR_TYPE_ITALIC = "italic";
	private static final String ATTR_TYPE_BOLD = "bold";
	private static final String ATTR_TYPE_EMPHASIZED = "emphasized";
	private static final String ATTR_LENGTH = "length";
	private static final String ATTR_SAMDAS_FROM = "from";
	public static final String VERSION = "1.0.0";
	public static final String GENERATOR = "Elexis";
	public static final String ELEM_ROOT = "SimpleStructuredDocument"; //$NON-NLS-1$
	public static final String ELEM_TEXT = "text"; //$NON-NLS-1$
	public static final String ELEM_RECORD = "record"; //$NON-NLS-1$
	private static final int EE_BASE = 100;
	public static final Namespace ns = Namespace.getNamespace(
			"SimpleStructuredText", "http://www.elexis.ch/XSD"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final Namespace nsxsi = Namespace.getNamespace(
			"xsi", "http://www.w3.org/2001/XML Schema-instance"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final Namespace nsschema = Namespace.getNamespace(
			"schemaLocation", "http://www.elexis.ch/XSD sst.xsd"); //$NON-NLS-1$ //$NON-NLS-2$

	private final StringBuilder contents;
	private final ArrayList<IRange> ranges;
	private final List<TextChangeListener> textChangeListeners = new ArrayList<TextChangeListener>();

	public SimpleStructuredDocument() {
		contents = new StringBuilder();
		ranges = new ArrayList<IRange>();
	}

	/**
	 * Parse an input String. Can parse plain text, Samdas or
	 * SimpleStructuredDocument
	 * 
	 * @param input
	 * @param bAppend
	 *            if true, new inpout will appended. If false, current contents
	 *            will be erased first.
	 * @throws ElexisException
	 *             if an XML input could not be parsed
	 */
	public void loadText(String input, boolean bAppend) throws ElexisException {
		if (!bAppend) {
			contents.setLength(0);
			ranges.clear();
		}
		if (input.startsWith("<")) {
			SAXBuilder builder = new SAXBuilder();
			try {
				CharArrayReader car = new CharArrayReader(input.toCharArray());
				Document doc = builder.build(car);
				Element eRoot = doc.getRootElement();
				if (eRoot.getName().equals("EMR")) {
					parseSamdas(eRoot);
				} else if (eRoot.getName().equals(ELEM_ROOT)) {
					parseSSD(eRoot);
				}
			} catch (JDOMException jde) {
				ExHandler.handle(jde);
				throw new ElexisException(getClass(), "Cannot parse input "
						+ jde.getMessage(), EE_BASE);
			} catch (IOException e) {
				ExHandler.handle(e);
				throw new ElexisException(getClass(), "Read error "
						+ e.getMessage(), EE_BASE + 1);
			}
		} else {
			contents.append(input);
		}
	}

	public String getPlaintext(){
		return contents.toString();
	}
	private void parseSamdas(Element eRoot) {
		Element eRecord = eRoot.getChild("record", ns);
		List<Element> eXRef = eRecord.getChildren(ELEM_XREF, ns);
		Element eText = eRecord.getChild("text", ns);
		contents.append(eText.getText());
		List<Element> eMarkup = eRecord.getChildren(ELEM_MARKUP, ns);
		for (Element el : eXRef) {
			int pos = Integer.parseInt(el.getAttributeValue(ATTR_SAMDAS_FROM));
			int len = Integer.parseInt(el.getAttributeValue(ATTR_LENGTH));
			String provider = el.getAttributeValue("provider");
			String id = el.getAttributeValue("id");
			ranges.add(new Xref(pos, len, provider, id));
		}
		for (Element el : eMarkup) {
			int pos = Integer.parseInt(el.getAttributeValue(ATTR_SAMDAS_FROM));
			int len = Integer.parseInt(el.getAttributeValue(ATTR_LENGTH));
			String type = el.getAttributeValue(ATTR_TYPE);
			IMarkup.TYPE t = IMarkup.TYPE.NORMAL;
			if (type.equalsIgnoreCase(ATTR_TYPE_EMPHASIZED)) {
				t = IMarkup.TYPE.EM;
			} else if (type.equals(ATTR_TYPE_BOLD)) {
				t = IMarkup.TYPE.BOLD;
			} else if (type.equalsIgnoreCase(ATTR_TYPE_ITALIC)) { //$NON-NLS-1$
				t = IMarkup.TYPE.ITALIC;
			} else if (type.equalsIgnoreCase(ATTR_TYPE_UNDERLINED)) { //$NON-NLS-1$
				t = IMarkup.TYPE.UNDERLINE;
			} else if (type.equalsIgnoreCase(ATTR_TYPE_STRIKETHRU)) {
				t = IMarkup.TYPE.STRIKETHRU;
			}
			Markup m = new Markup(pos, len, t);
			ranges.add(m);
		}

	}

	
	private void parseSSD(Element eRoot) {
		Element eText = eRoot.getChild(ELEM_TEXT, ns);
		contents.append(eText.getText());
		List<Element> eSections = eRoot.getChildren(ELEM_SECTION, ns);
		for (Element e : eSections) {
			ranges.add(new Section(e.getAttributeValue("name"), Integer
					.parseInt(e.getAttributeValue(ATTR_START_OFFSET)), Integer
					.parseInt(e.getAttributeValue(ATTR_LENGTH))));
		}
		List<Element> eMarkups=eRoot.getChildren(ELEM_MARKUP,ns);
		for(Element el: eMarkups){
			int pos = Integer.parseInt(el.getAttributeValue(ATTR_START_OFFSET));
			int len = Integer.parseInt(el.getAttributeValue(ATTR_LENGTH));
			String type = el.getAttributeValue(ATTR_TYPE);
			IMarkup.TYPE t = IMarkup.TYPE.NORMAL;
			if (type.equalsIgnoreCase(ATTR_TYPE_EMPHASIZED)) {
				t = IMarkup.TYPE.EM;
			} else if (type.equals(ATTR_TYPE_BOLD)) {
				t = IMarkup.TYPE.BOLD;
			} else if (type.equalsIgnoreCase(ATTR_TYPE_ITALIC)) { //$NON-NLS-1$
				t = IMarkup.TYPE.ITALIC;
			} else if (type.equalsIgnoreCase(ATTR_TYPE_UNDERLINED)) { //$NON-NLS-1$
				t = IMarkup.TYPE.UNDERLINE;
			} else if (type.equalsIgnoreCase(ATTR_TYPE_STRIKETHRU)) {
				t = IMarkup.TYPE.STRIKETHRU;
			}
			Markup m = new Markup(pos, len, t);
			ranges.add(m);
		}
		List<Element> eXRef = eRoot.getChildren(ELEM_XREF, ns);
		for (Element el : eXRef) {
			int pos = Integer.parseInt(el.getAttributeValue(ATTR_START_OFFSET));
			int len = Integer.parseInt(el.getAttributeValue(ATTR_LENGTH));
			String provider = el.getAttributeValue("provider");
			String id = el.getAttributeValue("id");
			ranges.add(new Xref(pos, len, provider, id));
		}
	
	}

	/**
	 * Convert the contents to a SimpleStructuredDocument file.
	 * 
	 * @param bCreateHeader
	 *            if false, a representation without header information is
	 *            created
	 * @return
	 */
	public String toXML(boolean bCreateHeader) {
		Document doc = new Document();
		Element eRoot = new Element(ELEM_ROOT, ns);
		if (bCreateHeader) {
			eRoot.setAttribute("created", new TimeTool()
					.toString(TimeTool.DATETIME_XML));
			eRoot.setAttribute("lastEdit", new TimeTool()
					.toString(TimeTool.DATETIME_XML));
			eRoot.setAttribute("createdBy", Hub.actMandant.getPersonalia());
			eRoot.setAttribute("editedBy", Hub.actUser.getPersonalia());
			eRoot.setAttribute("version", VERSION);
			eRoot.setAttribute("generator", Hub.APPLICATION_NAME);
			eRoot.setAttribute("generatorVersion", Hub.Version);
			eRoot.addNamespaceDeclaration(XChangeContainer.nsxsi);
			eRoot.addNamespaceDeclaration(XChangeContainer.nsschema);

		}
		Element eText = new Element(ELEM_TEXT, ns);
		eText.setText(contents.toString());
		doc.setRootElement(eRoot);
		eRoot.addContent(eText);
		for (IRange r : ranges) {
			if (r instanceof Section) {
				Element eSection = new Element(ELEM_SECTION, ns);
				eSection.setAttribute("name", ((Section) r).getName());
				eSection.setAttribute(ATTR_START_OFFSET, Integer.toString(r
						.getPosition()));
				eSection
						.setAttribute(ATTR_LENGTH, Integer.toString(r.getLength()));
				eRoot.addContent(eSection);
			}else if(r instanceof Markup){
				Element eMarkup=new Element(ELEM_MARKUP,ns);
				String m="normal";
				switch(((Markup)r).type){
				case EM:
					m=ATTR_TYPE_EMPHASIZED; break;
				case BOLD: m=ATTR_TYPE_BOLD; break;
				case ITALIC: m=ATTR_TYPE_ITALIC; break;
				case UNDERLINE: m=ATTR_TYPE_UNDERLINED; break;
				case STRIKETHRU: m=ATTR_TYPE_STRIKETHRU; break;
				}
				eMarkup.setAttribute(ATTR_TYPE, m);
				eMarkup.setAttribute(ATTR_START_OFFSET,Integer.toString(r.getPosition()));
				eMarkup.setAttribute(ATTR_LENGTH,Integer.toString(r.getPosition()));
		
			}else if(r instanceof XRef){
				Element eXRef=new Element(ELEM_XREF,ns);
				eRoot.addContent(eXRef);
			}
		}
		XMLOutputter xout = new XMLOutputter(Format.getCompactFormat());
		return xout.outputString(doc);
	}

	public void insert(Object ins, String replaced, int pos) {
		String t=(String)ins;
		if (pos > contents.length() || pos < 0) {
			contents.append(t);
		} else {
			for (IRange r : ranges) {
				int p = r.getPosition();
				int l = r.getLength();
				if (p < pos) {
					if (p + l < pos) {
						continue; // range is before insert position
					} else {
						r.setLength(l + t.length());
					}
				} else {
					r.setPosition(p + t.length());
				}
			}
			contents.insert(pos, t);
		}
	}

	public String remove(int pos, int len) {
		if (pos > contents.length()) {
			return "";
		}
		int end = pos + len;
		String ret = contents.substring(pos, end);
		contents.delete(pos, end);
		return ret;
	}

	public void addRange(IRange r) {
		ranges.add(r);
	}

	public List<IRange> getRanges() {
		return Collections.unmodifiableList(ranges);
	}

	public void addTextChangeListener(TextChangeListener listener) {
		textChangeListeners.add(listener);
	}

	public int getCharCount() {
		return contents.length();
	}

	public String getLineDelimiter() {
		return "\n";
	}

	public String getTextRange(int start, int length) {
		if (start < 0 || start > contents.length()) {
			return "";
		}
		return contents.substring(start, start + length);
	}

	public void removeTextChangeListener(TextChangeListener listener) {
		textChangeListeners.remove(listener);
	}

	public void replaceTextRange(int start, int replaceLength, String newText) {
		contents.replace(start, start + replaceLength, newText);
	}

	public void removeRange(IRange range) {
		ranges.remove(range);
	}

}
