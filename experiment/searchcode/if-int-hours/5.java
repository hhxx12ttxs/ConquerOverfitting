
public class Time implements Comparable<Time> {
int hours;
int minutes;

public Time(int a , int b){
hours = a;
minutes = b;

}
public Time(){
}
@Override
public int compareTo(Time o) {

