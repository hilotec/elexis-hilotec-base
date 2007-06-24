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
 * Last changes made by $Author: andreas $, $Date: 2006/10/04 12:14:24 $
 */
package ag.ion.bion.officelayer.util;

/**
 * Number format. 
 * 
 * @author Andreas Bröker
 * @version $Revision: 1.1 $
 */
public interface INumberFormat {
  
  //----------------------------------------------------------------------------
  /**
   * Returns format pattern.
   * 
   * @return format pattern
   * 
   * @author Andreas Bröker
   */
  public String getFormatPattern();
  //----------------------------------------------------------------------------
  /**
   * Returns number format for java code.
   * 
   * @return number format for java code
   * 
   * @throws UtilException if no suitable number format is available
   * 
   * @author Andreas Bröker
   */
  public IJavaNumberFormat getJavaNumberFormat() throws UtilException;
  //----------------------------------------------------------------------------
}