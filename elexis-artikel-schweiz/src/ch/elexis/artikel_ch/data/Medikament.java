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
 *  $Id: Medikament.java 1742 2007-02-06 20:48:17Z rgw_ch $
 *******************************************************************************/
package ch.elexis.artikel_ch.data;

import ch.elexis.data.Artikel;

public class Medikament extends Artikel{

	@Override
	protected String getConstraint() {
		return "Typ='Medikament'";
	}
	protected void setConstraint(){
		set("Typ","Medikament");
	}
	@Override
	public String getCodeSystemName() {
			return "Medikamente";
	}
	
	
	@Override
	public String getLabel() {
		return get("Name");
	}
	@Override
	public String getCode() {
		return getPharmaCode();
	}
	public static Medikament load(String id){
		return new Medikament(id);
	}
	protected Medikament(){}
	protected Medikament(String id){
		super(id);
	}
	@Override
	public boolean isDragOK() {
		return true;
	}
	
}