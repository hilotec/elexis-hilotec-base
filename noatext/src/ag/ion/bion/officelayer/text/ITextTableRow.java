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

import ag.ion.bion.officelayer.clone.ICloneServiceProvider;

/**
 * Row of a text table.
 * 
 * @author Andreas Br�ker
 * @author Markus Kr�ger
 * @version $Revision: 1.1 $
 */
public interface ITextTableRow extends ICloneServiceProvider {
  
  //----------------------------------------------------------------------------
  /**
   * Returns cells of the text table row.
   * 
   * @return cells of the text table row
   * 
   * @author Andreas Br�ker
   */
  public ITextTableCell[] getCells();
  //----------------------------------------------------------------------------
  /**
   * Returns the text table cell range.
   * 
   * @return text table cell range
   * 
   * @author Miriam Sutter
   */
  public ITextTableCellRange getCellRange();
  //----------------------------------------------------------------------------
  /**
   * Returns the row height.
   * 
   * @return the row height
   * 
   * @author Markus Kr�ger
   */
  public int getHeight();
  //----------------------------------------------------------------------------
  /**
   * Sets the row height.
   * 
   * @param height the row height to be set
   * 
   * @author Markus Kr�ger
   */
  public void setHeight(int height);
  //----------------------------------------------------------------------------
  /**
   * Returns if the row height is set to automatically be adjusted or not.
   * 
   * @return if the row height is set to automatically be adjusted or not
   * 
   * @author Markus Kr�ger
   */
  public boolean getAutoHeight();
  //----------------------------------------------------------------------------
  /**
   * Sets if the row height is set to automatically be adjusted or not.
   * 
   * @param autoHeight if the row height is set to automatically be adjusted or not
   * 
   * @author Markus Kr�ger
   */
  public void setAutoHeight(boolean autoHeight);
  //----------------------------------------------------------------------------
}