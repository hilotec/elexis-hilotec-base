/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: MFUList.java 2867 2007-07-22 19:27:12Z rgw_ch $
 *******************************************************************************/

package ch.elexis.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to keep track of the usage of certain objects
 * @author Gerry
 *
 * @param <T>
 */
public class MFUList<T> extends ArrayList{
	private static final long serialVersionUID = 3966224865760348882L;
	int maxNum;
	
	public MFUList(int objectsToStart, int objectsToKeep){
		super(objectsToStart);
		maxNum=objectsToKeep;
	}
	
	public void count(T obj){
		for(Entry<T> e:(List<Entry<T>>)this){
			
		}

	}
	static class Entry<X> implements Comparable<Entry<X>>{
		int count;
		Object o;
		public Entry(X obj){
			o=obj;
			count=0;
		}
		public int compareTo(Entry<X> obj) {
			return obj.count-count;
		}
		@Override
		public boolean equals(Object obj) {
			
			return super.equals(obj);
		}
		
	}
}
