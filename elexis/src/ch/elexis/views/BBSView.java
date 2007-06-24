// $Id: BBSView.java 1050 2006-10-05 21:05:45Z rgw_ch $
/*
 * Created on 10.09.2005
 */
package ch.elexis.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.LazyTreeLoader;
import ch.elexis.actions.GlobalActions;
import ch.elexis.data.BBSEntry;
import ch.elexis.data.Query;
import ch.elexis.util.CommonViewer;
import ch.elexis.util.DefaultControlFieldProvider;
import ch.elexis.util.SimpleWidgetProvider;
import ch.elexis.util.Tree;
import ch.elexis.util.TreeContentProvider;
import ch.elexis.util.ViewerConfigurer;

/**
 * Bulletin Board System - ein Schwarzes Brett. Im Prinzip Erweiterung
 * des Reminder-Konzepts zu Threads ähnlich newsreader und Webforen.
 * @author gerry
 */
public class BBSView extends ViewPart implements ISelectionChangedListener, ISaveablePart2{
    public static final String ID="ch.elexis.BBSView";
    private CommonViewer headlines;
    private ViewerConfigurer vc;
    private ScrolledForm form;
    private FormToolkit tk;
    private Query<BBSEntry> qbe;
    private LazyTreeLoader loader;
    private Label origin;
    private FormText msg;
    private Text input;
    

    @Override
    public void createPartControl(Composite parent)
    {
        SashForm sash=new SashForm(parent,SWT.NONE);
        qbe=new Query<BBSEntry>(BBSEntry.class);
        loader=new LazyTreeLoader("BBS",qbe,"reference",new String[]{"datum","time","Thema"});
        headlines=new CommonViewer();
		vc=new ViewerConfigurer(
				new TreeContentProvider(headlines,loader),
				new ViewerConfigurer.TreeLabelProvider(),
				new DefaultControlFieldProvider(headlines, new String[]{"Thema"}),
				new NewThread(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_TREE, SWT.NONE, null)
				);
		headlines.create(vc,sash,SWT.NONE,getViewSite());
		
        tk=new FormToolkit(Desk.theDisplay);
        form=tk.createScrolledForm(sash);
        form.getBody().setLayout(new GridLayout(1,false));
        form.setText("Bitte links ein Thema auswählen");
        origin=tk.createLabel(form.getBody(),"");
        GridData gd=new GridData(GridData.FILL_HORIZONTAL);
        origin.setLayoutData(gd);
        msg=tk.createFormText(form.getBody(),false);
        gd=new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL|GridData.FILL_VERTICAL);
        msg.setLayoutData(gd);
        msg.setColor("rot",Desk.theColorRegistry.get("rot"));
        msg.setColor("grün",Desk.theColorRegistry.get("grün"));
        msg.setColor("blau",Desk.theColorRegistry.get("blau"));
        input=tk.createText(form.getBody(),"",SWT.WRAP|SWT.MULTI|SWT.BORDER);
        gd=new GridData(GridData.FILL_HORIZONTAL|GridData.FILL_VERTICAL);
        input.setLayoutData(gd);
        Button send=tk.createButton(form.getBody(),"Senden",SWT.PUSH);
        send.addSelectionListener(new SelectionAdapter(){
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				Object[] sel=headlines.getSelection();
				if(sel==null || sel.length==0){
					return;
				}
				Tree item=(Tree)sel[0];
				BBSEntry en=(BBSEntry)(item).contents;
				BBSEntry ne=new BBSEntry(en.getTopic(),Hub.actUser,en,input.getText());
				Tree child=item.add(ne);
				((TreeViewer)headlines.getViewerWidget()).add(sel[0],child);
				((TreeViewer)headlines.getViewerWidget()).setSelection(new StructuredSelection(child),true);
			}
        	
        });
        headlines.getViewerWidget().addSelectionChangedListener(this);
        ((TreeContentProvider)headlines.getConfigurer().getContentProvider()).startListening();
        setDisplay();
    }

    @Override
    public void setFocus()
    {
        // TODO Auto-generated method stub

    }
    
    /* (Kein Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
        ((TreeContentProvider)headlines.getConfigurer().getContentProvider()).stopListening();
		super.dispose();
	}

	public void setDisplay(){
    	Object[] sel=headlines.getSelection();
    	if(sel==null || sel.length==0){
    		form.setText("Keine Nachricht ausgewählt");
    		return;
    	}
    	BBSEntry en=(BBSEntry)((Tree)sel[0]).contents;
    	form.setText(en.getTopic());
    	StringBuilder sb=new StringBuilder();
    	sb.append(en.getAuthor().getLabel()).append(" schrieb am ")
    		.append(en.getDate()).append(" um ")
    		.append(en.getTime()).append(" Uhr:");
    	origin.setText(sb.toString());
    	msg.setText("<form><p>"+en.getText()+"</p></form>",true,true);
    	input.setText("");
    }

    class NewThread implements ViewerConfigurer.ButtonProvider{

		public Button createButton(Composite parent) {
			Button ret=new Button(parent,SWT.PUSH);
			ret.setText("Neues Thema...");
			ret.addSelectionListener(new SelectionAdapter(){
	
				@Override
				public void widgetSelected(SelectionEvent e) {
					new BBSEntry(headlines.getConfigurer().getControlFieldProvider().getValues()[0],
							Hub.actUser,null,"");
				loader.invalidate();
				headlines.notify(CommonViewer.Message.update);
				setDisplay();
				}
				
			});
			return ret;
		}
	
		public boolean isAlwaysEnabled() {
				return false;
		}
	    	
    }
    public void selectionChanged(SelectionChangedEvent event) {
    	setDisplay();
    }
    /* ******
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2
	 * Wir benötigen das Interface nur, um das Schliessen einer View zu verhindern,
	 * wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */ 
	public int promptToSaveOnClose() {
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL : ISaveablePart2.NO;
	}
	public void doSave(IProgressMonitor monitor) { /* leer */ }
	public void doSaveAs() { /* leer */}
	public boolean isDirty() {
		return true;
	}
	public boolean isSaveAsAllowed() {
		return false;
	}
	public boolean isSaveOnCloseNeeded() {
		return true;
	}
}
