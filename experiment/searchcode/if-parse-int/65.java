import java.awt.event.*;
import java.awt.Image;
import java.util.Scanner;
import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.text.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.Font;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Color;
import java.io.*;
import javax.imageio.*;
import java.net.URL;

class Logo extends JPanel
{
    private List<Image> sheldon = new ArrayList<Image>();
    private Image icon;
    private Image icon2;
    private Image icon3 = null;
    private Image[] shelly;
    private static int i = 0;
    private static int contor = 0;
    private static int c = 0;
    private static int p = 0;
    public static String error = null;
    private RenderingHints renderingHints = new RenderingHints(
    RenderingHints.KEY_ANTIALIASING , RenderingHints.
    VALUE_ANTIALIAS_ON);
    private Timer timer;
    private boolean ok = false;


    public Logo()
    {
        this.setBackground(Color.CYAN.brighter());
        animate();
        // timer = new Timer();
        // timer.schedule(new RemindTask(), 100);
    }



    public void animate()
    {
        // try
        // {
        //      Thread.sleep(100);
        // }
        // catch (Exception e)
        // {}
        if(Inter.errorMes == null && Parse.error == null)
        {
            Logo.error = "Game loaded without any error!";
        }
        else
        {
            if(Inter.errorMes == null)
            {
                Logo.error = Parse.error;
            }
            else
                if(Parse.error == null)
                {
                    Logo.error = Inter.errorMes;
                }
                else
                {
                    Logo.error = Inter.errorMes + Parse.error;
                }
        }
        ok = true;
        repaint();
    }

    public void paintOneImg(Graphics g, int i)
    {
        try
        {
            if(Quiz.os.equals("Linux"))
            {
                icon2 = new ImageIcon(this.getClass().getResource("/sheldon/"+Integer.toString(i) + ".png")).getImage();
            }
            else
            {
                icon2 = new ImageIcon(this.getClass().getResource("\\sheldon\\"+Integer.toString(i) + ".png")).getImage();
            }
        }
        catch(Exception e)
        {
        }
        g.drawImage(icon2, 0, 0, 150, 80, null);

        this.revalidate();
    }

    public void drawFlag(Graphics2D g)
    {
        p++;
        
        try
        {
            if(Quiz.os.equals("Linux"))
            {
                icon = new ImageIcon(this.getClass().getResource(Parse.flags[contor])).getImage();
            }
            else
            {
                Parse.flags[contor] = Parse.flags[contor].replace(Quiz.home,"");
                icon = new ImageIcon(this.getClass().getResource(Parse.flags[contor])).getImage();
            }
        }

        catch(Exception e)
        {
        }
        g.setColor(Color.CYAN.darker());
        g.fillRect(0, 125, 400, 200);
        int len = p % 20;
        if(len > 20)
            len = 0;
        if(p % 4 == 0)
        {
            g.drawImage(icon, len*15, 125, 50 , 25 ,null);
        }
        if(p % 4 == 1)
        {
            g.drawImage(icon, len*15, 125, 70 , 40 , null);
        }
        else
        {
            g.drawImage(icon, len*15, 125, 80 , 50 , null);
        }
        contor++;
        if(contor >= Parse.flags.length)
            contor = 0;
    }

    public void drawLogo(Graphics2D g)
    {
        try
        {
            icon3 = new ImageIcon(this.getClass().getResource("sheldon.png")).getImage();
        } catch (Exception e)
        {
            Logo.error = "The logo photo was not found! Please reinstall";
        }
        if(icon3 != null)
        {
            g.drawImage(icon3, 151, 0, 140, 80, null);
        }
    }

    public void paintComponent(Graphics g)
    {
        c += 1;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHints(renderingHints);
        boolean ok = true;
        paintOneImg(g2d,i/1000);
        if(i >= 17000)
        {
            i = 0;
        }
        else
        {
            if(i > 10000)
            {
                i += 5;
            }
            else
            {
                i+= 3;
            }
        }
        g2d.setFont(new Font("Tahoma", Font.BOLD, 15));

        if( c % 1000 == 0)
        {
            drawFlag(g2d);
        }

        if(i % 1000 == 0)
        {
            // g.setColor(Color.CYAN.darker());
            // g.fillRect(0, 100, 20, 100);
            g2d.drawString(Logo.error, 75, 120);

            ok = false;
        }

        drawLogo(g2d);
        animate();
    }

    public static void main(String args[])
    {
        Logo logo = new Logo();
    }
}

