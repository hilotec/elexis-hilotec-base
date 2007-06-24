package ch.elexis.data;

import org.eclipse.jface.action.IAction;

public interface ICodeElement {
	 /** Name des zugrundeliegenden Codesystems liefern */
    public String getCodeSystemName();
    /** Kurzcode für das System liefern */
    public String getCodeSystemCode();
    /** Eine eindeutige ID für das Element liefern */
    public String getId();
	/** Das Element in Code-Form. Aus dem Code und der Klasse muss das Element sich
	 * wieder erstellen lassen */
    public String getCode();
    /** Das Element in Klartext-Form */
    public String getText();
    /** Kontext-Aktionen für dieses Code-Element */
    public Iterable<IAction> getActions();
}
