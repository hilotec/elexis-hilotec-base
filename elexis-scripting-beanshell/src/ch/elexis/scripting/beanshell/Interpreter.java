package ch.elexis.scripting.beanshell;

import bsh.EvalError;
import bsh.ParseException;
import bsh.TargetError;
import ch.elexis.ElexisException;
import ch.elexis.data.Script;
import ch.elexis.util.SWTHelper;

public class Interpreter implements ch.elexis.scripting.Interpreter {
	bsh.Interpreter scripter = new bsh.Interpreter();

	public Interpreter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setValue(String name, Object value) throws ElexisException {
		// TODO Auto-generated method stub

	}

	@Override
	public Object run(String script) throws ElexisException {
		try {

			return scripter.eval(script);
		} catch (TargetError e) {
			SWTHelper.showError("Script target Error", "Script Fehler",
					"Target Error: " + e.getTarget());
			throw (new ElexisException(Script.class, e.getMessage(),
					ElexisException.EE_UNEXPECTED_RESPONSE));
		} catch (ParseException e) {
			String msg = "";
			if (e != null) {
				try {
					msg = e.getErrorText();
					if (msg == null)
						;
					msg = "";
				} catch (Exception ex) {
					msg = "unbekannter Fehler";
				}
			}
			String line = "Script Syntax Fehler " + msg;
			String titel = "Script syntax Error";
			SWTHelper.showError(titel, line);
			// throw(new Exception(e.getMessage()));
		} catch (EvalError e) {
			SWTHelper.showError("Script general error", "Script Fehler",
					"Allgemeiner Script Fehler: " + e.getErrorText());
			throw (new ElexisException(Script.class,
					"Allgemeiner Script Fehler: " + e.getErrorText(),
					ElexisException.EE_UNEXPECTED_RESPONSE));
		}
		return null;

	}

}
