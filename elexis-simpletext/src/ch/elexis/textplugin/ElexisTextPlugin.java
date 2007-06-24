package ch.elexis.textplugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.text.ITextPlugin;
import ch.elexis.text.ReplaceCallback;

/**
 * @author bogdan314
 */
public class ElexisTextPlugin implements ITextPlugin {

	private ElexisEditor editor;

	private boolean showToolbar = true;

	private PageFormat pageFormat;

	private String font;

	private int style;

	private float size;

	public static ElexisTextPlugin tempInstance;

	public ElexisTextPlugin() {
		tempInstance = this;
	}

	public boolean clear() {
		if (editor != null) {
			editor.page.clear();
		}
		return true;
	}

	public Composite createContainer(Composite parent, ICallback handler) {
		if (editor == null) {
			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout grid = new GridLayout();
			grid.numColumns = 1;
			composite.setLayout(grid);

			editor = new ElexisEditor(composite, handler);
			GridData spec = new GridData();
			spec.horizontalAlignment = GridData.FILL;
			spec.grabExcessHorizontalSpace = true;
			spec.verticalAlignment = GridData.FILL;
			spec.grabExcessVerticalSpace = true;
			editor.setLayoutData(spec);

			showToolbar(showToolbar);

			return composite;
		} else {
			return editor.getParent();
		}
	}

	public boolean createEmptyDocument() {
		return clear();
	}

	public String getMimeType() {
		return "Mime-Type";
	}

	public boolean insertTable(String place, int properties, String[][] contents,
			int[] columnSizes) {
		
		if (editor == null) {
			return false;
		}

		Pattern pattern = Pattern.compile(place);
		String text = editor.page.getText();
		Matcher matcher = pattern.matcher(text);
		if (!matcher.find()) {
			return false;
		}
		
		editor.insertTable(matcher.start(), matcher.end(), contents, 
				(properties & FIRST_ROW_IS_HEADER) != 0, (properties & GRID_VISIBLE) != 0,
				font, (int) size, style);
		
		return false;
	}
	
	public Object insertText(String marke, final String text, int adjust) {
		if (editor == null) {
			return false;
		}
		return findOrReplace(marke, new ReplaceCallback() {
			public String replace(String in) {
				return text;
			}}, true);
	}

	public Object insertText(Object pos, String text, int adjust) {
		if (editor == null || !(pos instanceof Pos)) {
			return false;
		}
		Pos pospos = (Pos) pos;
		try {
			pospos.text.setCaretOffset(pospos.caret);
			pospos.text.insert(text);
			StyleRange original = pospos.text.getStyleRangeAtOffset(pospos.caret);
			if (original == null) {
				original = new StyleRange();
			}
			StyleRange style = (StyleRange) original.clone();
			style.start = pospos.caret;
			style.length = text.length();
			style.font = font != null ? new Font(editor.getDisplay(), font, (int) size, this.style) : null;
			style.fontStyle = this.style != 0 ? this.style : style.fontStyle;
			pospos.text.setStyleRange(style);
			
			Pos p = new Pos();
			p.text = pospos.text;
			p.caret = pospos.caret + text.length();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		return null;
	}

	public Object insertTextAt(int x, int y, int w, int h, String text, int adjust) {
		TextBox box = editor.insertBox(x, y, w, h);
		box.setText(text);
		return new Pos(box, text.length());
	}

	public boolean loadFromStream(InputStream is, boolean asTemplate) {
		return false;
	}

	public boolean print(String toPrinter, String toTray, boolean waitUntilFinished) {
		return false;
	}

	public boolean setFont(String name, int style, float size) {
		this.font = name;
		this.style = style;
		this.size = size;
		return true;
	}

	public void showMenu(boolean b) {
	}

	public void showToolbar(boolean b) {
		if (editor != null) {
			editor.toolBar.setVisible(b);
			GridData data = (GridData) editor.toolBar.getLayoutData();
			data.exclude = !b;
			editor.layout();
		} else {
			showToolbar = b;
		}
	}

	public byte[] storeToByteArray() {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(bout);

			editor.page.writeTo(out);

			bout.close();
			out.close();
			return bout.toByteArray();
		} catch (Exception ex) {
			ex.printStackTrace();
			return new byte[0];
		}
	}

	public boolean loadFromByteArray(byte[] bs, boolean asTemplate) {
		ByteArrayInputStream bin = new ByteArrayInputStream(bs);
		DataInputStream in = new DataInputStream(bin);
		try {
			editor.page.readFrom(in);

			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean findOrReplace(String pattern, ReplaceCallback cb) {
		return findOrReplace(pattern, cb, false) != null;
	}
	
	private Pos findOrReplace(String pattern, ReplaceCallback cb, boolean firstTimeOnly) {
		// carefull, might throw: PatternSyntaxException
		if (editor != null) {
			Pattern regexp = Pattern.compile(pattern);
			Pos result = null;
			
			result = findOrReplace(regexp, editor.page, cb, firstTimeOnly);
			if (result != null && (cb == null || firstTimeOnly)) {
				// no reason to keep searching
				return result;
			}

			for (Iterator<TextBox> it = editor.page.textBoxes.iterator(); it.hasNext(); ) {
				TextBox box = it.next();
				result = findOrReplace(regexp, box, cb, firstTimeOnly);
				if (result != null && (cb == null || firstTimeOnly)) {
					// no reason to keep searching
					return result;
				}
			}
			return result;
		}	
		return null;
	}
	
	private Pos findOrReplace(Pattern pattern, StyledText styledText, ReplaceCallback callback, boolean firstTimeOnly) {
		String text = styledText.getText();
		Matcher matcher = pattern.matcher(text);
		if (!matcher.find()) {
			return null;
		}
		int diff = 0;
		do {
			int start = matcher.start();
			int end = matcher.end();
			String str = text.substring(start, end);
			if (callback != null) {
				String replace = callback.replace(str);
				StyleRange style = styledText.getStyleRangeAtOffset(start + diff);
				style = (StyleRange) style.clone();
				if (firstTimeOnly) {
					// in this case, an insertion is made, so set the new style to the cached one
					style = (StyleRange) style.clone();
					style.fontStyle = (this.style != 0 ? this.style : style.fontStyle);
					if (font != null) {
						style.font = new Font(editor.getDisplay(), this.font, (int) size, this.style);
					}
				}
				styledText.replaceTextRange(start + diff, end-start, replace);
				style.start = start + diff;
				style.length = replace.length();
				styledText.setStyleRange(style);
				diff += replace.length() - end + start;
				if (firstTimeOnly) {
					return new Pos(styledText, end + diff);
				}
			} else {
				// no reason to keep searching
				return new Pos(styledText, end);
			}
			
		} while (matcher.find());
		return new Pos();
	}

	public PageFormat getFormat() {
		return pageFormat;
	}

	public void setFocus() {
		if (editor != null) {
			editor.page.forceFocus();
		}
	}

	public void setFormat(PageFormat f) {
		this.pageFormat = f;
	}

	public void dispose() {
		
	}
	
	static class Pos {
		StyledText text;
		int caret;
		public Pos() {}
		public Pos(StyledText text, int caret) {
			this.text = text;
			this.caret = caret;
		}
	}

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	public void setSaveOnFocusLost(boolean bSave) {
		// TODO Auto-generated method stub
		
	}
}
