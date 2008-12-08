/*******************************************************************************
 * Copyright (c) 2005, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: DynamicListDisplay.java 4771 2008-12-08 13:36:36Z rgw_ch $
 *******************************************************************************/

package ch.elexis.util;

import java.util.ArrayList;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.data.PersistentObject;

/**
 * Anzeige einer Liste von PersistentObjects plus ein Hyperlink, der das Hinzufügen von Objekten
 * (bzw. beliebige andere Aktionen) ermöglicht. Optional kann ein LabelProvider gestzt werden.
 * 
 * @deprecated use ListDisplay
 */
@Deprecated
public class DynamicListDisplay extends Composite {
	public interface DLDListener {
		public boolean dropped(PersistentObject dropped);
		
		public void hyperlinkActivated(String l);
	}
	
	// private FormToolkit tk;
	// private Hyperlink hl, hPrint;
	private IHyperlinkListener listen;
	protected List list;
	private final ArrayList<PersistentObject> objects;
	private DLDListener dlisten;
	private final Composite cLinks;
	private final FormToolkit tk = Desk.getToolkit();
	private final DragSource ds;
	
	private LabelProvider labelProvider = null;
	
	public void setDLDListener(final DLDListener dld){
		dlisten = dld;
	}
	
	public DynamicListDisplay(final Composite parent, final int flags, final DLDListener dld){
		super(parent, flags);
		objects = new ArrayList<PersistentObject>();
		dlisten = dld;
		setLayout(new GridLayout(1, false));
		cLinks = new Composite(this, SWT.NONE);
		cLinks.setLayout(new FillLayout());
		
		list = new List(this, SWT.SINGLE);
		list.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tk.adapt(this);
		ds = new DragSource(list, DND.DROP_COPY);
		Transfer[] types = new Transfer[] {
			TextTransfer.getInstance()
		};
		ds.setTransfer(types);
		ds.addDragListener(new DragSourceAdapter() {
			@Override
			public void dragSetData(final DragSourceEvent event){
				PersistentObject po = getSelection();
				event.data = po.storeToString();
			}
			
			@Override
			public void dragStart(final DragSourceEvent event){
				PersistentObject po = getSelection();
				if (po == null) {
					event.doit = false;
				} else {
					event.doit = po.isDragOK();
				}
			}
			
		});
		
		DropTarget dt = new DropTarget(this, DND.DROP_COPY);
		dt.setTransfer(types);
		dt.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragEnter(final DropTargetEvent event){
				if (dlisten == null) {
					event.detail = DND.DROP_NONE;
				} else {
					event.detail = DND.DROP_COPY;
				}
			}
			
			@Override
			public void drop(final DropTargetEvent event){
				if (dlisten != null) {
					String drp = (String) event.data;
					System.out.println(drp);
					String[] dl = drp.split(","); //$NON-NLS-1$
					for (String obj : dl) {
						PersistentObject dropped = Hub.poFactory.createFromString(obj);
						dlisten.dropped(dropped);
					}
				}
			}
			
		});
	}
	
	public void addHyperlinks(final String... titles){
		if (listen == null) {
			listen = new HyperlinkAdapter() {
				@Override
				public void linkActivated(final HyperlinkEvent e){
					if (dlisten != null) {
						dlisten.hyperlinkActivated(e.getLabel());
					}
				}
			};
		}
		for (String title : titles) {
			Hyperlink mhl = tk.createHyperlink(cLinks, title, SWT.NONE);
			mhl.addHyperlinkListener(listen);
		}
	}
	
	/**
	 * Ein Objekt der Liste hinzufügen
	 * 
	 * @param item
	 *            das Objekt. Muss getLabel() implementieren
	 */
	public void add(final PersistentObject item){
		objects.add(item);
		list.add(getLabel(item));
	}
	
	/**
	 * Ein Objekt aus der Liste entfernen
	 * 
	 * @param item
	 *            das Objekt
	 */
	public void remove(final PersistentObject item){
		objects.remove(item);
		list.remove(getLabel(item));
	}
	
	/** Die Liste leeren */
	public void clear(){
		list.removeAll();
		objects.clear();
	}
	
	/** Ein Kontextmenu für die Liste sezen */
	@Override
	public void setMenu(final Menu m){
		list.setMenu(m);
	}
	
	/**
	 * Das momentan ausgewählte Objekt holen
	 */
	public PersistentObject getSelection(){
		String[] obj = list.getSelection();
		if ((obj == null) || (obj.length == 0)) {
			return null;
		}
		for (PersistentObject po : objects) {
			if (getLabel(po).equals(obj[0])) {
				return po;
			}
		}
		return null;
		
	}
	
	public java.util.List<PersistentObject> getAll(){
		return objects;
	}
	
	public void addListener(final SelectionListener l){
		list.addSelectionListener(l);
	}
	
	public void removeListener(final SelectionListener l){
		list.removeSelectionListener(l);
	}
	
	public void setLabelProvider(final LabelProvider labelProvider){
		this.labelProvider = labelProvider;
	}
	
	private String getLabel(final PersistentObject po){
		if (labelProvider != null) {
			return labelProvider.getText(po);
		} else {
			return po.getLabel();
		}
	}
}
