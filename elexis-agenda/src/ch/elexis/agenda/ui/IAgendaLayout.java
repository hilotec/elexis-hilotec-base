package ch.elexis.agenda.ui;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Composite;

public interface IAgendaLayout {
	public Composite getComposite();
	public int getLeftOffset();
	public int getPadding();
	public double getWidthPerColumn();
	public double getPixelPerMinute();
	public MenuManager getContextMenuManager();
}
