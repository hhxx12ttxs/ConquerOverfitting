/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import interfaces.AppearAble;
import interfaces.DisappearAble;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 *
 * @author tkarpinski
 */
public class SyberiadaLabel extends JLabel implements AppearAble, DisappearAble {
    private double alpha = 1;
    private SyberiadaLabel label = null;
    private ActionListener fID = null; //fadeInDone
    private ActionListener fOD = null; //fadeOutDone
    private ActionListener cFD = null; //changeForegroundDone
    private ActionListener fBOD = null; //fadeBackgroundOutDone
    private ActionListener fBID = null; //fadeBackgroundOutDone
    private boolean changeForegroundStop = false;
    private boolean changingForegroundStopped = false; //jezeli skonczylo sie samo z sibe to ustaw na true
    private Font f;
    private boolean fadebackgroundOutStop = false;
    private boolean fadebackgroundInStop = false;
    
    SyberiadaLabel(String zamknij, int CENTER) {
        super(zamknij, CENTER);
    }

    SyberiadaLabel(String str) {
        super(str);
    }
    
    public void setFont(String aNazwa, int aSize) {
        if (aNazwa.equalsIgnoreCase("Nuptial")) {
            try {
                f = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(new File("").getAbsolutePath()+"/other/Nuptial.ttf")).deriveFont(Font.PLAIN, aSize);
                super.setFont(f);
            } catch(Exception e) {
                System.out.println(e);
            }
        } else if (aNazwa.equalsIgnoreCase("Neutron")) {
            try {
                f = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(new File("").getAbsolutePath()+"/other/Neuton-Bold.ttf")).deriveFont(Font.PLAIN, aSize);
                super.setFont(f);
                GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
                genv.registerFont(f);
            } catch(Exception e) {
                System.out.println(e);
            }
        }
    }
    
    public void fadeIn(final double alpha, final int howLong) {
        this.label = this;
        final Timer timer = new Timer(20, null);
        this.alpha = 0;
        float ileCykli = howLong/20;
        final double oIle = (float) ((alpha - this.getAlpha())/ileCykli);
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (label.getAlpha() < alpha) {
                    label.setAlpha((float) (label.getAlpha()+oIle));
                    Color c = label.getForeground();
                    int r = c.getRed();
                    int g = c.getGreen();
                    int b = c.getBlue();
                    double a = label.getAlpha();
                    if (label.getAlpha() > 1) label.setAlpha(1);
                    label.setForeground(new Color(r,g,b));
                } else {
                    timer.stop();
                    if (fID != null) fID.actionPerformed(e);
                    fID = null;
                }
            }
        });
        timer.start();
    }
    
    public void fadeIn(final double alpha, final int howLong, int ileCzekac) {
        final Timer pTimer = new Timer(ileCzekac, null);
        pTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fadeIn(alpha, howLong);
                pTimer.stop();
            }
        });
        pTimer.start();
    }
    
    public void whenFadeInDone(ActionListener al) {
        this.fID = al;
    }
    
    public void fadeOut(final double alpha, final int howLong) {
        this.label = this;
        final Timer timer = new Timer(20, null);
//        this.alpha = this.getAlpha();
        float ileCykli = howLong/20;
        final double oIle = (float) ((alpha - this.alpha)/ileCykli);
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (label.getAlpha() > alpha) {
                    label.setAlpha((float) (label.getAlpha()+oIle));
                    Color c = label.getForeground();
                    int r = c.getRed();
                    int g = c.getGreen();
                    if (label.getAlpha() < 0) label.setAlpha(0);
                    int b = c.getBlue();
                    double a = label.getAlpha();
                    label.setForeground(new Color(r,g,b));
                } else {
                    timer.stop();
                    if (fOD != null) fOD.actionPerformed(e);
                    fOD = null;
                }
            }
        });
        timer.start();
    }
    
    public void fadeOut(final double alpha, final int howLong, int ileCzekac) {
        final Timer pTimer = new Timer(ileCzekac, null);
        pTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fadeOut(alpha, howLong);
                pTimer.stop();
            }
        });
        pTimer.start();
    }
    
    public void whenFadeOutDone(ActionListener al) {
        this.fOD = al;
    }
    
    public void changeForegroundColor(final Color color, final int howLong) {
        this.changingForegroundStopped = false;
        this.label = this;
        final Timer timer = new Timer(20, null);
        final float ileCykli = howLong/20;
        final double oIleR = (double)(this.getForeground().getRed()-color.getRed())/ileCykli;
        final double oIleG = (double)(this.getForeground().getGreen()-color.getGreen())/ileCykli;
        final double oIleB = (double)(this.getForeground().getBlue()-color.getBlue())/ileCykli;
        timer.addActionListener(new ActionListener() {
            float cykl = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                double actualR = label.getForeground().getRed();
                double actualG = label.getForeground().getGreen();
                double actualB = label.getForeground().getBlue();
                if (changeForegroundStop == true) {
                    timer.stop();
                    changeForegroundStop = false;
                }
                if (cykl < ileCykli) {
                    cykl+=1;
                    actualR-=oIleR;
                    actualG-=oIleG;
                    actualB-=oIleB;
                    
                    if (actualR>255) actualR=255;
                    if (actualG>255) actualG=255;
                    if (actualB>255) actualB=255;
                    if (actualR<0)actualR=0;
                    if (actualG<0)actualG=0;
                    if (actualB<0)actualB=0;
                    label.setForeground(new Color((int)actualR, (int)actualG, (int)actualB));
                } else {
                    timer.stop();
                    if (cFD != null) cFD.actionPerformed(e);
                    cFD = null;
                }
            }
        });
        timer.start();
    }
    
    public void changeForegroundColor(final Color color, final int howLong, int ileCzekac) {
        final Timer pTimer = new Timer(ileCzekac, null);
        pTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeForegroundColor(color, howLong);
                pTimer.stop();
            }
        });
        pTimer.start();
    }
    
    public void whenChangeForegroundColorDone(ActionListener al) {
        this.cFD = al;
    }
    
    public void changeForegroundColorStop() {
        this.changeForegroundStop = true;
        final Timer pTimer = new Timer(20, null);
        pTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeForegroundStop = false;
                pTimer.stop();
            }
        });
        pTimer.start();
    }
    
    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
    public float getAlpha() {
        return (float)this.alpha;
    }
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)getAlpha()));
        super.paintComponent(g2d);
        SwingUtilities.getWindowAncestor(this).repaint();   
        g2d.dispose();
    }

    @Override
    public void appear(final float doIlu, final int jakiCzas) {
        this.fadeIn(doIlu, jakiCzas);
    }

    @Override
    public void appear(float doIlu, int jakiCzas, int ileCzekac) {
        this.fadeIn(doIlu, jakiCzas, ileCzekac);
    }

    @Override
    public void whenAppearDone(ActionListener al) {
        this.whenFadeInDone(al);
    }

    @Override
    public void disappear(float doIlu, int jakiCzas) {
        this.fadeOut(doIlu, jakiCzas);
    }

    @Override
    public void disappear(float doIlu, int jakiCzas, int ileCzekac) {
        this.fadeOut(doIlu, jakiCzas, ileCzekac);
    }

    @Override
    public void whenDisappearDone(ActionListener al) {
        this.whenFadeOutDone(al);
    }
    
    public void fadeBackgroundIn(final int alpha, final int howLong) {
        this.label = this;
        final Timer timer = new Timer(20, null);
        float ileCykli = howLong/20;
        final double oIle = (float) ((alpha - this.getBackground().getAlpha())/ileCykli);
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (label.fadebackgroundInStop == true) {
                    timer.stop();
                    label.fadebackgroundInStop = false;
                }
                if (label.getBackground().getAlpha() < alpha) {
                    Color c = label.getBackground();
                    int r = c.getRed();
                    int g = c.getGreen();
                    int b = c.getBlue();
                    int a = (int) (label.getBackground().getAlpha()+oIle);
                    if (a > 255) a = 255;
                    label.setBackground(new Color(r,g,b,a));
                } else {
                    timer.stop();
                    if (fBID != null) fBID.actionPerformed(e);
                    fBID = null;
                }
            }
        });
        timer.start();
    }
    
    public void fadeBackgroundIn(final int alpha, final int howLong, int ileCzekac) {
        final Timer pTimer = new Timer(ileCzekac, null);
        pTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fadeBackgroundIn(alpha, howLong);
                pTimer.stop();
            }
        });
        pTimer.start();
    }
    
    public void whenFadeBackgroundInDone(ActionListener al) {
        this.fBID = al;
    }
    
    public void fadeBackgroundOut(final int alpha, final int howLong) {
        this.label = this;
        final Timer timer = new Timer(20, null);
        float ileCykli = howLong/20;
        final double oIle = (float) ((alpha - this.getBackground().getAlpha())/ileCykli);
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (label.fadebackgroundOutStop == true) {
                    timer.stop();
                    label.fadebackgroundOutStop = false;
                }
                if (label.getBackground().getAlpha() > alpha) {
                    Color c = label.getBackground();
                    int r = c.getRed();
                    int g = c.getGreen();
                    int b = c.getBlue();
                    int a = (int) (label.getBackground().getAlpha()+oIle);
                    if (a < 0) a = 0;
                    label.setBackground(new Color(r,g,b,a));
                } else {
                    timer.stop();
                    if (fBOD != null) fBOD.actionPerformed(e);
                    fBOD = null;
                }
            }
        });
        timer.start();
    }
    
    public void fadeBackgroundOut(final int alpha, final int howLong, int ileCzekac) {
        final Timer pTimer = new Timer(ileCzekac, null);
        pTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fadeBackgroundOut(alpha, howLong);
                pTimer.stop();
            }
        });
        pTimer.start();
    }
    
    public void whenFadeBackgroundOutDone(ActionListener al) {
        this.fBOD = al;
    }
    
    public void fadeBackgroundOutStop() {
        this.fadebackgroundOutStop = true;
        final Timer pTimer = new Timer(200, null);
        pTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fadebackgroundOutStop = false;
                pTimer.stop();
            }
        });
        pTimer.start();
    }
    
    public void fadeBackgroundInStop() {
        this.fadebackgroundInStop = true;
        final Timer pTimer = new Timer(200, null);
        pTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fadebackgroundInStop = false;
                pTimer.stop();
            }
        });
        pTimer.start();
    }
    
    public void setBackgroundTransparent() {
        int clrR = this.getBackground().getRed();
        int clrG = this.getBackground().getGreen();
        int clrB = this.getBackground().getBlue();
        int a = 0;
        this.setBackground(new Color(clrR, clrG, clrB, a));
    }
}

