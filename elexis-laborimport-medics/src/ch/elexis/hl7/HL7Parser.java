package ch.elexis.hl7;

import java.util.List;
import java.util.Vector;

public abstract class HL7Parser {
	List<String> errorList = new Vector<String>();
	List<String> warnList = new Vector<String>();
	
	protected String receivingApplication = ""; //$NON-NLS-1$
	protected String receivingFacility = ""; //$NON-NLS-1$
	
	public HL7Parser(){
		super();
	}
	
	public HL7Parser(String receivingApplication, String receivingFacility){
		this();
		this.receivingApplication = receivingApplication;
		this.receivingFacility = receivingFacility;
	}
	
	/**
	 * Returns version of HL7
	 * 
	 * @return
	 */
	public abstract String getVersion();
	
	/**
	 * Clears all errors and warnings
	 */
	public void clearMessages(){
		errorList = new Vector<String>();
		warnList = new Vector<String>();
	}
	
	/**
	 * Returns error list
	 * 
	 * @return
	 */
	public List<String> getErrorList(){
		return errorList;
	}
	
	/**
	 * Returns warning list
	 * 
	 * @return
	 */
	public List<String> getWarningList(){
		return warnList;
	}
	
	/**
	 * Adds parsing error
	 * 
	 * @param error
	 */
	protected void addError(String error){
		errorList.add(error);
	}
	
	/**
	 * Adds a warning message
	 * 
	 * @param error
	 */
	protected void addWarning(String warn){
		errorList.add(warn);
	}
}
