
public class Polygon {
public Line[] lns;
double mass, rot, rotV;
public Vector P, rP, C, K; //rP is the initial center of mass relative to the lines.
int area;
Polygon(Line[] lns, double mass, Vector P) { //creates a polygon from an abstract set of lines.

