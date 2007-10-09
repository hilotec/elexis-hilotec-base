/*******************************************************************************
 * Copyright (c) 2007, G. Weirich
 * All rights reserved.    
 * $Id: RegisterAction.java 121 2007-06-10 21:24:54Z Gerry $
 *******************************************************************************/

package ch.elexis.connect.abxmicros;

import org.eclipse.jface.action.Action;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.util.SWTHelper;

public class ConnectAction extends Action {
	public ConnectAction(){
		super("Micros",Action.AS_CHECK_BOX);
		setImageDescriptor(Desk.theImageRegistry.getDescriptor(Desk.IMG_TICK));
		setToolTipText("Mit ABX Micros verbinden");
	}

	@Override
	public void run() {
		SWTHelper.showInfo("Micros", "Aktiviert");
	}
	
}
