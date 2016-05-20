
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
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import wsl.fw.util.Util;
import wsl.fw.util.Log;
import wsl.fw.datasource.DataObject;
import wsl.fw.datasource.Row;


/**
 * Section encapsulating
 */
public class WslQuerySection extends WslReportSection
{
    //--------------------------------------------------------------------------
    // attributes

    private Vector _rows;


    //--------------------------------------------------------------------------
    // init

    /**
     * Blank ctor
     */
    public WslQuerySection()
    {
    }

    /**
     * Set a single row into the section
     * @param row a DataObject or Row.
     * If the vector is of DataObjects each DataObject is treated as a Row
     */
    public void setRow(DataObject row)
    {
        _rows = new Vector();
        _rows.add(row);
    }

    /**
     * Set rows into the section
     * @param rows a Vector of Rows or DataObjects.
     * If the vector is of DataObjects each DataObject is treated as a Row
     */
    public void setRows(Vector rows)
    {
        _rows = rows;
    }

    /**
     * @return int the number of pages that this section requires
     * @param the height of a page
     */
    public int getNumPages(double pageHeight)
    {
        int numRows = (_rows == null)? 0: _rows.size();
        double totalHeight = numRows * getSectionHeight();
        return (int)Math.ceil(totalHeight / pageHeight);
    }


    //--------------------------------------------------------------------------
    // printing

    /**
     * Print a page of the section
     * @param g the Graphics context
     * @param pf the PageFormat
     * @param page the page to print
     * @return int Printable.PAGE_EXISTS if valid page, else Printable.NO_SUCH_PAGE
     */
    public int printPage(Graphics2D g, PageFormat pf, int page)
    {
        // if no data, return super
        int numRows = (_rows == null)? 0: _rows.size();
        if(numRows == 0)
            return super.printPage(g, pf, page);

        // get the start and end index
        int startIndex = 0;
        int endIndex = numRows - 1;
        double pageHeight = getPageHeight();
        double sectionHeight = getSectionHeight();
        if(doPrintOnce())
        {
            // if print once then only one iteration
            endIndex = 0;
        }

        // else calculate indices based on page
        else if(pageHeight != CALCULATED_HEIGHT)
        {
            int rowsPerPage = (int)(pageHeight / sectionHeight);
            startIndex = page * rowsPerPage;
            endIndex = (startIndex + rowsPerPage) - 1;
            if(endIndex > (numRows - 1))
                endIndex = numRows - 1;
        }

        // iterate the rows
        DataObject dobj;
        int printHeight = 0;
        for(int i = startIndex; i <= endIndex; i++)
        {
            // get the row
            dobj = (DataObject)_rows.elementAt(i);
            if(dobj != null)
            {
                // set values for the row
                setFieldValues(dobj);

                // print the section
                printSection(g, pf, page);
                printHeight += sectionHeight;
            }
        }

        // translate the graphics context to the full page height
        double diff = pageHeight - printHeight;
        if(diff > 0)
            g.translate(0, (int)diff);

        // return
        return (startIndex <= endIndex)? Printable.PAGE_EXISTS: Printable.NO_SUCH_PAGE;
    }

    /**
     * Set values into the fields for the row
     * @param row the Row or DataObject containing data
     */
    private void setFieldValues(DataObject row)
    {
        // iterate the print elements
        PrintElement pe;
        DataFieldElement dfe;
        DataObject dobj;
        for(int i = 0; i < getPrintElements().size(); i++)
        {
            // get the pe and check class
            pe = (PrintElement)getPrintElements().elementAt(i);
            if(pe != null && pe instanceof DataFieldElement)
            {
                // get the corresponding DataObject
                dfe = (DataFieldElement)pe;
                dobj = getDataObject(row, dfe.getEntityName());

                // set the value
                if(dobj != null)
                {
                    String value = dobj.getStringValue(dfe.getFieldName());
                    if(value != null)
                        dfe.setText(value);
                }
            }
        }
    }

    /**
     * @return the DataObject corresponding to the Row param and entity name
     * @param row the DataObject or Row
     * @param entityName the name of the entity to find
     * @return DataObject the DataObject in the Row that has the same entity name
     */
    private DataObject getDataObject(DataObject row, String entityName)
    {
        // param is a row
        if(row instanceof Row)
        {
            // iterate the Row
            DataObject dobj;
            Row r = (Row)row;
            for(int i = 0; i < r.getComponents().size(); i++)
            {
                // get the dobj and compare entity names
                dobj = (DataObject)r.getComponents().elementAt(i);
                if(dobj != null && dobj.getEntityName().equals(entityName))
                    return dobj;
            }

            // not found, return null
            return null;
        }

        // param is a single object
        else
        {
            // if the DataObject is not the same class return null;
            return row.getEntityName().equals(entityName)? row: null;
        }
    }
}
