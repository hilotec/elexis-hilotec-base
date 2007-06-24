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
 *  $Id: ETFDropReceiver.java 1724 2007-02-02 21:17:21Z rgw_ch $
 *******************************************************************************/
package ch.elexis.text;

import java.util.Hashtable;

import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Point;

import ch.elexis.Desk;
import ch.elexis.data.PersistentObject;
import ch.elexis.util.IKonsExtension;
import ch.elexis.util.PersistentObjectDropTarget.Receiver;

public class ETFDropReceiver implements Receiver{
	EnhancedTextField etf;
	Hashtable<Class, IKonsExtension> targets;
	
	ETFDropReceiver(EnhancedTextField et){
		etf=et;
		targets=new Hashtable<Class, IKonsExtension>();
	}
	
	public void addReceiver(Class clazz, IKonsExtension rec){
		targets.put(clazz, rec);
	}
	
	public void removeReceiver(Class clazz, IKonsExtension rec){
		targets.remove(clazz);
	}
	
	public boolean accept(PersistentObject o) {
		if(targets.get(o.getClass())!=null){
			return true;
		}
		return false;
	}

	public void dropped(PersistentObject o, DropTargetEvent ev) {
		IKonsExtension rec=targets.get(o.getClass());
		if(rec!=null){
			Point point=Desk.theDisplay.getCursorLocation();
			Point mapped=Desk.theDisplay.map(null, etf.text, point);
			Point maxOffset=etf.text.getLocationAtOffset(etf.text.getCharCount());
			int pos=etf.text.getCharCount();
			if(mapped.y<maxOffset.y){
				pos=etf.text.getOffsetAtLocation(new Point(0,mapped.y));
			}
			rec.insert(o, pos);
		}
		
	}

}
