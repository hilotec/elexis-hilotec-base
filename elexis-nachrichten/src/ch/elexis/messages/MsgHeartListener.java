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
 * $Id: MsgHeartListener.java 3089 2007-09-03 15:56:23Z rgw_ch $
 *******************************************************************************/

package ch.elexis.messages;

import java.util.List;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.Heartbeat.HeartListener;
import ch.elexis.data.Query;

public class MsgHeartListener implements HeartListener {
	boolean bSkip;
	
	public void heartbeat() {
		if(!bSkip){
			Query<Message> qbe=new Query<Message>(Message.class);
			qbe.add("to", "=", Hub.actUser.getId());
			final List<Message> res=qbe.execute();
			if(res.size()>0){
				Desk.theDisplay.asyncExec(new Runnable(){
					public void run() {
						bSkip=true;
						new MsgDetailDialog(Hub.getActiveShell(),res.get(0)).open();
						bSkip=false;
					}
				});
				
			}
		}
	}

}
