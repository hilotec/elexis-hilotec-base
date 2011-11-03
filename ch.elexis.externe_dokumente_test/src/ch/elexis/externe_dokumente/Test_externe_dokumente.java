package ch.elexis.externe_dokumente;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.data.Patient;
import ch.elexis.extdoc.preferences.PreferenceConstants;
import ch.elexis.extdoc.util.MatchPatientToPath;

public class Test_externe_dokumente {
	private Patient helena;
	private Patient werner;
	private Patient fritz;
	private Patient anneCecile;
	
	static class PathToFirstAndFamily {
		public String path;
		public String firstName;
		public String familyName;
		
		PathToFirstAndFamily(String pat, String family, String first){
			path = pat;
			firstName = first;
			familyName = family;
		}
	}
	
	static class PatOldNew {
		public Patient p;
		public String alt;
		public String neu;
		public int nrFiles;
		
		PatOldNew(Patient pat, String old, String neuer, int nrF){
			p = pat;
			alt = old;
			neu = neuer;
			nrFiles = nrF;
		}
	}
	
	PatOldNew[] validExamples;
	static String base_1;
	static String base_2;
	static String base_3;
	
	@Before
	public void setUp(){
		try {
			String testPfad_1 = "/data/test/1";
			String testPfad_2 = "/data/test/2";
			String testPfad_3 = "/data/test/3";
			File test1 = new File(testPfad_1);
			File test2 = new File(testPfad_2);
			File test3 = new File(testPfad_3);
			// Only org.apache.commons.io.FileUtils. has a simple method to recursively delete all
// files
			test1.mkdirs();
			test2.mkdirs();
			test3.mkdirs();
			ch.elexis.Hub.localCfg.set(PreferenceConstants.BASIS_PFAD1, testPfad_1);
			ch.elexis.Hub.localCfg.set(PreferenceConstants.BASIS_PFAD2, testPfad_2);
			ch.elexis.Hub.localCfg.set(PreferenceConstants.BASIS_PFAD3, testPfad_3);
			ch.elexis.Hub.userCfg.set(PreferenceConstants.SELECTED_PATHS, "7");

			PreferenceConstants.PathElement[] prefElems = PreferenceConstants.getPrefenceElements();
			base_1 = prefElems[0].baseDir;
			base_2 = prefElems[1].baseDir;
			base_3 = prefElems[2].baseDir;
			if (anneCecile == null) {
				anneCecile = new Patient("Beck", "Anne-Cécile", "01.07.2002", "f");
				fritz = new Patient("Meier", "Fritz", "04.01.1981", "m");
				helena = new Patient("Duck", "Helena", "01.01.2001", "f");
				werner = new Patient("Giezendanner", "Werner", "30.12.1980", "m");
			}
			PatOldNew[] valid =
				{
					new PatOldNew(anneCecile, base_1 + "/Beck  Anne-Cécile Pers.tif", base_1
						+ "/Beck  Annecécile 2002-07-01/Beck  Anne-Cécile Pers.tif", 2),
					new PatOldNew(anneCecile, base_1 + "/Beck  Anne-Cécile zweites dokument.txt", base_1
						+ "/Beck  Annecécile 2002-07-01/Beck  Anne-Cécile zweites dokument.txt", 2),
					new PatOldNew(helena, base_1 + "/Duck  Helena Pers.tif", base_1
						+ "/Duck  Helena 2001-01-01/Duck  Helena Pers.tif", 2),
					new PatOldNew(helena, base_1 + "/Duck  Helena.tif", base_1
						+ "/Duck  Helena 2001-01-01/Duck  Helena.tif", 2),
					new PatOldNew(fritz, base_1 + "/Meier Fritz PilonFxR_StnOSME RoeKSL.pdf",
						base_1 + "/Meier Fritz 1981-01-04/Meier Fritz PilonFxR_StnOSME RoeKSL.pdf", 1),
					new PatOldNew(werner, base_1 + "/GiezenWerner Antikoagulation.xls", base_1
						+ "/GiezenWerner 1980-12-30/GiezenWerner Antikoagulation.xls", 4),
					new PatOldNew(werner, base_2 + "/GiezenWerner Antikoagulation.txt", base_2
						+ "/GiezenWerner 1980-12-30/GiezenWerner Antikoagulation.txt", 4),
					new PatOldNew(werner, base_3 + "/GiezenWerner Antikoagulation.txt", base_3
						+ "/GiezenWerner 1980-12-30/GiezenWerner Antikoagulation.txt", 4),
					new PatOldNew(werner, base_3 + "/GiezenWerner test.txt", base_3
						+ "/GiezenWerner 1980-12-30/GiezenWerner test.txt", 4)
				};
			validExamples = valid;
			File temp = File.createTempFile("abc", "b");
			String dirName = temp.getAbsolutePath();
			File dir = new File(dirName);
			dir.mkdir();
			dir.deleteOnExit();
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}
	
	@After
	public void tearDown(){}
	
	@Test
	public void testSplitValid(){
		PathToFirstAndFamily[] valid =
			{
				new PathToFirstAndFamily("anything/Aack  Helena Pers.tif", "Aack", "Helena"),
				new PathToFirstAndFamily("anything/Back  Helena", "Back", "Helena"),
				new PathToFirstAndFamily("anything/Ceck  Max", "Ceck", "Max"),
				new PathToFirstAndFamily("anything/deeper/Dack  Helena.tif", "Dack", "Helena"),
			};
		for (int j = 0; j < valid.length; j++) {
			PathToFirstAndFamily t = valid[j];
			String names[] = MatchPatientToPath.getFirstAndFamilyNameFromPathOldConvention(t.path);
			String first  = names[0];
			String family = names[1];
			if (!first.equals(t.firstName))
				System.out.format("first %s should match %s", first, t.firstName);
			if (!family.equals(t.familyName))
				System.out.format("family %s should match %s", family, t.familyName);

			Assert.assertEquals("path and first  name should match", t.firstName, first);
			Assert.assertEquals("path and family name should match", t.familyName, family);
		}
	}
	
	@Test
	public void testSplitInalid(){
		PathToFirstAndFamily[] invalid =
			{
				new PathToFirstAndFamily("anything/deeper/Duck  Max.tif", "Duck", "Max "),
				new PathToFirstAndFamily("anything/Duck  Helena Pers.tif", "Duck ", "Helena"),
				new PathToFirstAndFamily("anything/Duck  Helena", "Duck", "Helen"),
				new PathToFirstAndFamily("anything/deeper/Duck  Max.tif", "Duck", "May"),
				new PathToFirstAndFamily("anything/deeper/Duck  Max", "Duck", "Max "),
			};
		for (int j = 0; j < invalid.length; j++) {
			PathToFirstAndFamily t = invalid[j];
			String first = MatchPatientToPath.getFirstAndFamilyNameFromPathOldConvention(t.path)[0];
			String family =
				MatchPatientToPath.getFirstAndFamilyNameFromPathOldConvention(t.path)[1];
			assert (first.equals(t.firstName));
			Assert.assertNotSame("path and family name must not match", family, t.familyName);
			assert (first.equals(t.familyName));
		}
	}
	
	@Test
	public void testMoveIntoSubDir(){
		for (int j = 0; j < validExamples.length; j++) {
			System.out.format("alt: %1s  => \nneu: %2s", validExamples[j].alt, validExamples[j].neu);
			// create old file
			File file = new File(validExamples[j].alt);
			try {
				file.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			assertTrue(file.exists());
			File neu = new File(validExamples[j].neu);
			if (neu.exists())
				assertTrue(neu.delete());
			
			// test where it should be moved to
			MatchPatientToPath m = new MatchPatientToPath(validExamples[j].p);
			String should = m.ShouldBeMovedToThisSubDir(validExamples[j].alt, validExamples[j].p.getGeburtsdatum());
			if (!validExamples[j].neu.equals(should))
				System.out.format("alt: %s => \nneu: %s should be equal \nshd: %s\n", validExamples[j].alt,
					validExamples[j].neu, should);
			assertEquals(validExamples[j].neu, should);
			if (!file.exists()) {
				boolean success = false;
				try {
					success = file.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				assertTrue(success);
			}
		}		
		List<File> oldFiles = MatchPatientToPath.getAllOldConventionFiles();
		assert (oldFiles.size() > 0);
		Iterator<File> iterator = oldFiles.iterator();
		while (iterator.hasNext()) {
			MatchPatientToPath.MoveIntoSubDir(iterator.next().getAbsolutePath());
		}
		
		for (int j = 0; j < validExamples.length; j++) {
			File neu = new File(validExamples[j].neu);
			if (!neu.exists())
				System.out.format("alt: %1s should exist %2s\n", neu.getAbsolutePath(), neu.exists());
			assertTrue(neu.exists());
			Object allFiles = MatchPatientToPath.getFilesForPatient(validExamples[j].p, null);
			assertEquals("class java.util.ArrayList", allFiles.getClass().toString());
			ArrayList<String> tst = (ArrayList<String>) allFiles;
			if (tst.size() != validExamples[j].nrFiles)
				System.out.format("validExamples %d %s: allFiles %d should match size of %d\n", j, validExamples[j].alt, validExamples[j].nrFiles, tst.size());
			assertTrue( tst.size() <= validExamples[j].nrFiles );
		}
	}
}
