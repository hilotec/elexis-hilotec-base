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
 *  $Id: IChunk.java 5224 2009-03-26 21:15:07Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.text;

import java.util.List;

public interface IChunk {
	public List<IChunk> getChildren();
	public IChunk getParent();
	public String getTitle();
	public String getText();
	public IStyle getStyle(String displayType);
}