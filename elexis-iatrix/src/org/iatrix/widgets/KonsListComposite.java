/*******************************************************************************
 * Copyright (c) 2006, G. Weirich, D. Lutz, P. Schönbucher and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    
 *  $Id$
 *******************************************************************************/

package org.iatrix.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ILayoutExtension;
import org.eclipse.ui.internal.forms.widgets.FormUtil;

import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Verrechnet;
import ch.elexis.util.Money;
import ch.rgw.tools.StringTool;

/**
 * Special composite with a special layout.
 * Don't set a layout!
 * 
 * @author danlutz
 */
public class KonsListComposite {
	private Composite composite;

	private FormToolkit toolkit;

	private Label loadingLabel;
	private List<WidgetRow> widgetRows;
	private Sash sash;
	
	private static final String CFG_SASH_X_PERCENT = "org.iatrix/widgets/konslistcomposite/sash_x_percent";
	private static final int SASH_X_NOTSET = -1;
	private static final int SASH_X_DEFAULT_PERCENT = 75;
	// current horizontal sash position.
	private int currentSashXPercent = SASH_X_NOTSET;
	
	private List<KonsData> konsultationen;
	private static LabelProvider verrechnetLabelProvider;
	
	{
		verrechnetLabelProvider = new LabelProvider() {
        	public String getText(Object element) {
        		if (!(element instanceof Verrechnet)) {
        			return "";
        		}
        		
        		Verrechnet verrechnet = (Verrechnet) element;
        		String name = verrechnet.getText();

        		// TODO replace with verrechnet.getNickname();
        		String vClass = verrechnet.getVerrechenbar().getClass().getName();
        		if (vClass.equals("ch.elexis.data.TarmedLeistung")) {
            		String nick = ((PersistentObject) verrechnet.getVerrechenbar()).get("Nick");
            		if (!StringTool.isNothing(nick)) {
            			name = nick;
            		}
        		}
        		
        		StringBuilder sb = new StringBuilder();
                int z = verrechnet.getZahl();
                Money preis=new Money(verrechnet.getEffPreis()).multiply(z);
                sb.append(z).append(" ").append(name)
                 .append(" (").append(preis.getAmountAsString()).append(")");
                return sb.toString();
            }
		};
	}
	
	public KonsListComposite(Composite parent, FormToolkit toolkit) {
		composite = toolkit.createComposite(parent);
		this.toolkit = toolkit;
		
		composite.setLayout(new MyLayout());
		
		loadingLabel = toolkit.createLabel(composite, "Lade Konsultationen...");
		loadingLabel.setVisible(false);

		widgetRows = new ArrayList<WidgetRow>();
		
		sash = new Sash(composite, SWT.VERTICAL);
		sash.setVisible(false);
		
		sash.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				int sashX = event.x;
				currentSashXPercent = absoluteToPercent(composite.getSize().x, sashX);
				Hub.localCfg.set(CFG_SASH_X_PERCENT, currentSashXPercent);
				composite.layout();
			}
		});
	}
	
	public void setLayoutData(Object layoutData) {
		composite.setLayoutData(layoutData);
	}
	
	public void setKonsultationen(List<KonsData> konsultationen) {
		this.konsultationen = konsultationen;
		refresh();
	}
	
	// refresh layout and all elements
	private void refresh() {
		// clear all widget rows
		for (WidgetRow row : widgetRows) {
			row.setKonsData(null);
		}
		
		List<WidgetRow> availableRows = new ArrayList<WidgetRow>();
		availableRows.addAll(widgetRows);

		if (konsultationen != null) {
			for (KonsData konsData : konsultationen) {
				WidgetRow row;
				if (availableRows.size() > 0) {
					row = availableRows.remove(0);
				} else {
					row = new WidgetRow(composite);
					widgetRows.add(row);
				}
				row.setKonsData(konsData);
			}
			
			loadingLabel.setVisible(false);
			sash.setVisible(konsultationen.size() > 0);
		} else {
			loadingLabel.setVisible(true);
			sash.setVisible(false);
		}
	}
	
	private int percentToAbsolute(int base, int percent) {
		return base * percent / 100;
	}
	
	private int absoluteToPercent(int base, int absolute) {
		return absolute * 100 / base;
	}
	
	/**
	 * This class encapsulates the required widgets for a row. It assumes that
	 */
	private class WidgetRow {
		Hyperlink hTitle;
		Label lFall;
		EnhancedTextFieldRO etf;
		Text verrechnung;
		
		Label verticalSeparator;
		Label horizontalSeparator;

		KonsData konsData;
		
		// collect controls for disposal in dispose()
		List<Control> controls;
		
		WidgetRow(Composite parent) {
			/*
			 * Important: Add all created controls to "controls"
			 *            for later disposal.
			 */
			
			controls = new ArrayList<Control>();
			
			// header
			
			hTitle = toolkit.createHyperlink(parent, "", SWT.NONE);
			hTitle.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					if (konsData != null) {
						GlobalEvents.getInstance().fireSelectionEvent(konsData.konsultation);
					}
				}
			});
			controls.add(hTitle);
			
			lFall = toolkit.createLabel(parent, "");
			controls.add(lFall);
			
			etf = new EnhancedTextFieldRO(parent);
			controls.add(etf);
			toolkit.adapt(etf);
			
			verrechnung = toolkit.createText(parent, "", SWT.WRAP | SWT.READ_ONLY);
			controls.add(verrechnung);
			
			verticalSeparator = toolkit.createLabel(parent, "", SWT.SEPARATOR | SWT.VERTICAL);
			controls.add(verticalSeparator);
			horizontalSeparator = toolkit.createLabel(parent, "", SWT.SEPARATOR | SWT.HORIZONTAL);
			controls.add(horizontalSeparator);

			konsData = null;
			showControls(false);
		}
		
		public void setKonsData(KonsData konsData) {
			this.konsData = konsData;
			if (konsData != null) {
				showControls(true);
			} else {
				showControls(false);
			}
			refresh();
		}
		
		// set the text of the controls
		private void refresh() {
			if (konsData != null) {
				hTitle.setText(konsData.konsTitle);
				lFall.setText(konsData.fallTitle);
				etf.setText(konsData.konsText);
				verrechnung.setText(konsData.verrechnungenText);
			} else {
				hTitle.setText("");
				lFall.setText("");
				etf.setText("");
				verrechnung.setText("");
			}
		}
		
		void showControls(boolean visible) {
			for (Control control : controls) {
				if (control != null) {
					control.setVisible(visible);
				}
			}
		}
		
		void dispose() {
			// dispose all used controls
			for (Control control : controls) {
				if (control != null) {
					control.dispose();
				}
			}
			controls.clear();
			
			konsData = null;
		}
	}
	
	public class MyLayout extends Layout implements ILayoutExtension {
		private static final int TITLE_SPACING = 2;
		private static final int ROW_SPACING = 4;
		
		// ILayoutExtension
		
		/**
		 * Computes the minimum width of the parent. All widgets capable of word
		 * wrapping should return the width of the longest word that cannot be
		 * broken any further.
		 * 
		 * @param parent the parent composite
		 * @param changed <code>true</code> if the cached information should be
		 * flushed, <code>false</code> otherwise.
		 * @return the minimum width of the parent composite
		 */
		public int computeMinimumWidth(Composite parent, boolean changed) {
			return computeMinimumMaximumWidth(parent, changed, false);
		}
		/**
		 * Computes the maximum width of the parent. All widgets capable of word
		 * wrapping should return the length of the entire text with wrapping
		 * turned off.
		 * 
		 * @param parent the parent composite
		 * @param changed <code>true</code> if the cached information
		 * should be flushed, <code>false</code> otherwise.
		 * @return the maximum width of the parent composite
		 */
		public int computeMaximumWidth(Composite parent, boolean changed) {
			return computeMinimumMaximumWidth(parent, changed, true);
		}
		
		private int computeMinimumMaximumWidth(Composite parent, boolean changed, boolean max) {
			int sashWidth = sash.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

			int leftWidth = 0;
			int rightWidth = 0;
			int totalWidth = 0;
			
			for (WidgetRow row : widgetRows) {
				if (row.konsData == null) {
					// ignore
					continue;
				}
				
				int width;

				// for hTitle and lFall, min/max are identical
				width = row.hTitle.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed).x
					+ row.lFall.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed).x;
				if (width > totalWidth) {
					totalWidth = width;
				}
				
				// left control (etf)
				if (max) {
					width = row.etf.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
				} else {
					width = row.etf.computeSize(5, SWT.DEFAULT).x;
				}
				if (width > leftWidth) {
					leftWidth = width;
				}

				// right control (verrechnung)
				if (max) {
					width = row.verrechnung.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
				} else {
					width = row.verrechnung.computeSize(5, SWT.DEFAULT).x;
				}
				if (width > rightWidth) {
					rightWidth = width;
				}
			}

			int width = Math.max(totalWidth, leftWidth + rightWidth);
			width += sashWidth;
			return width;
		}
		
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			Point size = layout(false, flushCache);
			return size;
		}
		
		protected void layout(Composite composite, boolean flushCache) {
			layout(true, flushCache);
		}
		
		private Point layout(boolean move, boolean flushCache) {
			int width = composite.getSize().x;

			if (loadingLabel.isVisible()) {
				return layoutLoadingLabel(move, width, flushCache);
			} else {
				return layoutRows(move, width, flushCache);
			}
		}
		
		private Point layoutLoadingLabel(boolean move, int width, boolean flushCache) {
			Point size = loadingLabel.computeSize(width, SWT.DEFAULT, flushCache);
			
			if (move) {
				loadingLabel.setSize(size);
			}
			
			return size;

		}
		
		private Point layoutRows(boolean move, int width, boolean flushCache) {
			if (widgetRows == null) {
				return new Point(0, 0);
			}
			
			int sashWidth = sash.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			
			int sashX;
			if (currentSashXPercent != SASH_X_NOTSET) {
				sashX = percentToAbsolute(width, currentSashXPercent);
			} else {
				// not yet set
				
				int cfgSashXPercent = Hub.localCfg.get(CFG_SASH_X_PERCENT, SASH_X_NOTSET); 
				if (cfgSashXPercent != SASH_X_NOTSET && cfgSashXPercent < 100) {
					sashX = percentToAbsolute(width, cfgSashXPercent);
				} else {
					// default: 2/3 of width
					sashX = percentToAbsolute(width, SASH_X_DEFAULT_PERCENT);
				}
			}
			
			int leftX = 0;
			int leftWidth = sashX;
			
			int rightX = sashX + sashWidth;
			int rightWidth = width - rightX;
			
			int y = 0;
			
			for (WidgetRow row : widgetRows) {
				if (row.konsData == null) {
					// ignore
					continue;
				}
				
				int currentHeight;
				
				Point konsTitleSize = row.hTitle.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
				Point fallTitleSize = row.lFall.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
				
				int konsTitleWidth = konsTitleSize.x;
				int fallTitleWidth = Math.min(width - konsTitleWidth, fallTitleSize.x);
				currentHeight = Math.max(konsTitleSize.y, fallTitleSize.y);
				
				if (move) {
					row.hTitle.setBounds(leftX, y, konsTitleWidth, currentHeight);
					row.lFall.setBounds(width - fallTitleWidth, y, fallTitleWidth, currentHeight);
				}
				
				y += currentHeight + TITLE_SPACING;
				
				Point etfSize = row.etf.computeSize(leftWidth, SWT.DEFAULT, flushCache);
				Point verrechnungSize = row.verrechnung.computeSize(rightWidth, SWT.DEFAULT, flushCache);
				currentHeight = Math.max(etfSize.y, verrechnungSize.y);
				
				if (move) {
					row.etf.setBounds(leftX, y, leftWidth, currentHeight);
					row.verrechnung.setBounds(rightX, y, rightWidth, currentHeight);
					row.verticalSeparator.setBounds(leftX + leftWidth, y, 1, currentHeight);
					row.horizontalSeparator.setBounds(leftX, y + currentHeight, width, 1);
				}
				
				y += currentHeight + 1 + ROW_SPACING;  // grow including border
			}
			
			int height = y - ROW_SPACING;  // the last ROW_SPACING is too much

			if (move) {
				sash.setBounds(sashX, 0, sashWidth, height);
			}
			
			Point size = new Point(width, height); 
			return size;
		}
	}

	public static class KonsData {
		Konsultation konsultation;
		
		// cache fields
		String konsTitle;
		String fallTitle;
		String konsText;
		String verrechnungenText;
		
		public KonsData(Konsultation konsultation) {
			this.konsultation = konsultation;
			
			updateCacheFields();
		}
		
		private void updateCacheFields() {
			if (konsultation != null) {
				String lineSeparator = System.getProperty("line.separator");

				konsTitle = konsultation.getLabel();
				fallTitle = konsultation.getFall().getLabel();

				konsText = konsultation.getEintrag().getHead();
				if (konsText == null) {
					konsText = "";
				}

				List<Verrechnet> leistungen = konsultation.getLeistungen();
				List<String> leistungenLabels = replaceBlocks(leistungen);

				StringBuffer sb = new StringBuffer();
				boolean isFirst = true;
				for (String leistungLabel : leistungenLabels) {
					if (isFirst) {
						isFirst = false;
					} else {
						sb.append(lineSeparator);
					}
					sb.append(leistungLabel);
				}

				verrechnungenText = sb.toString();
			} else {
				konsTitle = "";
				fallTitle = "";
				konsText = "";
				verrechnungenText = "";
			}
		}

		private List<String> replaceBlocks(List<Verrechnet> leistungen) {
			List<String> labels = new ArrayList<String>();

			/*
			List<Verrechnet> unassigned = new ArrayList<Verrechnet>();
			unassigned.addAll(leistungen);
			List<Verrechnet> assigned = new ArrayList<Verrechnet>();
			*/
			
			// TODO consider number of elements in blocks
			/*
			Query<Leistungsblock> query = new Query<Leistungsblock>(Leistungsblock.class);
			query.orderBy(false, "Name");
			List<Leistungsblock> blocks = query.execute();
			if (blocks != null) {
				for (Leistungsblock block : blocks) {
					if (containsBlock(unassigned, block)) {
						removeBlock(unassigned, block);
						// TODO sum
						labels.add(block.getName());
					}
				}
			}
			*/

			// add remaining leistungen
			//for (Verrechnet leistung : unassigned) {
			
			for (Verrechnet leistung : leistungen) {
				labels.add(verrechnetLabelProvider.getText(leistung));
			}

			return labels;
		}
	}
}
