/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ExportiereBloeckeCommand.java 5080 2009-02-03 18:28:58Z rgw_ch $
 *******************************************************************************/
package ch.elexis.commands;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import ch.elexis.data.Leistungsblock;
import ch.elexis.data.Query;
import ch.elexis.exchange.BlockContainer;

public class ExportiereBloeckeCommand extends AbstractHandler {
	public static final String ID = "serviceblocks.export";
	
	public Object execute(ExecutionEvent event) throws ExecutionException{
		Query<Leistungsblock> qbe = new Query<Leistungsblock>(Leistungsblock.class);
		List<Leistungsblock> bloecke = qbe.execute();
		BlockContainer bc = new BlockContainer();
		for (Leistungsblock block : bloecke) {
			bc.store(block);
		}
		
		return new Boolean(bc.finalizeExport());
	}
	
}
