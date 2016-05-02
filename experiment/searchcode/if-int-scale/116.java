package com.tetris;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import com.tetris.game.Piece;
import com.tetris.game.TetrisGrid;
import com.tetris.game.drawing.Color;
import com.tetris.game.drawing.Rectangle;
import com.tetris.game.drawing.Window;

public class CanvasWindow extends Canvas implements Window {
    private static final int SCALE = 1;
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final String NAME = "Tetris";
    
    private static final int SCALED_WIDTH = WIDTH * SCALE;
    private static final int SCALED_HEIGHT = HEIGHT * SCALE;
    
    
    private BufferedImage image;
    
    public CanvasWindow() {
        setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));

        JFrame frame = new JFrame(NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public void reset() {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public void drawGrid(TetrisGrid grid) {
        // TODO Auto-generated method stub
    }

    @Override
    public void drawCurrentPiece(Piece currentPiece) {
        if (currentPiece == null) {
            return;
        }
        
        int[] pixels = currentPiece.draw();
        Rectangle rect = Rectangle.fromOriginDimensions(currentPiece.getOrigin(), currentPiece.getWidth(), currentPiece.getHeight());
        overwrite(pixels, rect);
    }

    /**
     * Replace the given rectangle with the given pixels.
     * 
     * Ignores any pixels where alpha > 0. (No alpha blending YET) 
     */
    private void overwrite(int[] pixels, Rectangle rect) {
        int width = rect.getWidth();
        int right = rect.getRight();
        int bottom = rect.getBottom();
        for (int y = rect.getTop(); y < bottom; ++y) {
            for (int x = rect.getLeft(); x < right; ++x) {
                int rgba = pixels[x + (y * width)];
                if ((rgba & Color.ALPHA_MASK) > 0) {
                    continue; // Later, we can talk alpha blending if we want to be fancy
                }
                
                image.setRGB(x, y, rgba);
            }
        }
    }

    @Override
    public void drawNextPiece(Piece nextPiece) {
        // TODO Auto-generated method stub
    }

    @Override
    public void drawScore(int currentScore) {
        // TODO Auto-generated method stub
    }

    @Override
    public void updateBuffer() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            requestFocus();
            return;
        }
        
        Graphics g = bs.getDrawGraphics();
        g.fillRect(0, 0, getWidth(), getHeight());

        g.drawImage(image, 0, 0, SCALED_WIDTH, SCALED_HEIGHT, null);
        g.dispose();
        bs.show();
    
        clear();
    }

    /**
     * Clear the image to black
     */
    private void clear() {
        image.setRGB(0, 0, WIDTH, HEIGHT, new int[WIDTH * HEIGHT], 0, WIDTH);
    }
}

