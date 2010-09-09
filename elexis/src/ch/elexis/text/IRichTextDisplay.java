package ch.elexis.text;

import java.util.Map;

import ch.elexis.util.IKonsExtension;
import ch.rgw.tools.GenericRange;

public interface IRichTextDisplay {

	/**
	 * Note IKonsExtension is replaced by IRangeRenderer/SSDRange. addXRefHandler
	 * is deprecated because the framework finds all appropriate renderers by itself
	 * @Deprecated Don't use IKonsExtension and addXRefHandlers for new code
	 */
	public void addXrefHandler(String id, IKonsExtension ike);

	@Deprecated public void setXrefHandlers(Map<String, IKonsExtension> handlers);

	public void insertXRef(int pos, String textToDisplay, String providerId, String itemID);

	public void addDropReceiver(Class<?> clazz, IKonsExtension konsExtension);

	public String getWordUnderCursor();
	
	public String getContentsAsXML();
	
	public String getContentsPlaintext();
	
	public GenericRange getSelectedRange();
	
}
