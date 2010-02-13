/*******************************************************************************
 * Copyright (c) 2008-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ScriptView.java 6130 2010-02-13 06:05:17Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.actions.RestrictedAction;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.data.Script;
import ch.elexis.scripting.ScriptEditor;
import ch.elexis.util.PersistentObjectDragSource2;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ViewMenus;
import ch.rgw.tools.ExHandler;

/**
 * Display and edit Beanshell-Scripts
 * 
 * @author gerry
 * 
 */
public class ScriptView extends ViewPart {
	public static final String ID = "ch.elexis.scriptsView"; //$NON-NLS-1$
	private IAction newScriptAction, editScriptAction, removeScriptAction,
			execScriptAction;
	TableViewer tv;
	ScrolledForm form;

	public ScriptView() {

	}

	@Override
	public void createPartControl(Composite parent) {
		form = Desk.getToolkit().createScrolledForm(parent);
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		form.getBody().setLayout(new FillLayout());
		tv = new TableViewer(form.getBody(), SWT.SINGLE | SWT.FULL_SELECTION);
		tv.setContentProvider(new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {
				return Script.getScripts().toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		});
		tv.setLabelProvider(new LabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof Script) {
					return ((Script) element).getLabel();
				} else {
					return element.toString();
				}
			}

		});
		new PersistentObjectDragSource2(tv);
		makeActions();
		ViewMenus menu = new ViewMenus(getViewSite());
		menu.createToolbar(newScriptAction);
		menu.createViewerContextMenu(tv, editScriptAction, execScriptAction,
				null, removeScriptAction);
		tv.setInput(this);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private void makeActions() {
		newScriptAction = new RestrictedAction(
				AccessControlDefaults.SCRIPT_EDIT, Messages
						.getString("ScriptView.newScriptAction")) { //$NON-NLS-1$
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_NEW));
				setToolTipText(Messages
						.getString("ScriptView.newScriptTooltip")); //$NON-NLS-1$
			}

			@Override
			public void doRun() {
				InputDialog inp = new InputDialog(getSite().getShell(),
						Messages.getString("ScriptView.enterNameCaption"), //$NON-NLS-1$
						Messages.getString("ScriptView.enterNameBody"), null, //$NON-NLS-1$
						null);
				if (inp.open() == Dialog.OK) {
					/* Script n= */Script.create(inp.getValue(), ""); //$NON-NLS-1$
					tv.refresh();
				}
			}

		};
		editScriptAction = new RestrictedAction(
				AccessControlDefaults.SCRIPT_EDIT, Messages
						.getString("ScriptView.editScriptAction")) { //$NON-NLS-1$
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_EDIT));
				setToolTipText(Messages
						.getString("ScriptView.editScriptTooltip")); //$NON-NLS-1$
			}

			@Override
			public void doRun() {
				IStructuredSelection sel = (IStructuredSelection) tv
						.getSelection();
				if (sel != null && sel.size() != 0) {
					Script script = (Script) sel.getFirstElement();
					ScriptEditor sce = new ScriptEditor(getSite().getShell(),
							script.getString(), script.getLabel());
					if (sce.open() == Dialog.OK) {
						script.putString(sce.getScript());
					}
				}

			}
		};
		removeScriptAction = new RestrictedAction(
				AccessControlDefaults.SCRIPT_EDIT, Messages
						.getString("ScriptView.deleteScriptAction")) { //$NON-NLS-1$
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_DELETE));
				setToolTipText(Messages
						.getString("ScriptView.deleteScriptTooltip")); //$NON-NLS-1$
			}

			@Override
			public void doRun() {
				IStructuredSelection sel = (IStructuredSelection) tv
						.getSelection();
				if (sel != null && sel.size() != 0) {
					Script script = (Script) sel.getFirstElement();
					script.delete();
					tv.refresh();
				}
			}
		};
		execScriptAction = new RestrictedAction(
				AccessControlDefaults.SCRIPT_EXECUTE, Messages
						.getString("ScriptView.executeScriptAction")) { //$NON-NLS-1$
			{
				setImageDescriptor(Desk.getImageDescriptor(Desk.IMG_GOFURTHER));
				setToolTipText(Messages
						.getString("ScriptView.executeScriptTooltip")); //$NON-NLS-1$
			}

			@Override
			public void doRun() {
				IStructuredSelection sel = (IStructuredSelection) tv
						.getSelection();
				if (sel != null && sel.size() != 0) {
					Script script = (Script) sel.getFirstElement();
					try {
						Object ret = script.execute();
						SWTHelper
								.showInfo(
										Messages
												.getString("ScriptView.ScriptOutput"), ret.toString()); //$NON-NLS-1$
					} catch (Exception ex) {
						ExHandler.handle(ex);
					}
				}
			}
		};
	}
}
