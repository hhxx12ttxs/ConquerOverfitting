package db4ounit.extensions.util;

/**
* @exclude
*/
public class Binary {

public static long longForBits(long bits){
return (long) ((Math.pow(2, bits)) - 1);
}

public static int numberOfBits(long l){
if(l < 0){

