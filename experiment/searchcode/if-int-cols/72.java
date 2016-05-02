package org.chaingang.game.imageshuffle;

import java.awt.*;
import java.util.Random;


public class GridPos {
   public int cols;
   public int rows;
   Point [][] currentPos;
   Point emptyPiece;
   private Random rand;
   private int moveCount;
   private Rectangle lastSwap;

   public GridPos ( int cols, int rows) {
      this.cols = cols;
      this.rows = rows;

      currentPos = new Point[cols][rows];
      rand = new Random();
      shuffle();
   }


   public Point getCurrentPiece(Point p) {
      return currentPos[p.x][p.y];
   }

   public Point getCurrentPiece(int x, int y) {
      return currentPos[x][y];
   }

   public int getMoveCount() { return moveCount; }




   public boolean isWin() {
      Point p;
      for (int x=0; x<cols; x++) {
         for (int y=0; y<rows; y++) {
            p = currentPos[x][y];
            if (!((p.x==x) && (p.y==y))) {
               return false;
            }
         }
      }
      return true;
   }


   public boolean isLive(Point p) {
      if (p==null) return false;
      if (emptyPiece.x==p.x) {
         if ((emptyPiece.y==p.y-1) || (emptyPiece.y==p.y+1)) return true;
      }
      if (emptyPiece.y==p.y) {
         if ((emptyPiece.x==p.x-1) || (emptyPiece.x==p.x+1)) return true;
      }
      return false;
   }


   public Point [] getLiveList() {
      Point [] list = new Point[4];
      int size = 0;

      if (emptyPiece.x>0) list[size++] = new Point(emptyPiece.x-1, emptyPiece.y);
      if (emptyPiece.x<cols-1) list[size++] = new Point(emptyPiece.x+1, emptyPiece.y);
      if (emptyPiece.y>0) list[size++] = new Point(emptyPiece.x, emptyPiece.y-1);
      if (emptyPiece.y<rows-1) list[size++] = new Point(emptyPiece.x, emptyPiece.y+1);

      Point [] retList = new Point[size];
      for(int i=0; i<size; i++) {
         retList[i] = list[i];
      }

      return retList;
   }


   public Point [] subtractPointFromList(Point [] list, Point p) {
      int size = 0;

      for (int i=0; i<list.length; i++) {
         if (list[i].equals(p)) {
            Point [] retList = new Point[list.length - 1];
            int k=0;
            for (int j=0; j<list.length; j++) {
               if (i!=j) {
                  retList[k++]=list[j];
               }
            }
            return retList;
         }
      }

      return list;
   }


   public void move(Point p1, Point p2) {
      moveCount++;
      Point pt = currentPos[p1.x][p1.y];
      currentPos[p1.x][p1.y] = currentPos[p2.x][p2.y];
      currentPos[p2.x][p2.y] = pt;
   }

   public void move(Point p) {
      move(p, emptyPiece);
      emptyPiece = p;
   }


   public void shuffle() {
      int moveCount = 1000;
      Point lastMove = emptyPiece;
      Point select;
      Point [] list = new Point[4];

      for (int x=0; x<cols; x++) {
         for (int y=0; y<rows; y++) {
            currentPos[x][y] = new Point(x,y);
         }
      }
      emptyPiece = new Point(cols-1, rows-1);

      while(moveCount-- > 0) {
         list = subtractPointFromList(
            getLiveList(),
            lastMove
         );
         lastMove = emptyPiece;
         select = list[rand.nextInt(list.length)];
         //System.out.println("shuffle: " + moveCount + ":" + select);
         move(select);
      }
      this.moveCount = 0;
   }

}




