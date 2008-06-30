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
 * $Id: PersistentObjectDragSource2.java 4089 2008-06-30 14:21:21Z rgw_ch $
 *******************************************************************************/

package ch.elexis.util;

import java.util.List;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;

import ch.elexis.data.PersistentObject;

public class PersistentObjectDragSource2 extends DragSourceImpl implements DragSourceListener {
	Draggable renderer;
	Control dragSource;
	List<PersistentObject> selection;
	
	public PersistentObjectDragSource2(final Control source, final Draggable renderer){
		this.renderer=renderer;
		dragSource=source;
		DragSource mine=new DragSource(source,DND.DROP_COPY);
		mine.setTransfer(new Transfer[] {TextTransfer.getInstance()});
		mine.addDragListener(this);
	}
	public void dragFinished(final DragSourceEvent event) {
		// TODO Auto-generated method stub

	}

	public void dragSetData(final DragSourceEvent event) {
		
	    StringBuilder sb=new StringBuilder();
	    for(PersistentObject s:selection){
	    	sb.append(s.storeToString()).append(","); //$NON-NLS-1$
	    }
    
	    event.data=sb.toString().replace(",$",""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void dragStart(final DragSourceEvent event) {

		selection=renderer.getSelection();
		//IStructuredSelection select=(IStructuredSelection)viewer.getSelection();
	    //Object[] sel=select.toArray();
	    if((selection==null) || (selection.isEmpty())){
	        event.doit=false;
	    }else{
	        event.doit=selection.get(0).isDragOK();
	    }
	    if(event.doit){
	    	PersistentObjectDragSource.draggedObject=selection.get(0);
	    }
	}
	
	public interface Draggable{
		public List<PersistentObject> getSelection();
	}
}
