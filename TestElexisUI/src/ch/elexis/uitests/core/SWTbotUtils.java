/*******************************************************************************
 * Copyright (c) 2010 Niklaus Giger and medelexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Niklaus Giger - initial implementation
 *******************************************************************************/
package ch.elexis.uitests.core;

import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.apache.log4j.*;

/* 
 * Here we assemble a few utility procedures for running SWTbot
 * 
 * TODO:	Setup empty/demo/stresstest DB
 * 
 */
public class SWTbotUtils {
	public static final int FULL_SCREEN = 0;
	public static final int VIEW = 1;
	public static final int EDITOR = 2;
	public static final int SHELL = 3;
	private static Logger logger = Logger.getRootLogger();
	private static SWTWorkbenchBot bot;
	private static String logName = "elexis-swtbot.log";
	private static ConsoleAppender consoleAppender;
	
	/*
	 * This setup should be called by each test, as setting up a SWTbot is quite time consuming
	 * (e.g. on my machines a empty test takes almost 30 seconds
	 */
	public static SWTWorkbenchBot initSWTbot(){
		if (bot == null)
			bot = new SWTWorkbenchBot();
		return bot;
	}
	
	/*
	 * Setup logging to the specified file in the current directory Uses log4j. Adds a logging
	 * layout with ms precision to be able to easily detect time consuming operations
	 * 
	 * @filename Filename to use for the logging
	 */
	public static Logger initSWTbotLogging(String filename){
		if (logger != Logger.getRootLogger() && filename == logName)
			return logger;
		try {
			PatternLayout layout = new PatternLayout("%d{ISO8601} %-5p %c: %m%n");
			if (consoleAppender == null)
				consoleAppender = new ConsoleAppender(layout);
			logger.addAppender(consoleAppender);
			FileAppender fileAppender = new FileAppender(layout, logName, false);
			logger.addAppender(fileAppender);
			// ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF:
			logger.setLevel(org.apache.log4j.Level.INFO);
		} catch (Exception ex) {
			System.out.println(ex);
			return Logger.getRootLogger();
		}
		logger.setLevel(Level.INFO);
		logName = filename;
		return logger;
	}
	
	/*
	 * Setup logging to the file elexis-swtbot.log in the current directory
	 */
	public static Logger initSWTbotLogging(){
		return initSWTbotLogging(logName);
	}
	
	/*
	 * Easy method to take screenshots. As this is quite time consuming, we will only create them if
	 * explicitly demanded by a none empty property ch.elexis.saveScreenshot
	 */
	public static void takeScreenshot(String filename, int what){
		boolean res = false;
		if (System.getProperty("ch.elexis.saveScreenshot") == null) {
			logger.info("Skip snapshot snapshot: " + filename + " type " + what);
			return;
		}
		
		logger.info("before taking type " + what + " snapshot: " + filename);
		if (what == FULL_SCREEN) {
			res = SWTUtils.captureScreenshot(filename);
			
		} else if (what == VIEW) {
			try {
				bot.activeView().setFocus();
				
			} catch (Exception WidgetNotFound) {
				logger.error("no view active at the moment");
				return;
			}
			bot.activeView().setFocus();
			res = SWTUtils.captureScreenshot(filename, bot.getFocusedWidget());
			
		} else if (what == EDITOR) {
			try {
				bot.activeEditor().setFocus();
				
			} catch (Exception WidgetNotFound) {
				logger.error("no editor active at the moment");
				return;
			}
			bot.activeEditor().setFocus();
			res = SWTUtils.captureScreenshot(filename, bot.getFocusedWidget());
			
		} else if (what == SHELL) {
			try {
				bot.activeShell().setFocus();
				
			} catch (Exception WidgetNotFound) {
				logger.error("no shell active at the moment");
				return;
			}
			bot.activeShell().setFocus();
			res = SWTUtils.captureScreenshot(filename, bot.getFocusedWidget());
			
		} else {
			logger.error("invalid snapshot type " + what);
			return;
		}
		logger.info(" snapshot: " + filename + " " + res);
	}
	
	private static void displayNodes(SWTBotTreeItem item){
		int j = 0;
		List<String> nodes = item.getNodes();
		for (String node : nodes) {
			j += 1;
			logger.info("  Node " + j + ": " + node);
		}
		
	}
	
	/* see also http://eclipse.dzone.com/articles/eclipse-gui-testing-is-viable- */
	/*
	 * This is a handy procedure if you want just see, how the current view is organized. At least
	 * for newcomers like the writer of this procedure, this is not always is to figure out
	 */
	public static void displayTreeView(SWTBot bot){
		SWTBotTree tree = bot.tree();
		SWTBotTreeItem[] items = tree.getAllItems();
		logger.info("displayTreeView " + bot.toString() + " nr Items " + items.length);
		int j = 0;
		for (SWTBotTreeItem item : items) {
			j += 1;
			logger.info("item "
				+ String.format("%3d: %20s: %s", j, item.getText(), item.getClass()));
		}
	}
}
