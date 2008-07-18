package ch.elexis.exchange;

import java.io.CharArrayReader;
import java.util.List;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import ch.elexis.data.Anschrift;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.exchange.elements.MedicalElement;
import ch.elexis.util.Result;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

public class XChangeImporter extends XChangeContainer{
	public XChangeImporter(){
		bValid=false;
	}
	/**
	 * Create a Container from an existing xChange Document
	 * @param input a valid and well formed xml-document
	 */
	public XChangeImporter(String input){
		 load(input);
	}
	/**
	 * Load an xChange document into the container
	 * @param input a valid and well formed xml document
	 * @return true on success
	 */
	public boolean load(String input){
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
	
	/**
	 * Convert an already loaded xChange-Document into the internal data representation of elexis.
	 */
	@SuppressWarnings("unchecked")
	public Result<String> doImport(){

		List<Element> eCont=eRoot.getChildren("contact", ns);
		for(Element e:eCont){
			String iex=e.getAttributeValue("id");
			Kontakt insert=Kontakt.load(idMap.get(iex));
			Element eA=e.getChild("address", ns);
			if((insert==null) || (insert.state()<PersistentObject.DELETED)){
				boolean isPerson;
				if("organization".equals(e.getAttributeValue("type"))){
					isPerson=false;
					insert=KontaktMatcher.findOrganisation(e.getAttributeValue("name"), 
									eA.getAttributeValue("street"),
									eA.getAttributeValue("zip"),
									eA.getAttributeValue("city"),
									KontaktMatcher.CreateMode.CREATE);
				}else{
					isPerson=true;
					String natel="";
					List<Element> connections=e.getChildren("connection",ns);
					if(connections!=null){
						for(Element connection:connections){
							if(connection.getAttributeValue("type").equalsIgnoreCase("natel")){
								natel=connection.getAttributeValue("identification");
							}
						}
					}
					
					insert=KontaktMatcher.findPerson(e.getAttributeValue("lastname"),
							e.getAttributeValue("firstname"),
							e.getAttributeValue("birthdate"),
							e.getAttributeValue("sex").equals("male") ? Person.MALE : Person.FEMALE,
							eA.getAttributeValue("street"),
							eA.getAttributeValue("zip"),
							eA.getAttributeValue("city"),
							natel,
							KontaktMatcher.CreateMode.CREATE
							);
				}
				idMap.put(e.getAttributeValue("id"),insert.getId());		// Mapping eintragen
				if(isPerson){
					Element eMedical=e.getChild("medical",ns);
					if(eMedical!=null){
						MedicalElement medical=new MedicalElement(this);
						medical.readFromXML(eMedical, Patient.load(insert.getId()));
					}
				}
			}
			importAddress(e,insert);
			importRelations(e,insert);
		}
		return new Result<String>("OK");
		
	}
	
	/**
	 * Import an address element. Elexis does not support multiple adresses, so wie map all but the
	 * default address to "Bemerkung"
	 */
	private void importAddress(Element e, Kontakt insert){
		List<Element> lAdr=e.getChildren("address", ns);
		if(lAdr!=null){
			for(Element eAdr:lAdr){
				String type=eAdr.getAttributeValue("type");
				if(type==null || type.equalsIgnoreCase("default")){
					Anschrift an=new Anschrift(insert);
					an.setStrasse(eAdr.getAttributeValue("street"));
					an.setOrt(eAdr.getAttributeValue("city"));
					an.setPlz(eAdr.getAttributeValue("zip"));
					an.setLand(eAdr.getAttributeValue("country"));
					insert.setAnschrift(an);
					String postanschrift=eAdr.getText();
					if(StringTool.isNothing(postanschrift.trim())){
						insert.createStdAnschrift();
					}else{
						insert.set("Anschrift", postanschrift);
					}
				}
				else{
					StringBuilder sb=new StringBuilder();
					sb.append(insert.getBemerkung()).append("\nAdresse: ")
						.append(type).append(":")
						.append(eAdr.getChildText("street", ns))
						.append(", ").append(eAdr.getChildText("zip", ns))
						.append(" ").append(eAdr.getChildText("city", ns))
						.append(" ").append(eAdr.getChildText("country", ns));
					insert.setBemerkung(sb.toString());
				}
			}
		}
		
	}
	
	/**
	 * Define relationships between &lt;contact&gt;>a
	 */
	private void importRelations(Element e, Kontakt k){
		List<Element> lr=e.getChildren("contactref", ns);
		if(lr!=null){
			for(Element er:lr){
				String type=er.getAttributeValue("type");
				String id=er.getAttributeValue("id");
				Kontakt kRef=findContact(id);
				if(kRef!=null){
					k.addBezugsKontakt(kRef, type);
				}
			}
		}
	}
	
}
