/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ETFTextPlugin.java 3243 2007-10-09 04:25:36Z rgw_ch $
 *******************************************************************************/
package ch.elexis.text;

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.Desk;
import ch.elexis.util.IKonsExtension;
import ch.rgw.Compress.CompEx;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * A TextPlugin based on an EnhancedTextField
 * @author gerry
 *
 */
public class ETFTextPlugin implements ITextPlugin {
	EnhancedTextField etf;
	ICallback handler;
	boolean bSaveOnFocusLost=false;
	IKonsExtension ike;
	
	public boolean clear() {
		etf.setText("");
		return true;
	}
	
	public void setSaveOnFocusLost(boolean mode){
		bSaveOnFocusLost=mode;
	}

	public Composite createContainer(Composite parent, ICallback h) {
		handler=h;
		etf= new EnhancedTextField(parent);
		etf.text.addFocusListener(new FocusAdapter(){
			@Override
			public void focusLost(FocusEvent e) {
				if(bSaveOnFocusLost){
					if(handler!=null){
						handler.save();
					}
				}
			}
			
		});
		ike=new ExternalLink();
		ike.connect(etf);
		etf.setText("");
		return etf;
	}

	public boolean createEmptyDocument() {
		etf.setText("");
		return true;
	}

	public void dispose() {
		etf.dispose();
	}

	public boolean findOrReplace(String pattern, ReplaceCallback cb) {
		// TODO Auto-generated method stub
		return false;
	}

	public PageFormat getFormat() {
		return PageFormat.USER;
	}

	public String getMimeType() {
		return "text/xml";
	}

	public boolean insertTable(String place, int properties,
			String[][] contents, int[] columnSizes) {
		// TODO Auto-generated method stub
		return false;
	}

	public Object insertText(String marke, String text, int adjust) {
		int pos=0;
		if(StringTool.isNothing(marke)){
			etf.text.setSelection(0);
		}else{
			String tx=etf.text.getText();
			pos=tx.indexOf(marke);
			etf.text.setSelection(pos,pos+marke.length());
		}
		etf.text.insert(text);
		return new Integer(pos+text.length());
	}

	public Object insertText(Object pos, String text, int adjust) {
		if(!(pos instanceof Integer)){
			return null;
		}
		Integer px=(Integer)pos;
		etf.text.setSelection(px);
		etf.text.insert(text);
		return new Integer(px+text.length());
	}

	public Object insertTextAt(int x, int y, int w, int h, String text,
			int adjust) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean loadFromByteArray(byte[] bs, boolean asTemplate) {
		try{
			byte[] exp=CompEx.expand(bs);
			String cnt="";
			if(exp!=null){
				cnt=new String(exp,"UTF-8");
			}
			etf.setText(cnt);
			return true;
		}catch(Exception ex){
			ExHandler.handle(ex);
			return false;
		}
	}

	public byte[] storeToByteArray() {
		try{
			String cnt=etf.getDocumentAsText();
			byte[] exp=cnt.getBytes("UTF-8");
			return CompEx.Compress(exp, CompEx.ZIP);
		}catch(Exception ex){
			ExHandler.handle(ex);
			return null;
		}
		
	}

	public boolean loadFromStream(InputStream is, boolean asTemplate) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean print(String toPrinter, String toTray,
			boolean waitUntilFinished) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setFocus() {
		etf.setFocus();
	}

	public boolean setFont(String name, int style, float size) {
		Font font=new Font(Desk.theDisplay,name,Math.round(size),style);
		return true;
	}

	public void setFormat(PageFormat f) {
		// TODO Auto-generated method stub

	}

	public void showMenu(boolean b) {
		// TODO Auto-generated method stub

	}

	public void showToolbar(boolean b) {
		// TODO Auto-generated method stub

	}


	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		// TODO Auto-generated method stub

	}

}
