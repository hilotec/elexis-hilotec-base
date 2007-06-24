/*******************************************************************************
 * Copyright (c) 2006, G. Weirich, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Lutz - initial implementation based on RnPrintView
 *
 * $Id: KGPrintView.java 1728 2007-02-03 10:08:17Z rgw_ch $
 *******************************************************************************/

package ch.elexis.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Hub;
import ch.elexis.data.*;
import ch.elexis.text.ReplaceCallback;
import ch.elexis.text.TextContainer;
import ch.elexis.text.ITextPlugin.ICallback;

public class KGPrintView extends ViewPart {
    public static final String ID="ch.elexis.views.KGPrintView";

    private static final String TEMPLATE = "KG-Deckblatt";

    CTabFolder ctab;
    private int existing;


    public KGPrintView() {
    }

    @Override
    public void createPartControl(Composite parent) {
        ctab=new CTabFolder(parent,SWT.BOTTOM);
        ctab.setLayout(new FillLayout());

    }

    CTabItem addItem(final String template, final String title, final Kontakt adressat){
        CTabItem ret=new CTabItem(ctab,SWT.NONE);
        TextContainer text=new TextContainer(getViewSite());
        ret.setControl(text.getPlugin().createContainer(ctab,new ICallback(){
            public void save() {}
            public boolean saveAs() {
                return false;
            }

        }));
        Brief actBrief=text.createFromTemplateName(Konsultation.getAktuelleKons(),template,Brief.UNKNOWN,adressat, "KG");
        ret.setData("brief",actBrief);
        ret.setData("text",text);
        ret.setText(title);
        return ret;
    }
    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }


    @Override
    public void dispose() {
        clearItems();
        super.dispose();
    }

    public void clearItems(){
        for(int i=0;i<ctab.getItems().length;i++){
            useItem(i,null, null);
        }
    }
    public void useItem(int idx, String template, Kontakt adressat){
        CTabItem item=ctab.getItem(idx);
        Brief brief=(Brief)item.getData("brief");
        TextContainer text=(TextContainer)item.getData("text");
        text.saveBrief(brief,Brief.UNKNOWN);
        String betreff=brief.getBetreff();
        brief.delete();
        if(template!=null){
            Brief actBrief=text.createFromTemplateName(Konsultation.getAktuelleKons(),template,Brief.UNKNOWN,adressat,betreff);
            item.setData("brief",actBrief);
        }
    }
    /**
     * Drukt die KG-Huelle anhand der Vorlage "KG"
     * @param pat der Patient
     * @param monitor
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean doPrint(Patient pat, IProgressMonitor monitor){
        monitor.subTask(pat.getLabel());

        // TODO ?
        //GlobalEvents.getInstance().fireSelectionEvent(rn,getViewSite());

        existing=ctab.getItems().length;
        String printer=null;
        String tray=null;

        CTabItem ctKG;
        TextContainer text;

        if (--existing < 0) {
            ctKG = addItem(TEMPLATE, "KG", null);
        } else {
            ctKG = ctab.getItem(0);
            useItem(0, TEMPLATE, null);
        }

        text = (TextContainer) ctKG.getData("text");

        text.getPlugin().setFont("Serif", SWT.NORMAL, 9);
        text.replace("\\[Elexis\\]", new ReplaceCallback() {
            public String replace(String in) {
                return "ELEXIS";
            }

        });

        printer = Hub.localCfg.get("Drucker/A4/Name", null);
        tray = Hub.localCfg.get("Drucker/A4/Schacht", null);
        if (text.getPlugin().print(printer, tray, false) == false) {
            return false;
        }
        monitor.worked(1);
        return true;
    }
}