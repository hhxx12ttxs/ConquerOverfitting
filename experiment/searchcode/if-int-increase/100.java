/*
 * TjumbnailPane.java
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * Displays a panel of thumbnails. Allows the user to make a selection.
 *
 * @author Thomas Everingham
 * @version 1.0
 */
public class ThumbnailPane extends JPanel{
    /**
     * The array of icons to display in this thumbnail pane.
     */
    protected ImageIcon[] thumbnailImages = new ImageIcon[0];
    /**
     * Array of Thumbnail objects
     */
    protected Thumbnail[] thumbnails = new Thumbnail[0];
    /**
     * Required display size for thumbnails; can be between 10 and 150.
     */
    protected int thumbnailSize = 48;
    /**
     * The currently selected thumbnail.
     */
    protected int selection = 0;
    /**
     * Colour of the border of the panel, as well as the border of the selection.
     */
    protected Color borders = new Color(26, 97, 110);
    /**
     * Colour of the selection fill; this must have an associated transparency.
     */
    protected Color selectionFill = new Color(220, 97, 110, 50);
    /**
     * Colour of the background of the panel.
     */
    protected Color background = new Color(222, 233, 233);

    /**
     * The number of columns required.
     */
    protected int columns = 5;
    /**
     * Space placed between thumbnails horizontally.
     */
    protected int horizontalSpacing = 8;
    /**
     * Space placed between thumbnails vertically.
     */
    protected int verticalSpacing = 8;
    /**
     *
     */
    protected MouseListener[] thumbnailMouseListeners = new MouseListener[0];
    
    /**
     * Creates a new ThumbnailPane with no images, size 48 squares, and a 5 column layout.
     */
    public ThumbnailPane()
    {
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    }

    /**
     * Creates a new thumbnail pane with no images and a 5 column layout, and the
     * specified sized thumbnails.
     * @param thumbnailSize the required size of the thumbnails.
     */
    public ThumbnailPane(int thumbnailSize)
    {
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.thumbnailSize = thumbnailSize;
    }

    /**
     * Creates a new thumbnail pane with no images, the specified column layout, and the
     * specified sized thumbnails.
     * @param thumbnailSize the required size of the thumbnails.
     */
    public ThumbnailPane(int thumbnailSize, int columns)
    {
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.thumbnailSize = thumbnailSize;
        this.columns = columns;
    }

    /**
     * Creates a new thumbnail pane with the supplied images, at the
     * specified thumbnail size. Default grid layout of 5 columns is used.
     * @param thumbnails each image in this array is converted to a thumbnail and added to the pane.
     * @param thumbnailSize the size of the thumbnail squares.
     */
    public ThumbnailPane(ImageIcon[] thumbnails, int thumbnailSize)
    {
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.thumbnailImages = thumbnails;
        this.thumbnailSize = thumbnailSize;
        this.rebuildThumbnails();
    }

    /**
     * Creates a new thumbnail pane with the supplied images at the specified thumbnail size
     * and with the requested number of columns
     * @param thumbnails each image in this array is converted to a thumbnail and added to the pane.
     * @param thumbnailSize the size of the thumbnail squares.
     * @param columns number of columns to use.
     */
    public ThumbnailPane(ImageIcon[] thumbnails, int thumbnailSize, int columns)
    {
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.thumbnailImages = thumbnails;
        this.thumbnailSize = thumbnailSize;
        this.columns = columns;
    }

    /**
     * Sets the side length of the thumbnails; thumbnails are always square.
     * @param size the side length of the thumbnails.
     */
    public void setThumbnailSize(int size)
    {
        thumbnailSize = size;
        for (int i = 0; i < thumbnails.length; i++)
        {
            thumbnails[i].setIconSize(size);
        }
    }

    /**
     * Gets the horizontal space added between thumbnails.
     * @return the horizontal space added between thumbnails.
     */
    public int getHorizontalSpacing()
    {
        return horizontalSpacing;
    }

    /**
     * Gets the vertical space added between thumbnails.
     * @return the vertical space added between thumbnails.
     */
    public int getVerticalSpacing()
    {
        return verticalSpacing;
    }

    /**
     * Sets the horizontal space added between thumbnails.
     * @param horizontal the horizontal space added between thumbnails.
     */
    public void setHorizontalSpacing(int horizontal)
    {
        horizontalSpacing = horizontal;
    }

    /**
     * Sets the vertical space added between thumbnails.
     * @param vertical the vertical space added between thumbnails.
     */
    public void setVerticalSpacing(int vertical)
    {
        verticalSpacing = vertical;
    }

    /**
     * Rebuild the array of thumbnails. Called when the array of
     * ImageIcons is changed.
     */
    private void rebuildThumbnails()
    {
        this.removeAll();
        thumbnails = new Thumbnail[thumbnailImages.length];
        boolean working = true;
        Box horizontalBox = Box.createHorizontalBox();
        horizontalBox.add(Box.createHorizontalStrut(horizontalSpacing));
        int colsAdded = 0;
        this.add(Box.createVerticalStrut(verticalSpacing));
        for (int i = 0; i < thumbnailImages.length; i++)
        {
            working = true;
            //if we've not yet added the specified number of columns...
            if (colsAdded < columns)
            {
                //increase the column count
                colsAdded++;
                //create a new thumbnail
                thumbnails[i] = new Thumbnail(thumbnailImages[i],thumbnailSize,i);
                addListeners(thumbnails[i]);
                //add it to the box
                horizontalBox.add(thumbnails[i]);
                //add spacing before the next thumbnail if there will be one
                if (colsAdded < columns)
                    horizontalBox.add(Box.createHorizontalStrut(horizontalSpacing));
                else
                    horizontalBox.add(Box.createGlue());
            }
            else
            {
                //not working any more at this instant, as we've just added this box
                //only start the state of working again when we iterate thumbnails into a new box.
                working = false;
                this.add(horizontalBox);
                this.add(Box.createVerticalStrut(verticalSpacing));
                horizontalBox = Box.createHorizontalBox();
                horizontalBox.add(Box.createHorizontalStrut(horizontalSpacing));
                colsAdded = 0;

                //and now proceed with the current thumbnail
                //increase the column count
                colsAdded++;
                //create a new thumbnail
                thumbnails[i] = new Thumbnail(thumbnailImages[i],thumbnailSize,i);
                addListeners(thumbnails[i]);
                //add it to the box
                horizontalBox.add(thumbnails[i]);
                //add spacing before the next thumbnail if there will be one
                if (colsAdded < columns)
                    horizontalBox.add(Box.createHorizontalStrut(horizontalSpacing));
                else
                    horizontalBox.add(Box.createGlue());
            }
        }

        //if we were working when we ran out of thumbnails, then the current box
        //had not been added yet. Add it now.
        if (working)
        {
            horizontalBox.add(Box.createGlue());
            this.add(horizontalBox);
        }
        this.add(Box.createVerticalStrut(verticalSpacing));
        this.add(Box.createVerticalGlue());
    }

    /**
     * Replaces the array of icons to use for this thumbnail pane.
     * @param thumbnails the array of icons to use for this thumbnail pane.
     */
    public void setThumbnails(ImageIcon[] thumbnails)
    {
        selection = 0;
        this.thumbnailImages = thumbnails;
        rebuildThumbnails();
    }

    /**
     * Clears all current images. This method will not update the interface.
     * If you intend on adding thumbnails directly after clearing, then this will
     * update the interface. If not, then use <code>setThumbnails(new ImageIcon[0])</code>.
     */
    public void clearThumbnails()
    {
        thumbnailImages = new ImageIcon[0];
    }

    /**
     * Adds all of the icons in the specified array to the end of the current array
     * of icons.
     * @param thumbnails the icons to append to this thumbnail pane.
     */
    public void appendThumbnails(ImageIcon[] thumbnails)
    {
        ImageIcon[] temp = new ImageIcon[thumbnailImages.length + thumbnails.length];
        System.arraycopy(thumbnailImages, 0, temp, 0, thumbnailImages.length);
        System.arraycopy(thumbnails,0,temp,thumbnailImages.length,thumbnails.length);
        thumbnailImages = temp;
        rebuildThumbnails();
    }

    /**
     * Adds the specified icon to the end of the current icon array.
     * @param thumbnail the thumbnail to add to the end of the current icon array.
     */
    public void addThumbnail(ImageIcon thumbnail)
    {
        appendThumbnails(new ImageIcon[] {thumbnail});
    }

    /**
     * Gets the current array of icons encapsulated by this thumbnail pane.
     * @return the current array of icons encapsulated by this thumbnail pane.
     */
    public ImageIcon[] getThumbnails()
    {
        return thumbnailImages;
    }

    /**
     * Gets the thumbnail at the specified index.
     * @param index the index of the required thumbnail.
     * @return the thumbnail at the specified index.
     */
    public ImageIcon getThumbnail(int index)
    {
        return thumbnailImages[index];
    }

    /**
     * Replaces the thumbnail at the specified index with the specified thumbnail.
     * @param index the index of the thumbnail to replace.
     * @param newThumbnail the new thumbnail to be placed at the specified index.
     */
    public void replaceThumbnail(int index, ImageIcon newThumbnail)
    {
        if ((index >= 0) && (index < thumbnails.length))
        {
            thumbnails[index] = new Thumbnail(newThumbnail, thumbnailSize,index);
            addListeners(thumbnails[index]);
        }
        else
            throw new IndexOutOfBoundsException("Replacement index is out of thumbnail array bounds.");
    }

    /**
     * Gets the currently selected index.
     * @return the currently selected index.
     */
    public int getSelectedIndex()
    {
        return selection;
    }

    /**
     * Sets the currently selected index to that specified.
     * @param selection the new selected index.
     */
    public void setSelectedIndex(int selection)
    {
        if ((selection >= 0) && (selection < thumbnails.length))
        {
            try {
            thumbnails[this.selection].setSelected(false);
            this.selection = selection;
            thumbnails[selection].setSelected(true);
            repaint();
            }
            catch (IndexOutOfBoundsException e)
            {
                throw new IndexOutOfBoundsException("No thumbnail of index " + selection + ".");
            }
        }
        else
            throw new IndexOutOfBoundsException("No thumbnail of index " + selection + ".");

        thumbnailSelectionChanged();
    }

    /**
     * Gets the currently selected thumbnail.
     * @return the currently selected thumbnail.
     */
    public ImageIcon getSelectedThumbnail()
    {
        return thumbnailImages[selection];
    }

    private void addListeners(Thumbnail t)
    {
        for (int i = 0; i < thumbnailMouseListeners.length; i++)
            t.addMouseListener(thumbnailMouseListeners[i]);
    }

    /**
     * Adds a <code>MouseListener</code> to every thumbnail that exists or is subsequently added.
     * @param listener the listener to add.
     */
    public void addThumbnailMouseListener(MouseListener listener)
    {
        if (thumbnailMouseListeners.length == 0) {
            thumbnailMouseListeners = new MouseListener[]{listener};
        } else {
            MouseListener[] temp = new MouseListener[thumbnailMouseListeners.length+1];
            System.arraycopy(thumbnailMouseListeners, 0, temp, 0, thumbnailMouseListeners.length);
            temp[thumbnailMouseListeners.length] = listener;
            thumbnailMouseListeners = temp;
        }
        rebuildThumbnails();
    }

    /**
     * Remove this <code>MouseListener</code> from all existing thumbnails,
     * and no longer add it to subsequent thumbnails.
     * @param listener the listener to remove.
     * @return true if the operation is successful, and false if the listener could not be found and hence not removed.
     */
    public boolean removeThumbnailMouseListener(MouseListener listener)
    {
        if (thumbnailMouseListeners.length == 0)
            return false;
        if (thumbnailMouseListeners.length == 1)
            if (thumbnailMouseListeners[0].equals(listener))
            {
                thumbnailMouseListeners = new MouseListener[0];
                rebuildThumbnails();
                return true;
            }
            else
                return false;

        int index = -1;
        //get the index
        for (int i = 0; i < thumbnailMouseListeners.length; i++)
        {
            if (thumbnailMouseListeners[i].equals(listener))
            {
                index = i;
                break;
            }
        }

        //if index is -1, we have not found the listener
        if (index == -1)
            return false;

        //otherwise, get rid of the listener
        MouseListener[] temp = new MouseListener[thumbnailMouseListeners.length-1];
        if (index == 0)
        {
            System.arraycopy(thumbnailMouseListeners, 1, temp, 0, thumbnailMouseListeners.length-1);
            thumbnailMouseListeners = temp;
            rebuildThumbnails();
            return true;
        }
        else if (index == thumbnailMouseListeners.length-1)
        {
            System.arraycopy(thumbnailMouseListeners, 0, temp, 0, thumbnailMouseListeners.length-1);
            thumbnailMouseListeners = temp;
            rebuildThumbnails();
            return true;
        }
        else //the index is not on the edge of the array
        {
            System.arraycopy(thumbnailMouseListeners, 0, temp, 0, index);
            System.arraycopy(thumbnailMouseListeners, index+1, temp, index, thumbnailMouseListeners.length-index-1);
            thumbnailMouseListeners = temp;
            rebuildThumbnails();
            return true;
        }
    }

    /**
     * This method is called whenever the selected thumbnail is changed. It can
     * be overridden so that extending classes may offer extra functionality.
     */
    protected void thumbnailSelectionChanged()
    {

    }

    @Override
    public void paintComponent(Graphics g)
    {
        //run super method
        super.paintComponent(g);

        //initialise required objects
        Graphics2D g2d = (Graphics2D) g;
        RoundRectangle2D.Float shapeBg = new RoundRectangle2D.Float(3.0f, 3.0f, this.getWidth() - 6, this.getHeight() - 6, 6.0f, 6.0f);

        //set graphics options
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //fill the background of the menu
        g2d.setPaint(background);
        g2d.fill(shapeBg);

        //draw the border
        g2d.setPaint(borders);
        g2d.draw(shapeBg);
    }

}

