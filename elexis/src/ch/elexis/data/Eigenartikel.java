/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: Eigenartikel.java 4683 2008-11-15 20:39:23Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

public class Eigenartikel extends Artikel {
	
	@Override
	protected String getConstraint(){
		return "Typ='Eigenartikel'";
	}
	
	protected void setConstraint(){
		set("Typ", "Eigenartikel");
	}
	
	@Override
	public String getCodeSystemName(){
		return "Eigenartikel";
	}
	
	@Override
	public String getLabel(){
		return get("Name");
	}
	
	@Override
	public String getCode(){
		return get("SubID");
	}
	
	public String getGroup(){
		return checkNull(get("Codeclass"));
	}
	
	public static Eigenartikel load(String id){
		return new Eigenartikel(id);
	}
	
	protected Eigenartikel(){}
	
	protected Eigenartikel(String id){
		super(id);
	}
	
	@Override
	public boolean isDragOK(){
		return true;
	}
}
