package ch.elexis.views.codesystems;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.util.ImporterPage;
import ch.elexis.util.SWTHelper;

public class BlockImporter extends ImporterPage {
	
	@Override
	public Composite createPage(Composite parent){
		FileBasedImporter fbi= new FileBasedImporter(parent,this);
		fbi.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		return fbi;
	}
	
	@Override
	public IStatus doImport(IProgressMonitor monitor) throws Exception{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getDescription(){
		return "Leistungsblöcke importieren";
	}
	
	@Override
	public String getTitle(){
		// TODO Auto-generated method stub
		return null;
	}
	
}
