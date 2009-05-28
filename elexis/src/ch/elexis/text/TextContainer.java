/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: TextContainer.java 5321 2009-05-28 12:06:28Z rgw_ch $
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
import ch.elexis.data.Fall;
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

	private static final String WARNING_SIGN = "??"; //$NON-NLS-1$
	private static final String EXTENSION_POINT_TEXT = "ch.elexis.Text"; //$NON-NLS-1$
	private static final String MATCH_SQUARE_BRACKET = "[\\[\\]]"; //$NON-NLS-1$
	private static final String TEMPLATE_NOT_FOUND_HEADER=Messages.TextContainer_TemplateNotFoundHeader;
	private static final String TEMPLATE_NOT_FOUND_BODY = Messages.TextContainer_TemplateNotFoundBody;

	private ITextPlugin plugin = null;
	private static Log log = Log.get("TextContainer"); //$NON-NLS-1$
	private Shell shell;
	public static final String MATCH_TEMPLATE = "\\[[-a-zA-ZäöüÄÖÜéàè]+\\.[-a-zA-Z0-9äöüÄÖÜéàè]+\\]"; //$NON-NLS-1$
	public static final String MATCH_INDIRECT_TEMPLATE = "\\[[-a-zA-ZäöüÄÖÜéàè]+(\\.[-a-zA-Z0-9äöüÄÖÜéàè]+)+\\]"; //$NON-NLS-1$
	public static final String MATCH_GENDERIZE = "\\[[a-zA-Z]+:mwn?:[^\\[]+\\]"; //$NON-NLS-1$
	public static final String MATCH_IDATACCESS = "\\[[-_a-zA-Z0-9]+:[-a-zA-Z0-9]+:[-a-zA-Z0-9\\.]+:[-a-zA-Z0-9\\.]:?.*\\]"; //$NON-NLS-1$

	/**
	 * Der Konstruktor sucht nach dem in den Settings definierten Textplugin
	 * Wenn er kein Textplugin findet, wählt er ein rudimentäres Standardplugin
	 * aus (das in der aktuellen Version nur eine Fehlermeldung ausgibt)
	 */
	public TextContainer() {
		if (plugin == null) {
			String ExtensionToUse = Hub.localCfg.get(
					PreferenceConstants.P_TEXTMODUL, null);
			IExtensionRegistry exr = Platform.getExtensionRegistry();
			IExtensionPoint exp = exr.getExtensionPoint(EXTENSION_POINT_TEXT);
			if (exp != null) {
				IExtension[] extensions = exp.getExtensions();
				for (IExtension ex : extensions) {
					IConfigurationElement[] elems = ex
							.getConfigurationElements();
					for (IConfigurationElement el : elems) {
						if ((ExtensionToUse == null)
								|| el.getAttribute("name").equals( //$NON-NLS-1$
										ExtensionToUse)) {
							try {
								plugin = (ITextPlugin) el
										.createExecutableExtension("Klasse"); //$NON-NLS-1$
							} catch (/* Core */Exception e) {
								ExHandler.handle(e);
							}
						}

					}
				}
			}
		}
		if (plugin == null) {
			plugin = new DefaultTextPlugin();
		}
	}

	public TextContainer(final IViewSite s) {
		this();
		shell = s.getShell();
	}

	public TextContainer(final Shell s) {
		this();
		shell = s;
	}

	public void setFocus() {
		plugin.setFocus();
	}

	public ITextPlugin getPlugin() {
		return plugin;
	}

	public void dispose() {
		plugin.dispose();
	}

	/**
	 * Ein Dokument aus einer namentlich genannten Vorlage erstellen. Die
	 * Vorlage muss entweder dem aktuellen Mandanten oder allen Mandanten
	 * zugeordet sein.
	 * 
	 * @param templatename
	 *            Name der Vorlage
	 * @param typ
	 *            Typ des zu erstellenden Dokuments
	 * @param adressat
	 *            Adressat
	 * @param subject
	 *            TODO
	 * @return Ein Brief-Objekt oder null bei Fehler
	 */


	public Brief createFromTemplateName(final Konsultation kons,
			final String templatename, final String typ,
			final Kontakt adressat, final String subject) {
		Query<Brief> qbe = new Query<Brief>(Brief.class);
		qbe.add(Brief.TYPE, Query.EQUALS, Brief.TEMPLATE);
		qbe.and();
		qbe.add(Brief.SUBJECT, Query.EQUALS, templatename);
		qbe.startGroup();
		qbe.add(Brief.DESTINATION_ID, Query.EQUALS, Hub.actMandant.getId());
		qbe.or();
		qbe.add(Brief.DESTINATION_ID, Query.EQUALS, StringTool.leer);
		qbe.endGroup();
		List<Brief> list = qbe.execute();
		if ((list == null) || (list.size() == 0)) {
			SWTHelper.showError(TEMPLATE_NOT_FOUND_HEADER,
					TEMPLATE_NOT_FOUND_BODY + templatename);
			return null;
		}
		Brief template = list.get(0);
		return createFromTemplate(kons, template, typ, adressat, subject);
	}

	/**
	 * Ein Dokument aus einer Vorlage erstellen. Dabei werden
	 * Datensatz-Variablen durch die entsprechenden Inhalte ersetzt und
	 * geschlechtsspezifische Formulierungen entsprechend gewählt.
	 * 
	 * @param template
	 *            die Vorlage
	 * @param typ
	 *            Typ des zu erstellenden Dokuments
	 * @param subject
	 *            TODO
	 * @param Adressat
	 *            der Adressat
	 * @return true bei Erfolg
	 */
	public Brief createFromTemplate(final Konsultation kons,
			final Brief template, final String typ, Kontakt adressat,
			final String subject) {
		if (adressat == null) {
			KontaktSelektor ksel = new KontaktSelektor(shell, Kontakt.class,
					Messages.TextContainer_SelectDestinationHeader,
					Messages.TextContainer_SelectDestinationBody);
			if (ksel.open() != Dialog.OK) {
				return null;
			}
			adressat = (Kontakt) ksel.getSelection();
		}
		// Konsultation kons=getBehandlung();
		if (template == null) {
			if (plugin.createEmptyDocument()) {
				Brief brief = new Brief(subject == null ? Messages.TextContainer_EmptyDocument
						: subject, null, Hub.actUser, adressat, kons, typ);
				addBriefToKons(brief, kons);
				return brief;
			}
		} else {
			if (plugin.loadFromByteArray(template.loadBinary(), true) == true) {
				final Brief ret = new Brief(subject == null ? template
						.getBetreff() : subject, null, Hub.actUser, adressat,
						kons, typ);

				plugin.findOrReplace(MATCH_TEMPLATE, new ReplaceCallback() {
					public Object replace(final String in) {
						return replaceFields(ret, in.replaceAll(
								MATCH_SQUARE_BRACKET, StringTool.leer));
					}
				});
				plugin.findOrReplace(MATCH_INDIRECT_TEMPLATE,
						new ReplaceCallback() {
							public Object replace(final String in) {
								return replaceIndirectFields(ret, in
										.replaceAll(MATCH_SQUARE_BRACKET,
												StringTool.leer));
							}
						});
				plugin.findOrReplace(MATCH_GENDERIZE, new ReplaceCallback() {
					public String replace(final String in) {
						return genderize(ret, in.replaceAll(
								MATCH_SQUARE_BRACKET, StringTool.leer));
					}
				});
				plugin.findOrReplace(MATCH_IDATACCESS, new ReplaceCallback() {
					public Object replace(final String in) {
						String[][] ref = ScriptUtil.loadDataFromPlugin(in
								.replaceAll(MATCH_SQUARE_BRACKET,
										StringTool.leer));
						return ref;
					}
				});
				saveBrief(ret, typ);
				addBriefToKons(ret, kons);
				return ret;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private Object replaceFields(final Brief brief, final String b) {
		String[] q = b.split("\\."); //$NON-NLS-1$
		if (q.length != 2) {
			log.log(Messages.TextContainer_BadVariableFormat + b, Log.WARNINGS); // Kann
			// eigentlich
			// nie
			// vorkommen
			// ?!?
			return null;
		}
		if (q[0].equals("Datum")) { //$NON-NLS-1$
			return new TimeTool().toString(TimeTool.DATE_GER);
		}
		if (q[0].indexOf(":") != -1) { //$NON-NLS-1$
			String[][] ref = ScriptUtil.loadDataFromPlugin(b);
			return ref;
		}
		PersistentObject o = resolveObject(brief, q[0]);
		if (o == null) {
			return WARNING_SIGN + b + WARNING_SIGN;
		}

		String ret = o.get(q[1]);
		if ((ret == null) || (ret.startsWith("**"))) { //$NON-NLS-1$
		
			if (!(o.map(PersistentObject.EXTINFO).startsWith("**"))) { //$NON-NLS-1$
				Hashtable ext = o.getHashtable(PersistentObject.EXTINFO);
				String an = (String) ext.get(q[1]);
				if (an != null) {
					return an;
				}
			}
			log.log("Nicht erkanntes Feld in " + b, Log.WARNINGS); //$NON-NLS-1$
			return "???" + b + "???";
		}

		if (ret.startsWith("<?xml")) { //$NON-NLS-1$
			Samdas samdas = new Samdas(ret);
			ret = samdas.getRecordText();
		}
		return ret;
	}

	/**
	 * Resolve an indirect field, e. g. Fall.Kostentrager.Bezeichnung1
	 * 
	 * @param brief
	 *            the curren Brief
	 * @param field
	 *            the filed to resolv
	 * @return the resolved value
	 */
	private Object replaceIndirectFields(final Brief brief, final String field) {
		String[] tokens = field.split("\\."); //$NON-NLS-1$
		if (tokens.length <= 2) {
			return WARNING_SIGN + field + WARNING_SIGN;
		}

		String firstToken = tokens[0];
		String valueToken = tokens[tokens.length - 1];

		// resolve the first field
		PersistentObject first = resolveObject(brief, firstToken);
		if (first == null) {
			return WARNING_SIGN + field + WARNING_SIGN;
		}

		// resolve intermediate objects
		PersistentObject current = first;
		for (int i = 1; i < tokens.length - 1; i++) {
			PersistentObject next = resolveIndirectObject(current, tokens[i]);
			if (next == null) {
				return WARNING_SIGN + field + WARNING_SIGN;
			}
			current = next;
		}

		// resolve value

		PersistentObject o = current;

		String value = o.get(valueToken);
		if ((value == null) || (value.startsWith("**"))) { //$NON-NLS-1$
			log.log("Nicht erkanntes Feld in " + field, Log.WARNINGS); //$NON-NLS-1$
			return WARNING_SIGN + field + WARNING_SIGN;
		}

		if (value.startsWith("<?xml")) { //$NON-NLS-1$
			Samdas samdas = new Samdas(value);
			value = samdas.getRecordText();
		}
		return value;
	}

	private PersistentObject resolveIndirectObject(PersistentObject parent,
			String field) {
		if (parent instanceof Fall) {
			Fall fall = (Fall) parent;

			return fall.getReferencedObject(field);
		} else {
			// not yet supported
			return null;
		}
	}

	/**
	 * Format für Genderize: [Feld:mw:formulierung Mann/formulierung Frau] oder
	 * [Feld:mwn:mann/frau/neutral]
	 */
	private String genderize(final Brief brief, final String in) {
		String[] q = in.split(":"); //$NON-NLS-1$
		PersistentObject o = resolveObject(brief, q[0]);
		if (o == null) {
			return "???";
		}
		if (q.length != 3) {
			log.log("falsches genderize Format " + in, Log.ERRORS); //$NON-NLS-1$
			return null;
		}
		if (!(o instanceof Kontakt)) {
			return Messages.TextContainer_FieldTypeForContactsOnly;
		}
		Kontakt k = (Kontakt) o;
		String[] g = q[2].split("/"); //$NON-NLS-1$
		if (g.length < 2) {
			return Messages.TextContainer_BadFieldDefinition;
		}
		if (k.istPerson()) {
			Person p = Person.load(k.getId());

			if (p.get(Person.SEX).equals(Person.MALE)) {
				if (q[1].startsWith("m")) { //$NON-NLS-1$
					return g[0];
				}
				return g[1];
			} else {
				if (q[1].startsWith("w")) { //$NON-NLS-1$
					return g[0];
				}
				return g[1];
			}
		} else {
			if (g.length < 3) {
				return Messages.TextContainer_FieldTypeForPersonsOnly;
			}
			return g[2];
		}
	}

	private PersistentObject resolveObject(final Brief actBrief, final String k) {
		PersistentObject ret = null;
		if (k.equalsIgnoreCase("Mandant")) { //$NON-NLS-1$
			ret = Hub.actMandant;
		} else if (k.equalsIgnoreCase("Anwender")) { //$NON-NLS-1$
			ret = Hub.actUser;
		} else if (k.equalsIgnoreCase("Adressat")) { //$NON-NLS-1$
			ret = actBrief.getAdressat();
		} else {
			try {
				String fqname = "ch.elexis.data." + k; //$NON-NLS-1$
				ret = GlobalEvents.getInstance().getSelectedObject(
						Class.forName(fqname));
			} catch (Throwable ex) {
				log.log(Messages.TextContainer_UnrecognizedFieldType + k, Log.WARNINGS);
				ret = null;
			}
		}
		if (ret == null) {
			log.log(Messages.TextContainer_UnrecognizedFieldType + k, Log.WARNINGS);
		}
		return ret;
	}

	private void addBriefToKons(final Brief brief, final Konsultation kons) {
		if (kons != null) {
			String label = "\n[ " + brief.getLabel() + " ]"; //$NON-NLS-1$ //$NON-NLS-2$
			kons.addXRef(XrefExtension.providerID, brief.getId(), -1, label);
		}
	}

	/**
	 * Dokument speichern. Wenn noch kein Adressat vorhanden ist, wird eine
	 * Auswahl angeboten.
	 * 
	 * @param brief
	 *            das zu speichernde Dokument
	 * @param typ
	 *            Typ des Dokuments
	 */
	public void saveBrief(Brief brief, final String typ) {
		if ((brief == null) || (brief.getAdressat() == null)) {
			KontaktSelektor ksl = new KontaktSelektor(shell, Kontakt.class,
					Messages.TextContainer_SelectAdresseeHeader,
					Messages.TextContainer_SelectAdresseeBody);
			if (ksl.open() == Dialog.OK) {
				brief = new Brief(Messages.TextContainer_Letter, null, Hub.actUser, (Kontakt) ksl
						.getSelection(), Konsultation.getAktuelleKons(), typ);
			}
		}
		if (brief != null) {
			if (StringTool.isNothing(brief.getBetreff())) {
				InputDialog dlg = new InputDialog(
						shell,
						Messages.TextContainer_SaveDocumentHeader,
						Messages.TextContainer_SaveDocumentBody,
						brief.getBetreff(), null);
				if (dlg.open() == Dialog.OK) {
					brief.setBetreff(dlg.getValue());
				} else {
					brief.setBetreff(brief.getTyp());
				}
			}
			byte[] contents = plugin.storeToByteArray();
			if (contents == null) {
				log.log(Messages.TextContainer_NullSaveHeader, Log.ERRORS);
			}
			brief.save(contents, plugin.getMimeType());
			GlobalEvents.getInstance().fireUpdateEvent(Brief.class);
		}
	}

	/**
	 * Den Aktuellen Inhalt des Textpuffers als Vorlage speichern. Name und
	 * zuzuordender Mandant werden per Dialog erfragt.
	 * 
	 */
	public void saveTemplate(String name) {
		SaveTemplateDialog std = new SaveTemplateDialog(shell, name);
		// InputDialog dlg=new
		// InputDialog(getViewSite().getShell(),"Vorlage speichern","Geben Sie bitte einen Namen für die Vorlage ein","",null);
		if (std.open() == Dialog.OK) {
			String title = std.title;
			Brief brief = new Brief(title, null, Hub.actUser, std.selectedMand,
					null, Brief.TEMPLATE);
			if (std.bSysTemplate) {
				brief.set(Brief.KONSULTATION_ID, "SYS"); //$NON-NLS-1$
			}
			byte[] tmpl = plugin.storeToByteArray();
			if (tmpl == null) {
				log.log(Messages.TextContainer_NullSaveBody, Log.ERRORS);
			}
			brief.save(tmpl, plugin.getMimeType());
			// text.clear();
		}
	}

	/** Einen Brief einlesen */
	public boolean open(final Brief brief) {
		if (brief == null) {
			log.log(Messages.TextContainer_NullOpen, Log.WARNINGS);
			return false;
		}
		System.out.print(brief.getLabel());
		byte[] arr = brief.loadBinary();
		if (arr == null) {
			log.log(Messages.TextContainer_ErroneousLetter + brief.getLabel(),
					Log.WARNINGS);
			return false;
		}
		return plugin.loadFromByteArray(arr, false);
	}

	class SaveTemplateDialog extends TitleAreaDialog {
		Text name;
		Combo cMands;
		String title;
		Button btSysTemplate;
		boolean bSysTemplate;
		List<Mandant> lMands;
		Mandant selectedMand;
		String tmplName;

		protected SaveTemplateDialog(final Shell parentShell,
				String templateName) {
			super(parentShell);
			tmplName = templateName;
		}

		@Override
		public void create() {
			super.create();
			setTitle(Messages.TextContainer_SaveTemplateHeader);
			setMessage(Messages.TextContainer_SaveTemplateBody);
			getShell().setText(Messages.TextContainer_Template);
		}

		@Override
		protected Control createDialogArea(final Composite parent) {
			Composite ret = new Composite(parent, SWT.NONE);
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			ret.setLayout(new GridLayout());
			new Label(ret, SWT.NONE).setText(Messages.TextContainer_TemplateName);
			name = new Text(ret, SWT.BORDER);
			name.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			if (tmplName != null) {
				name.setText(tmplName);
			}
			new Label(ret, SWT.NONE).setText(Messages.TextContainer_Mandator);
			Composite line = new Composite(ret, SWT.NONE);
			line.setLayout(new FillLayout());
			line.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			cMands = new Combo(line, SWT.SINGLE);
			Query<Mandant> qbe = new Query<Mandant>(Mandant.class);
			lMands = qbe.execute();
			cMands.add(Messages.TextContainer_All);
			for (Mandant m : lMands) {
				cMands.add(m.getLabel());
			}
			btSysTemplate = new Button(line, SWT.CHECK);
			btSysTemplate.setText(Messages.TextContainer_SystemTemplate);
			return ret;
		}

		@Override
		protected void okPressed() {
			title = name.getText();
			bSysTemplate = btSysTemplate.getSelection();
			int i = cMands.getSelectionIndex();
			if (i != -1) {
				if (i == 0) {
					selectedMand = null;
				} else {
					selectedMand = lMands.get(i - 1);
				}
			}
			Query<Brief> qbe = new Query<Brief>(Brief.class);
			qbe.add(Brief.TYPE, Query.EQUALS, Brief.TEMPLATE);
			if (selectedMand != null) {
				qbe.startGroup();
				qbe.add(Brief.DESTINATION_ID, Query.EQUALS, selectedMand
						.getId());
				qbe.or();
				qbe.add(Brief.DESTINATION_ID, Query.EQUALS, StringTool.leer);
				qbe.endGroup();
				qbe.and();
			}
			qbe.add("geloescht", Query.NOT_EQUAL, StringTool.one); //$NON-NLS-1$
			qbe.add(Brief.SUBJECT, Query.EQUALS, title);
			List<Brief> l = qbe.execute();
			if (l.size() > 0) {
				if (MessageDialog
						.openQuestion(getShell(), Messages.TextContainer_TemplateExistsCaption,
								Messages.TextContainer_TemplateExistsBody)) {
					Brief old = l.get(0);
					old.delete();
				} else {
					return;
				}
			}
			super.okPressed();
		}

	}

	public boolean replace(final String pattern, final ReplaceCallback cb) {
		return plugin.findOrReplace(pattern, cb);
	}

	public boolean replace(final String pattern, final String repl) {
		return plugin.findOrReplace(pattern, new ReplaceCallback() {
			public String replace(final String in) {
				return repl;
			}
		});
	}

	static class DefaultTextPlugin implements ITextPlugin {
		private static final String expl = Messages.TextContainer_NoPlugin1
				+ Messages.TextContainer_NoPlugin2
				+ Messages.TextContainer_Noplugin3
				+ Messages.TextContainer_NoPlugin4
				+ Messages.TextContainer_NoPLugin5;

		public Composite createContainer(final Composite parent,
				final ITextPlugin.ICallback h) {
			parent.setLayout(new FillLayout());
			// Composite ret=new Composite(parent,SWT.BORDER);
			Form form = Desk.getToolkit().createForm(parent);
			form.setText(Messages.TextContainer_NoPluginCaption);
			form.getBody().setLayout(new FillLayout());
			FormText ft = Desk.getToolkit().createFormText(form.getBody(),
					false);
			ft.setText(expl, true, false);
			return form.getBody();
		}

		public void dispose() {
		}

		public void showMenu(final boolean b) {
		}

		public void showToolbar(final boolean b) {
		}

		public boolean createEmptyDocument() {
			return false;
		}

		public boolean loadFromByteArray(final byte[] bs,
				final boolean asTemplate) {
			return false;
		}

		public boolean findOrReplace(final String pattern,
				final ReplaceCallback cb) {
			return false;
		}

		public byte[] storeToByteArray() {
			return null;
		}

		public boolean clear() {
			return false;
		}

		public void setInitializationData(final IConfigurationElement config,
				final String propertyName, final Object data)
				throws CoreException {
		}

		public boolean loadFromStream(final InputStream is,
				final boolean asTemplate) {
			// TODO Automatisch erstellter Methoden-Stub
			return false;
		}

		public boolean print(final String printer, final String tray,
				final boolean waitUntilFinished) {
			return false;
		}

		public boolean insertTable(final String marke, final int props,
				final String[][] contents, final int[] columnSizes) {
			return false;
		}

		public void setFocus() {

		}

		public PageFormat getFormat() {
			return PageFormat.USER;
		}

		public void setFormat(final PageFormat f) {

		}

		public Object insertTextAt(final int x, final int y, final int w,
				final int h, final String text, final int adjust) {
			return null;
		}

		public boolean setFont(final String name, final int style,
				final float size) {
			return false;
		}

		public Object insertText(final String marke, final String text,
				final int adjust) {
			// TODO Auto-generated method stub
			return null;
		}

		public Object insertText(final Object pos, final String text,
				final int adjust) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getMimeType() {
			return "text/nothing"; //$NON-NLS-1$
		}

		public void setSaveOnFocusLost(final boolean bSave) {
			// TODO Auto-generated method stub

		}
	}

}
