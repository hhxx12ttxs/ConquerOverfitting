/**
 *  Fingy. Educational programming environment for children.
 *  Copyright (C) 2010.  Konstantin Svirsky aka crome <mailto:c@informatics.by>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package by.informatics.jcx.fingy.ge.turtle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class TurtleRenderer
{
    private String turtleImageFileName = "res/turtle.png";

    private Color backColor = Color.WHITE;
    private Color axisColor = Color.GRAY;
    private BasicStroke axisStroke = new BasicStroke(1,
        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[] {3}, 0);
    private Font axisFont = new Font(null, Font.PLAIN, 12);
    private Color transparentColor = new Color(0, 0, 0, 0);
    private Turtle turtle;

    private TurtlePanel panel;

    private TurtleDrawableFigure lastDrawnFigure = null;

    private ArrayList<TurtleDrawableFigure> figures;
    private ArrayList<TurtleText> texts;
    private ArrayList<BackgroundPicture> pictures;

    private int xOffset;
    private int yOffset;

    private int xAxis;
    private int yAxis;

    private Graphics2D backLayer;
    private Graphics2D figuresLayer;

    private BufferedImage figuresImage;
    private BufferedImage turtleImage;

    private int maxWidth;
    private int maxHeight;

    private boolean axisEnabled = true;
    private boolean axisVisible = false;
    private boolean needForceRedraw = false;

    private int axisCaptionX =  10;
    private int axisCaptionY = -10;

    public TurtleRenderer(Turtle t, int width, int height)
    {
        panel     = new TurtlePanel(width, height);
        panel.setVisible(true);
        turtle    = t;

        figures   = new ArrayList<TurtleDrawableFigure>();
        texts     = new ArrayList<TurtleText>();

        pictures  = new ArrayList<BackgroundPicture>();

        maxWidth  = panel.getMaxWidth();
        maxHeight = panel.getMaxHeight();

        xOffset   = maxWidth  / 2;
        yOffset   = maxHeight / 2;

        setupLayers();

        try
        {
            turtleImage = ImageIO.read(new File(turtleImageFileName ));
        }
        catch(IOException e)
        {
            System.err.println("Can not load resource '" + turtleImageFileName + "'.");
            e.printStackTrace();
        }
    }

    public void drawTurtle()
    {
        int x = (int) turtle.getX() + xOffset;
        int y = (int) turtle.getY() + yOffset;
        int w = turtleImage.getWidth();
        int h = turtleImage.getHeight();

        Graphics2D turtleG = (Graphics2D)turtleImage.getGraphics();
        AffineTransform xForm = turtleG.getTransform();

        xForm.rotate(Math.toRadians(turtle.getAngle() + 90), w / 2, h / 2);
        BufferedImage b = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) b.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.drawImage(turtleImage, xForm, null);

        int x_pivot = (x - w / 2);
        int y_pivot = (y - h / 2);

        backLayer.drawImage(b, x_pivot, y_pivot, null);
    }

    public void drawPlot(double r)
    {
        double x = Math.round(turtle.getX());
        double y = Math.round(turtle.getY());
        Color c  = turtle.getTrackColor();

        figures.add(new TurtlePlot(x, y, r, c));
    }

    public void drawFigures(boolean drawForce)
    {
        int n = figures.size();

        if (n == 0)
        {
            return;
        }

        if (drawForce)
        {
            figuresLayer.setBackground(transparentColor);
            figuresLayer.clearRect(0, 0, maxWidth, maxHeight);

            for (int i = 0; i < n - 1; i++)
            {
                figures.get(i).draw(figuresLayer, xOffset, yOffset);
            }
            lastDrawnFigure = null;
        }

        TurtleDrawableFigure lastFigure = figures.get(n - 1);

        if (lastDrawnFigure != null &&
            lastDrawnFigure != lastFigure)
        {
            /*
             * Next logic assumes that drawFigures()
             * is called at least once between
             * adding of two figures.
             */
            lastDrawnFigure.draw(figuresLayer, xOffset, yOffset);
            lastDrawnFigure = lastFigure;
        }
        else if (lastDrawnFigure == null)
        {
            lastDrawnFigure = lastFigure;
        }

        int x = panel.getXView();
        int y = panel.getYView();
        int w = panel.getWidth();
        int h = panel.getHeight();
        backLayer.drawImage(figuresImage, x, y, x + w, y + h, x, y, x + w, y + h, null);

        lastFigure.draw(backLayer, xOffset, yOffset);
    }

    public void clearScene()
    {
        cleanDrawingArea();

        figuresLayer.setBackground(transparentColor);
        figuresLayer.clearRect(0, 0, maxWidth, maxHeight);
        lastDrawnFigure = null;
    }

    public void drawPicture(String fileName, int x, int y)
    {
        try
        {
            BackgroundPicture p = new BackgroundPicture(ImageIO.read(new File(fileName)), x, y);
            pictures.add(p);
        }
        catch(IOException e)
        {
            System.err.println("Can not load resource '" + fileName + "'.");
            e.printStackTrace();
        }
    }

    public void setBackgroundColor(Color c)
    {
        backColor = c;
    }

    public void setXYView(int x, int y)
    {
        int w  = getWidth();
        int mw = panel.getMaxWidth();
        int h  = getHeight();
        int mh = panel.getMaxHeight();

        if (x + xOffset < 0 || x + w + xOffset > maxWidth ||
            yOffset - y < 0 || yOffset - y + h > maxHeight)
        {
            xOffset = - x + (mw - w) / 2;
            yOffset =   y + (mh - h) / 2;

            needForceRedraw = true;
//            if (mh - h < c || mw - w < c) { ... }
//            panel.recreateBackImage(w, h);
//            setupLayers();
            panel.setDefaultViewOffset();

            return;
        }

        panel.setXView(x + xOffset);
        panel.setYView(yOffset - y);
    }

    public int getXView()
    {
        return panel.getXView() - xOffset;
    }

    public int getYView()
    {
        return yOffset - panel.getYView();
    }

    private void cleanDrawingArea()
    {
        backLayer.setBackground(backColor);
        backLayer.clearRect(0, 0, maxWidth, maxHeight);
    }

    public void drawAxis()
    {
        backLayer.setColor(axisColor);
        Stroke s = backLayer.getStroke();
        backLayer.setStroke(axisStroke);
        int xo = panel.getXView();
        int yo = panel.getYView();
        backLayer.drawLine(0 + xo, yAxis + yo, getWidth() + xo, yAxis + yo);
        backLayer.drawLine(xAxis + xo, 0 + yo, xAxis + xo, getHeight() + yo);
        backLayer.setStroke(s);
        Font f = backLayer.getFont();
        backLayer.setFont(axisFont);
        backLayer.drawString(String.format("[ %1d; %2d]",  xAxis + xo - xOffset,
            - yAxis - yo + yOffset), xAxis + xo + axisCaptionX, yAxis + yo + axisCaptionY);
        backLayer.setFont(f);
    }

    private void drawBackgoundPicture()
    {
        for (int i = 0; i < pictures.size(); i++)
        {
            BackgroundPicture p = pictures.get(i);

            backLayer.drawImage(p.getPicture(), p.getX() + xOffset, p.getY() + yOffset, null);
        }
    }

    public void drawScene()
    {
        if (maxHeight != panel.getMaxHeight() || maxWidth != panel.getMaxWidth())
        {
            maxWidth  = panel.getMaxWidth();
            maxHeight = panel.getMaxHeight();
            setupLayers();
            needForceRedraw = true;
        }
        cleanDrawingArea();

        drawBackgoundPicture();

        drawFigures(needForceRedraw);
        needForceRedraw = false;

        drawTurtle();

        if (isAxisEnabled() && isAxisVisible())
        {
            drawAxis();
        }

        drawText();

        panel.repaint();
    }

    private void drawText()
    {
        for (int i = 0; i < texts.size(); i++)
        {
            TurtleText t = texts.get(i);
            backLayer.setColor(t.getColor());
            backLayer.setFont(t.getFont());
            backLayer.drawString(t.getText(), (int) t.getX() + panel.getXView(),
                (int) t.getY() + panel.getYView());
        }
    }

    public void setAxisXY(int x, int y)
    {
        xAxis = x;
        yAxis = y;
    }

    public void renderScene()
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    drawScene();
                }
            });
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    public boolean isAxisEnabled()
    {
        return axisEnabled;
    }

    public void setAxisEnabled(boolean axisEnabled)
    {
        this.axisEnabled = axisEnabled;
    }

    public boolean isAxisVisible()
    {
        return axisVisible;
    }

    public void setAxisVisible(boolean axisVisible)
    {
        this.axisVisible = axisVisible;
    }

    public void addMouseListener(MouseListener l)
    {
        panel.addMouseListener(l);
    }

    public void addMouseMotionListener(MouseMotionListener l)
    {
        panel.addMouseMotionListener(l);
    }

    public void addComponentListener(ComponentListener l)
    {
        panel.addComponentListener(l);
    }

    int getWidth()
    {
        return panel.getWidth();
    }

    int getHeight()
    {
        return panel.getHeight();
    }

    JPanel getPanel()
    {
        return panel;
    }

    void setPreferredSize(Dimension d)
    {
        panel.setPreferredSize(d);
    }

    void clearFigures()
    {
        figures.clear();
    }

    void addTurtleTrack(TurtleTrack t)
    {
        figures.add(t);
    }

    void addTurtleText(TurtleText t)
    {
        if (t.getCoordinateType() == TurtleText.GLOBALCOORDINATE)
        {
            figures.add(t);
        }
        else
        {
            texts.add(t);
        }
    }

    private void setupLayers()
    {
        backLayer    = panel.getGraphics2D();
        figuresImage = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
        figuresLayer = (Graphics2D) figuresImage.getGraphics();
        figuresLayer.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
    }
}

