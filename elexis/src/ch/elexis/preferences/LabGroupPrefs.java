/*******************************************************************************
 * Copyright (c) 2007, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    
 *  $Id$
 *******************************************************************************/

package ch.elexis.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import ch.elexis.data.LabGroup;
import ch.elexis.data.LabItem;
import ch.elexis.data.Query;
import ch.elexis.util.DefaultLabelProvider;
import ch.elexis.util.SWTHelper;

public class LabGroupPrefs extends PreferencePage implements
		IWorkbenchPreferencePage {
	
	private LabGroup actGroup = null;

	private ComboViewer groupsViewer;
	private ListViewer itemsViewer;
	
	public LabGroupPrefs() {
		super("Gruppen");
	}

	protected Control createContents(Composite parent){
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		
		Label label;
		
		Composite topArea = new Composite(composite, SWT.NONE);
		topArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		topArea.setLayout(new GridLayout(3, false));
		
		label = new Label(topArea, SWT.NONE);
		label.setText("Für die Laborverordnung können hier zusätzlich zu den normalen Laborgruppen "
				+ "weitere Gruppen definiert werden, z. B. \"Diabeteskontrolle\".");
		label.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		
		label = new Label(topArea, SWT.NONE);
		label.setText("Gruppe:");
		
		groupsViewer = new ComboViewer(topArea, SWT.READ_ONLY);
		groupsViewer.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		groupsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
		    public void selectionChanged(SelectionChangedEvent event) {
		    	IStructuredSelection sel = (IStructuredSelection) groupsViewer.getSelection();
		    	Object element = sel.getFirstElement();
		    	if (element instanceof LabGroup) {
		    		actGroup = (LabGroup) element;
		    		
		    		itemsViewer.refresh();
		    	}
		    }
		});
		
		groupsViewer.setContentProvider(new GroupsContentProvider());
		groupsViewer.setLabelProvider(new DefaultLabelProvider());
		
		groupsViewer.setInput(this);
		
		Button newButton = new Button(topArea, SWT.PUSH);
		newButton.setText("Neu...");
		newButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				InputDialog dialog = new InputDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"Neue Laborgruppe", "Bitte wählen Sie einen Namen für die Gruppe", "", null);
				int rc = dialog.open();
				if (rc == Window.OK) {
					String name = dialog.getValue();
					LabGroup group = new LabGroup(name, null);
					
					groupsViewer.refresh();
					groupsViewer.setSelection(new StructuredSelection(group));
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		Composite bottomArea = new Composite(composite, SWT.NONE);
		bottomArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		bottomArea.setLayout(new GridLayout(1, false));
		
		label = new Label(bottomArea, SWT.NONE);
		label.setText("Enthaltene Laborwerte:");
		
		itemsViewer = new ListViewer(bottomArea, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		itemsViewer.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		itemsViewer.setContentProvider(new GroupItemsContentProvider());
		itemsViewer.setLabelProvider(new ItemsLabelProvider());
		
		itemsViewer.setInput(this);
		
		Composite buttonArea = new Composite(bottomArea, SWT.NONE);
		buttonArea.setLayoutData(SWTHelper.getFillGridData(1, false, 1, false));
		buttonArea.setLayout(new GridLayout(2, true));
		
		Button newItemButton = new Button(buttonArea, SWT.PUSH);
		newItemButton.setText("Hinzufügen...");
		newItemButton.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		Button removeItemButton = new Button(buttonArea, SWT.PUSH);
		removeItemButton.setText("Entfernen");
		removeItemButton.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		newItemButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (actGroup != null) {
					ItemsSelectionDialog dialog = new ItemsSelectionDialog(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							actGroup);
					if (dialog.open() == ItemsSelectionDialog.OK) {
						itemsViewer.refresh();
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		removeItemButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (actGroup != null) {
					IStructuredSelection sel = (IStructuredSelection) itemsViewer.getSelection();
					for (Object obj : sel.toList()) {
						if (obj instanceof LabItem) {
							LabItem item = (LabItem) obj;
							actGroup.removeItem(item);
						}
					}
					
					itemsViewer.refresh();
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		selectFirstGroup();
		
		return composite;
	}
	
	public void init(IWorkbench workbench) {
		// nothing to do
	}
	
	private void selectFirstGroup() {
		Object element = groupsViewer.getElementAt(0);
		if (element != null) {
			groupsViewer.setSelection(new StructuredSelection(element));
		}
	}
	
	class GroupsContentProvider implements IStructuredContentProvider {
	    public Object[] getElements(Object inputElement) {
	    	Query<LabGroup> query = new Query<LabGroup>(LabGroup.class);
	    	query.orderBy(false, new String[] {"Name"});
	    	
	    	List<LabGroup> groups = query.execute();
	    	if (groups == null) {
	    		groups = new ArrayList<LabGroup>();
	    	}
	    	
	    	return groups.toArray();
	    }
	    
	    public void dispose() {
	    	// nothing to do
	    }
	    
	    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	    	// nothing to do
	    }
	}

	class ItemsLabelProvider extends DefaultLabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof LabItem) {
				LabItem item = (LabItem) element;
				
				StringBuffer sb = new StringBuffer();
				sb.append(item.getGroup());
				sb.append(" - ");
				sb.append(item.get("titel"));
				
				return sb.toString();
			} else {
				return element.toString();
			}
		}
	}

	class GroupItemsContentProvider implements IStructuredContentProvider {
	    public Object[] getElements(Object inputElement) {
	    	if (actGroup != null) {
	    		List<LabItem> items = actGroup.getItems();
	    		return items.toArray();
	    	} else {
	    		return new Object[] {};
	    	}
	    }
	    
	    public void dispose() {
	    	// nothing to do
	    }
	    
	    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	    	// nothing to do
	    }
	}
	
	class ItemsSelectionDialog extends TitleAreaDialog {
		private LabGroup group;
		
		private ListViewer viewer;
		
		ItemsSelectionDialog(Shell parentShell, LabGroup group) {
			super(parentShell);
			this.group = group;
		}
		
		protected Control createContents(Composite parent) {
			Control contents = super.createContents(parent);
			
			setMessage("Bitte wählen Sie die Laborwerte aus.");
			setTitle("Laborwerte wählen");
			
			return contents;
		}
		
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			composite.setLayout(new GridLayout(1, false));
			
			Label label = new Label(composite, SWT.NONE);
			label.setText("Gruppe: " + group.getName());
			
			viewer = new ListViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			viewer.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			
			viewer.setContentProvider(new ItemsContentProvider());
			viewer.setLabelProvider(new ItemsLabelProvider());
			
			viewer.setInput(this);
			
			setSelection();
			
			return composite;
		}
		
		private void setSelection() {
			List<LabItem> items = group.getItems();
			viewer.setSelection(new StructuredSelection(items));
		}
		
		protected void buttonPressed(int buttonId) {
			if (buttonId == OK) {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				List<LabItem> items = new ArrayList<LabItem>();
				for (Object obj : sel.toList()) {
					if (obj instanceof LabItem) {
						LabItem item = (LabItem) obj;
						items.add(item);
					}
				}
				
				group.setItems(items);
			}
			
			setReturnCode(buttonId);
			close();
		}
		
		class ItemsContentProvider implements IStructuredContentProvider {
		    public Object[] getElements(Object inputElement) {
		    	Query<LabItem> query = new Query<LabItem>(LabItem.class);
		    	query.orderBy(false, new String[] {"Gruppe", "prio", "titel"});
		    	
		    	List<LabItem> items = query.execute();
		    	if (items == null) {
		    		items = new ArrayList<LabItem>();
		    	}
		    	
		    	return items.toArray();
		    }
		    
		    public void dispose() {
		    	// nothing to do
		    }
		    
		    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		    	// nothing to do
		    }
		}
	}
}
