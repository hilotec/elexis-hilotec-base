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
 *  $Id: IRange.java 5224 2009-03-26 21:15:07Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.text;

public interface IRange {
	public int getPosition();
	public int getLength();

}
