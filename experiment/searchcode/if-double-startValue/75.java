/*
 * TetraFall - a puzzle game
 * Copyright (C) 2007 Jordan Miner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;

class ImageTheme {
	private BufferedImage[] blocks = new BufferedImage[7];
	private BufferedImage[] fullLines = new BufferedImage[5];
	private BufferedImage back;

	// name - the name of this theme
	ImageTheme(String name) throws IOException {
		blocks[TetraFall.I_PIECE] =
			ImageIO.read(new File(name+"_red_block.png"));
		blocks[TetraFall.T_PIECE] =
			ImageIO.read(new File(name+"_green_block.png"));
		blocks[TetraFall.O_PIECE] =
			ImageIO.read(new File(name+"_blue_block.png"));
		blocks[TetraFall.L_PIECE] =
			ImageIO.read(new File(name+"_orange_block.png"));
		blocks[TetraFall.J_PIECE] =
			ImageIO.read(new File(name+"_gray_block.png"));
		blocks[TetraFall.S_PIECE] =
			ImageIO.read(new File(name+"_purple_block.png"));
		blocks[TetraFall.Z_PIECE] =
			ImageIO.read(new File(name+"_turq_block.png"));

		//back = ImageIO.read(new File(name+"_back.png"));

		fullLines[1] = ImageIO.read(new File("single.png"));
		fullLines[2] = ImageIO.read(new File("double.png"));
		fullLines[3] = ImageIO.read(new File("triple.png"));
		fullLines[4] = ImageIO.read(new File("tetris.png"));
	}

	public BufferedImage getBlockImage(int piece) {
		return blocks[piece];
	}

	public BufferedImage getFullLinesImage(int numFullLines) {
		return fullLines[numFullLines];
	}

	public BufferedImage getBackImage() {
		return back;
	}
}

public class TetraFall extends JFrame {
	final static int NULL_PIECE = -1;
	final static int I_PIECE = 0;
	final static int T_PIECE = 1;
	final static int O_PIECE = 2;
	final static int L_PIECE = 3;
	final static int J_PIECE = 4;
	final static int S_PIECE = 5;
	final static int Z_PIECE = 6;
	static ArrayList themes = new ArrayList();
	static ImageTheme theme;

	GameArea gameArea;

	public TetraFall() {
		setResizable(false);
		pack();
		Insets in = getInsets();
		setSize(in.left+in.right+450, in.top+in.bottom+500);
		setLocationRelativeTo(null);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					exit();
				}
		});

		setContentPane(gameArea = new GameArea());
		setTitle("TetraFall 0.3");

		setVisible(true);
	}

	public void exit() {
		dispose();

		// save anything that needs saving

		Runtime.getRuntime().exit(0);
	}

	public static void main(String[] args) {
		try {
			themes.add(theme = new ImageTheme("default"));
			//themes.add(new ImageTheme("glass"));
		} catch(IOException ex) {
			System.out.println(ex);
			System.out.println("error opening image...exiting");
			Runtime.getRuntime().exit(1);
		}
		new TetraFall();
	}

	public static BufferedImage getBlockImage(int piece) {
		return theme.getBlockImage(piece);
	}

	public static BufferedImage getFullLinesImage(int numFullLines) {
		return theme.getFullLinesImage(numFullLines);
	}

	public static BufferedImage getBackImage() {
		return theme.getBackImage();
	}
}

class AnimatedInt {
	int value = 0;
	int startValue, endValue;
	int elapsed, duration;
	boolean animating;

	int get() {
		return value;
	}

	int getEnd() {
		return endValue;
	}

	AnimatedInt set(int newVal) {
		value = newVal;
		endValue = value;
		animating = false;
		return this;
	}

	void animate(int endVal, int dur) {
		animating = true;
		elapsed = 0;
		duration = dur;
		startValue = value;
		endValue = endVal;
	}

	boolean isAnimating() { return animating; }

	void advance(int time) {
		elapsed += time;
		if(!animating)
			return;
		if(elapsed > duration)
			set(endValue);
		else
			value = startValue+(endValue-startValue)*elapsed/duration;
	}

	int getElapsed() {
		return elapsed;
	}

	int getDuration() {
		return duration;
	}
}
class AnimatedFloat {
	float value = 0;
	float startValue, endValue;
	int elapsed, duration;
	boolean animating;
	float get() {
		return value;
	}

	float getEnd() {
		return endValue;
	}

	AnimatedFloat set(float newVal) {
		value = newVal;
		endValue = value;
		animating = false;
		return this;
	}

	void animate(float endVal, int dur) {
		animating = true;
		elapsed = 0;
		duration = dur;
		startValue = value;
		endValue = endVal;
	}

	boolean isAnimating() { return animating; }

	void advance(int time) {
		elapsed += time;
		if(!animating)
			return;
		if(elapsed > duration)
			set(endValue);
		else
			value = startValue+(endValue-startValue)*elapsed/duration;
	}

	int getElapsed() {
		return elapsed;
	}

	int getDuration() {
		return duration;
	}
}

class Piece {
	private int piece;
	private int blocks;
	private int size; // size*size bits are used in the blocks bitfield
	// x=how far from left blocks start, width=blocks wide, height=blocks tall
	private int x, y, width, height;
	final static int[] pieceBlocks = new int[] { 0x0003C00, // I piece blocks
		0xB8, 0x0660, 0x78,   // T, O, L piece blocks
		0x138, 0xF0, 0x198 }; // J, S, Z, piece blocks
	final static int[] pieceSizes = new int[] { 5, 3, 4, 3, 3, 3, 3 };

	// new Piece(TetraFall.O_PIECE)
	Piece(int _piece) {
		piece = _piece;
		reset();
	}

	void reset() {
		blocks = pieceBlocks[piece];
		size = pieceSizes[piece];
		int[][] xywh = new int[][] {
			{ 1, 2, 4, 1 }, { 0, 0, 3, 2 }, { 1, 1, 2, 2 }, { 0, 0, 3, 2 },
			{ 0, 0, 3, 2 }, { 0, 0, 3, 2 }, { 0, 0, 3, 2 }
		};
		x = xywh[piece][0];
		y = xywh[piece][1];
		width = xywh[piece][2];
		height = xywh[piece][3];
	}

	void rotateCW() {
		int rotBlocks = 0;
		for(int i = 0; i < size*size; ++i)
			rotBlocks |=
				(blocks >> (size*size - (i%size+1)*size + i/size) & 1) << i;
		blocks = rotBlocks;

		int x = size-y-height;
		int y = size-x-width;
		int tmp = width;
		width = height;
		height = tmp;
	}

	void rotateCCW() {
		rotateCW();
		rotateCW();
		rotateCW();
	}

	// width in blocks
	int getWidth() { return width; } // TODO: getCols

	// height in blocks
	int getHeight() { return height; } // TODO: getLines()

	int getPivotX() {
		return size*GameArea.BLOCK_SIZE/2-x*GameArea.BLOCK_SIZE;
	}

	int getPivotY() {
		return size*GameArea.BLOCK_SIZE/2-y*GameArea.BLOCK_SIZE;
	}

	// true if piece will fit at col, line without having any blocks
	// off playing area and without overlapping other blocks
	boolean canFit(int[][] lines, int line, int col) {
		col -= x;
		line -= y;
		for(int i = 0; i < size*size; ++i) {
			if((blocks >> i & 1) == 0)
				continue;
			int lineAtI = line+(size*size-1-i)/size;
			int colAtI = col+(size*size-1-i)%size;
			if(lineAtI < 0 || lineAtI >= lines.length)
				return false;
			if(colAtI < 0 || colAtI >= lines[0].length)
				return false;
			if(lines[lineAtI][colAtI] >= 0)
				return false;
		}
		return true;
	}

	// puts the piece on the board
	void affix(int[][] lines, int line, int col) {
		col -= x;
		line -= y;
		for(int i = 0; i < size*size; ++i) {
			if((blocks >> i & 1) == 0)
				continue;
			lines[line+(size*size-1-i)/size][col+(size*size-1-i)%size] = piece;
		}
	}

	// gx and gy are in pixels
	void paint(Graphics g2d, int gx, int gy) {
		gx -= x*GameArea.BLOCK_SIZE;
		gy -= y*GameArea.BLOCK_SIZE;
		for(int i = 0; i < size*size; ++i) {
			if((blocks >> i & 1) == 0)
				continue;
			g2d.drawImage(TetraFall.getBlockImage(piece),
				gx+(size*size-1-i)%size*GameArea.BLOCK_SIZE,
				gy+(size*size-1-i)/size*GameArea.BLOCK_SIZE, null);
		}
	}
}

class GameArea extends JComponent {
	final static int BLOCK_SIZE = 25;
	final static int NUM_COLUMNS = 10;
	final static int NUM_LINES = 20;

	long lastTime;
	// an array of all the rows
	int[][] lines;
	Piece curPiece, nextPiece;
	AnimatedFloat curPieceLine, curPieceCol;
	AnimatedFloat pieceRotation; // TODO: rotationEffect?
	AnimatedFloat fullLinesEffect, fallingBlocksEffect;
	Random rand;
	boolean dropFalling = false;
	boolean paused = false;
	int[] fullLines;

	float score;
	int lineCount, level;

	GameArea() {
		lines = new int[NUM_LINES][];
		for(int i = 0; i < lines.length; ++i)
			lines[i] = new int[NUM_COLUMNS];

		lastTime = System.currentTimeMillis();
		rand = new Random(lastTime);
		curPieceLine = new AnimatedFloat();
		curPieceCol = new AnimatedFloat();
		pieceRotation = new AnimatedFloat();
		fullLinesEffect = new AnimatedFloat();
		fallingBlocksEffect = new AnimatedFloat();
		initGame();

		// level 1 = 1100
		// level 40 = 100
		int[] levels = new int[] { 1, 5, 10, 20, 30, 40, 50, 60, 100 };
		System.out.println("Speed (lower is faster):");
		for(int i = 0; i < levels.length; ++i) {
			level = levels[i];
			System.out.println("at level "+level+": "+getFallSpeed());
		}
		level = 1;

		setFocusable(true);
		requestFocus();
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == e.VK_LEFT) {
					moveCurrentPieceLeft();
				} else if(e.getKeyCode() == e.VK_RIGHT) {
					moveCurrentPieceRight();
				} else if(e.getKeyCode() == e.VK_UP ||
						e.getKeyCode() == e.VK_X) {
					rotateCurrentPieceCW();
				} else if(e.getKeyCode() == e.VK_Z) {
					rotateCurrentPieceCCW();
				} else if(e.getKeyCode() == e.VK_DOWN) {
					if(curPiece == null)
						return;
					if(dropFalling == false) {
						dropFalling = true;
						updateFallAnimation();
					}
				} else if(e.getKeyCode() == e.VK_P ||
						e.getKeyCode() == e.VK_PAUSE) {
					if(paused)
						unpause();
					else
						pause();
				}
			}
			public void keyReleased(KeyEvent e) {
				if(curPiece == null)
					return;
				if(e.getKeyCode() == e.VK_DOWN) {
					dropFalling = false;
					updateFallAnimation();
				}
			}
		});
	}

	void initGame() {
		score = 0;
		lineCount = 0;
		level = 1;
		clearBoard();
		curPiece = null;
		nextPiece = new Piece(rand.nextInt(7));
		dropFalling = false;
		finishPiece();
	}

	void gameOver() {
		pause();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				int option = JOptionPane.showConfirmDialog(GameArea.this,
					"Game Over\n\nYou have lost.\n\nWould you like to play again?", "TetraFall", JOptionPane.YES_NO_OPTION);
				if(option == 1)
					Runtime.getRuntime().exit(0);
				initGame();
				unpause();
			}
		});
	}

	protected void clearBoard() {
		for(int line = 0; line < lines.length; ++line)
			for(int col = 0; col < lines[line].length; ++col)
				lines[line][col] = TetraFall.NULL_PIECE;
	}

	// returns the milliseconds to fall the distance of one block
	// the lower the number, the faster it falls
	int getFallSpeed() {
		if(dropFalling)
			return 25;

		// level 1: 1100
		// level 30: 150
		final int startSpeed = 1100;
		final int highLevel = 30;
		final int highSpeed = 150;
		final float xshift = (highLevel*highLevel*highSpeed-startSpeed) /
			(float)(startSpeed-highSpeed);
		final float amp = startSpeed + startSpeed*xshift;
		// Witch of Agnesi curve
		return (int) (amp/(level*level+xshift));
		// gets harder more quickly the first few levels
		// gets harder slowly at higher levels

		/* OLD...linear
			Speed (lower is faster):
			at level 1: 1076
			at level 5: 980
			at level 10: 860
			at level 20: 620
			at level 30: 380
			at level 40: 140
			at level 50: 110
			at level 60: 110
		*/
		/*final int slowestSpeed = 1100;
		final int topOutLevel = 40;
		final int fastestSpeed = 110;

		final int multiplier = (slowestSpeed-fastestSpeed)/topOutLevel;
		return Math.max(slowestSpeed-level*multiplier, fastestSpeed);*/
	}

	// gets the lowest line the current piece can go
	// this cannot be higher than where the piece currently is
	protected int getStopLine() {
		for(int line = (int)curPieceLine.get()+1; line <= lines.length; ++line)
			if(!curPiece.canFit(lines, line, (int)curPieceCol.getEnd()))
				return line - 1;
		throw new RuntimeException("stop line not found...this should not happen");
	}

	protected void advanceState(int time) {
		pieceRotation.advance(time);
		curPieceLine.advance(time);
		curPieceCol.advance(time);
		fullLinesEffect.advance(time);
		fallingBlocksEffect.advance(time);

		final float pointsPerLineDropped = 0.25f;
		if(dropFalling && curPieceLine.isAnimating()) // only if piece is moving
			score += pointsPerLineDropped * time/getFallSpeed();

		if(!fullLinesEffect.isAnimating()) {
			if(curPiece == null)
				nextPiece();
			// if 500 ms after the piece stopped moving
			if(curPieceLine.getElapsed()-curPieceLine.getDuration() > 500)
				finishPiece();
		}
	}

	void finishPiece() {
		if(curPiece == null)
			return;

		curPiece.affix(lines, (int)curPieceLine.get(), (int)curPieceCol.getEnd());
		curPiece = null;

		// check for game over
		for(int line = 0; line < 2; ++line) {
			for(int i = 0; i < lines[line].length; ++i) {
				if(lines[line][i] >= 0) {
					gameOver();
					return;
				}
			}
		}

		//check for full lines
		fullLines = findFullLines();
		lineCount += fullLines.length;
		final int[] scoreValues = new int[] { 0, 10, 50, 200, 500 };
		score += scoreValues[fullLines.length];

		level = lineCount/6+1; // TODO

		if(fullLines.length == 0)
			fullLines = null;
		else
			fullLinesEffect.set(0).animate(1f, 400);
	}

	void nextPiece() {
		if(fullLines != null) {
			for(int i = 0; i < fullLines.length; ++i) {
				int[] fullLine = lines[fullLines[i]];
				// shift lines down
				for(int j = fullLines[i]-1; j >= 0; --j) {
					lines[j+1] = lines[j];
				}
				lines[0] = fullLine;
				// clear line
				for(int j = 0; j < fullLine.length; ++j)
					fullLine[j] = TetraFall.NULL_PIECE;
			}
			fullLines = null;
		}

		curPiece = nextPiece;
		nextPiece = new Piece(rand.nextInt(7));
		curPieceLine.set(0);
		curPieceCol.set(3);

		updateFallAnimation();
	}

	// whenever the current piece is rotated, moved left, or moved right,
	// this needs to be called to update how far it falls
	void updateFallAnimation() {
		int stopLine = getStopLine();
		int time = (int)((stopLine - curPieceLine.get()) * getFallSpeed());
		curPieceLine.animate(stopLine, time);
	}

	void moveCurrentPieceLeft() {
		if(paused || curPiece == null)
			return;
		if(curPieceLine.get() - (int)curPieceLine.get() < 0.5 &&
				!curPiece.canFit(lines,
					(int)curPieceLine.get(), (int)curPieceCol.getEnd()-1))
			return;
		if(!curPiece.canFit(lines, (int)Math.ceil(curPieceLine.get()),
				(int)curPieceCol.getEnd()-1))
			return;
		curPieceCol.animate(curPieceCol.getEnd()-1, 100);
		updateFallAnimation();
	}

	void moveCurrentPieceRight() {
		if(paused || curPiece == null)
			return;
		if(curPieceLine.get() - (int)curPieceLine.get() < 0.5 &&
				!curPiece.canFit(lines,
					(int)curPieceLine.get(), (int)curPieceCol.getEnd()+1))
			return;
		if(!curPiece.canFit(lines, (int)Math.ceil(curPieceLine.get()),
				(int)curPieceCol.getEnd()+1))
			return;
		curPieceCol.animate(curPieceCol.getEnd()+1, 100);
		updateFallAnimation();
	}

	void rotateCurrentPieceCW() {
		if(paused || curPiece == null)
			return;
		curPiece.rotateCW();
		if(!curPiece.canFit(lines,
				(int)curPieceLine.get()+1,
				(int)curPieceCol.getEnd())) {
			curPiece.rotateCCW();
			return;
		}
		pieceRotation.set((float)-Math.PI/2).animate(0, 100);
		updateFallAnimation();
	}

	void rotateCurrentPieceCCW() {
		if(paused || curPiece == null)
			return;
		curPiece.rotateCCW();
		if(!curPiece.canFit(lines,
				(int)curPieceLine.get()+1,
				(int)curPieceCol.getEnd())) {
			curPiece.rotateCW();
			return;
		}
		pieceRotation.set((float)Math.PI/2).animate(0, 100);
		updateFallAnimation();
	}

	// returns the indexes of full lines
	// the higher lines appear first in the returned array
	int[] findFullLines() {
		int[] arr = new int[4];  // I _hate_ Java
		int count = 0;
		loop:
		for(int line = 0; line < lines.length; ++line) {
			for(int col = 0; col < lines[line].length; ++col) {
				if(lines[line][col] < 0)
					continue loop;
			}
			arr[count++] = line;
		}
		int[] arr2 = new int[count];
		System.arraycopy(arr, 0, arr2, 0, count);
		return arr2;
	}



	void pause() { paused = true; }

	void unpause() {
		if(!paused)
			return;
		// don't update for the time it has been paused!
		lastTime = System.currentTimeMillis();
		paused = false;
		repaint(); // get painting going again
	}

	public static void paintStringCentered(Graphics2D g2d, String str,
			int x, int y, int width, int height) {
		Rectangle2D rect = g2d.getFont().getStringBounds(str, g2d.getFontRenderContext());
		g2d.drawString(str, x+width/2-(int)rect.getWidth()/2, y+height/2-(int)rect.getHeight()/2);
	}

	// TODO: figure something out with background
	boolean paintBack = false;
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g.create();
		g2d.fillRect(0, 0, getWidth(), getHeight());

		long curTime;
		while(!paused &&
				(curTime = System.currentTimeMillis())-lastTime >= 20) {
			lastTime += 20;
			advanceState(20);
		}

		g2d.setComposite(AlphaComposite.Src);
		if(paintBack) {
			g2d.drawImage(TetraFall.getBackImage(), 0, 0, null);
		}
		g2d.setPaintMode();

		paintLeftSide(g2d, 50, getHeight());
		g2d.translate(50, 0);
		paintBoard(g2d, 250, getHeight());
		g2d.translate(250, 0);
		paintRightSide(g2d, getWidth()-300, getHeight());

		if(!paused)
			repaint();
	}

	protected void paintBoard(Graphics2D g2d, int width, int height) {
		for(int line = 0; line < lines.length; ++line) {
			for(int col = 0; col < lines[line].length; ++col) {
				if(lines[line][col] >= 0)
					g2d.drawImage(TetraFall.getBlockImage( lines[line][col] ),
						col * BLOCK_SIZE, line * BLOCK_SIZE,
						null);
			}
		}

		// full lines glow
		if(fullLines != null) {
			float alpha = fullLinesEffect.get();
			g2d.setColor(new Color(255, 255, 255, (int)(alpha*255)));
			for(int i = 0; i < fullLines.length; ++i)
				g2d.fillRect(0, fullLines[i]*BLOCK_SIZE, width, BLOCK_SIZE);
		}

		// lose zone (two two lines)
		g2d.setColor(new Color(255, 255, 255, 30));
		g2d.fillRect(0, 0, width, 2 * BLOCK_SIZE);

		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		if(curPiece != null) {
			g2d.translate(
				curPieceCol.get()*BLOCK_SIZE, curPieceLine.get()*BLOCK_SIZE);
			g2d.rotate(pieceRotation.get(),
				curPiece.getPivotX(), curPiece.getPivotY());

			curPiece.paint(g2d, 0, 0);

			g2d.rotate(-pieceRotation.get(),
				curPiece.getPivotX(), curPiece.getPivotY());
			g2d.translate(
				-curPieceCol.get()*BLOCK_SIZE, -curPieceLine.get()*BLOCK_SIZE);
		}

		// Single, Double, Triple, Tetris
		if(fullLines != null) {
			BufferedImage img = TetraFall.getFullLinesImage(fullLines.length);
			float alpha = fullLinesEffect.get();
			if(alpha < 0.33)
				alpha = alpha * 3; // 1 / 0.33 = 5
			else if(alpha > 0.8)
				alpha = (1-alpha) * 5;
			else
				alpha = 1;
			g2d.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, alpha ));
			g2d.drawImage(img, (width-img.getWidth())/2,
				fullLines[0]*BLOCK_SIZE-img.getHeight(), null);
			g2d.setPaintMode();
		}

		if(paused) {
			g2d.setColor(Color.WHITE);
			g2d.setFont(new Font("dialog", 0, 20));
			paintStringCentered(g2d, "Paused", 0, 0, width, height);
		}
	}

	final static Color sideColor = new Color(0, 40, 130);
	final static Color textColor = new Color(240, 240, 240);
	final static Font titleFont = new Font("dialog", Font.BOLD, 15);
	final static Font statsFont = new Font("dialog", 0, 14);
	protected void paintLeftSide(Graphics2D g2d, int width, int height) {
		if(!paintBack) {
			g2d.setColor(sideColor);
			g2d.fillRect(0, 0, width, height);
		}
	}
	final static int sides = 6;
	static Point[] pts = new Point[sides];
	static {
		for(int i = 0; i < pts.length; ++i)
			pts[i] = new Point();
	}
	protected void paintRightSide(Graphics2D g2d, int width, int height) {
		if(!paintBack) {
			g2d.setColor(sideColor);
			g2d.fillRect(0, 0, width, height);
		}

		nextPiece.paint(g2d, width/2-50, 10);

		g2d.setColor(textColor);
		g2d.setFont(titleFont);
		g2d.drawString("Score", 10, 150);
		g2d.drawString("Lines", 10, 200);
		g2d.drawString("Level", 10, 250);

		g2d.setFont(statsFont);
		g2d.drawString(Integer.toString((int)score), 40, 170);
		g2d.drawString(Integer.toString(lineCount), 40, 220);
		g2d.drawString(Integer.toString(level), 40, 270);

		final boolean CUSTOMIZE = false;
		String user = System.getProperty("user.name").toLowerCase();
		if(CUSTOMIZE && (user.indexOf("peter") != -1 || user.indexOf("dalton") != -1)) {
			paintStringCentered(g2d, "Rabbi edition", 0, height-130, width, 20);

			int radius = 50;
			for(int i = 0; i < sides; ++i) {
				double arcDist = 3.14159*2*i/sides;
				pts[i].setLocation(
					(int)Math.round(width/2 + radius*Math.sin(arcDist)),
					(int)Math.round(height-70 - radius*Math.cos(arcDist)));
			}

			int opp;
			for(int i = 0; i < pts.length; ++i) {
				opp = i + sides/2 + 1;
				if(opp >= pts.length)
					opp -= sides;
				g2d.drawLine(pts[i].x, pts[i].y, pts[opp].x, pts[opp].y);
			}
		}
		if(CUSTOMIZE && (user.indexOf("matt") != -1 || user.indexOf("stanley") != -1)) {
			paintStringCentered(g2d, "Corn-shucker edition", 0, height-130, width, 20);
		}
	}
}


