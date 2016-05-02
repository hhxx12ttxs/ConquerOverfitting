package Games;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.List;

import Basics.GameSurface;
import Pages.GamePage;

public class MaxInd implements GameSurface {
	int State, Diff, Key, Open, Act, Size, Gr, Cl, Str;
	int[][] Graf;
	int[][] Pos, Res;
	int[] Sel;
	boolean On;
	long start, time, lt;
	int eSec, eHour, eMin;
	List Text;

	public MaxInd() {
		Reset(0);
		time = 0;
		Str = 0;

		Text = new List();
		Text.add("How to play");
		Text.add("To opend the lock, you need to find and select the");
		Text.add("given number of independent points in the graph.");
		Text.add("Independent points are not connected to eachother.");
		Text.add("");
		Text.add("You can drag the points with the left mouse button");
		Text.add("and select or unselect them with the right one.");
		Text.add("Points connected to eachother can't be selected.");
		Text.add("");
		Text.add("Only one good combination exists.");
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
			G.fillOval(Pos[i][0] - 5, Pos[i][1] - 5, 10, 10);
			if (Sel[i] == 1) {
				G.drawOval(Pos[i][0] - 9, Pos[i][1] - 9, 17, 17);
				G.drawOval(Pos[i][0] - 8, Pos[i][1] - 8, 15, 15);
			}
			for (int l = i + 1; l < Size; l++)
				if (Graf[i][l] == 1)
					G.drawLine(Pos[i][0], Pos[i][1], Pos[l][0], Pos[l][1]);
		}

		G.drawOval(150, 20, 500, 500);
		G.drawOval(151, 21, 499, 499);

		G.setFont(GamePage.font[1]);
		G.drawString("Needed:  " + Key, 10, 70);
		G.drawString("Selected: " + Act, 10, 100);
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
			Pos[Gr][0] = (int) x;
			Pos[Gr][1] = (int) y;
			if (Math.pow(Pos[Gr][0] - 400, 2) + Math.pow(Pos[Gr][1] - 270, 2) > 250 * 250) {
				float len = (float) Math.sqrt(((x - 400) * (x - 400) + (y - 270) * (y - 270)) / (250 * 250));
				Pos[Gr][0] = (int) ((x - 400) / len) + 400;
				Pos[Gr][1] = (int) ((y - 270) / len) + 270;
			}
		}
		return false;
	}

	public boolean MouseDown(float x, float y, int button) {
		if (button == 1) {
			Gr = -1;
			for (int i = 0; i < Size; i++)
				if (Math.pow(Pos[i][0] - x, 2) + Math.pow(Pos[i][1] - y, 2) < 100) {
					Gr = i;
				}
		}
		if (button == 3) {
			for (int i = 0; i < Size; i++)
				if (Math.pow(Pos[i][0] - x, 2) + Math.pow(Pos[i][1] - y, 2) < 100) {
					int l = 1000;
					for (l = 0; l < Size; l++)
						if (Graf[i][l] > 0 && Sel[l] > 0)
							l = 1000;
					if (l < 500) {
						if (Sel[i] > 0)
							Act--;
						else
							Act++;
						if (Open < 1)
							Cl++;
						Sel[i] = 1 - Sel[i];
					}
				}
		}
		return false;
	}

	public boolean MouseUp(float x, float y, int button) {
		Gr = -1;
		if (Math.pow(Math.abs(x - 110), 2) + Math.pow(Math.abs(y - 490), 2) < 10000 && Open == 1)
			Open = 2;
		return false;
	}

	public void Move(double time) {
		if (Act == Key && Open == 0)
			Open = 1;
		if (Open == 2)
			State = -2;
		Str = 1;
	}

	public void Reset(int diff) {
		Str = 0;
		lt = start = System.currentTimeMillis();
		Cl = 0;
		State = 0;
		Diff = diff + 1;
		Key = 4 + (int) Math.sqrt(diff / 2);
		Size = Key + Key / 2 + Key;

		Sel = new int[Size];
		Graf = new int[Size][Size];
		Pos = new int[Size][2];
		Res = new int[Size][2];

		for (int i = 0; i < Size; i++) {
			Sel[i] = 0;
			Pos[i][0] = 400 + (int) (Math.cos((float) i / (float) Size * Math.PI * 2) * 200);
			Pos[i][1] = 270 + (int) (Math.sin((float) i / (float) Size * Math.PI * 2) * 200);
			for (int l = 0; l < Size; l++)
				Graf[i][l] = 0;
		}

		for (int i = 0; i < Key; i++) {
			for (int l = 0; l < Key; l++)
				if (i != l)
					Graf[i][l] = 1;
			for (int l = 0; l < Key; l++) {
				// if (i != l)
				Graf[Key + i][l] = 1;
				Graf[l][Key + i] = 1;
			}
		}
		for (int i = Key + Key; i < Size; i++) {
			for (int l = 0; l < Key; l++) {
				Graf[i][Key + l] = 1;
				Graf[Key + l][i] = 1;
			}
		}
		int b, c;
		int[] a;
		for (int i = 0; i < 10 * Size; i++) {
			b = (int) Math.floor(Math.random() * Size);
			c = (int) Math.floor(Math.random() * Size);
			a = Pos[b];
			Pos[b] = Pos[c];
			Pos[c] = a;
		}

		Open = Act = 0;
		Gr = -1;
		for (int i = 0; i < Size; i++) {
			Sel[i] = 0;
			Res[i][0] = Pos[i][0];
			Res[i][1] = Pos[i][1];
		}
	}

	public List GetDialog() {
		return Text;
	}

	public void Remake() {
		for (int i = 0; i < Size; i++) {
			Sel[i] = 0;
			Pos[i][0] = Res[i][0];
			Pos[i][1] = Res[i][1];
		}
	}
}

