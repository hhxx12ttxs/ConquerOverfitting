import java.awt.Color;

public class Mandelbrot
{
private static int mand(Complex z0, int max) {
Complex z = z0;
for (int t = 0; t<max; t++) {
if (z.abs() > 2.0) return t;

