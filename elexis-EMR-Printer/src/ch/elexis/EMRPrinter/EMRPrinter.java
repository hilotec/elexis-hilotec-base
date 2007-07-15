package ch.elexis.EMRPrinter;
import org.jdom.Element;

import ch.elexis.data.Patient;
import ch.elexis.exchange.XChangeExporter;
import ch.elexis.exchange.elements.ContactElement;
import ch.elexis.util.Log;
import ch.elexis.util.Result;

/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: EMRPrinter.java 2808 2007-07-15 10:30:43Z rgw_ch $
 *******************************************************************************/

/**
 * Class to export electronic medical records (EMR) to a printer
 */
public class EMRPrinter extends XChangeExporter{
	Patient mine;
	ContactElement base;
	boolean bSuccess;
	
	public EMRPrinter() {
	}
	public boolean canHandle(Class clazz) {
		if(clazz.equals(Patient.class)){
			return true;
		}
		return false;
	}

	public boolean finalizeExport() {
		new PrintDialog(this).open();
		return bSuccess;
	}

	public Result<Element> store(Object output) {
		if(output instanceof Patient){
			mine=(Patient)output;
			base=addContact(mine, true);
			bSuccess=true;
			return new Result<Element>(base.getElement());
		}
		return new Result<Element>(Log.ERRORS,1,"invalid element",null,true);
	}

}
