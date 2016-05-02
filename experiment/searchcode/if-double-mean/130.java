import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 
 */

/**
 * @author s0941897
 * Tester class for finding the average time a simulation takes to run and the longest time in one energy state. Choose which of there
 * is done by recompling. Not pretty but it works. 
 */
public class AverageRuntime {
	
	/**
	 * Choose the test to run when compling. Command line arguements are needed.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{
		
		averageTimeToComplete(args);
		
		timeInConstantEnergy(args);
	}

	/**
	 * The average time to complete the simulation for a range of temperatures. 
	 * @param args
	 * @throws IOException
	 */
	public static void averageTimeToComplete(String[] args) throws IOException{

		//number of files to loop over
		int files =5;
		//min and max temp range
		double minTemp = 0;
		double maxTemp = 0.07;
		//number of points on x
		double points = 20;
		//runs for each temp
		int runs = 5;
		//longest energy to restart (RESTART CONDITION)
		int pastEnergyLength = 250000;
		//the threshold for the above energy calulations dicimal
		double tolerance = 1.0;

		//loop over the files
		for (int d = 0; d < files; d++) {

			//progress
			System.out.println("------------------ New File: "+d+" ------------------");
			//int d=2;
		
			//set up output file
			PrintWriter out = new PrintWriter(new File(System.getProperty("user.dir")+"/data/65000_"+d+".dat"));

			//over the range of T
			for (int i = 0; i < (int)points; i++) {

				//Temp
				double temp = ((maxTemp-minTemp)/points)*(double)i + minTemp;

				int pastEnergyIndex =0;

				double[] set = new double[runs];
				
				double[] failStep = new double[runs];
				
				double[] failNum = new double[runs];
				for(int q=0;q<runs;q++) failNum[q]=0.0;

				//number of runs at each temp
				for (int j = 0; j < runs; j++) {

					SudokuSolver ss = new SudokuSolver(temp, d, j);
					ss.setClues(ss.getCluesForMain());
					ss.fillGridWithRules();

					int t = 0;

					double[] pastEnergy = new double[pastEnergyLength];

					boolean small = true;

					while(!ss.isStopped() && small){
						
						
						//restart conditions
						pastEnergyIndex = t%pastEnergyLength;
						pastEnergy[pastEnergyIndex] = ss.getEnergy();
						if(pastEnergyIndex==0){
							double error = tolerance*pastEnergy[0]; 
							double mPE = SudokuSolver.mean(pastEnergy);
							if((mPE > pastEnergy[0]-error && mPE < pastEnergy[0] + error) || mPE==pastEnergy[0] ){
								small=false;
								failStep[j] += t;
								failNum[j] += 1.0;
								System.out.printf("Stuck "+failNum[j]);
								j--;
								
							}
						}

						ss.iterate();
						t++;
					}

					//if solved and not restarted
					if(small) {
						set[j] += (double)t;
						failStep[j] += t;
					}
					
					System.out.println("  "+t+" "+ss.getEnergy());

				}
				
				//output to file
				out.println(temp +" "+ SudokuSolver.mean(set)+" "+SudokuSolver.stDev(set)
						+" "+ SudokuSolver.mean(failStep)+" "+SudokuSolver.stDev(failStep)
						+" "+ SudokuSolver.mean(failNum)+" "+SudokuSolver.stDev(failNum));
				out.flush();
			} 
			//close the output file
			out.close();
		}
	}
	/**
	 * Find the average amount of time the simulation stays in constant E for various T
	 * @param args
	 * @throws IOException
	 */
	public static void timeInConstantEnergy(String[] args) throws IOException{

		//Temp range
		double minTemp =Double.parseDouble(args[0]);
		double maxTemp = Double.parseDouble(args[1]);
		//Number of points on x axis
		int points = Integer.parseInt(args[2]);
		//runs per T
		int runs = Integer.parseInt(args[3]);
		
		String fileName = args[4];
		
		int d = Integer.parseInt(args[5]);

		//String fileName = System.getProperty("user.dir")+"/data/longestConstant_5_20.dat";

		//set up output file
		PrintWriter out = new PrintWriter(new File(fileName));

		//for(int d=0;d<4;d++){

		//for points
			for(int i=0; i<points; i++){

				//temp
				double temp = ((maxTemp-minTemp)/points)*(double)i + minTemp;

				double[] set = new double[runs];

				for(int r =0; r<runs; r++){

					SudokuSolver ss = new SudokuSolver(temp, d, r);
					ss.setClues(ss.getCluesForMain());
					ss.fillGridWithRules();

					double e1 = ss.getEnergy();
					double e2;

					int c =0;
					int cmax =0;
					int t =0;

					//carry on until solved or very long
					while(!ss.isStopped() && t<10000000){
						ss.iterate();
						e2 = ss.getEnergy();
						//double error = 0.1*e1;
						//if(e2 < e1+error && e2 > e1-error){
						if(e1==e2){
							c++;
						}else if(c > cmax){
							cmax = c;
							c=0;
						}
						e1=e2;
						t++;
					}

					//if(t>=10000000){
					//	r--;
					//}else{
					set[r] = (double)cmax;
					SudokuSolver.pln("  "+cmax+" "+t);
					//}

				}
				double mean = SudokuSolver.mean(set);
				double sd = SudokuSolver.stDev(set);
				
				//write output file
				System.out.printf("%.5f %.5f %.5f",temp,mean,sd);
				out.printf("%.5f %.5f %.5f",temp,mean,sd);
				out.flush();

			//}
			out.println();
			System.out.println(" ************************* ");
		}
			//close output file
		out.close();
	}

}

