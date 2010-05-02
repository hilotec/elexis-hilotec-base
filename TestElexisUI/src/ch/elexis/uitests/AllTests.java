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
package ch.elexis.uitests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import ch.elexis.uitests.core.*;
import ch.elexis.uitests.laborImport.*;

/*
 * Setup all SWTbot test for the elexis application
 * Don't forget to test your classes in other languages/OS, too.
 * 
 */

@RunWith(Suite.class)
@SuiteClasses( {
	TestElexisCore.class, TestAnalytica.class
})
public class AllTests {

}
