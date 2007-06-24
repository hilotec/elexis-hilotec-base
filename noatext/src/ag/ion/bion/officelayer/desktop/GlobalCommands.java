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
 * Last changes made by $Author: markus $, $Date: 2007/01/30 13:56:21 $
 */
package ag.ion.bion.officelayer.desktop;

/**
 * URL´s of global commands.
 * 
 * @author Andreas Bröker
 * @author Markus Krüger
 * @version $Revision: 1.2 $
 * @date 14.06.2006
 */
public class GlobalCommands {

  /** Command URL in order to switch the print preview. */
  public static final String PRINT_PREVIEW          = ".uno:PrintPreview";
  /** Command URL in order to save the document as new document. */
  public static final String SAVE                   = ".uno:Save";
  /** Command URL in order to save the document as new document. */
  public static final String SAVE_AS                = ".uno:SaveAs";
  /** Command URL in order to close the application. */
  public static final String QUIT_APPLICATION       = ".uno:Quit";
  /** Command URL in order to close the document. */
  public static final String CLOSE_DOCUMENT         = ".uno:CloseDoc";
  /** Command URL in order to close the window. */
  public static final String CLOSE_WINDOW           = ".uno:CloseWin";
  /** Command URL in order to print the document. */
  public static final String PRINT_DOCUMENT         = ".uno:Print";
  /** Command URL in order to diplay the new menu. */
  public static final String NEW_MENU               = ".uno:AddDirect";
  /** Command URL in order to create a new document. */
  public static final String NEW_DOCUMENT           = ".uno:NewDoc";
  /** Command URL in order to open a document. */
  public static final String OPEN_DOCUMENT          = ".uno:Open";
  /** Command URL in order to edit a document. */
  public static final String EDIT_DOCUMENT          = ".uno:EditDoc";
  /** Command URL in order to direktly export a document. */
  public static final String DIREKT_EXPORT_DOCUMENT = ".uno:ExportDirectToPDF";
  /** Command URL in order to mail a document. */
  public static final String MAIL_DOCUMENT          = ".uno:SendMail";
  /** Command URL in order to open the hyperlink dialog. */
  public static final String OPEN_HYPERLINK_DIALOG  = ".uno:HyperlinkDialog";
  /** Command URL in order to edit a hyperlink. */
  public static final String EDIT_HYPERLINK         = ".uno:EditHyperlink";
  /** Command URL in order to open the draw toolbar. */
  public static final String OPEN_DRAW_TOOLBAR      = ".uno:InsertDraw";
  /** Command URL in order to open the navigator. */
  public static final String OPEN_NAVIGATOR         = ".uno:Navigator";
  /** Command URL in order to open the gallery. */
  public static final String OPEN_GALLERY           = ".uno:Gallery";
  /** Command URL in order to open the datasources. */
  public static final String OPEN_DATASOURCES       = ".uno:ViewDataSourceBrowser";
  /** Command URL in order to open the style sheet. */
  public static final String OPEN_STYLE_SHEET       = ".uno:DesignerDialog";
  /** Command URL in order to open the help. */
  public static final String OPEN_HELP              = ".uno:HelpIndex";

  //----------------------------------------------------------------------------
  /**
   * Prevents instantiation.
   * 
   * @author Andreas Bröker
   * @date 14.06.2006 
   */
  private GlobalCommands() {
    //Prevents instantiation
  }
  //----------------------------------------------------------------------------

}