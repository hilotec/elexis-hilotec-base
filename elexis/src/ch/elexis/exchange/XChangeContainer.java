package ch.elexis.exchange;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import org.jdom.Element;
import org.jdom.Namespace;


public abstract class XChangeContainer implements IDataSender, IDataReceiver{
	protected Element eRoot;
	protected HashMap<String,byte[]> binFiles=new HashMap<String,byte[]>();
	public XIDHandler xidHandler=new XIDHandler();
	
	public abstract Namespace getNamespace();
	
	/**
	 * Add a binary content to the Container
	 * @param id a unique identifier for the content
	 * @param contents the content
	 */
	public void addBinary(String id, byte[] contents){
		binFiles.put(id, contents);
	}
	
	/**
	 * get a binary content from the Container
	 * @param id id of the content
	 * @return the content or null if no such content exists
	 */
	public byte[] getBinary(String id){
		return binFiles.get(id);
	}
	
	/**
	 * Create an implementation specific JDOM Element for in this container
	 * @param name name of the element. 
	 * @return the newly created Element with the given name and the apropriate namespace
	 */
	public Element createElement(String name){
		return new Element(name,getNamespace());
	}

	
	/**
	 * Get the root element.
	 * @return the root element
	 */
	public Element getRoot(){
		return eRoot;
	}
	
	/**
	 * Retrieve a List of all Elements with a given Name at a given path
	 * @param path a string of the form /element1/element2/name will get all Elements with "name" in the body of
	 * element2. If name is *, will retrieve all Children of element2. Path must begin at root level but without 
	 * naming the root element.
	 * @return a possibly empty list af all matching elements at the given position
	 */
	@SuppressWarnings("unchecked")
	public List<Element> getElements(String path){
		LinkedList<Element> ret=new LinkedList<Element>();
		String[] trace=path.split("/");
		Element runner=eRoot;
		for(int i=0;i<trace.length-1;i++){
			runner=runner.getChild(trace[i], getNamespace());
			if(runner==null){
				return ret;
			}
		}
		String name=trace[trace.length-1];
		if(trace.equals("*")){
			return runner.getChildren();
		}
		return runner.getChildren(name, getNamespace());
	}
	
	/**
	 * get an Iterator over all binary contents of this Container
	 */
	public Iterator<Entry<String, byte[]>> getBinaries(){
		return binFiles.entrySet().iterator();
	}
	
	
	/**
	 * Set any implementation-spezific configuration
	 * @param props
	 */
	public void setConfiguration(Properties props){
		this.props=props;
	}
	
	/**
	 * Set a named property for this container
	 * @param name the name of the property
	 * @param value the value for the property
	 */
	public void setProperty(String name, String value){
		if(props==null){
			props=new Properties();
		}
		props.setProperty(name, value);
	}
	
	public String getProperty(String name){
		if(props==null){
			props=new Properties();
		}
		return props.getProperty(name);
	}
	protected Properties getProperties(){
		return props;
	}
	protected Properties props;
}
