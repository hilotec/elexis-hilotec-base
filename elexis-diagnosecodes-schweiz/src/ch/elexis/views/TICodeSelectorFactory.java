/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: TICodeSelectorFactory.java 2209 2007-04-13 09:30:35Z danlutz $
 *******************************************************************************/

package ch.elexis.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import ch.elexis.data.TICode;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.DefaultControlFieldProvider;
import ch.elexis.util.SimpleWidgetProvider;
import ch.elexis.util.ViewerConfigurer;
import ch.elexis.util.ViewerConfigurer.CommonContentProvider;
import ch.elexis.views.codesystems.CodeSelectorFactory;

public class TICodeSelectorFactory extends CodeSelectorFactory{

	public TICodeSelectorFactory(){
		System.out.println("hier"); //$NON-NLS-1$
	}
	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer cv) {
		return new ViewerConfigurer(
				new TICodeContentProvider(),
				new TICodeLabelProvider(),
				new DefaultControlFieldProvider(cv, new String[]{"Text"}), //$NON-NLS-1$
				new ViewerConfigurer.DefaultButtonProvider(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_TREE, SWT.NONE, null)
				);
	}
	
	static class TICodeContentProvider implements ITreeContentProvider, CommonContentProvider{
		public Object[] getChildren(Object parentElement) {
			TICode c=(TICode)parentElement;
			return c.getChildren();
		}

		public Object getParent(Object element) {
			TICode c=(TICode)element;
			return c.getParent();
		}

		public boolean hasChildren(Object element) {
			TICode c=(TICode)element;
			return c.hasChildren();
		}

		public Object[] getElements(Object inputElement) {
			return TICode.getRootNodes();
		}

		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {}
		public void startListening() {}
		public void stopListening() {}
		public void changed(String[] fields, String[] values) {}
		public void reorder(String field) {}
		public void selected() {}
	}
	static class TICodeLabelProvider extends LabelProvider{
		public String getText(Object element) {
			TICode c=(TICode)element;
			return c.getCode()+" "+c.getText(); //$NON-NLS-1$
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}
		
	}
	@Override
	public Class getElementClass() {
		return TICode.class;
	}
	@Override
	public void dispose() {}
	@Override
	public String getCodeSystemName() {
		return "TI-Code"; //$NON-NLS-1$
	}
}
