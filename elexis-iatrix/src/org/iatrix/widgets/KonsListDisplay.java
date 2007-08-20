/*******************************************************************************
 * Copyright (c) 2007, G. Weirich, D. Lutz, P. Schönbucher and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    D. Lutz    - new version for Iatrix
 *    
 *  $Id$
 *******************************************************************************/

package org.iatrix.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.actions.BackgroundJob;
import ch.elexis.actions.BackgroundJob.BackgroundJobListener;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

/**
 * Anzeige der vergangenen Konsultationen inkl. Verrechnung.
 * Der Patient wird ueber die Methode setPatient(Patient) festgelegt.
 * @author Daniel Lutz
 *
 */
public class KonsListDisplay extends Composite implements BackgroundJobListener {
	private Patient patient = null;
	
	private FormToolkit toolkit;
	private ScrolledForm form;
	private Composite formBody;
	
	private KonsListComposite konsListComposite;

	private KonsLoader dataLoader;

	public KonsListDisplay(Composite parent) {
		super(parent, SWT.BORDER);
		
		setLayout(new FillLayout());

		toolkit = new FormToolkit(getDisplay());
		form = toolkit.createScrolledForm(this);
		formBody = form.getBody();
		
		formBody.setLayout(new GridLayout(1, false));
		
		konsListComposite = new KonsListComposite(formBody, toolkit);
		konsListComposite.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

		dataLoader = new KonsLoader();
		dataLoader.addListener(this);
	}
	
	/**
	 * reload contents
	 */
	private void reload(boolean showLoading) {
		if (patient != null && dataLoader.isValid()) {
			konsListComposite.setKonsultationen(dataLoader.getKonsultationen());
		} else {
			if (showLoading) {
				konsListComposite.setKonsultationen(null);
			}
		}

		refresh();
	}
	
	/*
	 * re-display data
	 */
	private void refresh() {
		form.reflow(true);
	}
	
	public void setPatient(Patient patient) {
		boolean patientChanged = patientChanged(patient);

		this.patient = patient;

		dataLoader.cancel();
		dataLoader.invalidate();

		// cause "loading" label to be displayed
		if (patientChanged) {
			reload(true);
		}

		dataLoader.setPatient(patient);
		dataLoader.schedule();
	}
	
	private boolean patientChanged(Patient newPatient) {
		if (this.patient != null || newPatient != null) {
			if (this.patient == null ||
					newPatient == null ||
					!this.patient.getId().equals(newPatient.getId())) {
				
				return true;
			}
		}

		return false;
	}
	
	public void jobFinished(BackgroundJob j) {
		reload(false);
	}
	
	class KonsLoader extends BackgroundJob {
		String name;
		Patient patient = null;
		List<KonsListComposite.KonsData> konsDataList = new ArrayList<KonsListComposite.KonsData>();
		
		public KonsLoader() {
			super("KonsLoader");
		}

		public void setPatient(Patient patient) {
			this.patient = patient;
			
			invalidate();
		}
		
		public IStatus execute(IProgressMonitor monitor) {
			synchronized (konsDataList) {
				konsDataList.clear();
				
				List<Konsultation> konsList = new ArrayList<Konsultation>();

				if (patient != null) {
					Fall[] faelle = patient.getFaelle();
					for (Fall fall : faelle) {
						Konsultation[] kons = fall.getBehandlungen(false);
						for (Konsultation k : kons) {
							konsList.add(k);
						}
						
						if (monitor.isCanceled()) {
							monitor.done();
							return Status.CANCEL_STATUS;
						}
					}
				}
				
				monitor.worked(1);

				Collections.sort(konsList, new Comparator<Konsultation>() {
					TimeTool t1=new TimeTool();
					TimeTool t2=new TimeTool();
					public int compare(final Konsultation o1, final Konsultation o2) {
						if((o1==null) || (o2==null)){
							return 0;
						}
						t1.set(o1.getDatum());
						t2.set(o2.getDatum());
						if(t1.isBefore(t2)){
							return 1;
						}
						if(t1.isAfter(t2)){
							return -1;
						}
						return 0;
					}
				});

				
				if (monitor.isCanceled()) {
					monitor.done();
					return Status.CANCEL_STATUS;
				}

				// convert Konsultation objects to KonsData objects
				
				for (Konsultation k : konsList) {
					KonsListComposite.KonsData ks = new KonsListComposite.KonsData(k);
					konsDataList.add(ks);
				}

				monitor.worked(1);
				monitor.done();

				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				} else {
					result = konsDataList;
					return Status.OK_STATUS;
				}
			}
		}
		
		public int getSize() {
			/*
			if (konsultationen != null) {
				synchronized (konsultationen) {
					return konsultationen.size();
				}
			} else {
				return 0;
			}
			*/
			
			// number of work steps in execute()
			return 2;
		}
		
		public List<KonsListComposite.KonsData> getKonsultationen() {
			Object data = getData();
			if (data instanceof List) {
				return (List<KonsListComposite.KonsData>) data;
			} else {
				return null;
			}
		}
	}
	
//	/**
//	 *	Extension of FormToolkit re-implementing createScrolledForm() 
//	 * @author danlutz
//	 */
//	class MyFormToolkit extends FormToolkit {
//		MyFormToolkit(Display display) {
//			super(display);
//		}
//		
//		/**
//		 * Create a toolkit without SWT.H_SCROLL.
//		 * Copied code from ScrolledForm.createScrolledForm(Composite parent)
//		 */
//		public ScrolledForm createScrolledForm(Composite parent) {
//			int orientation = getOrientation();
//			FormColors colors = getColors();
//			
//			ScrolledForm form = new ScrolledForm(parent, SWT.V_SCROLL
//					| orientation);
//			form.setExpandHorizontal(true);
//			form.setExpandVertical(true);
//			form.setBackground(colors.getBackground());
//			form.setForeground(colors.getColor(IFormColors.TITLE));
//			form.setFont(JFaceResources.getHeaderFont());
//			return form;
//		}
//
//	}
}
