int n = Integer.parseInt(args[0]);
double x = .5;
double y = 0;
double len = .5;
double h = len*Math.sin((Math.PI)/3);
public static void drawSierpinski(double x, double y, double len, int n) {
if (n == 0) {
return;
}
filledTriangle(x, y, len);

