package ch.elexis.exchange;

import java.util.List;

/**
 * Description of a capability to handle a certain kind of xChange data. The capability is declared by
 * (1) declaring the datatype (Name of the XML-Element as in xchange.xsd)
 * (2) declaring conditions to apply on this element
 * (3) declaring a value
 * 
 * If an xChange Element is to be imported, the xChange framework first searches a handler that can
 * handle the given type. If more than one such handler is found, the conditions are checked, and the
 * one meeting the most conditions is chosen. If more than one handler meets the same number of conditions,
 * then the one with the higher value is chosen. If more than one has the same value, then one is chosen
 * randomly.
 * @author Gerry
 *
 */
public interface IExchangeDataHandler {

	/**
	 * Return the name of the datatype (as in xchange.xsd) that can be handled. If tha name is * then
	 * this is a catch-all handler
	 */
	public String getDatatype();
	
	/**
	 * Return a self-declared "quality" of this ability. If more than one
	 */
	public int getValue();
	
	/**
	 * Return conditions to apply for this rule. 
	 * The syntax of the conditions is as follows:
	 * (name operator value)
	 * name is an xpath expression to apply on the given element
	 * operator is one of  = , < , > , <= , >= , ! , ~ 
	 * Value can contain * or can be a regular expression if operator is ~
	 *  
	 * @return A List of conditions or null if no such restrictions exist
	 */
	public String[] getRestrictions();
		
}
