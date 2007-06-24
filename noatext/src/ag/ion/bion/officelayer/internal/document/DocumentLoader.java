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
package ag.ion.bion.officelayer.internal.document;

import ag.ion.bion.officelayer.document.IDocument;

import ag.ion.bion.officelayer.internal.draw.DrawingDocument;

import ag.ion.bion.officelayer.internal.formula.FormulaDocument;

import ag.ion.bion.officelayer.internal.presentation.PresentationDocument;

import ag.ion.bion.officelayer.internal.spreadsheet.SpreadsheetDocument;

import ag.ion.bion.officelayer.internal.text.GlobalTextDocument;
import ag.ion.bion.officelayer.internal.text.TextDocument;

import ag.ion.bion.officelayer.internal.web.WebDocument;
import ag.ion.noa.internal.db.DatabaseDocument;

import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;

import com.sun.star.presentation.XPresentationSupplier;

import com.sun.star.sdb.XOfficeDatabaseDocument;
import com.sun.star.sheet.XSpreadsheetDocument;

import com.sun.star.text.XTextDocument;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;

import com.sun.star.drawing.XDrawPagesSupplier;

import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XFrame;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;

import com.sun.star.io.XInputStream;

import java.io.IOException;

/**
 * Document loading helper class. 
 * 
 * @author Andreas Br�ker
 * @version $Revision: 1.1 $
 */
public class DocumentLoader {
  
  //----------------------------------------------------------------------------
  /**
   * Loads document from submitted URL.
   * 
   * @param xMultiComponentFactory OpenOffice.org component factory
   * @param xComponentContext context of OpenOffice.org instance
   * @param URL URL of the document
   * 
   * @return loaded document
   * 
   * @throws Exception if an OpenOffice.org communication error occurs
   * @throws IOException if document can not be found
   */
  public static IDocument loadDocument(XMultiComponentFactory xMultiComponentFactory, XComponentContext xComponentContext, String URL) 
  throws Exception, IOException {
    return loadDocument(xMultiComponentFactory, xComponentContext, URL, null);
  }  
  //----------------------------------------------------------------------------
  /**
   * Loads document from submitted URL.
   * 
   * @param xMultiComponentFactory OpenOffice.org component factory
   * @param xComponentContext context of OpenOffice.org instance
   * @param URL URL of the document
   * @param properties properties for OpenOffice.org
   * 
   * @return loaded document
   * 
   * @throws Exception if an OpenOffice.org communication error occurs
   * @throws IOException if document can not be found
   */
  public static IDocument loadDocument(XMultiComponentFactory xMultiComponentFactory, XComponentContext xComponentContext, String URL, PropertyValue[] properties) 
  throws Exception, IOException {
    if(properties == null) {
      properties = new PropertyValue[0];
    }
    Object oDesktop = xMultiComponentFactory.createInstanceWithContext("com.sun.star.frame.Desktop", xComponentContext);
    XComponentLoader xComponentLoader = (XComponentLoader)UnoRuntime.queryInterface(XComponentLoader.class, oDesktop);
    return loadDocument(xComponentLoader, URL, "_blank", 0, properties);    
  }
  //----------------------------------------------------------------------------
  /**
   * Loads document on the basis of the submitted XInputStream implementation.
   * 
   * @param xMultiComponentFactory OpenOffice.org component factory
   * @param xComponentContext context of OpenOffice.org instance
   * @param xInputStream OpenOffice.org XInputStream inplementation
   * 
   * @return loaded Document
   * 
   * @throws Exception if an OpenOffice.org communication error occurs
   * @throws IOException if document can not be found
   */
  public static IDocument loadDocument(XMultiComponentFactory xMultiComponentFactory, XComponentContext xComponentContext, XInputStream xInputStream) 
  throws Exception, IOException {
    return loadDocument(xMultiComponentFactory, xComponentContext, xInputStream, null);
  }
  //----------------------------------------------------------------------------
  /**
   * Loads document on the basis of the submitted XInputStream implementation.
   * 
   * @param xMultiComponentFactory OpenOffice.org component factory
   * @param xComponentContext context of OpenOffice.org instance
   * @param xInputStream OpenOffice.org XInputStream inplementation
   * @param properties properties for OpenOffice.org
   * 
   * @return loaded Document
   * 
   * @throws Exception if an OpenOffice.org communication error occurs
   * @throws IOException if document can not be found
   */
  public static IDocument loadDocument(XMultiComponentFactory xMultiComponentFactory, XComponentContext xComponentContext, XInputStream xInputStream, PropertyValue[] properties) 
  throws Exception, IOException {
    if(properties == null) {
      properties = new PropertyValue[0];
    }    
    PropertyValue[] newProperties = new PropertyValue[properties.length + 1];
    for(int i=0; i<properties.length; i++) {
      newProperties[i] = properties[i];
    }
    newProperties[properties.length] = new PropertyValue(); 
    newProperties[properties.length].Name = "InputStream"; 
    newProperties[properties.length].Value = xInputStream;
    
    Object oDesktop = xMultiComponentFactory.createInstanceWithContext("com.sun.star.frame.Desktop", xComponentContext);
    XComponentLoader xComponentLoader = (XComponentLoader)UnoRuntime.queryInterface(XComponentLoader.class, oDesktop);
    return loadDocument(xComponentLoader, "private:stream", "_blank", 0, newProperties);    
  }
  //----------------------------------------------------------------------------
  /**
   * Loads document from the submitted URL into the OpenOffice.org frame.
   * 
   * @param xFrame frame to used for document
   * @param URL URL of the document
   * @param searchFlags search flags for the target frame
   * @param properties properties for OpenOffice.org
   * 
   * @return loaded document
   * 
   * @throws Exception if an OpenOffice.org communication error occurs
   * @throws IOException if document can not be found
   */
  public static IDocument loadDocument(XFrame xFrame, String URL, int searchFlags, PropertyValue[] properties) 
    throws Exception, IOException {
    if(xFrame != null) {
      if(properties == null) {
        properties = new PropertyValue[0];
      }
      XComponentLoader xComponentLoader = (XComponentLoader)UnoRuntime.queryInterface(XComponentLoader.class, xFrame);
      return loadDocument(xComponentLoader, URL, xFrame.getName(), searchFlags, properties);
    }
    return null;
  }  
  //----------------------------------------------------------------------------
  /**
   * Loads document into OpenOffice.org
   * 
   * @param xComponentLoader OpenOffice.org component loader
   * @param URL URL of the document
   * @param targetFrameName name of the OpenOffice.org target frame
   * @param searchFlags search flags for the target frame
   * @param properties properties for OpenOffice.org
   * 
   * @return loaded document
   * 
   * @throws Exception if an OpenOffice.org communication error occurs
   * @throws IOException if document can not be found
   */
  private static IDocument loadDocument(XComponentLoader xComponentLoader, String URL, String targetFrameName, int searchFlags, PropertyValue[] properties) 
    throws Exception, IOException {    
    XComponent xComponent = xComponentLoader.loadComponentFromURL(URL, targetFrameName, searchFlags, properties);
    if(xComponent != null) {      
      return getDocument(xComponent);      
    }
    else {
      throw new IOException("Document not found.");
    }    
  }  
  //----------------------------------------------------------------------------
  /**
   * Returns document on the basis of the submitted OpenOffice.org XComponent. Returns
   * null if the document can not be builded.
   * 
   * @param xComponent OpenOffice.org XComponent or null if the document can not be 
   * builded
   * 
   * @return constructed document or null
   */
  public static IDocument getDocument(XComponent xComponent) {
    XServiceInfo xServiceInfo = (XServiceInfo)UnoRuntime.queryInterface(XServiceInfo.class, xComponent);
    if(xServiceInfo.supportsService("com.sun.star.text.TextDocument")) {
      XTextDocument xTextDocument = (XTextDocument)UnoRuntime.queryInterface(XTextDocument.class, xComponent);
      if(xTextDocument != null) 
        return new TextDocument(xTextDocument); 
      return null;
    }
    else if(xServiceInfo.supportsService("com.sun.star.sheet.SpreadsheetDocument")) {
      XSpreadsheetDocument xSpreadsheetDocument = (XSpreadsheetDocument)UnoRuntime.queryInterface(XSpreadsheetDocument.class, xComponent);
      if(xSpreadsheetDocument != null)
        return new SpreadsheetDocument(xSpreadsheetDocument);
      else
        return null;
    }
    else if(xServiceInfo.supportsService("com.sun.star.presentation.PresentationDocument")) {
      XPresentationSupplier presentationSupplier = (XPresentationSupplier)UnoRuntime.queryInterface(XPresentationSupplier.class, xComponent);
      if(presentationSupplier != null)
        return new PresentationDocument(presentationSupplier);
      else
        return null;
    }
    else if(xServiceInfo.supportsService("com.sun.star.drawing.DrawingDocument")) {
      XDrawPagesSupplier xDrawPagesSupplier = (XDrawPagesSupplier)UnoRuntime.queryInterface(XDrawPagesSupplier.class, xComponent);
      if(xDrawPagesSupplier != null)
        return new DrawingDocument(xDrawPagesSupplier);
      else
        return null;
    }    
    else if(xServiceInfo.supportsService("com.sun.star.formula.FormulaProperties")) {
        XPropertySet xPropertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, xComponent);
        if(xPropertySet != null)
          return new FormulaDocument(xPropertySet);
        else
          return null;
    }
    else if(xServiceInfo.supportsService("com.sun.star.text.WebDocument")) {
    	XTextDocument xTextDocument = (XTextDocument)UnoRuntime.queryInterface(XTextDocument.class, xComponent);
    	if(xTextDocument != null)
        return new WebDocument(xTextDocument);
      else
        return null;
    }
    else if(xServiceInfo.supportsService("com.sun.star.text.GlobalDocument")) {
    	XTextDocument xTextDocument = (XTextDocument)UnoRuntime.queryInterface(XTextDocument.class, xComponent);
    	if(xTextDocument != null)
        return new GlobalTextDocument(xTextDocument);
      else
        return null;
    }
    else if(xServiceInfo.supportsService("com.sun.star.sdb.OfficeDatabaseDocument")) {
    	XOfficeDatabaseDocument xOfficeDatabaseDocument = (XOfficeDatabaseDocument)UnoRuntime.queryInterface(XOfficeDatabaseDocument.class, xComponent);
    	if(xOfficeDatabaseDocument != null)
        return new DatabaseDocument(xOfficeDatabaseDocument);
      else
        return null;
    }
    else {
      return null;
    }  
  }
  //----------------------------------------------------------------------------

}