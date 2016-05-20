import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

@SuppressWarnings("serial")
public class TetrisCourt extends JPanel {
  
  //Variable that determines how large the game window will be.
  //Everything in the game is scaled according to this value. 
  final static int PIXEL = 20;
  
  private int interval = 35; // Milliseconds between updates.
  private Timer timer;       // Each time timer fires we animate one step.

  final int COURTWIDTH = PIXEL * 16;
  final int COURTHEIGHT = PIXEL * 24;
  
  private Tetromino currentT;
  private Tetromino nextT;
  private Tetromino holdT;

  private int[][] taken = new int[20][10]; //The list of settled blocks are kept in a 2d array.
  private boolean holding;
  private boolean running; 
  
  private int turn = 0; //Limits the speed of the game.
  private int lines; //The number of lines the player has cleared
  private int score; 
  

  public TetrisCourt() {
    setPreferredSize(new Dimension(COURTWIDTH, COURTHEIGHT));
    setBorder(BorderFactory.createLineBorder(Color.BLACK));
    setFocusable(true);
    
    timer = new Timer(interval, new ActionListener() {
      public void actionPerformed(ActionEvent e) { tick(); }});
    timer.start(); 

    /*Control scheme:
     * Left Arrow - move block left
     * Right Arrow - move block right
     * Up Arrow - change orientation of the block
     * Down Arrow - make block fall faster
     * Space Bar - make block instantly fall
     * Shift - places block into hold
     * R - resets game
     */
    addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT)
          currentT.moveLeft();
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
          currentT.moveRight();
        else if (e.getKeyCode() == KeyEvent.VK_UP)
          currentT.rotate();
        else if (e.getKeyCode() == KeyEvent.VK_DOWN)
          turn=9-(lines/10);
        else if (e.getKeyCode() == KeyEvent.VK_R)
          reset();
        else if (e.getKeyCode() == KeyEvent.VK_SPACE)
        {
          if(running)
            drop();
        }
        else if (e.getKeyCode() == KeyEvent.VK_SHIFT)
        {
          if(!holding)
            hold();
        }
      }
    });
  }
  
  /* My collision detector checks to see that the blocks are within the bounds
   * of the playing field. It also checks to see if the falling block is 
   * colliding with any of the settled blocks. 
   */
  public boolean collision (int xpos, int ypos, int[] orient) {
    int xpp = xpos/PIXEL - 6;
    int ypp = ypos/PIXEL - 4;
    
    for (int i=0; i<16; i++) {
      if (orient[i] == 1) {
        int x = xpp + (i%4);
        int y = ypp + (int) Math.floor(i/4);
        if (x < 0 || x >= 10 || y > 19) {
          return true;
        }
        
        if (y > 0 && taken[y][x] != 0) {
          return true;
        }
      }
    }
    return false;
  }
  
  //Resets the game to initial state
  public void reset() {
    currentT = new Tetromino (PIXEL * 9, 0, randomT().getType(), this);
    nextT = randomT();
    holdT = null;
    taken = new int[20][10];
    lines = 0;
    score = 0;
    holding = false;
    running = true;
    grabFocus();
  }
  
  /* Generates a random Tetromino. The Tetromino spawned here is put into the 
   * next area of this JPanel. 
   */
  private Tetromino randomT()
  {
    int type = (int)(Math.random()*7);
    if (type == 0)
    {
      return new Tetromino (PIXEL * 1, PIXEL * 5, Piece.I, this);
    }
    else if (type == 1)
    {
      return new Tetromino (PIXEL * 1, PIXEL * 5, Piece.O, this);
    }
    else if (type == 2)
    {
      return new Tetromino (PIXEL * 1, PIXEL * 5, Piece.T, this);
    }
    else if (type == 3)
    {
      return new Tetromino (PIXEL * 1, PIXEL * 5, Piece.S, this);
    }
    else if (type == 4)
    {
      return new Tetromino (PIXEL * 1, PIXEL * 5, Piece.Z, this);
    }
    else if (type == 5)
    {
      return new Tetromino (PIXEL * 1, PIXEL * 5, Piece.J, this);
    }
    else
    {
      return new Tetromino (PIXEL * 1, PIXEL * 5, Piece.L, this);
    }
  }
  
  /* Allows the player to place a falling block into hold, or swap it out if 
   * there is already one in the hold. Each block can only be swapped into
   * the hold once. 
   */
  private void hold () {
    holding = true;
    Piece p = currentT.getType();
    if (holdT != null) {
      currentT = new Tetromino (PIXEL * 9, 0, holdT.getType(), this);
      holdT = new Tetromino (PIXEL * 1, PIXEL * 19, p, this);
    }
    else {
      holdT = new Tetromino (PIXEL * 1, PIXEL * 19, p, this);
      currentT = new Tetromino (PIXEL * 9, 0, nextT.getType(), this);
      nextT = randomT();
    }
  }
  
  /*Instantly drops a block by repeatedly calling tick()*/
  private void drop () {
    Tetromino temp = currentT;
    while (temp == currentT) {
      tick ();
    }
  }
  
  /* Progresses the game state step by step. Checks to see if the current 
   * falling block has landed on top of another, at which point it adds the 
   * block to a 2d array. It then calls a function to see if there is an 
   * entire row of blocks. It also checks to see if the blocks have reached
   * the top of the playing field, at which point the game is over. 
   */
  void tick () {
    if (running) {
      turn = ((turn+1) % Math.max(1, (10 - lines/10)));      
      if (turn == 0) {
        if (!currentT.moveDown()) {
          holding = false;
          int xpp = currentT.getX()/PIXEL - 6;
          int ypp = currentT.getY()/PIXEL - 4;
          int [] orient = currentT.getOrient();
          int c = 1;
          
          if (currentT.getType() == Piece.O) {
            c = 2;
          }
          else if (currentT.getType() == Piece.S) {
            c = 3;
          }
          else if (currentT.getType() == Piece.Z) {
            c = 4;
          }
          else if (currentT.getType() == Piece.J) {
            c = 5;
          }
          else if (currentT.getType() == Piece.L) {
            c = 6;
          }
          else if (currentT.getType() == Piece.T) {
            c = 7;
          }
          
          for (int i=0; i<16; i++) {
            if (orient[i] == 1) {
              int x = xpp + (i%4);
              int y = ypp + (int) Math.floor(i/4);
              if (y >= 0) {
                taken [y][x] = c;
              }
            } 
          }
          
          checkLines();
          currentT = new Tetromino (PIXEL * 9, 0, nextT.getType(), this);
          nextT = randomT();
          
          for (int i=0; i<10; i++) {
            if (taken[0][i] != 0) {
              running = false;
            }
          }
        }     
      }
      repaint();
    }
  }
  
  /*This function checks to see if a row in the playing field has been
   * successfully filled. If the row has been filled, this function deletes
   * that row from the 2d array holding the fallen blocks. It also updates 
   * the user's score according to how many rows were cleared at a time. 
   */
  public void checkLines() {
    int numclr = 0;
    
    for (int i=19; i>=0; i--) {
      int count = 0;
      
      while (count<10 && taken[i][count] != 0) {
        count ++;
      }
      if (count==10) {
        for (int j=i; j>0; j--) {
          taken[j] = taken[j-1];
        }
        taken[0] = new int [10];
        i++;
        lines++;
        numclr++;
      }
    }
    if (numclr == 1) {
      score += 10;
    }
    else if (numclr == 2) {
      score += 30;
    }
    else if (numclr == 3) {
      score += 60;
    }
    else if (numclr == 4) {
      score += 100; //If the player gets a TESTRIS they are awarded 100 points
    }
  }
  
  /*This function paints the playing field. It iterates through the 2d array 
   * containing the locations of the fallen blocks and paints them onto the
   * screen. The colors of the fallen blocks are kept. It then calls the draw
   * function on the three Tetrominoes that are on the field - the falling one,
   * the next one and the one in hold. It displays the statistics such as the
   * score and the number of lines cleared. 
   */
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.setColor(Color.black); //Background is black
    g.fillRect(0, 0, COURTWIDTH, COURTHEIGHT);
    
    for (int j=0; j<20; j++) {
      for (int i=0; i<10; i++) { //Iterates through the 2d array
        if (taken[j][i] == 1) {
          g.setColor(Color.CYAN);
          g.fillRect(PIXEL*(i+6), PIXEL*(j+4), PIXEL, PIXEL);
          g.setColor(new Color(0,205,205));
          g.fillRect(PIXEL*(i+6)+2, PIXEL*(j+4)+2, PIXEL-4, PIXEL-4);
          g.setColor(new Color(0,139,139));
          g.fillRect(PIXEL*(i+6)+4, PIXEL*(j+4)+4, PIXEL-8, PIXEL-8);  
        }
        
        else if (taken[j][i] == 2) {
          g.setColor(Color.YELLOW);
          g.fillRect(PIXEL*(i+6), PIXEL*(j+4), PIXEL, PIXEL);
          g.setColor(new Color(205,205,0));
          g.fillRect(PIXEL*(i+6)+2, PIXEL*(j+4)+2, PIXEL-4, PIXEL-4);
          g.setColor(new Color(139,139,0));
          g.fillRect(PIXEL*(i+6)+4, PIXEL*(j+4)+4, PIXEL-8, PIXEL-8);  
        }
        
        else if (taken[j][i] == 3) {
          g.setColor(Color.GREEN);
          g.fillRect(PIXEL*(i+6), PIXEL*(j+4), PIXEL, PIXEL);
          g.setColor(new Color(0,205,0));
          g.fillRect(PIXEL*(i+6)+2, PIXEL*(j+4)+2, PIXEL-4, PIXEL-4);
          g.setColor(new Color(0,139,0));
          g.fillRect(PIXEL*(i+6)+4, PIXEL*(j+4)+4, PIXEL-8, PIXEL-8); 
        }
        
        else if (taken[j][i] == 4) {
          g.setColor(Color.RED);
          g.fillRect(PIXEL*(i+6), PIXEL*(j+4), PIXEL, PIXEL);
          g.setColor(new Color(205,0,0));
          g.fillRect(PIXEL*(i+6)+2, PIXEL*(j+4)+2, PIXEL-4, PIXEL-4);
          g.setColor(new Color(139,0,0));
          g.fillRect(PIXEL*(i+6)+4, PIXEL*(j+4)+4, PIXEL-8, PIXEL-8); 
        }
        
        else if (taken[j][i] == 5) {
          g.setColor(Color.BLUE);
          g.fillRect(PIXEL*(i+6), PIXEL*(j+4), PIXEL, PIXEL);
          g.setColor(new Color(0,0,205));
          g.fillRect(PIXEL*(i+6)+2, PIXEL*(j+4)+2, PIXEL-4, PIXEL-4);
          g.setColor(new Color(0,0,139));
          g.fillRect(PIXEL*(i+6)+4, PIXEL*(j+4)+4, PIXEL-8, PIXEL-8);  
        }
        
        else if (taken[j][i] == 6) {
          g.setColor(Color.ORANGE);
          g.fillRect(PIXEL*(i+6), PIXEL*(j+4), PIXEL, PIXEL);
          g.setColor(new Color(238,154,0));
          g.fillRect(PIXEL*(i+6)+2, PIXEL*(j+4)+2, PIXEL-4, PIXEL-4);
          g.setColor(new Color(205,133,0));
          g.fillRect(PIXEL*(i+6)+4, PIXEL*(j+4)+4, PIXEL-8, PIXEL-8); 
        }
        
        else if (taken[j][i] == 7) {
          g.setColor(Color.MAGENTA);
          g.fillRect(PIXEL*(i+6), PIXEL*(j+4), PIXEL, PIXEL);
          g.setColor(new Color(205,0,205));
          g.fillRect(PIXEL*(i+6)+2, PIXEL*(j+4)+2, PIXEL-4, PIXEL-4);
          g.setColor(new Color(139,0,139));
          g.fillRect(PIXEL*(i+6)+4, PIXEL*(j+4)+4, PIXEL-8, PIXEL-8); 
        }
      }
      
      if (running) {
        currentT.draw(g);
      }
      Font Impact = new Font("Impact", Font.PLAIN, 18);
      g.setFont(Impact);
      g.setColor(Color.RED);
      
      g.drawString("Next", PIXEL * 1, PIXEL * 4);
      g.drawString("Level:", PIXEL * 1, PIXEL * 9);
      //The level is determined by how many lines the player has cleared.
      //Every 10 lines the level increases by 1, and the blocks fall faster
      g.drawString(Math.min(10,((lines/10)+1))+"", PIXEL * 3, PIXEL * 10);
      g.drawString("Lines:", PIXEL * 1, PIXEL * 12);
      g.drawString(lines+"",PIXEL * 3, PIXEL * 13);
      g.drawString("Score:", PIXEL * 1, PIXEL * 15);
      g.drawString(score+"", PIXEL * 3, PIXEL * 16);
      g.drawString("Hold:", PIXEL * 1, PIXEL * 18);
      
      nextT.draw(g);
      
      if(holdT!=null) {
        holdT.draw(g);
      }
      g.setColor(Color.black);
      g.fillRect(PIXEL * 6, 0, PIXEL * 10, PIXEL * 4);
      
      Font Impact2 = new Font("Impact", Font.PLAIN, 26);
      g.setFont(Impact2);
      g.setColor(Color.blue);
      g.drawRect(PIXEL * 6, PIXEL * 4, PIXEL * 10, PIXEL * 20);
      
      if(running)
      {
        g.drawString("TETRIS", PIXEL * 6, PIXEL * 2);
      }
      else {
        g.drawString("You Failed! Play Again?",PIXEL * 2, PIXEL * 2);
      }
    }
  }
}

