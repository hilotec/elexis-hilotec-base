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
 * $Id: PhysioComparator.java 5144 2009-02-17 21:40:23Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import java.util.Comparator;

public class PhysioComparator implements Comparator<PhysioLeistung> {
	
	public int compare(PhysioLeistung pl0, PhysioLeistung pl1){
		return pl0.getCode().compareTo(pl1.getCode());
	}
	
}
