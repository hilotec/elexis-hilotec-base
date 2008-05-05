package ch.elexis.text;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.program.Program;

import ch.elexis.util.IKonsExtension;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;

public class ExternalLink implements IKonsExtension {
	public static final String ID="ch.elexis.text.ExternalLink";
	//EnhancedTextField mine;
	public String connect(EnhancedTextField tf) {
		//mine=tf;
		return ID;
	}

	public boolean doLayout(StyleRange n, String provider, String id) {
		n.underline=true;
		return true;
	}

	public boolean doXRef(String refProvider, String refID) {
		try{
			int r=refID.lastIndexOf('.');
			String ext="";
			if(r!=-1){
				ext=refID.substring(r+1);
			}
			Program proggie=Program.findProgram(ext);
			if(proggie!=null){
				proggie.execute(refID);
			}else{
				if(Program.launch(refID)==false){
					Runtime.getRuntime().exec(refID);	
				}
			}
		}catch(Exception ex){
			ExHandler.handle(ex);
			SWTHelper.showError("Konnte Datei nicht starten", ex.getMessage());
		}

		return true;
	}

	public IAction[] getActions() {
		return null;
	}

	public void insert(Object o, int pos) {
	}

	public void removeXRef(String refProvider, String refID) {
	}

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
	}

}
