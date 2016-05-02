import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

/**
 * The model to solve the sudoku puzzles. 
 * @author s0941897
 *
 */
public class SudokuSolver {

	//Variables including the grid and the energy. K is set to 1. STOP and CORRECT are check when the sim is iterated and stopped respecfully
	private int[][] grid;
	private int N;
	private Point[]	clues;
	private double energy =0;
	private double K =1;
	private double T;
	private boolean STOP = false;
	private boolean CORRECT = false;


	/**
	 * Create a new solver which inisialises a new n dimentional grid
	 * @param n dimention
	 * @throws IOException 
	 */
	public SudokuSolver(int n, double t)  {
		setN(n);
		setT(t);
		grid = new int[n][n];
		initGrid();
		try {
			if(n==9) sampleGrid(6);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Create a new solver the an exsisting set of clues and a grid
	 * @param g
	 * @param c
	 */
	public SudokuSolver(int[][] g, Point[] c) {
		setN(g.length);
		grid =g;
		clues = c;
		energy=getTotalEnergy();
	}
	/**
	 * Create a solver with a temp and sample file
	 * @param t
	 * @param m
	 */
	public SudokuSolver(double t, int c, int n){
		setN(9);
		setT(t);
		grid = new int[9][9];
		initGrid();
		try {
		    getGridOfNClues(c,n);
		}catch (IOException e){
		    e.printStackTrace();
		}
	}


	//Getters
	/**
	 * Get the array of interger that represent the grid
	 * @return the grid
	 */
	public int[][] getGrid(){
		return grid;
	}
	/**
	 * Get a grid element with x and y pos
	 * @return an element
	 */
	public int getGridElement(int x,int y){
		return grid[x][y];
	}
	/**
	 * Return gridsize
	 * @return gridsize
	 */
	public int getN(){
		return N;
	}
	/**
	 * Get the temperarre
	 * @return T
	 */
	public double getT(){
		return T;
	}
	/**
	 * Get the energy
	 * @return E
	 */
	public double getEnergy(){
		return energy;
	}
	/**
	 * Is the simulation stop
	 * @return true/ false
	 */
	public boolean isStopped(){
		return STOP;
	}
	/**
	 * is the solution correct or local minima
	 * @return true/flase
	 */
	public boolean isCorrect(){
		return CORRECT;
	}
	//Setters
	/**
	 * Set a grid elememt
	 * @param x xpos
	 * @param y ypos
	 * @param the new value
	 */
	public void setGridElement(int x,int y,int val){
		grid[x][y]=val;
	}
	/**
	 * Set the gridsize
	 * @param n grid size
	 */
	public void setN(int n){
		N=n;
	}
	/**
	 * Fills the grid with 0's
	 */
	public void initGrid(){
		for(int i=0;i<getN();i++){
			for(int j = 0;j<getN();j++){
				setGridElement(j, i, 0);
			}
		}
		STOP = false;
		CORRECT = false;
	}
/**
 * set the clues from an array of points
 * @param p clues
 */
	public void setClues(Point[] p){
		clues = p;
		//for(int i=0;i<p.length;i++){
			//System.out.println(p[i]);
		//}
	}
	/**
	 * Set the temperature
	 * @param t new val
	 */
	public void setT(double t){
		T=t;
	}

	//end of accesor methods
	
	/**
	 * check if site is a clue
	 * @param x xpos
	 * @param y ypos
	 * @return true/false
	 */
	public boolean isClue(int x, int y){
		if(clues==null) {
			return false;
		}
		for (int i = 0; i < clues.length; i++) {
			if((int)clues[i].getX()==x && (int)clues[i].getY()==y) return true;
		}

		return false;
	}

	/**
	 * Get the total energy by suming the number of each number in each sub set then returning c-1 for each number and subset
	 * 
	 */
	public void totalEnergy(){ 
		int[] tempSet = {0,0,0,0,0,0,0,0,0};
		double en =0;
		for(int i=0;i<getN();i++){
			for(int j = 0;j<getN();j++){
				countElement(tempSet, getGridElement(i, j));
			}
			for(int k=0;k<tempSet.length;k++){
				if(tempSet[k]>1) en += tempSet[k]-1;
				tempSet[k]=0;
			}
		}
		for(int i=0;i<getN();i++){
			for(int j = 0;j<getN();j++){
				countElement(tempSet, getGridElement(j, i));
			}
			for(int k=0;k<tempSet.length;k++){
				if(tempSet[k]>1) en += tempSet[k]-1;
				tempSet[k]=0;
			}
		}
		for(int i=0;i<getN();i++){
			int bx = (i%3);
			int by = (i/3);
			for(int j=0;j<3;j++){
				for(int k=0;k<3;k++){
					countElement(tempSet, getGridElement((bx*3)+j, (by*3)+k));
				}
			}
			for(int k=0;k<tempSet.length;k++){
				if(tempSet[k]>1) en += tempSet[k]-1;
				tempSet[k]=0;
			}
		}
		energy = en;
	}
	/**
	 * Same as totalEnergy() but sets E variable and returns the value/ 
	 * @return
	 */
	public double getTotalEnergy(){ 
		int[] tempSet = {0,0,0,0,0,0,0,0,0};
		double en =0;
		//cols
		for(int i=0;i<getN();i++){
			for(int j = 0;j<getN();j++){
				countElement(tempSet, getGridElement(i, j));
			}
			for(int k=0;k<tempSet.length;k++){
				if(tempSet[k]>1) {
					en += tempSet[k]-1;
					//System.out.println("col:"+i+" "+k+" "+en);
				}
				tempSet[k]=0;
			}
		}
		//rows
		for(int i=0;i<getN();i++){
			for(int j = 0;j<getN();j++){
				countElement(tempSet, getGridElement(j, i));
			}
			for(int k=0;k<tempSet.length;k++){
				if(tempSet[k]>1) {
					en += tempSet[k]-1;
					//System.out.println("row:"+i+" "+k+" "+en);
				}

				tempSet[k]=0;
			}
		}
		//box
		for(int i=0;i<getN();i++){
			int bx = (i%3);
			int by = (i/3);
			for(int j=0;j<3;j++){
				for(int k=0;k<3;k++){
					countElement(tempSet, getGridElement((bx*3)+j, (by*3)+k));
				}
			}
			for(int k=0;k<tempSet.length;k++){
				if(tempSet[k]>1)  {
					en += tempSet[k]-1;
					//System.out.println("box:"+i+" "+k+" "+en);
				}
				tempSet[k]=0;
			}
		}

		return en;
	}
	/* Not used as total energy is more reliable
	public double getDE(int x, int y, int val, int nVal){
		double enI =0;double enF =0;
		int cI =0;
		int cF =0;
		//calc  col energy
		for (int i = 0; i < getN(); i++) {
			if(getGridElement(x, i)==val) cI++;
			if(getGridElement(x, i)==nVal) cF++;
		}
		if(cI>1) enI += cI-1;
		if(cF>1) enF += cF-1;
		cI=cF=0;
		//calc row energy
		for (int i = 0; i < getN(); i++) {
			if(getGridElement(i, y)==val) cI++;
			if(getGridElement(i, y)==nVal) cF++;
		}
		if(cI>1) enI += cI-1;
		if(cF>1) enF += cF-1;
		cI=cF=0;
		//find box number. Numbered 0 - 8, left to right, top to bottom.
		int boxNo = (y-(y%3))+(x/3);
		int bx = (boxNo%3);
		int by = (boxNo/3);
		//calc box energy
		for(int j=0;j<3;j++){
			for(int k=0;k<3;k++){
				if(getGridElement((bx*3)+j, (by*3)+k)==val) cI++;
				if(getGridElement((bx*3)+j, (by*3)+k)==nVal) cF++;
			}
		}
		if(cI>1) enI += cI-1;
		if(cF>1) enF += cF-1;
		//System.out.println(enI+" "+enF+" "+(enF - enI));
		return enF - enI;

	}*/
	/**
	 * Used to get the clues form this methods main class 
	 */
	public Point[] getCluesForMain(){
		//get number of clues
		int c =0;
		for(int i=0;i<grid.length;i++){
			for(int j=0;j<grid.length;j++){
				if(grid[j][i]!=0) c++;
			}
		}
		Point[] p = new Point[c];
		c=0;
		for(int i=0;i<grid.length;i++){
			for(int j=0;j<grid.length;j++){
				if(grid[j][i]!=0) {
					p[c] = new Point(j,i);
					c++;
				}
			}
		}
		return p;
	}
	/**
	 * count how many of each number are in a sub set
	 * @param set the subset
	 * @param val the number of number to look for
	 * @return the val
	 */
	public int[] countElement(int[] set, int val){
		switch (val) {
		case 1: set[0]++;break;
		case 2: set[1]++;break;
		case 3: set[2]++;break;
		case 4: set[3]++;break;
		case 5: set[4]++;break;
		case 6: set[5]++;break;
		case 7: set[6]++;break;
		case 8: set[7]++;break;
		case 9: set[8]++;break;
		default:break;
		}
		//System.out.println(val+": "+set[0]+" "+set[1]+" "+set[2]+" "+set[3]+" "+set[4]+" "+set[5]+" "+set[6]+" "+set[7]+" "+set[8]);
		return set;
	}
	
	/**
	 * See if the grid is correct with a logic check. 
	 * @return true/false
	 */
	public boolean checkGrid(){
		int[] tempSet = new int[]{0,0,0,0,0,0,0,0,0};
		int[] one = new int[]{1,1,1,1,1,1,1,1,1};
		for(int i=0;i<getN();i++){
			for(int j = 0;j<getN();j++){
				countElement(tempSet, getGridElement(i, j));
			}
			//System.out.println("Col: "+i+" "+tempSet[0]+" "+tempSet[1]+" "+tempSet[2]+" "+tempSet[3]+" "+tempSet[4]+" "+tempSet[5]+" "+tempSet[6]+" "+tempSet[7]+" "+tempSet[8]);
			if(!Arrays.equals(tempSet,one)) {
				System.out.println("Col:"+i);
				return false;
			}
			for (int j = 0; j < tempSet.length; j++) {
				tempSet[j]=0;
			}
		}
		for(int i=0;i<getN();i++){
			for(int j = 0;j<getN();j++){
				countElement(tempSet, getGridElement(j, i));
			}
			//System.out.println("row: "+i+" "+tempSet[0]+" "+tempSet[1]+" "+tempSet[2]+" "+tempSet[3]+" "+tempSet[4]+" "+tempSet[5]+" "+tempSet[6]+" "+tempSet[7]+" "+tempSet[8]);
			if(!Arrays.equals(tempSet,one)){
				System.out.println("Row:"+i);
				return false;
			}
			for (int j = 0; j < tempSet.length; j++) {
				tempSet[j]=0;
			}
		}
		for(int i=0;i<getN();i++){
			int bx = (i%3);
			int by = (i/3);
			for(int j=0;j<3;j++){
				for(int k=0;k<3;k++){
					countElement(tempSet, getGridElement((bx*3)+j, (by*3)+k));
				}
			}
			//System.out.println("box: "+i+" "+tempSet[0]+" "+tempSet[1]+" "+tempSet[2]+" "+tempSet[3]+" "+tempSet[4]+" "+tempSet[5]+" "+tempSet[6]+" "+tempSet[7]+" "+tempSet[8]);
			if(!Arrays.equals(tempSet,one)) {
				System.out.println("Box:"+i);
				return false;
			}
			for (int j = 0; j < tempSet.length; j++) {
				tempSet[j]=0;
			}
		}
		return true;
	}
	/**
	 * Randomly fill the grid but leave the clues untouched. 
	 */
	public void randomFillGrid(){
		for(int i=0;i<getN();i++){
			Random r = new Random();
			for (int j = 0; j < getN(); j++) {
				if(getGridElement(i, j)==0) setGridElement(i, j, r.nextInt(getN()-1)+1);
			}
		}
	}

	/**
	 * Fill the grid with 9 of each number
	 */
	public void fillGridWithRules(){
		int[] totals = new int[]{9,9,9,9,9,9,9,9,9};
		int val;
		boolean b = false;
		for (int p = 0; p < clues.length; p++) {
			val = getGridElement((int)clues[p].getX(), (int)clues[p].getY());
			totals[val-1]--;
		}
		for(int i=0;i<getN();i++){
			for (int j = 0; j < getN(); j++) {
				if(isClue(i,j)) {}else{
					b=false;
					while(!b){
						Random r = new Random();
						val = r.nextInt(getN()) + 1;
						if(totals[val-1]>0){
							setGridElement(i, j, val);
							totals[val-1]--;
							b = true;
							//System.out.println(i+" "+j+" "+val+": "+totals[0]+" "+totals[1]+" "+totals[2]+" "+totals[3]+" "+totals[4]+" "+totals[5]+" "+totals[6]+" "+totals[7]+" "+totals[8]);
						}
					}
				}
			}
		}
		energy = getTotalEnergy();

	}
	/**
	 * Get a grid from a file
	 * @param n the grid number
	 * @throws IOException
	 */
	public void sampleGrid(int n) throws IOException{
		String fileName = System.getProperty("user.dir")+"/bin/";
		switch (n) {
		case 0: fileName = fileName + "easy.dat"; break;
		case 1: fileName = fileName + "med.dat";break;
		case 2: fileName = fileName + "hard.dat";break;
		case 3: fileName = fileName + "vHard.dat";break;
		case 4: fileName = fileName + "fiend.dat";break;

		default: fileName = fileName + "grid1.dat";break;
		}
		Scanner sc = new Scanner(new File(fileName));
		for (int i = 0; i < getN(); i++) {
			for (int j = 0; j < getN(); j++) {
				grid[j][i]=sc.nextInt();
			}
		}
	}
	/**
	 * Get a grid of n clues 
	 */
	 public void getGridOfNClues(int c, int n) throws IOException{
			String fileName = "grids/nGrids";
			Scanner sc = new Scanner(new File(fileName));
			if(c!=17){
			    for(int i=0;i<((c-17)*6);i++){
				sc.nextLine();
			    }
			}
			for(int s=0;s<n;s++){
			    sc.nextLine();
			}
			sc.next();
			String l = sc.next();
			int q =0;
			for(int j=0; j<getN(); j++){
			    for(int k=0; k<getN(); k++){
				grid[k][j] = Integer.parseInt(l.substring(q,q+1));
				q++;
			    }
			}
			sc.close();
		    }
	/**
	 * get the prob of going up in energy called when T changes and for each de
	 * @param de
	 * @return
	 */
	public double getProb(double de){
		return Math.exp((-1.0*de)/(K*getT()));
	}

	/**
	 * Kawasaki dynamics. Makes a trail and then accepts or reject it with metropolis algorithm
	 */
	public void elementSwap(){
		int x1 = (int)(Math.random()*(double)getN());
		int y1 = (int)(Math.random()*(double)getN());
		int x2 = (int)(Math.random()*(double)getN());
		int y2 = (int)(Math.random()*(double)getN());

		if(isClue(x1, y1) || isClue(x2, y2)) {} else {

			double en1 = getTotalEnergy();
			int val1 = getGridElement(x1, y1);
			int val2 = getGridElement(x2, y2);
			setGridElement(x1, y1, val2);
			setGridElement(x2, y2, val1);
			double en2=getTotalEnergy();

			double de = en2 - en1;
			if(de <=0) {
				energy = en2;
			}
			else if(getProb(de)<Math.random()){
				setGridElement(x1, y1, val1);
				setGridElement(x2, y2, val2);
				energy = en1;
			}
		}
	}
	/**
	 * get the change in energy
	 * @param x xpos
	 * @param y ypos
	 * @param val old value of element
	 * @param nVal trail value of element
	 * @return
	 */
	public double newDE(int x, int y, int val, int nVal){

		double en1 = getTotalEnergy();
		setGridElement(x, y, nVal);
		double en2=getTotalEnergy();
		setGridElement(x, y, val);
		//System.out.println(en1+" "+en2+" "+(en2 - en1));
		return en2-en1;
	}

	//Update methods
	/**
	 * Steps n times checking to see if sim is stopped at E=0
	 */
	public void iterate(int steps){
		synchronized (this) {
			for(int i=0; i<steps;i++){
				double e = getEnergy();
				if(e==0) {
					//System.out.println(e);
					STOP=!STOP;
					return;
				}
				elementSwap();
				//System.out.println(e);
			}
		}
	}

	/**
	 * As above but for one iteration
	 */
	public void iterate(){

		double e = getEnergy();
		if(e==0) {
			//System.out.println(e);
			STOP=!STOP;
			return;
		}
		elementSwap();
		//System.out.println(e);

	}


	/**
	 * Get the standard deviation of an array
	 * @param a The array to calculate the stdev of
	 * @return standard deviation
	 */
	public static double stDev(double[] a){
		double tot =0;
		double mean = mean(a);
		for(int i=0;i<a.length;i++){
			double tmp = a[i] - mean;
			tot += tmp*tmp;
		}
		return Math.sqrt(tot/a.length);
	}
	/**
	 * Get the mean from an array of doubles
	 * 
	 * @param a The array to find the average of
	 * @return The Average of the array
	 */
	public static double mean(double[] a){
		double tot =0;
		for(int i=0;i<a.length;i++){
			tot += a[i];
		}
		return tot/a.length;
	}

	/**
	 * Performs the bootstrap algothim on an array of numbers.
	 * @param x An array of numbers to be bootstraped
	 * @param samples Number of times to sample
	 * @return double array with two elements, the first being the returned value and second the error on the value
	 */
	public static double[] bootstrap(double[] x,int samples){
		double[] test = new double[x.length];
		double[] tmp = new double[samples];

		for(int i=0;i<samples;i++){
			for(int j=0;j<x.length;j++){
				int rand = (int)(Math.random()*x.length);
				test[j]= x[rand];
			}
			tmp[i]=mean(test);
		}
		double[] ret = new double[2];
		ret[0]=mean(tmp);
		ret[1]=stDev(tmp);
		return ret;
	}

	public static void pln(String s){
		System.out.println(s);
	}
}

