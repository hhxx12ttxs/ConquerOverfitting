/**
 * Created by davidwang on 2/4/15.
 */
public class FooCorporation {


    public static void main(String[] args){

        compute(7.5, 35);
        compute(8.2, 47);
        compute(10.00, 73);

    }

    public static double compute(double pay, int hours) {

        if (hours <= 40){

            double result = 0;
            for (int i = 0; i < hours; i++){

                result = result + pay;


            }
            System.out.println(result);
            return result;

        }

        else if (hours > 40 && hours <= 60){

            double result = 40 * pay;
             for (int i = 41; i <= hours; i++){

                result = result + pay * 1.5;

            }
            System.out.println(result);
            return result;


        }


        System.out.println("Too Many Hours!");
        throw new IllegalArgumentException();

    }




}

