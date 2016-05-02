package ru.izucken.main;

// Cubic spline ftw
public class Spline {
	private float[] x, y;

	private SplineTuple[] splines;

	private class SplineTuple{
		public float a, b, c, d, x;
	}

	public Spline(float[] x, float[] y){
		int size = x.length;
		this.x = x;
		this.y = y;
		splines = new SplineTuple[size];
		for(int i = 0; i < size; i++){
			splines[i] = new SplineTuple();
		}

		build();
	}

	public int getLength(){
		return splines.length;
	}

	public void build(){
		int n = x.length;

		for(int i = 0; i < n; ++i){
			splines[i].x = x[i];
			splines[i].a = y[i];
		}

		splines[0].c = splines[n - 1].c = 0.0f;

		float[] alpha = new float[n - 1];
		float[] beta = new float[n - 1];
		alpha[0] = beta[0] = 0.0f;
		for(int i = 1; i < n - 1; ++i){
			float h_i = x[i] - x[i - 1], h_i1 = x[i + 1] - x[i];
			float A = h_i;
			float C = 2.0f * (h_i + h_i1);
			float B = h_i1;
			float F = 6.0f * ((y[i + 1] - y[i]) / h_i1 - (y[i] - y[i - 1]) / h_i);
			float z = (A * alpha[i - 1] + C);
			alpha[i] = -B / z;
			beta[i] = (F - A * beta[i - 1]) / z;
		}

		for (int i = n - 2; i > 0; --i)
			splines[i].c = alpha[i] * splines[i + 1].c + beta[i];

		for (int i = n - 1; i > 0; --i){
			float h_i = x[i] - x[i - 1];
			splines[i].d = (splines[i].c - splines[i - 1].c) / h_i;
			splines[i].b = h_i * (2.0f * splines[i].c + splines[i - 1].c) / 6.0f + (y[i] - y[i - 1]) / h_i;
		}
	}

	public float func(float x){
		int n = splines.length;
		SplineTuple s;

		if (x <= splines[0].x)
			s = splines[1];
		else if (x >= splines[n - 1].x)
			s = splines[n - 1];
		else {
			int i = 0, j = n - 1;
			while (i + 1 < j){
				int k = i + (j - i) / 2;
				if (x <= splines[k].x)
					j = k;
				else
					i = k;
			}
			s = splines[j];
		}

		float dx = (x - s.x);
		return s.a + (s.b + (s.c / 2.0f + s.d * dx / 6.0f) * dx) * dx;
	}
}
