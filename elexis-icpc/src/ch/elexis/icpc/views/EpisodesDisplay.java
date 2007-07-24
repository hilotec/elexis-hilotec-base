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
 *    $Id: EpisodesDisplay.java 2896 2007-07-24 20:11:38Z rgw_ch $
 *******************************************************************************/

package ch.elexis.icpc.views;

import java.util.List;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.Desk;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.icpc.Activator;
import ch.elexis.icpc.Episode;
import ch.elexis.util.PersistentObjectDragSource;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.VersionInfo;

public class EpisodesDisplay extends Composite {
	ScrolledForm form;
	Patient actPatient;
	TreeViewer tvEpisodes;
	public EpisodesDisplay(Composite parent){
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
		tvEpisodes.setSorter(new ViewerSorter(){

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				Episode ep1=(Episode)e1;
				Episode ep2=(Episode)e2;
				VersionInfo v1=new VersionInfo(ep1.get("Number"));
				VersionInfo v2=new VersionInfo(ep2.get("Number"));
				if(v1.isNewer(v2)){
					return 1;
				}else if(v1.isOlder(v2)){
					return -1;
				}
				return 0;
			}
			
		});
		/* PersistentObjectDragSource pods=*/new PersistentObjectDragSource(tvEpisodes);
		//lvEpisodes.addDragSupport(DND.DROP_COPY, new Transfer[] {TextTransfer.getInstance()}, pods);
		setPatient(GlobalEvents.getSelectedPatient());
	}
	public void setPatient(Patient pat){
		actPatient=pat;
		tvEpisodes.setInput(pat);
		tvEpisodes.refresh();
	}
	public Episode getSelectedEpisode() {
		IStructuredSelection sel=(IStructuredSelection) tvEpisodes.getSelection();
		if(!sel.isEmpty()){
			return (Episode)sel.getFirstElement();
		}
		return null;
	}
	class EpisodecontentProvider implements ITreeContentProvider{

		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof Episode){
				Episode ep=(Episode)parentElement;
				return new Object[]{ep.get("StartDate")};
			}
				
			
			return null;
		}

		public Object getParent(Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean hasChildren(Object element) {
			if(element instanceof Episode){
				return true;
			}
			return false;
		}

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
			// TODO Auto-generated method stub
			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
			
		}
		
	}
	class EpisodesLabelProvider extends LabelProvider implements IColorProvider{
		@Override
		public String getText(Object element) {
			if(element instanceof Episode){
				return ((Episode)element).getLabel();
			}else if(element instanceof String){
				return element.toString();
			}
			return super.getText(element);
		}

		public Color getBackground(Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		public Color getForeground(Object element) {
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
