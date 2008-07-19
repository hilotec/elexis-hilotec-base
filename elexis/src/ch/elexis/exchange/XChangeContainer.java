package ch.elexis.exchange;

import org.jdom.Namespace;

public interface XChangeContainer {
	public static final Namespace ns=Namespace.getNamespace("SgamXChange","http://informatics.sgam.ch/eXChange");
	public static final Namespace nsxsi=Namespace.getNamespace("xsi","http://www.w3.org/2001/XML Schema-instance");
	public static final Namespace nsschema=Namespace.getNamespace("schemaLocation","http://informatics.sgam.ch/eXChange SgamXChange.xsd");

}
