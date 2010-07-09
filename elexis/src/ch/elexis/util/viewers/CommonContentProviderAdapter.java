package ch.elexis.util.viewers;

import java.util.HashMap;

import org.eclipse.jface.viewers.Viewer;

import ch.elexis.util.viewers.ViewerConfigurer.CommonContentProvider;

public class CommonContentProviderAdapter implements CommonContentProvider {
	
	public Object[] getElements(Object inputElement){
		return null;
	}
	
	public void dispose(){

	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){

	}
	
	public void startListening(){
	// TODO Automatisch erstellter Methoden-Stub
	
	}
	
	public void stopListening(){
	// TODO Automatisch erstellter Methoden-Stub
	
	}
	
	public void changed(HashMap<String,String> values){
	// TODO Automatisch erstellter Methoden-Stub
	
	}
	
	public void reorder(String field){
	// TODO Automatisch erstellter Methoden-Stub
	
	}
	
	public void selected(){
	// TODO Automatisch erstellter Methoden-Stub
	}
}