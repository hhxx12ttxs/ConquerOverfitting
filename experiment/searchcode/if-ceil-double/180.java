package Viewer;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import Util.TissueFinder;

import loci.formats.FormatException;
import loci.formats.gui.BufferedImageReader;
import loci.formats.in.SVSReader;
import loci.formats.tiff.IFDList;


public class Slide {
	private File file;
	private Grid grid;
	private double mpp;
	private double appMag;
	private int fieldWidth;
	private int fieldHeight;
	private ArrayList<Field> fields = new ArrayList<Field>();
	private ArrayList<Integer> tissueFields = new ArrayList<Integer>();
	private int level0Width;
	private int level0Height;
	private int seriesCount;
	private int labelSeries;
	private int macroSeries;
	private int level0Series = 0;
	private int thumbSeries = 1;
	private int thumbWidth;
	private int thumbHeight;
	private double thumbScale;
	private int currentField = 0; //current TISSUE field
	
	public Slide(File f, int fw, int fh) {
		this.file = f;
		this.fieldWidth = fw;
		this.fieldHeight = fh;
		readSlideInfo();
		setFields();
		findTissueFields();
	}
	
	public void readSlideInfo() {
		System.out.println("Reading IFDs from SVS file " +  this.file.getPath() + " ...");
		try {
			SVSReader svsr = new SVSReader();
			svsr.setId(this.file.getPath());
			// read the ifd info we need
			IFDList ifds = svsr.getIFDs();
			StringTokenizer imageInfo = new StringTokenizer(ifds.get(0).getComment(), "|");
			while(imageInfo.hasMoreTokens()) {
				String[] temp;
				temp = imageInfo.nextToken().split("=");
				if (temp.length == 2) {
					String key = temp[0];
					String val = temp[1]; 
					//System.out.println(key.trim() + "=" + val.trim());
					if (key.trim().equals("MPP")) {
						this.mpp = Double.parseDouble(val.trim());
						System.out.println(key.trim() + "=" + val.trim());
					} else if (key.trim().equals("AppMag")) {
						this.appMag = Double.parseDouble(val.trim());
						System.out.println(key.trim() + "=" + val.trim());	
					}
				}
			}
			
			seriesCount = svsr.getSeriesCount();
			level0Series = 0;
			labelSeries = seriesCount - 2;
			macroSeries = seriesCount - 1;				  
			//get level0Series info
			svsr.setSeries(level0Series);
			this.level0Width = svsr.getSizeX();
			this.level0Height = svsr.getSizeY();

			//use LOCI to open the thumbnail series then get an imageprocessor and a scaled version for the GUI
			svsr.setSeries(thumbSeries);
			thumbWidth = svsr.getSizeX();
			thumbHeight = svsr.getSizeY();
			thumbScale = ((double)thumbWidth)/level0Width;
			svsr.close();		
		} catch (FormatException exc) {
			System.err.println("Sorry, an error occurred: " + exc.getMessage());
		} catch (IOException exc) {
			System.err.println("Sorry, an error occurred: " + exc.getMessage());
		}
	}
	
	public void setGrid(Grid g){
		this.grid = g;
	}
	
	public Grid getGrid(){
		return this.grid;
	}
	
	public BufferedImage getThumbImage() {
		BufferedImage bi  = new BufferedImage(thumbWidth,thumbHeight,BufferedImage.TYPE_INT_RGB); 
		try {
			BufferedImageReader r = new BufferedImageReader();
			r.setId(this.file.getPath());
			r.setSeries(thumbSeries);
			bi = r.openImage(0,0,0,thumbWidth,thumbHeight); 
			r.close();
		} catch (FormatException exc) {
			System.err.println("Sorry, an error occurred: " + exc.getMessage());
		} catch (IOException exc) {
			System.err.println("Sorry, an error occurred: " + exc.getMessage());
		}
		return bi;
	}
	
	public void setFields() {
		this.fields.clear();
		int fieldCols = (int)Math.ceil((double)level0Width/(double)fieldWidth);
		int fieldRows = (int)Math.ceil((double)level0Height/(double)fieldHeight);
		int numFields = fieldCols * fieldRows;
		for (int row = 0; row < fieldRows; row++) {
			for (int col = 0; col < fieldCols; col++) {
				int x = col*fieldWidth;
				int y = row*fieldHeight;
				int w,h;
				if ((x + fieldWidth) > level0Width) {
					w = level0Width-x;
				} else {
					w = fieldWidth;
				}
				if ((y + fieldHeight) > level0Height) {
					h = level0Height-y;
				} else {
					h = fieldHeight;
				}
				this.fields.add(new Field(new Rectangle(x,y,w,h)));
			}
		}
	}

	public void findTissueFields() {
		this.tissueFields.clear();
		int macroWidth = 200;
		double thumbScale = ((double)thumbWidth)/level0Width;
		double thumbDisplayScale = ((double)macroWidth)/level0Width;
	
		ColorProcessor thumbCp = new ColorProcessor(getThumbImage());
		//thumbDisplayScale = ((double)macroW)/level0Width;
		TissueFinder tf = new TissueFinder();
		ByteProcessor tissueBp = tf.getTissueMask(thumbCp);
		ImagePlus imp = new ImagePlus("imp",tissueBp);
		//IJ.save(imp,"tissueBp.tif");
		int[][] tissueValues = tissueBp.getIntArray();
	
		for (int i = 0; i < countFields(); i++) {
			int x = getField(i).getX();
			int y = getField(i).getY();
			int w = getField(i).getWidth();
			int h = getField(i).getHeight();
			int thumbX = (int)Math.floor(x*thumbScale);
			int thumbY = (int)Math.floor(y*thumbScale);
			int thumbW = (int)Math.floor(w*thumbScale);
			int thumbH = (int)Math.floor(h*thumbScale);
			int thumbDisplayX = (int)Math.floor(x*thumbDisplayScale);
			int thumbDisplayY = (int)Math.floor(y*thumbDisplayScale);
			int thumbDisplayW = (int)Math.floor(w*thumbDisplayScale);
			int thumbDisplayH = (int)Math.floor(h*thumbDisplayScale);
			int pixelCount = 0;
			for (int j = thumbY; j < thumbY+thumbH; j++) {
				for (int k = thumbX; k < thumbX+thumbW; k++) {
					if (tissueValues[k][j] > 0) pixelCount++;
				}
			}

			if (pixelCount > 110) {
				getField(i).select();
				tissueFields.add(i);
//				thumbFields.add(new Rectangle(thumbDisplayX,thumbDisplayY,thumbDisplayW,thumbDisplayH));
			} else {
				// System.out.println("field "+i+" is empty");
			}
		}
	}
	
	public int countTissueFields() {
		return this.tissueFields.size();
	}
	
	private BufferedImage getFieldImage(int i) {
		BufferedImage bi  = new BufferedImage(fieldWidth,fieldHeight,BufferedImage.TYPE_INT_RGB); 
		try {
			BufferedImageReader r = new BufferedImageReader();
			r.setId(this.file.getPath());
			r.setSeries(this.level0Series);
			int x = getField(i).getX();
			int y = getField(i).getY();
			int w = getField(i).getWidth();
			int h = getField(i).getHeight();
			bi = r.openImage(0,x,y,w,h);
			r.close();
		} catch (FormatException exc) {
			System.err.println("Sorry, an error occurred: " + exc.getMessage());
		} catch (IOException exc) {
			System.err.println("Sorry, an error occurred: " + exc.getMessage());
		}
		return bi;
	}
	
	public BufferedImage getCurrentFieldImage() {
		int i = this.tissueFields.get(this.currentField);
		return getFieldImage(i);		
	}
	
	public void setCurrentField(int i) {
		this.currentField = i;
	}

	public void nextField() {
		this.currentField++;
		if (this.currentField == countTissueFields()) {
			this.currentField = 0;
		}
	}

	public void previousField() {
		this.currentField--;
		if (this.currentField < 0) {
			this.currentField = countTissueFields() - 1;
		}
	}

	public int countFlaggedFields() {
		int count = 0;
			for (Field f : this.fields) {
				if (f.isFlagged()) {
					count++;
				}
			}
		return count;
	}
	
	public void gotoNextFlag() {
		int i = this.currentField;		
		do {
			i++;					
			if (i == countTissueFields()) {
				i = 0;
			}
		} while (!(getTissueField(i).isFlagged()) && (i != this.currentField));
		this.currentField = i;		
	}
	
	public void gotoPreviousFlag() {
		int i = this.currentField;
		do {
			i--;					
			if (i < 0) {
				i = countTissueFields() - 1;
			}
		} while (!(getTissueField(i).isFlagged()) && (i != this.currentField));
		this.currentField = i;
	}
	
	public Field getCurrentField() {
		return getTissueField(this.currentField);
	}

	public int getCurrentFieldIndex() {
		return this.currentField;
	}
	
	public Field getField(int i) {
		return this.fields.get(i);
	}
	
	public Field getTissueField(int i) {
		return this.fields.get(this.tissueFields.get(i));
	}

	public ArrayList<Field> getTissueFields() {
		ArrayList<Field> myFields = new ArrayList<Field>();
		for (Integer i : this.tissueFields) {
			myFields.add(getField(i));
		}
		return myFields;
	}
	
	public int tissueFieldToField(int i) {
		return this.tissueFields.get(i);
	}
	
	public int countFields() {
		return this.fields.size();
	}
	
	public double getMpp() {
		return this.mpp;
	}
	
	public void setMpp(double m) {
		this.mpp = m;
	}

	public double appMag() {
		return this.appMag;
	}
	
	public void appMag(double am) {
		this.appMag = am;
	}
	
	public int getHeight() {
		return this.level0Height;
	}
	
	public void setHeight(int h) {
		this.level0Height = h;
	}
	
	public int getWidth() {
		return this.level0Width;
	}
	
	public void setWidth(int w) {
		this.level0Width = w;
	}

	public int getFieldHeight() {
		return this.fieldHeight;
	}
	
	public void setFieldHeight(int h) {
		this.fieldHeight = h;
	}
	
	public int getFieldWidth() {
		return this.fieldWidth;
	}
	
	public void setFieldWidth(int w) {
		this.fieldWidth = w;
	}	
	
	public double getThumbScale() {
		return this.thumbScale;
	}
	
	public int getThumbHeight() {
		return this.thumbHeight;
	}
		
	public int getThumbWidth() {
		return this.thumbWidth;
	}
	
	public int getThumbSeries() {
		return this.thumbSeries;
	}

	public File getFile() {
		return this.file;
	}

	public void setFile(File f) {
		this.file = f;
	}
	
	public String getFileName() {
		return this.file.getName();
	}
}

