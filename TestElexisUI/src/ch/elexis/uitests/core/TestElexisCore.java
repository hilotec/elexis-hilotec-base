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

import java.io.File;
import ch.rgw.io.FileTool;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith; //import org.eclipse.core.runtime.Assert;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import ch.elexis.actions.GlobalActions;
import ch.elexis.actions.Messages;
import org.apache.log4j.*;
import ch.elexis.uitests.core.SWTbotUtils;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TestElexisCore {
	private static SWTWorkbenchBot bot;
	private static File testDir;
	private static Logger logger;
	
	/**
	 * TODO: Am Anfang der Tests wird eine H2-Datenbank entweder aus einem dump zurückgeladen oder
	 * neu erstellt.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void initialConnect() throws Exception{
		bot = SWTbotUtils.initSWTbot();
		logger = SWTbotUtils.initSWTbotLogging();
	}
	
	@Test
	public void testSnapshots() throws Exception{
		return;
		/*
		 * SWTbotUtils.takeScreenshot("FULL_SCREEN.png", SWTbotUtils.FULL_SCREEN);
		 * SWTbotUtils.takeScreenshot("EDITOR.png", SWTbotUtils.EDITOR);
		 * SWTbotUtils.takeScreenshot("VIEW.png", SWTbotUtils.VIEW);
		 * SWTbotUtils.takeScreenshot("SHELL.png", SWTbotUtils.SHELL);
		 */
	}
	
	@Test
	public void createTestPatient() throws Exception{
		assert (true);
		logger.info("createTestPatient");
		/*
		 * bot.textWithLabel("Anwendername").setText("test"); log.log("initialConnect got new bot",
		 * Log.ERRORS); bot.textWithLabel("Passwort").setText("test");
		 * bot.buttonWithLabel("Anmelden").click(); //
		 * bot.menu("Datei").menu("Verbindung...").click(); // SWTBotShell
		 * dialog=bot.shell("Verbindung zu einer Datenbank herstellen"); // SWTBotCCombo
		 * cbTyp=bot.ccomboBoxWithLabel("Geben Sie hier den Typ ein"); // cbTyp.setSelection("H2");
		 * // SWTBotText dbName=bot.textWithLabel("Datenbankname"); //
		 * dbName.setText(testDir.getAbsolutePath()+File.separator+"db"); loginDialog.setFocus(); //
		 */
	}
	
	/*
	 * TODO: Dieser Test selectiert nach dem Elexis-Start den ersten Patienten, dessen Name mit
	 * "testpers" anfängt.
	 * 
	 * @throws Exception Wenn der Test fehlschlägt (@see JUnit) /
	 * 
	 * @Test public void canSelectTestPerson() throws Exception{
	 * 
	 * SWTBotText tName = bot .textWithTooltip(
	 * "Filterbedingungen eingeben. Beginnen Sie mit '%', um innerhalb des Worts zu suchen."); if
	 * (tName != null) { tName.setText("testpers"); // Und irgendwas Schlaues machen. } }
	 */
}
