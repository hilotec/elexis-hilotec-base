/*
 * ProblemsTable
 */

package org.iatrix.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.iatrix.data.Problem;

/**
 *
 * Description of ProblemsTable
 *
 * @author danlutz@watz.ch
 */
public class ProblemsTable {
    static final int DATE_COLUMN_INDEX = 0;
    static final int NUMBER_COLUMN_INDEX = 1;
    static final int DESCRIPTION_COLUMN_INDEX = 2;
    static final int PROCEDURE_COLUMN_INDEX = 3;
    static final int DEBTOR_COLUMN_INDEX = 4;
    static final int BILLING_COLUMN_INDEX = 5;
    static final int STATE_COLUMN_INDEX = 6;
    
    static final String[] COLUMN_NAMES = {
            "Year", //$NON-NLS-1$
            "ProblemNumber", //$NON-NLS-1$
            "Description", //$NON-NLS-1$
            "Procedure", //$NON-NLS-1$
            "Debtor", //$NON-NLS-1$
            "Billing", //$NON-NLS-1$
            "State" //$NON-NLS-1$
    };
    
    private TableViewer problemsTableViewer;
    private Composite mainArea;
    
    private List listeners = new ArrayList();
    
    private List managedItems = new ArrayList();

    public ProblemsTable(Composite parent) {
        mainArea = new Composite(parent, SWT.NONE);
        mainArea.setLayout(new FillLayout());
        
        Table problemsTable = new Table(mainArea, SWT.SINGLE | SWT.FULL_SELECTION);
        //problemsTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        problemsTable.setHeaderVisible(true);
        problemsTable.setLinesVisible(true);

        // Note: the columns must be defined in sync with the COLUMN_INDEX constants
        TableColumn column;
        column = new TableColumn(problemsTable, SWT.LEFT);
        column.setText(IatrixMessages.getString(COLUMN_NAMES[DATE_COLUMN_INDEX]));
        column = new TableColumn(problemsTable, SWT.LEFT);
        column.setText(IatrixMessages.getString(COLUMN_NAMES[NUMBER_COLUMN_INDEX]));
        column = new TableColumn(problemsTable, SWT.LEFT);
        column.setText(IatrixMessages.getString(COLUMN_NAMES[DESCRIPTION_COLUMN_INDEX]));
        column = new TableColumn(problemsTable, SWT.LEFT);
        column.setText(IatrixMessages.getString(COLUMN_NAMES[PROCEDURE_COLUMN_INDEX]));
        column = new TableColumn(problemsTable, SWT.LEFT);
        column.setText(IatrixMessages.getString(COLUMN_NAMES[DEBTOR_COLUMN_INDEX]));
        column = new TableColumn(problemsTable, SWT.LEFT);
        column.setText(IatrixMessages.getString(COLUMN_NAMES[BILLING_COLUMN_INDEX]));
        column = new TableColumn(problemsTable, SWT.LEFT);
        column.setText(IatrixMessages.getString(COLUMN_NAMES[STATE_COLUMN_INDEX]));
        
        
        /*
        TableLayout layout = new TableLayout();
        ColumnLayoutData cld;
        
        cld = new ColumnPixelData(10);  // ProblemNumber
        layout.addColumnData(cld);
        cld = new ColumnPixelData(10);  // Date
        layout.addColumnData(cld);

        cld = new ColumnWeightData(60);  // Description
        layout.addColumnData(cld);
        cld = new ColumnWeightData(40);  // Procedure
        layout.addColumnData(cld);
        
        cld = new ColumnPixelData(30);  // Debtor
        layout.addColumnData(cld);
        
        cld = new ColumnPixelData(10);  // State
        layout.addColumnData(cld);

        problemsTable.setLayout(layout);
        
        problemsTable.layout(true);
        */
        
        //problemsTable.setLayout(new ProblemsTableLayout());
        //problemsTable.layout(true);
        
        
        
        problemsTableViewer = new TableViewer(problemsTable);
        
        problemsTableViewer.setContentProvider(new ProblemsTableContentProvider());
        problemsTableViewer.setLabelProvider(new ProblemLabelProvider());
        problemsTableViewer.setColumnProperties(COLUMN_NAMES);

        
        packProblemsTable();

/*
        // Create the cell editors
        
        CellEditor[] editors = new CellEditor[COLUMN_NAMES.length];
        
        editors[STATE_COLUMN_INDEX] = new CheckboxCellEditor(problemsTable);
        editors[NUMBER_COLUMN_INDEX] = null;
        editors[DATE_COLUMN_INDEX] = new TextCellEditor(problemsTable);
        editors[DESCRIPTION_COLUMN_INDEX] = new TextCellEditor(problemsTable);
        editors[PROCEDURE_COLUMN_INDEX] = new TextCellEditor(problemsTable);
        editors[DEBTOR_COLUMN_INDEX] = null;

        problemsTableViewer.setCellEditors(editors);
        
        // Set the cell modifier for the viewer
        problemsTableViewer.setCellModifier(new CellModifier());
*/
        
        problemsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                ProblemsTable.this.selectionChanged(event);
            }
        });
    }
    
    public void addListener(Listener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(Listener listener) {
        if (listeners.remove(listener));
    }

    private List prepareInput(List problems) {
        List flatProblems = new ArrayList();
        addItemsToInput(flatProblems, problems, "");
        return flatProblems;
    }
    
    private void addItemsToInput(List input, List problems, String numberPrefix) {
        if (problems != null) {
            int currentNumber = 1;
            for (Iterator it = problems.iterator(); it.hasNext();) {
                Problem problem = (Problem) it.next();

                ProblemsTableElement element = new ProblemsTableElement(
                        problem, numberPrefix + currentNumber);
                input.add(element);

                ++currentNumber;
            }
        }
    }

    public void setInput(List problems) {
        managedItems = prepareInput(problems);
        
        problemsTableViewer.setInput(managedItems);
        
        packProblemsTable();

    }
    
    private void packProblemsTable() {
        // TODO deprecated
        
        Table problemsTable = problemsTableViewer.getTable();
        int width = problemsTable.getSize().x;
        for (int i = 0; i < problemsTable.getColumnCount(); i++) {
            TableColumn column = problemsTable.getColumn(i);
            column.pack();
            int w = column.getWidth();
            width -= column.getWidth();
        }
        
        /*TableColumn descriptionColumn = problemsTable.getColumn(2);
        TableColumn procedereColumn = problemsTable.getColumn(3);
        if (width > 0) {
            int w1 = width / 2;
            int w2 = width - w1;
            descriptionColumn.setWidth(descriptionColumn.getWidth() + w1);
            procedereColumn.setWidth(procedereColumn.getWidth() + w2);
        }*/

    }

    public void selectionChanged(SelectionChangedEvent event) {
        // TODO support listeners, inform listeners
        
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        Problem problem = ((ProblemsTableElement) selection.getFirstElement()).getProblem();
        
        // notify listeners
        for (Iterator it = listeners.iterator(); it.hasNext(); ) {
            Listener listener = (Listener) it.next();
            listener.selectionChanged(problem);
        }
    }
    
    int getColumnIndex(String columnName) {
        int index = -1;
        for (int i = 0; i < COLUMN_NAMES.length; ++i) {
            if (columnName.equals(COLUMN_NAMES[i])) {
                index = i;
                break;
            }
        }
        
        return index;
    }

    class ProblemsTableElement {
        private Problem problem;
        private String number;
        
        public ProblemsTableElement(Problem problem, String number) {
            this.problem = problem;
            this.number = number;
        }
        
        public Problem getProblem() {
            return problem;
        }
        
        public String getNumber() {
            return number;
        }
    }
    
    class ProblemsTableContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof List) {
                List list = (List) inputElement;
                return list.toArray();
            } else {
                return null;
            }
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }


    }

    class ProblemLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String  getText(Object element) {
            return getColumnText(element, ProblemsTable.DESCRIPTION_COLUMN_INDEX);
        }
        
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            if (!(element instanceof ProblemsTableElement)) {
                return null;
            }
            
            ProblemsTableElement problemsTableElement = (ProblemsTableElement) element;
            Problem problem = problemsTableElement.getProblem();
            switch (columnIndex) {
            case ProblemsTable.STATE_COLUMN_INDEX:
                return IatrixMessages.getString(problem.getStatusText());
            case ProblemsTable.NUMBER_COLUMN_INDEX:
               return problemsTableElement.getNumber();
            case ProblemsTable.DATE_COLUMN_INDEX:
                return problem.getDatum();
            case ProblemsTable.DESCRIPTION_COLUMN_INDEX:
                return problem.getBezeichnung();
            case ProblemsTable.PROCEDURE_COLUMN_INDEX:
                // TODO PROCEDERE
                return "";
            case ProblemsTable.DEBTOR_COLUMN_INDEX:
                // TODO DEBTOR
                return "";
            case ProblemsTable.BILLING_COLUMN_INDEX:
                // TODO DEBTOR
                return "";
            default:
                return null;
            }
        }

        //public void dispose() {
        //}
    }
/*
    class CellModifier implements ICellModifier {
        public CellModifier() {
        }
        
        public boolean canModify(Object element, String property) {
            int index = getColumnIndex(property);
            
            switch (index) {
            case STATE_COLUMN_INDEX:
                return true;
            case NUMBER_COLUMN_INDEX:
                return false;
            case DATE_COLUMN_INDEX:
                return true;
            case DESCRIPTION_COLUMN_INDEX:
                return true;
            case PROCEDURE_COLUMN_INDEX:
                return true;
            case DEBTOR_COLUMN_INDEX:
                return false;
            case BILLING_COLUMN_INDEX:
                return false;
            }
            
            return false;
        }
        
        public Object getValue(Object element, String property) {
            Problem problem = ((ProblemsTableElement) element).getProblem();
            int index = getColumnIndex(property);
            
            switch (index) {
            case STATE_COLUMN_INDEX:
                return (problem.getState() == Problem.ACTIVE ? new Boolean(true) : new Boolean(false));
            case NUMBER_COLUMN_INDEX:
                return ((ProblemsTableElement) element).getNumber();
            case DATE_COLUMN_INDEX:
                return problem.getDate();
            case DESCRIPTION_COLUMN_INDEX:
                return problem.getDescription();
            case PROCEDURE_COLUMN_INDEX:
                // TODO Procedure
                return "";
            case DEBTOR_COLUMN_INDEX:
                // TODO Debtor
                return "";
            case BILLING_COLUMN_INDEX:
                // TODO Debtor
                return "";
            }
            
            return null;
        }
        
        public void modify(Object element, String property, Object value) {
           // TODO modify
            
            int index = getColumnIndex(property);
            
            if (element instanceof Item) {
                element = ((Item) element).getData();
            }
            
            ProblemsTableElement pte = (ProblemsTableElement) element;
            Problem problem = pte.getProblem();
            String valueString;

            switch (index) {
            case STATE_COLUMN_INDEX:
                if (((Boolean) value).booleanValue()) {
                    problem.setState(Problem.ACTIVE);
                } else {
                    problem.setState(Problem.INACTIVE);
                }
                break;
            case DATE_COLUMN_INDEX:
                // TODO
                break;
            case DESCRIPTION_COLUMN_INDEX:
                // TODO
                break;
            case PROCEDURE_COLUMN_INDEX:
                // TODO
                break;
            case BILLING_COLUMN_INDEX:
                // TODO
                break;
            }
            
            try {
                problem.store();
            } catch (WorkingetorixException e) {
                // TODO what to do?
                e.printStackTrace();
            }
            
            problemsTableViewer.update(pte, null);

        }
    }
*/
    
    public static class Listener {
        public void selectionChanged(Problem problem) {
            // do nothing
        }
    }
}
