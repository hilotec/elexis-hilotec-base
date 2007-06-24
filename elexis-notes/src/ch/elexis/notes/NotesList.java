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
 *  $Id: NotesList.java 1647 2007-01-22 21:44:08Z rgw_ch $
 *******************************************************************************/

package ch.elexis.notes;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.actions.GlobalEvents;
import ch.elexis.util.DefaultLabelProvider;
import ch.elexis.util.SWTHelper;

public class NotesList extends Composite {
	TreeViewer tv;
	NotesList(Composite parent){
		super(parent,SWT.NONE);
		tv=new TreeViewer(this,SWT.NONE);
		setLayout(new GridLayout());
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
	
}
