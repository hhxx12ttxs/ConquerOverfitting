package Games;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.List;

import Basics.GameSurface;
import Pages.GamePage;

public class Graf implements GameSurface {
	int State, Diff, Key, Open, Size, Gr, Cl, Po, X, Y, Str;
	int[][] Graf;
	int[][] Num, Pos, Res, Pos2, Res2;
	int[] Par;
	boolean On;
	long start, time, lt;
	int eSec, eHour, eMin;
	List Text;

	public Graf() {
		Reset(0);
		time = 0;

		Text = new List();
		Text.add("How to play");
		Text.add("You must pair the nodes of the two given graphs with to open");
		Text.add("the lock. You can move the points of both graphs, and you");
		Text.add("add numbers to the points of the left graph, with dragging");
		Text.add("them with the right mouse button.");
		Text.add("The lock opens if the numbers on the point are matching their");
		Text.add("positions are irrelevant. All good solutions are accepted.");
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
		for (int i = 0; i < Size; i++) {
			G.fillOval(Pos[i][0] - 12, Pos[i][1] - 12, 25, 25);
			G.setColor(Color.BLACK);
			G.drawString("" + (i + 1), Pos[i][0] - G.getFontMetrics().stringWidth("" + (i + 1)) / 2, Pos[i][1] + 8);
			G.setColor(Color.WHITE);
			for (int l = i + 1; l < Size; l++)
				if (Graf[i][l] == 1)
					G.drawLine(Pos[i][0], Pos[i][1], Pos[l][0], Pos[l][1]);
		}

		for (int i = 0; i < Size; i++) {
			G.fillOval(Pos2[i][0] - 12, Pos2[i][1] - 12, 25, 25);
			G.setColor(Color.BLACK);
			if (Par[i] > -1)
				G.drawString("" + (Par[i] + 1), Pos2[i][0] - G.getFontMetrics().stringWidth("" + (Par[i] + 1)) / 2, Pos2[i][1] + 8);
			G.setColor(Color.WHITE);
			for (int l = i + 1; l < Size; l++)
				if (Graf[i][l] == 1)
					G.drawLine(Pos2[i][0], Pos2[i][1], Pos2[l][0], Pos2[l][1]);
		}
		int a;
		for (int i = 0; i < Size; i++) {
			a = -1;
			for (int l = 0; l < Size && a < 0; l++)
				if (i == Par[l])
					a = l;
			if (a < 0 && Po != i) {
				G.drawOval(Num[i][0] - 12, Num[i][1] - 12, 25, 25);
				G.drawString("" + (i + 1), Num[i][0] - G.getFontMetrics().stringWidth("" + (i + 1)) / 2, Num[i][1] + 8);
			}
			if (Po == i)
				G.drawString("" + (i + 1), X - G.getFontMetrics().stringWidth("" + (i + 1)) / 2, Y + 8);
		}

		G.drawOval(415, 65, 340, 340);
		G.drawOval(416, 66, 339, 339);
		G.drawOval(55, 65, 340, 340);
		G.drawOval(56, 66, 339, 339);

		G.setFont(GamePage.font[1]);
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
		if (Gr > -1) {
			if (Gr < Size) {
				Pos[Gr][0] = (int) x;
				Pos[Gr][1] = (int) y;
				if (Math.pow(Pos[Gr][0] - 585, 2) + Math.pow(Pos[Gr][1] - 235, 2) > 170 * 170) {
					float len = (float) Math.sqrt(((x - 585) * (x - 585) + (y - 235) * (y - 235)) / (170 * 170));
					Pos[Gr][0] = (int) ((x - 585) / len) + 585;
					Pos[Gr][1] = (int) ((y - 235) / len) + 235;
				}
			} else {
				Pos2[Gr - Size][0] = (int) x;
				Pos2[Gr - Size][1] = (int) y;
				if (Math.pow(Pos2[Gr - Size][0] - 225, 2) + Math.pow(Pos2[Gr - Size][1] - 235, 2) > 170 * 170) {
					float len = (float) Math.sqrt(((x - 225) * (x - 225) + (y - 235) * (y - 235)) / (170 * 170));
					Pos2[Gr - Size][0] = (int) ((x - 225) / len) + 225;
					Pos2[Gr - Size][1] = (int) ((y - 235) / len) + 235;
				}
			}
		}
		X = (int) x;
		Y = (int) y;

		return false;
	}

	public boolean MouseDown(float x, float y, int button) {
		Gr = -1;
		if (button == 1) {
			for (int i = 0; i < Size; i++)
				if (Math.pow(Pos[i][0] - x, 2) + Math.pow(Pos[i][1] - y, 2) < 400) {
					Gr = i;
				}
			for (int i = 0; i < Size; i++)
				if (Math.pow(Pos2[i][0] - x, 2) + Math.pow(Pos2[i][1] - y, 2) < 400) {
					Gr = Size + i;
				}
		}
		Po = -1;
		if (button == 3 && Open < 1) {
			for (int i = 0; i < Size; i++) {
				if (Par[i] > -1 && Math.pow(Pos2[i][0] - x, 2) + Math.pow(Pos2[i][1] - y, 2) < 400) {
					Po = Par[i];
					Par[i] = -1;
				}
				int a = -1;
				for (int l = 0; l < Size && a < 0; l++)
					if (i == Par[l])
						a = l;
				if (a < 0 && Math.pow(Num[i][0] - x, 2) + Math.pow(Num[i][1] - y, 2) < 400) {
					Po = i;
				}
			}
		}
		return false;
	}

	public boolean MouseUp(float x, float y, int button) {
		Gr = -1;
		if (Math.pow(Math.abs(x - 110), 2) + Math.pow(Math.abs(y - 490), 2) < 10000 && Open == 1)
			Open = 2;
		if (Po > -1)
			for (int i = 0; i < Size; i++) {
				if (Math.pow(Pos2[i][0] - X, 2) + Math.pow(Pos2[i][1] - Y, 2) < 400) {
					Par[i] = Po;
				}
			}
		if (Po > -1)
			Check();
		Po = -1;
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
		Po = -1;
		State = 0;
		Diff = diff + 1;
		Key = 3 + (int) Math.sqrt(Diff / 2);
		Size = Key + Key;

		Par = new int[Size];
		Graf = new int[Size][Size];
		Pos = new int[Size][2];
		Res = new int[Size][2];
		Pos2 = new int[Size][2];
		Res2 = new int[Size][2];
		Num = new int[Size][2];

		for (int i = 0; i < Size; i++) {
			Par[i] = -1;
			Pos[i][0] = 585 + (int) (Math.cos((float) i / (float) Size * Math.PI * 2) * 150);
			Pos[i][1] = 235 + (int) (Math.sin((float) i / (float) Size * Math.PI * 2) * 150);
			Pos2[i][0] = 225 + (int) (Math.cos((float) i / (float) Size * Math.PI * 2) * 150);
			Pos2[i][1] = 235 + (int) (Math.sin((float) i / (float) Size * Math.PI * 2) * 150);
			Num[i][0] = 225 + (int) (Math.cos((float) i / (float) Size * Math.PI * 2) * 180);
			Num[i][1] = 235 + (int) (Math.sin((float) i / (float) Size * Math.PI * 2) * 180);
			for (int l = 0; l < Size; l++)
				Graf[i][l] = 0;
		}

		for (int i = 0; i < Size; i++) {
			Graf[i][(i + 1) % Size] = 1;
			Graf[(i + 1) % Size][i] = 1;
		}

		int b, c;
		int[] a;
		for (int i = 0; i < Key * Math.sqrt(Key) * 2 + 2; i++) {
			b = (int) Math.floor(Math.random() * Size);
			c = (int) Math.floor(Math.random() * Size);
			Graf[c][b] = 1;
			Graf[b][c] = 1;
		}

		for (int i = 0; i < 10 * Size; i++) {
			b = (int) Math.floor(Math.random() * Size);
			c = (int) Math.floor(Math.random() * Size);
			a = Pos[b];
			Pos[b] = Pos[c];
			Pos[c] = a;
			b = (int) Math.floor(Math.random() * Size);
			c = (int) Math.floor(Math.random() * Size);
			a = Pos2[b];
			Pos2[b] = Pos2[c];
			Pos2[c] = a;
		}

		Open = 0;
		Gr = -1;
		for (int i = 0; i < Size; i++) {
			Res[i][0] = Pos[i][0];
			Res[i][1] = Pos[i][1];
			Res2[i][0] = Pos2[i][0];
			Res2[i][1] = Pos2[i][1];
		}
	}

	void Check() {
		int a = 0;
		for (int i = 0; i < Size && a == 0; i++)
			if (Par[i] < 0)
				a = 1;

		for (int i = 0; i < Size && a < 1; i++)
			for (int l = 0; l < Size && a < 1; l++) {
				if (Graf[i][l] != Graf[Par[i]][Par[l]])
					a = 1;
			}
		if (a == 0)
			Open = 1;
	}

	public List GetDialog() {
		return Text;
	}

	public void Remake() {
		for (int i = 0; i < Size; i++) {
			Pos[i][0] = Res[i][0];
			Pos[i][1] = Res[i][1];
			Pos2[i][0] = Res2[i][0];
			Pos2[i][1] = Res2[i][1];
		}
	}
}

