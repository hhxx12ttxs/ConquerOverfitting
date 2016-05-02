// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) fieldsfirst 

package com.aapeli.applet;

import com.aapeli.client.Parameters;
import com.aapeli.client.TextManager;
import com.aapeli.colorgui.RoundButton;
import com.aapeli.tools.Tools;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Referenced classes of package com.aapeli.applet:
//            AApplet, a

public class LoaderPanel extends Panel
    implements Runnable, ActionListener
{

    public static Font b = new Font("Dialog", 0, 14);
    public static Font c = new Font("Dialog", 1, 20);
    public AApplet g;
    public Parameters h;
    public TextManager i;
    public String j;
    public double k;
    public double l;
    public double m;
    public int n;
    public boolean o;
    public boolean p;
    public boolean q;
    public boolean r;
    public Image s;
    public Graphics t;
    public AdCanvas u;
    public boolean v;
    public RoundButton w;
    public RoundButton x;
    public int y;

    public LoaderPanel(AApplet aapplet)
    {
        g = aapplet;
        j = null;
        k = l = 0.0D;
        m = 0.0018D;
        n = 50;
        q = false;
        r = false;
        o = true;
        p = true;
        y = -1;
    }

    public void paint(Graphics g1)
    {
        update(g1);
    }

    public synchronized void update(Graphics g1)
    {
        if(r)
            return;
        AApplet aapplet = g;
        if(aapplet == null)
            return;
        int i1 = aapplet.appletWidth;
        int j1 = aapplet.appletHeight;
        if(s == null)
        {
            s = createImage(i1, j1);
            t = s.getGraphics();
            o = true;
        }
        Color color = getBackground();
        if(color.equals(Color.black))
            color = new Color(24, 24, 24);
        boolean flag = o;
        o = false;
        if(flag)
        {
            a(t, color, 0, 32, 0, j1, 0, i1, p);
            p = false;
            if(j != null && y == -1)
            {
                t.setColor(getForeground());
                a(t, b, j);
            }
            if(u != null)
                u.repaint();
        }
        if(y == -1)
        {
            a(t, Color.white, 0, 48, 25, 40, 5, i1 - 5, true);
            int k1 = (int)((double)(i1 - 10) * l);
            if(k1 > 0)
                a(t, Color.green, 144, 144, 25, 40, 5, 5 + k1, true);
            t.setColor(Color.black);
            t.drawRect(5, 25, i1 - 10 - 1, 14);
        }
        g1.drawImage(s, 0, 0, this);
    }

    public void setBackground(Color color)
    {
        super.setBackground(color);
        o = true;
        repaint();
    }

    public void run()
    {
        do
        {
            try
            {
                Thread.sleep(n);
            }
            catch(InterruptedException interruptedexception) { }
            if(r)
                return;
            boolean flag = false;
            if(l < k)
            {
                l += g();
                if(l > 1.0D)
                    l = 1.0D;
                flag = true;
            }
            if(k >= 1.0D && g.isDebug())
            {
                l = 1.0D;
                flag = true;
            }
            if(flag)
                repaint();
        } while(l < 1.0D);
        q = true;
    }

    public void actionPerformed(ActionEvent actionevent)
    {
        if(actionevent.getSource() == w)
        {
            y = 1;
        } else
        {
            g.setEndState(8);
            h.showCreditPurchasePage(false);
        }
    }

    public void a(Parameters parameters, TextManager textmanager)
    {
        h = parameters;
        i = textmanager;
    }

    public void a()
    {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void a(String s1)
    {
        j = s1;
        o = true;
        repaint();
    }

    public void a(double d1)
    {
        k += d1;
    }

    public void a(AdCanvas a1, boolean flag)
    {
        setLayout(null);
        int i1 = g.appletWidth - 5 - 5;
        int j1 = g.appletHeight - 5 - 5 - 40;
        Dimension dimension = a1.getSize();
        a1.setLocation((5 + i1 / 2) - dimension.width / 2, (45 + j1 / 2) - dimension.height / 2);
        add(a1);
        a1.setLoaderPanel(this);
        u = a1;
        v = flag;
    }

    public void b(double d1)
    {
        k = d1;
    }

    public void c(double d1)
    {
        m *= d1;
    }

    public Image b()
    {
        return s;
    }

    public void c()
    {
        n = 25;
    }

    public boolean d()
    {
        return q;
    }

    public void e()
    {
        if(u == null)
            return;
        if(!v)
            return;
        y = 0;
        o = true;
        repaint();
        char c1 = '\u012C';
        int i1 = (g.appletWidth - 25 - 15 - 15 - 25) / 2;
        int j1 = Math.min(c1, i1);
        w = new RoundButton(i.getShared("Loader_Button_StartGame"));
        w.setBounds(g.appletWidth / 2 + 15, 10, j1, 35);
        w.setBackground(new Color(96, 224, 96));
        w.setForeground(Color.black);
        w.setFont(c);
        w.addActionListener(this);
        add(w);
        if(h.isCreditPurchasePageAvailable())
        {
            x = new RoundButton(i.getShared("Loader_Button_MorePaymentOptions"));
            x.setBounds(g.appletWidth / 2 - 15 - j1, 10, j1, 35);
            x.setBackground(new Color(96, 96, 255));
            x.setForeground(Color.black);
            x.setFont(c);
            x.addActionListener(this);
            add(x);
        }
        do
            Tools.sleep(25L);
        while(y == 0 && !r);
        remove(w);
    }

    public synchronized void f()
    {
        r = true;
        if(u != null)
        {
            remove(u);
            u.destroy();
            u = null;
        }
        j = null;
        if(t != null)
        {
            t.dispose();
            t = null;
        }
        if(s != null)
        {
            s.flush();
            s = null;
        }
        g = null;
    }

    public void a(Graphics g1, Color color, int i1, int j1, int k1, int l1, int i2, 
            int j2, boolean flag)
    {
        int k2 = color.getRed();
        int l2 = color.getGreen();
        int i3 = color.getBlue();
        int j3 = k2 + i1;
        int k3 = l2 + i1;
        int l3 = i3 + i1;
        int i4 = k2 - j1;
        int j4 = l2 - j1;
        int k4 = i3 - j1;
        if(j3 > 255)
            j3 = 255;
        if(k3 > 255)
            k3 = 255;
        if(l3 > 255)
            l3 = 255;
        if(i4 < 0)
            i4 = 0;
        if(j4 < 0)
            j4 = 0;
        if(k4 < 0)
            k4 = 0;
        if(flag)
            a(g1, k1, l1, i2, j2, j3, i4, k3, j4, l3, k4);
        else
            b(g1, k1, l1, i2, j2, j3, i4, k3, j4, l3, k4);
    }

    public void a(Graphics g1, int i1, int j1, int k1, int l1, int i2, int j2, 
            int k2, int l2, int i3, int j3)
    {
        for(int j4 = i1; j4 < j1; j4++)
        {
            double d1 = (1.0D * (double)(j4 - i1)) / (double)(j1 - i1);
            int k3 = (int)((double)i2 + (double)(j2 - i2) * d1);
            int l3 = (int)((double)k2 + (double)(l2 - k2) * d1);
            int i4 = (int)((double)i3 + (double)(j3 - i3) * d1);
            g1.setColor(new Color(k3, l3, i4));
            g1.drawLine(k1, j4, l1 - 1, j4);
        }

    }

    public void b(Graphics g1, int i1, int j1, int k1, int l1, int i2, int j2, 
            int k2, int l2, int i3, int j3)
    {
        int k4 = -1;
        for(int l4 = i1; l4 < j1; l4++)
        {
            double d1 = (1.0D * (double)(l4 - i1)) / (double)(j1 - i1);
            for(int i5 = k1; i5 < l1; i5++)
            {
                double d2;
                if(i5 == k1)
                    d2 = 0.0D;
                else
                    d2 = Math.random() * 1.98D - 0.98999999999999999D;
                int k3 = (int)((double)i2 + (double)(j2 - i2) * d1 + d2);
                int l3 = (int)((double)k2 + (double)(l2 - k2) * d1 + d2);
                int i4 = (int)((double)i3 + (double)(j3 - i3) * d1 + d2);
                int j4 = k3 * 256 * 256 + l3 * 256 + i4;
                if(i5 == k1)
                {
                    k4 = j4;
                    g1.setColor(new Color(k4));
                    g1.drawLine(k1, l4, l1, l4);
                    continue;
                }
                if(j4 != k4)
                {
                    g1.setColor(new Color(j4));
                    g1.fillRect(i5, l4, 1, 1);
                }
            }

        }

    }

    public void a(Graphics g1, Font font, String s1)
    {
        for(; getFontMetrics(font).stringWidth(s1) > g.appletWidth - 12; font = new Font(font.getName(), font.getStyle(), font.getSize() - 1));
        g1.setFont(font);
        g1.drawString(j, 6, 19);
    }

    public double g()
    {
        if(u == null)
            return m;
        int i1 = u.c();
        if(i1 <= 0)
            return m;
        double d1 = 1.0D - l;
        double d2 = (d1 * (double)n) / (double)i1;
        if(d2 > m)
            return m;
        else
            return d2;
    }

}

