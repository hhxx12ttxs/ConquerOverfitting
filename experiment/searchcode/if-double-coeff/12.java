public double[] Highpass(double[] filter_coeff, double[] image){
double[] pixel_coeff = Downsample(ConvHL(filter_coeff,Lshift(image)));
return pixel_coeff;
}

public double[] Lowpass(double[] filter_coeff, double[] image){

