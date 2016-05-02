/**
 * Copyright Š 2009 Inkrypt Technologies Corporation
 * @author Shaheen Georgee
 * @date June 10, 2010
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * Referenced;
 * {@link net.rim.device.api.math.Fixed32}
 * {@link net.rim.device.api.system.EncodedImage#scaleImage32(int, int)}
 * http://supportforums.blackberry.com/t5/Java-Development/Resizing-a-Bitmap-using-scaleImage32-instead-of-setScale/m-p/255438
 */
package com.inkrypt.bb.ui;

import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.RadioButtonGroup;

/**
 * Custom button field that shows how to use images as button backgrounds. The behaviour of this button field is like that of a radio button
 * field;
 * <ul>
 * <li>1) At most one PictureRadioButtonField may be selected from a group</li>
 * <li>2) A selected PictureRadioButtonField looks different from its unselected state.</li>
 * <li>3) In addition to 2), onFocus and unFocus further change the appearance of a PictureRadioButtonField</li>
 * </ul>
 */
public class PictureRadioButtonField extends RadioButtonField {

  private static final int PADDING_NAVTOOLBAR_VERTICAL = 2;
  private static final int PADDING_NAVTOOLBAR_HORIZONTAL = 4;

  private final static int i_PADDING_VERTICAL = 2;
  private final static int i_PADDING_HORIZONTAL = 2;

  private static final int ROUNDRECT_OUTER_ARCWIDTH = 16;
  private static final int ROUNDRECT_OUTER_ARCHEIGHT = 16;
  private static final int ROUNDRECT_INNER_ARCWIDTH = 18;
  private static final int ROUNDRECT_INNER_ARCHEIGHT = 18;

  public static final int COLOR_FOCUSED_BORDER = Color.BLACK;
  public static final int COLOR_FOCUSED_BACKGROUND = Color.LIGHTBLUE;
  public static final int COLOR_FOCUSED_FOREGROUND = Color.WHITE;

  public static final int ALPHA_FULLY_TRANSPARENT = 0;
  public static final int ALPHA_TRANSLUCENT_1 = 51;
  public static final int ALPHA_TRANSLUCENT_2 = 102;
  public static final int ALPHA_TRANSLUCENT_3 = 153;
  public static final int ALPHA_TRANSLUCENT_4 = 204;
  public static final int ALPHA_SOLID = 255;

  private Bitmap _currentPicture;
  private Bitmap _onPicture;
  private Bitmap _offPicture;

  /**
   * @param onPicturePath
   *          A string literal indicating the path to the onFocus image
   * @param offPicturePath
   *          A string literal indicating the path to the onUnfocus image
   * @param squareSideLength
   *          The desired display width of the onFocus and onUnfocus images. The source images must be square (width == height) as
   *          distortion is undesirable.
   * @param toolTip
   *          The textual description of the action being represented by the images.
   * @param group
   *          The RadioButtonGroup this object will be added to.
   * 
   *          NOTE: The supplied source images (onFocus and onUnfocus) need not necessarily have the same dimensions as one another, so long
   *          that they are each square.
   */
  public PictureRadioButtonField(String onPicturePath, String offPicturePath, int reqSquareSideLength, String toolTip,
      RadioButtonGroup group) {

    // NOTE: constructor initialized to false to avoid fieldChangeNotify being automatically invoked and further
    // complications (see design documents)
    super(toolTip, group, false);

    // Load the images
    EncodedImage onImage = EncodedImage.getEncodedImageResource(onPicturePath);
    EncodedImage offImage = EncodedImage.getEncodedImageResource(offPicturePath);

    // Check if the supplied images are not square dimensionally
    if ((onImage.getWidth() != onImage.getHeight()) || (offImage.getWidth() != offImage.getHeight())) {
      throw new IllegalArgumentException("The provided images are not square.");
    }
    else if (reqSquareSideLength <= 0) {
      throw new IllegalArgumentException("The desired image dimension needs to be a positive pixel count.");
    }

    // Determine the desired scaling factor
    int numerator = Fixed32.toFP(onImage.getWidth());
    int denominator = Fixed32.toFP(reqSquareSideLength);
    int onScale32 = Fixed32.div(numerator, denominator);
    numerator = Fixed32.toFP(offImage.getWidth());
    int offScale32 = Fixed32.div(numerator, denominator);

    // Scale images
    onImage = onImage.scaleImage32(onScale32, onScale32);
    offImage = offImage.scaleImage32(offScale32, offScale32);

    _onPicture = onImage.getBitmap();
    _offPicture = offImage.getBitmap();
    _currentPicture = _offPicture;
  }

  protected void drawFocus(Graphics graphics, boolean on) {
    // override implemented in paint(...)
  }

  protected void onFocus(int direction) {
    super.onFocus(direction);
    invalidate();
  }

  protected void onUnfocus() {
    super.onUnfocus();
    invalidate();
  }

  protected void fieldChangeNotify(int context) {
    super.fieldChangeNotify(context);
    if (isSelected()) {
      _currentPicture = _onPicture;
    }
    else {
      _currentPicture = _offPicture;
    }
    invalidate();
  }

  public int getPreferredHeight() {
    return _currentPicture.getHeight() + (2 * PADDING_NAVTOOLBAR_VERTICAL);
  }

  public int getPreferredWidth() {
    return _currentPicture.getWidth() + (2 * PADDING_NAVTOOLBAR_HORIZONTAL);
  }

  /**
   * Field implementation.
   * 
   * @see net.rim.device.api.ui.Field#layout(int, int)
   */
  protected void layout(int width, int height) {
    setExtent(Math.min(width, getPreferredWidth()), Math.min(height, getPreferredHeight())); // REQUIRED_FEATURE
  }

  protected void paint(Graphics graphics) {
    // before paint(Graphics) is called, the region is cleared by the platform

    int originalColor = graphics.getColor();
    int previousTranp = graphics.getGlobalAlpha();

    int xLeft = i_PADDING_HORIZONTAL;
    int xRight = getWidth() - (2 * xLeft);
    int yTop = i_PADDING_VERTICAL;
    int yBottom = getHeight() - (2 * yTop);

    if (isFocus()) {
      // First, set the background colour
      graphics.setColor(COLOR_FOCUSED_BACKGROUND);
      // then draw a round rectangular background that is larger than the foreground image
      graphics.fillRoundRect(xLeft, yTop, xRight, yBottom, ROUNDRECT_OUTER_ARCWIDTH, ROUNDRECT_OUTER_ARCHEIGHT);

      // then frame the background with a border
      graphics.setColor(COLOR_FOCUSED_BORDER);
      graphics.drawRoundRect(xLeft, yTop, xRight, yBottom, ROUNDRECT_OUTER_ARCWIDTH, ROUNDRECT_OUTER_ARCHEIGHT);

      // then apply a shine to the button
      drawShineOverRegion(graphics, (PADDING_NAVTOOLBAR_HORIZONTAL * 2), (PADDING_NAVTOOLBAR_HORIZONTAL * 2), getWidth(), getHeight(), true);

      // FUTURE_FEATURE ? then draw the tooltip text
      // graphics.setColor(Color.BLACK);
      // graphics.setFont(_font);
      // graphics.drawText(_label, 4, 2, (int)( getStyle() & DrawStyle.ELLIPSIS | DrawStyle.HALIGN_MASK ), getWidth() - 6);
    }
    else if (isSelected()) {
      // First, set the background colour
      graphics.setColor(Color.WHITE);
      graphics.setGlobalAlpha(ALPHA_TRANSLUCENT_2);
      // then draw a round rectangular background that is larger than the foreground image
      graphics.fillRoundRect(xLeft, yTop, xRight, yBottom, ROUNDRECT_OUTER_ARCWIDTH, ROUNDRECT_OUTER_ARCHEIGHT);

      // then frame the background with a border
      graphics.setColor(COLOR_FOCUSED_BORDER);
      graphics.drawRoundRect(xLeft, yTop, xRight, yBottom, ROUNDRECT_OUTER_ARCWIDTH, ROUNDRECT_OUTER_ARCHEIGHT);
    }

    // reset color and transparency
    graphics.setColor(originalColor);
    graphics.setGlobalAlpha(previousTranp);

    graphics.drawBitmap(PADDING_NAVTOOLBAR_HORIZONTAL, PADDING_NAVTOOLBAR_VERTICAL, _currentPicture.getWidth(),
        _currentPicture.getHeight(), _currentPicture, 0, 0);
  }

  public void drawShineOverRegion(Graphics graphics, int xOffset, int yOffset, int width, int height, boolean nearBottom) {

    int previousColor = graphics.getColor();
    int previousTranp = graphics.getGlobalAlpha();
    graphics.setColor(Color.WHITE);
    graphics.setGlobalAlpha(ALPHA_TRANSLUCENT_2); // set transparency

    int xNew = xOffset;
    int yNew = yOffset;
    int widthNew = width - (2 * xOffset); // No fancy stuff with width
    int heightNew;

    if (nearBottom) {
      yNew = yOffset + ((height - yOffset) / 3); // start after 1/3 the height difference (in addition to y offset)
      heightNew = height - yOffset - yNew;
    }
    else { // near Top
      heightNew = (int) (height / 1.5);
    }

    // FUTURE_FEATURE exponential transparency, with focus on upper quarter of height, extending up 4/7 of total height
    // or do a circular gradient as in Flixster app.
    // graphics.drawTexturedPath(xPts, yPts, pointTypes, offsets, xOrigin, yOrigin, dux, dvx, duy, dvy, textureData)
    graphics.fillRoundRect(xNew, yNew, widthNew, heightNew, ROUNDRECT_INNER_ARCWIDTH, ROUNDRECT_INNER_ARCHEIGHT);

    // reset
    graphics.setColor(previousColor);
    graphics.setGlobalAlpha(previousTranp);
  }
}

