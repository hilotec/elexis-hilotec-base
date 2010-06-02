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
import ch.elexis.laborimport.analytica.Messages;
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
		logger.info("testAnalyticaSetup"); //$NON-NLS-1$
		logger.info("XX File: " +ch.elexis.uitests.Messages.getString("TestCore.File"));//$NON-NLS-1$
		logger.info("XX File: " +ch.elexis.uitests.Messages.getString("TestCore.Preferences"));//$NON-NLS-1$
		bot.menu(ch.elexis.uitests.Messages.getString("TestCore.File")).menu(ch.elexis.uitests.Messages.getString("TestCore.Preferences")).click(); //$NON-NLS-1$ //$NON-NLS-2$
		SWTBotShell shell = bot.shell("Preferences"); //$NON-NLS-1$
		SWTbotUtils.displayTreeView(shell.bot());
		SWTBot pBot = shell.bot();
		SWTbotUtils.takeScreenshot("Preferences.png", SWTbotUtils.FULL_SCREEN);		
		logger.info("XX 1: " + ch.elexis.uitests.Messages.getString("TestCore.Datenaustausch"));
        SWTbotUtils.takeScreenshot("Preferences_2a.png", SWTbotUtils.EDITOR);
        SWTBotTreeItem treeItem = pBot.tree().getTreeItem(ch.elexis.uitests.Messages.getString("TestCore.Datenaustausch")).select().expand();
        SWTbotUtils.takeScreenshot("Preferences_2b.png", SWTbotUtils.VIEW);
        logger.info("XX 2: " + Messages.getString("PreferencePage.title.description"));
        treeItem.getNode("Labor Analytica").select().expand();
        SWTbotUtils.takeScreenshot("Preferences_2c.png", SWTbotUtils.SHELL);

		SWTBotText cbHost = pBot.textWithLabel(Messages.getString("PreferencePage.label.host")); //$NON-NLS-1$
		cbHost.setText(System.getProperty("ch.elexis.ovpn.host")); //$NON-NLS-1$


		SWTBotText cbDown = pBot.textWithLabel(Messages.getString("PreferencePage.label.download")); //$NON-NLS-1$
		cbDown.setText(System.getProperty("ch.elexis.ovpn.download")); //$NON-NLS-1$

		SWTBotText cbUser = pBot.textWithLabel(Messages.getString("PreferencePage.label.user")); //$NON-NLS-1$
		cbUser.setText(System.getProperty("ch.elexis.ovpn.user")); //$NON-NLS-1$

		SWTBotText cbPw = pBot.textWithLabel(Messages.getString("PreferencePage.label.password")); //$NON-NLS-1$
		cbPw.setText(System.getProperty("ch.elexis.ovpn.password")); //$NON-NLS-1$

		SWTBotText  cbOvpn = pBot.textWithLabel(Messages.getString("PreferencePage.label.ovpn")); //$NON-NLS-1$
		cbOvpn.setText(System.getProperty("ch.elexis.ovpn.conf")); //$NON-NLS-1$
		
		SWTbotUtils.takeScreenshot("Preferences_3s.png", SWTbotUtils.SHELL);		 //$NON-NLS-1$
		pBot.button("Apply").click();
		logger.info("testAnalyticaSetup Applied"); //$NON-NLS-1$
		pBot.button("OK").click();
		SWTbotUtils.takeScreenshot("Preferences_4.png", SWTbotUtils.SHELL);		 //$NON-NLS-1$
		logger.info("testAnalyticaSetup Okay"); //$NON-NLS-1$
		assert (true);
	}
}
