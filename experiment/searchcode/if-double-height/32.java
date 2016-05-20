/*
 * ImageEditorController.java
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
package org.ingatan.image;

import org.ingatan.component.colour.ColourSwatchPane;
import org.ingatan.component.image.EditorCanvas;
import org.ingatan.component.image.ImageEditorPane;
import org.ingatan.component.image.ImageToolbar;
import org.ingatan.component.image.optionpanes.ShapeDrawOptionPane;
import org.ingatan.component.text.RichTextArea;
import org.ingatan.event.ImageToolbarEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

/**
 * This class is the controller for the ImageEditor. It ties together the following components:
 * <ul>
 *     <li>The <code>ImageToolbar</code>.</li>
 *     <li>The <code>EditorColourPane</code>.</li>
 *     <li>The <code>ToolOptionPane</code>s.</li>
 * </ul>
 *
 * This class looks at the current state of these components, for example the current colour,
 * the current tool selection, etc., and then responds to user interaction accordingly.<br>
 * For example; if the user has selected the line tool and clicks and drags across the canvas,
 * the controller will pass the canvas' <code>BufferedImage</code> graphics object to the
 * <code>ImageUtilities</code> class's <code>drawLine</code> method, along with the selected colour,=
 * and the selected line options.
 *
 * @author Thomas Everingham
 * @version 1.0
 */
public class ImageEditorController {

    /**
     * The editor pane that this <code>ImageEditorController</code> is controlling.
     */
    ImageEditorPane editorPane;
    /**
     * The canvas that this <code>ImageEditorController</code> is controlling.
     */
    EditorCanvas canvas;
    /**
     * The toolbar that this <code>ImageEditorController</code> is controlling.
     */
    ImageToolbar toolbar;
    /**
     * The colour swatch pane that this <code>ImageEditorController</code> is controlling.
     */
    ColourSwatchPane colourPane;
    /**
     * The previous point of mouse depression on the canvas.
     */
    Point2D previousMouseDepression;
    /**
     * The previous point of mouse release on the canvas.
     */
    Point2D previousMouseRelease;
    /**
     * Used to store 'the polygon so far' when drawing a polygon.
     */
    Path2D polygon;
    /**
     * Used to store 'the polygon so far' when drawing a polygon.
     */
    Path2D polygonSelection;
    /**
     * Indicates whether or not we are currently drawing a polygon.
     */
    boolean drawingPolygon = false;
    /**
     * Indicates whether or not we are currently selecting a polygon.
     */
    boolean selectingPolygon = false;
    /**
     * Whatever shape the current selection is; this is where it is stored.
     */
    Shape currentSelection;
    /**
     * Stores the new selection which has been made by the rotate tool. The rotate tool does not alter the current selection
     * because the shape is rotated from its original position each time a rotate occurs (every mouse drag).
     */
    Shape newSelection;
    /**
     * Rectangle defining the user-specified bounds for the placement of text (add-text tool)
     */
    Rectangle2D shapeTextPlacement;
    /**
     * This area is a combination of 8 small circles, one sitting on each of the four corners
     * of a selection.
     */
    Area selectionHandles;
    /**
     * Holds the current selection.
     */
    BufferedImage selectionSubImage;
    /**
     * Holds the currently copied image.
     */
    BufferedImage copy;
    /**
     * Indicates whether or not the selection has previously been dragged. If it hasn't,
     * then the controller fills the area where the selection used to be with the background colour.
     */
    boolean selectionBeenDragged = false;
    /**
     * Indicates the mouse position of the previous drag event.
     */
    Point2D previousMouseDragPosition;
    /**
     * This is set to true every time the mouse mouseButton released event is called.
     */
    boolean mouseHasReleased = false;
    /**
     * Indicates whether there is any selection currently made
     */
    boolean selectionExists = false;
    /**
     * Indicates whether a handle is being dragged
     */
    boolean draggingHandle = false;
    /**
     * Holds the modifier of a drag MouseEvent which indicates which mouseButton is 'doing the dragging'.
     */
    int mouseButton = 0;
    /**
     * Whether or not to draw selection handles. This flag is only set by the createSelectionHandles()
     * method, and only unset by the destroySelectionHandles() method.
     */
    boolean useSelectionHandles = false;
    /**
     * Which selection handle is being pulled.
     */
    int selectionHandle = -1;
    /**
     * the latest transform to be created from the currently selected subImage
     */
    BufferedImage latestTransform;
    /**
     * When an selection is being resized, this field holds the aspect ratio of the original selection.
     * The ratio is width:height.
     */
    double aspectRatio;
    /**
     * Flag indicating whether or not the scale tool is selected. This flag is required as
     * the scale tool works by setting the current tool to a selection tool, and setting
     * <code>useSelectionHandles</code> true. This second boolean, <code>scaleToolSelected</code> is needed
     * as the rotate tool works in a similar way, and there would be not way to differentiate otherwise.
     */
    boolean scaleToolSelected = false;
    /**
     * Flag indicating whether or not the rotate tool is selected. This flag is required, as
     * the rotate tool works by setting the current tool to a selection tool, and setting
     * <code>useSelectionHandles</code> true. This second boolean, <code>rotateToolSelected</code> is needed
     * as the scale tool works in a similar way, and there would be not way to differentiate otherwise.
     */
    boolean rotateToolSelected = false;
    /**
     * Flag indicating whether or not the user is currently editing text.
     */
    boolean editingText = false;
    /**
     * Passed to ImageUtils. This is the number of pixels between the different line angles.
     */
    double lineThreshold = 20;
    /**
     * Maximum canvas width can be set, so as to not exceed the memory allocated by the VM.
     */
    double maxCanvasWidth = 1280;
    /**
     * Maximum canvas heigth can be set, so as to not exceed the memory allocated by the VM.
     */
    double maxCanvasHeight = 1280;
    /**
     * This flag is set by such methods as the paste method, or the math method. These methods both
     * create a selection. An undo point must be created before this selection is merged.
     * The selectionLostFocus method checks to see whether or not this flag is set, and creates an undo point
     * accordingly.
     */
    boolean undoPointOnNextMerge = false;

    /**
     * Create a new instance of <code>ImageEditorController</code>.
     * @param editorPane the <code>ImageEditorPane</code> being controlled.
     * @param canvas the <code>EditorCanvas</code> being controlled.
     * @param toolbar the <code>ImageToolbar</code> being controlled.
     * @param colourPane the <code>ColourSwatchPane</code> being controlled.
     */
    public ImageEditorController(ImageEditorPane editorPane, EditorCanvas canvas, ImageToolbar toolbar, ColourSwatchPane colourPane) {
        this.editorPane = editorPane;
        this.canvas = canvas;
        this.toolbar = toolbar;
        this.colourPane = colourPane;

        canvas.addMouseListener(new CanvasListener());
        canvas.addMouseMotionListener(new CanvasMotionListener());
        canvas.addMouseWheelListener(new CanvasWheelListener());
    }

    /**
     * Disposes of the current selection. By specifying <code>delete</code> to
     * true, the selection will simply be removed, unless the selection has not yet
     * been dragged, in which case the selection will be filled with the background colour.
     * If the <code>delete</code> field is passed as false, then the selection image will be
     * merged with the underlying canvas image.
     * @param delete whether or not to delete the image.
     */
    public void selectionLostFocus(boolean delete) {
        //don't do anythign if no selection exists... just make sure all fields been set properly
        if (!selectionExists) {
            selectionBeenDragged = false;
            selectionSubImage = null;
            currentSelection = null;
            return;
        }

        //if the user is using the math tool, no undo point will have been set before this point.
        if (undoPointOnNextMerge) {
            /*the only time we do not want an undo point before merging this selection
             * where the selection is being deleted. The undoPointOnNextMerge flag
             * is only set for floating selections. So no change to the image will
             * occur on the event of the selection being deleted.
             */
            if (!delete) {
                canvas.setUndoPoint();
            }
            undoPointOnNextMerge = false;
        }

        Graphics2D g2d = (Graphics2D) canvas.getCanvasBufferedImage().createGraphics();

        //if not been dragged, then fill the background with the current background colour
        if (delete) {
            if (!selectionBeenDragged) {
                g2d.setPaint(colourPane.getSelectedBackgroundColour());
                g2d.fill(currentSelection);
            }
        } else {
            g2d.drawImage(selectionSubImage, (int) currentSelection.getBounds().getX(), (int) currentSelection.getBounds().getY(), canvas);
        }
        canvas.clearGlassPane();
        currentSelection = null;
        selectionExists = false;
        selectionSubImage = null;
        selectionBeenDragged = false;
        canvas.repaint();
    }

    /**
     * Merges the any current selection, and creates a new selection consisting of the entire canvas.
     */
    public void selectAll() {
        //if any selection currently exists, merge it down
        selectionLostFocus(false);
        /**
         * The selection is created by setting the selection flag to true, setting the selection shape
         * and then indicating that the selection has not been dragged. If the user tries to drag this selection, then
         * the drag event code will automatically create the sub image to be dragged (in this case, the whole image).
         * If the user deletes the image before the subimage is created, then we are simply deleting the whole image,
         * and this is consistent with the existing code.
         */
        selectionExists = true;
        currentSelection = new Rectangle2D.Double(0, 0, canvas.getCanvasBufferedImage().getWidth(), canvas.getCanvasBufferedImage().getHeight());
        selectionBeenDragged = false;
        /* We must also set the current tool to the rectangular selection tool so that any drag events will
         * result in the appropriate action. Any of the three selection tools would be fine for this,
         * but rectangular seems most logical.
         */
        toolbar.setCurrentTool(ImageToolbar.SELECT_RECT);
        editorPane.setToSelectTool();
        paintCurrentSelection();
    }

    /**
     * Set or unset the scaleToolSelected flag - this flag allows the controller to know whether the
     * rotate tool or scale tool is selected over the top of a selection tool. This method sets
     * scaleToolSelected = selected, and rotateToolSelected = !selected;
     * @param selected
     */
    public void setScaleToolSelected(boolean selected) {
        scaleToolSelected = selected;
        rotateToolSelected = !selected;
    }

    /**
     * Set or unset the rotateToolSelected flag - this flag allows the controller to know whether the
     * rotate tool or scale tool is selected over the top of a selection tool. This method sets
     * rotateToolSelected = selected, and scaleToolSelected = !selected;
     * @param selected
     */
    public void setRotateToolSelected(boolean selected) {
        rotateToolSelected = selected;
        scaleToolSelected = !selected;
    }

    /**
     * Checks whether the <code>scaleToolSelected</code> flag is set or not, this allows
     * the controller to tell whether it is the scale tool or rotate tool that is selected over the
     * top of the selection tool.
     * @return whether or not the <code>scaleToolSelected</code> is selected.
     */
    public boolean isScaleToolSelected() {
        return scaleToolSelected;
    }

    /**
     * Checks whether the <code>rotateToolSelected</code> flag is set or not, this allows
     * the controller to tell whether it is the scale tool or rotate tool that is selected over the
     * top of the selection tool.
     * @return whether or not the <code>rotateToolSelected</code> is selected.
     */
    public boolean isRotateToolSelected() {
        return rotateToolSelected;
    }

    /**
     * Crop the image around the current selection. If there is no selection, then
     * ignore.
     */
    public void crop() {

        if (selectionExists) {
            //if the selection has been dragged, then it already has a subimage
            if (!selectionBeenDragged) {
                selectionSubImage = ImageUtils.getSubImage(canvas.getCanvasBufferedImage(), currentSelection, colourPane.getSelectedBackgroundColourOpaque());
            }
            //resize the canvas to the size of the bounds of the current selection
            resizeCanvas(currentSelection.getBounds().width, currentSelection.getBounds().height);
            Graphics2D g2d = (Graphics2D) canvas.getCanvasBufferedImage().createGraphics();
            g2d.setColor(colourPane.getSelectedBackgroundColourOpaque());
            g2d.fillRect(0, 0, currentSelection.getBounds().width, currentSelection.getBounds().height);
            g2d.drawImage(selectionSubImage, 0, 0, canvas);
            selectionExists = false;
            currentSelection = null;
            selectionSubImage = null;
            selectionBeenDragged = false;
        } else {
            return;
        }
    }

    /**
     * Resize the canvas to the specified dimension (x,y). The <code>EditorCanvas</code>s
     * <code>setPreferredSize</code> method has been overriden to take care of the
     * resizing of <code>BufferedImage</code>s and the like.
     *
     * Any size outside the range [10,2000] will be set to either 10 or 2000, depending
     * on which bound it exceeds.
     * @param width the new width of the image.
     * @param height the new height of the image.
     */
    public void resizeCanvas(int width, int height) {
        if (width < 10) {
            width = 10;
        }
        if (height < 10) {
            height = 10;
        }

        if (width > maxCanvasWidth) {
            width = (int) maxCanvasWidth;
        }
        if (height > maxCanvasHeight) {
            height = (int) maxCanvasHeight;
        }

        System.gc();
        canvas.setUndoPoint();

        canvas.setPreferredSize(new Dimension(width, height));

        editorPane.setMenuSizeDisplay(canvas.getPreferredSize());
        canvas.repaint();
    }

    /**
     * Flip the current selection horizontally, or, if no current selection exists,
     * flip the entire image horizontally.
     */
    public void flipHorizontally() {
        if (currentSelection != null) {
            //flip current selection INCLUDING SHAPE

            //but make sure that the sub image exists first
            if (!selectionBeenDragged) {
                canvas.setUndoPoint();
                selectionSubImage = ImageUtils.getSubImage(canvas.getCanvasBufferedImage(), currentSelection, colourPane.getSelectedBackgroundColour());
                selectionBeenDragged = true;
            }

            BufferedImage newSubImage = new BufferedImage(selectionSubImage.getWidth(), selectionSubImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = newSubImage.createGraphics();
            g2d.drawImage(selectionSubImage, 0, 0, selectionSubImage.getWidth(), selectionSubImage.getHeight(), selectionSubImage.getWidth(), 0, 0, selectionSubImage.getHeight(), null);
            selectionSubImage = newSubImage;
            AffineTransform transform = AffineTransform.getTranslateInstance(currentSelection.getBounds().getCenterX() + currentSelection.getBounds().getWidth(), 0);
            transform.scale(-1, 1);
            transform.translate(-currentSelection.getBounds().getCenterX() + currentSelection.getBounds().getWidth(), 0);
            currentSelection = transform.createTransformedShape(currentSelection);
            paintCurrentSelection();

            canvas.repaint();
        } else {
            canvas.setUndoPoint();
            BufferedImage newCanvas = new BufferedImage(canvas.getCanvasBufferedImage().getWidth(), canvas.getCanvasBufferedImage().getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = newCanvas.createGraphics();
            g2d.drawImage(canvas.getCanvasBufferedImage(), 0, 0, canvas.getCanvasBufferedImage().getWidth(), canvas.getCanvasBufferedImage().getHeight(), canvas.getCanvasBufferedImage().getWidth(), 0, 0, canvas.getCanvasBufferedImage().getHeight(), null);
            canvas.setCanvasBufferedImage(newCanvas);
            canvas.repaint();
        }
    }

    /**
     * Flip the current selection vertically, or, if no current selection exists,
     * flip the entire image vertically.
     */
    public void flipVertically() {
        if (currentSelection != null) {
            //flip current selection INCLUDING SHAPE
            //but make sure that the sub image exists first
            if (!selectionBeenDragged) {
                canvas.setUndoPoint();
                selectionSubImage = ImageUtils.getSubImage(canvas.getCanvasBufferedImage(), currentSelection, colourPane.getSelectedBackgroundColour());
                selectionBeenDragged = true;
            }

            BufferedImage newSubImage = new BufferedImage(selectionSubImage.getWidth(), selectionSubImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = newSubImage.createGraphics();
            g2d.drawImage(selectionSubImage, 0, 0, selectionSubImage.getWidth(), selectionSubImage.getHeight(), 0, selectionSubImage.getHeight(), selectionSubImage.getWidth(), 0, null);
            selectionSubImage = newSubImage;
            AffineTransform transform = AffineTransform.getTranslateInstance(0, currentSelection.getBounds().getCenterY() + currentSelection.getBounds().getHeight());
            transform.scale(1, -1);
            transform.translate(0, -currentSelection.getBounds().getCenterY() + currentSelection.getBounds().getHeight());
            currentSelection = transform.createTransformedShape(currentSelection);
            paintCurrentSelection();

            canvas.repaint();
        } else {
            canvas.setUndoPoint();
            BufferedImage newCanvas = new BufferedImage(canvas.getCanvasBufferedImage().getWidth(), canvas.getCanvasBufferedImage().getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = newCanvas.createGraphics();
            g2d.drawImage(canvas.getCanvasBufferedImage(), 0, 0, canvas.getCanvasBufferedImage().getWidth(), canvas.getCanvasBufferedImage().getHeight(), 0, canvas.getCanvasBufferedImage().getHeight(), canvas.getCanvasBufferedImage().getWidth(), 0, null);
            canvas.setCanvasBufferedImage(newCanvas);
            canvas.repaint();
        }
    }

    public void previewBrightnessAndContrast(double brightnessOffset, double contrastScale) {
        BufferedImage src;
        BufferedImage dest;
        BufferedImage repainted;
        RescaleOp rescale = new RescaleOp((float) contrastScale, (float) brightnessOffset, null);

        if (currentSelection != null) {
            //flip current selection INCLUDING SHAPE
            //but make sure that the sub image exists first
            if (!selectionBeenDragged) {
                selectionSubImage = ImageUtils.getSubImage(canvas.getCanvasBufferedImage(), currentSelection, colourPane.getSelectedBackgroundColour());
                selectionBeenDragged = true;
            }
            src = new BufferedImage(selectionSubImage.getWidth(), selectionSubImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            dest = new BufferedImage(selectionSubImage.getWidth(), selectionSubImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            repainted = new BufferedImage(selectionSubImage.getWidth(), selectionSubImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            src.createGraphics().drawImage(selectionSubImage, 0, 0, null);
        } else {
            src = new BufferedImage(canvas.getCanvasBufferedImage().getWidth(), canvas.getCanvasBufferedImage().getHeight(), BufferedImage.TYPE_INT_RGB);
            dest = new BufferedImage(canvas.getCanvasBufferedImage().getWidth(), canvas.getCanvasBufferedImage().getHeight(), BufferedImage.TYPE_INT_RGB);
            repainted = new BufferedImage(canvas.getCanvasBufferedImage().getWidth(), canvas.getCanvasBufferedImage().getHeight(), BufferedImage.TYPE_INT_ARGB);
            src.createGraphics().drawImage(canvas.getCanvasBufferedImage(), 0, 0, null);
        }

        rescale.filter(src, dest);
        repainted.createGraphics().drawImage(dest, 0, 0, null);
        BufferedImage temp;

        //if working on a selection, then just redraw the selection
        if (currentSelection != null) {
            temp = selectionSubImage;
            selectionSubImage = repainted;
            paintCurrentSelection();
            selectionSubImage = temp;
        } else {
            canvas.setGlassPaneBufferedImage(repainted);
        }

        canvas.repaint();
    }

    public void setBrightnessAndContrast(double brightnessOffset, double contrastScale) {
        BufferedImage src;
        BufferedImage dest;
        BufferedImage repainted;
        RescaleOp rescale = new RescaleOp((float) contrastScale, (float) brightnessOffset, null);

        if (currentSelection != null) {
            //make sure that the sub image exists first
            if (!selectionBeenDragged) {
                selectionSubImage = ImageUtils.getSubImage(canvas.getCanvasBufferedImage(), currentSelection, colourPane.getSelectedBackgroundColour());
                selectionBeenDragged = true;
            }
            src = new BufferedImage(selectionSubImage.getWidth(), selectionSubImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            dest = new BufferedImage(selectionSubImage.getWidth(), selectionSubImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            repainted = new BufferedImage(selectionSubImage.getWidth(), selectionSubImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            src.createGraphics().drawImage(selectionSubImage, 0, 0, null);
        } else {
            src = new BufferedImage(canvas.getCanvasBufferedImage().getWidth(), canvas.getCanvasBufferedImage().getHeight(), BufferedImage.TYPE_INT_RGB);
            dest = new BufferedImage(canvas.getCanvasBufferedImage().getWidth(), canvas.getCanvasBufferedImage().getHeight(), BufferedImage.TYPE_INT_RGB);
            repainted = new BufferedImage(canvas.getCanvasBufferedImage().getWidth(), canvas.getCanvasBufferedImage().getHeight(), BufferedImage.TYPE_INT_ARGB);
            src.createGraphics().drawImage(canvas.getCanvasBufferedImage(), 0, 0, null);
        }

        rescale.filter(src, dest);
        repainted.createGraphics().drawImage(dest, 0, 0, null);
        BufferedImage temp;

        //if working on a selection, then just redraw the selection
        if (currentSelection != null) {
            selectionSubImage = repainted;
            paintCurrentSelection();
        } else {
            canvas.setUndoPoint();
            canvas.setCanvasBufferedImage(repainted);
        }

        canvas.repaint();
    }

    /**
     * If the selection transparency property has been changed, i.e. whether or not to paint
     * the current background colour as transparent, then the current selection sub-image must
     * be altered correspondingly.
     */
    public void updateSelectionTransparencyProperty() {
        //ignore this event if no selection available.
        if ((currentSelection == null) || (selectionExists == false)) {
            return;
        }

        //no need to worry about transparent pixels if there is no sub image.
        if (selectionSubImage == null) {
            return;
        }

        if (editorPane.getSelectionOptions().isBackgroundTransparent()) {
            ImageUtils.setColourTransparent(selectionSubImage, colourPane.getSelectedBackgroundColourOpaque());
        } else {
            ImageUtils.setTransparencyOpaque(selectionSubImage, colourPane.getSelectedBackgroundColourOpaque());
        }

        canvas.clearGlassPane();
        paintCurrentSelection();
    }

    /**
     * Merges any current selection down, and pastes any image data from the clipboard
     * as a new selection in the top left corner.
     */
    public void paste() {
        /*
         * Note! We must place the clipboard data into this variable
         * rather than straight into the selectionSubImage variable. This is
         * because if and only if we successfully retreive data from the clipboard,
         * we must use selectionLostFocus(boolean) to merge any current selection.
         * This will set selectionSubImage to null, and we will hence lose our data.
         */
        BufferedImage imgFromClipboard = null;

        /*if we are unsuccessful in obtaining an image, then we don't want to make
         * any changes to current selection state, etc. So we'll return on any exception.
         */
        try {
            imgFromClipboard = ImageUtils.getImageFromClipboard(true, true);
        } catch (UnsupportedFlavorException ex) {
            return;
        } catch (IOException ex) {
            return;
        }

        //double check that an image was retreived from the clipboard
        if (imgFromClipboard == null) {
            return;
        }

        if ((imgFromClipboard.getWidth() > canvas.getCanvasBufferedImage().getWidth()) || (imgFromClipboard.getHeight() > canvas.getCanvasBufferedImage().getHeight())) {
            int response = JOptionPane.showConfirmDialog(editorPane, "The image on the clipboard is bigger than the canvas.\nDo you want to resize the canvas to the size of the clipboard image?", "Paste Data", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                float zf = canvas.getZoomFactor();
                canvas.setZoomFactor(1.0f);
                this.resizeCanvas(imgFromClipboard.getWidth(), imgFromClipboard.getHeight());
                canvas.setZoomFactor(zf);
            }
        }

        //merge any current selection down
        selectionLostFocus(false);

        selectionSubImage = imgFromClipboard;

        //we now have a selection, so set the flag to true
        selectionExists = true;

        toolbar.dispatchToolbarEvent(new ImageToolbarEvent(toolbar, ImageToolbar.SELECT_RECT));

        /* we set this flag to true so that the mouse drag event knows
         * not to take a sub-image from the canvas, when the user tries to
         * drag the new paste.
         */
        selectionBeenDragged = true;
        undoPointOnNextMerge = true;
        currentSelection = new Rectangle2D.Double(0.0, 0.0, selectionSubImage.getWidth(), selectionSubImage.getHeight());
        updateSelectionTransparencyProperty();
    }

    /**
     * Set the specified <code>BufferedImage</code> as a new floating selection. Any old selection will be merged down first.
     * The selection will be positioned at the specified coordinate (x,y).
     * @param newSelection the <code>BufferedImage</code> to be used as the new selection.
     * @param x the x coordinate at which to position the selection.
     * @param y the y coordinate at which to position the selection.
     */
    public void setCurrentSelection(BufferedImage newSelection, int x, int y) {
        //won't merge any selection down in case the method using this method
        //produces a lot of selections. If any current selection needs to be
        //merged down, then a call to selectionLostFocus() can be made.
        selectionSubImage = newSelection;

        //we now have a selection, so set the flag to true
        selectionExists = true;
        /* we set this flag to true so that the mouse drag event knows
         * not to take a sub-image from the canvas, when the user tries to
         * drag the new paste.
         */
        selectionBeenDragged = true;
        undoPointOnNextMerge = true;
        currentSelection = new Rectangle2D.Double(x, y, selectionSubImage.getWidth(), selectionSubImage.getHeight());
        updateSelectionTransparencyProperty();

    }

    /**
     * Copies the current selection to the clipboard, if there is a current selection.
     */
    public void copy() {
        // Can't copy if nothing has been selected
        if (selectionExists) {
            // If the selection has not been dragged, then the sub image has not yet been created.
            if (!selectionBeenDragged) {
                selectionSubImage = ImageUtils.getSubImage(canvas.getCanvasBufferedImage(), currentSelection, colourPane.getSelectedBackgroundColour());
                if (editorPane.getSelectionOptions().isBackgroundTransparent()) {
                    ImageUtils.setColourTransparent(selectionSubImage, colourPane.getSelectedBackgroundColour());
                }
            }
            //now set this data to the clipboard
            ImageUtils.setImageToClipboard(selectionSubImage);
        }
    }

    /**
     * This is a convenience method which simply calls the <code>copy</code> method
     * of this class, and then deletes the current selection.
     */
    public void cut() {
        copy();
        canvas.setUndoPoint();
        selectionLostFocus(true);
    }

    /**
     * Creates 8 handles for the current selection as an area. This means that
     * area.contains(point) can be used to determine whether or not the mouse is
     * interacting with a handle. The handles will be drawn onto every selection
     * until the destroySelectionHandles() method is called.
     * @return an array of <code>Ellipse2D</code> shapes which can be used to determine which handle was clicked, etc.
     *         the array holds the handles in the following order: NW, N, NE, E, SE, S, SW, W.
     */
    public Ellipse2D[] createSelectionHandles() {
        if (currentSelection == null) {
            return null;
        }
        Ellipse2D NW = new Ellipse2D.Double(currentSelection.getBounds().getX() - 3, currentSelection.getBounds().getY() - 2, 6, 6);
        Ellipse2D N = new Ellipse2D.Double(currentSelection.getBounds().getCenterX() - 3, currentSelection.getBounds().getY() - 3, 6, 6);
        Ellipse2D NE = new Ellipse2D.Double(currentSelection.getBounds().getX() + currentSelection.getBounds().getWidth() - 3, currentSelection.getBounds().getY() - 3, 6, 6);
        Ellipse2D E = new Ellipse2D.Double(currentSelection.getBounds().getX() + currentSelection.getBounds().getWidth() - 3, currentSelection.getBounds().getCenterY() - 3, 6, 6);
        Ellipse2D SE = new Ellipse2D.Double(currentSelection.getBounds().getX() + currentSelection.getBounds().getWidth() - 3, currentSelection.getBounds().getY() + currentSelection.getBounds().getHeight() - 3, 6, 6);
        Ellipse2D S = new Ellipse2D.Double(currentSelection.getBounds().getCenterX() - 3, currentSelection.getBounds().getY() + currentSelection.getBounds().getHeight() - 3, 6, 6);
        Ellipse2D SW = new Ellipse2D.Double(currentSelection.getBounds().getX() - 3, currentSelection.getBounds().getY() + currentSelection.getBounds().getHeight() - 3, 6, 6);
        Ellipse2D W = new Ellipse2D.Double(currentSelection.getBounds().getX() - 3, currentSelection.getBounds().getCenterY() - 3, 6, 6);

        Ellipse2D handles[] = new Ellipse2D[]{NW, N, NE, E, SE, S, SW, W};

        Area retVal = new Area(NW);
        retVal.add(new Area(N));
        retVal.add(new Area(NE));
        retVal.add(new Area(E));
        retVal.add(new Area(SE));
        retVal.add(new Area(S));
        retVal.add(new Area(SW));
        retVal.add(new Area(W));

        selectionHandles = retVal;
        useSelectionHandles = true;
        return handles;
    }

    /**
     * No longer use the selection handles.
     */
    public void destroySelectionHandles() {
        useSelectionHandles = false;
    }

    /**
     * Painting code for any current selection and selection handles
     */
    public void paintCurrentSelection() {
        //can't go painting a non-existant selection, now can we?
        if (currentSelection == null) {
            return;
        }

        canvas.clearGlassPane();
        Graphics2D g2d = (Graphics2D) canvas.getGlassPaneBufferedImage().createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        if (selectionBeenDragged) {
            g2d.drawImage(selectionSubImage, (int) currentSelection.getBounds().getX(), (int) currentSelection.getBounds().getY(), canvas);
        }

        g2d.setStroke(new BasicStroke(1 / canvas.getZoomFactor(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4, 3}, 3.0f));
        g2d.setPaint(new Color(0, 0, 0));
        g2d.draw(currentSelection);
        g2d.setStroke(new BasicStroke(1 / canvas.getZoomFactor(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4, 3}, 0.0f));
        g2d.setPaint(new Color(255, 255, 255));
        g2d.draw(currentSelection);

        if (useSelectionHandles) {
            g2d.setColor(new Color(222, 233, 233));
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.fill(selectionHandles);
            g2d.setPaint(new Color(26, 97, 110));
            g2d.draw(selectionHandles);
        }
        canvas.repaint();
    }
    /*FOR TEXT ENTRY: the user clicks the text entry tool and marks out the bounds for the text. A RichTextArea
     * appears there, and once it loses focus, the text is converted into a BufferedImage selection...
     * the tool will have now changed to 'rectangular selection', and so the selection option pane will be available to
     * edit text bg transparency.
     */

    private class CanvasListener implements MouseListener {

        public void mouseClicked(MouseEvent e) {
            //DRAWING A POLYGON
            if (toolbar.getCurrentTool() == ImageToolbar.DRAW_POLY) {
                if ((e.getClickCount() == 2) && (drawingPolygon)) {
                    polygon.closePath();
                    canvas.setUndoPoint();
                    int drawPolicy = editorPane.getShapeDrawOptions().getDrawPolicy();
                    Graphics g = canvas.getCanvasBufferedImage().createGraphics();
                    Graphics2D g2d = (Graphics2D) g;

                    g2d.setStroke(editorPane.getShapeDrawOptions().getSelectedStroke());
                    if (editorPane.isAntialiasSelected()) {
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    } else {
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                    }

                    if (drawPolicy == ShapeDrawOptionPane.FILL_AND_STROKE) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            g2d.setPaint(colourPane.getSelectedBackgroundColour());
                        } else {
                            g2d.setPaint(colourPane.getSelectedForegroundColour());
                        }

                        g2d.fill(polygon);
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            g2d.setPaint(colourPane.getSelectedForegroundColour());
                        } else {
                            g2d.setPaint(colourPane.getSelectedBackgroundColour());
                        }
                        g2d.draw(polygon);
                    } else if (drawPolicy == ShapeDrawOptionPane.FILL_ONLY) {
                        g2d.setPaint(colourPane.getSelectedBackgroundColour());
                        g2d.fill(polygon);
                    } else if (drawPolicy == ShapeDrawOptionPane.STROKE_ONLY) {
                        g2d.setPaint(colourPane.getSelectedForegroundColour());
                        g2d.draw(polygon);
                    }

                    canvas.clearGlassPane();
                    canvas.repaint();

                    drawingPolygon = false;
                }
                //SELECTING A POLYGON
            } else if (toolbar.getCurrentTool() == ImageToolbar.SELECT_POLY) {
                if ((e.getClickCount() == 2) && (selectingPolygon)) {
                    polygonSelection.closePath();
                    currentSelection = polygonSelection;
                    selectionExists = true;

                    //any selection drawing goes here

                    selectingPolygon = false;
                }
                //PERFORMING BUCKET-FILL
            } else if (toolbar.getCurrentTool() == ImageToolbar.BUCKET_FILL) {
                canvas.setUndoPoint();
                if (e.getButton() == MouseEvent.BUTTON1) {
                    ImageUtils.floodFill(canvas.getCanvasBufferedImage(), e.getX(), e.getY(),
                            new Color(canvas.getCanvasBufferedImage().getRGB(e.getX(), e.getY())),
                            colourPane.getSelectedForegroundColour(), editorPane.getBucketFillOptionPane().getTolerance());
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    ImageUtils.floodFill(canvas.getCanvasBufferedImage(), e.getX(), e.getY(),
                            new Color(canvas.getCanvasBufferedImage().getRGB(e.getX(), e.getY())),
                            colourPane.getSelectedBackgroundColour(), editorPane.getBucketFillOptionPane().getTolerance());
                }
                canvas.repaint();
            } //STAMP TOOL
            else if (toolbar.getCurrentTool() == ImageToolbar.STAMP) {
                canvas.setUndoPoint();
                Graphics2D g2d = (Graphics2D) canvas.getCanvasBufferedImage().createGraphics();
                g2d.drawImage(editorPane.getStampOptions().getSelectedStamp(), e.getX(), e.getY(), canvas);
                canvas.repaint();
            } //STAMP TOOL
            else if (toolbar.getCurrentTool() == ImageToolbar.PENCIL) {
                Shape pencil = editorPane.getPencilOptions().getSelectedPencil();
                Graphics g = canvas.getCanvasBufferedImage().createGraphics();
                Graphics2D g2d = (Graphics2D) g;

                Color c;
                if (mouseButton == MouseEvent.BUTTON1) {
                    c = colourPane.getSelectedForegroundColour();
                } else {
                    c = colourPane.getSelectedBackgroundColour();
                }

                if (editorPane.isAntialiasSelected()) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }

                g2d.setPaint(c);

                pencil = AffineTransform.getTranslateInstance(e.getX() - pencil.getBounds().getCenterX(), e.getY() - pencil.getBounds().getCenterY()).createTransformedShape(pencil);
                g2d.fill(pencil);
                canvas.repaint();
            } //ERASER TOOL
            else if (toolbar.getCurrentTool() == ImageToolbar.ERASER) {
                Shape eraser = editorPane.getEraserOptions().getSelectedEraser();
                Graphics g = canvas.getCanvasBufferedImage().createGraphics();
                Graphics2D g2d = (Graphics2D) g;

                Color c = colourPane.getSelectedBackgroundColour();

                if (editorPane.isAntialiasSelected()) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }

                g2d.setPaint(c);

                eraser = AffineTransform.getTranslateInstance(e.getX() - eraser.getBounds().getCenterX(), e.getY() - eraser.getBounds().getCenterY()).createTransformedShape(eraser);
                if (mouseButton == MouseEvent.BUTTON1) {
                    g2d.fill(eraser);
                } else {
                    ImageUtils.replaceColour(canvas.getCanvasBufferedImage(), eraser, colourPane.getSelectedForegroundColour(), colourPane.getSelectedBackgroundColour());
                }
                canvas.repaint();
            }//TEXT TOOL
            else if (toolbar.getCurrentTool() == ImageToolbar.TEXT) {
                RichTextArea txtPane = editorPane.getTextPaneForTextTool();
                //turn the current text into a selection.
                if (!txtPane.getText().isEmpty()) {

                    //RESIZE THE TEXT AREA TO ITS CONTENT-----------------
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
                    } //------------------------------------------------

                    canvas.setUndoPoint();
                    txtPane.setBorder(BorderFactory.createEmptyBorder());

                    //we can have a transparent background, if required
                    if (editorPane.getSelectionOptions().isBackgroundTransparent()) {
                        txtPane.setOpaque(false);
                    } else {
                        txtPane.setBackground(colourPane.getSelectedBackgroundColour());
                    }

                    //select none so that we do not paint the highlight of any selected text
                    txtPane.select(0, 0);
                    txtPane.getCaret().setVisible(false);
                    selectionExists = true;
                    selectionBeenDragged = true;
                    currentSelection = txtPane.getBounds();
                    BufferedImage temp = new BufferedImage(txtPane.getWidth(), txtPane.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    txtPane.paint(temp.createGraphics());
                    selectionSubImage = temp;
                    paintCurrentSelection();
                    txtPane.setOpaque(true);
                    txtPane.getCaret().setVisible(true);
                }
                txtPane.setText("");
                txtPane.setVisible(false);
                editorPane.requestFocus();
                toolbar.fireButtonPress(ImageToolbar.SELECT_RECT);
            } else if (toolbar.getCurrentTool() == ImageToolbar.MATH) {
                //if the current selection(i.e. math preview) contains this
                //mouse click, then we do not wish to change anything.
                if ((currentSelection == null) || !(currentSelection.contains(e.getPoint()))) {

                    JTextField txtMath = editorPane.getTextFieldForMathTool();

                    txtMath.setVisible(!txtMath.isVisible());
                    if (!txtMath.isVisible()) {
                        editorPane.requestFocus();
                    }
                    txtMath.setText("");

                    int currentHeight = txtMath.getHeight();
                    //(txtMath.getHeight() + currentSelection.getBounds().height) is the total height of the
                    //math text field and the current preview
                    if (currentSelection != null) {
                        currentHeight += currentSelection.getBounds().height;
                    }

                    boolean xPositionOkay = ((e.getX() + txtMath.getWidth()) < canvas.getWidth());
                    boolean yPositionOkay = ((e.getY() + currentHeight) < canvas.getHeight());


                    if (xPositionOkay && yPositionOkay) {
                        txtMath.setLocation(e.getPoint());
                    } else if (xPositionOkay && !yPositionOkay) {
                        txtMath.setLocation(e.getX(), canvas.getHeight() - currentHeight);
                    } else if (!xPositionOkay && yPositionOkay) {
                        txtMath.setLocation(canvas.getWidth() - txtMath.getWidth(), e.getY());
                    } else if (!xPositionOkay && !yPositionOkay) {
                        txtMath.setLocation(canvas.getWidth() - txtMath.getWidth(), canvas.getHeight() - currentHeight);
                    }
                }
            } else if (toolbar.getCurrentTool() == ImageToolbar.EYEDROPPER) {
                Color selectedColour = new Color(canvas.getCanvasBufferedImage().getRGB(e.getX(), e.getY()));
                if (e.getButton() == MouseEvent.BUTTON1) {
                    colourPane.setForegroundColour(selectedColour);
                } else {
                    colourPane.setBackgroundColour(selectedColour);
                }
            }
        }

        public void mousePressed(MouseEvent e) {

            Line2D line = null;
            boolean isCtrlDown = ((e.getModifiers() & ActionEvent.CTRL_MASK) > 0);
            //if we are using the draw polygon tool
            if (toolbar.getCurrentTool() == ImageToolbar.DRAW_POLY) {
                //and we've already started drawing a polygon
                if (drawingPolygon) {
                    //then append the line created between the click point and the previous click point.
                    line = ImageUtils.makeLineStraight(previousMouseDepression.getX(), previousMouseDepression.getY(), e.getX(), e.getY(), lineThreshold, isCtrlDown);
                    polygon.append(line, true);
                } else {
                    //otherwise.. start drawing a polygon by creating a new GeneralPath and setting the
                    //drawingPolygon flag.
                    polygon = new GeneralPath();
                    drawingPolygon = true;
                }
            }//if we're using the select polygon tool
            else if (toolbar.getCurrentTool() == ImageToolbar.SELECT_POLY) {
                //and we've already started selecting
                if (selectingPolygon) {
                    //append the line created between the click point and the previous click point
                    line = ImageUtils.makeLineStraight(previousMouseDepression.getX(), previousMouseDepression.getY(), e.getX(), e.getY(), lineThreshold, isCtrlDown);
                    polygonSelection.append(line, true);
                } else {
                    //or start selecting a new polygon.. but make sure we deal with any other selection
                    if (!selectionExists) {
                        polygonSelection = new GeneralPath();
                        selectingPolygon = true;
                    }

                }
            } else if ((toolbar.getCurrentTool() == ImageToolbar.TEXT) && (editingText)) {
                editingText = false;
                selectionLostFocus(false);
                canvas.repaint();
            } else if ((toolbar.getCurrentTool() == ImageToolbar.PENCIL) || (toolbar.getCurrentTool() == ImageToolbar.ERASER)) {
                canvas.setUndoPoint();
            }

            if ((selectionExists) && (useSelectionHandles)) {
                if (selectionHandles.contains(e.getPoint())) {
                    draggingHandle = true;
                    selectionHandle = -1;
                    aspectRatio = currentSelection.getBounds().getHeight() / currentSelection.getBounds().getWidth();
                    Ellipse2D handles[] = createSelectionHandles();
                    for (int i = 0; i < handles.length; i++) {
                        if (handles[i].contains(e.getPoint())) {
                            selectionHandle = i;
                            break;
                        }
                    }
                }
            }

            /*
             * This is where any selection is destroyed if an area outside the selection is pressed. This
             * ensures that if we already have a selection, and a new one is created by drag somewhere else,
             * that the selection has already been destroyed before the drag code is reached.
             */
            if (selectionExists) {
                if (useSelectionHandles) {
                    //not contained by selection handles OR by the selection
                    if ((selectionHandles.contains(e.getPoint()) == false) && (currentSelection.contains(e.getPoint()) == false)) {
                        selectionLostFocus(false);
                    }
                } //if we're not using selection handles, just check if the mouse click is inside the selection or not
                else if (currentSelection.contains(e.getPoint()) == false) {
                    selectionLostFocus(false);
                }
            }


            previousMouseDepression = e.getPoint();
            //mouseButton variable set for the draw polygon function so that if the previous
            //mouse click was the right hand button, the preview colours can be inverted fg/bg to bg/fg
            mouseButton = e.getButton();

            //if the ctrl key is down, we have straightened the line, and so the current mouse depression
            //is actually at the end of this line.
            if (isCtrlDown && ((toolbar.getCurrentTool() == ImageToolbar.DRAW_POLY) || (toolbar.getCurrentTool() == ImageToolbar.SELECT_POLY))) {
                previousMouseDepression.setLocation(line.getP2());
            }
        }

        public void mouseReleased(MouseEvent e) {
            mouseHasReleased = true;
            previousMouseRelease = e.getPoint();

            boolean isCtrlDown = ((e.getModifiers() & ActionEvent.CTRL_MASK) > 0);

            //<editor-fold defaultstate="collapsed" desc="line draw code">
            if (toolbar.getCurrentTool() == ImageToolbar.DRAW_LINE) {
                canvas.setUndoPoint();
                //clear the glass pane of any other construction lines..
                canvas.clearGlassPane();
                //draw a line onto the glass pane.
                Graphics g = canvas.getCanvasBufferedImage().getGraphics();
                Graphics2D g2d = (Graphics2D) g;
                if (editorPane.isAntialiasSelected()) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }
                g2d.setStroke(editorPane.getLineOptions().getSelectedStroke());
                //set the paint colour to a slightly transparent version of what is selected.
                if (e.getButton() == MouseEvent.BUTTON1) {
                    g2d.setPaint(colourPane.getSelectedForegroundColour());
                } else {
                    g2d.setPaint(colourPane.getSelectedBackgroundColour());
                }
                g2d.draw(ImageUtils.makeLineStraight(previousMouseDepression.getX(), previousMouseDepression.getY(),
                        previousMouseRelease.getX(), previousMouseRelease.getY(), lineThreshold, isCtrlDown));
                canvas.repaint();
                //</editor-fold>
            } //<editor-fold defaultstate="collapsed" desc="arrow draw code">
            else if (toolbar.getCurrentTool() == ImageToolbar.DRAW_ARROW) {
                canvas.setUndoPoint();
                //clear the glass pane of any other construction lines..
                canvas.clearGlassPane();
                //draw a line onto the glass pane.
                Graphics g = canvas.getCanvasBufferedImage().getGraphics();
                Graphics2D g2d = (Graphics2D) g;
                if (editorPane.isAntialiasSelected()) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }
                g2d.setStroke(editorPane.getArrowOptions().getSelectedStroke());
                boolean fill = editorPane.getArrowOptions().getFillArrow();

                Path2D toDraw = ImageUtils.setArrowAlongLine(editorPane.getArrowOptions().getSelectedArrow(),
                        ImageUtils.makeLineStraight(previousMouseDepression.getX(), previousMouseDepression.getY(), e.getX(), e.getY(), lineThreshold, isCtrlDown));

                if (fill) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        g2d.setPaint(colourPane.getSelectedBackgroundColour());
                    } else {
                        g2d.setPaint(colourPane.getSelectedForegroundColour());
                    }
                    g2d.fill(toDraw);
                }
                if (e.getButton() == MouseEvent.BUTTON1) {
                    g2d.setPaint(colourPane.getSelectedForegroundColour());
                } else {
                    g2d.setPaint(colourPane.getSelectedBackgroundColour());
                }
                g2d.draw(toDraw);
                canvas.repaint();
                //</editor-fold>
            } //<editor-fold defaultstate="collapsed" desc="rectangle draw code">
            else if (toolbar.getCurrentTool() == ImageToolbar.DRAW_RECT) {
                canvas.setUndoPoint();
                //clear the glass pane of any other construction lines..
                canvas.clearGlassPane();
                //draw a line onto the glass pane.
                Graphics g = canvas.getCanvasBufferedImage().createGraphics();
                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(editorPane.getShapeDrawOptions().getSelectedStroke());
                if (editorPane.isAntialiasSelected()) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }
                int drawPolicy = editorPane.getShapeDrawOptions().getDrawPolicy();

                Rectangle2D rectToDraw = ImageUtils.mouseRectToLogicalRect((Point) previousMouseDepression, (Point) previousMouseRelease, isCtrlDown);

                if (drawPolicy == ShapeDrawOptionPane.FILL_AND_STROKE) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        g2d.setPaint(colourPane.getSelectedBackgroundColour());
                    } else {
                        g2d.setPaint(colourPane.getSelectedForegroundColour());
                    }
                    g2d.fill(rectToDraw);
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        g2d.setPaint(colourPane.getSelectedForegroundColour());
                    } else {
                        g2d.setPaint(colourPane.getSelectedBackgroundColour());
                    }
                    g2d.draw(rectToDraw);
                } else if (drawPolicy == ShapeDrawOptionPane.STROKE_ONLY) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        g2d.setPaint(colourPane.getSelectedForegroundColour());
                    } else {
                        g2d.setPaint(colourPane.getSelectedBackgroundColour());
                    }
                    g2d.draw(rectToDraw);
                } else if (drawPolicy == ShapeDrawOptionPane.FILL_ONLY) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        g2d.setPaint(colourPane.getSelectedBackgroundColour());
                    } else {
                        g2d.setPaint(colourPane.getSelectedForegroundColour());
                    }
                    g2d.fill(rectToDraw);
                }
                canvas.repaint();
                //</editor-fold>
            } //<editor-fold defaultstate="collapsed" desc="oval draw code">
            else if (toolbar.getCurrentTool() == ImageToolbar.DRAW_OVAL) {
                canvas.setUndoPoint();
                //clear the glass pane of any other construction lines..
                canvas.clearGlassPane();
                //draw a line onto the glass pane.
                Graphics g = canvas.getCanvasBufferedImage().createGraphics();
                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(editorPane.getShapeDrawOptions().getSelectedStroke());
                if (editorPane.isAntialiasSelected()) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }
                int drawPolicy = editorPane.getShapeDrawOptions().getDrawPolicy();

                Ellipse2D ovalToDraw = null;
                Rectangle2D bounds = ImageUtils.mouseRectToLogicalRect((Point) previousMouseDepression, (Point) previousMouseRelease, isCtrlDown);
                ovalToDraw = new Ellipse2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());

                if (drawPolicy == ShapeDrawOptionPane.FILL_AND_STROKE) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        g2d.setPaint(colourPane.getSelectedBackgroundColour());
                    } else {
                        g2d.setPaint(colourPane.getSelectedForegroundColour());
                    }
                    g2d.fill(ovalToDraw);
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        g2d.setPaint(colourPane.getSelectedForegroundColour());
                    } else {
                        g2d.setPaint(colourPane.getSelectedBackgroundColour());
                    }
                    g2d.draw(ovalToDraw);
                } else if (drawPolicy == ShapeDrawOptionPane.STROKE_ONLY) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        g2d.setPaint(colourPane.getSelectedForegroundColour());
                    } else {
                        g2d.setPaint(colourPane.getSelectedBackgroundColour());
                    }
                    g2d.draw(ovalToDraw);
                } else if (drawPolicy == ShapeDrawOptionPane.FILL_ONLY) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        g2d.setPaint(colourPane.getSelectedBackgroundColour());
                    } else {
                        g2d.setPaint(colourPane.getSelectedForegroundColour());
                    }
                    g2d.fill(ovalToDraw);
                }
                canvas.repaint();
                //</editor-fold>
            } //<editor-fold defaultstate="collapsed" desc="rounded rectangle draw code">
            else if (toolbar.getCurrentTool() == ImageToolbar.DRAW_ROUNDED_RECT) {
                canvas.setUndoPoint();
                //clear the glass pane of any other construction lines..
                canvas.clearGlassPane();
                //draw a line onto the glass pane.
                Graphics g = canvas.getCanvasBufferedImage().createGraphics();
                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(editorPane.getShapeDrawOptions().getSelectedStroke());
                if (editorPane.isAntialiasSelected()) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }
                int drawPolicy = editorPane.getShapeDrawOptions().getDrawPolicy();

                RoundRectangle2D roundRectToDraw = null;
                Rectangle2D bounds = ImageUtils.mouseRectToLogicalRect((Point) previousMouseDepression, (Point) previousMouseRelease, isCtrlDown);
                roundRectToDraw = new RoundRectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 24.0, 24.0);

                if (drawPolicy == ShapeDrawOptionPane.FILL_AND_STROKE) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        g2d.setPaint(colourPane.getSelectedBackgroundColour());
                    } else {
                        g2d.setPaint(colourPane.getSelectedForegroundColour());
                    }
                    g2d.fill(roundRectToDraw);
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        g2d.setPaint(colourPane.getSelectedForegroundColour());
                    } else {
                        g2d.setPaint(colourPane.getSelectedBackgroundColour());
                    }
                    g2d.draw(roundRectToDraw);
                } else if (drawPolicy == ShapeDrawOptionPane.STROKE_ONLY) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        g2d.setPaint(colourPane.getSelectedForegroundColour());
                    } else {
                        g2d.setPaint(colourPane.getSelectedBackgroundColour());
                    }
                    g2d.draw(roundRectToDraw);
                } else if (drawPolicy == ShapeDrawOptionPane.FILL_ONLY) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        g2d.setPaint(colourPane.getSelectedBackgroundColour());
                    } else {
                        g2d.setPaint(colourPane.getSelectedForegroundColour());
                    }
                    g2d.fill(roundRectToDraw);
                }
                canvas.repaint();
                //</editor-fold>
            } //<editor-fold defaultstate="collapsed" desc="polygon select code">
            else if ((toolbar.getCurrentTool() == ImageToolbar.SELECT_OVAL) || (toolbar.getCurrentTool() == ImageToolbar.SELECT_RECT)) {
                if (currentSelection != null) {
                    selectionExists = true;
                }
            }

            if (draggingHandle) {
                draggingHandle = false;
                selectionSubImage = latestTransform;
                if (isRotateToolSelected()) {
                    currentSelection = newSelection;
                }
            } //</editor-fold>
            else if ((toolbar.getCurrentTool() == ImageToolbar.TEXT) && (editingText)) {
                canvas.clearGlassPane();
                RichTextArea txtPane = editorPane.getTextPaneForTextTool();
                canvas.add(txtPane);
                txtPane.setSize(new Dimension((int) shapeTextPlacement.getWidth(), (int) shapeTextPlacement.getHeight()));
                txtPane.setLocation((int) shapeTextPlacement.getX(), (int) shapeTextPlacement.getY());
                txtPane.setBorder(BorderFactory.createLineBorder(Color.gray));
                txtPane.setBackground(colourPane.getSelectedBackgroundColourOpaque());
                txtPane.setForeground(colourPane.getSelectedForegroundColour());
                txtPane.setVisible(true);
            }
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }

    //THIS IS WHERE THE PREVIEWS ARE DRAWN ON THE GLASS PANE
    private class CanvasMotionListener implements MouseMotionListener {

        final int NORTH_WEST = 0;
        final int NORTH = 1;
        final int NORTH_EAST = 2;
        final int EAST = 3;
        final int SOUTH_EAST = 4;
        final int SOUTH = 5;
        final int SOUTH_WEST = 6;
        final int WEST = 7;
        double initialAngle;

        public void mouseDragged(MouseEvent e) {

            boolean isCtrlDown = ((e.getModifiers() & ActionEvent.CTRL_MASK) > 0);

            //which mouseButton is being used?
            if (SwingUtilities.isLeftMouseButton(e)) {
                mouseButton = MouseEvent.BUTTON1;
            } else if (SwingUtilities.isRightMouseButton(e)) {
                mouseButton = MouseEvent.BUTTON3;
            }

            if (previousMouseDragPosition == null) {
                previousMouseDragPosition = e.getPoint();
            }

            //<editor-fold defaultstate="collapsed" desc="line draw code">
            if (toolbar.getCurrentTool() == ImageToolbar.DRAW_LINE) {
                //clear the glass pane of any other construction lines..
                canvas.clearGlassPane();
                //draw a line onto the glass pane.
                Graphics g;
                if (editorPane.isLineEchoSelected()) {
                    g = canvas.getCanvasBufferedImage().createGraphics();
                } else {
                    g = canvas.getGlassPaneBufferedImage().createGraphics();
                }
                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(editorPane.getLineOptions().getSelectedStroke());
                if (editorPane.isAntialiasSelected()) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }

                Line2D lineToDraw = ImageUtils.makeLineStraight(previousMouseDepression.getX(),
                        previousMouseDepression.getY(), e.getX(), e.getY(), lineThreshold, isCtrlDown);

                Color c;
                if (mouseButton == MouseEvent.BUTTON1) {
                    c = colourPane.getSelectedForegroundColour();
                } else {
                    c = colourPane.getSelectedBackgroundColour();
                }

                //set the paint colour to a slightly transparent version of what is selected.
                g2d.setPaint(new Color(c.getRed(), c.getGreen(), c.getBlue(), 100));
                g2d.draw(lineToDraw);
                canvas.repaint();
                //</editor-fold>
            } //<editor-fold defaultstate="collapsed" desc="arrow draw code">
            else if (toolbar.getCurrentTool() == ImageToolbar.DRAW_ARROW) {
                //clear the glass pane of any other construction lines..
                canvas.clearGlassPane();
                //draw a line onto the glass pane.
                Graphics g = canvas.getGlassPaneBufferedImage().createGraphics();
                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(editorPane.getArrowOptions().getSelectedStroke());
                if (editorPane.isAntialiasSelected()) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }
                boolean fill = editorPane.getArrowOptions().getFillArrow();

                Path2D toDraw = ImageUtils.setArrowAlongLine(editorPane.getArrowOptions().getSelectedArrow(),
                        ImageUtils.makeLineStraight(previousMouseDepression.getX(), previousMouseDepression.getY(), e.getX(), e.getY(), lineThreshold, isCtrlDown));

                Color cf;
                if (mouseButton == MouseEvent.BUTTON1) {
                    cf = colourPane.getSelectedForegroundColour();
                } else {
                    cf = colourPane.getSelectedBackgroundColour();
                }

                Color cb;
                if (mouseButton == MouseEvent.BUTTON1) {
                    cb = colourPane.getSelectedBackgroundColour();
                } else {
                    cb = colourPane.getSelectedForegroundColour();
                }

                //set the paint colour to a slightly transparent version of what is selected.
                if (fill) {
                    g2d.setPaint(new Color(cb.getRed(), cb.getGreen(), cb.getBlue(), 100));
                    g2d.fill(toDraw);
                }
                g2d.setPaint(new Color(cf.getRed(), cf.getGreen(), cf.getBlue(), 100));
                g2d.draw(toDraw);
                canvas.repaint();
                //</editor-fold>
            } //<editor-fold defaultstate="collapsed" desc="rectangle draw code">
            else if (toolbar.getCurrentTool() == ImageToolbar.DRAW_RECT) {
                //clear the glass pane of any other construction lines..
                canvas.clearGlassPane();
                //draw a line onto the glass pane.
                Graphics g = canvas.getGlassPaneBufferedImage().createGraphics();
                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(editorPane.getShapeDrawOptions().getSelectedStroke());
                if (editorPane.isAntialiasSelected()) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }
                int drawPolicy = editorPane.getShapeDrawOptions().getDrawPolicy();


                Rectangle2D rectToDraw = null;
                rectToDraw = ImageUtils.mouseRectToLogicalRect((Point) previousMouseDepression, e.getPoint(), isCtrlDown);

                Color cf;
                Color cb;
                if (mouseButton == MouseEvent.BUTTON1) {
                    cf = colourPane.getSelectedForegroundColour();
                    cb = colourPane.getSelectedBackgroundColour();
                } else {
                    cf = colourPane.getSelectedBackgroundColour();
                    cb = colourPane.getSelectedForegroundColour();
                }


                //set the paint colour to a slightly transparent version of what is selected.
                if (drawPolicy == ShapeDrawOptionPane.FILL_AND_STROKE) {
                    g2d.setPaint(new Color(cb.getRed(), cb.getGreen(), cb.getBlue(), 100));
                    g2d.fill(rectToDraw);
                    g2d.setPaint(new Color(cf.getRed(), cf.getGreen(), cf.getBlue(), 100));
                    g2d.draw(rectToDraw);
                } else if (drawPolicy == ShapeDrawOptionPane.STROKE_ONLY) {
                    g2d.setPaint(new Color(cf.getRed(), cf.getGreen(), cf.getBlue(), 100));
                    g2d.draw(rectToDraw);
                } else if (drawPolicy == ShapeDrawOptionPane.FILL_ONLY) {
                    g2d.setPaint(new Color(cb.getRed(), cb.getGreen(), cb.getBlue(), 100));
                    g2d.fill(rectToDraw);
                }
                canvas.repaint();
                //</editor-fold>
            } //<editor-fold defaultstate="collapsed" desc="oval draw code">
            else if (toolbar.getCurrentTool() == ImageToolbar.DRAW_OVAL) {
                //clear the glass pane of any other construction lines..
                canvas.clearGlassPane();
                //draw a line onto the glass pane.
                Graphics g = canvas.getGlassPaneBufferedImage().createGraphics();
                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(editorPane.getShapeDrawOptions().getSelectedStroke());
                if (editorPane.isAntialiasSelected()) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }
                int drawPolicy = editorPane.getShapeDrawOptions().getDrawPolicy();

                Ellipse2D ovalToDraw = null;
                Rectangle2D ovalBounds = ImageUtils.mouseRectToLogicalRect((Point) previousMouseDepression, e.getPoint(), isCtrlDown);
                ovalToDraw = new Ellipse2D.Double(ovalBounds.getX(), ovalBounds.getY(), ovalBounds.getWidth(), ovalBounds.getHeight());

                Color cf;
                Color cb;
                if (mouseButton == MouseEvent.BUTTON1) {
                    cf = colourPane.getSelectedForegroundColour();
                    cb = colourPane.getSelectedBackgroundColour();
                } else {
                    cf = colourPane.getSelectedBackgroundColour();
                    cb = colourPane.getSelectedForegroundColour();
                }

                //set the paint colour to a slightly transparent version of what is selected.
                if (drawPolicy == ShapeDrawOptionPane.FILL_AND_STROKE) {
                    g2d.setPaint(new Color(cb.getRed(), cb.getGreen(), cb.getBlue(), 100));
                    g2d.fill(ovalToDraw);
                    g2d.setPaint(new Color(cf.getRed(), cf.getGreen(), cf.getBlue(), 100));
                    g2d.draw(ovalToDraw);
                } else if (drawPolicy == ShapeDrawOptionPane.STROKE_ONLY) {
                    g2d.setPaint(new Color(cf.getRed(), cf.getGreen(), cf.getBlue(), 100));
                    g2d.draw(ovalToDraw);
                } else if (drawPolicy == ShapeDrawOptionPane.FILL_ONLY) {
                    g2d.setPaint(new Color(cb.getRed(), cb.getGreen(), cb.getBlue(), 100));
                    g2d.fill(ovalToDraw);
                }
                canvas.repaint();
                //</editor-fold>
            } //<editor-fold defaultstate="collapsed" desc="rounded rectangle draw code">
            else if (toolbar.getCurrentTool() == ImageToolbar.DRAW_ROUNDED_RECT) {
                //clear the glass pane of any other construction lines..
                canvas.clearGlassPane();
                //draw a line onto the glass pane.
                Graphics g = canvas.getGlassPaneBufferedImage().createGraphics();
                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(editorPane.getShapeDrawOptions().getSelectedStroke());
                if (editorPane.isAntialiasSelected()) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }
                int drawPolicy = editorPane.getShapeDrawOptions().getDrawPolicy();

                RoundRectangle2D roundRectToDraw = null;
                Rectangle2D bounds = ImageUtils.mouseRectToLogicalRect((Point) previousMouseDepression, e.getPoint(), isCtrlDown);
                roundRectToDraw = new RoundRectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 24.0, 24.0);

                Color cf;
                Color cb;
                if (mouseButton == MouseEvent.BUTTON1) {
                    cf = colourPane.getSelectedForegroundColour();
                    cb = colourPane.getSelectedBackgroundColour();
                } else {
                    cf = colourPane.getSelectedBackgroundColour();
                    cb = colourPane.getSelectedForegroundColour();
                }

                //set the paint colour to a slightly transparent version of what is selected.
                if (drawPolicy == ShapeDrawOptionPane.FILL_AND_STROKE) {
                    g2d.setPaint(new Color(cb.getRed(), cb.getGreen(), cb.getBlue(), 100));
                    g2d.fill(roundRectToDraw);
                    g2d.setPaint(new Color(cf.getRed(), cf.getGreen(), cf.getBlue(), 100));
                    g2d.draw(roundRectToDraw);
                } else if (drawPolicy == ShapeDrawOptionPane.STROKE_ONLY) {
                    g2d.setPaint(new Color(cf.getRed(), cf.getGreen(), cf.getBlue(), 100));
                    g2d.draw(roundRectToDraw);
                } else if (drawPolicy == ShapeDrawOptionPane.FILL_ONLY) {
                    g2d.setPaint(new Color(cb.getRed(), cb.getGreen(), cb.getBlue(), 100));
                    g2d.fill(roundRectToDraw);
                }
                canvas.repaint();
//</editor-fold>
            } //<editor-fold defaultstate="collapsed" desc="pencil draw code">
            else if (toolbar.getCurrentTool() == ImageToolbar.PENCIL) {
                Shape pencil = editorPane.getPencilOptions().getSelectedPencil();
                Graphics g = canvas.getCanvasBufferedImage().createGraphics();
                Graphics2D g2d = (Graphics2D) g;

                Color c;
                if (mouseButton == MouseEvent.BUTTON1) {
                    c = colourPane.getSelectedForegroundColour();
                } else {
                    c = colourPane.getSelectedBackgroundColour();
                }

                if (editorPane.isAntialiasSelected()) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }

                g2d.setPaint(c);

                /*
                 * The mouse can move faster than this method is called, and as such, our pencil
                 * tool looks quite splotchy, and the shapes which are the 'brushes' are drawn individually
                 * if a swift motion is made (or if the brush is quite small).
                 * To combat this, we will draw the shape at each point along a line between the previous registered
                 * mouse drag position and the current, unless the mouse has been released in the mean time.
                 *
                 * With this method, swift mouse movements will create slightly jagged lines. It may be worth
                 * drawing from point to point by first calcuating the slope, rather than using 45 degree slope
                 * by assumption. Do this later.
                 */
                if (mouseHasReleased) {
                    pencil = AffineTransform.getTranslateInstance(e.getX() - pencil.getBounds().getCenterX(), e.getY() - pencil.getBounds().getCenterY()).createTransformedShape(pencil);
                    g2d.fill(pencil);
                } else {
                    int x1 = (int) previousMouseDragPosition.getX();
                    int y1 = (int) previousMouseDragPosition.getY();
                    int x2 = e.getX();
                    int y2 = e.getY();
                    float slope = (x2 == x1) ? 100 : (((float) y2 - y1) / ((float) x2 - x1));
                    float curX = x1;
                    float curY = y1;

                    float xStep = 0.3f;
                    float yStep = 0.3f;

                    float distance = (float) Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));

                    /*
                     * A mix of methods is used here... I couldn't find a way to get a nice pencil line. The first method is to add or subtract 1
                     * to each y and x coordiantes. This is done if the distance between the points is small. The next method uses the slope of the
                     * line between the points and tries to follow it. Excuse the magic numbers.
                     */

                    if (distance < 15) {
                        int stepX = (x1 < x2) ? 1 : -1;
                        int stepY = (y1 < y2) ? 1 : -1;

                        while ((x1 != x2) || (y1 != y2)) {
                            if (x1 != x2) {
                                x1 += stepX;
                            }

                            if (y1 != y2) {
                                y1 += stepY;
                            }

                            pencil = AffineTransform.getTranslateInstance(x1 - pencil.getBounds().getCenterX(), y1 - pencil.getBounds().getCenterY()).createTransformedShape(pencil);
                            if (mouseButton == MouseEvent.BUTTON1) {
                                g2d.fill(pencil);
                            } else {
                                ImageUtils.replaceColour(canvas.getCanvasBufferedImage(), pencil, colourPane.getSelectedForegroundColour(), colourPane.getSelectedBackgroundColour());
                            }
                        }
                    } else {
                        if (slope > 1) {
                            if (((x2 > x1) && (y2 > y1)) || ((x2 < x1) && (y2 > y1))) {
                                while (curY < y2) {
                                    curY += yStep;
                                    curX = x1 + ((curY - y1) / slope);

                                    pencil = AffineTransform.getTranslateInstance(curX - pencil.getBounds().getCenterX(), curY - pencil.getBounds().getCenterY()).createTransformedShape(pencil);
                                    if (mouseButton == MouseEvent.BUTTON1) {
                                        g2d.fill(pencil);
                                    } else {
                                        ImageUtils.replaceColour(canvas.getCanvasBufferedImage(), pencil, colourPane.getSelectedForegroundColour(), colourPane.getSelectedBackgroundColour());
                                    }
                                }
                            } else if (((x2 < x1) && (y2 < y1)) || ((x2 > x1) && (y2 < y1))) {
                                while (curY > y2) {
                                    curY -= yStep;
                                    curX = x1 - (y1 - curY) / slope;

                                    pencil = AffineTransform.getTranslateInstance(curX - pencil.getBounds().getCenterX(), curY - pencil.getBounds().getCenterY()).createTransformedShape(pencil);
                                    if (mouseButton == MouseEvent.BUTTON1) {
                                        g2d.fill(pencil);
                                    } else {
                                        ImageUtils.replaceColour(canvas.getCanvasBufferedImage(), pencil, colourPane.getSelectedForegroundColour(), colourPane.getSelectedBackgroundColour());
                                    }
                                }
                            }
                        } else {

                            if (((x2 > x1) && (y2 > y1)) || ((x2 > x1) && (y2 < y1))) {
                                while (curX < x2) {
                                    curX += xStep;
                                    curY = (y1 + (curX - x1) * slope);

                                    pencil = AffineTransform.getTranslateInstance(curX - pencil.getBounds().getCenterX(), curY - pencil.getBounds().getCenterY()).createTransformedShape(pencil);
                                    if (mouseButton == MouseEvent.BUTTON1) {
                                        g2d.fill(pencil);
                                    } else {
                                        ImageUtils.replaceColour(canvas.getCanvasBufferedImage(), pencil, colourPane.getSelectedForegroundColour(), colourPane.getSelectedBackgroundColour());
                                    }
                                }
                            } else if (((x2 < x1) && (y2 < y1)) || ((x2 < x1) && (y2 > y1))) {
                                while (curX > x2) {
                                    curX -= xStep;
                                    curY = (y1 - (x1 - curX) * slope);

                                    pencil = AffineTransform.getTranslateInstance(curX - pencil.getBounds().getCenterX(), curY - pencil.getBounds().getCenterY()).createTransformedShape(pencil);
                                    if (mouseButton == MouseEvent.BUTTON1) {
                                        g2d.fill(pencil);
                                    } else {
                                        ImageUtils.replaceColour(canvas.getCanvasBufferedImage(), pencil, colourPane.getSelectedForegroundColour(), colourPane.getSelectedBackgroundColour());
                                    }
                                }
                            }

                        }
                    }
                }


                canvas.repaint();

//</editor-fold>
            }//<editor-fold defaultstate="collapsed" desc="eraser draw code">
            else if (toolbar.getCurrentTool() == ImageToolbar.ERASER) {
                Shape eraser = editorPane.getEraserOptions().getSelectedEraser();
                Graphics g = canvas.getCanvasBufferedImage().createGraphics();
                Graphics2D g2d = (Graphics2D) g;

                Color c = colourPane.getSelectedBackgroundColour();

                if (editorPane.isAntialiasSelected()) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }

                g2d.setPaint(c);

                /*
                 * The mouse can move faster than this method is called, and as such, our pencil
                 * tool looks quite splotchy, and the shapes which are the 'brushes' are drawn individually
                 * if a swift motion is made (or if the brush is quite small).
                 * To combat this, we will draw the shape at each point along a line between the previous registered
                 * mouse drag position and the current, unless the mouse has been released in the mean time.
                 *
                 * With this method, swift mouse movements will create slightly jagged lines. It may be worth
                 * drawing from point to point by first calcuating the slope, rather than using 45 degree slope
                 * by assumption. Do this later.
                 */
                if (mouseHasReleased) {
                    eraser = AffineTransform.getTranslateInstance(e.getX() - eraser.getBounds().getCenterX(), e.getY() - eraser.getBounds().getCenterY()).createTransformedShape(eraser);
                    if (mouseButton == MouseEvent.BUTTON1) {
                        g2d.fill(eraser);
                    } else {
                        ImageUtils.replaceColour(canvas.getCanvasBufferedImage(), eraser, colourPane.getSelectedForegroundColour(), colourPane.getSelectedBackgroundColour());
                    }
                } else {
                    int x1 = (int) previousMouseDragPosition.getX();
                    int y1 = (int) previousMouseDragPosition.getY();
                    int x2 = e.getX();
                    int y2 = e.getY();

                    int stepX = 0;
                    int stepY = 0;
                    if (x1 < x2) {
                        stepX = 1;
                    } else {
                        stepX = -1;
                    }

                    if (y1 < y2) {
                        stepY = 1;
                    } else {
                        stepY = -1;
                    }



                    while ((x1 != x2) || (y1 != y2)) {
                        if (x1 != x2) {
                            x1 += stepX;
                        }

                        if (y1 != y2) {
                            y1 += stepY;
                        }

                        eraser = AffineTransform.getTranslateInstance(x1 - eraser.getBounds().getCenterX(), y1 - eraser.getBounds().getCenterY()).createTransformedShape(eraser);
                        if (mouseButton == MouseEvent.BUTTON1) {
                            g2d.fill(eraser);
                        } else {
                            ImageUtils.replaceColour(canvas.getCanvasBufferedImage(), eraser, colourPane.getSelectedForegroundColour(), colourPane.getSelectedBackgroundColour());
                        }
                    }
                }


                canvas.repaint();

//</editor-fold>
            } else if ((useSelectionHandles) && (draggingHandle)) {
                if (selectionExists) {
                    performSelectionHandleDragEvent(e.getPoint(), e);
                }
            } //<editor-fold defaultstate="collapsed" desc="selection create/drag code">
            //math tool also considered here.
            else if ((!draggingHandle) && ((toolbar.getCurrentTool() == ImageToolbar.SELECT_OVAL) || (toolbar.getCurrentTool() == ImageToolbar.SELECT_RECT) || (toolbar.getCurrentTool() == ImageToolbar.SELECT_POLY) || (toolbar.getCurrentTool() == ImageToolbar.MATH))) {
                /*
                 * This drag may represent one of two situations:
                 *  -the user has already made a selection and is now attempting to drag it
                 *  -the user is making a new selection (whether or not one already exists).
                 *  Logic flow:
                 *  1) if selection (global shape var) is null, then no selection exists, and one may
                 *     be created.
                 *  2) if selection is not null, then a selection exists. Does the current mouse click exist within
                 *     that selection?
                 *  3) if it does not, then we are creating a new selection, and the old one will automatically be forgotten
                 *  4) if the current click exists within the selection, then we are dragging the selection.
                 *      - in this case, we use ImageUtils to obtain the subImage, as the selection is not neccessarily
                 *        rectangular.
                 *      - we paint the background of where the selection was with the background colour
                 *        but only if this is the first drag, as indicated by global boolean <code>selectonBeenDragged</code>.
                 *      - we translate the selection shape (global var) to the new coordinates chosen by the user
                 *      - we check the selection options panel, and call ImageUtils to replace all background pixels
                 *        of the subimage with transparent pixels, if required.
                 *      - we paint the subimage (selection) onto the glass pane, as well as the selection border
                 */

                if (selectionExists) {
                    //we are dragging the image
                    if (!selectionBeenDragged) {
                        canvas.setUndoPoint();
                        //get subimage
                        selectionSubImage = ImageUtils.getSubImage(canvas.getCanvasBufferedImage(), currentSelection, colourPane.getSelectedBackgroundColour());
                        if (editorPane.getSelectionOptions().isBackgroundTransparent()) {
                            ImageUtils.setColourTransparent(selectionSubImage, colourPane.getSelectedBackgroundColour());
                        }
                        //delete the area from which the selection originates
                        Graphics2D g2d = (Graphics2D) canvas.getCanvasBufferedImage().createGraphics();
                        g2d.setPaint(colourPane.getSelectedBackgroundColour());
                        g2d.fill(currentSelection);
                        selectionBeenDragged = true;
                    }
                    //if the mouse has been released since our last encounter with this translate, then perform the
                    //translation from where the mouse started being dragged.
                    if (mouseHasReleased) {
                        previousMouseDragPosition = previousMouseDepression;
                    }
                    currentSelection = AffineTransform.getTranslateInstance(e.getX() - previousMouseDragPosition.getX(),
                            e.getY() - previousMouseDragPosition.getY()).createTransformedShape(currentSelection);

                    //also move the math text field, if we're currently using the math tool
                    if (toolbar.getCurrentTool() == ImageToolbar.MATH) {
                        editorPane.getTextFieldForMathTool().setLocation(currentSelection.getBounds().x, currentSelection.getBounds().y - editorPane.getTextFieldForMathTool().getHeight() - 2);
                    }

                    //create the selection handles, if appropriate
                    if (useSelectionHandles) {
                        createSelectionHandles();
                    }

                    //now lets start drawing
                    paintCurrentSelection();
                    //otherwise we are creating a new selection, so let the method
                    //carry on with the below code to create a new selection.
                } else {
                    /*
                     * BELOW: creating a /new/ selection... the above code for dragging a current selection returns before it
                     * reaches this point.
                     */
                    //we don't want to create a new selection if we're using the MATH tool.. in this case we only want to drag.
                    if (toolbar.getCurrentTool() == ImageToolbar.MATH) {
                        return;
                    }

                    //clear the glass pane of any other construction lines..
                    canvas.clearGlassPane();
                    //draw a line onto the glass pane.
                    Graphics g = canvas.getGlassPaneBufferedImage().createGraphics();
                    Graphics2D g2d = (Graphics2D) g;
                    if (editorPane.isAntialiasSelected()) {
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    } else {
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                    }

                    currentSelection = null;

                    //if we're selecting a rectangle
                    if (toolbar.getCurrentTool() == ImageToolbar.SELECT_RECT) {
                        currentSelection = ImageUtils.mouseRectToLogicalRect((Point) previousMouseDepression, e.getPoint(), isCtrlDown);
                    } else if (toolbar.getCurrentTool() == ImageToolbar.SELECT_OVAL) { //if we're selecting an ellipse
                        Rectangle2D ovalBounds = ImageUtils.mouseRectToLogicalRect((Point) previousMouseDepression, e.getPoint(), isCtrlDown);
                        currentSelection = new Ellipse2D.Double(ovalBounds.getX(), ovalBounds.getY(), ovalBounds.getWidth(), ovalBounds.getHeight());
                    }

                    if (useSelectionHandles) {
                        createSelectionHandles();
                    }
                    //set the paint colour to a slightly transparent grey and light grey
                    paintCurrentSelection();
                    //</editor-fold>
                }
            } //<editor-fold defaultstate="collapsed" desc="add-text tool">
            else if (toolbar.getCurrentTool() == ImageToolbar.TEXT) {
                canvas.clearGlassPane();

                shapeTextPlacement = ImageUtils.mouseRectToLogicalRect((Point) previousMouseDepression, e.getPoint(), isCtrlDown);

                Graphics2D g2d = canvas.getGlassPaneBufferedImage().createGraphics();

                g2d.setPaint(new Color(150, 150, 150, 100));
                g2d.draw(shapeTextPlacement);
                canvas.repaint();
                editingText = true;
                //</editor-fold>
            }

            previousMouseDragPosition = e.getPoint();
            mouseHasReleased = false;

        }

        /**
         * This method is called when a selection already exists and when a handle is dragged.
         */
        public void performSelectionHandleDragEvent(Point mousePoint, MouseEvent e) {

            //if the selection hasn't been dragged, then we need to get the
            //sub image first.
            if (!selectionBeenDragged) {
                //get subimage
                canvas.setUndoPoint();
                selectionSubImage = ImageUtils.getSubImage(canvas.getCanvasBufferedImage(), currentSelection, colourPane.getSelectedBackgroundColour());
                if (editorPane.getSelectionOptions().isBackgroundTransparent()) {
                    ImageUtils.setColourTransparent(selectionSubImage, colourPane.getSelectedBackgroundColour());
                }
                //delete the area from which the selection originates
                Graphics2D g2d = (Graphics2D) canvas.getCanvasBufferedImage().createGraphics();
                g2d.setPaint(colourPane.getSelectedBackgroundColour());
                g2d.fill(currentSelection);
                selectionBeenDragged = true;
            }

            if (scaleToolSelected) //then scale the selection accordingly
            {
                scaleSelection(mousePoint, e);
            } else if (rotateToolSelected) //then rotate the selection accordingly
            {
                rotateSelection(mousePoint);
            }

        }

        /**
         * This method was created to tidy up the <code>performSelectionHandleDragEvent(Point)</code> method.
         * It takes care of any rotate-by-handle-dragging.
         * @param mousePoint the point of the mouse-drag
         */
        private void rotateSelection(Point mousePoint) {
            if (mouseHasReleased) {
                initialAngle = getAngle(new Point((int) currentSelection.getBounds().getCenterX(), (int) currentSelection.getBounds().getCenterY()), mousePoint);
                return;
            }
            double currentAngle = getAngle(new Point((int) currentSelection.getBounds().getCenterX(), (int) currentSelection.getBounds().getCenterY()), mousePoint);
            double angle = currentAngle - initialAngle;

            Shape oldSelection = new Path2D.Double(currentSelection);
            Rectangle2D oldBounds = currentSelection.getBounds2D();
            currentSelection = AffineTransform.getRotateInstance(angle).createTransformedShape(currentSelection);
            Point selCentre = new Point((int) currentSelection.getBounds().getCenterX(), (int) currentSelection.getBounds().getCenterY());
            currentSelection = AffineTransform.getTranslateInstance(oldBounds.getCenterX() - selCentre.getX(), oldBounds.getCenterY() - selCentre.getY()).createTransformedShape(currentSelection);
            latestTransform = new BufferedImage((int) currentSelection.getBounds().getWidth(), (int) currentSelection.getBounds().getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) latestTransform.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.translate((latestTransform.getWidth() - oldBounds.getWidth()) * 0.5, (latestTransform.getHeight() - oldBounds.getHeight()) * 0.5);
            g2d.rotate(angle, selectionSubImage.getWidth() / 2, selectionSubImage.getHeight() / 2);
            g2d.drawImage(selectionSubImage, null, 0, 0);
            //swap the selection sub image for the transformed one just while we
            //are painting...
            BufferedImage temp = selectionSubImage;
            selectionSubImage = latestTransform;
            paintCurrentSelection();
            //now swap it back
            selectionSubImage = temp;
            //save this as the 'new selection', in case the user releases the mouse button.
            newSelection = currentSelection;
            createSelectionHandles();
            currentSelection = new Path2D.Double(oldSelection);
        }

        public double getAngle(Point origin, Point other) {
            double dy = other.y - origin.y;
            double dx = other.x - origin.x;
            double angle;

            if (dx == 0) // special case
            {
                angle = dy >= 0 ? Math.PI / 2 : -Math.PI / 2;
            } else {
                angle = Math.atan(dy / dx);
                if (dx < 0) // hemisphere correction
                {
                    angle += Math.PI;
                }
            }
            // all between 0 and 2PI
            if (angle < 0) // between -PI/2 and 0
            {
                angle += 2 * Math.PI;
            }
            return angle;
        }

        /**
         * This method was created to tidy up the <code>performSelectionHandleDragEvent(Point)</code> method.
         * It takes care of any scale-by-handle-drag.
         * @param mousePoint the point of the mouse-drag
         */
        private void scaleSelection(Point mousePoint, MouseEvent e) {
            boolean isCtrlDown = ((e.getModifiers() & ActionEvent.CTRL_MASK) > 0);
            /**
             * Difference between the old selection width and the new selection width.
             */
            double xDisplacement = 0;
            /**
             * difference between the old selection height and the new selection height.
             */
            double yDisplacement = 0;
            /**
             * Proportion of the difference in size between the old selection size
             * and the new selection size and the x dimension of the old selection size.
             */
            double proportionX = 0;
            /**
             * Proportion of the difference in size between the old selection size
             * and the new selection size and the y dimension of the old selection size.
             */
            double proportionY = 0;
            /**
             * keeps track of the x position of the current selection. When resizing,
             * the x position changes and the selection must be repositioned so as to
             * preserve the location of all other handles.
             */
            double selectionX;
            /**
             * keeps track of the y position of the current selection. When resizing,
             * the y position changes and the selection must be repositioned so as to
             * preserve the location of all other handles.
             */
            double selectionY;
            /**
             * Bounds of the current selection.
             */
            Rectangle2D selectionBounds;
            /**
             * Bounds of the new selection (current selection x,y, and mouse x,y)
             */
            Rectangle2D newBounds = null;
            /**
             * The x coordinate here is out of range, so set proportionX to 0.
             */
            boolean badX = false;
            /**
             * The y coordinate here is out of range, so set proportionY to 0.
             */
            boolean badY = false;
            /*we are scaling the image by the proportion of the x displacement to original
             * size and proportion y displacement to original size
             * now set the displacements as difference between the current point and the current bottom right
             * corner of the selection
             */
            selectionBounds = currentSelection.getBounds();

            switch (selectionHandle) {
                case NORTH_WEST:
                    //if the rectangle is negative, then make it positive.
                    newBounds = ImageUtils.mouseRectToLogicalRect(mousePoint, new Point((int) selectionBounds.getX() + (int) selectionBounds.getWidth(), (int) selectionBounds.getY() + (int) selectionBounds.getHeight()), false);
                    //we must now fix aspect ratio to the original
                    if (isCtrlDown) {
                        Rectangle2D temp = newBounds;
                        newBounds = ImageUtils.setBoundsToAspectRatio(newBounds, aspectRatio);
                        //we have resized in a SOUTH_EAST fashion, because this is NORTH_WEST, we must move the shape
                        newBounds = AffineTransform.getTranslateInstance(temp.getWidth() - newBounds.getWidth(), temp.getHeight() - newBounds.getHeight()).createTransformedShape(newBounds).getBounds2D();
                    }

                    //if user is out of range..
                    if (mousePoint.getX() > (selectionBounds.getX() + selectionBounds.getWidth() - 10)) {
                        //set x proportion to 0 so that the x size stays the same after scaling
                        badX = true;
                        newBounds = selectionBounds;
                    }
                    if (mousePoint.getY() > (selectionBounds.getY() + selectionBounds.getHeight() - 10)) {
                        //set y proportion to 0 so that the y size stays the same after scaling.
                        badY = true;
                        newBounds = selectionBounds;
                    }

                    xDisplacement = selectionBounds.getX() - newBounds.getX();
                    yDisplacement = selectionBounds.getY() - newBounds.getY();
                    break;
                case NORTH:
                    //if the rectangle is negative, then make it positive.
                    newBounds = ImageUtils.mouseRectToLogicalRect(new Point((int) selectionBounds.getX(), mousePoint.y), new Point((int) selectionBounds.getX() + (int) selectionBounds.getWidth(), (int) selectionBounds.getY() + (int) selectionBounds.getHeight()), false);
                    //if user is out of range..
                    if (mousePoint.getY() > (selectionBounds.getY() + selectionBounds.getHeight() - 10)) {
                        //set y proportion to 0 so that the y size stays the same after scaling.
                        badY = true;
                        newBounds = selectionBounds;
                    }
                    xDisplacement = 0;
                    yDisplacement = selectionBounds.getY() - newBounds.getY();
                    break;
                case NORTH_EAST:
                    newBounds = ImageUtils.mouseRectToLogicalRect(new Point((int) selectionBounds.getX(), (int) selectionBounds.getY() + (int) selectionBounds.getHeight()), mousePoint, false);
                    //we must now fix aspect ratio to the original
                    if (isCtrlDown) {
                        Rectangle2D temp = newBounds;
                        newBounds = ImageUtils.setBoundsToAspectRatio(newBounds, aspectRatio);
                        //we have resized in a SOUTH_EAST fashion, because this is NORTH_EAST, we must move the shape
                        newBounds = AffineTransform.getTranslateInstance(0, temp.getHeight() - newBounds.getHeight()).createTransformedShape(newBounds).getBounds2D();
                    }
                    //if user is out of range..
                    if (mousePoint.getX() < (selectionBounds.getX() + 10)) {
                        //set x proportion to 0 so that the x size stays the same after scaling
                        badX = true;
                        newBounds = selectionBounds;
                    }
                    if (mousePoint.getY() > (selectionBounds.getY() + selectionBounds.getHeight() - 10)) {
                        //set y proportion to 0 so that the y size stays the same after scaling.
                        badY = true;
                        newBounds = selectionBounds;
                    }
                    xDisplacement = (newBounds.getX() + newBounds.getWidth()) - (selectionBounds.getX() + selectionBounds.getWidth());
                    yDisplacement = selectionBounds.getY() - newBounds.getY();
                    break;
                case EAST:
                    //if the rectangle is negative, then make it positive.
                    newBounds = ImageUtils.mouseRectToLogicalRect(new Point((int) selectionBounds.getX(), (int) selectionBounds.getY()), mousePoint, false);
                    //if user is out of range..
                    if (mousePoint.getX() < (currentSelection.getBounds().getX() + 10)) {
                        //set x proportion to 0 so that the x size stays the same after scaling
                        badX = true;
                        newBounds = selectionBounds;
                    }
                    xDisplacement = (newBounds.getX() + newBounds.getWidth()) - (selectionBounds.getX() + selectionBounds.getWidth());
                    yDisplacement = 0;
                    break;
                case SOUTH_EAST:
                    //if the rectangle is negative, then make it positive.
                    newBounds = ImageUtils.mouseRectToLogicalRect(new Point((int) selectionBounds.getX(), (int) selectionBounds.getY()), mousePoint, false);
                    //we must now fix aspect ratio to the original
                    if (isCtrlDown) {
                        newBounds = ImageUtils.setBoundsToAspectRatio(newBounds, aspectRatio);
                    }
                    //if user is out of range..
                    if (mousePoint.getX() < (currentSelection.getBounds().getX() + 10)) {
                        //set x proportion to 0 so that the x size stays the same after scaling
                        badX = true;
                        newBounds = selectionBounds;
                    }
                    if (mousePoint.getY() < (currentSelection.getBounds().getY() + 10)) {
                        //set y proportion to 0 so that the y size stays the same after scaling.
                        badY = true;
                        newBounds = selectionBounds;
                    }
                    xDisplacement = (newBounds.getX() + newBounds.getWidth()) - (selectionBounds.getX() + selectionBounds.getWidth());
                    yDisplacement = (newBounds.getY() + newBounds.getHeight()) - (selectionBounds.getY() + selectionBounds.getHeight());
                    break;
                case SOUTH:
                    //if the rectangle is negative, then make it positive.
                    newBounds = ImageUtils.mouseRectToLogicalRect(new Point((int) selectionBounds.getX(), (int) selectionBounds.getY()), mousePoint, false);
                    //if user is out of range..
                    if (mousePoint.getY() < (currentSelection.getBounds().getY() + 10)) {
                        //set y proportion to 0 so that the y size stays the same after scaling.
                        badY = true;
                        newBounds = selectionBounds;
                    }
                    xDisplacement = 0;
                    yDisplacement = (newBounds.getY() + newBounds.getHeight()) - (selectionBounds.getY() + selectionBounds.getHeight());
                    break;
                case SOUTH_WEST:
                    newBounds = ImageUtils.mouseRectToLogicalRect(new Point((int) selectionBounds.getX() + (int) selectionBounds.getWidth(), (int) selectionBounds.getY()), mousePoint, false);
                    //we must now fix aspect ratio to the original
                    if (isCtrlDown) {
                        Rectangle2D temp = newBounds;
                        newBounds = ImageUtils.setBoundsToAspectRatio(newBounds, aspectRatio);
                        //we have resized in a SOUTH_EAST fashion, because this is NORTH_EAST, we must move the shape
                        newBounds = AffineTransform.getTranslateInstance(temp.getWidth() - newBounds.getWidth(), 0).createTransformedShape(newBounds).getBounds2D();
                    }
                    //if user is out of range..
                    if (mousePoint.getX() > (selectionBounds.getX() + selectionBounds.getWidth() - 10)) {
                        //set x proportion to 0 so that the x size stays the same after scaling
                        badX = true;
                        newBounds = selectionBounds;
                    }
                    if (mousePoint.getY() < (selectionBounds.getY() + 10)) {
                        //set y proportion to 0 so that the y size stays the same after scaling.
                        badY = true;
                        newBounds = selectionBounds;
                    }
                    xDisplacement = selectionBounds.getX() - newBounds.getX();
                    yDisplacement = (newBounds.getY() + newBounds.getHeight()) - (selectionBounds.getY() + selectionBounds.getHeight());
                    break;
                case WEST:
                    //if the rectangle is negative, then make it positive.
                    newBounds = ImageUtils.mouseRectToLogicalRect(new Point(mousePoint.x, (int) selectionBounds.getY()), new Point((int) selectionBounds.getX() + (int) selectionBounds.getWidth(), (int) selectionBounds.getY() + (int) selectionBounds.getHeight()), false);
                    //if user is out of range..
                    if (mousePoint.getX() > (selectionBounds.getX() + selectionBounds.getWidth() - 10)) {
                        //set x proportion to 0 so that the x size stays the same after scaling
                        badX = true;
                        newBounds = selectionBounds;
                    }
                    xDisplacement = selectionBounds.getX() - newBounds.getX();
                    yDisplacement = 0;
                    break;
                default:
                    System.out.println("unknown selection handle: " + selectionHandle);
            }


            //the proportions of x and y displacement to the width and height. Used for the AffineTransform scale instance.
            proportionX = xDisplacement / currentSelection.getBounds().getWidth();
            proportionY = yDisplacement / currentSelection.getBounds().getHeight();
            //if we know that the mouse was not in a good place
            if (badX) {
                proportionX = 0;
            }
            if (badY) {
                proportionY = 0;
            }

            currentSelection = AffineTransform.getScaleInstance(1 + proportionX, 1 + proportionY).createTransformedShape(currentSelection);


            selectionX = newBounds.getX() - currentSelection.getBounds().getX();
            selectionY = newBounds.getY() - currentSelection.getBounds().getY();

            BufferedImage newSubImage = new BufferedImage((int) currentSelection.getBounds().getWidth(), (int) currentSelection.getBounds().getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) newSubImage.createGraphics();
            g2d.drawImage(selectionSubImage, 0, 0, newSubImage.getWidth(), newSubImage.getHeight(), null);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            latestTransform = newSubImage;

            currentSelection = AffineTransform.getTranslateInstance(selectionX, selectionY).createTransformedShape(currentSelection);
            createSelectionHandles();

            //swap the selection sub image for the transformed one just while we
            //are painting...
            BufferedImage temp = selectionSubImage;
            selectionSubImage = latestTransform;
            paintCurrentSelection();
            //now swap it back
            selectionSubImage = temp;
        }

        /**
         * Takes care of all mouse motion events. Mouse motion events include:
         * <ul>
         *     <li>Drawing a polygon (if <code>drawingPolygon</code> flag is set)</li>
         *     <li>Selecting a polygon (if <code>selectingPolygon</code> flag is set)</li>
         * </ul>
         * @param e
         */
        public void mouseMoved(MouseEvent e) {
            boolean isCtrlDown = ((e.getModifiers() & ActionEvent.CTRL_MASK) > 0);

            if ((toolbar.getCurrentTool() == ImageToolbar.DRAW_POLY) && (drawingPolygon)) {
                //clear the glass pane of any other construction lines..
                canvas.clearGlassPane();
                //draw a line onto the glass pane.
                Graphics g = canvas.getGlassPaneBufferedImage().createGraphics();
                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(editorPane.getShapeDrawOptions().getSelectedStroke());
                if (editorPane.isAntialiasSelected()) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }

                //get the draw policy.. i.e. fill and stroke, just stroke, or just fill.
                int drawPolicy = editorPane.getShapeDrawOptions().getDrawPolicy();

                //get colours
                Color cf;
                Color cb;
                if (mouseButton == MouseEvent.BUTTON1) {
                    cf = colourPane.getSelectedForegroundColour();
                    cb = colourPane.getSelectedBackgroundColour();
                } else {
                    cf = colourPane.getSelectedBackgroundColour();
                    cb = colourPane.getSelectedForegroundColour();
                }

                //create preview based on current mouse coordinates and existing polygon progress
                GeneralPath preview = (GeneralPath) polygon.clone();
                preview.append(ImageUtils.makeLineStraight(previousMouseDepression.getX(), previousMouseDepression.getY(), e.getX(), e.getY(), lineThreshold, isCtrlDown), true);
                preview.closePath();

                //fill/stroke according to the fill/stroke draw policy from the option panes
                if (drawPolicy == ShapeDrawOptionPane.FILL_AND_STROKE) {
                    g2d.setPaint(new Color(cb.getRed(), cb.getGreen(), cb.getBlue(), 100));
                    g2d.fill(preview);
                    g2d.setPaint(new Color(cf.getRed(), cf.getGreen(), cf.getBlue(), 100));
                    g2d.draw(preview);
                } else if (drawPolicy == ShapeDrawOptionPane.FILL_ONLY) {
                    g2d.setPaint(new Color(cb.getRed(), cb.getGreen(), cb.getBlue(), 100));
                    g2d.fill(preview);
                } else if (drawPolicy == ShapeDrawOptionPane.STROKE_ONLY) {
                    g2d.setPaint(new Color(cf.getRed(), cf.getGreen(), cf.getBlue(), 100));
                    g2d.draw(preview);
                }
                canvas.repaint();
                //SELECTING POLYGON
            } else if ((toolbar.getCurrentTool() == ImageToolbar.SELECT_POLY) && (selectingPolygon)) {
                //clear the glass pane of any other construction lines..
                canvas.clearGlassPane();
                //draw a line onto the glass pane.
                Graphics g = canvas.getGlassPaneBufferedImage().createGraphics();
                Graphics2D g2d = (Graphics2D) g;

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);


                //create preview based on current mouse coordinates and existing polygon progress
                GeneralPath preview = (GeneralPath) polygonSelection.clone();

                preview.append(ImageUtils.makeLineStraight(previousMouseDepression.getX(), previousMouseDepression.getY(), e.getX(), e.getY(), lineThreshold, isCtrlDown), true);
                preview.closePath();

                g2d.setPaint(new Color(0, 0, 0));
                g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{3, 3}, 3.0f));
                g2d.draw(preview);
                g2d.setPaint(new Color(255, 255, 255));
                g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{3, 3}, 0.0f));
                g2d.draw(preview);

                canvas.repaint();
            } //STAMP TOOL PREVIEW
            else if (toolbar.getCurrentTool() == ImageToolbar.STAMP) {
                canvas.clearGlassPane();
                Graphics2D g2d = (Graphics2D) canvas.getGlassPaneBufferedImage().createGraphics();
                g2d.drawImage(editorPane.getStampOptions().getSelectedStamp(), e.getX(), e.getY(), null);
                canvas.repaint();
            }
        }
    }

    private class CanvasWheelListener implements MouseWheelListener {

        public void mouseWheelMoved(MouseWheelEvent e) {

            editorPane.getCanvasScrollPane().dispatchEvent(e);
            if (e.isControlDown() == false) {
                return;
            }

            if (canvas.getZoomFactor() == 0.25f) {
                //mouse wheel was rotated away from user
                if (e.getWheelRotation() < 0) {
                    canvas.setZoomFactor(0.5f);
                }
            } else if (canvas.getZoomFactor() == 0.5f) {
                //mouse wheel was rotated away from user
                if (e.getWheelRotation() < 0) {
                    canvas.setZoomFactor(1.0f);
                } else {
                    canvas.setZoomFactor(0.25f);
                }
            } else if (canvas.getZoomFactor() == 1.0f) {
                //mouse wheel was rotated away from user
                if (e.getWheelRotation() < 0) {
                    canvas.setZoomFactor(1.5f);
                } else //mouse wheel rotated toward user
                {
                    canvas.setZoomFactor(0.5f);
                }
            } else if (canvas.getZoomFactor() == 1.5f) {
                //mouse wheel was rotated away from user
                if (e.getWheelRotation() < 0) {
                    canvas.setZoomFactor(2.0f);
                } else //mouse wheel rotated toward user
                {
                    canvas.setZoomFactor(1.0f);
                }
            } else if (canvas.getZoomFactor() == 2.0f) {
                //mouse wheel was rotated away from user
                if (e.getWheelRotation() < 0) {
                    canvas.setZoomFactor(3.0f);
                } else //mouse wheel rotated toward user
                {
                    canvas.setZoomFactor(1.5f);
                }
            } else if (canvas.getZoomFactor() == 3.0f) {
                //mouse wheel was rotated away from user
                if (e.getWheelRotation() < 0) {
                    canvas.setZoomFactor(4.0f);
                } else //mouse wheel rotated toward user
                {
                    canvas.setZoomFactor(2.0f);
                }
            } else if (canvas.getZoomFactor() == 4.0f) {
                //mouse wheel was rotated toward user
                if (e.getWheelRotation() > 0) {
                    canvas.setZoomFactor(3.0f);
                }
            }

            paintCurrentSelection();
        }
    }
}

