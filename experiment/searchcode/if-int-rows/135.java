package org.chaingang.game.imageshuffle;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

public class Puzzle extends JPanel
   implements MouseListener, MouseMotionListener
{
   private Image imgComplete;
   private int cols;
   private int rows;
   private int gutter = 1;
   private int border = 3;
   private Dimension totalSize;
   private Image [][] imgTile;
   private Rectangle [][] grid;
   private GridPos gridPos;
   private Point emptyPiece;
   private Random rand;
   private Cursor cursorDef, cursorSelect, cursorCurrent;
   private Color emptyColor;
   private boolean playState = false;
   private ActionListener parentListener;
   //private MediaTracker mt;

   public Puzzle(ActionListener parentListener, Image imgComplete, int cols, int rows, Color emptyColor, Color backColor) {
      this.parentListener = parentListener;
      this.imgComplete = imgComplete;
      this.cols = cols;
      this.rows = rows;
      this.emptyColor = emptyColor;

      MediaTracker mt = new MediaTracker(this);

      gridPos = new GridPos(cols, rows);
      Dimension imageSize = new Dimension(imgComplete.getWidth(null), imgComplete.getHeight(null) );
      Dimension sliceSize = new Dimension(
         (int)((imageSize.width-(gutter*(cols)))/cols),
         (int)((imageSize.height-(gutter*(rows)))/rows)
      );

      /*
      Dimension sliceSize = new Dimension(
         (int)Math.floor((imageSize.width-(gutter*(cols-1)))/cols),
         (int)Math.floor((imageSize.height-(gutter*(rows-1)))/rows)
      );
      */

      Image newImgComplete = getImageSlice(
         new Rectangle( 0,0,
            ((int)(imageSize.width/cols))*cols - (gutter*2),
            ((int)(imageSize.height/rows))*rows - (gutter*2)
         ),
         mt
      );

      waitForSlice(mt);

      this.imgComplete = newImgComplete;


      totalSize = new Dimension(
         (sliceSize.width*cols) + (gutter*(cols-1)) + (border*2),
         (sliceSize.height*rows) + (gutter*(rows-1)) + (border*2)
      );

      cursorDef = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
      cursorSelect = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
      cursorCurrent = cursorDef; setCursor(cursorCurrent);


      imgTile = new Image[cols][rows];
      grid = new Rectangle[cols][rows];
      for (int x=0; x<cols; x++) {
         for (int y=0; y<rows; y++) {
            Rectangle imageSlice = new Rectangle(
               (x*sliceSize.width) + (x * gutter ),
               (y*sliceSize.height) + (y * gutter ),
               (sliceSize.width-1), (sliceSize.height-1)
            );

            grid[x][y] = new Rectangle(
               imageSlice.x + border,
               imageSlice.y + border,
               imageSlice.width, imageSlice.height
            );
            imgTile[x][y] = getImageSlice(imageSlice, mt );
         }
      }
      waitForSlice(mt);

      addMouseListener(this);
      addMouseMotionListener(this);
      setPreferredSize(totalSize);
      setMaximumSize(totalSize);
      setMinimumSize(totalSize);

      setBackground(backColor);
   }

   public void setPlayState(boolean playState) {
      this.playState = playState;
      cursorCurrent = cursorDef;
      setCursor(cursorCurrent);
      repaint( 0, 0,0, getWidth(), getHeight() );
   }

   public boolean getPlayState() { return this.playState; }


   public int getMoveCount() { return gridPos.getMoveCount(); }


   public Point getPieceClicked(Point p) {
      for (int x=0; x<cols; x++) {
         for (int y=0; y<rows; y++) {
            if (grid[x][y].contains(p)) {
               return new Point(x,y);
            }
         }
      }
      return null;
   }


   private void drawTile(Graphics g, int x, int y) {
      Rectangle r = grid[x][y];
      Point cp = gridPos.getCurrentPiece(x, y);
      if ( (cp.x==cols-1) && (cp.y==rows-1) ) {
         g.setColor(emptyColor);
         g.fillRect(r.x, r.y, r.width, r.height );
      } else {
         g.drawImage(
            imgTile[cp.x][cp.y],
            r.x, r.y,
            this
         );
      }
   }

   private void drawTiles(Graphics g) {
      for (int x=0; x<cols; x++) {
         for (int y=0; y<rows; y++) {
            drawTile(g, x, y);
         }
      }
   }


   public void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (playState) {
         drawTiles(g);
      } else {
         g.drawImage(
            imgComplete,
            border, border,
            this
         );
      }

   }


   public void mouseDragged(MouseEvent e) { }
   public void mousePressed(MouseEvent e) { }
   public void mouseClicked(MouseEvent e) { }
   public void mouseEntered(MouseEvent e) { }
   public void mouseExited(MouseEvent e) { }


   public void mouseReleased(MouseEvent e) {
      if (!playState) return;
      Point p = getPieceClicked(e.getPoint());
      if (gridPos.isLive(p)) {
         Rectangle repaintRect = new Rectangle(grid[p.x][p.y]);
         repaintRect.add(grid[gridPos.emptyPiece.x][gridPos.emptyPiece.y]);

         gridPos.move(p);
         cursorCurrent = cursorDef;
         setCursor(cursorCurrent);

         if (gridPos.isWin()) {
            parentListener.actionPerformed(
               new ActionEvent(
                  e.getSource(), 0, Globals.CMD_WIN
               )
            );
            repaint( 0, 0,0, getWidth(), getHeight() );
         } else {
            repaint( 0, repaintRect.x, repaintRect.y, repaintRect.width, repaintRect.height );
         }
      }

   }


   public void mouseMoved(MouseEvent e) {
      if (!playState) return;
      Point p = getPieceClicked(e.getPoint());
      Cursor tmpCursor = gridPos.isLive(p) ? cursorSelect : cursorDef;
      if (tmpCursor != cursorCurrent) {
         setCursor(tmpCursor);
         cursorCurrent = tmpCursor;
      }
   }

   public void cmdShuffle() {
      gridPos.shuffle();
      setPlayState(true);
   }


   private Image getImageSlice(Rectangle r, MediaTracker mt) {
      ImageFilter filter = new CropImageFilter(r.x, r.y, r.width, r.height);
      ImageProducer producer = new FilteredImageSource(imgComplete.getSource(), filter);
      Image resultImage = createImage(producer);
      mt.addImage(resultImage, 2);

      return resultImage;
   }

   private void waitForSlice(MediaTracker mt) {
      try {
         mt.waitForID(2);
      } catch (InterruptedException ie)  {
         System.err.println(ie);
         System.exit(1);
      }
   }

}

