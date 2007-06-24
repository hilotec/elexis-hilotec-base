/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: AUFZeugnis.java 1728 2007-02-03 10:08:17Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.actions.GlobalEvents;
import ch.elexis.actions.GlobalEvents.ActivationListener;
import ch.elexis.data.AUF;
import ch.elexis.data.Brief;
import ch.elexis.data.Konsultation;
import ch.elexis.text.TextContainer;
import ch.elexis.text.ITextPlugin.ICallback;
import ch.elexis.text.ITextPlugin.PageFormat;

public class AUFZeugnis extends ViewPart implements ICallback, ActivationListener {
	public static final String ID="ch.elexis.AUFView";
	TextContainer text;
	Brief actBrief;
	ViewPart self;
	
	public AUFZeugnis() {
	}
	
	@Override
	public void dispose(){
		GlobalEvents.getInstance().removeActivationListener(this,this);
		if(text!=null){
			text.dispose();
		}
		super.dispose();
	}
	@Override
	public void createPartControl(Composite parent) {
		self=this;
		text=new TextContainer(getViewSite());
		text.getPlugin().createContainer(parent,this);
		GlobalEvents.getInstance().addActivationListener(this,this);
	}

	@Override
	public void setFocus() {
		text.setFocus();
	}

	public void createAUZ(final AUF auf){
		actBrief=text.createFromTemplateName(Konsultation.getAktuelleKons(),"AUF-Zeugnis",Brief.AUZ, null, null);
		text.getPlugin().setFormat(PageFormat.A5);
	}
	public TextContainer getTextContainer(){
		return text;
	}
	public void save() {
		if(actBrief!=null){
			actBrief.save(text.getPlugin().storeToByteArray(),text.getPlugin().getMimeType());
		}
	}

	public boolean saveAs() {
		return true;
	}

	public void activation(boolean mode) {
		if(mode==false){
			save();
		}
	}

	public void visible(boolean mode) {
	}

}
