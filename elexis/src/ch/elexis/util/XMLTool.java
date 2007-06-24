/*******************************************************************************
 * Copyright (c) 2007, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    
 *  $Id$
 *******************************************************************************/

package ch.elexis.util;

import java.util.List;

import org.jdom.Element;


/**
 * This class provides various helper methods for handling XML data. 
 *
 * @author danlutz
 */

public class XMLTool {
	public static String moneyToXmlDouble(Money money) {
		int cents = money.getCents();
		// we force to use a literal "."
		String xmlDouble = String.format("%d.%02d", cents / 100, cents % 100);
		return xmlDouble;
	}
	
	public static Money xmlDoubleToMoney(String xmlDouble) throws NumberFormatException {
		if (xmlDouble == null) {
			throw new NumberFormatException("xmlDouble must not be null");
		}
		Double d = Double.parseDouble(xmlDouble);
		return new Money(d);
	}
	
	/**
	 * Convert a double value to String conforming to the double datatype
	 * of the XML specification.
	 * @param value the value to be converted
	 * @param factionalDigits the number of digits after the point (currently ignored, i. e. 2)
	 * @return the formated String
	 */
	public static String doubleToXmlDouble(double value, int factionalDigits) {
		long cents = Math.round(value * 100);
		// we force to use a literal "."
		String xmlDouble = String.format("%d.%02d", cents / 100, cents % 100);
		return xmlDouble;
	}
	
	/**
	 * Convert a XML-Table formatted like
	 * &lt;table&gt;
	 * 		&lt;row&gt;
	 * 			&lt;col1&gt;Col 1&lt;col1/&gt;
	 * 			&lt;col2&gt;Col 2&lt;col2/&gt;
	 * 		&lt;/row&gt;
	 * 		&lt;row&gt;
	 * 			...
	 * 		&lt;/row&gt;
	 * &lt;/table&gt;
	 * to a csv table
	 * @param table the table to convert
	 * @param separator String that separates columns
	 * @return a string containing the csv table. Rows separated by \n, colums
	 * separated by separator
	 */
	@SuppressWarnings("unchecked")
	public static String XMLTableToCSVTable(Element table, String separator){
		List<Element> rows=table.getChildren();
		StringBuilder ret=new StringBuilder();
		for(Element row:rows){
			List<Element> cols=row.getChildren();
			for(Element col:cols){
				ret.append(col.getText()).append(separator);
			}
			ret.replace(ret.length()-separator.length(), ret.length(), "\n");
		}
		return ret.toString();
	}
	
	/**
	 * Convert a XML-Table formatted like
	 * &lt;table&gt;
	 * 		&lt;row&gt;
	 * 			&lt;col1&gt;Col 1&lt;col1/&gt;
	 * 			&lt;col2&gt;Col 2&lt;col2/&gt;
	 * 		&lt;/row&gt;
	 * 		&lt;row&gt;
	 * 			...
	 * 		&lt;/row&gt;
	 * &lt;/table&gt;
	 * to a html table
	 * @param table the table to convert
	 * @return a string containing the html table.
	 */
	@SuppressWarnings("unchecked")
	public static String XMLTableToHTMLTable(Element table){
		List<Element> rows=table.getChildren();
		StringBuilder ret=new StringBuilder();
		ret.append("<table>");
		for(Element row:rows){
			ret.append("<tr>");
			List<Element> cols=row.getChildren();
			for(Element col:cols){
				ret.append("<td>").append(col.getText()).append("</td>");
			}
			ret.append("</tr>");
		}
		ret.append("</table>");
		return ret.toString();
	}
}
