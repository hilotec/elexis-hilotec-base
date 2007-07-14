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
 *  $Id: LabResult.java 2736 2007-07-07 14:07:40Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.Heartbeat.HeartListener;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;

/**
 * This view displays all LabResults that are not marked as seen by the doctor. One can mark them individually
 * or globally as seen from this view.
 * @author gerry
 *
 */
public class LabNotSeenView extends ViewPart implements ActivationListener, HeartListener {
	public final static String ID="ch.elexis.LabNotSeenView";
	CheckboxTableViewer tv;
	private static final String[] columnHeaders={"Patient","Parameter","Normbereich","Datum","Wert"};
	private static final int[] colWidths=new int[]{250,100,60,70,50};
	
	public LabNotSeenView() {
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new FillLayout());
		Table table=new Table(parent,SWT.CHECK|SWT.V_SCROLL);
		for(int i=0;i<columnHeaders.length;i++){
			TableColumn tc=new TableColumn(table,SWT.NONE);
			tc.setText(columnHeaders[i]);
			tc.setWidth(colWidths[i]);
		}
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tv=new CheckboxTableViewer(table);
		tv.setContentProvider(new LabNotSeenContentProvider());
		tv.setLabelProvider(new LabNotSeenLabelProvider());
		tv.setUseHashlookup(true);
		GlobalEvents.getInstance().addActivationListener(this, this);
		Hub.heart.addListener(this);
		tv.addCheckStateListener(new ICheckStateListener(){
			public void checkStateChanged(final CheckStateChangedEvent event) {
				LabResult lr=(LabResult)event.getElement();
				boolean state=event.getChecked();
				if(state){
					lr.removeFromUnseen();
				}else{
					lr.addToUnseen();
				}
			}
			
		});
		tv.setInput(this);
	}

	@Override
	public void dispose() {
		GlobalEvents.getInstance().removeActivationListener(this, this);
		Hub.heart.removeListener(this);
		super.dispose();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	static class LabNotSeenLabelProvider extends LabelProvider implements ITableLabelProvider{

		public Image getColumnImage(final Object element, final int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(final Object element, final int columnIndex) {
			LabResult lr=(LabResult)element;
			switch(columnIndex){
			case 0: return lr.getPatient().getLabel();
			case 1: return lr.getItem().getName();
			case 2: 
				Patient pat=lr.getPatient();
				if(pat.getGeschlecht().equalsIgnoreCase("m")){
					return lr.getItem().getRefM();
				}else{
					return lr.getItem().getRefW();
				}
			case 3: return lr.getDate();
			case 4:	return lr.getResult();
			}
			return "?";
		}
		
	}
	
	class LabNotSeenContentProvider implements IStructuredContentProvider{

		public Object[] getElements(final Object inputElement) {
			List<LabResult> unseen=LabResult.getUnseen();
			return unseen.toArray();
		}

		public void dispose() { /* don't mind */}
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// don't mind
		}
		
	}

	public void activation(final boolean mode) {
		if(mode){
			tv.refresh();
		}
	}

	public void visible(final boolean mode) {
		// don't mind
	}

	public void heartbeat() {
		tv.refresh();
	}
}
