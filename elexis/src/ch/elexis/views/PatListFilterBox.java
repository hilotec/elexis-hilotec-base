package ch.elexis.views;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.util.ListDisplay;
import ch.elexis.util.PersistentObjectDropTarget;

public class PatListFilterBox extends ListDisplay<PersistentObject> implements IFilter{
    PersistentObjectDropTarget dropTarget;
    private static final String FELD_HINZU="Feld...";
    private static final String LEEREN="Leeren";

    
	PatListFilterBox(Composite parent){
		super(parent,SWT.NONE,new LDListener(){

			public String getLabel(Object o) {
				if(o instanceof PersistentObject){
					return o.getClass().getSimpleName()+":"+((PersistentObject)o).getLabel();
				}else{
					return o.toString();
				}
			}

			public void hyperlinkActivated(String l) {
				// TODO Auto-generated method stub
				
			}});
		addHyperlinks(FELD_HINZU,LEEREN);
		dropTarget=new PersistentObjectDropTarget("Statfilter",this,new DropReceiver());

	}
	private  class DropReceiver implements PersistentObjectDropTarget.Receiver {
		public void dropped(final PersistentObject o, final DropTargetEvent ev) {
			PatListFilterBox.this.add(o);
		}

		public boolean accept(final PersistentObject o) {
			return true;
		}
	}
	
	public boolean select(Object toTest) {
		if(toTest instanceof Patient){
			Patient p=(Patient)toTest;
			
		}
		return false;
	}
	
	public interface IFilterObject<T>{
		public boolean accept(Patient p, T o);
	}
}
