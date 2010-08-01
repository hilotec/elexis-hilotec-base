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

import org.eclipse.swt.graphics.Rectangle;

/**
 * An Irange is some part of a document. It has a position and a length within the text
 * Optionally, it can be places outside the text flos. In that case, it must provide a
 * viewport position relative to the character indicated by position. The contents of the IRAnge is
 * toally implementation specific. It ,might be some text or some graphics or both.  
 * @author gerry
 *
 */
public interface IRange {
	/** Offset from the beginning of the document */
	public int getPosition();
	/** Length in characters. can be 0 */
	public int getLength();
	public void setPosition(int pos);
	public void setLength(int pos);
	/** View Port where the range is displayed */
	public Rectangle getViewPort();
}
