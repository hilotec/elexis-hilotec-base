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
 *    $Id: BildanzeigeFenster.java 1208 2006-11-02 09:09:35Z rgw_ch $
 *******************************************************************************/

package ch.elexis.images;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.Desk;
import ch.elexis.util.SWTHelper;

public class BildanzeigeFenster extends TitleAreaDialog {
	Bild bild;
	Image img;
	public BildanzeigeFenster(Shell shell, Bild bild){
		super(shell);
		this.bild=bild;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		//parent.setLayout(new FillLayout());
		ScrolledComposite ret=new ScrolledComposite(parent,SWT.H_SCROLL|SWT.V_SCROLL|SWT.BORDER);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Composite canvas=new Composite(ret,SWT.NONE);
		ret.setContent(canvas);
		img=bild.createImage();
		Rectangle r=img.getBounds();
		//GridData gd=new GridData(r.width,r.height);
		//canvas.setLayoutData(gd);
		canvas.addPaintListener(new PaintListener(){
			public void paintControl(PaintEvent e) {
				GC gc=e.gc;
				gc.drawImage(img, 0, 0);
			}
		});
		canvas.addDisposeListener(new DisposeListener(){
			public void widgetDisposed(DisposeEvent e) {
				img.dispose();
			}
			
		});
		canvas.setSize(r.width, r.height);
		return ret;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText(bild.getPatient().getLabel());
		setTitle(bild.getLabel());
		setMessage(bild.get("Info"));
		setTitleImage(Desk.theImageRegistry.get(Desk.IMG_LOGO48));
	}
	
	
	
}
