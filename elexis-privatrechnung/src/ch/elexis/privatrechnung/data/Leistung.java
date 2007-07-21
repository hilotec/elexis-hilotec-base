/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: Leistung.java 2850 2007-07-21 05:00:02Z rgw_ch $
 *******************************************************************************/

package ch.elexis.privatrechnung.data;

import ch.elexis.data.Fall;
import ch.elexis.data.VerrechenbarAdapter;
import ch.rgw.tools.TimeTool;

/**
 * A billing plugin that is able to manage several arbitrary tax systems
 * @author gerry
 *
 */
public class Leistung extends VerrechenbarAdapter {
	private static final String TABLENAME="CH_ELEXIS_PRIVATRECHNUNG";
	
	static{
		addMapping(TABLENAME,"name","short","cost","price","subsystem");
	}
	
	
	@Override
	public String getCodeSystemCode() {
		return "999";
	}

	@Override
	public String getCodeSystemName() {
		return "privat";
	}

	@Override
	public String getLabel() {
		return get("name");
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}

	public String[] getDisplayedFields() {
		return new String[]{"name","price"};
	}

	public double getFactor(final TimeTool date, final Fall fall) {
		return getVKMultiplikator(date, fall);
	}

	public int getTP(final TimeTool date, final Fall fall) {
		return checkZero(get("price"));
	}

}
