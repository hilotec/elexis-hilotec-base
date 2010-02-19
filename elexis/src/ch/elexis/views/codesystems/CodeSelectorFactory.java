/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *  $Id: CodeSelectorFactory.java 6153 2010-02-19 18:34:28Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views.codesystems;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IViewSite;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.StringConstants;
import ch.elexis.actions.CodeSelectorHandler;
import ch.elexis.actions.ElexisEvent;
import ch.elexis.actions.ElexisEventDispatcher;
import ch.elexis.actions.ElexisEventListenerImpl;
import ch.elexis.data.Anwender;
import ch.elexis.data.ICodeElement;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.PersistentObjectFactory;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.Extensions;
import ch.elexis.util.Log;
import ch.elexis.util.PersistentObjectDragSource;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.viewers.CommonViewer;
import ch.elexis.util.viewers.ViewerConfigurer;
import ch.elexis.util.viewers.CommonViewer.DoubleClickListener;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * Bereitstellung der Auswahlliste für Codes aller Art: Oben häufigste des
 * Anwenders, in der Mitte häufigste des Patienten, unten ganze Systenatik
 * 
 * @author Gerry
 * 
 */
public abstract class CodeSelectorFactory implements IExecutableExtension {
	private static final String CAPTION_ERROR = Messages
	.getString("CodeSelectorFactory.error"); //$NON-NLS-1$
	/** Anzahl der in den oberen zwei Listen zu haltenden Elemente */
	public static int ITEMS_TO_SHOW_IN_MFU_LIST = 15;
	
	public CodeSelectorFactory() {
	}
	
	public void setInitializationData(IConfigurationElement config,
		String propertyName, Object data) throws CoreException {
		
	}
	
	public abstract ViewerConfigurer createViewerConfigurer(CommonViewer cv);
	
	public abstract Class<? extends PersistentObject> getElementClass();
	
	public abstract void dispose();
	
	public abstract String getCodeSystemName();
	
	public String getCodeSystemCode() {
		return "999"; //$NON-NLS-1$
	}
	
	public PersistentObject findElement(String code) {
		String s = getElementClass().getName() + StringConstants.DOUBLECOLON
		+ code;
		return Hub.poFactory.createFromString(s);
	}
	
	public static void makeTabs(CTabFolder ctab, IViewSite site, String point) {
		ITEMS_TO_SHOW_IN_MFU_LIST = Hub.userCfg.get(
			PreferenceConstants.USR_MFU_LIST_SIZE, 15);
		java.util.List<IConfigurationElement> list = Extensions
		.getExtensions(point);
		ctab.setSimple(false);
		
		if (list != null) {
			for (IConfigurationElement ic : list) {
				try {
					PersistentObjectFactory po = (PersistentObjectFactory) ic
					.createExecutableExtension("ElementFactory"); //$NON-NLS-1$
					CodeSelectorFactory cs = (CodeSelectorFactory) ic
					.createExecutableExtension("CodeSelectorFactory"); //$NON-NLS-1$
					if (cs == null) {
						SWTHelper.alert(CAPTION_ERROR,
						"CodeSelectorFactory is null"); //$NON-NLS-1$
					}
					ICodeElement ics = (ICodeElement) po.createTemplate(cs
						.getElementClass());
					if (ics == null) {
						SWTHelper.alert(CAPTION_ERROR, "CodeElement is null"); //$NON-NLS-1$
					}
					String cname = ics.getCodeSystemName();
					if (StringTool.isNothing(cname)) {
						SWTHelper.alert(CAPTION_ERROR, "codesystemname"); //$NON-NLS-1$
						cname = "??"; //$NON-NLS-1$
					}
					CTabItem ct = new CTabItem(ctab, SWT.NONE);
					
					ct.setText(cname);
					ct.setData(ics);
					cPage page = new cPage(ctab, site, ics, cs);
					ct.setControl(page);
					
				} catch (CoreException ex) {
					ExHandler.handle(ex);
				}
			}
			if (ctab.getItemCount() > 0) {
				ctab.setSelection(0);
			}
		}
	}
	
	private static class ResizeListener extends ControlAdapter {
		private final String k;
		private final SashForm mine;
		
		ResizeListener(SashForm form, String key) {
			k = key;
			mine = form;
		}
		
		@Override
		public void controlResized(ControlEvent e) {
			int[] weights = mine.getWeights();
			StringBuilder v = new StringBuilder();
			v.append(Integer.toString(weights[0]))
			.append(",").append(Integer.toString(weights[1])) //$NON-NLS-1$
			.append(",").append(Integer.toString(weights[2])); //$NON-NLS-1$
			Hub.localCfg.set(k, v.toString());
			
		}
		
	}
	
	public static class cPage extends Composite {
		IViewSite site;
		ICodeElement template;
		java.util.List<String> lUser, lPatient;
		ArrayList<PersistentObject> alPatient;
		ArrayList<PersistentObject> alUser;
		List lbPatient, lbUser;
		CommonViewer cv;
		ViewerConfigurer vc;
		int[] sashWeights = null;
		ResizeListener resizeListener;
		
		private final ElexisEventListenerImpl eeli_user = new ElexisEventListenerImpl(
			Anwender.class, ElexisEvent.EVENT_USER_CHANGED) {
			
			public void runInUi(ElexisEvent ev) {
				lbPatient.setFont(Desk
					.getFont(PreferenceConstants.USR_DEFAULTFONT));
				lbUser.setFont(Desk
					.getFont(PreferenceConstants.USR_DEFAULTFONT));
				cv.getViewerWidget().getControl().setFont(
					Desk.getFont(PreferenceConstants.USR_DEFAULTFONT));
				refresh();
			}
		};
		
		/*
		private ElexisEventListenerImpl eeli_pat = new ElexisEventListenerImpl(
				Patient.class) {

			public void runInUi(ElexisEvent ev) {
				refresh();

			}
		};
		 */
		
		protected cPage(CTabFolder ctab) {
			super(ctab, SWT.NONE);
		}
		
		cPage(final CTabFolder ctab, final IViewSite s, final ICodeElement v,
			final CodeSelectorFactory cs) {
			super(ctab, SWT.NONE);
			template = v;
			site = s;
			setLayout(new FillLayout());
			SashForm sash = new SashForm(this, SWT.VERTICAL | SWT.SMOOTH);
			String cfgKey = "ansicht/codesystem/" + v.getCodeSystemName(); //$NON-NLS-1$
			resizeListener = new ResizeListener(sash, cfgKey);
			String sashW = Hub.localCfg.get(cfgKey, "20,20,60"); //$NON-NLS-1$
			sashWeights = new int[3];
			int i = 0;
			for (String sw : sashW.split(",")) { //$NON-NLS-1$
				sashWeights[i++] = Integer.parseInt(sw);
			}
			Group gUser = new Group(sash, SWT.NONE);
			gUser.addControlListener(resizeListener);
			gUser.setText(Messages
				.getString("CodeSelectorFactory.yourMostFrequent")); //$NON-NLS-1$
			gUser.setLayout(new FillLayout());
			gUser.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			lbUser = new List(gUser, SWT.MULTI | SWT.V_SCROLL);
			
			Group gPatient = new Group(sash, SWT.NONE);
			gPatient.addControlListener(resizeListener);
			gPatient.setText(Messages
				.getString("CodeSelectorFactory.patientsMostFrequent")); //$NON-NLS-1$
			gPatient.setLayout(new FillLayout());
			gPatient.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			lbPatient = new List(gPatient, SWT.MULTI | SWT.V_SCROLL);
			
			Group gAll = new Group(sash, SWT.NONE);
			gAll.setText(Messages.getString("CodeSelectorFactory.all")); //$NON-NLS-1$
			gAll.setLayout(new GridLayout());
			cv = new CommonViewer();
			Iterable<IAction> actions = v.getActions(null);
			if (actions != null) {
				MenuManager menu = new MenuManager();
				menu.setRemoveAllWhenShown(true);
				menu.addMenuListener(new IMenuListener() {
					public void menuAboutToShow(IMenuManager manager) {
						Iterable<IAction> actions = v.getActions(null);
						for (IAction ac : actions) {
							manager.add(ac);
						}
						
					}
				});
				cv.setContextMenu(menu);
			}
			vc = cs.createViewerConfigurer(cv);
			Composite cvc = new Composite(gAll, SWT.NONE);
			cvc.setLayout(new GridLayout());
			cvc.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			cv.create(vc, cvc, SWT.NONE, this);
			cv.getViewerWidget().getControl().setLayoutData(
				SWTHelper.getFillGridData(1, true, 1, true));
			vc.getContentProvider().startListening();
			
			// add double click listener for CodeSelectorTarget
			cv.addDoubleClickListener(new DoubleClickListener() {
				public void doubleClicked(PersistentObject obj, CommonViewer cv) {
					ICodeSelectorTarget target = CodeSelectorHandler
					.getInstance().getCodeSelectorTarget();
					if (target != null) {
						target.codeSelected(obj);
					}
				}
			});
			
			doubleClickEnable(lbUser);
			doubleClickEnable(lbPatient);
			
			// dragEnable(lbUser);
			// dragEnable(lbPatient);
			new PersistentObjectDragSource(lbUser, new DragEnabler(lbUser));
			new PersistentObjectDragSource(lbPatient, new DragEnabler(
				lbPatient));
			
			try {
				sash.setWeights(sashWeights);
			} catch (Throwable t) {
				ExHandler.handle(t);
				sash.setWeights(new int[] { 20, 20, 60 });
			}
			
			ElexisEventDispatcher.getInstance().addListeners(eeli_user);
			refresh();
		}
		
		/*
		 * (Kein Javadoc)
		 * 
		 * @see org.eclipse.swt.widgets.Widget#dispose()
		 */
		@Override
		public void dispose() {
			vc.getContentProvider().stopListening();
			ElexisEventDispatcher.getInstance().removeListeners(eeli_user);
			super.dispose();
		}
		
		public void refresh() {
			lbUser.removeAll();
			if (Hub.actUser == null) {
				// Hub.log.log("ActUser ist null!", Log.ERRORS);
				return;
			}
			if (template == null) {
				Hub.log
				.log(
					Messages.getString("CodeSelectorFactory.16"), Log.ERRORS); //$NON-NLS-1$
				return;
			}
			lUser = Hub.actUser.getStatForItem(template.getClass().getName());
			alUser = new ArrayList<PersistentObject>();
			lbUser.setData(alUser);
			for (int i = 0; i < ITEMS_TO_SHOW_IN_MFU_LIST; i++) {
				if (i >= lUser.size()) {
					break;
				}
				PersistentObject po = Hub.poFactory.createFromString(lUser
					.get(i));
				alUser.add(po);
				String lbl = po.getLabel();
				if (StringTool.isNothing(lbl)) {
					lbl = "?"; //$NON-NLS-1$
					continue;
				}
				lbUser.add(lbl);
			}
			lbPatient.removeAll();
			
			Patient act = ElexisEventDispatcher.getSelectedPatient();
			if (act != null) {
				lPatient = act.getStatForItem(template.getClass().getName());
			} else {
				lPatient = new java.util.ArrayList<String>();
			}
			alPatient = new ArrayList<PersistentObject>();
			lbPatient.setData(alPatient);
			for (int i = 0; i < ITEMS_TO_SHOW_IN_MFU_LIST; i++) {
				if (i >= lPatient.size()) {
					break;
				}
				PersistentObject po = Hub.poFactory.createFromString(lPatient
					.get(i));
				if (po != null) {
					alPatient.add(po);
					String label = po.getLabel();
					if (label == null) {
						lbPatient.add("?"); //$NON-NLS-1$
					} else {
						lbPatient.add(label);
					}
				}
			}
			
		}
		
	}
	
	static class DragEnabler implements PersistentObjectDragSource.ISelectionRenderer {
		List list;
		
		DragEnabler(final List list) {
			this.list = list;
		}
		
		public java.util.List<PersistentObject> getSelection() {
			int sel = list.getSelectionIndex();
			ArrayList<PersistentObject> backing = (ArrayList<PersistentObject>) list
			.getData();
			PersistentObject po = backing.get(sel);
			ArrayList<PersistentObject> ret = new ArrayList<PersistentObject>();
			ret.add(po);
			return ret;
		}
		
	}
	
	// add double click listener for ICodeSelectorTarget
	static void doubleClickEnable(final List list) {
		list.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				// normal selection, do nothing
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				// double clicked
				
				int sel = list.getSelectionIndex();
				if (sel != -1) {
					ArrayList<PersistentObject> backing = (ArrayList<PersistentObject>) list
					.getData();
					PersistentObject po = backing.get(sel);
					
					ICodeSelectorTarget target = CodeSelectorHandler
					.getInstance().getCodeSelectorTarget();
					if (target != null) {
						target.codeSelected(po);
					}
					
				}
			}
		});
	}
}
