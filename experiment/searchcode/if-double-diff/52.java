
/**
 * Title:        <p>
 * Description:  <p>
 * Copyright:    Copyright (c) <p>
 * Company:      <p>
 * @author
 * @version 1.0
 */
package wsl.fw.report;

// imports
import java.util.Vector;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.print.*;
import wsl.fw.util.Util;
import wsl.fw.util.Log;
import wsl.fw.util.Type;

/**
 * Encapsulates a section of a report
 */
public class WslReportSection
{
    //--------------------------------------------------------------------------
    // constants

    public static double CALCULATED_HEIGHT = -1.0;
    public static double DEFAULT_COLUMN_WIDTH = 100;
    public static double DEFAULT_ROW_HEIGHT = 24;

    //--------------------------------------------------------------------------
    // attributes

    private double _sectionHeight = CALCULATED_HEIGHT;
    private double _calcHeight = -1;
    private double _pageHeight = CALCULATED_HEIGHT;
    private int[] _tabs;
    private Vector _printElements = new Vector();
    private boolean _printOnce = false;


    //--------------------------------------------------------------------------
    // constructors

    /**
     * Blank ctor
     */
    public WslReportSection()
    {
    }

    /**
     * Ctor taking tab stops
     * @param tabs tab stops
     */
    public WslReportSection(int[] tabs)
    {
        setTabStops(tabs);
    }


    //--------------------------------------------------------------------------
    // setup

    /**
     * Advise the section to only print once per page
     * @param printOnce
     */
    public void setPrintOnce(boolean printOnce)
    {
        _printOnce = printOnce;
    }

    /**
     * @return boolean true if the section shoulf print once per page
     */
    public boolean doPrintOnce()
    {
        return _printOnce;
    }

    /**
     * @return int the number of pages that this section requires
     * @param the height of a page
     */
    public int getNumPages(double pageHeight)
    {
        return 1;
    }

    /**
     * Set the height of the section
     * @param sectionHeight
     */
    public void setSectionHeight(double sectionHeight)
    {
        _sectionHeight = sectionHeight;
    }

    /**
     * @return double the height of the section
     */
    public double getSectionHeight()
    {
        if(_sectionHeight == CALCULATED_HEIGHT)
        {
            // if cached, return
            if(_calcHeight >= 0)
                return _calcHeight;

            // iterate pes
            int maxY = 0;
            PrintElement pe;
            for(int i = 0; i < _printElements.size(); i++)
            {
                // get the element and its height
                pe = (PrintElement)_printElements.elementAt(i);
                if(pe != null)
                {
                    Point pos = getPrintElementPosition(pe);
                    if(pos.y > maxY)
                        maxY = pos.y;
                }
            }

            // add row height
            _calcHeight = maxY + DEFAULT_ROW_HEIGHT;
            return _calcHeight;
        }
        else
            return _sectionHeight;
    }

    /**
     * Set the height of the page
     * @param pageHeight
     */
    public void setPageHeight(double pageHeight)
    {
        _pageHeight = pageHeight;
    }

    /**
     * @return double the maximum page height
     */
    public double getPageHeight()
    {
        return _pageHeight;
    }

    /**
     * Set tab stops into the section
     * @param tabs
     */
    public void setTabStops(int[] tabs)
    {
        _tabs = tabs;
    }


    //--------------------------------------------------------------------------
    // print elements

    public void addPrintElement(PrintElement pe)
    {
        _printElements.add(pe);
    }

    /**
     * @return Vector the vector of PrintElements
     */
    protected Vector getPrintElements()
    {
        return _printElements;
    }

    /**
     * Set a value into a field
     * @param value the value to set
     */
    public void setFieldValue(String name, String value)
    {
        // get the field
        TextFieldElement tfe = getField(name);
        if(tfe != null)
            tfe.setText(value);
    }

    /**
     * Find and return a TextFieldElement by name
     * @param name the name of the TextFieldElement
     * @return TextFieldElement
     */
    protected TextFieldElement getField(String name)
    {
        // iterate pe vector
        TextFieldElement tfe;
        PrintElement pe;
        for(int i = 0; i < _printElements.size(); i++)
        {
            // get the pe and check class
            pe = (PrintElement)_printElements.elementAt(i);
            if(pe != null && pe instanceof TextFieldElement)
            {
                // compare field name
                tfe = (TextFieldElement)pe;
                if(tfe.getFieldName().equals(name))
                    return tfe;
            }
        }

        // not found
        return null;
    }

    /**
     * @return Point the position of a PrintElement
     * @param pe the PrintElement
     */
    private Point getPrintElementPosition(PrintElement pe)
    {
        // verify param
        Util.argCheckNull(pe);

        // if the pe has a position use that
        WslPos pos = pe.getPosition();
        Point p = pos._pos;

        // if no point, calculate from row and column
        if(p == null)
        {
            // get the coordinate positions
            double x = pos._x;
            double y = pos._y;

            // if no coordinate x, use column
            if(x == Type.NULL_DOUBLE)
            {
                // use tab stops for x
                int col = pos._col;
                if(_tabs != null && col <= _tabs.length && col > 0)
                    x = (double)_tabs[col - 1];
                else
                    x = (double)col * DEFAULT_COLUMN_WIDTH;
            }

            // if no coordinate y, use row
            if(y == Type.NULL_DOUBLE)
            {
                // row pos uses default height for now
                y = (double)pos._row * DEFAULT_ROW_HEIGHT;
            }

            // create the pos
            p = new Point((int)x, (int)y);
        }

        // return
        return p;
    }


    //--------------------------------------------------------------------------
    // printing

    /**
     * Print the section
     * @param g the Graphics context
     */
    public void printSection(Graphics2D g, PageFormat pf, int page)
    {
        // iterate the PrintElements
        PrintElement pe;
        PrintData pd = new PrintData();
        pd.g = g;
        pd.pf = pf;
        pd.page = page;
        for(int i = 0; i < _printElements.size(); i++)
        {
            // get the element
            pe = (PrintElement)_printElements.elementAt(i);
            if(pe != null)
            {
                // get the position of the element
                pd.pos = getPrintElementPosition(pe);

                // draw the element
                pe.drawElement(pd);
            }
        }

        // translate the graphics context
        g.translate(0, (int)getSectionHeight());
    }

    /**
     * Print a page of the section
     * @param g the Graphics context
     * @param pf the PageFormat
     * @param page the page to print
     * @return int Printable.PAGE_EXISTS if valid page, else Printable.NO_SUCH_PAGE
     */
    public int printPage(Graphics2D g, PageFormat pf, int page)
    {
        // print the section
        printSection(g, pf, page);

        // translate the graphics context to the full page height
        double diff = getPageHeight() - getSectionHeight();
        if(diff > 0)
            g.translate(0, (int)diff);

        // return
        return Printable.PAGE_EXISTS;
    }
}
