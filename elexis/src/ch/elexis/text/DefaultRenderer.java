package ch.elexis.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

import ch.elexis.ElexisException;
import ch.elexis.text.model.Range;

public class DefaultRenderer implements IRangeRenderer {
	 
	@Override
	public boolean canRender(String rangeType, OUTPUT outputType) {
		if(rangeType.equals(Range.TYPE_MARKUP)){
			if(outputType.equals(OUTPUT.STYLED_TEXT)){
				return true;
			}
		}
		return false;
	}

	@Override
	public Object doRender(Range range, OUTPUT outputType)
			throws ElexisException {
		if(range.getType().equals(Range.TYPE_MARKUP)){
			StyleRange sr=new StyleRange();
			sr.start=range.getPosition();
			sr.length=range.getLength();
			String id=range.getID();
			if(id.equals("bold")){
				sr.fontStyle=SWT.BOLD;
			}else if(id.equals("italic")){
				sr.fontStyle=SWT.ITALIC;
			}else if(id.equals("underline")){
				sr.underline=true;
			}
			return sr;
		}else{
			throw new ElexisException(getClass(), range.getType()+" not supported", ElexisException.EE_NOT_SUPPORTED);
		}
	}

}
