/*
 * IOManager.java
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
package org.ingatan.io;

import java.awt.Graphics2D;
import java.awt.Image;
import org.ingatan.component.answerfield.AnsFieldLabelPicture;
import org.ingatan.component.answerfield.AnsFieldList;
import org.ingatan.component.answerfield.AnsFieldMultiChoice;
import org.ingatan.component.answerfield.AnsFieldSelfGraded;
import org.ingatan.component.answerfield.AnsFieldSimpleMultiChoice;
import org.ingatan.component.answerfield.AnsFieldSimpleText;
import org.ingatan.component.answerfield.AnsFieldTrueFalse;
import org.ingatan.data.AnswerFieldsFile;
import org.ingatan.data.FlexiQuestion;
import org.ingatan.data.Group;
import org.ingatan.data.IQuestion;
import org.ingatan.data.Library;
import org.ingatan.data.QuizHistoryEntry;
import org.ingatan.data.QuizHistoryFile;
import org.ingatan.data.TableQuestion;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.ingatan.component.answerfield.AnsFieldEmbeddedAudio;
import org.ingatan.component.answerfield.AnsFieldHint;
import org.ingatan.component.answerfield.AnsFieldTable;
import org.ingatan.component.image.ImageAcquisitionDialog;
import org.ingatan.data.HistoryEntry;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Manages IO operations, and takes care of removing all temporary directories
 * as they are no longer needed. This class hinges on all other classes reporting to it
 * when the resource is no longer needed. For example, the Library Manager must
 * report that it no longer requires a particular library to be open, and so the
 * library's temporary directory may then be deleted provided no other component
 * requires it.
 *
 * @author Thomas Everingham
 * @version 1.0
 */
public abstract class IOManager {

    /**
     * The path in which the libraries are held.
     */
    private static final String LIBRARY_PATH = "/.ingatan/quesLibs/";
    /**
     * The path in which answer field classes are kept
     */
    private static final String ANSWER_FIELD_PATH = "/.ingatan/answerFields/";
    /**
     * The answer fields file. Describes all answer fields that have been added as plug-ins, as well as
     * all default answer fields, and contains their default values.
     */
    private static final String ANSWER_FIELD_FILE = "/.ingatan/ansFields";
    /**
     * The preferences file. This includes preferences that may be set, as well as the map of what symbols are displayed to the user in the symbol
     * menu for each character. The symbol menu is shown inline for almost all Ingatan text fields
     * when the user presses Ctrl+space followed by a character.
     */
    private static final String PREFERENCES_FILE = "/.ingatan/prefs";
    /**
     * The quiz history file. This file contains the overall historic score, as well as entries for
     * each time a quiz has been taken.
     */
    private static final String QUIZ_HISTORY_FILE = "/.ingatan/quizHistory";
    /**
     * The groups file. This file contains the list of libraries and which group they belong to.
     */
    private static final String GROUPS_FILE = "/.ingatan/groups";
    /**
     * The path in which libraries are temporarily extracted.
     */
    private static final String TEMP_LIBRARY_PATH = "/.ingatan/temp/";
    /**
     * Base directory that exists in the user's home directory.
     */
    private static final String BASE_DIRECTORY = "/.ingatan/";
    /**
     * Base directory for custom SketchEl templates.
     */
    private static final String CHEM_TEMPLATES_DIRECTORY = "/.ingatan/chem_templates/";
    /**
     * Collections directory. Any folders in this directory are interpretted as categories,
     * and the images within those folders are read in as the images belonging to that category.
     * These images are used in the "From Collection" method in the ImageAcquisitionDialog, and
     * also by the stamp tool in the ImageEditorPane.
     */
    private static final String COLLECTIONS_PATH = "/.ingatan/collections/";
    /**
     * Any method that generates an image ID should respect this value for maximum length.
     */
    public static final int MAX_IMAGE_ID_LENGTH = 15;
    /**
     * All groups files, as loaded upon initiation
     */
    protected static Group[] groups;
    /**
     * List of library IDs, as loaded from the groups file. When a new library is
     * created, or an old library is deleted, this array is updated. The ID is
     * also the filename of the library. When the groups file is resaved, the values
     * for the 'all libraries' field come directly from this array.
     */
    protected static String[] libraryIDs;
    /**
     * The names of the libraries as they correspond to memembers of the libraryIDs array.
     */
    protected static String[] libraryNames;
    /**
     * Library objects representing libraries that have been loaded. Not all libraries
     * are loaded at once. The libraries in this array have been loaded, and will be
     * unloaded once the IOManager detects that they are no longer in use anywhere.
     */
    protected static Library[] libraries;
    /**
     * The AnswerFieldsFile contains all default and plug-in answer field classes in a hashtable. The
     * classes are accessible by using their class name. Default values for all answer fields are also
     * included in a hashmap. These are generated and read by each answer field, and set by the user.
     */
    protected static AnswerFieldsFile answerFieldsFile;
    /**
     * File that contains the total overall score acheived so far, as well as records of all quizes that
     * have been undertaken (dates, results, questions asked/skipped, and libraries used). The user can
     * delete specific entries, so may not be complete.
     */
    protected static QuizHistoryFile quizHistoryFile;
    /**
     * This flag is set so that the manager cannot be initialised twice.
     */
    protected static boolean beenInitialised = false;
    /**
     * This is the home path for this user, as reported by System.getProperty("user.home"). This can
     * be problematic on some systems, and so it is possible to set a custom path for
     */
    protected static String userHomePath;
    /**
     * This is set when the user uses the --homeDir=[directory] option to load ingatan. If this
     * string is not empty, then the userHomePath obtained from System.getProperty("user.home") will
     * be overwritten with this value.
     */
    protected static String customHomePath = "";
    /**
     * URLClassLoader which is instantiated with the user's home directory as a classpath. The answer
     * field plug-in classes are located here, and this provides a means by which to load them.
     */
    protected static URLClassLoader ansFieldClassLoader;
    /**
     * Instance of the <code>Random</code> class that is seeded with the current time in milliseconds
     * when the IOManager is instantiated.
     */
    public static Random random = new Random(Calendar.getInstance().getTimeInMillis());
    /**
     * Character map for the symbol menu as loaded from the symbol menu file, or generated from default if
     * the symbol menu file does not exist.
     */
    private static HashMap symbolMenuCharMap;
    /**
     * Ingatan's icon sized for windows/frames.
     */
    public static Image windowIcon;
    /**
     * Little michael logo for the selector tabs of the question containers. One is chosen randomly each time the selector
     * tab is painted to show that the tab is selected.
     */
    private static Image[] selectorTabIcons;
    /**
     * Little dot for the selector tabs of the question containers. This is only used if the user has made this dot the
     * preference over the little Michaels (above).
     */
    private static Image selectorTabDot;
    /**
     * Whether or not a little michael icon should be used to indicate selection in the question list.
     */
    private static boolean useMichaelForSelectorTabs = false;
    /**
     * Whether or not the library manager has been loaded before. If it hasn't, then a message will be displayed
     * with some basic information on how to use Ingatan.
     */
    private static boolean firstTimeLoadingLibManager = true;
    /**
     * Whether or not this is the first time Ingatan has been loaded for this installation.
     */
    private static boolean firstTimeLoadingIngatan = true;
    /**
     * The group that was last selected in the Library Manager before it was closed. The library
     * manager will show this group when it is opened.
     */
    private static String previouslySelectedGroup = "- All Libraries -";
    /**
     * Image acquisition dialog so that a new one does not need to be loaded every time.
     */
    private static ImageAcquisitionDialog imgAcquisition;

    /**
     * Initiates the IOManager. Loads all group files, and a list of all library files.
     * If the IOManager has already been initialised and this method is called, then
     * no action is taken.
     */
    public static void initiateIOManager() {
        if (!beenInitialised) {
            beenInitialised = true;

            if (customHomePath.isEmpty() == false) {
                userHomePath = customHomePath;
            } else {
                userHomePath = System.getProperty("user.home");
                System.out.println("Using: '" + userHomePath + "' as default configuration directory."
                        + "\n       If this is not working out for you, please load"
                        + "\n       ingatan using the --homeDir=\"directory_here\" option.\n");
            }

            //the urlClassLoader provides a way to load classes from a different class path
            try {
                ansFieldClassLoader = new URLClassLoader(new URL[]{new URL("file:" + userHomePath + ANSWER_FIELD_PATH)});
            } catch (MalformedURLException ex) {
                Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, "while trying to create the answer field class loader (IOManager initiation)", ex);
            }

            //ensure that USER.HOME/.ingatan/ exists
            File ingatanPathFile = new File(userHomePath + BASE_DIRECTORY);
            if (ingatanPathFile.exists() == false) {
                System.out.println(userHomePath + BASE_DIRECTORY + " does not exist. Attempting to create it.");
                if (ingatanPathFile.mkdir()) {
                    System.out.println("    -created successfully.");
                } else {
                    //not sure what to do if we can't even create the base directory.
                    System.out.println("    -could not create " + userHomePath + BASE_DIRECTORY + ", check permissions. Terminating.\n\n");
                    Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, "Could not create the base directory /.ingatan.");
                    System.exit(-1);
                }
            }

            //ensure that LIBRARY_PATH exists
            File libPathFile = new File(getLibraryPath());
            if (libPathFile.exists() == false) {
                System.out.println("Library path does not exist. Attempting to make it.");
                if (libPathFile.mkdir()) {
                    System.out.println("    -library path created at: " + getLibraryPath());
                } else {
                    System.out.println("    -could not create library path, check permissions. Terminating.\n\n");
                    System.exit(-1);
                }
            }

            //ensure that TEMP_LIBRARY_PATH exists
            File tempPathFile = new File(getTempPath());
            if (tempPathFile.exists() == false) {
                System.out.println("Temporary library extraction path does not exist. Attempting to make it.");
                if (tempPathFile.mkdir()) {
                    System.out.println("    -temporary extraction path created at: " + getTempPath());
                } else {
                    //need to exit, because we need somewhere to extract the open libraries.
                    System.out.println("    -could not create temporary extraction path, check permissions. Terminating.\n\n");
                    System.exit(-1);
                }
            }

            //ensure that ANSWER_FIELD_PATH exists
            File AnsFieldPathFile = new File(getAnswerFieldPath());
            if (AnsFieldPathFile.exists() == false) {
                System.out.println("Answer fields path does not exist. Attempting to make it.");
                if (AnsFieldPathFile.mkdir()) {
                    System.out.println("    -answer fields path created at: " + getAnswerFieldPath());
                } else {
                    System.out.println("    -could not create answer fields path, check permissions.\n\n");
                }
            }

            //ensure that COLLECTIONS_PATH exists
            File collectionsPathFile = new File(getCollectionsPath());
            if (collectionsPathFile.exists() == false) {
                System.out.println("Collections path does not exist. Attempting to make it.");
                if (collectionsPathFile.mkdir()) {
                    System.out.println("    -collections path created at: " + getCollectionsPath());
                } else {
                    System.out.println("    -could not create collections path, check permissions.\n\n");
                }
            }

            //ensure that COLLECTIONS_PATH exists
            File templatesPathFile = new File(getChemTemplatesPath());
            if (templatesPathFile.exists() == false) {
                System.out.println("Chem custom templates path does not exist. Attempting to make it.");
                if (templatesPathFile.mkdir()) {
                    System.out.println("    -chem custom templates path created at: " + getChemTemplatesPath());
                } else {
                    System.out.println("    -could not create custom templates path, check permissions.\n\n");
                }
            }

            //load the groups file
            File groupsFile = new File(getGroupsFile());
            groups = new Group[0];
            libraryIDs = new String[0];
            libraryNames = new String[0];
            libraries = new Library[0];
            if (groupsFile.exists() == false) {
                System.out.println("Groups file does not exist. Using defaults.");
            } else {
                SAXBuilder sax = new SAXBuilder();
                Document doc = null;
                try {
                    doc = sax.build(groupsFile);
                } catch (JDOMException ex) {
                    Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, "while trying to create xml document from the groups file", ex);
                } catch (IOException ex) {
                    Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, "while trying to create xml document from the groups file", ex);
                }

                //we really can't do anything if we've failed to load this document AND we've checked
                //that it exists..
                if (doc == null) {
                    System.out.println("Groups doc says it exists, but it cannot be accessed. Terminating.");
                    Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, "The groups file apparently exists, but it could not be accessed.\n"
                            + "Without access to the groups file, there is not a lot that can be done.");
                    System.exit(-1);
                }
                try {
                    libraryIDs = doc.getRootElement().getChild("allLibIDs").getText().split("<;>");
                    //if there are no libraries, we will get the String array {""}. Do not want this
                    //added to the library list
                    if (libraryIDs.length == 1) {
                        if (libraryIDs[0].isEmpty()) {
                            libraryIDs = new String[0];
                        }
                    }
                    libraryNames = doc.getRootElement().getChild("allLibNames").getText().split("<;>");
                    //if there are no libraries, we will get the String array {""}. Do not want this
                    //added to the library list
                    if (libraryNames.length == 1) {
                        if (libraryNames[0].isEmpty()) {
                            libraryNames = new String[0];
                        }
                    }

                    //iterates through each group element in the groups file. Each group element represents one single group.
                    ListIterator groupIterator = doc.getRootElement().getChild("groups").getChildren("group").listIterator();

                    //The groups array collects the groups as they are created.
                    groups = new Group[0];
                    Group[] temp;
                    Element eGroup;
                    Group newGroup;
                    String[] libIDs;
                    String[] libNames;
                    //now iterate the groups
                    while (groupIterator.hasNext()) {
                        //expand the groups array by 1
                        temp = new Group[groups.length + 1];
                        System.arraycopy(groups, 0, temp, 0, groups.length);
                        //collect information from the element
                        eGroup = (Element) groupIterator.next();
                        libIDs = eGroup.getText().split("<;>");
                        libNames = new String[libIDs.length];
                        for (int i = 0; i < libNames.length; i++) {
                            libNames[i] = getLibraryName(libIDs[i]);
                        }
                        //create the group and store it in the groups array
                        newGroup = new Group(eGroup.getAttribute("name").getValue(), libNames, libIDs);
                        temp[groups.length] = newGroup;
                        groups = temp;
                    }
                } catch (NullPointerException badFile) {
                    System.out.println("Bad groups file :-(\n    -setting groups and libraries as empty\n    -libraries or groups that exist will not be visible.");
                    System.out.println("\n ** If you do not wish for the current data to be overwritten with\nthese empty values, exit now, and fix " + getGroupsFile() + " manually! **");
                    groups = new Group[0];
                    libraryIDs = new String[0];
                    libraryNames = new String[0];
                    libraries = new Library[0];
                }

            }


            File ansFieldsFile = new File(getAnswerFieldFile());
            if (ansFieldsFile.exists() == false) {
                System.out.println("Answer fields file does not exist. Initialising defaults.");
                Hashtable defaults = new Hashtable<String, String>();
                Hashtable classes = new Hashtable<String, Class>();
                addDefaultAnswerFieldsToTables(classes, defaults);
                //default answer fields must be added to the hashtables here
                answerFieldsFile = new AnswerFieldsFile(classes, defaults);
            } else {
                answerFieldsFile = ParserWriter.parseAnswerFieldFile(ansFieldsFile);
            }

            File quizHistFile = new File(getQuizHistoryFilePath());
            if (quizHistFile.exists() == false) {
                System.out.println("Quiz history file does not exist. Creating new file.");
                //create a few 'starter' rewards
                ArrayList<String> names = new ArrayList<String>();
                ArrayList<String> icons = new ArrayList<String>();
                ArrayList<Number> prices = new ArrayList<Number>();
                names.add("Chocolate Bar"); names.add("Bottle of Wine"); names.add("See a Movie"); names.add("Play a Game"); names.add("Afternoon Tea");
                icons.add("jar://resources/rewards/choc.png"); icons.add("jar://resources/rewards/wine.png"); icons.add("jar://resources/rewards/movie.png"); icons.add("jar://resources/rewards/game.png"); icons.add("jar://resources/rewards/coffee.png");
                prices.add(250); prices.add(2000); prices.add(1200); prices.add(500); prices.add(150);
                quizHistoryFile = new QuizHistoryFile(new ArrayList<QuizHistoryEntry>(), 0, names, prices, icons);
                ParserWriter.writeQuizHistoryFile(quizHistoryFile);
            } else {
                quizHistoryFile = ParserWriter.parseQuizHistoryFile();
            }


            File preferencesFile = new File(getPreferencesFile());
            if (preferencesFile.exists() == false) {
                System.out.println("Preferences file does not exist. Creating with defaults.");
                //set up the default values and write the file
                defaultSymbolMenuCharacterMap();
                //default is to use mini michaels to indicate selection in the questionlist
                //default is that the library manager has never been loaded for this installation.
                ParserWriter.writePreferencesFile(symbolMenuCharMap);
            } else {
                symbolMenuCharMap = ParserWriter.parsePreferencesFile();
            }
        }

        System.out.print("Initialising image acquisition...");
        //LOAD THE IMAGE ACQUISITION MENU
        imgAcquisition = new ImageAcquisitionDialog();
        System.out.println("done");

        //LOAD THE WINDOW ICON AND THE SELECTOR TAB ICONS
        try {
            windowIcon = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource("resources/icons/windowIcon.png"));

            selectorTabIcons = new Image[3];
            selectorTabIcons[0] = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource("resources/icons/selMike1.png"));
            selectorTabIcons[1] = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource("resources/icons/selMike2.png"));
            selectorTabIcons[2] = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource("resources/icons/selMike3.png"));
            selectorTabDot = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource("resources/icons/selBall.png"));
        } catch (IOException ex) {
            Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, "Trying to load the window icon while initiating IO manager", ex);
        }
    }

    /**
     * Gets a main menu background, randomly selected from the present images embedded
     * within the jar.
     * @return the randomly selected main menu image.
     */
    public static BufferedImage getNewMenuBackground() {
        String[] images = new String[]{
            "resources/AaronLogan.png",
            "resources/Daisy.png",
            "resources/WolfgangWander.png",
            "resources/PineNuts.png",
            "resources/Michael.png",
            "resources/MichaelSleep.png",};

        //load a random image from the above
        BufferedImage img = null;
        try {
            //dictates which background will be loaded.
            int imgIndex = random.nextInt(images.length);
            //dictates whether or not the 'go to ingatan.org' speech bubble will be overlaid if the michael.png image is loaded.
            int imgSpeech = random.nextInt(10);

            //if this is the first load, show the micahel.png image with a speech bubble.
            if (isFirstTimeLoadingIngatan())
            {
                imgIndex = 4;
                imgSpeech = 10;
            }

            img = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource(images[imgIndex]));
            if ((imgSpeech > 4) && (imgIndex == 4))
            {
                BufferedImage imgSpeechBubble = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource("resources/hello.png"));
                Graphics2D g = (Graphics2D) img.getGraphics();
                g.drawImage(imgSpeechBubble, null, 150, 220);
            }
        } catch (Exception e) {
            Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, "while trying to load one of the background images for the main menu", e);
        }

        return img;
    }

    /**
     * Gets the ImageAcquisitionDialog object held by the IOManager. The <code>reset</code>
     * method is called before returning the object.
     * @return the reset <code>ImageAcquisitionDialog</code>.
     */
    public static ImageAcquisitionDialog getImageAcquisitionDialog() {
        imgAcquisition.reset();
        return imgAcquisition;
    }

    /**
     * Gets an image for the indication that a selector tab is selected. It is a preference
     * as to whether a circle dot should be used, or playful icons of Michael. The appropriate
     * icon is returned depending on this setting.
     * @return an image for the indication that a selector tab is selected.
     */
    public static Image getSelectionIcon() {
        if (useMichaelForSelectorTabs) {
            return selectorTabIcons[random.nextInt(selectorTabIcons.length)];
        } else {
            return selectorTabDot;
        }
    }

    /**
     * Checks whether or not the library manager has been loaded before.
     * @return if the library manager has ever been loaded before for this installation.
     */
    public static boolean isFirstTimeLoadingLibManager() {
        return firstTimeLoadingLibManager;
    }

    /**
     * Set whether the library manager has ever been loaded before for this installation.
     * @param firstTimeLoadingLibManager whether or not the library manager has been loaded before for this installation.
     */
    public static void setFirstTimeLoadingLibManager(boolean firstTimeLoadingLibManager) {
        IOManager.firstTimeLoadingLibManager = firstTimeLoadingLibManager;
    }

    /**
     * Sets the Library Manager group that was last selected when the Manager was
     * last closed. The Library Manager will show this group initially when next loaded.
     * @param group
     */
    public static void setPreviouslySelectedGroup(String group) {
        previouslySelectedGroup = group;
    }

    /**
     * Gets the Library Manager group that was last selected when the Manager was
     * last closed. The Library Manager will show this group initially when next loaded.
     * @return The name of the previously selected group.
     */
    public static String getPreviouslySelectedGroup() {
        return previouslySelectedGroup;
    }

    /**
     * Checks whether or not Ingatan has been loaded before for this installation.
     * @return <code>true</code> if Ingatan has never been loaded before for this installation.
     */
    public static boolean isFirstTimeLoadingIngatan() {
        return firstTimeLoadingIngatan;
    }

    /**
     * Set whether or not Ingatan has been loaded before for this installation.
     * @param firstTimeLoadingIngatan <code>true</code> if Ingatan has never been loaded before for this installation.
     */
    public static void setFirstTimeLoadingIngatan(boolean firstTimeLoadingIngatan) {
        IOManager.firstTimeLoadingIngatan = firstTimeLoadingIngatan;
    }

    /**
     * Whether or not mini Michael icons should be used in QuestionList to indicate
     * selection. If not, a small blue ball is used.
     * @param useMichael <code>true</code> if Michael should be used to indicate selection.
     */
    public static void setUseMichaelAsSelectionIndicator(boolean useMichael) {
        useMichaelForSelectorTabs = useMichael;
    }

    /**
     * Checks whether or not mini Michael icons are being used in QuestionList to indicate
     * selection. If not, a small blue ball is being used.
     */
    public static boolean isUsingMichaelAsSelectionIndicator() {
        return useMichaelForSelectorTabs;
    }

    /**
     * Adds all "hard coded" default answer fields to the provided class and defaults HashTables. This is the central
     * location for the listing of default answer fields. Adds empty defaults to the defaults table.
     * @param classTable the Hashtable with String keys and Class objects to which the default answer fields should be added.
     * @param defaultsTable the Hashtable with String keys and String objects to which the default answer fields should be added.
     */
    public static void addDefaultAnswerFieldsToTables(Hashtable<String, Class> classTable, Hashtable<String, String> defaultsTable) {
        
        classTable.put("org.ingatan.component.answerfield.AnsFieldMultiChoice", AnsFieldMultiChoice.class);
        defaultsTable.put("org.ingatan.component.answerfield.AnsFieldMultiChoice", "");

        classTable.put("org.ingatan.component.answerfield.AnsFieldTrueFalse", AnsFieldTrueFalse.class);
        defaultsTable.put("org.ingatan.component.answerfield.AnsFieldTrueFalse", "");

        classTable.put("org.ingatan.component.answerfield.AnsFieldSimpleMultiChoice", AnsFieldSimpleMultiChoice.class);
        defaultsTable.put("org.ingatan.component.answerfield.AnsFieldSimpleMultiChoice", "");

        classTable.put("org.ingatan.component.answerfield.AnsFieldLabelPicture", AnsFieldLabelPicture.class);
        defaultsTable.put("org.ingatan.component.answerfield.AnsFieldLabelPicture", "");

        classTable.put("org.ingatan.component.answerfield.AnsFieldList", AnsFieldList.class);
        defaultsTable.put("org.ingatan.component.answerfield.AnsFieldList", "");

        classTable.put("org.ingatan.component.answerfield.AnsFieldTable", AnsFieldTable.class);
        defaultsTable.put("org.ingatan.component.answerfield.AnsFieldTable", "");

        classTable.put("org.ingatan.component.answerfield.AnsFieldSelfGraded", AnsFieldSelfGraded.class);
        defaultsTable.put("org.ingatan.component.answerfield.AnsFieldSelfGraded", "");

        classTable.put("org.ingatan.component.answerfield.AnsFieldEmbeddedAudio", AnsFieldEmbeddedAudio.class);
        defaultsTable.put("org.ingatan.component.answerfield.AnsFieldEmbeddedAudio", "");

        classTable.put("org.ingatan.component.answerfield.AnsFieldHint", AnsFieldHint.class);
        defaultsTable.put("org.ingatan.component.answerfield.AnsFieldHint", "");

        classTable.put("org.ingatan.component.answerfield.AnsFieldSimpleText", AnsFieldSimpleText.class);
        defaultsTable.put("org.ingatan.component.answerfield.AnsFieldSimpleText", "");
    }

    /**
     * Gets the symbol menu character map. This contains the symbol lists that correspond
     * to a particular symbol menu generator. For example, if the user presses Ctrl+space to
     * initiate the menu, and then the 'a' key to generate the menu, a is used as the key in this
     * map to retreive the list of symbols that should be shown.
     * @return the symbol menu character map.
     */
    public static HashMap<String, String> getSymbolMenuCharacterMap() {
        return symbolMenuCharMap;
    }

    /**
     * Reset the symbol menu character map to the default values. This writes the
     * default values to the symbol menu file, and sets the IOManager symbol menu character map variable.
     */
    public static void defaultSymbolMenuCharacterMap() {
        symbolMenuCharMap = setUpSymbolDefaults();
        ParserWriter.writePreferencesFile(symbolMenuCharMap);
    }

    /**
     * Gets the quiz history file initialised/parsed at IOManager initialisation.
     * May have been mutated since then.
     * @return the quiz history file.
     */
    public static QuizHistoryFile getQuizHistoryFile() {
        return quizHistoryFile;
    }

    /**
     * Gets the hard coded defaults for the symbol menu character map.
     * @return the hard coded default symbol menu character map.
     */
    private static HashMap setUpSymbolDefaults() {
        HashMap characterMap = new HashMap<String, String>();
        characterMap.put("a", "äĺŕáăâć???");
        characterMap.put("c", "ç????");
        characterMap.put("d", "??");
        characterMap.put("e", "čéęë?????");
        characterMap.put("f", "");
        characterMap.put("g", "?????");
        characterMap.put("h", "????");
        characterMap.put("i", "ěíîď????");
        characterMap.put("j", "?");
        characterMap.put("k", "??");
        characterMap.put("l", "?????");
        characterMap.put("m", "???");
        characterMap.put("n", "ń?????");
        characterMap.put("o", "öřňó?ôő???");
        characterMap.put("r", "???");
        characterMap.put("s", "?????");
        characterMap.put("t", "??????");
        characterMap.put("u", "ůú?űü?????");
        characterMap.put("w", "??");
        characterMap.put("y", "????");
        characterMap.put("z", "????");
        characterMap.put("A", "äĺŕáăâć???".toUpperCase());
        characterMap.put("C", "ç????".toUpperCase());
        characterMap.put("D", "??".toUpperCase());
        characterMap.put("E", "čéęë?????".toUpperCase());
        characterMap.put("F", "".toUpperCase());
        characterMap.put("G", "?????".toUpperCase());
        characterMap.put("H", "????".toUpperCase());
        characterMap.put("I", "ěíîď????".toUpperCase());
        characterMap.put("J", "?".toUpperCase());
        characterMap.put("K", "??".toUpperCase());
        characterMap.put("L", "?????".toUpperCase());
        characterMap.put("M", "???".toUpperCase());
        characterMap.put("N", "ń?????".toUpperCase());
        characterMap.put("O", "öřňó?ôő???".toUpperCase());
        characterMap.put("R", "???".toUpperCase());
        characterMap.put("S", "?????".toUpperCase());
        characterMap.put("T", "??????".toUpperCase());
        characterMap.put("U", "ůú?űü?????".toUpperCase());
        characterMap.put("W", "??".toUpperCase());
        characterMap.put("Y", "????".toUpperCase());
        characterMap.put("Z", "????".toUpperCase());
        characterMap.put("0", "asdsad");
        characterMap.put("1", "????????");
        characterMap.put("2", "????????");
        characterMap.put("3", "?????????");
        characterMap.put("4", " ");
        characterMap.put("5", " ");
        characterMap.put("6", " ");
        characterMap.put("7", " ");
        characterMap.put("8", " ");
        characterMap.put("9", " ");
        characterMap.put("", " ");

        return characterMap;
    }

    /**
     * Cleans up the ingatan directory, repacks any open libraries and deletes the temporary
     * library directory. Also rewrites the groups file.
     */
    public static void cleanUpAndRepackage() throws IOException {
        //if the library exists in this array, then it has been unpacked
        for (int i = 0; i < libraries.length; i++) {
            packageLibrary(libraries[i].getId());
            //delete the temporary data for this library
            File tempPath = libraries[i].getPathTempLib();
            if (tempPath.exists()) {
                //must delete the temporary path and all files within it
                File[] files = tempPath.listFiles();
                for (int j = 0; j < files.length; j++) {
                    files[j].delete();
                }
                tempPath.delete();
            }
        }
        ParserWriter.writeGroupsFile(groups);
        libraries = new Library[0];
    }

    /**
     * Gets all groups that contain the library specified.
     * @param libraryID the ID of the library to search for.
     * @return String array of all groups that contain the library with the specified ID.
     */
    public static String[] getGroupsThatContain(String libraryID) {
        String[] retVal = new String[0];
        String[] temp;

        //look through each group
        for (int i = 0; i < groups.length; i++) {
            //if the library exists in this group, then add the group name to the
            //return value
            if (groups[i].containsLibrary(libraryID)) {
                temp = new String[retVal.length + 1];
                System.arraycopy(retVal, 0, temp, 0, retVal.length);
                temp[retVal.length] = groups[i].getGroupName();
                retVal = temp;
            }
        }

        return retVal;
    }

    /**
     * Writes the groups file encapsulated by IOManager to file. This simply calls <br>
     * <br><code>ParserWriter.writeGroupsFile(groups);</code>
     * <br>where <code>groups</code> is a private variable, the <code>Group[]</code> array.
     */
    public static void writeGroupsFile() {
        ParserWriter.writeGroupsFile(groups);
    }

    /**
     * Gets the path for the library files relative to the user home directory path (or custom path). Has a trailing '/' attached.
     * @return the path of the directory where the library files are saved.
     */
    public static String getLibraryPath() {
        return userHomePath + LIBRARY_PATH;
    }

    /**
     * Gets the path for the answer field classes relative to the user home directory path (or custom path). Has a trailing '/' attached.
     * @return the path of the directory where the answer field classes are stored.
     */
    public static String getAnswerFieldPath() {
        return userHomePath + ANSWER_FIELD_PATH;
    }

    /**
     * Gets the path for the clip art collections relative to the user home directory path (or custom path). Has a trailing '/' attached.
     * @return the path of the directory where the clip art collections are stored.
     */
    public static String getCollectionsPath() {
        return userHomePath + COLLECTIONS_PATH;
    }

    /**
     * Gets the path for custom SketchEl templates relative to the user home directory path (or custom path). Has a trailing '/' attached.
     * @return the path of the directory where custom SketchEl templates are stored.
     */
    public static String getChemTemplatesPath() {
        return userHomePath + CHEM_TEMPLATES_DIRECTORY;
    }

    /**
     * Gets the URLClassLoader which has the user's home directory (or custom set path) as a classpath. The answer
     * field plug-in classes are located here, and this provides a means by which to load them.
     * @return the URLClassLoader
     */
    public static URLClassLoader getUrlClassLoader() {
        return ansFieldClassLoader;
    }

    /**
     * Gets the path for the answer field file (absolute).
     * @return the path for the answer field file which describes which fields have been added, their defaults, etc.
     */
    public static String getAnswerFieldFile() {
        return userHomePath + ANSWER_FIELD_FILE;
    }

    /**
     * Gets the path for the quiz history file (absolute).
     * @return the path for the quiz historty file.
     */
    public static String getQuizHistoryFilePath() {
        return userHomePath + QUIZ_HISTORY_FILE;
    }

    /**
     * Gets the path for the preferences file (absolute). This file contains general preferences and describes the map of symbol menu characters to
     * initiator characters. That is, the symbols that are shown in the symbol menu for each different key that can
     * be pressed after pressing Ctrl+space.
     * @return the path for the path for the symbol menu file (absolute).
     */
    public static String getPreferencesFile() {
        return userHomePath + PREFERENCES_FILE;
    }

    /**
     * Gets the path for the groups file (absolute).
     * @return the path of the groups file which defines all groups and provides a list of all libraries and library ids.
     */
    public static String getGroupsFile() {
        return userHomePath + GROUPS_FILE;
    }

    /**
     * Gets the path into which library files are temporarily extracted when they are opened (absolute).
     * @return the path into which library files are temporarily extracted when they are opened.
     */
    public static String getTempPath() {
        return userHomePath + TEMP_LIBRARY_PATH;
    }

    /**
     * Checks whether or not the specified group name is unique, or whether it is already in use.
     * @param groupName the group name to check.
     * @return true if the group name specified is unique.
     */
    public static boolean isGroupNameUnique(String groupName) {
        for (int i = 0; i < groups.length; i++) {
            if (groups[i].getGroupName().equals(groupName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a new group to the group list.
     * @param groupName the name of the group to create.
     * @param libIDs the library IDs of libraries to be included within this new group.
     */
    public static void createGroup(String groupName, String[] libIDs) {
        if (isGroupNameUnique(groupName) == false) {
            System.out.println("\n----->Could not create group " + groupName + ", this group already exists.");
            return;
        }
        Group[] temp = new Group[groups.length + 1];
        System.arraycopy(groups, 0, temp, 0, groups.length);
        String[] libNames = new String[libIDs.length];

        for (int i = 0; i < libIDs.length; i++) {
            libNames[i] = getLibraryName(libIDs[i]);
        }

        temp[groups.length] = new Group(groupName, libNames, libIDs);
        groups = temp;

        ParserWriter.writeGroupsFile(groups);
    }

    /**
     * Delete the group with the specified name, and write this change to file.
     * @param groupName the name of the group to delete.
     */
    public static void deleteGroup(String groupName) {
        int index = -1;
        Group[] temp = new Group[groups.length - 1];
        //find the index of the group to remove
        for (int i = 0; i < groups.length; i++) {
            if (groups[i].getGroupName().equals(groupName)) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            System.out.println("\n----->Could not delete group " + groupName + "; it could not be found.");
            return;
        }

        //if it is the first group, then just copy after the group
        if (index == 0) {
            System.arraycopy(groups, 1, temp, 0, temp.length);
        } //if it is the last group, then just copy until that point
        else if (index == groups.length - 1) {
            System.arraycopy(groups, 0, temp, 0, temp.length);
        } //otherwise...
        else {
            System.arraycopy(groups, 0, temp, 0, index);
            System.arraycopy(groups, index + 1, temp, index, groups.length - index - 1);
        }

        groups = temp;
        ParserWriter.writeGroupsFile(groups);
    }

    /**
     * Rename a group, and save the change.
     * @param currentName the group to rename.
     * @param newName the new name for the group.
     */
    public static void renameGroup(String currentName, String newName) {
        if (isGroupNameUnique(newName) == false) {
            System.out.println("\n----->Could not rename group from " + currentName + " to " + newName + " as the new name is not unique.");
            return;
        }
        getGroup(currentName).setGroupName(newName);
        ParserWriter.writeGroupsFile(groups);
    }

    /**
     * Create a new library and optionally add it to the specified group.
     * @param name the name of the new library.
     * @param description the descrscription of the new library.
     * @param groupName the group to add the library to, or <code>null</code> if the library should not
     * be added to a group.
     */
    public static void createLibrary(String name, String description, String groupName) throws IOException {
        String id = generateLibraryID(name);
        File tempPath = new File(getTempPath() + id);
        tempPath.mkdir();
        //create Library object
        Library lib = new Library(name, id, description, new Date(), new IQuestion[0], tempPath, new File(getTempPath() + id + "/" + id), new ArrayList<HistoryEntry>(), new Hashtable<String, File>());
        ParserWriter.writeLibraryFile(lib, true);

        //update libraries array
        Library[] temp = new Library[libraries.length + 1];
        System.arraycopy(libraries, 0, temp, 0, libraries.length);
        temp[libraries.length] = lib;
        libraries = temp;

        //update library IDs array
        String[] strTemp = new String[libraryIDs.length + 1];
        System.arraycopy(libraryIDs, 0, strTemp, 0, libraryIDs.length);
        strTemp[libraryIDs.length] = id;
        libraryIDs = strTemp;

        //update library names array
        strTemp = new String[libraryNames.length + 1];
        System.arraycopy(libraryNames, 0, strTemp, 0, libraryNames.length);
        strTemp[libraryNames.length] = name;
        libraryNames = strTemp;


        //add the library to a group, if needed
        if (groupName.compareTo("- All Libraries -") != 0) {
            String[] groupLibIDs = getGroup(groupName).getLibraryIDs();
            strTemp = new String[groupLibIDs.length + 1];
            System.arraycopy(groupLibIDs, 0, strTemp, 0, groupLibIDs.length);
            strTemp[groupLibIDs.length] = id;
            getGroup(groupName).setLibraries(strTemp);
        }

        ParserWriter.writeGroupsFile(groups);

        packageLibrary(id);
    }

    /**
     * Import the specified library file, and add it to the specified group. If the
     * library should not be added to any group, then pass the <code>group</code> parameter
     * as <code>null</code>.
     * @param libraryToImport the file to import.
     * @param groupName the group to add the library to, or <code>null</code> if the library
     * should not be added to any group.
     */
    public static void importLibrary(File libraryToImport, String groupName) throws IOException {
        String libID = libraryToImport.getName();
        Library lib;
        //used to edit the library before import, if required
        File tempPath = null;

        if (isLibraryIDUnique(libID) == false) {
            //generate a unique ID, and then unpack the library to that location so the library data file can
            //be renamed, and the ID within that file changed.
            String oldLibID = libID;
            libID = generateLibraryID(libID);
            tempPath = new File(getTempPath() + libID);
            tempPath.mkdir();
            ZipTools.unzip(libraryToImport.getAbsolutePath(), getTempPath() + libID + "/");

            //rename the library data file so that it has the new ID.
            new File(tempPath.getAbsolutePath() + "/" + oldLibID).renameTo(new File(tempPath.getAbsolutePath() + "/" + libID));

            try {
                lib = ParserWriter.parseLibraryFile(new File(getTempPath() + libID + "/" + libID));
            } catch (DataConversionException ex) {
                ex.printStackTrace();
                throw new IOException("DataConversionException while attempting to load a library.");
            }

            lib.setId(libID);

            //update libraries array
            Library[] temp = new Library[libraries.length + 1];
            System.arraycopy(libraries, 0, temp, 0, libraries.length);
            temp[libraries.length] = lib;
            libraries = temp;

            ParserWriter.writeLibraryFile(lib, true);

            packageLibrary(libID);

        } else {
            copy(libraryToImport.getAbsolutePath(), getLibraryPath() + libID);
            lib = loadLibrary(libID);
        }

        //update library IDs array
        String[] strTemp = new String[libraryIDs.length + 1];
        System.arraycopy(libraryIDs, 0, strTemp, 0, libraryIDs.length);
        strTemp[libraryIDs.length] = libID;
        libraryIDs = strTemp;

        //update library names array
        strTemp = new String[libraryNames.length + 1];
        System.arraycopy(libraryNames, 0, strTemp, 0, libraryNames.length);
        strTemp[libraryNames.length] = lib.getName();
        libraryNames = strTemp;

        //add the library to a group, if needed
        if (groupName != null) {
            String[] groupLibIDs = getGroup(groupName).getLibraryIDs();
            strTemp = new String[groupLibIDs.length + 1];
            System.arraycopy(groupLibIDs, 0, strTemp, 0, groupLibIDs.length);
            strTemp[groupLibIDs.length] = libID;
            getGroup(groupName).setLibraries(strTemp);
        }

        ParserWriter.writeGroupsFile(groups);

    }

    /**
     * Delete the specified library and all the questions that it contains.
     * @param libID the ID of the library to delete.
     */
    public static void deleteLibrary(String libID) {
        //search each group, and remove the ID from the arrays (group library names arrays auto update by setLibraries() method).
        String[] ids;
        String[] temp;
        int index = -1;
        for (int i = 0; i < groups.length; i++) {
            ids = groups[i].getLibraryIDs();
            for (int j = 0; j < ids.length; j++) {
                if (ids[j].equals(libID)) {
                    //we found a group that contains the libraryID
                    //the group is at j in the libraryID array
                    temp = removeIndexFromArray(j, ids);
                    groups[i].setLibraries(temp);
                    break;
                }
            }
        }

        //remove the id and name from the IOManager arrays
        //find index
        for (int i = 0; i < libraryIDs.length; i++) {
            if (libraryIDs[i].equals(libID)) {
                index = i;
                break;
            }
        }
        //remove from the IDs and name arrays of the IOManager
        libraryIDs = removeIndexFromArray(index, libraryIDs);
        libraryNames = removeIndexFromArray(index, libraryNames);

        //remove it from the Libraries[] array of the IOManager
        for (int i = 0; i < libraries.length; i++) {
            if (libraries[i].getId().equals(libID)) {
                index = i;
                break;
            }
        }
        libraries = removeIndexFromArray(index, libraries);

        //ensure that the temporary directory has been deleted
        File f = new File(getTempPath() + libID);
        if (f.exists()) {
            //must delete the temporary path and all files within it
            File[] files = f.listFiles();
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }
            f.delete();
        }

        //delete the zip file from the libraries directory
        f = new File(getLibraryPath() + libID);
        if (f.exists()) {
            f.delete();
        }

        //write the groups file out so that it updates all of these removals
        ParserWriter.writeGroupsFile(groups);
    }

    /**
     * Edits the name and description values of the specified library, and provides
     * the option to reset the statistics for all questions contained by the library. If
     * no change is desired to either/both name and description fields, then these parameters
     * can be <code>null</code>. If the specified library is not found, then no change will be made.
     *
     * @param libID the ID of the library that should be edited.
     * @param newName the new name for this library, or <code>null</code> if no change is desired.
     * @param newDescription the new description for this library, or <code>null</code> if no change is desired.
     * @param resetCorrectness <code>true</code> if all statistics should be cleared for all questions contained by this library.
     * @throws IOException if something goes wrong while packaging the library
     */
    public static void editLibrary(String libID, String newName, String newDescription, boolean resetCorrectness) throws IOException {
        Library lib;
        //is library loaded? if not, load it.
        if (isLibraryLoaded(libID)) {
            lib = loadLibrary(libID);
        } else {
            lib = getLibraryFromID(libID);
        }

        //change name
        if (newName != null) {
            if (newName.isEmpty()) {
                System.out.println("\n-----> Cannot rename library as empty string.");
            } else {
                lib.setName(newName);
                libraryNames[getLibraryIDIndex(libID)] = newName;
            }
        }

        //change description
        if (newDescription != null) {
            lib.setDescription(newDescription);
        }

        //clear statistics data, if requested
        if (resetCorrectness) {
            IQuestion[] questions = lib.getQuestions();
            for (int i = 0; i < questions.length; i++) {
                if (questions[i] instanceof FlexiQuestion) {
                    ((FlexiQuestion) questions[i]).setMarksAvailable(0);
                    ((FlexiQuestion) questions[i]).setMarksAwarded(0);
                    ((FlexiQuestion) questions[i]).setTimesAsked(0);
                } else if (questions[i] instanceof TableQuestion) {
                    Arrays.fill(((TableQuestion) questions[i]).getMarksAvailable(), 0);
                    Arrays.fill(((TableQuestion) questions[i]).getMarksAwarded(), 0);
                    Arrays.fill(((TableQuestion) questions[i]).getTimesAsked(), 0);
                }
            }
            //clear the library specific histories too
            lib.clearQuizHistory();
        }

        //write the file to disk
        ParserWriter.writeLibraryFile(lib, true);
        packageLibrary(libID);

        ParserWriter.writeGroupsFile(groups);
    }

    /**
     * Removes the specified index from the array.
     * @param index the index of the element to remove.
     * @param array the array from which the element should be removed.
     * @return the specified array with the element at <code>index</code> removed.
     */
    public static String[] removeIndexFromArray(int index, String[] array) {
        if (index < 0) {
            return null;
        }
        if (index >= array.length) {
            return null;
        }

        if (array.length == 1) {
            return new String[0];
        }

        String[] temp = new String[array.length - 1];
        //if it is the first library, then just copy after the group
        if (index == 0) {
            System.arraycopy(array, 1, temp, 0, temp.length);
        } //if it is the last library, then just copy until that point
        else if (index == array.length - 1) {
            System.arraycopy(array, 0, temp, 0, temp.length);
        } //otherwise...
        else {
            System.arraycopy(array, 0, temp, 0, index);
            System.arraycopy(array, index + 1, temp, index, array.length - index - 1);
        }

        return temp;
    }

    /**
     * Removes the specified item from the array.
     * @param item the item to remove
     * @param array the array from which the item should be removed.
     * @return the specified array with the item removed; <code>null</code> if the item cannot be found.
     */
    public static String[] removeItemFromArray(String item, String[] array) {
        int index = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(item)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return null;
        } else {
            return removeIndexFromArray(index, array);
        }
    }

    /**
     * Add the specified item to the end of a copy of the specified array, and return that copy.
     * @param item the item to add.
     * @param array the array to copy and append.
     * @return the array with the item added to the end.
     */
    public static String[] appendToArray(String item, String[] array) {
        String[] temp = new String[array.length + 1];
        System.arraycopy(array, 0, temp, 0, array.length);
        temp[array.length] = item;
        return temp;
    }

    /**
     * Removes the specified index from the array.
     * @param index the index of the element to remove.
     * @param array the array from which the element should be removed.
     * @return the specified array with the element at <code>index</code> removed.
     */
    public static Library[] removeIndexFromArray(int index, Library[] array) {
        if (index < 0) {
            return null;
        }
        if (index >= array.length) {
            return null;
        }

        if (array.length == 1) {
            return new Library[0];
        }

        Library[] temp = new Library[array.length - 1];
        //if it is the first library, then just copy after the group
        if (index == 0) {
            System.arraycopy(array, 1, temp, 0, temp.length);
        } //if it is the last library, then just copy until that point
        else if (index == array.length - 1) {
            System.arraycopy(array, 0, temp, 0, temp.length);
        } //otherwise...
        else {
            System.arraycopy(array, 0, temp, 0, index);
            System.arraycopy(array, index + 1, temp, index, array.length - index - 1);
        }

        return temp;
    }

    /**
     * Removes the specified index from the array.
     * @param index the index of the element to remove.
     * @param array the array from which the element should be removed.
     * @return the specified array with the element at <code>index</code> removed.
     */
    public static int[] removeIndexFromArray(int index, int[] array) {
        if (index < 0) {
            return null;
        }
        if (index >= array.length) {
            return null;
        }

        if (array.length == 1) {
            return new int[0];
        }

        int[] temp = new int[array.length - 1];
        //if it is the first library, then just copy after the group
        if (index == 0) {
            System.arraycopy(array, 1, temp, 0, temp.length);
        } //if it is the last library, then just copy until that point
        else if (index == array.length - 1) {
            System.arraycopy(array, 0, temp, 0, temp.length);
        } //otherwise...
        else {
            System.arraycopy(array, 0, temp, 0, index);
            System.arraycopy(array, index + 1, temp, index, array.length - index - 1);
        }

        return temp;
    }

    /**
     * Copy file.
     * @param fromFileName file to copy
     * @param toFileName where to copy it to
     * @throws IOException if there is a problem
     */
    public static void copy(String fromFileName, String toFileName)
            throws IOException {
        File fromFile = new File(fromFileName);
        File toFile = new File(toFileName);

        if (!fromFile.exists()) {
            return;
        }
        if (!fromFile.isFile()) {
            return;
        }
        if (!fromFile.canRead()) {
            return;
        }

        if (toFile.isDirectory()) {
            toFile = new File(toFile, fromFile.getName());
        }

        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead); // write
            }
        } finally {
            if (from != null) {
                try {
                    from.close();
                } catch (IOException e) {
                }
            }
            if (to != null) {
                try {
                    to.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Save the library with the specified ID to a zip file with all resources bundled in.
     * @param libID the library ID of the library to package.
     * @throws IOException upon an IO problem while reading from the temporary directory or writing to the destination.
     */
    public static void packageLibrary(String libID) throws IOException {
        if (isLibraryLoaded(libID) == false) {
            System.out.println("\n-----> Did not save library with ID '" + libID + "' because it was not loaded.");
            return;
        }

        //CLEAN OUT ANY UNUSED PICTURES
        //    -get a list of all filenames within the temp folder
        //    -delete any file whose name does not appear within the library file
        //     and whose name is not the library ID.
        //    -this method will fail if the user has included any image file's ID
        //     as text within the library (unlikely).
        //    -cannot do this by searching for [IMG] tags, as answer fields may have
        //     images too, and they are not bound to using the same method of serialisation.
        File[] filesInLibrary = getLibraryFromID(libID).getPathTempLib().listFiles();
        String libString = ParserWriter.writeLibraryToString(getLibraryFromID(libID));
        if (filesInLibrary != null) {
            for (int i = 0; i < filesInLibrary.length; i++) {
                if ((filesInLibrary[i].getName().equals(libID) == false) && (libString.contains(filesInLibrary[i].getName()) == false)) {
                    //this file is not the library file, and it is not contained within the library string representation
                    //so delete it
                    if (filesInLibrary[i].delete() == false) {
                        System.out.println("\n-----> could not clean up unused image '" + filesInLibrary[i].getName() + "' from library '" + libID + "' before repacking.");
                    }
                }
            }
        }
        ZipTools.createZip(getLibraryFromID(libID).getPathTempLib().listFiles(), new File(getLibraryPath() + libID));
    }

    /**
     * Checks whether or not the specified library ID is unique.
     * @param libID the library ID to test.
     * @return true if the library ID specified is unique.
     */
    public static boolean isLibraryIDUnique(String libID) {
        for (int i = 0; i < libraryIDs.length; i++) {
            if (libraryIDs[i].equals(libID)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Keeps altering the base until it is unique. Will not return a string of greater
     * length than 15.
     * @param base usually the library name.
     * @return a unique library ID equal to or less than 15 characters in length.
     */
    public static String generateLibraryID(String base) {

        //ensure there are no illegal characters (/,?,:,...etc.)
        base = getSafeID(base);

        if (base.length() > 15) {
            base = base.substring(0, 15);
        }

        int index = 0;
        while (isLibraryIDUnique(base) == false) {
            base = base.substring(0, base.length() - ("" + index).length());
            base += index;
            index++;
        }

        return base;
    }

    /**
     * Gets the path from which this jar is executing. Does not include a trailing '/', so
     * this will need to be added.
     * @return the path from which this jar is executing.
     */
    public static String getUserHomePath() {
        return userHomePath;
    }

    /**
     * Gets the name of the library with ID <code>libraryID</code>.
     * @param libraryID the ID of the library whose name will be returned
     * @return the name of the library with the specified ID or "" if the library can't be found.
     */
    public static String getLibraryName(String libraryID) {
        //find index of the library id in the array
        int index = -1;
        for (int i = 0; i < libraryIDs.length; i++) {
            if (libraryIDs[i].equals(libraryID)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return "";
        } else {
            return libraryNames[index];
        }

    }

    /**
     * Checks whether or not the library is already loaded, and if not, loads the
     * library. Returns the library. If the library is already loaded, then this
     * is equivalent to the <code>getLibraryFromID</code> method.
     * @return the library that has been loaded.
     */
    public static Library loadLibrary(String libraryID) throws IOException {
        //if the library is already loaded, then there is no need to load it again
        if (isLibraryLoaded(libraryID) == true) {
            return getLibraryFromID(libraryID);
        }

        //create the temporary directory (should not exist if the library is not loaded)
        new File(getTempPath() + libraryID).mkdir();
        //extract library into temporary directory
        ZipTools.unzip(getLibraryPath() + libraryID, getTempPath() + libraryID + "/");

        //read in the library data
        Library lib;
        try {
            lib = ParserWriter.parseLibraryFile(new File(getTempPath() + libraryID + "/" + libraryID));
        } catch (DataConversionException ex) {
            ex.printStackTrace();
            throw new IOException("DataConversionException while attempting to load a library.");
        }

        //add this library to the IOManager's library array
        Library[] temp = new Library[libraries.length + 1];
        System.arraycopy(libraries, 0, temp, 0, libraries.length);
        temp[libraries.length] = lib;
        libraries = temp;

        return lib;
    }

    /**
     * Gets the library from the ID if the library is currently loaded,
     * loads the library first if needed.
     * @param libraryID
     * @return the library corresponding to this library ID, or null if the library
     * cannot be loaded for some reason.
     * @throws IOException if there is a problem loading this library (if it was not already loaded)
     */
    public static Library getLibraryFromID(String libraryID) throws IOException {
        if (isLibraryLoaded(libraryID) == false) {
            return loadLibrary(libraryID);
        }

        for (int i = 0; i < libraries.length; i++) {
            if (libraries[i].getId().equals(libraryID)) {
                return libraries[i];
            }
        }
        return null;
    }

    public static int getLibraryIDIndex(String libraryID) {
        for (int i = 0; i < libraryIDs.length; i++) {
            if (libraryIDs[i].equals(libraryID)) {
                return i;
            }
        }
        //couldn't find the library, eep.
        return -1;
    }

    public static boolean isLibraryLoaded(String libraryID) {
        for (int i = 0; i < libraries.length; i++) {
            //library IDs are unique
            if (libraries[i].getId().equals(libraryID)) {
                return true;
            }
        }
        //the library with that ID was not found in the loaded library array, so it is not open.
        return false;
    }

    /**
     * Attempts to return the group with the specified group name, if the group
     * cannot be found, method returns null.
     * @param groupName the name of the group that should be returned
     * @return the group with the specified name, or null if that group cannot be found.
     */
    public static Group getGroup(String groupName) {
        for (int i = 0; i < groups.length; i++) {
            if (groups[i].getGroupName().equals(groupName)) {
                return groups[i];
            }
        }
        return null;
    }

    /**
     * Gets the answer field class associated with the specified classID.
     * @param classID the class ID of the answer field that is required.
     * @return the answer field class associated with the specified classID, or null if the answer field cannot be found.
     */
    public static Class getAnswerFieldClass(String classID) {
        return (Class) answerFieldsFile.getAnswerFields().get(classID);
    }

    /**
     * Gets the default value text for the answer field class associated with the specified
     * classID. This text is interpretted entirely by the particular answer field implementation it is passed to.
     * @param classID the class ID of the answer field for which the defaults text is required.
     * @return the default value text for the answer field class associated with the specified class ID.
     */
    public static String getAnswerFieldDefault(String classID) {
        return (String) answerFieldsFile.getAnswerFieldDefaults().get(classID);
    }

    /**
     * Gets an enumeration of all classIDs for answer fields.
     * @return enumeration of all classIDs of answer fields.
     */
    public static Enumeration getAnswerFieldClassIDs() {
        return answerFieldsFile.getAnswerFieldClassIDs();
    }

    /**
     * Get the object encapsulating data held in the Answer Fields file. This object
     * contains a list of all added answer fields and their defaults.
     * @return the answer fields file.
     */
    public static AnswerFieldsFile getAnswerFieldsFile() {
        return answerFieldsFile;
    }

    /**
     * Gets a list of all library names.
     * @return an array of all library names.
     */
    public static String[] getLibraryNames() {
        return libraryNames;
    }

    /**
     * Gets a list of all library IDs.
     * @return an array of all library IDs.
     */
    public static String[] getLibraryIDs() {
        return libraryIDs;
    }

    /**
     * A list of all the names of groups that have been created.
     * @return an array of all the names of groups that have been created.
     */
    public static String[] getGroupList() {
        String[] retVal = new String[groups.length];
        for (int i = 0; i < groups.length; i++) {
            retVal[i] = groups[i].getGroupName();
        }

        return retVal;
    }

    /**
     * Convenience method for getting the group list with a 'None' entry at the
     * beginning. This can be used where the user is prompted to select a group, but may
     * also select 'None'. Equivalent to adding a \"None\" element to the beginning
     * of the array resulting from <code>getGroupList()</code>.
     * @return the list of all group names with an additional element called 'None' at index 0.
     */
    public static String[] getGroupListWithNoneOption() {
        String[] retVal = new String[groups.length + 1];
        retVal[0] = "None";
        for (int i = 0; i < groups.length; i++) {
            retVal[i + 1] = groups[i].getGroupName();
        }

        return retVal;
    }

    /**
     * Saves a <code>BufferedImage</code> to the specified library, with the
     * specified imageID. If the imageID is not unique within the specified library,
     * then it will be altered by appending a number to the end. Any imageID of more
     * than MAX_IMAGE_ID_LENGTH characters will be shortened to MAX_IMAGE_ID_LENGTH characters.
     * @param libID the library to which the image should be saved
     * @param image the image to save
     * @param imageID the imageID to use. This is altered if not unique for the specified
     *        library. Also, this may be left blank ("") if an imageID should be automatically
     *        generated.
     * @return String the unique imageID that this image has been assigned. This may
     *         be different to the specified ID value, as the specified value may not
     *         have been unique. Returns an empty string if the operation was unsuccessful.
     * @throws IOException if there is a problem writing the image.
     */
    public static String saveImage(String libID, BufferedImage image, String imageID) throws IOException {
        Library lib = getLibraryFromID(libID);
        if (lib == null) {
            throw new IOException("The specified library could not be loaded.");
        }

        //ensure there are no illegal characters (/,?,:,...etc.)
        imageID = getSafeID(imageID);

        //get the temporary path that this library has been extracted into
        File libTempPath = lib.getPathTempLib();

        //check that the imageID is a legal length
        if (imageID.length() > MAX_IMAGE_ID_LENGTH) {
            imageID = imageID.substring(0, MAX_IMAGE_ID_LENGTH - 2);
        }

        //ensure that imageID is unique for this library
        File newFile = new File(libTempPath.getAbsolutePath() + "/" + imageID);

        int suffix = 0;
        if (newFile.exists()) {
            do {
                imageID = imageID.substring(0, imageID.length() - ("" + suffix).length());
                imageID += suffix;
                newFile = new File(libTempPath.getAbsolutePath() + "/" + imageID);
                suffix += 1; //increment the suffix

            } while (newFile.exists());
        }

        ImageIO.write(image, "png", newFile);


        lib.getImages().put(imageID, newFile);
        return imageID;
    }

    /**
     * Saves the specified file as an image to the specified library. A unique imageID of
     * no more than 15 characters is automatically generated from the given image
     * filename. This method loads the specified image and calls saveImage(Library,
     * BufferedImage,String).
     * @param libID the library to which this image should be saved.
     * @param imageFileName the image to save to the specified library.
     * @return String the unique imageID that this image has been assigned. Returns
     *         an empty String if the operation has been unsuccessful.
     * @throws IOException if there is a problem writing the image.
     */
    public static String saveImage(String libID, String imageFileName) throws IOException {
        //try to load the image.
        BufferedImage imageIn = null;
        try {
            imageIn = ImageIO.read(new File(imageFileName));
        } catch (Exception evt) {
            Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, "While attempting to load the image: " + imageFileName, evt);
            return "";
        } catch (OutOfMemoryError evt) {
            Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, "While attempting to load the image: " + imageFileName, evt);
            return "";
        }

        //if the image is null, then return nothing
        if (imageIn == null) {
            return "";
        }

        return saveImage(libID, imageIn, new File(imageFileName).getName());
    }

    /**
     * Saves the image with the specified ID to the specified library without ensuring that the ID
     * is unique within that library. This means that if any resource exists with that ID, it will
     * be overwritten. This method still ensures that the ID is no larger than MAX_IMAGE_ID_LENGTH.
     * @param img the image to write.
     * @param libID the ID of the library to which the image should be written.
     * @param imageID the ID that the image should take.
     */
    public static void saveImageWithOverWrite(BufferedImage img, String libID, String imageID) throws IOException {
        Library lib = getLibraryFromID(libID);
        if (lib == null) {
            throw new IOException("The specified library could not be loaded.");
        }

        //ensure there are no illegal characters (/,?,:,...etc.)
        imageID = getSafeID(imageID);

        //get the temporary path that this library has been extracted into
        File libTempPath = lib.getPathTempLib();
        File newFile = new File(libTempPath.getAbsolutePath() + "/" + imageID);

        //check that the imageID is a legal length
        if (imageID.length() > MAX_IMAGE_ID_LENGTH) {
            imageID = imageID.substring(0, MAX_IMAGE_ID_LENGTH - 2);
        }

        ImageIO.write(img, "png", newFile);


        lib.getImages().put(imageID, newFile);
    }

    /**
     * Loads the image with the specified ID from the specified library.
     * @param libID the library from which to load the image.
     * @param imageID the ID of the image that should be loaded.
     * @return the requested image from library <code>lib</code> with ID <code>imageID</code>.
     * @throws IOException if there is a problem loading the image.
     */
    public static BufferedImage loadImage(String libID, String imageID) throws IOException {
        Library lib = getLibraryFromID(libID);
        if (lib == null) {
            throw new IOException("The specified library is not/cannot be loaded.");
        }
        //try to load the image.
        BufferedImage retVal = null;
        File imgFile = (File) lib.getImages().get(imageID);

        if (imgFile == null) {
            //this likely is an answer field requesting an image be loaded, as the ID cannot be found in the library listing
            imgFile = new File(lib.getPathTempLib() + "/" + imageID);
        }

        retVal = ImageIO.read(imgFile);
        return retVal;
    }

    /**
     * Loads the file with the specified ID from the specified library. This allows for non-image
     * files to be saved and loaded. This opens the possibility for answer fields that use audio, for
     * example.
     * Note: has not been well tested.
     * @param libID the library from which to load the file.
     * @param fileID the ID of the file that should be loaded.
     * @return the input stream for the requested file.
     * @throws IOException if there is a problem loading the file.
     */
    public static FileInputStream loadResource(String libID, String fileID) throws IOException {
        Library lib = getLibraryFromID(libID);
        if (lib == null) {
            throw new IOException("The specified library is not/cannot be loaded.");
        }

        File fileToLoad = (File) lib.getImages().get(fileID);

        if (fileToLoad == null) {
            //this likely is an answer field requesting a resource be loaded, as the ID cannot be found in the library listing
            fileToLoad = new File(lib.getPathTempLib() + "/" + fileID);
        }

        return new FileInputStream(fileToLoad);
    }

    /**
     * Saves the specified file to the specified library. A unique fileID of
     * no more than 15 characters is automatically generated from the given
     * filename. This method copies the specified file to the specified library.
     * Note: has only been marginally tested.
     * @param libID the library to which this file should be saved.
     * @param filename the file to save to the specified library.
     * @return String the unique fileID that this file has been assigned. Returns
     *         an empty String if the operation has been unsuccessful.
     * @throws IOException if there is a problem writing the file.
     */
    public static String saveResource(String libID, String filename) throws IOException {
        File fileIn = new File(filename);

        //if the file is null, then return nothing
        if (fileIn == null) {
            return "";
        }

        Library lib = getLibraryFromID(libID);
        if (lib == null) {
            throw new IOException("The specified library could not be loaded.");
        }

        //get the temporary path that this library has been extracted into
        File libTempPath = lib.getPathTempLib();

        String fileID = fileIn.getName();
        //ensure there are no illegal characters (/,?,:,...etc.)
        fileID = getSafeID(fileID);

        //check that the fileID is a legal length
        if (fileID.length() > MAX_IMAGE_ID_LENGTH) {
            fileID = fileID.substring(0, MAX_IMAGE_ID_LENGTH - 2);
        }

        //ensure that fileID is unique for this library
        File newFile = new File(libTempPath.getAbsolutePath() + "/" + fileID);
        int suffix = 0;
        if (newFile.exists()) {
            do {
                fileID = fileID.substring(0, fileID.length() - ("" + suffix).length());
                fileID += suffix;
                newFile = new File(libTempPath.getAbsolutePath() + "/" + fileID);
                suffix += 1; //increment the suffix
            } while (newFile.exists());
        }

        copy(filename, newFile.getAbsolutePath());

        return fileID;
    }

    /**
     * Copies the specified resource to the specified destination library.
     * @param sourceLibraryID The libraryID in which the resource currently exists.
     * @param sourceID the ID of the resource as it exists within the source library.
     * @param destLibraryID the ID of the library to which the resource should be copied.
     * @return the new ID for the destination resource. This is likely to be the same as <code>sourceID</code>,
     * unless <code>sourceID</code> was not unique in the destination library.
     */
    public static String copyResource(String sourceLibraryID, String sourceID, String destLibraryID) {
        Library destLib = null;
        Library sourceLib = null;
        try {
            destLib = loadLibrary(destLibraryID);
            sourceLib = loadLibrary(sourceLibraryID);
        } catch (IOException ex) {
            Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, "While attempting to load the source and destination libraries in order to copy a resource.\n"
                    + "sourceLibID=" + sourceLibraryID + " :: destLibID=" + destLibraryID, ex);
        }

        //ensure id has no illegal characters
        String destID = getSafeID(sourceID);
        //ensure that fileID is unique for this library
        File newFile = new File(destLib.getPathTempLib() + "/" + destID);
        int suffix = 0;
        if (newFile.exists()) {
            do {
                destID = destID.substring(0, destID.length() - ("" + suffix).length());
                destID += suffix;
                newFile = new File(destLib.getPathTempLib() + "/" + destID);
                suffix += 1; //increment the suffix
            } while (newFile.exists());
        }
        try {
            copy(sourceLib.getPathTempLib() + "/" + sourceID, destLib.getPathTempLib() + "/" + destID);
        } catch (IOException ex) {
            Logger.getLogger(IOManager.class.getName()).log(Level.SEVERE, "While attempting to copy a file between libraries.\n"
                    + "sourceLibID=" + sourceLibraryID + " :: destLibID=" + destLibraryID + " :: derived destID=" + destID, ex);
        }

        return destID;
    }

    /**
     * Copies the specified image to the specified destination library. This method is just included for
     * consistency, and is actually the same as calling <code>copyResource</code>.
     * @param sourceLibraryID The libraryID in which the image currently exists.
     * @param sourceID the ID of the image as it exists within the source library.
     * @param destLibraryID the ID of the library to which the image should be copied.
     * @return the new ID for the destination image. This is likely to be the same as <code>sourceID</code>,
     * unless <code>sourceID</code> was not unique in the destination library.
     */
    public static String copyImage(String sourceLibraryID, String sourceID, String destLibraryID) {
        return copyResource(sourceLibraryID, sourceID, destLibraryID);
    }

    /**
     * Saves the file with the specified ID to the specified library without ensuring that the ID
     * is unique within that library. This means that if any resource exists with that ID, it will
     * be overwritten. This method still ensures that the ID is no larger than MAX_IMAGE_ID_LENGTH.
     * Note: has not been well tested.
     * @param filename the file to write.
     * @param libID the ID of the library to which the file should be written.
     * @param fileID the ID that the file should take.
     */
    public static void saveResourceWithOverWrite(String filename, String libID, String fileID) throws IOException {
        Library lib = getLibraryFromID(libID);
        if (lib == null) {
            throw new IOException("The specified library could not be loaded.");
        }

        //get the temporary path that this library has been extracted into
        File libTempPath = lib.getPathTempLib();
        File newFile = new File(libTempPath.getAbsolutePath() + "/" + fileID);

        //check that the imageID is a legal length
        if (fileID.length() > MAX_IMAGE_ID_LENGTH) {
            fileID = fileID.substring(0, MAX_IMAGE_ID_LENGTH - 2);
        }

        copy(filename, newFile.getAbsolutePath());
    }

    public static String getSafeID(String id) {
        char[] illegalChars = new char[]{'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};
        for (int i = 0; i < illegalChars.length; i++) {
            id = id.replace(illegalChars[i], 'A');
        }

        return id;
    }
}

