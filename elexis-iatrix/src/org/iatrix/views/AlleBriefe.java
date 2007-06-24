/*
 * AlleBriefe
 */

package org.iatrix.views;

import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.data.Brief;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.elexis.views.TextView;
import ch.rgw.tools.ExHandler;

public class AlleBriefe extends ViewPart {
    static Log log = Log.get("AlleBriefe");

    private TableViewer briefeTableViewer;
    
    private Label briefLabel;
    private Label patientKonsLabel;
    private Label patientBriefLabel;
    private Label konsLabel;
    private Label absenderLabel;
    private Label adressatLabel;
    
    @Override
    public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, true));

    	briefeTableViewer = new TableViewer(parent, SWT.SINGLE | SWT.FULL_SELECTION);
    	briefeTableViewer.setContentProvider(new IStructuredContentProvider() {
				public Object[] getElements(Object inputElement) {
					Query<Brief> query = new Query<Brief>(Brief.class);
					List<Brief> list = query.execute();
					return list.toArray();
				}

				public void dispose() {
						// nothing to do
				}

				public void inputChanged(Viewer viewer, Object oldInput,
							Object newInput) {
						// nothing to do
				}
		});
    	briefeTableViewer.setLabelProvider(new ITableLabelProvider() {
    	    public void addListener(ILabelProviderListener listener) {
    		// nothing to do
    	    }

    	    public void removeListener(ILabelProviderListener listener) {
    		// nothing to do
    	    }

    	    public void dispose() {
    		// nothing to do
    	    }

    	    public Image getColumnImage(Object element, int columnIndex) {
    		// not yet implemented
    		return null;
    	    }

    	    public String getColumnText(Object element, int columnIndex) {
    	    	if (!(element instanceof Brief)) {
    	    		return "";
    	    	}

    	    	Brief brief = (Brief) element;
    	    	String id = brief.get("BehandlungsID");
    	    	Konsultation kons = null;
    	    	if (id != null) {
    	    		kons = Konsultation.load(id);
    	    	}
    	    	Patient patient = null;
    	    	if (kons != null) {
    	    		patient = kons.getFall().getPatient();
    	    	} else {
    	    		String id2 = brief.get("PatientID");
    	    		if (id2 != null) {
    	    			patient = Patient.load(id2);
    	    		}
    	    	}
    		

    	    	switch (columnIndex) {
    	    	case 0:
    	    		return brief.getLabel();
    	    	case 1:
    	    		if (patient != null) {
    	    			return patient.getName() + " " + patient.getVorname();
    	    		} else {
    	    			return "null";
    	    		}
    	    	case 2:
    	    		return brief.getTyp();
    	    	default:
    	    		return "";
    	    	}
    	    }

    	    public boolean isLabelProperty(Object element, String property) {
    		return false;
    	    }
    	});

    	Table table = briefeTableViewer.getTable();
    	table.setHeaderVisible(true);
    	table.setLinesVisible(true);

    	TableColumn[] tc = new TableColumn[3];

    	tc[0] = new TableColumn(table, SWT.NONE);
    	tc[0].setText("Brief");
    	tc[0].setWidth(100);

    	tc[1] = new TableColumn(table, SWT.NONE);
    	tc[1].setText("Patient");
    	tc[1].setWidth(100);

    	tc[2] = new TableColumn(table, SWT.NONE);
    	tc[2].setText("?");
    	tc[2].setWidth(30);

    	table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));

    	briefeTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
    		public void selectionChanged(SelectionChangedEvent event) {
    			String briefStr = "x";
    			String patientKonsStr = "x";
    			String patientBriefStr = "x";
    			String konsStr = "x";
    			String absenderStr = "x";
    			String adressatStr = "x";
    			
    			IStructuredSelection sel = (IStructuredSelection) event.getSelection();
    	        Object obj = sel.getFirstElement();
    	        if (obj instanceof Brief) {
    	        	Brief brief = (Brief) obj;
    	        	
    	    		String id;
    	    		
    	    		Konsultation kons = null;
    	    		id = brief.get("BehandlungsID");
    	    		if (id != null) {
    	    			kons = Konsultation.load(id);
    	    		}
    	    		
    	    		Patient patientKons = null;
    	    		if (kons != null) {
    	    			patientKons = kons.getFall().getPatient();
    	    		}
    	    		
    	    		Patient patientBrief = null;
    	    		id = brief.get("PatientID");
    	    		if (id != null) {
    	    			patientBrief = Patient.load(id);;
    	    		}
    	    		
    	    		Kontakt absender = null;
    	    		id = brief.get("AbsenderID");
    	    		if (id != null) {
    	    			absender = Kontakt.load(id);
    	    		}
    	    		
    	    		Kontakt dest = null;
    	    		id = brief.get("DestID");
    	    		if (id != null) {
    	    			dest = Kontakt.load(id);
    	    		}
    	    		
    	        	briefStr = brief.getLabel() + ", " + brief.getTyp() + " [" + brief.getId() + "]";
    	        	if (patientKons != null) {
    	        		patientKonsStr = patientKons.getName() + " " + patientKons.getVorname() + " [" + patientKons.getId() + "]";
    	        	} else {
    	        		patientKonsStr = "null";
    	        	}
    	        	if (patientBrief != null) {
    	        		patientBriefStr = patientBrief.getName() + " " + patientBrief.getVorname() + " [" + patientBrief.getId() + "]";
    	        	} else {
    	        		patientBriefStr = "null";
    	        	}
    	        	if (kons != null) {
    	        		konsStr = kons.getLabel() + ", " + kons.getFall().getLabel() + " [" + kons.getId() + "]";
    	        	} else {
    	        		konsStr = "null";
    	        	}
    	        	if (absender != null) {
    	        		absenderStr = absender.get("Bezeichnung1") + " " + absender.get("Bezeichnung2") + " [" + absender.getId() + "]";
    	        	} else {
    	        		absenderStr = "null";
    	        	}
    	        	if (dest != null) {
    	        		adressatStr = dest.get("Bezeichnung1") + " " + dest.get("Bezeichnung2") + " [" + dest.getId() + "]";
    	        	} else {
    	        		adressatStr = "null";
    	        	}
    	        }
    	        
    	        briefLabel.setText(briefStr);
    	        patientKonsLabel.setText(patientKonsStr);
    	        patientBriefLabel.setText(patientBriefStr);
    	        konsLabel.setText(konsStr);
    	        absenderLabel.setText(absenderStr);
    	        adressatLabel.setText(adressatStr);
    		}
    	});
    	briefeTableViewer.addDoubleClickListener(new IDoubleClickListener() {
    		public void doubleClick(DoubleClickEvent event) {
    			IStructuredSelection sel = (IStructuredSelection) event.getSelection();
    	        Object obj = sel.getFirstElement();
    	        if (obj instanceof Brief) {
    	        	Brief brief = (Brief) obj;
    	        	
    	        	try{
						TextView tv=(TextView)getViewSite().getPage().showView(TextView.ID);
					
						if(tv.openDocument(brief)==false){
							SWTHelper.alert("Fehler","Konnte Text nicht laden");
						}
					}catch(Throwable ex){
						ExHandler.handle(ex);
					}
    	        }
    		}
    	});

    	briefeTableViewer.setInput(this);
    	
    	Composite composite = new Composite(parent, SWT.NONE);
    	composite.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
    	
    	composite.setLayout(new GridLayout(2, false));
    	
    	new Label(composite, SWT.NONE).setText("Brief");
        briefLabel = new Label(composite, SWT.NONE);
        briefLabel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
    	new Label(composite, SWT.NONE).setText("Patient(Kons)");
        patientKonsLabel = new Label(composite, SWT.NONE);
        patientKonsLabel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
    	new Label(composite, SWT.NONE).setText("Patient(Brief)");
        patientBriefLabel = new Label(composite, SWT.NONE);
        patientBriefLabel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
    	new Label(composite, SWT.NONE).setText("Kons");
        konsLabel = new Label(composite, SWT.NONE);
        konsLabel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
    	new Label(composite, SWT.NONE).setText("Absender");
        absenderLabel = new Label(composite, SWT.NONE);
        absenderLabel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
    	new Label(composite, SWT.NONE).setText("Adressat");
        adressatLabel = new Label(composite, SWT.NONE);
        adressatLabel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
	}

    @Override
    public void setFocus() {
	// TODO Auto-generated method stub

    }
}
