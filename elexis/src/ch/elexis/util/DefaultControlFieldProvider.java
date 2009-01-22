/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: DefaultControlFieldProvider.java 5001 2009-01-22 15:50:06Z rgw_ch $
 *******************************************************************************/

package ch.elexis.util;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import ch.elexis.Desk;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.text.ElexisText;
import ch.elexis.util.ViewerConfigurer.ControlFieldListener;
import ch.elexis.util.ViewerConfigurer.ControlFieldProvider;
import ch.rgw.tools.StringTool;

/**
 * Standardimplementation des ControlFieldProviders. Erzeugt ein Composite mit je einem
 * Texteingabefeld für jedes beim Konstruktor übergebene Feld. Feuert einen ChangedEvent, wenn
 * mindestens zwei Zeichen in eins der Felder eingegeben wurden.
 * 
 * @author Gerry
 */
public class DefaultControlFieldProvider implements ControlFieldProvider {
	protected String[] dbFields, fields, lastFiltered;
	protected ElexisText[] selectors;
	protected final ModListener ml;
	protected final SelListener sl;
	protected boolean modified;
	protected final List<ControlFieldListener> listeners;
	private final FormToolkit tk;
	protected CommonViewer myViewer;
	boolean bCeaseFire;
	
	public DefaultControlFieldProvider(final CommonViewer viewer, final String[] flds){
		fields = new String[flds.length];
		dbFields = new String[fields.length];
		myViewer = viewer;
		// this.fields=new String[fields.length];
		lastFiltered = new String[fields.length];
		for (int i = 0; i < flds.length; i++) {
			lastFiltered[i] = ""; //$NON-NLS-1$
			if (flds[i].indexOf('=') != -1) {
				String[] s = flds[i].split("="); //$NON-NLS-1$
				fields[i] = s[1];
				dbFields[i] = s[0];
			} else {
				fields[i] = dbFields[i] = flds[i];
			}
		}
		ml = new ModListener();
		sl = new SelListener();
		listeners = new LinkedList<ControlFieldListener>();
		tk = Desk.getToolkit();
	}
	
	public Composite createControl(final Composite parent){
		// Form form=tk.createForm(parent);
		// form.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		// Composite ret=form.getBody();
		Composite ret = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		ret.setLayout(layout);
		ret.setBackground(parent.getBackground());
		
		ImageHyperlink hClr = tk.createImageHyperlink(ret, SWT.NONE); //$NON-NLS-1$
		hClr.setImage(Desk.getImage(Desk.IMG_CLEAR));
		hClr.addHyperlinkListener(new HyperlinkAdapter() {
			
			@Override
			public void linkActivated(final HyperlinkEvent e){
				clearValues();
			}
			
		});
		hClr.setBackground(parent.getBackground());
		
		Composite inner = new Composite(ret, SWT.NONE);
		GridLayout lRet = new GridLayout(fields.length, true);
		inner.setLayout(lRet);
		inner.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		for (String l : fields) {
			Hyperlink hl = tk.createHyperlink(inner, l, SWT.NONE);
			hl.addHyperlinkListener(new HyperlinkAdapter() {
				
				@Override
				public void linkActivated(final HyperlinkEvent e){
					Hyperlink h = (Hyperlink) e.getSource();
					fireSortEvent(h.getText());
				}
				
			});
			hl.setBackground(parent.getBackground());
		}
		
		createSelectors(fields.length);
		for (int i = 0; i < selectors.length; i++) {
			selectors[i] = new ElexisText(tk.createText(inner, "", SWT.BORDER)); //$NON-NLS-1$
			selectors[i].addModifyListener(ml);
			selectors[i].addSelectionListener(sl);
			selectors[i].setToolTipText(Messages
				.getString("DefaultControlFieldProvider.enterFilter")); //$NON-NLS-1$
			selectors[i].setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			SWTHelper.setSelectOnFocus((Text) selectors[i].getWidget());
		}
		
		return ret;
	}
	
	protected void createSelectors(int length){
		selectors = new ElexisText[fields.length];
	}
	
	public void setFocus(){
		selectors[0].setFocus();
	}
	
	public boolean isModified(){
		return modified;
	}
	
	public String[] getDBFields(){
		return dbFields;
	}
	
	/**
	 * Reaktion auf Eingaben in die Filterfelder. Reagiert erst wenn mindestens zwei Zeichen
	 * eingegeben wurden oder das Feld geleert wurde.
	 * */
	class ModListener implements ModifyListener {
		public void modifyText(final ModifyEvent e){
			modified = true;
			Text t = (Text) e.getSource();
			String s = t.getText();
			if (!StringTool.leer.equals(s)) {
				if (s.length() == 1) {
					return;
				}
			}
			for (int i = 0; i < lastFiltered.length; i++) {
				lastFiltered[i] = selectors[i].getText();
			}
			fireChangedEvent();
			
		}
	}
	
	/**
	 * Reaktion auf ENTER den Filterfelder. Weist den Viewer an, eine Selektion vorzunehmen.
	 */
	class SelListener implements SelectionListener {
		public void widgetSelected(final SelectionEvent e){
			fireSelectedEvent();
		}
		
		public void widgetDefaultSelected(final SelectionEvent e){
			widgetSelected(e);
		}
	}
	
	public void fireChangedEvent(){
		if (!bCeaseFire) {
			Desk.getDisplay().syncExec(new Runnable() {
				public void run(){
					for (ControlFieldListener lis : listeners) {
						lis.changed(fields, lastFiltered);
					}
				}
			});
		}
	}
	
	public void fireSortEvent(final String text){
		if (!bCeaseFire) {
			for (ControlFieldListener ls : listeners) {
				ls.reorder(text);
			}
		}
	}
	
	public void fireSelectedEvent(){
		if (!bCeaseFire) {
			for (ControlFieldListener ls : listeners) {
				ls.selected();
			}
		}
	}
	
	public void addChangeListener(final ControlFieldListener cl){
		listeners.add(cl);
	}
	
	public void removeChangeListener(final ControlFieldListener cl){
		listeners.remove(cl);
	}
	
	public String[] getValues(){
		return lastFiltered;
	}
	
	/**
	 * Alle Eingabefelder löschen und einen "changeEvent" feuern". Aber nur, wenn die Felder nicht
	 * schon vorher leer waren.
	 */
	public void clearValues(){
		if (!isEmpty()) {
			bCeaseFire = true;
			for (int i = 0; i < selectors.length; i++) {
				selectors[i].setText(StringTool.leer);
				lastFiltered[i] = StringTool.leer;
			}
			modified = false;
			bCeaseFire = false;
			fireChangedEvent();
		}
	}
	
	public void setQuery(final Query q){
		boolean ch = false;
		for (int i = 0; i < fields.length; i++) {
			if (!lastFiltered[i].equals(StringTool.leer)) {
				q.add(dbFields[i], "LIKE", lastFiltered[i] + "%", true); //$NON-NLS-1$ //$NON-NLS-2$
				q.and();
				ch = true;
			}
		}
		if (ch) {
			q.insertTrue();
		}
		
	}
	
	public ViewerFilter createFilter(){
		return new ViewerFilter() {
			
			@Override
			public boolean select(final Viewer viewer, final Object parentElement,
				final Object element){
				PersistentObject po = null;
				if (element instanceof Tree) {
					po = (PersistentObject) ((Tree) element).contents;
				} else if (element instanceof PersistentObject) {
					po = (PersistentObject) element;
				} else {
					return false;
				}
				if (po.isMatching(dbFields, PersistentObject.MATCH_LIKE, lastFiltered)) {
					return true;
				} else {
					if (parentElement instanceof Tree) {
						po = (PersistentObject) ((Tree) parentElement).contents;
					} else if (parentElement instanceof PersistentObject) {
						po = (PersistentObject) parentElement;
					} else {
						return false;
					}
					return po.isMatching(dbFields, PersistentObject.MATCH_LIKE, lastFiltered);
				}
				
			}
		};
		
	}
	
	public IFilter createIFilter(){
		return new IFilter() {
			
			public boolean select(final Object element){
				PersistentObject po = null;
				if (element instanceof Tree) {
					po = (PersistentObject) ((Tree) element).contents;
				} else if (element instanceof PersistentObject) {
					po = (PersistentObject) element;
				} else {
					return false;
				}
				return po.isMatching(dbFields, PersistentObject.MATCH_LIKE, lastFiltered);
			}
			
		};
		
	}
	
	public boolean isEmpty(){
		for (String s : lastFiltered) {
			if (!s.equals(StringTool.leer)) {
				return false;
			}
		}
		return true;
	}
	
	public void ceaseFire(final boolean bCeaseFire){
		this.bCeaseFire = bCeaseFire;
	}
	
	public CommonViewer getCommonViewer(){
		return this.myViewer;
	}
}