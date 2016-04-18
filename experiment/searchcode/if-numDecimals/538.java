/**
 * @author Eric D. Dill eddill@ncsu.edu
 * @author James D. Martin jdmartin@ncsu.edu
 * Copyright � 2010-2013 North Carolina State University. All rights reserved
 */
package pdfgetx3;


import io.MyFile;
import io.MyPrintStream;
import io.StringConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Vector;

import readFile.PDFGetX3Output;
import readFile.ReadFile;

import matrix.SVD;

import fileToMatrix.Files1DTo2DMatrix;
import formatting.FormatDouble;

public class ParseX3Output_v1 implements Runnable {

	private double xMin, xMax, yMin, yMax;
	private int idx = 0;
	private File[] files;
	
	private double timePerFrame = 2;
	
	private String extension = ".gr";
	
	private File folder = new File("E:\\Documents referenced in lab notebooks\\Dill-12\\34\\b\\230-145-#2\\total scattering pdf\\8\\");
	private File output = folder;
	private File[] folders;
	private String[][] summaryU, summaryS, summaryV;
	private int[] singularVectorIndices = new int[] {1, 2, 3, 4, 5};
	private boolean xLimited = false, yLimited = false;
	private Vector<double[][]> A, U, S, V, A_partial;
	private Vector<String> root;
	private Vector<String[][]> formattedOutput;
	private double[] rowLabels1_full, rowLabels2_full,
		rowLabels1_partial, rowLabels2_partial;
	
	private String getInfo() {
		String s = "ParseX3Output Parameters\n";
		s += "file extension = " + extension + "\n";
		s += "\nFiles processed:\n";
		for(int i = 0; i < folders.length; i++) {
			s += "Folder " + (i+1) + "/" + folders.length + ": " + folders[i].getAbsolutePath() + "\n";
			File[] filesInFolder = folders[i].listFiles();
			
			for(int j = 0; j < filesInFolder.length; j++) {
				if(filesInFolder[j].getName().contains(extension)) {
					s += "\t\t" + filesInFolder[j].getName() + "\n";
				}
			}
		}
		
		if(xLimited) {
			s += "x limits\n\txMin = " + xMin + "\txMax = " + xMax + "\n";
		} 
		if(yLimited) {
			s += "y limits\n\tyMin = " + yMin + "\tyMax = " + yMax + "\n";
		}

		return s;
	}
	public ParseX3Output_v1() {
		folders = new File[] {folder};
	}
	public ParseX3Output_v1(File f) {
		folder = f;
		output = folder;
		folders = new File[] {folder};
	}
	
	public ParseX3Output_v1(File[] folders, File output) {
		this.folders = folders;
		this.output = output;
	}
	public void run()  {
		constructA();
		printA();
		runSVD();
		formatOutput();
		printOutput();
	}

	private void printOutput() {
		MyPrintStream mps;
		String[][] out;
		for(int i = 0; i < formattedOutput.size(); i++) {
			mps = new MyPrintStream(new File(output.getParent() + File.separator + "SVD_Summary -- " + idx + " -- " + (i+1) + ".txt"));
			out = formattedOutput.get(i);
			mps.println();
			for(int j = 0; j < out.length; j++) {
				mps.println(StringConverter.arrayToTabString(out[j]));
			}
		}
	}
	
	private void formatOutput() {
		formattedOutput = new Vector<String[][]>();
		String[][] out;
		int maxRows = Math.max(rowLabels1_full.length, rowLabels2_full.length)+2;
		int numCols = 2 * (1 + U.size());
		double[][] curU, curV, curS;
		int curSingValIdx;
		int longNameRow = 0;
		int commentsRow = 1;
		int rsvCol = 0;
		int lsvCol = U.size() + 1;
		FormatDouble fd = new FormatDouble(FormatDouble.DecimalPlaces.THREE);
		for(int a = 0; a < singularVectorIndices.length; a++) {
			out = new String[maxRows][numCols];
			for(int i = 0; i < maxRows; i++) {
				for(int j = 0; j < numCols; j++) {
					out[i][j] = "";
				}
			}
			curSingValIdx = singularVectorIndices[a];
			out[longNameRow][rsvCol] = "time";
			out[commentsRow][rsvCol] = "s";
			out[longNameRow][lsvCol] = "G(r)";
			out[commentsRow][lsvCol] = "A";
			
			for(int i = 0; i < rowLabels2_full.length; i++) {
				out[i+2][rsvCol] = rowLabels2_full[i] * timePerFrame + "";
			}
			for(int i = 0; i < rowLabels1_full.length; i++) {
				out[i+2][lsvCol] = rowLabels1_full[i] + "";
			}
			for(int i = 0; i < U.size(); i++) {
				curU = U.get(i);
				curV = V.get(i);
				curS = S.get(i);
	
				out[longNameRow][rsvCol + i+1] = fd.format(curS[1][curSingValIdx-1]) + "";
				out[commentsRow][rsvCol + i+1] = curSingValIdx + " -- " + (i+1);
				
				out[longNameRow][lsvCol + i+1] = fd.format(curS[1][curSingValIdx-1]) + "";
				out[commentsRow][lsvCol + i+1] = curSingValIdx + " -- " + (i+1);
				
				for(int j = 1; j < curV.length; j++) {
					out[j+1][rsvCol + i + 1] = curV[j][curSingValIdx] + "";
				}
				for(int j = 1; j < curU.length; j++) {
					out[j+1][lsvCol + i + 1] = curU[j][curSingValIdx] + "";
				}
				
			}
			formattedOutput.add(out);
		}
	}
	
	private void runSVD() {
		U = new Vector<double[][]>();
		S = new Vector<double[][]>();
		V = new Vector<double[][]>();
		SVD svd;
		double[][] data;
		for(int i = 0; i < A.size(); i++) {
			data = A.get(i);
			svd = new SVD(data, true);
			svd.run();
			
			U.add(svd.getU());
			V.add(svd.getV());
			S.add(normalizeS(svd.getSRow()));
			
			printVerboseSVD(svd, i);
		}
	}
	private double[][] normalizeS(double[][] S) {
		double[][] normalized = new double[S.length][S[0].length];
		
		double sum = 0;
		for(int i = 0; i < normalized[0].length; i++) {
			sum += S[1][i];
		}
		
		for(int i = 0; i < normalized[0].length; i++) {
			normalized[0][i] = S[0][i];
			normalized[1][i] = S[1][i] / sum * 100.;
		}
		
		return normalized;
	}
	private void printVerboseSVD(SVD svd, int i) {
		double[][] dataU, dataS, dataV;
		
		dataU = svd.getU();
		dataV = svd.getV();
		dataS = svd.getSRow();

		File individual = new File(output + File.separator + "individial folders");
		System.out.println("Make individual folder: " + individual.mkdir());
		if(!individual.mkdir() && !individual.exists()) {
			System.out.println("Make individual folder: " + individual.mkdirs());
		}
		
		MyPrintStream mps = new MyPrintStream(new File(individual + File.separator + (i+1) + "--" + root.get(i) + "--SVD"));

		mps.println("Singular Values");
		for(int j = 0; j < dataS.length; j++) {
			mps.println(StringConverter.arrayToTabString(dataS[j]));
		}
		
		mps.println("\nLeft Singular Vectors");
		for(int j = 0; j < dataU.length; j++) {
			mps.println(StringConverter.arrayToTabString(dataU[j]));
		}
		
		mps.println("\nRight Singular Vectors");
		for(int j = 0; j < dataV.length; j++) {
			mps.println(StringConverter.arrayToTabString(dataV[j]));
		}
	}
	
	private void printA() {
		MyPrintStream mps;
		
		double[][] data;
		for(int a = 0; a < A.size(); a++) {
			mps = new MyPrintStream(new File(output + File.separator + "A -- " + a + ".txt"));
			data = A.get(a);
			for(int i = 0; i < data.length; i++) {
				mps.println(StringConverter.arrayToTabString(data[i]));
			}
			mps.close();
		}
	}
	
	private void constructA() {
		A = new Vector<double[][]>();
		root = new Vector<String>();
		for(int j = 0; j < folders.length; j++) {
			System.out.println("Processing folder " + (j+1) + " / " + folders.length);
			folder = folders[j];
			files = folder.listFiles();
			root.add(files[0].getName());
			Vector<File> toSVD = new Vector<File>();
			Vector<String> fileNames = new Vector<String>();
			String name;
			for(int i = 0; i < files.length; i++) {
				if(files[i].getName().contains(extension)) {
					toSVD.add(files[i]);
					name = files[i].getName();
					int numDecimals = 0;
					while(name.contains(".")) {
						name = name.substring(name.indexOf(".")+1);
						numDecimals++;
					}
					name = files[i].getName();
					for(int k = 0; k < numDecimals-1; k++) {
						name = name.substring(name.indexOf(".")+1);
					}
					name = name.substring(0, name.lastIndexOf("."));
					fileNames.add(name);
				}
			}
			File[] forSVD = new File[toSVD.size()];
			forSVD = toSVD.toArray(forSVD);
			
			String[] fileIndices = new String[fileNames.size()];
			fileIndices = fileNames.toArray(fileIndices);
			
			Files1DTo2DMatrix toMat = new Files1DTo2DMatrix(forSVD);
			PDFGetX3Output x3 = new PDFGetX3Output();
			if(xLimited) {
				x3.setxLimits(xLimited);
				x3.setxMin(xMin);
				x3.setxMax(xMax);
			}
			if(yLimited) {
				x3.setyLimits(yLimited);
				x3.setyMin(yMin);
				x3.setyMax(yMax);
			}
			toMat.setFileReader(x3);
			toMat.run();
			
			double[][] data = (double[][]) toMat.getData();
			rowLabels1_full = toMat.getRowLabels();
			rowLabels2_full = toMat.getColLabels();
			
			double[] x = (double[]) toMat.getRowLabels();
			
			A.add(data);
		}
	}
			
	public static void main(String[] args) {
		String drive = "C";
		String root = ":\\Documents referenced in lab notebooks\\Dill-12\\34\\analyzed\\aps2009\\";
		root = ":\\$temp\\pdfgetx3\\12-34-a\\out\\regular pdf\\";
		int idx = 1;
		int[] qmin = new int[] {0, 1, 2, 3, 4, 5, 7, 9, 11, 15, 25, 30, 35, 40};
		int[] qmax = new int[] {50, 25, 10, 7, 5, 4, 3, 2};
		double[][] limits = new double[][] {{1.81, 2.77}, {3.29, 4.17}, {4.17, 11.17}, {11.17, 15}, {0, 1.81}, 
				{0, 10}, {0, 15}, {0, 20}};
		limits = new double[][] {{0, 50}};
//		double[][] limits = new double[][] {{1.81, 2.77}, {3.29, 4.17}};
		double qMinVal, qMaxVal;
		Object[][] vals = new Object[][] {
				/*{"230-135", "gr", 2., 2, 4},
				{"230-135-#2", "gr", 4., 2, 4},
				{"230-145", "gr", 4., 1, 3},
				{"230-145-#2", "gr", 4., 1, 2},*/
				{"230-80-#2", "gr", 2., 2, 4}
		};
		for(int j = 0; j < vals.length; j++) {
			idx = 1;
			String exptName = (String) vals[j][0];
			String extension = (String) vals[j][1];
			double timePerFrame = (Double) vals[j][2];
			int startNum = (Integer) vals[j][3];
			int finishNum = (Integer) vals[j][4];
			for(int i = 0; i < limits.length; i++) {
	//		for(int i = 0; i < qmin.length; i++) {
	//			for(int j = 0; j < qmax.length; j++) {
	//				qMinVal = qmin[i];
	//				qMinVal = qmin[j];
					qMinVal = limits[i][0];
					qMaxVal = limits[i][1];
	//				if(qMinVal >= qMaxVal) {
	//					System.out.println("Skipping idx: " + idx + "\tqmin: " + qMinVal + "\tqmax: " + qMaxVal);
	//					continue;
	//				} else {
						System.out.println("idx: " + idx + "\tqmin: " + qMinVal + "\tqmax: " + qMaxVal);
	//				}
					
					Vector<File> foldersVec = new Vector<File>();
					String prefix = drive + root + exptName + File.separator;
					String suffix = "\\";
					for(int k = startNum; k <= finishNum; k++) {
						foldersVec.add(new File(prefix + k + suffix));
					}
					
					File[] folders = new File[foldersVec.size()];
					folders = foldersVec.toArray(folders);
					File output = new File(drive + root + exptName + "\\SVD\\" + extension + File.separator + idx + File.separator);
					if(!output.exists()) {
						System.out.println("Make output folder: " + output.getAbsolutePath() + "\n\tSuccess == " + output.mkdirs());
					}
					ParseX3Output_v1 x3 = new ParseX3Output_v1(folders, output);
					x3.xMin = qMinVal;
					x3.xMax = qMaxVal;
					x3.xLimited = true;
					x3.idx = idx++;
					x3.extension = extension;
					x3.timePerFrame = timePerFrame;
					x3.run();
	//			}
			}
		}
	}
}

