public class TransferFunction {


public static double sigmoid(double x, double threshold, double sharpness){
//max(0,(-(|x|-200)/(100+(|x|-200)^2)^(1/2)) | x € (-infinite,infinite)

if(threshold < 0 || sharpness < 0){

