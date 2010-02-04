package ch.elexis.developer.resources;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * A LabelProvider renders visual Representations of Objects held in a Viewer
 * @author gerry
 *
 */
public class SampleDataLabelProvider extends LabelProvider {

	/**
	 * We can provide any image here, maybe depending on context.
	 * Or we can return null, then no image will be shown. (We could leave away this method, then)
	 */
	@Override
	public Image getImage(Object element) {
		return null;
	}

	/**
	 * A textual Representation of the object
	 */
	@Override
	public String getText(Object element) {
		SampleDataType sdt=(SampleDataType) element; // it will always be this type
		return sdt.getLabel(); // We keep things simple
	}

}
