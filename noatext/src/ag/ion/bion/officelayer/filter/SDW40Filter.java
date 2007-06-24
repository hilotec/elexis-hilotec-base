/****************************************************************************
 * ubion.ORS - The Open Report Suite                                        *
 *                                                                          *
 * ------------------------------------------------------------------------ *
 *                                                                          *
 * Subproject: NOA (Nice Office Access)                                     *
 *                                                                          *
 *                                                                          *
 * The Contents of this file are made available subject to                  *
 * the terms of GNU Lesser General Public License Version 2.1.              *
 *                                                                          * 
 * GNU Lesser General Public License Version 2.1                            *
 * ======================================================================== *
 * Copyright 2003-2005 by IOn AG                                            *
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
 *  http://www.ion.ag                                                       *
 *  info@ion.ag                                                             *
 *                                                                          *
 ****************************************************************************/
 
/*
 * Last changes made by $Author: andreas $, $Date: 2006/10/04 12:14:21 $
 */
package ag.ion.bion.officelayer.filter;

import ag.ion.bion.officelayer.document.IDocument;

import ag.ion.noa.filter.AbstractFilter;

/**
 * Contains information in order to export an OpenOffice.org document 
 * to StarWriter 4.0.
 * 
 * @author Markus Krüger
 * @author Andreas Bröker
 * @version $Revision: 1.1 $
 * 
 * @deprecated Use StarOffice40Filter instead.
 */
public class SDW40Filter extends AbstractFilter implements IFilter {
	
	/** Global filter for StarWriter 4.0.*/
	public static final IFilter FILTER = new SDW40Filter();
	
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
      return "StarWriter 4.0";
    }
    if(document.getDocumentType().equals(IDocument.GLOBAL)) {
      return "StarWriter 4.0 (StarWriter/Global Document)";
    }
    if(document.getDocumentType().equals(IDocument.WEB)) {
      return "StarWriter 4.0 (StarWriter/Web)";
    }
    if(document.getDocumentType().equals(IDocument.CALC)) {
      return "StarCalc 4.0";
    }
    if(document.getDocumentType().equals(IDocument.IMPRESS)) {
      return "StarImpress 4.0";
    }
    if(document.getDocumentType().equals(IDocument.MATH)) {
      return "StarMath 4.0";
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
      return "sdw";
    }
    if(document.getDocumentType().equals(IDocument.GLOBAL)) {
      return "sgl";
    }
    if(document.getDocumentType().equals(IDocument.WEB)) {
      return "html";
    }
    if(document.getDocumentType().equals(IDocument.CALC)) {
      return "sdc";
    }
    if(document.getDocumentType().equals(IDocument.IMPRESS)) {
      return "sdd";
    }
    if(document.getDocumentType().equals(IDocument.MATH)) {
      return "smf";
    }
    else {
      return null;
    }
	}
  //----------------------------------------------------------------------------
	
}