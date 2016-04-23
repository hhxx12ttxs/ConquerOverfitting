package trussoptimizater.Truss;

import java.io.*;
import java.io.ObjectOutputStream;


/**
 * This class is made up of random static methods which are of general use to many classes.
 * @author Chris
 */
public class Utils {


    /**
     * This method uses serilization to write objects to a specific file path.
     * Note also that multiple objects can be written by a filepath by just encapsulating
     * them in an Array/ArrayList etc.
     *
     * @param filename The absolute file path where myobject is to be written to
     * @param myobject Data that is to be written at the absolute path "Filename"
     */
    public static void writeObjectToFile(String filename, Object myobject) {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            if (filename.endsWith(".ser")) {
                System.out.println("Saving too " + filename);
                fos = new FileOutputStream(filename);
            } else {
                System.out.println("Saving too " + filename + ".ser");
                fos = new FileOutputStream(filename + ".ser");
            }

            out = new ObjectOutputStream(fos);
            out.writeObject(myobject);
            out.close();
        } catch (IOException ex) {
            System.out.println("Writing to file ERROR: " + ex);
        }
    }//end of method


    /**
     * This method uses serilization to read data at a specic path
     * @param filename the absolute path where the object to be read is
     * @return data in the form of an object at the path "Filename"
     */
    public static Object readObjectfromFile(String filename) {
        Object myObject = null;
        FileInputStream fis = null;
        ObjectInputStream in = null;
        try {
            fis = new FileInputStream(filename);
            in = new ObjectInputStream(fis);
            myObject = in.readObject();
            in.close();
        } catch (IOException ex) {
            System.out.println("Reading from file ERROR: " + ex);
        } catch (ClassNotFoundException ex) {
            System.out.println("Reading from file ERROR: " + ex);
        }
        return myObject;
    }//end of method


    /**
     *
     * @param n
     * @param r
     * @return C(n,r) where
     * <p>
     * C(n,r) = n! / r! (n - r)!
     * </p>
     */
    public static double combination(int n, int r){
        return (Utils.factorial(n)  /   (Utils.factorial(r)* Utils.factorial(n-r)));
    }        

    /**
     * Find the factorial of a number
     * <p>
     * For example  factorial(5) = 5*4*3*2*1 = 120
     * </p>
     *
     * @param fac
     * @return factorial of fac
     */
    public static double factorial(int fac){
        double result =1;
        for(int i = 1;i<=fac;i++){
            result = result*i;
        }
        return result;
    }

    public static void printArray(Object[][] arr){
        for(int i =0;i<arr.length;i++){
            for(int j =0;j<arr[0].length;j++){
                System.out.print(arr[i][j]);
                if(j<arr[0].length-1){
                    System.out.print(",");
                }
            }
            System.out.println("");
        }
    }

    
    

}


