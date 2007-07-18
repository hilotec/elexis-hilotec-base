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
 * $Id: IVerrechenbar.java 2839 2007-07-18 17:44:17Z rgw_ch $
 *******************************************************************************/
package ch.elexis.data;

import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.IFilter;
import ch.elexis.util.IOptifier;
import ch.elexis.util.Money;
import ch.elexis.util.Result;
import ch.rgw.tools.TimeTool;

/**
 * Das Leistungskonzept ist "pluggable" definiert. Dies, damit neue Abrechnungssysteme
 * jederzeit leicht integriert werden können.
 * Ein Leistungssystem muss nur das Interface Verrechenbar implementieren, um von
 * Elexis ohne weitere Modifikationen genutzt werden zu können.
 * @author gerry
 *
 */
public interface IVerrechenbar extends ICodeElement{
	public static IOptifier optifier=new DefaultOptifier();
	public static Comparator comparator=new IVerrechenbar.DefaultComparator();
	public static IFilter ifilter=new IVerrechenbar.DefaultFilter();
    
    public IOptifier getOptifier();
    /** Einen Comparator zum Sortieren von Leistungen dieses Typs liefern */
    public Comparator getComparator();
    /** Einen Filter liefern, um Elemente dieses Typs nach Mandant zu filtern */
    public IFilter getFilter(Mandant m);
    /** Betrag dieser Verrechenbar (in TP*100) an einem bestimmten Datum liefern 
     * */
    public int getTP(TimeTool date, Fall fall);
    public double getFactor(TimeTool date, Fall fall);

    /** Eigene Kosten für diese Leistung 
     * @param dat Datum, für das die Kosten geliefert werden sollen */
    public Money getKosten(TimeTool dat);
    /** Zeitanrechnung für diese Leistung (in Minuten)*/
    public int getMinutes();
    //public AbstractDataLoaderJob getDataloader();
    public String [] getDisplayedFields();
    
    public static class DefaultComparator implements Comparator{
		public int compare(final Object o1, final Object o2) {
			IVerrechenbar v1=(IVerrechenbar)o1;
			IVerrechenbar v2=(IVerrechenbar)o2;
			int i=v1.getCodeSystemName().compareTo(v2.getCodeSystemName());
			if(i==0){
				i=v1.getCode().compareTo(v2.getCode());
			}
			return i;
		}
    	
    }
    public static class DefaultFilter implements IFilter{
		public boolean select(final Object toTest) {
			return true;
		}
    	
    }
    public static class DefaultOptifier implements IOptifier{

		public Result<Konsultation> optify(final Konsultation kons) {
			return new Result<Konsultation>(kons);
		}

		public Result<IVerrechenbar> add(final IVerrechenbar code, final Konsultation kons) {
			List<Verrechnet> old=kons.getLeistungen();
			old.add(new Verrechnet(code,kons,1));
			return new Result<IVerrechenbar>(code);
		}

		public Result<Verrechnet> remove(final Verrechnet v, final Konsultation kons) {
			List<Verrechnet> old=kons.getLeistungen();
			old.remove(v);
			v.delete();
			return new Result<Verrechnet>(null);
		}

		
    	
    }
}
