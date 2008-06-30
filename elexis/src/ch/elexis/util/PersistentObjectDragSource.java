/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: PersistentObjectDragSource.java 4089 2008-06-30 14:21:21Z rgw_ch $
 *******************************************************************************/
package ch.elexis.util;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import ch.elexis.data.PersistentObject;

/**
 * Universelle DragSource für  PersistentObjects. Kann an jedes Control, das PersistentObjects
 * enthält angehängt werden.
 * Wegen eines seltsamen Verhaltens von MacOSX, das die Selection zwischen dragStart und dragSetData zu 
 * vergessen scheint, wählen wir hier eine etwas fragwürdige Methode mit lokalem 
 * Zwischenspeichern der Selection.
 * @author gerry
 *
 */
public class PersistentObjectDragSource extends DragSourceImpl implements DragSourceListener {
	private final StructuredViewer viewer;
	private IStructuredSelection ts;
	
	public PersistentObjectDragSource(final StructuredViewer v){
		viewer=v;
		v.addDragSupport(DND.DROP_COPY, new Transfer[] {TextTransfer.getInstance()}, this);
	}
	
	public void dragStart(final DragSourceEvent event)
	{	//System.out.println("Drag start");
		ts=(IStructuredSelection)viewer.getSelection();
		//IStructuredSelection select=(IStructuredSelection)viewer.getSelection();
	    //Object[] sel=select.toArray();
		Object[] sel=ts.toArray();
	    if((sel==null) || (sel.length==0)){
	        event.doit=false;
	        draggedObject=null;
	    }else{
		    Object s=sel[0];
		    PersistentObject po=null;
		    if (s instanceof ch.elexis.util.Tree) {
		        ch.elexis.util.Tree tree = (ch.elexis.util.Tree) s;
		        if (tree.contents  instanceof PersistentObject) {
		            po = (PersistentObject) tree.contents;
		            event.doit=po.isDragOK();
		        }
		    }else if(s instanceof PersistentObject){
		        po = (PersistentObject) s;
		        event.doit=po.isDragOK();
		    }else{
		        event.doit=false;
		    }
		    if(event.doit){
		    	draggedObject=po;
		    }
	    }
	}

	public void dragSetData(final DragSourceEvent event)
	{
		//IStructuredSelection select=(IStructuredSelection)viewer.getSelection();
	    //Object[] sel=select.toArray();
		Object[] sel=ts.toArray();				// Workaround für MacOS X (???)
	    StringBuilder sb=new StringBuilder();
	    for(Object s:sel){
	        if(s instanceof ch.elexis.util.Tree){
	            PersistentObject o=(PersistentObject)((ch.elexis.util.Tree)s).contents;
	            sb.append(o.storeToString()).append(","); //$NON-NLS-1$
	        }else if(s instanceof PersistentObject){
	            sb.append(((PersistentObject)s).storeToString()).append(","); //$NON-NLS-1$
	        }
	        else{
	            sb.append("error").append(","); //$NON-NLS-1$ //$NON-NLS-2$
	            //event.data="error";
	        }
	    }
	    event.data=sb.toString().replace(",$",""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void dragFinished(final DragSourceEvent event)
	{
	    // TODO Auto-generated method stub
	    
	}
	
	public static PersistentObject getDraggedObject(){
		return draggedObject;
	}
}