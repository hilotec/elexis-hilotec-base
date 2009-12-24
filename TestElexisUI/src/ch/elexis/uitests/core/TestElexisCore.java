package ch.elexis.uitests.core;

import java.io.File;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.rgw.io.FileTool;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TestElexisCore {
	private final SWTWorkbenchBot bot=new SWTWorkbenchBot();
	private static File testDir;
	
	/**
	 * Am Anfang der Tests wird eine H2-Datenbank entweder aus einem dump zurückgeladen oder neu erstellt.
	 * @throws Exception
	 */
	@BeforeClass
	public static void initialConnect() throws Exception{
		/* does not work
		String dir=System.getProperty("user.home")+File.separator+"elexisTests";
		testDir=new File(dir,"h2db");
		File backup=new File(dir,"h2db_backup");
		FileTool.deltree(testDir.getAbsolutePath());
		testDir.mkdirs();
		if(backup.exists()){
			FileTool.copyDirectory(backup, testDir);
		}
		*/
	}
	
	@Test
	public void createTestPatient() throws Exception{
		//SWTBotShell loginDialog=bot.shell("Elexis - Anmeldung");
		/* redesign
		bot.textWithLabel("Anwendername").setText("test");
		bot.textWithLabel("Passwort").setText("test");
		bot.buttonWithLabel("Anmelden").click();
		bot.menu("Datei").menu("Verbindung...").click();
		SWTBotShell dialog=bot.shell("Verbindung zu einer Datenbank herstellen");
		SWTBotCCombo cbTyp=bot.ccomboBoxWithLabel("Geben Sie hier den Typ ein");
		cbTyp.setSelection("H2");
		SWTBotText dbName=bot.textWithLabel("Datenbankname");
		dbName.setText(testDir.getAbsolutePath()+File.separator+"db");
		bot.buttonWithLabel("Finish").click();
		*/
	}
	
	/**
	 * Dieser Test selectiert nach dem Elexis-Start den ersten Patienten, dessen Name
	 * mit "testpers" anfängt.
	 * @throws Exception Wenn der Test fehlschlägt (@see JUnit)
	 */
	@Test
	public void canSelectTestPerson() throws Exception{
		
		SWTBotText tName =
			bot
			.textWithTooltip("Filterbedingungen eingeben. Beginnen Sie mit '%', um innerhalb des Worts zu suchen.");
		if (tName != null) {
			tName.setText("testpers");
			// Und irgendwas Schlaues machen.
		}
	}
}
