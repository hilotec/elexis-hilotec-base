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
 *  $Id: IRange.java 6241 2010-03-20 12:25:06Z rgw_ch $
 *******************************************************************************/

package ch.elexis.text.model;

public interface IRange {
	public int getPosition();
	public int getLength();
	public void setPosition(int pos);
	public void setLength(int pos);
}
