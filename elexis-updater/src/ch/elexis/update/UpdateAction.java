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
 *  $Id: UpdateAction.java 2562 2007-06-23 04:51:56Z rgw_ch $
 *******************************************************************************/
package ch.elexis.update;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents.SelectionListener;
import ch.elexis.data.Anwender;
import ch.elexis.data.PersistentObject;

public class UpdateAction extends Action implements SelectionListener{

	IWorkbenchWindow win;	

	UpdateAction(){
		
		setText("Update");
	}

	@Override
	public void run() {
		AutoUpdate update=new AutoUpdate();
		update.doUpdate();
	}

	public void clearEvent(Class template) {
		// TODO Auto-generated method stub
		
	}

	public void selectionEvent(PersistentObject obj) {
		if(obj instanceof Anwender){
			setEnabled(Hub.acl.request(Activator.AC_UPDATE));
		}
	}

	

}
