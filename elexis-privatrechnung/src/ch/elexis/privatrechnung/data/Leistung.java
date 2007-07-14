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
 * $Id: TarmedLeistung.java 2763 2007-07-08 20:35:31Z rgw_ch $
 *******************************************************************************/

package ch.elexis.privatrechnung.data;

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
		// TODO Auto-generated method stub
		return super.getCodeSystemCode();
	}

	@Override
	public String getCodeSystemName() {
		// TODO Auto-generated method stub
		return super.getCodeSystemName();
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
		// TODO Auto-generated method stub
		return null;
	}

	public double getFactor(TimeTool date, String subgroup) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getTP(TimeTool date, String subgroup) {
		// TODO Auto-generated method stub
		return 0;
	}

}
