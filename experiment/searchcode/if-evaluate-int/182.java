/*
 * Tic Tac Toe 3D
 * Copyright (C) 2008  Mathias Stephan Panzenb??ck
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package tictactoe3d.gamelogic.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import tictactoe3d.gamelogic.Cell;
import tictactoe3d.gamelogic.Game;
import tictactoe3d.gamelogic.GameActionListener;
import tictactoe3d.gamelogic.Player;

/**
 * @author panzi
 */
public class DefaultComputerEnemy extends AbstractComputerEnemy {
	private static Random random = new Random();
	
	private CellInfo[][][] cells     = new CellInfo[4][4][4];
	private List<CellInfo> openCells = new ArrayList<CellInfo>(4 * 4 * 4);
	
	private GameActionListener listener = new GameActionListener() {
		public void join(String userName, Player player) {}

		public void move(int x, int y, int z, Player player) {
			get(x, y, z).player = player;

			if (player == other) {
				if (!evaluate(x, y, z)) {
					DefaultComputerEnemy.this.move();
				}
			}
			else {
				evaluate(x, y, z);
			}
		}

		public void draw() {}

		public void winner(Player player, Cell[] line) {}

		public void reset() {}

		public void leave(Player player) {}
	};
	
	public DefaultComputerEnemy() {
		for (int z = 0; z < cells.length; ++ z) {
			CellInfo[][] field = cells[z];
			for (int y = 0; y < field.length; ++ y) {
				CellInfo[] line = field[y];
				for (int x = 0; x < line.length; ++ x) {
					line[x] = new CellInfo(x, y, z);
					openCells.add(line[x]);
				}
			}
		}
	}
	
	@Override
	public void init(Game game, Player self, String name) {
		super.init(game, self, name);
		game.addGameActionListener(listener);
	}
	
	@Override
	public void reset() {
		openCells.clear();
		for (CellInfo[][] field : cells) {
			for (CellInfo[] line : field) {
				for (CellInfo info : line) {
					info.clear();
					openCells.add(info);
				}
			}
		}
	}
	
	@Override
	public void join() {
		game.join(name, self);
	}
	
	@Override
	public void leave() {
		super.leave();
		game.removeGameActionListener(listener);
	}
	
	@Override
	public void move() {
		List<CellInfo> best = findBestCells();
		int n = best.size();
		
		if (n > 0) {
			int index = n > 1 ? random.nextInt(n) : 0;
			Cell cell = best.get(index).cell;
			game.move(cell.x, cell.y, cell.z, self);
		}
	}
	
	protected List<CellInfo> openCells() {
		for (int i = 0; i < openCells.size();) {
			if (openCells.get(i).player != null) {
				openCells.remove(i);
			}
			else {
				++ i;
			}
		}
		
		return openCells;
	}
	
	protected List<CellInfo> findBestCells() {
		List<CellInfo> infos = openCells();
		List<CellInfo> best  = new ArrayList<CellInfo>(infos.size());
		
		if (infos.size() > 0) {
			CellInfo bestInfo = infos.get(0);
			best.add(bestInfo);
			
			for (int i = 1; i < infos.size(); ++ i) {
				CellInfo info = infos.get(i);
				int n = bestInfo.compareTo(info);
				
				if (n == 0) {
					best.add(info);
				}
				else if (n > 0) {
					best.clear();
					best.add(info);
					bestInfo = info;
				}
			}
		}
		
		return best;
	}
	
	private CellInfo get(int x, int y, int z) {
		return cells[z][y][x];
	}
	
	private CellInfo get(Cell cell) {
		return cells[cell.z][cell.y][cell.x];
	}
	
	private boolean evaluate(int x, int y, int z, int lineId) {
		CellInfo newInfo = get(x, y, z);
		Cell[]   line    = Cell.buildLine(x, y, z, lineId);
		int defensive = 0;
		int offensive = 0;
		
		if (newInfo.player == self) {
			int eval = newInfo.evaluation[CellInfo.OFFENSIVE][lineId];
			if (eval >= 0) {
				offensive = eval + 1;
			}
			defensive = -1;
		}
		else if (newInfo.player == other) {
			int eval = newInfo.evaluation[CellInfo.DEFENSIVE][lineId];
			if (eval >= 0) {
				defensive = eval + 1;
			}
			offensive = -1;
		}
		
		for (Cell cell : line) {
			CellInfo info = get(cell);
			info.evaluation[CellInfo.DEFENSIVE][lineId] = defensive;
			info.evaluation[CellInfo.OFFENSIVE][lineId] = offensive;
		}
		
		// if count == 4 then a line is complete -> game over
		return defensive == 4 || offensive == 4;
	}
	
	private boolean evaluate(int x, int y, int z) {
		// axial lines:
		
		// x-axis:
		if (evaluate(x, y, z, Cell.AXIS_X))
			return true;
		
		// y-axis:
		if (evaluate(x, y, z, Cell.AXIS_Y))
			return true;
		
		// z-axis:
		if (evaluate(x, y, z, Cell.AXIS_Z))
			return true;
		
		// 2d diagonal lines:
		
		// x/y-axis:
		if (x == y) {
			if (evaluate(x, y, z, Cell.DIAG_XY1))
				return true;
		}
		
		if (x == 3 - y) {
			if (evaluate(x, y, z, Cell.DIAG_XY2))
				return true;
		}
		
		// x/z-axis:
		if (x == z) {
			if (evaluate(x, y, z, Cell.DIAG_XZ1))
				return true;
		}
		
		if (x == 3 - z) {
			if (evaluate(x, y, z, Cell.DIAG_XZ2))
				return true;
		}
		
		// y/z-axis:
		if (y == z) {
			if (evaluate(x, y, z, Cell.DIAG_YZ1))
				return true;
		}
		
		if (y == 3 - z) {
			if (evaluate(x, y, z, Cell.DIAG_YZ2))
				return true;
		}
		
		// 3d diagonal lines:
		
		if (x == y && y == z) {
			if (evaluate(x, y, z, Cell.DIAG_XYZ1))
				return true;
		}
		
		if (x == y && y == 3 - z) {
			if (evaluate(x, y, z, Cell.DIAG_XYZ2))
				return true;
		}
		
		if (x == 3 - y && x == 3 - z) {
			if (evaluate(x, y, z, Cell.DIAG_XYZ3))
				return true;
		}
		
		if (3 - y == x && x == z) {
			return evaluate(x, y, z, Cell.DIAG_XYZ4);
		}
		
		return false;
	}
	
	public void print() {
		System.out.println("defensive:");
		print(CellInfo.DEFENSIVE);
		System.out.println("offensive:");
		print(CellInfo.OFFENSIVE);
		System.out.println();
	}
	
	public void print(int what) {
		for (CellInfo[][] field : cells) {
			for (CellInfo[] line : field) {
				for (CellInfo cell : line) {
					System.out.printf("%s ",
							Arrays.toString(cell.evaluation[what]));
				}
				System.out.println();
			}
			System.out.println();
		}
		System.out.println();
	}
}

