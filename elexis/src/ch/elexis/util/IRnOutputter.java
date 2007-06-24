/*******************************************************************************
 * Copyright (c) 2006-2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id$
 *******************************************************************************/

package ch.elexis.util;

import java.util.Collection;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ch.elexis.data.Rechnung;

/**
 * An Object that is able to output a bill. Can be a ptinter, a file, an
 * electronic connection or whatever else.
 * @author Gerry
 *
 */
public interface IRnOutputter {
	public static enum TYPE{ORIG,COPY,STORNO};
	
	/**
	 * A short textual description for this output (as Label)
	 */
	public String getDescription();

	/**
	 * Do the actual output
	 * @param type	Type of the bill
	 * @param rnn		collection with all bills to process
	 * @return a result indicating errors 
	 */
	public Result<Rechnung> doOutput(TYPE type, Collection<Rechnung> rnn); 

	/**
	 * Create a Control to perform necessary setings for his outputter.
	 * @param parent
	 * @return
	 */
	public Control createSettingsControl(Composite parent);

}
