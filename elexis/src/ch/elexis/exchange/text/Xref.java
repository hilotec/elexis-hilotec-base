/*******************************************************************************
 * Copyright (c) 2009-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *  $Id: Xref.java 6153 2010-02-19 18:34:28Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.text;

/**
 * An Xref is some additional meta information of a range. It has a provider that
 * can produce such meta informations
 * @author gerry
 *
 */
public class Xref implements IXref {
	
	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getProvider() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int getLength() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int getPosition() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void setLength(int pos){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setPosition(int pos){
		// TODO Auto-generated method stub
		
	}
	
}
