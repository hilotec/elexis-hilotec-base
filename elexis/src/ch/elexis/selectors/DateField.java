/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: DateField.java 5354 2009-06-13 20:03:52Z rgw_ch $
 *******************************************************************************/
package ch.elexis.selectors;

import java.util.Properties;

import org.eclipse.swt.widgets.Composite;

public class DateField extends TextField {
	
	public DateField(Composite parent, int displayBits, Properties props){
		super(parent, displayBits, props);
	}
	
}
