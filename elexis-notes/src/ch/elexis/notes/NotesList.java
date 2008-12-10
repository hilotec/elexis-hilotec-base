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
 *  $Id: NotesList.java 4802 2008-12-10 18:26:18Z rgw_ch $
 *******************************************************************************/

package ch.elexis.notes;

import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import ch.elexis.actions.GlobalEvents;
import ch.elexis.util.DefaultLabelProvider;
import ch.elexis.util.SWTHelper;

public class NotesList extends Composite {
	TreeViewer tv;
	Composite parent;
	Text tFilter;
	String filterExpr;
	NotesFilter notesFilter = new NotesFilter();
	
	NotesList(Composite parent){
		super(parent, SWT.NONE);
		setLayout(new GridLayout());
		this.parent = parent;
		tFilter = new Text(this, SWT.SINGLE);
		tFilter.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		// ((GridData) filter.getLayoutData()).heightHint = 15;
		tFilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e){
				filterExpr = tFilter.getText().toLowerCase();
				if (filterExpr.length() == 0) {
					tv.removeFilter(notesFilter);
					tv.collapseAll();
				} else {
					tv.addFilter(notesFilter);
					tv.expandAll();
				}
				
			}
		});
		tv = new TreeViewer(this, SWT.NONE);
		tv.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tv.setContentProvider(new NotesContentProvider());
		tv.setLabelProvider(new DefaultLabelProvider());
		tv.setUseHashlookup(true);
		tv.setInput(parent);
		tv.addSelectionChangedListener(GlobalEvents.getInstance().getDefaultListener());
	}
	
	public void dispose(){
		tv.removeSelectionChangedListener(GlobalEvents.getInstance().getDefaultListener());
	}
	
	class NotesFilter extends ViewerFilter {
		
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element){
			String f = tFilter.getText();
			if (f.length() == 0) {
				return true;
			}
			return isMatch((Note) element, f);
		}
		
		private boolean isMatch(Note n, String t){
			if (n.getLabel().toLowerCase().startsWith(t)) {
				return true;
			}
			if(n.getKeywords().contains(t)){
				return true;
			}
			List<Note> l = n.getChildren();
			for (Note note : l) {
				if (isMatch(note, t)) {
					return true;
				}
			}
			return false;
		}
		
	}
	
}
