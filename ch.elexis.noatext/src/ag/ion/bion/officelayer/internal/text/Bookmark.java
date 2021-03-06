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
 * Last changes made by $Author: markus $, $Date: 2007-08-03 14:12:53 +0200 (Fr, 03 Aug 2007) $
 */
package ag.ion.bion.officelayer.internal.text;

import ag.ion.bion.officelayer.text.IBookmark;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextRange;
import ag.ion.bion.officelayer.text.IViewCursor;
import ag.ion.bion.officelayer.util.Assert;

import com.sun.star.text.XTextRange;

/**
 * Bookmark of a text document.
 * 
 * @author Markus Krüger
 * @version $Revision: 11549 $
 */
public class Bookmark extends TextRange implements IBookmark, ITextRange {
  
	private ITextDocument textDocument = null;
	
  private String name = null;
  
  //----------------------------------------------------------------------------
  /**
   * Constructs new TextRange.
   * 
   * @param textDocument text document to be used
   * @param xTextRange OpenOffice.org XTextRange interface
   * @param bookmarkName the name of the bookmark to be used
   * 
   * @author Markus Krüger
   */
  public Bookmark(ITextDocument textDocument, XTextRange xTextRange, String bookmarkName) {
    super(textDocument,xTextRange);
    Assert.isNotNull(bookmarkName, String.class, this);
    name = bookmarkName;
  }
  //----------------------------------------------------------------------------
  /**
   * Returns the name of the bookmark.
   * 
   * @return the name of the bookmark
   * 
   * @author Markus Krüger
   * @date 13.07.2006
   */
  public String getName() {
    return name;
  }
  //----------------------------------------------------------------------------
  /**
   * Jumps to the bookmark.
   * 
   * @author Markus Krüger
   */
  public void jumpTo() {
    IViewCursor viewCursor = textDocument.getViewCursorService().getViewCursor();
    viewCursor.goToRange(this,false);
  }
  //----------------------------------------------------------------------------
  
}