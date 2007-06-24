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
 * Last changes made by $Author: andreas $, $Date: 2006/10/04 12:14:22 $
 */
package ag.ion.bion.officelayer.text;

import com.sun.star.text.XTextDocument;

import ag.ion.bion.officelayer.document.IDocument;

import ag.ion.bion.officelayer.util.INumberFormatService;
import ag.ion.noa.document.ISearchableDocument;
import ag.ion.noa.text.IDocumentIndexService;

/**
 * OpenOffice.org text document.
 * 
 * @author Andreas Bröker
 * @author Markus Krüger
 * @version $Revision: 1.1 $
 */
public interface ITextDocument extends IDocument, ISearchableDocument {
  
  //----------------------------------------------------------------------------
  /**
   * Returns OpenOffice.org XTextDocument interface. This method
   * is not intended to be called by clients.
   * 
   * @return OpenOffice.org XTextDocument interface
   * 
   * @author Andreas Bröker 
   */
  public XTextDocument getXTextDocument();  
  //----------------------------------------------------------------------------
  /**
   * Returns page service of the document.
   * 
   * @return page service of the document
   * 
   * @author Andreas Bröker
   */
  public IPageService getPageService();
  //----------------------------------------------------------------------------
  /**
   * Returns service for text fields.
   * 
   * @return service for text fields
   * 
   * @author Andreas Bröker
   */
  public ITextFieldService getTextFieldService();
  //----------------------------------------------------------------------------
  /**
   * Returns text service.
   * 
   * @return text service
   * 
   * @author Andreas Bröker
   */
  public ITextService getTextService();
  //----------------------------------------------------------------------------
  /**
   * Returns view cursor service.
   * 
   * @return view cursor service
   * 
   * @author Andreas Bröker
   */
  public IViewCursorService getViewCursorService(); 
  //----------------------------------------------------------------------------
  /**
   * Returns text table service.
   * 
   * @return text table service
   * 
   * @author Andreas Bröker
   */
  public ITextTableService getTextTableService();
  //----------------------------------------------------------------------------
  /**
   * Returns number format service.
   * 
   * @return number format service
   * 
   * @author Andreas Bröker
   */
  public INumberFormatService getNumberFormatService();
  //----------------------------------------------------------------------------
  /**
   * Returns index service of the document.
   * 
   * @return index service of the document
   * 
   * @author Andreas Bröker
   * @date 17.08.2006
   */
  public IDocumentIndexService getIndexService();
  //----------------------------------------------------------------------------
  
}