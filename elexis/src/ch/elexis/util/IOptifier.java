/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: IOptifier.java 3756 2008-03-28 17:33:20Z rgw_ch $
 *******************************************************************************/
package ch.elexis.util;

import java.util.List;

import ch.elexis.data.IVerrechenbar;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Verrechnet;

/**
 * Ein Optifier ist ein Optimizer und Verifier für Code-Systeme
 * @author gerry
 *
 */
public interface IOptifier {
	/**
	 * Eine Konsultation optifizieren 
	 */
	public Result<Konsultation> optify(Konsultation kons);
	/**
	 * Eine Leistung einer Konsultation hinzufügen; die anderen Leistungen der Kons ggf. anpassen
	 * @param code	der hinzuzufügende code
	 * @param kons die Konsultation
	 * @return Result mit der möglicherweise veränderten Liste
	 */
	public Result<IVerrechenbar> add(IVerrechenbar code, Konsultation kons);
	/**
	 * Eine Leistung aus einer Konsultation entfernen; die Liste ggf. anpassen
	 * @param code der zu enfternende code
	 * @param kons die KOnsultation
	 * @return Result mit der möglicherweise veränderten Liste
	 */
	public Result<Verrechnet> remove(Verrechnet code, Konsultation kons);


}