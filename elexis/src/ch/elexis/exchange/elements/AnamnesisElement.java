/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id$
 *******************************************************************************/

package ch.elexis.exchange.elements;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.jdom.Element;

import ch.elexis.data.ICodeElement;
import ch.elexis.data.IDiagnose;
import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObjectFactory;
import ch.elexis.exchange.XChangeContainer;
import ch.elexis.util.Extensions;
import ch.elexis.views.codesystems.CodeSelectorFactory;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class AnamnesisElement extends XChangeElement{
	HashMap<IDiagnose, Element> hLink=new HashMap<IDiagnose,Element>();
	HashMap<Element,IDiagnose> hBacklink;
	HashMap<String,Element> hElements;
	MedicalElement eMed;

	public List<EpisodeElement> getEpisodes(){
		List<EpisodeElement> ret=new LinkedList<EpisodeElement>();
		return ret;
	}
	
	public AnamnesisElement(MedicalElement eMed){
		super(eMed.parent);
		e=new Element("anamnesis",XChangeContainer.ns);
		this.eMed=eMed;
		
	}
	
	
	@SuppressWarnings("unchecked")
	public AnamnesisElement(MedicalElement eMed, Element e1){
		super(eMed.parent,e1);
		hBacklink=new HashMap<Element,IDiagnose>();
		hElements=new HashMap<String,Element>();
		List<Element> episodes=e1.getChildren("episode",XChangeContainer.ns);
		if(episodes!=null){
			for(Element ep:episodes){
				hElements.put(ep.getAttributeValue("id"), ep);
				Element eDiag=ep.getChild("diagnosis", XChangeContainer.ns);
				String codesys=eDiag.getAttributeValue("codesystem");
				String dgCode=eDiag.getAttributeValue("code");
				List<IConfigurationElement> list=Extensions.getExtensions("ch.elexis.Diagnosecode");
				for(IConfigurationElement ic:list){
					try {
						PersistentObjectFactory po=(PersistentObjectFactory)ic.createExecutableExtension("ElementFactory");
						CodeSelectorFactory cs=(CodeSelectorFactory)ic.createExecutableExtension("CodeSelectorFactory");
						if(cs.getCodeSystemName().equalsIgnoreCase(codesys)){
							ICodeElement ics=(ICodeElement)po.createTemplate(cs.getElementClass());
							String diag=ics.getClass().getName()+"::"+dgCode;
							IDiagnose key=(IDiagnose)po.createFromString(diag);
							if(key!=null){
								hBacklink.put(ep,key);
							}
						}
						
					} catch (CoreException ex) {
						ExHandler.handle(ex);
					}
				}
			}
		}
	}
	
	/**
	 * link a record element to this anamnesis (every episodehas a number of treatments related to that episode)
	 * We try to find an episode for each of the diagnoses of the Konsultation given
	 * @param k
	 * @param r
	 */
	public void link(Konsultation k, RecordElement r){
		List<IDiagnose> kdl=k.getDiagnosen();
		for(IDiagnose dg:kdl){
			Element episode=hLink.get(dg);
			if(episode==null){
				episode=new Element("episode",XChangeContainer.ns);
				hLink.put(dg,episode);
				e.addContent(episode);
				episode.setAttribute("date",new TimeTool(k.getDatum()).toString(TimeTool.DATE_ISO));
				episode.setAttribute("id",StringTool.unique("episode"));
				Element eDiag=new Element("diagnosis",XChangeContainer.ns);
				episode.addContent(eDiag);
				eDiag.setAttribute("codesystem",dg.getCodeSystemName());
				eDiag.setAttribute("code",dg.getCode());
				episode.setAttribute("title",dg.getLabel());
			}
			Element episodeRef=new Element("episode",XChangeContainer.ns);
			episodeRef.setAttribute("id", episode.getAttributeValue("id"));
			r.e.addContent(episodeRef);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void doImport(RecordElement r, Konsultation k){
		List<Element> eRefs=r.e.getChildren("episode",XChangeContainer.ns);
		if(eRefs!=null){
			for(Element eRef:eRefs){
				String id=eRef.getAttributeValue("id");
				Element episode=hElements.get(id);
				if(episode!=null){
					IDiagnose dg=hBacklink.get(episode);
					if(dg!=null){
						k.addDiagnose(dg);
					}
				}
			}
		}
	}
	
	public String toString(){
		StringBuilder ret=new StringBuilder();
		List<EpisodeElement> episodes=getEpisodes();
		for(EpisodeElement episode:episodes){
			ret.append(episode.getDiagnosis()).append(": ")
				.append(new TimeTool(episode.getBeginDate()).toString(TimeTool.DATE_GER));
			String end=episode.getEndDate();
			if(end.equals("")){
				ret.append(": offen.");
			}else{
				ret.append("-").append(new TimeTool(end).toString(TimeTool.DATE_GER));
			}
			ret.append("\n").append(episode.getText()).append("\n");
		}
		
		return ret.toString();
	}
	/*
	public Result<Element> create(Patient p){
		Fall[] faelle=p.getFaelle();
		for(Fall fall:faelle){
			Konsultation[] konsultationen=fall.getBehandlungen(false);
			for(Konsultation k:konsultationen){
				List<IDiagnose> kdl=k.getDiagnosen();
				
			}
		}
		return new Result<Element>(e);
	}
	*/
}
