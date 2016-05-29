public class GammaCorrection implements PixelProcessor<Float> {

protected double gamma;

/**
* 构造函数
*/
public GammaCorrection() {
this.gamma = 0.2;
}

public GammaCorrection(double gamma) {
this.gamma = gamma;
}

@Override
public Float processPixel(Float pixel) {

