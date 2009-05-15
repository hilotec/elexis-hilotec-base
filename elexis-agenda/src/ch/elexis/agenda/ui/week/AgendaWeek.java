package ch.elexis.agenda.ui.week;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ch.elexis.Hub;
import ch.elexis.actions.Activator;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.agenda.data.IPlannable;
import ch.elexis.agenda.data.Termin;
import ch.elexis.agenda.preferences.PreferenceConstants;
import ch.elexis.agenda.ui.BaseView;
import ch.elexis.data.PersistentObject;
import ch.elexis.util.PersistentObjectDragSource2;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class AgendaWeek extends BaseView {

	private ProportionalSheet sheet;
	private ColumnHeader header;

	public AgendaWeek() {

	}

	public ColumnHeader getHeader() {
		return header;
	}

	@Override
	protected void create(Composite parent) {
		makePrivateActions();
		Composite wrapper = new Composite(parent, SWT.NONE);
		wrapper.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		wrapper.setLayout(new GridLayout());
		header = new ColumnHeader(wrapper, this);
		header.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		ScrolledComposite bounding = new ScrolledComposite(wrapper,
				SWT.V_SCROLL);
		bounding.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		// bounding.setBackground(Desk.getColor(Desk.COL_RED));
		sheet = new ProportionalSheet(bounding, this);
		// sheet.setSize(sheet.computeSize(SWT.DEFAULT,SWT.DEFAULT));
		bounding.setContent(sheet);
		bounding.setMinSize(sheet.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		bounding.setExpandHorizontal(true);
		bounding.setExpandVertical(true);
		new PersistentObjectDragSource2(bounding,
				new PersistentObjectDragSource2.Draggable() {

					public List<PersistentObject> getSelection() {
						System.out.println("Dragging");
						ArrayList<PersistentObject> ret = new ArrayList<PersistentObject>(
								1);
						ret.add(GlobalEvents.getInstance().getSelectedObject(
								Termin.class));
						return ret;
					}
				});

	}

	void clear() {
		sheet.clear();
	}

	@Override
	protected IPlannable getSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void refresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public String[] getDisplayedDays() {
		TimeTool ttMonday = Activator.getDefault().getActDate();
		ttMonday.set(TimeTool.DAY_OF_WEEK, TimeTool.MONDAY);
		ttMonday.chop(3);
		String resources = Hub.localCfg.get(PreferenceConstants.AG_DAYSTOSHOW,
				StringTool.join(TimeTool.Wochentage, ","));
		if (resources == null) {
			return new String[0];
		} else {
			ArrayList<String> ret = new ArrayList<String>(resources.length());
			for (String wd : TimeTool.Wochentage) {
				if (resources.indexOf(wd) != -1) {
					ret.add(ttMonday.toString(TimeTool.DATE_COMPACT));
				}
				ttMonday.addDays(1);
			}
			return ret.toArray(new String[0]);
		}
	}

	private void makePrivateActions() {
		final IAction zoomAction = new Action("Zoom", Action.AS_DROP_DOWN_MENU) {
			Menu mine;
			{
				setToolTipText("Massstab einstellen");
				setImageDescriptor(Activator
						.getImageDescriptor("icons/zoom.png"));
				setMenuCreator(new IMenuCreator() {

					public void dispose() {
						mine.dispose();
					}

					public Menu getMenu(Control parent) {
						mine = new Menu(parent);
						fillMenu();
						return mine;
					}

					public Menu getMenu(Menu parent) {
						mine = new Menu(parent);
						fillMenu();
						return mine;
					}
				});
			}

			private void fillMenu() {
				for (String s : new String[] { "40", "60", "80", "100", "120",
						"140", "160", "200", "300" }) {
					MenuItem it = new MenuItem(mine, SWT.RADIO);
					it.setText(s + "%");
					it.addSelectionListener(new SelectionAdapter() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							MenuItem mi = (MenuItem) e.getSource();
							int scale = Integer.parseInt(mi.getText()
									.split("%")[0]);
							double factor = scale / 100.0;
							Hub.localCfg.set(
									PreferenceConstants.AG_PIXEL_PER_MINUTE,
									Double.toString(factor));
							sheet.recalc();
						}

					});
				}
			}
		};
		IToolBarManager tmr = getViewSite().getActionBars().getToolBarManager();
		tmr.add(new Separator());
		tmr.add(zoomAction);
	}

}
