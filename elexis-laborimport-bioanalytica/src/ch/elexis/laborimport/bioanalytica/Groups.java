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
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.elexis.util.PlatformHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * This class provides the groups and codes used by the lab Bioanalytica.
 * 
 * Important: Each public method must run init() to make sure data is loaded.
 * @author Daniel Lutz
 */
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
	private static final String DEFAULT_ORDER = "00000";
	
	private static final String CHARSET_UTF_8 = "UTF-8";
	private static final String CHARSET = CHARSET_UTF_8;
	
	private static HashMap<String, Group> groups = null;
	private static HashMap<String, Code> codes = null;
	
	
	
	static {
		init();
	}
	
	private static final InputStreamReader getFileAsSreamReader(String path) {
		String basePath = PlatformHelper.getBasePath(Importer.PLUGIN_ID);
		File file = new File(basePath + path);
		if (file.exists() && file.isFile()) {
			try {
				return new InputStreamReader(new FileInputStream(file), CHARSET);
			} catch (FileNotFoundException ex) {
				ExHandler.handle(ex);
				return null;
			} catch (UnsupportedEncodingException ex) {
				ExHandler.handle(ex);
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Initialize data
	 */
	private static void init() {
		if (groups == null || codes == null) {
			loadData();
		}
	}
	
	/**
	 * Load data from GROUPS_FILE and CODES_FILE
	 */
	private static void loadData() {
		groups = new HashMap<String, Group>();
		codes = new HashMap<String, Code>();

		InputStreamReader isr;
		BufferedReader in;
		String line;
		
		try {
			isr = getFileAsSreamReader(GROUPS_FILE);
			if (isr != null) {
				in = new BufferedReader(isr);
				while ((line = in.readLine()) != null) {
					parseGroup(line);
				}
				in.close();
			}
			
			isr = getFileAsSreamReader(CODES_FILE);
			if (isr != null) {
				in = new BufferedReader(isr);
				while ((line = in.readLine()) != null) {
					parseCode(line);
				}
				in.close();
			}
		} catch (Throwable ex) {
			groups = null;
			codes = null;
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
			// pattern: <Order>;<Code>;<Group Key>[;<Name>]
			Pattern p = Pattern.compile("^([0-9]+);([^;]+);([^;]+)(;([^;]*))?");
			Matcher m = p.matcher(line);
			if (m.matches()) {
				String order = m.group(1).trim();
				String key = m.group(2).trim();
				String groupKey = m.group(3).trim();
				String name = "";
				if (m.group(5) != null) {
					name = m.group(5);
				}
				Group group = groups.get(groupKey);
				if (group != null) {
					Code code = new Code(group, key, name, order); 
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
		
		init();
		
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
		init();
		
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

	/**
	 * Get the code's order number
	 * @param key the code
	 * @return the order of the code
	 */
	public static String getCodeOrder(String key) {
		init();
		
		Code code = codes.get(key);
		if (code != null) {
			if (!StringTool.isNothing(code.order)) {
				return code.order;
			} else {
				return DEFAULT_ORDER;
			}
		} else {
			return DEFAULT_ORDER;
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
	
	/**
	 * Representation of a Bioanlaytica code
	 * @author danlutz
	 */
	static class Code {
		Group group;
		String key;
		String name;
		/**
		 * The ordering string for this code
		 */
		String order;
		
		Code(Group group, String key, String name, String order) {
			this.group = group;
			this.key = key;
			this.name = name;
			this.order = order;
		}
	}
}
