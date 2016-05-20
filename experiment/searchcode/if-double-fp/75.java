//package it.unina.cloudclusteringnaive;
//
//
//
//import java.io.BufferedReader;
//import java.io.CharArrayReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.StringTokenizer;
//import java.util.Vector;
//import java.io.*;
//import java.util.*;
//
//import libsvm.svm;
//import libsvm.svm_node;
//import libsvm.svm_parameter;
//import libsvm.svm_print_interface;
//import libsvm.svm_problem;
//
///**
// * ogni volta che viene istanziato un oggetto ProblemGenerator viene creato un 
// * svm_problem sulla base del file contenente il dataset passato al costruttore 
// * ed un svm_parameter con i parametri settati tutti ai valori di default
// * l'svm_problem codifica il problema settando 3 valori:
// * - l => numero di elementio del dataset
// * - y => array contenente le label di ogni elemento del dataset di dimensione 
// *        l (elle)
// * - x => vettore bidimensionale di svm_node. Un svm_node contiene una coppia 
// *        (indice di colonna, valore)
// *        ogni riga della matrice x corrisponde ad un rigo del dataset e quindi 
// *        ad un singolo elemento del dataset (ad esempio, se il dataset riguarda
// *        feature di un progetto sw allora un rigo corrisponde alle feature di
// *        un singolo progetto sw)
// * @author barnap
// *
// */
//public class ProblemGenerator {
//	svm_parameter param;
//	svm_problem prob;
//
//	public ProblemGenerator(String inputFileName) {
//		// TODO Auto-generated constructor stub
//		createDefaultParamValues();
//		
//		try {
//			createProblem(inputFileName);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	/**
//	 * l'array di char sarŕ trasformato in un bufferedReader
//	 * @param inputFile
//	 */
//	public ProblemGenerator(char[] inputFile) {
//		// TODO Auto-generated constructor stub
//		createDefaultParamValues();
//		
//		try {
//			createProblem(inputFile);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	private void createDefaultParamValues()
//	{
////		int i;
////		svm_print_interface print_func = null;	// default printing to stdout
//
//		param = new svm_parameter();
//		// default values
//		param.svm_type = svm_parameter.C_SVC;
//		param.kernel_type = svm_parameter.RBF;
//		param.degree = 3;
//		param.gamma = 0;	// 1/num_features
//		param.coef0 = 0;
//		param.nu = 0.5;
//		param.cache_size = 100;
//		param.C = 1;
//		param.eps = 1e-3;
//		param.p = 0.1;
//		param.shrinking = 1;
//		param.probability = 0;
//		param.nr_weight = 0;
//		param.weight_label = new int[0];
//		param.weight = new double[0];
////		cross_validation = 0;
//
//		// parse options
///*		for(i=0;i<argv.length;i++)
//		{
//			if(argv[i].charAt(0) != '-') break;
//			if(++i>=argv.length)
//				exit_with_help();
//			switch(argv[i-1].charAt(1))
//			{
//				case 's':
//					param.svm_type = atoi(argv[i]);
//					break;
//				case 't':
//					param.kernel_type = atoi(argv[i]);
//					break;
//				case 'd':
//					param.degree = atoi(argv[i]);
//					break;
//				case 'g':
//					param.gamma = atof(argv[i]);
//					break;
//				case 'r':
//					param.coef0 = atof(argv[i]);
//					break;
//				case 'n':
//					param.nu = atof(argv[i]);
//					break;
//				case 'm':
//					param.cache_size = atof(argv[i]);
//					break;
//				case 'c':
//					param.C = atof(argv[i]);
//					break;
//				case 'e':
//					param.eps = atof(argv[i]);
//					break;
//				case 'p':
//					param.p = atof(argv[i]);
//					break;
//				case 'h':
//					param.shrinking = atoi(argv[i]);
//					break;
//				case 'b':
//					param.probability = atoi(argv[i]);
//					break;
//				case 'q':
//					print_func = svm_print_null;
//					i--;
//					break;
//				case 'v':
//					cross_validation = 1;
//					nr_fold = atoi(argv[i]);
//					if(nr_fold < 2)
//					{
//						System.err.print("n-fold cross validation: n must >= 2\n");
//						exit_with_help();
//					}
//					break;
//				case 'w':
//					++param.nr_weight;
//					{
//						int[] old = param.weight_label;
//						param.weight_label = new int[param.nr_weight];
//						System.arraycopy(old,0,param.weight_label,0,param.nr_weight-1);
//					}
//
//					{
//						double[] old = param.weight;
//						param.weight = new double[param.nr_weight];
//						System.arraycopy(old,0,param.weight,0,param.nr_weight-1);
//					}
//
//					param.weight_label[param.nr_weight-1] = atoi(argv[i-1].substring(2));
//					param.weight[param.nr_weight-1] = atof(argv[i]);
//					break;
//				default:
//					System.err.print("Unknown option: " + argv[i-1] + "\n");
//					exit_with_help();
//			}
//		}
//
//		svm.svm_set_print_string_function(print_func);
//
//		// determine filenames
//
//		if(i>=argv.length)
//			exit_with_help();
//
//		input_file_name = argv[i];
//
//		if(i<argv.length-1)
//			model_file_name = argv[i+1];
//		else
//		{
//			int p = argv[i].lastIndexOf('/');
//			++p;	// whew...
//			model_file_name = argv[i].substring(p)+".model";
//		} */
//	}
//
//	// read in a problem (in svmlight format)
//
//	private void createProblem(char[] charFile) throws IOException{
//		//TODO: implementare il codice
//		BufferedReader fp = new BufferedReader(new CharArrayReader(charFile));
//		createProblem(fp);
//
//		fp.close();
//	}
//	private void createProblem(String input_file_name) throws IOException
//	{ 
//		
//		BufferedReader fp = new BufferedReader(new FileReader(input_file_name));
//		createProblem(fp);
//
//		fp.close();
//	}
//
//	private void createProblem(BufferedReader fp) throws IOException {
//		Vector<Double> vy = new Vector<Double>();
//		Vector<svm_node[]> vx = new Vector<svm_node[]>();
//		int max_index = 0;
//
//		while(true)
//		{
//			String line = fp.readLine();
//			if(line == null) break;
//
//			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");
//
//			vy.addElement(atof(st.nextToken()));
//			int m = st.countTokens()/2;
//			svm_node[] x = new svm_node[m];
//			for(int j=0;j<m;j++)
//			{
//				x[j] = new svm_node();
//				x[j].index = atoi(st.nextToken());
//				x[j].value = atof(st.nextToken());
//			}
//			if(m>0) max_index = Math.max(max_index, x[m-1].index);
//			vx.addElement(x);
//		}
//
//		prob = new svm_problem();
//		prob.l = vy.size();
//		prob.x = new svm_node[prob.l][];
//		for(int i=0;i<prob.l;i++)
//			prob.x[i] = vx.elementAt(i);
//		prob.y = new double[prob.l];
//		for(int i=0;i<prob.l;i++)
//			prob.y[i] = vy.elementAt(i);
//
//		if(param.gamma == 0 && max_index > 0)
//			param.gamma = 1.0/max_index;
//
//		if(param.kernel_type == svm_parameter.PRECOMPUTED)
//			for(int i=0;i<prob.l;i++)
//			{
//				if (prob.x[i][0].index != 0)
//				{
//					System.err.print("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
//					System.exit(1);
//				}
//				if ((int)prob.x[i][0].value <= 0 || (int)prob.x[i][0].value > max_index)
//				{
//					System.err.print("Wrong input format: sample_serial_number out of range\n");
//					System.exit(1);
//				}
//			}
//	}
//	
//	private static double atof(String s)
//	{
//		double d = Double.valueOf(s).doubleValue();
//		if (Double.isNaN(d) || Double.isInfinite(d))
//		{
//			System.err.print("NaN or Infinity in input\n");
//			System.exit(1);
//		}
//		return(d);
//	}
//
//	private static int atoi(String s)
//	{
//		return Integer.parseInt(s);
//	}
//
//	public svm_parameter getParam() {
//		return param;
//	}
//
//	public void setParam(svm_parameter param) {
//		this.param = param;
//	}
//
//	public svm_problem getProb() {
//		return prob;
//	}
//
//}

package it.unina.cloudclusteringnaive;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.*;
import java.util.*;

import libsvm.svm;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_print_interface;
import libsvm.svm_problem;

/**
 * ogni volta che viene istanziato un oggetto ProblemGenerator viene creato un 
 * svm_problem sulla base del file contenente il dataset passato al costruttore 
 * ed un svm_parameter con i parametri settati tutti ai valori di default
 * l'svm_problem codifica il problema settando 3 valori:
 * - l => numero di elementio del dataset
 * - y => array contenente le label di ogni elemento del dataset di dimensione 
 *        l (elle)
 * - x => vettore bidimensionale di svm_node. Un svm_node contiene una coppia 
 *        (indice di colonna, valore)
 *        ogni riga della matrice x corrisponde ad un rigo del dataset e quindi 
 *        ad un singolo elemento del dataset (ad esempio, se il dataset riguarda
 *        feature di un progetto sw allora un rigo corrisponde alle feature di
 *        un singolo progetto sw)
 * @author barnap
 *
 */
public class ProblemGenerator {
	svm_parameter param;
	svm_problem prob;

	public ProblemGenerator(String inputFileName) {
		// TODO Auto-generated constructor stub
		createParameters();
		
		try {
			createProblem(inputFileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * l'array di char sarŕ trasformato in un bufferedReader
	 * @param inputFile
	 */
	public ProblemGenerator(char[] inputFile) {
		// TODO Auto-generated constructor stub
		createParameters();
		
		try {
			createProblem(inputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * l'array di char sarŕ trasformato in un bufferedReader
	 * @param inputFile
	 * @param parameters
	 */
	public ProblemGenerator(char[] inputFile, String parameters) {
		createParameters(parameters);
		try {
			createProblem(inputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createParameters(){
		param = new svm_parameter();
		// default values
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		param.gamma = 0;	// 1/num_features
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 100;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 0;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];
	}

	private void createParameters(String params){
		//poichč la stringa prevista deve cominciare per libsvm eliminiamo tale parola e passiamo solo i parametri
		//TODO: controllo stringhe non previste
		createParameters(params.substring("libsvm".length()+1).split(" "));
	}

	private void createParameters(String[] params){
		//System.out.println("\nCREATEPARAMETERS:\n");

		
		createParameters();
		int i;
//		svm_print_interface print_func = null;	// default printing to stdout
//		cross_validation = 0;

		// parse options
		for(i=0;i<params.length;i++){
//			System.out.println("\nPARAMETRO LETTO: "+params[i]);
			if(params[i].charAt(0) != '-') break;
			if(++i>=params.length) break;
				//exit_with_help();//errore: opzione senza valore
			switch(params[i-1].charAt(1)){
				case 's':
					param.svm_type = atoi(params[i]);
					break;
				case 't':
					param.kernel_type = atoi(params[i]);
					break;
				case 'd':
					param.degree = atoi(params[i]);
					break;
				case 'g':
					param.gamma = atof(params[i]);
					break;
				case 'r':
					param.coef0 = atof(params[i]);
					break;
				case 'n':
					param.nu = atof(params[i]);
					break;
				case 'm':
					param.cache_size = atof(params[i]);
					break;
				case 'c':
					param.C = atof(params[i]);
					break;
				case 'e':
					param.eps = atof(params[i]);
					break;
				case 'p':
					param.p = atof(params[i]);
					break;
				case 'h':
					param.shrinking = atoi(params[i]);
					break;
				case 'b':
					param.probability = atoi(params[i]);
					break;
//				case 'q':
//					print_func = svm_print_null;
//					i--;
//					break;
//				case 'v':
//					cross_validation = 1;
//					nr_fold = atoi(params[i]);
//					if(nr_fold < 2)
//					{
//						System.err.print("n-fold cross validation: n must >= 2\n");
//						exit_with_help();
//					}
//					break;
					//NON SAPPIAMO A CHE SERVE
//				case 'w':
//					++param.nr_weight;
//					{
//						int[] old = param.weight_label;
//						param.weight_label = new int[param.nr_weight];
//						System.arraycopy(old,0,param.weight_label,0,param.nr_weight-1);
//					}
//
//					{
//						double[] old = param.weight;
//						param.weight = new double[param.nr_weight];
//						System.arraycopy(old,0,param.weight,0,param.nr_weight-1);
//					}
//
//					param.weight_label[param.nr_weight-1] = atoi(params[i-1].substring(2));
//					param.weight[param.nr_weight-1] = atof(params[i]);
//					break;
				default:
					//errore opzione sconosciuta
//					System.err.print("Unknown option: " + params[i-1] + "\n");
//					exit_with_help();
			}
		}

		//svm.svm_set_print_string_function(print_func);

		// determine filenames
		//NON USIAMO FILE PASSATI COME ARGOMENTO
//
//		if(i>=params.length)
//			exit_with_help();
//
//		input_file_name = params[i];
//
//		if(i<params.length-1)
//			model_file_name = params[i+1];
//		else
//		{
//			int p = params[i].lastIndexOf('/');
//			++p;	// whew...
//			model_file_name = params[i].substring(p)+".model";
//		}
	}

	// read in a problem (in svmlight format)

	private void createProblem(char[] charFile) throws IOException{
		//TODO: implementare il codice
		BufferedReader fp = new BufferedReader(new CharArrayReader(charFile));
		createProblem(fp);

		fp.close();
	}
	
	private void createProblem(String input_file_name) throws IOException
	{ 
		
		BufferedReader fp = new BufferedReader(new FileReader(input_file_name));
		createProblem(fp);

		fp.close();
	}

	private void createProblem(BufferedReader fp) throws IOException {
		Vector<Double> vy = new Vector<Double>();
		Vector<svm_node[]> vx = new Vector<svm_node[]>();
		int max_index = 0;

		while(true)
		{
			String line = fp.readLine();
			if(line == null) break;

			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

			vy.addElement(atof(st.nextToken()));
			int m = st.countTokens()/2;
			svm_node[] x = new svm_node[m];
			for(int j=0;j<m;j++)
			{
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}
			if(m>0) max_index = Math.max(max_index, x[m-1].index);
			vx.addElement(x);
		}

		prob = new svm_problem();
		prob.l = vy.size();
		prob.x = new svm_node[prob.l][];
		for(int i=0;i<prob.l;i++)
			prob.x[i] = vx.elementAt(i);
		prob.y = new double[prob.l];
		for(int i=0;i<prob.l;i++)
			prob.y[i] = vy.elementAt(i);

		if(param.gamma == 0 && max_index > 0)
			param.gamma = 1.0/max_index;

		if(param.kernel_type == svm_parameter.PRECOMPUTED)
			for(int i=0;i<prob.l;i++)
			{
				if (prob.x[i][0].index != 0)
				{
					System.err.print("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
					System.exit(1);
				}
				if ((int)prob.x[i][0].value <= 0 || (int)prob.x[i][0].value > max_index)
				{
					System.err.print("Wrong input format: sample_serial_number out of range\n");
					System.exit(1);
				}
			}
	}
	
	private static double atof(String s)
	{
		double d = Double.valueOf(s).doubleValue();
		if (Double.isNaN(d) || Double.isInfinite(d))
		{
			System.err.print("NaN or Infinity in input\n");
			System.exit(1);
		}
		return(d);
	}

	private static int atoi(String s)
	{
		return Integer.parseInt(s);
	}

	public svm_parameter getParam() {
		return param;
	}

	public void setParam(svm_parameter param) {
		this.param = param;
	}

	public svm_problem getProb() {
		return prob;
	}

}


