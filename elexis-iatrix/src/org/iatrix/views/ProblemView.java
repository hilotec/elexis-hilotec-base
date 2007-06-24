/*******************************************************************************
 * Copyright (c) 2005, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Lutz - initial implementation
 *    Gerry Weirich - angepasst an neues Rezeptmodell
 *    
 *  $Id: ProblemView.java 2366 2007-05-14 09:13:06Z danlutz $
 *******************************************************************************/

package org.iatrix.views;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;
import org.iatrix.data.Problem;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.data.Artikel;
import ch.elexis.data.IDiagnose;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Rezept;
import ch.elexis.dialogs.MediDetailDialog;
import ch.elexis.util.DynamicListDisplay;
import ch.elexis.util.LabeledInputField;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;
import ch.elexis.util.DynamicListDisplay.DLDListener;
import ch.elexis.views.RezeptBlatt;
import ch.elexis.views.codesystems.DiagnosenView;
import ch.elexis.views.codesystems.LeistungenView;
import ch.rgw.tools.ExHandler;

/**
 * View for editing Problem properties.
 * 
 * TODO When a Prescription is removed from the Patient's Prescriptions (e. g.
 *      in the PatientDetailView), the Prescription should be removed from the
 *      Problem, too. Is there an event available for this? 
 * 
 * @author danlutz
 */

public class ProblemView extends ViewPart implements GlobalEvents.SelectionListener, ActivationListener, ISaveablePart2 {
    public static final String ID="org.iatrix.views.ProblemView";
    
    static Log log = Log.get("Problemliste");
    
    private Problem actProblem;

    private FormToolkit tk;
    private ScrolledForm form;
    
	private final static String[] lbSimple = {
		"Problem/Diagnose",
		"Nummer",
		"Datum"
	};
	private final static String[] dfSimple = {
		"Bezeichnung",
		"Nummer",
		"Datum"
		};
	private Text[] txSimple=new Text[lbSimple.length];
	
	private DynamicListDisplay dlDauerMedi;

    private TableViewer diagnosenViewer;
    private TableViewer konsultationenViewer;
    
    /* diagnosenViewer */
    private IAction delDiagnoseAction;
    
    /* konsultationenViewer */
    private IAction unassignProblemAction;

    private ViewMenus menus;

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());
        Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new FillLayout());

        tk=Desk.theToolkit;
        form=tk.createScrolledForm(main);
		form.getBody().setLayout(new GridLayout(1, true));

		// Tableau
		
        LabeledInputField.Tableau tblProblem = new LabeledInputField.Tableau(form.getBody());
        tblProblem.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
        int tl = txSimple.length;
		for (int i = 0; i < tl; i++) {
		    txSimple[i] = (Text)tblProblem.addComponent(lbSimple[i]).getControl();
            txSimple[i].addFocusListener(new Focusreact(dfSimple[i]));
        }

        SashForm mainSash = new SashForm(form.getBody(), SWT.VERTICAL);
        mainSash.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));

        Composite dauermedikationComposite = tk.createComposite(mainSash);
        Composite bottomComposite = tk.createComposite(mainSash);
        
        mainSash.setWeights(new int[] {25, 75});

		// Dauermedikation
        
        dauermedikationComposite.setLayout(new GridLayout());
		
        Label lDauermedikation = tk.createLabel(dauermedikationComposite, "Fixmedikation");
        lDauermedikation.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
        
		dlDauerMedi=new DynamicListDisplay(dauermedikationComposite, SWT.NONE,new DLDListener(){
			public boolean dropped(PersistentObject dropped) {
				if (dropped instanceof Artikel) {
					if (actProblem != null) {
						Artikel artikel = (Artikel) dropped;
						Patient patient = actProblem.getPatient();

						// create Prescription and add it to the Problem
						Prescription prescription = new Prescription(artikel, patient, "", "");
						actProblem.addPrescription(prescription);
						
						// Let the user set the Prescription properties
						
						MediDetailDialog dlg = new MediDetailDialog(dlDauerMedi.getShell(), prescription);
						if (dlg.open() == Dialog.OK) {
							dlDauerMedi.add(prescription);
							form.reflow(true);
						}
						
                        // tell other viewers that something has changed
                        GlobalEvents.getInstance().fireObjectEvent(actProblem, GlobalEvents.CHANGETYPE.update);
                        
						return true;
					}
				}
				
				return false;
			}

			public void hyperlinkActivated(String l) {
				if (actProblem == null) {
					return;
				}
				
				try{
					if(l.equals("Hinzu... ")){
						getViewSite().getPage().showView(LeistungenView.ID);
					}else if(l.equals("Liste... ")){
						RezeptBlatt rpb=(RezeptBlatt)getViewSite().getPage().showView(RezeptBlatt.ID);
						rpb.createEinnahmeliste(actProblem.getPatient(),(Prescription[])dlDauerMedi.getAll().toArray(new Prescription[0]));
					}else if(l.equals("Rezept... ")){
						Rezept rp=new Rezept(actProblem.getPatient());
					
						
						for(Prescription p:(Prescription[])dlDauerMedi.getAll().toArray(new Prescription[0])){
							rp.addPrescription(p);
							/*
							 * *	ich habe das geändert weil ich die Rezepte geändert habe. RpZeile ist nicht mehr public. gw,
							rp.addLine(new RpZeile("1",p.getArtikel().getLabel(),"",
									p.getDosis(),p.getBemerkung()));
									*/
						}
						RezeptBlatt rpb=(RezeptBlatt)getViewSite().getPage().showView(RezeptBlatt.ID);
						rpb.createRezept(rp);
					}
				}catch(Exception ex){
					ExHandler.handle(ex);
				}
                // tell other viewers that something has changed
                GlobalEvents.getInstance().fireObjectEvent(actProblem, GlobalEvents.CHANGETYPE.update);

			}});
        dlDauerMedi.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		dlDauerMedi.addHyperlinks("Hinzu... ","Liste... ","Rezept... ");
		Menu mDauerMedi=new Menu(dlDauerMedi);
		MenuItem mDauerMediRemove=new MenuItem(mDauerMedi,SWT.NONE);
		mDauerMediRemove.setText("Löschen");
		mDauerMediRemove.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (actProblem != null) {
					Prescription pr=(Prescription) dlDauerMedi.getSelection();
					if(pr!=null){
						dlDauerMedi.remove(pr);
						actProblem.removePrescription(pr);
						pr.delete();
					}
                    // tell other viewers that something has changed
                    GlobalEvents.getInstance().fireObjectEvent(actProblem, GlobalEvents.CHANGETYPE.update);
				}
			}
		});
		MenuItem mDauerMediChange=new MenuItem(mDauerMedi,SWT.NONE);
		mDauerMediChange.setText("Ändern");
		mDauerMediChange.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				Prescription pr=(Prescription) dlDauerMedi.getSelection();
				if(pr!=null){
					new MediDetailDialog(dlDauerMedi.getShell(),pr).open();
					dlDauerMedi.redraw();
				}
			}
			
		});
		dlDauerMedi.setMenu(mDauerMedi);
		
		bottomComposite.setLayout(new GridLayout());
		
        SashForm bottomSash = new SashForm(bottomComposite, SWT.HORIZONTAL);
        bottomSash.setLayoutData(SWTHelper.getFillGridData(1, true, 1,
                true));

        Composite diagnosenComposite = tk.createComposite(bottomSash);
        Composite konsultationenComposite = tk.createComposite(bottomSash);
        
        bottomSash.setWeights(new int[] {25, 75});
        
        diagnosenComposite.setLayout(new GridLayout(1, true));

        Hyperlink hDiagnosen = tk.createHyperlink(diagnosenComposite, "Diagnosen",
                SWT.NONE);
        hDiagnosen
                .setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
        hDiagnosen.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent e) {
                try {
                    getViewSite().getPage().showView(DiagnosenView.ID);
                } catch (Exception ex) {
                    ExHandler.handle(ex);
                    log.log("Fehler beim Starten des Diagnosencodes "
                            + ex.getMessage(), Log.ERRORS);
                }
            }
        });

        Table diagnosenTable = tk.createTable(diagnosenComposite, SWT.SINGLE);
        diagnosenTable.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
        diagnosenViewer = new TableViewer(diagnosenTable);
        diagnosenViewer.getControl().setLayoutData(
                SWTHelper.getFillGridData(1, true, 1, true));
        diagnosenViewer.setContentProvider(new IStructuredContentProvider() {
            public Object[] getElements(Object inputElement) {
                if (actProblem != null) {
                    List<IDiagnose> diagnosen = actProblem.getDiagnosen();
                    return diagnosen.toArray();
                }
                return new Object[0];
            }

            public void dispose() {
                // nothing to do
            }

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
                // nothing to do
            }
        });
        diagnosenViewer.setLabelProvider(new LabelProvider() {
        	public String getText(Object element) {
        		if (!(element instanceof IDiagnose)) {
        			return "";
        		}
        		
        		IDiagnose diagnose = (IDiagnose) element;
        		return diagnose.getLabel();
            }

        });
        diagnosenViewer.setInput(this);

        konsultationenComposite.setLayout(new GridLayout(1, true));

        Label lKonsultationen = tk.createLabel(konsultationenComposite, "Konsultationen",
                SWT.LEFT);
        lKonsultationen.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

        Table konsultationenTable = tk.createTable(konsultationenComposite, SWT.SINGLE);
        konsultationenTable.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
        konsultationenViewer = new TableViewer(konsultationenTable);
        konsultationenViewer.getControl().setLayoutData(
                SWTHelper.getFillGridData(1, true, 1, true));
        konsultationenViewer.setContentProvider(new IStructuredContentProvider() {
            public Object[] getElements(Object inputElement) {
                if (actProblem != null) {
                    List<Konsultation> konsultationen = actProblem.getKonsultationen();
                    return konsultationen.toArray();
                }
                return new Object[0];
            }

            public void dispose() {
                // nothing to do
            }

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
                // nothing to do
            }
        });
        konsultationenViewer.setLabelProvider(new LabelProvider() {
        	public String getText(Object element) {
        		if (!(element instanceof Konsultation)) {
        			return "";
        		}
        		
        		Konsultation konsultation = (Konsultation) element;
        		return konsultation.getLabel();
            }

        });
        konsultationenViewer.setInput(this);

        
        /* Implementation Drag&Drop */
        
        final TextTransfer textTransfer = TextTransfer.getInstance();
        Transfer[] types = new Transfer[] { textTransfer };

        // diagnosenComposite
        DropTarget dtarget = new DropTarget(diagnosenComposite, DND.DROP_COPY);
        dtarget.setTransfer(types);
        dtarget.addDropListener(new DropTargetListener() {
            public void dragEnter(DropTargetEvent event) {
                /* Wir machen nur Copy-Operationen */
                event.detail = DND.DROP_COPY;
            }

            /* Mausbewegungen mit gedrückter Taste sind uns egal */
            public void dragLeave(DropTargetEvent event) {
                /* leer */
            }

            public void dragOperationChanged(DropTargetEvent event) {
                /* leer */
            }

            public void dragOver(DropTargetEvent event) {
                /* leer */
            }

            /* Erst das Loslassen interessiert uns wieder */
            public void drop(DropTargetEvent event) {
                String drp = (String) event.data;
                String[] dl = drp.split(",");
                for (String obj : dl) {
                    PersistentObject dropped = Hub.poFactory
                            .createFromString(obj);
                    if (dropped instanceof IDiagnose) {
                        IDiagnose diagnose = (IDiagnose) dropped;
                        actProblem.addDiagnose(diagnose);

                        // tell other viewers that something has changed
                        GlobalEvents.getInstance().fireObjectEvent(actProblem, GlobalEvents.CHANGETYPE.update);
                        
                        // update ourselves
                        // TODO: implement ObjectListener
                        diagnosenViewer.refresh();
                    }
                }
            }

            public void dropAccept(DropTargetEvent event) {
                /* leer */
            }
        });


        makeActions();
        menus = new ViewMenus(getViewSite());
        menus.createViewerContextMenu(diagnosenViewer, delDiagnoseAction);
        menus.createViewerContextMenu(konsultationenViewer, unassignProblemAction);

        GlobalEvents.getInstance().addActivationListener(this, this);
        
    }

    @Override
    public void setFocus()
    {
        // TODO Auto-generated method stub

    }
    
	/* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose()
    {
    	GlobalEvents.getInstance().removeSelectionListener(this);
    	GlobalEvents.getInstance().removeActivationListener(this, this);
        super.dispose();
    }

    public void selectionEvent(PersistentObject obj) {
        if ((obj instanceof Problem)) {
            setProblem((Problem) obj);
        }
    }

	public void clearEvent(Class template) {
		if (template.equals(Problem.class)) {
			setProblem(null);
		}
	}
	
    private void makeActions() {
		// Diagnosen
		
		delDiagnoseAction = new Action("Diagnose entfernen") {
			public void run() {
                Object sel = ((IStructuredSelection) diagnosenViewer
                        .getSelection()).getFirstElement();
                if (sel != null && actProblem != null) {
                    IDiagnose diagnose = (IDiagnose) sel;
                	actProblem.removeDiagnose(diagnose);
                	
                	// TODO Diagnosen von Konsultationen entfernen
                    diagnosenViewer.refresh();
                    
                    // tell other viewers that something has changed
                    GlobalEvents.getInstance().fireObjectEvent(actProblem, GlobalEvents.CHANGETYPE.update);
                }
			}
		};
		
		// Konsultationen
		
        unassignProblemAction = new Action("Problem entfernen") {
            {
                setToolTipText("Problem von Konsulation entfernen");
            }

            public void run() {
                Object sel = ((IStructuredSelection) konsultationenViewer
                        .getSelection()).getFirstElement();
                if (sel != null && actProblem != null) {
                    Konsultation konsultation = (Konsultation) sel;

                    actProblem.removeFromKonsultation(konsultation);
                    konsultationenViewer.refresh();

                    // tell other viewers that something has changed
                    GlobalEvents.getInstance().fireObjectEvent(actProblem, GlobalEvents.CHANGETYPE.update);
                }
            }
        };

    }


	public void activation(boolean mode) {
		// do nothing
	}

	public void visible(boolean mode) {
		if (mode == true) {
			setProblem((Problem) GlobalEvents.getInstance().getSelectedObject(Problem.class));
			GlobalEvents.getInstance().addSelectionListener(this);
		} else {
			GlobalEvents.getInstance().removeSelectionListener(this);
		}
	}


    /* ******
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2
	 * Wir benötigen das Interface nur, um das Schliessen einer View zu verhindern,
	 * wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */ 
	public int promptToSaveOnClose() {
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL : ISaveablePart2.NO;
	}
	public void doSave(IProgressMonitor monitor) { /* leer */ }
	public void doSaveAs() { /* leer */}
	public boolean isDirty() {
		return true;
	}
	public boolean isSaveAsAllowed() {
		return false;
	}
	public boolean isSaveOnCloseNeeded() {
		return true;
	}
	
	private void setProblem(Problem problem) {
		actProblem = problem;
		
		if (actProblem != null) {
			form.setText("Problem " + problem.getLabel() + " von " + problem.getPatient().getLabel());

			for(int i = 0; i<txSimple.length; i++) {
				txSimple[i].setText(PersistentObject.checkNull(problem.get(dfSimple[i])));
			}
			form.reflow(true);
		} else {
	            form.setText("Kein Problem ausgewählt");
	            for (int i = 0; i < txSimple.length; i++) {
	                txSimple[i].setText("");
	            }
		}

		diagnosenViewer.refresh();
		konsultationenViewer.refresh();
		
		// Fixmedikation
		
		dlDauerMedi.clear();
		if (actProblem != null) {
			List<Prescription> prescriptions = actProblem.getPrescriptions();
			for(Prescription prescription : prescriptions){
				dlDauerMedi.add(prescription);
			}
		}
		form.reflow(true);

}

	
	// FocusListener fuer Felder
	class Focusreact extends FocusAdapter{
		private String field;
		
		Focusreact(String f) {
			field = f;
		}
		
		@Override
		public void focusLost(FocusEvent e) {
			if(actProblem == null) {
				return;
			}
			
			String oldvalue = actProblem.get(field);
			String newvalue = ((Text) e.getSource()).getText();
			if(oldvalue != null) {
				if (oldvalue.equals(newvalue)) {
					return;
				}
			}
			if(newvalue != null) {
				actProblem.set(field, newvalue);
			}
		}
	}

}
