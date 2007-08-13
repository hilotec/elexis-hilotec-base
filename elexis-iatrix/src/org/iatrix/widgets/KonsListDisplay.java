/*******************************************************************************
 * Copyright (c) 2006, G. Weirich, D. Lutz, P. Schönbucher and Elexis
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
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import ch.elexis.actions.BackgroundJob;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.BackgroundJob.BackgroundJobListener;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Verrechnet;
import ch.elexis.util.Money;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Anzeige der vergangenen Konsultationen inkl. Verrechnung.
 * Der Patient wird ueber die Methode setPatient(Patient) festgelegt.
 * @author Daniel Lutz
 *
 */
public class KonsListDisplay extends Composite implements BackgroundJobListener {
	// TODO DEBUG
	static long lastTime = System.currentTimeMillis();
	
	private static final int COLUMNS = 2;
	
	private Patient patient = null;
	private List<WidgetRow> rows = new ArrayList<WidgetRow>();
	
	private FormToolkit toolkit;
	private ScrolledForm form;
	private Composite formBody;
	private Label loadingLabel = null;
	
	private LabelProvider verrechnetLabelProvider;
	
	private KonsLoader dataLoader;
	
	public KonsListDisplay(Composite parent) {
		super(parent, SWT.BORDER);
		
		setLayout(new FillLayout());

		toolkit = new FormToolkit(getDisplay());
		form = toolkit.createScrolledForm(this);
		formBody = form.getBody();
		
		TableWrapLayout layout = SWTHelper.createTableWrapLayout(COLUMNS, true);
		layout.leftMargin = 0;
		layout.rightMargin = 0;
		layout.topMargin = 0;
		layout.bottomMargin = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		formBody.setLayout(layout);
		
		verrechnetLabelProvider = new LabelProvider() {
        	public String getText(Object element) {
        		if (!(element instanceof Verrechnet)) {
        			return "";
        		}
        		
        		Verrechnet verrechnet = (Verrechnet) element;
        		String name = verrechnet.getText();

        		// TODO replace with verrechnet.getNickname();
        		String vClass = verrechnet.getVerrechenbar().getClass().getName();
        		if (vClass.equals("ch.elexis.data.TarmedLeistung")) {
            		String nick = ((PersistentObject) verrechnet.getVerrechenbar()).get("Nick");
            		if (!StringTool.isNothing(nick)) {
            			name = nick;
            		}
        		}
        		
        		StringBuilder sb = new StringBuilder();
                int z = verrechnet.getZahl();
                Money preis=new Money(verrechnet.getEffPreis()).multiply(z);
                sb.append(z).append(" ").append(name)
                 .append(" (").append(preis.getAmountAsString()).append(")");
                return sb.toString();
            }
		};

		dataLoader = new KonsLoader();
		dataLoader.addListener(this);
	}
	
	/*
	 * re-display data
	 */
	private void refresh() {
		form.reflow(true);

		for (WidgetRow row : rows) {
			row.setVerticalSeparatorVisibility();
		}
	}
	
	/**
	 * reload contents
	 */
	private void reload() {
		if (loadingLabel != null) {
			loadingLabel.dispose();
		}
		
		for (WidgetRow row : rows) {
			row.dispose();
		}
		
		rows.clear();
		
		if (dataLoader.isValid()) {
			if (patient != null) {
				List<Konsultation> konsultationen = new ArrayList<Konsultation>();

				for (Fall fall : patient.getFaelle()) {
					for (Konsultation konsultation: fall.getBehandlungen(false)) {
						konsultationen.add(konsultation);
					}
				}

				Collections.sort(konsultationen, new Comparator<Konsultation>() {
					TimeTool t1 = new TimeTool();
					TimeTool t2 = new TimeTool();

					public int compare(final Konsultation o1, final Konsultation o2) {
						if ((o1 == null) || (o2 == null)) {
							return 0;
						}
						t1.set(o1.getDatum());
						t2.set(o2.getDatum());
						if (t1.isBefore(t2)) {
							return 1;
						}
						if (t1.isAfter(t2)) {
							return -1;
						}
						return 0;
					}
				});

				for (Konsultation konsultation : konsultationen) {
					WidgetRow row = new WidgetRow(formBody, konsultation);
					row.refresh();
					rows.add(row);
				}
			}
		} else {
			loadingLabel = toolkit.createLabel(formBody, "Lade Konsultationen...");
			loadingLabel.setLayoutData(SWTHelper.getFillTableWrapData(COLUMNS, true, 1, false));
		}

		refresh();
	}
	
	public void setPatient(Patient patient) {
		this.patient = patient;
		dataLoader.invalidate();
		dataLoader.setPatient(patient);
		reload();
		dataLoader.schedule();
	}
	
	private void selectConsultation(Konsultation konsultation) {
		GlobalEvents.getInstance().fireSelectionEvent(konsultation);
	}
	
	public void jobFinished(BackgroundJob j) {
		reload();
	}
	
	/**
	 * This class encapsulates the required widgets for a row. It assumes that
	 * the parent has set a TableWrapLayout with COLUMNS columns.
	 */
	class WidgetRow {
		Label horizontalSeparator;
		Hyperlink hTitle;
		Label lFall;
		EnhancedTextFieldRO etf;
		Text verrechnung;
		Label leftVerticalSeparator;
		Label rightVerticalSeparator;

		Konsultation konsultation;
		
		// collect controls for disposal in dispose()
		List<Control> controls;
		
		// TODO DEBUG
		private void debug(String message) {
			long time = System.currentTimeMillis();
			long delta = time - lastTime;
			lastTime = time;
			System.err.println(message + "[" + delta + ", " + time + "]");
		}
		
		WidgetRow(Composite parent, Konsultation konsultation) {
			/*
			 * Important: Add all created controls to "controls"
			 *            for later disposal.
			 */
			
			// TODO DEBUG
			debug("Creating Row");

			TableWrapLayout twLayout;
			TableWrapData layoutData;
			
			controls = new ArrayList<Control>();
			
			// header
			
			Composite headerArea = toolkit.createComposite(parent);
			controls.add(headerArea);
			headerArea.setLayoutData(SWTHelper.getFillTableWrapData(COLUMNS, true, 1, false));
			GridLayout gridLayout = new GridLayout(2, false);
			gridLayout.marginLeft = 0;
			gridLayout.marginRight = 0;
			gridLayout.marginTop = 0;
			gridLayout.marginBottom = 0;
			gridLayout.horizontalSpacing = 0;
			gridLayout.verticalSpacing = 0;
			headerArea.setLayout(gridLayout);
			
			hTitle = toolkit.createHyperlink(headerArea, "", SWT.NONE);
			hTitle.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					selectConsultation(WidgetRow.this.konsultation);
				}
			});
			controls.add(hTitle);
			
			lFall = toolkit.createLabel(headerArea, "");
			controls.add(lFall);
			lFall.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL));
			
			// COLUMNS controls for parent
			
			// leftArea containing etf and verticalSeparator
			Composite leftArea = toolkit.createComposite(parent);
			controls.add(leftArea);
			layoutData = SWTHelper.getFillTableWrapData(1, true, 1, false);
			layoutData.valign = TableWrapData.FILL;
			leftArea.setLayoutData(layoutData);
			
			twLayout = SWTHelper.createTableWrapLayout(2, false);
			twLayout.leftMargin = 0;
			twLayout.rightMargin = 0;
			twLayout.topMargin = 0;
			twLayout.bottomMargin = 0;
			twLayout.horizontalSpacing = 0;
			twLayout.verticalSpacing = 0;
			leftArea.setLayout(twLayout);
			
			etf = new EnhancedTextFieldRO(leftArea);
			controls.add(etf);
			toolkit.adapt(etf);
			etf.setLayoutData(SWTHelper.getFillTableWrapData(1, true, 1, false));
			
			leftVerticalSeparator = toolkit.createLabel(leftArea, "", SWT.SEPARATOR | SWT.VERTICAL);
			controls.add(leftVerticalSeparator);
			layoutData = SWTHelper.getFillTableWrapData(1, false, 1, false);
			layoutData.valign = TableWrapData.FILL;
			// work-around: default height of separators is 64
			layoutData.maxHeight = 0;
			leftVerticalSeparator.setLayoutData(layoutData);

			// rightArea containing verticalSeparator and verrechnung
			Composite rightArea = toolkit.createComposite(parent);
			controls.add(rightArea);
			layoutData = SWTHelper.getFillTableWrapData(1, true, 1, false);
			layoutData.valign = TableWrapData.FILL;
			rightArea.setLayoutData(layoutData);
			
			twLayout = SWTHelper.createTableWrapLayout(2, false);
			twLayout.leftMargin = 0;
			twLayout.rightMargin = 0;
			twLayout.topMargin = 0;
			twLayout.bottomMargin = 0;
			twLayout.horizontalSpacing = 0;
			twLayout.verticalSpacing = 0;
			rightArea.setLayout(twLayout);
			
			rightVerticalSeparator = toolkit.createLabel(rightArea, "", SWT.SEPARATOR | SWT.VERTICAL);
			controls.add(rightVerticalSeparator);
			layoutData = SWTHelper.getFillTableWrapData(1, false, 1, false);
			layoutData.valign = TableWrapData.FILL;
			// work-around: default height of separators is 64, that's too heigh
			layoutData.maxHeight = 0;
			rightVerticalSeparator.setLayoutData(layoutData);
			
			verrechnung = toolkit.createText(rightArea, "", SWT.WRAP | SWT.READ_ONLY);
			controls.add(verrechnung);
			verrechnung.setLayoutData(SWTHelper.getFillTableWrapData(1, true, 1, false));

			// separator
			
			horizontalSeparator = toolkit.createLabel(parent, "", SWT.SEPARATOR | SWT.HORIZONTAL);
			controls.add(horizontalSeparator);
			horizontalSeparator.setLayoutData(SWTHelper.getFillTableWrapData(COLUMNS, true, 1, false));

			this.konsultation = konsultation;
		}
		
		void refresh() {
			// TODO DEBUG
			debug("Refreshing Row");


			String lineSeparator = System.getProperty("line.separator");
			
			hTitle.setText(konsultation.getLabel());
			lFall.setText(konsultation.getFall().getLabel());

			String text = konsultation.getEintrag().getHead();
			if (text == null) {
				text = "";
			}
			etf.setText(text);

			List<Verrechnet> leistungen = konsultation.getLeistungen();
			List<String> leistungenLabels = replaceBlocks(leistungen);
			
			StringBuffer sb = new StringBuffer();
			boolean isFirst = true;
			for (String leistungLabel : leistungenLabels) {
				if (isFirst) {
					isFirst = false;
				} else {
					sb.append(lineSeparator);
				}
				sb.append(leistungLabel);
			}

			verrechnung.setText(sb.toString());
		}
		
		public void setVerticalSeparatorVisibility() {
			// only show one separator (layout work-around)
			int height1 = leftVerticalSeparator.getSize().y;
			int height2 = rightVerticalSeparator.getSize().y;
			if (height1 >= height2) {
				leftVerticalSeparator.setVisible(true);
				rightVerticalSeparator.setVisible(false);
			} else {
				leftVerticalSeparator.setVisible(false);
				rightVerticalSeparator.setVisible(true);
			}
			
			System.err.println("heights: " + height1 + ", " + height2);
		}
		
		private List<String> replaceBlocks(List<Verrechnet> leistungen) {
			// TODO DEBUG
			debug("Replacing Blocks");

			List<String> labels = new ArrayList<String>();

			List<Verrechnet> unassigned = new ArrayList<Verrechnet>();
			unassigned.addAll(leistungen);
			List<Verrechnet> assigned = new ArrayList<Verrechnet>();
			
			// TODO consider number of elements in blocks
			/*
			Query<Leistungsblock> query = new Query<Leistungsblock>(Leistungsblock.class);
			query.orderBy(false, "Name");
			List<Leistungsblock> blocks = query.execute();
			if (blocks != null) {
				for (Leistungsblock block : blocks) {
					if (containsBlock(unassigned, block)) {
						removeBlock(unassigned, block);
						// TODO sum
						labels.add(block.getName());
					}
				}
			}
			*/

			// add remaining leistungen
			for (Verrechnet leistung : unassigned) {
				labels.add(verrechnetLabelProvider.getText(leistung));
			}
			
			return labels;
		}
		
		/*
		private boolean compareVerrechenbar(IVerrechenbar v1, IVerrechenbar v2) {
			final Comparator comparator = new IVerrechenbar.DefaultComparator();
			
			int result = comparator.compare(v1, v2); 
			return (result == 0);
		}

		private boolean containsBlock(List<Verrechnet> leistungen, Leistungsblock block) {
			for (ICodeElement element : block.getElements()) {
				if (element instanceof IVerrechenbar) {
					IVerrechenbar blockVerrechenbar = (IVerrechenbar) element;
					
					for (Verrechnet leistung : leistungen) {
						IVerrechenbar verrechenbar = leistung.getVerrechenbar();
						
						if (!compareVerrechenbar(blockVerrechenbar, verrechenbar)) {
							return false;
						}
					}
				}
			}
			
			return true;
		}
		
		private void removeBlock(List<Verrechnet> leistungen, Leistungsblock block) {
			List<Verrechnet> removed = new ArrayList<Verrechnet>();
			
			for (ICodeElement element : block.getElements()) {
				if (element instanceof IVerrechenbar) {
					IVerrechenbar blockVerrechenbar = (IVerrechenbar) element;
					
					for (Verrechnet leistung : leistungen) {
						IVerrechenbar verrechenbar = leistung.getVerrechenbar();
						
						if (compareVerrechenbar(blockVerrechenbar, verrechenbar)) {
							removed.add(leistung);
							// continue with next block element
							break;
						}

				}
			}
		}
		*/
		
		void dispose() {
			// dispose all used controls
			for (Control control : controls) {
				if (control != null) {
					control.dispose();
				}
			}
			controls.clear();
			
			konsultation = null;
		}
	}
	
	class KonsLoader extends BackgroundJob {
		String name;
		Patient patient = null;
		List<Konsultation> konsultationen = new ArrayList<Konsultation>();
		
		public KonsLoader() {
			super("KonsLoader");
		}

		public void setPatient(Patient patient) {
			this.patient = patient;
			
			invalidate();
		}
		
		public IStatus execute(IProgressMonitor monitor) {
			synchronized (konsultationen) {
				konsultationen.clear();

				// TODO DEBUG
				System.err.println("START lade kons");
				
				if (patient != null) {
					// TODO DEBUG
					System.err.println("START lade kons " + patient.getLabel());
					
					Fall[] faelle = patient.getFaelle();
					for (Fall fall : faelle) {
						// TODO DEBUG
						System.err.println("LADE fall " + fall.getLabel());
						
						Konsultation[] kons = fall.getBehandlungen(false);
						for (Konsultation k : kons) {
							konsultationen.add(k);
						}
					}
				}
				
				monitor.worked(1);

				// TODO DEBUG
				System.err.println("SORT");

				Collections.sort(konsultationen, new Comparator<Konsultation>() {
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

				// TODO DEBUG
				System.err.println("FINISHED");

				monitor.worked(1);
				monitor.done();
				result = konsultationen;
				return Status.OK_STATUS;
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
