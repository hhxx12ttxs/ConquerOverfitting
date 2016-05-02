package Games;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.List;

import Basics.GameSurface;
import Pages.GamePage;

public class Cube implements GameSurface {
	int State, Diff, Size, Open, Cl, Str;
	int[][][] Cube, Key, Res;
	int[][][] Side, KeySide;
	boolean On;
	long start, time, lt;
	int eSec, eHour, eMin;
	List Text;

	public Cube() {
		time = 0;

		Text = new List();
		Text.add("How to play");
		Text.add("The lock consist of two cubes, both segmented into smaller");
		Text.add("pieces. In each of these segments can be a small (white)");
		Text.add("part, or it can be empty. You can only see the cube as");
		Text.add("the views (top, side, front). These views are squares,");
		Text.add("made of smaller squares, showing if there any small part");
		Text.add("in the cube are present in that particluar line of pieces.");
		Text.add("");
		Text.add("You need to set the left cube to the state to the");
		Text.add("state of the right one. To do this, you can use each view");
		Text.add("on the left, and you can shift the row of pieces by clicking.");
		Text.add("If you click on a piece, the other pieces behind are shifting, ");
		Text.add("as you can see it on the other view, not in the one you clicked.");
		Text.add("Only white parts are clickable as black ones means empty lines.");
		Text.add("");
		Text.add("It is probably the hardest game. Experiment!");
	}

	public int GetState() {
		return State;
	}

	public long GetTime() {
		long t = time;
		time = 0;
		return t;
	}

	public long ReadTime() {
		return time;
	}

	public void Draw(Graphics2D G) {
		long elapsedTimeMillis = lt - start;
		if (Str > 0)
			time += System.currentTimeMillis() - lt;
		lt = System.currentTimeMillis();

		if (Open < 1) {
			eSec = (int) (elapsedTimeMillis / 1000F) % 60;
			eMin = (int) (elapsedTimeMillis / (60 * 1000F)) % 60;
			eHour = (int) (elapsedTimeMillis / (60 * 60 * 1000F));
		}

		G.setColor(Color.WHITE);
		G.setFont(GamePage.font[2]);
		G.drawString("Clicks: " + Cl, 550, 40);
		G.drawString("Time passed: " + ((eHour < 10) ? "0" : "") + eHour + ":" + ((eMin < 10) ? "0" : "") + eMin + ":"
				+ ((eSec < 10) ? "0" : "") + eSec, 550, 20);

		for (int i = 0; i < 3; i++) {
			G.drawRect(250, 80 + i * 170, 122, 122);
			for (int l = 0; l < Size; l++) {
				for (int n = 0; n < Size; n++) {
					if (Side[i][l][n] > 0)
						G.fillRect(252 + l * 120 / Size, 82 + n * 120 / Size + i * 170, 120 / Size - 1, 120 / Size - 1);
				}
			}
		}

		for (int i = 0; i < 3; i++) {
			G.drawRect(430, 80 + i * 170, 122, 122);
			for (int l = 0; l < Size; l++) {
				for (int n = 0; n < Size; n++) {
					if (KeySide[i][l][n] > 0)
						G.fillRect(432 + l * 120 / Size, 82 + n * 120 / Size + i * 170, 120 / Size - 1, 120 / Size - 1);
				}
			}
		}

		G.setFont(GamePage.font[0]);
		G.drawString(Diff + ". level", 10, 40);
		if (On) {
			G.drawOval(10, 390, 200, 200);
			G.drawOval(11, 391, 198, 198);
		} else {
			G.fillOval(10, 390, 200, 200);
			G.setColor(Color.BLACK);
		}
		if (Open > 0)
			G.drawString("Open!", 110 - G.getFontMetrics().stringWidth("Open!") / 2, 500);
		else
			G.drawString("Closed!", 110 - G.getFontMetrics().stringWidth("Closed!") / 2, 500);
	}

	public boolean MouseMove(float x, float y, float dx, float dy) {
		On = false;
		if (Math.pow(Math.abs(x - 110), 2) + Math.pow(Math.abs(y - 490), 2) < 10000)
			On = true;
		return false;
	}

	public boolean MouseDrag(float x, float y, float dx, float dy) {
		return false;
	}

	public boolean MouseDown(float x, float y, int button) {
		return false;
	}

	public boolean MouseUp(float x, float y, int button) {
		int w = -1, a = -1, b = -1;
		for (int i = 0; i < 3; i++) {
			if (x > 250 && x < 250 + 122 && y > 80 + i * 170 && y < 80 + i * 170 + 122)
				w = i;
			for (int l = 0; l < Size; l++) {
				for (int n = 0; n < Size; n++) {
					if (x > 252 + l * 120 / Size && x < 252 + l * 120 / Size + 120 / Size - 1 && y > 82 + n * 120 / Size + i * 170
							&& y < 82 + n * 120 / Size + i * 170 + 120 / Size - 1) {
						a = l;
						b = n;
						if (Open < 1)
							Cl++;
					}
				}
			}
		}
		if (w > -1 && w < 3 && a > -1 && a < Size && b > -1 && b < Size && Open == 0) {
			switch (w) {
			case 0:
				Push(a, b, -1);
				break;
			case 1:
				Push(a, -1, b);
				break;
			case 2:
				Push(-1, a, b);
				break;
			}
			Side[0] = Vetit(Cube, 0);
			Side[1] = Vetit(Cube, 1);
			Side[2] = Vetit(Cube, 2);
			int k = 1;
			for (int i = 0; i < 3; i++) {
				for (int l = 0; l < Size; l++) {
					for (int n = 0; n < Size; n++) {
						if (Side[i][l][n] != KeySide[i][l][n])
							k = 0;
					}
				}
			}
			if (k == 1)
				Open = 1;
		}

		if (Math.pow(Math.abs(x - 110), 2) + Math.pow(Math.abs(y - 490), 2) < 10000 && Open == 1)
			Open = 2;

		return false;
	}

	public void Move(double time) {
		Str = 1;
		if (Open == 2)
			State = -2;
	}

	public void Reset(int diff) {
		Str = 0;
		lt = start = System.currentTimeMillis();
		Cl = 0;
		State = 0;
		Diff = diff + 1;
		Size = 3 + (int) Math.floor(diff / 35f);
		Res = new int[Size][Size][Size];
		Cube = new int[Size][Size][Size];
		Key = new int[Size][Size][Size];
		Side = new int[3][Size][Size];
		KeySide = new int[3][Size][Size];

		for (int i = 0; i < Size; i++)
			for (int l = 0; l < Size; l++)
				for (int n = 0; n < Size; n++) {
					Cube[i][l][n] = Key[i][l][n] = 0;
					if (i < 3)
						Side[i][l][n] = KeySide[i][l][n] = 0;
				}
		int a, b, c;
		for (int i = 0; i < 3 + diff / 5; i++) {
			do {
				a = (int) Math.floor(Math.random() * Size);
				b = (int) Math.floor(Math.random() * Size);
				c = (int) Math.floor(Math.random() * Size);
			} while (Cube[a][b][c] == 1);
			Cube[a][b][c] = Key[a][b][c] = 1;
		}
		for (int i = 0; i < 20 + diff / 4; i++) {
			int w = (int) Math.floor(Math.random() * 3);
			a = (int) Math.floor(Math.random() * Size);
			b = (int) Math.floor(Math.random() * Size);
			switch (w) {
			case 0:
				Push(a, b, -1);
				break;
			case 1:
				Push(a, -1, b);
				break;
			case 2:
				Push(-1, a, b);
				break;
			}
		}
		for (int i = 0; i < Size; i++)
			for (int l = 0; l < Size; l++)
				for (int n = 0; n < Size; n++) {
					Res[i][l][n] = Cube[i][l][n];
				}
		KeySide[0] = Vetit(Key, 0);
		KeySide[1] = Vetit(Key, 1);
		KeySide[2] = Vetit(Key, 2);
		Side[0] = Vetit(Cube, 0);
		Side[1] = Vetit(Cube, 1);
		Side[2] = Vetit(Cube, 2);
		Open = 0;
	}

	int[][] Vetit(int[][][] A, int dir) {
		int[][] V = new int[A.length][A.length];
		for (int i = 0; i < Size; i++)
			for (int l = 0; l < Size; l++)
				V[i][l] = 0;

		switch (dir) {
		case 0:
			for (int i = 0; i < A.length; i++)
				for (int l = 0; l < A.length; l++)
					for (int n = 0; n < A.length; n++)
						if (A[i][l][n] == 1)
							V[i][l] = 1;
			break;
		case 1:
			for (int i = 0; i < A.length; i++)
				for (int l = 0; l < A.length; l++)
					for (int n = 0; n < A.length; n++)
						if (A[i][l][n] == 1)
							V[i][n] = 1;
			break;
		case 2:
			for (int i = 0; i < A.length; i++)
				for (int l = 0; l < A.length; l++)
					for (int n = 0; n < A.length; n++)
						if (A[i][l][n] == 1)
							V[l][n] = 1;
			break;
		}
		return V;
	}

	void Push(int x, int y, int z) {
		int[][][] C = new int[Size][Size][Size];

		for (int i = 0; i < Size; i++)
			for (int l = 0; l < Size; l++)
				for (int n = 0; n < Size; n++)
					C[i][l][n] = Cube[i][l][n];

		for (int i = 0; i < Size; i++)
			for (int l = 0; l < Size; l++)
				for (int n = 0; n < Size; n++) {
					Cube[i][l][n] = C[(i + ((y == l && z == n) ? 1 : 0)) % Size][(l + ((x == i && z == n) ? 1 : 0)) % Size][(n + ((x == i && y == l) ? 1
							: 0))
							% Size];
				}
	}

	public void Remake() {
		for (int i = 0; i < Size; i++)
			for (int l = 0; l < Size; l++)
				for (int n = 0; n < Size; n++) {
					Cube[i][l][n] = Res[i][l][n];
				}
		Side[0] = Vetit(Cube, 0);
		Side[1] = Vetit(Cube, 1);
		Side[2] = Vetit(Cube, 2);
	}

	public List GetDialog() {
		return Text;
	}

}

