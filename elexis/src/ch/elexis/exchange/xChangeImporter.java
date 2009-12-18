package ch.elexis.exchange;

import org.jdom.Element;

import ch.rgw.tools.Result;

public class xChangeImporter implements IDataReceiver {
	private XChangeContainer container;
	
	public Result finalizeImport(){
		// TODO Auto-generated method stub
		return null;
	}
	
	public Result<Object> load(Element input, Object context){
		// TODO Auto-generated method stub
		return null;
	}
	
	public XChangeContainer getContainer(){
		return container;
	}
}
