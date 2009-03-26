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
 *  $Id: Chunk.java 5224 2009-03-26 21:15:07Z rgw_ch $
 *******************************************************************************/

package ch.elexis.exchange.text;

import java.util.List;

/**
 * A Chunk is a piece of (possibly structured) text that can contain some markups and
 * display hints. 
 * @author gerry
 *
 */
public class Chunk implements IChunk {

	public List<IChunk> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	public IChunk getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	public IStyle getStyle(String displayType) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
