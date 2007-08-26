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
 *    $Id: Rechnungssteller.java 3016 2007-08-26 13:26:12Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

/**
 * This class is only needed to denote a person or organization that can
 * make a bill. It is simply a contact.
 * @author Gerry
 *
 */
public class Rechnungssteller extends Kontakt {

	public static Rechnungssteller load(String id){
		return new Rechnungssteller(id);
	}
	
	protected Rechnungssteller(String id){
		super(id);
	}
	protected Rechnungssteller(){}
}
