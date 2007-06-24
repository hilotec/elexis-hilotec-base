/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Container.java 2582 2007-06-23 21:11:15Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange;

import java.io.CharArrayReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.data.*;
import ch.elexis.exchange.elements.AnamnesisElement;
import ch.elexis.exchange.elements.DocumentElement;
import ch.elexis.exchange.elements.LabElement;
import ch.elexis.exchange.elements.MedicalElement;
import ch.elexis.exchange.elements.RecordElement;
import ch.elexis.util.*;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;


@SuppressWarnings("unchecked")
public class Container implements IExecutableExtension{
	private static final long serialVersionUID = 4519140729265283533L;
	public static final String Version="0.2.0";
	
	public static final Namespace ns=Namespace.getNamespace("SgamXChange","http://informatics.sgam.ch/eXChange");
	public static final Namespace nsxsi=Namespace.getNamespace("xsi","http://www.w3.org/2001/XML Schema-instance");
	public static final Namespace nsschema=Namespace.getNamespace("schemaLocation","http://informatics.sgam.ch/eXChange SgamXChange.xsd");

	protected Document doc;
	protected Element eRoot;
	//AnamnesisElement anamnesis;
	private boolean valid;
	


	
	
	
	
	
	
	private Result<String> importDocuments(Patient p, Element ed){
		List<Element> eDocs=ed.getChildren("document", ns);
		Result<String> ret=new Result<String>("OK");
		if(eDocs!=null){
			
		}
		return ret;
	}

	/**
	 * Return a concise description of this Container. The default implementation returns an empty String
	 * @return
	 */
	public String getDescription(){
		return "";
	}
	
	
	
	
	/*
	public static boolean addBinaryContent(Element e, byte[] content, boolean inline){
		
	}
	*/
	
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		// TODO Auto-generated method stub
		
	}

}
