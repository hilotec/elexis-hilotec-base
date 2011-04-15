/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Medikament.java 5932 2010-01-14 22:30:04Z rgw_ch $
 *******************************************************************************/
package ch.elexis.artikel_ch.data;

import ch.elexis.data.Artikel;

public class Medikament extends Artikel {
	
	@Override
	protected String getConstraint(){
		return "Typ='Medikament'"; //$NON-NLS-1$
	}
	
	protected void setConstraint(){
		set("Typ", "Medikament"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public String getCodeSystemName(){
		return Messages.Medikament_CodeSystemNameMedicaments;
	}
	
	@Override
	public String getLabel(){
		return get("Name"); //$NON-NLS-1$
	}
	
	@Override
	public String getCode(){
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
	public boolean isDragOK(){
		return true;
	}
	
}