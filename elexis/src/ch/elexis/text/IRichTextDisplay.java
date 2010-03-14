package ch.elexis.text;

import ch.elexis.util.IKonsExtension;

public interface IRichTextDisplay {

	void addXrefHandler(String id, IKonsExtension ike);

	void insertXRef(int pos, String textToDisplay, String providerId, String itemID);

	void addDropReceiver(Class<?> clazz, IKonsExtension konsExtension);

	String getDocumentAsText();

}
