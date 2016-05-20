/*
 * BEGIN_HEADER - DO NOT EDIT
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-esb.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-esb.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)PieProperties.java
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 *
 * END_HEADER - DO NOT EDIT
 */
package org.openesb.tools.extchart.property.pie;



import org.openesb.tools.extpropertysheet.impl.ExtPropertyGroup;
import java.awt.Color;
import java.awt.Font;


/**
 * PieChartDefaultsImpl.java
 *
 * PieChartDefaultsImpl is used to encapsulate all defaults specific only to
 * pie charts.
 *
 * @author Chris Johnston
 * @version :$Revision: 1.4 $
 */

public class PieProperties extends ExtPropertyGroup implements PieConstants {

    /** the RCS id */
    public static final String RCS_ID =
        "$Id: PieProperties.java,v 1.4 2010/07/18 16:18:11 ksorokin Exp $";
    /** DEFAULT_SECTION_LABEL is used as a hard default. */
    public static final int DEFAULT_SECTION_LABEL = 1;//PiePlot.NAME_AND_VALUE_LABELS; //PiePlot.NO_LABELS;

    /** DEFAULT_TOOLTIP_LABEL_FORMAT is used as a hard default. */
    public static final int DEFAULT_TOOLTIP_LABEL_FORMAT = 1;

    /** TOOLTIP_LABEL_FORMATS is used automate the section label processing. */
    //public static final String[] TOOLTIP_LABEL_FORMATS;
    public static final String[] TOOLTIP_LABEL_FORMATS = {
        TOOLTIP_NO_LABELS_STRING,
        TOOLTIP_NAME_LABELS_STRING,
        TOOLTIP_VALUE_LABELS_STRING,
        TOOLTIP_PERCENT_LABELS_STRING,
        TOOLTIP_NAME_AND_VALUE_LABELS_STRING,
        TOOLTIP_NAME_AND_PERCENT_LABELS_STRING,
        TOOLTIP_VALUE_AND_PERCENT_LABELS_STRING,
        TOOLTIP_NAME_VALUE_AND_PERCENT_LABELS_STRING
    };

    /** SECTION_LABELS is used automate the section label processing. */
    //public static final String[] SECTION_LABELS;
    public static final String[] SECTION_LABELS = {
        SECTION_NO_LABELS_STRING,
        SECTION_NAME_LABELS_STRING,
        SECTION_VALUE_LABELS_STRING,
        SECTION_PERCENT_LABELS_STRING,
        SECTION_NAME_AND_VALUE_LABELS_STRING,
        SECTION_NAME_AND_PERCENT_LABELS_STRING,
        SECTION_VALUE_AND_PERCENT_LABELS_STRING,
        SECTION_NAME_VALUE_AND_PERCENT_LABELS_STRING
    };

    Object[] defaultObjects = {
        new Double(DEFAULT_RADIUS),
        new Integer(DEFAULT_SECTION_LABEL),
        new Integer(DEFAULT_TOOLTIP_LABEL_FORMAT)
    };

    /** MY_DEFAULT_KEYS contains the default keys for this class */
    protected static final String[] MY_DEFAULT_KEYS = {
        RADIUS_KEY,
        SECTION_LABEL_TYPE_KEY,
        TOOLTIP_LABEL_FORMAT_KEY,
        INDIVIDUAL_SLICE_LABEL_KEY,
        INDIVIDUAL_SLICE_IS_EXPLODED_KEY,
        INDIVIDUAL_SLICE_EXPLODE_PERCENT_KEY
    };
    
    public static final Font DEFAULT_SECTION_FONT =
        new Font("SansSerif", DEFAULT_ALL_FIELDS_FONT_STYLE, DEFAULT_ALL_FIELDS_FONT_SIZE);
    public static final Font DEFAULT_SERIES_FONT =
        new Font("SansSerif", DEFAULT_ALL_FIELDS_FONT_STYLE, DEFAULT_ALL_FIELDS_FONT_SIZE);

    static final String[] PIE_CHART_KEYS;
    static {
        //String[] superKeys = ChartDefaults.MY_DEFAULT_KEYS;
        String[] myKeys    = new String[ MY_DEFAULT_KEYS.length];
        for (int n = 0; n < myKeys.length; n++) {
            //if (n < superKeys.length) {
             //   myKeys[n] = superKeys[n];
            //} else {
                myKeys[n] = MY_DEFAULT_KEYS[n ];
            //}
        }
        PIE_CHART_KEYS = myKeys;
    }

    /**
     * @see com.stc.ebam.server.chart.engine.view.ViewDefaults.getKeys()
     */
    public String[] getKeys() {
        return PIE_CHART_KEYS;
    }
    /**
     * New instance
     */
    public PieProperties( ) {
        super();
       
        initDefaults();
        
    }
     
    /**
     * Init
     */
    public void initDefaults()  {
        setSectionLabel(DEFAULT_SECTION_LABEL);
        setTooltipLabelFormat(DEFAULT_TOOLTIP_LABEL_FORMAT);
        setRadius(DEFAULT_RADIUS);
        setSectionLabelFont(DEFAULT_SECTION_FONT);
        setSeriesLabelFont(DEFAULT_SERIES_FONT);
        // setPieChartDefaults
    }


    /**
     * @see com.stc.ebam.server.chart.engine.view.tabular.TabularProperties.getFieldWidth()
     */
    /*
    public String getSliceLabel(String fieldName) {
        FieldProperties fieldProp = this.getFieldPropertyForColumn(fieldName);
        if (fieldProp != null) {
            String label = (String) fieldProp.getFieldPropertyMap().getPropertyValue(INDIVIDUAL_SLICE_LABEL_KEY);
            return label;
        }

        return fieldName;
    }
*/
    /**
     * @see com.stc.ebam.server.chart.engine.view.tabular.TabularProperties.getFieldWidth()
     */
    /*
    public boolean isSliceExploded(String fieldName) {
        FieldProperties fieldProp = this.getFieldPropertyForColumn(fieldName);
        if (fieldProp != null) {
            Boolean exploded = (Boolean) fieldProp.getFieldPropertyMap().getPropertyValue(
                INDIVIDUAL_SLICE_IS_EXPLODED_KEY);
            return exploded.booleanValue();
        }

        return false;
    }
*/
    /**
     * @see com.stc.ebam.server.chart.engine.view.tabular.TabularProperties.getFieldWidth()
     */
    /*
    public int getSliceExplodedPercent(String fieldName) {
        FieldProperties fieldProp = this.getFieldPropertyForColumn(fieldName);
        if (fieldProp != null) {
            Integer explodedPercent = (Integer) fieldProp.getFieldPropertyMap().getPropertyValue(
                INDIVIDUAL_SLICE_EXPLODE_PERCENT_KEY);
            return explodedPercent.intValue();
        }

        return DEFAULT_ALL_EXPLODED_PERCENT;
    }
*/
    /**
     * @see com.stc.ebam.server.chart.engine.view.chart.pie.PieChartDefaults.getRadius()
     */
    public double getRadius() {
        Double val = (Double) this.getPropertyValue(RADIUS_KEY);
        if (val != null) {
            return val.doubleValue();
        }

        //return default
        return DEFAULT_RADIUS;

    }

    /**
     * @see com.stc.ebam.server.chart.engine.view.chart.pie.PieChartDefaults.setRadius()
     */
    public void setRadius(double newRadius)  {
        
        setProperty(RADIUS_KEY, new Double(newRadius));
    }

    /**
     * @see com.stc.ebam.server.chart.engine.view.chart.pie.PieChartDefaults.setSectionLabel()
     */
    public void setTooltipLabelFormat(int nTooltipLabelFormat) {
        if ((nTooltipLabelFormat >= 0) && (nTooltipLabelFormat < TOOLTIP_LABEL_FORMATS.length)) {
            setProperty(TOOLTIP_LABEL_FORMAT_KEY, new Integer(nTooltipLabelFormat));
        }
    }

    /**
     * @see com.stc.ebam.server.chart.engine.view.chart.pie.PieChartDefaults.getSectionLabel()
     */
    public int getTooltipLabelFormat() {
        Integer val = (Integer) this.getPropertyValue(TOOLTIP_LABEL_FORMAT_KEY);
        if (val != null) {
            return val.intValue();
        }
        return DEFAULT_TOOLTIP_LABEL_FORMAT;
    }

    /**
     * @see com.stc.ebam.server.chart.engine.view.chart.pie.PieChartDefaults.setSectionLabel()
     */
    public void setSectionLabel(int nSectionLabelType) {
        if ((nSectionLabelType >= 0) && (nSectionLabelType < SECTION_LABELS.length)) {
            setProperty(SECTION_LABEL_TYPE_KEY, new Integer(nSectionLabelType));
        }
    }

    /**
     * @see com.stc.ebam.server.chart.engine.view.chart.pie.PieChartDefaults.getSectionLabel()
     */
    public int getSectionLabel() {
        Integer val = (Integer) this.getPropertyValue(SECTION_LABEL_TYPE_KEY);
		if(val.intValue() == 0) {
			setSectionLabel(DEFAULT_SECTION_LABEL);
			return DEFAULT_SECTION_LABEL;
		}
        if (val != null) {
            return val.intValue();
        }
        return DEFAULT_SECTION_LABEL;
    }
    /**
     * Returns section label font
     * @return section label font
     */
    public Font getSectionLabelFont() {
        Font f =(Font) getPropertyValue(SECTION_LABEL_FONT_KEY);
        if( f== null){
            f = DEFAULT_SECTION_FONT;
        }
        return f;
    }
    /**
     * Sets section label font
     * @param font - section label font
     */
    public void setSectionLabelFont(Font font) {
        setProperty(SECTION_LABEL_FONT_KEY,font);
    }
    /**
     * Returns section label gap
     * @return gap
     */
    public double getSectionLabelGap() {
        Double val = (Double) this.getPropertyValue(SECTION_LABEL_GAP_KEY);
        if (val != null) {
            return val.doubleValue();
        }
        return 0.3;
    }
    /**
     * Returns if show series labels
     * @return true/false
     */
    public boolean isShowSectionLabels() {
        Boolean val = (Boolean) this.getPropertyValue(SHOW_SECTION_LABELS_KEY);
        if (val != null) {
            return val.booleanValue();
        }
        return false;
    }
    /**
     * Sets show series labels
     * @param show - true/false
     */
    public void setShowSectionLabels(boolean show) {
        this.setProperty(SHOW_SECTION_LABELS_KEY,  new Boolean(show));
    }
    /**
     * Returns series label font
     * @return font
     */
    public Font getSeriesLabelFont() {
        Font val = (Font) this.getPropertyValue(SERIES_LABEL_FONT_KEY);
        if (val == null) {
            val = DEFAULT_SERIES_FONT;
        }
        return val;
      
    }
    /**
     * Sets series label font
     * @param font - font
     */
    public void setSeriesLabelFont(Font font) {
        setProperty(SERIES_LABEL_FONT_KEY,font);
    }
    /**
     * Returns series label paint
     * @return series label paint
     */
    public Color getSeriesLabelPaint() {
        Color val = (Color) this.getPropertyValue(SERIES_LABEL_PAINT_KEY);
        return val;
    }
    /**
     * Sets series label paint
     * @param paint - paint
     */
    public void setSeriesLabelPaint(Color paint) {
        if (paint != null) {
            setProperty(SERIES_LABEL_PAINT_KEY, paint);
        }
    }
    /**
     * Sets section label gap
     * @param gap - gap
     */
    public void setSectionLabelGap(double gap)  {
        
        this.setProperty(SECTION_LABEL_GAP_KEY, new Double(gap));
    }
    /**
     * Returns section label paint
     * @return section label paint
     */
    public Color getSectionLabelPaint() {
        Color val = (Color) this.getPropertyValue(SECTION_LABEL_PAINT_KEY);
        return val;
    }
    /**
     * Sets section label paint
     * @param paint - section label paint
     */
    public void setSectionLabelPaint(Color paint) {
        if (paint != null) {
            setProperty(SECTION_LABEL_PAINT_KEY, paint);
        }
    }
    
    
    
  
    /**
     * toString
     * @return - string
     */
    public String toString() {
        return "Pie Chart";
    }
    /**
     * Return property template name
     * @return - template name
     */
    public String getPropertyTemplateName() {
        return "PieChart";
    }
    

}

