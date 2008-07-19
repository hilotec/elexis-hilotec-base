package ch.elexis.exchange;

import java.util.Properties;


public abstract class XChangeContainer implements IDataSender, IDataReceiver{
	
	/**
	 * Set any implementation-spezific configuration
	 * @param props
	 */
	public void setConfiguration(Properties props){
		this.props=props;
	}
	
	public void setProperty(String name, String value){
		if(props==null){
			props=new Properties();
		}
		props.setProperty(name, value);
	}
	
	protected Properties getProperties(){
		return props;
	}
	protected Properties props;
}
