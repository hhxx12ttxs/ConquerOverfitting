package mypackage;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.ObjectChoiceField;

public class Custom_DropDownField extends ObjectChoiceField {
	public static final int TEXT_HALINGMENT_CENTER = 1;

	String[] m_Choices;

	private int m_MarginLeft = 1;

	private int m_PaddingTop = 1 + 1;
	private int m_PaddingBottom = 1;
	private int m_MarginTop = 1;
	private int m_TextMarginRight = 5;
	private int m_TextMarginLeft = 5;
	private int m_HTextAlignment = 0;
	private int m_ChoiceFieldWidth;
	private int m_Height;
	private boolean m_isFocused = false;
	private Bitmap image, image_hover;
	private Font font;
	private FontFamily fontFamily[] = FontFamily.getFontFamilies();

	public Custom_DropDownField(String label, String[] choices,
			int initialIndex, long style) {
		super(label, choices, initialIndex, Field.FIELD_LEFT | Field.FOCUSABLE);

		m_Choices = choices;
		font = fontFamily[0].getFont(FontFamily.CBTF_FONT, 15);
		Font currFont = font;
		m_Height = currFont.getHeight();
		m_ChoiceFieldWidth = 250;
		image = Config_GlobalFunction.Bitmap("dropdown-box-1.png");
		image_hover = Config_GlobalFunction.Bitmap("dropdown-box-1_active.png");
	}

	public void setChoices(Object[] choices) {
		super.setChoices(choices);
		m_Choices = (String[]) choices;
	}

	public int getPreferredWidth() {
		return image_hover.getWidth() + 5;
	}

	public int getPreferredHeight() {
		return image.getHeight();
	}

	public boolean isFocusable() {
		return true;
	}

	public void getFocusRect(XYRect rect) {

		rect.set(m_MarginLeft + getFont().getAdvance(getLabel()), m_MarginTop,
				m_ChoiceFieldWidth, m_Height + m_PaddingBottom + m_PaddingTop
						+ 6);
	}

	protected void drawFocus(Graphics graphics, boolean on) {
		invalidate();
	}

	protected void layout(int width, int height) {
		setExtent(Math.min(getPreferredWidth(), width),
				Math.min(getPreferredHeight(), height));
	}

	public void onFocus(int direction) {
		m_isFocused = true;
		invalidate();
	}

	public void onUnfocus() {
		m_isFocused = false;
		invalidate();
	}

	public void paint(Graphics g) {
		String visibleText = (getSelectedIndex() == -1 || getSelectedIndex() >= m_Choices.length) ? ""
				: m_Choices[getSelectedIndex()];
		int textLength = font.getAdvance(visibleText);
		int labelLength = font.getAdvance(getLabel());
		int textY = m_MarginTop + m_PaddingTop + (m_Height - font.getHeight())
				/ 2;
		if (textY < 0)
			textY = m_MarginTop;

		while (textLength > m_ChoiceFieldWidth - m_TextMarginLeft
				- m_TextMarginRight
				&& visibleText.length() > 0) {
			visibleText = visibleText.substring(0, visibleText.length() - 1);
			textLength = font.getAdvance(visibleText);
		}
		int textX = (m_HTextAlignment == TEXT_HALINGMENT_CENTER) ? m_MarginLeft
				+ labelLength
				+ m_TextMarginLeft
				+ (m_ChoiceFieldWidth - m_TextMarginLeft - m_TextMarginRight - textLength)
				/ 2
				: m_MarginLeft + labelLength + m_TextMarginLeft;
		if (textX < 0)
			textX = m_MarginLeft + labelLength + m_TextMarginLeft;

		g.drawText(getLabel(), m_MarginLeft, 0);
		if (m_isFocused == true) {
			g.drawBitmap(m_MarginLeft + labelLength - 1, m_MarginTop - 1,
					image.getWidth(), image.getHeight() + 11, image_hover, 0, 0);
			g.drawText(visibleText, textX, 0, 0, 230);
		} else {
			g.drawBitmap(m_MarginLeft + labelLength - 1, m_MarginTop - 1,
					image_hover.getWidth(), image_hover.getHeight() + 11,
					image, 0, 0);
			g.drawText(visibleText, textX, 0, 0, 230);
		}
	}
}
