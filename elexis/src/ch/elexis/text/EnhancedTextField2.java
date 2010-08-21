package ch.elexis.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.Desk;
import ch.elexis.preferences.PreferenceConstants;
import ch.elexis.util.IKonsExtension;
import ch.elexis.util.SWTHelper;
import ch.rgw.tools.GenericRange;

public class EnhancedTextField2 extends Composite implements IRichTextDisplay {
	private StyledText st;
	
	public EnhancedTextField2(Composite parent) {
		super(parent, SWT.NONE);
		setLayout(new GridLayout());
		st = new StyledText(this, SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		st.setFont(Desk.getFont(PreferenceConstants.USR_DEFAULTFONT));
		st.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
	}

	@Override
	public void addXrefHandler(String id, IKonsExtension ike) {
		// TODO Auto-generated method stub

	}

	@Override
	public void insertXRef(int pos, String textToDisplay, String providerId,
			String itemID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDropReceiver(Class<?> clazz, IKonsExtension konsExtension) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getContentsAsXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContentsPlaintext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GenericRange getSelectedRange() {
		// TODO Auto-generated method stub
		return null;
	}

}
