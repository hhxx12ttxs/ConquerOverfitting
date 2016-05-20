/*
 * Library.java
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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

/**
 * Encapsulates a library of questions.
 *
 * @author Thomas Everingham
 * @version 1.0
 */
public class Library {
    /**
     * The name of the library. Must not be null or an empty string.
     */
    protected String name;
    /**
     * Libraries may have the same name, so a unique id was generated for this library
     * when it was created.
     */
    protected String id;
    /**
     * The library description, can be an empty string.
     */
    protected String description;
    /**
     * The date of creation of this library.
     */
    protected Date creationDate;
    /**
     * The questions contained by this library.
     */
    protected IQuestion[] questions;
    /**
     * The path of the expanded library file. Library files are zip files, and when
     * opened, they are extracted to a temporary location.
     */
    protected File pathTempLib;
    /**
     * The library's text file; contains all question data, etc.
     */
    protected File fileLibraryFile;
    /**
     * A list of all previous results from quizzes that included this library.
     */
    protected List<HistoryEntry> quizHistory;
    /**
     * A hashtable of image identifiers and the corresponding File object. The
     * Parser class has methods to generate this hash table.
     */
    protected Hashtable images;


    /**
     * Create a new library object.
     *
     * @param libName the name of this library (doesn't need to be unique)
     * @param id the ID of this library (necessarily unique)
     * @param libDescription a description of this library, what it contains (can be empty)
     * @param dateOfCreation the date this library was created
     * @param questions the question objects that belong to this library.
     * @param pathTempLib the path of the temporary directory into which this library has been extracted.
     * @param fileQuestionData the file in the temporary directory that contains all question data for this library.
     * @param images hashtable of imageIDs and their corresponding File objects that point to files in the temporary directory.
     */
    public Library(String libName, String id, String libDescription, Date dateOfCreation, IQuestion[] questions, File pathTempLib, File fileQuestionData, List<HistoryEntry> quizHistory, Hashtable images)
    {
        this.name = libName;
        this.id = id;
        this.description = libDescription;
        this.creationDate = dateOfCreation;
        this.questions = questions;
        this.pathTempLib = pathTempLib;
        this.fileLibraryFile = fileQuestionData;
        this.quizHistory = quizHistory;
        this.images = images;
    }

    /**
     * Gets the creation date of this library.
     * @return the creation date of this library.
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Gets the list of HistoryEntries associated with this library.
     * @return the list of quiz HistoryEntries held by this library.
     */
    public List<HistoryEntry> getQuizHistory() {
        return quizHistory;
    }

    /**
     * Add a quiz history to this library.
     * @param entry the entry to add.
     */
    public void addQuizHistory(HistoryEntry entry) {
        quizHistory.add(entry);
    }

    /**
     * Sets the creation date of this library.
     * @param creationDate the creation date of this library.
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Gets the description of this library. May be an empty string.
     * @return the description of this library.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this library. May be an empty string.
     * @param description the new description of this library.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the library file associated with this Library.
     * @return the library file associated with this Library.
     */
    public File getFileLibraryFile() {
        return fileLibraryFile;
    }

    /**
     * Gets the hashtable of image ID keys and corresponding image files.
     * @return the hashtable of image ID's and image files.
     */
    public Hashtable getImages() {
        return images;
    }

    /**
     * Gets the name of the library.
     * @return the name of the library.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this library.
     * @param name the new name of this library.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Clears the HistoryEntry items from this library. Does not clear the individual history for
     * each quesiton in the library.
     */
    public void clearQuizHistory() {
        quizHistory = new ArrayList<HistoryEntry>();
    }

    /**
     * Gets the ID of this library (the ID is guaranteed to be a unique identifier
     * for the library, whereas the library name is not necessarily unique).
     * @return the ID of this library.
     */
    public String getId() {
        return id;
    }
    /**
     * Sets the ID of this library (the ID is guaranteed to be a unique identifier
     * for the library, whereas the library name is not necessarily unique).
     */
    public void setId(String id) {
        this.id = id;
    }



    /**
     * The temporary path for library files (images and data). Does not include
     * trailing "/".
     * @return the temporary path for library files.
     */
    public File getPathTempLib() {
        return pathTempLib;
    }

    /**
     * Gets the array of all questions contained within this library.
     * @return the array of all questions within this library.
     */
    public IQuestion[] getQuestions() {
        return questions;
    }

    /**
     * Sets the array of all questions within this library.
     * @param questions the new array of all questions within this library.
     */
    public void setQuestions(IQuestion[] questions) {
        this.questions = questions;
    }


    /**
     * Gets the question at the specified index of the questions array.
     * @param index the question to be returned.
     * @return the question a the specified index of the questions array.
     */
    public IQuestion getQuestion(int index)
    {
        return questions[index];
    }

    /**
     * Get the number of questions in this library.
     * @return the number of questions in this library.
     */
    public int getQuestionCount()
    {
        return questions.length;
    }

    /**
     * Add the specified question to this library.
     * @param q the question to add.
     */
    public void addQuestion(IQuestion q) {
        IQuestion[] temp = new IQuestion[questions.length + 1];
        System.arraycopy(questions, 0, temp, 0, questions.length);
        temp[questions.length] = q;
        questions = temp;
    }


}

