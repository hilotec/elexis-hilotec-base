/*******************************************************************************
 * Copyright (c) 2005-2006, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Lutz - initial implementation
 *    
 *  $Id$
 *******************************************************************************/

package ch.elexis.dialogs;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.Desk;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.TimeTool;

import com.tiff.common.ui.datepicker.DatePickerCombo;

public class FileEditDialog extends TitleAreaDialog {
	private static final int WIDGET_SPACE = 20;
	
	private Text tDateiname;
	private Text tExtension;
	
	private DatePickerCombo dp;
	
	private File file;

	public FileEditDialog(Shell parent, File file) {
		super(parent);
		
		this.file = file;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		String fileName = file.getName();
		// TODO ext
		String fileExtension = null;
		
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		area.setLayout(new GridLayout(2, true));
		
		Label label;
		GridData gd;
		
		// filename text (without extension)
		
		label = new Label(area,SWT.NONE);
		label.setText("Dateiname");
		label.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		
		tDateiname=new Text(area,SWT.BORDER);
		// TODO ext
		tDateiname.setText(fileName);
		tDateiname.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		
		// date label
		label = new Label(area, SWT.NONE);
		label.setText("Datum" + " (" + new TimeTool(file.lastModified()).toString(TimeTool.DATE_GER) +  ")");
		gd = SWTHelper.getFillGridData(1, true, 1, false);
		gd.verticalIndent = WIDGET_SPACE;
		label.setLayoutData(gd);
		
		// extension label
		label = new Label(area, SWT.NONE);
		label.setText("Extension");
		gd = SWTHelper.getFillGridData(1, true, 1, false);
		gd.verticalIndent = WIDGET_SPACE;
		label.setLayoutData(gd);
		
		// date text / datepickercombo
		
		dp = new DatePickerCombo(area, SWT.NONE);
		// current date of file
		/*
		 * doesn't work, because you can't decide if user has changed date
		 * or just only set cursor into date edit field
		 */
		//dp.setDate(new Date(file.lastModified()));
		// TODO DEBUG
		//System.out.println("old time: " + file.lastModified());

		// extension text
		tExtension = new Text(area, SWT.BORDER);
		// TODO ext
		//tExtension.setText(file.getName());
		tExtension.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		
		return area;
	}
	@Override
	public void create() {
		super.create();
		setMessage("Datei umbenennen oder Datum der letzten Änderung setzen");
		setTitle("Datei-Eigenschaften");
		getShell().setText("Datei-Eigenschaften");
		setTitleImage(Desk.theImageRegistry.get("elexislogo48"));
	}
	@Override
	protected void okPressed() {
		// TODO ext
		String dateiname = tDateiname.getText();
		Date datum = dp.getDate();
		
		if (datum != null && datum.getTime() != file.lastModified()) {
			// set time to 00:00
			Calendar cal = Calendar.getInstance();
			cal.setTime(datum);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			Long newTime = cal.getTimeInMillis();

			System.out.println("new time: " + newTime + " ("
					+ file.lastModified() + ")");
			file.setLastModified(newTime);
		}

		if (!file.getName().equals(dateiname)) {
			File newFile = new File(file.getParent(), dateiname);
			System.out.println("new filiename: " + newFile.getAbsolutePath() + " ( " + file.getAbsolutePath() + ")");
			if (file.renameTo(newFile)) {
				// seems to be required on Windows
				//newFile.setLastModified(file.lastModified());
			} else {
				MessageDialog.openError(
						getShell(), "Datei-Eigenschaften: Fehler", "Die Datei konnte nicht umbenannt werden.");
			}
		}

		super.okPressed();
	}
	
}
