package dimension;

import Common.MyProperties;
import MyClass.Joint;
import MyClass.Segment;
import MyClass.SelectReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolyLine extends PointsD {

	public PointD small;
	public double delta;
	public PointD maxstep;
	public String name = "";
	public File file;
	public String path;
	public String trig = "";

//	PointD minstep;
	public PolyLine(double x[], double y[], double dt) {
		super(x, y);
		delta = dt;
		initialize();
	}

	public PolyLine(double y[], double delta) {
		super(y, y);
		for (int i = 0; i < n; i++) {
			pt[i].p[X] = delta * (double) i;
			pt[i].p[Y] = y[i];
		}
		this.delta = delta;

		initialize();
	}

	public PolyLine(double x[], double y[]) {
		super(x, y);
		delta = x[1] - x[0];

		initialize();
	}

	public PolyLine(double x[][]) {
		super(x);
		try {
			if (x.length != 0 && x[0].length > 1) {
				delta = x[PointD.X][1] - x[PointD.X][0];
			} else {
				System.out.println("error at PolyLine line:50");
				delta = 0.05;
			}
			initialize();
		} catch (ArrayIndexOutOfBoundsException ex) {
			ex.printStackTrace();
			delta = 0;
		}
	}

	public PolyLine(PolyLine pl) {
		super(new double[pl.dim][pl.n]);
		delta = pl.delta;
		n = pl.n;
		for (int i = 0; i < pl.dim; i++) {
			for (int j = 0; j < pl.n; j++) {
				pt[j].p[i] = pl.pt[j].p[i];
			}
		}
		initialize();
	}

	public PolyLine(PolyLine pl, String path) {
		this(pl);
		setPath(path);
		initialize();
	}

	public PolyLine(File file) throws FileNotFoundException, IOException {
		super(file);
		delta = pt[1].p[X] - pt[0].p[X];
		setPath(file.getAbsolutePath());
	}

	public static PolyLine[] getPolyLinesWavelett(File file) throws FileNotFoundException, IOException {
		SelectReader sr = null;
		double[] x;
		double[][] y;
		int[] level;
		int n;
		try {
			sr = new SelectReader(new FileReader(file));
			String str = sr.readLineWithoutC();
			int count = 0;
			while (str != null) {
				count++;
				str = sr.readLineWithoutC();
			}
			sr.close();
			sr = new SelectReader(new FileReader(file));
			StringTokenizer st = new StringTokenizer((String) MyProperties.prop.get("WAVELETT_LEVEL"));
			level = new int[st.countTokens()];
			for (int i = 0; i < level.length; i++) {
				level[i] = Integer.parseInt(st.nextToken()) + 1;
			}
			n = count;
			x = new double[n];
			y = new double[level.length][n];
			for (int i = 0; i < n; i++) {
				st = new StringTokenizer(sr.readLineWithoutC(), " \t\n\r\f,");
				x[i] = Double.parseDouble(st.nextToken());
				count = 0;
				String tkn;
				for (int j = 0; j <= level[level.length - 1]; j++) {
					tkn = st.nextToken();
					if (j == level[count]) {
						y[count][i] = Double.parseDouble(tkn);
						count++;
					}
				}
			}
		} catch (FileNotFoundException ex) {
			throw new FileNotFoundException();
		} catch (IOException ex) {
			throw new IOException();
		} finally {
			try {
				sr.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		PolyLine[] pl = new PolyLine[level.length];
		for (int i = 0; i < level.length; i++) {
			pl[i] = new PolyLine(x, y[i]);
			pl[i].setFile(file);
			pl[i].setPath(file.getAbsolutePath());
			pl[i].setName(file.getName().substring(0, file.getName().lastIndexOf(".")) + "_wavelett" + (new DecimalFormat("00")).format(level[i] - 1));
		}
		return pl;
	}

	public void setPath(String path) {
		this.file = new File(path);
		this.name = this.file.getName();
		this.path = path;
		try {
			trig = name.substring(name.lastIndexOf("."), name.length());
			name = name.substring(0, name.lastIndexOf("."));
		} catch (StringIndexOutOfBoundsException e) {
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	public String getPaht(){
		return path;
	}
	
	public File getFile(){
		return file;
	}
	
	public String getName(){
		return name;
	}

	public void initialize() {
		super.initialize();
		setMinMax();
	}

	protected void setMinMax() {
		super.setMinMax();
		int maxstep1 = 0;
		int maxstep2 = 0;
		double sml[] = new double[dim];
		double mxs[] = new double[dim];
		for (int i = 0; i < dim; i++) {
			sml[i] = Double.MAX_VALUE;
			for (int j = 0; j < n; j++) {
				if (0 < pt[j].p[i] && pt[j].p[i] < sml[i]) {
					sml[i] = pt[j].p[i];
				}
				if (min.p[i] > pt[j].p[i]) {
					min.p[i] = pt[j].p[i];
					maxstep1 = j;
				}
				if (max.p[i] < pt[j].p[i]) {
					max.p[i] = pt[j].p[i];
					maxstep2 = j;
				}
			}
			if (Math.abs(max.p[i]) > Math.abs(min.p[i])) {
				mxs[i] = maxstep2 * delta;
			} else {
				mxs[i] = maxstep1 * delta;
			}
		}
		small = new PointD(sml);
		maxstep = new PointD(mxs);
	}

	public PolyLine subLinePTS(int str, int end) {
//	double p[][]=new double[dim][end-str];
		PolyLine pl = this.clone();
		pl.pt = new PointD[end - str];
		for (int j = str; j < end; j++) {
			pl.pt[j - str] = new PointD(new double[2]);
			for (int i = 0; i < dim; i++) {
				pl.pt[j - str].p[i] = pt[j].p[i];
			}
		}
		pl.setPath(file.getAbsolutePath());
		pl.trig = this.trig;
		pl.addToPath("_sub");
		pl.initialize();
		return pl;
	}

	public PolyLine subLine(double strX, double endX) {
		int count = 0;
		for (int j = 0; j < n; j++) {
			if (strX <= pt[j].p[X] && pt[j].p[X] <= endX) {
				count++;
			}
		}
		double[][] p = new double[dim][count];
		count = 0;
		for (int j = 0; j < n; j++) {
			if (strX <= pt[j].p[X] && pt[j].p[X] <= endX) {
				for (int i = 0; i < dim; i++) {
					p[i][count] = pt[j].p[i];
				}
				count++;
			}
		}
		PolyLine pl = new PolyLine(p);
		pl.setPath(file.getAbsolutePath());
		pl.addToPath("_sub");
		return pl;
	}

	public void addToPath(String add) {
		name = name + add;
		file = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(Joint.s) + 1) + name + trig);
	}

	/**
	 * public static String additionalPath(PolyLine pl1,PolyLine pl2){
	 * String a=pl1.path.getAbsolutePath();
	 * String b=pl2.path.getAbsolutePath();
	 * String c="";
	 * int str=0;
	 * boolean ft=true;
	 * for(int i=0;i<a.length();i++){
	 * if(a.charAt(i)!=b.charAt(i)){
	 * if(ft)str=i;
	 * c+=a.charAt(i);
	 * ft=false;
	 * }
	 * }
	 * return a.substring(0,str)+c+"_"+b.substring(str);
	 * }
	 */
	public void changeTrig(String trig) {
		this.trig = trig;
		file = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("\\")));
	}

	public void changeDir(String dir) {
		Matcher m = dirP2.matcher(file.getAbsolutePath());
		setPath(m.replaceFirst(dir));
	}

	public String getHead() {
		String head = name;
		try {
			if (head.contains("_CH-")) {
				head = name.substring(0, name.indexOf("-") + 1);
			} else if (head.contains("_")) {
				head = name.substring(0, name.indexOf("_") + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return head;
	}
	private static final String dirS = "[UNER][DSWC]";
	private static final String idS = "[BR0-9][F]";
	private static final Pattern calibP = Pattern.compile(".*\\d{3}" + dirS + ".*");
	private static final Pattern obsP = Pattern.compile(".*[nsewcomrlay]{3}" + dirS + ".*");
	private static final Pattern calibID = Pattern.compile(".*(\\d{3}).*");
	private static final Pattern obsID = Pattern.compile(".*(..[nsewcomrlay]{3}).*");
	private static final Pattern obsIDP2 = Pattern.compile(idS);
	private static final Pattern dirP = Pattern.compile(".*(" + dirS + ").*");
	private static final Pattern dirP2 = Pattern.compile(dirS);
	private static final Pattern vseP = Pattern.compile(".*_CH-\\d.*");
	private static final Pattern vseID = Pattern.compile(".*_CH-(\\d).*");

	public String getID() {
		String id = name;
		try {
			Matcher m;
			if (calibP.matcher(id).matches()) {
				m = calibID.matcher(id);
			} else if (obsP.matcher(id).matches()) {
				m = obsID.matcher(id);
			} else if (vseP.matcher(id).matches()) {
				m = vseID.matcher(id);
			} else {
				throw new Exception();
			}
			m.matches();
			id = m.group(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

	public String getDir() {
		String dir = name;
		try {
			Matcher m = dirP.matcher(dir);
			m.matches();
			dir = m.group(1);
		} catch (Exception e) {/*e.printStackTrace();*/
			dir = "";
		}
		return dir;
	}

	public PolyLine clone() {
		double p[][] = new double[dim][pt.length];
		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < pt.length; j++) {
				double p1 = pt[j].p[i];
				p[i][j] = p1;
			}
		}
		return new PolyLine(p);
	}

	public void output(File opf) {
		opf = new File(opf.getAbsolutePath().substring(0, opf.getAbsolutePath().lastIndexOf(".")) + ".csv");
		output(opf, false);
	}

	public void output(File opf, String type) {
		opf = new File(opf.getAbsolutePath().substring(0, opf.getAbsolutePath().lastIndexOf(".")) + "." + type);
		outputD(opf, false);
	}

	public void outputD(File opf, boolean append) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(opf, append));
			for (int j = 0; j < n; j++) {
				for (int i = 0; i < dim - 1; i++) {
					bw.write(pt[j].p[i] + "\t");
				}
				bw.write(pt[j].p[dim - 1] + "\n");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void output(File opf, boolean append) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(opf, append));
			for (int j = 0; j < n; j++) {
				for (int i = 0; i < dim - 1; i++) {
					bw.write(pt[j].p[i] + ",");
				}
				bw.write(pt[j].p[dim - 1] + "\n");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public PolyLine[] sepLine(Segment seg) {
		double x[] = new double[seg.n2];
		double y[] = new double[seg.n2];
		PolyLine pl[] = new PolyLine[seg.getSegN()];
		for (int i = 0; i < seg.getSegN(); i++) {
			for (int j = 0; j < seg.nd; j++) {
				x[j] = this.pt[seg.point[Segment.str][i] + j].p[X];
				y[j] = this.pt[seg.point[Segment.str][i] + j].p[Y];
			}
			pl[i] = new PolyLine(x, y, this.delta);
		}
		return pl;
	}

	public PolyLine[] sepHanLine(Segment seg) {
		double x[] = new double[seg.n2];
		double y[] = new double[seg.n2];
		double f0 = 0.5d;
		double f1 = 0.5d;
		final double omega = 2d * Math.PI / (double) (seg.nd - 1);
		PolyLine pl[] = new PolyLine[seg.getSegN()];
		for (int i = 0; i < seg.getSegN(); i++) {
			for (int j = 0; j < seg.nd; j++) {
				x[j] = this.pt[seg.point[Segment.str][i] + j].p[X];
				y[j] = this.pt[seg.point[Segment.str][i] + j].p[Y] * (f0 + f1 * Math.cos(omega * (double) j - Math.PI));
			}
			pl[i] = new PolyLine(x, y, this.delta);
		}
		return pl;
	}

	public static PolyLine additionY(PolyLine pl1, PolyLine pl2) {
		return basicMath(pl1, pl2, "addition");
	}

	public static PolyLine subtractionY(PolyLine pl1, PolyLine pl2) {
		return basicMath(pl1, pl2, "subtraction");
	}

	public static PolyLine multiplitionY(PolyLine pl1, PolyLine pl2) {
		return basicMath(pl1, pl2, "multiplition");
	}

	public static PolyLine divisionY(PolyLine pl1, PolyLine pl2) {
		return basicMath(pl1, pl2, "division");
	}

	private static PolyLine basicMath(PolyLine pl1, PolyLine pl2, String type) {
		if (pl1.dim != 2 || pl2.dim != 2) {
			return null;
		} else {
			double[] x = null, y = null;
			int n = Math.min(pl1.n, pl2.n);
			if (pl1.delta == pl2.delta) {
				x = new double[n];
				y = new double[n];
				if (type.equals("addition")) {
					for (int i = 0; i < n; i++) {
						x[i] = i * pl1.delta;
						y[i] = pl1.pt[i].p[Y] + pl2.pt[i].p[Y];
					}
				} else if (type.equals("subtraction")) {
					for (int i = 0; i < n; i++) {
						x[i] = i * pl1.delta;
						y[i] = pl1.pt[i].p[Y] - pl2.pt[i].p[Y];
					}
				} else if (type.equals("multiplition")) {
					for (int i = 0; i < n; i++) {
						x[i] = i * pl1.delta;
						y[i] = pl1.pt[i].p[Y] * pl2.pt[i].p[Y];
					}
				} else if (type.equals("division")) {
					for (int i = 0; i < n; i++) {
						x[i] = i * pl1.delta;
						if (pl2.pt[i].p[Y] != 0) {
							y[i] = pl1.pt[i].p[Y] / pl2.pt[i].p[Y];
						} else {
							y[i] = Double.POSITIVE_INFINITY;
						}
					}
				} else {
					int count = 0;
					for (int i = 0; i < pl1.n; i++) {
						for (int j = 0; j < pl2.n; j++) {
							if (pl1.delta * i == pl2.delta * j) {
								count++;
							}
						}
					}
					x = new double[count];
					y = new double[count];
					if (type.equals("addition")) {
						for (int i = 0; i < pl1.n; i++) {
							for (int j = 0; j < pl2.n; j++) {
								if (pl1.delta * i == pl2.delta * j) {
									x[i] = i * pl1.delta;
									y[i] = pl1.pt[i].p[Y] + pl2.pt[i].p[Y];
								}
							}
						}
					} else if (type.equals("subtraction")) {
						for (int i = 0; i < pl1.n; i++) {
							for (int j = 0; j < pl2.n; j++) {
								if (pl1.delta * i == pl2.delta * j) {
									x[i] = i * pl1.delta;
									y[i] = pl1.pt[i].p[Y] - pl2.pt[i].p[Y];
								}
							}
						}
					} else if (type.equals("multiplition")) {
						for (int i = 0; i < pl1.n; i++) {
							for (int j = 0; j < pl2.n; j++) {
								if (pl1.delta * i == pl2.delta * j) {
									x[i] = i * pl1.delta;
									y[i] = pl1.pt[i].p[Y] * pl2.pt[i].p[Y];
								}
							}
						}
					} else if (type.equals("division")) {
						for (int i = 0; i < pl1.n; i++) {
							for (int j = 0; j < pl2.n; j++) {
								if (pl1.delta * i == pl2.delta * j) {
									x[i] = i * pl1.delta;
									y[i] = pl1.pt[i].p[Y] / pl2.pt[i].p[Y];
								}
							}
						}
					}
				}
			}
			return new PolyLine(x, y);
		}
	}
}
