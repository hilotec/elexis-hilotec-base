// $Id: ReminderPerspektive.java 1332 2006-11-26 07:32:34Z rgw_ch $
package ch.elexis;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import ch.elexis.views.ReminderView;
import ch.elexis.views.Starter;

public class ReminderPerspektive implements IPerspectiveFactory {
	public static final String ID="ch.elexis.ReminderPerspektive"; //$NON-NLS-1$
	
	public void createInitialLayout(IPageLayout layout) {
			String editorArea = layout.getEditorArea();
			layout.setEditorAreaVisible(false);
			layout.setFixed(false);
			layout.addStandaloneView(Starter.ID,false,SWT.LEFT,0.1f,editorArea);
			layout.addView(ReminderView.ID,SWT.RIGHT,0.8f,editorArea);
		}

}
