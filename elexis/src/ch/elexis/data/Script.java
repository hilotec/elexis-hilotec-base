/*******************************************************************************
 * Copyright (c) 2008-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: Script.java 5970 2010-01-27 16:43:04Z rgw_ch $
 *******************************************************************************/

package ch.elexis.data;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import ch.elexis.ElexisException;
import ch.elexis.Hub;
import ch.elexis.actions.ElexisEventDispatcher;
import ch.elexis.scripting.Interpreter;
import ch.elexis.text.TextContainer;
import ch.elexis.util.Extensions;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * A script. At this moment only beanshell is supported as interpreter, but
 * others are possible
 * 
 * @author gerry
 * 
 */
public class Script extends NamedBlob2 {
	public static final String INTERPRETER_BEANSHELL = "BSH";
	public static final String INTERPRETER_SCALA = "SCALA";
	public static final String INTERPRETER_DEFAULT = INTERPRETER_BEANSHELL;
	private static final Pattern varPattern = Pattern
			.compile(TextContainer.MATCH_TEMPLATE);
	private static final String PREFIX = "Script:";
	private static Interpreter scripter = null;

	private static Interpreter getInterpreter(String name)
			throws ElexisException {
		if (name == null) {
			return getInterpreter(INTERPRETER_BEANSHELL);
		}
		List<IConfigurationElement> scripters = Extensions
				.getExtensions("ch.elexis.scripting");
		for (IConfigurationElement scripter : scripters) {
			if (scripter.getAttribute("name").equals(name)) {
				try {
					return (Interpreter) scripter
							.createExecutableExtension("class");
				} catch (CoreException e) {
					ExHandler.handle(e);
					throw new ElexisException(Script.class,
							"Could not load intepreter " + e.getMessage(),
							ElexisException.EE_NOT_SUPPORTED);
				}
			}
		}
		throw new ElexisException(Script.class, "interpreter not installed "
				+ name, ElexisException.EE_NOT_SUPPORTED);

	}

	private String load() throws ElexisException {
		String script = getString();
		Pattern ip = Pattern.compile("^#!\\s*([A-Z]+)$", Pattern.MULTILINE);
		Matcher m = ip.matcher(script);
		if (m.matches()) {
			scripter = getInterpreter(m.group(1));
		} else {
			scripter = getInterpreter(null);
		}
		return script.substring(m.group().length() + 2);
	}

	public static Script create(String name, String contents)
			throws ElexisException {
		String mid = PREFIX + name;
		Script ret = new Script(mid);
		if (ret.state() == INEXISTENT) {
			ret.create(mid);
		} else if (ret.state() == DELETED) {
			ret.undelete();
		}
		if (StringTool.isNothing(contents)) {
			contents = "#!BSH\n";
		}
		ret.putString(contents);
		ret.load();
		return ret;
	}

	@Override
	public String getLabel() {
		String[] name = getId().split(":");
		return name[1];
	}

	public void init() throws Exception {
		if (scripter == null) {
			load();
		}
		scripter.setValue("finished", false);
		scripter.setValue("init", true);
		scripter.run(parse(getString(), new PersistentObject[0]));
		scripter.setValue("init", false);
	}

	public void finished() throws Exception {
		scripter.setValue("finished", true);
		scripter.run(parse(getString(), (PersistentObject[]) null));
	}

	public void setVariable(String name, Object value) throws ElexisException {
		scripter.setValue(name, value);
	}

	/**
	 * Replace variables of the form [Patient.Name] in the script with their
	 * respective values for the current call
	 * 
	 * @param t
	 *            the script
	 * @param params
	 *            all Variables to replace
	 * @return the parsed Script
	 */
	private String parse(String t, PersistentObject... params) {
		if (params == null) {
			params = new PersistentObject[0];
		}
		Matcher matcher = varPattern.matcher(t);
		// Suche Variablen der Form [Patient.Alter]
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			boolean bMatched = false;
			String var = matcher.group()
					.replaceAll("[\\[\\]]", StringTool.leer);
			String[] fields = var.split("\\.");
			if (fields.length > 1) {
				String fqname = "ch.elexis.data." + fields[0];
				for (PersistentObject o : params) {
					if (o.getClass().getName().equals(fqname)) {
						String repl = o.get(fields[1]);
						repl = repl.replace('\\', '/');
						repl = repl.replace('\"', ' ');
						repl = repl.replace('\n', ' ');
						repl = repl.replace('\r', ' ');
						matcher.appendReplacement(sb, "\"" + repl + "\"");
						bMatched = true;
					}
				}
			}
			if (!bMatched) {
				matcher.appendReplacement(sb, "\"\"");
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * execute a script with the given interpreter
	 * 
	 * @param interpreter
	 *            only BSH supported at this time
	 * @param objects
	 *            optional Objects to repalce in Variables like [Fall.Grund] in
	 *            the script
	 * @param params
	 *            optional parameters. These can be of the form
	 *            <i>name=value</i> or <i>value</i>. if no name is given, the
	 *            variables will be inserted for $1, $2 ... in the script. If a
	 *            name is given, $names in the script will be replaced with the
	 *            respective values.
	 * @return The result of the script interpreter
	 * @throws ElexisException
	 */
	public Object execute(String params,
			PersistentObject... objects) throws ElexisException {
		String t = load();
		if (!StringTool.isNothing(t)) {
			if (params != null) {
				String var = "\\$";
				String[] parameters = params.split(",");
				for (int i = 0; i < parameters.length; i++) {
					String parm = parameters[i].trim();
					String[] p = parm.split("=");
					if (p.length == 2) {
						t = t.replaceAll("\\" + p[0], p[1]);
					} else {
						t = t.replaceAll(var + i, p[0]);
					}
				}
			}
			String parsed = parse(t, objects);
			scripter.setValue("actPatient",
					ElexisEventDispatcher.getSelectedPatient());
			scripter.setValue("actFall",
					ElexisEventDispatcher.getSelected(Fall.class));
			scripter.setValue("actKons",
					ElexisEventDispatcher.getSelected(Konsultation.class));
			scripter.setValue("Elexis", Hub.plugin);
			return scripter.run(parsed);
		}
		return null;
	}

	public static List<Script> getScripts() {
		Query<Script> qbe = new Query<Script>(Script.class);
		qbe.add("ID", "LIKE", PREFIX + "%");
		return qbe.execute();
	}

	public static Object executeScript(String call,
			PersistentObject... objects) throws ElexisException {
		String name = call;
		String params = null;
		int x = call.indexOf('(');
		if (x != -1) {
			name = call.substring(0, x);
			params = call.substring(x + 1, call.length() - 1);
		}
		Query<Script> qbe = new Query<Script>(Script.class);
		qbe.add("ID", Query.EQUALS, PREFIX + name);
		List<Script> found = qbe.execute();
		if (found.size() == 0) {
			throw new ElexisException(Script.class,
					"A Script with this name was not found " + name,
					ElexisException.EE_NOT_FOUND);
		}
		Script script = found.get(0);
		try {
			return script.execute(params, objects);
		} catch (Exception e) {
			ExHandler.handle(e);
			throw new ElexisException(Script.class, "Error while executing "
					+ name + ": " + e.getMessage(),
					ElexisException.EE_UNEXPECTED_RESPONSE);
		}
	}

	@Override
	public boolean isDragOK() {
		return true;
	}

	@Override
	public boolean isValid() {
		if (getId().matches(PREFIX + "[a-zA-Z0-9_-]+")) {
			return super.isValid();
		}
		return false;
	}

	public static Script load(String id) {
		Script ret = new Script(id);
		if (ret.isValid()) {
			return ret;
		}
		return null;
	}

	protected Script(String id) {
		super(id);
	}

	protected Script() {
	}

}
