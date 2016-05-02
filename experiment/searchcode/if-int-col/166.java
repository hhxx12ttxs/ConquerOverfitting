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
import java.awt.image.BufferedImage;

class Remember
{
    private static int r;
    private static int c;
    public static Image[][] image;
    private Image icon  = new ImageIcon(this.getClass().getResource("white.png")).getImage();
    private Image icon0 = new ImageIcon(this.getClass().getResource("4row_board.png")).getImage();
    private Image icon1 = new ImageIcon(this.getClass().getResource("4row_board_red.png")).getImage();
    private Image icon2 = new ImageIcon(this.getClass().getResource("4row_red.png")).getImage();
    private Image icon3 = new ImageIcon(this.getClass().getResource("4row_board_black.png")).getImage();
    private Image suggestion = new ImageIcon(this.getClass().getResource("yellow.png")).getImage();
    private static Image i;
    private static Image i0;
    private static Image i1;
    private static Image i2;
    private static Image i3;
    private static Image backupSuggestion;
    private static int suggestedRow;
    private static int backupCol;
    private static Image s;
    private Image winRed = new ImageIcon(this.getClass().getResource("win_red.png")).getImage();
    private Image winBlack = new ImageIcon(this.getClass().getResource("win_black.png")).getImage();
    private static Image winr;
    private static Image winb;

    public Remember(int row, int col)
    {
        winr = winRed;
        winb = winBlack;
        s = suggestion;
        i = icon;
        i0 = icon0;
        i1 = icon1;
        i2 = icon2;
        i3 = icon3;
        r = row;
        c = col;

        image = new Image[row+1][col];

        for(int k = 0; k < row + 1; ++k)
        {
            for(int j = 0; j < col; ++j)
            {
                if(k == 0 && j == 0)
                {
                    image[k][j] = icon2;
                }
                else if(k == 0)
                {
                    image[k][j] = icon;
                }
                else if(k > 0)
                {
                    image[k][j] = icon0;
                }
            }
        }
        image[row][0] = suggestion;
        suggestedRow = row;
        backupCol = 0;
    }

    public static void makeFinish(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, Player p)
    {
        if(p == Player.One)
        {
            image[x1+1][y1] = winr;
            image[x2+1][y2] = winr;
            image[x3+1][y3] = winr;
            image[x4+1][y4] = winr;
            return;
        }
        if(p == Player.Two)
        {
            image[x1+1][y1] = winb;
            image[x2+1][y2] = winb;
            image[x3+1][y3] = winb;
            image[x4+1][y4] = winb;
            return;
        }
    }

    public static void modifyImage(int row, int col, Player p)
    {
        if(image[r][0] == s)
            image[r][0] = i0;
        if(p == Player.None)
            throw new Error("Player none");
        // we are moving the disk on the left or right
        if(row == 0)
        {
            if(image[suggestedRow][backupCol] == s)
                image[suggestedRow][backupCol] = backupSuggestion;
            int x = Play.possibleMove(col);
            if(x != -1)
            {
                suggestedRow = x;
                backupSuggestion = image[suggestedRow][col];
                backupCol = col;
                image[suggestedRow][col] = s;
            }
            for(int j = 0; j < c; j++)
                image[0][j] = i;
            if(p == Player.One)
            {
                image[0][col] = i2; 
            }
            else
            {
                image[0][col] = i3;
            }
            
        }
        //the disk must slide down to the available position
        else if(row > 0)
        {
            if(row > 1)
                image[row-1][col] = i0;
            if(p == Player.Two)
            {
                image[row][col] = i1; 
            }
            else
            {
                image[row][col] = i3;
            }
        }
    }
}

