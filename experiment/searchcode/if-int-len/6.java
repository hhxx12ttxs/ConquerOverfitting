public class Solution {
public int removeElement(int[] A, int elem){
int len = A.length;

int i = 0;
while( i <= len-1 ){
if( A[i] == elem ){
while( i < len-1 ){
if( A[len-1] == elem ){

