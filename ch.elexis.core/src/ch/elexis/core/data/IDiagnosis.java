/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id$
 *******************************************************************************/

package ch.elexis.core.data;

/**
 * Diagnosen sind ebensop wie Leistungen "Pluggable" definiert, damit neue Codesysteme leicht
 * eingebaut werden können. Ein neues Diagnosesystem muss das Interface Diagnose implementieren, und
 * sich im Erweiterungspunkt ch.elexis.Diagnosecodes einhängen
 * 
 * @author gerry
 * 
 */
public interface IDiagnosis extends ICodeElement {
	
	/** Einen kurzen Text zur Diagnose liefern */
	public String getLabel();
	
}
