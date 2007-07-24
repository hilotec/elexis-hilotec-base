/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ReplaceCallback.java 2891 2007-07-24 15:45:59Z rgw_ch $
 *******************************************************************************/


package ch.elexis.text;

public interface ReplaceCallback {
	public Object replace(String in);
}
