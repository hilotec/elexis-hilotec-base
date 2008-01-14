/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Rezept.java 3529 2008-01-14 14:56:24Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import ch.elexis.Hub;
import ch.elexis.util.Log;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Ein Rezept besteht aus einem Mandanten, einem Patienten, einem Datum und einer Prescription-Liste
 * Aus kompatibilitätsgründen wird in Moment noch der RpText mitgeschleppt
 * @author Gerry
 *
 */
public class Rezept extends PersistentObject {
	@Override
	protected String getTableName() {
		return "REZEPTE";
	}
	static{
		addMapping("REZEPTE","PatientID","MandantID","Datum=S:D:Datum","Text=RpTxt","BriefID",
				"Zeilen=LIST:RezeptID:PATIENT_ARTIKEL_JOINT");
	}
	public static Rezept load(final String id){
		return new Rezept(id);
	}
	public Rezept(final Patient pat){
		create(null);
		set(new String[]{"PatientID","MandantID","Datum"},
				pat.getId(),Hub.actMandant.getId(),
				new TimeTool().toString(TimeTool.DATE_GER));
	}
	public Patient getPatient(){
		return Patient.load(get("PatientID"));
	}
	public Mandant getMandant(){
		Mandant mret=Mandant.load(get("MandantID"));
		return mret;
	}
	public String getDate(){
		return get("Datum");
	}
	public String getText(){
		return get("Text");
	}
	
	protected Rezept() {}

	protected Rezept(final String id) {
		super(id);
	}

	/** 
	 * Den "Brief" liefern. Dieser existiert, wenn das Rezept mindestens einmal gedruckt wurde
	 * und ist die Print-Repräsentation mit etwaigen manuellen Änderungen
	 * @return der Brief oder null, wenn keiner existiert.
	 */
	public Brief getBrief(){
		Brief brief= Brief.load(get("BriefID"));
		if(brief.exists()){
			return brief;
		}
		return null;
	}
	
	public void setBrief(final Brief brief){
		if(brief==null){
			log.log("Null Brief gesetzt bei setBrief", Log.ERRORS);
		}else{
			set("BriefID",brief.getId());
		}
	}
	@Override
	public String getLabel() {
		Mandant m=getMandant();
		if(m==null){
			return getDate()+" (unbekannt)";
		}
		return getDate()+" "+m.getLabel();
	}
	
	/** Alle Rezeotzeilen als Liste holen */ 
	@Deprecated
	public List<RpZeile> getLinesOld(){
		String raw=getText();
		ArrayList<RpZeile> ret=new ArrayList<RpZeile>();
		if(!StringTool.isNothing(raw)){
			for(String l:raw.split("\\n")){
				RpZeile z=new RpZeile(l);
				ret.add(z);
			}
		}
		return ret;
	}
	
	public List<Prescription> getLines(){
		List<String> list=getList("Zeilen",false);
		// Kompatibilitätslayer
		if(list.isEmpty()){			
			Query<Artikel> qbe=new Query<Artikel>(Artikel.class);
			List<RpZeile> rz=getLinesOld();
			List<Prescription> lr=new ArrayList<Prescription>(rz.size());
			for(RpZeile r:rz){
				qbe.clear();
				Artikel art=Artikel.load(qbe.findSingle("Name", "=",r.name));
				if(art!=null){
					Prescription p=new Prescription(art, getPatient(), r.ds, r.bem);
					p.setBeginDate(getDate());
					p.set("RezeptID", getId());
					lr.add(p);
				}
			}
			return lr;
		}
		// Ende KOmpatibilitätslayer
		List<Prescription> ret=new ArrayList<Prescription>(list.size());
		for(String s:list){
			ret.add(Prescription.load(s));
		}
		return ret;
	}
	
	/** Eine Rezeptzeile entfernen 
	 * @deprecated use removePrescripion*/
	@Deprecated
	public void removeLine(final RpZeile z){
		String raw=getText();
		String zs=z.toString();
		raw=raw.replaceFirst(zs, "");
		raw=raw.replaceFirst("^\\r*\\n", "");
		set("Text",raw.replaceAll("\\r*\\n\\r*\\n", "\n"));
	}
	
	public void removePrescription(final Prescription p){
		p.set("RezeptID", "");
	}
	/** Eine Rezeptzeile hinzufügen 
	 * @deprecated use addPrescription
	 * */
	@Deprecated 
	public void addLine(final RpZeile z){
		String raw=getText();
		if(StringTool.isNothing(raw)){
			raw=z.toString();
		}else{
			raw.replaceFirst("\\n$","");
			raw+="\n"+z.toString();
		}
		set("Text",raw);
	}
	
	public void addPrescription(final Prescription p){
		p.set("RezeptID", getId());
	}
	@Override
	public boolean delete() {
		Brief brief=getBrief();
		if(brief!=null){
			brief.delete();
		}
		return super.delete();
	}
	public Document toXML(){
		List<Prescription> lines=getLines();
		Document ret=new Document();
		Element root=new Element("Rezept");
		root.setAttribute("Datum",getDate());
		root.setAttribute("Patient",getPatient().getLabel());
		root.setAttribute("Aussteller",getMandant().getLabel());
		ret.setRootElement(root);
		for(Prescription l:lines){
			Element item=new Element("Item");
			item.setAttribute("Verordnung",l.getDosis());
			item.setAttribute("Bemerkung",l.getBemerkung());
			item.addContent(l.getLabel());
			root.addContent(item);
		}
		return ret;
	}
	
	private class RpZeile{
		public static final String fieldSeparator="¦";
		String num,name,pck,ds,bem;

		@Override
		public String toString(){
			StringBuilder sb=new StringBuilder();
			sb.append(num).append(fieldSeparator).append(name).append(fieldSeparator)
				.append(pck).append(fieldSeparator).append(ds).append(fieldSeparator)
				.append(bem);
			return sb.toString();
		}
		public RpZeile(){}
		public RpZeile(final String in){
			String[] parts=in.split(fieldSeparator);

			num=parts.length>0 ? parts[0]:"";
			name=parts.length>1 ? parts[1]:"";
			pck=parts.length>2 ? parts[2]:"";
			ds=parts.length>3 ? parts[3]:"";
			bem=parts.length>4 ? parts[4]:"" ;
			
		}
		public RpZeile(final String num, final String name, final String pck, final String ds, final String bem){
			this.num=num;
			this.name=name;
			this.pck=pck;
			this.ds=ds;
			this.bem=bem;
		}
		public String getBem() {
			return bem;
		}

		public void setBem(final String bem) {
			this.bem = bem;
		}

		public String getDs() {
			return ds;
		}

		public void setDs(final String ds) {
			this.ds = ds;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public String getNum() {
			return num;
		}

		public void setNum(final String num) {
			this.num = num;
		}

		public String getPck() {
			return pck;
		}

		public void setPck(final String pck) {
			this.pck = pck;
		} 
		
		
	}
	/*
	public Document asXML(){
	 SAXBuilder builder = new SAXBuilder();
	 	String raw=getText();
        try {
			Document doc = builder.build(raw);
			Element root=doc.getRootElement();
			List<Element> list=root.getChildren();{
				for(Element el:list){
					if(el.getName().equals("Header")){
						importAuftrag(root,el);
					}
				}
			}
		} catch (JDOMException e) {
			SWTHelper.alert("Fehler beim Datenimport","Die XML-Datei enthält formale Fehler");
			ExHandler.handle(e);
		} catch (IOException e) {
			SWTHelper.alert("Fehler beim Datenimport","Die Importdatei konnte nicht geöffnet werden");
			ExHandler.handle(e);
		}

	}*/
	

}
