
public class LineSegment {
double start;
double end;

public LineSegment(double start, double end){
this.start=start;
this.end=end;
}

boolean isPointOnSegment(double point){
if(start<=point&amp;&amp;point<end){

