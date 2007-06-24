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
 *    $Id: EpisodesDisplay.java 1771 2007-02-09 20:46:19Z rgw_ch $
 *******************************************************************************/

package ch.elexis.icpc.views;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.icpc.Activator;
import ch.elexis.icpc.Episode;
import ch.elexis.util.PersistentObjectDragSource;
import ch.elexis.util.SWTHelper;

public class EpisodesDisplay extends Composite {
	ScrolledForm form;
	Patient actPatient;
	ListViewer lvEpisodes;
	public EpisodesDisplay(Composite parent){
		super(parent,SWT.NONE);
		setLayout(new GridLayout());
		form=Activator.getToolkit().createScrolledForm(this);
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Composite body=form.getBody();
		body.setLayout(new GridLayout());
		lvEpisodes=new ListViewer(body);
		lvEpisodes.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		lvEpisodes.setLabelProvider(new LabelProvider(){

			@Override
			public String getText(Object element) {
				if(element instanceof Episode){
					return ((Episode)element).getLabel();
				}
				return super.getText(element);
			}
			
		});
		lvEpisodes.setContentProvider(new IStructuredContentProvider(){

			public Object[] getElements(Object inputElement) {
				if(actPatient!=null){
					Query<Episode> qbe=new Query<Episode>(Episode.class);
					qbe.add("PatientID", "=", actPatient.getId());
					List<Episode> list=qbe.execute();
					return list.toArray();
				}
				return new Object[0];
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
		});
		/* PersistentObjectDragSource pods=*/new PersistentObjectDragSource(lvEpisodes);
		//lvEpisodes.addDragSupport(DND.DROP_COPY, new Transfer[] {TextTransfer.getInstance()}, pods);
		setPatient(GlobalEvents.getSelectedPatient());
	}
	public void setPatient(Patient pat){
		actPatient=pat;
		lvEpisodes.setInput(pat);
		lvEpisodes.refresh();
	}
	public Episode getSelectedEpisode() {
		IStructuredSelection sel=(IStructuredSelection) lvEpisodes.getSelection();
		if(!sel.isEmpty()){
			return (Episode)sel.getFirstElement();
		}
		return null;
	}
}
