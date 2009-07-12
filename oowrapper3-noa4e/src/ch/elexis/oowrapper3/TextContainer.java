/*******************************************************************************
 * Copyright (c) 2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: TextContainer.java 5552 2009-07-12 17:23:52Z tschaller $
 *******************************************************************************/

package ch.elexis.oowrapper3;

import java.awt.font.TextHitInfo;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Bundle;

import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.desktop.GlobalCommands;
import ag.ion.bion.officelayer.desktop.IFrame;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.DocumentException;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.text.ITextCursor;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextRange;
import ag.ion.bion.officelayer.text.ITextTable;
import ag.ion.bion.officelayer.text.TextException;
import ag.ion.bion.officelayer.text.table.ITextTablePropertyStore;
import ag.ion.noa.frame.IDispatch;
import ag.ion.noa.frame.IDispatchDelegate;
import ag.ion.noa.printing.IPrintProperties;
import ag.ion.noa.search.ISearchResult;
import ag.ion.noa.search.SearchDescriptor;
import ag.ion.noa4e.ui.widgets.OfficePanel;
import ch.elexis.text.ITextPlugin;
import ch.elexis.text.ReplaceCallback;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;

public class TextContainer implements ITextPlugin {
	public static final String MIMETYPE_OO2 = "application/vnd.oasis.opendocument.text";
	
	private IOfficeApplication office;
	private OfficePanel panel;
	private ITextDocument doc;
	private File myFile;
	private SaveDelegate saveDelegate = new SaveDelegate();
	private IDispatch saveold;
	ICallback textHandler;
	private static final Log log = Log.get("oowrapper3");
	private boolean bSaveOnFocusLost;
	
	public Composite createContainer(Composite parent, ICallback handler){
		panel = new OfficePanel(parent, SWT.NONE);
		parent.setLayout(new GridLayout());
		panel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		panel.setBuildAlwaysNewFrames(true);
		textHandler = handler;
		return panel;
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
	
	private boolean setDoc(IDocument it){
		if (it != null) {
			doc = (ITextDocument) it;
			IFrame frame = panel.getFrame();
			// IDispatch saveold=frame.getDispatch(GlobalCommands.SAVE);
			frame.disableDispatch(GlobalCommands.CLOSE_DOCUMENT);
			frame.disableDispatch(GlobalCommands.QUIT_APPLICATION);
			frame.disableDispatch(GlobalCommands.CLOSE_WINDOW);
			
			frame.addDispatchDelegate(GlobalCommands.SAVE, saveDelegate);
			frame.updateDispatches();
			return true;
		}
		return false;
	}
	
	public boolean clear(){
		if (textHandler != null) {
			textHandler.save();
		}
		clean();
		doc.close();
		return true;
	}
	
	public boolean createEmptyDocument(){
		try {
			clean();
			Bundle bundle = Platform.getBundle("ch.elexis.oowrapper3");
			Path path = new Path("rsc/empty.odt");
			InputStream is = FileLocator.openStream(bundle, path, false);
			FileOutputStream fos = new FileOutputStream(myFile);
			FileTool.copyStreams(is, fos);
			is.close();
			fos.close();
			panel.loadDocument(false, myFile.getAbsolutePath(), DocumentDescriptor.DEFAULT);
			return setDoc(panel.getDocument());
			
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return false;
		}
	}
	
	public void dispose(){
		if (doc != null) {
			doc.close();
			doc = null;
		}
		if (panel != null) {
			panel.dispose();
		}
		
	}
	
	public boolean findOrReplace(String pattern, ReplaceCallback cb){
		SearchDescriptor search = new SearchDescriptor(pattern);
		search.setUseRegularExpression(true);
		if (doc == null) {
			SWTHelper.showError("No doc", "Fehler:", "Dokument ist leer der nicht vorhanden");
			return false;
		}
		ISearchResult searchResult = doc.getSearchService().findAll(search);
		
		if (!searchResult.isEmpty()) {
			ITextRange[] textRanges = searchResult.getTextRanges();
			if (cb != null) {
				ITextCursor cur;
				try {
					cur = doc.getTextService().getText().getTextCursorService().getTextCursor();
				} catch (TextException e) {
					ExHandler.handle(e);
					return false;
				}
				for (ITextRange r : textRanges) {
					
					cur.gotoRange(r, false);
					String orig = cur.getString();
					
					// String orig = r.getXTextRange().getString();
					
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
	
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
		throws CoreException{
		// TODO Auto-generated method stub
		System.out.println("textplugin");
	}
	
	public String getMimeType(){
		return MIMETYPE_OO2;
	}
	
	public boolean insertTable(String place, int properties, String[][] contents,
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
	
	public Object insertText(String marke, String text, int adjust){
		
		SearchDescriptor search = new SearchDescriptor(marke);
		search.setIsCaseSensitive(true);
		ISearchResult searchResult = doc.getSearchService().findFirst(search);
		// XText myText = doc.getXTextDocument().getText();
		// XTextCursor cur = myText.createTextCursor();
		ITextCursor cur = doc.getTextService().getCursorService().getTextCursor();
		if (!searchResult.isEmpty()) {
			ITextRange r = searchResult.getTextRanges()[0];
			r.setText(text);
			cur.gotoRange(r, false);
		}
		return cur;
	}
	
	public Object insertText(Object pos, String text, int adjust){
		ITextCursor cur = (ITextCursor) pos;
		if (cur != null) {
			cur.setString(text);
			cur.goRight((short) 1, false);
		}
		return cur;
		
	}
	
	public Object insertTextAt(int x, int y, int w, int h, String text, int adjust){
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean loadFromByteArray(byte[] bs, boolean asTemplate){
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
			return setDoc(panel.getDocument());
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return false;
		}
	}
	
	public boolean loadFromStream(InputStream is, boolean asTemplate){
		try {
			clean();
			doc =
				(ITextDocument) office.getDocumentService().loadDocument(is,
					DocumentDescriptor.DEFAULT_HIDDEN);
			if (doc != null) {
				doc.getPersistenceService().store(myFile.getAbsolutePath());
				doc.close();
				panel.loadDocument(false, myFile.getAbsolutePath(), DocumentDescriptor.DEFAULT);
				doc = (ITextDocument) panel.getDocument();
				return setDoc(doc);
			}
		} catch (Exception e) {
			ExHandler.handle(e);
			
		}
		return false;
	}
	
	public boolean print(String toPrinter, String toTray, final boolean waitUntilFinished){
		PrintProperties pprops = new PrintProperties("1-");
		try {
			doc.getPrintService().print(pprops);
			return true;
		} catch (DocumentException e) {
			ExHandler.handle(e);
			SWTHelper.showError("Fehler beim Drucken", e.getMessage());
			return false;
		}
	}
	
	public void setFocus(){
		panel.setFocus();
	}
	
	public boolean setFont(String name, int style, float size){
		
		return false;
	}
	
	public boolean setStyle(int style){
		
		return false;
	}
	
	public void setFormat(PageFormat f){
	// TODO Auto-generated method stub
	
	}
	
	public void setSaveOnFocusLost(boolean save){
		bSaveOnFocusLost=save;
	
	}
	
	public void showMenu(boolean b){
	// TODO Auto-generated method stub
	
	}
	
	public void showToolbar(boolean b){
	// TODO Auto-generated method stub
	
	}
	
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
	
	private class SaveDelegate implements IDispatchDelegate {
		
		public void dispatch(Object[] arg0){
			if(textHandler!=null){
				textHandler.save();
			}
			
		}
		
	}
	static class PrintProperties implements IPrintProperties {
		private String mPages = "";
		
		PrintProperties(String pages){
			mPages = pages;
		}
		
		public short getCopyCount(){
			return (short) 1;
		}
		
		public String getPages(){
			return mPages;
		}
		
	}
	
	
}
