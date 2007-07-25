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
 *    $Id: EpisodesDisplay.java 2905 2007-07-25 10:53:10Z rgw_ch $
 *******************************************************************************/

package ch.elexis.icpc.views;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.Desk;
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
	TreeViewer tvEpisodes;
	public EpisodesDisplay(final Composite parent){
		super(parent,SWT.NONE);
		setLayout(new GridLayout());
		form=Activator.getToolkit().createScrolledForm(this);
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Composite body=form.getBody();
		body.setLayout(new GridLayout());
		tvEpisodes=new TreeViewer(body);
		tvEpisodes.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tvEpisodes.setLabelProvider(new EpisodesLabelProvider());
		tvEpisodes.setContentProvider(new EpisodecontentProvider());
		tvEpisodes.addSelectionChangedListener(GlobalEvents.getInstance().getDefaultListener());
		/* PersistentObjectDragSource pods=*/new PersistentObjectDragSource(tvEpisodes);
		//lvEpisodes.addDragSupport(DND.DROP_COPY, new Transfer[] {TextTransfer.getInstance()}, pods);
		setPatient(GlobalEvents.getSelectedPatient());
	}
	public void setPatient(final Patient pat){
		actPatient=pat;
		if(pat!=null){
			tvEpisodes.setInput(pat);
		}
		tvEpisodes.refresh();
	}
	public Episode getSelectedEpisode() {
		Tree widget=tvEpisodes.getTree();
		TreeItem[] sel=widget.getSelection();
		TreeItem f=sel[0];
		TreeItem p=f;
		do{
			f=p;
			p=f.getParentItem();
		}while(p!=null);
		String etext=f.getText();
		for(Object o:((ITreeContentProvider)tvEpisodes.getContentProvider()).getElements(actPatient)){
			if(o instanceof Episode){
				Episode ep=(Episode)o;
				if(ep.getLabel().equals(etext)){
					return ep;
				}
			}
		}
		return null;
	}
	class EpisodecontentProvider implements ITreeContentProvider{

		public Object[] getChildren(final Object parentElement) {
			if(parentElement instanceof Episode){
				Episode ep=(Episode)parentElement;
				return new Object[]{"Seit: "+ep.get("StartDate"),
						"Status: "+ep.getStatusText()};
			}
				
			
			return null;
		}

		public Object getParent(final Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean hasChildren(final Object element) {
			if(element instanceof Episode){
				return true;
			}
			return false;
		}

		public Object[] getElements(final Object inputElement) {
			if(actPatient!=null){
				Query<Episode> qbe=new Query<Episode>(Episode.class);
				qbe.add("PatientID", "=", actPatient.getId());
				List<Episode> list=qbe.execute();
				Collections.sort(list);
				return list.toArray();
			}
			return new Object[0];

		}

		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// TODO Auto-generated method stub
			
		}
		
	}
	class EpisodesLabelProvider extends LabelProvider implements IColorProvider{
		@Override
		public String getText(final Object element) {
			if(element instanceof Episode){
				return ((Episode)element).getLabel();
			}else if(element instanceof String){
				return element.toString();
			}
			return super.getText(element);
		}

		public Color getBackground(final Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		public Color getForeground(final Object element) {
			if(element instanceof Episode){
				Episode e=(Episode) element;
				if(e.getStatus()==Episode.INACTIVE){
					return Desk.theColorRegistry.get(Desk.COL_LIGHTGREY);
				}
			}
			return Desk.theColorRegistry.get(Desk.COL_BLACK);
		}
		
	}
}
