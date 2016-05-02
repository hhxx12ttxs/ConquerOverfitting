package com.bibodha.magnetfactory.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import android.util.Log;

public class MagnetFactoryModel
{
	Piece[][] _board = null;
	int _currentPieceVal = 0;
	boolean _falling = false;
	int _score = 0;
	int _lastRow = 0;
	Piece _currentPiece = null;

	double DROP_RATE = 60;
	double _countDownUntilDrop = DROP_RATE;
	double THRESHOLD = 10;

	final int HEIGHT = 15;
	int _currentRow = HEIGHT - 1;
	
	int _dropRow1 = 0;
	int _dropRow2 = 0;
	
	ArrayList<Integer> testArray = new ArrayList<Integer>();
//	int[] testArray = { 1, 1, 2, 2, 3, 3, 2, 2, 2, 2 };
	int testIdx = 0;
	
	boolean _gameOver = false;
	
	private OnGameStatusListener listener = null;
	
	public MagnetFactoryModel()
	{
		testArray.add(1);
		testArray.add(1);
		testArray.add(1);
		testArray.add(2);
		testArray.add(2);
		testArray.add(2);
		testArray.add(3);
		testArray.add(3);
		testArray.add(3);
		Collections.shuffle(testArray);
		_board = new Piece[HEIGHT][11];
		getLastRow();
	}

	private void getLastRow()
	{
		int i = 0;
		while (_board[i][5] != null && _board[i][5].getFalling() == false)
		{
			i++;
		}
		_lastRow = i;
	}

	public void update()
	{
		_countDownUntilDrop--;
		if (_countDownUntilDrop < 0)
		{
			validate();
			// on every iteration, move the piece down 1.
			if (_falling)
			{
				if (_currentRow > _lastRow)
				{
					_board[_currentRow - 1][5] = _board[_currentRow][5];
					_board[_currentRow][5] = null;
					_currentRow--;

					if (_currentRow == _lastRow)
					{
						_board[_currentRow][5].setPos(_currentRow, 5);

						_board[_currentRow][5].setNextPiece(_board[_currentRow][6]);
						if (_board[_currentRow][5].getNextPiece() != null)
							_board[_currentRow][5].getNextPiece().setPrevPiece(_board[_currentRow][5]);

						_board[_currentRow][5].setPrevPiece(_board[_currentRow][4]);
						if (_board[_currentRow][5].getPrevPiece() != null)
							_board[_currentRow][5].getPrevPiece().setNextPiece(_board[_currentRow][5]);

						_board[_currentRow][5].setFalling(false);
						_falling = false;
						_currentRow = HEIGHT - 1;
						getLastRow();

					}
				}
				else
				{

					_board[_currentRow][5].setNextPiece(_board[_currentRow][6]);
					if (_board[_currentRow][5].getNextPiece() != null)
						_board[_currentRow][5].getNextPiece().setPrevPiece(_board[_currentRow][5]);

					_board[_currentRow][5].setPrevPiece(_board[_currentRow][4]);
					if (_board[_currentRow][5].getPrevPiece() != null)
						_board[_currentRow][5].getPrevPiece().setNextPiece(_board[_currentRow][5]);
					_falling = false;
					_board[_currentRow][5].setFalling(false);
					_currentRow = HEIGHT - 1;
				}
			}
			else
			{
				if(_board[HEIGHT-1][5] != null)
				{
					_gameOver = true;
					if(listener != null)
						listener.onGameOver(true, _score);
				}
				// add another piece to the top and repeat.
				Piece piece = new Piece();
				// set the value to either middle, left or right. (randomly)
				int pieceValue = (int) ((Math.random() * 3) + 1);
				if(testIdx > testArray.size()-1)
				{
					Collections.shuffle(testArray);
					testIdx = 0;
				}
				pieceValue = testArray.get(testIdx++);
				_currentPieceVal = pieceValue;
				piece.setValue(pieceValue);

				// set falling to true.
				piece.setFalling(true);
				_falling = true;

				// set complete to false.
				piece.setComplete(false);

				_currentPiece = piece;
				// add piece to the top of the board, in the middle [10][6].
				_board[HEIGHT - 1][5] = piece;
			}
			// _countDownUntilDrop = DROP_RATE / (1*DECREASE_RATE);
			if (DROP_RATE < THRESHOLD)
				DROP_RATE = THRESHOLD;
			else
				DROP_RATE = DROP_RATE / (1.01);
			_countDownUntilDrop = DROP_RATE;
		}
	}

	private void validate()
	{
		int tempScore = 0;
		Vec2 start = null;
		Vec2 end = null;
		for (int i = 0; i < HEIGHT; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				if (_board[i][j] != null)
				{
					if (_board[i][j].getValue() == 1)
					{
						Stack<Piece> stack = new Stack<Piece>();
						start = _board[i][j].getPos();
						Piece p = _board[i][j];
						stack.push(p);
						while (p.getNextPiece() != null && p.getNextPiece().getValue() == 2)
						{
							p = p.getNextPiece();
							tempScore += 2;
						}
						if (p.getNextPiece() != null && p.getNextPiece().getValue() == 3)
						{
							Piece firstPiece = stack.pop();
							Piece lastPiece = p.getNextPiece();
							end = p.getNextPiece().getPos();

							int row = (int) firstPiece.getPos().x;
							int firstCol = (int) firstPiece.getPos().y;
							int lastCol = (int) lastPiece.getPos().y;

							if (_board[row][firstCol].getPrevPiece() != null)
							{
								_board[row][firstCol].getPrevPiece().setNextPiece(_board[row + 1][firstCol]);
								if (_board[row + 1][firstCol] != null)
								{
									_board[row + 1][firstCol].setPrevPiece(_board[row][firstCol].getPrevPiece());
								}
							}

							if (_board[row][lastCol].getNextPiece() != null)
							{
								_board[row][lastCol].getNextPiece().setPrevPiece(_board[row + 1][lastCol]);
								if (_board[row + 1][lastCol] != null)
									_board[row + 1][lastCol].setNextPiece(_board[row][lastCol].getNextPiece());
							}
							shiftDown((int) start.x, (int) start.y, (int) end.y);
							tempScore += 2;
							validate();
						}
						else
							tempScore = 0;
					}
					_score += tempScore;
				}
			}
		}
	}

	private void shiftDown(int startRow, int startColumn, int endColumn)
	{
		Vec2 end = new Vec2(startRow, endColumn);
		Piece p = _board[startRow][startColumn];
		int col = (int) p.getPos().y;

		while (!p.getPos().equals(end))
		{
			col = (int) p.getPos().y;
			int i = startRow;
			do
			{
				_board[i][col] = _board[i + 1][col];
				if (_board[i][col] != null)
					_board[i][col].setPos(i, col);
				i++;
			}
			while (_board[i][col] != null);
			p = p.getNextPiece();
		}

		col = (int) p.getPos().y;
		int i = startRow;

		do
		{
			_board[i][col] = _board[i + 1][col];
			if (_board[i][col] != null)
				_board[i][col].setPos(i, col);
			i++;
		}
		while (_board[i][col] != null);
		getLastRow();
	}

	public Piece[][] getBoard()
	{
		return _board;
	}

	public int getScore()
	{
		return _score;
	}

	public void shiftRight()
	{
		synchronized (_board)
		{
			Piece[] temp = new Piece[HEIGHT];
			if (_board[_currentRow][4] == null)
			{
				for (int i = 0; i < HEIGHT; i++)
				{
					if (_board[i][10] != null)
						_board[i][10].setPos(i, 0);
					temp[i] = _board[i][10];
					_board[i][10] = null;
				}
				for (int i = 0; i < HEIGHT - 1; i++)
				{
					for (int j = 10; j > 0; j--)
					{
						if (_board[i][j - 1] == null || _board[i][j - 1].getFalling() == false)
						{
							_board[i][j] = _board[i][j - 1];
							if (_board[i][j] != null)
								_board[i][j].setPos(i, j);
							_board[i][j-1] = null;

						}
						else
							j--;
					}
				}
				for (int i = 0; i < HEIGHT; i++)
				{
					_board[i][0] = temp[i];
				}
				getLastRow();
			}
			else
				Log.w("message", "Could not shift right");
		}

	}

	public void shiftLeft()
	{
		synchronized (_board)
		{
			Piece[] temp = new Piece[HEIGHT];
			if (_board[_currentRow][6] == null)
			{
				for (int i = 0; i < HEIGHT; i++)
				{
					if (_board[i][0] != null)
						_board[i][0].setPos(i, 10);
					temp[i] = _board[i][0];
					_board[i][0] = null;
				}
				for (int i = 0; i < HEIGHT - 1; i++)
				{
					for (int j = 0; j < 10; j++)
					{
						if (_board[i][j + 1] == null || _board[i][j + 1].getFalling() == false)
						{
							_board[i][j] = _board[i][j + 1];
							if (_board[i][j] != null)
								_board[i][j].setPos(i, j);
							_board[i][j+1] = null;
						}
						else
							j++;
					}
				}
				for (int i = 0; i < HEIGHT; i++)
				{
					_board[i][10] = temp[i];
				}
				getLastRow();
			}
			else
				Log.w("message", "Could not shift left.");

		}
	}
	
	public interface OnGameStatusListener
	{
		public void onGameOver(boolean status, int score);
	}
	
	public void setOnGameStatusListener(OnGameStatusListener listener)
	{
		this.listener = listener;
	}
}
