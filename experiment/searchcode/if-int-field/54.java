/*
 * RichTextArea.java
 *
 * Copyright (C) 2011 Thomas Everingham
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * A copy of the GNU General Public License can be found in the file
 * LICENSE.txt provided with the source distribution of this program (see
 * the META-INF directory in the source jar). This license can also be
 * found on the GNU website at http://www.gnu.org/licenses/gpl.html.
 *
 * If you did not receive a copy of the GNU General Public License along
 * with this program, contact the lead developer, or write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 *
 * If you find this program useful, please tell me about it! I would be delighted
 * to hear from you at tom.ingatan@gmail.com.
 */
package org.ingatan.component.text;

import org.ingatan.component.answerfield.IAnswerField;
import org.ingatan.event.ColourChooserPaneEvent;
import org.ingatan.event.ColourChooserPaneListener;
import org.ingatan.event.RichTextToolbarEvent;
import org.ingatan.event.RichTextToolbarListener;
import org.ingatan.io.IOManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * An extension of the <code>JTextPane</code>, the rich text area includes an instance
 * of <code>RichTextToolbar</code>, which allows for easy formatting. It also sets
 * a keymap of standard formatting hot keys such as Ctrl+B for bold, Crtl+U for underline, etc.<br>
 * <br>
 * This class implements undo/redo listening, and maps the undo/redo keys
 * to Ctrl+z and Ctrl+Shift+z respectively. It also implements the SymbolMenu for easy insertion
 * of non standard keyboard characters.
 * <br>
 * The toolbar buttons have all been implemented apart from the insert picture button. You can
 * implement your own insert picture routine, following your particular needs, by using a
 * <code>RichTextToolbarListener<code> and listening for e.getEventID == RichTextToolbarEvent.INSERT_PICTURE.
 *
 * This listener can be added to the RichTextArea's toolbar using
 * </code>richTextArea.getToolbar().addRichTextToolbarListener()</code>
 *<br>
 * When adding this class to a container, you will need to user <code>container.add(richTextArea.getScroller())</code> if you
 * want the toolbar and scroll pane. Otherwise, just add it as usual.
 * @author Thomas Everingham
 * @version 1.0
 */
public class RichTextArea extends JTextPane {

    /**
     * Tag used for specifying the font family.
     */
    public static final String TAG_FONT_FAMILY = "fam";
    /**
     * Tag used for a new line (i.e. \n)
     */
    public static final String TAG_NEW_LINE = "br";
    /**
     * Tag used for specifying the font size.
     */
    public static final String TAG_FONT_SIZE = "sze";
    /**
     * Tag used for specifying font colour. The usage is as follows:<br>
     * <code>[TAG_FONT_COLOUR]red,green,blue[!TAG_FONT_COLOUR]</code><br>
     * where red, green and blue are the integer values specifying the colour.
     */
    public static final String TAG_FONT_COLOUR = "col";
    /**
     * Tag used for specifying bold font.
     */
    public static final String TAG_BOLD = "b";
    /**
     * Tag used for specifying italic font.
     */
    public static final String TAG_ITALIC = "i";
    /**
     * Tag used for specifying underlined font.
     */
    public static final String TAG_UNDERLINE = "u";
    /**
     * Tag used for specifying subscript font.
     */
    public static final String TAG_SUBSCRIPT = "sub";
    /**
     * Tag used for specifying superscript font.
     */
    public static final String TAG_SUPERSCRIPT = "sup";
    /**
     * Tag used for specifying an embedded image.
     */
    public static final String TAG_IMAGE = "img";
    /**
     * Tag used for specifying embedded JMathTeX.
     */
    public static final String TAG_MATH = "math";
    /**
     * Tag used for specifying an answer field component.
     */
    public static final String TAG_ANSWER_FIELD = "ans";
    /**
     * Tag used for specifying alignment.<br>
     * Usage is: <code>[TAG_ALIGNMENT]int[!TAG_ALIGNMENT].<br>
     * Where int is 0, 1 or 2, corresponding to left, centre or right.
     */
    public static final String TAG_ALIGNMENT = "aln";
    /**
     * This tag marks the end of the rich text mark-up
     */
    public static final String TAG_DOCUMENT_END = "end";
    /**
     * Code for the opening square brackets
     */
    public static final String CHARCODE_OPENING_SQUARE_BRACKET = "!osqb;";
    /**
     * Code for the closing square brackets
     */
    public static final String CHARCODE_CLOSING_SQUARE_BRACKET = "!csqb;";
    /**
     * The <code>RichTextToolbar</code> object associated witht this <code>RichTextArea</code> isntance.
     */
    private RichTextToolbar toolbar;
    /**
     * The scroll pane for this rich text area.
     */
    private JScrollPane scrollPane;
    /**
     * The symbol menu used by this text area.
     */
    private SymbolMenu symbolMenu;
    /**
     * The KeyStroke that activates the SymbolMenu and means that the next keystroke
     * will become the base character for the symbol menu to be shown.
     */
    private KeyStroke symbolMenuActivateKey = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK);
    /**
     * Whether or not the symbol menu activate keystroke was the previous keystroke to occur/.
     */
    boolean menuActivated = false;
    /**
     * The undo manager associated with this rich text area.
     */
    private UndoManager undo = new UndoManager();
    /**
     * Whether or not the toolbar is currently added as a column header to the scroll pane.
     */
    private boolean toolbarIsVisible = true;

    /**
     * Creates a new RichTextArea instance with the specified toolbar layout.
     * @param toolbarLayout either horizontal or vertical - use the fields in the RichTextToolbar class.
     */
    public RichTextArea(int toolbarLayout) {
        this.setMargin(new Insets(5, 5, 5, 5));
        toolbar = new RichTextToolbar(toolbarLayout);
        this.addCaretListener(new CaretListen());
        scrollPane = new JScrollPane(this);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(this.getSize());
        scrollPane.setColumnHeaderView(toolbar);
        scrollPane.setOpaque(false);
        toolbar.addRichTextToolbarListener(new ToolbarListener());
        toolbar.setLocation(0, 0);
        toolbar.addColourChooserPaneListener(new ColourListener());
        toolbar.setOpaque(true);
        symbolMenu = new SymbolMenu();
        this.add(symbolMenu);
        symbolMenu.setVisible(false);
        this.addKeyListener(new TextKeyListener());
        symbolMenu.addKeyListener(new MenuKeyListener());

        this.getStyledDocument().addUndoableEditListener(new TextUndoableEditListener());
        undo.setLimit(-1);
        setUpKeyMap();

    }

    /**
     * Creates a new RichTextArea with horizontal toolbar layout.
     */
    public RichTextArea() {
        this(RichTextToolbar.HORIZONTAL_LAYOUT);
    }

    /**
     * This method parses the raw text string that is passed to it and generates
     * the formatted text. If answer field tags are encountered, then the
     * corresponding classes will be instantiated and added to the text field.
     *
     * If image tags are encountered, the corresponding resources will be added
     * to the text field.
     *
     * This method will overwrite any existing content within the text field.
     *
     * @param rawText the text to parse into the field.
     */
    public void setRichText(String rawText) {

        //the attribute set generated by the latest parse
        SimpleAttributeSet curAttr = new SimpleAttributeSet();
        //use the previous attributes to insert string
        SimpleAttributeSet prevAttr = new SimpleAttributeSet();

        this.setText("");

        //start parsing the data in.
        Pattern p = Pattern.compile("\\[.*?\\]");
        Matcher m = p.matcher(rawText);
        int postTagTextStartIndex = 0;
        //gets appended to the string being inserted.
        String strAppend = "";
        //flag for text insertion; text between font family tags, for example, should not be inserted!
        boolean insertIntraTagText = true;
        //number of elements at a given point.
        int elCount = 0;
        while (m.find()) {
            insertIntraTagText = true;
            strAppend = "";
            if (m.group().equals("[" + TAG_BOLD + "]")) {
                StyleConstants.setBold(curAttr, !StyleConstants.isBold(curAttr));
            } else if (m.group().equals("[" + TAG_ITALIC + "]")) {
                StyleConstants.setItalic(curAttr, !StyleConstants.isItalic(curAttr));
            } else if (m.group().equals("[" + TAG_UNDERLINE + "]")) {
                StyleConstants.setUnderline(curAttr, !StyleConstants.isUnderline(curAttr));
            } else if (m.group().equals("[" + TAG_SUPERSCRIPT + "]")) {
                StyleConstants.setSuperscript(curAttr, !StyleConstants.isSuperscript(curAttr));
            } else if (m.group().equals("[" + TAG_SUBSCRIPT + "]")) {
                StyleConstants.setSubscript(curAttr, !StyleConstants.isSubscript(curAttr));
            } else if (m.group().equals("[!" + TAG_ALIGNMENT + "]")) {
                insertIntraTagText = false;
                int alignVal = Integer.valueOf(rawText.substring(postTagTextStartIndex, m.start()));
                switch (alignVal) {
                    case StyleConstants.ALIGN_LEFT:
                        StyleConstants.setAlignment(curAttr, StyleConstants.ALIGN_LEFT);
                        break;
                    case StyleConstants.ALIGN_CENTER:
                        //StyleConstants.setAlignment(curAttr, StyleConstants.ALIGN_CENTER);
                        StyleConstants.ParagraphConstants.setAlignment(curAttr, StyleConstants.ParagraphConstants.ALIGN_CENTER);
                        //curAttr.addAttribute(StyleConstants.ParagraphConstants.Alignment, StyleConstants);
                        break;
                    case StyleConstants.ALIGN_RIGHT:
                        StyleConstants.setAlignment(curAttr, StyleConstants.ALIGN_RIGHT);
                        break;
                    case StyleConstants.ALIGN_JUSTIFIED:
                        StyleConstants.setAlignment(curAttr, StyleConstants.ALIGN_JUSTIFIED);
                        break;
                }
            } else if (m.group().equals("[!" + TAG_FONT_FAMILY + "]")) {
                insertIntraTagText = false;
                curAttr.addAttribute(StyleConstants.Family, rawText.substring(postTagTextStartIndex, m.start()));
            } else if (m.group().equals("[!" + TAG_FONT_SIZE + "]")) {
                insertIntraTagText = false;
                curAttr.addAttribute(StyleConstants.Size, Integer.valueOf(rawText.substring(postTagTextStartIndex, m.start())));
            } else if (m.group().equals("[!" + TAG_FONT_COLOUR + "]")) {
                insertIntraTagText = false;
                String[] colour = rawText.substring(postTagTextStartIndex, m.start()).split(",");
                Color c = new Color(Integer.valueOf(colour[0]), Integer.valueOf(colour[1]), Integer.valueOf(colour[2]));
                curAttr.addAttribute(StyleConstants.Foreground, c);
            } else if (m.group().equals("[" + TAG_NEW_LINE + "]")) {
                strAppend = "\n";
            } else if (m.group().equals("[" + TAG_DOCUMENT_END + "]")) {
                //do nothing
            } else if (m.group().equals("[!" + TAG_ANSWER_FIELD + "]")) {
                insertIntraTagText = false;
                String[] ansFieldData = rawText.substring(postTagTextStartIndex, m.start()).split("<name;content>");

                //if there was no content text, then the split method will return an array of lenght 1, but we need an empty string to pass as the content
                if (ansFieldData.length == 1) {
                    String[] temp = new String[2];
                    temp[0] = ansFieldData[0];
                    temp[1] = "";
                    ansFieldData = temp;
                }
                //instantiate answer field using ansFieldData[0], and set its content to ansFieldData[1].
                try {
                    Class ansField = IOManager.getAnswerFieldClass(ansFieldData[0]);
                    IAnswerField newField = null;
                    if (ansField != null) {
                        newField = (IAnswerField) ansField.newInstance();
                    }
                    if (newField != null) {
                        //tell the new field instance to read in the content text, and then insert it into the document.
                        //replace square bracket codes with the square bracket characters
                        newField.readInXML(ansFieldData[1].replace(CHARCODE_OPENING_SQUARE_BRACKET, "[").replace(CHARCODE_CLOSING_SQUARE_BRACKET, "]"));
                        this.insertComponent((JComponent) newField);
                    } else {
                        System.out.println("Answer field was null after instantiation attempt - could not insert. Originating from RichTextArea.setRichText()");
                    }
                } catch (InstantiationException ex) {
                    Logger.getLogger(RichTextArea.class.getName()).log(Level.SEVERE, "Could not instantiate the answer field '" + ansFieldData[0] + " in setRichText method.", ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(RichTextArea.class.getName()).log(Level.SEVERE, "For the answer field '" + ansFieldData[0] + " in setRichText method.", ex);
                }
                //load class of name ansFieldData[0] and tell it to generate an instance using data in ansFieldData[1]
                //don't forget to set any CHARCODE_OPENING_SQUARE_BRACKET and CHARCODE_CLOSING_SQUARE_BRACKET
            } else if (m.group().equals("[!" + TAG_IMAGE + "]")) {
                String[] data = rawText.substring(postTagTextStartIndex, m.start()).replace(CHARCODE_OPENING_SQUARE_BRACKET, "[").replace(CHARCODE_CLOSING_SQUARE_BRACKET, "]").split("<;>");
                String imageID = data[0];
                String libID = data[1];
                insertIntraTagText = false;
                this.setCaretPosition(this.getStyledDocument().getLength());
                try {
                    this.insertComponent(new EmbeddedImage(IOManager.loadImage(libID, imageID), imageID, libID));
                } catch (IOException ex) {
                    Logger.getLogger(RichTextArea.class.getName()).log(Level.SEVERE, "While trying to insert an embedded image after creating it (setRichText)", ex);
                }
            } else if (m.group().equals("[!" + TAG_MATH + "]")) {
                String mathText[] = rawText.substring(postTagTextStartIndex, m.start()).split("<;>");
                String[] colVals = mathText[2].split(",");
                insertIntraTagText = false;
                this.setCaretPosition(this.getStyledDocument().getLength());
                this.insertComponent(new EmbeddedMathTeX(mathText[0].replace(CHARCODE_OPENING_SQUARE_BRACKET, "[").replace(CHARCODE_CLOSING_SQUARE_BRACKET, "]"), Integer.valueOf(mathText[1]), new Color(Integer.valueOf(colVals[0]), Integer.valueOf(colVals[1]), Integer.valueOf(colVals[2]))));
            }


            if (insertIntraTagText) {
                elCount = this.getDocument().getDefaultRootElement().getElementCount();
                try {
                    this.getStyledDocument().setParagraphAttributes(this.getStyledDocument().getLength(), this.getStyledDocument().getLength(), prevAttr, true);
                    this.getStyledDocument().insertString(this.getDocument().getLength(), (rawText.substring(postTagTextStartIndex, m.start()) + strAppend).replace(CHARCODE_OPENING_SQUARE_BRACKET, "[").replace(CHARCODE_CLOSING_SQUARE_BRACKET, "]"), prevAttr);
                } catch (BadLocationException ex) {
                    Logger.getLogger(RichTextArea.class.getName()).log(Level.SEVERE, "While attempting to parse data into the text field, an invalid location was accessed.", ex);
                }
            }
            prevAttr = new SimpleAttributeSet(curAttr);

            postTagTextStartIndex = m.end();
        }

    }

    /**
     * Encodes the current text, formatting and contained answer fields/images
     * to a single String. This string can then be used to reconstruct the content
     * of the text field through use of the <code>setRichText</code> method. A
     * HTML-like markup is used with special tags for answer fields and images.
     *
     * @return a string containing the markup description of the text field content
     * including any answer fields and images that may be contained by it, and all rich text formatting.
     */
    public String getRichText() {
        String retVal = "";
        int runCount;
        int paragraphCount = this.getDocument().getDefaultRootElement().getElementCount();
        Element curEl;
        AttributeSet curAttr;
        AttributeSet prevAttr = null;


        //traverse all runs in this document, and write each.

        for (int i = 0; i < paragraphCount; i++) {
            //each paragraph has 'runCount' runs
            runCount = this.getDocument().getDefaultRootElement().getElement(i).getElementCount();
            for (int j = 0; j < runCount; j++) {
                curEl = this.getDocument().getDefaultRootElement().getElement(i).getElement(j);
                curAttr = curEl.getAttributes();

                //FIRST WRITE THE ATTRIBUTES OF RUN
                if ((i == 0) && (j == 0)) {
                    //very first run, so don't compare back to the prevAttr object
                    if (StyleConstants.isBold(curAttr)) {
                        retVal += "[" + TAG_BOLD + "]";
                    }
                    if (StyleConstants.isItalic(curAttr)) {
                        retVal += "[" + TAG_ITALIC + "]";
                    }
                    if (StyleConstants.isUnderline(curAttr)) {
                        retVal += "[" + TAG_UNDERLINE + "]";
                    }
                    if (StyleConstants.isSuperscript(curAttr)) {
                        retVal += "[" + TAG_SUPERSCRIPT + "]";
                    }
                    if (StyleConstants.isSubscript(curAttr)) {
                        retVal += "[" + TAG_SUBSCRIPT + "]";
                    }

                    retVal += "[" + TAG_ALIGNMENT + "]" + StyleConstants.getAlignment(curAttr) + "[!" + TAG_ALIGNMENT + "]";

                    retVal += "[" + TAG_FONT_FAMILY + "]" + StyleConstants.getFontFamily(curAttr) + "[!" + TAG_FONT_FAMILY + "]";
                    retVal += "[" + TAG_FONT_SIZE + "]" + StyleConstants.getFontSize(curAttr) + "[!" + TAG_FONT_SIZE + "]";
                    Color fontCol = StyleConstants.getForeground(curAttr);
                    retVal += "[" + TAG_FONT_COLOUR + "]" + fontCol.getRed() + "," + fontCol.getGreen() + "," + fontCol.getBlue() + "[!" + TAG_FONT_COLOUR + "]";

                } else {
                    //the styles for the RichTextArea are set by toggling the current style. As a result, no end tags are
                    //required.
                    if (StyleConstants.isBold(curAttr) != StyleConstants.isBold(prevAttr)) {
                        retVal += "[" + TAG_BOLD + "]";
                    }
                    if (StyleConstants.isItalic(curAttr) != StyleConstants.isItalic(prevAttr)) {
                        retVal += "[" + TAG_ITALIC + "]";
                    }
                    if (StyleConstants.isUnderline(curAttr) != StyleConstants.isUnderline(prevAttr)) {
                        retVal += "[" + TAG_UNDERLINE + "]";
                    }
                    if (StyleConstants.isSuperscript(curAttr) != StyleConstants.isSuperscript(prevAttr)) {
                        retVal += "[" + TAG_SUPERSCRIPT + "]";
                    }
                    if (StyleConstants.isSubscript(curAttr) != StyleConstants.isSubscript(prevAttr)) {
                        retVal += "[" + TAG_SUBSCRIPT + "]";
                    }
                    if (StyleConstants.getAlignment(curAttr) != StyleConstants.getAlignment(prevAttr)) {
                        retVal += "[" + TAG_ALIGNMENT + "]" + StyleConstants.getAlignment(curAttr) + "[!" + TAG_ALIGNMENT + "]";
                    }
                    if (StyleConstants.getFontFamily(curAttr).equals(StyleConstants.getFontFamily(prevAttr)) == false) {
                        retVal += "[" + TAG_FONT_FAMILY + "]" + StyleConstants.getFontFamily(curAttr) + "[!" + TAG_FONT_FAMILY + "]";
                    }
                    if (StyleConstants.getFontSize(curAttr) != StyleConstants.getFontSize(prevAttr)) {
                        retVal += "[" + TAG_FONT_SIZE + "]" + StyleConstants.getFontSize(curAttr) + "[!" + TAG_FONT_SIZE + "]";
                    }
                    if (StyleConstants.getForeground(curAttr).equals(StyleConstants.getForeground(prevAttr)) == false) {
                        Color fontCol = StyleConstants.getForeground(curAttr);
                        retVal += "[" + TAG_FONT_COLOUR + "]" + fontCol.getRed() + "," + fontCol.getGreen() + "," + fontCol.getBlue() + "[!" + TAG_FONT_COLOUR + "]";
                    }
                }

                prevAttr = curAttr;

                //NOW WRITE CONTENT OF RUN

                if (curEl.getName().equals(StyleConstants.ComponentElementName)) //this is a component
                {
                    //this run is a component. May be an answer field, picture or math text component.
                    Component o = (Component) curAttr.getAttribute(StyleConstants.ComponentAttribute);
                    if (o instanceof IAnswerField) {
                        IAnswerField ansField = (IAnswerField) o;
                        String ansFieldText = ansField.writeToXML().replace("[", CHARCODE_OPENING_SQUARE_BRACKET).replace("]", CHARCODE_CLOSING_SQUARE_BRACKET);
                        if (ansFieldText.contains("<name;content>")) {
                            System.out.println("\n---->Answer field has written reserved tags. Removing these.");
                            ansFieldText = ansFieldText.replace("<name;content>", "");
                        }
                        retVal += "[" + TAG_ANSWER_FIELD + "]" + ansField.getClass().getName() + "<name;content>" + ansFieldText + "[!" + TAG_ANSWER_FIELD + "]";
                    } else if (o instanceof EmbeddedImage) {
                        EmbeddedImage embeddedImage = (EmbeddedImage) o;
                        String imgID = embeddedImage.getImageID();
                        imgID = imgID.replace("[", CHARCODE_OPENING_SQUARE_BRACKET).replace("]", CHARCODE_CLOSING_SQUARE_BRACKET);
                        retVal += "[" + TAG_IMAGE + "]" + imgID + "<;>" + embeddedImage.libraryID + "[!" + TAG_IMAGE + "]";
                    } else if (o instanceof EmbeddedMathTeX) {
                        EmbeddedMathTeX embeddedMathTeX = (EmbeddedMathTeX) o;
                        String mathText = embeddedMathTeX.getMathTeX();
                        mathText = mathText.replace("[", CHARCODE_OPENING_SQUARE_BRACKET).replace("]", CHARCODE_CLOSING_SQUARE_BRACKET);
                        retVal += "[" + TAG_MATH + "]" + mathText + "<;>" + embeddedMathTeX.getRenderSize() + "<;>" + embeddedMathTeX.getRenderColour().getRed() + "," + embeddedMathTeX.getRenderColour().getGreen() + "," + embeddedMathTeX.getRenderColour().getBlue() + "[!" + TAG_MATH + "]";
                    }
                } else //we do not have a component, we have styled text
                {
                    try {
                        retVal += getRunElementText(curEl).replace("[", CHARCODE_OPENING_SQUARE_BRACKET).replace("]", CHARCODE_CLOSING_SQUARE_BRACKET).replace("\n", "[" + TAG_NEW_LINE + "]");
                    } catch (BadLocationException ignore) {
                        Logger.getLogger(RichTextArea.class.getName()).log(Level.WARNING, "While trying to get the text associated with a particular run in the getRichText method.", ignore);
                    }
                }

            }
        }
        if (retVal.endsWith("[" + TAG_NEW_LINE + "]")) {
            retVal = retVal.substring(0, retVal.length() - ("[" + TAG_NEW_LINE + "]").length());
        }

        //indicates the end of the document - this is required so that any text between this tag and the previous tag is added.
        retVal += "[" + TAG_DOCUMENT_END + "]";

        return retVal;
    }

    /**
     * Encodes the text that exists between <code>startIndex</code> and <code>endIndex</code>
     * with formatting and contained answer fields/images
     * to a single String. This string can then be used to reconstruct the content
     * of the text field through use of the <code>setRichText</code> method. A
     * HTML-like markup is used with special tags for answer fields and images.
     *
     * @param endIndex the end index of the text to encode.
     * @param startIndex the start index of the text to encode.
     * @return a string containing the markup description of the text field content specified
     * including any answer fields and images that may be contained by it, and all rich text formatting.
     */
    public String getRichText(int startIndex, int endIndex) {
        //return value string built up through traversal of document elements
        String retVal = "";
        //number of runs within a paragraph
        int runCount;
        //number of paragraphs in the entire document
        int paragraphCount = this.getDocument().getDefaultRootElement().getElementCount();
        //paragraph closest to the start index
        int startPara = this.getDocument().getDefaultRootElement().getElementIndex(startIndex);
        //paragraph closest to the end index
        int endPara = this.getDocument().getDefaultRootElement().getElementIndex(endIndex);
        //the index of the individual run that is closest to the start index
        int startRun;
        //the index of the individual run that is closes to the end index
        int endRun;
        //current element through iteration
        Element curEl;
        //attributes of the current element
        AttributeSet curAttr;
        //previous attributes, null if not applicable
        AttributeSet prevAttr = null;


        //traverse all runs in this document, and write each.

        for (int i = startPara; i <= endPara; i++) {
            //each paragraph has 'runCount' runs
            runCount = this.getDocument().getDefaultRootElement().getElement(i).getElementCount();
            startRun = this.getDocument().getDefaultRootElement().getElement(i).getElementIndex(startIndex);
            endRun = this.getDocument().getDefaultRootElement().getElement(i).getElementIndex(endIndex);

            for (int j = startRun; j <= endRun; j++) {
                curEl = this.getDocument().getDefaultRootElement().getElement(i).getElement(j);
                curAttr = curEl.getAttributes();

                //FIRST WRITE THE ATTRIBUTES OF RUN
                if ((i == startPara) && (j == startRun)) {
                    //very first run, so don't compare back to the prevAttr object
                    if (StyleConstants.isBold(curAttr)) {
                        retVal += "[" + TAG_BOLD + "]";
                    }
                    if (StyleConstants.isItalic(curAttr)) {
                        retVal += "[" + TAG_ITALIC + "]";
                    }
                    if (StyleConstants.isUnderline(curAttr)) {
                        retVal += "[" + TAG_UNDERLINE + "]";
                    }
                    if (StyleConstants.isSuperscript(curAttr)) {
                        retVal += "[" + TAG_SUPERSCRIPT + "]";
                    }
                    if (StyleConstants.isSubscript(curAttr)) {
                        retVal += "[" + TAG_SUBSCRIPT + "]";
                    }

                    retVal += "[" + TAG_ALIGNMENT + "]" + StyleConstants.getAlignment(curAttr) + "[!" + TAG_ALIGNMENT + "]";

                    retVal += "[" + TAG_FONT_FAMILY + "]" + StyleConstants.getFontFamily(curAttr) + "[!" + TAG_FONT_FAMILY + "]";
                    retVal += "[" + TAG_FONT_SIZE + "]" + StyleConstants.getFontSize(curAttr) + "[!" + TAG_FONT_SIZE + "]";
                    Color fontCol = StyleConstants.getForeground(curAttr);
                    retVal += "[" + TAG_FONT_COLOUR + "]" + fontCol.getRed() + "," + fontCol.getGreen() + "," + fontCol.getBlue() + "[!" + TAG_FONT_COLOUR + "]";

                } else {
                    //the styles for the RichTextArea are set by toggling the current style. As a result, no end tags are
                    //required.
                    if (StyleConstants.isBold(curAttr) != StyleConstants.isBold(prevAttr)) {
                        retVal += "[" + TAG_BOLD + "]";
                    }
                    if (StyleConstants.isItalic(curAttr) != StyleConstants.isItalic(prevAttr)) {
                        retVal += "[" + TAG_ITALIC + "]";
                    }
                    if (StyleConstants.isUnderline(curAttr) != StyleConstants.isUnderline(prevAttr)) {
                        retVal += "[" + TAG_UNDERLINE + "]";
                    }
                    if (StyleConstants.isSuperscript(curAttr) != StyleConstants.isSuperscript(prevAttr)) {
                        retVal += "[" + TAG_SUPERSCRIPT + "]";
                    }
                    if (StyleConstants.isSubscript(curAttr) != StyleConstants.isSubscript(prevAttr)) {
                        retVal += "[" + TAG_SUBSCRIPT + "]";
                    }
                    if (StyleConstants.getAlignment(curAttr) != StyleConstants.getAlignment(prevAttr)) {
                        retVal += "[" + TAG_ALIGNMENT + "]" + StyleConstants.getAlignment(curAttr) + "[!" + TAG_ALIGNMENT + "]";
                    }
                    if (StyleConstants.getFontFamily(curAttr).equals(StyleConstants.getFontFamily(prevAttr)) == false) {
                        retVal += "[" + TAG_FONT_FAMILY + "]" + StyleConstants.getFontFamily(curAttr) + "[!" + TAG_FONT_FAMILY + "]";
                    }
                    if (StyleConstants.getFontSize(curAttr) != StyleConstants.getFontSize(prevAttr)) {
                        retVal += "[" + TAG_FONT_SIZE + "]" + StyleConstants.getFontSize(curAttr) + "[!" + TAG_FONT_SIZE + "]";
                    }
                    if (StyleConstants.getForeground(curAttr).equals(StyleConstants.getForeground(prevAttr)) == false) {
                        Color fontCol = StyleConstants.getForeground(curAttr);
                        retVal += "[" + TAG_FONT_COLOUR + "]" + fontCol.getRed() + "," + fontCol.getGreen() + "," + fontCol.getBlue() + "[!" + TAG_FONT_COLOUR + "]";
                    }
                }

                prevAttr = curAttr;

                //NOW WRITE CONTENT OF RUN

                if (curEl.getName().equals(StyleConstants.ComponentElementName)) //this is a component
                {
                    //this run is a component. May be an answer field, picture or math text component.
                    Component o = (Component) curAttr.getAttribute(StyleConstants.ComponentAttribute);
                    if (o instanceof IAnswerField) {
                        IAnswerField ansField = (IAnswerField) o;
                        String ansFieldText = ansField.writeToXML().replace("[", CHARCODE_OPENING_SQUARE_BRACKET).replace("]", CHARCODE_CLOSING_SQUARE_BRACKET);
                        if (ansFieldText.contains("<name;content>")) {
                            System.out.println("\n---->Answer field has written reserved tags. Removing these.");
                            ansFieldText = ansFieldText.replace("<name;content>", "");
                        }
                        retVal += "[" + TAG_ANSWER_FIELD + "]" + ansField.getClass().getName() + "<name;content>" + ansFieldText + "[!" + TAG_ANSWER_FIELD + "]";
                    } else if (o instanceof EmbeddedImage) {
                        EmbeddedImage embeddedImage = (EmbeddedImage) o;
                        String imgID = embeddedImage.getImageID();
                        imgID = imgID.replace("[", CHARCODE_OPENING_SQUARE_BRACKET).replace("]", CHARCODE_CLOSING_SQUARE_BRACKET);
                        retVal += "[" + TAG_IMAGE + "]" + imgID + "<;>" + embeddedImage.libraryID + "[!" + TAG_IMAGE + "]";
                    } else if (o instanceof EmbeddedMathTeX) {
                        EmbeddedMathTeX embeddedMathTeX = (EmbeddedMathTeX) o;
                        String mathText = embeddedMathTeX.getMathTeX();
                        mathText = mathText.replace("[", CHARCODE_OPENING_SQUARE_BRACKET).replace("]", CHARCODE_CLOSING_SQUARE_BRACKET);
                        retVal += "[" + TAG_MATH + "]" + mathText + "<;>" + embeddedMathTeX.getRenderSize() + "<;>" + embeddedMathTeX.getRenderColour().getRed() + "," + embeddedMathTeX.getRenderColour().getGreen() + "," + embeddedMathTeX.getRenderColour().getBlue() + "[!" + TAG_MATH + "]";
                    }
                } else //we do not have a component, we have styled text
                {
                    try {
                        //if this is the start run, it is possible that we do not want to return all content of the run
                        if ((j == startRun) && (i == startPara)) {
                            String run = "";
                            try {
                                run = this.getText(startIndex, curEl.getEndOffset() - startIndex);

                            } catch (BadLocationException ex) {
                                throw new BadLocationException("RichTextArea.getRunElementText tried to access an invalid location within the document.\n" + ex.getMessage(), ex.offsetRequested());
                            }
                            retVal += run.replace("[", CHARCODE_OPENING_SQUARE_BRACKET).replace("]", CHARCODE_CLOSING_SQUARE_BRACKET).replace("\n", "[" + TAG_NEW_LINE + "]");
                        } else if ((i == endPara) && (j == endRun)) { //if this is the end run, we might not want to return all content of the run
                            String run = "";
                            try {
                                run = this.getText(curEl.getStartOffset(), endIndex - curEl.getStartOffset());
                            } catch (BadLocationException ex) {
                                throw new BadLocationException("RichTextArea.getRunElementText tried to access an invalid location within the document.\n" + ex.getMessage(), ex.offsetRequested());
                            }
                            retVal += run.replace("[", CHARCODE_OPENING_SQUARE_BRACKET).replace("]", CHARCODE_CLOSING_SQUARE_BRACKET).replace("\n", "[" + TAG_NEW_LINE + "]");
                        } else { //otherwise this is a middle run, and we want all of it. om nom nom.
                            retVal += getRunElementText(curEl).replace("[", CHARCODE_OPENING_SQUARE_BRACKET).replace("]", CHARCODE_CLOSING_SQUARE_BRACKET).replace("\n", "[" + TAG_NEW_LINE + "]");

                        }

                    } catch (BadLocationException ignore) {
                        Logger.getLogger(RichTextArea.class.getName()).log(Level.WARNING, "While trying to get the text associated with a particular run in the getRichText method.", ignore);
                    }
                }

            }
        }
        if (retVal.endsWith("[" + TAG_NEW_LINE + "]")) {
            retVal = retVal.substring(0, retVal.length() - ("[" + TAG_NEW_LINE + "]").length());
        }

        //indicates the end of the document - this is required so that any text between this tag and the previous tag is added.
        if (retVal.endsWith("[" + TAG_DOCUMENT_END + "]") == false) {
            retVal += "[" + TAG_DOCUMENT_END + "]";
        }

        return retVal;
    }

    /**
     * This method parses the raw text string that is passed to it and generates
     * the formatted text. If answer field tags are encountered, then the
     * corresponding classes will be instantiated and added to the text field.
     *
     * If image tags are encountered, the corresponding resources will be added
     * to the text field.
     *
     * This method inserts the resulting rich text at the current caret position
     * of the <code>RichTextArea</code>. If there is a selection, it will be replaced
     * by the parsed rich text.
     *
     * @param rawText the text to parse into the field.
     */
    public void insertRichText(String rawText) {

        //the attribute set generated by the latest parse
        SimpleAttributeSet curAttr = new SimpleAttributeSet();
        //use the previous attributes to insert string
        SimpleAttributeSet prevAttr = new SimpleAttributeSet();

        //start parsing the data in.
        Pattern p = Pattern.compile("\\[.*?\\]");
        Matcher m = p.matcher(rawText);
        int postTagTextStartIndex = 0;
        //gets appended to the string being inserted.
        String strAppend = "";
        //flag for text insertion; text between font family tags, for example, should not be inserted!
        boolean insertIntraTagText = true;
        //number of elements at a given point.
        int elCount = 0;
        //while we find any pattern that matches a tag "[tag_text]".
        while (m.find()) {
            insertIntraTagText = true;
            strAppend = "";
            if (m.group().equals("[" + TAG_BOLD + "]")) {
                StyleConstants.setBold(curAttr, !StyleConstants.isBold(curAttr));
            } else if (m.group().equals("[" + TAG_ITALIC + "]")) {
                StyleConstants.setItalic(curAttr, !StyleConstants.isItalic(curAttr));
            } else if (m.group().equals("[" + TAG_UNDERLINE + "]")) {
                StyleConstants.setUnderline(curAttr, !StyleConstants.isUnderline(curAttr));
            } else if (m.group().equals("[" + TAG_SUPERSCRIPT + "]")) {
                StyleConstants.setSuperscript(curAttr, !StyleConstants.isSuperscript(curAttr));
            } else if (m.group().equals("[" + TAG_SUBSCRIPT + "]")) {
                StyleConstants.setSubscript(curAttr, !StyleConstants.isSubscript(curAttr));
            } else if (m.group().equals("[!" + TAG_ALIGNMENT + "]")) {
                insertIntraTagText = false;
                int alignVal = Integer.valueOf(rawText.substring(postTagTextStartIndex, m.start()));
                switch (alignVal) {
                    case StyleConstants.ALIGN_LEFT:
                        StyleConstants.setAlignment(curAttr, StyleConstants.ALIGN_LEFT);
                        break;
                    case StyleConstants.ALIGN_CENTER:
                        //StyleConstants.setAlignment(curAttr, StyleConstants.ALIGN_CENTER);
                        StyleConstants.ParagraphConstants.setAlignment(curAttr, StyleConstants.ParagraphConstants.ALIGN_CENTER);
                        //curAttr.addAttribute(StyleConstants.ParagraphConstants.Alignment, StyleConstants);
                        break;
                    case StyleConstants.ALIGN_RIGHT:
                        StyleConstants.setAlignment(curAttr, StyleConstants.ALIGN_RIGHT);
                        break;
                    case StyleConstants.ALIGN_JUSTIFIED:
                        StyleConstants.setAlignment(curAttr, StyleConstants.ALIGN_JUSTIFIED);
                        break;
                }
            } else if (m.group().equals("[!" + TAG_FONT_FAMILY + "]")) {
                insertIntraTagText = false;
                curAttr.addAttribute(StyleConstants.Family, rawText.substring(postTagTextStartIndex, m.start()));
            } else if (m.group().equals("[!" + TAG_FONT_SIZE + "]")) {
                insertIntraTagText = false;
                curAttr.addAttribute(StyleConstants.Size, Integer.valueOf(rawText.substring(postTagTextStartIndex, m.start())));
            } else if (m.group().equals("[!" + TAG_FONT_COLOUR + "]")) {
                insertIntraTagText = false;
                String[] colour = rawText.substring(postTagTextStartIndex, m.start()).split(",");
                Color c = new Color(Integer.valueOf(colour[0]), Integer.valueOf(colour[1]), Integer.valueOf(colour[2]));
                curAttr.addAttribute(StyleConstants.Foreground, c);
            } else if (m.group().equals("[" + TAG_NEW_LINE + "]")) {
                strAppend = "\n";
            } else if (m.group().equals("[" + TAG_DOCUMENT_END + "]")) {
                //do nothing
            } else if (m.group().equals("[!" + TAG_ANSWER_FIELD + "]")) {
                insertIntraTagText = false;
                String[] ansFieldData = rawText.substring(postTagTextStartIndex, m.start()).split("<name;content>");

                //if there was no content text, then the split method will return an array of lenght 1, but we need an empty string to pass as the content
                if (ansFieldData.length == 1) {
                    String[] temp = new String[2];
                    temp[0] = ansFieldData[0];
                    temp[1] = "";
                    ansFieldData = temp;
                }
                //instantiate answer field using ansFieldData[0], and set its content to ansFieldData[1].
                try {
                    Class ansField = IOManager.getAnswerFieldClass(ansFieldData[0]);
                    IAnswerField newField = null;
                    if (ansField != null) {
                        newField = (IAnswerField) ansField.newInstance();
                    }
                    if (newField != null) {
                        //tell the new field instance to read in the content text, and then insert it into the document.
                        newField.readInXML(ansFieldData[1].replace("[", CHARCODE_OPENING_SQUARE_BRACKET).replace("]", CHARCODE_CLOSING_SQUARE_BRACKET));
                        this.insertComponent((JComponent) newField);
                    } else {
                        System.out.println("Answer field was null after instantiation attempt - could not insert. Originating from RichTextArea.setRichText()");
                    }
                } catch (InstantiationException ex) {
                    Logger.getLogger(RichTextArea.class.getName()).log(Level.SEVERE, "Could not instantiate the answer field '" + ansFieldData[0] + " in setRichText method.", ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(RichTextArea.class.getName()).log(Level.SEVERE, "For the answer field '" + ansFieldData[0] + " in setRichText method.", ex);
                }
                //load class of name ansFieldData[0] and tell it to generate an instance using data in ansFieldData[1]
                //don't forget to set any CHARCODE_OPENING_SQUARE_BRACKET and CHARCODE_CLOSING_SQUARE_BRACKET
            } else if (m.group().equals("[!" + TAG_IMAGE + "]")) {
                String[] data = rawText.substring(postTagTextStartIndex, m.start()).replace(CHARCODE_OPENING_SQUARE_BRACKET, "[").replace(CHARCODE_CLOSING_SQUARE_BRACKET, "]").split("<;>");
                String imageID = data[0];
                String libID = data[1];
                insertIntraTagText = false;
                try {
                    this.insertComponent(new EmbeddedImage(IOManager.loadImage(libID, imageID), imageID, libID));
                } catch (IOException ex) {
                    Logger.getLogger(RichTextArea.class.getName()).log(Level.SEVERE, "While trying to insert an embedded image after creating it (setRichText)", ex);
                }
            } else if (m.group().equals("[!" + TAG_MATH + "]")) {
                String mathText[] = rawText.substring(postTagTextStartIndex, m.start()).split("<;>");
                String[] colVals = mathText[2].split(",");
                insertIntraTagText = false;
                this.insertComponent(new EmbeddedMathTeX(mathText[0].replace(CHARCODE_OPENING_SQUARE_BRACKET, "[").replace(CHARCODE_CLOSING_SQUARE_BRACKET, "]"), Integer.valueOf(mathText[1]), new Color(Integer.valueOf(colVals[0]), Integer.valueOf(colVals[1]), Integer.valueOf(colVals[2]))));
            }


            if (insertIntraTagText) {
                elCount = this.getDocument().getDefaultRootElement().getElementCount();
                try {
                    this.getStyledDocument().setParagraphAttributes(this.getCaretPosition(), this.getCaretPosition(), prevAttr, true);
                    this.getStyledDocument().insertString(this.getCaretPosition(), (rawText.substring(postTagTextStartIndex, m.start()) + strAppend).replace(CHARCODE_OPENING_SQUARE_BRACKET, "[").replace(CHARCODE_CLOSING_SQUARE_BRACKET, "]"), prevAttr);
                } catch (BadLocationException ex) {
                    Logger.getLogger(RichTextArea.class.getName()).log(Level.SEVERE, "While attempting to parse data into the text field, an invalid location was accessed.", ex);
                }
            }
            prevAttr = new SimpleAttributeSet(curAttr);

            postTagTextStartIndex = m.end();
        }

    }

    /**
     * Gets the text from an element within a document.
     * @param e the element from which to get the text.
     * @return the text from the document corresponding to this element.
     * @throws BadLocationException if the start and end indices given by the element are invalid locations within the document.
     */
    protected String getRunElementText(Element e) throws BadLocationException {
        String run;
        int rangeStart = e.getStartOffset();
        int rangeEnd = e.getEndOffset();

        try {
            run = this.getText(rangeStart, rangeEnd - rangeStart);
        } catch (BadLocationException ex) {
            throw new BadLocationException("RichTextArea.getRunElementText tried to access an invalid location within the document.\n" + ex.getMessage(), ex.offsetRequested());
        }

        return run;
    }

    /**
     * Sets up the relationship between keystrokes and formatting actions.
     */
    public void setUpKeyMap() {
        InputMap inputMap = this.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK), DefaultEditorKit.selectAllAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), DefaultEditorKit.selectWordAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), DefaultEditorKit.copyAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), DefaultEditorKit.pasteAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), DefaultEditorKit.cutAction);


        //Styled
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK), new StyledEditorKit.BoldAction());
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK), new StyledEditorKit.ItalicAction());
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK), new StyledEditorKit.UnderlineAction());
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), new StyledEditorKit.AlignmentAction("Left", StyleConstants.ALIGN_LEFT));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), new StyledEditorKit.AlignmentAction("Right", StyleConstants.ALIGN_RIGHT));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK), new StyledEditorKit.AlignmentAction("Centre", StyleConstants.ALIGN_CENTER));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_J, KeyEvent.CTRL_DOWN_MASK), new StyledEditorKit.AlignmentAction("Justify", StyleConstants.ALIGN_JUSTIFIED));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK), new SuperscriptAction());
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK), new SubscriptAction());
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), new UndoAction());
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), new RedoAction());
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), new RedoAction());

    }

    /**
     * Returns the <code>JScrollPane</code> which holds this <code>RichTextArea</code> instance.
     * This allows external classes to add the scroll pane, rather than the text area itself.
     * @return the <code>JScrollPane</code> which holds this <code>RichTextArea</code> instance.
     */
    public JScrollPane getScroller() {
        return scrollPane;
    }

    /**
     * Gets the toolbar associated with this <code>RichTextToolbar</code>.
     * @return the toolbar associated with this <code>RichTextToolbar</code>.
     */
    public RichTextToolbar getToolbar() {
        return toolbar;

    }

    /**
     * Set whether or not the toolbar is visible.
     * @param isVisible whether or not the toolbar is visible.
     */
    public void setToolbarVisible(boolean isVisible) {
        if (isVisible) {
            scrollPane.setColumnHeaderView(toolbar);
        } else {
            scrollPane.setColumnHeaderView(null);
        }

        toolbarIsVisible = isVisible;
    }

    /**
     * Gets whether or not the toolbar is currently visible.
     * @return whether or not the toolbar is currently visible.
     */
    public boolean isToolbarVisible() {
        return toolbarIsVisible;


    }

    /**
     * Get the current key associated with telling the menu that the next character to
     * be typed is the new base character. Supports modifiers.
     *
     * @return the current menu activate key.
     */
    public KeyStroke getSymbolMenuActivateKey() {
        return symbolMenuActivateKey;


    }

    /**
     * Get the SymbolMenu instance associated with this component. This allows you
     * to set the colours of the component.
     *
     * @return the SymbolMenu instance associated with this component.
     */
    public SymbolMenu getSymbolMenu() {
        return symbolMenu;


    }

    /**
     * Sets the menu activate key. This is the key that tells the SymbolMenu instance that
     * the next key to be pressed is the new base character. Supports modifiers.
     *
     * @param symbolMenuActivateKey the new menu activate key.
     */
    public void setSymbolMenuActivateKey(KeyStroke symbolMenuActivateKey) {
        this.symbolMenuActivateKey = symbolMenuActivateKey;
    }

    /**
     * As the user moves the caret or specifies new formatting, the toolbar must indicate
     * the formatting under the current caret position. This method set this information
     * to the toolbar.
     * @param position the current position of the caret.
     */
    public void setToolbarToggles(int position) {
        if ((position == this.getText().length()) && (!this.getText().isEmpty())) {
            position = position - 1;


        }
        AttributeSet attrs = this.getStyledDocument().getCharacterElement(position).getAttributes();


        this.getToolbar().setButtonToggles(StyleConstants.isBold(attrs),
                StyleConstants.isItalic(attrs), StyleConstants.isUnderline(attrs),
                StyleConstants.isSuperscript(attrs), StyleConstants.isSubscript(attrs),
                StyleConstants.getAlignment(attrs),
                new Font(StyleConstants.getFontFamily(attrs), Font.PLAIN, StyleConstants.getFontSize(attrs)));
    }

    /**
     * Listens for and responds to toolbar events.
     */
    private class ToolbarListener implements RichTextToolbarListener {

        public void buttonPressed(RichTextToolbarEvent e) {
            if (e.getEventID() == RichTextToolbarEvent.BOLD_BUTTON) {
                new StyledEditorKit.BoldAction().actionPerformed(null);
            } else if (e.getEventID() == RichTextToolbarEvent.ITALIC_BUTTON) {
                new StyledEditorKit.ItalicAction().actionPerformed(null);
            } else if (e.getEventID() == RichTextToolbarEvent.UNDERNLINE_BUTTON) {
                new StyledEditorKit.UnderlineAction().actionPerformed(null);
            } else if (e.getEventID() == RichTextToolbarEvent.SUPERSCRIPT_BUTTON) {
                new SuperscriptAction().actionPerformed(null);
            } else if (e.getEventID() == RichTextToolbarEvent.SUBSCRIPT_BUTTON) {
                new SubscriptAction().actionPerformed(null);
            } else if (e.getEventID() == RichTextToolbarEvent.JUSTIFY_LEFT) {
                new StyledEditorKit.AlignmentAction("Left Align", StyleConstants.ALIGN_LEFT).actionPerformed(null);
            } else if (e.getEventID() == RichTextToolbarEvent.JUSTIFY_CENTRE) {
                new StyledEditorKit.AlignmentAction("Centre", StyleConstants.ALIGN_CENTER).actionPerformed(null);
            } else if (e.getEventID() == RichTextToolbarEvent.JUSTIFY_RIGHT) {
                new StyledEditorKit.AlignmentAction("Right Align", StyleConstants.ALIGN_RIGHT).actionPerformed(null);
            } else if (e.getEventID() == RichTextToolbarEvent.INSERT_PICTURE) {
            }
            RichTextArea.this.setToolbarToggles(RichTextArea.this.getCaretPosition());
            RichTextArea.this.requestFocus();
        }

        public void fontChanged(RichTextToolbarEvent e) {
            if (e.getEventID() == RichTextToolbarEvent.FONT_FAMILY_CHANGED) {
                new StyledEditorKit.FontFamilyAction("Font", e.getSource().getSelectedFontFamily()).actionPerformed(null);
            } else if (e.getEventID() == RichTextToolbarEvent.FONT_SIZE_CHANGED) {
                new StyledEditorKit.FontSizeAction("Font size", e.getSource().getSelectedFontSize()).actionPerformed(new ActionEvent(RichTextArea.this, 0, "" + e.getSource().getSelectedFontSize()));
            } else if (e.getEventID() == RichTextToolbarEvent.FONT_COLOUR_CHANGED) {
                new StyledEditorKit.ForegroundAction("Font colour", e.getSource().getSelectedColour()).actionPerformed(new ActionEvent(RichTextArea.this, 0, ""));
            }
            RichTextArea.this.requestFocus();
        }
    }

    /**
     * Listens for new colour selection events on the ColourChooserPane owned by the toolbar.
     */
    private class ColourListener implements ColourChooserPaneListener {

        public void colourSelected(ColourChooserPaneEvent e) {
            if (e.getChangeSourceID() != ColourChooserPaneEvent.IMAGE_DRAG) {
                RichTextArea.this.getToolbar().hideColourChooser();
                RichTextToolbarListener[] listeners = RichTextArea.this.getToolbar().getRichTextToolbarListeners();
                for (int i = 0; i < listeners.length; i++) {
                    listeners[i].fontChanged(new RichTextToolbarEvent(RichTextArea.this.getToolbar(), RichTextToolbarEvent.FONT_COLOUR_CHANGED));
                }
            }
        }
    }

    /**
     * Subscript action.
     */
    private class SubscriptAction extends StyledEditorKit.StyledTextAction {

        public SubscriptAction() {
            super(StyleConstants.Subscript.toString());
        }

        public void actionPerformed(ActionEvent ae) {
            JTextPane editor = (JTextPane) getEditor(ae);
            if (editor != null) {
                StyledEditorKit kit = getStyledEditorKit(editor);
                MutableAttributeSet attr = kit.getInputAttributes();
                boolean subscript = (StyleConstants.isSubscript(attr)) ? false : true;
                SimpleAttributeSet sas = new SimpleAttributeSet();
                StyleConstants.setSubscript(sas, subscript);
                StyleConstants.setSuperscript(sas, false);
                setCharacterAttributes(editor, sas, false);
            }
        }
    }

    /**
     * Superscript action.
     */
    private class SuperscriptAction extends StyledEditorKit.StyledTextAction {

        public SuperscriptAction() {
            super(StyleConstants.Superscript.toString());
        }

        public void actionPerformed(ActionEvent ae) {
            JTextPane editor = (JTextPane) getEditor(ae);
            if (editor != null) {
                StyledEditorKit kit = getStyledEditorKit(editor);
                MutableAttributeSet attr = kit.getInputAttributes();
                boolean superscript = (StyleConstants.isSuperscript(attr)) ? false : true;
                SimpleAttributeSet sas = new SimpleAttributeSet();
                StyleConstants.setSuperscript(sas, superscript);
                StyleConstants.setSubscript(sas, false);
                setCharacterAttributes(editor, sas, false);
            }
        }
    }

    /**
     * Undo action.
     */
    private class UndoAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            try {
                undo.undo();
            } catch (CannotUndoException ex) {
            }
        }
    }

    /**
     * Redo action.
     */
    private class RedoAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            try {
                undo.redo();
            } catch (CannotRedoException ex) {
            }
        }
    }

    /**
     * Listens for changes in position of the character, and then calls the
     * <code>setToolbarToggles</code> method to update the 'current formatting'
     * decoaration of the toolbar.
     */
    private class CaretListen implements CaretListener {

        public void caretUpdate(CaretEvent e) {
            int position = e.getDot();
            RichTextArea.this.setToolbarToggles(position);
        }
    }

    /**
     * KeyListener for the SimpleTextField.
     */
    private class TextKeyListener implements KeyListener {

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            if ((e.getKeyCode() == symbolMenuActivateKey.getKeyCode()) && ((e.getModifiersEx() & symbolMenuActivateKey.getModifiers()) == InputEvent.CTRL_DOWN_MASK)) {
                menuActivated = true;
            } else if (menuActivated) {
                //shift will deactivate the symbol menu otherwise, meaning that capital letters can't be accessed
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    return;
                }
                menuActivated = false;
                if (symbolMenu.charHasSpecialChars(e.getKeyChar())) //if the baseCharacter is valid...
                {
                    //then set the base character,
                    symbolMenu.setBaseCharacter(e.getKeyChar());
                    //and position the symbol menu logically at the caret
                    Point p = RichTextArea.this.getCaret().getMagicCaretPosition();
                    if (p == null) {
                        p = new Point(2, RichTextArea.this.getHeight() / 2 - 8);
                    }
                    if (symbolMenu.getStringWidth() > (RichTextArea.this.getWidth() - p.x)) {
                        symbolMenu.setLocation(RichTextArea.this.getWidth() - (symbolMenu.getStringWidth() + 5), p.y - 1);
                    } else {
                        symbolMenu.setLocation(p.x + 1, p.y - 1);
                    }

                    symbolMenu.setVisible(true);
                    symbolMenu.requestFocus();
                    symbolMenu.repaint();
                }
            }
        }

        public void keyReleased(KeyEvent e) {
        }
    }

    /**
     * KeyListener for the SymbolMenu.
     */
    private class MenuKeyListener implements KeyListener {

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == 35) //end
            {
                symbolMenu.moveSelectionEnd();
            } else if (e.getKeyCode() == 36) //home
            {
                symbolMenu.moveSelectionStart();
            } else if (e.getKeyCode() == 37) //left
            {
                symbolMenu.moveSelectionLeft();
            } else if (e.getKeyCode() == 39) //right
            {
                symbolMenu.moveSelectionRight();
            } else if (e.getKeyCode() == 10) { //enter key
                RichTextArea.this.select(RichTextArea.this.getCaretPosition() - 1, RichTextArea.this.getCaretPosition());
                RichTextArea.this.replaceSelection("" + symbolMenu.getSelectedCharacter());
                symbolMenu.setVisible(false);
                RichTextArea.this.requestFocus();
            } else if (e.getKeyCode() == 27) { //escape key
                symbolMenu.setVisible(false);
                RichTextArea.this.requestFocus();
            } else if (e.getKeyCode() == 8) {
                symbolMenu.moveSelectionLeft();
            } else {
                symbolMenu.moveSelectionRight();
            }
            symbolMenu.repaint();
        }

        public void keyReleased(KeyEvent e) {
        }

        public void keyTyped(KeyEvent e) {
        }
    }

    private class TextUndoableEditListener implements UndoableEditListener {

        public void undoableEditHappened(UndoableEditEvent e) {
            undo.addEdit(e.getEdit());
        }
    }
}

