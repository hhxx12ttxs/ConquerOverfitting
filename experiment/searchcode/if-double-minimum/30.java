// Ue04 Bildverarbeitung SS2013
// Prof. K. Jung

//Kay Schoppe
//Anna Münster

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

public class Histo extends JPanel {

	// some constants
	//
	private static final long serialVersionUID = 1L;
	private static final int histWidth = 256;
	private static final int histHeight = 256;
	private static final int layoutBorder = 10;
	private static final int maxImageWidth = 605;
	private static final int maxImageHeight = 600;

	private int minimum = 0;
	private int maximum = 0;
	private int median = 0;
	double mittelwert = 0.0;
	double varianz = 0.0;
	double entropie = 0.0;

	private int[] imgPixels;
	private int[] origPixels;
	private int[] histoOrig;

	// main frame
	//
	private static JFrame frame;

	// layout items
	//
	private ImageView imgView; // image view
	private ImageView histoView; // histogram view
	private JLabel[] label = new JLabel[8]; // text display

	// internal status

	private int[] h; // häufigkeiten
	private double[] p; // wahrscheinlichkeiten
	private int imageSize;

	private JSlider contrastSlider, brightnessSlider;
	private int brightness;
	private double contrast;

	public Histo() {
		super(new BorderLayout(layoutBorder, layoutBorder));

		// load the default image
		File input = new File("mountains.png");

		h = new int[256];
		p = new double[256];
		minimum = 255;
		contrast = 1.0;
		brightness = 0;

		if (!input.canRead())
			input = openFile(); // file not found, choose another image

		imgView = new ImageView(input);
		imgView.setMaxSize(new Dimension(maxImageWidth, maxImageHeight));
		imgPixels = imgView.getPixels();

		origPixels = imgView.getPixels().clone();

		// create an empty histogram image
		histoView = new ImageView(histWidth, histHeight);

		histoOrig = histoView.getPixels().clone();

		// load image button
		JButton load = new JButton("Open Image ");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File input = openFile();
				if (input != null) {
					imgView.loadImage(input);
					imgView.setMaxSize(new Dimension(maxImageWidth,
							maxImageHeight));
					frame.pack();
					resetImage();
				}
			}
		});

		// text display
		JPanel controlPanel = new JPanel(new GridLayout(5, 2));

		String[] string = { "Maximum: ", "Minimum: ", "Median: ",
				"Mittelwert: ", "Varianz: ", "Entropie", "Helligkeit",
				"Kontrast" };

		for (int i = 0; i < 8; i++) {
			label[i] = new JLabel(string[i]);
			controlPanel.add(label[i]);
		}

		// slider
		contrastSlider = new JSlider(0, 50, 10); //create slider from 0 to 5, no contrast means 1
		contrastSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				contrast = (double) contrastSlider.getValue();
				contrast = contrast / 10.0;
				changePic();
			}
		});

		brightnessSlider = new JSlider(-128, 128, brightness);
		brightnessSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				brightness = brightnessSlider.getValue();
				changePic();
			}
		});

		//add them to the panel
		controlPanel.add(brightnessSlider);
		controlPanel.add(contrastSlider);

		JPanel images = new JPanel(new FlowLayout());
		images.add(imgView);
		images.add(histoView);

		add(load, BorderLayout.NORTH);
		add(images, BorderLayout.CENTER);
		add(controlPanel, BorderLayout.SOUTH);

		setBorder(BorderFactory.createEmptyBorder(layoutBorder, layoutBorder,
				layoutBorder, layoutBorder));

		// perform the initial scaling
		resetImage();
		fillArray(); //fill h
		drawHisto(); // make a histogram of it
		updateText(); //change the textes

	}

	private File openFile() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
		chooser.setFileFilter(filter);
		int ret = chooser.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile();
		return null;
	}

	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Statistical Image Analysis");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JComponent newContentPane = new Histo();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// display the window.
		frame.pack();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		frame.setLocation((screenSize.width - frame.getWidth()) / 2,
				(screenSize.height - frame.getHeight()) / 2);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private void resetImage() {
		// a new image has been laoded
		drawHisto();
	}

	/*
	 * apply contrast and brightness
	 */
	private void changePic() {

		int pixels[] = origPixels.clone(); //take the pixels from the original image

		for (int pos = 0; pos < pixels.length; pos++) {

			// get pixel
			int c = pixels[pos];

			// get RGB values
			int r = (c & 0xff0000) >> 16;
			int g = (c & 0x00ff00) >> 8;
			int b = (c & 0x0000ff);

			// apply contrast - multiply with contrast-factor
			r = (int) (r * contrast);
			g = (int) (g * contrast);
			b = (int) (b * contrast);

			// apply brightness - add brightness summand
			r += brightness;
			g += brightness;
			b += brightness;

			// normalize - values are between 0 and 255 now
			r = normalize(r);
			g = normalize(g);
			b = normalize(b);

			// restore pixel
			pixels[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8)
					+ (b & 0xff);
		}
		imgPixels = pixels;
		fillArray(); //update h- and p-array and calculate values
		updatePic();
		drawHisto();
	}

	private void updatePic() {
		int[] pixels = imgPixels.clone();
		imgView.setPixels(pixels);
		updateText();
	}

	// ueberlauf abfangen
	// zu RGB normalisieren (0; 255)
	public int normalize(int a) {
		if (a < 0) {
			a = 0;
			return a;
		}
		if (a > 255) {
			a = 255;
			return a;
		} else {
			// do nothing
			return a;
		}
	}

	/*
	 * set the calculated values for maximum, mittelwert, minimum, varianz, median, entropie, helligkeit, kontrast
	 */
	private void updateText() {

		label[0].setText("Maximum: " + maximum);
		label[1].setText("Mittelwert: " + mittelwert);
		label[2].setText("Minimum: " + minimum);
		label[3].setText("Varianz: " + varianz);
		label[4].setText("Median: " + median);
		label[5].setText("Entropie: " + entropie);
		label[6].setText("Helligkeit: " + brightness);
		label[7].setText("Kontrast: " + contrast);
	}

	/*
	 * fill the h-array (häufigkeiten)
	 * that is needed for the histogram
	 */
	void fillArray() {

		maximum = 0;
		minimum = 255;
		Arrays.fill(h, 0);

		int[] pixels = imgView.getPixels();
		imageSize = pixels.length;
		for (int pos = 0; pos < pixels.length; pos++) {

			// get pixel
			int c = pixels[pos];

			// get RGB values
			int r = (c & 0xff0000) >> 16;
			int g = (c & 0x00ff00) >> 8;
			int b = (c & 0x0000ff);

			//calculate max and min
			int avg = (r + g + b) / 3;
			if (avg > maximum)
				maximum = avg;
			if (avg < minimum && avg != 0)
				minimum = avg;
			h[avg]++;
		}
		
		//calculate information sizes
		calcMittelwert();
		calcMedian();
		calcVarianz();
		fillPArray(); //change the values of the h-array to the propability
		calcEntropie(); //calculate the entropie with it
		updateText(); //update the calculated information
	}

	/*
	 * calculate an array with propabilities of the frequentnesses
	 * to calculate the entropie
	 */
	void fillPArray() {

		int[] pixels = imgView.getPixels();
		imageSize = pixels.length; // anzahl von h = 100% = 1
		double factor = (100.0 / (double) imageSize) / 100.0;
		for (int pos = 0; pos < h.length; pos++) {
			p[pos] = (double) h[pos] * factor; //multiply the frequency with the scale-factor
		}
	}


	/**
	 * calc all the image information sizes
	 */
	void calcMittelwert() {
		int[] pixels = imgView.getPixels();
		double sum = 0;
		for (int pos = 0; pos < pixels.length; pos++) {

			// get pixel
			int c = pixels[pos];

			// get RGB values
			int r = (c & 0xff0000) >> 16;
			int g = (c & 0x00ff00) >> 8;
			int b = (c & 0x0000ff);

			double grey = (r + g + b) / 3;
			sum += grey;
		}

		mittelwert = sum / imageSize;

	}

	void calcMedian() {
		int[] pixels = imgView.getPixels();
		int[] imageArray = new int[imageSize];
		for (int pos = 0; pos < pixels.length; pos++) {

			// get pixel
			int c = pixels[pos];

			// get RGB values
			int r = (c & 0xff0000) >> 16;
			int g = (c & 0x00ff00) >> 8;
			int b = (c & 0x0000ff);

			int grey = (r + g + b) / 3;
			imageArray[pos] = grey;
		}

		Arrays.sort(imageArray);
		median = imageArray[imageSize / 2];
	}

	void calcEntropie() {

		double sum = 0.0;
		for (int pos = 0; pos < p.length; pos++) {
			if(p[pos] == 0){
				//sum does not change because otherwise logarithm is out of bounds
			}
			else{
			sum = sum + p[pos] * (Math.log(p[pos]) / Math.log(2));
			}
		}

		entropie = -sum;

	}

	void calcVarianz() {
		int[] pixels = imgView.getPixels();
		double sum = 0;
		for (int pos = 0; pos < pixels.length; pos++) {

			// get pixel
			int c = pixels[pos];

			// get RGB values
			int r = (c & 0xff0000) >> 16;
			int g = (c & 0x00ff00) >> 8;
			int b = (c & 0x0000ff);

			double grey = (r + g + b) / 3;
			grey = Math.pow(grey - mittelwert, 2);
			sum = sum + grey;
		}

		varianz = sum / imageSize;

	}

	/*
	 * find the highest value in an array and return it
	 */
	int searchHighestValue(int[] array) {
		int highestValue = 0;
		for (int p = 0; p < array.length; p++) {
			if (array[p] > highestValue) {
				highestValue = array[p];
			}
		}
		return highestValue;
	}


	/**
	 * draw histogram where all frequencies of color-values are shown
	 */
	void drawHisto() {
		int[] pixels = histoOrig;
		double max = searchHighestValue(h);
		double scaleFactor = histHeight / max; //to normalize the length of the histogram-peaks

		for (int x = 0; x < histWidth; x++) {
			for (int y = 0; y < histHeight; y++) {

				int pos = x * histWidth + y;
				double value = h[y] * scaleFactor;
				int hoehe = (int) value; //height of the peaks

				int white = histHeight - hoehe;
//				int black = hoehe;
				//black + white = histHeight
				
				if (x < white) {
					// paint it white (from above)
					pixels[pos] = 0xffffffff;
				} else {
					// paint it black (the rest after the white stripe)
					pixels[pos] = 0xff000000;

				}
			}
		}
		histoView.setPixels(pixels);
	}

}

