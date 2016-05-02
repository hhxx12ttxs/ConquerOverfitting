import javax.swing.*;
import java.awt.*;
import java.awt.Image;
import java.awt.event.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.Scanner;
import java.util.*;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.text.*;
import javax.swing.*;
import java.awt.event.MouseEvent;

class Inter
{
    private JFrame initialFrame;
    private JButton start;
    private JButton instruction;
    private JTextArea textArea;
    private JTextField errorText;
    public static String errorMes = null;
    private ImageIcon gameLogo;
    private JFrame ins = new JFrame("Instructions");
    private ImageIcon instr;
    private JFrame quiz = new JFrame("Quiz");
    private Logo labelLogo = new Logo();
    private Parse parse = new Parse(10);
    private JLabel flagPhoto;
    private JTextField answer = new JTextField(60);
    private JButton a = new JButton("");
    private JButton b = new JButton("");
    private JButton c = new JButton("");
    private JButton d = new JButton("");
    private static boolean ok;
    private Box boxQ = Box.createVerticalBox();
    private SpringLayout layout = new SpringLayout();
    private ImageIcon flag;
    private static int counter = 0;
    public static int correct = -1;

    public Inter()
    {
        // creating the first frame and adding animation with Sheldon
        initialMethod();

        //if the instructions button is pressed (instructions frame)
        instructionMethod();

        //quiz frame
        quizMethod();
    }

    public void initialMethod()
    {
        initialFrame = new JFrame("Fun with Flags!");
        initialFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initialFrame.setLocation(100, 100);
        initialFrame.setBackground(Color.CYAN.darker());
        initialFrame.setMinimumSize(new Dimension(400, 200));

        start = new JButton("START");
        instruction = new JButton("INSTRUCTIONS");
        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        instruction.setAlignmentX(Component.CENTER_ALIGNMENT);

        Box box = Box.createVerticalBox();

        errorText = new JTextField(70);
        errorText.setText("This is where the error messages from loading will occur");
        errorText.setHorizontalAlignment(JTextField.CENTER);
        errorText.setEditable(false);

        try
        {
            instr = new ImageIcon(this.getClass()
                    .getResource("instructions.png"));
        }
        catch (NullPointerException e)
        {
            Inter.errorMes += "Instructions logo not found";
        }

        start.setBackground(Color.GRAY);
        start.addMouseListener(new MouseAdapter()
        {
            Font originalFont = null;
            public void mouseEntered(MouseEvent evt)
            {
                start.setBackground(Color.LIGHT_GRAY);
            }
            public void mouseExited(MouseEvent evt)
            {
                start.setBackground(Color.GRAY);
            }
        });
        instruction.setBackground(Color.GRAY);
        instruction.addMouseListener(new MouseAdapter()
        {
            Font originalFont = null;
            public void mouseEntered(MouseEvent evt)
            {
                instruction.setBackground(Color.LIGHT_GRAY);
            }
            public void mouseExited(MouseEvent evt)
            {
                instruction.setBackground(Color.GRAY);
            }
        });

        labelLogo.setLayout(layout);
        labelLogo.add(start);
        labelLogo.add(instruction);

        layout.putConstraint(SpringLayout.EAST, start,
                             -10,
                             SpringLayout.EAST, labelLogo);
        layout.putConstraint(SpringLayout.NORTH, start,
                             40,
                             SpringLayout.NORTH, labelLogo);
        layout.putConstraint(SpringLayout.EAST, instruction,
                             -10,
                             SpringLayout.EAST, labelLogo);
        layout.putConstraint(SpringLayout.NORTH, instruction,
                             80,
                             SpringLayout.NORTH, labelLogo);

        labelLogo.setBackground(Color.CYAN);
        initialFrame.add(labelLogo);

        initialFrame.setResizable(true);
        initialFrame.pack();
        initialFrame.setVisible(true);
    }

    public void quizMethod()
    {
        start.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent g)
            {
                quiz.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                quiz.setLocation(100,100);

                Box box1 = Box.createHorizontalBox();

                JTextField question = new JTextField();
                question.setBackground(Color.CYAN.brighter());
                question.setHorizontalAlignment(JTextField.RIGHT);
                question.setText("Which country is represented by this flag?");
                question.setEditable(false);

                box1.add(question);

                flagPhoto = new JLabel(flag);
                flagPhoto.setAlignmentX(Component.CENTER_ALIGNMENT);
                box1.add(flagPhoto);

                JPanel box2 = new JPanel();
                box2.setLayout(new GridLayout(2, 2, 5, 5));
                a.setSize(new Dimension(231,200));
                b.setSize(new Dimension(231,200));
                c.setSize(new Dimension(231,200));
                d.setSize(new Dimension(231,200));
                a.setBackground(Color.LIGHT_GRAY);
                b.setBackground(Color.LIGHT_GRAY);
                c.setBackground(Color.LIGHT_GRAY);
                d.setBackground(Color.LIGHT_GRAY);
                box2.add(a);
                box2.add(b);
                box2.add(c);
                box2.add(d);

                answer.setPreferredSize(new Dimension(100, 20));
                answer.setText("This is where the correct answer will appear");
                answer.setHorizontalAlignment(JTextField.CENTER);
                answer.setEditable(false);
                box1.setAlignmentX(Component.CENTER_ALIGNMENT);
                boxQ.add(box1);
                box1.setAlignmentX(Component.CENTER_ALIGNMENT);
                boxQ.add(box2);
                quiz.add(boxQ);
                quiz.add(answer);
                quiz.setBackground(Color.CYAN.brighter());
                boxQ.setBackground(Color.CYAN);
                answer.setBackground(Color.CYAN);

                SpringLayout layout = new SpringLayout();
                quiz.setLayout(layout);

                //this is the method which adds the new question (counter keeps track)
                quiz.pack();
                quiz.setSize(new Dimension(answer.getWidth(), 200));
                int ab = quiz.getWidth()/2 - boxQ.getWidth()/2 - 50;
                layout.putConstraint(SpringLayout.SOUTH, answer,
                             120,
                             SpringLayout.SOUTH, quiz);
                layout.putConstraint(SpringLayout.WEST, answer,
                             0,
                             SpringLayout.WEST, quiz);
                layout.putConstraint(SpringLayout.NORTH, boxQ,
                             5,
                             SpringLayout.NORTH, quiz);
                layout.putConstraint(SpringLayout.WEST, boxQ,
                             ab,
                             SpringLayout.WEST, quiz);

                initialFrame.setVisible(false);
                quiz.setVisible(true);
                quiz.pack();
                quiz.validate();
                quiz.setResizable(false);

                addQuestion();
            }
        });
    }

    public void addQuestion()
    {
        quiz.setMinimumSize(new Dimension(600,200));
        quiz.validate();

        flag = null;
        try
        {
            if(Quiz.os.equals("Linux"))
            {
                flag = new ImageIcon(this.getClass()
                    .getResource("/flags/" + Parse.var[counter][4] + ".png"));
            }
            else
            {
                Parse.var[counter][4].replace(Quiz.home,"");
                flag = new ImageIcon(this.getClass()
                    .getResource("\\flags\\"+ Parse.var[counter][4] +".png"));
            }
        }
        catch(Exception e)
        {
            Logo.error += "Image " + Parse.var[counter][4] + ".png was not loaded. Reinstall game!";
        }
        if(flag == null)
        {
            return;
        }
        flagPhoto.setIcon(flag);

        a.setText(Parse.var[counter][0]);
        a.addMouseListener(new MouseAdapter()
        {
            Font originalFont = null;
            public void mouseEntered(MouseEvent evt)
            {
                a.setBackground(Color.GRAY);
            }
            public void mouseExited(MouseEvent evt)
            {
                a.setBackground(Color.LIGHT_GRAY);
            }
            public void mouseClicked(MouseEvent e)
            {
                String[] ans = Parse.var[counter][4].split("_");
                String ans2 = null;
                for(int i = 0; i < ans.length; ++i)
                {
                    if(ans2 == null)
                    {
                        ans2 = ans[i];
                    }
                    else
                    {
                        ans2 += ans[i];
                    }
                    if(i < (ans.length - 1))
                    {
                        ans2 += " ";
                    }
                }
                if(Parse.var[counter][0].equals(ans2))
                {
                    answer.setText("Your answer was right!");
                    change(a);
                }
                else
                {
                    answer.setText("Right answer was " + ans2);
                }
            }
        });

        b.setText(Parse.var[counter][1]);
        b.addMouseListener(new MouseAdapter()
        {
            Font originalFont = null;
            public void mouseEntered(MouseEvent evt)
            {
                b.setBackground(Color.GRAY);
            }
            public void mouseExited(MouseEvent evt)
            {
                b.setBackground(Color.LIGHT_GRAY);
            }
            public void mouseClicked(MouseEvent e)
            {
                String[] ans = Parse.var[counter][4].split("_");
                String ans2 = null;
                for(int i = 0; i < ans.length; ++i)
                {
                    if(ans2 == null)
                    {
                        ans2 = ans[i];
                    }
                    else
                    {
                        ans2 += ans[i];
                    }
                    if(i < (ans.length - 1))
                    {
                        ans2 += " ";
                    }
                }
                if(Parse.var[counter][1].equals(ans2))
                {
                    answer.setText("Your answer was right!");
                    change(b);
                }
                else
                {
                    answer.setText("Right answer was " + ans2);
                }
            }
        });

        c.setText(Parse.var[counter][2]);
        c.addMouseListener(new MouseAdapter()
        {
            Font originalFont = null;
            public void mouseEntered(MouseEvent evt)
            {
                c.setBackground(Color.GRAY);
            }
            public void mouseExited(MouseEvent evt)
            {
                c.setBackground(Color.LIGHT_GRAY);
            }
            public void mouseClicked(MouseEvent e)
            {
                String[] ans = Parse.var[counter][4].split("_");
                String ans2 = null;
                for(int i = 0; i < ans.length; ++i)
                {
                    if(ans2 == null)
                    {
                        ans2 = ans[i];
                    }
                    else
                    {
                        ans2 += ans[i];
                    }
                    if(i < (ans.length - 1))
                    {
                        ans2 += " ";
                    }
                }
                if(Parse.var[counter][2].equals(ans2))
                {
                    answer.setText("Your answer was right!");
                    change(c);
                }
                else
                {
                    answer.setText("Right answer was " + ans2);
                }
            }
        });

        d.setText(Parse.var[counter][3]);
        d.addMouseListener(new MouseAdapter()
        {
            Font originalFont = null;
            public void mouseEntered(MouseEvent evt)
            {
                d.setBackground(Color.GRAY);
            }
            public void mouseExited(MouseEvent evt)
            {
                d.setBackground(Color.LIGHT_GRAY);
            }
            public void mouseClicked(MouseEvent e)
            {
                String[] ans = Parse.var[counter][4].split("_");
                String ans2 = null;
                for(int i = 0; i < ans.length; ++i)
                {
                    if(ans2 == null)
                    {
                        ans2 = ans[i];
                    }
                    else
                    {
                        ans2 += ans[i];
                    }
                    if(i < (ans.length - 1))
                    {
                        ans2 += " ";
                    }
                }
                if(Parse.var[counter][3].equals(ans2))
                {
                    answer.setText("Your answer was right!");
                    change(d);
                }
                else
                {
                    answer.setText("Right answer was " + ans2);
                }
            }
        });
    }

    void change(JButton x)
    {
        x.setBackground(Color.GREEN.brighter());
        x.setOpaque(true);
        //quiz.validate();
        try
        {
            x.setBackground(Color.GREEN.brighter());
            Thread.sleep(1000);
        }
        catch(Exception k)
        {}

        counter++;
        a.setText(Parse.var[counter][0]);
        b.setText(Parse.var[counter][1]);
        c.setText(Parse.var[counter][2]);
        d.setText(Parse.var[counter][3]);
        flag = null;
        try
        {
            if(Quiz.os.equals("Linux"))
            {
                flag = new ImageIcon(this.getClass()
                    .getResource("/flags/" + Parse.var[counter][4] + ".png"));
            }
            else
            {
                flag = new ImageIcon(this.getClass()
                    .getResource("\\flags\\" + Parse.var[counter][4] + ".png"));
            }
        }
        catch(Exception e)
        {
            Logo.error += "Image " + Parse.var[counter][4] + ".png was not loaded. Reinstall game!";
        }
        if(flag == null)
        {
            return;
        }
        flagPhoto.setIcon(flag);

        quiz.validate();
        if(counter == 9)
        {
            quiz.setVisible(false);
            addFinalFrame();
        }
    }

    public void addFinalFrame()
    {
        JFrame finalFrame = new JFrame("congratulations!");
        finalFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        finalFrame.setVisible(true);
        finalFrame.setResizable(false);
        finalFrame.setLocation(100,100);

        Win win = new Win();
        win.setBackground(Color.WHITE);
        finalFrame.setBackground(Color.WHITE);
        finalFrame.add(win);

        finalFrame.setMinimumSize(new Dimension(400,200));
        finalFrame.pack();
    }

    public void instructionMethod()
    {
        if(Inter.errorMes != null)
        {
            errorText.setText(Inter.errorMes + "! Please reinstall game!");
        }
        else
        {
            if(Inter.errorMes == null)
            {

                instruction.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent f)
                    {
                        ins.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        ins.setLocation(100, 100);
                        ins.setMinimumSize(new Dimension(400,200));
                        Box box = Box.createHorizontalBox();
                        JLabel label0 = new JLabel(instr);
                        box.add(label0);
                        initialFrame.setVisible(false);
                        JButton back = new JButton("Start");
                        Box backBox = Box.createHorizontalBox();
                        backBox.add(back);
                        Box bigBox = Box.createVerticalBox();
                        bigBox.add(box);
                        bigBox.add(backBox);
                        JPanel p = new JPanel();
                        p.add(bigBox);
                        p.setBackground(Color.WHITE);
                        ins.add(p);
                        ins.setVisible(true);
                        ins.pack();
                        back.addActionListener(new ActionListener()
                        {
                            public void actionPerformed(ActionEvent h)
                            {
                                ins.setVisible(false);
                                initialFrame.setVisible(true);
                            }
                        });
                    }
                });

            }
        }
    }
}

