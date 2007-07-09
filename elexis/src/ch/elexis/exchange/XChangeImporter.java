package ch.elexis.exchange;

import java.io.CharArrayReader;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import ch.elexis.Desk;
import ch.elexis.data.Anschrift;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Organisation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.elexis.exchange.elements.MedicalElement;
import ch.elexis.util.Result;
import ch.rgw.tools.ExHandler;

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
	public Result doImport(){

		List<Element> eCont=eRoot.getChildren("contact", ns);
		Result result=null;
		for(Element e:eCont){
			String iex=e.getAttributeValue("id");
			Kontakt insert=Kontakt.load(idMap.get(iex));
			if((insert==null) || (insert.existence()<PersistentObject.DELETED)){
				String[] params, values;
				boolean isPerson;
				if(e.getAttributeValue("type").equals("organization")){
					params=new String[]{"Bezeichnung1"};
					values=new String[]{e.getAttributeValue("name")};
					isPerson=false;
				}else{
					params=new String[]{"Bezeichnung1","Bezeichnung2","Geburtsdatum","Geschlecht"};
					values=new String[]{e.getAttributeValue("lastname"),
										e.getAttributeValue("firstname"),
										e.getAttributeValue("birthdate"),
										e.getAttributeValue("sex").equals("male") ? "m" : "w"};
					isPerson=true;
				}
				List<Kontakt> list=new Query<Kontakt>(Kontakt.class).queryFields(params, values, true);
				if(list.size()==0){
					insert=createKontakt(e);
				}else{
					Result res=resolveConflict(values[0]+" "+(values.length>1 ? values[1] : ""), list.toArray(new PersistentObject[0]));
					if(res==null){
						continue;
					}
					insert=(Kontakt) res.get();
					if(insert==null){
						insert=createKontakt(e);
					}else{
						idMap.put(iex, insert.getId());
					}
				}
				if(isPerson){
					insert.set("istPerson", "1");
					Element eMedical=e.getChild("medical",ns);
					if(eMedical!=null){
						MedicalElement medical=new MedicalElement(this,eMedical,Patient.load(insert.getId()));
					}
				}else{
					insert.set("istOrganisation", "1");
				}
			}
			importAddress(e,insert);
			importRelations(e,insert);
		}
		return result;
		
	}
	private Result<PersistentObject> resolveConflict(String prop, PersistentObject... choices){
		ConflictSolver cs=new ConflictSolver(prop,choices);
		Desk.theDisplay.syncExec(cs);
		return cs.getResult();
	}
	private class ConflictSolver implements Runnable{
		Result<PersistentObject> result;
		PersistentObject[] choices;
		String prop;
		
		ConflictSolver(String prop, PersistentObject[] choices){
			this.choices=choices;
			this.prop=prop;
		}
		public void run() {
			Shell shell=Desk.theDisplay.getActiveShell();
			ImportKonfliktDialog ikd=new ImportKonfliktDialog(shell,choices,prop);
			if(ikd.open()==Dialog.OK){
				result=ikd.result;
			} 
		}
		Result<PersistentObject> getResult(){
			return result;
		}
	}
	private Kontakt createKontakt(Element e){
		Kontakt ret;
		if(e.getAttributeValue("type").equals("person")){
			String name=e.getAttributeValue("lastname");
			String vorname=e.getAttributeValue("firstname");
			String gebdat=e.getAttributeValue("birthdate");
			String geschlecht=e.getAttributeValue("sex").equals("male") ? "m" : "w";
			ret=new Person(name,vorname,gebdat,geschlecht);
		}else{
			String name=e.getAttributeValue("name");
			ret=new Organisation(name,"");
		}
		idMap.put(e.getAttributeValue("id"),ret.getId());		// Mapping eintragen
		return ret;
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
					an.setStrasse(eAdr.getChildText("street", ns));
					an.setOrt(eAdr.getChildText("city",ns));
					an.setPlz(eAdr.getChildText("zip", ns));
					an.setLand(eAdr.getChildText("country", ns));
					insert.setAnschrift(an);	
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
