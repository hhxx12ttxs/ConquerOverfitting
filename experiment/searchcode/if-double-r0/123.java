package com.googlecode.gchart.gcharttestapp.client;

import java.util.Arrays;

import com.google.gwt.user.client.ui.HTML;
import com.googlecode.gchart.client.GChart;
import com.googlecode.gchart.client.GChartCanvasFactory;
import com.googlecode.gchart.client.GChartCanvasLite;

/**
 * Simple proof-of-concept performance test for ASCII-Art based raster
 * rendering.
 * <p>
 *
 * The idea performed surprisingly well, getting close to VML in
 * performance in some tests, and around half the VML (via GWTCanvas)
 * speeds in others.
 * <p>
 * 
 * The idea proved unworkable, mainly because, since the browser sees
 * the pixelated characters as text, a plethora of text related options,
 * will screw up the graphical image if employed by the user. For
 * example, changing the encoding to Chinese will change the color of
 * the curve from red to a yellowish orange in this example. Also,
 * different browsers handle zooming differently for text, zooming out
 * can make text disappear (FF3) or become a giant block of color (IE7)
 * Selecting the "Zoom Text Only" option in FF overrides the fixed size
 * text specified, distorting the image hugely.
 * <p>
 *
 * Another problem is that intensity changes as you zoom in because the
 * small character size covers less and less of the total curve length.
 * <p>
 *
 * Although some of these problems could be worked around, perhaps, big
 * picture is that text is just too different to be used as a reliable
 * raster pixel in the browser. Much wiser to wrestle with VML's
 * inconsistencies than with these, methinks.
 * <p>
 *
 * Still, it is kind of a sad commentary on VML that ASCII art is almost
 * as fast...
 * 
 */
public class TestGChart61 extends GChart {
	final static int HEIGHT = 200;
	final static int WIDTH = 1000;
	final static int MAX_POINTS = 1000;

	static final class PreVG extends HTML implements GChartCanvasLite {

		PreVG() {
			super();
			setPixelSize(WIDTH, HEIGHT);
		}


		private final static char pixels[] = new char[(HEIGHT)*(WIDTH + 1)];
    private static double[] x = new double[MAX_POINTS];
    private static double[] y = new double[MAX_POINTS];
    private static int nPts = 0; // number of points actually in buffer now

    // remembers 'lineTo' points
    private void logPoint(double xIn, double yIn) {
      if (nPts < MAX_POINTS) { // not yet full buffer
        nPts++;
      } else {
        throw new IllegalStateException("Max allowed points "
                                        + MAX_POINTS + " exceeded.");
      }
      x[nPts - 1] = xIn;
      y[nPts - 1] = yIn;
    }

    static int iPass = 0;
    public void beginPath() {
      nPts = 0;
      if (iPass++ > 0)
        rasterize(' ');
      else {
      Arrays.fill(pixels, 0, pixels.length, ' ');
      for (int iRow = 0; iRow < HEIGHT; iRow++) {
        pixels[iRow*(WIDTH+1) + WIDTH] = '\n';
      }
      }
    }
    public void beginPathX() {
      for (int iRow = 0; iRow < HEIGHT; iRow++) {
        int r0 = iRow*(WIDTH+1);
        for (int iCol = 0; iCol < WIDTH; iCol++)
          pixels[r0 + iCol] = ' ';
        pixels[r0 + WIDTH] = '\n';
      }
      nPts = 0;
		}

    final StringBuffer spixels = new StringBuffer();
    public void closePath() {}
    private void rasterize(char c) {
	        int x0 = (int) x[0];
	        int y0 = (int) y[0];
	        for (int i = 1; i < nPts; i++) {
	          int x1 = (int) x[i];
	          int y1 = (int) y[i];
	          if (x1 != x0 || y1 != y0) {
	            int xsign = (x1 >= x0) ? 1 : -1;
	            int ysign = (y1 >= y0) ? 1 : -1;
	            if (xsign * (x1 - x0) > ysign * (y1 - y0)) {
	              for (int ix = x0; ix != x1; ix += xsign) {
	                int iy = (y0 * (x1 - ix) + y1 * (ix - x0))
	                         / (x1 - x0);
	                int r0 = iy*(WIDTH+1);
	                if (r0 + ix < pixels.length)
	                  pixels[r0 + ix] = c;
	              }
	            } else {
	              for (int iy = y0; iy != y1; iy += ysign) {
	                int r0 = iy*(WIDTH+1);
	                int ix = (x0 * (y1 - iy) + x1 * (iy - y0))
	                         / (y1 - y0);
	                if (r0 + ix < pixels.length)
	                  pixels[r0 + ix] = c;
	              }
	            }
	          }
	          x0 = x1;
	          y0 = y1;
	        }
		}

//    public void stroke() { setHTML("HTML");}
    public void stroke() {
		String s0 = "<pre style='font: 1px/1px monospace; letter-spacing:0px; color:rgb(255,0,0); '>";
    StringBuffer sb = new StringBuffer(s0);
    rasterize('X');
    spixels.delete(0, spixels.length());
    spixels.append(pixels);			
        sb.append(spixels);
        sb.append("</pre>");
        String result = sb.toString();
//				String result = "<pre style='font: 1px/1px monospace; letter-spacing:0px; color:rgb(255,0,0); '>HTML</pre>";
		setHTML(result);
		}

		public void fill() {
		}

		public void moveTo(double xIn, double yIn) {
		}

		public void lineTo(double xIn, double yIn) {
			logPoint(xIn, yIn);
		}

		// GChartCanvasLite requires CSS/RGBA color strings, but
		// GWTCanvas uses its own Color class instead, so we wrap:
		public void setStrokeStyle(String cssColor) {
		}

		public void setFillStyle(String cssColor) {
		}

		public void arc(double x, double y, double radius, double startAngle,
				double endAngle, boolean antiClockwise) {
			// TODO Auto-generated method stub

		}

		public void clear() {
			// TODO Auto-generated method stub

		}

		public void resize(int width, int height) {
			// TODO Auto-generated method stub

		}

		public void setLineWidth(double width) {
			// TODO Auto-generated method stub

		}
	} // class PreVG

	static final class PreVGFactory implements GChartCanvasFactory {
		public GChartCanvasLite create() {
			GChartCanvasLite result = new PreVG();
			return result;
		}
	}

	TestGChart61() {
		final double PERIOD = 200;
		final double NCURVES = 5;
//    GChart.setCanvasFactory(new GWTCanvasBasedCanvasFactory());
		GChart.setCanvasFactory(new PreVGFactory());
    setChartSize(WIDTH, HEIGHT);
    getXAxis().setTickCount(0);
    getYAxis().setTickCount(0);
    getXAxis().setAxisMax(MAX_POINTS);
		for (int iC = 0; iC < NCURVES; iC++) {
      addCurve();
      getCurve().getSymbol().setHoverAnnotationEnabled(false);
      getCurve().getSymbol().setHoverSelectionEnabled(false);
			// solid, 2px thick connecting lines:
			getCurve().getSymbol().setSymbolType(SymbolType.LINE);
			getCurve().getSymbol().setWidth(0);
			getCurve().getSymbol().setFillThickness(1);
			getCurve().getSymbol().setFillSpacing(0);
			for (int i = 0; i < MAX_POINTS; i++)
				getCurve().addPoint(i,
						Math.sin(2 * Math.PI * (i / PERIOD + iC / NCURVES)));
		}
	}
}

