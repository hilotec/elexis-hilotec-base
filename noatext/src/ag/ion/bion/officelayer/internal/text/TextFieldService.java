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
 * Last changes made by $Author: markus $, $Date: 2007/01/23 14:06:10 $
 */
package ag.ion.bion.officelayer.internal.text;

import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextField;
import ag.ion.bion.officelayer.text.ITextFieldMaster;
import ag.ion.bion.officelayer.text.ITextFieldService;
import ag.ion.bion.officelayer.text.TextException;

import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XNameAccess;

import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;

import com.sun.star.text.XDependentTextField;
import com.sun.star.text.XTextField;
import com.sun.star.text.XTextFieldsSupplier;

import com.sun.star.uno.Any;
import com.sun.star.uno.UnoRuntime;

import java.util.ArrayList;

/**
 * Textfield service of a text document.
 * 
 * @author Andreas Bröker
 * @version $Revision: 1.2 $
 */
public class TextFieldService implements ITextFieldService {
  
  private static final ITextFieldMaster[] EMPTY_TEXTFIELD_MASTER_ARRAY = new ITextFieldMaster[0];
  
  private static final String USER_TEXTFIELD_PREFIX = "com.sun.star.text.FieldMaster.User.";
  
  private ITextDocument textDocument = null;
  
  //----------------------------------------------------------------------------
  /**
   * Constructs new TextFieldService.
   * 
   * @param textDocument text document to be used
   * 
   * @throws IllegalArgumentException if the OpenOffice.org interface is not valid
   * 
   * @author Andreas Bröker
   */
  public TextFieldService(ITextDocument textDocument) {
    if(textDocument == null)
      throw new IllegalArgumentException("The submitted text document is not valid.");
    this.textDocument = textDocument;
  }
  //----------------------------------------------------------------------------
  /**
   * Returns master of a user textfield with the submitted name. Returns null if
   * a user textfield with the submitted name is not available.
   * 
   * @param name name of the master of the user textfield
   * 
   * @return master of a user textfield with the submitted name or null if a user textfield with
   * the submitted name is not available
   * 
   * @throws TextException if the user text field can not be provided
   * 
   * @author Andreas Bröker
   */
  public ITextFieldMaster getUserTextFieldMaster(String name) throws TextException {
    try {
      XTextFieldsSupplier xTextFieldsSupplier = (XTextFieldsSupplier)UnoRuntime.queryInterface(XTextFieldsSupplier.class, textDocument.getXTextDocument());
      XNameAccess xNameAccess = xTextFieldsSupplier.getTextFieldMasters();
      Any any = null;
      
      try {
        any = (Any)xNameAccess.getByName(USER_TEXTFIELD_PREFIX + name);
      }
      catch(NoSuchElementException noSuchElementException) {
        return null;
      }
      
      XPropertySet xPropertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, any);
      if(xPropertySet != null)
        return new TextFieldMaster(textDocument, xPropertySet);
      else
        return null;
    }
    catch(Exception exception) {
      throw new TextException(exception);
    }
  }
  //----------------------------------------------------------------------------
  /**
   * Returns masters of the user textfields with the submitted name prefix.
   * 
   * @param prefix name prefix to be used
   * 
   * @return masters of the user textfields with the submitted name prefix
   * 
   * @throws TextException if the masters of the user textfields can not be constructed
   * 
   * @author Andreas Bröker
   */
  public ITextFieldMaster[] getUserTextFieldMasters(String prefix) throws TextException {
    if(prefix == null)
      return EMPTY_TEXTFIELD_MASTER_ARRAY;
    
    try {
      XTextFieldsSupplier xTextFieldsSupplier = (XTextFieldsSupplier)UnoRuntime.queryInterface(XTextFieldsSupplier.class, textDocument.getXTextDocument());
      XNameAccess xNameAccess = xTextFieldsSupplier.getTextFieldMasters();
      String[] names = xNameAccess.getElementNames();
      ArrayList arrayList = new ArrayList();
      Any any = null;
      XPropertySet xPropertySet = null;
      for(int i=0, n=names.length; i<n; i++) {
        String name = names[i];
        if(name.startsWith(USER_TEXTFIELD_PREFIX)) {
          String fieldName = name.substring(USER_TEXTFIELD_PREFIX.length());
          if(fieldName.startsWith(prefix)) {                        
            try {
              any = (Any)xNameAccess.getByName(name);
              xPropertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, any);
              if(xPropertySet != null)
                arrayList.add(new TextFieldMaster(textDocument, xPropertySet));
            }
            catch(NoSuchElementException noSuchElementException) {
              //do nothing
            }
          }
        }
      }  
      return (ITextFieldMaster[])arrayList.toArray(new ITextFieldMaster[arrayList.size()]);      
    }
    catch(Exception exception) {
      throw new TextException(exception);
    }
  }
  //----------------------------------------------------------------------------
  /**
   * Returns masters of the user textfields with the submitted name prefix and suffix.
   * 
   * @param prefix name prefix to be used
   * @param suffix name suffix to be used
   * 
   * @return masters of the user textfields with the submitted name prefix and suffix
   * 
   * @throws TextException if the masters of the user textfields can not be constructed
   * 
   * @author Markus Krüger
   */
  public ITextFieldMaster[] getUserTextFieldMasters(String prefix, String suffix) throws TextException {
    if(prefix == null)
      return EMPTY_TEXTFIELD_MASTER_ARRAY;
    
    try {
      XTextFieldsSupplier xTextFieldsSupplier = (XTextFieldsSupplier)UnoRuntime.queryInterface(XTextFieldsSupplier.class, textDocument.getXTextDocument());
      XNameAccess xNameAccess = xTextFieldsSupplier.getTextFieldMasters();
      String[] names = xNameAccess.getElementNames();
      ArrayList arrayList = new ArrayList();
      Any any = null;
      XPropertySet xPropertySet = null;
      for(int i=0, n=names.length; i<n; i++) {
        String name = names[i];
        if(name.startsWith(USER_TEXTFIELD_PREFIX)) {
          String fieldName = name.substring(USER_TEXTFIELD_PREFIX.length());
          if(fieldName.startsWith(prefix) && fieldName.endsWith(suffix)) {                        
            try {
              any = (Any)xNameAccess.getByName(name);
              xPropertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, any);
              if(xPropertySet != null)
                arrayList.add(new TextFieldMaster(textDocument, xPropertySet));
            }
            catch(NoSuchElementException noSuchElementException) {
              //do nothing
            }
          }
        }
      }  
      return (ITextFieldMaster[])arrayList.toArray(new ITextFieldMaster[arrayList.size()]);      
    }
    catch(Exception exception) {
      throw new TextException(exception);
    }
  }
  //----------------------------------------------------------------------------
  /**
   * Adds new user textfield.
   * 
   * @param name name of the textfield
   * @param content content of the textfield
   * 
   * @return new textfield
   * 
   * @throws TextException if any error occurs during textfield creation
   * 
   * @author Andreas Bröker
   */
  public ITextField addUserTextField(String name, String content) throws TextException {
    try {
      XMultiServiceFactory xMultiServiceFactory = (XMultiServiceFactory)UnoRuntime.queryInterface(XMultiServiceFactory.class, textDocument.getXTextDocument());
      Object textField = xMultiServiceFactory.createInstance("com.sun.star.text.TextField.User");
      XDependentTextField xDependentTextField = (XDependentTextField)UnoRuntime.queryInterface(XDependentTextField.class, textField);
      
      Object oFieldMaster = xMultiServiceFactory.createInstance("com.sun.star.text.FieldMaster.User");
      XPropertySet xPropertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, oFieldMaster);
  
      xPropertySet.setPropertyValue("Name", name);
      xPropertySet.setPropertyValue("Content", content);
  
      xDependentTextField.attachTextFieldMaster(xPropertySet);
      
      return new TextField(textDocument, xDependentTextField);
    }
    catch(Exception exception) {
      throw new TextException(exception);
    }
  }
  //---------------------------------------------------------------------------- 
  /**
   * Returns all available user textfields.
   * 
   * @return all available user textfields
   * 
   * @throws TextException if the user textfields can not be constructed
   * 
   * @author Andreas Bröker
   */
  public ITextField[] getUserTextFields() throws TextException {
    try {
      XTextFieldsSupplier xTextFieldsSupplier = (XTextFieldsSupplier)UnoRuntime.queryInterface(XTextFieldsSupplier.class, textDocument.getXTextDocument());
      XEnumerationAccess xEnumerationAccess = xTextFieldsSupplier.getTextFields();
      XEnumeration xEnumeration = xEnumerationAccess.createEnumeration();
      ArrayList arrayList = new ArrayList();
      while(xEnumeration.hasMoreElements()) {
        Object object = xEnumeration.nextElement();
        XTextField xTextField = (XTextField)UnoRuntime.queryInterface(XTextField.class, object);
        arrayList.add(new TextField(textDocument, xTextField));
      }
      return (ITextField[])arrayList.toArray(new ITextField[arrayList.size()]);
    }
    catch(Exception exception) {
      throw new TextException(exception);
    }
  }  
  //----------------------------------------------------------------------------   
  /**
   * Returns all available placeholder textfields.
   * 
   * @return all available placeholder textfields
   * 
   * @throws TextException if the placeholder textfields can not be constructed
   * 
   * @author Markus Krüger
   * @date 23.01.2007
   */
  public ITextField[] getPlaceholderFields() throws TextException {
    try {
      XTextFieldsSupplier xTextFieldsSupplier = (XTextFieldsSupplier)UnoRuntime.queryInterface(XTextFieldsSupplier.class, textDocument.getXTextDocument());
      XEnumerationAccess xEnumerationAccess = xTextFieldsSupplier.getTextFields();
      XEnumeration xEnumeration = xEnumerationAccess.createEnumeration();
      ArrayList arrayList = new ArrayList();
      while(xEnumeration.hasMoreElements()) {
        Object object = xEnumeration.nextElement();
        XTextField xTextField = (XTextField)UnoRuntime.queryInterface(XTextField.class, object);
        XServiceInfo xInfo = (XServiceInfo)UnoRuntime.queryInterface(XServiceInfo.class, xTextField); 
        if(xInfo.supportsService("com.sun.star.text.TextField.JumpEdit")){
          arrayList.add(new TextField(textDocument, xTextField));
        }
      }
      return (ITextField[])arrayList.toArray(new ITextField[arrayList.size()]);
    }
    catch(Exception exception) {
      throw new TextException(exception);
    }
  }
  //----------------------------------------------------------------------------  
  
}