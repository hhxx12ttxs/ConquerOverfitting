package com.jeasonzhao.report.vml.base;

import com.jeasonzhao.report.vml.enums.Positions;
import com.jeasonzhao.report.vml.exception.VMLException;


public class Container extends VmlElement
{
    private int m_css_zIndex = -1;
    private String m_css_width = null;
    private String m_css_height = null;
    private int m_css_top = 0;
    private int m_css_left = 0;
    private Positions m_css_position = Positions.None;
    private Vector2D m_attr_CoordSize = null;
    private Vector2D m_attr_CoordOrigin = null;

    public Container()
    {
        super();
    }

    public void setByCenter(int x,int y,int nW,int nH)
    {
        this.setLeft(x - nW / 2);
        this.setTop(y - nH / 2);
        this.setWidth("" + nW);
        this.setHeight("" + nH);
    }

    public void setByCenter(Vector2D tpt,int nW,int nH)
    {
        setByCenter(tpt.getX(),tpt.getY(),nW,nH);
    }

    public void setByCenter(Vector2D tpt,Vector2D size)
    {
        setByCenter(tpt.getX(),tpt.getY(),size.getWidth(),size.getHeight());
    }

    protected boolean prepareHTML()
        throws VMLException
    {
        if(null == m_css_height)
        {
            m_css_width = "100%";
        }
        if(null == m_css_height)
        {
            m_css_height = "100%";
        }
        if(m_css_position.equals(Positions.None))
        {
            m_css_position = Positions.Relative;
        }
        return super.prepareHTML();
    }

    protected String wrapHtmlAttrName(String str)
    {
        if(null != str && str.equalsIgnoreCase("zIndex"))
        {
            return "z-index";
        }
        return super.wrapHtmlAttrName(str);
    }

    public String getHeight()
    {
        return m_css_height;
    }

    public int getLeft()
    {
        return m_css_left;
    }

    public Positions getPosition()
    {
        return m_css_position;
    }

    public int getTop()
    {
        return m_css_top;
    }

    public String getWidth()
    {
        return m_css_width;
    }

    public int getZIndex()
    {
        return m_css_zIndex;
    }

    public void setWidth(String width)
    {
        this.m_css_width = width;
    }

    public void setTop(int top)
    {
        this.m_css_top = top;
    }

    public void setPosition(Positions position)
    {
        this.m_css_position = position;
    }

    public void setLeft(int left)
    {
        this.m_css_left = left;
    }

    public void setHeight(String height)
    {
        this.m_css_height = height;
    }

    public void setZIndex(int ZIndex)
    {
        this.m_css_zIndex = ZIndex;
    }

    public Vector2D getCoordOrigin()
    {
        return m_attr_CoordOrigin;
    }

    public Vector2D getCoordSize()
    {
        return m_attr_CoordSize;
    }

    public void setCoordSize(int w,int h)
    {
        this.setCoordSize(new Vector2D(w,h));
    }

    public void setCoordSize(Vector2D coordSize)
    {
        this.m_attr_CoordSize = coordSize;
    }

    public void setCoordOrigin(Vector2D coordOrigin)
    {
        this.m_attr_CoordOrigin = coordOrigin;
    }
}

