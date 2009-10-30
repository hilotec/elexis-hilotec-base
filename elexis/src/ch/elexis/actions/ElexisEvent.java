package ch.elexis.actions;

import ch.elexis.data.PersistentObject;

public class ElexisEvent {
	/** The Object was newly created */
	public static final int EVENT_CREATE=0x0001;
	/** The object is about to be deleted */
	public static final int EVENT_DELETE=0x0002;
	/** The object has changed some of its properties */
	public static final int EVENT_UPDATE=0x0004;
	/** All Objects of this class have been reloaded */
	public static final int EVENT_RELOAD=0x0008;
	/** The Object has been selected */
	public static final int EVENT_SELECTED=0x0010;
	/** All Objects of this type have been deselected */
	public static final int EVENT_DESELECTED=0x0020;
	
	PersistentObject obj;
	Class<? extends PersistentObject> objClass;
	int type;
	
	public ElexisEvent(PersistentObject o, Class<? extends PersistentObject> c, int type){
		obj=o;
		objClass=c;
		this.type=type;
	}
	
	public PersistentObject getObject(){
		return obj;
	}
	public Class<? extends PersistentObject> getObjectClass(){
		return objClass;
	}
	
	public int getType(){
		return type;
	}
	public boolean matches(ElexisEvent event){
		if(event.getObject()!=null){
			if(!getObject().getId().equals(event.getObject().getId())){
				return false;
			}
		}
		if(event.getObjectClass()!=null){
			if(!getObjectClass().equals(event.getObjectClass())){
				return false;
			}
		}
		if(event.getType()!=0){
			if((type&event.getType())==0){
				return false;
			}
		}
		return true;
	}
}
