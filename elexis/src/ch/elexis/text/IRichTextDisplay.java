package ch.elexis.text;

import ch.elexis.util.IKonsExtension;
import ch.rgw.tools.GenericRange;

public interface IRichTextDisplay {

	public void addXrefHandler(String id, IKonsExtension ike);

	public void insertXRef(int pos, String textToDisplay, String providerId, String itemID);

	public void addDropReceiver(Class<?> clazz, IKonsExtension konsExtension);

	public String getWordUnderCursor();
	
	public String getContentsAsXML();
	
	public String getContentsPlaintext();
	
	public GenericRange getSelectedRange();
	
}
