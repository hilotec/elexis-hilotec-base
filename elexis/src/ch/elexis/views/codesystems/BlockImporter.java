package ch.elexis.views.codesystems;

import java.io.FileInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.exchange.BlockContainer;
import ch.elexis.util.ImporterPage;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;

public class BlockImporter extends ImporterPage {
	
	@Override
	public Composite createPage(Composite parent){
		FileBasedImporter fbi = new FileBasedImporter(parent, this);
		fbi.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		return fbi;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IStatus doImport(IProgressMonitor monitor) throws Exception{
		
		String filename = results[0];
		if (StringTool.isNothing(filename)) {
			return new Status(SWT.ERROR, "ch.elexis", "No file given");
		}
		
		try {
			FileInputStream fips = new FileInputStream(filename);
			BlockContainer blc = new BlockContainer(fips);
			if (blc.finalizeImport().isOK()) {
				return Status.OK_STATUS;
			} else {
				return Status.CANCEL_STATUS;
			}
		} catch (Exception ex) {
			return new Status(SWT.ERROR, "ch.elexis", "file not found: " + ex.getMessage());
		}
		
	}
	
	@Override
	public String getDescription(){
		return "Leistungsblöcke importieren";
	}
	
	@Override
	public String getTitle(){
		return "Leistungsblöcke";
	}
	
}
