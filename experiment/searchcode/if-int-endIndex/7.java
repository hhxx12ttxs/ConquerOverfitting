package chapter3_3;

public class ReorderOddEven {

public int[] modify(int[] A){

if (A == null || A.length == 0) {
return A;
}

int startIndex = 0;
int endIndex = A.length -1;

