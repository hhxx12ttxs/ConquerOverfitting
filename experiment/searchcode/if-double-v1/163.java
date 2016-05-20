/**
 * Classe statica che definisce le operazioni fondamentali da compiere sui segnali discreti come 
 * le operazioni di convoluzione e filtraggio
 * 
 * Nota: la convoluzione ďż˝ un operazione sovraccarica, invocarla opportunamente
 * @author Antonio Tedeschi
 *
 */
package it.sp4te.signalprocessing;

import it.sp4te.domain.*;
import it.sp4te.util.Utils;


public class SignalProcessor {

	/**
	 * NB La funzione length degli array restuisce la lunghezza dell'array che non 
	 * coincide con la numerazione degli indici!
	 * pertanto per applicare correttamente la formula della convoluzione Ă¨ necessario prestare un po' 
	 * di attenzione agli indici con cui andiamo a lavorare per definire l'indice j per il ciclo for 
	 * della traslazione temporale in maniera dinamica in modo da saltare i casi in cui l'indice 
	 * j sia maggiore delle dimensione delle sequenze
	 *
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static double[] convoluzione(double[] v1, double[] v2){

		//definizione della lunghezza del vettore finale contenente i risultati della convoluzione
		int finalLength = v1.length + v2.length-1;
		double[] result = new double[finalLength];

		//inizializzazione delle variabili temporali
		int upperBound = 0;
		int lowerBound = 0;

		//calcolo della convoluzione
		for(int k=0; k<finalLength; k++) {
			upperBound = Math.min(k, v2.length-1);
			lowerBound = Math.max(0, k - v1.length+1);
			for(int j=lowerBound; j<=upperBound; j++)
				result[k] += (v1[k-j]*v2[j]);
		}
		return result;

	}

	/**
	 * Convoluzione tra due vettori di numeri complessi
	 *  
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static Complex[] convoluzione(Complex[] v1, Complex[] v2){

		//definizione della lunghezza del vettore finale contenente i risultati della convoluzione
		int finalLength = v1.length + v2.length-1;
		Complex[] result = new Complex[finalLength];

		//inizializzazione delle variabili temporali
		int upperBound = 0;
		int lowerBound = 0;

		//calcolo della convoluzione
		for(int k=0; k<finalLength; k++) {
			upperBound = Math.min(k, v2.length-1);
			lowerBound = Math.max(0, k - v1.length+1);
			result[k] = new Complex(0,0);
			for(int j=lowerBound; j<=upperBound; j++)
				result[k] = result[k].somma(v1[k-j].prodotto(v2[j]));			
		}
		return result;

	}

	/**
	 * Calcola il valore della sinc nel punto x.
	 * Se x=0 allora torna 1
	 * Nota: sinc(x) = (sin(pi*x))/(pi*x)
	 * @param x
	 * @return il valore della sinc per il punto x
	 */
	public static double sinc(double n, double band){
		double res = 0;
		if(n==0)
			res = 1;
		else if(n % (1/band) == 0)
			return 0;
		else 
			res = (Math.sin(Math.PI*band*n))/(Math.PI*band*n);

			return res;
	}

	/**
	 * Crea un nuovo segnale rappresentante il filtro passa-basso
	 * NOTA BENE: il numero di campioni che deve essere passato deve essere dispari
	 * Ottimizzare il metodo come richiesto nell'homework
	 * 
	 * @param band
	 * @param numCampioni
	 * @return Segnale discreto
	 */
	private static Signal lowPassFilter(double band) {

		double fs = 2 * band;
		double tc = 1/fs;
		int numCampioni;
		int lunghezza = (int)(tc*10);
		if(lunghezza%2 == 0)
			numCampioni = lunghezza - 1;
		else numCampioni = lunghezza;
		Complex[] values = new Complex[numCampioni];
		int simmetria = numCampioni / 2;

		for(int n = - simmetria; n <= simmetria; n++){
			double realval = 2* band * sinc(n, 2 * band);
			values[n + simmetria] = new Complex(realval, 0);
		}

		Signal lpf = new Signal(values);
		return lpf;
	}

	private static Signal lowPassFilter(double band, double f1) {

        double fs = 2*band;
        double tc = 1/fs;
        int numCampioni;
        int lunghezza = (int)(tc*10);
		if(lunghezza%2 == 0)
			numCampioni = lunghezza - 1;
		else numCampioni = lunghezza;
        Complex[] values = new Complex[numCampioni];
        int simmetria = (numCampioni) / 2;

        for(int n = - simmetria; n <= simmetria; n++){
                double realval = f1*2* band * sinc(n, 2 * band);
                values[n + simmetria] = new Complex(realval, 0);
        }

        Signal lpf = new Signal(values);

        return lpf;
	}

	/**
	 * @param freq frequenza centrale
	 * @param band larghezza di banda centrata in freq
	 * @return segnale filtrato all'interno di band
	 */
	private static Signal bandPassFilter(double freq, double band) {
		double fs = 2 * band;
		double tc = 1/fs;
		int numCampioni;
		int fattore = (int)(tc*10);
		if(fattore%2 == 0)
			numCampioni = fattore - 1;
		else numCampioni = fattore;
		Complex values[] = new Complex[numCampioni];
		int simmetria = numCampioni/2;
		for (int n = -simmetria; n <= simmetria; n++) {
			double res = fs*sinc(n,2*fs) * 2*Math.cos(2*Math.PI*freq*n);
			Complex c = new Complex(res,0);
			values[n+simmetria] = c;
		}
		return new Signal(values);
	}

	/**
	 * Operazione di convoluzione fra segnali:
	 * implementa un'operazione di convoluzione discreta fra due segnali passati come parametro.
	 * Presuppone che il segnale d'ingresso abbia parte reale e immaginaria non nulle 
	 * e che il filtro abbia solo parte reale.
	 * @param segnaleIn
	 * @param rispImpulsivaFiltro
	 * @return
	 */
	public static Signal convoluzione(Signal segnaleIn, Signal rispImpulsivaFiltro){

		Complex[] values = convoluzione(segnaleIn.getValues(), rispImpulsivaFiltro.getValues());
		Signal signal = new Signal(values);

		return signal;
	}

	public static Signal espansione(Signal segnaleIn, int F1){

        Complex[] sequenzaIn = segnaleIn.values;
        Complex[] espansa = new Complex[sequenzaIn.length * F1];
        int j=0;
        for (int i = 0; i < espansa.length; i++){
                if(i%F1==0){
                        espansa[i]=sequenzaIn[j];
                        j++;
                }else{
                        espansa[i] = new Complex(0,0);
                }
        }

        return new Signal(espansa) ;
	}

	public static Signal interpolazione(Signal signalIn, int F1){

         double band = 1/(2.0*F1);
         Signal lpf = lowPassFilter(band, F1);
         Signal interpolato = convoluzione(signalIn, lpf);
         Complex[] val = new Complex[signalIn.getLength()];
         int n = (lpf.getLength() - 1)/2;
         int j = 0;

         for (int i = n; i < interpolato.getLength() - n; i++){
                 val[j] = interpolato.values[i];
                 j++;
         } 

         return new Signal(val);

 }

	public static Signal decimazione(Signal in, int F2) {
		Complex[] vectorIn = in.getValues();
		Complex[] vectorDecimato = new Complex[vectorIn.length/F2];

		int j = 0;
		for (int i = 0; i < vectorIn.length; i++) {
			if(i % F2 ==0 && j <vectorDecimato.length) {
				vectorDecimato[j] = vectorIn[i];
				j++;
			}

		}
		return new Signal(vectorDecimato);
	}

	public static Signal cambioTassoCampionamento(int T1, int T2, Signal signalIN) {
		int F1 = calcolaFattori(T1, T2)[0];
		int F2 = calcolaFattori(T1, T2)[1];
		Signal res = null;
		if (F1 == 1)
			res = signalIN;
		else {
			res = espansione(signalIN,F1);
			res = interpolazione(res,F1);
		}
		if (F2 == 1)
			return res;
		res = decimazione(res,F2);
		return res;
	}

	public static int[] calcolaFattori(int T1, int T2) {
		int[] res = new int[2];
		int mcd = Utils.mcd(T1, T2);
		int F1 = T1/mcd;
		int F2 = T2/mcd;
		res[0] = F1;
		res[1] = F2;
		return res;
	}
	
	public static Signal selettoreCanale(double deltaF, Signal signalIn){
		
	    Complex[] valuesIn = signalIn.getValues();
	    Complex[] valuesOut = new Complex[valuesIn.length];
	    
	    for(int i = 0; i < valuesIn.length; i++){
	    	valuesOut[i]=valuesIn[i].prodottoScalare(Math.pow(Math.E, 2*Math.PI*deltaF*i));
	    }
	    
	    Signal signalOut = new Signal(valuesOut);
	    return signalOut;
	}
	
	public static Complex[] shift(Signal signalIn){
		Complex[] in = signalIn.getValues();
		Complex ultimo = in[in.length - 1];
		Complex[] shifted = new Complex[in.length];

		for (int i = in.length -2; i>= 0; i--) {
			shifted[i + 1] = in[i];
		}		
		
		shifted[0] = ultimo;

		return shifted;
	}
	
	public static Signal rapportoIncrementale(Signal signalIn){
		Complex[] in = signalIn.getValues();
		Complex[] shifted = shift(signalIn);
		Complex[] res = new Complex[in.length];
		
		for(int i = 0; i<in.length; i++){
			res[i] = in[i].prodotto(shifted[i].coniugato());
		}
		
		return new Signal(res);
	}
	
	public static double[] calcoloFrequenze(Signal signalIn){
		Complex[] in = signalIn.getValues();
		double[] out = new double[in.length];
		for(int i = 0; i<in.length; i++){
			out[i] = Math.atan(in[i].getImmaginaria()/in[i].getReale());
		}
		
		return out;
	}
	
	public static double[] demodulatore(Signal signalIn){
		return calcoloFrequenze(rapportoIncrementale(signalIn));
	}
	
	public static Signal DDC(Signal signalIn, double band, double deltaF, int T1, int T2){
		Signal temp = selettoreCanale(deltaF, signalIn);
		temp = convoluzione(temp, lowPassFilter(band));
		temp = cambioTassoCampionamento(T1, T2, temp);
		return temp;
	}

	public static void main(String[] args){

		// Esempio convoluzione tra reali
		double[] v1 = {3,2,1};
		double[] v2 = {1,1,2,1};

		double[] v3 = SignalProcessor.convoluzione(v1, v2);
		for(int i= 0; i< v3.length;i++)
			System.out.println(v3[i]);

		// esempio convoluzione tra Complessi		
		Complex[] vet1 = {new Complex(3,0), new Complex(2,0), new Complex(1,0)};
		Complex[] vet2 = {new Complex(1,0), new Complex(2,0), new Complex(1,0), new Complex(1,0)};

		Complex[] vet3 = SignalProcessor.convoluzione(vet1, vet2);
		System.out.println("\n----Convoluzione vet1 vet2\n");
		for(int i= 0; i< vet3.length;i++)
			System.out.println(vet3[i].toString());

		//esempio di filtraggio (convoluzione tra un segnale e il filtro passa-basso)
		Signal lpf = lowPassFilter(0.5);
		System.out.println("\n----PassaBasso\n");
		System.out.println("Numero campioni LPF: "+lpf.getLength());
		Signal s = new Signal(vet3);
		Signal filtrato = SignalProcessor.convoluzione(s, lpf);
		System.out.println(filtrato.toString());

		Signal bpf = bandPassFilter(0.5, 0.3);
		System.out.println("\n----PassaBanda\n");
		System.out.println("Numero campioni BPF: "+bpf.getLength());
		Signal filtratoBPF = SignalProcessor.convoluzione(s, bpf);
		System.out.println(filtratoBPF.toString());
		System.out.println(Math.cos(2*Math.PI*0.5));

		//Verifica calcolo fattori
		System.out.println("\n-----Verifica fattori\n");
		int vect[] = calcolaFattori(18, 24);
		System.out.println("F1: "+vect[0]+"\nF2: "+vect[1]);

		//Verifica cambio tasso campionamento
		System.out.println("\n-----Cambio tasso\n");
		Signal s1 = new Signal(vet2);
		Signal s2 = cambioTassoCampionamento(18, 24, s1);
		System.out.println(s1.toString() + "\n\n");
		System.out.println(s2.toString());
		
		//Modificare opportunamente le stringhe a seconda delle directory di lettura e scrittura file
		Utils.scriviCampioni("/home/demodulatore_out.txt", demodulatore(Utils.leggiCampioni("/home/cristiano/Documenti/CBB_FM.txt")));
		
		
		
	}
}
