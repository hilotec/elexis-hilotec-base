package ch.elexis.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;

import ch.elexis.Hub;
import ch.elexis.data.PersistentObject;
import ch.elexis.views.codesystems.ICodeSelectorTarget;

/**
 * Universelles DropTarget für PersistentObjects
 * @author gerry
 *
 */
public class PersistentObjectDropTarget implements DropTargetListener, ICodeSelectorTarget {
	Receiver rc;
	String name="";
	private Color normalColor;
    private Color highlightColor;
	private Control mine;
    
	public PersistentObjectDropTarget(Control target, Receiver r){
		normalColor = target.getBackground();
    	highlightColor = target.getDisplay().getSystemColor(SWT.COLOR_RED);
    	mine=target;
		rc=r;
		DropTarget dtarget=new DropTarget(target,DND.DROP_COPY);
        final TextTransfer textTransfer = TextTransfer.getInstance();
        Transfer[] types = new Transfer[] {textTransfer};
        dtarget.setTransfer(types);
        dtarget.addDropListener(this);
	}
	
	public PersistentObjectDropTarget(String name, Control target, Receiver r){
		this(target,r);
		this.name=name;
	}
	public void dragEnter(DropTargetEvent event) {
		/*
		boolean bOk=false;
		String drp=(String)event.data;
        String[] dl=drp.split(",");
        for(String obj:dl){
            PersistentObject dropped=Hub.poFactory.createFromString(obj);
            if(rc.accept(dropped)){
            	bOk=true;
            }
        }
		if(bOk){
			event.detail=DND.DROP_COPY;
		}else{
			event.detail=DND.DROP_NONE;
		}
		*/
		event.detail=DND.DROP_COPY;

	}

	public void dragLeave(DropTargetEvent event) {
		// TODO Auto-generated method stub

	}

	public void dragOperationChanged(DropTargetEvent event) {
		// TODO Auto-generated method stub

	}

	public void dragOver(DropTargetEvent event) {
		// TODO Auto-generated method stub

	}

	public void drop(DropTargetEvent event) {
		String drp=(String)event.data;
        String[] dl=drp.split(","); //$NON-NLS-1$
        for(String obj:dl){
            PersistentObject dropped=Hub.poFactory.createFromString(obj);
            rc.dropped(dropped,event);
        }

	}

	public void dropAccept(DropTargetEvent event) {
		// TODO Auto-generated method stub

	}
	 
	public interface Receiver{
		public void dropped(PersistentObject o, DropTargetEvent e);
		public boolean accept(PersistentObject o);
	}

	public void codeSelected(PersistentObject obj) {
		rc.dropped(obj, null);
	}
	
	public String getName() {
		return name;
	}
	public void registered(boolean bIsRegistered) {
		highlight(bIsRegistered);
		
	}
	
    private void highlight(boolean bOn) {
		if (bOn) {
			mine.setBackground(highlightColor);
		} else {
			mine.setBackground(normalColor);
		}
    }

}
