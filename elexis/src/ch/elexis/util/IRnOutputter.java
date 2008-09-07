/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: IRnOutputter.java 4382 2008-09-07 13:58:58Z rgw_ch $
 *******************************************************************************/

package ch.elexis.util;

import java.util.Collection;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ch.elexis.data.Fall;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.Result;

/**
 * An Object that is able to output a bill. Can be a ptinter, a file, an electronic connection or
 * whatever else.
 * 
 * @author Gerry
 * 
 */
public interface IRnOutputter {
	public static enum TYPE {
		ORIG, COPY, STORNO
	};
	
	/**
	 * A short textual description for this output (as Label)
	 */
	public String getDescription();
	
	/**
	 * Do the actual output
	 * 
	 * @param type
	 *            Type of the bill
	 * @param rnn
	 *            collection with all bills to process
	 * @return a result indicating errors
	 */
	public Result<Rechnung> doOutput(TYPE type, Collection<Rechnung> rnn);
	
	/**
	 * Cancelling an already output bill: Depending on the type of the outputter, this might result
	 * in propagataing the cancel information to the final destination of the bill.
	 * 
	 * @param rn
	 *            the specific bill to cancel or null, if it is just a general question
	 * @return true if this outputter wants to be informed if this (or any in case of Rn==null) bill
	 *         is cancelled. If an outputter does need to react on storno messages, it should return
	 *         false.
	 */
	public boolean canStorno(Rechnung rn);
	
	/**
	 * check whether a case could be billed, i.e. all billing data are present. This should only
	 * check for required absolutely mandatory data. At output time, the outputter still can reject.
	 * 
	 * @param fall
	 *            the case to check
	 * @return true if we can send a bill from this case
	 */
	public boolean canBill(Fall fall);
	
	/**
	 * Create a Control to perform necessary setings for his outputter.
	 * 
	 * @param parent
	 * @return
	 */
	public Control createSettingsControl(Composite parent);
	
	/**
	 * Methode is called before dialog is closed. Used to save the widget contents before the
	 * composite is disposed;
	 */
	public void saveComposite();
	
}
