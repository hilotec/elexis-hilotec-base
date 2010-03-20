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
 *  $Id: IMarkup.java 6241 2010-03-20 12:25:06Z rgw_ch $
 *******************************************************************************/

package ch.elexis.text.model;


public interface IMarkup extends IRange {
	public enum TYPE{NORMAL,BOLD,EM,UNDERLINE,ITALIC,STRIKETHRU};
	public TYPE getType();
}
