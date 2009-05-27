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
 *  $Id: ETFDropReceiver.java 5320 2009-05-27 16:51:14Z rgw_ch $
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

	Hashtable<Class<?>, IKonsExtension> targets;
	
	ETFDropReceiver(final EnhancedTextField et){
		etf=et;
		targets=new Hashtable<Class<?>, IKonsExtension>();
	}
	
	public void addReceiver(final Class<?> clazz, final IKonsExtension rec){
		targets.put(clazz, rec);
	}
	
	public void removeReceiver(final Class<?> clazz, final IKonsExtension rec){
		targets.remove(clazz);
	}
	
	public boolean accept(final PersistentObject o) {
		if(targets.get(o.getClass())!=null){
			return true;
		}
		return false;
	}

	public void dropped(final PersistentObject o, final DropTargetEvent ev) {
		IKonsExtension rec=targets.get(o.getClass());
		if(rec!=null){
			Point point=Desk.getDisplay().getCursorLocation();
			Point mapped=Desk.getDisplay().map(null, etf.text, point);
			Point maxOffset=etf.text.getLocationAtOffset(etf.text.getCharCount());
			int pos=etf.text.getCharCount();
			if(mapped.y<maxOffset.y){
				pos=etf.text.getOffsetAtLocation(new Point(0,mapped.y));
			}
			rec.insert(o, pos);
		}
		
	}

}
