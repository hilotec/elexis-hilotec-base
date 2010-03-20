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
 *  $Id: IStyle.java 6241 2010-03-20 12:25:06Z rgw_ch $
 *******************************************************************************/

package ch.elexis.text.model;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

public interface IStyle {
	public Rectangle getBounds();
	public RGB getBackground();
	public double getOpacity();
}
