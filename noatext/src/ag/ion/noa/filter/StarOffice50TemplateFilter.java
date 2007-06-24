/****************************************************************************
 *                                                                          *
 * NOA (Nice Office Access)                                     						*
 * ------------------------------------------------------------------------ *
 *                                                                          *
 * The Contents of this file are made available subject to                  *
 * the terms of GNU Lesser General Public License Version 2.1.              *
 *                                                                          * 
 * GNU Lesser General Public License Version 2.1                            *
 * ======================================================================== *
 * Copyright 2003-2006 by IOn AG                                            *
 *                                                                          *
 * This library is free software; you can redistribute it and/or            *
 * modify it under the terms of the GNU Lesser General Public               *
 * License version 2.1, as published by the Free Software Foundation.       *
 *                                                                          *
 * This library is distributed in the hope that it will be useful,          *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of           *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 * Lesser General Public License for more details.                          *
 *                                                                          *
 * You should have received a copy of the GNU Lesser General Public         *
 * License along with this library; if not, write to the Free Software      *
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,                    *
 * MA  02111-1307  USA                                                      *
 *                                                                          *
 * Contact us:                                                              *
 *  http://www.ion.ag																												*
 *  http://ubion.ion.ag                                                     *
 *  info@ion.ag                                                             *
 *                                                                          *
 ****************************************************************************/
 
/*
 * Last changes made by $Author: andreas $, $Date: 2006/10/04 12:14:21 $
 */
package ag.ion.noa.filter;

import ag.ion.bion.officelayer.document.IDocument;

import ag.ion.bion.officelayer.filter.IFilter;

/**
 * Filter for the StarWriter 5.0 Template format.
 * 
 * @author Andreas Bröker
 * @version $Revision: 1.1 $
 * @date 09.07.2006
 */ 
public class StarOffice50TemplateFilter extends AbstractFilter implements IFilter {
	
	/** Global filter for StarWriter 5.0 Template.*/
	public static final IFilter FILTER = new StarOffice50TemplateFilter();
	
  //----------------------------------------------------------------------------
	/**
	* Returns definition of the filter.
	* 
	* @param document document to be exported
	* 
	* @return definition of the filter
  * 
  * @author Markus Krüger
  * @author Andreas Bröker
	*/
  public String getFilterDefinition(IDocument document) {
    if(document.getDocumentType().equals(IDocument.WRITER)) {
      return "StarWriter 5.0 Vorlage/Template";
    }
    else if(document.getDocumentType().equals(IDocument.WEB)) {
      return "StarWriter/Web 5.0 Vorlage/Template";
    }
    else if(document.getDocumentType().equals(IDocument.CALC)) {
      return "StarCalc 5.0 Vorlage/Template";
    }
    else if(document.getDocumentType().equals(IDocument.IMPRESS)) {
      return "StarImpress 5.0 Vorlage";
    }
    else if(document.getDocumentType().equals(IDocument.DRAW)) {
      return "StarDraw 5.0 Vorlage";
    }
    else {
      return null;
    }
  }
	//----------------------------------------------------------------------------
	/**
	 * Returns file extension of the filter. Returns null
	 * if the document is not supported by the filter.
	 * 
	 * @param document document to be used
	 * 
	 * @return file extension of the filter
	 * 
	 * @author Andreas Bröker
	 * @date 08.07.2006
	 */
	public String getFileExtension(IDocument document) {
    if(document.getDocumentType().equals(IDocument.WRITER)) {
      return "vor";
    }
    else if(document.getDocumentType().equals(IDocument.WEB)) {
      return "vor";
    }
    else if(document.getDocumentType().equals(IDocument.CALC)) {
      return "vor";
    }
    else if(document.getDocumentType().equals(IDocument.IMPRESS)) {
      return "vor";
    }
    else if(document.getDocumentType().equals(IDocument.DRAW)) {
      return "vor";
    }
    else {
      return null;
    }
	}
	//----------------------------------------------------------------------------
	/**
	 * Returns name of the filter. Returns null
	 * if the submitted document is not supported by the filter.
	 * 
	 * @param document document to be used
	 * 
	 * @return name of the filter
	 * 
	 * @author Andreas Bröker
	 * @date 14.07.2006
	 */
	public String getName(IDocument document) {
		if(document.getDocumentType().equals(IDocument.WRITER)) {
      return "StarWriter 5.0 Template";
    }
		else if(document.getDocumentType().equals(IDocument.WEB)) {
      return "StarWriter 5.0 Template (StarWriter/Web)";
    }
		else if(document.getDocumentType().equals(IDocument.CALC)) {
      return "StarCalc 5.0 Template";
    }
		else if(document.getDocumentType().equals(IDocument.IMPRESS)) {
      return "StarImpress 5.0 Template";
    }
		else if(document.getDocumentType().equals(IDocument.DRAW)) {
      return "StarDraw 5.0 Template";
    }
    else {
      return null;
    }
	}
	//----------------------------------------------------------------------------
	
}