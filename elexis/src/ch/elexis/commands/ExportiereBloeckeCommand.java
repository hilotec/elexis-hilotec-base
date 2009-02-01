/*******************************************************************************
 * Copyright (c) 2008-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ExportiereBloeckeCommand.java 5073 2009-02-01 15:24:52Z rgw_ch $
 *******************************************************************************/
package ch.elexis.commands;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.jdom.Document;
import org.jdom.Element;

import ch.elexis.Hub;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Xid;
import ch.elexis.exchange.XChangeContainer;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.XMLTool;
import ch.rgw.tools.Result.SEVERITY;

public class ExportiereBloeckeCommand extends AbstractHandler {
	public static final String BLOECKE = "leistungsbloecke";

	
	public Object execute(ExecutionEvent event) throws ExecutionException{
		Query<Leistungsblock> qbe = new Query<Leistungsblock>(Leistungsblock.class);
		List<Leistungsblock> bloecke = qbe.execute();
		for(Leistungsblock block:bloecke){
			
		}
		return new Boolean(true);
	}
	
	static{

	}
	static class BlockContainer extends XChangeContainer {
		Document doc;
		Element eRoot;
		
		public BlockContainer(){
			doc = new Document();
			eRoot = new Element(ROOT_ELEMENT, ns);
			eRoot.addNamespaceDeclaration(nsxsi);
			eRoot.addNamespaceDeclaration(nsschema);
			eRoot.setAttribute("timestamp", new TimeTool().toString(TimeTool.DATETIME_XML));
			eRoot.setAttribute("id", XMLTool.idToXMLID(StringTool.unique("xChange")));
			eRoot.setAttribute("origin", XMLTool.idToXMLID(Hub.actMandant.getId()));
			eRoot.setAttribute("destination", "undefined");
			eRoot.setAttribute("responsible", XMLTool.idToXMLID(Hub.actMandant.getId()));
			doc.setRootElement(eRoot);
		}
		
		@Override
		public Kontakt findContact(String id){
			return null;
		}
		
		public boolean canHandle(Class<? extends PersistentObject> clazz){
			if(clazz.equals(Leistungsblock.class)){
				return true;
			}
			return false;
		}
		
		public boolean finalizeExport(){
			return false;
		}
		
		public Result<Element> store(Object output){
			if(output instanceof Leistungsblock){
				
			}
			return new Result<Element>(SEVERITY.ERROR,1,"Can't handle object type "+output.getClass().getName(),null,true);
		}
		
		public Result finalizeImport(){
			// TODO Auto-generated method stub
			return null;
		}
		
		public Result<Object> load(Element input, Object context){
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
