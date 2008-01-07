/*******************************************************************************
 * Copyright (c) 2007, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    
 * $Id$
 *******************************************************************************/

package ch.elexis.laborimport.bioanalytica;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.elexis.util.PlatformHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class Groups {
	public static void main(String[] args) {
		System.out.println("Groups");
	}
	
	/**
	 * Groups and Code data of Lab Bioanalytica
	 */
	private static final String GROUPS_FILE = "/rsc/groups.dat";
	private static final String CODES_FILE = "/rsc/codes.dat";
	private static final String UNKNOWN_PREFIX = "00 Automatisch";
	
	private static HashMap<String, Group> groups;
	private static HashMap<String, Code> codes;
	
	static {
		loadData();
	}
	
	private static final InputStreamReader getFileAsSreamReader(String path) {
		String basePath = PlatformHelper.getBasePath(Importer.PLUGIN_ID);
		File file = new File(basePath + path);
		if (file.exists() && file.isFile()) {
			try {
			return new InputStreamReader(new FileInputStream(file));
			} catch (FileNotFoundException ex) {
				ExHandler.handle(ex);
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Load data from GROUPS_FILE and CODES_FILE
	 */
	private static void loadData() {
		groups = new HashMap<String, Group>();
		codes = new HashMap<String, Code>();
		
		BufferedReader in;
		String line;
		
		try {
			in = new BufferedReader(getFileAsSreamReader(GROUPS_FILE));
			while ((line = in.readLine()) != null) {
				parseGroup(line);
			}
			in.close();
			
			in = new BufferedReader(getFileAsSreamReader(CODES_FILE));
			while ((line = in.readLine()) != null) {
				parseCode(line);
			}
			in.close();
		} catch (IOException ex) {
			// read error, stop reading
			ex.printStackTrace(System.err);
		} catch (Throwable ex) {
			ex.printStackTrace(System.err);
		}
	}
	
	private static void parseGroup(String line) {
		if (!isCommentLine(line)) {
			// pattern: <Key>;<Name>
			Pattern p = Pattern.compile("^([^;]+);([^;]+)$");
			Matcher m = p.matcher(line);
			if (m.matches()) {
				String key = m.group(1).trim();
				String name = m.group(2).trim();
				Group group = new Group(key, name);
				groups.put(key, group);
			}
		}
	}
	
	private static void parseCode(String line) {
		if (!isCommentLine(line)) {
			// pattern: <Code>;<Group Key>[;<Name>]
			Pattern p = Pattern.compile("^([^;]+);([^;]+)(;([^;]*))?");
			Matcher m = p.matcher(line);
			if (m.matches()) {
				String key = m.group(1).trim();
				String groupKey = m.group(2).trim();
				String name = "";
				if (m.group(4) != null) {
					name = m.group(4);
				}
				Group group = groups.get(groupKey);
				if (group != null) {
					Code code = new Code(group, key, name); 
					codes.put(key, code);
				}
			}
		}
	}
	
	private static boolean isCommentLine(String line) {
		if (line.matches("^\\s*#.*$") || line.matches("^\\s*$")) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Get the group's name of a given code
	 * @param code
	 * @return
	 */
	public static String getGroupNameOfCode(String key) {
		String name;
		
		Code code = codes.get(key);
		if (code != null) {
			Group group = groups.get(code.group.key);
			name = group.key + " " + group.name;
		} else {
			String dat = new TimeTool().toString(TimeTool.DATE_GER);
			name = UNKNOWN_PREFIX;
		}

		return name;
	}
	
	/**
	 * Get the code's name
	 * @param key the code
	 * @return the name of the code
	 */
	public static String getCodeName(String key) {
		Code code = codes.get(key);
		if (code != null) {
			if (!StringTool.isNothing(code.name)) {
				return code.name;
			} else {
				return key;
			}
		} else {
			return key;
		}
		
	}
	
	static class Group {
		String key;
		String name;
		
		Group(String key, String name) {
			this.key = key;
			this.name = name;
		}
	}
	
	static class Code {
		Group group;
		String key;
		String name;
		
		Code(Group group, String key, String name) {
			this.group = group;
			this.key = key;
			this.name = name;
		}
	}
}
