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
package ch.elexis.uitests.laborImport;

import java.io.File;
import ch.rgw.io.FileTool;

import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith; //import org.eclipse.core.runtime.Assert;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.Messages;
import org.apache.log4j.*;
import ch.elexis.uitests.core.SWTbotUtils;

/*
 * Tests for the analytica labor importation
 * TODO: - Add test for setup
 * TODO: - Add test for importation (success)
 * TODO: - Add test for importation. Patient matches
 * TODO: - Add test for importation. Patient does not match
 * TODO: - Add test for importation. Test imported lab values
 * TODO: - Check handling for german umlaute and french accents
 * 
 * We assume that parsing HL7 files work perfectly.
 * 
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class TestAnalytica {
	
	private static SWTWorkbenchBot bot;
	private static File testDir;
	private static Logger logger;
	
	/*
	 * Tests for Analytica LaborImport TODO: Multilingual TODO: More testcases
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void initialConnect() throws Exception{
		bot = SWTbotUtils.initSWTbot();
		logger = SWTbotUtils.initSWTbotLogging();
	}
	
	@Test
	public void testAnalyticaSetup() throws Exception{
		logger.info("testAnalyticaSetup");
		assert (true);
		bot.menu("Datei").menu("Einstellungen").click();
		SWTbotUtils.displayTreeView(bot);
		SWTBotShell shell = bot.shell("Preferences");
		SWTbotUtils.takeScreenshot("Preferences.png", SWTbotUtils.SHELL);		
		SWTbotUtils.displayTreeView(shell.bot());
		SWTBot pBot = shell.bot();
		//pBot.clabelInGroup("Labor Analytica", "Datenaustausch");
		//logger.info("Datenaustausc Laborimport Analytica 1");
		SWTbotUtils.takeScreenshot("Preferences_2a.png", SWTbotUtils.SHELL);		

		// pBot.treeWithLabelInGroup("Labor Analytica", "Datenaustausch");
		// logger.info("Datenaustausc Laborimport Analytica 3");
		// SWTbotUtils.takeScreenshot("Preferences_2c.png", SWTbotUtils.SHELL);		

		pBot.cTabItemInGroup("Labor Analytica", "Datenaustausch");
		logger.info("Datenaustausc Laborimport Analytica 2e");
		SWTbotUtils.takeScreenshot("Preferences_2e.png", SWTbotUtils.SHELL);		

		//pBot.treeWithLabel("Datenaustausch").expandNode("Labor Analytica");
		//logger.info("Datenaustausc Laborimport Analytica 5");
		//SWTbotUtils.takeScreenshot("Preferences_2d.png", SWTbotUtils.SHELL);		
		pBot.cTabItemInGroup("Labor Analytica", "Datenaustausch");
		logger.info("Datenaustausch Laborimport Analytica 2");
		SWTbotUtils.takeScreenshot("Preferences_2b.png", SWTbotUtils.SHELL);		
//		pBot.labelInGroup("Labor Analytica", "Datenaustausch");
		SWTBotCCombo cbUser = pBot.ccomboBoxWithLabel("FTP Benutzername");
		logger.info("Benutzername" + cbUser); pBot.sleep(200); 
		cbUser.setSelection("niklausAsUser");
		SWTBotCCombo cbPw = pBot.ccomboBoxWithLabel("FTP Passwort");
		logger.info("password" + cbPw); pBot.sleep(200); 
		cbUser.setSelection("1234asPw");
		SWTbotUtils.takeScreenshot("Preferences_3.png", SWTbotUtils.SHELL);		
		logger.info("testAnalyticaSetup.done");
		/*
		 * TODO: Select the correct page and verify that we can to a few things with it SWTBotTree
		 * tree = bot.treeWithLabel("Datenaustausch"); logger.info("Got Datenaustausch tree");
		 * bot.sleep(200); tree.expandNode("Datenaustausch", true);
		 * logger.info("Got Datenaustausch expanded"); bot.sleep(200); SWTBotTree tree2 =
		 * bot.treeWithLabel("Labor Analytica"); logger.info("Got Datenaustausch tree");
		 * bot.sleep(200);
		 * 
		 * SWTBotShell shell = bot.shellWithId("Datenaustausch", "Labor Analytica");
		 * logger.info("Got Datenaustausch/Labo Shell");
		 * logger.info("Got Datenaustausch"+shell.toString());
		 * SWTbotUtils.displayTreeView(shell.bot()); bot.sleep(200); // bot.getFocusedWidget();
		 * logger.info("Got Analytica"); bot.sleep(200);
		 */
	}
}
