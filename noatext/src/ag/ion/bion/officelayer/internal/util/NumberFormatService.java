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
package ag.ion.bion.officelayer.internal.util;

import ag.ion.bion.officelayer.util.INumberFormat;
import ag.ion.bion.officelayer.util.INumberFormatService;
import ag.ion.bion.officelayer.util.UtilException;

import com.sun.star.beans.XPropertySet;

import com.sun.star.util.XNumberFormatsSupplier;

/**
 * Number format service.
 * 
 * @author Andreas Bröker
 * @version $Revision: 1.1 $
 */
public class NumberFormatService implements INumberFormatService {

  private XNumberFormatsSupplier xNumberFormatsSupplier = null;
  
  //----------------------------------------------------------------------------
  /**
   * Constructs new NumberFormatService.
   * 
   * @param xNumberFormatsSupplier OpenOffice.org XNumberFormatsSupplier interface
   * 
   * @throws IllegalArgumentException if the OpenOffice.org XNumberFormatsSupplier interface is not valid
   * 
   * @author Andreas Bröker
   */
  public NumberFormatService(XNumberFormatsSupplier xNumberFormatsSupplier) throws IllegalArgumentException {
    if(xNumberFormatsSupplier == null)
      throw new IllegalArgumentException("Submitted OpenOffice.org XNumberFormatsSupplier interface is not valid.");
    this.xNumberFormatsSupplier = xNumberFormatsSupplier;
  }
  //----------------------------------------------------------------------------
  /**
   * Returns number format on the basis of the submitted key.
   * 
   * @param key key of the number format
   * 
   * @return number format on the basis of the submitted key
   * 
   * @throws UtilException if the number format is not available
   * 
   * @author Andreas Bröker
   */
  public INumberFormat getNumberFormat(int key) throws UtilException {
    try {
      XPropertySet xPropertySet = xNumberFormatsSupplier.getNumberFormats().getByKey(key);
      return new NumberFormat(xPropertySet);
    }
    catch(Exception exception) {
      UtilException utilException = new UtilException(exception.getMessage());
      utilException.initCause(exception);
      throw utilException;
    }
  }
  //----------------------------------------------------------------------------
}