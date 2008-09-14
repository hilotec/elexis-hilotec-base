/*******************************************************************************
 * Copyright (c) 2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: PrintFindingsDialog.java 2516 2007-06-12 15:56:07Z rgw_ch $
 *******************************************************************************/
package ch.elexis.befunde.xchange;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import ch.elexis.befunde.Messwert;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.exchange.IExchangeContributor;
import ch.elexis.exchange.IExchangeDataHandler;
import ch.elexis.exchange.XChangeContainer;
import ch.elexis.exchange.elements.MedicalElement;
import ch.rgw.tools.StringTool;

/**
 * Plug into the elexis xChange model.
 * Since we have that slightly weird model of finding definition in this
 * elexis-befunde-Plugin, this is more complicated as in other plugins.
 * First we need to figure out the Names of the Tabs in the FindingsView. These are
 * encoded in the hashtable as value of the Item "Setup". We can retrieve this item 
 * with the call to Messwert.getSetup(); Its Befunde-Member holds a hashtable that encodes
 * all finding items.
 * The names of the items are listet as hash value with the key "names", a String separated by SETUP_SEPARATOR
 * The fields of each item are listed as hash values with the key [name]_FIELDS. Each is a String
 * separated by SETUP_SEPARATOR. Every field contains a name and a multiline-indicator, separated by
 * SETUP_CHECKSEPARATOR. The name can, optionally, contain a script definition. In that case it ist a
 * String of the form result=script
 * 
 * We create from every finding item an xChange:BefundeItem and from every finding entry an 
 * xChange:BefundElement.
 * Thus if we have a tab "Cardio" defined, with the elements RRSyst,RRDiast,HR, there will be three
 * FindingElements called Cardio:RRSyst, Cardio:RRDiast, Cardio:HR
 * @author Gerry
 *
 */
public class XChangeContributor implements IExchangeContributor {
	private Hashtable<String, Object> hash;
	private Hashtable<String,String[]> params=new Hashtable<String, String[]>();
	private String[] paramNames;
	
	@SuppressWarnings("unchecked")
	public void exportHook(MedicalElement me) {
		Patient pat=(Patient)me.getContainer().getMapping(me);
		if(pat!=null){
			Messwert setup=Messwert.getSetup();
			hash=setup.getHashtable("Befunde");
			String names=(String)hash.get("names");
			if(!StringTool.isNothing(names)){
				paramNames=names.split(Messwert.SETUP_SEPARATOR);
				for(String n:paramNames){
					String vals=(String)hash.get(n+"_FIELDS");	
					if(vals!=null){
						String[]flds=vals.split(Messwert.SETUP_SEPARATOR);
						for(int i=0;i<flds.length;i++){
							flds[i]=flds[i].split(Messwert.SETUP_CHECKSEPARATOR)[0];
							String [] header=flds[i].split("=",2);
							flds[i]=header[0];
						}
						params.put(n, flds);
					}
				}
				
			}
			Query<Messwert> qbe=new Query<Messwert>(Messwert.class);
			qbe.add("PatientID", "=", pat.getId());
			List<Messwert> mw=qbe.execute();
			for(Messwert m:mw){
				String name=m.get("Name");
				String[] fl=params.get(name);
				if(fl!=null){
					for(String field:fl){
						BefundElement.addBefund(me,m,field);
					}
				}
			}
		}
		
		
	}

	public void importHook(XChangeContainer container, PersistentObject context) {
		// TODO Auto-generated method stub

	}

	public boolean init(MedicalElement me, boolean export) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		

	}

	public IExchangeDataHandler[] getImportHandlers(){
		// TODO Auto-generated method stub
		return null;
	}

}
