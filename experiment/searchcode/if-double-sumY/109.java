package it.unina.cloudclusteringnaive;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;

/**
 * testa il modello utilizzando un Testset. richiama la funzione svm_predict
 * un numero di volte pari al numero di righe (cio?¨ al numero di elementi) del 
 * Testset.
 * Stampa a video l'accuratezza del modello testato
 * 
 * TODO: capire come funziona l'opzione probability
 * TODO: capire come calcola l'accuratezza ed eventualmente restituirla e 
 *       non stamparla a video
 * @author barnap
 * @deprecated
 */
public class SvmPredictNew{
	BufferedReader input;
	
	public SvmPredictNew(char[] input_file){
		input = new BufferedReader(new CharArrayReader(input_file));
	}
	
	/**
	 * NO COMMENT
	 * @param s
	 * @return
	 */
	private static double atof(String s)
	{
		return Double.valueOf(s).doubleValue();
	}

	private static int atoi(String s)
	{
		return Integer.parseInt(s);
	}

	public SvmTestResult predict(svm_model model, int predict_probability) throws IOException
	{
		
		int correct = 0;
		int total = 0;
		double error = 0;
		double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
		Float sumMre = 0f;
		List<Float> mre = new LinkedList<Float>();
		List<Float> emre = new LinkedList<Float>();
		
		int svm_type=svm.svm_get_svm_type(model);
		int nr_class=svm.svm_get_nr_class(model);
		double[] prob_estimates=null;

		if(predict_probability == 1)
		{
			if(svm_type == svm_parameter.EPSILON_SVR ||
			   svm_type == svm_parameter.NU_SVR)
			{
				//System.out.print("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="+svm.svm_get_svr_probability(model)+"\n");
			}
			else
			{
				int[] labels=new int[nr_class];
				svm.svm_get_labels(model,labels);
				prob_estimates = new double[nr_class];
//				output.writeBytes("labels");
//				for(int j=0;j<nr_class;j++)
//					output.writeBytes(" "+labels[j]);
//				output.writeBytes("\n");
			}
		}
		while(true)
		{
			String line = input.readLine();
			if(line == null) break;

			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

			double target = atof(st.nextToken());
			int m = st.countTokens()/2;
			svm_node[] x = new svm_node[m];
			for(int j=0;j<m;j++)
			{
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}

			double v;
			if (predict_probability==1 && (svm_type==svm_parameter.C_SVC || svm_type==svm_parameter.NU_SVC))
			{
				v = svm.svm_predict_probability(model,x,prob_estimates);
//				output.writeBytes(v+" ");
//				for(int j=0;j<nr_class;j++)
//					output.writeBytes(prob_estimates[j]+" ");
//				output.writeBytes("\n");
			}
			else
			{
				v = svm.svm_predict(model,x);
//				output.writeBytes(v+"\n");
			}

			if(v == target) ++correct;
			error += (v-target)*(v-target);
			sumv += v;
			sumy += target;
			sumvv += v*v;
			sumyy += target*target;
			sumvy += v*target;
			++total;
			
			/* calcolo MMRE */
			//Float mre = new Float(Math.abs(v - target)/target);
			//sumMre += mre;
			mre.add(new Float(Math.abs(v - target)/target));
			emre.add(new Float(Math.abs(v - target)/v));
			
		}
//		if(svm_type == svm_parameter.EPSILON_SVR ||
//		   svm_type == svm_parameter.NU_SVR)
//		{
//			System.out.print("Mean squared error = "+error/total+" (regression)\n");
//			System.out.print("Squared correlation coefficient = "+
//				 ((total*sumvy-sumv*sumy)*(total*sumvy-sumv*sumy))/
//				 ((total*sumvv-sumv*sumv)*(total*sumyy-sumy*sumy))+
//				 " (regression)\n");
//		}
//		else
//			System.out.print("Accuracy = "+(double)correct/total*100+
//				 "% ("+correct+"/"+total+") (classification)\n");
		
//		return new SvmTestResult(calculateMedia(mre), calculateMediana(mre), calculateMedia(emre), calculateMediana(emre));
		return new SvmTestResult("","");
	}
	
	private Float calculateMedia(List<Float> numbers){
		Float total=0f;
		Float sum=0f;
		
		for(Float num:numbers){
			sum+=num;
			total++;
		}
		
		return new Float(sum/total);
	}
	
	private Float calculateMediana(List<Float> numbers){
		Collections.sort(numbers);
		return numbers.get(numbers.size()/2);
	}

}

