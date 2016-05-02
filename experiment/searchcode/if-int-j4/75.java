// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) fieldsfirst 

package com.aapeli.colorgui;

import com.aapeli.client.IPanel;
import java.awt.*;

public class ProgressBar extends IPanel
{

    public static int TYPE_SOLID = 0;
    public static int TYPE_SLIDE = 1;
    public static int TYPE_BOX = 2;
    public static int TYPE_GLOSSY = 3;
    public static Color f = new Color(0, 0, 0);
    public static Color g = new Color(255, 255, 255);
    public static Color h = new Color(0, 0, 255);
    public Color i;
    public Color j;
    public Color k;
    public int l;
    public int m;
    public double n;
    public Image o;
    public Graphics p;
    public int q;
    public int r;

    public ProgressBar()
    {
        this(0, 1, 0.0D);
    }

    public ProgressBar(int i1)
    {
        this(i1, 1, 0.0D);
    }

    public ProgressBar(int i1, int j1)
    {
        this(i1, j1, 0.0D);
    }

    public ProgressBar(int i1, int j1, double d1)
    {
        setBarType(i1);
        setBorderSize(j1);
        setBorderColor(f);
        setBackgroundColor(g);
        setBarColor(h);
        setProgress(d1);
        q = r = -1;
    }

    public void update(Graphics g1)
    {
        Dimension dimension = getSize();
        int i1 = dimension.width;
        int j1 = dimension.height;
        if(i1 != q || j1 != r)
            o = null;
        if(o == null)
            try
            {
                o = createImage(i1, j1);
                p = o.getGraphics();
            }
            catch(NullPointerException nullpointerexception)
            {
                return;
            }
        p.setColor(j);
        p.fillRect(0, 0, i1, j1);
        int k1 = m;
        a(p, k1, k1, i1 - k1 * 2, j1 - k1 * 2);
        if(k1 > 0)
        {
            p.setColor(i);
            for(int l1 = 0; l1 < k1; l1++)
                p.drawRect(l1, l1, i1 - l1 - 1, j1 - l1 - 1);

        }
        g1.drawImage(o, 0, 0, this);
    }

    public void setBarType(int i1)
    {
        l = i1;
        repaint();
    }

    public void setBorderSize(int i1)
    {
        m = i1;
    }

    public void setBorderColor(Color color)
    {
        i = color;
        repaint();
    }

    public void setBackgroundColor(Color color)
    {
        j = color;
        setBackground(j);
        repaint();
    }

    public void setBarColor(Color color)
    {
        k = color;
        repaint();
    }

    public void setProgress(double d1)
    {
        if(d1 < 0.0001D)
            d1 = 0.0D;
        else
        if(d1 > 0.99990000000000001D)
            d1 = 1.0D;
        n = d1;
        repaint();
    }

    public double getProgress()
    {
        return n;
    }

    public int getProgressPercent()
    {
        int i1 = (int)(n * 100D + 0.5D);
        if(i1 == 0 && n > 0.0D)
            i1 = 1;
        else
        if(i1 == 100 && n < 1.0D)
            i1 = 99;
        return i1;
    }

    public boolean isCompleted()
    {
        return getProgressPercent() == 100;
    }

    public void a(Graphics g1, int i1, int j1, int k1, int l1)
    {
        int i2 = (int)((double)k1 * n + 0.5D);
        if(i2 == 0)
            return;
        if(l == 0)
            b(g1, i1, j1, i2, l1);
        else
        if(l == 1)
            c(g1, i1, j1, i2, l1);
        else
        if(l == 2)
            d(g1, i1, j1, i2, l1);
        else
        if(l == 3)
            e(g1, i1, j1, i2, l1);
    }

    public void b(Graphics g1, int i1, int j1, int k1, int l1)
    {
        g1.setColor(k);
        g1.fillRect(i1, j1, k1, l1);
    }

    public void c(Graphics g1, int i1, int j1, int k1, int l1)
    {
        int i2 = k.getRed();
        int j2 = k.getGreen();
        int k2 = k.getBlue();
        int l2 = (j.getRed() + i2) / 2;
        int i3 = (j.getGreen() + j2) / 2;
        int j3 = (j.getBlue() + k2) / 2;
        int k3 = i2 - l2;
        int l3 = j2 - i3;
        int i4 = k2 - j3;
        double d1 = (1.0D * (double)k3) / (double)k1;
        double d2 = (1.0D * (double)l3) / (double)k1;
        double d3 = (1.0D * (double)i4) / (double)k1;
        for(int j4 = 0; j4 < k1; j4++)
        {
            g1.setColor(new Color((int)((double)l2 + d1 * (double)j4 + 0.5D), (int)((double)i3 + d2 * (double)j4 + 0.5D), (int)((double)j3 + d3 * (double)j4 + 0.5D)));
            g1.drawLine(i1 + j4, j1, i1 + j4, (j1 + l1) - 1);
        }

    }

    public void d(Graphics g1, int i1, int j1, int k1, int l1)
    {
        int i2 = (l1 * 2) / 3;
        int j2 = (k1 + i2 / 2) / i2;
        if(n > 0.0D && j2 == 0)
            j2 = 1;
        if(n == 1.0D && j2 * i2 < k1)
            j2++;
        g1.setColor(k);
        for(int k2 = 0; k2 < j2; k2++)
            g1.fillRect(i1 + k2 * i2 + 1, j1 + 1, i2 - 2, l1 - 2);

    }

    public void e(Graphics g1, int i1, int j1, int k1, int l1)
    {
        int i2 = k.getRed();
        int j2 = k.getGreen();
        int k2 = k.getBlue();
        i2 = (255 + i2) / 2;
        j2 = (255 + j2) / 2;
        k2 = (255 + k2) / 2;
        g1.setColor(k);
        g1.fillRect(i1, j1, k1, l1);
        g1.setColor(new Color(i2, j2, k2));
        java.awt.Shape shape = g1.getClip();
        g1.setClip(i1, j1, k1, l1);
        g1.fillRoundRect(i1, j1 - l1 / 2, k1, l1, 10, 10);
        g1.setClip(shape);
    }

}

