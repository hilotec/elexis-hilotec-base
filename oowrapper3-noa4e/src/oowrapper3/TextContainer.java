package oowrapper3;

import java.io.File;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.noa4e.ui.widgets.OfficePanel;
import ch.elexis.Desk;
import ch.elexis.text.ITextPlugin;
import ch.elexis.text.ReplaceCallback;
import ch.elexis.util.PlatformHelper;
import ch.elexis.util.SWTHelper;

public class TextContainer implements ITextPlugin {
	
	OfficePanel panel;
	
	public TextContainer(){
		System.out.println("constructor");
	}
	

	public Composite createContainer(Composite parent, ICallback handler){
		panel = new OfficePanel(parent, SWT.NONE);
		parent.setLayout(new GridLayout());
		panel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		panel.setBuildAlwaysNewFrames(true);
		return panel;
	}
	

	public boolean clear(){
		// TODO Auto-generated method stub
		return false;
	}
	

	public boolean createEmptyDocument(){
		String myBase = PlatformHelper.getBasePath("ch.elexis.oowrapper3");
		final String empty = myBase + File.separator + "rsc" + File.separator + "empty.odt";
		Desk.asyncExec(new Runnable() {
			public void run(){
				panel.loadDocument(true, empty, DocumentDescriptor.DEFAULT);
				
			}
		});
		
		return true;
	}
	

	public void dispose(){
	// TODO Auto-generated method stub
	
	}
	

	public boolean findOrReplace(String pattern, ReplaceCallback cb){
		// TODO Auto-generated method stub
		return false;
	}
	

	public PageFormat getFormat(){
		// TODO Auto-generated method stub
		return null;
	}
	

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
		throws CoreException{
		// TODO Auto-generated method stub
		System.out.println("textplugin");
	}


	public String getMimeType(){
		// TODO Auto-generated method stub
		return null;
	}


	public boolean insertTable(String place, int properties, String[][] contents, int[] columnSizes){
		// TODO Auto-generated method stub
		return false;
	}


	public Object insertText(String marke, String text, int adjust){
		// TODO Auto-generated method stub
		return null;
	}


	public Object insertText(Object pos, String text, int adjust){
		// TODO Auto-generated method stub
		return null;
	}


	public Object insertTextAt(int x, int y, int w, int h, String text, int adjust){
		// TODO Auto-generated method stub
		return null;
	}


	public boolean loadFromByteArray(byte[] bs, boolean asTemplate){
		// TODO Auto-generated method stub
		return false;
	}


	public boolean loadFromStream(InputStream is, boolean asTemplate){
		// TODO Auto-generated method stub
		return false;
	}


	public boolean print(String toPrinter, String toTray, boolean waitUntilFinished){
		// TODO Auto-generated method stub
		return false;
	}


	public void setFocus(){
		// TODO Auto-generated method stub
		
	}


	public boolean setFont(String name, int style, float size){
		// TODO Auto-generated method stub
		return false;
	}


	public void setFormat(PageFormat f){
		// TODO Auto-generated method stub
		
	}


	public void setSaveOnFocusLost(boolean save){
		// TODO Auto-generated method stub
		
	}


	public void showMenu(boolean b){
		// TODO Auto-generated method stub
		
	}


	public void showToolbar(boolean b){
		// TODO Auto-generated method stub
		
	}


	public byte[] storeToByteArray(){
		// TODO Auto-generated method stub
		return null;
	}
	
}
