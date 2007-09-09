/*******************************************************************************
 * Copyright (c) 2005-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: ListDisplay.java 3124 2007-09-09 09:40:44Z rgw_ch $
 *******************************************************************************/

package ch.elexis.util;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
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

/**
 * A List of objects with UI (definable hyperlinks)
 * Replaces DynamicListDisplay
 */
public class ListDisplay<T> extends Composite {
	public interface LDListener {
		public void hyperlinkActivated(String l);
		public String getLabel(Object o);
	}
	private IHyperlinkListener listen;
	protected List list;
    private ArrayList<T> objects;
    private LDListener dlisten;
    private Composite cLinks;
	private FormToolkit tk=Desk.theToolkit;
	
	    
	public void setDLDListener(LDListener dld){
		dlisten=dld;
	}
	public ListDisplay(Composite parent, int flags,LDListener dld){
		super(parent,flags);
        objects=new ArrayList<T>();
        dlisten=dld;
		setLayout(new GridLayout(1,false));
		cLinks=new Composite(this,SWT.NONE);
		cLinks.setLayout(new FillLayout());
		
		list=new List(this,SWT.SINGLE);
		list.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		tk.adapt(this);
	}
	public void addHyperlinks(String... titles){
		if(listen==null){
			listen=new HyperlinkAdapter(){
				public void linkActivated(HyperlinkEvent e) {
					if(dlisten!=null){
						dlisten.hyperlinkActivated(e.getLabel());
					}
				}};
		}
		for(String title:titles){
			Hyperlink mhl=tk.createHyperlink(cLinks,title,SWT.NONE);
			mhl.addHyperlinkListener(listen);
		}
	}

	/**
	 * Ein Objekt der Liste hinzufügen
	 * @param item das Objekt. Muss getLabel() implementieren
	 */
	public void add(T item){
        objects.add(item);
		list.add(dlisten.getLabel(item));
	}
	
	/**
	 * Ein Objekt aus der Liste entfernen
	 * @param item das Objekt
	 */
	public void remove(T item){
        objects.remove(item);
		list.remove(dlisten.getLabel(item));
	}
	/** Die Liste leeren */
	public void clear(){
		list.removeAll();
		objects.clear();
	}
	/** Ein Kontextmenu für die Liste sezen */
    public void setMenu(Menu m){
        list.setMenu(m);
    }
    
    /**
     * Das momentan ausgewählte Objekt holen
     */
    public T getSelection(){
        String[] obj=list.getSelection();
        if(obj==null || obj.length==0){
            return null;
        }
        for(T po:objects){
            if(dlisten.getLabel(po).equals(obj[0])){
                return po;
            }
        }
        return null;
        
    }
    public java.util.List<T> getAll(){
    	return objects;
    }
    public void addListener(SelectionListener l){
    	list.addSelectionListener(l);
    }
    public void removeListener(SelectionListener l){
    	list.removeSelectionListener(l);
    }
    
}
