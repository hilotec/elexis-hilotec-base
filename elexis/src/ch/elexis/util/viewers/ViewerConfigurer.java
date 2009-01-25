/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    $Id: ViewerConfigurer.java 5039 2009-01-25 19:49:39Z rgw_ch $
 *******************************************************************************/

package ch.elexis.util.viewers;

import java.util.HashMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.Hub;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.util.Messages;
import ch.rgw.tools.IFilter;
import ch.rgw.tools.Tree;

/**
 * Funktionalität für einen CommonViewer bereitstellen. Der ViewerConfigurer ist ein Container für
 * eine Anzahl xyProvider für je eine Eigenschaft des Viewers Für alle Provider existiert eine
 * Defaultimplementation, die direkt verwendet werden kann, wenn keine speziellen Funktionen
 * benötigt werden.
 * 
 * @see CommonViewer
 */
public class ViewerConfigurer {
	
	private CommonContentProvider contentProvider;
	private LabelProvider labelProvider;
	ControlFieldProvider controlFieldProvider;
	private ButtonProvider buttonProvider;
	private WidgetProvider widgetProvider;
	
	/**
	 * Standard Konstruktor. Erstellt einen Viewer mit Kontrollfeld und Button
	 */
	public ViewerConfigurer(CommonContentProvider cnp, LabelProvider lp, ControlFieldProvider cfp,
		ButtonProvider bp, WidgetProvider wp){
		
		contentProvider = cnp;
		labelProvider = (lp == null) ? new DefaultLabelProvider() : lp;
		controlFieldProvider = cfp;
		buttonProvider = bp;
		widgetProvider = wp;
	}
	
	/**
	 * Vereinfachter Konstruktor. Kein Kontrollfeld und kein Button
	 * 
	 * @param cnp
	 * @param lp
	 * @param wp
	 */
	public ViewerConfigurer(CommonContentProvider cnp, LabelProvider lp, WidgetProvider wp){
		contentProvider = cnp;
		labelProvider = lp;
		buttonProvider = new DefaultButtonProvider();
		widgetProvider = wp;
	}
	
	public interface CommonContentProvider extends IStructuredContentProvider, ControlFieldListener {
		public void startListening();
		
		public void stopListening();
	}
	
	/**
	 * Provider für das Kontrollfeld oberhalb der Liste. Das Kontrollfeld muss einige Elemente
	 * beinhalten, die dem Anwender das Filtern der in der Liste angezeigten Elemente ermöglichen,
	 * und es muss interessierten Listeners die änderung der Filterbedingungen mitteilen können.
	 * 
	 * @see DefaultControlFieldProvider
	 * @author Gerry
	 */
	public interface ControlFieldProvider {
		/** Das Kontrollfeld erstellen */
		public Composite createControl(final Composite parent);
		
		/** Einen Listener enifügen */
		public void addChangeListener(final ControlFieldListener cl);
		
		/** Einen Listener entfernen */
		public void removeChangeListener(final ControlFieldListener cl);
		
		/** Die Werte der Filterbedingungen liefern */
		public String[] getValues();
		
		/** Die Eingabefelder löschen */
		public void clearValues();
		
		/** Anfrage, ob die Eingabefelder leer sind */
		public boolean isEmpty();
		
		/**
		 * Eine Query so modifizieren, dass sie den Filterbedingungen entspricht
		 * 
		 * @see Query
		 */
		public void setQuery(Query<? extends PersistentObject> q);
		
		/** Einen Filter erstellen, der den momentanen Bedingungen entspricht */
		public IFilter createFilter();
		
		/** Eine Meldung absenden, dass der Filter geändert wurde */
		
		public void fireChangedEvent();
		
		public void fireSortEvent(String text);
		
		public void setFocus();
		
	}
	
	/** Listener für Änderungen des Kontrollfelds */
	public interface ControlFieldListener {
		//public void changed(final String[] fields, final String[] values);
		public void changed(HashMap<String, String> values);
		public void reorder(final String field);
		
		/**
		 * ENTER has been pressed
		 */
		public void selected();
	}
	
	/** Provider für den unterliegenden JFace-Viewer */
	public interface WidgetProvider {
		public StructuredViewer createViewer(Composite parent);
	}
	
	/** Provider für den "neu erstellen"- Knopf */
	public interface ButtonProvider {
		public Button createButton(Composite parent);
		
		public boolean isAlwaysEnabled();
	}
	
	/** ****************************************************** */
	public ButtonProvider getButtonProvider(){
		return buttonProvider;
	}
	
	public void setButtonProvider(ButtonProvider buttonProvider){
		this.buttonProvider = buttonProvider;
	}
	
	public WidgetProvider getWidgetProvider(){
		return widgetProvider;
	}
	
	public void setWidgetProvider(WidgetProvider widgetProvider){
		this.widgetProvider = widgetProvider;
	}
	
	public CommonContentProvider getContentProvider(){
		return contentProvider;
	}
	
	public void setContentProvider(CommonContentProvider contentProvider){
		this.contentProvider = contentProvider;
	}
	
	public ControlFieldProvider getControlFieldProvider(){
		return controlFieldProvider;
	}
	
	public void setControlFieldProvider(ControlFieldProvider controlFieldProvider){
		this.controlFieldProvider = controlFieldProvider;
	}
	
	public LabelProvider getLabelProvider(){
		return labelProvider;
	}
	
	public void setLabelProvider(LabelProvider labelProvider){
		this.labelProvider = labelProvider;
	}
	
	/**
	 * Ein LabelProvider, der Objekte des Typs Tree analysiert
	 * 
	 * @see Tree
	 * @author Gerry
	 */
	public static class TreeLabelProvider extends LabelProvider {
		
		@Override
		public String getText(Object element){
			if (element instanceof Tree) {
				Tree tree = (Tree) element;
				return ((PersistentObject) tree.contents).getLabel();
			} else {
				return element.toString();
			}
		}
		
	}
	
	/**
	 * Defaultimplementation des Buttonproviders. Liefert einen Button, der ein neues
	 * PersistentObject anhand der ausgewählten Felder erstellt, und der nur dann aktivierbar ist,
	 * wenn die Anzeigeliste leer ist. Wenn man den leeren Konstruktor benutzt, wird kein Button
	 * erzeugt.
	 * 
	 * @author Gerry
	 * 
	 */
	public static class DefaultButtonProvider implements ButtonProvider {
		Class clazz;
		CommonViewer vcf;
		Dialog dlg;
		private String text = Messages.getString("ViewerConfigurer.createNew"); //$NON-NLS-1$
		
		/** Keinen Button erzeugen */
		public DefaultButtonProvider(){}
		
		/** Einen Button erzeugen, der Default-Objekte der Klasse cl erstellt */
		public DefaultButtonProvider(CommonViewer cv, Class cl){
			clazz = cl;
			vcf = cv;
		}
		
		/**
		 * Einen Button erzeugen, der eine individuelle Beschriftung t hat
		 * 
		 * @param cl
		 *            Klasse, deren Objekte erzeugt werden sollen
		 * @param t
		 *            Text, der auf dem Button stehen soll
		 */
		public DefaultButtonProvider(CommonViewer cv, Class cl, String t){
			clazz = cl;
			vcf = cv;
			text = t;
		}
		
		/**
		 * Einen Button erzeugen, der bei Klick einen Dialog öffnet
		 * 
		 */
		public DefaultButtonProvider(CommonViewer cv, String t, Dialog dlg){
			vcf = cv;
			text = t;
			this.dlg = dlg;
		}
		
		public Button createButton(Composite parent){
			if (vcf == null) {
				return null;
			}
			Button ret = new Button(parent, SWT.PUSH);
			ret.setText(text);
			ret.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					if (dlg != null) {
						dlg.open();
					} else {
						/* final PersistentObject po= */
						Hub.poFactory.create(clazz, ((DefaultControlFieldProvider) vcf
							.getConfigurer().getControlFieldProvider()).getDBFields(), vcf
							.getConfigurer().getControlFieldProvider().getValues());
						if (vcf.getConfigurer().getContentProvider() instanceof LazyContentProvider) {
							LazyContentProvider lc =
								(LazyContentProvider) vcf.getConfigurer().getContentProvider();
							lc.dataloader.invalidate();
						}
					}
					vcf.getViewerWidget().getControl().redraw();
					vcf.getViewerWidget().getControl().update();
					vcf.getConfigurer().getControlFieldProvider().clearValues();
				}
			});
			return ret;
		}
		
		public boolean isAlwaysEnabled(){
			return false;
		}
		
	}
	
	public static class ContentProviderAdapter implements CommonContentProvider {
		
		public Object[] getElements(Object inputElement){
			return null;
		}
		
		public void dispose(){

		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){

		}
		
		public void startListening(){
		// TODO Automatisch erstellter Methoden-Stub
		
		}
		
		public void stopListening(){
		// TODO Automatisch erstellter Methoden-Stub
		
		}
		
		public void changed(HashMap<String,String> values){
		// TODO Automatisch erstellter Methoden-Stub
		
		}
		
		public void reorder(String field){
		// TODO Automatisch erstellter Methoden-Stub
		
		}
		
		public void selected(){
		// TODO Automatisch erstellter Methoden-Stub
		}
	}
	
	/**
	 * Sometimes we'd like to use a CommonViewer without controlfield
	 * 
	 * @author Gerry
	 * 
	 */
	public static class EmptyControlfieldProvider implements ControlFieldProvider {
		
		public void addChangeListener(ControlFieldListener cl){}
		
		public void clearValues(){}
		
		public Composite createControl(Composite parent){
			return new Composite(parent, SWT.NONE);
		}
		
		public IFilter createFilter(){
			return null;
		}
		
		public void fireChangedEvent(){}
		
		public void fireSortEvent(String text){}
		
		public String[] getValues(){
			return new String[0];
		}
		
		public boolean isEmpty(){
			return true;
		}
		
		public void removeChangeListener(ControlFieldListener cl){}
		
		public void setFocus(){}
		
		public void setQuery(Query q){}
		
	}
	
}
