package math.builder;
import math.complex.ComplexNumber;

/**
 * Created with IntelliJ IDEA.
 * User: Fora
 * Date: 18.01.13
 * Time: 19:39
 * To change this template use File | Settings | File Templates.
 */
public class VGComplexNumberBuilder {
    public VGComplexNumberBuilder(){}
    ////

    public static ComplexNumber parse(String snumber){
        //Create two string variables for real and imaginary parts of a complex number
        String sreal=new String();
        String simaginary=new String();

        //Create variable index
        int index=0;
        //Create a parameter to switch from one case(imaginary+real) to another(real+another)
        int parameter=100;
        //Seach for the number of index, where we have character 'i' and imaginary part is on the second place

        for (int i=1; i<snumber.length(); i++){
            if (snumber.charAt(i)=='i'){
                //Give to variable index magnitude of index of element 'i'
                 index=i;
            }
            //Number five we have from: -(i*(-1)+realpart; five is maximum position of '-' or'+' in imaginary part;
            if (snumber.charAt(i)=='+'||snumber.charAt(i)=='-'){
                parameter=i;

            }


        }


        //When variable index is index of 'i'
        //Case real+imaginary and i is on the first place in imaginary part
        if (index>parameter&&index<snumber.length()-1){
            if (snumber.charAt(index) == 'i'){
                //For-loop from 0 to (index-1)
                for (int j=0; j<index-1; j++){
                    // skip indexes with value of '(' Đžr ')'
                    if (snumber.charAt(j)!=')'&&snumber.charAt(j)!='('){
                        // For all other cases add to string variable sreal,
                        // which introduce real part of complex number
                        //character of snumber

                        sreal+=snumber.charAt(j);
                    }


                }

            }
            //multiply imaginary part by -1 if before 'i' stays '-'
            if (snumber.charAt(index-1)=='-'){
                simaginary+="-";}
            //For-loop from element, which stays after 'i', to the end of snumber
            for (int k=index+2; k<snumber.length(); k++){
                //skip indexes with value of '(' Đžr ')'
                if (snumber.charAt(k)!=')'&& snumber.charAt(k)!='('){
                    // For all other cases add to string variable simaginary,
                    // which introduce imaginary part of complex number
                    //character of snumber
                    simaginary+=snumber.charAt(k);
                }

            }
        }
        if (index>parameter&&index>snumber.length()-2){
            for (int n=0; n<parameter; n++ ){
                if (snumber.charAt(n)!=')'&&snumber.charAt(n)!='('){
                    // For all other cases add to string variable sreal,
                    // which introduce real part of complex number
                    //character of snumber
                    sreal+=snumber.charAt(n);
                }
            }
            for (int m=parameter; m<snumber.length();m++){
                if (snumber.charAt(m)!=')'&& snumber.charAt(m)!='('&&snumber.charAt(m)!='*'&&snumber.charAt(m)!='i'){
                    // For all other cases add to string variable simaginary,
                    // which introduce imaginary part of complex number
                    //character of snumber
                    simaginary+=snumber.charAt(m);
                }

            }
        }
        ///Case imaginary+real: i*a+b
        if (index<parameter/*index==0||index==1*/){
            for (int b=0; b<parameter; b++){
                if (snumber.charAt(b)!=')'&& snumber.charAt(b)!='('&&snumber.charAt(b)!='*'&&snumber.charAt(b)!='i'){
                    // For all other cases add to string variable simaginary,
                    // which introduce imaginary part of complex number
                    //character of snumber
                    simaginary+=snumber.charAt(b);
                }
            }
            for (int a=parameter; a<snumber.length(); a++){
                if (snumber.charAt(a)!=')'&&snumber.charAt(a)!='('){
                    // For all other cases add to string variable sreal,
                    // which introduce real part of complex number
                    //character of snumber
                    sreal+=snumber.charAt(a);
                }
            }
        }

        //////
        return new ComplexNumber(Double.parseDouble(sreal), Double.parseDouble(simaginary));
    }



}

