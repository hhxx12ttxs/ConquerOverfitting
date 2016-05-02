package com.beadingschememaker.gui.pallete;

import com.beadingschememaker.gui.ApproveFileChooser;
import com.beadingschememaker.gui.BeadingPanel;
import com.beadingschememaker.utils.Colors;
import com.beadingschememaker.utils.Utils;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.event.MouseInputAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Aloren
 */
public class Pallete extends JPanel implements Observer, Scrollable {

    private Box mouseOverBox = null;
    private BeadingPanel beadingPanel;
    private ArrayList<Box> colorBoxes = new ArrayList<>();
    private MODE mode = MODE.Simple;
    private ColorBoxPanel colorBoxPanel;
    public static final int X_BEGIN_OFFSET = 5;
    public static final int Y_BEGIN_OFFSET = 5;
    public static final int GAP = 5;

    public void save() {
        JFileChooser fc = new ApproveFileChooser(ApproveFileChooser.getPalleteFilters());
        int res = fc.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!file.exists()) {
                try {
                    file = Utils.checkExtension(file, Utils.pal);
                    file.createNewFile();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error creating file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            try {
                this.save(file);
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "No such file.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ParserConfigurationException | TransformerException ex) {
                JOptionPane.showMessageDialog(this, "Error while parsing file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void load() {
        JFileChooser fc = new ApproveFileChooser(ApproveFileChooser.getPalleteFilters());
        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                this.load(f);
            } catch (ParserConfigurationException | SAXException ex) {
                JOptionPane.showMessageDialog(this, "Error parsing file.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error reading file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static enum MODE {

        Simple, ActiveSelect
    };

    public Pallete() {
        colorBoxes.add(new EmptyColorBox());
        ColorBoxMouseInputAdapter m = new ColorBoxMouseInputAdapter();
        addMouseListener(m);
        addMouseMotionListener(m);
    }

    public void save(File file) throws FileNotFoundException, ParserConfigurationException, TransformerConfigurationException, TransformerException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document doc = builder.newDocument();
        Element rootEl = doc.createElement("pallete");
        doc.appendChild(rootEl);

        Color[] colors = getColors();
        for (int i = 0; i < colors.length; i++) {
            Color color = colors[i];
            Element colorEl = doc.createElement("color");
            colorEl.appendChild(doc.createTextNode(Integer.toString(color.getRGB())));
            rootEl.appendChild(colorEl);
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    public void load(File file) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();
        NodeList list = doc.getElementsByTagName("color");
        Color[] newColors = new Color[list.getLength()];
        for (int i = 0; i < list.getLength(); i++) {
            Node nNode = list.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) nNode;
                newColors[i] = new Color(Integer.parseInt(el.getTextContent()));
            }
        }
        addColors(newColors);
        repaint();
    }

    public void addRandomColors(int number) {
        addColors(getRandomColors(number));
    }

    public void setRandomColors(int number) {
        setColors(getRandomColors(number));
    }

    public void setColors(Color[] colors) {
        colorBoxes = new ArrayList<>(colors.length + 1);
        colorBoxes.add(new EmptyColorBox());
        for (int i = 0; i < colors.length; i++) {
            colorBoxes.add(new ColorBox(colors[i]));
        }
    }

    public Color[] getColors() {
        if (isEmpty()) {
            return new Color[]{};
        }
        Color[] colors = new Color[colorBoxes.size() - 1];
        for (int i = 0, ii = 1; i < colors.length; i++, ii++) {
            colors[i] = colorBoxes.get(ii).getColor();
        }
        return colors;
    }

    private static Color[] getRandomColors(int number) {
        Random rand = new Random();
        Color[] randomColors = new Color[number];
        for (int i = 0; i < number; i++) {
            randomColors[i] = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
        }
        return randomColors;
    }

    public static int getPreferredHeight(int rows) {
        int height = rows * Box.height + (rows - 1) * Pallete.GAP + 2 * Pallete.Y_BEGIN_OFFSET;
        return height;
    }

    public boolean isEmpty() {
        return colorBoxes.size() <= 1;
    }

    private int getCols(int xoffset, int boxWidth) {
        int allWidth = this.getWidth() - 2 * xoffset;
        int visibleCols = (int) Math.ceil((double) ((allWidth + GAP) / (boxWidth + GAP)));
        if (visibleCols == 0) {
            visibleCols = 1;
        }
        if (visibleCols > colorBoxes.size()) {//|| visibleCols < colorBoxes.size()
            visibleCols = colorBoxes.size();
        }
        return visibleCols;
    }

    private int getRows(int visibleCols) {
        int rows = (int) Math.ceil(((double) colorBoxes.size() / visibleCols));
        return rows;
    }

    private void updatePrefferedSize(int cols, int rows) {
        int w = cols * Box.width + (cols - 1) * GAP + 2 * X_BEGIN_OFFSET;
        int h = rows * Box.height + (rows - 1) * GAP + 2 * Y_BEGIN_OFFSET;
        setPreferredSize(new Dimension(w, h));
    }

    public void addColor(Color c) {
        ColorBox cb = new ColorBox(c);
        if (!colorBoxes.contains(cb)) {
            colorBoxes.add(cb);
            int cols = getCols(X_BEGIN_OFFSET, Box.width);
            int rows = getRows(cols);
            updatePrefferedSize(cols, rows);
            revalidate();
        }
    }

    private void deleteColorBox(Box colorBox) {
        colorBoxes.remove(colorBox);
    }

    private void deleteColorBoxes() {
        colorBoxes.clear();
        colorBoxes.add(new EmptyColorBox());
    }

    public void addColors(Color[] c) {
        for (int i = 0; i < c.length; i++) {
            ColorBox colorBox = new ColorBox(c[i]);
            if (!colorBoxes.contains(colorBox)) {
                colorBoxes.add(colorBox);
            }
        }
        int cols = getCols(X_BEGIN_OFFSET, Box.width);
        int rows = getRows(cols);
        updatePrefferedSize(cols, rows);
        revalidate();
    }

    public void setMode(MODE newMode) {
        switch (newMode) {
            case Simple:
                colorBoxPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                break;
            case ActiveSelect:
                colorBoxPanel.setCursor(getColorPickerCursor());
                break;
            default:
                throw new RuntimeException("New mode added!");
        }
        this.mode = newMode;
    }

    private Cursor getColorPickerCursor() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Cursor cursor = toolkit.createCustomCursor(Toolkit.getDefaultToolkit().getImage(this.getClass().
                getResource("/cursors/colorpicker.gif")), new Point(0, 31), "ColorPicker");
        return cursor;
    }

    /**
     * Adds new ColorBox when new color for Bead was selected and if the pallete
     * doesn't contain this color.
     *
     * @param o
     * @param newColor
     */
    @Override
    public void update(Observable o, Object newColor) {
        if (newColor instanceof Color) {
            Color color = (Color) newColor;
            ColorBox cb = new ColorBox(color);
            if (!colorBoxes.contains(cb)) {
                colorBoxes.add(cb);
            }
            repaint();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int w = getWidth();
        int h = getHeight();

        g2d.setColor(Colors.background);
        g2d.fillRect(0, 0, w, h);
        //Paint ColorBoxes
        if (!colorBoxes.isEmpty()) {
            int xoffset = X_BEGIN_OFFSET;
            int yoffset = Y_BEGIN_OFFSET;

            int boxWidth = Box.width;
            int boxHeight = Box.height;
            int cols = getCols(xoffset, boxWidth);
            int rows = getRows(cols);

            Iterator<Box> it = colorBoxes.iterator();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (it.hasNext()) {
                        Box box = it.next();
                        box.setX(xoffset);
                        box.setY(yoffset);
                        box.draw(g2d, xoffset, yoffset);
                    }
                    xoffset += boxWidth + GAP;
                }
                xoffset = 5;
                yoffset += boxHeight + GAP;
            }
            updatePrefferedSize(cols, rows);
        }
    }

    public void setColorBoxPanel(ColorBoxPanel colorBoxPanel) {
        this.colorBoxPanel = colorBoxPanel;
    }

    public void setBeadingPanel(BeadingPanel beadingPanel) {
        this.beadingPanel = beadingPanel;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Math.max(visibleRect.height * 9 / 10, 1);
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        if (getParent() instanceof JViewport) {
            JViewport viewport = (JViewport) getParent();
            return this.getPreferredSize().height < viewport.getHeight();
        }
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Math.max(visibleRect.height / 10, 1);
    }

    private class ColorBoxMouseInputAdapter extends MouseInputAdapter {

        @Override
        public void mouseReleased(MouseEvent me) {
            int clickCount = me.getClickCount();
            if (me.getButton() == MouseEvent.BUTTON3) {
                for (final Box colorBox : colorBoxes) {
                    if (colorBox.containsPoint(me.getX(), me.getY())) {
                        if (colorBox instanceof EmptyColorBox) {
                            return;
                        }
                        if (me.isPopupTrigger()) {
                            new ColorPopupMenu((ColorBox) colorBox).show(me.getComponent(), me.getX(), me.getY());
                        }
                        return;
                    }
                }
                if (me.isPopupTrigger()) {
                    new PalletePopupMenu().show(me.getComponent(), me.getX(), me.getY());
                }
                return;
            }
            switch (mode) {
                case ActiveSelect://select color for active color
                    if (clickCount != 1) {
                        return;
                    }
                    for (Box colorBox : colorBoxes) {
                        if (colorBox.containsPoint(me.getX(), me.getY())) {
                            //TODO this is bad
                            Color color = colorBox.getColor();
                            if (color != null) {
                                if (colorBox instanceof EmptyColorBox) {
                                    addColor(color);
                                    repaint();
                                }
                                colorBoxPanel.getActiveColorPanel().setActiveBrushColor(color);
                                break;
                            }

                        }
                    }
                    break;
                case Simple:
                    if (clickCount != 1) {
                        return;
                    }
                    for (Box colorBox : colorBoxes) {
                        if (colorBox.containsPoint(me.getX(), me.getY())) {
                            //TODO this is bad
                            if (colorBox instanceof EmptyColorBox) {
                                Color color = colorBox.getColor();
                                if (color != null) {
                                    addColor(color);
                                    repaint();
                                }
                            } else {
                                Color newColor = JColorChooser.showDialog(null, "Change color", colorBox.getColor());
                                if (newColor != null) {
                                    if (beadingPanel != null) {
                                        int result = JOptionPane.showConfirmDialog(null, "Substitute color for beads?", "Color substitution", JOptionPane.YES_NO_CANCEL_OPTION);
                                        if (result == JOptionPane.YES_OPTION) {
                                            beadingPanel.substituteBeadsColor(colorBox.getColor(), newColor);
                                        }
                                    }
                                    colorBox.setColor(newColor);
                                    repaint(colorBox.getX(), colorBox.getY(), Box.width, Box.height);
                                }
                            }
                            break;
                        }
                    }
                    break;
            }

        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (mouseOverBox == null) {
                for (Box colorBox : colorBoxes) {
                    if (colorBox.containsPoint(e.getX(), e.getY())) {
                        mouseOverBox = colorBox;
                        colorBox.setMouseOver(true);
                        repaint(colorBox.getX(), colorBox.getY(),
                                Box.width + 2, Box.height + 2);
                        break;
                    }
                }
            } else {
                if (!mouseOverBox.containsPoint(e.getX(), e.getY())) {
                    mouseOverBox.setMouseOver(false);
                    repaint(mouseOverBox.getX(), mouseOverBox.getY(),
                            Box.width + 2, Box.height + 2);
                    mouseOverBox = null;

                }
            }

        }

        private class PalletePopupMenu extends JPopupMenu {

            public PalletePopupMenu() {
                JMenuItem loadItem = new JMenuItem("Load");
                JMenuItem saveItem = new JMenuItem("Save");
                add(loadItem);
                add(saveItem);
                loadItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        load();
                        Pallete.this.repaint();
                    }
                });
                saveItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        save();
                        Pallete.this.repaint();
                    }
                });
            }
        }

        private class ColorPopupMenu extends JPopupMenu {

            public ColorPopupMenu(final ColorBox colorBox) {
                JMenuItem deleteItem = new JMenuItem("Delete");
                JMenuItem deleteAllItem = new JMenuItem("Delete all");
                add(deleteItem);
                add(deleteAllItem);
                deleteItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        deleteColorBox(colorBox);
                        Pallete.this.repaint();
                    }
                });
                deleteAllItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        deleteColorBoxes();
                        Pallete.this.repaint();
                    }
                });
            }
        }
    }
}

