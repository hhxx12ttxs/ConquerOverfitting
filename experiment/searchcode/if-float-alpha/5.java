public class Rhombus implements Shape {
float a;
float h;
float alpha;

public Rhombus(float a, float alpha, float h) {
@Override
public double area() {
if(alpha!=0) {
alpha=(float)Math.toRadians(alpha);

