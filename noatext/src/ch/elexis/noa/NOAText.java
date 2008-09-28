/*******************************************************************************
 * Copyright (c) 2007-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: NOAText.java 4486 2008-09-28 06:31:41Z rgw_ch $
 *******************************************************************************/
package ch.elexis.noa;

import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.LinkedList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Bundle;

import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.DocumentException;
import ag.ion.bion.officelayer.event.ICloseEvent;
import ag.ion.bion.officelayer.event.ICloseListener;
import ag.ion.bion.officelayer.event.IEvent;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextRange;
import ag.ion.bion.officelayer.text.ITextTable;
import ag.ion.bion.officelayer.text.table.ITextTablePropertyStore;
import ag.ion.bion.workbench.office.editor.core.EditorCorePlugin;
import ag.ion.noa.search.ISearchResult;
import ag.ion.noa.search.SearchDescriptor;
import ag.ion.noa4e.ui.widgets.OfficePanel;
import ch.elexis.Hub;
import ch.elexis.noa.OOPrinter.MyXPrintJobListener;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.text.ITextPlugin;
import ch.elexis.text.ReplaceCallback;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

import com.sun.star.awt.FontWeight;
import com.sun.star.awt.Size;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.style.ParagraphAdjust;
import com.sun.star.text.HoriOrientation;
import com.sun.star.text.RelOrientation;
import com.sun.star.text.TextContentAnchorType;
import com.sun.star.text.VertOrientation;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextFrame;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.view.PrintableState;
import com.sun.star.view.XPrintable;

public class NOAText implements ITextPlugin {
	public static final String MIMETYPE_OO2 = "application/vnd.oasis.opendocument.text";
	public static LinkedList<NOAText> noas = new LinkedList<NOAText>();
	OfficePanel panel;
	ITextDocument doc;
	ICallback textHandler;
	File myFile;
	private final Log log = Log.get("NOAText");
	IOfficeApplication office;
	private String font;
	private float hi;
	private int stil;
	
	public NOAText(){
		System.out.println("noa loaded");
		File base = new File(Hub.getBasePath());
		File fDef = new File(base.getParentFile().getParent() + "/ooo");
		String defaultbase;
		if (fDef.exists()) {
			defaultbase = fDef.getAbsolutePath();
			Hub.localCfg.set(PreferenceConstants.P_OOBASEDIR, defaultbase);
		} else {
			defaultbase = Hub.localCfg.get(PreferenceConstants.P_OOBASEDIR, ".");
		}
		System.setProperty("openoffice.path.name", defaultbase);
	}
	
	/*
	 * We keep track on opened office windows
	 */
	private void createMe(){
		if (office == null) {
			office = EditorCorePlugin.getDefault().getManagedLocalOfficeApplication();
		}
		doc = (ITextDocument) panel.getDocument();
		if (doc != null) {
			doc.addCloseListener(new closeListener(office));
			noas.add(this);
		}
	}
	
	/*
	 * We deactivate the office application as the user closes the last office window
	 */
	private void removeMe(){
		
		try {
			if (textHandler != null) {
				textHandler.save();
				noas.remove(this);
				if (doc != null) {
					doc.setModified(false);
					doc.close();
				}
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
		if (noas.isEmpty()) {
			try {
				office.deactivate();
				log.log("Office deactivated", Log.INFOS);
			} catch (OfficeApplicationException e) {
				ExHandler.handle(e);
				log.log("Office deactivation failed", Log.ERRORS);
			}
		}
	}
	
	public boolean clear(){
		if (textHandler != null) {
			try {
				textHandler.save();
				doc.setModified(false);
				return true;
			} catch (DocumentException e) {
				ExHandler.handle(e);
			}
		}
		return false;
	}
	
	/**
	 * Create the OOo-Container that will appear inside the view or dialog for Text-Display. Here we
	 * use a slightly adapted OfficePanel from NOA4e (www.ubion.org)
	 */
	public Composite createContainer(final Composite parent, final ICallback handler){
		new Frame();
		panel = new OfficePanel(parent, SWT.NONE);
		panel.setBuildAlwaysNewFrames(false);
		office = EditorCorePlugin.getDefault().getManagedLocalOfficeApplication();
		return panel;
	}
	
	/**
	 * Create an empty text document. We simply use an empty template and save it immediately into a
	 * temporary file to avoid OOo's complaints when we close the Container or overwrite its
	 * contents.
	 */
	public boolean createEmptyDocument(){
		try {
			clean();
			Bundle bundle = Platform.getBundle("ch.elexis.noatext");
			Path path = new Path("rsc/empty.odt");
			InputStream is = FileLocator.openStream(bundle, path, false);
			FileOutputStream fos = new FileOutputStream(myFile);
			FileTool.copyStreams(is, fos);
			is.close();
			fos.close();
			panel.loadDocument(false, myFile.getAbsolutePath(), DocumentDescriptor.DEFAULT);
			createMe();
			return true;
			/*
			 * doc=(ITextDocument)office.getDocumentService().constructNewDocument(IDocument.WRITER,
			 * DocumentDescriptor.DEFAULT); if(doc!=null){
			 * doc.getPersistenceService().store(myFile.getAbsolutePath()); doc.close();
			 * panel.loadDocument(false, myFile.getAbsolutePath(), DocumentDescriptor.DEFAULT);
			 * doc=(ITextDocument)panel.getDocument(); return true; }
			 */

		} catch (Exception e) {
			ExHandler.handle(e);
			
		}
		return false;
	}
	
	/**
	 * Load a file from a byte array. Again, wie store it first into a temporary disk file because
	 * OOo does not like documents that have no representation on disk.
	 */
	public boolean loadFromByteArray(final byte[] bs, final boolean asTemplate){
		if (bs == null) {
			log.log("Null-Array zum speichern!", Log.ERRORS);
			return false;
		}
		try {
			clean();
			FileOutputStream fout = new FileOutputStream(myFile);
			fout.write(bs);
			fout.close();
			panel.loadDocument(false, myFile.getAbsolutePath(), DocumentDescriptor.DEFAULT);
			createMe();
			return true;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return false;
		}
	}
	
	/**
	 * Load a file from an input stream. Explanations
	 * 
	 * @see loadFromByteArray()
	 */
	public boolean loadFromStream(final InputStream is, final boolean asTemplate){
		try {
			clean();
			doc =
				(ITextDocument) office.getDocumentService().loadDocument(is,
					DocumentDescriptor.DEFAULT_HIDDEN);
			if (doc != null) {
				doc.getPersistenceService().store(myFile.getAbsolutePath());
				doc.close();
				panel.loadDocument(false, myFile.getAbsolutePath(), DocumentDescriptor.DEFAULT);
				createMe();
			}
		} catch (Exception e) {
			ExHandler.handle(e);
			
		}
		return false;
	}
	
	/**
	 * Store the contents of the OOo-Frame into a byte array. We save it into a temporary disk file
	 * first to ensure OOo, that the file ist really saved. That way OOo will not complain about
	 * corrupted or lost files.
	 */
	public byte[] storeToByteArray(){
		if (doc == null) {
			return null;
		}
		try {
			doc.getPersistenceService().store(myFile.getAbsolutePath());
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
			byte[] ret = new byte[(int) myFile.length()];
			int pos = 0, len = 0;
			while (pos + (len = bis.read(ret)) != ret.length) {
				pos += len;
			}
			return ret;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}
	
	/**
	 * Destroy the Panel with the OOo frame
	 */
	public void dispose(){
		if (doc != null) {
			doc.close();
			doc = null;
		}
		if (panel != null) {
			panel.dispose();
		}
		
	}
	
	public boolean findOrReplace(final String pattern, final ReplaceCallback cb){
		SearchDescriptor search = new SearchDescriptor(pattern);
		search.setUseRegularExpression(true);
		if (doc == null) {
			SWTHelper.showError("No doc in bill", "Fehler:",
				"Es ist keine Rechnungsvorlage definiert");
			return false;
		}
		ISearchResult searchResult = doc.getSearchService().findAll(search);
		if (!searchResult.isEmpty()) {
			ITextRange[] textRanges = searchResult.getTextRanges();
			if (cb != null) {
				for (ITextRange r : textRanges) {
					String orig = r.getXTextRange().getString();
					Object replace = cb.replace(orig);
					if (replace == null) {
						r.setText("??Auswahl??");
					} else if (replace instanceof String) {
						// String repl=((String)replace).replaceAll("\\r\\n[\\r\\n]*", "\n")
						String repl = ((String) replace).replaceAll("\\r", "\n");
						repl = repl.replaceAll("\\n\\n+", "\n");
						r.setText(repl);
					} else if (replace instanceof String[][]) {
						String[][] contents = (String[][]) replace;
						try {
							ITextTable textTable =
								doc.getTextTableService().constructTextTable(contents.length,
									contents[0].length);
							doc.getTextService().getTextContentService().insertTextContent(r,
								textTable);
							r.setText("");
							ITextTablePropertyStore props = textTable.getPropertyStore();
							// long w=props.getWidth();
							// long percent=w/100;
							for (int row = 0; row < contents.length; row++) {
								String[] zeile = contents[row];
								for (int col = 0; col < zeile.length; col++) {
									textTable.getCell(col, row).getTextService().getText().setText(
										zeile[col]);
								}
							}
							textTable.spreadColumnsEvenly();
							
							return true;
						} catch (Exception ex) {
							ExHandler.handle(ex);
							r.setText("Fehler beim Ersetzen");
						}
						
					} else {
						r.setText("Not a String");
					}
				}
			}
			return true;
		}
		return false;
	}
	
	public PageFormat getFormat(){
		return ITextPlugin.PageFormat.USER;
	}
	
	public String getMimeType(){
		return MIMETYPE_OO2;
	}
	
	/**
	 * Insert a table.
	 * 
	 * @param place
	 *            A string to search for and replace with the table
	 * @param properties
	 *            properties for the table
	 * @param contents
	 *            An Array of String[]s describing each line of the table
	 * @param columnsizes
	 *            int-array describing the relative width of each column (all columns together are
	 *            taken as 100%). May be null, in that case the columns will bhe spread evenly
	 */
	public boolean insertTable(final String place, final int properties, final String[][] contents,
		final int[] columnSizes){
		int offset = 0;
		if ((properties & ITextPlugin.FIRST_ROW_IS_HEADER) == 0) {
			offset = 1;
		}
		SearchDescriptor search = new SearchDescriptor(place);
		search.setIsCaseSensitive(true);
		ISearchResult searchResult = doc.getSearchService().findFirst(search);
		if (!searchResult.isEmpty()) {
			ITextRange r = searchResult.getTextRanges()[0];
			
			try {
				ITextTable textTable =
					doc.getTextTableService().constructTextTable(contents.length + offset,
						contents[0].length);
				doc.getTextService().getTextContentService().insertTextContent(r, textTable);
				r.setText("");
				ITextTablePropertyStore props = textTable.getPropertyStore();
				long w = props.getWidth();
				long percent = w / 100;
				for (int row = 0; row < contents.length; row++) {
					String[] zeile = contents[row];
					for (int col = 0; col < zeile.length; col++) {
						textTable.getCell(col, row + offset).getTextService().getText().setText(
							zeile[col]);
					}
				}
				if (columnSizes == null) {
					textTable.spreadColumnsEvenly();
				} else {
					for (int col = 0; col < contents[0].length; col++) {
						textTable.getColumn(col).setWidth((short) (columnSizes[col] * percent));
					}
					
				}
				
				return true;
			} catch (Exception ex) {
				ExHandler.handle(ex);
			}
		}
		return false;
		
	}
	
	/**
	 * Insert Text and return a cursor describing the position We can not avoid using UNO here,
	 * because NOA does not give us enough control over the text cursor
	 */
	public Object insertText(final String marke, final String text, final int adjust){
		SearchDescriptor search = new SearchDescriptor(marke);
		search.setIsCaseSensitive(true);
		ISearchResult searchResult = doc.getSearchService().findFirst(search);
		XText myText = doc.getXTextDocument().getText();
		XTextCursor cur = myText.createTextCursor();
		// ITextCursor cur=doc.getTextService().getCursorService().getTextCursor();
		if (!searchResult.isEmpty()) {
			ITextRange r = searchResult.getTextRanges()[0];
			cur = myText.createTextCursorByRange(r.getXTextRange());
			cur.setString(text);
			try {
				setFormat(cur);
			} catch (Exception e) {
				ExHandler.handle(e);
			}
			
			cur.collapseToEnd();
		}
		return cur;
	}
	
	/**
	 * Insert text at a position returned by insertText(String,text,adjust)
	 */
	public Object insertText(final Object pos, final String text, final int adjust){
		XTextCursor cur = (XTextCursor) pos;
		if (cur != null) {
			cur.setString(text);
			try {
				setFormat(cur);
			} catch (Exception e) {
				ExHandler.handle(e);
			}
			cur.collapseToEnd();
		}
		return cur;
	}
	
	/**
	 * Insert Text inside a rectangular area. Again we need UNO to get access to a Text frame.
	 */
	public Object insertTextAt(final int x, final int y, final int w, final int h,
		final String text, final int adjust){
		
		try {
			XTextDocument myDoc = doc.getXTextDocument();
			com.sun.star.lang.XMultiServiceFactory documentFactory =
				(com.sun.star.lang.XMultiServiceFactory) UnoRuntime.queryInterface(
					com.sun.star.lang.XMultiServiceFactory.class, myDoc);
			
			Object frame = documentFactory.createInstance("com.sun.star.text.TextFrame");
			
			XText docText = myDoc.getText();
			XTextFrame xFrame = (XTextFrame) UnoRuntime.queryInterface(XTextFrame.class, frame);
			
			XShape xWriterShape = (XShape) UnoRuntime.queryInterface(XShape.class, xFrame);
			
			xWriterShape.setSize(new Size(w * 100, h * 100));
			
			XPropertySet xFrameProps =
				(XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xFrame);
			
			// Setting the vertical position
			xFrameProps.setPropertyValue("AnchorPageNo", new Short((short) 1));
			xFrameProps.setPropertyValue("VertOrientRelation", RelOrientation.PAGE_FRAME);
			xFrameProps.setPropertyValue("AnchorType", TextContentAnchorType.AT_PAGE);
			xFrameProps.setPropertyValue("HoriOrient", HoriOrientation.NONE);
			xFrameProps.setPropertyValue("VertOrient", VertOrientation.NONE);
			xFrameProps.setPropertyValue("HoriOrientPosition", x * 100);
			xFrameProps.setPropertyValue("VertOrientPosition", y * 100);
			
			XTextCursor docCursor = docText.createTextCursor();
			docCursor.gotoStart(false);
			// docText.insertControlCharacter(docCursor,ControlCharacter.PARAGRAPH_BREAK,false);
			docText.insertTextContent(docCursor, xFrame, false);
			
			// get the XText from the shape
			
			// XText xShapeText = ( XText ) UnoRuntime.queryInterface( XText.class, writerShape );
			
			XText xFrameText = xFrame.getText();
			XTextCursor xtc = xFrameText.createTextCursor();
			com.sun.star.beans.XPropertySet charProps = setFormat(xtc);
			ParagraphAdjust paradj;
			switch (adjust) {
			case SWT.LEFT:
				paradj = ParagraphAdjust.LEFT;
				break;
			case SWT.RIGHT:
				paradj = ParagraphAdjust.RIGHT;
				break;
			default:
				paradj = ParagraphAdjust.CENTER;
			}
			
			charProps.setPropertyValue("ParaAdjust", paradj);
			xFrameText.insertString(xtc, text, false);
			
			return xtc;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return false;
		}
		
	}
	
	/**
	 * Print the contents of the panel. NOA does no allow us to select printer and tray, so we do it
	 * with UNO again.
	 */
	public boolean print(final String toPrinter, final String toTray,
		final boolean waitUntilFinished){
		try {
			PropertyValue[] pprops;
			if (StringTool.isNothing(toPrinter)) {
				pprops = new PropertyValue[1];
				pprops[0] = new PropertyValue();
				pprops[0].Name = "Pages";
				pprops[0].Value = "1-";
			} else {
				pprops = new PropertyValue[2];
				pprops[0] = new PropertyValue();
				pprops[0].Name = "Pages";
				pprops[0].Value = "1-";
				pprops[1] = new PropertyValue();
				pprops[1].Name = "Name";
				pprops[1].Value = toPrinter;
			}
			if (!StringTool.isNothing(toTray)) {
				XTextDocument myDoc = doc.getXTextDocument();
				// XTextDocument myDoc=(XTextDocument)
				// UnoRuntime.queryInterface(com.sun.star.text.XTextDocument.class,
				// doc);
				if (!OOPrinter.setPrinterTray(myDoc, toTray)) {
					return false;
				}
			}
			XPrintable xPrintable =
				(XPrintable) UnoRuntime.queryInterface(com.sun.star.view.XPrintable.class, doc
					.getXTextDocument());
			
			com.sun.star.view.XPrintJobBroadcaster selection =
				(com.sun.star.view.XPrintJobBroadcaster) UnoRuntime.queryInterface(
					com.sun.star.view.XPrintJobBroadcaster.class, xPrintable);
			
			MyXPrintJobListener myXPrintJobListener = new MyXPrintJobListener();
			selection.addPrintJobListener(myXPrintJobListener);
			
			// bean.getDocument().print(pprops);
			xPrintable.print(pprops);
			long timeout = System.currentTimeMillis();
			while ((myXPrintJobListener.getStatus() == null)
				|| (myXPrintJobListener.getStatus() == PrintableState.JOB_STARTED)) {
				Thread.sleep(100);
				long to = System.currentTimeMillis();
				if ((to - timeout) > 10000) {
					break;
				}
			}
			
			return true;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return false;
		}
	}
	
	public void setFocus(){
	// TODO Auto-generated method stub
	
	}
	
	public void setFormat(final PageFormat f){
	// TODO Auto-generated method stub
	
	}
	
	public void setSaveOnFocusLost(final boolean bSave){
	// TODO Auto-generated method stub
	
	}
	
	public void showMenu(final boolean b){
	// TODO Auto-generated method stub
	
	}
	
	public void showToolbar(final boolean b){
	// TODO Auto-generated method stub
	
	}
	
	public void setInitializationData(final IConfigurationElement config,
		final String propertyName, final Object data) throws CoreException{
	// TODO Auto-generated method stub
	
	}
	
	/**
	 * basically: ensure that OpenOffice is happy closing the document and create a new temporary
	 * file
	 * 
	 */
	private void clean(){
		try {
			if (doc != null) {
				doc.getPersistenceService().store(myFile.getAbsolutePath());
				// doc.close();
				myFile.delete();
			}
			myFile = File.createTempFile("noa", ".odt");
			myFile.deleteOnExit();
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
	}
	
	public boolean setFont(final String name, final int style, final float size){
		font = name;
		hi = size;
		stil = style;
		return true;
	}
	
	private com.sun.star.beans.XPropertySet setFormat(final XTextCursor xtc)
		throws UnknownPropertyException, PropertyVetoException, IllegalArgumentException,
		WrappedTargetException{
		com.sun.star.beans.XPropertySet charProps =
			(com.sun.star.beans.XPropertySet) UnoRuntime.queryInterface(
				com.sun.star.beans.XPropertySet.class, xtc);
		if (font != null) {
			charProps.setPropertyValue("CharFontName", font);
			charProps.setPropertyValue("CharHeight", new Float(hi));
			switch (stil) {
			case SWT.MIN:
				charProps.setPropertyValue("CharWeight", 15f /* FontWeight.ULTRALIGHT */);
				break;
			case SWT.NORMAL:
				charProps.setPropertyValue("CharWeight", FontWeight.LIGHT);
				break;
			case SWT.BOLD:
				charProps.setPropertyValue("CharWeight", FontWeight.BOLD);
				break;
			}
		}
		
		return charProps;
	}
	
	class closeListener implements ICloseListener {
		
		private IOfficeApplication officeAplication = null;
		
		// ----------------------------------------------------------------------------
		/**
		 * Constructs a new SnippetDocumentCloseListener
		 * 
		 * @author Sebastian Rösgen
		 * @date 17.03.2006
		 */
		public closeListener(final IOfficeApplication officeAplication){
			this.officeAplication = officeAplication;
		}
		
		// ----------------------------------------------------------------------------
		/**
		 * Is called when someone tries to close a listened object. Not needed in here.
		 * 
		 * @param closeEvent
		 *            close event
		 * @param getsOwnership
		 *            information about the ownership
		 * 
		 * @author Sebastian Rösgen
		 * @date 17.03.2006
		 */
		public void queryClosing(final ICloseEvent closeEvent, final boolean getsOwnership){
		// nothing to do in here
		}
		
		// ----------------------------------------------------------------------------
		/**
		 * Is called when the listened object is closed really.
		 * 
		 * @param closeEvent
		 *            close event
		 * 
		 * @author Sebastian Rösgen
		 * @date 17.03.2006
		 */
		public void notifyClosing(final ICloseEvent closeEvent){
			try {
				removeMe();
			} catch (Exception exception) {
				System.err.println("Error closing office application!");
				exception.printStackTrace();
			}
		}
		
		// ----------------------------------------------------------------------------
		/**
		 * Is called when the broadcaster is about to be disposed.
		 * 
		 * @param event
		 *            source event
		 * 
		 * @author Sebastian Rösgen
		 * @date 17.03.2006
		 */
		public void disposing(final IEvent event){
		// nothing to do in here
		}
		// ----------------------------------------------------------------------------
		
	}
}
