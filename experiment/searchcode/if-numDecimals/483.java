/**
 * @author Eric D. Dill eddill@ncsu.edu
 * @author James D. Martin jdmartin@ncsu.edu
 * Copyright � 2010-2013 North Carolina State University. All rights reserved
 */
package pdfgetx3;


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

public class ParseX3Output implements Runnable {

	private double xMin, xMax, yMin, yMax;
	private int idx = 0;
	private File[] files;
	
	private String extension = ".gr";
	private String exptName = "230-80-#2";
	private String datExtension = ".txt";
	private String infoExtension = ".info";
	
	private File folder = new File("E:\\Documents referenced in lab notebooks\\Dill-12\\34\\b\\230-145-#2\\total scattering pdf\\8\\");
	private File output = folder;
	private File[] folders;
	private String[][] summaryU, summaryS, summaryV;
	private int[] singularVectorIndices = new int[] {1, 2, 3, 4, 5};
	private boolean xLimits = false, yLimits = false;
		
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
		
		if(xLimits) {
			s += "x limits\n\txMin = " + xMin + "\txMax = " + xMax + "\n";
		} 
		if(yLimits) {
			s += "y limits\n\tyMin = " + yMin + "\tyMax = " + yMax + "\n";
		}

		return s;
	}
	public ParseX3Output() {
		folders = new File[] {folder};
	}
	public ParseX3Output(File f) {
		folder = f;
		output = folder;
		folders = new File[] {folder};
	}
	
	public ParseX3Output(File[] folders, File output) {
		this.folders = folders;
		this.output = output;
	}
	public void run()  {
		int curIdx = 0;

		Vector<String[][]> allOutput;
		Vector<String> info;
		Vector<String> outputFileNames;
		int[] lsvColIdx, rsvColIdx;
		
		// init output 
		allOutput = new Vector<String[][]>();
		info = new Vector<String>();
		outputFileNames = new Vector<String>();
		for(int i = 0; i < folders.length; i++) {
			for(int k = 0; k < singularVectorIndices.length; k++) {
				String outName = "SVD -- " + exptName + " -- " + i + " -- " + singularVectorIndices[k];
				outputFileNames.add(outName);
				allOutput.add(new Vector<String>());
				info.add(new Vector<String>());
			}
		}
		
		// create output
		for(int j = 0; j < folders.length; j++) {
			System.out.println("Processing folder " + (j+1) + " / " + folders.length);
			folder = folders[j];
			files = folder.listFiles();
			String root = files[0].getName();
			Vector<File> toSVD = new Vector<File>();
			Vector<String> fileNames = new Vector<String>();
			String name;
			int maxNumDataLines = 0;
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
			String tmp;
			for(int i = 0; i < fileIndices.length; i++) {
				
			}
			Files1DTo2DMatrix toMat = new Files1DTo2DMatrix(forSVD);
			PDFGetX3Output x3 = new PDFGetX3Output();
			if(xLimits) {
				x3.setxLimits(xLimits);
				x3.setxMin(xMin);
				x3.setxMax(xMax);
			}
			if(yLimits) {
				x3.setyLimits(yLimits);
				x3.setyMin(yMin);
				x3.setyMax(yMax);
			}
			toMat.setFileReader(x3);
			toMat.run();
			
			double[][] data = (double[][]) toMat.getData();
			
			SVD svd = new SVD(data, true);
			svd.run();
			
			double[][] U = svd.getU();
			double[][] V = svd.getV();
			double[][] S = svd.getSRow();
			if(j == 0) {
				
			}
			
			File individual = new File(output + File.separator + "individial folders");
			individual.mkdir();
			
			MyPrintStream mpsU = new MyPrintStream(new File(individual + File.separator + (j+1) + "--" + root + "--U"));
			MyPrintStream mpsV = new MyPrintStream(new File(individual + File.separator + (j+1) + "--" + root + "--S"));
			MyPrintStream mpsS = new MyPrintStream(new File(individual + File.separator + (j+1) + "--" + root + "--V"));
			
			String header;
			for(int i = 0; i < singularVectorIndices.length; i++) {
				Vector<String> singleOutput = new Vector<String>();
				header = "\t";
				for(int k = 0; )
				
			}
			
			Vector<String> curOutput;
			
			// set up output headers
			
			int maxRows = Math.max(U.length, V.length);
			
			
			for(int i = 0; i < maxRows.length; i++) {
			
			}
			for(int i = 0; i < U.length; i++) {
				if(j == 0) {
					//first file in series, set up row indices
					if(i == 0) {
						// first row U matrix
					} else {
						// install row indices
						for(int k = 0; k < r_indices.length; k++) {
							summaryU[i][r_indices[k]] = "" + U[i][0];
						}
					}
				}
				for(int k = 0; k < col_indices.length; k++) {
					if(i == 0) {
						// columm header
						summaryU[i][col_indices[k]] = singularVectorIndices[k] + " -- " + (j+1);
					} else {
						summaryU[i][col_indices[k]] = U[i][singularVectorIndices[k]] + "";
					}
				}
				mpsU.println(StringConverter.arrayToTabString(U[i]));
			}
			for(int i = 0; i < fileIndices.length+1; i++) {
				if(j == 0) {
					//first file in series, set up row indices
					if(i == 0) {
						// first row U matrix
					} else {
						// install row indices
						for(int k = 0; k < r_indices.length; k++) {
							summaryV[i][r_indices[k]] = fileIndices[i-1];
						}
					}
				}
				for(int k = 0; k < col_indices.length; k++) {
					if(i == 0) {
						// column header
						summaryV[i][col_indices[k]] = singularVectorIndices[k] + " -- " + (j+1);
					} else {
						// data
						summaryV[i][col_indices[k]] = V[i][singularVectorIndices[k]] + "";
					}
				}
				mpsV.println(StringConverter.arrayToTabString(V[i]));
			}
			// sum singular values
			double totalVal = 0;
			for(int i = 0; i < S[1].length; i++) {
				totalVal += S[1][i];
			}
			for(int i = 0; i < S.length; i++) {
				if(j == 0) {
					// do nothing
				}
				for(int k = 0; k < col_indices.length; k++) {
					if(i == 0) {
						summaryS[i][col_indices[k]] = singularVectorIndices[k] + " -- " + (j+1);
					} else {
						summaryS[i][col_indices[k]] = (S[i][singularVectorIndices[k]-1]/totalVal*100) + "";
					}
				}
				mpsS.println(StringConverter.arrayToTabString(S[i]));
			}
		}
		printSummary(allOutput, info, fileNames);
	}
	
	private void printSummary(Vector<Vector<String>> output, Vector<Vector<String>> info, Vector<String> fileNames) {
		MyPrintStream mps = new MyPrintStream(new File(output.getParent() + File.separator + "SVD_Summary -- " + idx + ".txt"));

		mps.println("Singular values");
		for(int i = 0; i < summaryS.length; i++) {
			mps.println(StringConverter.arrayToTabString(summaryS[i]));
		}
		
		mps.println("\nLeft singular vectors");
		for(int i = 0; i < summaryU.length-2; i++) {
			mps.println(StringConverter.arrayToTabString(summaryU[i]));
		}

		mps.println("\nRight singular vectors");
		for(int i = 0; i < summaryV.length-1; i++) {
			mps.println(StringConverter.arrayToTabString(summaryV[i]));
		}
		mps.println("\n\n\n" + getInfo());
	}
	
	private void extendUArray(int newLen) {
		String[][] newU = new String[newLen][summaryU[0].length];
		for(int i = 0; i < newU.length; i++) {
			Arrays.fill(newU[i], "");
		}
		for(int i = 0; i < summaryU.length; i++) {
			for(int j = 0; j < summaryU[i].length; j++) {
				newU[i][j] = summaryU[i][j];
			}
		}
		summaryU = newU;
	}
	public static void main(String[] args) {
		String drive = "C";
		String root = ":\\Documents referenced in lab notebooks\\Dill-12\\34\\analyzed\\aps2009\\";
		root = ":\\$temp\\pdfgetx3\\12-34-a\\out\\";
		int idx = 1;
		int startNum = 2;
		int finishNum = 7;
		int[] qmin = new int[] {0, 1, 2, 3, 4, 5, 7, 9, 11, 15, 25, 30, 35, 40};
		int[] qmax = new int[] {50, 25, 10, 7, 5, 4, 3, 2};
		double[][] limits = new double[][] {{1.81, 2.77}, {3.29, 4.17}, {4.17, 11.17}, {11.17, 15}, {0, 1.81}, {11.17, 20}, {11.17, 30}, {11.17, 40}, {11.17, 50}};
		String extension = "gr";
		String exptName = "230-80-#2";
		double qMinVal, qMaxVal;
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
				System.out.println("Make output folder: " + output.mkdir());
				ParseX3Output x3 = new ParseX3Output(folders, output);
				x3.xMin = qMinVal;
				x3.xMax = qMaxVal;
				x3.xLimits = true;
				x3.idx = idx++;
				x3.extension = extension;
				x3.run();
//			}
		}
	}
}

