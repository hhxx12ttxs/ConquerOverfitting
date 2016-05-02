package com.life.canvas;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import com.life.cell.Cell;

public class CellCanvas extends JPanel implements MouseListener {
	private static final long serialVersionUID = 1L;
	
	// The amount of cells in a horizontal line across the canvas.
	private static final int boardWidth = 40;
	// The amount of cells in a vertical line down the canvas.
	private static final int boardHeight = 25;
	
	private Cell [][] cells;
	
	private int generations;
	public int getGenerations() { return this.generations; }

	public CellCanvas() {
		// Initialize the cell grid.
		cells = new Cell[boardHeight][boardWidth];
		
		// Initialize each individual cell.
		for(int rows = 0;rows < boardHeight;rows++) {
			for(int cols = 0;cols < boardWidth;cols++) {
				int x = (cols * 10);
				int y = (rows * 10);
				cells[rows][cols] = new Cell(new Rectangle2D.Double(x, y, 10, 10));
			}
		}
		
		// Set the initial generation count to 0.
		generations = 0;
		
		setFocusable(true);
		addMouseListener(this);
	}
	
	public void paintComponent(Graphics g) {
		// Clear the canvas.
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		
		// Draw the cell squares.
		for(int rows = 0;rows < boardHeight;rows++) {
			for(int cols = 0;cols < boardWidth;cols++) {
				Cell current = cells[rows][cols];
				g2d.setPaint((current.isAlive())?Color.YELLOW:Color.DARK_GRAY);
				g2d.fill(current.getBounds());
			}
		}
		
		// Draw the grid lines.
		g2d.setPaint(Color.BLACK);
		for(int rows = 1;rows < boardHeight;rows++) {
			for(int cols = 1;cols < boardWidth;cols++) {
				int x1, x2;
				x1 = x2 = (cols * 10);
				int y1 = 0;
				int y2 = (boardHeight * 10);
				g2d.drawLine(x1, y1, x2, y2);
			}
			int x1 = 0;
			int x2 = (boardWidth * 10);
			int y1, y2;
			y1 = y2 = (rows * 10);
			g2d.drawLine(x1, y1, x2, y2);
		}
	}

	@Override
	public void mouseClicked(MouseEvent m) {
		int mouseX = m.getX();
		int mouseY = m.getY();
		
		// Check to see if any cells were clicked.
		for(int rows = 0;rows < boardHeight;rows++) {
			for(int cols = 0;cols < boardWidth;cols++) {
				Cell current = cells[rows][cols];
				if(current.getBounds().contains(mouseX, mouseY)) {
					// If the cell was clicked, toggle it.
					current.setAlive(((current.isAlive())?false:true));
				}
			}
		}
		
		repaint();
	}
	
	public void nextGeneration() {
		generations++;
		
		// Count the neighbors for each cell.
		for(int rows = 0;rows < boardHeight;rows++) {
			for(int cols = 0;cols < boardWidth;cols++) {
				Cell current = cells[rows][cols];
				current.setNeighbors(neighborCount(rows,cols));
			}
		}
		
		// Apply game logic to each cell.
		for(int rows = 0;rows < boardHeight;rows++) {
			for(int cols = 0;cols < boardWidth;cols++) {
				Cell current = cells[rows][cols];
				int neighbors = current.getNeighbors();
				if(((neighbors != 2)&&(neighbors != 3))&&(current.isAlive())) {
					// Either has one or no neighbors, or four or more neighbors. Cell dies.
					current.setAlive(false);
					continue; // This ensures that the cell is not checked again.
				}
				if((neighbors == 3)&&(!current.isAlive())) {
					// Dead cell has exactly three neighbors. Cell is created.
					current.setAlive(true);
					continue; // This ensures that the cell is not checked again.
				}
			}
		}
	}
	
	public void clearCanvas() {
		generations = 0;
		
		// Reset all cells to dead.
		for(int rows = 0;rows < boardHeight;rows++) {
			for(int cols = 0;cols < boardWidth;cols++) {
				Cell current = cells[rows][cols];
				current.setAlive(false);
			}
		}
	}
	
	public int neighborCount(int row, int col) {
		int neighbors = 0;
		
		// Check each of the eight positions surrounding the cell.
		
		// Check the Top-Left (-1,-1)
		if((!((row - 1) < 0))&&(!((col - 1) < 0)))
			if(cells[row - 1][col - 1].isAlive()) neighbors++;
		// Check the Top-Center (0,-1)
		if(!((row - 1) < 0))
			if(cells[row - 1][col].isAlive()) neighbors++;
		// Check the Top-Right (+1,-1)
		if((!((row - 1) < 0))&&(!((col + 1) > (boardWidth - 1))))
			if(cells[row - 1][col + 1].isAlive()) neighbors++;
		// Check the Center-Right (+1,0)
		if(!((col + 1) > (boardWidth - 1)))
			if(cells[row][col + 1].isAlive()) neighbors++;
		// Check the Bottom-Right (+1,+1)
		if((!((row + 1) > (boardHeight - 1)))&&(!((col + 1) > (boardWidth - 1))))
			if(cells[row + 1][col + 1].isAlive()) neighbors++;
		// Check the Bottom-Center (0,+1)
		if(!((row + 1) > (boardHeight - 1)))
			if(cells[row + 1][col].isAlive()) neighbors++;
		// Check the Bottom-Left (-1,+1)
		if((!((row + 1) > (boardHeight - 1)))&&(!((col - 1) < 0)))
			if(cells[row + 1][col - 1].isAlive()) neighbors++;
		// Check the Center-Left (-1,0)
		if(!((col - 1) < 0))
			if(cells[row][col - 1].isAlive()) neighbors++;
		
		return neighbors;
	}

	@Override
	public void mouseEntered(MouseEvent m) {
	}
	@Override
	public void mouseExited(MouseEvent m) {
	}
	@Override
	public void mousePressed(MouseEvent m) {
	}
	@Override
	public void mouseReleased(MouseEvent m) {
	}
}

