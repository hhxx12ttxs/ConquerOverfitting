package Games;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.List;

import Basics.GameSurface;
import Pages.GamePage;

public class Sticks implements GameSurface {
	int State, Size, Open, Diff, Cl, Str;
	int[][] Mat;
	int[] Lock, Pair, Shake;
	boolean On;
	long start, time, lt;
	int eSec, eHour, eMin;
	List Text;

	public Sticks() {
		State = 0;
		Size = 0;
		time = 0;

		Text = new List();
		Text.add("How to play");
		Text.add("All the sticks must be up to open this lock.");
		Text.add("A stick can be set to up with a click on it.");
		Text.add("Some stick will cause other stick to drop,");
		Text.add("but these rules can be modified by the state");
		Text.add("of other sticks. E.g.: Clicking on stick A will");
		Text.add("cause stick B to drop, only if stick C is down.");
		Text.add("If a stick will drop others those will shake");
		Text.add("as the mouse is over the stick.");
		Text.add("");
		Text.add("Each game is random, and surely solveable!");
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

		float dx = 0, dy = 0;
		G.setColor(Color.WHITE);
		G.setFont(GamePage.font[2]);
		G.drawString("Clicks: " + Cl, 550, 40);
		G.drawString("Time passed: " + ((eHour < 10) ? "0" : "") + eHour + ":" + ((eMin < 10) ? "0" : "") + eMin + ":"
				+ ((eSec < 10) ? "0" : "") + eSec, 550, 20);
		for (int i = 0; i < Size; i++) {
			G.drawString("" + (i + 1), 405 + i * 30 - Size * 15 - G.getFontMetrics().stringWidth("" + (i + 1)) / 2,
					200 - Lock[Pair[i]] * 50 - 20);
			if (Shake[Pair[i]] == 1) {
				dx = (float) (Math.random() * 4f - 2f);
				dy = (float) (Math.random() * 4f - 2f);
			} else {
				dx = 0;
				dy = 0;
			}
			G.fillRect(400 + i * 30 - Size * 15 + (int) dx, 200 - Lock[Pair[i]] * 50 + (int) dy, 10, 100);
		}
		G.drawLine(350 - Size * 15, 301, (Size - 1) * 15 + 450, 301);
		G.drawLine(350 - Size * 15, 201, (Size - 1) * 15 + 450, 201);
		/*
		 * for (int i = 0; i < Size; i++) for (int l = 0; l < Size; l++) {
		 * G.drawString(""+Mat[i][l],20+l*20,20+i*20); }
		 */
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
			G.drawString("Open!", 110 - G.getFontMetrics().stringWidth("Open!!") / 2, 500);
		else
			G.drawString("Closed!", 110 - G.getFontMetrics().stringWidth("Closed!") / 2, 500);
	}

	public boolean MouseMove(float x, float y, float dx, float dy) {
		On = false;
		if (Math.pow(Math.abs(x - 110), 2) + Math.pow(Math.abs(y - 490), 2) < 10000)
			On = true;
		for (int i = 0; i < Size; i++)
			Shake[i] = 0;
		for (int i = 0; i < Size; i++)
			if (x > 400 + i * 30 - Size * 15 - 5 && y > 200 - Lock[Pair[i]] * 50 && x < 400 + i * 30 - Size * 15 + 20
					&& y < 200 - Lock[Pair[i]] * 50 + 100 && Lock[Pair[i]] == 0) {
				for (int l = 0; l < Size; l++) {
					if (Mat[Pair[i]][l] == -1 && Lock[l] == 1)
						Shake[l] = 1;
					if (Mat[Pair[i]][l] > 0 && Lock[Mat[Pair[i]][l] - 1] == 0 && Lock[l] == 1) {
						Shake[l] = 1;
					}
				}
			}
		return false;
	}

	public boolean MouseDrag(float x, float y, float dx, float dy) {
		return false;
	}

	public boolean MouseDown(float x, float y, int button) {
		return false;
	}

	public boolean MouseUp(float x, float y, int button) {
		for (int i = 0; i < Size; i++)
			if (x > 400 + i * 30 - Size * 15 - 5 && y > 200 - Lock[Pair[i]] * 50 && x < 400 + i * 30 - Size * 15 + 20
					&& y < 200 - Lock[Pair[i]] * 50 + 100 && Lock[Pair[i]] == 0) {
				if (Open < 1)
					Cl++;
				Lock[Pair[i]] = 1;
				for (int l = 0; l < Size; l++) {
					if (Mat[Pair[i]][l] == -1)
						Lock[l] = 0;
					if (Mat[Pair[i]][l] > 0 && Lock[Mat[Pair[i]][l] - 1] == 0) {
						Lock[l] = 0;
					}
				}
			}
		if (Math.pow(Math.abs(x - 110), 2) + Math.pow(Math.abs(y - 490), 2) < 10000 && Open == 1)
			Open = 2;
		return false;
	}

	public void Move(double time) {
		if (Open == 2)
			State = -2;
		int i = 100;
		for (i = 0; i < Size; i++) {
			if (Lock[i] == 0)
				i = 100;
		}
		if (i < 70)
			Open = 1;
		Str = 1;
	}

	public void Reset(int diff) {
		Str = 0;
		lt = start = System.currentTimeMillis();
		Cl = 0;
		State = 0;
		Diff = diff + 1;
		Size = 5 + (int) Math.sqrt(diff / 2);
		Shake = new int[Size];
		Lock = new int[Size];
		Pair = new int[Size];
		Mat = new int[Size][Size];
		for (int i = 0; i < Size; i++) {
			Pair[i] = i;
			Lock[i] = 0;
			Shake[i] = 0;
			for (int l = 0; l < Size; l++) {
				Mat[i][l] = 0;
			}
		}
		int a, b, c, d;
		for (int i = 0; i < 10 * Size; i++) {
			b = (int) Math.floor(Math.random() * Size);
			c = (int) Math.floor(Math.random() * Size);
			a = Pair[b];
			Pair[b] = Pair[c];
			Pair[c] = a;
		}
		for (int i = 0; i < 3 + (Size * Size) / 2 + (diff % 7) + (diff / 4); i++) {
			do {
				b = (int) Math.floor(Math.random() * (Size - 1));
				c = (int) Math.floor(Math.random() * Size);
				d = (int) Math.floor(Math.random() * (Size - b - 1)) + b + 1;
			} while (b == c);
			if (b != c) {
				if (b > c)
					Mat[b][c] = d + 1;
				else
					Mat[b][c] = -1;
			}
		}
		Open = 0;
	}

	public List GetDialog() {
		return Text;
	}

	public void Remake() {
		for (int i = 0; i < Size; i++) {
			Lock[i] = 0;
			Shake[i] = 0;
		}
	}

}

