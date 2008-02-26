/*******************************************************************************
 * Copyright (c) 2006-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: TextContainer.java 3707 2008-02-26 09:22:50Z rgw_ch $
 *******************************************************************************/

package ch.elexis.text;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;

import ch.elexis.Desk;
import ch.elexis.Hub;
import ch.elexis.actions.GlobalEvents;
import ch.elexis.data.Brief;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.elexis.dialogs.KontaktSelektor;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.Log;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.ScriptUtil;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class TextContainer {
	
	private ITextPlugin plugin;
	private static Log log=Log.get("TextContainer");
	private Shell shell;
	private static final String TEMPLATE_REGEXP="\\[[-a-zA-ZäöüÄÖÜéàè]+\\.[-a-zA-Z0-9äöüÄÖÜéàè]+\\]";
	private static final String GENDERIZE_REGEXP="\\[[a-zA-Z]+:mwn?:[^\\[]+\\]";
	private static final String IDATACCESS_REGEXP="\\[[-_a-zA-Z0-9]+:[-a-zA-Z0-9]+:[-a-zA-Z0-9\\.]+:[-a-zA-Z0-9\\.]:?.*\\]"; 
	
	/**
	 * Der Konstruktor sucht nach dem in den Settings definierten Textplugin Wenn er kein Textplugin findet, wählt er
	 * ein rudimentäres Standardplugin aus (das in der aktuellen Version nur eine
	 * Fehlermeldung ausgibt)
	 */
	public TextContainer(){
		if(plugin==null){
			String ExtensionToUse=Hub.localCfg.get(PreferenceConstants.P_TEXTMODUL, null);
			IExtensionRegistry exr=Platform.getExtensionRegistry();
			IExtensionPoint exp=exr.getExtensionPoint("ch.elexis.Text");
			if(exp!=null){
				IExtension[] extensions=exp.getExtensions();
				for(IExtension ex:extensions){
					IConfigurationElement[] elems=ex.getConfigurationElements();
					for(IConfigurationElement el:elems){
						if((ExtensionToUse==null) || el.getAttribute("name").equals(ExtensionToUse)){
							try {
								plugin=(ITextPlugin)el.createExecutableExtension("Klasse");
							} catch (/*Core*/Exception e) {
								ExHandler.handle(e);
							}
						}
						
					}
				}
			}
		}
		if(plugin==null){
			plugin= new DefaultTextPlugin();
		}
	}
	public TextContainer(final IViewSite s){
		this();
		shell=s.getShell();
	}
	
	public TextContainer(final Shell s){
		this();
		shell=s;
	}
	public void setFocus(){
		plugin.setFocus();
	}
	public ITextPlugin getPlugin(){
		return plugin;
	}
	public void dispose(){
		plugin.dispose();
	}
	/**
	 * Ein Dokument aus einer namentlich genannten Vorlage erstellen. Die Vorlage muss entweder dem
	 * aktuellen Mandanten oder allen Mandanten zugeordet sein. 
	 * @param templatename	Name der Vorlage
	 * @param typ	Typ des zu erstellenden Dokuments
	 * @param adressat	Adressat
	 * @param subject TODO
	 * @return	Ein Brief-Objekt oder null bei Fehler
	 */
	public Brief createFromTemplateName(final Konsultation kons, final String templatename, final String typ, final Kontakt adressat, final String subject){
		Query<Brief> qbe=new Query<Brief>(Brief.class);
		qbe.add("Typ","=",Brief.TEMPLATE);
		qbe.and();
		qbe.add("Betreff","=",templatename);
		qbe.startGroup();
		qbe.add("DestID","=",Hub.actMandant.getId());
		qbe.or();
		qbe.add("DestID","=","");
		qbe.endGroup();
		List<Brief> list=qbe.execute();
		if((list==null) || (list.size()==0)){
			SWTHelper.showError("Dokumentvorlage nicht gefunden", "Die benötigte Formatvorlage "+templatename+" wurde nicht gefunden.");
			return null;
		}
		Brief template=list.get(0);
		return createFromTemplate(kons, template,typ,adressat, subject);
	}
	
	/**
	 * Ein Dokument aus einer Vorlage erstellen. Dabei werden Datensatz-Variablen
	 * durch die entsprechenden Inhalte ersetzt und geschlechtsspezifische Formulierungen
	 * entsprechend gewählt.
	 * @param template die Vorlage
	 * @param typ Typ des zu erstellenden Dokuments
	 * @param subject TODO
	 * @param Adressat der Adressat
	 * @return true bei Erfolg
	 */
	public Brief createFromTemplate(final Konsultation kons, final Brief template, final String typ, Kontakt adressat, final String subject){
		if(adressat==null){
			KontaktSelektor ksel=new KontaktSelektor(shell,
				Kontakt.class,"Adressaten auswählen","Bitte wählen Sie den Adressaten für den Brief aus");
			if(ksel.open()!=Dialog.OK){
				return null;
			}
			adressat=(Kontakt)ksel.getSelection();
		}
		// Konsultation kons=getBehandlung();
		if(template==null){
			if (plugin.createEmptyDocument()) {
				Brief brief=new Brief(subject==null ? "leeres Dokument" : subject,null,Hub.actUser,adressat,kons,typ);
				addBriefToKons(brief, kons);
				return brief;
			}
		}else{
			if(plugin.loadFromByteArray(template.loadBinary(), true)==true){
				final Brief ret=new Brief(subject==null ? template.getBetreff():subject,null,Hub.actUser,adressat,kons,typ);

				plugin.findOrReplace(TEMPLATE_REGEXP,new ReplaceCallback(){
					public Object replace(final String in) {
						return replaceFields(ret,in.replaceAll("[\\[\\]]",""));
					}
				});
				plugin.findOrReplace(GENDERIZE_REGEXP,new ReplaceCallback(){
					public String replace(final String in) {
						return genderize(ret,in.replaceAll("[\\[\\]]",""));
					}
				});
				plugin.findOrReplace(IDATACCESS_REGEXP,new ReplaceCallback(){
					public Object replace(final String in) {
						String[][] ref=ScriptUtil.loadDataFromPlugin(in.replaceAll("[\\[\\]]",""));
						return ref;
					}
				});
				saveBrief(ret,typ);
				addBriefToKons(ret, kons);
				return ret;
			}
		}
		return null;
	}
	private Object replaceFields(final Brief brief, final String b){
		String[] q=b.split("\\.");
		if(q.length!=2){
			log.log("falsches Variablenformat "+b,Log.WARNINGS);		// Kann eigentlich nie vorkommen ?!?
			return null;
		}
		if(q[0].equals("Datum")){
			return new TimeTool().toString(TimeTool.DATE_GER);
		}
		if(q[0].indexOf(":")!=-1){
			String[][] ref=ScriptUtil.loadDataFromPlugin(b);
			return ref;
		}
		PersistentObject o=resolveObject(brief,q[0]);
		if(o==null){
			return "??"+b+"??";
		}
		/*
		int pdp=q[1].indexOf(':');
		if(pdp!=-1){
			String pid=o.get(q[1].substring(0,pdp));
			if(!StringTool.isNothing(pid)){
				
			}
			String plf=q[1].substring(pdp+1);
			
			PersistentObject po=resolveObject(brief,q[1].substring(0,pdp));
		}*/
		String ret=o.get(q[1]);
		if((ret==null) || (ret.startsWith("**"))){
			/*if(o instanceof Kontakt){
				if(q[1].equals("Anschrift")){
					String an=((Kontakt)o).getPostAnschrift(true).replaceAll("\\r","");
					return an;
				}
			}*/
			if(!(o.map("ExtInfo").startsWith("**"))){
				Hashtable ext=o.getHashtable("ExtInfo");
				String an=(String)ext.get(q[1]);
				if(an!=null){
					return an;
				}
			}
			log.log("Nicht erkanntes Feld in "+b,Log.WARNINGS);
			return "???"+b+"???";
		}
		
		if(ret.startsWith("<?xml")){
			Samdas samdas=new Samdas(ret);
			ret= samdas.getRecordText();
		}
		return ret; 
	}
	/**
	 * Format für Genderize:
	 * [Feld:mw:formulierung Mann/formulierung Frau] oder
	 * [Feld:mwn:mann/frau/neutral]
	 */
	private String genderize(final Brief brief, final String in) {
		String[] q=in.split(":");
		PersistentObject o=resolveObject(brief, q[0]);
		if(o==null){
			return "???";
		}
		if(q.length!=3){
			log.log("falsches genderize Format "+in,Log.ERRORS);
			return null;
		}
		if(!(o instanceof Kontakt)){
			return"<* Feldtyp nur für Kontakte *>";
		}
		Kontakt k=(Kontakt)o;
		String[] g=q[2].split("/");
		if(g.length<2){
				return"<* falsch defniertes Feld *>";
		}
		if(k.istPerson()){
			Person p=Person.load(k.getId());
			
			if(p.get("Geschlecht").equalsIgnoreCase("m")){
				if(q[1].startsWith("m")){
					return g[0];
				}
				return g[1];
			}else{
				if(q[1].startsWith("w")){
					return g[0];
				}
				return g[1];
			}	
		}else{
			if(g.length<3){
				return "<* Feld nur für Personen definiert *>";
			}
			return g[2];
		}
	}

	private PersistentObject resolveObject(final Brief actBrief, final String k){
		PersistentObject ret=null;
		if(k.equalsIgnoreCase("Mandant")){
			ret=Hub.actMandant;
		}else if(k.equalsIgnoreCase("Anwender")){
			ret=Hub.actUser;
		}else if(k.equalsIgnoreCase("Adressat")){
			ret=actBrief.getAdressat();
		}else{
			try{
				String fqname="ch.elexis.data."+k;
				ret=GlobalEvents.getInstance().getSelectedObject(Class.forName(fqname));
			}catch(Throwable ex){
				log.log("Nicht erkannter Feldtyp in "+k,Log.WARNINGS);
				ret= null;
			}
		}
		if(ret==null){
			log.log("Nicht erkannter Feldtyp in "+k,Log.WARNINGS);
		}
		return ret;
	}
	
	
	
	private void addBriefToKons(final Brief brief, final Konsultation kons){
		if(kons!=null){
			String label="\n[ "+brief.getLabel()+" ]";
			kons.addXRef(XrefExtension.providerID, brief.getId(), -1, label);
		}
	}
		
	/** 
	 * Dokument speichern. Wenn noch kein Adressat vorhanden ist, wird eine Auswahl angeboten.
	 * @param brief das zu speichernde Dokument
	 * @param typ Typ des Dokuments
	 */
	public void saveBrief(Brief brief, final String typ){
		if((brief==null) || (brief.getAdressat()==null)){
			KontaktSelektor ksl=new KontaktSelektor(shell,Kontakt.class,"Adressat auswählen","Geben Sie bitte den Adressaten für den Brief an");
			if(ksl.open()==Dialog.OK){
				brief=new Brief("ein Brief",null,Hub.actUser,(Kontakt)ksl.getSelection(),Konsultation.getAktuelleKons(),typ);		
			}
		}
		if(brief!=null){
			if(StringTool.isNothing(brief.getBetreff())){
				InputDialog dlg=new InputDialog(shell,"Dokument speichern","Geben Sie bitte einen Titel oder Betreff für das Dokument ein",brief.getBetreff(),null);
				if(dlg.open()==Dialog.OK){
					brief.setBetreff(dlg.getValue());
				}else{
					brief.setBetreff(brief.getTyp());
				}
			}
			byte[] contents=plugin.storeToByteArray();
			if(contents==null){
				log.log("Nullwert beim Speichern",Log.ERRORS);
			}
			brief.save(contents,plugin.getMimeType());
			GlobalEvents.getInstance().fireUpdateEvent(Brief.class);
		}
	}
	
	/**
	 * Den Aktuellen Inhalt des Textpuffers als Vorlage speichern. 
	 * Name und zuzuordender Mandant werden per Dialog erfragt.
	 *
	 */
	public void saveTemplate(String name){
		SaveTemplateDialog std=new SaveTemplateDialog(shell,name);
		//InputDialog dlg=new InputDialog(getViewSite().getShell(),"Vorlage speichern","Geben Sie bitte einen Namen für die Vorlage ein","",null);
		if(std.open()==Dialog.OK){
			String title=std.title;
			Brief brief=new Brief(title,null,Hub.actUser,std.selectedMand,null,Brief.TEMPLATE);
			if(std.bSysTemplate){
				brief.set("BehandlungsID", "SYS");
			}
			byte[] tmpl=plugin.storeToByteArray();
			if(tmpl==null){
				log.log("Null wert beim Speichern des Template",Log.ERRORS);
			}
			brief.save(tmpl,plugin.getMimeType());
			//text.clear();
		}
	}
	/** Einen Brief einlesen */
	public boolean open(final Brief brief) {
		if(brief==null){
			log.log("Null brief zum öffnen", Log.WARNINGS);
			return false;
		}
		System.out.print(brief.getLabel());
		byte [] arr=brief.loadBinary();
		if(arr==null){
			log.log("Fehlerhafter Brief in Datenbank "+brief.getLabel(),Log.WARNINGS);
			return false;
		}
		return plugin.loadFromByteArray(arr, false);
	}


	class SaveTemplateDialog extends TitleAreaDialog{
		Text name;
		Combo cMands;
		String title;
		Button btSysTemplate;
		boolean bSysTemplate;
		List<Mandant> lMands;
		Mandant selectedMand;
		String tmplName;
		protected SaveTemplateDialog(final Shell parentShell,String templateName) {
			super(parentShell);
			tmplName=templateName;
		}

		@Override
		public void create() {
			super.create();
			setTitle("Dokumentvorlage speichern");
			setMessage("Bitte geben Sie einen Namen für die Vorlage, und den Mandanten bei dem sie auftauchen soll ein");
			getShell().setText("Dokumentvorlage");
		}

		@Override
		protected Control createDialogArea(final Composite parent) {
			Composite ret=new Composite(parent,SWT.NONE);
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			ret.setLayout(new GridLayout());
			new Label(ret,SWT.NONE).setText("Name der Vorlage");
			name=new Text(ret,SWT.BORDER);
			name.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			if(tmplName!=null){
				name.setText(tmplName);
			}
			new Label(ret,SWT.NONE).setText("Mandant");
			Composite line=new Composite(ret,SWT.NONE);
			line.setLayout(new FillLayout());
			line.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			cMands=new Combo(line,SWT.SINGLE);
			Query<Mandant> qbe=new Query<Mandant>(Mandant.class);
			lMands=qbe.execute();
			cMands.add("Alle");
			for(Mandant m:lMands){
				cMands.add(m.getLabel());
			}
			btSysTemplate=new Button(line,SWT.CHECK);
			btSysTemplate.setText("Als System-Vorlage");
			return ret;
		}

		@Override
		protected void okPressed() {
			title=name.getText();
			bSysTemplate=btSysTemplate.getSelection();
			int i=cMands.getSelectionIndex();
			if(i!=-1){
				if(i==0){
					selectedMand=null;
				}else{
					selectedMand=lMands.get(i-1);
				}
			}
			Query<Brief> qbe=new Query<Brief>(Brief.class);
			qbe.add("Typ","=",Brief.TEMPLATE);
			if(selectedMand!=null){
				qbe.startGroup();
					qbe.add("DestID","=",selectedMand.getId());
					qbe.or();
					qbe.add("DestID", "=", "");
				qbe.endGroup();
				qbe.and();
			}
			qbe.add("geloescht","<>","1");
			qbe.add("Betreff","=",title);
			List<Brief> l=qbe.execute();
			if(l.size()>0){
				if(MessageDialog.openQuestion(getShell(), "Vorlage schon vorhanden", "Soll die vorhandene Vorlage mit demselben Namen überschrieben werden?")){
					Brief old=l.get(0);
					old.delete();
				}else{
					return;
				}
			}
			super.okPressed();
		}
		
	}
	public boolean replace(final String pattern, final ReplaceCallback cb){
		return plugin.findOrReplace(pattern,cb);
	}
	
	public boolean replace(final String pattern, final String repl){
		return plugin.findOrReplace(pattern,new ReplaceCallback(){
			public String replace(final String in) {
				return repl;
			}
		});
	}
	static class DefaultTextPlugin implements ITextPlugin{
		private static final String expl="<form>Es konnte keine Verbindung mit einem Textprogramm "+
		"hergestellt werden. Mögliche Gründe könnten sein:"+
		"<li>Es ist kein Text-Plugin geladen</li>"+
		"<li>Das Text-Plugin wurde nicht richtig konfiguriert</li>"+
		"<li>Ein externes Textprogramm wurde gelöscht</li></form>";
		
		public Composite createContainer(final Composite parent, final ITextPlugin.ICallback h) {
			parent.setLayout(new FillLayout());
			//Composite ret=new Composite(parent,SWT.BORDER);
			Form form=Desk.theToolkit.createForm(parent);
			form.setText("Texterstellung nicht möglich");
			form.getBody().setLayout(new FillLayout());
			FormText ft=Desk.theToolkit.createFormText(form.getBody(),false);
			ft.setText(expl,true,false);
			return form.getBody();
		}

		public void dispose() {
		}

		public void showMenu(final boolean b) {}

		public void showToolbar(final boolean b) {}

		public boolean createEmptyDocument() {
			return false;
		}

		public boolean loadFromByteArray(final byte[] bs, final boolean asTemplate) {
			return false;
		}

		public boolean findOrReplace(final String pattern, final ReplaceCallback cb) {
			return false;
		}

		public byte[] storeToByteArray() {
			return null;
		}

		public boolean clear() {
			return false;
		}

		public void setInitializationData(final IConfigurationElement config, final String propertyName, final Object data) throws CoreException {
		}

		public boolean loadFromStream(final InputStream is, final boolean asTemplate) {
			// TODO Automatisch erstellter Methoden-Stub
			return false;
		}
		public boolean print(final String printer, final String tray, final boolean waitUntilFinished){
			return false;
		}

		public boolean insertTable(final String marke, final int props, final String[][] contents, final int[] columnSizes) {
			return false;
		}
		public void setFocus(){
			
		}

		public PageFormat getFormat() {
			return PageFormat.USER;
		}

		public void setFormat(final PageFormat f) {
			
		}

		public Object insertTextAt(final int x, final int y, final int w, final int h, final String text, final int adjust) {
				return null;
		}

		public boolean setFont(final String name, final int style, final float size) {
			return false;
		}

		public Object insertText(final String marke, final String text, final int adjust) {
			// TODO Auto-generated method stub
			return null;
		}

		public Object insertText(final Object pos, final String text, final int adjust) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getMimeType() {
			return "text/nothing";
		}

		public void setSaveOnFocusLost(final boolean bSave) {
			// TODO Auto-generated method stub
			
		}
	}
	
}
