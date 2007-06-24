/****************************************************************************
 *                                                                          *
 * NOA (Nice Office Access)                                                 *
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
 *  http://www.ion.ag                                                       *
 *  http://ubion.ion.ag                                                     *
 *  info@ion.ag                                                             *
 *                                                                          *
 ****************************************************************************/
 
/*
 * Last changes made by $Author: andreas $, $Date: 2006/10/04 12:14:26 $
 */
package ag.ion.noa.document;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Adapter for document URLs. This adapter can be used
 * in order to build valid document URLs for loading
 * and storing.
 * 
 * @author Andreas Bröker
 * @version $Revision: 1.1 $
 * @date 25.08.2006
 */ 
public class URLAdapter {
  
  //----------------------------------------------------------------------------
  /**
   * Adapts the submitted URL to a valid OpenOffice.org URL. 
   * 
   * @param url URL to be adapted
   * 
   * @return adapted URL for OpenOffice.org
   * 
   * @throws MalformedURLException if the URL can not be adapted
   * 
   * @author Andreas Bröker
   * @date 25.08.2006
   */
  public static String adaptURL(String url) throws MalformedURLException {
    if(url == null)
      return null;
    
    url = url.replace('\\', '/' );
    try {
      URL urlObject = new URL(url);
      url = urlObject.toExternalForm();      
    }
    catch(Throwable throwable) {
      URL urlObject = new URL("file:///" + url);
      url = urlObject.toExternalForm();
    }
    url = url.replaceAll(" ", "%20");
    url = url.replaceFirst("file:/", "file:///");
    return url;    
  }
  //----------------------------------------------------------------------------
  
}