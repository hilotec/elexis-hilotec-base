/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: FieldDisplayView.java 5322 2009-05-29 10:59:45Z rgw_ch $
 *******************************************************************************/
package ch.elexis.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.actions.Heartbeat.HeartListener;
import ch.elexis.data.Anwender;
import ch.elexis.data.Mandant;
import ch.elexis.data.PersistentObject;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * This view displays the content of an arbitrary field.
 * 
 * @author gerry
 * 
 */
public class FieldDisplayView extends ViewPart implements ActivationListener,
		SelectionListener, HeartListener {
	public static final String ID = "ch.elexis.dbfielddisplay"; //$NON-NLS-1$
	private IAction newViewAction, editDataAction;
	Text text;
	Class<? extends PersistentObject> myClass;
	String myField;
	boolean bCanEdit;
	ScrolledForm form;
	FormToolkit tk = Desk.getToolkit();
	String subid;
	String NODE = "FeldAnzeige"; //$NON-NLS-1$

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		form = tk.createScrolledForm(parent);
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		form.getBody().setLayout(new GridLayout());
		text = tk.createText(form.getBody(), "", SWT.MULTI | SWT.V_SCROLL); //$NON-NLS-1$
		text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		text.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent arg0) {
				if (bCanEdit) {
					PersistentObject mine = GlobalEvents.getInstance()
							.getSelectedObject(myClass);
					if (mine != null) {
						mine.set(myField, text.getText());
					}
				}
			}

		});
		text.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {
				arg0.doit = bCanEdit;

			}

			public void keyReleased(KeyEvent arg0) {
			}
		});
		makeActions();
		ViewMenus menu = new ViewMenus(getViewSite());
		menu.createToolbar(newViewAction, editDataAction);
		String nx = "Patient.Diagnosen"; //$NON-NLS-1$
		Integer canEdit = null;
		subid = getViewSite().getSecondaryId();
		if (subid == null) {
			subid = "defaultData"; //$NON-NLS-1$
		}
		nx = Hub.userCfg.get("FieldDisplayViewData/" + subid, null); //$NON-NLS-1$
		canEdit = Hub.userCfg.get("FieldDisplayViewCanEdit/" + subid, 0); //$NON-NLS-1$
		setField(nx == null ? "Patient.Diagnosen" : nx, canEdit == null ? false //$NON-NLS-1$
				: (canEdit != 0));
		GlobalEvents.getInstance().addActivationListener(this,
				getViewSite().getPart());
	}

	@Override
	public void dispose() {
		GlobalEvents.getInstance().removeActivationListener(this,
				getViewSite().getPart());
	}

	@Override
	public void setFocus() {
		text.setFocus();
	}

	public void activation(boolean mode) {

	}

	public void visible(boolean mode) {
		if (mode) {
			GlobalEvents.getInstance().addSelectionListener(this);
			Hub.heart.addListener(this);
			heartbeat();
		} else {
			GlobalEvents.getInstance().removeSelectionListener(this);
			Hub.heart.removeListener(this);
		}

	}

	@SuppressWarnings("unchecked")
	public void clearEvent(Class template) {
		if (template.equals(myClass)) {
			text.setText(""); //$NON-NLS-1$
		}
	}

	public void selectionEvent(PersistentObject obj) {
		if (myClass.isInstance(obj)) {
			String val = obj.get(myField);
			if (val == null) {
				SWTHelper.showError(Messages
						.getString("FieldDisplayView.ErrorFieldCaption"), //$NON-NLS-1$
						Messages.getString("FieldDisplayView.ErrorFieldBody") //$NON-NLS-1$
								+ myField);
				text.setText(StringTool.leer);
			} else {
				text.setText(obj.get(myField));
			}
		} else if (obj instanceof Anwender) {
			String nx = Hub.userCfg.get("FieldDisplayViewData/" + subid, null); //$NON-NLS-1$
			Integer canEdit = Hub.userCfg.get("FieldDisplayViewCanEdit/" //$NON-NLS-1$
					+ subid, 0);
			setField(nx == null ? "Patient.Diagnosen" : nx, //$NON-NLS-1$
					canEdit == null ? false : (canEdit != 0));
		}
	}

	public void heartbeat() {
		PersistentObject mine = GlobalEvents.getInstance().getSelectedObject(
				myClass);
		if (mine == null) {
			clearEvent(myClass);
		} else {
			selectionEvent(mine);
		}
	}

	@SuppressWarnings("unchecked")
	private void setField(String field, boolean canEdit) {
		String[] def = field.split("\\."); //$NON-NLS-1$
		if (def.length != 2) {
			SWTHelper.showError(Messages
					.getString("FieldDisplayView.BadDefinitionCaption"), //$NON-NLS-1$
					Messages.getString("FieldDisplayView.BadDefinitionBody")); //$NON-NLS-1$
		} else {
			myClass = resolveName(def[0]);
			if (myClass != null) {
				myField = def[1];
				bCanEdit = canEdit;
				setPartName(myField);
				Hub.userCfg.set("FieldDisplayViewData/" + subid, myClass //$NON-NLS-1$
						.getSimpleName() + "." + myField); //$NON-NLS-1$
				Hub.userCfg.set("FieldDisplayViewCanEdit/" + subid, canEdit); //$NON-NLS-1$

			}
		}
	}

	@SuppressWarnings("unchecked")
	private Class resolveName(String k) {
		Class ret = null;
		if (k.equalsIgnoreCase("Mandant")) { //$NON-NLS-1$
			ret = Mandant.class;
		} else if (k.equalsIgnoreCase("Anwender")) { //$NON-NLS-1$
			ret = Anwender.class;
		} else {
			try {
				String fqname = "ch.elexis.data." + k; //$NON-NLS-1$
				ret = Class.forName(fqname);
			} catch (java.lang.Exception ex) {
				SWTHelper
						.showError(
								Messages
										.getString("FieldDisplayView.WrongTypeCaption"), //$NON-NLS-1$
								Messages
										.getString("FieldDisplayView.WrongTypeBody") + k + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				ret = null;
			}
		}
		return ret;
	}

	private void makeActions() {
		newViewAction = new Action(Messages
				.getString("FieldDisplayView.NewWindow")) { //$NON-NLS-1$
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_ADDITEM));
				setToolTipText(Messages
						.getString("FieldDisplayView.NewWindowToolTip")); //$NON-NLS-1$
			}

			@Override
			public void run() {
				try {
					String fieldtype = new SelectDataDialog().run();
					FieldDisplayView n = (FieldDisplayView) getViewSite()
							.getPage().showView(ID,
									StringTool.unique("DataDisplay"), //$NON-NLS-1$
									IWorkbenchPage.VIEW_VISIBLE);
					n.setField(fieldtype, false);
					heartbeat();
				} catch (PartInitException e) {
					ExHandler.handle(e);
				}
			}
		};
		editDataAction = new Action(Messages
				.getString("FieldDisplayView.DataTypeAction")) { //$NON-NLS-1$
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_EDIT));
				setToolTipText(Messages
						.getString("FieldDisplayView.DataTypeToolTip")); //$NON-NLS-1$
			}

			public void run() {
				SelectDataDialog sdd = new SelectDataDialog();
				if (sdd.open() == Dialog.OK) {
					setField(sdd.result, sdd.bEditable);
					heartbeat();
				}
			}
		};
	}

	class SelectDataDialog extends TitleAreaDialog {
		private final String DATATYPE = Messages
				.getString("FieldDisplayView.DataType"); //$NON-NLS-1$
		String[] nodes;
		Combo cbNodes;
		Button btEditable;
		String result;
		boolean bEditable;

		SelectDataDialog() {
			super(getViewSite().getShell());
		}

		String run() {
			create();
			if (nodes.length > 1) {
				if (open() == Dialog.OK) {
					return result;
				}

			}
			return nodes[0];
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite ret = new Composite(parent, SWT.NONE);
			ret.setLayout(new GridLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			cbNodes = new Combo(ret, SWT.SINGLE);
			cbNodes.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			nodes = Hub.localCfg.get(NODE, "Patient.Diagnosen").split(","); //$NON-NLS-1$ //$NON-NLS-2$
			cbNodes.setItems(nodes);
			btEditable = new Button(ret, SWT.CHECK);
			btEditable.setText(Messages
					.getString("FieldDisplayView.FieldCanBeChanged")); //$NON-NLS-1$
			return ret;
		}

		@Override
		public void create() {
			super.create();
			setTitle(DATATYPE);
			setMessage(Messages.getString("FieldDisplayView.EnterExpression"), //$NON-NLS-1$
					IMessageProvider.INFORMATION);
		}

		@Override
		protected void okPressed() {
			String tx = cbNodes.getText();
			if (StringTool.getIndex(nodes, tx) == -1) {
				String tm = StringTool.join(nodes, ",") + "," + tx; //$NON-NLS-1$ //$NON-NLS-2$
				Hub.localCfg.set(NODE, tm);
			}
			result = tx;
			bEditable = btEditable.getSelection();
			super.okPressed();
		}

	}
}
