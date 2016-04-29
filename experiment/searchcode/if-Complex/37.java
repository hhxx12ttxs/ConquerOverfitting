package FFT;

import java.util.*;

public class FFT {
	public final static double PI = 3.141592;
	int n;
	complex complex;
	ArrayList<complex> complex_list;
	
	FFT(ArrayList<complex> cl, complex c){
		this.n = cl.size();
		this.complex = c;
		this.complex_list = cl;
		
		// if the number of coefficients is not the power of 2, add 0s
		if(log2(n) != (int)(log2(n))){
			for(int i=this.n; i<Math.pow(2, Math.ceil(log2(n))); i++){
				this.complex_list.add(new complex(0, 0));
			}
		}
		this.n = cl.size();
		
//		e^(2*pi/n) = cos(2*pi/n)+ i*sin(2*pi/n)
		this.complex.setRP(Math.cos(2*PI/this.n));
		this.complex.setIP(Math.sin(2*PI/this.n));
	}
	
	public double log2(double x){
		double b = 2;
		return Math.log(x) / Math.log(b);
	}
	
	public void doFFT(){
//		do the FFT
		if(this.n == 1)
			return;

		ArrayList<complex> even_list = new ArrayList<complex>();
		ArrayList<complex> odd_list = new ArrayList<complex>();
		
//		divide the list to even and odd lists 
		for(int i=0; i<n/2; i++){
			even_list.add(this.complex_list.get(i*2));
			odd_list.add(this.complex_list.get(i*2+1));
		}
		
		FFT even_FFT = new FFT(even_list, complex.multiplyComplex(this.complex, this.complex));
		FFT odd_FFT = new FFT(odd_list, complex.multiplyComplex(this.complex, this.complex));
		
		// do the FFT recursively
		even_FFT.doFFT();
		odd_FFT.doFFT();
		
		complex ww = new complex(1, 0);
		for(int i=0; i<n/2; i++){
			//get w of index i
			complex offset = complex.multiplyComplex(ww, odd_FFT.complex_list.get(i));
			this.complex_list.set(i, complex.addComplex(even_FFT.complex_list.get(i), offset));
			this.complex_list.set(i+n/2, complex.subtractComplex(even_FFT.complex_list.get(i),offset));
			ww = complex.multiplyComplex(ww, this.complex);
		}
	}
	
	public String printComplexList(){
		complex now_complex;
		String result = "";
		for(int i=0; i<this.complex_list.size(); i++){
			now_complex = this.complex_list.get(i);
			now_complex.roundUp();
//			System.out.println(now_complex.getRP()+" "+now_complex.getIP());
			result += String.format("%.6f",now_complex.getRP())+" "+String.format("%.6f",now_complex.getIP());
			result += "\n";
		}
		return result;
	}
}

