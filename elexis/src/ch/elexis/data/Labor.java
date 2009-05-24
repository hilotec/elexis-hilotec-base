/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Labor.java 5317 2009-05-24 15:00:37Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;

public class Labor extends Organisation {
	static {
		addMapping(Kontakt.TABLENAME, "Name	=Bezeichnung1", "Zusatz1=Bezeichnung2", "Zusatz2=ExtInfo", "Kuerzel=PatientNr", "Ansprechperson=Bezeichnung3", "istOrganisation", "istLabor");
	}

	@Override
	protected String getConstraint() {
		return new StringBuilder(Kontakt.IS_LAB)
		.append(Query.EQUALS)
		.append(JdbcLink.wrap(StringTool.one))
		.toString();
	}

	@Override
	protected void setConstraint() {
		set(new String[] { Kontakt.IS_LAB,Kontakt.IS_ORGANIZATION }, StringTool.one, StringTool.one);
	}

	public Labor(String Kuerzel, String Name) {
		super(Name, "Labor");
		set("Kuerzel", Kuerzel);
	}

	public static Labor load(String id) {
		Labor ret = new Labor(id);
		if (ret.exists()) {
			return ret;
		}
		return null;
	}

	protected Labor(String id) {
		super(id);
	}

	protected Labor() {
	}
}
