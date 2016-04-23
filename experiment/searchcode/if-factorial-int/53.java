package org.dipler.traffic;

public class Traffic {
	
	/**
	 * calula la probabilidad de bloqueos seg?n erlang B
	 * @param A intensidad de tr?fico cursado
	 * @param m n?mero de recursos
	 * @return
	 */
	public static double erlangB_pb(double a, int m){
		if(a == 0) return .0;
		double pb = .0;
		double aux = .0;
		for(int i = 0; i <= m; i++){
			aux += Math.pow(a, i) / factorial(i);
		}
		pb = Math.pow(a, m) / factorial(m);
		pb /= aux;
		return pb;
	}
	
	/**
	 * calula el n?mero de circuitos de erlangB, siempre redondea al alza y devuelve el n?mero superior aunque sea por muy poco
	 * @param A intensidad de tr?fico cursado
	 * @param pb probabilidad de bloqueo
	 * @return
	 */
	public static int erlangB_m(double a, double pb){
		if(pb > 1 || pb < 0) return -1;
		int m = 0;
		double actualPb = 0;
		double anteriorPb = 0;
		double aux = .0;
		do{
			anteriorPb = actualPb;
			aux += Math.pow(a, m) / factorial(m);
			actualPb = Math.pow(a, m) / factorial(m);
			actualPb /= aux;
			m++;
		}while(pb < actualPb || pb > anteriorPb);
		
		return --m;
	}
	
	/**
	 * calula el n?mero de circuitos de erlangB, siempre redondea al alza y devuelve el n?mero superior aunque sea por muy poco
	 * @param A intensidad de tr?fico cursado
	 * @param pb probabilidad de bloqueo
	 * @param decimal precisi?n
	 * @return
	 */
	public static double erlangB_m(double a, double pb, int decimal){

		if(pb > 1 || pb < 0) return -1;
		int m = 0;
		double actualPb = 0;
		double anteriorPb = 0;
		double aux = .0;
		do{
			anteriorPb = actualPb;
			aux += Math.pow(a, m) / factorial(m);
			actualPb = Math.pow(a, m) / factorial(m);
			actualPb /= aux;
			actualPb = truncateToDecimal(actualPb, decimal);
			m++;
		}while(pb < actualPb || pb > anteriorPb);
		
		return --m;
	}
	
	/**
	 * Calcula la intensidad de tr?fico de erlang c
	 * @param pb
	 * @param m
	 * @param acuracy
	 * @return
	 */
	public static double erlangB_a(double pb, int m, double acuracy) {
		if(pb > 1 || pb <= 0) return -1; // una probabilidad de bloqueo de 0 es imposible, aunque se puede hacer que tienda a 0
		if(m <= 0) return -1;
		
		double a = acuracy;
		double actualPb = Double.MAX_VALUE;
		double anteriorPb = Double.MAX_VALUE;
		while(pb > actualPb || pb < anteriorPb){
			a += acuracy;
			anteriorPb = actualPb;
			actualPb = erlangB_pb(a, m);
		}
		
		return a;
	}
	
	/**
	 * calcula la probabilidad de bloqueo de erlang c
	 * @param a
	 * @param m
	 * @return
	 */
	public static double erlangC_pb(double a, int m){
		if(a == 0) return .0;
		double pb = .0;
		double aux = .0;
		pb = (Math.pow(a, m)/factorial(m));
		pb *= (m/(m-a));
		for(int i = 0; i < m; i++){
			aux += Math.pow(a, i) / factorial(i);
		}
		pb = pb / (pb + aux);
		if(pb == Double.NaN) return -1.;
		return pb;
	}
	
	/**
	 * calcula el n?mero de circuitos de erlang c
	 * @param a
	 * @param pb
	 * @return
	 */
	public static int erlangC_m(double a, double pb){

		if(pb > 1 || pb < 0) return -1;
		int m = 0;
		double actualPb = 0;
		double anteriorPb = 0;
		double aux = .0;
		double pbAux = .0;
		do{
			anteriorPb = actualPb;
			aux += Math.pow(a, m) / factorial(m);
			pbAux = (Math.pow(a, m)/factorial(m));
			pbAux *= (m/(m-a));
			actualPb = pbAux / (pbAux + aux);
			m++;
		}while(pb < actualPb || pb > anteriorPb);
		
		return --m;
	}
	
	/**
	 * calcula el n?mero de circuitos con erlang c con una precisi?n dada
	 * @param a
	 * @param pb
	 * @param decimal
	 * @return
	 */
	public static int erlangC_m(double a, double pb, int decimal){

		if(pb > 1 || pb < 0) return -1;
		int m = 0;
		double actualPb = 0;
		double anteriorPb = 0;
		double aux = .0;
		double pbAux = .0;
		do{
			anteriorPb = actualPb;
			aux += Math.pow(a, m) / factorial(m);
			pbAux = (Math.pow(a, m)/factorial(m));
			pbAux *= (m/(m-a));
			actualPb = pbAux / (pbAux + aux);
			actualPb = truncateToDecimal(actualPb, decimal);
			m++;
		}while(pb < actualPb || pb > anteriorPb);
		
		return --m;
	}
	
	/**
	 * Calcula la intensidad de tr?fico de erlang c
	 * @param pb
	 * @param m
	 * @param acuracy
	 * @return
	 */
	public static double erlangC_a(double pb, int m, double acuracy) {
		if(pb > 1 || pb < 0) return -1;
		
		double a = acuracy;
		double actualPb = Double.MAX_VALUE;
		double anteriorPb = Double.MAX_VALUE;
		while(pb > actualPb || pb < anteriorPb){
			a += acuracy;
			anteriorPb = actualPb;
			actualPb = erlangC_pb(a, m);
		}
		
		return a;
	}
	
	/**
	 * calcula la probabilidad de bloqueo de engset
	 * @param a
	 * @param s
	 * @param m
	 * @return
	 */
	public static double engset_pb(double a, int s, int m){
		if(s <= m) return 0.;	
	    double step = 0.0005;
	    double diff = 1;
	    double actualValue = -step;
	    double p1 = -step;
	    double lastDiff;
	    double lastValue;
	    do {
	      lastValue = p1;
	      actualValue = actualValue+step;
	      double x = 1;
	      for (int i = 0; i <= m; i++) {
	        x=(s - a * (1 - actualValue)) * x * i / (a * (s - i)) + 1;
	      }
	      p1 = 1/x;
	      lastDiff = diff;
	      diff = Math.abs(actualValue - p1);
	    } while (diff <= lastDiff);
	    return (actualValue - step + lastValue) / 2;
	}
	
	/**
	 * calcula el n?mero de lineas de engset
	 * @param s
	 * @param a
	 * @param pb
	 * @return
	 */
	public static int engset_m(int s, double a, double pb){
		if(a == 0) return 0;
		if(pb == 0) return -1;
		int lines = 0;
		if (a > 0){
			lines = 0;
			do{
				lines++;
			}while (engset_pb(a, s, lines) > pb);
			return lines;
		}else{
			return 0;
		}
	}
	
	/**
	 * calcula la intensidad de tr?fico de engset
	 * @param s
	 * @param m
	 * @param pb
	 * @param accuracy 0.001
	 * @return
	 */
	public static double engset_a(int s, int m, double pb, double accuracy){
	  double bottom, top, a;
	  double pbAux;
	  if ((m < 20) && (engset_pb(0.01,s,m) > pb)){
	    return 0;
	  } else {
	    if (engset_pb(s, s, m) < pb){
	      return s;
	    }
	    top = s;
	    bottom = 0;
	    do{
	      a = (bottom + top)/2;
	      pbAux = engset_pb(a, s, m);
	      if (pbAux > pb){
	        top = a;
	      }else{
	        if (pbAux < (1 - accuracy) * pb){
	          bottom = a;
	        }else{
	          return a;
	        }
	      }
	    }while (true);
	  }
	}
	
	/**
	 * trunca el valor dado a los x decimales
	 * @param value
	 * @param dec
	 * @return
	 */
	public static double truncateToDecimal(double value, int dec){
		double power = Math.pow(10, dec);
		int aux = (int)(value * power);
		return (double)aux/power;
	}
	
	/**
	 * calcula el factorial de un n?mero
	 * @param num
	 * @return
	 */
	public static double factorial(int num){
		double fac = 1.;
		if(num < 0) return num;
		for(int i = num; i > 1; i--){
			fac *= i;
		}
		return fac;
	}
	
	/**
	 * calcula el combinatorio de dos n?meros
	 * @param m
	 * @param n
	 * @return
	 */
	public static double combinatorio(int m, int n) {
	    double resultado = 0;
	 
	    resultado = factorial(m)  /  (factorial(n) * factorial(m - n));
	 
	    return resultado;
	}
}

