/*
 * QuestionList.java
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
package org.ingatan.component.librarymanager;

import org.ingatan.ThemeConstants;
import org.ingatan.component.text.RichTextArea;
import org.ingatan.data.FlexiQuestion;
import org.ingatan.data.IQuestion;
import org.ingatan.data.Library;
import org.ingatan.data.TableQuestion;
import org.ingatan.io.IOManager;
import org.ingatan.io.ParserWriter;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpinnerListModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.ingatan.component.answerfield.IAnswerField;
import org.ingatan.component.text.EmbeddedImage;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * The question list is a JScrollPane based container which holds all of the
 * QuestionContainers for questions that exist in a library. It has a few methods
 * relevant to the state of the questions within the list, such as select/deselect all,
 * minimise/maximise all, etc.
 *
 * NOTE: SHOULD HAVE USED ARRAY LIST OF CONTAINERS HERE, NOT ARRAY - will be re-implemented
 * @author Thomas Everingham
 * @version 1.0
 */
public class QuestionList extends JPanel {

    /**
     * Content pane of the scroller. The questions are added to this.
     */
    protected JPanel scrollerContent = new JPanel();
    /**
     * Scroll pane that holds the list of questions.
     */
    protected JScrollPane scroller = new JScrollPane(scrollerContent);
    /**
     * The library whose questions are contained within this question list.
     */
    protected Library library;
    /**
     * References to all question containers that exist within this list.
     */
    protected ArrayList<AbstractQuestionContainer> questionContainers = new ArrayList<AbstractQuestionContainer>();
    /**
     * Maximum width of question containers.
     */
    protected int maximumQuestionContainerWidth = 1000;
    /**
     * All <code>ActionListener</code>s that have been added and not removed from this <code>QuestionList</code>.
     */
    protected ActionListener[] actionListeners = new ActionListener[0];

    /**
     * Creates a new question list with no questions added.
     */
    public QuestionList() {
        super();
        this.setOpaque(false);

        this.add(scroller);
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        scroller.setMinimumSize(new Dimension(300, 500));
        scroller.setMaximumSize(new Dimension(500, 1500));
        scroller.setOpaque(false);
        scrollerContent.setOpaque(false);
        scroller.setViewportBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        scroller.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, ThemeConstants.borderUnselected));
        scrollerContent.setLayout(new BoxLayout(scrollerContent, BoxLayout.Y_AXIS));
        scrollerContent.setMinimumSize(new Dimension(300, 100));

        this.setOpaque(false);
    }

    public int getMaximumQuestionContainerWidth() {
        return maximumQuestionContainerWidth;
    }

    public void setMaximumQuestionContainerWidth(int maximumWidth) {
        this.maximumQuestionContainerWidth = maximumWidth;
    }

    /**
     * Gets the question data from the container that is an ancestor of the
     * currently focussed component.
     * @param c The component that is currently focussed.
     * @return the question data from the container that is the ancestor fo teh currently
     * focussed component, or <code>null</code> if the container cannot be found.
     */
    public IQuestion getQuestionFromFocussedComponent(Component c) {
        for (int i = 0; i < questionContainers.size(); i++) {
            if (questionContainers.get(i).isAncestorOf(c)) {
                return questionContainers.get(i).getQuestion();
            }
        }
        return null;
    }

    /**
     * Creates a new QuestionList from the specified library.
     * @param lib the library of questions with which the list should be populated.
     */
    public QuestionList(Library lib) {
        this();
        setLibrary(lib);
    }

    public void setLibrary(Library lib) {
        this.library = lib;
        scrollerContent.removeAll();
        questionContainers = new ArrayList<AbstractQuestionContainer>();
        IQuestion[] questions = lib.getQuestions();
        for (int i = 0; i < questions.length; i++) {
            addQuestion(questions[i]);
        }

        minimiseAll();
    }

    /**
     * Set all of the question containers in this list to the selected state.
     */
    public void selectAll() {
        for (int i = 0; i < questionContainers.size(); i++) {
            questionContainers.get(i).setSelected(true);
        }
    }

    /**
     * Set all of the question containers in this list to the deselected state.
     */
    public void deselectAll() {
        for (int i = 0; i < questionContainers.size(); i++) {
            questionContainers.get(i).setSelected(false);
        }
    }

    /**
     * Minimise all question containers.
     */
    public void minimiseAll() {
        for (int i = 0; i < questionContainers.size(); i++) {
            questionContainers.get(i).minimise();
        }
    }

    /**
     * Maximise all question containers.
     */
    public void maximiseAll() {
        for (int i = 0; i < questionContainers.size(); i++) {
            questionContainers.get(i).maximise();
        }
    }

    /**
     * Searches all questions contained by this question container for the specified
     * terms. The terms are split by whitespace.
     * @param searchFor search terms, each separated by whitespace
     */
    public void searchFor(String searchFor) {
        //we will maximise the question containers that contain the search terms.
        //this is a nice easy way of showing results :-P It is also less disorientating
        //for the user to not be taken to some other search screen
        this.minimiseAll();

        updateQuestionsWithContent();

        String[] terms = searchFor.toLowerCase().split("\\s+");

        for (int i = 0; i < questionContainers.size(); i++) {
            for (int j = 0; j < terms.length; j++) {
                if (questionContainers.get(i).getQuestion().getSearchableRepresentation().toLowerCase().contains(terms[j])) {
                    questionContainers.get(i).maximise();
                    //if we found one term, it doesn't matter if there are other terms present.. just maximise and break
                    break;
                }
            }
        }
    }

    /**
     * Rebuilds the question list from the questionContainers array.
     * Note: be sure that the questions have been updated using the
     * updateQuestionsWithContent method first!
     */
    public void rebuildList() {
        scrollerContent.removeAll();
        Iterator<AbstractQuestionContainer> iterate = questionContainers.iterator();
        while (iterate.hasNext()) {
            scrollerContent.add(iterate.next());
            scrollerContent.add(Box.createVerticalStrut(5));
        }

        scrollerContent.validate();
        this.repaint();
    }

    /**
     * Groups separated questions up so that the first entry in the containers
     * parameter is the first member of the group. Assumes that the first element
     * of the containers parameter is the container most near the top of the list.
     * If will be the case if the containers parameter has been generated
     * by the 'getSelectedContainers' method. If not, and the first element
     * is not the highest in the list, then odd index behaviour may result.
     *
     * @param containers the containers to group.
     */
    public void groupContainers(ArrayList<AbstractQuestionContainer> containers) {
        IQuestion[] questions = new IQuestion[questionContainers.size()];
        int index = -1;

        //return if there exists less than two containers in the parameter array
        if (containers.size() < 2) {
            return;
        }

        //find the index of the first parameter array container
        index = questionContainers.indexOf(containers.get(0));

        //exit if the first container not found
        if (index == -1) {
            return;
        }

        //remove all parameter array containers apart from the 'lowest' indexed one.
        for (int i = 1; i < containers.size(); i++) {
            this.removeQuestion(containers.get(i));
            questionContainers.add(index + i, containers.get(i));
        }

        rebuildList();

        minimiseAll();
    }

    /**
     * Adds an <code>ActionListener</code> to this <code>QuestionList</code> instance. The
     * action listeners are notified when the current question changes focus. The question
     * that has focus after the change is accessible by using the event ID passed in the
     * ActionEvent and using this as the index for questionList.getQuestions().<br><br>
     * Example:<br><code>questionList.getQuestions[actionEvent.getID()];</code>
     * @param listener the <code>ActionListener</code> to add.
     */
    public void addActionListener(ActionListener listener) {
        if (actionListeners.length == 0) {
            actionListeners = new ActionListener[]{listener};
        } else {
            ActionListener[] temp = new ActionListener[actionListeners.length + 1];
            System.arraycopy(actionListeners, 0, temp, 0, actionListeners.length);
            temp[actionListeners.length] = listener;
            actionListeners = temp;
        }
    }

    /**
     * Removes a <code>ActionListener</code> from this <code>QuestionList</code> instance.
     * @param listener the <code>ActionListener</code> to remove.
     * @return true if the listener could be found and removed, and false otherwise.
     */
    public boolean removeActionListener(ActionListener listener) {
        if (actionListeners.length == 0) {
            return false;
        }
        if (actionListeners.length == 1) {
            if (actionListeners[0].equals(listener)) {
                actionListeners = new ActionListener[0];
                return true;
            } else {
                return false;
            }
        }

        int index = -1;
        //get the index
        for (int i = 0; i < actionListeners.length; i++) {
            if (actionListeners[i].equals(listener)) {
                index = i;
                break;
            }
        }

        //if index is -1, we have not found the listener
        if (index == -1) {
            return false;
        }

        //otherwise, get rid of the listener
        ActionListener[] temp = new ActionListener[actionListeners.length - 1];
        if (index == 0) {
            System.arraycopy(actionListeners, 1, temp, 0, actionListeners.length - 1);
            actionListeners = temp;
            return true;
        } else if (index == actionListeners.length - 1) {
            System.arraycopy(actionListeners, 0, temp, 0, actionListeners.length - 1);
            actionListeners = temp;
            return true;
        } else //the index is not on the edge of the array
        {
            System.arraycopy(actionListeners, 0, temp, 0, index);
            System.arraycopy(actionListeners, index + 1, temp, index, actionListeners.length - index - 1);
            actionListeners = temp;
            return true;
        }
    }

    /**
     * Remove all questions from the list and the questionContainers Array.
     */
    @Override
    public void removeAll() {
        scrollerContent.removeAll();
        questionContainers = new ArrayList<AbstractQuestionContainer>();
    }

    /**
     * Gets an array of selected question containers.
     * @return an array containing all of the currently selected question containers.
     * Returns an empty AbstractQuestionContainer array if no containers are selected.
     */
    public ArrayList<AbstractQuestionContainer> getSelectedContainers() {
        ArrayList<AbstractQuestionContainer> retVal = new ArrayList<AbstractQuestionContainer>();
        AbstractQuestionContainer[] temp;
        for (int i = 0; i < questionContainers.size(); i++) {
            if (questionContainers.get(i).isSelected()) {
                //we've found a selected container, so add it to the list of selected containers
                retVal.add(questionContainers.get(i));
            }
        }

        return retVal;
    }

    /**
     * Tests whether the array of containers passed are grouped or not. Assumes
     * that the first member of <code>containers</code> are in order as they would
     * appear moving down the question list.
     * @return true if the containers specified are grouped, or if the parameter
     *         array contains less than 2 elements. Returns false
     *         if the containers are not contiguous, or if the first container
     *         does not exist in the array.
     */
    public boolean containersAreContiguous(ArrayList<AbstractQuestionContainer> containers) {

        //returns -1 if the sublist cannot be found as a contiguous sublist.
        if (Collections.indexOfSubList(questionContainers, containers) == -1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Gets the index of the selection. If multiple items are selected, this is the index
     * of the first selected item (highest in the list). If no selection exists, then
     * this method will return -1.
     * @return the index of the first selected item found in the list, or -1 if no selected
     * items are found.
     */
    public int getSelectionIndex() {
        for (int i = 0; i < questionContainers.size(); i++) {
            if (questionContainers.get(i).isSelected()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Copies the specified questions to the clipboard as text - copies them as elements
     * as returned by <code>WriterParser</code>. The copied text contains a header which
     * indicates which library the questions belong to so that the paste destination knows
     * where to access the resources (images, etc.)
     */
    public void copyQuestions(ArrayList<AbstractQuestionContainer> containers) {
        updateQuestionsWithContent();

        if (containers.size() == 0) {
            return;
        }

        //populate array of questions
        IQuestion[] questions = new IQuestion[containers.size()];
        for (int i = 0; i < questions.length; i++) {
            questions[i] = containers.get(i).getQuestion();
        }

        //create XML document which will be placed on clipboard
        Document doc = new Document();
        Element root = new Element("copiedQuestion");
        doc.setRootElement(root);

        Element e;
        root.setAttribute("fromLibraryID", questions[0].getParentLibrary());

        for (int i = 0; i < questions.length; i++) {
            e = ParserWriter.questionToElement(questions[i]);
            root.addContent(e);
        }

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(new XMLOutputter().outputString(doc)), null);


    }

    /**
     * Calls <code>copyQuestions</code> and then removes the selected questions
     * from the list.
     */
    public void cutQuestions(ArrayList<AbstractQuestionContainer> containers) {
        copyQuestions(containers);
        for (int i = 0; i < containers.size(); i++) {
            removeQuestion(containers.get(i));
        }
        updateQuestionsWithContent();

    }

    /**
     * If the clipboard contains questions then this method will insert them at
     * the bottom of the current question list. Image references should be resolved
     * automatically.
     */
    public void pasteQuestions(String destinationLibraryID) throws DataConversionException, IOException, UnsupportedFlavorException {
        updateQuestionsWithContent();

        StringReader read = null;
        Document doc = null;

        //try to get clipboard data
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (clip.getContents(this).isDataFlavorSupported(DataFlavor.stringFlavor)) {
            read = new StringReader((String) clip.getContents(this).getTransferData(DataFlavor.stringFlavor));
        } else {
            //can't do anything if there's no data
            return;
        }

        SAXBuilder sax = new SAXBuilder();
        try {
            doc = sax.build(read);
        } catch (JDOMException ex) {
            //this text is probably not question data
            return;
        }
        //create list of questions
        String sourceLibrary = doc.getRootElement().getAttributeValue("fromLibraryID");
        ListIterator questionList = doc.getRootElement().getChildren("question").listIterator();

        IQuestion q;
        FlexiQuestion flexiQ;
        while (questionList.hasNext()) {
            q = ParserWriter.questionFromElement((Element) questionList.next(), destinationLibraryID);



            //resolve all references to the source library to become the destination library, if copying
            //to a new library. Files/images are copied to the destination library where needed.
            if ((q instanceof FlexiQuestion) && (sourceLibrary.compareTo(destinationLibraryID) != 0)) {
                //must resolve any image references
                flexiQ = (FlexiQuestion) q;
                flexiQ.setQuestionText(resolveLibraryIDReferences(flexiQ.getQuestionText(), destinationLibraryID));
                flexiQ.setAnswerText(resolveLibraryIDReferences(flexiQ.getAnswerText(), destinationLibraryID));
                flexiQ.setPostAnswerText(resolveLibraryIDReferences(flexiQ.getPostAnswerText(), destinationLibraryID));

                q = flexiQ;
            }


            //add the question to the list
            addQuestion(q);

        }

        this.validate();
        updateQuestionsWithContent();
        this.repaint();

    }

    /**
     * Resolves library ID references of a copied question so that there are no problems
     * when it is pasted into the destination library. This searches for all embedded images,
     * and answer fields, and updates their references to any resources with the new libraryID
     * and copies files to the new library.
     * @param richText the rich text to have references resolved.
     * @param newLibID the ID of the destination library.
     * @return the new richText, with all references resolved.
     */
    private String resolveLibraryIDReferences(String richText, String newLibID) {
        RichTextArea tempEditArea = new RichTextArea();
        tempEditArea.setRichText(richText);

        //traverse the rich text area for any components, and reset their parentLibrary values
        int runCount;
        int paragraphCount = tempEditArea.getDocument().getDefaultRootElement().getElementCount();
        javax.swing.text.Element curEl = null;
        AttributeSet curAttr = null;
        AttributeSet prevAttr = null;

        for (int i = 0; i < paragraphCount; i++) {
            //each paragraph has 'runCount' runs
            runCount = tempEditArea.getDocument().getDefaultRootElement().getElement(i).getElementCount();
            for (int j = 0; j < runCount; j++) {
                curEl = tempEditArea.getDocument().getDefaultRootElement().getElement(i).getElement(j);
                curAttr = curEl.getAttributes();

                if (curEl.getName().equals(StyleConstants.ComponentElementName)) //this is a component
                {
                    //this run is a component. May be an answer field, picture or math text component.
                    Component o = (Component) curAttr.getAttribute(StyleConstants.ComponentAttribute);
                    if (o instanceof IAnswerField) {
                        ((IAnswerField) o).resaveImagesAndResources(newLibID);
                        ((IAnswerField) o).setParentLibraryID(newLibID);
                    } else if (o instanceof EmbeddedImage) {

                        String id = IOManager.copyImage(((EmbeddedImage) o).getParentLibraryID(), ((EmbeddedImage) o).getImageID(), newLibID);
                        ((EmbeddedImage) o).setParentLibraryID(newLibID);
                        ((EmbeddedImage) o).setImageID(id);


                    }
                }
            }
        }
        
        return tempEditArea.getRichText();
    }

    /**
     * Add a question in the appropriate question container to the question list. Also
     * adds the question to the questionContainers array.
     * @param question the question to add.
     */
    public void addQuestion(IQuestion question) {
        addQuestion(question, false, -1);
    }

    /**
     * Add a question in the appropriate question container to the question list. Also
     * adds the question to the questionContainers array.
     * @param question the question to add.
     * @param selected whether or not the container should be selected
     */
    public void addQuestion(IQuestion question, boolean selected) {
        addQuestion(question, selected, -1);
    }

    /**
     * Add a question in the appropriate question container to the question list. Also
     * adds the question to the questionContainers array.
     * @param question the question to add.
     * @param selected whether or not the container should be selected
     * @param index the index at which the question should be added, -1 if you want it added to the end.
     */
    public void addQuestion(IQuestion question, boolean selected, int index) {
        updateQuestionsWithContent();

        AbstractQuestionContainer newContainer = null;
        if (question instanceof FlexiQuestion) {
            newContainer = new FlexiQuestionContainer((FlexiQuestion) question);
        } else if (question instanceof TableQuestion) {
            newContainer = new TableQuestionContainer((TableQuestion) question);
        }

        newContainer.setPreferredSize(new Dimension(scrollerContent.getWidth() - 20, 500));
        newContainer.setMinimumSize(new Dimension(scrollerContent.getWidth() - 50, (question instanceof TableQuestion) ? 250 : 440));
        newContainer.setMaximumSize(new Dimension(maximumQuestionContainerWidth, (question instanceof TableQuestion) ? 500 : 600));
        newContainer.setSelected(selected);
        newContainer.minimise();

        //hmm.. shouldn't ever happen
        if (newContainer == null) {
            return;
        }

        //add the new container to the scroller content pane
        scrollerContent.add(Box.createVerticalStrut(5));
        scrollerContent.add(newContainer);


        //add the new question container to the question container reference array
        if (index == -1) {
            questionContainers.add(newContainer);
        } else {
            questionContainers.add(index, newContainer);
        }

        updateQuestionsWithContent();


    }

    /**
     * Remove the specified question container from the question list. If the question
     * container does not exist in the list, no action is taken.
     * @param qContainer the question to be removed.
     */
    public void removeQuestion(AbstractQuestionContainer qContainer) {
        updateQuestionsWithContent();
        //check if there are any containers within the list to start with
        if (questionContainers.size() == 0) {
            return;
        }

        //otherwise, remove the container
        questionContainers.remove(qContainer);
        scrollerContent.remove(qContainer);


        updateQuestionsWithContent();

    }

    /**
     * Move the specified containers down by one index (if possible). The containers will
     * be grouped if not already. The first container in the array must be the highest in the
     * question list (i.e. the one with the smallest index).
     * @param containers the containers to move.
     */
    public void moveQuestionsDown(ArrayList<AbstractQuestionContainer> containers) {
        //can't move no containers.. or more philosophically, no containers have already been moved. Oooh.
        if (containers.size() < 1) {
            return;
        }

        //are containers contiguous?
        if (containers.size() > 1) {
            if (containersAreContiguous(containers) == false) {
                groupContainers(containers);
            }
        }

        int index = -1;
        //what is the index of the first container in the parameter array?
        index = Collections.indexOfSubList(questionContainers, containers);

        if (index == -1) {
            //couldn't find the index of the first container, so just... just leave :-(
            return;
        }


        if ((index + containers.size()) == questionContainers.size()) {
            //can't move down any further
            return;
        }


        for (int i = containers.size() - 1; i >= 0; i--) {
            this.removeQuestion(containers.get(i));
            questionContainers.add(index + containers.size(), containers.get(i));
        }

        rebuildList();

        minimiseAll();
    }

    /**
     * Move the specified containers up by one index (if possible). The containers will
     * be grouped if not already. The first container in the array must be the highest in the
     * question list (i.e. the one with the smallest index).
     * @param containers the containers to move.
     */
    public void moveQuestionsUp(ArrayList<AbstractQuestionContainer> containers) {
        //can't move no containers.. or more philosophically, no containers have already been moved. Oooh.
        if (containers.size() < 1) {
            return;
        }

        //are containers contiguous?
        if (containers.size() > 1) {
            if (containersAreContiguous(containers) == false) {
                groupContainers(containers);
            }
        }

        int index = -1;
        //what is the index of the first container in the parameter array?
        index = Collections.indexOfSubList(questionContainers, containers);

        if (index == -1) {
            //couldn't find the index of the first container, so just... just leave :-(
            return;
        }
        if (index == 0) {
            //can't move up any further so leave
            return;
        }


        for (int i = 0; i < containers.size(); i++) {
            this.removeQuestion(containers.get(i));
            questionContainers.add(index - 1, containers.get(i));
        }

        rebuildList();

        minimiseAll();

    }

    /**
     * Updates the internal question classes with the data in the question containers.
     */
    public void updateQuestionsWithContent() {
        IQuestion question;
        FlexiQuestionContainer fcontainer;
        TableQuestionContainer tcontainer;
        FlexiQuestion fquestion;
        TableQuestion tquestion;

        Iterator<AbstractQuestionContainer> iterate = questionContainers.iterator();
        AbstractQuestionContainer curContainer;
        while (iterate.hasNext()) {
            curContainer = iterate.next();
            question = curContainer.getQuestion();

            if (question instanceof FlexiQuestion) {
                fquestion = (FlexiQuestion) question;
                fcontainer = (FlexiQuestionContainer) curContainer;

                fquestion.setAnswerText(fcontainer.getAnswerText());
                fquestion.setQuestionText(fcontainer.getQuestionText());
                fquestion.setPostAnswerText(fcontainer.getPostAnswerText());
                fquestion.setUsePostAnswerText(fcontainer.getUsePostAnswerText());
            } else if (question instanceof TableQuestion) {
                tquestion = (TableQuestion) question;
                tcontainer = (TableQuestionContainer) curContainer;

                tquestion.setCol1Data(tcontainer.getColumn1Data());
                tquestion.setCol2Data(tcontainer.getColumn2Data());

                tquestion.setAskInReverse(tcontainer.getOptionPane().getAskInReverse().isSelected());
                tquestion.setMarksPerCorrectAnswer(tcontainer.getOptionPane().getMarksPerAnswer().getValue());
                tquestion.setQuestionTemplateFwd(tcontainer.getOptionPane().getFwdQuestionTemplate().getText());
                tquestion.setQuestionTemplateBwd(tcontainer.getOptionPane().getBwdQuestionTemplate().getText());
                //get the ask style model, and the index of the selected ask style
                SpinnerListModel askStyleModel = ((SpinnerListModel) tcontainer.getOptionPane().getAskStyle().getModel());
                tquestion.setQuizMethod(askStyleModel.getList().indexOf(askStyleModel.getValue()));
                tquestion.setFontFamilyName(tcontainer.getOptionPane().getSelectedFont().getFamily());
                tquestion.setFontSize(tcontainer.getOptionPane().getSelectedFontSize());

                //ensure that the column data array sizes are equal, and extend or shrink
                //the length of the timesAsked, marksAwarded, and marksAcheived arrays
                tquestion.validateArrays();
            }
        }
    }

    /**
     * Get the questions listed in this <code>QuestionList</code> instance.
     * @return the questions listed in this <code>QuestionList</code> instance.
     */
    public IQuestion[] getQuestions() {
        IQuestion[] retVal = new IQuestion[questionContainers.size()];
        for (int i = 0; i < questionContainers.size(); i++) {
            retVal[i] = questionContainers.get(i).getQuestion();
        }

        return retVal;
    }
}

