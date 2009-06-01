package ch.rgw.tools;

import java.util.HashMap;
import java.util.Map;

/**
 * A generic variable that can be transported between medelexis client and server. It is a 
 * simplified Result Object 
 * A Var is a HashMap<String,Object> and therefore holds an indefinite number of key-value-pairs
 * where the keys are always Strings and the Values can be any java objects.
 * There are a number of getter Methods for different value types that handle necessary casting
 * @author gerry
 *
 */
public class Var extends HashMap<String,Object> implements Map<String, Object> {
	private static final long serialVersionUID = -6595896662589053488L;
	/** A key to store the return value of a call */
	public static final String KEY_RESULT="return";
	/** A key to store error conditions */
	public static final String KEY_ERROR="error";
	
	public Var(){}
	
	public Var(String...parms){
		for(int i=0;i<parms.length;i+=2){
			put(parms[i],parms[i+1]);
		}
	}
	public Var(Map<String,Object> src){
		super(src);
	}
	
	public String getString(String key){
		return (String)get(key);
	}
	public boolean isOK(){
		return get(KEY_ERROR)==null;
	}
}
