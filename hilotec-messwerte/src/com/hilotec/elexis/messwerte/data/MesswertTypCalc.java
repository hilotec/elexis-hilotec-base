/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    A. Kaufmann - initial implementation 
 *    
 * $Id$
 *******************************************************************************/

package com.hilotec.elexis.messwerte.data;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swt.widgets.Text;

import bsh.EvalError;
import bsh.Interpreter;

import ch.elexis.util.SWTHelper;
import ch.rgw.tools.Log;

/**
 * @author Antoine Kaufmann
 */
public class MesswertTypCalc extends MesswertBase implements IMesswertTyp {
	private Log log = Log.get("Messwerte");

	/**
	 * Eigentlicher Code der Formel
	 */
	private String formula;
	
	/**
	 * Interpreter, der benutzt werden soll, um die 
	 */
	private String interpreter;

	/**
	 * Liste mit den Variablen die fuer die Formel gesetzt werden sollen
	 */
	private ArrayList<CalcVar> variables = new ArrayList<CalcVar>(); 
	
	
	public MesswertTypCalc(String n, String t, String u) {
		super(n, t, u);
	}
	
	/**
	 * Kontext des Interpreters vorbereiten um die Formel auswerten zu koennen.
	 * Dabei werden die Variablen importiert.
	 * 
	 * TODO: Ist noch Beanshell-spezifisch
	 * 
	 * @param interpreter Interpreter
	 * @param messung     Messung in der die Formel ausgewertet werden soll
	 * @throws EvalError
	 */
	private void interpreterSetzeKontext(Interpreter interpreter,
		Messung messung) throws EvalError
	{
		for (CalcVar cv: variables) {
			Object wert = holeVariable(messung, cv.getName(), cv.getSource());
			if (wert != null) {
				interpreter.set(cv.getName(), wert);
			}
		}
	}
	
	/**
	 * Wert einer Variable fuer die Formel bestimmen
	 * 
	 * @param messung Messung in der die Formel ausgewertet werten soll
	 * @param name    Name der Variable. Kann mit . getrennt sein, wenn sich
	 *                links vom Punkt jeweils ein Data-Feld befindet, dabei
	 *                bezieht sich der Teil rechts vom Punkt auf das Feld in
	 *                dem referenzierten Objekt.
	 * @param source  Quelle der Variable
	 * 
	 * @return Wert der dem Interpreter uebergeben werden soll. Haengt vom typ
	 *         der Variable ab.
	 */
	private Object holeVariable(Messung messung, String name, String source) {
		String[] parts = source.split("\\.");
		Messwert messwert = messung.getMesswert(parts[0]);
		IMesswertTyp typ = messwert.getTyp();
		
		if (parts.length == 1) {
			if (typ instanceof MesswertTypNum) {
				return Double.parseDouble(messwert.getWert());
			} else if (typ instanceof MesswertTypBool) {
				return Boolean.parseBoolean(messwert.getWert());
			} else if (typ instanceof MesswertTypStr) {
				return messwert.getWert();
			} else if (typ instanceof MesswertTypCalc) {
				return Double.parseDouble(messwert.getDarstellungswert());
			} else if (typ instanceof MesswertTypEnum) {
				return Integer.parseInt(messwert.getWert());
			} else if (typ instanceof MesswertTypData) {
				log.log("Fehler beim Auswerten einer Variable(" + name +"): " +
						"wertet auf ein Data-Feld aus.", Log.ERRORS);
				return null;
			}
		}
		
		if (!(typ instanceof MesswertTypData)) {
			log.log("Fehler beim Auswerten einer Variable(" + name + "): "+
				"Dereferenziertes Feld ist nicht vom Typ DATA", Log.ERRORS);
			return null;
		}
		MesswertTypData t = (MesswertTypData) typ;
		Messung dm = t.getMessung(messwert);
		return holeVariable(dm, name + "." +
			parts[0],source.substring(source.indexOf(".") + 1));
	}
	
	/**
	 * Interne Klasse die eine Variable fuer die Formel darstellt(nur
	 * deklaration).
	 * 
	 * @author Antoine Kaufmann
	 */
	private class CalcVar {
		/**
		 * Name der Variable
		 */
		private String name;
		
		/**
		 * Quelle der Variable(meist Feldname in der Messung) 
		 */
		private String source;
		
		CalcVar(String n, String s) {
			name = n;
			source = s;
		}
		
		String getName() {
			return name;
		}
		
		String getSource() {
			return source;
		}
	}
	
	/**
	 * Neue Variable hinzufuegen
	 * 
	 * @param name   Name der Variable  
	 * @param source Quelle fuer den Variableninhalt
	 */
	public void addVariable(String name, String source) {
		variables.add(new CalcVar(name, source));
	}
	
	/**
	 * Formel, die berechnet werden soll, setzen.
	 *
	 * @param f Formel
	 * @param i Interpreter fuer die Formel
	 */
	public void setFormula(String f, String i) {
		formula = f;
		interpreter = i;
	}

	public String erstelleDarstellungswert(Messwert messwert) {
		if (!interpreter.equals("beanshell")) {
			log.log("Unbekannter Interpreter: " + interpreter, Log.ERRORS);
			return "";
		}
		
		Interpreter interpreter = new Interpreter();

		try {
			interpreterSetzeKontext(interpreter, messwert.getMessung());
			Object wert = interpreter.eval(formula);
			return ((Double) wert).toString();
		} catch (EvalError e) {
			e.printStackTrace();
			log.log("Fehler beim Berechnen eines Wertes: " + e.getMessage(), Log.ERRORS);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getDefault() {
		return "";
	}

	public void setDefault(String str) {
	}
	
	public Widget createWidget(Composite parent, Messwert messwert) {
		Text text = SWTHelper.createText(parent, 1, SWT.NONE);
		text.setText(messwert.getDarstellungswert());
		text.setEditable(false);
		return text;
	}
	
	public void saveInput(Widget widget, Messwert messwert) {
	}
}
