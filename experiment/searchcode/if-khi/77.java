/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package heartsgamejava;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 * @author 0812627
 */
public class HeartsGamePanel extends JPanel implements Runnable{
    private static final int PWIDTH = 1000;// kich thuot cua panel
    private static final int PHEIGHT = 700;// kich thuot cua panel
    static int iSTT[]  = new int[13];
    private Thread animator; //tuyen chay hoat canh
    private boolean running = false ;// bi?n ?i?u ki?n ng?ng vňng l?p
    private boolean gameOver = false ;// bi?n ?i?u ki?n k?t thúc game
    //
    //các giá tr? toŕn c?c ?? v? ngoŕi mŕn hěnh
    private Graphics dbg;
    private Image dbImage = null;
    HeartsGameGraphics dbGraphic;

    //Các bi?n toŕn c?c c?a bŕi
    private boolean [] isDraw = new boolean[3];
    //Rectangle position = new Rectangle(300, 400, 200, 200);
    Image[] img;
    // m?i thęm 21/6
    HeartsLocationPanel heartsLocationPanel = new HeartsLocationPanel();
    int abc = -1;

    public HeartsGamePanel()
    {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(PWIDTH,PHEIGHT));

        setFocusable(true);
        requestFocus();// JPanel bây gi? có th? nh?n s? ki?n phím
        readyForTermination();
        //t?o các thŕnh ph?n game

        //L?ng nghe s? ki?n nh?n chu?t
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                HeartsPress(e.getX(), e.getY());
            }


        });
    }

    @Override
    public void addNotify()
    // Ch? JFrame ???c g?n vŕo tr??c khi b?t ??u
    {
        super.addNotify();
        startGame();
    }

    private void startGame()
    // kh?i t?o vŕ b?t ??u Thread
    {
        for(int i = 0; i < isDraw.length ;i++)
        {
            isDraw[i] = true;
        }



        if(animator == null || !running)
        {
            animator = new Thread(this);
            animator.start();
        }
    }

    public void stopGame()
    {
        running = false;
    }


    public void run()
    {
        running = true;
        while(running)
        {
            gameUpdate();// tr?ng thái game ???c c?p nh?t
            try {
                gameRender(); //repaint(); // v? b? ??m lęn mŕn hěnh
                //repaint(); // v? b? ??m lęn mŕn hěnh
            } catch (InterruptedException ex) {
                Logger.getLogger(HeartsGamePanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            paintScreen();
            try {
                Thread.sleep(20); // ng? m?t kho?ng ng?n
            } catch (InterruptedException ex) {
                Logger.getLogger(HeartsGamePanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        System.exit(0);// ?óng JFrame
    }

    private void gameUpdate() {
        if(!gameOver) // c?p nh?t tr?ng thái game
        {
            if(HeartsStaticObject.iCardClient[0] !=-1)
            {
              HeartsStaticObject.heartDeckStatic.getPlayers(1).getCardsInHand().get(HeartsStaticObject.iCardClient[0]).setiStatusPlay(1);

            }
            if(HeartsStaticObject.iCardClient[1] !=-1)
            {
              HeartsStaticObject.heartDeckStatic.getPlayers(2).getCardsInHand().get(HeartsStaticObject.iCardClient[1]).setiStatusPlay(1);

            }
            if(HeartsStaticObject.iCardClient[2] !=-1)
            {
              HeartsStaticObject.heartDeckStatic.getPlayers(3).getCardsInHand().get(HeartsStaticObject.iCardClient[2]).setiStatusPlay(1);

            }
        }
    }

    private void gameRender() throws InterruptedException
    // v? m?t frame hi?n hŕnh vŕo m?t ?nh c?a b? ??m
    {

        if(dbImage == null)
        {
            //t?o ?nh b? ??m
            dbImage = createImage(PWIDTH,PHEIGHT);
            if(dbImage == null)
            {

            }
            else
                dbGraphic = new HeartsGameGraphics(dbImage);
  
        }

        // V? các ph?n t? game
        
        dbGraphic.drawGraphics(this);

        
        if(gameOver)
            gameOverMessage(dbGraphic);
    }

    private void gameOverMessage(HeartsGameGraphics dbGraphic) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);
        if(dbImage != null)
        {
            g.drawImage(dbImage, 0, 0, null);
        }
    }

    private void readyForTermination()
    {
        addKeyListener(new KeyAdapter() {
            //L?ng nghe các phím éc,q,end
            @Override
            public void keyPressed(KeyEvent e)
            {
                int keyCode = e.getKeyCode();
                if((keyCode == KeyEvent.VK_ESCAPE)&&e.isControlDown())
                {
                    running = false;
                }
            }
        });

    }


    int HeartsPlayOneCardBasic(int iLocation, int iPlayer)
    {
          // těm s? th? t? các quân bŕi trong m?ng d?a vŕo v? trí c?a chúng tręn mŕn hěnh
                iSTT[iLocation] = HeartsStaticObject.heartDeckStatic.getPlayers(iPlayer).findLocation(iLocation);
          // gán tr?ng thái ?ă ch?i c?a quân bŕi těm ???c lŕ 1
                HeartsStaticObject.heartDeckStatic.getPlayers(iPlayer).getCardsInHand().get(iSTT[iLocation]).setiStatusPlay(1);
           // gán các v? trí ti?p theo trong danh sách các quân bŕi
                //gi?m ?i m?t ??n v? k? t? quân bŕi ?ă ch?n
                // ??u vŕo lŕ v? trí quân bŕi ?ă ch?n
                // x? lí: xét các v? trí ti?p theo

                // k?t qu?: các v? trí ti?p theo t?ng lęn m?t ??n v?
                HeartsStaticObject.heartDeckStatic.getPlayers(iPlayer).setLocations(iLocation);
              // gán v? trí c?a quân bŕi ?ó lŕ -1
                // vě nó ?ă ch?i nęn không cňn ??ng tręn mŕn hěnh n?a
                HeartsStaticObject.heartDeckStatic.getPlayers(iPlayer).getCardsInHand().get(iSTT[iLocation]).setiLocation(-1);

                return iSTT[iLocation];

    }
    // ??u vŕo lŕ v? trí tręn mŕn hěnh
    // tr? v? s? th? t? c?a lá bŕi vŕ ng??i ch?i th? m?y
    int  HeartsPlayOneCard(int iLocation, int iPlayer)
    {
          // těm s? th? t? c?a lá bŕi
          iSTT[iLocation] = HeartsStaticObject.heartDeckStatic.getPlayers(iPlayer).findLocation(iLocation);

          // n?u s? th? t? b?ng -1
          if(iSTT[iLocation]== -1)
          {
                return HeartsPlayOneCardBasic(iLocation - 1, iPlayer);

          }
          else
          {
                HeartsPlayOneCardBasic(iLocation,iPlayer);
                return iSTT[iLocation];
          }


    }

    private void HeartsPress(int x, int y)
    {
        if(!gameOver)
        {
            for(int i= 0 ; i <13; ++i)
            {
            // th?c hi?n ch?n bŕi ki?m tra bŕi

                if(( heartsLocationPanel.iLocationPanel[i] < x )&&( x < heartsLocationPanel.iLocationPanel[i+1] )&&(500 < y) &&(y<635))
                {

                    HeartsStaticObject.iCardServer = HeartsPlayOneCard(i,0);
                }
                if(( heartsLocationPanel.iLocationPanel[i] < x )&&( x < heartsLocationPanel.iLocationPanel[i+1] )&&(300 < y) &&(y<400))
                {
                    HeartsStaticObject.iCardClient[1] = i;
                    //HeartsStaticObject.iCardServer = HeartsPlayOneCard(i,1);
                }
                if(( heartsLocationPanel.iLocationPanel[i] < x )&&( x < heartsLocationPanel.iLocationPanel[i+1] )&&(400 < y) &&(y<500))
                {
                    HeartsStaticObject.iCardClient[0] = i;

                }
                if(( heartsLocationPanel.iLocationPanel[i] < x )&&( x < heartsLocationPanel.iLocationPanel[i+1] )&&(0 < y) &&(y<300))
                {
                    HeartsStaticObject.iCardClient[2] = i;

                }
            }

            if(( 0 < x )&&( x <100 ))
            {
                HeartsStaticObject.heartDeckStatic.getPlayers(0).getCardsInHand().get(4).setiStatusPlay(2);
                isDraw[1] = false;

            }

        }
    }

    private void paintScreen()
            // v? b? ??m ?nh lęn mŕn hěnh
    {
        Graphics g;
        try
        {
            g = this.getGraphics();// l?y ng? c?nh ?? h?a c?a panel
            if((g!=null)&&(dbImage!=null))
            {
                g.drawImage(dbImage, 0, 0, this);
            }
        }
        catch(Exception e){
            System.out.println("error graphic draw"+ e);
        }
    }


}

