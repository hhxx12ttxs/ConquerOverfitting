private Double length;
private Double width;

public Rectangle(Double length, Double width) {
this.setLength(length);
public Double getArea() {
if(length == null || width == null || length.doubleValue() < 0.0 || width.doubleValue() < 0.0)

