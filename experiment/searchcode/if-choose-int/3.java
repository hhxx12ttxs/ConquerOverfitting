/*
 * ImageEditorPane.java
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
 * the META-INF directory in the source jar). This license can also beis
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
import org.ingatan.component.FileChooserPreviewPane;
import org.ingatan.component.IconFileView;
import org.ingatan.component.image.optionpanes.BrightnessContrastOptionPane;
import org.ingatan.component.OptionPane;
import org.ingatan.component.colour.ColourSwatchPane;
import org.ingatan.component.image.optionpanes.ArrowOptionPane;
import org.ingatan.component.image.optionpanes.BucketFillOptionPane;
import org.ingatan.component.image.optionpanes.CanvasSizeOptionPane;
import org.ingatan.component.image.optionpanes.EraserOptionPane;
import org.ingatan.component.image.optionpanes.JMathTeXOptionPane;
import org.ingatan.component.image.optionpanes.LineOptionPane;
import org.ingatan.component.image.optionpanes.PencilOptionPane;
import org.ingatan.component.image.optionpanes.SelectionOptionPane;
import org.ingatan.component.image.optionpanes.ShapeDrawOptionPane;
import org.ingatan.component.image.optionpanes.StampOptionPane;
import org.ingatan.component.text.RichTextArea;
import org.ingatan.component.text.RichTextToolbar;
import org.ingatan.event.ImageToolbarEvent;
import org.ingatan.event.ImageToolbarListener;
import org.ingatan.image.ImageEditorController;
import org.ingatan.image.ImageUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.StyledEditorKit;

/**
 * A simple image editor, including toolbar, colour chooser, tool options and a scrollable
 * canvas. This class generates the interface and responds to any user interaction that
 * should result in an interface change - for example clicking a new tool should both
 * highlight that tool and show the relevant options pane.
 *
 * The <code>ImageEditorController</code> is added to this class, and the controller is responsible
 * for handling all other user interaction. The controller handles all communication between components,
 * and has listeners registered with all components. For example, if the user were to select a new colour,
 * the controller would then fire a method telling all concerned components that a new colour has been set.
 * The new colour is used by the <code>LineOptionsPane</code>, for example, to draw previews of strokes.
 *
 *
 *
 * @author Thomas Everingham
 * @version 1.0
 */
public class ImageEditorPane extends JPanel {

    /**
     * The toolbar used.
     */
    private ImageToolbar toolbar = new ImageToolbar();
    /**
     * The canvas, where the user draws the image.
     */
    private EditorCanvas canvas = new EditorCanvas();
    /**
     * Options pane holder. As different tools are selected, different options
     * are presented to the user. These option panes are individual classes,
     * and this scroll pane provides a placeholder for them.
     */
    private JScrollPane optionsScrollPane = new JScrollPane();
    /**
     * Holds the canvas so that when the canvas is larger than the provided screen space, the user
     * can scroll around it.
     */
    private JScrollPane canvasScrollPane = new JScrollPane();
    /**
     * The colour chooser for the image editor. Includes the foreground and background
     * colour previews.
     */
    private ColourSwatchPane colourChooser = new ColourSwatchPane();
    /**
     * Layout manager, yayzorz.
     */
    private GridBagConstraints c;
    /**
     * Provides line styles options.
     */
    private LineOptionPane lineOptions = new LineOptionPane();
    /**
     * Provides options for the drawing and filling of shapes.
     */
    private ShapeDrawOptionPane shapeDrawOptionPane = new ShapeDrawOptionPane();
    /**
     * Provides a list of stampable images for the user to choose from.
     */
    private StampOptionPane stampOptionPane = new StampOptionPane();
    /**
     * Provides a list of erasers that the user may choose from.
     */
    private EraserOptionPane eraserOptionPane = new EraserOptionPane(this);
    /**
     * The options provided for the selection tools.
     */
    private SelectionOptionPane selectionOptionPane = new SelectionOptionPane();
    /**
     * The tolerance of the bucket fill tool is set by this pane.
     */
    private BucketFillOptionPane bucketFillOptionPane = new BucketFillOptionPane();
    /**
     * GUI for user to choose the brush type
     */
    private PencilOptionPane pencilOptionPane = new PencilOptionPane();
    /**
     * Interface for altering the brightness and contrast of the image.
     */
    private BrightnessContrastOptionPane brightnessContrastOptionPane = new BrightnessContrastOptionPane();
    /**
     * The text field where the user enters the TeX to be rendered.
     */
    private JTextField txtMath = new JTextField();
    /**
     * Cheat sheet and render size setting for the JMathTeX tool.
     */
    private JMathTeXOptionPane jMathTeXOptionPane = new JMathTeXOptionPane(txtMath);
    /**
     * Options for selection arrow head and line style.
     */
    private ArrowOptionPane arrowOptionPane = new ArrowOptionPane();
    /**
     * Reference to the current options pane, allows it to be easily removed from <code>optionsScrollPane</code>
     * when we need to add a new one.
     */
    private OptionPane currentOptionPane;
    /**
     * Colour of the borders painted.
     */
    private Color borders = new Color(26, 97, 110);
    /**
     * Provides interface for the user to set the canvas size.
     */
    private CanvasSizeOptionPane canvasSizeOptionPane = new CanvasSizeOptionPane(new CanvasResizeAction());
    /**
     * Embedded in the corner menu, and allows the user to set antialiasing on and off when required.
     */
    private JCheckBoxMenuItem antialiasOption = new JCheckBoxMenuItem();
    /**
     * Pencil cursor used with the pencil tool.
     */
    private Cursor pencil = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(ImageEditorPane.class.getResource("/resources/icons/cursor/pencil.png")).getImage(), new Point(10, 22), "pencil");
    /**
     * Bucket fill cursors used with the bucket fill tool.
     */
    private Cursor bucket = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(ImageEditorPane.class.getResource("/resources/icons/cursor/floodfill.png")).getImage(), new Point(22, 21), "bucket");
    /**
     * A nicer font for labels and such
     */
    private Font niceFont = new Font(this.getFont().getFamily(), Font.PLAIN, 10);
    /**
     * Pop up menu for save/load/use, canvas size, and antialiasing option.
     */
    private JPopupMenu cornerMenu = new JPopupMenu();
    /**
     * Button to invoke the popup corner menu.
     */
    private JButton menu = new JButton();
    /**
     * Allows the user to select whether or not line echoing should be used.
     */
    private JCheckBox echoOption = new JCheckBox("Line echo");
    /**
     * The text pane used for the text edit tool
     */
    private RichTextArea txtPane = new RichTextArea(RichTextToolbar.VERTICAL_LAYOUT);
    /**
     * Responds to non UI user input. Responsible for canvas interaction and painting.
     */
    private ImageEditorController controller;
    /**
     * File chooser for loading and saving images
     */
    JFileChooser choose = new JFileChooser();

    public ImageEditorPane() {
        //the controller takes care of almost all user interaction (mouse events on the canvas especially).
        controller = new ImageEditorController(this, canvas, toolbar, colourChooser);
        setUpGUIBoxLayout();
        toolbar.addImageToolbarListener(new ToolbarListener());
        menu.setFocusable(false);

        this.setFocusable(true);
        this.requestFocusInWindow();

        controller.resizeCanvas(300, 300);

    }

    /**
     * Set up the GUI.
     */
    private void setUpGUIBoxLayout() {
        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        txtMath.setSize(200, 20);
        txtMath.setFont(niceFont);
        txtMath.addKeyListener(new MathToolFieldListener());

        optionsScrollPane.setOpaque(false);
        optionsScrollPane.setPreferredSize(new Dimension(188, 300));
        optionsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        optionsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        canvasScrollPane.setOpaque(true); //should be false
        canvasScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        canvasScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        canvasScrollPane.setViewportView(canvas);
        canvasScrollPane.setBorder(BorderFactory.createLineBorder(borders));

        antialiasOption.setAction(new AntialiasChangeAction());
        antialiasOption.setText("Use Antialiasing");
        antialiasOption.setFont(niceFont);
        antialiasOption.setSelected(true);

        echoOption.setFont(niceFont);
        echoOption.setOpaque(false);
        echoOption.setFocusable(false);

        //set the thumbnail listeners
        colourChooser.addColourPreviewPropertyChangeListener(new ColourChangeListener());
        lineOptions.addThumbnailMouseListener(new ThumbnailMouseListener());
        arrowOptionPane.addThumbnailMouseListener(new ThumbnailMouseListener());
        eraserOptionPane.addThumbnailMouseListener(new ThumbnailMouseListener());
        pencilOptionPane.addThumbnailMouseListener(new ThumbnailMouseListener());
        shapeDrawOptionPane.addThumbnailMouseListener(new ThumbnailMouseListener());
//        stampOptionPane.addThumbnailMouseListener(new ThumbnailMouseListener());
        brightnessContrastOptionPane.addChangeListener(new BrightnessContrastChangeListener());
        brightnessContrastOptionPane.setActionApplyButton(new BrightnessContrastApplyAction());
        brightnessContrastOptionPane.setActionResetButton(new BrightnessContrastResetAction());

        selectionOptionPane.setRadioButtonActions(new SelectionOptionPaneAction());

        JMenuItem mnuitemLoadImage = new JMenuItem("Load Image");
        mnuitemLoadImage.setAction(new LoadAction());
        mnuitemLoadImage.setText("Load Image");
        mnuitemLoadImage.setFont(niceFont);
        JMenuItem mnuitemSaveImage = new JMenuItem("Save Image");
        mnuitemSaveImage.setAction(new SaveAction());
        mnuitemSaveImage.setText("Save Image");
        mnuitemSaveImage.setFont(niceFont);
        JMenuItem mnuItemUndo = new JMenuItem(new UndoAction());
        mnuItemUndo.setText("Undo");
        mnuItemUndo.setFont(niceFont);
        JMenuItem mnuItemRedo = new JMenuItem(new RedoAction());
        mnuItemRedo.setText("Redo");
        mnuItemRedo.setFont(niceFont);
        JMenu mnuZoom = new JMenu("Zoom");
        mnuZoom.setFont(niceFont);

        JMenuItem mnuCut = new JMenuItem(new CutAction());
        mnuCut.setFont(niceFont);
        JMenuItem mnuCopy = new JMenuItem(new CopyAction());
        mnuCopy.setFont(niceFont);
        JMenuItem mnuPaste = new JMenuItem(new PasteAction());
        mnuPaste.setFont(niceFont);
        JMenuItem mnuSelectAll = new JMenuItem(new SelectAllAction());
        mnuSelectAll.setFont(niceFont);


        ZoomAction zoom = new ZoomAction();

        JMenuItem mnuItemZoom1 = new JMenuItem(zoom);
        mnuItemZoom1.setText("100%");
        mnuItemZoom1.setFont(niceFont);
        mnuZoom.add(mnuItemZoom1);

        JMenuItem mnuItemZoomp5 = new JMenuItem(zoom);
        mnuItemZoomp5.setText("50%");
        mnuItemZoomp5.setFont(niceFont);
        mnuZoom.add(mnuItemZoomp5);

        JMenuItem mnuItemZoomp25 = new JMenuItem(zoom);
        mnuItemZoomp25.setText("25%");
        mnuItemZoomp25.setFont(niceFont);
        mnuZoom.add(mnuItemZoomp25);

        JMenuItem mnuItemZoom1p5 = new JMenuItem(zoom);
        mnuItemZoom1p5.setText("150%");
        mnuItemZoom1p5.setFont(niceFont);
        mnuZoom.add(mnuItemZoom1p5);

        JMenuItem mnuItemZoom2 = new JMenuItem(zoom);
        mnuItemZoom2.setText("200%");
        mnuItemZoom2.setFont(niceFont);
        mnuZoom.add(mnuItemZoom2);

        JMenuItem mnuItemZoom3 = new JMenuItem(zoom);
        mnuItemZoom3.setText("300%");
        mnuItemZoom3.setFont(niceFont);
        mnuZoom.add(mnuItemZoom3);

        JMenuItem mnuItemZoom4 = new JMenuItem(zoom);
        mnuItemZoom4.setText("400%");
        mnuItemZoom4.setFont(niceFont);
        mnuZoom.add(mnuItemZoom4);

        cornerMenu.setBorder(BorderFactory.createLineBorder(borders));
        cornerMenu.setFont(niceFont);
        cornerMenu.add(mnuitemLoadImage);
        cornerMenu.add(mnuitemSaveImage);
        cornerMenu.addSeparator();
        cornerMenu.add(mnuItemUndo);
        cornerMenu.add(mnuItemRedo);
        cornerMenu.add(mnuZoom);
        cornerMenu.addSeparator();
        cornerMenu.add(mnuSelectAll);
        cornerMenu.add(mnuCut);
        cornerMenu.add(mnuCopy);
        cornerMenu.add(mnuPaste);
        cornerMenu.addSeparator();
        cornerMenu.add(canvasSizeOptionPane);
        cornerMenu.addSeparator();
        cornerMenu.add(antialiasOption);

        choose.setFileView(new IconFileView());
        FileChooserPreviewPane preview = new FileChooserPreviewPane();
        choose.setAccessory(preview);
        choose.addPropertyChangeListener(preview);
        choose.setDragEnabled(false);

        menu.setAction(new MenuAction());
        menu.setText("Menu");
        menu.setFont(new Font(this.getFont().getFamily(), Font.PLAIN, 10));
        menu.setMargin(new Insets(1, 1, 1, 1));
        menu.setMaximumSize(new Dimension(38, 20));
        menu.setPreferredSize(new Dimension(38, 20));

        toolbar.setMaximumSize(toolbar.getPreferredSize());
        colourChooser.setMaximumSize(colourChooser.getPreferredSize());
        optionsScrollPane.setMaximumSize(new Dimension((int) colourChooser.getPreferredSize().getWidth(), 350));
        menu.setAlignmentX(LEFT_ALIGNMENT);
        toolbar.setAlignmentX(LEFT_ALIGNMENT);
        colourChooser.setAlignmentX(LEFT_ALIGNMENT);
        optionsScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        canvasScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        canvasScrollPane.setMinimumSize(new Dimension((int) toolbar.getPreferredSize().getWidth(), 200));

        Box v = Box.createVerticalBox();

        v.add(menu);
        v.add(optionsScrollPane);
        v.add(colourChooser);
        v.add(Box.createVerticalGlue());
        //add the first vertical box, containing menu button, options scroller and colour chooser
        v.setAlignmentX(LEFT_ALIGNMENT);
        this.add(v);

        //new vertical and horizontal boxes, add the toolbar
        v = Box.createVerticalBox();
        v.add(toolbar);
        v.add(canvasScrollPane);
        //add the second vertical panel containing the toolbar and canvas scroller
        v.setAlignmentX(LEFT_ALIGNMENT);
        this.add(v);

        canvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    /**
     * Sets up the key and action maps on the JComponent c.
     */
    public void setUpKeyBindings(JComponent c) {
        InputMap inMap = c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap aMap = c.getActionMap();

        inMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        inMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK), "selectAll");
        inMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "undo");
        inMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), "redo");
        inMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK & KeyEvent.SHIFT_DOWN_MASK), "redo");
        inMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "copy");
        inMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), "paste");
        inMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), "cut");

        aMap.put("undo", new UndoAction());
        aMap.put("redo", new RedoAction());
        aMap.put("cut", new CutAction());
        aMap.put("copy", new CopyAction());
        aMap.put("paste", new PasteAction());
        aMap.put("selectAll", new SelectAllAction());
        aMap.put("delete", new DeleteAction());


        c.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inMap);
    }

    /**
     * Sets the canvas BufferedImage (hence changing size) to the specified <code>image</code>. Clears the glass pane, and creates
     * a glass pane BufferedImage of equal size to the new image. Sets the zoom factor to 1.0.
     * @param image the image that should be set as the canvas document.
     */
    public void setDocumentImage(BufferedImage image) {
        canvas.setZoomFactor(1.0f);
        canvas.setCanvasBufferedImage(image);
        canvas.setGlassPaneBufferedImage(new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB));
        canvas.clearGlassPane();
    }

    /**
     * Checks whether or not the antialias menu option is selected or not.
     * @return whether or not the antialias menu option is selected or not. True means that the user wishes to
     *         use antialiasing.
     */
    public boolean isAntialiasSelected() {
        return antialiasOption.isSelected();
    }

    /**
     * Clears the canvas' undo/redo history.
     */
    public void clearCanvasUndoHistory() {
        canvas.clearUndoHistory();
    }

    /**
     * Gets a copy of the canvas image.
     * @return a copy of the canvas image.
     */
    public BufferedImage getCanvasImage() {
        return canvas.getCanvasBufferedImage().getSubimage(0, 0, canvas.getCanvasBufferedImage().getWidth(), canvas.getCanvasBufferedImage().getHeight());
    }

    /**
     * Gets the <code>LineOptionPane</code> used by this <code>ImageEditorPane</code>.
     * @return the <code>LineOptionPane</code> used by this <code>ImageEditorPane</code>.
     */
    public LineOptionPane getLineOptions() {
        return lineOptions;
    }

    /**
     * Gets the <code>ArrowOptionPane</code> used by this <code>ImageEditorPane</code>.
     * @return the <code>ArrowOptionPane</code> used by this <code>ImageEditorPane</code>.
     */
    public ArrowOptionPane getArrowOptions() {
        return arrowOptionPane;
    }

    /**
     * Gets the <code>ShapeDrawOptionPane</code> used by this <code>ImageEditorPane</code>.
     * @return the <code>ShapeDrawOptionPane</code> used by this <code>ImageEditorPane</code>.
     */
    public ShapeDrawOptionPane getShapeDrawOptions() {
        return shapeDrawOptionPane;
    }

    /**
     * Gets the <code>SelectionOptionPane</code> used by this <code>ImageEditorPane</code>.
     * @return the <code>SelectionOptionPane</code> used by this <code>ImageEditorPane</code>.
     */
    public SelectionOptionPane getSelectionOptions() {
        return selectionOptionPane;
    }

    /**
     * Gets the <code>PencilOptionPane</code> used by this <code>ImageEditorPane</code>.
     * @return the <code>PencilOptionPane</code> used by this <code>ImageEditorPane</code>.
     */
    public PencilOptionPane getPencilOptions() {
        return pencilOptionPane;
    }

    /**
     * Gets the <code>EraserOptionPane</code> used by this <code>ImageEditorPane</code>.
     * @return the <code>EraserOptionPane</code> used by this <code>ImageEditorPane</code>.
     */
    public EraserOptionPane getEraserOptions() {
        return eraserOptionPane;
    }

    /**
     * Gets the <code>StampOptionPane</code> used by this <code>ImageEditorPane</code>.
     * @return the <code>StampOptionPane</code> used by this <code>ImageEditorPane</code>.
     */
    public StampOptionPane getStampOptions() {
        return stampOptionPane;
    }

    /**
     * Gets the <code>BrightnessContrastOptionPane</code> used by this <code>ImageEditorPane</code>.
     * @return the <code>BrightnessContrastOptionPane</code> used by this <code>ImageEditorPane</code>.
     */
    public BrightnessContrastOptionPane getBrightnessContrastOptions() {
        return brightnessContrastOptionPane;
    }

    /**
     * Gets the <code>BucketFillOptionPane</code> used by this <code>ImageEditorPane</code>. This is the panel
     * that contains the tolerance slider allowing the user to set how sensitive the bucket fill tool is to change
     * in colour.
     * @return the <code>BucketFillOptionPane</code> used by this <code>ImageEditorPane</code>.
     */
    public BucketFillOptionPane getBucketFillOptionPane() {
        return bucketFillOptionPane;
    }

    /**
     * Gets the <code>RichTextArea</code> used by this <code>ImageEditorPane</code> for the text insert tool.
     * @return the <code>RichTextArea</code> used by this <code>ImageEditorPane</code> for the text insert tool.
     */
    public RichTextArea getTextPaneForTextTool() {
        return txtPane;
    }

    /**
     * Gets the <code>SimpleTextField</code> used by for the MathTeX tool.
     * @return the <code>SimpleTextField</code> used by for the MathTeX tool.
     */
    public JTextField getTextFieldForMathTool() {
        return txtMath;
    }

    /**
     * Gets the JScrollPane which holds the canvas.
     * @return the JScrollPane which holds the canvas.
     */
    public JScrollPane getCanvasScrollPane() {
        return canvasScrollPane;
    }

    public void updateEraserIcon() {
        Shape eraser = eraserOptionPane.getSelectedEraser();
        eraser = AffineTransform.getTranslateInstance(0 - eraser.getBounds().x, 0 - eraser.getBounds().y).createTransformedShape(eraser);
        BufferedImage b = new BufferedImage(eraser.getBounds().width + 1, eraser.getBounds().height + 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) b.createGraphics();
        g2d.setPaint(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, b.getWidth(), b.getHeight());
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.setPaint(new Color(255, 255, 255, 200));
        g2d.fill(eraser);
        g2d.setPaint(Color.black);
        g2d.draw(eraser);
        canvas.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(b, new Point((int) eraser.getBounds().getCenterX(), (int) eraser.getBounds().getCenterY()), "eraser"));
    }

    /**
     * Checks whether or not the line echo check box is selected or not.
     * @return boolean indicating whether or not the line echo check box is selected.
     */
    public boolean isLineEchoSelected() {
        return echoOption.isSelected();
    }

    /**
     * Action associated with the menu bar in the top left hand corner of the image editor.
     */
    public class MenuAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            cornerMenu.show(menu, menu.getX(), menu.getY());
        }
    }

    /**
     * Action associated with updating the canvas size through the top left corner menu, either
     * by pressing the accept button or hitting enter while in the focus of either of the text fields.
     */
    public class CanvasResizeAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            cornerMenu.setVisible(false);
            float zf = canvas.getZoomFactor();
            canvas.setZoomFactor(1.0f);
            controller.resizeCanvas(canvasSizeOptionPane.getSpecifiedSize().width, canvasSizeOptionPane.getSpecifiedSize().height);
            canvas.setZoomFactor(zf);
        }
    }

    /**
     * Action for undo.
     */
    public class UndoAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            canvas.undo();
            controller.paintCurrentSelection();
        }
    }

    /**
     * Action for redo.
     */
    public class RedoAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            canvas.redo();
            controller.paintCurrentSelection();
        }
    }

    /**
     * Save the current image.
     */
    public class SaveAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            int response = choose.showSaveDialog(ImageEditorPane.this);
            if (response == JFileChooser.APPROVE_OPTION) {
                if (choose.getSelectedFile().exists()) {
                    int resp = JOptionPane.showConfirmDialog(ImageEditorPane.this, "That file already exists. Overwrite?", "Save Image", JOptionPane.YES_NO_OPTION);
                    if (resp == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
                ImageUtils.saveImage(canvas.getCanvasBufferedImage(), "png", choose.getSelectedFile().getPath(), ImageEditorPane.this);
            }

        }
    }

    /**
     * Load an image (destroys current canvas).
     */
    public class LoadAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            int response = choose.showOpenDialog(ImageEditorPane.this);
            if (response == JFileChooser.APPROVE_OPTION) {
                if (choose.getSelectedFile().canRead() == false) {
                    JOptionPane.showMessageDialog(ImageEditorPane.this, "Cannot read from the specified location.", "Load Image", JOptionPane.OK_OPTION);
                    return;
                }
                System.gc();
                BufferedImage imgLoaded = ImageUtils.loadImage(choose.getSelectedFile().getPath(), ImageEditorPane.this);
                if (imgLoaded == null) {
                    JOptionPane.showMessageDialog(ImageEditorPane.this, "Image was not loaded successfully.", "Load Image", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                controller.selectionLostFocus(true);
                canvas.setPreferredSize(new Dimension(imgLoaded.getWidth(), imgLoaded.getHeight()));
                canvas.setCanvasBufferedImage(imgLoaded);
            }
        }
    }

    public class ZoomAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            if (((JMenuItem) e.getSource()).getText().equals("25%")) {
                canvas.setZoomFactor(0.25f);
            } else if (((JMenuItem) e.getSource()).getText().equals("50%")) {
                canvas.setZoomFactor(0.5f);
            } else if (((JMenuItem) e.getSource()).getText().equals("100%")) {
                canvas.setZoomFactor(1.0f);
            } else if (((JMenuItem) e.getSource()).getText().equals("150%")) {
                canvas.setZoomFactor(1.5f);
            } else if (((JMenuItem) e.getSource()).getText().equals("200%")) {
                canvas.setZoomFactor(2.0f);
            } else if (((JMenuItem) e.getSource()).getText().equals("300%")) {
                canvas.setZoomFactor(3.0f);
            } else if (((JMenuItem) e.getSource()).getText().equals("400%")) {
                canvas.setZoomFactor(4.0f);
            }
            controller.paintCurrentSelection();
        }
    }

    /**
     * updates each option pane so that it displays any previews, etc with the appropriate antialias option
     */
    public class AntialiasChangeAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            lineOptions.updateForAntialias(antialiasOption.isSelected());
            arrowOptionPane.updateForAntialias(antialiasOption.isSelected());
            brightnessContrastOptionPane.updateForAntialias(antialiasOption.isSelected());
            eraserOptionPane.updateForAntialias(antialiasOption.isSelected());
            pencilOptionPane.updateForAntialias(antialiasOption.isSelected());
            selectionOptionPane.updateForAntialias(antialiasOption.isSelected());
            shapeDrawOptionPane.updateForAntialias(antialiasOption.isSelected());
            stampOptionPane.updateForAntialias(antialiasOption.isSelected());
        }
    }

    /**
     * Listens for clicks on the toolbar buttons.
     */
    public class ToolbarListener implements ImageToolbarListener {

        public void buttonPress(ImageToolbarEvent e) {
            ImageEditorPane.this.toolbar.setOnlyButtonSelected(e.getEventID());
            txtMath.setText("");
            txtPane.setText("");
            txtMath.setVisible(false);
            txtPane.setVisible(false);
            optionsScrollPane.setBorder(BorderFactory.createEmptyBorder());
            optionsScrollPane.setColumnHeaderView(null);
            canvas.clearGlassPane();
            controller.paintCurrentSelection();
            canvas.repaint();
            //destroy the selection handles, as we do not want to draw them unless an
            //appropriate tool is selected
            controller.destroySelectionHandles();
            if (e.getEventID() == ImageToolbarEvent.DRAW_LINE_BUTTON) {
                controller.selectionLostFocus(false);
                optionsScrollPane.setViewportView(lineOptions);
                lineOptions.setSelectedIndex(2);
                lineOptions.setPreferredSize(lineOptions.getLayout().minimumLayoutSize(lineOptions));
                optionsScrollPane.setColumnHeaderView(echoOption);
                toolbar.setCurrentTool(e.getEventID());
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            } else if ((e.getEventID() == ImageToolbarEvent.DRAW_CIRCLE_BUTTON) || (e.getEventID() == ImageToolbarEvent.DRAW_RECTANGLE_BUTTON) || (e.getEventID() == ImageToolbarEvent.DRAW_ROUNDED_RECT_BUTTON) || (e.getEventID() == ImageToolbarEvent.DRAW_POLYGON_BUTTON)) {
                controller.selectionLostFocus(false);
                optionsScrollPane.setViewportView(shapeDrawOptionPane);
                shapeDrawOptionPane.setPreferredSize(shapeDrawOptionPane.getLayout().minimumLayoutSize(shapeDrawOptionPane));
                currentOptionPane = shapeDrawOptionPane;
                toolbar.setCurrentTool(e.getEventID());
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            } else if (e.getEventID() == ImageToolbarEvent.STAMP_BUTTON) {
                controller.selectionLostFocus(false);
                optionsScrollPane.setViewportView(stampOptionPane);
                stampOptionPane.setPreferredSize(stampOptionPane.getLayout().minimumLayoutSize(stampOptionPane));
                currentOptionPane = stampOptionPane;
                toolbar.setCurrentTool(e.getEventID());
            } else if (e.getEventID() == ImageToolbarEvent.ERASER_BUTTON) {
                controller.selectionLostFocus(false);
                optionsScrollPane.setViewportView(eraserOptionPane);
                eraserOptionPane.setPreferredSize(eraserOptionPane.getLayout().minimumLayoutSize(eraserOptionPane));
                currentOptionPane = eraserOptionPane;
                toolbar.setCurrentTool(e.getEventID());
                updateEraserIcon();
            } else if ((e.getEventID() == ImageToolbarEvent.SELECT_OVAL_BUTTON) || (e.getEventID() == ImageToolbarEvent.SELECT_POLYGON_BUTTON) || (e.getEventID() == ImageToolbarEvent.SELECT_RECT_BUTTON)) {
                optionsScrollPane.setViewportView(selectionOptionPane);
                selectionOptionPane.setPreferredSize(new Dimension(optionsScrollPane.getWidth() - 20, 60));
                currentOptionPane = selectionOptionPane;
                toolbar.setCurrentTool(e.getEventID());
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            } else if (e.getEventID() == ImageToolbarEvent.PENCIL_BUTTON) {
                controller.selectionLostFocus(false);
                optionsScrollPane.setViewportView(pencilOptionPane);
                pencilOptionPane.setPreferredSize(pencilOptionPane.getLayout().minimumLayoutSize(pencilOptionPane));
                currentOptionPane = pencilOptionPane;
                toolbar.setCurrentTool(e.getEventID());
                canvas.setCursor(pencil);
            } else if (e.getEventID() == ImageToolbarEvent.BRIGHTNESS_CONTRAST_BUTTON) {
                controller.paintCurrentSelection();
                optionsScrollPane.setViewportView(brightnessContrastOptionPane);
                brightnessContrastOptionPane.setPreferredSize(new Dimension(optionsScrollPane.getWidth() - 20, 60));
                currentOptionPane = brightnessContrastOptionPane;
                toolbar.setCurrentTool(e.getEventID());
            } else if (e.getEventID() == ImageToolbarEvent.MATH_BUTTON) {
                controller.selectionLostFocus(false);
                optionsScrollPane.setViewportView(jMathTeXOptionPane);
                currentOptionPane = jMathTeXOptionPane;
                optionsScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                canvas.add(txtMath);
                toolbar.setCurrentTool(e.getEventID());
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            } else if (e.getEventID() == ImageToolbarEvent.DRAW_ARROW_BUTTON) {
                controller.selectionLostFocus(false);
                optionsScrollPane.setViewportView(arrowOptionPane);
                arrowOptionPane.setPreferredSize(arrowOptionPane.getLayout().minimumLayoutSize(arrowOptionPane));
                currentOptionPane = arrowOptionPane;
                toolbar.setCurrentTool(e.getEventID());
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            } else if (e.getEventID() == ImageToolbarEvent.TEXT_BUTTON) {
                controller.selectionLostFocus(false);
                optionsScrollPane.setViewportView(null);
                optionsScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                optionsScrollPane.setViewportView(txtPane.getToolbar());
                txtPane.addKeyListener(new TextToolFieldListener());
                currentOptionPane = null;
                toolbar.setCurrentTool(e.getEventID());
                txtPane.getToolbar().setUseInsertPictureButton(false);
                txtPane.getToolbar().setUseTextColourButton(false);
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            } else if (e.getEventID() == ImageToolbarEvent.BUCKET_FILL_BUTTON) {
                controller.selectionLostFocus(false);
                optionsScrollPane.setViewportView(bucketFillOptionPane);
                bucketFillOptionPane.setPreferredSize(new Dimension(optionsScrollPane.getWidth() - 20, 60));
                currentOptionPane = bucketFillOptionPane;
                toolbar.setCurrentTool(e.getEventID());
                canvas.setCursor(bucket);
            } else if (e.getEventID() == ImageToolbarEvent.EYEDROPPER_BUTTON) {
                controller.selectionLostFocus(false);
                optionsScrollPane.setViewportView(null);
                optionsScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                currentOptionPane = null;
                toolbar.setCurrentTool(e.getEventID());
                canvas.setCursor(Cursor.getDefaultCursor());
            } else if (e.getEventID() == ImageToolbarEvent.CROP_BUTTON) {
                optionsScrollPane.setViewportView(null);
                optionsScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                currentOptionPane = null;
                controller.crop();
                controller.selectionLostFocus(false);
            } else if (e.getEventID() == ImageToolbarEvent.SCALE_BUTTON) {
                /*
                 * Rather than using 'setCurrentTool' on the toolbar so that the
                 * tool can be dealt with in the controllers if-else 'tool equals' structure,
                 * the scale tool sets a 'useSelectionHandles' flag within the controller.
                 * If the current tool is a selection tool, then it is left as one, else it is
                 * set to the rectangular selection tool. The user may then create new selections
                 * and resize these as well as resize the current selection. The toolbar leaves the
                 * resize icon selected during this time. If the resize icon is not selected, then
                 * the handles are no longer used when drawing selections. A scaleToolSelected flag within
                 * the controller is also set.
                 */
//
//                //if toolbar is anything but a selection tool, then set it to the rectangular selection tool
                if (!((toolbar.getCurrentTool() == ImageToolbarEvent.SELECT_OVAL_BUTTON) || (toolbar.getCurrentTool() == ImageToolbarEvent.SELECT_POLYGON_BUTTON) || (toolbar.getCurrentTool() == ImageToolbarEvent.SELECT_RECT_BUTTON))) {
                    toolbar.setCurrentTool(ImageToolbarEvent.SELECT_RECT_BUTTON);
                    toolbar.setOnlyButtonSelected(ImageToolbarEvent.SCALE_BUTTON);
                }
                optionsScrollPane.setViewportView(null);
                optionsScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                currentOptionPane = null;
                controller.createSelectionHandles();
                controller.setScaleToolSelected(true);
                controller.paintCurrentSelection();
            } else if (e.getEventID() == ImageToolbarEvent.ROTATE_BUTTON) {
                /*
                 * Rather than using 'setCurrentTool' on the toolbar so that the
                 * tool can be dealt with in the controllers if-else 'tool equals' structure,
                 * the rotate tool sets a 'useSelectionHandles' flag within the controller.
                 * If the current tool is a selection tool, then it is left as one, else it is
                 * set to the rectangular selection tool. The user may then create new selections
                 * and rotate these as well as rotate the current selection. The toolbar leaves the
                 * rotate icon selected during this time. If the rotate icon is not selected, then
                 * the handles are no longer used when drawing selections. A rotateToolSelected flag
                 * within the controller is also set.
                 */
                //if toolbar is anything but a selection tool, then set it to the rectangular selection tool
                if (!((toolbar.getCurrentTool() == ImageToolbarEvent.SELECT_OVAL_BUTTON) || (toolbar.getCurrentTool() == ImageToolbarEvent.SELECT_POLYGON_BUTTON) || (toolbar.getCurrentTool() == ImageToolbarEvent.SELECT_RECT_BUTTON))) {
                    toolbar.setCurrentTool(ImageToolbarEvent.SELECT_RECT_BUTTON);
                    toolbar.setOnlyButtonSelected(ImageToolbarEvent.SCALE_BUTTON);
                }
                optionsScrollPane.setViewportView(null);
                optionsScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                currentOptionPane = null;
                controller.createSelectionHandles();
                controller.setRotateToolSelected(true);
                controller.paintCurrentSelection();
            } else if (e.getEventID() == ImageToolbar.FLIP_HORIZONTAL) {
                controller.flipHorizontally();
            } else if (e.getEventID() == ImageToolbar.FLIP_VERTICAL) {
                controller.flipVertically();
            } else {
                optionsScrollPane.setViewportView(null);
                optionsScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                currentOptionPane = null;
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                controller.selectionLostFocus(false);
            }
        }
    }


    /**
     * Sets the current tool as the selection tool, with corresponding option pane. This method
     * is used by the controller when Ctrl+A is pressed, or some item is pasted, thus generating
     * a selection.
     */
    public void setToSelectTool() {
        optionsScrollPane.setViewportView(selectionOptionPane);
        selectionOptionPane.setPreferredSize(new Dimension(optionsScrollPane.getWidth() - 20, 60));
        currentOptionPane = selectionOptionPane;
        toolbar.setCurrentTool(ImageToolbarEvent.SELECT_RECT_BUTTON);
        canvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    /**
     * Sets the size displayed by the in-menu canvas size option pane.
     * @param dimension the new dimension to display
     */
    public void setMenuSizeDisplay(Dimension dimension) {
        canvasSizeOptionPane.setSpecifiedSize(dimension);
    }

    /**
     * Listens for a change on either the brightness or contrast sliders and
     * requests that the controller provide a preview of the altered image
     * on the canvas' glass pane.
     */
    private class BrightnessContrastChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (brightnessContrastOptionPane.isEitherSliderChanging()) {
                return;
            }
            controller.previewBrightnessAndContrast(brightnessContrastOptionPane.getBrightness(), ((double) brightnessContrastOptionPane.getContrast()) / 10.0);
        }
    }

    /**
     * Listens for keystrokes on the text tool field and resizes the text field
     * accordingly.
     */
    private class TextToolFieldListener implements KeyListener {

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            try {
                Document doc = txtPane.getDocument();
                Dimension d = txtPane.getSize();
                Rectangle r = txtPane.modelToView(txtPane.getDocument().getLength());
                d.height = r.y + r.height;

                if ((d.height + txtPane.getY()) > canvas.getCanvasBufferedImage().getHeight()) {
                    d.height = canvas.getCanvasBufferedImage().getHeight() - txtPane.getY() - 3;
                }
                txtPane.setSize(d);

                canvas.validate();
            } catch (Exception e2) {
            }
        }

        public void keyReleased(KeyEvent e) {
        }
    }

    /**
     * Listens for keystrokes on the math tool text field and paints a preview
     * as the currently selected image, just below the field.
     */
    private class MathToolFieldListener implements KeyListener {

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            BufferedImage preview;

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                TeXFormula formula = null;
                try {
                    formula = new TeXFormula(txtMath.getText());
                } catch (ParseException ex) {
                    preview = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                }

                if (txtMath.getText().isEmpty()) {
                    preview = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                }
                formula.setBackground(colourChooser.getSelectedBackgroundColour());
                formula.setColor(colourChooser.getSelectedForegroundColour());
                Icon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, jMathTeXOptionPane.getRenderSize());
                preview = new BufferedImage(icon.getIconWidth() + 1, icon.getIconHeight() + 5, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = preview.createGraphics();
                g2d.setColor(colourChooser.getSelectedBackgroundColour());
                g2d.fillRect(0, 0, preview.getWidth(), preview.getHeight());
                icon.paintIcon(new JLabel(), g2d, 0, 5);
                controller.setCurrentSelection(preview, txtMath.getX(), txtMath.getY() + txtMath.getHeight() + 2);
            }
        }

        public void keyReleased(KeyEvent e) {
        }
    }

    /**
     * Action for the brightness and contrast option pane apply button. Tells the
     * controller to apply the current settings to the canvas' canvas buffered image.
     * Resets the slider positions.
     */
    private class BrightnessContrastApplyAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            controller.setBrightnessAndContrast(brightnessContrastOptionPane.getBrightness(), ((double) brightnessContrastOptionPane.getContrast()) / 10.0);
            brightnessContrastOptionPane.setBrightness(0);
            brightnessContrastOptionPane.setContrast(1.0);
        }
    }

    /**
     * Action for the Brightness and Constrast option pane reset button. Clears the glass pane and resets the
     * position of the brightness and contrast sliders.
     */
    private class BrightnessContrastResetAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            canvas.clearGlassPane();
            brightnessContrastOptionPane.setBrightness(0);
            brightnessContrastOptionPane.setContrast(1.0);
        }
    }

    private class ColourChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("background")) {
                lineOptions.updateForNewColour(colourChooser.getSelectedForegroundColour(), colourChooser.getSelectedBackgroundColour());
                arrowOptionPane.updateForNewColour(colourChooser.getSelectedForegroundColour(), colourChooser.getSelectedBackgroundColour());
                brightnessContrastOptionPane.updateForNewColour(colourChooser.getSelectedForegroundColour(), colourChooser.getSelectedBackgroundColour());
                eraserOptionPane.updateForNewColour(colourChooser.getSelectedForegroundColour(), colourChooser.getSelectedBackgroundColour());
                pencilOptionPane.updateForNewColour(colourChooser.getSelectedForegroundColour(), colourChooser.getSelectedBackgroundColour());
                selectionOptionPane.updateForNewColour(colourChooser.getSelectedForegroundColour(), colourChooser.getSelectedBackgroundColour());
                shapeDrawOptionPane.updateForNewColour(colourChooser.getSelectedForegroundColour(), colourChooser.getSelectedBackgroundColour());
                stampOptionPane.updateForNewColour(colourChooser.getSelectedForegroundColour(), colourChooser.getSelectedBackgroundColour());
                new StyledEditorKit.ForegroundAction("Font colour", colourChooser.getSelectedForegroundColour()).actionPerformed(null);
                txtPane.setBackground(colourChooser.getSelectedBackgroundColour());
                //this is called of the controller in case the background colour changes - we want to see this reflected in the selection
                controller.updateSelectionTransparencyProperty();
                if (toolbar.getCurrentTool() == ImageToolbar.MATH) {
                    KeyListener[] keyListeners = txtMath.getKeyListeners();
                    for (int i = 0; i < keyListeners.length; i++) {
                        keyListeners[i].keyPressed(new KeyEvent(txtPane, 0, 0, 0, KeyEvent.VK_ENTER, '\n', 0));
                    }
                }
            }
        }
    }

    private class ThumbnailMouseListener implements MouseListener {

        public void mouseClicked(MouseEvent e) {
            Thumbnail t = null;
            if (e.getComponent() instanceof Thumbnail) {
                t = (Thumbnail) e.getComponent();
                ((ThumbnailPane) t.getParent().getParent()).setSelectedIndex(t.getIndex());
            } else {
                return;
            }
            lineOptions.rebuildSelf();
            arrowOptionPane.rebuildSelf();
            brightnessContrastOptionPane.rebuildSelf();
            eraserOptionPane.rebuildSelf();
            pencilOptionPane.rebuildSelf();
            selectionOptionPane.rebuildSelf();
            shapeDrawOptionPane.rebuildSelf();
            stampOptionPane.rebuildSelf();
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }

    private class SelectAllAction extends AbstractAction {

        public SelectAllAction() {
            super("Select All");
        }

        public void actionPerformed(ActionEvent e) {
            controller.selectAll();
        }
    }

    private class CopyAction extends AbstractAction {

        public CopyAction() {
            super("Copy");
        }

        public void actionPerformed(ActionEvent e) {
            controller.copy();
        }
    }

    private class PasteAction extends AbstractAction {

        public PasteAction() {
            super("Paste");
        }

        public void actionPerformed(ActionEvent e) {
            controller.paste();
        }
    }

    private class CutAction extends AbstractAction {

        public CutAction() {
            super("Cut");
        }

        public void actionPerformed(ActionEvent e) {
            controller.cut();
        }
    }

    private class DeleteAction extends AbstractAction {

        public DeleteAction() {
            super("Delete");
        }

        public void actionPerformed(ActionEvent e) {
            controller.selectionLostFocus(true);
        }
    }

    private class SelectionOptionPaneAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            controller.updateSelectionTransparencyProperty();
        }
    }
}

