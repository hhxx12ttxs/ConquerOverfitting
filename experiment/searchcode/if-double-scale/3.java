SCALE_100000(&quot;1:100000&quot;, 0.01);

private String scale;
private Double rate;

private Scale(String scale, Double rate) {
this.scale = scale;
public Double getRate() {
return rate;
}

public static Double getRateForScale(String scale) {

