package ch.elexis.util;

import ch.elexis.data.PersistentObject;

public abstract class DragSourceImpl {
	
	protected DragSourceImpl(){
		draggedObject=null;
	}
	
	protected static PersistentObject draggedObject;
}
