package selectingmeropsmatrix;

import java.awt.geom.QuadCurve2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MainSelecting	 {

	private int[][] Matrix = new int[20][8];
	private double[][] DMatrix = new double[20][8];
	private int[] interesting = new int[20];
	private String proteasename;
	private String matrixbasedon;
	private String MatrixData;
	private String meropsurl;
	private List<BufferedReader> files = new ArrayList<BufferedReader>();
	private double [] values =null;

	public MainSelecting(String filename) throws IOException {
		PrintStream csvWriter = null;
		try {
		// Lire les donnees pour chaque proteases
		BufferedReader bReader = createBufferedreader(filename);
		File processedLines = new File("processedLines.txt");
		PrintStream out = null;
		List<String> processedLinesList = new ArrayList<String>(); 
		if(processedLines.exists()) {
			BufferedReader bprocessed = createBufferedreader(processedLines.getName());
			String processedLine;  
			while((processedLine = bprocessed.readLine()) != null) {
				processedLinesList.add(processedLine);
			}
		}
		else {
			out = new PrintStream(processedLines);
		}
		
		String line;

		LinkedList<MatrixEntry> allmatrixout = new LinkedList<MatrixEntry>();
		// Faire le fichier
		File resultsFile = new File("testresult.txt");
		FileOutputStream fout = new FileOutputStream(resultsFile, true);
		csvWriter = new PrintStream(fout);
		
		while ((line = bReader.readLine()) != null) {
			if (!processedLinesList.contains(line)) {
				String splitarray[] = line.split("\t");
				proteasename = splitarray[0];
				matrixbasedon = splitarray[1];
				MatrixData = splitarray[2];
				meropsurl = splitarray[3];
				
				System.out.println("-----------------");
				populateData(csvWriter, processSingleTable());
				//allmatrixout.add();
				out.println(line);
			}
		}

		
		} catch (FileNotFoundException ex) {
			Logger.getLogger(MainSelecting.class.getName()).log(Level.SEVERE,
					null, ex);
		} 
		finally {
			csvWriter.close();
		}
	}

	private MatrixEntry processSingleTable() {
		if (!MatrixData.contains("xxx")) {
			return createEmptyMatrixElem();
		}

		String splitmatrix[] = MatrixData.split("xxx");
		System.out.println(MatrixData);


		// Choisir celles qui ont au moins une colonne avec un aa max 10>=
		// aa min
		for (int i = 0; i < 8; i++) {
			for ( int k = 0; k < 20; k++ ) {
				Matrix[k][i] = Integer.parseInt(splitmatrix[i+8*k]);					
			}
		}

		boolean interestingP4 = isInteresting(0);
		boolean interestingP3 = isInteresting(1);
		boolean interestingP2 = isInteresting(2);
		boolean interestingP1 = isInteresting(3);
		boolean interestingP1prime = isInteresting(4);
		boolean interestingP2prime = isInteresting(5);
		boolean interestingP3prime = isInteresting(6);
		boolean interestingP4prime = isInteresting(7);


		// check whether the matrix is interesting
		if (!(interestingP4 || interestingP3 || interestingP2 || interestingP1 || interestingP1prime
				|| interestingP2prime || interestingP3prime|| interestingP4prime)) 
		{
			return createEmptyMatrixElem();
		}

		MatrixEntry proteaseentry = new MatrixEntry();
		proteaseentry.setProteasesymbol(proteasename);
		proteaseentry.setMeropsurl(meropsurl);
		int[] nP4 = new int[20];
		int[] nP3 = new int[20];
		int[] nP2 = new int[20];
		int[] nP1 = new int[20];
		int[] nP1prime = new int[20];
		int[] nP2prime = new int[20];
		int[] nP3prime = new int[20];
		int[] nP4prime = new int[20];

		for (int k=0; k<20;k++) {
			nP4[k] = Matrix[k][0];
		}
		proteaseentry.nP4 = Arrays.toString(nP4);
		proteaseentry.nP4 = proteaseentry.nP4.replaceAll("\\[", "");
		proteaseentry.nP4 = proteaseentry.nP4.replaceAll("\\]", "");		
		for (int k=0; k<20;k++) {
			nP3[k] = Matrix[k][1];
		}
		proteaseentry.nP3 = Arrays.toString(nP3);
		proteaseentry.nP3 = proteaseentry.nP3.replaceAll("\\[", "");
		proteaseentry.nP3 = proteaseentry.nP3.replaceAll("\\]", "");
		for (int k=0; k<20;k++) {
			nP2[k] = Matrix[k][2];
		}
		proteaseentry.nP2 = Arrays.toString(nP2);
		proteaseentry.nP2 = proteaseentry.nP2.replaceAll("\\[", "");
		proteaseentry.nP2 = proteaseentry.nP2.replaceAll("\\]", "");
		for (int k=0; k<20;k++) {
			nP1[k] = Matrix[k][3];
		}
		proteaseentry.nP1 = Arrays.toString(nP1);
		proteaseentry.nP1 = proteaseentry.nP1.replaceAll("\\[", "");
		proteaseentry.nP1 = proteaseentry.nP1.replaceAll("\\]", "");
		for (int k=0; k<20;k++) {
			nP1prime[k] = Matrix[k][4];
		}
		proteaseentry.nP1prime = Arrays.toString(nP1prime);
		proteaseentry.nP1prime = proteaseentry.nP1prime.replaceAll("\\[", "");
		proteaseentry.nP1prime = proteaseentry.nP1prime.replaceAll("\\]", "");
		for (int k=0; k<20;k++) {
			nP2prime[k] = Matrix[k][5];
		}
		proteaseentry.nP2prime = Arrays.toString(nP2prime);
		proteaseentry.nP2prime = proteaseentry.nP2prime.replaceAll("\\[", "");
		proteaseentry.nP2prime = proteaseentry.nP2prime.replaceAll("\\]", "");
		for (int k=0; k<20;k++) {
			nP3prime[k] = Matrix[k][6];
		}
		proteaseentry.nP3prime = Arrays.toString(nP3prime);
		proteaseentry.nP3prime = proteaseentry.nP3prime.replaceAll("\\[", "");
		proteaseentry.nP3prime = proteaseentry.nP3prime.replaceAll("\\]", "");
		for (int k=0; k<20;k++) {
			nP4prime[k] = Matrix[k][7];
		}
		proteaseentry.nP4prime = Arrays.toString(nP4prime);
		proteaseentry.nP4prime = proteaseentry.nP4prime.replaceAll("\\[", "");
		proteaseentry.nP4prime = proteaseentry.nP4prime.replaceAll("\\]", "");

		boolean validityP4 = isValid(0);
		boolean validityP3 = isValid(1);
		boolean validityP2 = isValid(2);
		boolean validityP1 = isValid(3);
		boolean validityP1prime = isValid(4);
		boolean validityP2prime = isValid(5);
		boolean validityP3prime = isValid(6);
		boolean validityP4prime = isValid(7);

		double P4 = 1;
		double P3 = 1;
		double P2 = 1;
		double P1 = 1;
		double P1prime = 1;
		double P2prime = 1; 
		double P3prime = 1;
		double P4prime = 1;

		int dP4 = validityP4 ? Integer.parseInt(matrixbasedon) : 1;
		int dP3 = validityP3 ? Integer.parseInt(matrixbasedon) : 1;
		int dP2 = validityP2 ? Integer.parseInt(matrixbasedon) : 1;
		int dP1 = validityP1 ? Integer.parseInt(matrixbasedon) : 1;
		int dP1prime = validityP1prime ? Integer.parseInt(matrixbasedon) : 1;
		int dP2prime = validityP2prime ? Integer.parseInt(matrixbasedon) : 1; 
		int dP3prime = validityP3prime ? Integer.parseInt(matrixbasedon) : 1;
		int dP4prime = validityP4prime ? Integer.parseInt(matrixbasedon) : 1;

		proteaseentry.dP4 = dP4;
		proteaseentry.dP3 = dP3;
		proteaseentry.dP2 = dP2;
		proteaseentry.dP1 = dP1;
		proteaseentry.dP1prime = dP1prime;
		proteaseentry.dP2prime = dP2prime;
		proteaseentry.dP3prime = dP3prime;
		proteaseentry.dP4prime = dP4prime;

		for ( int i = 0; i < 20; i++ ) {
			DMatrix[i][0] = validityP4 ? (Matrix[i][0] != 0 ? Math.log(Matrix[i][0]) : -1.0) : 0.0;
			DMatrix[i][1] = validityP3 ? (Matrix[i][1] != 0 ? Math.log(Matrix[i][1]) : -1.0) : 0.0;
			DMatrix[i][2] = validityP2 ? (Matrix[i][2] != 0 ? Math.log(Matrix[i][2]) : -1.0) : 0.0;
			DMatrix[i][3] = validityP1 ? (Matrix[i][3] != 0 ? Math.log(Matrix[i][3]) : -1.0) : 0.0;
			DMatrix[i][4] = validityP1prime ? (Matrix[i][4] != 0 ? Math.log(Matrix[i][4]) : -1.0) : 0.0;
			DMatrix[i][5] = validityP2prime ? (Matrix[i][5] != 0 ? Math.log(Matrix[i][5]) : -1.0) : 0.0;
			DMatrix[i][6] = validityP3prime ? (Matrix[i][6] != 0 ? Math.log(Matrix[i][6]) : -1.0) : 0.0;
			DMatrix[i][7] = validityP4prime ? (Matrix[i][7] != 0 ? Math.log(Matrix[i][7]) : -1.0) : 0.0;
		}
		double lndP4 = Math.log(dP4);
		double lndP3 = Math.log(dP3);
		double lndP2 = Math.log(dP2);
		double lndP1 = Math.log(dP1);
		double lndP1prime = Math.log(dP1prime);
		double lndP2prime = Math.log(dP2prime);
		double lndP3prime = Math.log(dP3prime);
		double lndP4prime = Math.log(dP4prime);



		for (int iP4 = 0; iP4 < 20; iP4++ ) {
			P4 = DMatrix[iP4][0];
			if (P4 >= 0) {
				for (int iP3 = 0; iP3 < 20; iP3++ ) {
					P3 = DMatrix[iP3][1];
					if (P3 >= 0) {
						ArrayList<Double> lnprobaarray = new ArrayList<Double>();

						for (int iP2 = 0; iP2 < 20; iP2++ ) {
							P2 = DMatrix[iP2][2];
							if (P2 >= 0) {
								for (int iP1 = 0; iP1 < 20; iP1++ ) {
									P1 = DMatrix[iP1][3];
									if (P1 >= 0) {
										for (int iP1p = 0; iP1p < 20; iP1p++ ) {
											P1prime = DMatrix[iP1p][4];
											if (P1prime >= 0) {
												for (int iP2p = 0; iP2p < 20; iP2p++ ) {
													P2prime = DMatrix[iP2p][5];
													if (P2prime >= 0) {
														for (int iP3p = 0; iP3p < 20; iP3p++ ) {
															P3prime = DMatrix[iP3p][6];
															if (P3prime >= 0) {
																for (int iP4p = 0; iP4p < 20; iP4p++ ) {
																	P4prime = DMatrix[iP4p][7];
																	if (P4prime>=0) {

																		double proba = P4 + P3 + P2 + P1 + P1prime 
																				+ P2prime + P3prime + P4prime 
																				- (lndP4 + lndP3 + lndP2 + lndP1 + lndP1prime 
																					+ lndP2prime + lndP3prime + lndP4prime );
																		lnprobaarray.add(proba);
																	}
																}
															}	
														}
													}	
												}
											}	
										}
									}	
								}
							}	
						}

						System.out.println(lnprobaarray.size());
						Collections.sort(lnprobaarray);
						
						String Filename = new String("file-"+iP4+"-"+iP3+".txt");
						try {
							PrintStream file = new PrintStream(Filename);
							file.print(lnprobaarray.size());
							for ( double d: lnprobaarray) {
								file.print("\n");
								file.print(d);
							}
							file.close();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		// setup files
		long total = 0;
		for ( int i = 0; i < 20; i++ ) 
			for ( int j = 0; j < 20; j++ ) {
				try {
					File file = new File("file-"+i+"-"+j+".txt");
					BufferedReader f  = createBufferedreader("file-"+i+"-"+j+".txt");
					String sizeString = f.readLine();
					if ( sizeString == null )
						continue;
					long size = Integer.parseInt(sizeString); 
					if ( size == 0 )
						continue;
					total += size;
					files.add(f);
					file.delete();
				} catch (FileNotFoundException e) {
					continue;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		// setup sorter
		values = new double[files.size()];
		for ( int i = 0; i < files.size(); i++ ) {
			values[i] = getNewValue(i);
		}
		
		// do the thing!!
		long current = 0;
		double [] Q = new double[3];
		for ( int i = 1; i < 4; i++ ) {
			long limit = total/4*i;
			while ( current++ != limit )
				findMin();
			Q[i-1] = findMin();
		}
		
		System.out.println("total: " + total);
		System.out.println(Q[2]);
		System.out.println(Q[1]);
		System.out.println(Q[0]);

		proteaseentry.setQ75(Q[2]);
		proteaseentry.setQ50(Q[1]);
		proteaseentry.setQ25(Q[0]);
		
		return proteaseentry;

	}

	private double findMin() {
		double cand = values[0];
		int candi = 0;
		for ( int i = 1; i < files.size(); i++ )
			if ( cand > values[i] ) {
				cand = values[i];
				candi = i;
			}
		values[candi] = getNewValue(candi);
		return cand;
	}

	private double getNewValue(int i) {
		String s;
		try {
			s = files.get(i).readLine();
		} catch (IOException e) {
			return 1;
		}
		if ( s == null )
			return 1;
		return Double.parseDouble(s);
	}

	private MatrixEntry createEmptyMatrixElem() {
		MatrixEntry proteaseentry = new MatrixEntry();
		proteaseentry.setProteasesymbol(proteasename);
		proteaseentry.setMeropsurl(meropsurl);

		proteaseentry.nP4 = "-";
		proteaseentry.nP3 = "-";
		proteaseentry.nP2 = "-";
		proteaseentry.nP1 = "-";
		proteaseentry.nP1prime = "-";
		proteaseentry.nP2prime = "-";
		proteaseentry.nP3prime = "-";
		proteaseentry.nP4prime = "-";

		proteaseentry.dP4 = 0;
		proteaseentry.dP3 = 0;
		proteaseentry.dP2 = 0;
		proteaseentry.dP1 = 0;
		proteaseentry.dP1prime = 0;
		proteaseentry.dP2prime = 0;
		proteaseentry.dP3prime = 0;
		proteaseentry.dP4prime = 0;

		return proteaseentry;
	}

	public static void main(String[] args) throws Throwable {
		// TODO code application logic here
		MainSelecting MainSelecting = new MainSelecting(args[0]);
	}

	private BufferedReader createBufferedreader(String datafilename)
			throws FileNotFoundException {
		BufferedReader bReader = new BufferedReader(
				new FileReader(datafilename));
		return bReader;

	}

	private void populateData(PrintStream csvWriter, MatrixEntry entry) {

		csvWriter.print(entry.getProteasesymbol());
		csvWriter.print("\t");
		csvWriter.print(entry.getnP4());
		csvWriter.print("\t");
		csvWriter.print(entry.getnP3());
		csvWriter.print("\t");
		csvWriter.print(entry.getnP2());
		csvWriter.print("\t");
		csvWriter.print(entry.getnP1());
		csvWriter.print("\t");
		csvWriter.print(entry.getnP1prime());
		csvWriter.print("\t");
		csvWriter.print(entry.getnP2prime());
		csvWriter.print("\t");
		csvWriter.print(entry.getnP3prime());
		csvWriter.print("\t");
		csvWriter.print(entry.getnP4prime());
		csvWriter.print("\t");
		csvWriter.print(entry.getdP4());
		csvWriter.print("\t");
		csvWriter.print(entry.getdP3());
		csvWriter.print("\t");
		csvWriter.print(entry.getdP2());
		csvWriter.print("\t");
		csvWriter.print(entry.getdP1());
		csvWriter.print("\t");
		csvWriter.print(entry.getdP1prime());
		csvWriter.print("\t");
		csvWriter.print(entry.getdP2prime());
		csvWriter.print("\t");
		csvWriter.print(entry.getdP3prime());
		csvWriter.print("\t");
		csvWriter.print(entry.getdP4prime());
		csvWriter.print("\t");
		csvWriter.print(entry.getQ75());
		csvWriter.print("\t");
		csvWriter.print(entry.getQ50());
		csvWriter.print("\t");
		csvWriter.print(entry.getQ25());
		csvWriter.print("\t");
		csvWriter.print(entry.getMeropsurl());
		csvWriter.print("\n");
	}

	private boolean isInteresting (int i) {

		for ( int k = 0; k < 20; k++) {
			interesting[k] = Matrix[k][i];	
		}
		Arrays.sort(interesting);
		int min = interesting[0];
		int max = interesting[19];

		if ((min == 0) && (max < 10)) {
			return false;
		} else if ((min >= 1) && (max < 10 * min)) {
			return false;
		}

		return true;
	}

	private boolean isValid ( int j ) {
		for ( int k = 0; k < 20; ++k ) {
			if ( Matrix[k][j] != 0 )
				return true;
		}
		return false;
	}

}

