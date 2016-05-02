package experiment;

import sort.*;
import type.lib.Fraction;

import java.util.Arrays;

public class Experiment {

    static public void main(String[] args){
        int N = 1000;
        long aux;

        /*para evitar errores */
        for(int pow=8; pow<21; pow++){
            System.out.println();
            long[] mTime = new long[N], qTime = new long[N],
                    iTime = new long[N], bTime = new long[N];
            long[] mComp = new long[N], qComp = new long[N],
                    iComp = new long[N], bComp = new long[N];
            long start, end, time;

            Fraction mstd1 = new Fraction(1,1), qstd1 = new Fraction(1,1), istd1 = new Fraction(1,1), bstd1 = new Fraction(1,1);
            Fraction mstd2 = new Fraction(1,1), qstd2 = new Fraction(1,1), istd2 = new Fraction(1,1), bstd2 = new Fraction(1,1);

            int a1 = 0, a2= 0, a3= 0,a4 = 0, a5 = 0, a6 = 0, a7 = 0, a8 = 0;

            Fraction error = new Fraction(5,100);

            for(int i=0; i<N+3; i++){
                int[] A = ArrayGenerator.random((int) Math.pow(2, pow));
                /* int[] A = ArrayGenerator.almostSorted((int) Math.pow(2, pow));  */


                /*Prueba de tiempo Quicksort*/
                if(error.compareTo(qstd1) == -1){

                    start = System.nanoTime();
                    Quicksort.sort(Arrays.copyOf(A,A.length));
                    end = System.nanoTime();
                    time = end - start;
                    if(i>2) qTime[i-3] = time;

                    if(i>5){
                        long prom = mean(qTime,i-3);
                        qstd1 = new Fraction(standardDeviation(qTime,i-3,prom),prom);
                    }
                }
                else{
                    if(a1==0){
                        a1++;
                        aux = mean(qTime,i-4);
                        System.out.println("Prueba tiempo Quicksort. n°elementos =" + Math.pow(2, pow)+ " N="+N+", i="+(i-3)+", error = "+(new Fraction(standardDeviation(qTime,i-4,aux),aux)).toString()+" time= "+ aux );
                    }
                    }


                /*Prueba de Comparaciones Quicksort*/
                if(error.compareTo(qstd2) == -1){
                    Quicksort2.sort(Arrays.copyOf(A,A.length));
                    if(i>2) qComp[i-3] = Quicksort2.comp;
                    Quicksort2.comp = 0;

                    if(i>5) {
                        long prom = mean(qComp,i-3);
                        qstd2 = new Fraction(standardDeviation(qComp,i-3,prom),prom);}
                    }
                 else {
                    if(a2==0){
                        a2++;
                        aux = mean(qComp,i-4);
                        System.out.println("Prueba comparaciones Quicksort. n°elementos =" + Math.pow(2, pow)+ "  N="+N+", i="+(i-3)+", error = "+(new Fraction(standardDeviation(qComp,i-4,aux),aux)).toString()+" comp= "+ aux);}
                }



                /*Prueba de tiempo Mergesort*/
                if(error.compareTo(mstd1) == -1){
                    start = System.nanoTime();
                    Mergesort.sort(Arrays.copyOf(A,A.length));
                    end = System.nanoTime();
                    time = end - start;
                    if(i>2)mTime[i-3] = time;

                    if(i>5){
                        long prom = mean(mTime,i-3);
                        mstd1 = new Fraction(standardDeviation(mTime,i-3,prom),prom);
                    }
                }
                else{
                    if(a3==0){
                        a3++;
                        aux = mean(mTime,i-4);
                        System.out.println("Prueba tiempo Mergesort. n°elementos =" + Math.pow(2, pow)+ " N="+N+", i="+(i-3)+", error = "+(new Fraction(standardDeviation(mTime,i-4,aux),aux)).toString()+" time= "+ aux); }
                }

                /*Prueba de comparaciones Mergesort*/
                if(error.compareTo(mstd2) == -1){
                    Mergesort2.sort(Arrays.copyOf(A,A.length));
                    if(i>2)mComp[i-3] = Mergesort2.comp;
                    Mergesort2.comp = 0;

                    if(i>5){
                        long prom = mean(mComp,i-3);
                        mstd2 = new Fraction(standardDeviation(mComp,i-3,prom),prom);
                    }
                } else {
                    if(a4==0){
                        a4++;
                        aux = mean(mComp,i-4);
                        System.out.println("Prueba comparaciones Mergesort. n°elementos =" + Math.pow(2, pow)+ " N="+N+", i="+(i-3)+", error = "+(new Fraction(standardDeviation(mComp,i-4,aux),aux)).toString()+" comp= "+ aux);  }
                }

                /*Prueba de tiempo InsertSort*/
                if(error.compareTo(istd1) == -1){
                    start = System.nanoTime();
                    Insertsort.sort(Arrays.copyOf(A,A.length));
                    end = System.nanoTime();
                    time = end - start;
                    if(i>2)iTime[i-3] = time;

                    if(i>5){
                        long prom = mean(iTime,i-3);
                        istd1 = new Fraction(standardDeviation(iTime,i-3,prom),prom);
                    }
                }
                else{
                    if(a5==0){
                        a5++;
                        aux = mean(iTime,i-4);
                        System.out.println("Prueba tiempo Insertsort. n°elementos =" + Math.pow(2, pow)+ "  N="+N+", i="+(i-3)+", error = "+(new Fraction(standardDeviation(iTime,i-4,aux),aux)).toString()+" time= "+ aux);             }
                }

                /*Prueba de Comparaciones InsertSort*/
                if(error.compareTo(istd2) == -1){
                    Insertsort2.sort(Arrays.copyOf(A,A.length));
                    if(i>2)iComp[i-3] = Insertsort2.comp;
                    Insertsort2.comp = 0;

                    if(i>5){
                        long prom = mean(iComp,i-3);
                        istd2 = new Fraction(standardDeviation(iComp,i-3,prom),prom);
                    }
                } else {
                    if(a6==0){
                        a6++;
                        aux = mean(iComp,i-4);
                        System.out.println("Prueba comparaciones Insertsort. n°elementos =" + Math.pow(2, pow)+ " N="+N+", i="+(i-3)+", error = "+(new Fraction(standardDeviation(iComp,i-4,aux),aux)).toString()+" comp= "+ aux);       }
                }



                /*Prueba de tiempo Bubblesort*/
                if(error.compareTo(bstd1) == -1){
                    start = System.currentTimeMillis();
                    Bubblesort.sort(Arrays.copyOf(A,A.length));
                    end = System.currentTimeMillis();
                    time = end - start;
                    if(i>2)bTime[i-3] = time;

                    if(i>5) {
                        long prom = mean(bTime,i-3);
                        bstd1 = new Fraction(standardDeviation(bTime,i-3,prom),prom);
                    }
                }
                else{
                    if(a7==0){
                        a7++;
                        aux = mean(bTime,i-4);
                        System.out.println("Prueba tiempo Bubblesort. n°elementos =" + Math.pow(2, pow)+ " N="+N+", i="+(i-3)+", error = "+(new Fraction(standardDeviation(bTime,i-4,aux),aux)).toString()+" time= "+ aux);               }
                }


                /*Prueba de comparaciones Bubblesort*/
                if(error.compareTo(bstd2) == -1){
                    Bubblesort2.sort(Arrays.copyOf(A,A.length));
                    if(i>2) bComp[i-3] = Bubblesort2.comp;
                    Bubblesort2.comp = 0;

                    if(i>5){
                        long prom = mean(bComp,i-3);
                        bstd2 = new Fraction(standardDeviation(bComp,i-3,prom),prom);
                    }
                } else {
                    if(a8==0){
                        a8++;
                        aux = mean(bComp,i-4);
                        System.out.println("Prueba comparaciones Bubblesort. n°elementos =" + Math.pow(2, pow)+ " N="+N+", i="+(i-3)+", error = "+(new Fraction(standardDeviation(bComp,i-4,aux),aux)).toString()+" comp= "+ aux);         }
                }


            }

            if(a1==0) System.out.println("Prueba tiempo Quicksort. n°elementos =" + Math.pow(2, pow)+ " N="+N+", error ="+(new Fraction(standardDeviation(qTime,N,mean(qTime)),mean(qTime))).toString() +" time= "+ mean(qTime));
            if(a2==0) System.out.println("Prueba comparaciones Quicksort. n°elementos =" + Math.pow(2, pow)+ " N="+N+", error ="+(new Fraction(standardDeviation(qComp,N,mean(qComp)),mean(qComp))).toString() +" comp= "+ mean(qComp));
            if(a3==0) System.out.println("Prueba tiempo Mergesort. n°elementos =" + Math.pow(2, pow)+ " N="+N+" error ="+(new Fraction(standardDeviation(mTime,N,mean(mTime)),mean(mTime))).toString() +" time= "+ mean(mTime));
            if(a4==0) System.out.println("Prueba comparaciones Mergesort. n°elementos =" + Math.pow(2, pow)+ " N="+N+", error ="+(new Fraction(standardDeviation(mComp,N,mean(mComp)),mean(mComp))).toString() +" comp= "+ mean(mComp));
            if(a5==0) System.out.println("Prueba tiempo Insertsort. n°elementos =" + Math.pow(2, pow)+ " N="+N+", error ="+(new Fraction(standardDeviation(iTime,N,mean(iTime)),mean(iTime))).toString() +" time= "+ mean(iTime));
            if(a6==0) System.out.println("Prueba comparaciones Insertsort. n°elementos =" + Math.pow(2, pow)+ " N="+N+", error ="+(new Fraction(standardDeviation(iComp,N,mean(iComp)),mean(iComp))).toString() +" comp= "+ mean(iComp));
            if(a7==0) System.out.println("Prueba tiempo Bubblesort. n°elementos =" + Math.pow(2, pow)+ " N="+N+", error ="+(new Fraction(standardDeviation(bTime,N,mean(bTime)),mean(bTime))).toString() +" time= "+ mean(bTime));
            if(a8==0) System.out.println("Prueba comparaciones Bubblesort. n°elementos =" + Math.pow(2, pow)+ " N="+N+", error ="+(new Fraction(standardDeviation(bComp,N,mean(bComp)),mean(bComp))).toString() +" comp= "+ mean(bComp));

        }

        

    }



    private static long standardDeviation(long[] a, int n, long prom){
        if(n == 0) return 1;
        long aux = 0L;
        for(int i = 0; i < n ; i++){
            aux+= (long)Math.pow((double)(a[i]-prom),2);
        }
        return (long)Math.sqrt((double)(aux/n));
    }



    private static long mean(long[] a, int n){
        if(n==1) return a[0];
        long sum = 0L;

        if((n%2) == 0){
            for(int i = 0;i<n;i=i+2){
                sum+=mean(Arrays.copyOfRange(a,i,i+1));
            }
            return 2*sum/n;
        }
        else{
            for(int i = 0;i<n-1;i=i+2){
                sum+=mean(Arrays.copyOfRange(a,i,i+1));
            }
            return ((2*sum/(n-1))+a[n-1])/2;

        }

    }

    private static long mean(long[]a){
        long sum = 0L;
        for(int i = 0 ; i <a.length;i++){
            sum += a[i];
        }
        return sum/(a.length);
    }

}

