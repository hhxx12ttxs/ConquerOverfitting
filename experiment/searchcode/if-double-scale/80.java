package hoipolloi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import java.beans.*;
import java.io.*;

/**
 * Hoi Polloi picture editor, mainly used to crop/resize pictures to fit the
 * size specified by the program for the profile picture.
 * 
 * Cropping the picture will consist of dragging a correctly sized square around
 * a picture and the selecting the "crop" option to cut out the portion of the
 * picture in the bounds of the rectangle.
 * 
 * For note, the crop area is officially 291x356 pixels.
 * 
 * Resizing will be used for pictures that are large to allow faces to fit
 * within the bounds of the cropping rectangle.
 * 
 * @author  Brandon Buck
 * @author  Brandon Tanner
 * @author  Sam Harrison
 * @version 1.4 (Jan 17, 2010)
 * @since   December 18, 2007
 */
public class HPPictureEditor extends JDialog implements ActionListener, MouseWheelListener {
    private MainMenu parent;
    // Buttons
    private JButton acceptButton = new JButton("Accept");
    private JButton cancelButton = new JButton("Cancel");
    private JImageLabel imageLabel = new JImageLabel(291, 356);
    private BufferedImage originalImage = null;
    private BufferedImage currentImage = null;
    private java.io.File lastSelectedFile = null;
    private int userID;

    /** The Original Aspect Ratio (oAR) of the Photo being Cropped. */
    protected double oAR;

    /**
     * Default constructor for the HPPictureEditor. This constructor sets up
     * frames and frame components for use with the editor.
     * 
     * @param parent The Frame that is HPPictureEditor belongs to.
     * @param uid    The Person ID who we are adding a photo for.
     */
    public HPPictureEditor(Frame parent, int uid) {
        super(parent);
        this.parent = (MainMenu)parent;
        this.setTitle("Picture Editor");
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setModal(true);
        this.setResizable(false);
        this.userID = uid;

        // JMenu
        ImageIcon loadImageIcon = new ImageIcon(getClass().getClassLoader().getResource("picture.png"));
        ImageIcon resetImageIcon = new ImageIcon(getClass().getClassLoader().getResource("bomb.png"));
        ImageIcon closeImageIcon = new ImageIcon(getClass().getClassLoader().getResource("blank.png"));

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        JMenuItem loadImageItem = new JMenuItem(new MenuAction("Open Picture", this));
        loadImageItem.setIcon(loadImageIcon);
        loadImageItem.setMnemonic('O');
        loadImageItem.setAccelerator(KeyStroke.getKeyStroke('O', KeyEvent.CTRL_DOWN_MASK));

        JMenuItem resetImageItem = new JMenuItem(new MenuAction("Reset", this));
        resetImageItem.setIcon(resetImageIcon);
        resetImageItem.setAccelerator(KeyStroke.getKeyStroke('R', KeyEvent.CTRL_DOWN_MASK));
        resetImageItem.setMnemonic('R');

        JMenuItem disposeItem = new JMenuItem(new MenuAction("Close", this));
        disposeItem.setIcon(closeImageIcon);
        disposeItem.setAccelerator(KeyStroke.getKeyStroke('D', KeyEvent.CTRL_DOWN_MASK + KeyEvent.ALT_DOWN_MASK));
        disposeItem.setMnemonic('C');

        fileMenu.add(loadImageItem);
        fileMenu.add(resetImageItem);
        fileMenu.addSeparator();
        fileMenu.add(disposeItem);
        
        // Setup Main Menu Bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        this.setJMenuBar(menuBar);
        // End JMenu

        // Component set up
        acceptButton.addActionListener(this);
        cancelButton.addActionListener(this);
        // End Component set up

        // Frame Contents
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(acceptButton);
        buttonPanel.add(cancelButton);

        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK));
        imagePanel.add(BorderLayout.CENTER, imageLabel);
        imagePanel.addMouseWheelListener(this);

        this.add(BorderLayout.CENTER, imagePanel);
        this.add(BorderLayout.SOUTH, buttonPanel);
        // End Frame Contents

        // Frame size and location
        this.setSize(400, 400);

        // Set the location of the frame relative to the MainMenu
        // --CENTER--
        Point frameLocation = new Point();
        double frameX = parent.getLocation().getX() + ((parent.getWidth() / 2) - (this.getWidth() / 2));
        double frameY = parent.getLocation().getY() + ((parent.getHeight() / 2) - (this.getHeight() / 2));
        frameLocation.setLocation(frameX, frameY);
        this.setLocation(frameLocation);
        // --END CENTER--
        
        this.setVisible(true);
    }

    /**
     * Displays the HPPictureEditor with the given owner.
     * 
     * @param owner The frame that this HPPictureEditor belongs to.
     * @param uid   The Person ID who we are adding a photo for.
     */
    public static void showHPPictureEditor(Frame owner, int uid) {
        new HPPictureEditor(owner, uid);
    }

    /**
     * Handles any events that may be generated or used by this HPPictureEditor.
     * 
     * @param evt The ActionEvent that called this method.
     */
    public void actionPerformed(ActionEvent evt) {
        JButton sourceButton = (JButton)(evt.getSource());

        if (sourceButton == acceptButton) {
            // Use getSubimage to get an area of the original image into a new BufferedImage
            // then use ImageIO to save to disk.
            try {
               Rectangle mybox = imageLabel.getCropBounds();
               int topLeftX = (int)mybox.getX();
               int topLeftY = (int)mybox.getY();
               BufferedImage outimage = currentImage.getSubimage(topLeftX + 1, topLeftY + 1, 289, 354); // Added 1 to the X and the Y to get the image within the outline
               // Save the new Picture to Disk
               File outputFile = new File("pictures/" + this.userID + ".jpg");
               ImageIO.write(outimage, "JPG", outputFile);
               // Update the Main Window
               parent.showProfile(parent.getCurrentPerson());
               // Close the Crop Editor
               this.dispose();
            }
            catch (Exception e) {
                Debug.print("Error Cropping Photo:");
                Debug.print(e.getMessage());
            }

        } else if (sourceButton == cancelButton) {
            this.dispose();
        }
    }

    /** Packs and centers the frame. */
    public void resizeFrame() {
        this.pack();

        // Set the location of the frame relative to the MainMenu
        // --CENTER--
        Point frameLocation = new Point();

        double frameX = parent.getLocation().getX() + ((parent.getWidth() / 2) - (this.getWidth() / 2));
        double frameY = parent.getLocation().getY() + ((parent.getHeight() / 2) - (this.getHeight() / 2));

        frameLocation.setLocation(frameX, frameY);
        this.setLocation(frameLocation);
        // --END CENTER--
    }

    /** Closed the picture editor window. */
    public void disposeEditor() {
        this.dispose();
    }

    /** Deletes a current profile photo if it exists. */
    public void resetImage() {
        // Delete Image from Disk
        File outputFile = new File("pictures/"+this.userID+".jpg");
        if (outputFile.exists()) {
            if (!outputFile.delete()) {
                Debug.print("Failed to Delete Current Image File");
            }
        }
        parent.showProfile(parent.getCurrentPerson());
        // Dispose of Editor Window
        disposeEditor();
    }

    /**
     * Scales the image given a width and height.
     *
     * @param newWidth  The new width in pixels.
     * @param newHeight The new height in pixels.
     */
    public void resizeImage(final int newWidth, final int newHeight) {
        final BufferedImage newCurrentImage = createBlankImage(originalImage, newWidth, newHeight);
        final Graphics2D g2 = newCurrentImage.createGraphics();
        g2.drawImage(originalImage, 0, 0, newWidth, newHeight, 0, 0, originalImage.getWidth(), originalImage.getHeight(), null);
        g2.dispose();
        currentImage = newCurrentImage;
        imageLabel.setIcon(new ImageIcon(currentImage));
        imageLabel.setText("");
    }

    /**
     * Captures mouse scroll events, and resizes the picture.
     *
     * The aspect ratio of an image is its width divided by its height.
     */
    public void mouseWheelMoved(MouseWheelEvent e) {
        String message;

        if (this.currentImage == null) {
            message = "No image to resize yet.";
        }
        else {
            final boolean  bWheelUp = ( e.getWheelRotation() < 0 ); // Up is negative, down is positive.

            // Get Current Width/Height of Image
            final int curWidth  = this.currentImage.getWidth();
            final int curHeight = this.currentImage.getHeight();

            // 5% Increments of the Picture
            final int fivePercentWidth  = (int)(.05 * curWidth  * this.oAR);
            final int fivePercentHeight = (int)(.05 * curHeight * this.oAR);

            final int newWidth  = curWidth  + (fivePercentWidth  * ( (bWheelUp) ? 1 : -1 ));
            final int newHeight = curHeight + (fivePercentHeight * ( (bWheelUp) ? 1 : -1 ));

            message = "Resizing Picture to " + newWidth + " x " + newHeight;
            this.resizeImage(newWidth, newHeight);
            this.resizeFrame();
        }
        Debug.print(message);
    }

    class MenuAction extends AbstractAction {
        private HPPictureEditor parent;
        public MenuAction(String name, HPPictureEditor parent) {
            super(name);
            this.parent = parent;
        }

        public void actionPerformed(ActionEvent evt) {
            String selection = (String)getValue(Action.NAME);

            if (selection.equals("Open Picture")) {
                JFileChooser browser = new JFileChooser();
                browser.setFileFilter(new ImageFileFilter());
                browser.setAcceptAllFileFilterUsed(false);
                ImagePreview preview = new ImagePreview(browser);
                browser.addPropertyChangeListener(preview);
                browser.setAccessory(preview);

                if (lastSelectedFile != null)
                    browser.setSelectedFile(lastSelectedFile);

                int browserSelection = browser.showDialog(parent, "Open");

                if (browserSelection == JFileChooser.APPROVE_OPTION) {
                    try {
                        lastSelectedFile = browser.getSelectedFile();
                        originalImage = ImageIO.read(lastSelectedFile);
                        currentImage = originalImage;
                        oAR = (double)originalImage.getWidth() / originalImage.getHeight();
                        imageLabel.setIcon(new ImageIcon(lastSelectedFile.getPath()));
                        Debug.print("Current Size: "+originalImage.getWidth()+" x "+originalImage.getHeight());
                        Debug.print("Original Aspect Ratio is: "+(double)originalImage.getWidth() / originalImage.getHeight());

                        resizeFrame();
                    } catch(java.io.IOException exc) {
                        Debug.print("ERROR: Problem reading file in.");
                    }
                }
            }
            else if (selection.equals("Close")) {
                disposeEditor();
            }
            else if (selection.equals("Reset")) {
                resetImage();
            }
        }
    }

    public static BufferedImage createBlankImage(BufferedImage src, int w, int h) {
        int type = src.getType();
        if (type != BufferedImage.TYPE_CUSTOM)
            return new BufferedImage(w, h, type);
        else {
            ColorModel cm = src.getColorModel();
            WritableRaster raster = src.getRaster().createCompatibleWritableRaster(w, h);
            boolean isRasterPremultiplied = src.isAlphaPremultiplied();
            return new BufferedImage(cm, raster, isRasterPremultiplied, null);
        }
    }

    class ImageFileFilter extends javax.swing.filechooser.FileFilter {
        public ImageFileFilter() { super(); }

        public boolean accept(java.io.File file) {
            String filePath = file.getPath();
            String fileType = "";
            if (!file.isDirectory()) {
                if (filePath.length() > 5 && (filePath.lastIndexOf('.') > -1))
                    fileType = filePath.substring(filePath.lastIndexOf('.'));
                else
                    return false;
            }
            boolean answer = false;

            if (fileType.equalsIgnoreCase(".jpg") ||
                    fileType.equalsIgnoreCase(".jpeg") || 
                    fileType.equalsIgnoreCase(".gif") || 
                    fileType.equalsIgnoreCase(".png") || file.isDirectory())
                answer = true;

            return answer;
        }

        public String getDescription() {
            return "Picture files (JPG, JPEG, PNG, GIF)";
        }
    }

    /**
     * Following code is Hack #31 from "Swing Hacks" by Chris Adamson and
     * Joshua Marinacci. Minor edits include making the classes internal instead
     * of separate files.
     * 
     * This adds a preview image to the JFileChooser so that the user may
     * preview a thumbnail of the selected image instead of guessing or
     * memorizing the names of the picture they are looking for.
     * 
     */
    class ImagePreview extends JPanel implements PropertyChangeListener {
        private JFileChooser jfc;
        private Image img;

        public ImagePreview(JFileChooser jfc) {
            this.jfc = jfc;
            Dimension sz = new Dimension(200,200);
            setPreferredSize(sz);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            try {
                //Debug.print("Updating.");
                java.io.File file = jfc.getSelectedFile();
                updateImage(file);
            } catch (java.io.IOException ex) {
                    Debug.print(ex.getMessage());
            }
        }

        public void updateImage(java.io.File file) throws java.io.IOException {
            if(file == null) {
                return;
            }

            img = ImageIO.read(file);
            repaint();
        }

        @Override public void paintComponent(Graphics g) {
            // fill the background
            g.setColor(Color.GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());

            if(img != null) {
                // calculate the scaling factor
                int imgW = img.getWidth(null);
                int imgH = img.getHeight(null);
                int side = Math.max(imgW, imgH);
                double scale = 200.0 / (double)side;
                int w = (int)(scale * (double)imgW);
                int h = (int)(scale * (double)imgH);

                // draw the image
                g.drawImage(img, 0, 0, w, h, null);

                // draw the image dimensions
                String dim = imgW + " x " + imgH;
                g.setColor(Color.BLACK);
                g.drawString(dim, 31, 196);
                g.setColor(Color.WHITE);
                g.drawString(dim, 30, 195);
            } //else {
                // print a message
                //g.setColor(Color.BLACK);
                //g.drawString("Not an image", 30, 100);
            //}
        }
    }

    /** Unused now? */
    class ResizeDialog extends JDialog implements ActionListener{
        private HPPictureEditor parent;

        private JRadioButton setSizeRadio;
        private JRadioButton byPercentageRadio;
        private JCheckBox constraintsBox;
        private JTextField newHeightField;
        private JTextField newWidthField;
        private JLabel constraintWidthLabel;
        private JLabel constraintHeightLabel;

        private ImageIcon locked;
        private ImageIcon unlocked;

        public ResizeDialog(HPPictureEditor parent) {
            super();
            this.setTitle("Resize");
            this.setModal(true);
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            this.parent = parent;
            this.createDialog();

            // Set the location of the frame relative to the MainMenu
            // --CENTER--
            Point frameLocation = new Point();

            double frameX = this.parent.getLocation().getX() + ((this.parent.getWidth() / 2) - (this.getWidth() / 2));
            double frameY = this.parent.getLocation().getY() + ((this.parent.getHeight() / 2) - (this.getHeight() / 2));

            frameLocation.setLocation(frameX, frameY);
            this.setLocation(frameLocation);
            // --END CENTER--

            this.setVisible(true);
        }

        private void createDialog() {
            locked = new ImageIcon(getClass().getClassLoader().getResource("link.png"));
            unlocked = new ImageIcon(getClass().getClassLoader().getResource("link_break.png"));

            constraintsBox = new JCheckBox("Keep constraints");
            constraintsBox.setSelected(true);
            constraintsBox.addActionListener(this);
            setSizeRadio = new JRadioButton("Scale by height and width");
            setSizeRadio.addActionListener(this);
            byPercentageRadio = new JRadioButton("Scale by percenage");
            byPercentageRadio.addActionListener(this);

            newWidthField = new JTextField(15);
            newHeightField = new JTextField(15);

            constraintWidthLabel = new JLabel();
            constraintWidthLabel.setIcon(locked);
            constraintHeightLabel = new JLabel();
            constraintHeightLabel.setIcon(locked);

            JLabel widthLabel = new JLabel("Width:");
            JLabel heightLabel = new JLabel("Height:");

            JPanel widthTextPanel = new JPanel();
            widthTextPanel.add(widthLabel);
            widthTextPanel.add(newWidthField);
            widthTextPanel.add(constraintWidthLabel);
            JPanel widthPanel = new JPanel();
            widthPanel.add(widthTextPanel);

            JPanel heightTextPanel = new JPanel();
            heightTextPanel.add(heightLabel);
            heightTextPanel.add(newHeightField);
            heightTextPanel.add(constraintHeightLabel);
            JPanel heightPanel = new JPanel();
            heightPanel.add(heightTextPanel);

            JPanel constraintPanel = new JPanel();
            constraintPanel.add(constraintsBox);

            JPanel bySizePanel = new JPanel();
            bySizePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "By Size"));
            bySizePanel.setLayout(new BorderLayout());
            bySizePanel.add(widthPanel, BorderLayout.NORTH);
            bySizePanel.add(heightPanel, BorderLayout.CENTER);
            bySizePanel.add(constraintPanel, BorderLayout.SOUTH);

            this.add(bySizePanel);
            this.pack();
        }

        public void actionPerformed(ActionEvent evt) {
            if (evt.getSource() == constraintsBox) {
                if (constraintsBox.isSelected()) {
                    constraintWidthLabel.setIcon(locked);
                    constraintHeightLabel.setIcon(locked);
                }
                else {
                    constraintWidthLabel.setIcon(unlocked);
                    constraintHeightLabel.setIcon(unlocked);
                }
            }
        }
    }
}

