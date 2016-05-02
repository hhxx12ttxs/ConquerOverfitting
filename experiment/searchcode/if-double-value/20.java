package com.jeasonzhao.commons.chart.model;

import com.jeasonzhao.commons.utils.WebColor;

public class Point implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;
    private static final double NULLVALUE = Double.MIN_NORMAL;
    private double value = NULLVALUE;
    private double y = NULLVALUE;
    private double z = NULLVALUE;

    private String tooltip = null;
    private String label = null;
    private WebColor color = null;
    public Point()
    {
        this(null,0,0,0,null);
    }

    public Point(double lfValue)
    {
        this(null,lfValue,0,0,null);
    }

    public Point(double lfValue,double yValue)
    {
        this(null,lfValue,yValue,0,null);
    }

    public Point(double lfValue,double yValue,double zValue)
    {
        this(null,lfValue,yValue,zValue,null);
    }

    public Point(double lfValue,String strTooltip)
    {
        this(null,lfValue,0,0,strTooltip);
    }

    public Point(double lfValue,double yValue,String strTooltip)
    {
        this(null,lfValue,yValue,0,strTooltip);
    }

    public Point(String strCategory,double lfValue)
    {
        this(strCategory,lfValue,0,0,null);
    }

    public Point(String strCategory,double lfValue,double yValue)
    {
        this(strCategory,lfValue,yValue,0,null);
    }

    public Point(String strCategory,double lfValue,double yValue,double zValue)
    {
        this(strCategory,lfValue,yValue,zValue,null);
    }

    public Point(String strCategory,double lfValue,String strTooltip)
    {
        this(strCategory,lfValue,0,0,strTooltip);
    }

    public Point(String strCategory,double lfValue,double yValue,String strTooltip)
    {
        this(strCategory,lfValue,yValue,0,strTooltip);
    }

    public Point(String strCategory,double lfValue,double yValue,double zValue,String strTooltip)
    {
        this.label = strCategory;
        this.value = lfValue;
        this.y = yValue;
        this.z = zValue;
        this.tooltip = strTooltip;
    }

    public double getValue()
    {
        return value;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }

    public String getTooltip()
    {
        return tooltip;
    }

    public String getCategory()
    {
        return label;
    }

    public WebColor getColor()
    {
        return color;
    }

    public String getLabel()
    {
        return label;
    }

    public Point setZ(double z)
    {
        this.z = z;
        return this;
    }

    public Point setY(double y)
    {
        this.y = y;
        return this;
    }

    public Point setValue(double value)
    {
        this.value = value;
        return this;
    }

    public Point setTooltip(String tooltip)
    {
        this.tooltip = tooltip;
        return this;
    }

    public Point setCategory(String category)
    {
        this.label = category;
        return this;
    }

    public Point setColor(WebColor color)
    {
        this.color = color;
        return this;
    }

    public Point setLabel(String label)
    {
        this.label = label;
        return this;
    }

    public Point alignToValue()
    {
        if(this.value == NULLVALUE)
        {
            return this;
        }
        else
        {
            this.value = this.y == NULLVALUE ? this.z:this.y;
            return this;
        }
    }
}

