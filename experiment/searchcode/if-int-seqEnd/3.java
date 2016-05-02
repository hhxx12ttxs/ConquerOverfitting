package simulate;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JApplet;
import javax.swing.JFrame;

public class Visualization extends JApplet {

	GenomeSimpleRep sample_genome = null;
	ArrayList<PairedEndRead> concordant_reads = null;
	Cluster read_cluster = null;
	boolean iambadatprogramming = false;
	ArrayList<Color> clustColors = null;
	
	public Visualization(GenomeSimpleRep g, ArrayList<PairedEndRead> cr,
			Cluster rc, ArrayList<Color> colors, boolean wtf) {
		sample_genome = g;
		concordant_reads = cr;
		read_cluster = rc;
		iambadatprogramming = wtf;
		clustColors = colors;

	}

	public void drawStuff(boolean writeToFile) {

		int windowSizeX = 1200;
		int windowSizeY = 400;

		JFrame f = new JFrame("");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		JApplet applet = this;
		f.getContentPane().add("Center", applet);
		applet.init();
		f.pack();
		f.setSize(new Dimension(windowSizeX, windowSizeY));
		f.setVisible(true);

		// http://stackoverflow.com/questions/4515902/how-to-remove-the-title-bar-from-a-jframe-screenshot
		if (writeToFile) {
			
			String s = new Integer(sample_genome.hashCode()).toString();
			if(concordant_reads != null)
					s += new Integer(concordant_reads.hashCode()).toString();
			if(read_cluster != null)
					s += new Integer(read_cluster.hashCode()).toString();

			MessageDigest m;
			try {
				m = MessageDigest.getInstance("MD5");
				m.update(s.getBytes(), 0, s.length());
				String fileName = new BigInteger(1, m.digest()).toString(16)
						+ ".png";

				BufferedImage image = new BufferedImage(windowSizeX,
						windowSizeY, BufferedImage.TYPE_INT_ARGB);
				this.paint(image.getGraphics());
				ImageIO.write(image, "png", new File(fileName));
			} catch (Exception e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	final static Color bg = Color.black;
	final static Color fg = Color.white;

	final static BasicStroke stroke = new BasicStroke(2.0f);
	final static BasicStroke wideStroke = new BasicStroke(5.0f,
			BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
	final static BasicStroke arrowStroke = new BasicStroke(5.0f,
			BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);

	Dimension totalSize;
	FontMetrics fontMetrics;

	public void init() {
		// Initialize drawing colors
		setBackground(bg);
		setForeground(fg);
	}

	public void paint(Graphics g) {

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		Dimension d = getSize();

		// Drawing Constants

		int outerSpacing = 30;

		// ARROW CONSTANTS
		int arrowGap = 10;
		int arrowHeight = d.height - 50;
		int arrowStart = outerSpacing / 2;

		// ARROWHEAD CONSTANTS
		int numLengthY = 10;
		int numLengthX = 15;

		// NUMBER CONSTANTS
		int numberHeight = arrowHeight + 30;
		int numberGap = 20;

		// PAIREND CONSTANTS
		int peCurveHeight = arrowHeight - 100;
		int peHeight = arrowHeight - 20;
		int clustHeight = -100;

		// >>>>> START SIMULATION CODE
		/*
		 * GenomeSimpleRep sample_genome = new GenomeSimpleRep(30);
		 * sample_genome.print(); sample_genome.invert(15, 25);
		 * sample_genome.invert(10, 20); sample_genome.print();
		 */
		// <<<<< END SIMULATION CODE

		int[] genomeArray = sample_genome.toArray();
		ArrayList<Arrow> arrows = new ArrayList<Arrow>();

		// Initalize with first genome element
		int prev = genomeArray[0];
		int arrowFirst = genomeArray[0];

		// Loop through genome pulling out segments that are consecutive
		// and create arrow objects to add to array
		for (int i = 1; i < genomeArray.length; i++) {
			int curr = genomeArray[i];
			if (prev + 1 == curr || prev - 1 == curr) {
				prev = curr;
			} else {
				arrows.add(new Arrow(arrowFirst, prev));
				arrowFirst = curr;
				prev = curr;
			}
		}
		arrows.add(new Arrow(arrowFirst, prev));

		int numLength = (d.width - outerSpacing) / genomeArray.length;
		int numberWidth = numLength - numberGap;
		// Assign coordinates and directionality to array
		for (int i = 0; i < arrows.size(); i++) {
			Arrow arrow = arrows.get(i);
			arrow.x1 = arrowStart;
			arrowStart += (Math.abs(arrow.start - arrow.end) + 1) * numLength;
			arrow.x2 = arrowStart - arrowGap;
			if (arrow.start < 0 || arrow.end < 0)
				arrow.pointingForward = false;
		}

		Read[] numLocation = new Read[genomeArray.length];

		// Draw Arrows
		int ctr = 0;
		g2.setColor(Color.WHITE);
		for (int i = 0; i < arrows.size(); i++) {

			// Draw arrow body
			Arrow arrow = arrows.get(i);
			// System.out.println(arrow);
			g2.setStroke(wideStroke);
			g2.draw(new Line2D.Double(arrow.x1, arrowHeight, arrow.x2,
					arrowHeight));

			g2.setStroke(arrowStroke);
			// Draw arrow head depending on directionality
			if (arrow.pointingForward) {
				g2.draw(new Line2D.Double(arrow.x2, arrowHeight, arrow.x2
						- numLengthX, arrowHeight - numLengthY));
				g2.draw(new Line2D.Double(arrow.x2, arrowHeight, arrow.x2
						- numLengthX, arrowHeight + numLengthY));
			} else {
				g2.draw(new Line2D.Double(arrow.x1, arrowHeight, arrow.x1
						+ numLengthX, arrowHeight - numLengthY));
				g2.draw(new Line2D.Double(arrow.x1, arrowHeight, arrow.x1
						+ numLengthX, arrowHeight + numLengthY));
			}

			// Number arrows
			int numX = arrow.x1 + numLength / 2;
			int j;
			for (j = arrow.start; j <= arrow.end; j++) {
				g2.drawString(new Integer(j).toString(), numX, numberHeight);

				// Store number x locations for further use
				numLocation[ctr] = new Read();
				numLocation[ctr].mid = numX;
				ctr++;

				numX += numLength;

			}

		}

		// >>>>> START SIMULATION CODE
		/*
		 * ArrayList<PairedEndRead> reads = new ArrayList<PairedEndRead>();
		 * ArrayList<PairedEndRead> concordant_reads = new
		 * ArrayList<PairedEndRead>();
		 * 
		 * for (int i = 0; i < 40; i++) { PairedEndRead sample_read =
		 * sample_genome.pairedEndRead(4); if (!sample_read.isConcordant())
		 * reads.add(sample_read); else concordant_reads.add(sample_read); }
		 */
		// <<<<< END SIMULATION CODE

		// Make a hashmap from numbering to x value and
		// Assign appropriate lengths for reads
		HashMap<Integer, Visualization.Read> indexToRead = new HashMap<Integer, Visualization.Read>();
		for (int i = 0; i < numLocation.length; i++) {
			if (genomeArray[i] < 0) {
				//System.out.println("ehre" + iambadatprogramming);
				numLocation[i].seqStart = numLocation[i].mid + numberWidth / 2;
				numLocation[i].seqEnd = numLocation[i].mid - numberWidth / 2;
			} else {
				numLocation[i].seqStart = numLocation[i].mid - numberWidth / 2;
				numLocation[i].seqEnd = numLocation[i].mid + numberWidth / 2;
			}

			indexToRead.put(genomeArray[i], numLocation[i]);

		}
		
		ArrayList<Integer> readsToDraw = new ArrayList<Integer>();

		// Draw Concordant Reads
		if (concordant_reads != null) {
			for (int i = 0; i < concordant_reads.size(); i++) {
				g2.setStroke(stroke);
				// g2.setColor(Visualization.getColor());
				PairedEndRead read = concordant_reads.get(i);
				readsToDraw.add(read.first_location);
				readsToDraw.add(read.second_location);

				int num1 = indexToRead.get(new Integer(read.first_location)).seqEnd;
				int num2 = indexToRead.get(new Integer(read.second_location)).seqStart;
				int numMid = (num1 + num2) / 2;
				g2.draw(new QuadCurve2D.Double(num1, peHeight, numMid,
						peCurveHeight, num2, peHeight));
			}
		}

		// >>>>> START SIMULATION CODE
		// Cluster read_cluster = new Cluster(reads);
		// <<<<< END SIMULATION CODE

		ArrayList<ArrayList<PairedEndRead>> clusters = read_cluster.clustered_reads;
		
		// Draw clusters and label via coloring
		for (int i = 0; i < clusters.size(); i++) {
			//g2.setColor(Visualization.getColor());
			g2.setColor(clustColors.get(i));

			for (int j = 0; j < clusters.get(i).size(); j++) {
				g2.setStroke(stroke);
				PairedEndRead read = clusters.get(i).get(j);
				readsToDraw.add(read.first_location);
				readsToDraw.add(read.second_location);
				int num1, num2 = 0;
				if (iambadatprogramming) {
					if (Math.signum(read.first_location) < 0)
						num1 = indexToRead.get(Math.abs(new Integer(read.first_location))).seqStart;
					else
						num1 = indexToRead.get(Math.abs(new Integer(read.first_location))).seqEnd;
					
					if (Math.signum(read.second_location) < 0)
						num2 = indexToRead.get(Math.abs(new Integer(read.second_location))).seqEnd;
					else
						num2 = indexToRead.get(Math.abs(new Integer(read.second_location))).seqStart;
				} else {
					num1 = indexToRead.get(new Integer(read.first_location)).mid;
					num2 = indexToRead.get(new Integer(read.second_location)).mid;
				}
				int numMid = (num1 + num2 + i*10) / 2;
				g2.draw(new QuadCurve2D.Double(num1, peHeight, numMid,
						clustHeight, num2, peHeight));
			}
		}
		
		Visualization.removeDuplicate(readsToDraw);
		
		// Draw Read
		g2.setStroke(stroke);
		g2.setColor(Color.WHITE);
		for (int i = 0; i < readsToDraw.size(); i++) {
			Read num;
			if (iambadatprogramming) {
				num = indexToRead.get(Math.abs(new Integer (readsToDraw.get(i))));
			} else {
				num = indexToRead.get(new Integer (readsToDraw.get(i)));
			}
			
			g2.draw(new Line2D.Double(num.seqStart, peHeight,num.seqEnd, peHeight));
		}
		

	}

	/*
	 * public static void main(String s[]) {
	 * 
	 * // >>>>> START SIMULATION CODE GenomeSimpleRep sample_genome = new
	 * GenomeSimpleRep(30); sample_genome.print(); sample_genome.invert(15, 25);
	 * sample_genome.invert(10, 20); sample_genome.print();
	 * ArrayList<PairedEndRead> reads = new ArrayList<PairedEndRead>();
	 * ArrayList<PairedEndRead> concordant_reads = new
	 * ArrayList<PairedEndRead>();
	 * 
	 * for (int i = 0; i < 40; i++) { PairedEndRead sample_read =
	 * sample_genome.pairedEndRead(4); if (!sample_read.isConcordant())
	 * reads.add(sample_read); else concordant_reads.add(sample_read); } Cluster
	 * read_cluster = new Cluster(reads); // <<<<< END SIMULATION CODE
	 * 
	 * JFrame f = new JFrame(""); f.addWindowListener(new WindowAdapter() {
	 * public void windowClosing(WindowEvent e) { System.exit(0); } }); JApplet
	 * applet = new Visualization(sample_genome, concordant_reads,
	 * read_cluster); f.getContentPane().add("Center", applet); applet.init();
	 * f.pack(); f.setSize(new Dimension(1200, 400)); f.setVisible(true); }
	 */

	// http://www.devx.com/tips/Tip/20864
	public static void removeDuplicate(ArrayList<Integer> arlList)
	  {
	   HashSet<Integer> h = new HashSet<Integer>(arlList);
	   arlList.clear();
	   arlList.addAll(h);
	  }
	
	/**
	 * http://martin.ankerl.com/2009/12/09/how-to-create-random-colors-
	 * programmatically/
	 */
	static Random generator = new Random(12345);
	static double rand = generator.nextDouble();

	public static Color getColor() {
		double golden_ratio_conjugate = 0.618033988749895;
		rand += golden_ratio_conjugate;
		return new Color(Color.HSBtoRGB((float) rand, 0.99f, 0.99f));
	}
	
	public static ArrayList<Color> generateColor() {
		ArrayList<Color> colorList = new ArrayList<Color>();
		for (int i = 0; i < 50; i++) {
			colorList.add(getColor());
		}
		return colorList;
	}

	

	public class Read {
		int mid;
		int seqStart;
		int seqEnd;
		int num;
	}

}
