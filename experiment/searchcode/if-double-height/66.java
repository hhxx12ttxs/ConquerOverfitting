class Rectangle1 {

private double width;
private double height;

public Rectangle1(double width, double height) {
if (width <= 0 || height <= 0) {
throw new IllegalArgumentException();

