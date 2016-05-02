// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 2012/6/25 ?? 06:04:28
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   FE11.java

package fe.s100502010;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.PrintStream;
import javax.swing.*;

public class FE11 extends JApplet
{
    private class MyAction extends AbstractAction
    {

        public void actionPerformed(ActionEvent e)
        {
            if(animal == "dog")
            {
                img.setIcon(FE11.all[0]);
                choose = 0;
            } else
            if(animal == "cat")
            {
                img.setIcon(FE11.all[3]);
                choose = 3;
            } else
            if(animal == "mouse")
            {
                img.setIcon(FE11.all[6]);
                choose = 6;
            }
            if(e.getSource() == pre)
            {
                System.out.println("pre");
                if(animal == "dog")
                {
                    if(choose != 0)
                    {
                        choose--;
                        img.setIcon(FE11.all[choose]);
                    }
                } else
                if(animal == "cat")
                {
                    if(choose != 3)
                    {
                        choose--;
                        img.setIcon(FE11.all[choose]);
                    }
                } else
                if(animal == "mouset" && choose != 6)
                {
                    choose--;
                    img.setIcon(FE11.all[choose]);
                }
            } else
            if(e.getSource() == next)
                if(animal == "dog")
                {
                    if(choose != 2)
                    {
                        choose++;
                        img.setIcon(FE11.all[choose]);
                    }
                } else
                if(animal == "cat")
                {
                    if(choose != 5)
                    {
                        choose++;
                        img.setIcon(FE11.all[choose]);
                    }
                } else
                if(animal == "mouset" && choose != 8)
                {
                    choose++;
                    img.setIcon(FE11.all[choose]);
                }
        }

        String animal;
        String temp;
        int tempint;
        final FE11 this$0;

        MyAction(String animal, Icon pic)
        {
        	super(animal, pic);
        	this$0 = FE11.this;
            
            this.animal = "";
            temp = "";
            tempint = 0;
            temp = this.animal;
            putValue("ShortDescription", (new StringBuilder(" Select the ")).append(animal).append("picg to display").toString());
            this.animal = animal;
        }
    }


    public FE11()
    {
        dogicon = new ImageIcon(getClass().getResource("image/dog_icon.jpg"));
        caticon = new ImageIcon(getClass().getResource("image/cat_icon.jpg"));
        mouseicon = new ImageIcon(getClass().getResource("image/mouse_icon.jpg"));
        d1 = new ImageIcon(getClass().getResource("image/dog1.jpg"));
        d2 = new ImageIcon(getClass().getResource("image/dog2.jpg"));
        d3 = new ImageIcon(getClass().getResource("image/dog3.jpg"));
        c1 = new ImageIcon(getClass().getResource("image/cat1.jpg"));
        c2 = new ImageIcon(getClass().getResource("image/cat2.jpg"));
        c3 = new ImageIcon(getClass().getResource("image/cat3.jpg"));
        m1 = new ImageIcon(getClass().getResource("image/mouse1.jpg"));
        m2 = new ImageIcon(getClass().getResource("image/mouse2.jpg"));
        m3 = new ImageIcon(getClass().getResource("image/mouse3.jpg"));
        img = new JLabel();
        pre = new JButton("\u2190");
        next = new JButton("\u2192");
        choose = 0;
        all[0] = d1;
        all[1] = d2;
        all[2] = d3;
        all[3] = c1;
        all[4] = c2;
        all[5] = c3;
        all[6] = m1;
        all[7] = m2;
        all[8] = m3;
        javax.swing.Action dog = new MyAction("dog", dogicon);
        javax.swing.Action cat = new MyAction("cat", caticon);
        javax.swing.Action mouse = new MyAction("mouse", mouseicon);
        javax.swing.Action prev = new MyAction("pre", dogicon);
        javax.swing.Action next1 = new MyAction("next", dogicon);
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("ANIMAL");
        setJMenuBar(menuBar);
        menuBar.add(menu);
        menu.add(dog);
        menu.add(cat);
        menu.add(mouse);
        JToolBar toolBar = new JToolBar(1);
        toolBar.setBorder(BorderFactory.createLineBorder(Color.red));
        toolBar.add(dog);
        toolBar.add(cat);
        toolBar.add(mouse);
        JPanel p1 = new JPanel();
        p1.setLayout(new GridLayout(1, 2, 10, 10));
        p1.add(pre);
        p1.add(next);
        add(toolBar, "East");
        add(img, "Center");
        add(p1, "South");
    }

    private ImageIcon dogicon;
    private ImageIcon caticon;
    private ImageIcon mouseicon;
    private ImageIcon d1;
    private ImageIcon d2;
    private ImageIcon d3;
    private ImageIcon c1;
    private ImageIcon c2;
    private ImageIcon c3;
    private ImageIcon m1;
    private ImageIcon m2;
    private ImageIcon m3;
    private static ImageIcon all[] = new ImageIcon[9];
    private static ImageIcon dog[] = new ImageIcon[3];
    private static ImageIcon cat[] = new ImageIcon[3];
    private static ImageIcon mouse[] = new ImageIcon[3];
    private JLabel img;
    private JButton pre;
    private JButton next;
    private int choose;







}
