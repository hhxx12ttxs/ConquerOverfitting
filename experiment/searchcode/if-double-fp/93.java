package it.unina.cloudclusteringnaive;

import java.io.BufferedReader;
import java.io.CharArrayReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class ProblemDataManager {

//	private char[] trainingSet;
//	private char[] testSet;

	private svm_parameter param;
	private svm_problem prob;
	private svm_problem training;
	private svm_problem test;


	/**
	 * COSTRUTTORI
	 */

	public ProblemDataManager(char[] trainingSet, char[] testSet) {
		/* inizializzare i parametri di default */
		createParameters();
		
		/* creazione di training set e test set */
		createProbFromTrainAndTest(trainingSet, testSet);
	}

	public ProblemDataManager(char[] trainingSet, char[] testSet, String parameters) {
		/* inizializzare i parametri passati in input */
		createParameters(parameters);
		
		/* creazione di training set e test set */
		createProbFromTrainAndTest(trainingSet, testSet);
	}
	
	public ProblemDataManager(char[] dataSet, Integer totalGroups, Integer testGroup) {
		/* inizializzare i parametri di default */
		createParameters();
		
		/* creazione di training set e test set */
		createProbFromData(dataSet, totalGroups, testGroup);
	}

	public ProblemDataManager(char[] dataSet, Integer totalGroups, Integer testGroup, String parameters) {
		/* inizializzare i parametri passati in input */
		createParameters(parameters);

		/* creazione di training set e test set */
		createProbFromData(dataSet, totalGroups, testGroup);
	}
	
	
	
	/* funzioni ausiliarie per i costruttori */
	
	private void createProbFromTrainAndTest(char[] trainingSet, char[] testSet) {
		/* leggere Training Set e Test Set */
		BufferedReader trainingSetReader = new BufferedReader(new CharArrayReader(trainingSet));
		BufferedReader testSetReader = new BufferedReader(new CharArrayReader(testSet));
		try {
			training=parseDataSet(trainingSetReader);
			test=parseDataSet(testSetReader);
			trainingSetReader.close();
			testSetReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void createProbFromData(char[] dataSet, Integer totalGroups,
			Integer testGroup) {
		BufferedReader dataSetReader = new BufferedReader(new CharArrayReader(dataSet));
		try {
			prob=parseDataSet(dataSetReader);
			dataSetReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/* Splittare il DataSet costruendo Training Set e Test Set */
		splitDataSet(totalGroups, testGroup);
	}
	
	/**
	 * 
	 * @param totalGroups indica il numero di gruppi in cui dividere il dataset
	 * se č negativo o maggiore del numero di righe presenti allora il dataset
	 * sarŕ scomposto in un gruppo per ogni riga 
	 * @param testGroup indica il gruppo da considerare come testset. se il 
	 * numero inserito č negativo oppure maggiore del numero di gruppi sarŕ posto
	 * uguale a zero e quindi si considera come test set il primo gruppo
	 */
	private void splitDataSet(Integer totalGroups, Integer testGroup) {
		/* legge l'svm_problem contenuto in prob e lo divide in due svm_problem test e training */
		training=new svm_problem();
		test=new svm_problem();
		//Logger log = Logger.getLogger(SvmMapper.class.getName());
		Integer rowsForGroup;
		if(totalGroups<=0 || totalGroups>prob.l) {
			rowsForGroup=1;
			totalGroups=prob.l;
			//log.info("prob.l="+prob.l);
		}else if ((prob.l % totalGroups)==0){
			rowsForGroup = prob.l/totalGroups;
		}else rowsForGroup = prob.l/totalGroups + 1;
		
		if(testGroup>totalGroups || testGroup<=0) testGroup=1;
		
		/* determiniamo l */
		if(testGroup==totalGroups){
			/* il test set č l'ultimo gruppo del data set */
			training.l=(totalGroups-1)*rowsForGroup;
			test.l=prob.l-training.l;
		} else {
			/* il test set non č l'ultimo gruppo del data set */
			test.l=rowsForGroup;
			training.l=prob.l-test.l;
		}
		
		/* Splittiamo */
		test.y=new double[test.l];
		training.y=new double[training.l];
		test.x=new svm_node[test.l][];
		training.x=new svm_node[training.l][];
		
		Integer testIndex=0;
		Integer trainingIndex=0;
		Integer i,j;
		for(i=0; i<prob.l; i++){
			if(i % totalGroups == (testGroup -1)){ /*(i / rowsForGroup == (testGroup-1)){ */
				/* riga del test Set*/
				test.y[testIndex]=prob.y[i];
				/*copia l'array di svm_node*/
				test.x[testIndex]=new svm_node[prob.x[i].length];
				for(j=0;j<prob.x[i].length;j++){
					test.x[testIndex][j]=prob.x[i][j];
				}
				testIndex++;
			}else {
				/* riga del training Set */
				training.y[trainingIndex]=prob.y[i];
				/*copia l'array di svm_node*/
				training.x[trainingIndex]=new svm_node[prob.x[i].length];
				for(j=0;j<prob.x[i].length;j++){
					training.x[trainingIndex][j]=prob.x[i][j];
				}
				trainingIndex++;
			}
		}
	}

	
	/* FUNZIONI DI GESTIONE PARAMETRI */
	private void createParameters() {
		param = new svm_parameter();
		// default values
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		param.gamma = 0; // 1/num_features
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

	private void createParameters(String params) {
		// TODO: controllo stringhe non previste
		//createParameters(params.substring("libsvm".length()+1).split(" "));
		createParameters(params.split(" "));
	}

	private void createParameters(String[] params) {
		// System.out.println("\nCREATEPARAMETERS:\n");

		createParameters();
		int i;
		// svm_print_interface print_func = null; // default printing to stdout
		// cross_validation = 0;

		// parse options
		for (i = 0; i < params.length; i++) {
			// System.out.println("\nPARAMETRO LETTO: "+params[i]);
			if (params[i].charAt(0) != '-')
				break;
			if (++i >= params.length)
				break;
			// exit_with_help();//errore: opzione senza valore
			switch (params[i - 1].charAt(1)) {
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
			// case 'q':
			// print_func = svm_print_null;
			// i--;
			// break;
			// case 'v':
			// cross_validation = 1;
			// nr_fold = atoi(params[i]);
			// if(nr_fold < 2)
			// {
			// System.err.print("n-fold cross validation: n must >= 2\n");
			// exit_with_help();
			// }
			// break;
			// NON SAPPIAMO A CHE SERVE
			// case 'w':
			// ++param.nr_weight;
			// {
			// int[] old = param.weight_label;
			// param.weight_label = new int[param.nr_weight];
			// System.arraycopy(old,0,param.weight_label,0,param.nr_weight-1);
			// }
			//
			// {
			// double[] old = param.weight;
			// param.weight = new double[param.nr_weight];
			// System.arraycopy(old,0,param.weight,0,param.nr_weight-1);
			// }
			//
			// param.weight_label[param.nr_weight-1] =
			// atoi(params[i-1].substring(2));
			// param.weight[param.nr_weight-1] = atof(params[i]);
			// break;
			default:
				// errore opzione sconosciuta
				// System.err.print("Unknown option: " + params[i-1] + "\n");
				// exit_with_help();
			}
		}

		// svm.svm_set_print_string_function(print_func);

		// determine filenames
		// NON USIAMO FILE PASSATI COME ARGOMENTO
		//
		// if(i>=params.length)
		// exit_with_help();
		//
		// input_file_name = params[i];
		//
		// if(i<params.length-1)
		// model_file_name = params[i+1];
		// else
		// {
		// int p = params[i].lastIndexOf('/');
		// ++p; // whew...
		// model_file_name = params[i].substring(p)+".model";
		// }
	}
	
	/* FUNZIONI DI GESTIONE PROBLEMA */

//	private void createProblem(char[] charFile) throws IOException {
//		BufferedReader fp = new BufferedReader(new CharArrayReader(charFile));
//		createProblem(fp);
//		fp.close();
//	}
//
//	private void createProblem(String input_file_name) throws IOException {
//		BufferedReader fp = new BufferedReader(new FileReader(input_file_name));
//		createProblem(fp);
//		fp.close();
//	}
//
//	private void createProblem(BufferedReader fp) throws IOException {
//		this.prob = parseDataSet(fp);
//	}

	
	private svm_problem parseDataSet(BufferedReader dataSetReader) throws IOException{
		svm_problem prob;
		
		Vector<Double> vy = new Vector<Double>();
		Vector<svm_node[]> vx = new Vector<svm_node[]>();
		int max_index = 0;

		while (true) {
			String line = dataSetReader.readLine();
			if (line == null)
				break;

			StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

			vy.addElement(atof(st.nextToken()));
			int m = st.countTokens() / 2;
			svm_node[] x = new svm_node[m];
			for (int j = 0; j < m; j++) {
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}
			if (m > 0)
				max_index = Math.max(max_index, x[m - 1].index);
			vx.addElement(x);
		}

		prob = new svm_problem();
		prob.l = vy.size();
		prob.x = new svm_node[prob.l][];
		for (int i = 0; i < prob.l; i++)
			prob.x[i] = vx.elementAt(i);
		prob.y = new double[prob.l];
		for (int i = 0; i < prob.l; i++)
			prob.y[i] = vy.elementAt(i);

		if (param.gamma == 0 && max_index > 0)
			param.gamma = 1.0 / max_index;

		if (param.kernel_type == svm_parameter.PRECOMPUTED)
			for (int i = 0; i < prob.l; i++) {
				if (prob.x[i][0].index != 0) {
					System.err
							.print("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
					System.exit(1);
				}
				if ((int) prob.x[i][0].value <= 0
						|| (int) prob.x[i][0].value > max_index) {
					System.err
							.print("Wrong input format: sample_serial_number out of range\n");
					System.exit(1);
				}
			}
		return prob;
	}
	
	/* getter */
	
	
	public svm_parameter getParam() {
		return param;
	}

	public svm_problem getTraining() {
		return training;
	}

	public svm_problem getTest() {
		return test;
	}

	/* funzioni ausiliarie */
	private static double atof(String s) {
		double d = Double.valueOf(s).doubleValue();
		if (Double.isNaN(d) || Double.isInfinite(d)) {
			System.err.print("NaN or Infinity in input\n");
			System.exit(1);
		}
		return (d);
	}

	private static int atoi(String s) {
		return Integer.parseInt(s);
	}

//	/*test*/
//	public static void main(String[] args){
//		try {
//			System.out.println("START");
//			BufferedReader fp = new BufferedReader(new FileReader("svmguide4"));
//			String s;
//			StringBuffer file=new StringBuffer("");
//			s=fp.readLine();
//			while(s!=null){
//				file.append(s+"\n");
//				//System.out.println(s);
//				s=fp.readLine();
//			}
//			char[] fch=file.toString().toCharArray();
//			fp.close();
//			System.out.println("STAMPIAMO L'ARRAY DI CHAR");
//			for(int k =0; k<fch.length; k++){
//				System.out.print(fch[k]);
//			}
//			System.out.println("\nCONVERSIONE");
//			ProblemDataManager pdm = new ProblemDataManager(fch, 300, 2);
//			
//			System.out.println("STAMPE");
//			svmProbPrint(pdm.getTraining());
//			svmProbPrint(pdm.getTest());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
//	
//	public static void svmProbPrint(svm_problem probl){
//		System.out.println("In questo oggetto ci sono "+probl.l+" elementi");
//		for(int i=0; i<probl.l;i++){
//			System.out.print(probl.y[i]+" ");
//			for(int j=0; j<probl.x[i].length;j++){
//				System.out.print(probl.x[i][j].index+":"+probl.x[i][j].value+" ");
//			}
//			System.out.println();
//		}
//	}
}

