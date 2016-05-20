package com.breaktrycatch.needmorehumans.control.webcam;

import java.awt.Rectangle;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PImage;

public class MeanShiftVectorBackgroundSubtraction
{
	private PApplet _app;
	private ArrayList<PImage> _backgroundList;
	private PImage _backgroundImage;

	private boolean _backgroundComputed = false;

	private double[][] gaussian_table;
	private double[][] ln_gaussian_table;
	private double inv_sqrt_2pi;
	private double threshold;
	private int neighborhood;
	private int hh;
	private int ww;

	public MeanShiftVectorBackgroundSubtraction(PApplet app)
	{
		_app = app;
		clearBackgroundList();
	}

	public void clearBackgroundList()
	{
		_backgroundComputed = false;
		_backgroundList = new ArrayList<PImage>();
	}

	public void addBackgroundImage(PImage background)
	{
		_backgroundList.add(background);
	}

	public int numBackgroundImages()
	{
		return _backgroundList.size();
	}

	public boolean isBackgroundComputed()
	{
		return _backgroundComputed;
	}

	public PImage computeBackground()
	{
		if (_backgroundList.size() == 0)
		{
			PApplet.println("No background images to compute!");
			return null;
		}

		PApplet.println("Computing background image with " + _backgroundList.size() + "source images");

		_backgroundImage = _app.createImage(_backgroundList.get(0).width, _backgroundList.get(0).height, PApplet.ARGB);

		Rectangle rect = new Rectangle(0, 0, _backgroundImage.width, _backgroundImage.height);
		computeImage(_backgroundList.toArray(new PImage[_backgroundList.size()]), _backgroundImage, rect);

		_backgroundComputed = true;

		return _backgroundImage;
	}

	protected void computeImage(PImage[] sources, PImage dest, Rectangle destRect)
	{

		neighborhood = 3;
		threshold = .01;

		int[] db1, db2, db3;
		int xt, x;
		final int min_mov = neighborhood;
		double Pr, Pn, Pc, Pn_tmp, sigma;
		double inv_sigma_2;
		double threshold2 = 0.5;
		inv_sqrt_2pi = 1 / Math.sqrt(2 * Math.PI);

		PImage src = sources[0];
		hh = src.height;
		ww = src.width;

		PApplet.println("Sources: " + sources.length + " : " + hh + " :  " + ww);
		PApplet.println("neighbourhood: " + neighborhood);

		precalculatedensityFunction();

		dest.loadPixels();
		for (int q = 0; q < dest.pixels.length; q++)
		{
			dest.pixels[q] = 0xff0000;
		}

		int[] db = dest.pixels;
		db1 = sources[sources.length - 1].pixels;

		for (int i = 0; i < ww; i++)
		{
			for (int j = 0; j < hh; j++)
			{
				xt = db1[i + j * ww];
				// Compute kernel bandwidth

				sigma = 0;
				for (int k = 0; k < sources.length - 1; k++)
				{
					db2 = sources[k].pixels;
					db3 = sources[k + 1].pixels;
					sigma += Math.abs(db2[i + j * ww] - db3[i + j * ww]);
				}
				sigma /= sources.length;
				sigma /= 0.68 * Math.sqrt(2);
				inv_sigma_2 = 1 / (Math.pow(sigma, 2));

				PApplet.println("PR: " + sources + " " + Integer.toHexString(xt) + " inv_sig" + inv_sigma_2 + " : " + threshold + " L " + (i + j * ww) + " SIGMA: " + sigma);
				Pr = densityEstimation(sources, xt, inv_sigma_2, threshold, i + j * ww);

				if (Pr < threshold)
				{
					// Compute probability to be foreground or background
					Pn = 0;
					for (int l = -min_mov; l < min_mov; l++)
					{
						for (int m = -min_mov; m < min_mov; m++)
						{
							if (i + l >= 0 && i + l < ww && j + m >= 0 && j + m < hh && l != 0 && m != 0)
							{
								Pn_tmp = densityEstimation(sources, xt, inv_sigma_2, threshold, i + l + (j + m) * ww);
								Pn = Math.max(Pn, Pn_tmp);
							}
						}
					}
					Pc = 1;
					if (Pn <= threshold)
						db[i + j * ww] = 255;
					else
					{
						db2 = sources[sources.length - 2].pixels;
						for (int l = -min_mov; l < min_mov; l++)
						{
							for (int m = -min_mov; m < min_mov; m++)
							{
								if (i + l >= 0 && i + l < ww && j + m >= 0 && j + m < hh && l != 0 && m != 0)
								{
									if (db[(i + l) + (j + m) * ww] > 0)
									{
										x = db2[(i + l) + (j + m) * ww];

										Pn_tmp = Math.exp(-0.5 * Math.pow((xt - x) / sigma, 2));
										Pn_tmp *= inv_sqrt_2pi * inv_sigma_2;
										Pc *= Pn_tmp;
									}
								}
							}
						}
						
						if (Pc <= threshold2)
						{
							db[i + j * ww] = 255;
						}
					}
				}
			}
		}
	}

	private void precalculatedensityFunction()
	{
		gaussian_table = new double[256][256];
		ln_gaussian_table = new double[256][256];

		for (int xt = 0; xt < 256; xt++)
		{
			for (int x = 0; x < 256; x++)
			{
				gaussian_table[xt][x] = Math.exp(-0.5 * Math.pow((xt - x), 2));
				ln_gaussian_table[xt][x] = -0.5 * Math.pow((xt - x), 2);
			}
		}
	}

	private double densityEstimation(PImage[] image, int xt, double inv_sigma_2, double threshold, int pixel)
	{
		double P = 0, tmp;
		int x, l = image.length;
		int[] db;
		// Compute probability to be foreground or background
		for (int k = 0; k < l - 1; k++)
		{
			db = image[k].pixels;
			x = db[pixel];
			P += Math.exp(-0.5 * Math.pow((xt - x), 2) * inv_sigma_2);
			P *= inv_sqrt_2pi * inv_sigma_2;
			tmp = P / l;
			if (tmp >= threshold)
				return tmp;
		}
		return P / l;
	}
}

