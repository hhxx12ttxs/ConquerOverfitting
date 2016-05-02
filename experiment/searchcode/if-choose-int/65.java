/*
 * ImageAcquisitionDialog.java
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
package org.ingatan.component.image;

import be.ugent.caagt.jmathtex.ParseException;
import be.ugent.caagt.jmathtex.TeXConstants;
import be.ugent.caagt.jmathtex.TeXFormula;
import java.awt.Color;
import org.ingatan.component.FileChooserPreviewPane;
import org.ingatan.component.IconFileView;
import org.ingatan.component.PaintedJPanel;
import org.ingatan.io.IOManager;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.sf.sketchel.MainPanel;

/**
 * Extension of the JFrame that ties together all image acquisition methods
 * into a menu. The image acquisition options are:
 * <ul>
 *     <li>Load from file</li>
 *     <li>Import from library</li>
 *     <li>Select from Ingatan's inbuilt resource library</li>
 *     <li>Create a new image</li>
 * </ul>
 * @author Thomas Everingham
 * @version 1.0
 */
public class ImageAcquisitionDialog extends JDialog implements WindowListener {

    /**
     * Width of the menu buttons
     */
    protected static final int BTN_WIDTH = 150;
    /**
     * Width of the menu buttons
     */
    protected static final int BTN_HEIGHT = 40;
    /**
     * Maximum width of images the can be imported. If too large, the user will be
     * asked whether the image should be resized.
     */
    protected static final int MAX_IMAGE_WIDTH = 500;
    /**
     * Maximum height of images that can be imported. If too large, the user will be
     * asked whether the image should be resized.
     */
    protected static final int MAX_IMAGE_HEIGHT = 500;
    /**
     * Image source is currently set to from file.
     */
    public static final int FROM_FILE = 0;
    /**
     * Image source is currently set to from a library.
     */
    public static final int FROM_LIBRARY = 1;
    /**
     * Image source is currently set to from a collection.
     */
    public static final int FROM_COLLECTION = 2;
    /**
     * Image source is currently set to from creation of a new image.
     */
    public static final int FROM_NEW = 3;
    /**
     * Image source is currently set to from the math text editor.
     */
    public static final int FROM_MATH_TEXT = 4;
    /**
     * If the user pressed cancel
     */
    public static final int NONE = 5;
    /**
     * Image source is currently set to from the math text editor.
     */
    public static final int FROM_CHEM_STRUCTURE = 6;
    /**
     * Button for acquisition of image from file.
     */
    protected JButton btnFile;
    /**
     * Button for acquisition of image from within the library/another library.
     */
    protected JButton btnLibrary;
    /**
     * Button for acquisition of image from within a collection.
     */
    protected JButton btnCollection;
    /**
     * Button for creation of a new image.
     */
    protected JButton btnCreateNew;
    /**
     * Button for acquisition of image from JMathTeX.
     */
    protected JButton btnMathText;
    /**
     * Button for the user to accept the current image selection.
     */
    protected JButton btnUseImage;
    /**
     * Button for showing the chem drawing panel.
     */
    protected JButton btnChemDraw;
    /**
     * Button for the user to load the current image selection into the image editor.
     */
    protected JButton btnEditFirst;
    /**
     * Do not add any image.
     */
    protected JButton btnCancel;
    /**
     * Layout constraints.
     */
    protected GridBagConstraints c = new GridBagConstraints();
    /**
     * File chooser for acquisition from file.
     */
    protected JFileChooser choose;
    /**
     * MathTeX creator for generation of an image in that way.
     */
    protected JMathTeXCreationPane mathTeXCreationPane;
    /**
     * Component for browsing through the images stored within other libraries.
     */
    protected LibraryResourceBrowser libResourceBrowser;
    /**
     * Component for browsing through collections of provided images. Essentially
     * this is a clip art gallery.
     */
    protected ImageCollectionBrowser collectionBrowser;
    /**
     * Component for editing images or creating a new image.
     */
    protected ImageEditorPane editorPane;
    /**
     * SketchEl chemistry editor pane. For the drawing of nice vector chem sketches.
     * (c) 2007-2009 Dr. Alex M. Clark, under the GNU License: www.gnu.org for details.
     */
    protected MainPanel chemEditorPane = new MainPanel(null, MainPanel.MODE_NORMAL, null);
    /**
     * The image that has been acquired in whatever way chosen. This will be generated
     * when the user selects the 'use image' button.
     */
    protected BufferedImage acquiredImage;
    /**
     * Upon an image being chosen, this String holds the most sensible description
     * of that image. For example, if the image was chosen from file, this string
     * will be the filename of the image (without the path). This will hold the
     * math text if the source was math text.
     */
    protected String acquiredImageSource;
    /**
     * The currently displayed acquisition source. Set to one of the 'FROM_' fields
     * of this class.
     */
    protected int imageSource;
    /**
     * The side bar panel that holds all the menu buttons (image acquisition sources).
     */
    PaintedJPanel menuPanel = new PaintedJPanel();

    /**
     * Create a new <code>ImageAcquisitionDialog</code>.
     */
    public ImageAcquisitionDialog() {//Window owner) {
        super();
        this.setModal(true);

        this.setContentPane(new JPanel());
        this.setMinimumSize(new Dimension(600, 200));
        this.setPreferredSize(new Dimension(900, 460));
        this.addWindowListener(this);
        this.setTitle("Image Acquisition");
        this.setIconImage(IOManager.windowIcon);
        //listen for resize events; a small work around has been used for size/position of a scroll pane
        this.addComponentListener(new ResizeListener());
        ((JPanel) this.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));

        //set up the file chooser
        choose = new JFileChooser();
        choose.setFileView(new IconFileView());
        FileChooserPreviewPane preview = new FileChooserPreviewPane();
        choose.setAccessory(preview);
        choose.addPropertyChangeListener(preview);
        choose.setDragEnabled(false);
        choose.setControlButtonsAreShown(false);



        //instantiate other
        mathTeXCreationPane = new JMathTeXCreationPane();

        //get a libraryID..
        String[] libIDs = IOManager.getLibraryIDs();
        if (libIDs.length == 0) {
            libResourceBrowser = new LibraryResourceBrowser("");
        } else {
            libResourceBrowser = new LibraryResourceBrowser(libIDs[0]);
        }

        collectionBrowser = new ImageCollectionBrowser();
        editorPane = new ImageEditorPane();

        //set button properties
        btnFile = new JButton(new FromFileAction());
        btnFile.setMargin(new Insets(1, 1, 1, 1));
        btnFile.setFocusPainted(false);
        btnFile.setMaximumSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btnFile.setMinimumSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btnFile.setPreferredSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btnFile.setIcon(new ImageIcon(ImageAcquisitionDialog.class.getResource("/resources/icons/folder.png")));
        btnLibrary = new JButton(new FromLibraryAction());
        btnLibrary.setMargin(new Insets(1, 1, 1, 1));
        btnLibrary.setMaximumSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btnLibrary.setMinimumSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btnLibrary.setIcon(new ImageIcon(ImageAcquisitionDialog.class.getResource("/resources/icons/books.png")));
        btnLibrary.setPreferredSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btnCollection = new JButton(new FromCollectionAction());
        btnCollection.setIcon(new ImageIcon(ImageAcquisitionDialog.class.getResource("/resources/icons/color_swatch.png")));
        btnCollection.setMaximumSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btnCollection.setMinimumSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btnCollection.setMargin(new Insets(1, 1, 1, 1));
        btnCollection.setPreferredSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btnCreateNew = new JButton(new NewImageAction());
        btnCreateNew.setMaximumSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btnCreateNew.setMargin(new Insets(1, 1, 1, 1));
        btnCreateNew.setIcon(new ImageIcon(ImageAcquisitionDialog.class.getResource("/resources/icons/image/pencil.png")));
        btnCreateNew.setPreferredSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btnCreateNew.setMinimumSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btnChemDraw = new JButton(new ChemDrawAction());
        btnChemDraw.setMaximumSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btnChemDraw.setMargin(new Insets(1, 1, 1, 1));
        btnChemDraw.setIcon(new ImageIcon(ImageAcquisitionDialog.class.getResource("/resources/icons/image/chem.png")));
        btnChemDraw.setPreferredSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btnChemDraw.setMinimumSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btnMathText = new JButton(new MathTextAction());
        btnMathText.setMaximumSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btnMathText.setPreferredSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btnMathText.setMinimumSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btnMathText.setMargin(new Insets(1, 1, 1, 1));
        btnMathText.setIcon(new ImageIcon(ImageAcquisitionDialog.class.getResource("/resources/icons/image/math.png")));
        btnUseImage = new JButton(new UseImageAction());
        btnUseImage.setIcon(new ImageIcon(ImageAcquisitionDialog.class.getResource("/resources/icons/accept.png")));
        btnUseImage.setMaximumSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));

        btnEditFirst = new JButton(new EditFirstAction());
        btnEditFirst.setIcon(new ImageIcon(ImageAcquisitionDialog.class.getResource("/resources/icons/image/pencil.png")));
        btnEditFirst.setMaximumSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));



        menuPanel.setMaximumSize(new Dimension(BTN_WIDTH + 15, 400));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.add(btnFile);
        menuPanel.add(Box.createVerticalStrut(7));
        menuPanel.add(btnLibrary);
        menuPanel.add(Box.createVerticalStrut(7));
        menuPanel.add(btnCollection);
        menuPanel.add(Box.createVerticalStrut(7));
        menuPanel.add(btnCreateNew);
        menuPanel.add(Box.createVerticalStrut(7));
        menuPanel.add(btnChemDraw);
        menuPanel.add(Box.createVerticalStrut(7));
        menuPanel.add(btnMathText);
        menuPanel.add(Box.createVerticalStrut(16));
        menuPanel.add(btnUseImage);
        menuPanel.add(Box.createVerticalStrut(7));
        menuPanel.add(btnEditFirst);


        //LAYOUT TASKS -------------------------------------
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
        Box v = Box.createVerticalBox();
        v.add(menuPanel);
        v.add(Box.createVerticalGlue());

        this.getContentPane().add(v);
        this.getContentPane().add(Box.createHorizontalStrut(15));



        this.pack();
    }

    /**
     * Reset the image acquisition dialog so that it no longer contains the previous selection data.
     */
    public void reset() {
        //ensure we're not in editor only
        setEditorOnly(false);

        ImageAcquisitionDialog.this.getContentPane().remove(libResourceBrowser);
        ImageAcquisitionDialog.this.getContentPane().remove(collectionBrowser);
        ImageAcquisitionDialog.this.getContentPane().remove(mathTeXCreationPane);
        mathTeXCreationPane.clearTextField();
        ImageAcquisitionDialog.this.getContentPane().remove(editorPane);
        ImageAcquisitionDialog.this.getContentPane().remove(chemEditorPane);
        chemEditorPane.editorPane().clear();
        ImageAcquisitionDialog.this.getContentPane().remove(choose);

        acquiredImage = null;
        acquiredImageSource = "";
        imageSource = ImageAcquisitionDialog.NONE;

        ImageAcquisitionDialog.this.editorPane.clearCanvasUndoHistory();

        editorPane.setDocumentImage(new BufferedImage(150, 150, editorPane.getCanvasImage().getType()));
        Graphics2D g = (Graphics2D) editorPane.getCanvasImage().getGraphics();
        g.setPaint(Color.white);
        g.fillRect(0, 0, 150, 150);
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        this.imageSource = ImageAcquisitionDialog.NONE;
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public class FromFileAction extends AbstractAction {

        public FromFileAction() {
            super("From File");
        }

        public void actionPerformed(ActionEvent e) {
            ImageAcquisitionDialog.this.getContentPane().remove(libResourceBrowser);
            ImageAcquisitionDialog.this.getContentPane().remove(collectionBrowser);
            ImageAcquisitionDialog.this.getContentPane().remove(mathTeXCreationPane);
            ImageAcquisitionDialog.this.getContentPane().remove(editorPane);
            ImageAcquisitionDialog.this.getContentPane().remove(chemEditorPane);
            ImageAcquisitionDialog.this.getContentPane().add(choose);
            ImageAcquisitionDialog.this.validate();
            ImageAcquisitionDialog.this.repaint();
            imageSource = FROM_FILE;
        }
    }

    public class FromLibraryAction extends AbstractAction {

        public FromLibraryAction() {
            super("From Library");
        }

        public void actionPerformed(ActionEvent e) {
            ImageAcquisitionDialog.this.getContentPane().remove(choose);
            ImageAcquisitionDialog.this.getContentPane().remove(collectionBrowser);
            ImageAcquisitionDialog.this.getContentPane().remove(mathTeXCreationPane);
            ImageAcquisitionDialog.this.getContentPane().remove(editorPane);
            ImageAcquisitionDialog.this.getContentPane().remove(chemEditorPane);
            ImageAcquisitionDialog.this.getContentPane().add(libResourceBrowser);
            ImageAcquisitionDialog.this.validate();
            ImageAcquisitionDialog.this.repaint();
            imageSource = FROM_LIBRARY;
        }
    }

    /**
     * Gets the image that has been acquired.
     * @return the image that has been acquired.
     */
    public BufferedImage getAcquiredImage() {
        return acquiredImage;
    }

    /**
     * Gets the logical name of the source image.
     * @return the logical name of the source image. If taken from file, then
     * this will be the file name, if created from new, it will be something like
     * "new600x400".
     */
    public String getAcquiredImageData() {
        return acquiredImageSource;
    }

    /**
     * Sets the image editor's document image.
     * @param image the image to set to the canvas.
     */
    public void setEditorDocumentImage(BufferedImage image) {
        editorPane.setDocumentImage(image);
    }

    public void setEditorOnly(boolean editor) {
        menuPanel.removeAll();

        if (editor == true) {
            menuPanel.add(btnUseImage);
        } else {
            menuPanel.add(btnFile);
            menuPanel.add(Box.createVerticalStrut(7));
            menuPanel.add(btnLibrary);
            menuPanel.add(Box.createVerticalStrut(7));
            menuPanel.add(btnCollection);
            menuPanel.add(Box.createVerticalStrut(7));
            menuPanel.add(btnCreateNew);
            menuPanel.add(Box.createVerticalStrut(7));
            menuPanel.add(btnChemDraw);
            menuPanel.add(Box.createVerticalStrut(7));
            menuPanel.add(btnMathText);
            menuPanel.add(Box.createVerticalStrut(16));
            menuPanel.add(btnUseImage);
            menuPanel.add(Box.createVerticalStrut(7));
            menuPanel.add(btnEditFirst);
        }
        
        editorPane.setUpKeyBindings(ImageAcquisitionDialog.this.getRootPane());
        ImageAcquisitionDialog.this.getContentPane().remove(choose);
        ImageAcquisitionDialog.this.getContentPane().remove(libResourceBrowser);
        ImageAcquisitionDialog.this.getContentPane().remove(mathTeXCreationPane);
        ImageAcquisitionDialog.this.getContentPane().remove(collectionBrowser);
        ImageAcquisitionDialog.this.getContentPane().remove(chemEditorPane);
        ImageAcquisitionDialog.this.getContentPane().add(editorPane);
        ImageAcquisitionDialog.this.validate();
        ImageAcquisitionDialog.this.repaint();
        imageSource = FROM_NEW;
    }

    /**
     * Gets the source of image acquisition. Check this with one of the fields of this class (ImageAcquisitionDialog).
     * Possible values are FROM_NEW, FROM_FILE, FROM_LIBRARY... etc.
     * @return the source of the image. Check this value up against one of the fields of the ImageAcquisitionDialog class.
     */
    public int getAcquisitionSource() {
        return imageSource;
    }

    public class NewImageAction extends AbstractAction {

        public NewImageAction() {
            super("Create New");
        }

        public void actionPerformed(ActionEvent e) {
            ImageAcquisitionDialog.this.getContentPane().remove(choose);
            ImageAcquisitionDialog.this.getContentPane().remove(libResourceBrowser);
            ImageAcquisitionDialog.this.getContentPane().remove(mathTeXCreationPane);
            ImageAcquisitionDialog.this.getContentPane().remove(collectionBrowser);
            ImageAcquisitionDialog.this.getContentPane().remove(chemEditorPane);
            ImageAcquisitionDialog.this.getContentPane().add(editorPane);
            editorPane.setUpKeyBindings(ImageAcquisitionDialog.this.getRootPane());
            ImageAcquisitionDialog.this.validate();
            ImageAcquisitionDialog.this.repaint();
            imageSource = FROM_NEW;
        }
    }

    public class FromCollectionAction extends AbstractAction {

        public FromCollectionAction() {
            super("From Collection");
        }

        public void actionPerformed(ActionEvent e) {
            ImageAcquisitionDialog.this.getContentPane().remove(choose);
            ImageAcquisitionDialog.this.getContentPane().remove(libResourceBrowser);
            ImageAcquisitionDialog.this.getContentPane().remove(mathTeXCreationPane);
            ImageAcquisitionDialog.this.getContentPane().remove(editorPane);
            ImageAcquisitionDialog.this.getContentPane().remove(chemEditorPane);
            ImageAcquisitionDialog.this.getContentPane().add(collectionBrowser);
            ImageAcquisitionDialog.this.validate();
            ImageAcquisitionDialog.this.repaint();
            imageSource = FROM_COLLECTION;
        }
    }

    public class MathTextAction extends AbstractAction {

        public MathTextAction() {
            super("Math Text");
        }

        public void actionPerformed(ActionEvent e) {
            ImageAcquisitionDialog.this.getContentPane().remove(choose);
            ImageAcquisitionDialog.this.getContentPane().remove(libResourceBrowser);
            ImageAcquisitionDialog.this.getContentPane().remove(collectionBrowser);
            ImageAcquisitionDialog.this.getContentPane().remove(editorPane);
            ImageAcquisitionDialog.this.getContentPane().remove(chemEditorPane);
            ImageAcquisitionDialog.this.getContentPane().add(mathTeXCreationPane);
            ImageAcquisitionDialog.this.validate();
            ImageAcquisitionDialog.this.repaint();
            imageSource = FROM_MATH_TEXT;
        }
    }

    public class ChemDrawAction extends AbstractAction {

        public ChemDrawAction() {
            super("Chem Draw");
        }

        public void actionPerformed(ActionEvent e) {
            ImageAcquisitionDialog.this.getContentPane().remove(choose);
            ImageAcquisitionDialog.this.getContentPane().remove(libResourceBrowser);
            ImageAcquisitionDialog.this.getContentPane().remove(collectionBrowser);
            ImageAcquisitionDialog.this.getContentPane().remove(editorPane);
            ImageAcquisitionDialog.this.getContentPane().remove(mathTeXCreationPane);
            ImageAcquisitionDialog.this.getContentPane().add(chemEditorPane);
            chemEditorPane.setUpKeyBindings(ImageAcquisitionDialog.this.getRootPane());
            ImageAcquisitionDialog.this.validate();
            ImageAcquisitionDialog.this.repaint();
            imageSource = FROM_CHEM_STRUCTURE;
        }
    }

    public class UseImageAction extends AbstractAction {

        public UseImageAction() {
            super("Use Image");
        }

        public void actionPerformed(ActionEvent e) {
            //check which option is open
            switch (imageSource) {
                case FROM_FILE:
                    //is an image file selected?
                    if (choose.getSelectedFile() == null) {
                        JOptionPane.showMessageDialog(ImageAcquisitionDialog.this, "Please select an image file.", "No Selection", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    //try to load the image.
                    BufferedImage imageIn = null;
                    try {
                        imageIn = ImageIO.read(choose.getSelectedFile());
                    } catch (Exception evt) {
                        JOptionPane.showMessageDialog(ImageAcquisitionDialog.this, "Could not load the specified image:"
                                + "\n    " + choose.getSelectedFile().getName() + "\nEnsure"
                                + "that this is an image file, and that you have read rights at that location.", "Load Image", JOptionPane.ERROR_MESSAGE);
                        return;
                    } catch (OutOfMemoryError evt) {
                        JOptionPane.showMessageDialog(ImageAcquisitionDialog.this, "Not enough memory available to load this image.", "Load Image", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    //create a new EmbeddedImage object
                    if (imageIn == null) {
                        JOptionPane.showMessageDialog(ImageAcquisitionDialog.this, "Failed to load the specified image.", "Load Image", JOptionPane.ERROR_MESSAGE);
                    }

                    acquiredImage = imageIn;
                    acquiredImageSource = choose.getSelectedFile().getName();
                    ImageAcquisitionDialog.this.setVisible(false);
                    break;
                case FROM_LIBRARY:
                    acquiredImage = null;
                    //the image ID and library are passed as a single string which can later be split as acquiredImageSource.split("\n")
                    acquiredImageSource = libResourceBrowser.getSelectedImageID() + "\n" + libResourceBrowser.getSelectedLibraryID();
                    ImageAcquisitionDialog.this.setVisible(false);
                    break;
                case FROM_COLLECTION:
                    acquiredImage = collectionBrowser.getSelectedImage();
                    //provide the name of the image file
                    acquiredImageSource = collectionBrowser.getSelectedImageName();
                    ImageAcquisitionDialog.this.setVisible(false);
                    break;
                case FROM_NEW:
                    acquiredImage = editorPane.getCanvasImage();
                    //generate an imageID based on the colour of pixel 0,0 and the width and height of the image
                    acquiredImageSource = "new(" + acquiredImage.getRGB(0, 0) + ")(" + acquiredImage.getWidth() + "x" + acquiredImage.getHeight() + ")";
                    ImageAcquisitionDialog.this.setVisible(false);
                    break;
                case FROM_MATH_TEXT:
                    TeXFormula formula;
                    try {
                        if (mathTeXCreationPane.getMathText().isEmpty()) {
                            throw new ParseException("Empty math text expression.");
                        }
                        formula = new TeXFormula(mathTeXCreationPane.getMathText());
                    } catch (ParseException ex) {
                        JOptionPane.showMessageDialog(ImageAcquisitionDialog.this, "Invalid or empty JMathTeX expression. Please check for invalid escape sequences, etc.", "Invalid TeX", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    acquiredImageSource = mathTeXCreationPane.getMathText() + "\n" + mathTeXCreationPane.getRenderSize() + "\n70,70,70";
                    ImageAcquisitionDialog.this.setVisible(false);
                    break;
                case FROM_CHEM_STRUCTURE:
                    acquiredImage = chemEditorPane.getChemicalStrucgtureImage();
                    if (acquiredImage == null) {
                        JOptionPane.showMessageDialog(ImageAcquisitionDialog.this, "Could not generate an image from the current structure.", "Render Fail", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    acquiredImageSource = "chem(" + acquiredImage.getRGB(0, 0) + ")(" + acquiredImage.getWidth() + "x" + acquiredImage.getHeight() + ")";
                    ImageAcquisitionDialog.this.setVisible(false);
                    break;
                default:
                    break;
            }
            //check whether an image is selected, if needed (or check if a math tex formula has been entered, check that it is valid)
            //create an embedded image object or an embedded formula object.
            //set this as acquired image variable so it is accessible elsewhere
            //save the image to the currently open library!! (unless acquired from within this library).
            //ensure that the 'acquire from library' currently open library has been closed by some mechanism
            //close the dialog
        }
    }

    public class EditFirstAction extends AbstractAction {

        public EditFirstAction() {
            super("Edit First");
        }

        public void actionPerformed(ActionEvent e) {

            switch (imageSource) {
                case FROM_FILE:
                    //is an image file selected?
                    if (choose.getSelectedFile() == null) {
                        JOptionPane.showMessageDialog(ImageAcquisitionDialog.this, "Please select an image file.", "No Selection", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    //try to load the image.
                    BufferedImage imageIn = null;
                    try {
                        imageIn = ImageIO.read(choose.getSelectedFile());
                    } catch (Exception evt) {
                        JOptionPane.showMessageDialog(ImageAcquisitionDialog.this, "Could not load the specified image:"
                                + "\n    " + choose.getSelectedFile().getName() + "\nEnsure"
                                + "that this is an image file, and that you have read rights at that location.", "Load Image", JOptionPane.ERROR_MESSAGE);
                        return;
                    } catch (OutOfMemoryError evt) {
                        JOptionPane.showMessageDialog(ImageAcquisitionDialog.this, "Not enough memory available to load this image.", "Load Image", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    //set image editor document
                    editorPane.setDocumentImage(imageIn);
                    break;
                case FROM_LIBRARY:
                    try {
                        editorPane.setDocumentImage(IOManager.loadImage(libResourceBrowser.getSelectedLibraryID(), libResourceBrowser.getSelectedImageID()));
                    } catch (IOException ex) {
                        Logger.getLogger(ImageAcquisitionDialog.class.getName()).log(Level.SEVERE, "Was trying to load image through IOManager for insertion into the image\n"
                                + "editor. LibraryID=" + libResourceBrowser.getSelectedLibraryID() + " , imageID" + libResourceBrowser.getSelectedImageID(), ex);
                    }
                    break;
                case FROM_COLLECTION:
                    editorPane.setDocumentImage(collectionBrowser.getSelectedImage());
                    break;
                case FROM_NEW:
                    break;
                case FROM_MATH_TEXT:
                    TeXFormula formula;
                    try {
                        if (mathTeXCreationPane.getMathText().isEmpty()) {
                            throw new ParseException("Empty math text expression.");
                        }
                        formula = new TeXFormula(mathTeXCreationPane.getMathText());
                    } catch (ParseException ex) {
                        JOptionPane.showMessageDialog(ImageAcquisitionDialog.this, "Invalid or empty JMathTeX expression. Please check for invalid escape sequences, etc.", "Invalid TeX", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Icon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, mathTeXCreationPane.getRenderSize());
                    BufferedImage renderedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight() + 6, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = renderedImage.createGraphics();
                    g2.fillRect(0, 0, renderedImage.getWidth(), renderedImage.getHeight());
                    icon.paintIcon(new JLabel(), g2, 0, 5);

                    editorPane.setDocumentImage(renderedImage);
                    break;
                case FROM_CHEM_STRUCTURE:
                    BufferedImage imageRender = chemEditorPane.getChemicalStrucgtureImage();
                    if (imageRender == null) {
                        JOptionPane.showMessageDialog(ImageAcquisitionDialog.this, "Could not generate an image from the current structure.", "Render Fail", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    editorPane.setDocumentImage(imageRender);
                    break;
                default:
                    break;
            }

            ImageAcquisitionDialog.this.getContentPane().remove(choose);
            ImageAcquisitionDialog.this.getContentPane().remove(libResourceBrowser);
            ImageAcquisitionDialog.this.getContentPane().remove(mathTeXCreationPane);
            ImageAcquisitionDialog.this.getContentPane().remove(collectionBrowser);
            ImageAcquisitionDialog.this.getContentPane().remove(chemEditorPane);
            ImageAcquisitionDialog.this.setSize(900, 400);
            ImageAcquisitionDialog.this.getContentPane().add(editorPane);
            ImageAcquisitionDialog.this.validate();
            ImageAcquisitionDialog.this.repaint();
            editorPane.setUpKeyBindings(ImageAcquisitionDialog.this.getRootPane());
            imageSource = FROM_NEW;
        }
    }

    /**
     * Sets up the key bindings for the image editor pane. The key bindings are set to
     * the root pane of the ImageAcquisitionDialog, as WHEN_ANCESTOR_OF_FOCUSSED_COMPONENT.
     * Keybindings include ctrl+z/y undo redo, ctrl+a select all, delete for clear, etc.
     */
    public void setUpKeyBindingsForEditorPane() {
        editorPane.setUpKeyBindings(ImageAcquisitionDialog.this.getRootPane());
    }

    private class ResizeListener implements ComponentListener {

        public void componentResized(ComponentEvent e) {
            //the following line stops the canvas JScrollPane from assuming a preferred size of its own. This was making it
            //so that the box layout placed the scrollpane away from the option pane and colour chooser. Odd behaviour, I don't
            //quite understand, but assigning an arbitrary preferred size and then allowing BoxLayout to set a nice size seems
            //to have worked.... hmm!
            ImageAcquisitionDialog.this.editorPane.getCanvasScrollPane().setPreferredSize(new Dimension(500, 200));
            ImageAcquisitionDialog.this.getContentPane().getLayout().layoutContainer(ImageAcquisitionDialog.this.getContentPane());
        }

        public void componentMoved(ComponentEvent e) {
        }

        public void componentShown(ComponentEvent e) {
        }

        public void componentHidden(ComponentEvent e) {
        }
    }
}

