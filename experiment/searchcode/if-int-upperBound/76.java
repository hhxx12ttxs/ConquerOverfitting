
public class MemoryBlock {

int lowBound;
int upperBound;
int currentLowBound;

public MemoryBlock(){
public void setCurrentBound(int c){
currentLowBound += c;
}

public int checkFit(int num){
if(currentLowBound == upperBound){

