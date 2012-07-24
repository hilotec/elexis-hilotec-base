package ch.elexis.ebanking_ch;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.elexis.banking.ESRRecord;
import ch.elexis.data.Rechnung;

public class FilterSearchField extends ViewerFilter {
	
	private static FilterSearchField instance;
	
	private FilterSearchField(){}
	
	public static FilterSearchField getInstance(){
		if (null == instance) {
			instance = new FilterSearchField();
		}
		return instance;
	}
	
	private String searchString;
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element){
		if (searchString == null)
			return true;
		ESRRecord e = (ESRRecord) element;
		
		String datum = e.get("Datum");
		String betrag = e.getBetrag().getAmountAsString();
		String patLabel = e.getPatient().getLabel().toLowerCase();
		
		if(datum.matches(".*" + searchString + ".*")) return true;
		if(betrag.matches(".*" + searchString + ".*")) return true;
		if(patLabel.matches(".*" + searchString + ".*")) return true;
		if(e.getEinlesedatatum().matches(".*" + searchString + ".*")) return true;
		if(e.getVerarbeitungsdatum().matches(".*" + searchString + ".*")) return true;
		
		Rechnung rn = e.getRechnung();
		if (rn != null) {
			String rgNr = rn.getNr();
			if(rgNr.matches(".*" + searchString + ".*")) return true;
		}
		
		return false;
	}
	
	public void setSearchText(String s){
		if (s == null || s.length() == 0 || s.startsWith("#"))
			searchString = s;
		else
			searchString = s.toLowerCase(); //$NON-NLS-1$ //$NON-NLS-2$
		// filter "dirty" characters
		if (searchString != null)
			searchString = searchString.replaceAll("[^#\\.$, a-zA-Z0-9]", "");
	}
}
