package ch.elexis.notes;

import java.util.Comparator;

public class NoteComparator implements Comparator<Note> {
	
	public int compare(Note arg0, Note arg1){
		return arg0.getLabel().compareToIgnoreCase(arg1.getLabel());
	}
}
