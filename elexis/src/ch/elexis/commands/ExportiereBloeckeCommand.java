/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ExportiereBloeckeCommand.java 5074 2009-02-01 15:58:15Z rgw_ch $
 *******************************************************************************/
package ch.elexis.commands;

import java.io.FileOutputStream;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.exchange.XChangeContainer;
import ch.elexis.exchange.elements.ServiceBlockElement;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.XMLTool;
import ch.rgw.tools.Result.SEVERITY;

public class ExportiereBloeckeCommand extends AbstractHandler {
	public static final String ID = "serviceblocks.export";
	
	public Object execute(ExecutionEvent event) throws ExecutionException{
		Query<Leistungsblock> qbe = new Query<Leistungsblock>(Leistungsblock.class);
		List<Leistungsblock> bloecke = qbe.execute();
		BlockContainer bc=new BlockContainer();
		for(Leistungsblock block:bloecke){
			bc.store(block);
		}
		
		return new Boolean(bc.finalizeExport());
	}
	
	static{

	}
	static class BlockContainer extends XChangeContainer {
		Document doc;
		Element eRoot;
		Element lbs;
		
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
			lbs=new Element(ServiceBlockElement.ENCLOSING,ns);
			eRoot.addContent(lbs);
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
			FileDialog fd=new FileDialog(Desk.getTopShell(),SWT.SAVE);
			fd.setText("Blockbeschreibung als XChange-Datei speichern");
			fd.setFilterExtensions(new String[] {
				"*.xchange"
			});
			fd.setFilterNames(new String[] {
				"SGAM-xChange Dateien"
			});
			String filename=fd.open();
			if(filename!=null){
				Format format = Format.getPrettyFormat();
				format.setEncoding("utf-8");
				XMLOutputter xmlo = new XMLOutputter(format);
				String xmlAspect = xmlo.outputString(doc);
				try{
					FileOutputStream fos=new FileOutputStream(filename);
					fos.write(xmlAspect.getBytes());
					fos.close();
				}catch(Exception ex){
					ExHandler.handle(ex);
				}
			}
			return false;
		}
		
		public Result<Element> store(Object output){
			if(output instanceof Leistungsblock){
				ServiceBlockElement sbe=new ServiceBlockElement(this,(Leistungsblock)output);
				lbs.addContent(sbe);
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
