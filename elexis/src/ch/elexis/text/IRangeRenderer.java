package ch.elexis.text;

import ch.elexis.ElexisException;
import ch.elexis.text.model.Range;

/**
 * Contract to display a Range in some specific context
 * @author Gerry Weirich
 *
 */
public interface IRangeRenderer {
	public static enum OUTPUT{HTML,STYLED_TEXT,XCHANGE};
	
	/**
	 * Ask whether this IRangeRenderer can create output of a specific type
	 * @param rangeType the type of the Range to render
	 * @param outputType teh type of desired output
	 * @return
	 */
	public boolean canRender(String rangeType, OUTPUT outputType);
	/**
	 * creaste a visual representation of a Range in a given type
	 * @param range the Range to render
	 * @param outputType the desired type of output
	 * @return an output specific result.
	 */
	public Object doRender(Range range, OUTPUT outputType) throws ElexisException;
}
