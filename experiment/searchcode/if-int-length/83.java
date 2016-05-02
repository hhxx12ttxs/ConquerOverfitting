/*
 * TableQuestion.java
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

package org.ingatan.data;

import org.ingatan.component.quiztime.TableQuestionUnit;
import java.util.ArrayList;
import java.util.Arrays;
import org.ingatan.ThemeConstants;

/**
 * This is an implementation of the IQuestion interface.
 * This class encapsulates the data associated with the table question field.
 *
 * The table question type is ideal for vocabulary training and other flash-card
 * style learning exercises (trig identities, anyone?).
 *
 * The question is made up of a table, with some options regarding whether or not
 * the question and answer column are reversible and whether the questions should be
 * asked as auto-generated multiple choice or written, etc.
 *
 * @author Thomas Everingham
 * @version 1.0
 */
public class TableQuestion implements IQuestion {

    /**
     * In the written type of question, the user will be provided with a text
     * field in which to type an answer.
     */
    public static final int WRITTEN = 0;
    /**
     * In the multiple choice type of question, the user will be asked to select
     * the correct answer from a randomly generated selection of 5 other answers
     * taken from the opposite column of data.
     */
    public static final int MULTI_CHOICE = 1;
    /**
     * Both written and multiple choice type questions will be used at random.
     */
    public static final int RANDOM = 2;
    /**
     * The user can write a template question for the set of data, using placeholders
     * for the table entries. For example 'translate [col1] from english to swedish'.
     */
    protected String questionTemplateFwd = "";
    /**
     * The user can write a template question for the set of data, using placeholders
     * for the table entries. For example 'translate [col1] from english to swedish'.
     * This field may need to be different to questionTemplateFwd when asking the question
     * in reverse.
     */
    protected String questionTemplateBwd = "";
    /**
     * Column 1's data.
     */
    protected String[] col1Data;
    /**
     * Column 2's data.
     */
    protected String[] col2Data;
    /**
     * Whether or not the questions can be asked in reverse, i.e. column 2 as the
     * question.
     */
    protected boolean askInReverse;
    /**
     * One of the fields of this class. Can be WRITTEN, MULTIPLE_CHOICE, or RANDOM.
     * Indicates how the questions in the table will be asked.
     */
    protected int quizMethod;
    /**
     * The sum of marks that have been awarded over all the times each question has been asked.
     * The overall correctness of this question can be calculated as marksAwarded/marksAvailable.
     */
    protected int[] marksAwarded;
    /**
     * The sum of marks that have been available over all the times each question has been asked.
     * The overall correctness of this question can be calculated as marksAwarded/marksAvailable.
     * The reason this field is saved is that the user may wish to change the number of marks allocated
     * for a particular answer field, without affecting the correctness value acheived thus far.
     */
    protected int[] marksAvailable;
    /**
     * The number of marks to award for each correct answer.
     */
    protected int marksPerCorrectAnswer;
    /**
     * The number of times a question has been asked based on this table of questions.
     */
    protected int[] timesAsked;

    /**
     * The libraryID of the library that this question belongs to.
     */
    protected String parentLibrary;
    /**
     * The font to use for this table question.
     */
    protected String fontFamilyName=ThemeConstants.niceFont.getFamily();
    /**
     * The font size to display the questions at during quiz time.
     */
    protected int fontSize = 12;

    /**
     * Creats a new TableQuestion object.
     * @param parent the parent library of this question.
     * @param questionTemplateFwd the question template when the question is asked forwards (col1 as question, col2 as answer)
     * @param questionTemplateBwd the question template when the question is asked backwards (col1 as answer, col2 as question)
     * @param col1Data the data contained by column 1
     * @param col2Data the data contained by column 2
     * @param askInReverse whether or not it is okay to ask these questions in reverse (i.e. col1 as the answer, col2 as the question)
     * @param quizMethod the answer field types to use for this table question - generate multiple choice, written answers, or use both randomly.
     * @param marksAwarded how many marks have been awarded for each entry
     * @param marksAvailable the marks available for each entry
     * @param marksPerCorrectAnswer the number of marks to award for each correct answer.
     * @param timesAsked the number of times each entry has been asked.
     */
    public TableQuestion(String parent, String questionTemplateFwd, String questionTemplateBwd, String[] col1Data, String[] col2Data,
            boolean askInReverse, int quizMethod, String fontName, int fontSize, int[] marksAwarded, int[] marksAvailable,
            int marksPerCorrectAnswer, int[] timesAsked) {

        if (col1Data.length != col2Data.length)
            throw new IllegalArgumentException("column 1 and column 2 data arrays must be of the same length!");
        this.questionTemplateFwd = questionTemplateFwd;
        this.questionTemplateBwd = questionTemplateBwd;
        this.col1Data = col1Data;
        this.col2Data = col2Data;
        this.askInReverse = askInReverse;
        this.quizMethod = quizMethod;
        this.marksAwarded = marksAwarded;
        this.marksAvailable = marksAvailable;
        this.marksPerCorrectAnswer = marksPerCorrectAnswer;
        this.timesAsked = timesAsked;
        this.parentLibrary = parent;
        this.fontFamilyName = fontName;
        this.fontSize = fontSize;
    }

    /**
     * Gets whether or not ask in reverse is set to being allowed. Ask in reverse
     * allows the second column of data to be framed as the question, as well as
     * the other way around. This is interchanged randomly.
     * @return whether or not ask in reverse is allowed.
     */
    public boolean isAskInReverse() {
        return askInReverse;
    }

    /**
     * Set whether or not ask in reverse is allowed. Ask in reverse
     * allows the second column of data to be framed as the question, as well as
     * the other way around. This is interchanged randomly.
     * @param askInReverse true if ask in reverse should be allowed.
     */
    public void setAskInReverse(boolean askInReverse) {
        this.askInReverse = askInReverse;
    }

    /**
     * Gets the data associated with column 1 ('question' column).
     * @return the data associated with column 1.
     */
    public String[] getCol1Data() {
        String[] retVal = new String[col1Data.length];
        System.arraycopy(col1Data, 0, retVal, 0, col1Data.length);
        return retVal;
    }

    /**
     * Get the a copy of the column (Questions) 1 data in the form of an array list.
     * @return ArrayList containing the column 1 data ('Questions' column).
     */
    public ArrayList<String> getCol1DataArrayList() {
        ArrayList<String> retVal = new ArrayList<String>();
        for (int i = 0; i < col1Data.length; i++) {
            retVal.add(col1Data[i]);
        }
        return retVal;
    }

    /**
     * Sets the data associated with column 1.
     * @param col1Data the new data associated with column 1.
     */
    public void setCol1Data(String[] col1Data) {
        this.col1Data = col1Data;
    }

    /**
     * Gets the data associated with column 2 ('Answers' column).
     * @return the data associated with column 2.
     */
    public String[] getCol2Data() {
        String[] retVal = new String[col2Data.length];
        System.arraycopy(col2Data, 0, retVal, 0, col2Data.length);
        return retVal;
    }

    /**
     * Get the a copy of the column (Answers) 2 data in the form of an array list.
     * @return ArrayList containing the column 2 data ('Answers' column).
     */
    public ArrayList<String> getCol2DataArrayList() {
        ArrayList<String> retVal = new ArrayList<String>();
        for (int i = 0; i < col2Data.length; i++) {
            retVal.add(col2Data[i]);
        }
        return retVal;
    }

    /**
     * Sets the data associated with column 2.
     * @param col2Data the new data associated with column 2.
     */
    public void setCol2Data(String[] col2Data) {
        this.col2Data = col2Data;
    }

    /**
     * Gets the array containing the number of marks which have been at some point
     * obtainable during quiz time. Each array entry corresponds to an entry in the
     * data table (i.e. one question).
     * @return the marks available array.
     */
    public int[] getMarksAvailable() {
        return marksAvailable;
    }

    /**
     * Get the a copy of the marks available data in the form of an array list.
     * @return ArrayList containing the marks available data.
     */
    public ArrayList getMarksAvailableArrayList() {
        ArrayList retVal = new ArrayList();
        for (int i = 0; i < marksAvailable.length; i++) {
            retVal.add(marksAvailable[i]);
        }
        return retVal;
    }

    /**
     * Sets the marks available for all questions.
     * @param marksAvailable the array of marks available.
     */
    public void setMarksAvailable(int[] marksAvailable) {
        this.marksAvailable = marksAvailable;
    }
    
    /**
     * Set the marks available for only the question at <code>index</code> to
     * the value <code>marksAvailable</code>.
     * @param index the question to set marks available for.
     * @param marksAvailable the number of marks.
     */
    public void setMarksAvailable(int index, int marksAvailable)
    {
        this.marksAvailable[index] = marksAvailable;
    }

    /**
     * Gets the array of marks that have been awarded for each question within this
     * table. Each array entry corresponds to a table entry.
     * @return the marks awarded array.
     */
    public int[] getMarksAwarded() {
        return marksAwarded;
    }

    /**
     * Get the a copy of the marks awarded data in the form of an array list.
     * @return ArrayList containing the marks awarded data.
     */
    public ArrayList getMarksAwardedArrayList() {
        ArrayList retVal = new ArrayList();
        for (int i = 0; i < marksAwarded.length; i++) {
            retVal.add(marksAwarded[i]);
        }
        return retVal;
    }

    /**
     * Sets the marks that have been awarded for all questions in this table.
     * @param marksAwarded the array of marks awarded values, entries corresponding
     * to questions in the column data arrays.
     */
    public void setMarksAwarded(int[] marksAwarded) {
        this.marksAwarded = marksAwarded;
    }

    /**
     * Sets the marks that have been awarded for question <code>index</code> in the
     * table.
     * @param index the question to set the marks awarded value for.
     * @param marksAwarded the marks that have been awarded for answers of this question.
     */
    public void setMarksAwarded(int index, int marksAwarded)
    {
        this.marksAwarded[index] = marksAwarded;
    }

    /**
     * Gets the number of marks to award for each correct answer.
     * @return the number of marks to award for each correct answer.
     */
    public int getMarksPerCorrectAnswer() {
        return marksPerCorrectAnswer;
    }

    /**
     * Sets the number of marks to award for each correct answer.
     * @param marksPerCorrectAnswer the number of marks to award for each correct answer.
     */
    public void setMarksPerCorrectAnswer(int marksPerCorrectAnswer) {
        this.marksPerCorrectAnswer = marksPerCorrectAnswer;
    }

    /**
     * Gets the question template associated with this table question. The question template
     * uses placeholders for the question and answer taken from the column data arrays. An example is
     * Translate [placeholder] from English to Swedish.
     * @return the question template associated with this table question.
     */
    public String getQuestionTemplateFwd() {
        return questionTemplateFwd;
    }

    /**
     * In the library editor, the column 1 and column 2 data arrays are updated with the gui
     * content just before they are writen to the library file. This method updates the marksAwarded,
     * marksAvailable, and timesAsked arrays so that they are the same size as column 1 and column 2. It also ensures
     * that column 1 and column 2 are the same size so as to avoid possible errors, and that any empty entries into
     * either column 1 or 2 are recorded appropriately.
     */
    public void validateArrays() {
        if (col1Data == null)
            col1Data = new String[0];
        if (col2Data == null)
            col2Data = new String[0];

        //make the column data array length the size of the shorter array
        if (col1Data.length < col2Data.length)
            col2Data = Arrays.copyOf(col2Data, col1Data.length);
        if (col2Data.length < col1Data.length)
            col1Data = Arrays.copyOf(col1Data, col2Data.length);

        //ensure that any empty entries are recorded properly by setting them to " ".
        for (int i = 0; i < col1Data.length && i < col2Data.length; i++) {
            if ((col1Data[i] == null) || (col1Data[i].isEmpty())) col1Data[i] = " ";
            if ((col2Data[i] == null) || (col2Data[i].isEmpty())) col2Data[i] = " ";
        }

        //update marks arrays
        marksAvailable = Arrays.copyOf(marksAvailable, col1Data.length);
        marksAwarded = Arrays.copyOf(marksAwarded, col1Data.length);
        timesAsked = Arrays.copyOf(timesAsked, col1Data.length);
    }

    /**
     * Sets the question template associated with this table question. The question template
     * uses placeholders for the question and answer taken from the column data arrays. An example is
     * Translate [placeholder] from English to Swedish.
     * @param questionTemplate the question template associated with this table question.
     */
    public void setQuestionTemplateFwd(String questionTemplate) {
        this.questionTemplateFwd = questionTemplate;
    }

    /**
     * Gets the question template associated with this table question. The question template
     * uses placeholders for the question and answer taken from the column data arrays. An example is
     * Translate [placeholder] from English to Swedish. This is the backward template used when the
     * ask in reverse setting is true.
     * @return the question template associated with this table question.
     */
    public String getQuestionTemplateBwd() {
        return questionTemplateBwd;
    }

    /**
     * Sets the question template associated with this table question. The question template
     * uses placeholders for the question and answer taken from the column data arrays. An example is
     * Translate [placeholder] from English to Swedish. This is the backward template, used when the
     * ask in reverse setting is true.
     * @param questionTemplate the question template associated with this table question.
     */
    public void setQuestionTemplateBwd(String questionTemplate) {
        this.questionTemplateBwd = questionTemplate;
    }

    /**
     * Gets the quiz method that should be used at quiz time for this table. The
     * currently supported methods are 'written', where the user types the answer in
     * a text field, 'multiple choice' where the user is presented with several possible
     * responses automatically taken from the table of data, or 'random' where both of the
     * aforementioned modes are used interchangably.
     * @return the quiz method that should be used at quiz time for this table. See the
     * fields of this class; WRITTEN, MULTI_CHOICE, and RANDOM.
     */
    public int getQuizMethod() {
        return quizMethod;
    }

    /**
     * Sets the quiz method that should be used at quiz time for this table. The
     * currently supported methods are 'written', where the user types the answer in
     * a text field, 'multiple choice' where the user is presented with several possible
     * responses automatically taken from the table of data, or 'random' where both of the
     * aforementioned modes are used interchangably.
     * @param quizMethod one of the fields of this class; WRITTEN, MULTI_CHOICE, or RANDOM.
     */
    public void setQuizMethod(int quizMethod) {
        this.quizMethod = quizMethod;
    }

    /**
     * Gets the array of 'times asked' values. This array indicates how many times each
     * question in the table has been asked.
     * @return the array of 'times asked' values.
     */
    public int[] getTimesAsked() {
        return timesAsked;
    }

    /**
     * Get the a copy of the times asked data in the form of an array list.
     * @return ArrayList containing the times asked data.
     */
    public ArrayList getTimesAskedArrayList() {
        ArrayList retVal = new ArrayList();
        for (int i = 0; i < timesAsked.length; i++) {
            retVal.add(timesAsked[i]);
        }
        return retVal;
    }

    /**
     * Sets the array of 'times asked' values. This array indicates how many times each
     * question in the table has been asked.
     * @param timesAsked the array of 'times asked' values.
     */
    public void setTimesAsked(int[] timesAsked) {
        this.timesAsked = timesAsked;
    }

    /**
     * Sets a member of the 'times asked' array. This array indicates how many times each
     * question in the table has been asked.
     * @param index the table entry to set the times asked value for.
     * @param timesAsked the number of times the question at <code>index</code> has been asked.
     */
    public void setTimesAsked(int index, int timesAsked)
    {
        this.timesAsked[index] = timesAsked;
    }

    /**
     * Gets the question type.
     * @return value corresponding to a field in that <code>IQuestion</code> interface.
     */
    public int getQuestionType() {
        return IQuestion.TABLE_QUESTION;
    }

    /**
     * Gets the libraryID of the library that this question belongs to.
     * @return the libraryID of the library that this question belongs to.
     */
    public String getParentLibrary() {
        return parentLibrary;
    }

    /**
     * Gets the font family name used by this table question.
     * @return the font used by this table question.
     */
    public String getFontFamilyName() {
        return fontFamilyName;
    }

    /**
     * Sets the font family name used by this table question.
     * @param fontFamilyName the new font name that should be used by this question.
     */
    public void setFontFamilyName(String fontFamilyName) {
        this.fontFamilyName = fontFamilyName;
    }

    /**
     * Gets the display size for this table question at quiz-time.
     * @return the display size for this table question at quiz-time.
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Sets the display size for this table question at quiz-time.
     * @param fontSize the new display size for this table question at quiz-time.
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * Text description of this question type.
     * @return text description of this question type.
     */
    public String getQuestionTypeName() {
        return "TableQuestion";
    }


    public TableQuestionUnit[] getQuestionUnits() {
        TableQuestionUnit[] retVal = new TableQuestionUnit[col1Data.length];
        for (int i = 0; i < retVal.length; i++) {
            retVal[i] = new TableQuestionUnit(col1Data[i], col2Data[i], this, i);
        }
        return retVal;
    }

    /**
     * Gets a single string representation of the content of this question
     * which can be used by external search methods.
     * @return a signle string representation of the content of this question.
     */
    public String getSearchableRepresentation() {
        String retVal = "";
        for (int i = 0; i < col1Data.length && i < col2Data.length; i++)
        {
            retVal += col1Data[i] + " " + col2Data[i] + " ";
        }
        retVal += " ]![ " + questionTemplateFwd + " ]![ " + questionTemplateBwd;
        return retVal;
    }

    /**
     * Set the libraryID of the library that this question belongs to.
     * @param parent the libraryID of the library that this question belongs to.
     */
    public void setParentLibrary(String parent) {
        this.parentLibrary = parent;
    }
}

